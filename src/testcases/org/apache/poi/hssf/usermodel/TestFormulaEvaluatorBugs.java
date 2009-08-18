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

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.record.aggregates.FormulaRecordAggregate;
import org.apache.poi.hssf.record.formula.AreaPtg;
import org.apache.poi.hssf.record.formula.FuncVarPtg;
import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.hssf.record.formula.eval.ValueEval;
import org.apache.poi.ss.formula.EvaluationCell;
import org.apache.poi.ss.formula.EvaluationListener;
import org.apache.poi.ss.formula.WorkbookEvaluator;
import org.apache.poi.ss.formula.WorkbookEvaluatorTestHelper;

/**
 *
 */
public final class TestFormulaEvaluatorBugs extends TestCase {

	private static final boolean OUTPUT_TEST_FILES = false;
	private String tmpDirName;

	protected void setUp() {

		tmpDirName = System.getProperty("java.io.tmpdir");
	}

	/**
	 * An odd problem with evaluateFormulaCell giving the
	 *  right values when file is opened, but changes
	 *  to the source data in some versions of excel
	 *  doesn't cause them to be updated. However, other
	 *  versions of excel, and gnumeric, work just fine
	 * WARNING - tedious bug where you actually have to
	 *  open up excel
	 */
	public void test44636() throws Exception {
		// Open the existing file, tweak one value and
		// re-calculate

		HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("44636.xls");
		HSSFSheet sheet = wb.getSheetAt(0);
		HSSFRow row = sheet.getRow(0);

		row.getCell(0).setCellValue(4.2);
		row.getCell(2).setCellValue(25);

		HSSFFormulaEvaluator.evaluateAllFormulaCells(wb);
		assertEquals(4.2 * 25, row.getCell(3).getNumericCellValue(), 0.0001);

		FileOutputStream out;
		if (OUTPUT_TEST_FILES) {
			// Save
			File existing = new File(tmpDirName, "44636-existing.xls");
			out = new FileOutputStream(existing);
			wb.write(out);
			out.close();
			System.err.println("Existing file for bug #44636 written to " + existing.toString());
		}
		// Now, do a new file from scratch
		wb = new HSSFWorkbook();
		sheet = wb.createSheet();

		row = sheet.createRow(0);
		row.createCell(0).setCellValue(1.2);
		row.createCell(1).setCellValue(4.2);

		row = sheet.createRow(1);
		row.createCell(0).setCellFormula("SUM(A1:B1)");

		HSSFFormulaEvaluator.evaluateAllFormulaCells(wb);
		assertEquals(5.4, row.getCell(0).getNumericCellValue(), 0.0001);

		if (OUTPUT_TEST_FILES) {
			// Save
			File scratch = new File(tmpDirName, "44636-scratch.xls");
			out = new FileOutputStream(scratch);
			wb.write(out);
			out.close();
			System.err.println("New file for bug #44636 written to " + scratch.toString());
		}
	}

	/**
	 * Bug 44297: 32767+32768 is evaluated to -1
	 * Fix: IntPtg must operate with unsigned short. Reading signed short results in incorrect formula calculation
	 * if a formula has values in the interval [Short.MAX_VALUE, (Short.MAX_VALUE+1)*2]
	 *
	 * @author Yegor Kozlov
	 */
	public void test44297() {

		HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("44297.xls");

		HSSFRow row;
		HSSFCell cell;

		HSSFSheet sheet = wb.getSheetAt(0);

		HSSFFormulaEvaluator eva = new HSSFFormulaEvaluator(wb);

		row = sheet.getRow(0);
		cell = row.getCell(0);
		assertEquals("31+46", cell.getCellFormula());
		assertEquals(77, eva.evaluate(cell).getNumberValue(), 0);

		row = sheet.getRow(1);
		cell = row.getCell(0);
		assertEquals("30+53", cell.getCellFormula());
		assertEquals(83, eva.evaluate(cell).getNumberValue(), 0);

		row = sheet.getRow(2);
		cell = row.getCell(0);
		assertEquals("SUM(A1:A2)", cell.getCellFormula());
		assertEquals(160, eva.evaluate(cell).getNumberValue(), 0);

		row = sheet.getRow(4);
		cell = row.getCell(0);
		assertEquals("32767+32768", cell.getCellFormula());
		assertEquals(65535, eva.evaluate(cell).getNumberValue(), 0);

		row = sheet.getRow(7);
		cell = row.getCell(0);
		assertEquals("32744+42333", cell.getCellFormula());
		assertEquals(75077, eva.evaluate(cell).getNumberValue(), 0);

		row = sheet.getRow(8);
		cell = row.getCell(0);
		assertEquals("327680/32768", cell.getCellFormula());
		assertEquals(10, eva.evaluate(cell).getNumberValue(), 0);

		row = sheet.getRow(9);
		cell = row.getCell(0);
		assertEquals("32767+32769", cell.getCellFormula());
		assertEquals(65536, eva.evaluate(cell).getNumberValue(), 0);

		row = sheet.getRow(10);
		cell = row.getCell(0);
		assertEquals("35000+36000", cell.getCellFormula());
		assertEquals(71000, eva.evaluate(cell).getNumberValue(), 0);

		row = sheet.getRow(11);
		cell = row.getCell(0);
		assertEquals("-1000000-3000000", cell.getCellFormula());
		assertEquals(-4000000, eva.evaluate(cell).getNumberValue(), 0);
	}

