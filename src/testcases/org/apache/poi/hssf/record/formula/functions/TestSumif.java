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

import org.apache.poi.hssf.record.formula.eval.AreaEval;
import org.apache.poi.hssf.record.formula.eval.NumberEval;
import org.apache.poi.hssf.record.formula.eval.NumericValueEval;
import org.apache.poi.hssf.record.formula.eval.StringEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;

/**
 * Test cases for SUMPRODUCT()
 *
 * @author Josh Micich
 */
public final class TestSumif extends TestCase {
	private static final NumberEval _30 = new NumberEval(30);
	private static final NumberEval _40 = new NumberEval(40);
	private static final NumberEval _50 = new NumberEval(50);
	private static final NumberEval _60 = new NumberEval(60);

	private static ValueEval invokeSumif(int rowIx, int colIx, ValueEval...args) {
		return new Sumif().evaluate(args, rowIx, colIx);
	}
	private static void confirmDouble(double expected, ValueEval actualEval) {
		if(!(actualEval instanceof NumericValueEval)) {
			throw new AssertionFailedError("Expected numeric result");
		}
		NumericValueEval nve = (NumericValueEval)actualEval;
		assertEquals(expected, nve.getNumberValue(), 0);
	}

	public void testBasic() {
		ValueEval[] arg0values = new ValueEval[] { _30, _30, _40, _40, _50, _50  };
		ValueEval[] arg2values = new ValueEval[] { _30, _40, _50, _60, _60, _60 };

		AreaEval arg0;
		AreaEval arg2;

		arg0 = EvalFactory.createAreaEval("A3:B5", arg0values);
		arg2 = EvalFactory.createAreaEval("D1:E3", arg2values);

		confirm(60.0, arg0, new NumberEval(30.0));
		confirm(70.0, arg0, new NumberEval(30.0), arg2);
		confirm(100.0, arg0, new StringEval(">45"));

	}
	private static void confirm(double expectedResult, ValueEval...args) {
		confirmDouble(expectedResult, invokeSumif(-1, -1, args));
	}


	/**
	 * test for bug observed near svn r882931
	 */
	public void testCriteriaArgRange() {
		ValueEval[] arg0values = new ValueEval[] { _50, _60, _50, _50, _50, _30,  };
		ValueEval[] arg1values = new ValueEval[] { _30, _40, _50, _60,  };

		AreaEval arg0;
		AreaEval arg1;
		ValueEval ve;

		arg0 = EvalFactory.createAreaEval("A3:B5", arg0values);
		arg1 = EvalFactory.createAreaEval("A2:D2", arg1values); // single row range

		ve = invokeSumif(0, 2, arg0, arg1);  // invoking from cell C1
		if (ve instanceof NumberEval) {
			NumberEval ne = (NumberEval) ve;
			if (ne.getNumberValue() == 30.0) {
				throw new AssertionFailedError("identified error in SUMIF - criteria arg not evaluated properly");
			}
		}

		confirmDouble(200, ve);

		arg0 = EvalFactory.createAreaEval("C1:D3", arg0values);
		arg1 = EvalFactory.createAreaEval("B1:B4", arg1values); // single column range

		ve = invokeSumif(3, 0, arg0, arg1); // invoking from cell A4

		confirmDouble(60, ve);
	}
}
