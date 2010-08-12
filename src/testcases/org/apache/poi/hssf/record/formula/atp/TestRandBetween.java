/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
package org.apache.poi.hssf.record.formula.atp;

import junit.framework.TestCase;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * Testcase for 'Analysis Toolpak' function RANDBETWEEN()
 * 
 * @author Brendan Nolan
 */
public class TestRandBetween extends TestCase {

	private Workbook wb;
	private FormulaEvaluator evaluator;
	private Cell bottomValueCell;
	private Cell topValueCell;
	private Cell formulaCell;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		wb = HSSFTestDataSamples.openSampleWorkbook("TestRandBetween.xls");
		evaluator = wb.getCreationHelper().createFormulaEvaluator();
		
		Sheet sheet = wb.createSheet("RandBetweenSheet");
		Row row = sheet.createRow(0);
		bottomValueCell = row.createCell(0);
		topValueCell = row.createCell(1);
		formulaCell = row.createCell(2, HSSFCell.CELL_TYPE_FORMULA);
	}
	
	@Override
	protected void tearDown() throws Exception {
		// TODO Auto-generated method stub
		super.tearDown();
	}
	
	/**
	 * Check where values are the same
	 */
	public void testRandBetweenSameValues() {
		
		evaluator.clearAllCachedResultValues();
		formulaCell.setCellFormula("RANDBETWEEN(1,1)");
		evaluator.evaluateFormulaCell(formulaCell);
		assertEquals(1, formulaCell.getNumericCellValue(), 0);
		evaluator.clearAllCachedResultValues();
		formulaCell.setCellFormula("RANDBETWEEN(-1,-1)");
		evaluator.evaluateFormulaCell(formulaCell);
		assertEquals(-1, formulaCell.getNumericCellValue(), 0);

	}
	
	/**
	 * Check special case where rounded up bottom value is greater than 
	 * top value.
	 */
	public void testRandBetweenSpecialCase() {
		

		bottomValueCell.setCellValue(0.05);		
		topValueCell.setCellValue(0.1);
		formulaCell.setCellFormula("RANDBETWEEN($A$1,$B$1)");
		evaluator.clearAllCachedResultValues();
		evaluator.evaluateFormulaCell(formulaCell);
		assertEquals(1, formulaCell.getNumericCellValue(), 0);
		bottomValueCell.setCellValue(-0.1);		
		topValueCell.setCellValue(-0.05);
		formulaCell.setCellFormula("RANDBETWEEN($A$1,$B$1)");
		evaluator.clearAllCachedResultValues();
		evaluator.evaluateFormulaCell(formulaCell);
		assertEquals(0, formulaCell.getNumericCellValue(), 0);
		bottomValueCell.setCellValue(-1.1);		
		topValueCell.setCellValue(-1.05);
		formulaCell.setCellFormula("RANDBETWEEN($A$1,$B$1)");
		evaluator.clearAllCachedResultValues();
		evaluator.evaluateFormulaCell(formulaCell);
		assertEquals(-1, formulaCell.getNumericCellValue(), 0);
		bottomValueCell.setCellValue(-1.1);		
		topValueCell.setCellValue(-1.1);
		formulaCell.setCellFormula("RANDBETWEEN($A$1,$B$1)");
		evaluator.clearAllCachedResultValues();
		evaluator.evaluateFormulaCell(formulaCell);
		assertEquals(-1, formulaCell.getNumericCellValue(), 0);
	}
	
	/**
	 * Check top value of BLANK which Excel will evaluate as 0
	 */
	public void testRandBetweenTopBlank() {

		bottomValueCell.setCellValue(-1);		
		topValueCell.setCellType(Cell.CELL_TYPE_BLANK);
		formulaCell.setCellFormula("RANDBETWEEN($A$1,$B$1)");
		evaluator.clearAllCachedResultValues();
		evaluator.evaluateFormulaCell(formulaCell);
		assertTrue(formulaCell.getNumericCellValue() == 0 || formulaCell.getNumericCellValue() == -1);
	
	}
	/**
	 * Check where input values are of wrong type
	 */
	public void testRandBetweenWrongInputTypes() {
		// Check case where bottom input is of the wrong type
		bottomValueCell.setCellValue("STRING");		
		topValueCell.setCellValue(1);
		formulaCell.setCellFormula("RANDBETWEEN($A$1,$B$1)");
		evaluator.clearAllCachedResultValues();
		evaluator.evaluateFormulaCell(formulaCell);
		assertEquals(Cell.CELL_TYPE_ERROR, formulaCell.getCachedFormulaResultType());
		assertEquals(ErrorEval.VALUE_INVALID.getErrorCode(), formulaCell.getErrorCellValue());
		
		
		// Check case where top input is of the wrong type
		bottomValueCell.setCellValue(1);
		topValueCell.setCellValue("STRING");		
		formulaCell.setCellFormula("RANDBETWEEN($A$1,$B$1)");
		evaluator.clearAllCachedResultValues();
		evaluator.evaluateFormulaCell(formulaCell);
		assertEquals(Cell.CELL_TYPE_ERROR, formulaCell.getCachedFormulaResultType());
		assertEquals(ErrorEval.VALUE_INVALID.getErrorCode(), formulaCell.getErrorCellValue());

		// Check case where both inputs are of wrong type
		bottomValueCell.setCellValue("STRING");
		topValueCell.setCellValue("STRING");		
		formulaCell.setCellFormula("RANDBETWEEN($A$1,$B$1)");
		evaluator.clearAllCachedResultValues();
		evaluator.evaluateFormulaCell(formulaCell);
		assertEquals(Cell.CELL_TYPE_ERROR, formulaCell.getCachedFormulaResultType());
		assertEquals(ErrorEval.VALUE_INVALID.getErrorCode(), formulaCell.getErrorCellValue());
	
	}
	
	/**
	 * Check case where bottom is greater than top
	 */
	public void testRandBetweenBottomGreaterThanTop() {

		// Check case where bottom is greater than top
		bottomValueCell.setCellValue(1);		
		topValueCell.setCellValue(0);
		formulaCell.setCellFormula("RANDBETWEEN($A$1,$B$1)");
		evaluator.clearAllCachedResultValues();
		evaluator.evaluateFormulaCell(formulaCell);
		assertEquals(Cell.CELL_TYPE_ERROR, formulaCell.getCachedFormulaResultType());
		assertEquals(ErrorEval.NUM_ERROR.getErrorCode(), formulaCell.getErrorCellValue());		
		bottomValueCell.setCellValue(1);		
		topValueCell.setCellType(Cell.CELL_TYPE_BLANK);
		formulaCell.setCellFormula("RANDBETWEEN($A$1,$B$1)");
		evaluator.clearAllCachedResultValues();
		evaluator.evaluateFormulaCell(formulaCell);
		assertEquals(Cell.CELL_TYPE_ERROR, formulaCell.getCachedFormulaResultType());
		assertEquals(ErrorEval.NUM_ERROR.getErrorCode(), formulaCell.getErrorCellValue());
	}
	
	/**
	 * Boundary check of Double MIN and MAX values
	 */
	public void testRandBetweenBoundaryCheck() {

		bottomValueCell.setCellValue(Double.MIN_VALUE);		
		topValueCell.setCellValue(Double.MAX_VALUE);
		formulaCell.setCellFormula("RANDBETWEEN($A$1,$B$1)");
		evaluator.clearAllCachedResultValues();
		evaluator.evaluateFormulaCell(formulaCell);
		assertTrue(formulaCell.getNumericCellValue() >= Double.MIN_VALUE && formulaCell.getNumericCellValue() <= Double.MAX_VALUE);		
		
	}
	
}
