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

package org.apache.poi.ss.formula.functions;

import java.util.Arrays;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.eval.AreaEval;
import org.apache.poi.ss.formula.eval.MissingArgEval;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.formula.WorkbookEvaluator;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;

/**
 * Tests for the INDEX() function.</p>
 *
 * This class contains just a few specific cases that directly invoke {@link Index},
 * with minimum overhead.<br>
 * Another test: {@link TestIndexFunctionFromSpreadsheet} operates from a higher level
 * and has far greater coverage of input permutations.<br>
 *
 * @author Josh Micich
 */
public final class TestIndex extends TestCase {

	private static final Index FUNC_INST = new Index();
	private static final double[] TEST_VALUES0 = {
			1, 2,
			3, 4,
			5, 6,
			7, 8,
			9, 10,
			11, 12,
	};

	/**
	 * For the case when the first argument to INDEX() is an area reference
	 */
	public void testEvaluateAreaReference() {

		double[] values = TEST_VALUES0;
		confirmAreaEval("C1:D6", values, 4, 1, 7);
		confirmAreaEval("C1:D6", values, 6, 2, 12);
		confirmAreaEval("C1:D6", values, 3, 1, 5);

		// now treat same data as 3 columns, 4 rows
		confirmAreaEval("C10:E13", values, 2, 2, 5);
		confirmAreaEval("C10:E13", values, 4, 1, 10);
	}

	/**
	 * @param areaRefString in Excel notation e.g. 'D2:E97'
	 * @param dValues array of evaluated values for the area reference
	 * @param rowNum 1-based
	 * @param colNum 1-based, pass -1 to signify argument not present
	 */
	private static void confirmAreaEval(String areaRefString, double[] dValues,
			int rowNum, int colNum, double expectedResult) {
		ValueEval[] values = new ValueEval[dValues.length];
		for (int i = 0; i < values.length; i++) {
			values[i] = new NumberEval(dValues[i]);
		}
		AreaEval arg0 = EvalFactory.createAreaEval(areaRefString, values);

		ValueEval[] args;
		if (colNum > 0) {
			args = new ValueEval[] { arg0, new NumberEval(rowNum), new NumberEval(colNum), };
		} else {
			args = new ValueEval[] { arg0, new NumberEval(rowNum), };
		}

		double actual = invokeAndDereference(args);
		assertEquals(expectedResult, actual, 0D);
	}

	private static double invokeAndDereference(ValueEval[] args) {
		ValueEval ve = FUNC_INST.evaluate(args, -1, -1);
		ve = WorkbookEvaluator.dereferenceResult(ve, -1, -1);
		assertEquals(NumberEval.class, ve.getClass());
		return ((NumberEval)ve).getNumberValue();
	}

	/**
	 * Tests expressions like "INDEX(A1:C1,,2)".<br>
	 * This problem was found while fixing bug 47048 and is observable up to svn r773441.
	 */
	public void testMissingArg() {
		ValueEval[] values = {
				new NumberEval(25.0),
				new NumberEval(26.0),
				new NumberEval(28.0),
		};
		AreaEval arg0 = EvalFactory.createAreaEval("A10:C10", values);
		ValueEval[] args = new ValueEval[] { arg0, MissingArgEval.instance, new NumberEval(2), };
		ValueEval actualResult;
		try {
			actualResult = FUNC_INST.evaluate(args, -1, -1);
		} catch (RuntimeException e) {
			if (e.getMessage().equals("Unexpected arg eval type (org.apache.poi.hssf.record.formula.eval.MissingArgEval")) {
				throw new AssertionFailedError("Identified bug 47048b - INDEX() should support missing-arg");
			}
			throw e;
		}
		// result should be an area eval "B10:B10"
		AreaEval ae = confirmAreaEval("B10:B10", actualResult);
		actualResult = ae.getValue(0, 0);
		assertEquals(NumberEval.class, actualResult.getClass());
		assertEquals(26.0, ((NumberEval)actualResult).getNumberValue(), 0.0);
	}

