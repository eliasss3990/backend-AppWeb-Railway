package com.eliasgonzalez.cartones.shared.util;

import lombok.AllArgsConstructor;
import org.apache.poi.ss.usermodel.*;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;

@AllArgsConstructor
public class Util {

    public static String normalize(String s) {
        return s == null ? null : s.trim().toLowerCase().replaceAll("\\s+", "");
    }

    public static boolean isRowEmpty(Row row, Integer colIdx, FormulaEvaluator evaluator) {
        if (row == null || colIdx == null) return true;
        Cell c = row.getCell(colIdx);
        if (c == null || c.getCellType() == CellType.BLANK) return true;

        try {
            CellValue cellValue = evaluator.evaluate(c);
            if (cellValue == null) return true;

            return switch (cellValue.getCellType()) {
                case STRING -> cellValue.getStringValue().trim().isBlank();
                case NUMERIC -> false;
                case BOOLEAN -> false;
                case ERROR -> true; // #REF!, #VALUE! se consideran vacíos/inválidos para saltar o fallar después
                default -> true;
            };
        } catch (Exception e) {
            // Si el evaluador falla (ej. fórmula no soportada),
            // NO consideramos la fila vacía para que el validador intente leerla y reporte el error "Fila X: Error..."
            // en lugar de ignorarla silenciosamente o romper todo.
            return false;
        }
    }

    public static String getStringCell(Row row, Integer colIdx, FormulaEvaluator evaluator) {
        if (colIdx == null || row == null) return null;
        Cell c = row.getCell(colIdx);
        if (c == null || c.getCellType() == CellType.BLANK) return null;

        try {
            CellValue cellValue = evaluator.evaluate(c);
            if (cellValue == null) return null;

            return switch (cellValue.getCellType()) {
                case STRING -> cellValue.getStringValue().trim();
                case NUMERIC -> {
                    if (DateUtil.isCellDateFormatted(c)) {
                        // Formateo de fecha
                        yield new SimpleDateFormat("yyyy/MM/dd").format(c.getDateCellValue());
                    }
                    double val = cellValue.getNumberValue();
                    // BigDecimal para limpiar la conversión de Double a String
                    if (val == (long) val) {
                        yield String.valueOf((long) val);
                    } else {
                        yield BigDecimal.valueOf(val).toPlainString(); // Evita notación científica 1.5E2
                    }
                }
                case BOOLEAN -> String.valueOf(cellValue.getBooleanValue());
                case ERROR -> "ERROR_CODIGO_" + cellValue.getErrorValue(); // Retornamos el error para que el validador lo detecte
                default -> null;
            };
        } catch (Exception e) {
            return "ERROR_FORMULA"; // Retorno seguro en caso de fallo de POI
        }
    }

    public static Integer getIntCell(Row row, Integer colIdx, FormulaEvaluator evaluator) {
        if (colIdx == null || row == null) return null;
        Cell c = row.getCell(colIdx);
        if (c == null) return null;

        try {
            CellValue cellValue = evaluator.evaluate(c);
            if (cellValue == null || cellValue.getCellType() == CellType.BLANK) return null;

            if (cellValue.getCellType() == CellType.NUMERIC) {
                return (int) Math.round(cellValue.getNumberValue());
            }

            // Si el resultado de la fórmula es un String que parece número
            if (cellValue.getCellType() == CellType.STRING) {
                return (int) Double.parseDouble(cellValue.getStringValue());
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

}
