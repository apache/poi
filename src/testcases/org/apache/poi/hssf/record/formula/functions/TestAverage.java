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

import junit.framework.TestCase;

import org.apache.poi.hssf.record.formula.eval.BlankEval;
import org.apache.poi.hssf.record.formula.eval.BoolEval;
import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.NumberEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;
/**
 * Tests for Excel function AVERAGE()
 *
 * @author Josh Micich
 */
public final class TestAverage extends TestCase {

	private static ValueEval invokeAverage(ValueEval[] args) {
		return AggregateFunction.AVERAGE.evaluate(args, -1, (short)-1);
	}

	private void confirmAverage(ValueEval[] args, double expected) {
		ValueEval result = invokeAverage(args);
		assertEquals(NumberEval.class, result.getClass());
		assertEquals(expected, ((NumberEval)result).getNumberValue(), 0);
	}

	private void confirmAverage(ValueEval[] args, ErrorEval expectedError) {
		ValueEval result = invokeAverage(args);
		assertEquals(ErrorEval.class, result.getClass());
		assertEquals(expectedError.getErrorCode(), ((ErrorEval)result).getErrorCode());
	}

	public void testBasic() {

		ValueEval[] values = {
				new NumberEval(1),
				new NumberEval(2),
				new NumberEval(3),
				new NumberEval(4),
		};

		confirmAverage(values, 2.5);

		values = new ValueEval[] {
				new NumberEval(1),
				new NumberEval(2),
				BlankEval.instance,
				new NumberEval(3),
				BlankEval.instance,
				new NumberEval(4),
				BlankEval.instance,
		};

		confirmAverage(values, 2.5);
	}

	/**
	 * Valid cases where values are not pure numbers
	 */
	public void testUnusualArgs() {
		ValueEval[] values = {
				new NumberEval(1),
				new NumberEval(2),
				BoolEval.TRUE,
				BoolEval.FALSE,
		};

		confirmAverage(values, 1.0);

	}

	public void testErrors() {
		ValueEval[] values = {
				new NumberEval(1),
				ErrorEval.NAME_INVALID,
				new NumberEval(3),
				ErrorEval.DIV_ZERO,
		};
		confirmAverage(values, ErrorEval.NAME_INVALID);
	}
}
