package org.apache.poi.ss.formula;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Table;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;

/**
 * Tests Excel Table expressions (structured references)
 * @see <a href="https://support.office.com/en-us/article/Using-structured-references-with-Excel-tables-F5ED2452-2337-4F71-BED3-C8AE6D2B276E">
 *         Excel Structured Reference Syntax
 *      </a>
 */
public class TestStructuredReferences {

    /**
     * Test the regular expression used in INDIRECT() evaluation to recognize structured references
     */
    @Test
    public void testTableExpressionSyntax() {
        assertTrue("Valid structured reference syntax didn't match expression", Table.isStructuredReference.matcher("abc[col1]").matches());
        assertTrue("Valid structured reference syntax didn't match expression", Table.isStructuredReference.matcher("_abc[col1]").matches());
        assertTrue("Valid structured reference syntax didn't match expression", Table.isStructuredReference.matcher("_[col1]").matches());
        assertTrue("Valid structured reference syntax didn't match expression", Table.isStructuredReference.matcher("\\[col1]").matches());
        assertTrue("Valid structured reference syntax didn't match expression", Table.isStructuredReference.matcher("\\[col1]").matches());
        assertTrue("Valid structured reference syntax didn't match expression", Table.isStructuredReference.matcher("\\[#This Row]").matches());
        assertTrue("Valid structured reference syntax didn't match expression", Table.isStructuredReference.matcher("\\[ [col1], [col2] ]").matches());
        
        // can't have a space between the table name and open bracket
        assertFalse("Invalid structured reference syntax didn't fail expression", Table.isStructuredReference.matcher("\\abc [ [col1], [col2] ]").matches());
    }
    
    @Test
    public void testTableFormulas() throws Exception {
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("StructuredReferences.xlsx");
        try {
            
            final FormulaEvaluator eval = new XSSFFormulaEvaluator(wb);
            confirm(eval, wb.getSheet("Table").getRow(5).getCell(0), 49);
            confirm(eval, wb.getSheet("Formulas").getRow(0).getCell(0), 209);
        } finally {
            wb.close();
        }
    }

    private static void confirm(FormulaEvaluator fe, Cell cell, double expectedResult) {
        fe.clearAllCachedResultValues();
        CellValue cv = fe.evaluate(cell);
        if (cv.getCellType() != Cell.CELL_TYPE_NUMERIC) {
            fail("expected numeric cell type but got " + cv.formatAsString());
        }
        assertEquals(expectedResult, cv.getNumberValue(), 0.0);
    }
}