	/**
	 * Bug 44410: SUM(C:C) is valid in excel, and means a sum
	 *  of all the rows in Column C
	 *
	 * @author Nick Burch
	 */
	public void test44410() {

		HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("SingleLetterRanges.xls");

		HSSFSheet sheet = wb.getSheetAt(0);

		HSSFFormulaEvaluator eva = new HSSFFormulaEvaluator(wb);

		// =index(C:C,2,1) -> 2
		HSSFRow rowIDX = sheet.getRow(3);
		// =sum(C:C) -> 6
		HSSFRow rowSUM = sheet.getRow(4);
		// =sum(C:D) -> 66
		HSSFRow rowSUM2D = sheet.getRow(5);

		// Test the sum
		HSSFCell cellSUM = rowSUM.getCell(0);

		FormulaRecordAggregate frec = (FormulaRecordAggregate) cellSUM.getCellValueRecord();
		Ptg[] ops = frec.getFormulaRecord().getParsedExpression();
		assertEquals(2, ops.length);
		assertEquals(AreaPtg.class, ops[0].getClass());
		assertEquals(FuncVarPtg.class, ops[1].getClass());

		// Actually stored as C1 to C65536
		// (last row is -1 === 65535)
		AreaPtg ptg = (AreaPtg) ops[0];
		assertEquals(2, ptg.getFirstColumn());
		assertEquals(2, ptg.getLastColumn());
		assertEquals(0, ptg.getFirstRow());
		assertEquals(65535, ptg.getLastRow());
		assertEquals("C:C", ptg.toFormulaString());

		// Will show as C:C, but won't know how many
		// rows it covers as we don't have the sheet
		// to hand when turning the Ptgs into a string
		assertEquals("SUM(C:C)", cellSUM.getCellFormula());

		// But the evaluator knows the sheet, so it
		// can do it properly
		assertEquals(6, eva.evaluate(cellSUM).getNumberValue(), 0);

		// Test the index
		// Again, the formula string will be right but
		// lacking row count, evaluated will be right
		HSSFCell cellIDX = rowIDX.getCell(0);
		assertEquals("INDEX(C:C,2,1)", cellIDX.getCellFormula());
		assertEquals(2, eva.evaluate(cellIDX).getNumberValue(), 0);

		// Across two colums
		HSSFCell cellSUM2D = rowSUM2D.getCell(0);
		assertEquals("SUM(C:D)", cellSUM2D.getCellFormula());
		assertEquals(66, eva.evaluate(cellSUM2D).getNumberValue(), 0);
	}

	/**
	 * Tests that we can evaluate boolean cells properly
	 */
	public void testEvaluateBooleanInCell_bug44508() {
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet();
		wb.setSheetName(0, "Sheet1");
		HSSFRow row = sheet.createRow(0);
		HSSFCell cell = row.createCell(0);

		cell.setCellFormula("1=1");

		HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
		try {
			fe.evaluateInCell(cell);
		} catch (NumberFormatException e) {
			fail("Identified bug 44508");
		}
		assertEquals(true, cell.getBooleanCellValue());
	}

