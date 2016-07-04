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
package org.apache.poi.hssf.usermodel;

import java.io.IOException;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.ss.usermodel.CellType;

import junit.framework.TestCase;

public class TestExternalReferenceChange extends TestCase {

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
	
	public void testDummyToSource() throws IOException {
		boolean changed = mainWorkbook.changeExternalReference("DOESNOTEXIST", SOURCE_WORKBOOK_FILENAME);
		assertFalse(changed);
		
		changed = mainWorkbook.changeExternalReference(SOURCE_DUMMY_WORKBOOK_FILENAME, SOURCE_WORKBOOK_FILENAME);
		assertTrue(changed);

		HSSFSheet lSheet = mainWorkbook.getSheetAt(0);
		HSSFCell lA1Cell = lSheet.getRow(0).getCell(0);
		
		assertEquals(CellType.FORMULA, lA1Cell.getCellType());
		
		HSSFFormulaEvaluator lMainWorkbookEvaluator = new HSSFFormulaEvaluator(mainWorkbook);
		HSSFFormulaEvaluator lSourceEvaluator = new HSSFFormulaEvaluator(sourceWorkbook);
		HSSFFormulaEvaluator.setupEnvironment(
				new String[]{MAIN_WORKBOOK_FILENAME, SOURCE_WORKBOOK_FILENAME}, 
				new HSSFFormulaEvaluator[] {lMainWorkbookEvaluator, lSourceEvaluator});
		
		assertEquals(CellType.NUMERIC, lMainWorkbookEvaluator.evaluateFormulaCell(lA1Cell));

		assertEquals(20.0d, lA1Cell.getNumericCellValue(), 0.00001d);

	}
	
}
