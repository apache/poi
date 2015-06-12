package org.apache.poi.ss.formula;

import org.apache.poi.hssf.usermodel.HSSFEvaluationWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFEvaluationWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import junit.framework.TestCase;

/**
 * Test {@link FormulaParser}'s handling of row numbers at the edge of the
 * HSSF/XSSF ranges.
 * 
 * @author David North
 */
public class TestFormulaParser extends TestCase {

    public void testHSSFFailsForOver65536() {
        FormulaParsingWorkbook workbook = HSSFEvaluationWorkbook.create(new HSSFWorkbook());
        try {
            FormulaParser.parse("Sheet1!1:65537", workbook, FormulaType.CELL, 0);
            fail("Expected exception");
        }
        catch (FormulaParseException expected) {
        }
    }

    public void testHSSFPassCase() {
        FormulaParsingWorkbook workbook = HSSFEvaluationWorkbook.create(new HSSFWorkbook());
        FormulaParser.parse("Sheet1!1:65536", workbook, FormulaType.CELL, 0);
    }

    public void testXSSFWorksForOver65536() {
        FormulaParsingWorkbook workbook = XSSFEvaluationWorkbook.create(new XSSFWorkbook());
        FormulaParser.parse("Sheet1!1:65537", workbook, FormulaType.CELL, 0);
    }

    public void testXSSFFailCase() {
        FormulaParsingWorkbook workbook = XSSFEvaluationWorkbook.create(new XSSFWorkbook());
        try {
            FormulaParser.parse("Sheet1!1:1048577", workbook, FormulaType.CELL, 0); // one more than max rows.
            fail("Expected exception");
        }
        catch (FormulaParseException expected) {
        }
    }

}
