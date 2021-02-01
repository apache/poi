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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public abstract class BaseTestMissingWorkbook {
    protected Workbook mainWorkbook;
    protected Workbook sourceWorkbook;

    protected final String MAIN_WORKBOOK_FILENAME;
    protected final String SOURCE_DUMMY_WORKBOOK_FILENAME;
    protected final String SOURCE_WORKBOOK_FILENAME;

    protected BaseTestMissingWorkbook(String MAIN_WORKBOOK_FILENAME,
									  String SOURCE_DUMMY_WORKBOOK_FILENAME, String SOURCE_WORKBOOK_FILENAME) {
        this.MAIN_WORKBOOK_FILENAME = MAIN_WORKBOOK_FILENAME;
        this.SOURCE_DUMMY_WORKBOOK_FILENAME = SOURCE_DUMMY_WORKBOOK_FILENAME;
        this.SOURCE_WORKBOOK_FILENAME = SOURCE_WORKBOOK_FILENAME;
    }

	@BeforeEach
	protected void setUp() throws Exception {
		mainWorkbook = HSSFTestDataSamples.openSampleWorkbook(MAIN_WORKBOOK_FILENAME);
		sourceWorkbook = HSSFTestDataSamples.openSampleWorkbook(SOURCE_WORKBOOK_FILENAME);

		assertNotNull(mainWorkbook);
		assertNotNull(sourceWorkbook);
	}

	@AfterEach
	void tearDown() throws Exception {
		if(mainWorkbook != null) {
			mainWorkbook.close();
		}

		if(sourceWorkbook != null) {
			sourceWorkbook.close();
		}
	}

	@Test
	void testMissingWorkbookMissing() {
		FormulaEvaluator evaluator = mainWorkbook.getCreationHelper().createFormulaEvaluator();

		Sheet lSheet = mainWorkbook.getSheetAt(0);
		Row lARow = lSheet.getRow(0);
		Cell lA1Cell = lARow.getCell(0);

		assertEquals(CellType.FORMULA, lA1Cell.getCellType());
		RuntimeException re = assertThrows(RuntimeException.class, () -> evaluator.evaluateFormulaCell(lA1Cell),
										   "Missing external workbook reference exception expected!");
		assertTrue(re.getMessage().contains(SOURCE_DUMMY_WORKBOOK_FILENAME));
	}

	@Test
	void testMissingWorkbookMissingOverride() {
		Sheet lSheet = mainWorkbook.getSheetAt(0);
		Cell lA1Cell = lSheet.getRow(0).getCell(0);
		Cell lB1Cell = lSheet.getRow(1).getCell(0);
		Cell lC1Cell = lSheet.getRow(2).getCell(0);

		assertEquals(CellType.FORMULA, lA1Cell.getCellType());
		assertEquals(CellType.FORMULA, lB1Cell.getCellType());
		assertEquals(CellType.FORMULA, lC1Cell.getCellType());

		// Check cached values
        assertEquals(10.0d, lA1Cell.getNumericCellValue(), 0.00001d);
        assertEquals("POI rocks!", lB1Cell.getStringCellValue());
		assertTrue(lC1Cell.getBooleanCellValue());

        // Evaluate
		FormulaEvaluator evaluator = mainWorkbook.getCreationHelper().createFormulaEvaluator();
        evaluator.setIgnoreMissingWorkbooks(true);

		assertEquals(CellType.NUMERIC, evaluator.evaluateFormulaCell(lA1Cell));
		assertEquals(CellType.STRING,  evaluator.evaluateFormulaCell(lB1Cell));
		assertEquals(CellType.BOOLEAN, evaluator.evaluateFormulaCell(lC1Cell));

		assertEquals(10.0d, lA1Cell.getNumericCellValue(), 0.00001d);
		assertEquals("POI rocks!", lB1Cell.getStringCellValue());
		assertTrue(lC1Cell.getBooleanCellValue());
	}

	@Test
	void testExistingWorkbook() {
		Sheet lSheet = mainWorkbook.getSheetAt(0);
		Cell lA1Cell = lSheet.getRow(0).getCell(0);
		Cell lB1Cell = lSheet.getRow(1).getCell(0);
		Cell lC1Cell = lSheet.getRow(2).getCell(0);

		assertEquals(CellType.FORMULA, lA1Cell.getCellType());
		assertEquals(CellType.FORMULA, lB1Cell.getCellType());
		assertEquals(CellType.FORMULA, lC1Cell.getCellType());

		FormulaEvaluator lMainWorkbookEvaluator = mainWorkbook.getCreationHelper().createFormulaEvaluator();
		FormulaEvaluator lSourceEvaluator = sourceWorkbook.getCreationHelper().createFormulaEvaluator();
		Map<String,FormulaEvaluator> workbooks = new HashMap<>();
		workbooks.put(MAIN_WORKBOOK_FILENAME, lMainWorkbookEvaluator);
		workbooks.put(SOURCE_DUMMY_WORKBOOK_FILENAME, lSourceEvaluator);
		lMainWorkbookEvaluator.setupReferencedWorkbooks(workbooks);

		assertEquals(CellType.NUMERIC, lMainWorkbookEvaluator.evaluateFormulaCell(lA1Cell));
		assertEquals(CellType.STRING, lMainWorkbookEvaluator.evaluateFormulaCell(lB1Cell));
		assertEquals(CellType.BOOLEAN, lMainWorkbookEvaluator.evaluateFormulaCell(lC1Cell));

		assertEquals(20.0d, lA1Cell.getNumericCellValue(), 0.00001d);
		assertEquals("Apache rocks!", lB1Cell.getStringCellValue());
		assertFalse(lC1Cell.getBooleanCellValue());
	}
}
