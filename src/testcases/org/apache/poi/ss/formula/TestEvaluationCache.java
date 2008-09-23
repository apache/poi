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

package org.apache.poi.ss.formula;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.poi.hssf.model.HSSFFormulaParser;
import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.hssf.record.formula.eval.BlankEval;
import org.apache.poi.hssf.record.formula.eval.BoolEval;
import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.NumberEval;
import org.apache.poi.hssf.record.formula.eval.StringEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellReference;

/**
 * Tests {@link EvaluationCache}.  Makes sure that where possible (previously calculated) cached 
 * values are used.  Also checks that changing cell values causes the correct (minimal) set of
 * dependent cached values to be cleared.
 *
 * @author Josh Micich
 */
public class TestEvaluationCache extends TestCase {
	private static final class EvalListener extends EvaluationListener {

		private final List _logList;
		private final HSSFWorkbook _book;

		public EvalListener(HSSFWorkbook wb) {
			_book = wb;
			_logList = new ArrayList();
		}
		public void onCacheHit(int sheetIndex, int rowIndex, int columnIndex, ValueEval result) {
			log("hit", rowIndex, columnIndex, result);
		}
		public void onReadPlainValue(int sheetIndex, int rowIndex, int columnIndex, ValueEval value) {
			log("value", rowIndex, columnIndex, value);
		}
		public void onStartEvaluate(int sheetIndex, int rowIndex, int columnIndex, Ptg[] ptgs) {
			log("start", rowIndex, columnIndex, ptgs);
		}
		public void onEndEvaluate(int sheetIndex, int rowIndex, int columnIndex, ValueEval result) {
			log("end", rowIndex, columnIndex, result);
		}
		public void onClearCachedValue(int sheetIndex, int rowIndex, int columnIndex, ValueEval value) {
			log("clear", rowIndex, columnIndex, value);
		}
		public void onClearDependentCachedValue(int sheetIndex, int rowIndex, int columnIndex,
				ValueEval value,int depth) {
			log("clear" + depth, rowIndex, columnIndex, value);
		}
		private void log(String tag, int rowIndex, int columnIndex, Object value) {
			StringBuffer sb = new StringBuffer(64);
			sb.append(tag).append(' ');
			sb.append(new CellReference(rowIndex, columnIndex).formatAsString());
			if (value != null) {
				sb.append(' ').append(formatValue(value));
			}
			_logList.add(sb.toString());
		}
		private String formatValue(Object value) {
			if (value instanceof Ptg[]) {
				Ptg[] ptgs = (Ptg[]) value;
				return HSSFFormulaParser.toFormulaString(_book, ptgs);
			}
			if (value instanceof NumberEval) {
				NumberEval ne = (NumberEval) value;
				return ne.getStringValue();
			}
			if (value instanceof StringEval) {
				StringEval se = (StringEval) value;
				return "'" + se.getStringValue() + "'";
			}
			if (value instanceof BoolEval) {
				BoolEval be = (BoolEval) value;
				return be.getStringValue();
			}
			if (value == BlankEval.INSTANCE) {
				return "#BLANK#";
			}
			if (value instanceof ErrorEval) {
				ErrorEval ee = (ErrorEval) value;
				return ErrorEval.getText(ee.getErrorCode());
			}
			throw new IllegalArgumentException("Unexpected value class ("
					+ value.getClass().getName() + ")");
		}
		public String[] getAndClearLog() {
			String[] result = new String[_logList.size()];
			_logList.toArray(result);
			_logList.clear();
			return result;
		}
	}
	/**
	 * Wrapper class to manage repetitive tasks from this test,
	 *
	 * Note - this class does a little bit more than just plain set-up of data. The method
	 * {@link WorkbookEvaluator#clearCachedResultValue(HSSFSheet, int, int)} is called whenever a
	 * cell value is changed.
	 *
	 */
	private static final class MySheet {

		private final HSSFSheet _sheet;
		private final WorkbookEvaluator _evaluator;
		private final HSSFWorkbook _wb;
		private final EvalListener _evalListener;

