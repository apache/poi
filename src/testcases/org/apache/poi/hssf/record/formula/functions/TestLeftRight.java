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

import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.NumberEval;
import org.apache.poi.hssf.record.formula.eval.StringEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;

import junit.framework.TestCase;

/**
 * 
 * Test cases for {@link TextFunction#LEFT} and {@link TextFunction#RIGHT}
 * 
 * @author Brendan Nolan
 *
 */
public class TestLeftRight extends TestCase {

	private static final NumberEval NEGATIVE_OPERAND = new NumberEval(-1.0);
	private static final StringEval ANY_STRING_VALUE = new StringEval("ANYSTRINGVALUE");

	
	private static ValueEval invokeLeft(ValueEval text, ValueEval operand) {
		ValueEval[] args = new ValueEval[] { text, operand };
		return TextFunction.LEFT.evaluate(args, -1, (short)-1);
	}
	
	private static ValueEval invokeRight(ValueEval text, ValueEval operand) {
		ValueEval[] args = new ValueEval[] { text, operand };
		return TextFunction.RIGHT.evaluate(args, -1, (short)-1);
	}
	
	public void testLeftRight_bug49841() {

		try {
			invokeLeft(ANY_STRING_VALUE, NEGATIVE_OPERAND);
			invokeRight(ANY_STRING_VALUE, NEGATIVE_OPERAND);
		} catch (StringIndexOutOfBoundsException e) {
			fail("Identified bug 49841");
		}

	}
	
	public void testLeftRightNegativeOperand() {
		
		assertEquals(ErrorEval.VALUE_INVALID, invokeRight(ANY_STRING_VALUE, NEGATIVE_OPERAND));		
		assertEquals(ErrorEval.VALUE_INVALID, invokeLeft(ANY_STRING_VALUE, NEGATIVE_OPERAND));

	}
	
	
	
	

	
}
