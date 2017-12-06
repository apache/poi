package org.apache.poi.ss.formula.functions;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.junit.Test;

import junit.framework.TestCase;

/**
 * Test the VLOOKUP function
 */
public class TestVlookup extends TestCase {

    @Test
    public void testFullColumnAreaRef61841() {
        final Workbook wb = XSSFTestDataSamples.openSampleWorkbook("VLookupFullColumn.xlsx");
        FormulaEvaluator feval = wb.getCreationHelper().createFormulaEvaluator();
        feval.evaluateAll();
        assertEquals("Wrong lookup value",  "Value1", feval.evaluate(wb.getSheetAt(0).getRow(3).getCell(1)).getStringValue());
        assertEquals("Lookup should return #N/A", CellType.ERROR, feval.evaluate(wb.getSheetAt(0).getRow(4).getCell(1)).getCellType());
    }

}
