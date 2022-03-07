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
}
