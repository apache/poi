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

package org.apache.poi.hssf.record.formula.functions;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.poi.hssf.record.formula.eval.EvaluationException;
import org.apache.poi.hssf.record.formula.eval.NumberEval;
import org.apache.poi.hssf.record.formula.functions.Offset.LinearOffsetRange;

/**
 * Tests for OFFSET function implementation
 *
 * @author Josh Micich
 */
public final class TestOffset extends TestCase {

	private static void confirmDoubleConvert(double doubleVal, int expected) {
		try {
			assertEquals(expected, Offset.evaluateIntArg(new NumberEval(doubleVal), -1, -1));
		} catch (EvaluationException e) {
			throw new AssertionFailedError("Unexpected error '" + e.getErrorEval().toString() + "'.");
		}
	}
	/**
	 * Excel's double to int conversion (for function 'OFFSET()') behaves more like Math.floor().
	 * Note - negative values are not symmetrical
	 * Fractional values are silently truncated.
	 * Truncation is toward negative infinity.
	 */
	public void testDoubleConversion() {

		confirmDoubleConvert(100.09, 100);
		confirmDoubleConvert(100.01, 100);
		confirmDoubleConvert(100.00, 100);
		confirmDoubleConvert(99.99, 99);

		confirmDoubleConvert(+2.01, +2);
		confirmDoubleConvert(+2.00, +2);
		confirmDoubleConvert(+1.99, +1);
		confirmDoubleConvert(+1.01, +1);
		confirmDoubleConvert(+1.00, +1);
		confirmDoubleConvert(+0.99,  0);
		confirmDoubleConvert(+0.01,  0);
		confirmDoubleConvert( 0.00,  0);
		confirmDoubleConvert(-0.01, -1);
		confirmDoubleConvert(-0.99, -1);
		confirmDoubleConvert(-1.00, -1);
		confirmDoubleConvert(-1.01, -2);
		confirmDoubleConvert(-1.99, -2);
		confirmDoubleConvert(-2.00, -2);
		confirmDoubleConvert(-2.01, -3);
	}

	public void testLinearOffsetRange() {
		LinearOffsetRange lor;

		lor = new LinearOffsetRange(3, 2);
		assertEquals(3, lor.getFirstIndex());
		assertEquals(4, lor.getLastIndex());
		lor = lor.normaliseAndTranslate(0); // expected no change
		assertEquals(3, lor.getFirstIndex());
		assertEquals(4, lor.getLastIndex());

		lor = lor.normaliseAndTranslate(5);
		assertEquals(8, lor.getFirstIndex());
		assertEquals(9, lor.getLastIndex());

		// negative length

		lor = new LinearOffsetRange(6, -4).normaliseAndTranslate(0);
		assertEquals(3, lor.getFirstIndex());
		assertEquals(6, lor.getLastIndex());


		// bounds checking
		lor = new LinearOffsetRange(0, 100);
		assertFalse(lor.isOutOfBounds(0, 16383));
		lor = lor.normaliseAndTranslate(16300);
		assertTrue(lor.isOutOfBounds(0, 16383));
		assertFalse(lor.isOutOfBounds(0, 65535));
	}
}
