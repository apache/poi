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

import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.StringEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test case for TRUNC()
 *
 * @author Stephen Wolke (smwolke at geistig.com)
 */
public final class TestTrunc extends BaseTestNumeric {
	private static final NumericFunction F = null;

	@Test
	public void testTruncWithStringArg() {

		ValueEval strArg = new StringEval("abc");
		ValueEval[] args = { strArg, new NumberEval(2) };
		ValueEval result = NumericFunction.TRUNC.evaluate(args, -1, (short)-1);
		Assert.assertEquals(ErrorEval.VALUE_INVALID, result);
	}

	@Test
	public void testTruncWithWholeNumber() {
		ValueEval[] args = { new NumberEval(200), new NumberEval(2) };
		@SuppressWarnings("static-access")
		ValueEval result = F.TRUNC.evaluate(args, -1, (short)-1);
		assertEquals("TRUNC", (new NumberEval(200d)).getNumberValue(), ((NumberEval)result).getNumberValue());
	}

	@Test
	public void testTruncWithDecimalNumber() {
		ValueEval[] args = { new NumberEval(2.612777), new NumberEval(3) };
		@SuppressWarnings("static-access")
		ValueEval result = F.TRUNC.evaluate(args, -1, (short)-1);
		assertEquals("TRUNC", (new NumberEval(2.612d)).getNumberValue(), ((NumberEval)result).getNumberValue());
	}

	@Test
	public void testTruncWithProblematicDecimalNumber() {
		ValueEval[] args = { new NumberEval(0.29), new NumberEval(2) };
		ValueEval result = NumericFunction.TRUNC.evaluate(args, -1, (short)-1);
		assertEquals("TRUNC", (new NumberEval(0.29d)).getNumberValue(), ((NumberEval)result).getNumberValue());
	}

	@Test
	public void testTruncWithProblematicCalculationResult() {

		ValueEval[] args = { new NumberEval(21.624d / 24d + .009d), new NumberEval(2) };
		ValueEval result = NumericFunction.TRUNC.evaluate(args, -1, (short)-1);
		assertEquals("TRUNC", (new NumberEval(0.91d)).getNumberValue(), ((NumberEval)result).getNumberValue());
	}

	@Test
	public void testTruncWithDecimalNumberOneArg() {
		ValueEval[] args = { new NumberEval(2.612777) };
		ValueEval result = NumericFunction.TRUNC.evaluate(args, -1, (short)-1);
		assertEquals("TRUNC", (new NumberEval(2d)).getNumberValue(), ((NumberEval)result).getNumberValue());
	}

	@Test
    public void testNegative() {
        ValueEval[] args = { new NumberEval(-8.9), new NumberEval(0) };
        @SuppressWarnings("static-access")
        ValueEval result = F.TRUNC.evaluate(args, -1, (short)-1);
        assertEquals("TRUNC", (new NumberEval(-8)).getNumberValue(), ((NumberEval)result).getNumberValue());
    }
}