		public MySheet() {
			_wb = new HSSFWorkbook();
			_evalListener = new EvalListener(_wb);
			_evaluator = WorkbookEvaluatorTestHelper.createEvaluator(_wb, _evalListener);
			_sheet = _wb.createSheet("Sheet1");
		}

		public void setCellValue(String cellRefText, double value) {
			HSSFCell cell = getOrCreateCell(cellRefText);
			// be sure to blank cell, in case it is currently a formula
			cell.setCellType(HSSFCell.CELL_TYPE_BLANK);
			// otherwise this line will only set the formula cached result;
			cell.setCellValue(value);
			_evaluator.setCachedPlainValue(_sheet, cell.getRowIndex(), cell.getCellNum(), new NumberEval(value));
		}

		public void setCellFormula(String cellRefText, String formulaText) {
			HSSFCell cell = getOrCreateCell(cellRefText);
			cell.setCellFormula(formulaText);
			_evaluator.notifySetFormula(_sheet, cell.getRowIndex(), cell.getCellNum());
		}

		private HSSFCell getOrCreateCell(String cellRefText) {
			CellReference cr = new CellReference(cellRefText);
			int rowIndex = cr.getRow();
			HSSFRow row = _sheet.getRow(rowIndex);
			if (row == null) {
				row = _sheet.createRow(rowIndex);
			}
			int cellIndex = cr.getCol();
			HSSFCell cell = row.getCell(cellIndex);
			if (cell == null) {
				cell = row.createCell(cellIndex);
			}
			return cell;
		}

		public ValueEval evaluateCell(String cellRefText) {
			return _evaluator.evaluate(getOrCreateCell(cellRefText));
		}

		public String[] getAndClearLog() {
			return _evalListener.getAndClearLog();
		}

		public void clearAllCachedResultValues() {
			_evaluator.clearAllCachedResultValues();
		}
	}

	private static MySheet createMediumComplex() {
		MySheet ms = new MySheet();

		// plain data in D1:F3
		ms.setCellValue("D1", 12);
		ms.setCellValue("E1", 13);
		ms.setCellValue("D2", 14);
		ms.setCellValue("E2", 15);
		ms.setCellValue("D3", 16);
		ms.setCellValue("E3", 17);


		ms.setCellFormula("C1", "SUM(D1:E2)");
		ms.setCellFormula("C2", "SUM(D2:E3)");
		ms.setCellFormula("C3", "SUM(D3:E4)");

		ms.setCellFormula("B1", "C2-C1");
		ms.setCellFormula("B2", "B3*C1-C2");
		ms.setCellValue("B3", 2);

		ms.setCellFormula("A1", "MAX(B1:B2)");
		ms.setCellFormula("A2", "MIN(B3,D2:F2)");
		ms.setCellFormula("A3", "B3*C3");

		// clear all the logging from the above initialisation
		ms.getAndClearLog();
		ms.clearAllCachedResultValues();
		return ms;
	}

