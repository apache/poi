package org.apache.poi.ss.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaError;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;

public class Utils {
    public static void addRow(Sheet sheet, int rownum, Object... values) {
        Row row = sheet.createRow(rownum);
        for (int i = 0; i < values.length; i++) {
            Cell cell = row.createCell(i);
            if (values[i] instanceof Integer) {
                cell.setCellValue((Integer) values[i]);
            } else if (values[i] instanceof Double) {
                cell.setCellValue((Double) values[i]);
            } else if (values[i] instanceof Boolean) {
                cell.setCellValue((Boolean) values[i]);
            } else if (values[i] instanceof Calendar) {
                cell.setCellValue((Calendar) values[i]);
            } else if (values[i] instanceof Date) {
                cell.setCellValue((Date) values[i]);
            } else if (values[i] instanceof LocalDate) {
                cell.setCellValue((LocalDate) values[i]);
            } else if (values[i] instanceof LocalDateTime) {
                cell.setCellValue((LocalDateTime) values[i]);
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