	public void testClassCast_bug44861() {
		HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("44861.xls");

		// Check direct
		HSSFFormulaEvaluator.evaluateAllFormulaCells(wb);

		// And via calls
		int numSheets = wb.getNumberOfSheets();
		for (int i = 0; i < numSheets; i++) {
			HSSFSheet s = wb.getSheetAt(i);
			HSSFFormulaEvaluator eval = new HSSFFormulaEvaluator(wb);

			for (Iterator rows = s.rowIterator(); rows.hasNext();) {
				HSSFRow r = (HSSFRow) rows.next();

				for (Iterator cells = r.cellIterator(); cells.hasNext();) {
					HSSFCell c = (HSSFCell) cells.next();
					eval.evaluateFormulaCell(c);
				}
			}
		}
	}

	public void testEvaluateInCellWithErrorCode_bug44950() {
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("Sheet1");
		HSSFRow row = sheet.createRow(1);
		HSSFCell cell = row.createCell(0);
		cell.setCellFormula("na()"); // this formula evaluates to an Excel error code '#N/A'
		HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
		try {
			fe.evaluateInCell(cell);
		} catch (NumberFormatException e) {
			if (e.getMessage().equals("You cannot get an error value from a non-error cell")) {
				throw new AssertionFailedError("Identified bug 44950 b");
			}
			throw e;
		}
	}

	private static final class EvalListener extends EvaluationListener {
		private int _countCacheHits;
		private int _countCacheMisses;

		public EvalListener() {
			_countCacheHits = 0;
			_countCacheMisses = 0;
		}
		public int getCountCacheHits() {
			return _countCacheHits;
		}
		public int getCountCacheMisses() {
			return _countCacheMisses;
		}

		public void onCacheHit(int sheetIndex, int srcRowNum, int srcColNum, ValueEval result) {
			_countCacheHits++;
		}
		public void onStartEvaluate(EvaluationCell cell, ICacheEntry entry, Ptg[] ptgs) {
			_countCacheMisses++;
		}
	}

	/**
	 * The HSSFFormula evaluator performance benefits greatly from caching of intermediate cell values
	 */
	public void testSlowEvaluate45376() {

		// Firstly set up a sequence of formula cells where each depends on the  previous multiple
		// times.  Without caching, each subsequent cell take about 4 times longer to evaluate.
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("Sheet1");
		HSSFRow row = sheet.createRow(0);
		for(int i=1; i<10; i++) {
			HSSFCell cell = row.createCell(i);
			char prevCol = (char) ('A' + i-1);
			String prevCell = prevCol + "1";
			// this formula is inspired by the offending formula of the attachment for bug 45376
			String formula = "IF(DATE(YEAR(" + prevCell + "),MONTH(" + prevCell + ")+1,1)<=$D$3," +
					"DATE(YEAR(" + prevCell + "),MONTH(" + prevCell + ")+1,1),NA())";
			cell.setCellFormula(formula);

		}
		Calendar cal = new GregorianCalendar(2000, 0, 1, 0, 0, 0);
		row.createCell(0).setCellValue(cal);

		// Choose cell A9, so that the failing test case doesn't take too long to execute.
		HSSFCell cell = row.getCell(8);
		EvalListener evalListener = new EvalListener();
		WorkbookEvaluator evaluator = WorkbookEvaluatorTestHelper.createEvaluator(wb, evalListener);
		evaluator.evaluate(HSSFEvaluationTestHelper.wrapCell(cell));
		int evalCount = evalListener.getCountCacheMisses();
		if (evalCount > 10) {
			// Without caching, evaluating cell 'A9' takes 21845 evaluations which consumes
			// much time (~3 sec on Core 2 Duo 2.2GHz)
			System.err.println("Cell A9 took " + evalCount + " intermediate evaluations");
			throw new AssertionFailedError("Identifed bug 45376 - Formula evaluator should cache values");
		}
		// With caching, the evaluationCount is 8 which is a big improvement
		// Note - these expected values may change if the WorkbookEvaluator is
		// ever optimised to short circuit 'if' functions.
		assertEquals(8, evalCount);
		assertEquals(24, evalListener.getCountCacheHits());
	}
}