	public void testMediumComplex() {

		MySheet ms = createMediumComplex();
		// completely fresh evaluation
		confirmEvaluate(ms, "A1", 46);
		confirmLog(ms, new String[] {
			"start A1 MAX(B1:B2)",
				"start B1 C2-C1",
					"start C2 SUM(D2:E3)",
						"value D2 14", "value E2 15", "value D3 16", "value E3 17",
					"end C2 62",
					"start C1 SUM(D1:E2)",
						"value D1 12", "value E1 13", "hit D2 14", "hit E2 15",
					"end C1 54",
				"end B1 8",
				"start B2 B3*C1-C2",
					"value B3 2",
					"hit C1 54",
					"hit C2 62",
				"end B2 46",
			"end A1 46",
		});


		// simple cache hit - immediate re-evaluation with no changes
		confirmEvaluate(ms, "A1", 46);
		confirmLog(ms, new String[] { "hit A1 46", });

		// change a low level cell
		ms.setCellValue("D1", 10);
		confirmLog(ms, new String[] {
				"clear1 C1 54",
				"clear2 B1 8",
				"clear3 A1 46",
				"clear2 B2 46",
		});
		confirmEvaluate(ms, "A1", 42);
		confirmLog(ms, new String[] {
			"start A1 MAX(B1:B2)",
				"start B1 C2-C1",
					"hit C2 62",
					"start C1 SUM(D1:E2)",
						"hit D1 10", "hit E1 13", "hit D2 14", "hit E2 15",
					"end C1 52",
				"end B1 10",
				"start B2 B3*C1-C2",
					"hit B3 2",
					"hit C1 52",
					"hit C2 62",
				"end B2 42",
			"end A1 42",
		});

		// Reset and try changing an intermediate value
		ms = createMediumComplex();
		confirmEvaluate(ms, "A1", 46);
		ms.getAndClearLog();

		ms.setCellValue("B3", 3); // B3 is in the middle of the dependency tree
		confirmLog(ms, new String[] {
				"clear1 B2 46",
				"clear2 A1 46",
		});
		confirmEvaluate(ms, "A1", 100);
		confirmLog(ms, new String[] {
			"start A1 MAX(B1:B2)",
				"hit B1 8",
				"start B2 B3*C1-C2",
					"hit B3 3",
					"hit C1 54",
					"hit C2 62",
				"end B2 100",
			"end A1 100",
		});
	}

	public void testMediumComplexWithDependencyChange() {

		// Changing an intermediate formula
		MySheet ms = createMediumComplex();
		confirmEvaluate(ms, "A1", 46);
		ms.getAndClearLog();
		ms.setCellFormula("B2", "B3*C2-C3"); // used to be "B3*C1-C2"
		confirmLog(ms, new String[] {
			"clear1 A1 46",
		});

		confirmEvaluate(ms, "A1", 91);
		confirmLog(ms, new String[] {
			"start A1 MAX(B1:B2)",
				"hit B1 8",
				"start B2 B3*C2-C3",
					"hit B3 2",
					"hit C2 62",
					"start C3 SUM(D3:E4)",
						"hit D3 16", "hit E3 17", "value D4 #BLANK#", "value E4 #BLANK#",
					"end C3 33",
				"end B2 91",
			"end A1 91",
		});

		//----------------
		// Note - From now on the demonstrated POI behaviour is not optimal
		//----------------

		// Now change a value that should no longer affect B2
		ms.setCellValue("D1", 11);
		confirmLog(ms, new String[] {
			"clear1 C1 54",
			// note there is no "clear2 B2 91" here because B2 doesn't depend on C1 anymore
			"clear2 B1 8",
			"clear3 A1 91",
		});

		confirmEvaluate(ms, "B2", 91);
		confirmLog(ms, new String[] {
			"hit B2 91",  // further confirmation that B2 was not cleared due to changing D1 above
		});

		// things should be back to normal now
		ms.setCellValue("D1", 11);
		confirmLog(ms, new String[] {  });
		confirmEvaluate(ms, "B2", 91);
		confirmLog(ms, new String[] {
			"hit B2 91",
		});
	}

	/**
	 * verifies that when updating a plain cell, depending (formula) cell cached values are cleared
	 * only when the palin cell's value actually changes
	 */
	public void testRedundantUpdate() {
		MySheet ms = new MySheet();

		ms.setCellValue("B1", 12);
		ms.setCellValue("C1", 13);
		ms.setCellFormula("A1", "B1+C1");

		// evaluate twice to confirm caching looks OK
		ms.evaluateCell("A1");
		ms.getAndClearLog();
		confirmEvaluate(ms, "A1", 25);
		confirmLog(ms, new String[] {
			"hit A1 25",
		});

		// Make redundant update, and check re-evaluation
		ms.setCellValue("B1", 12); // value didn't change
		confirmLog(ms, new String[] {});
		confirmEvaluate(ms, "A1", 25);
		confirmLog(ms, new String[] {
			"hit A1 25",
		});

		ms.setCellValue("B1", 11); // value changing
		confirmLog(ms, new String[] {
			"clear1 A1 25",	// expect consuming formula cached result to get cleared
		});
		confirmEvaluate(ms, "A1", 24);
		confirmLog(ms, new String[] {
			"start A1 B1+C1",
			"hit B1 11",
			"hit C1 13",
			"end A1 24",
		});
	}

