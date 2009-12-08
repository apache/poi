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

import org.apache.poi.hssf.record.formula.AreaPtg;
import org.apache.poi.hssf.record.formula.functions.EvalFactory;

/**
 * Tests for <tt>AreaEval</tt>
 *
 * @author Josh Micich
 */
public final class TestAreaEval extends TestCase {

	public void testGetValue_bug44950() {
		// TODO - this test probably isn't testing much anymore
		AreaPtg ptg = new AreaPtg("B2:D3");
		NumberEval one = new NumberEval(1);
		ValueEval[] values = {
				one,
				new NumberEval(2),
				new NumberEval(3),
				new NumberEval(4),
				new NumberEval(5),
				new NumberEval(6),
		};
		AreaEval ae = EvalFactory.createAreaEval(ptg, values);
		if (one == ae.getAbsoluteValue(1, 2)) {
			throw new AssertionFailedError("Identified bug 44950 a");
		}
		confirm(1, ae, 1, 1);
		confirm(2, ae, 1, 2);
		confirm(3, ae, 1, 3);
		confirm(4, ae, 2, 1);
		confirm(5, ae, 2, 2);
		confirm(6, ae, 2, 3);

	}

	private static void confirm(int expectedValue, AreaEval ae, int row, int col) {
		NumberEval v = (NumberEval) ae.getAbsoluteValue(row, col);
		assertEquals(expectedValue, v.getNumberValue(), 0.0);
	}
}
