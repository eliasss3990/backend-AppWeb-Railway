package com.eliasgonzalez.cartones.service;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;

public class Util {

    protected static String normalize(String s) {
        return s == null ? null : s.trim().toLowerCase().replaceAll("\\s+", "");
    }

    protected static boolean isRowEmpty(Row row) {
        for (Cell c : row) {
            if (c != null && c.getCellType() != CellType.BLANK) {
                if (c.getCellType() == CellType.STRING && !c.getStringCellValue().isBlank()) return false;
                if (c.getCellType() == CellType.NUMERIC) return false;
                if (c.getCellType() == CellType.BOOLEAN) return false;
            }
        }
        return true;
    }

    protected static String getStringCell(Row row, Integer colIdx) {
        if (colIdx == null) return null;
        Cell c = row.getCell(colIdx);
        if (c == null) return null;
        if (c.getCellType() == CellType.STRING) return c.getStringCellValue();
        if (c.getCellType() == CellType.NUMERIC) return String.valueOf(c.getNumericCellValue());
        if (c.getCellType() == CellType.BOOLEAN) return String.valueOf(c.getBooleanCellValue());
        return null;
    }

    protected static Integer getIntCell(Row row, Integer colIdx) {
        String s = getStringCell(row, colIdx);
        if (s == null || s.isBlank()) return null;
        try {
            double d = Double.parseDouble(s);
            return (int) d;
        } catch (NumberFormatException ex) {
            try {
                return Integer.valueOf(s.trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }

    protected static Integer getInicio (Integer finAnterior) {
        return finAnterior + 1;
    }

    protected static Integer getFin (Integer inicio, Integer cantidad) {

        return inicio + cantidad - 1;
    }
}
