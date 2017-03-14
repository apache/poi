package org.apache.poi.ss.formula.functions;

import static org.junit.Assert.assertEquals;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.junit.Test;

/**
 *
 */
public class TestSumifsXSSF {

    /**
     * handle null cell predicate
     */
    @Test
    public void testBug60858() {
        Workbook wb = XSSFTestDataSamples.openSampleWorkbook("bug60858.xlsx");
        FormulaEvaluator fe = wb.getCreationHelper().createFormulaEvaluator();

        Sheet sheet = wb.getSheetAt(0);
        Cell cell = sheet.getRow(1).getCell(5);
        fe.evaluate(cell);
        assertEquals(0.0, cell.getNumericCellValue(), 0.0000000000000001);
    }

}