	/**
	 * When the argument to INDEX is a reference, the result should be a reference
	 * A formula like "OFFSET(INDEX(A1:B2,2,1),1,1,1,1)" should return the value of cell B3.
	 * This works because the INDEX() function returns a reference to A2 (not the value of A2)
	 */
	public void testReferenceResult() {
		ValueEval[] values = new ValueEval[4];
		Arrays.fill(values, NumberEval.ZERO);
		AreaEval arg0 = EvalFactory.createAreaEval("A1:B2", values);
		ValueEval[] args = new ValueEval[] { arg0, new NumberEval(2), new NumberEval(1), };
		ValueEval ve = FUNC_INST.evaluate(args, -1, -1);
		confirmAreaEval("A2:A2", ve);
	}

	/**
	 * Confirms that the result is an area ref with the specified coordinates
	 * @return <tt>ve</tt> cast to {@link AreaEval} if it is valid
	 */
	private static AreaEval confirmAreaEval(String refText, ValueEval ve) {
		CellRangeAddress cra = CellRangeAddress.valueOf(refText);
		assertTrue(ve instanceof AreaEval);
		AreaEval ae = (AreaEval) ve;
		assertEquals(cra.getFirstRow(), ae.getFirstRow());
		assertEquals(cra.getFirstColumn(), ae.getFirstColumn());
		assertEquals(cra.getLastRow(), ae.getLastRow());
		assertEquals(cra.getLastColumn(), ae.getLastColumn());
		return ae;
	}

	public void test61859(){
		Workbook wb = HSSFTestDataSamples.openSampleWorkbook("maxindextest.xls");
		FormulaEvaluator fe = wb.getCreationHelper().createFormulaEvaluator();

		Sheet example1 = wb.getSheetAt(0);
		Cell ex1cell1 = example1.getRow(1).getCell(6);
		assertEquals("MAX(INDEX(($B$2:$B$11=F2)*$A$2:$A$11,0))", ex1cell1.getCellFormula());
		fe.evaluate(ex1cell1);
		assertEquals(4.0, ex1cell1.getNumericCellValue());

		Cell ex1cell2 = example1.getRow(2).getCell(6);
		assertEquals("MAX(INDEX(($B$2:$B$11=F3)*$A$2:$A$11,0))", ex1cell2.getCellFormula());
		fe.evaluate(ex1cell2);
		assertEquals(10.0, ex1cell2.getNumericCellValue());

		Cell ex1cell3 = example1.getRow(3).getCell(6);
		assertEquals("MAX(INDEX(($B$2:$B$11=F4)*$A$2:$A$11,0))", ex1cell3.getCellFormula());
		fe.evaluate(ex1cell3);
		assertEquals(20.0, ex1cell3.getNumericCellValue());
	}

	public void test61116(){
		Workbook workbook = HSSFTestDataSamples.openSampleWorkbook("61116.xls");
		FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
		Sheet sheet = workbook.getSheet("sample2");

		Row row = sheet.getRow(1);
		assertEquals(3.0, evaluator.evaluate(row.getCell(1)).getNumberValue());
		assertEquals(3.0, evaluator.evaluate(row.getCell(2)).getNumberValue());

		row = sheet.getRow(2);
		assertEquals(5.0, evaluator.evaluate(row.getCell(1)).getNumberValue());
		assertEquals(5.0, evaluator.evaluate(row.getCell(2)).getNumberValue());
	}

	/**
	 * If both the Row_num and Column_num arguments are used,
	 * INDEX returns the value in the cell at the intersection of Row_num and Column_num
	 */
	public void testReference2DArea(){
		Workbook wb = new HSSFWorkbook();
		Sheet sheet = wb.createSheet();
		/**
		 * 1	2	3
		 * 4	5	6
		 * 7	8	9
		 */
		int val = 0;
		for(int i = 0; i < 3; i++){
			Row row = sheet.createRow(i);
			for(int j = 0; j < 3; j++){
				row.createCell(j).setCellValue(++val);
			}
		}
		FormulaEvaluator fe = wb.getCreationHelper().createFormulaEvaluator();

		Cell c1 = sheet.getRow(0).createCell(5);
		c1.setCellFormula("INDEX(A1:C3,2,2)");
		Cell c2 = sheet.getRow(0).createCell(6);
		c2.setCellFormula("INDEX(A1:C3,3,2)");

		assertEquals(5.0, fe.evaluate(c1).getNumberValue());
		assertEquals(8.0, fe.evaluate(c2).getNumberValue());
	}

