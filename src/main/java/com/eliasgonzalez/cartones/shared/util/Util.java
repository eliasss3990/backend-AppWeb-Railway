package com.eliasgonzalez.cartones.shared.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;

import java.util.UUID;

@AllArgsConstructor
@Builder
public class Util {

    private String procesoId;

    public String getProcesoId (){
        return UUID.randomUUID().toString();
    }

    public static String normalize(String s) {
        return s == null ? null : s.trim().toLowerCase().replaceAll("\\s+", "");
    }

    public static boolean isRowEmpty(Row row) {
        int indexNombreVendedor = 0; // Verificamos solo esta columna, ya que las demás si pueden estar nulas o vacías
        Cell c = row.getCell(indexNombreVendedor);
        if (c != null && c.getCellType() != CellType.BLANK) {
            if (c.getCellType() == CellType.STRING && !c.getStringCellValue().isBlank()) return false;
            if (c.getCellType() == CellType.NUMERIC) return false;
            if (c.getCellType() == CellType.BOOLEAN) return false;
            if (c.getCellType() == CellType.FORMULA) return false;
            if (c.getCellType() == CellType.ERROR) return true; // Celdas con error se consideran vacías
        }
        return true; // BLANK, _NONE u otros casos
    }

    public static String getStringCell(Row row, Integer colIdx) {
        if (colIdx == null) return null;
        Cell c = row.getCell(colIdx);
        if (c == null) return null;

        return switch (c.getCellType()) {
            case STRING -> c.getStringCellValue().trim();
            case NUMERIC -> {
                // Formatea valores numéricos para evitar ".0" si son enteros
                if (c.getNumericCellValue() % 1 == 0) {
                    yield String.valueOf((long) c.getNumericCellValue());
                } else {
                    yield String.valueOf(c.getNumericCellValue());
                }
            }
            case BOOLEAN -> String.valueOf(c.getBooleanCellValue());
            case FORMULA -> {
                // Intenta evaluar la fórmula si es posible
                try {
                    yield c.getStringCellValue().trim(); // Podría fallar si es NUMERIC
                } catch (IllegalStateException e) {
                    // Si no es STRING, trata como numérico de fórmula
                    yield String.valueOf((long) c.getNumericCellValue());
                }
            }
            default -> null; // BLANK, ERROR, etc.
        };
    }

    public static Integer getIntCell(Row row, Integer colIdx) {
        if (colIdx == null) return null;
        Cell c = row.getCell(colIdx);
        if (c == null || c.getCellType() == CellType.BLANK) return null;

        try {
            if (c.getCellType() == CellType.NUMERIC) {
                // Acceso directo y conversión segura a Integer/Long para evitar pérdida de precisión
                return (int) Math.round(c.getNumericCellValue());
            }
            // Intenta parsear la cadena si el tipo no es NUMERIC
            String s = getStringCell(row, colIdx);
            return s != null && !s.isBlank() ? Integer.parseInt(s.trim()) : null;

        } catch (NumberFormatException e) {
            // Si falla el parseo de cadena (ej. "12a"), devolvemos null,
            // que luego será capturado por el ExcelValidationService
            return null;
        }
    }

    public static Integer getInicio (Integer finAnterior) {
        return finAnterior + 1;
    }

    public static Integer getFin (Integer inicio, Integer cantidad) {

        return inicio + cantidad - 1;
    }
}
