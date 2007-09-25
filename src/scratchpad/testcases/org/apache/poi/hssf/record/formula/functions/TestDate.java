/*
 * Created on Sep 11, 2007
 * 
 * The Copyright statements and Licenses for the commons application may be
 * found in the file LICENSE.txt
 */

package org.apache.poi.hssf.record.formula.functions;

import junit.framework.TestCase;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;

/**
 * @author Pavel Krupets (pkrupets at palmtreebusiness dot com)
 */
public class TestDate extends TestCase {
    public void setUp() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet("new sheet");
        HSSFRow row1 = sheet.createRow((short) 0);
        
        this.cell11 = row1.createCell((short) 0);
        
        this.evaluator = new HSSFFormulaEvaluator(sheet, wb);
        this.evaluator.setCurrentRow(row1);
    }
    
	/**
	 * Test disabled pending a fix in the formula parser
	 */
    public void DISABLEDtestSomeArgumentsMissing() throws Exception {
        this.cell11.setCellFormula("DATE(, 1, 0)");
        assertEquals(0.0, this.evaluator.evaluate(this.cell11).getNumberValue(), 0);
        
        this.cell11.setCellFormula("DATE(, 1, 1)");
        assertEquals(1.0, this.evaluator.evaluate(this.cell11).getNumberValue(), 0);
    }
    
    public void testValid() throws Exception {
        this.cell11.setCellType(HSSFCell.CELL_TYPE_FORMULA);
        
        this.cell11.setCellFormula("DATE(1900, 1, 1)");
        assertEquals(1, this.evaluator.evaluate(this.cell11).getNumberValue(), 0);
        
        this.cell11.setCellFormula("DATE(1900, 1, 32)");
        assertEquals(32, this.evaluator.evaluate(this.cell11).getNumberValue(), 0);
        
        this.cell11.setCellFormula("DATE(1900, 222, 1)");
        assertEquals(6727, this.evaluator.evaluate(this.cell11).getNumberValue(), 0);
        
        this.cell11.setCellFormula("DATE(1900, 2, 0)");
        assertEquals(31, this.evaluator.evaluate(this.cell11).getNumberValue(), 0);
        
        this.cell11.setCellFormula("DATE(2000, 1, 222)");
        assertEquals(36747.00, this.evaluator.evaluate(this.cell11).getNumberValue(), 0);
        
        this.cell11.setCellFormula("DATE(2007, 1, 1)");
        assertEquals(39083, this.evaluator.evaluate(this.cell11).getNumberValue(), 0);
    }
    
    public void testBugDate() {
        this.cell11.setCellFormula("DATE(1900, 2, 29)");
        assertEquals(60, this.evaluator.evaluate(this.cell11).getNumberValue(), 0);
        
        this.cell11.setCellFormula("DATE(1900, 2, 30)");
        assertEquals(61, this.evaluator.evaluate(this.cell11).getNumberValue(), 0);
        
        this.cell11.setCellFormula("DATE(1900, 1, 222)");
        assertEquals(222, this.evaluator.evaluate(this.cell11).getNumberValue(), 0);
        
        this.cell11.setCellFormula("DATE(1900, 1, 2222)");
        assertEquals(2222, this.evaluator.evaluate(this.cell11).getNumberValue(), 0);
        
        this.cell11.setCellFormula("DATE(1900, 1, 22222)");
        assertEquals(22222, this.evaluator.evaluate(this.cell11).getNumberValue(), 0);
    }
    
    public void testPartYears() {
        this.cell11.setCellFormula("DATE(4, 1, 1)");
        assertEquals(1462.00, this.evaluator.evaluate(this.cell11).getNumberValue(), 0);
        
        this.cell11.setCellFormula("DATE(14, 1, 1)");
        assertEquals(5115.00, this.evaluator.evaluate(this.cell11).getNumberValue(), 0);
        
        this.cell11.setCellFormula("DATE(104, 1, 1)");
        assertEquals(37987.00, this.evaluator.evaluate(this.cell11).getNumberValue(), 0);
        
        this.cell11.setCellFormula("DATE(1004, 1, 1)");
        assertEquals(366705.00, this.evaluator.evaluate(this.cell11).getNumberValue(), 0);
    }
    
    private HSSFCell cell11;
    private HSSFFormulaEvaluator evaluator;
}

