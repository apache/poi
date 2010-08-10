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

/**
 * Tests for <tt>OperandResolver</tt>
 *
 * @author Brendan Nolan
 */
public final class TestOperandResolver extends TestCase {

	public void testParseDouble_bug48472() {
		
		String value = "-";
		
		Double resolvedValue = null;
		
		try {
			resolvedValue = OperandResolver.parseDouble(value);
		} catch (StringIndexOutOfBoundsException e) { 
			throw new AssertionFailedError("Identified bug 48472");
		}
		
		assertEquals(null, resolvedValue);
		
	}
	
	public void testParseDouble_bug49723() {
		
		String value = ".1";
		
		Double resolvedValue = null;
		
		resolvedValue = OperandResolver.parseDouble(value);
		
		assertNotNull("Identified bug 49723", resolvedValue);
		
	}
	
	/**
	 * 
	 * Tests that a list of valid strings all return a non null value from {@link OperandResolver#parseDouble(String)}
	 * 
	 */
	public void testParseDoubleValidStrings() {
				
		String[] values = new String[]{".19", "0.19", "1.9", "1E4", "-.19", "-0.19", "8.5","-1E4", ".5E6","+1.5","+1E5", "  +1E5  "};
		
		for (String value : values) {
			assertTrue(OperandResolver.parseDouble(value) != null);
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
			assertEquals(null, OperandResolver.parseDouble(value));
		}

	}
	
}
