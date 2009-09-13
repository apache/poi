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
import org.apache.poi.hssf.record.formula.eval.ValueEval;
import org.apache.poi.hssf.record.formula.eval.NumberEval;
import org.apache.poi.hssf.record.formula.eval.StringEval;

/**
 * Test cases for ROUND(), ROUNDUP(), ROUNDDOWN()
 *
 * @author Josh Micich
 */
public final class TestRoundFuncs extends TestCase {
	private static final NumericFunction F = null;
	public void testRounddownWithStringArg() {

		ValueEval strArg = new StringEval("abc");
		ValueEval[] args = { strArg, new NumberEval(2), };
		ValueEval result = F.ROUNDDOWN.evaluate(args, -1, (short)-1);
		assertEquals(ErrorEval.VALUE_INVALID, result);
	}

	public void testRoundupWithStringArg() {

		ValueEval strArg = new StringEval("abc");
		ValueEval[] args = { strArg, new NumberEval(2), };
		ValueEval result = F.ROUNDUP.evaluate(args, -1, (short)-1);
		assertEquals(ErrorEval.VALUE_INVALID, result);
	}

}
