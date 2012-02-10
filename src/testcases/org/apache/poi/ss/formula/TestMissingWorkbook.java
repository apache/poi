/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.ss.formula;

import junit.framework.TestCase;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;

import java.io.IOException;

public class TestMissingWorkbook extends TestCase {
	private static final String MAIN_WORKBOOK_FILENAME = "52575_main.xls";
	private static final String SOURCE_DUMMY_WORKBOOK_FILENAME = "source_dummy.xls";
	private static final String SOURCE_WORKBOOK_FILENAME = "52575_source.xls";
	
	private HSSFWorkbook mainWorkbook;
	private HSSFWorkbook sourceWorkbook;
	
	@Override
	protected void setUp() throws Exception {
		mainWorkbook = HSSFTestDataSamples.openSampleWorkbook(MAIN_WORKBOOK_FILENAME);
		sourceWorkbook = HSSFTestDataSamples.openSampleWorkbook(SOURCE_WORKBOOK_FILENAME);
		
		assertNotNull(mainWorkbook);
		assertNotNull(sourceWorkbook);
	}

	public void testMissingWorkbookMissing() throws IOException {
		FormulaEvaluator evaluator = mainWorkbook.getCreationHelper().createFormulaEvaluator();
		
		HSSFSheet lSheet = mainWorkbook.getSheetAt(0);
		HSSFRow lARow = lSheet.getRow(0);
		HSSFCell lA1Cell = lARow.getCell(0);
		
		assertEquals(Cell.CELL_TYPE_FORMULA, lA1Cell.getCellType());
		try {
			evaluator.evaluateFormulaCell(lA1Cell);
			fail("Missing external workbook reference exception expected!");
		}catch(RuntimeException re) {
			assertTrue("Unexpected exception: " + re, re.getMessage().indexOf(SOURCE_DUMMY_WORKBOOK_FILENAME) != -1);
		}
	}
	
	public void testMissingWorkbookMissingOverride() throws IOException {
		HSSFSheet lSheet = mainWorkbook.getSheetAt(0);
		HSSFCell lA1Cell = lSheet.getRow(0).getCell(0);
		HSSFCell lB1Cell = lSheet.getRow(1).getCell(0);
		HSSFCell lC1Cell = lSheet.getRow(2).getCell(0);
		
		assertEquals(Cell.CELL_TYPE_FORMULA, lA1Cell.getCellType());
		assertEquals(Cell.CELL_TYPE_FORMULA, lB1Cell.getCellType());
		assertEquals(Cell.CELL_TYPE_FORMULA, lC1Cell.getCellType());
		
		HSSFFormulaEvaluator evaluator = mainWorkbook.getCreationHelper().createFormulaEvaluator();
        evaluator.setIgnoreMissingWorkbooks(true);

		assertEquals(Cell.CELL_TYPE_NUMERIC, evaluator.evaluateFormulaCell(lA1Cell));
		assertEquals(Cell.CELL_TYPE_STRING, evaluator.evaluateFormulaCell(lB1Cell));
		assertEquals(Cell.CELL_TYPE_BOOLEAN, evaluator.evaluateFormulaCell(lC1Cell));

		assertEquals(10.0d, lA1Cell.getNumericCellValue(), 0.00001d);
		assertEquals("POI rocks!", lB1Cell.getStringCellValue());
		assertEquals(true, lC1Cell.getBooleanCellValue());
	}
	

	public void testExistingWorkbook() throws IOException {
		HSSFSheet lSheet = mainWorkbook.getSheetAt(0);
		HSSFCell lA1Cell = lSheet.getRow(0).getCell(0);
		HSSFCell lB1Cell = lSheet.getRow(1).getCell(0);
		HSSFCell lC1Cell = lSheet.getRow(2).getCell(0);
		
		assertEquals(Cell.CELL_TYPE_FORMULA, lA1Cell.getCellType());
		assertEquals(Cell.CELL_TYPE_FORMULA, lB1Cell.getCellType());
		assertEquals(Cell.CELL_TYPE_FORMULA, lC1Cell.getCellType());

		HSSFFormulaEvaluator lMainWorkbookEvaluator = new HSSFFormulaEvaluator(mainWorkbook);
		HSSFFormulaEvaluator lSourceEvaluator = new HSSFFormulaEvaluator(sourceWorkbook);
		HSSFFormulaEvaluator.setupEnvironment(
				new String[]{MAIN_WORKBOOK_FILENAME, SOURCE_DUMMY_WORKBOOK_FILENAME}, 
				new HSSFFormulaEvaluator[] {lMainWorkbookEvaluator, lSourceEvaluator});
		
		assertEquals(Cell.CELL_TYPE_NUMERIC, lMainWorkbookEvaluator.evaluateFormulaCell(lA1Cell));
		assertEquals(Cell.CELL_TYPE_STRING, lMainWorkbookEvaluator.evaluateFormulaCell(lB1Cell));
		assertEquals(Cell.CELL_TYPE_BOOLEAN, lMainWorkbookEvaluator.evaluateFormulaCell(lC1Cell));

		assertEquals(20.0d, lA1Cell.getNumericCellValue(), 0.00001d);
		assertEquals("Apache rocks!", lB1Cell.getStringCellValue());
		assertEquals(false, lC1Cell.getBooleanCellValue());
	}

}