	/**
	 * Changing any input to a formula may cause the formula to 'use' a different set of cells.
	 * Functions like INDEX and OFFSET make this effect obvious, with functions like MATCH
	 * and VLOOKUP the effect can be subtle.  The presence of error values can also produce this
	 * effect in almost every function and operator.
	 */
	public void testSimpleWithDependencyChange() {

		MySheet ms = new MySheet();

		ms.setCellFormula("A1", "INDEX(C1:E1,1,B1)");
		ms.setCellValue("B1", 1);
		ms.setCellValue("C1", 17);
		ms.setCellValue("D1", 18);
		ms.setCellValue("E1", 19);
		ms.clearAllCachedResultValues();
		ms.getAndClearLog();

		confirmEvaluate(ms, "A1", 17);
		confirmLog(ms, new String[] {
			"start A1 INDEX(C1:E1,1,B1)",
			"value B1 1",
			"value C1 17",
			"end A1 17",
		});
		ms.setCellValue("B1", 2);
		ms.getAndClearLog();

		confirmEvaluate(ms, "A1", 18);
		confirmLog(ms, new String[] {
			"start A1 INDEX(C1:E1,1,B1)",
			"hit B1 2",
			"value D1 18",
			"end A1 18",
		});

		// change C1. Note - last time A1 evaluated C1 was not used
		ms.setCellValue("C1", 15);
		ms.getAndClearLog();
		confirmEvaluate(ms, "A1", 18);
		confirmLog(ms, new String[] {
			"hit A1 18",
		});

		// but A1 still uses D1, so if it changes...
		ms.setCellValue("D1", 25);
		ms.getAndClearLog();
		confirmEvaluate(ms, "A1", 25);
		confirmLog(ms, new String[] {
			"start A1 INDEX(C1:E1,1,B1)",
			"hit B1 2",
			"hit D1 25",
			"end A1 25",
		});

	}

	private static void confirmEvaluate(MySheet ms, String cellRefText, double expectedValue) {
		ValueEval v = ms.evaluateCell(cellRefText);
		assertEquals(NumberEval.class, v.getClass());
		assertEquals(expectedValue, ((NumberEval)v).getNumberValue(), 0.0);
	}

	private static void confirmLog(MySheet ms, String[] expectedLog) {
		String[] actualLog = ms.getAndClearLog();
		int endIx = actualLog.length;
		PrintStream ps = System.err;
		if (endIx != expectedLog.length) {
			ps.println("Log lengths mismatch");
			dumpCompare(ps, expectedLog, actualLog);
			throw new AssertionFailedError("Log lengths mismatch");
		}
		for (int i=0; i< endIx; i++) {
			if (!actualLog[i].equals(expectedLog[i])) {
				String msg = "Log entry mismatch at index " + i;
				ps.println(msg);
				dumpCompare(ps, expectedLog, actualLog);
				throw new AssertionFailedError(msg);
			}
		}

	}

	private static void dumpCompare(PrintStream ps, String[] expectedLog, String[] actualLog) {
		int max = Math.max(actualLog.length, expectedLog.length);
		ps.println("Index\tExpected\tActual");
		for(int i=0; i<max; i++) {
			ps.print(i + "\t");
			printItem(ps, expectedLog, i);
			ps.print("\t");
			printItem(ps, actualLog, i);
			ps.println();
		}
		ps.println();
		debugPrint(ps, actualLog);
	}

	private static void printItem(PrintStream ps, String[] ss, int index) {
		if (index < ss.length) {
			ps.print(ss[index]);
		}
	}

	private static void debugPrint(PrintStream ps, String[] log) {
		for (int i = 0; i < log.length; i++) {
			ps.println('"' + log[i] + "\",");

		}
	}
}