	/**
	 * If Column_num is 0 (zero), INDEX returns the array of values for the entire row.
	 */
	public void testArrayArgument_RowLookup(){
		Workbook wb = new HSSFWorkbook();
		Sheet sheet = wb.createSheet();
		/**
		 * 1	2	3
		 * 4	5	6
		 * 7	8	9
		 */
		int val = 0;
		for(int i = 0; i < 3; i++){
			Row row = sheet.createRow(i);
			for(int j = 0; j < 3; j++){
				row.createCell(j).setCellValue(++val);
			}
		}
		Cell c1 = sheet.getRow(0).createCell(5);
		c1.setCellFormula("SUM(INDEX(A1:C3,1,0))"); // sum of all values in the 1st row: 1 + 2 + 3 = 6

		Cell c2 = sheet.getRow(0).createCell(6);
		c2.setCellFormula("SUM(INDEX(A1:C3,2,0))"); // sum of all values in the 2nd row: 4 + 5 + 6 = 15

		FormulaEvaluator fe = wb.getCreationHelper().createFormulaEvaluator();

		assertEquals(6.0, fe.evaluate(c1).getNumberValue());
		assertEquals(15.0, fe.evaluate(c2).getNumberValue());

	}

	/**
	 * If Row_num is 0 (zero), INDEX returns the array of values for the entire column.
	 */
	public void testArrayArgument_ColumnLookup(){
		Workbook wb = new HSSFWorkbook();
		Sheet sheet = wb.createSheet();
		/**
		 * 1	2	3
		 * 4	5	6
		 * 7	8	9
		 */
		int val = 0;
		for(int i = 0; i < 3; i++){
			Row row = sheet.createRow(i);
			for(int j = 0; j < 3; j++){
				row.createCell(j).setCellValue(++val);
			}
		}
		Cell c1 = sheet.getRow(0).createCell(5);
		c1.setCellFormula("SUM(INDEX(A1:C3,0,1))"); // sum of all values in the 1st column: 1 + 4 + 7 = 12

		Cell c2 = sheet.getRow(0).createCell(6);
		c2.setCellFormula("SUM(INDEX(A1:C3,0,3))"); // sum of all values in the 3rd column: 3 + 6 + 9 = 18

		FormulaEvaluator fe = wb.getCreationHelper().createFormulaEvaluator();

		assertEquals(12.0, fe.evaluate(c1).getNumberValue());
		assertEquals(18.0, fe.evaluate(c2).getNumberValue());
	}

	/**
	 * =SUM(B1:INDEX(B1:B3,2))
	 *
	 * 	 The sum of the range starting at B1, and ending at the intersection of the 2nd row of the range B1:B3,
	 * 	 which is the sum of B1:B2.
	 */
	public void testDynamicReference(){
		Workbook wb = new HSSFWorkbook();
		Sheet sheet = wb.createSheet();
		/**
		 * 1	2	3
		 * 4	5	6
		 * 7	8	9
		 */
		int val = 0;
		for(int i = 0; i < 3; i++){
			Row row = sheet.createRow(i);
			for(int j = 0; j < 3; j++){
				row.createCell(j).setCellValue(++val);
			}
		}
		Cell c1 = sheet.getRow(0).createCell(5);
		c1.setCellFormula("SUM(B1:INDEX(B1:B3,2))"); // B1:INDEX(B1:B3,2) evaluates to B1:B2

		FormulaEvaluator fe = wb.getCreationHelper().createFormulaEvaluator();

		assertEquals(7.0, fe.evaluate(c1).getNumberValue());
	}
}
