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

package org.apache.poi.ss.formula.atp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Calendar;
import java.util.Iterator;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.eval.EvaluationException;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.util.LocaleUtil;
import org.junit.jupiter.api.Test;

/**
 * Tests YearFracCalculator using test-cases listed in a sample spreadsheet
 */
final class TestYearFracCalculatorFromSpreadsheet {

	private static final class SS {

		public static final int BASIS_COLUMN = 1; // "B"
		public static final int START_YEAR_COLUMN = 2; // "C"
		public static final int END_YEAR_COLUMN = 5; // "F"
		public static final int YEARFRAC_FORMULA_COLUMN = 11; // "L"
		public static final int EXPECTED_RESULT_COLUMN = 13; // "N"
	}

	@Test
	void testAll() throws Exception {

		HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("yearfracExamples.xls");
		HSSFSheet sheet = wb.getSheetAt(0);
		HSSFFormulaEvaluator formulaEvaluator = new HSSFFormulaEvaluator(wb);
		int nSuccess = 0;
		Iterator<Row> rowIterator = sheet.rowIterator();
		while(rowIterator.hasNext()) {
			HSSFRow row = (HSSFRow) rowIterator.next();

			HSSFCell cell = row.getCell(SS.YEARFRAC_FORMULA_COLUMN);
			if (cell == null || cell.getCellType() != CellType.FORMULA) {
				continue;
			}
            processRow(row, cell, formulaEvaluator);
            nSuccess++;
		}

		assertTrue(nSuccess > 0, "No test sample cases found");
		wb.close();
	}

	private void processRow(HSSFRow row, HSSFCell cell, HSSFFormulaEvaluator formulaEvaluator)
	throws EvaluationException {

		double startDate = makeDate(row, SS.START_YEAR_COLUMN);
		double endDate = makeDate(row, SS.END_YEAR_COLUMN);

		int basis = getIntCell(row, SS.BASIS_COLUMN);

		double expectedValue = getDoubleCell(row, SS.EXPECTED_RESULT_COLUMN);

		double actualValue = YearFracCalculator.calculate(startDate, endDate, basis);

		String loc = " - row " + (row.getRowNum()+1);
		assertEquals(expectedValue, actualValue, 0, "Direct calculate failed"+loc);
		actualValue = formulaEvaluator.evaluate(cell).getNumberValue();
		assertEquals(expectedValue, actualValue, 0, "Formula evaluate failed"+loc);
	}

	private static double makeDate(HSSFRow row, int yearColumn) {
		int year = getIntCell(row, yearColumn + 0);
		int month = getIntCell(row, yearColumn + 1);
		int day = getIntCell(row, yearColumn + 2);
		Calendar c = LocaleUtil.getLocaleCalendar(year, month-1, day);
		return DateUtil.getExcelDate(c.getTime());
	}

	private static int getIntCell(HSSFRow row, int colIx) {
		double dVal = getDoubleCell(row, colIx);
		String msg = "Non integer value (" + dVal + ") cell found at column " + (char)('A' + colIx);
		assertEquals(Math.floor(dVal), dVal, 0, msg);
		return (int)dVal;
	}

	private static double getDoubleCell(HSSFRow row, int colIx) {
		HSSFCell cell = row.getCell(colIx);
		assertNotNull(cell, "No cell found at column " + colIx);
        return cell.getNumericCellValue();
	}
}
