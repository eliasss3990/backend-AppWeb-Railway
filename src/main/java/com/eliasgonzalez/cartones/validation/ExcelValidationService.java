package com.eliasgonzalez.cartones.validation;

import com.eliasgonzalez.cartones.service.ExcelEnum;
import com.eliasgonzalez.cartones.service.Util;
import org.apache.poi.ss.usermodel.Row;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class ExcelValidationService {

    /**
     * Valida una fila individual del Excel contra las reglas de negocio (no nulo, formato).
     * Devuelve una lista de errores. Si la lista está vacía, la fila es válida.
     */
    public List<String> validateRow(Row r, Map<String, Integer> idx, int filaActual) {
        List<String> rowErrors = new ArrayList<>();

        // -----------------------------------------------------------
        // 1. VALIDACIÓN DE CAMPOS OBLIGATORIOS
        // -----------------------------------------------------------

        String nombre = Util.getStringCell(r, idx.get(ExcelEnum.VENDEDOR.getValue()));
        if (nombre == null || nombre.isBlank()) {
            rowErrors.add(String.format("Fila %d: El campo NOMBRE del vendedor no puede estar vacío.", filaActual));
        }

        // Validación de SALDO (Debe ser numérico)
        String deudaStr = Util.getStringCell(r, idx.get(ExcelEnum.SALDO.getValue()));
        if (deudaStr != null && !deudaStr.isBlank()) {
            try {
                // Intenta convertir para verificar el formato, pero no se almacena aquí.
                new BigDecimal(deudaStr.trim());
            } catch (NumberFormatException e) {
                rowErrors.add(String.format("Fila %d: El campo SALDO ('%s') no es un número válido. Use puntos o comas según configuración.", filaActual, deudaStr));
            }
        }

        // -----------------------------------------------------------
        // 2. VALIDACIÓN DE SENETE (Condicional: Ambos o ninguno)
        // -----------------------------------------------------------

        Integer cantidadSenete = Util.getIntCell(r, idx.get(ExcelEnum.CANT_SENETE.getValue()));
        Integer resultadoSenete = Util.getIntCell(r, idx.get(ExcelEnum.RESULT_SENETE.getValue()));

        boolean tieneDatosSenete = cantidadSenete != null || resultadoSenete != null;

        if (tieneDatosSenete) {
            if (cantidadSenete == null) {
                rowErrors.add(String.format("Fila %d: Datos Senete incompletos. Si hay Resultado, CANT_SENETE es obligatorio y no válido.", filaActual));
            }
            if (resultadoSenete == null) {
                rowErrors.add(String.format("Fila %d: Datos Senete incompletos. Si hay Cantidad, RESULT_SENETE es obligatorio y no válido.", filaActual));
            }
        }

        // -----------------------------------------------------------
        // 3. VALIDACIÓN DE TELEBINGO (Condicional: Ambos o ninguno)
        // -----------------------------------------------------------

        Integer cantidadTelebingo = Util.getIntCell(r, idx.get(ExcelEnum.CANT_TELEBINGO.getValue()));
        Integer resultadoTelebingo = Util.getIntCell(r, idx.get(ExcelEnum.RESULT_TELEBINGO.getValue()));

        boolean tieneDatosTelebingo = cantidadTelebingo != null || resultadoTelebingo != null;

        if (tieneDatosTelebingo) {
            if (cantidadTelebingo == null) {
                rowErrors.add(String.format("Fila %d: Datos Telebingo incompletos. Si hay Resultado, CANT_TELEBINGO es obligatorio y no válido.", filaActual));
            }
            if (resultadoTelebingo == null) {
                rowErrors.add(String.format("Fila %d: Datos Telebingo incompletos. Si hay Cantidad, RESULT_TELEBINGO es obligatorio y no válido.", filaActual));
            }
        }

        return rowErrors;
    }
}