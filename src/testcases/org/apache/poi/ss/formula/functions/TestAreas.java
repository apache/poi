package org.apache.poi.ss.formula.functions;

import junit.framework.TestCase;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;

public final class TestAreas extends TestCase {

    public void testAreas() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFCell cell = wb.createSheet().createRow(0).createCell(0);
        HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);

        String formulaText = "AREAS(B1)";
        confirmResult(fe, cell, formulaText,1.0);

        formulaText = "AREAS(B2:D4)";
        confirmResult(fe, cell, formulaText,1.0);

        formulaText = "AREAS((B2:D4,E5,F6:I9))";
        confirmResult(fe, cell, formulaText,3.0);

        formulaText = "AREAS((B2:D4,E5,C3,E4))";
        confirmResult(fe, cell, formulaText,4.0);

        formulaText = "AREAS((I9))";
        confirmResult(fe, cell, formulaText,1.0);
    }

    private static void confirmResult(HSSFFormulaEvaluator fe, HSSFCell cell, String formulaText,Double expectedResult) {
        cell.setCellFormula(formulaText);
        fe.notifyUpdateCell(cell);
        CellValue result = fe.evaluate(cell);
        assertEquals(result.getCellTypeEnum(), CellType.NUMERIC);
        assertEquals(expectedResult, result.getNumberValue());
    }
}
