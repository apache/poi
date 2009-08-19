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

import junit.framework.TestCase;

import org.apache.poi.hssf.record.formula.functions.EvalFactory;
import org.apache.poi.hssf.record.formula.functions.NumericFunctionInvoker;

/**
 * Test for divide operator evaluator.
 *
 * @author Josh Micich
 */
public final class TestDivideEval extends TestCase {

	private static void confirm(ValueEval arg0, ValueEval arg1, double expectedResult) {
		ValueEval[] args = {
			arg0, arg1,
		};

		double result = NumericFunctionInvoker.invoke(EvalInstances.Divide, args, 0, 0);

		assertEquals(expectedResult, result, 0);
	}

	public void testBasic() {
		confirm(new NumberEval(5), new NumberEval(2), 2.5);
		confirm(new NumberEval(3), new NumberEval(16), 0.1875);
		confirm(new NumberEval(-150), new NumberEval(-15), 10.0);
		confirm(new StringEval("0.2"), new NumberEval(0.05), 4.0);
		confirm(BoolEval.TRUE, new StringEval("-0.2"), -5.0);
	}

	public void test1x1Area() {
		AreaEval ae0 = EvalFactory.createAreaEval("B2:B2", new ValueEval[] { new NumberEval(50), });
		AreaEval ae1 = EvalFactory.createAreaEval("C2:C2", new ValueEval[] { new NumberEval(10), });
		confirm(ae0, ae1, 5);
	}
	public void testDivZero() {
		ValueEval[] args = {
			new NumberEval(5), NumberEval.ZERO,
		};
		ValueEval result = EvalInstances.Divide.evaluate(args, 0, (short) 0);
		assertEquals(ErrorEval.DIV_ZERO, result);
	}
}
