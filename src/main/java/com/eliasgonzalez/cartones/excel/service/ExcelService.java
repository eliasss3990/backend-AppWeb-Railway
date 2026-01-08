package com.eliasgonzalez.cartones.excel.service;

import com.eliasgonzalez.cartones.excel.enums.ExcelEnum;
import com.eliasgonzalez.cartones.excel.interfaces.IExcelService;
import com.eliasgonzalez.cartones.shared.exception.FileProcessingException;
import com.eliasgonzalez.cartones.vendedor.dto.VendedorExcelDTO;
import com.eliasgonzalez.cartones.shared.exception.ExcelProcessingException;
import com.eliasgonzalez.cartones.vendedor.entity.Vendedor;
import com.eliasgonzalez.cartones.vendedor.interfaces.VendedorRepository;
import com.eliasgonzalez.cartones.shared.util.Util;
import lombok.AllArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servicio encargado de la orquestación (I/O) y persistencia de datos
 * desde un archivo Excel, aplicando un enfoque de "Todo o Nada".
 */
@Service
@AllArgsConstructor
public class ExcelService implements IExcelService {

    private final VendedorRepository vendedorRepo;
    private final ExcelValidationService validationService;
    private static final Logger logger = LoggerFactory.getLogger(ExcelService.class);

    @Override
    @Transactional // Si ocurre una RuntimeException, se revierte todo
    public void leerExcel(MultipartFile file, LocalDate fechaSorteo) {

        logger.info("Iniciando procesamiento del archivo Excel: {}", file.getOriginalFilename());

        List<String> erroresGlobales = new ArrayList<>();
        List<Vendedor> vendedoresParaGuardar = new ArrayList<>();

        try (InputStream is = file.getInputStream();
             Workbook wb = WorkbookFactory.create(is)) {

            // --- 1. CONFIGURACIÓN INICIAL ---
            int sheetIndex = wb.getSheetIndex(ExcelEnum.HOJA_SISTEMA_ETIQUETAS.getValue());
            if (sheetIndex < 0) {
                throw new ExcelProcessingException("La hoja ('" + ExcelEnum.HOJA_SISTEMA_ETIQUETAS.getValue() + "') no fue encontrada.", List.of());
            }
            Sheet sheet = wb.getSheetAt(sheetIndex);

            Row header = sheet.getRow(0); // Se asume que la primera fila es el encabezado.
            if (header == null) {
                throw new ExcelProcessingException("El archivo Excel está vacío o no tiene encabezados.", List.of());
            }

            Map<String, Integer> idx = new HashMap<>();
            for (Cell c : header) {
                String name = c.getStringCellValue();
                if (name != null) idx.put(Util.normalize(name), c.getColumnIndex());
            }

            /*
             Validar encabezados
             Fuera del for, ya que solo es necesario validarlo una vez.
            */
            validateHeader(idx);

            // --- 2. LECTURA Y VALIDACIÓN ---
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                int filaActual = i + 1;
                Row row = sheet.getRow(i);

                // Si la fila está vacía, se omite y se pasa al siguiente.
                if (Util.isRowEmpty(row)) continue;

                try {
                    // Mapeo al DTO
                    VendedorExcelDTO dto = DTOExcelMapper(idx, row, filaActual);

                    // Delegar validación lógica
                    List<String> erroresFila = validationService.validate(dto);

                    if (!erroresFila.isEmpty()) {
                        erroresGlobales.addAll(erroresFila);
                    } else {
                        // Si la fila es válida, la transformamos en entidad y la guardamos en la lista temporal
                        vendedoresParaGuardar.add(convertToEntity(dto, fechaSorteo));
                    }

                } catch (Exception e) {
                    String errorMessage = String.format("Fila %d: Error inesperado al procesar: %s", filaActual, e.getMessage());
                    logger.error(errorMessage);
                    erroresGlobales.add(errorMessage);
                }
            }

            // --- 3. DECISIÓN FINAL (TODO O NADA) ---
            if (!erroresGlobales.isEmpty()) {
                logger.warn("Se detectaron {} errores. Abortando operación sin guardar nada.", erroresGlobales.size());
                // Lanzar la excepción provoca el Rollback automático de la transacción
                throw new ExcelProcessingException("El archivo Excel contiene errores. No se ha guardado ningún dato.", erroresGlobales);
            }

            // Si se llegó hasta acá, significa que NO hubo errores en ninguna fila
            if (!vendedoresParaGuardar.isEmpty()) {
                vendedorRepo.saveAll(vendedoresParaGuardar);
                logger.info("Se han guardado exitosamente {} registros.", vendedoresParaGuardar.size());
            }

        } catch (ExcelProcessingException | FileProcessingException e) {
            throw e; // Relanzar para que el GlobalExceptionHandler lo maneje
        } catch (Exception e) {
            logger.error("Fallo crítico en el procesamiento del Excel", e);
            throw new RuntimeException("Error al procesar el archivo Excel: " + e.getMessage(), e);
        }
    }

    private static void validateHeader(Map<String, Integer> idx) {
        String[] required = {
                ExcelEnum.VENDEDOR.getValue(),
                ExcelEnum.SALDO.getValue(),
                ExcelEnum.CANT_SENETE.getValue(),
                ExcelEnum.RESULT_SENETE.getValue(),
                ExcelEnum.CANT_TELEBINGO.getValue(),
                ExcelEnum.RESULT_TELEBINGO.getValue()
        };
        List<String> faltantes = new ArrayList<>();
        for (String h : required) {
            if (!idx.containsKey(Util.normalize(h))) faltantes.add(h);
        }
        if (!faltantes.isEmpty()) {
            throw new ExcelProcessingException("Faltan encabezados requeridos: " + faltantes, List.of());
        }
    }

    private static VendedorExcelDTO DTOExcelMapper(Map<String, Integer> idx, Row row, int filaActual) {
        return VendedorExcelDTO.builder()
                .nombre(Util.getStringCell(row, idx.get(Util.normalize(ExcelEnum.VENDEDOR.getValue()))))
                .deudaStr(Util.getStringCell(row, idx.get(Util.normalize(ExcelEnum.SALDO.getValue()))))
                .cantidadSenete(Util.getIntCell(row, idx.get(Util.normalize(ExcelEnum.CANT_SENETE.getValue()))))
                .resultadoSenete(Util.getIntCell(row, idx.get(Util.normalize(ExcelEnum.RESULT_SENETE.getValue()))))
                .cantidadTelebingo(Util.getIntCell(row, idx.get(Util.normalize(ExcelEnum.CANT_TELEBINGO.getValue()))))
                .resultadoTelebingo(Util.getIntCell(row, idx.get(Util.normalize(ExcelEnum.RESULT_TELEBINGO.getValue()))))
                .filaActual(filaActual)
                .build();
    }

    private static Vendedor convertToEntity(VendedorExcelDTO dto, LocalDate fechaSorteo) {
        String deudaStr = dto.getDeudaStr();
        BigDecimal deuda = (deudaStr == null || deudaStr.isBlank()) ?
                BigDecimal.ZERO : new BigDecimal(deudaStr.trim());

        Vendedor v = Vendedor.builder()
                .nombre(dto.getNombre().trim())
                .deuda(deuda)
                .cantidadSenete(dto.getCantidadSenete())
                .resultadoSenete(dto.getResultadoSenete())
                .cantidadTelebingo(dto.getCantidadTelebingo())
                .resultadoTelebingo(dto.getResultadoTelebingo())
                .build();

        if (fechaSorteo != null) {
            v.setFechaSorteo(fechaSorteo);
        }
        return v;
    }
}