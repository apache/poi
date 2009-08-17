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

import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.NumberEval;
import org.apache.poi.hssf.record.formula.eval.StringEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;

/**
 * Tests for {@link Value}
 *
 * @author Josh Micich
 */
public final class TestValue extends TestCase {

	private static ValueEval invokeValue(String strText) {
		ValueEval[] args = new ValueEval[] { new StringEval(strText), };
		return new Value().evaluate(args, -1, (short) -1);
	}

	private static void confirmValue(String strText, double expected) {
		ValueEval result = invokeValue(strText);
		assertEquals(NumberEval.class, result.getClass());
		assertEquals(expected, ((NumberEval) result).getNumberValue(), 0.0);
	}

	private static void confirmValueError(String strText) {
		ValueEval result = invokeValue(strText);
		assertEquals(ErrorEval.class, result.getClass());
		assertEquals(ErrorEval.VALUE_INVALID, result);
	}

	public void testBasic() {

		confirmValue("100", 100);
		confirmValue("-2.3", -2.3);
		confirmValue(".5", 0.5);
		confirmValue(".5e2", 50);
		confirmValue(".5e-2", 0.005);
		confirmValue(".5e+2", 50);
		confirmValue("+5", 5);
		confirmValue("$1,000", 1000);
		confirmValue("100.5e1", 1005);
		confirmValue("1,0000", 10000);
		confirmValue("1,000,0000", 10000000);
		confirmValue("1,000,0000,00000", 1000000000000.0);
		confirmValue(" 100 ", 100);
		confirmValue(" + 100", 100);
		confirmValue("10000", 10000);
		confirmValue("$-5", -5);
		confirmValue("$.5", 0.5);
		confirmValue("123e+5", 12300000);
		confirmValue("1,000e2", 100000);
		confirmValue("$10e2", 1000);
		confirmValue("$1,000e2", 100000);
	}

	public void testErrors() {
		confirmValueError("1+1");
		confirmValueError("1 1");
		confirmValueError("1,00.0");
		confirmValueError("1,00");
		confirmValueError("$1,00.5e1");
		confirmValueError("1,00.5e1");
		confirmValueError("1,0,000");
		confirmValueError("1,00,000");
		confirmValueError("++100");
		confirmValueError("$$5");
		confirmValueError("-");
		confirmValueError("+");
		confirmValueError("$");
		confirmValueError(",300");
		confirmValueError("0.233,4");
		confirmValueError("1e2.5");
	}
}
