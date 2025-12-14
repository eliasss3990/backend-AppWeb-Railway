package com.eliasgonzalez.cartones.service;

import com.eliasgonzalez.cartones.model.Senete;
import com.eliasgonzalez.cartones.model.Telebingo;
import com.eliasgonzalez.cartones.model.Vendedor;
import com.eliasgonzalez.cartones.repository.SeneteRepository;
import com.eliasgonzalez.cartones.repository.TelebingoRepository;
import com.eliasgonzalez.cartones.repository.VendedorRepository;
import com.eliasgonzalez.cartones.exception.ExcelProcessingException;
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

@Service
@AllArgsConstructor
public class ExcelService {

    private final VendedorRepository vendedorRepo;
    private final TelebingoRepository telebingoRepo;
    private final SeneteRepository seneteRepo;

    @Transactional
    public void leerExcel(MultipartFile file) {

        List<String> errores = new ArrayList<>();
        int filaActual = 1;

        try (InputStream is = file.getInputStream();
             Workbook wb = WorkbookFactory.create(is)) {

            Sheet sheet = wb.getSheetAt(wb.getSheetIndex(ExcelEnum.HOJA_SISTEMA_ETIQUETAS.getValue()));
            Iterator<Row> rows = sheet.iterator();
            if (!rows.hasNext()) return;

            // Leer encabezado y mapear nombres normalizados a índices
            Row header = rows.next();
            Map<String, Integer> idx = new HashMap<>();
            for (Cell c : header) {
                String name = c.getStringCellValue();
                if (name != null) idx.put(Util.normalize(name), c.getColumnIndex());
            }

            // --- INICIO DE LECTURA DE DATOS ---
            while (rows.hasNext()) {
                filaActual++;

                try {
                    Row r = rows.next();
                    if (Util.isRowEmpty(r)) continue;

                    // ----------------------------------------
                    // VALIDACIÓN DE NULIDAD PARA VENDEDOR
                    // ----------------------------------------

                    String nombre = Util.getStringCell(r, idx.get(ExcelEnum.VENDEDOR.getValue()));
                    if (nombre == null || nombre.isBlank()) {
                        errores.add(String.format("Fila %d: El NOMBRE del vendedor no puede estar vacío.", filaActual));
                        continue; // Saltar la fila si falta el nombre
                    }

                    String deudaStr = Util.getStringCell(r, idx.get(ExcelEnum.SALDO.getValue()));
                    BigDecimal deuda;

                    try {
                        // Si la celda está vacía se la considera como 0, pero si tiene texto inválido, falla.
                        deuda = (deudaStr == null || deudaStr.isBlank()) ?
                                BigDecimal.ZERO : new BigDecimal(deudaStr.trim());
                    } catch (NumberFormatException e) {
                        errores.add(String.format("Fila %d: El campo SALDO ('%s') no es un número válido.", filaActual, deudaStr));
                        continue; // Saltar la fila si el formato es incorrecto
                    }

                    // --- CREACIÓN Y GUARDADO DE VENDEDOR ---
                    Vendedor v = new Vendedor();
                    v.setNombre(nombre);
                    v.setDeuda(deuda);
                    Vendedor savedV = vendedorRepo.save(v);


                    // ----------------------------------------
                    // VALIDACIÓN DE NULIDAD PARA SENETE (Si existe el registro)
                    // ----------------------------------------
                    Integer cantidadSenete = Util.getIntCell(r, idx.get(ExcelEnum.CANT_SENETE.getValue()));
                    Integer resultadoSenete = Util.getIntCell(r, idx.get(ExcelEnum.RESULT_SENETE.getValue()));

                    // Solo intentar guardar si al menos uno de los campos de Senete existe.
                    if (cantidadSenete != null || resultadoSenete != null) {
                        if (cantidadSenete == null) {
                            errores.add(String.format("Fila %d: Falta CANT_SENETE. Si hay datos de Senete, la cantidad es obligatoria.", filaActual));
                            // NOTA: Podrías usar 'continue' aquí si quieres que la fila entera falle.
                            // Aquí solo se registra el error, pero se guarda Telebingo si existe.
                        }
                        if (resultadoSenete == null) {
                            errores.add(String.format("Fila %d: Falta RESULT_SENETE. Si hay datos de Senete, el resultado es obligatorio.", filaActual));
                        }

                        // Solo guardar si ambos campos son válidos y no nulos
                        if (cantidadSenete != null && resultadoSenete != null) {
                            Senete s = new Senete();
                            s.setVendedor(savedV);
                            s.setCantidadSenete(cantidadSenete);
                            s.setResultadoSenete(resultadoSenete);
                            seneteRepo.save(s);
                        }
                    }

                    // ----------------------------------------
                    // VALIDACIÓN DE NULIDAD PARA TELEBINGO (Si existe el registro)
                    // ----------------------------------------
                    Integer cantidadTelebingo = Util.getIntCell(r, idx.get(ExcelEnum.CANT_TELEBINGO.getValue()));
                    Integer resultadoTelebingo = Util.getIntCell(r, idx.get(ExcelEnum.RESULT_TELEBINGO.getValue()));

                    if (cantidadTelebingo != null || resultadoTelebingo != null) {
                        if (cantidadTelebingo == null) {
                            errores.add(String.format("Fila %d: Falta CANT_TELEBINGO. Si hay datos de Telebingo, la cantidad es obligatoria.", filaActual));
                        }
                        if (resultadoTelebingo == null) {
                            errores.add(String.format("Fila %d: Falta RESULT_TELEBINGO. Si hay datos de Telebingo, el resultado es obligatorio.", filaActual));
                        }

                        if (cantidadTelebingo != null && resultadoTelebingo != null) {
                            Telebingo t = new Telebingo();
                            t.setVendedor(savedV);
                            t.setCantidadTelebingo(cantidadTelebingo);
                            t.setResultadoTelebingo(resultadoTelebingo);
                            telebingoRepo.save(t);
                        }
                    }

                } catch (Exception e) {
                    // Captura cualquier error inesperado dentro del procesamiento de una fila
                    errores.add(String.format("Fila %d: Error inesperado en el procesamiento de la fila. Detalle: %s", filaActual, e.getMessage()));
                }
            }

            // --- LANZAMIENTO FINAL DE EXCEPCIÓN ---
            if (!errores.isEmpty()) {
                // Si hay errores, lanzamos la excepción de negocio que detiene la transacción
                // y que el @ControllerAdvice transformará en 422.
                throw new ExcelProcessingException("El archivo Excel contiene errores de validación.", errores);
            }

        } catch (ExcelProcessingException e) {
            // Re-lanzar nuestra excepción de negocio.
            throw e;
        } catch (Exception e) {
            // Captura errores de IO, problemas con el WorkbookFactory, etc. (Errores del servidor 500)
            errores.add("Error crítico al procesar el archivo o la estructura de la hoja: " + e.getMessage());
            throw new RuntimeException("Fallo crítico al procesar el archivo.", e);
        }
    }
}