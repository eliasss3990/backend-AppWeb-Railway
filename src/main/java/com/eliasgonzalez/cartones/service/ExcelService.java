package com.eliasgonzalez.cartones.service;

import com.eliasgonzalez.cartones.dto.VendedorExcelDTO;
import com.eliasgonzalez.cartones.exception.ExcelProcessingException;
import com.eliasgonzalez.cartones.model.Senete;
import com.eliasgonzalez.cartones.model.Telebingo;
import com.eliasgonzalez.cartones.model.Vendedor;
import com.eliasgonzalez.cartones.repository.SeneteRepository;
import com.eliasgonzalez.cartones.repository.TelebingoRepository;
import com.eliasgonzalez.cartones.repository.VendedorRepository;
import com.eliasgonzalez.cartones.util.Util;
import com.eliasgonzalez.cartones.validation.ExcelValidationService;
import lombok.AllArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Servicio encargado de la orquestación (I/O) y persistencia de datos
 * desde un archivo Excel, delegando la validación a ExcelValidationService.
 */
@Service
@AllArgsConstructor
public class ExcelService {

    private final VendedorRepository vendedorRepo;
    private final TelebingoRepository telebingoRepo;
    private final SeneteRepository seneteRepo;
    private final ExcelValidationService validationService; // Inyección de la lógica de validación

    @Transactional
    public void leerExcel(MultipartFile file) {

        List<String> erroresGlobales = new ArrayList<>();
        int filaActual = 1;

        try (InputStream is = file.getInputStream();
             Workbook wb = WorkbookFactory.create(is)) {

            // --- Configuración Inicial ---
            int sheetIndex = wb.getSheetIndex(ExcelEnum.HOJA_SISTEMA_ETIQUETAS.getValue());
            if (sheetIndex < 0) {
                throw new ExcelProcessingException("La hoja de cálculo esperada ('" + ExcelEnum.HOJA_SISTEMA_ETIQUETAS.getValue() + "') no fue encontrada.", List.of());
            }
            Sheet sheet = wb.getSheetAt(sheetIndex);
            Iterator<Row> rows = sheet.iterator();
            if (!rows.hasNext()) return;

            // Mapeo de encabezados
            Row header = rows.next();
            Map<String, Integer> idx = new HashMap<>();
            for (Cell c : header) {
                String name = c.getStringCellValue();
                if (name != null) idx.put(Util.normalize(name), c.getColumnIndex());
            }

            // --- INICIO DE LECTURA DE DATOS Y PROCESAMIENTO ---
            while (rows.hasNext()) {
                filaActual++;
                Row r = rows.next();
                if (Util.isRowEmpty(r)) continue;

                try {
                    // 1. LECTURA y MAPEO al DTO Intermedio
                    VendedorExcelDTO dto = new VendedorExcelDTO(
                            filaActual,
                            Util.getStringCell(r, idx.get(ExcelEnum.VENDEDOR.getValue())),
                            Util.getStringCell(r, idx.get(ExcelEnum.SALDO.getValue())),
                            Util.getIntCell(r, idx.get(ExcelEnum.CANT_SENETE.getValue())),
                            Util.getIntCell(r, idx.get(ExcelEnum.RESULT_SENETE.getValue())),
                            Util.getIntCell(r, idx.get(ExcelEnum.CANT_TELEBINGO.getValue())),
                            Util.getIntCell(r, idx.get(ExcelEnum.RESULT_TELEBINGO.getValue()))
                    );

                    // 2. DELEGAR LA VALIDACIÓN
                    List<String> erroresFila = validationService.validate(dto);

                    if (!erroresFila.isEmpty()) {
                        erroresGlobales.addAll(erroresFila);
                        continue; // Si falla, salta a la siguiente fila
                    }

                    // 3. PERSISTENCIA (Solo si la validación pasó)

                    // La validación garantiza que deudaStr es numérico si no está vacío.
                    BigDecimal deuda = (dto.getDeudaStr() == null || dto.getDeudaStr().isBlank()) ?
                            BigDecimal.ZERO : new BigDecimal(dto.getDeudaStr().trim());

                    Vendedor v = new Vendedor();
                    v.setNombre(dto.getNombre());
                    v.setDeuda(deuda);
                    Vendedor savedV = vendedorRepo.save(v);

                    // Guardado condicional de Senete (la validación asegura que ambos campos existen si hay datos)
                    if (dto.getCantidadSenete() != null && dto.getResultadoSenete() != null) {
                        Senete s = new Senete();
                        s.setVendedor(savedV);
                        s.setCantidadSenete(dto.getCantidadSenete());
                        s.setResultadoSenete(dto.getResultadoSenete());
                        seneteRepo.save(s);
                    }

                    // Guardado condicional de Telebingo
                    if (dto.getCantidadTelebingo() != null && dto.getResultadoTelebingo() != null) {
                        Telebingo t = new Telebingo();
                        t.setVendedor(savedV);
                        t.setCantidadTelebingo(dto.getCantidadTelebingo());
                        t.setResultadoTelebingo(dto.getResultadoTelebingo());
                        telebingoRepo.save(t);
                    }

                } catch (Exception e) {
                    // Captura errores inesperados (ej. error de BD, error de Hibernate, etc.)
                    erroresGlobales.add(String.format("Fila %d: ERROR CRÍTICO AL PROCESAR/GUARDAR. Detalle: %s", filaActual, e.getMessage()));
                }
            }

            // --- LANZAMIENTO FINAL DE EXCEPCIÓN ---
            if (!erroresGlobales.isEmpty()) {
                // Lanza la excepción de negocio que será capturada por el @ControllerAdvice (422)
                throw new ExcelProcessingException("El archivo Excel contiene errores de validación. La transacción ha sido revertida.", erroresGlobales);
            }

        } catch (ExcelProcessingException e) {
            // Re-lanzar nuestra excepción de negocio específica
            throw e;
        } catch (Exception e) {
            // Manejo de errores de IO/Workbook (Errores del servidor 500)
            throw new RuntimeException("Fallo crítico al abrir o procesar el archivo Excel. Verifique si el archivo es un formato válido.", e);
        }
    }
}