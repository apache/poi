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

import org.apache.poi.hssf.record.formula.AreaI;
import org.apache.poi.hssf.record.formula.AreaI.OffsetArea;
import org.apache.poi.hssf.util.AreaReference;
import org.apache.poi.hssf.util.CellReference;

import junit.framework.TestCase;

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
		
		Eval[] args = {
			createRefEval(refA),	
			createRefEval(refB),	
		};
		AreaReference ar = new AreaReference(expectedAreaRef);
		Eval result = RangeEval.instance.evaluate(args, 0, (short)0);
		assertTrue(result instanceof AreaEval);
		AreaEval ae = (AreaEval) result;
		assertEquals(ar.getFirstCell().getRow(), ae.getFirstRow());
		assertEquals(ar.getLastCell().getRow(), ae.getLastRow());
		assertEquals(ar.getFirstCell().getCol(), ae.getFirstColumn());
		assertEquals(ar.getLastCell().getCol(), ae.getLastColumn());
	}

	private static Eval createRefEval(String refStr) {
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
		public ValueEval getRelativeValue(int relativeRowIndex, int relativeColumnIndex) {
			throw new RuntimeException("not expected to be called during this test");
		}
		public AreaEval offset(int relFirstRowIx, int relLastRowIx, int relFirstColIx,
				int relLastColIx) {
			throw new RuntimeException("not expected to be called during this test");
		}
	}
}
