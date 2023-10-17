package org.apache.poi.ss.formula.functions;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.FormulaError;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.apache.poi.ss.util.Utils.assertError;

final class TestErrors {
    @Test
    void testTextDivide() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFRow row = wb.createSheet().createRow(0);
            HSSFCell cell = row.createCell(0);
            cell.setCellValue("text");
            HSSFCell evalCell = row.createCell(1);
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            assertError(fe, evalCell, "A1/2", FormulaError.VALUE);
        }
    }
}
