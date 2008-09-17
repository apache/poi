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

package org.apache.poi.hssf.record.formula;

import junit.framework.TestCase;

/**
 * Tests for {@link FormulaShifter}.
 *
 * @author Josh Micich
 */
public final class TestFormulaShifter extends TestCase {
	// Note - the expected result row coordinates here were determined/verified
	// in Excel 2007 by manually testing.

	/**
	 * Tests what happens to area refs when a range of rows from inside, or overlapping are
	 * moved
	 */
	public void testShiftAreasSourceRows() {

		// all these operations are on an area ref spanning rows 10 to 20
		AreaPtg aptg  = createAreaPtg(10, 20);

		confirmAreaShift(aptg,  9, 21, 20, 30, 40);
		confirmAreaShift(aptg, 10, 21, 20, 30, 40);
		confirmAreaShift(aptg,  9, 20, 20, 30, 40);

		confirmAreaShift(aptg, 8, 11,  -3, 7, 20); // simple expansion of top
		// rows containing area top being shifted down:
		confirmAreaShift(aptg, 8, 11,  3, 13, 20);
		confirmAreaShift(aptg, 8, 11,  7, 17, 20);
		confirmAreaShift(aptg, 8, 11,  8, 18, 20);
		confirmAreaShift(aptg, 8, 11,  9, 12, 20); // note behaviour changes here
		confirmAreaShift(aptg, 8, 11, 10, 12, 21);
		confirmAreaShift(aptg, 8, 11, 12, 12, 23);
		confirmAreaShift(aptg, 8, 11, 13, 10, 20);  // ignored

		// rows from within being moved:
		confirmAreaShift(aptg, 12, 16,  3, 10, 20);  // stay within - no change
		confirmAreaShift(aptg, 11, 19, 20, 10, 20);  // move completely out - no change
		confirmAreaShift(aptg, 16, 17, -6, 10, 20);  // moved exactly to top - no change
		confirmAreaShift(aptg, 16, 17, -7, 11, 20);  // truncation at top
		confirmAreaShift(aptg, 12, 16,  4, 10, 20);  // moved exactly to bottom - no change
		confirmAreaShift(aptg, 12, 16,  6, 10, 17);  // truncation at bottom

		// rows containing area bottom being shifted up:
		confirmAreaShift(aptg, 18, 22, -1, 10, 19); // simple contraction at bottom
		confirmAreaShift(aptg, 18, 22, -7, 10, 13); // simple contraction at bottom
		confirmAreaShift(aptg, 18, 22, -8, 10, 17); // top calculated differently here
		confirmAreaShift(aptg, 18, 22, -9,  9, 17);
		confirmAreaShift(aptg, 18, 22,-15, 10, 20); // no change because range would be turned inside out
		confirmAreaShift(aptg, 15, 19, -7, 13, 20); // dest truncates top (even though src is from inside range)
		confirmAreaShift(aptg, 19, 23,-12,  7, 18); // complex: src encloses bottom, dest encloses top

		confirmAreaShift(aptg, 18, 22,  5, 10, 25); // simple expansion at bottom
	}
	/**
	 * Tests what happens to an area ref when some outside rows are moved to overlap
	 * that area ref
	 */
	public void testShiftAreasDestRows() {
		// all these operations are on an area ref spanning rows 20 to 25
		AreaPtg aptg  = createAreaPtg(20, 25);

		// no change because no overlap:
		confirmAreaShift(aptg,  5, 10,  9, 20, 25);
		confirmAreaShift(aptg,  5, 10, 21, 20, 25);

		confirmAreaShift(aptg, 11, 14, 10, 20, 25);

		confirmAreaShift(aptg,   7, 17, 10, -1, -1); // converted to DeletedAreaRef
		confirmAreaShift(aptg,   5, 15,  7, 23, 25); // truncation at top
		confirmAreaShift(aptg,  13, 16, 10, 20, 22); // truncation at bottom
	}

	private static void confirmAreaShift(AreaPtg aptg,
			int firstRowMoved, int lastRowMoved, int numberRowsMoved,
			int expectedAreaFirstRow, int expectedAreaLastRow) {

		FormulaShifter fs = FormulaShifter.createForRowShift(0, firstRowMoved, lastRowMoved, numberRowsMoved);
		boolean expectedChanged = aptg.getFirstRow() != expectedAreaFirstRow || aptg.getLastRow() != expectedAreaLastRow;

		AreaPtg copyPtg = (AreaPtg) aptg.copy(); // clone so we can re-use aptg in calling method
		Ptg[] ptgs = { copyPtg, };
		boolean actualChanged = fs.adjustFormula(ptgs, 0);
		if (expectedAreaFirstRow < 0) {
			assertEquals(AreaErrPtg.class, ptgs[0].getClass());
			return;
		}
		assertEquals(expectedChanged, actualChanged);
		assertEquals(copyPtg, ptgs[0]);  // expected to change in place (although this is not a strict requirement)
		assertEquals(expectedAreaFirstRow, copyPtg.getFirstRow());
		assertEquals(expectedAreaLastRow, copyPtg.getLastRow());

	}
	private static AreaPtg createAreaPtg(int initialAreaFirstRow, int initialAreaLastRow) {
		return new AreaPtg(initialAreaFirstRow, initialAreaLastRow, 2, 5, false, false, false, false);
	}
}
