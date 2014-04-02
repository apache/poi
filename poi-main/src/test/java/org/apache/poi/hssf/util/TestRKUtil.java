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

package org.apache.poi.hssf.util;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

/**
 * Tests the {@link RKUtil} class.
 */
public final class TestRKUtil extends TestCase {

	/**
	 * Check we can decode correctly.
	 */
	public void testDecode() {

		int[] values = { 1074266112, 1081384961, 1081397249, 
				0x3FF00000, 0x405EC001, 0x02F1853A, 0x02F1853B, 0xFCDD699A,
		};
		double[] rvalues = { 3.0, 3.3, 3.33,
				1, 1.23, 12345678, 123456.78, -13149594, 
		};

		for (int j = 0; j < values.length; j++) {

			int intBits = values[j];
			double expectedValue = rvalues[j];
			double actualValue = RKUtil.decodeNumber(intBits);
			if (expectedValue != actualValue) {
				throw new AssertionFailedError("0x" + Integer.toHexString(intBits)
						+ " should decode to " + expectedValue + " but got " + actualValue);
			}
		}
	}
}
