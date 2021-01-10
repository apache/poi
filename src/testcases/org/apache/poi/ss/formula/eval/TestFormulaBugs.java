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

package org.apache.poi.ss.formula.eval;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.Test;

/**
 * Miscellaneous tests for bugzilla entries.<p> The test name contains the
 * bugzilla bug id.
 */
final class TestFormulaBugs {

	/**
	 * Bug 27349 - VLOOKUP with reference to another sheet.<p> This test was
	 * added <em>long</em> after the relevant functionality was fixed.
	 */
	@Test
	void test27349() throws Exception {
		// 27349-vlookupAcrossSheets.xls is bugzilla/attachment.cgi?id=10622
		InputStream is = HSSFTestDataSamples.openSampleFileStream("27349-vlookupAcrossSheets.xls");
		// original bug may have thrown exception here,
		// or output warning to stderr
		Workbook wb = new HSSFWorkbook(is);

		Sheet sheet = wb.getSheetAt(0);
		Row row = sheet.getRow(1);
		Cell cell = row.getCell(0);

		// this definitely would have failed due to 27349
		assertEquals("VLOOKUP(1,'DATA TABLE'!$A$8:'DATA TABLE'!$B$10,2)", cell
				.getCellFormula());

		// We might as well evaluate the formula
		FormulaEvaluator fe = wb.getCreationHelper().createFormulaEvaluator();
		CellValue cv = fe.evaluate(cell);

		assertEquals(CellType.NUMERIC, cv.getCellType());
		assertEquals(3.0, cv.getNumberValue(), 0.0);

		wb.close();
		is.close();
	}

	/**
	 * Bug 27405 - isnumber() formula always evaluates to false in if statement<p>
	 *
	 * seems to be a duplicate of 24925
	 */
	@Test
	void test27405() throws Exception {
		Workbook wb = new HSSFWorkbook();
		Sheet sheet = wb.createSheet("input");
		// input row 0
		Row row = sheet.createRow(0);
		/*Cell cell =*/ row.createCell(0);
		Cell cell = row.createCell(1);
		cell.setCellValue(1); // B1
		// input row 1
		row = sheet.createRow(1);
		cell = row.createCell(1);
		cell.setCellValue(999); // B2

		int rno = 4;
		row = sheet.createRow(rno);
		cell = row.createCell(1); // B5
		cell.setCellFormula("isnumber(b1)");
		cell = row.createCell(3); // D5
		cell.setCellFormula("IF(ISNUMBER(b1),b1,b2)");

//		if (false) { // set true to check excel file manually
//			// bug report mentions 'Editing the formula in excel "fixes" the problem.'
//			try {
//				FileOutputStream fileOut = new FileOutputStream("27405output.xls");
//				wb.write(fileOut);
//				fileOut.close();
//			} catch (IOException e) {
//				throw new RuntimeException(e);
//			}
//		}

		// use POI's evaluator as an extra sanity check
		FormulaEvaluator fe = wb.getCreationHelper().createFormulaEvaluator();
		CellValue cv;
		cv = fe.evaluate(cell);
		assertEquals(CellType.NUMERIC, cv.getCellType());
		assertEquals(1.0, cv.getNumberValue(), 0.0);

		cv = fe.evaluate(row.getCell(1));
		assertEquals(CellType.BOOLEAN, cv.getCellType());
        assertTrue(cv.getBooleanValue());

		wb.close();
	}

	/**
	 * Bug 42448 - Can't parse SUMPRODUCT(A!C7:A!C67, B8:B68) / B69 <p>
	 */
	@Test
	void test42448() throws IOException {
		try (Workbook wb = new HSSFWorkbook()) {
			Sheet sheet1 = wb.createSheet("Sheet1");

			Row row = sheet1.createRow(0);
			Cell cell = row.createCell(0);

			// it's important to create the referenced sheet first
			Sheet sheet2 = wb.createSheet("A"); // note name 'A'
			// TODO - POI crashes if the formula is added before this sheet
			// RuntimeException("Zero length string is an invalid sheet name")
			// Excel doesn't crash but the formula doesn't work until it is
			// re-entered

			String inputFormula = "SUMPRODUCT(A!C7:A!C67, B8:B68) / B69"; // as per bug report
			try {
				cell.setCellFormula(inputFormula);
			} catch (StringIndexOutOfBoundsException e) {
				fail("Identified bug 42448");
			}

			assertEquals("SUMPRODUCT(A!C7:A!C67,B8:B68)/B69", cell.getCellFormula());

			// might as well evaluate the sucker...

			addCell(sheet2, 5, 2, 3.0); // A!C6
			addCell(sheet2, 6, 2, 4.0); // A!C7
			addCell(sheet2, 66, 2, 5.0); // A!C67
			addCell(sheet2, 67, 2, 6.0); // A!C68

			addCell(sheet1, 6, 1, 7.0); // B7
			addCell(sheet1, 7, 1, 8.0); // B8
			addCell(sheet1, 67, 1, 9.0); // B68
			addCell(sheet1, 68, 1, 10.0); // B69

			double expectedResult = (4.0 * 8.0 + 5.0 * 9.0) / 10.0;

			FormulaEvaluator fe = wb.getCreationHelper().createFormulaEvaluator();
			CellValue cv = fe.evaluate(cell);

			assertEquals(CellType.NUMERIC, cv.getCellType());
			assertEquals(expectedResult, cv.getNumberValue(), 0.0);
		}
	}

