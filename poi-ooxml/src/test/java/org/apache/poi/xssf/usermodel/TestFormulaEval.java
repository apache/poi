package org.apache.poi.xssf.usermodel;

import org.apache.poi.ss.usermodel.CellType;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestFormulaEval {
    @Test
    void testCircularRef() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sheet = wb.createSheet();
            XSSFRow row = sheet.createRow(0);
            XSSFCell cell = row.createCell(0);
            cell.setCellFormula("A1");
            XSSFFormulaEvaluator formulaEvaluator = wb.getCreationHelper().createFormulaEvaluator();
            // the following assert should probably be NUMERIC not ERROR (from testing in Excel itself)
            assertEquals(CellType.ERROR, formulaEvaluator.evaluateFormulaCell(cell));

            cell.setCellFormula(null);
            formulaEvaluator.notifyUpdateCell(cell);
            //the following assert should probably be BLANK not ERROR
            assertEquals(CellType.ERROR, cell.getCellType());
        }
    }

    @Test
    void testCircularRef2() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sheet = wb.createSheet();
            XSSFRow row = sheet.createRow(0);
            XSSFCell cell0 = row.createCell(0);
            XSSFCell cell1 = row.createCell(1);
            cell0.setCellFormula("B1");
            cell1.setCellFormula("A1");
            XSSFFormulaEvaluator formulaEvaluator = wb.getCreationHelper().createFormulaEvaluator();
            formulaEvaluator.evaluateAll();

            cell0.setCellFormula(null);
            cell1.setCellFormula(null);
            formulaEvaluator.notifyUpdateCell(cell0);
            formulaEvaluator.notifyUpdateCell(cell1);
            //the following asserts should probably be BLANK not ERROR
            assertEquals(CellType.ERROR, cell0.getCellType());
            assertEquals(CellType.ERROR, cell1.getCellType());
        }
    }
}
