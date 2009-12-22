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

package org.apache.poi.hssf.record.formula.eval;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.poi.hssf.record.formula.AreaI;
import org.apache.poi.hssf.record.formula.AreaI.OffsetArea;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.AreaReference;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.ss.formula.TwoDEval;
import org.apache.poi.ss.usermodel.CellValue;

/**
 * Test for unary plus operator evaluator.
 *
 * @author Josh Micich
 */
public final class TestRangeEval extends TestCase {

	public void testPermutations() {

		confirm("B3", "D7", "B3:D7");
		confirm("B1", "B1", "B1:B1");

		confirm("B7", "D3", "B3:D7");
		confirm("D3", "B7", "B3:D7");
		confirm("D7", "B3", "B3:D7");
	}

	private static void confirm(String refA, String refB, String expectedAreaRef) {

		ValueEval[] args = {
			createRefEval(refA),
			createRefEval(refB),
		};
		AreaReference ar = new AreaReference(expectedAreaRef);
		ValueEval result = EvalInstances.Range.evaluate(args, 0, (short)0);
		assertTrue(result instanceof AreaEval);
		AreaEval ae = (AreaEval) result;
		assertEquals(ar.getFirstCell().getRow(), ae.getFirstRow());
		assertEquals(ar.getLastCell().getRow(), ae.getLastRow());
		assertEquals(ar.getFirstCell().getCol(), ae.getFirstColumn());
		assertEquals(ar.getLastCell().getCol(), ae.getLastColumn());
	}

	private static ValueEval createRefEval(String refStr) {
		CellReference cr = new CellReference(refStr);
		return new MockRefEval(cr.getRow(), cr.getCol());

	}

	private static final class MockRefEval extends RefEvalBase {

		public MockRefEval(int rowIndex, int columnIndex) {
			super(rowIndex, columnIndex);
		}
		public ValueEval getInnerValueEval() {
			throw new RuntimeException("not expected to be called during this test");
		}
		public AreaEval offset(int relFirstRowIx, int relLastRowIx, int relFirstColIx,
				int relLastColIx) {
			AreaI area = new OffsetArea(getRow(), getColumn(),
					relFirstRowIx, relLastRowIx, relFirstColIx, relLastColIx);
			return new MockAreaEval(area);
		}
	}

	private static final class MockAreaEval extends AreaEvalBase {

		public MockAreaEval(AreaI ptg) {
			super(ptg);
		}
		private MockAreaEval(int firstRow, int firstColumn, int lastRow, int lastColumn) {
			super(firstRow, firstColumn, lastRow, lastColumn);
		}
		public ValueEval getRelativeValue(int relativeRowIndex, int relativeColumnIndex) {
			throw new RuntimeException("not expected to be called during this test");
		}
		public AreaEval offset(int relFirstRowIx, int relLastRowIx, int relFirstColIx,
				int relLastColIx) {
			AreaI area = new OffsetArea(getFirstRow(), getFirstColumn(),
					relFirstRowIx, relLastRowIx, relFirstColIx, relLastColIx);

			return new MockAreaEval(area);
		}
		public TwoDEval getRow(int rowIndex) {
			if (rowIndex >= getHeight()) {
				throw new IllegalArgumentException("Invalid rowIndex " + rowIndex
						+ ".  Allowable range is (0.." + getHeight() + ").");
			}
			return new MockAreaEval(rowIndex, getFirstColumn(), rowIndex, getLastColumn());
		}
		public TwoDEval getColumn(int columnIndex) {
			if (columnIndex >= getWidth()) {
				throw new IllegalArgumentException("Invalid columnIndex " + columnIndex
						+ ".  Allowable range is (0.." + getWidth() + ").");
			}
			return new MockAreaEval(getFirstRow(), columnIndex, getLastRow(), columnIndex);
		}
	}

	public void testRangeUsingOffsetFunc_bug46948() {
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFRow row = wb.createSheet("Sheet1").createRow(0);
		HSSFCell cellA1 = row.createCell(0);
		HSSFCell cellB1 = row.createCell(1);
		row.createCell(2).setCellValue(5.0); // C1
		row.createCell(3).setCellValue(7.0); // D1
		row.createCell(4).setCellValue(9.0); // E1


		cellA1.setCellFormula("SUM(C1:OFFSET(C1,0,B1))");

		cellB1.setCellValue(1.0); // range will be C1:D1

		HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
		CellValue cv;
		try {
			cv = fe.evaluate(cellA1);
		} catch (IllegalArgumentException e) {
			if (e.getMessage().equals("Unexpected ref arg class (org.apache.poi.ss.formula.LazyAreaEval)")) {
				throw new AssertionFailedError("Identified bug 46948");
			}
			throw e;
		}

		assertEquals(12.0, cv.getNumberValue(), 0.0);

		cellB1.setCellValue(2.0); // range will be C1:E1
		fe.notifyUpdateCell(cellB1);
		cv = fe.evaluate(cellA1);
		assertEquals(21.0, cv.getNumberValue(), 0.0);

		cellB1.setCellValue(0.0); // range will be C1:C1
		fe.notifyUpdateCell(cellB1);
		cv = fe.evaluate(cellA1);
		assertEquals(5.0, cv.getNumberValue(), 0.0);
	}
}