	private static void addCell(Sheet sheet, int rowIx, int colIx,
			double value) {
		sheet.createRow(rowIx).createCell(colIx).setCellValue(value);
	}

	@Test
	void test55032() throws IOException {
		Workbook wb = new HSSFWorkbook();
		Sheet sheet = wb.createSheet("input");

		Row row = sheet.createRow(0);
		Cell cell = row.createCell(1);

		checkFormulaValue(wb, cell, "PV(0.08/12, 20*12, 500, ,0)", -59777.14585);
		checkFormulaValue(wb, cell, "PV(0.08/12, 20*12, 500, ,)", -59777.14585);
		checkFormulaValue(wb, cell, "PV(0.08/12, 20*12, 500, 500,)", -59878.6315455);

		checkFormulaValue(wb, cell, "FV(0.08/12, 20*12, 500, ,)", -294510.2078107270);
		checkFormulaValue(wb, cell, "PMT(0.08/12, 20*12, 500, ,)", -4.1822003450);
		checkFormulaValue(wb, cell, "NPER(0.08/12, 20*12, 500, ,)", -2.0758873434);

		wb.close();
	}

	// bug 52063: LOOKUP(2-arg) and LOOKUP(3-arg)
	// FIXME: This could be moved into LookupFunctionsTestCaseData.xls, which is tested by TestLookupFunctionsFromSpreadsheet.java
	@Test
	void testLookupFormula() throws Exception {
		Workbook wb = new HSSFWorkbook();
		Sheet sheet = wb.createSheet("52063");

		// Note: Values in arrays are in ascending order since LOOKUP expects that in order to work properly
		//		 column
		//		 A B C
		//	   +-------
		// row 1 | P Q R
		// row 2 | X Y Z
		Row row = sheet.createRow(0);
		row.createCell(0).setCellValue("P");
		row.createCell(1).setCellValue("Q");
		row.createCell(2).setCellValue("R");
		row = sheet.createRow(1);
		row.createCell(0).setCellValue("X");
		row.createCell(1).setCellValue("Y");
		row.createCell(2).setCellValue("Z");

		Cell evalcell = sheet.createRow(2).createCell(0);

		//// ROW VECTORS
		// lookup and result row are the same
		checkFormulaValue(wb, evalcell, "LOOKUP(\"Q\", A1:C1)", "Q");
		checkFormulaValue(wb, evalcell, "LOOKUP(\"R\", A1:C1)", "R");
		checkFormulaValue(wb, evalcell, "LOOKUP(\"Q\", A1:C1, A1:C1)", "Q");
		checkFormulaValue(wb, evalcell, "LOOKUP(\"R\", A1:C1, A1:C1)", "R");

		// lookup and result row are different
		checkFormulaValue(wb, evalcell, "LOOKUP(\"Q\", A1:C2)", "Y");
		checkFormulaValue(wb, evalcell, "LOOKUP(\"R\", A1:C2)", "Z");
		checkFormulaValue(wb, evalcell, "LOOKUP(\"Q\", A1:C1, A2:C2)", "Y");
		checkFormulaValue(wb, evalcell, "LOOKUP(\"R\", A1:C1, A2:C2)", "Z");

		//// COLUMN VECTORS
		// lookup and result column are different
		checkFormulaValue(wb, evalcell, "LOOKUP(\"P\", A1:B2)", "Q");
		checkFormulaValue(wb, evalcell, "LOOKUP(\"X\", A1:A2, C1:C2)", "Z");

		wb.close();
	}

	private CellValue evaluateFormulaInCell(Workbook wb, Cell cell, String formula) {
		cell.setCellFormula(formula);

		FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();

        return evaluator.evaluate(cell);
	}

	private void checkFormulaValue(Workbook wb, Cell cell, String formula, double expectedValue) {
		CellValue value = evaluateFormulaInCell(wb, cell, formula);
		assertEquals(expectedValue, value.getNumberValue(), 0.0001);
	}

	private void checkFormulaValue(Workbook wb, Cell cell, String formula, String expectedValue) {
		CellValue value = evaluateFormulaInCell(wb, cell, formula);
		assertEquals(expectedValue, value.getStringValue());
	}
}
