package org.apache.poi.ss.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaError;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

public class Utils {
    public static void addRow(Sheet sheet, int rownum, Object... values) {
        Row row = sheet.createRow(rownum);
        for (int i = 0; i < values.length; i++) {
            Cell cell = row.createCell(i);
            if (values[i] instanceof Integer) {
                cell.setCellValue((Integer) values[i]);
            } else if (values[i] instanceof FormulaError) {
                cell.setCellErrorValue(((FormulaError)values[i]).getCode());
            } else if (values[i] == null) {
                cell.setBlank();
            } else {
                cell.setCellValue(values[i].toString());
            }
        }
    }

}
