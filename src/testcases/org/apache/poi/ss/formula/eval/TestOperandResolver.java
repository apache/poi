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

package org.apache.poi.ss.formula.eval;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tests for <tt>OperandResolver</tt>
 *
 * @author Brendan Nolan
 */
public final class TestOperandResolver extends TestCase {

	public void testParseDouble_bug48472() {
		final Double resolvedValue;
		
		try {
			resolvedValue = OperandResolver.parseDouble("-");
		} catch (StringIndexOutOfBoundsException e) { 
			throw new AssertionFailedError("Identified bug 48472");
		}

		assertNull(resolvedValue);
	}
	
	public void testParseDouble_bug49723() {
		String value = ".1";
		
		Double resolvedValue = OperandResolver.parseDouble(value);
		
		assertNotNull("Identified bug 49723", resolvedValue);
	}
	
	/**
	 * 
	 * Tests that a list of valid strings all return a non null value from {@link OperandResolver#parseDouble(String)}
	 * 
	 */
	public void testParseDoubleValidStrings() {
		String[] values = new String[]{".19", "0.19", "1.9", "1E4", "-.19", "-0.19",
				"8.5","-1E4", ".5E6","+1.5","+1E5", "  +1E5  ", " 123 ", "1E4", "-123" };

		for (String value : values) {
			assertNotNull(OperandResolver.parseDouble(value));
			assertEquals(OperandResolver.parseDouble(value), Double.parseDouble(value));
		}
	}
	
	/**
	 * 
	 * Tests that a list of invalid strings all return null from {@link OperandResolver#parseDouble(String)}
	 * 
	 */
	public void testParseDoubleInvalidStrings() {
		String[] values = new String[]{"-", "ABC", "-X", "1E5a", "Infinity", "NaN", ".5F", "1,000"};
		
		for (String value : values) {
			assertNull(OperandResolver.parseDouble(value));
		}
	}

	public void testCoerceDateStringToNumber() throws EvaluationException {
		Map<String, Double> values = new LinkedHashMap<>();
		values.put("2019/1/18", 43483.);
		values.put("01/18/2019", 43483.);
		values.put("18 Jan 2019", 43483.);
		values.put("18-Jan-2019", 43483.);

		for (String str : values.keySet()) {
			assertEquals(OperandResolver.coerceValueToDouble(new StringEval(str)), values.get(str), 0.00001);
		}
	}

	public void testCoerceTimeStringToNumber() throws EvaluationException {
		Map<String, Double> values = new LinkedHashMap<>();
		values.put("00:00", 0.0);
		values.put("12:00", 0.5);
		values.put("15:43:09", 0.654965278);
		values.put("15:43", 0.654861111);
		values.put("3:43 PM", 0.654861111);

		for (String str : values.keySet()) {
			assertEquals(OperandResolver.coerceValueToDouble(new StringEval(str)), values.get(str), 0.00001);
		}
	}
}
