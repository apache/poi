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

package org.apache.poi.ss.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.poi.ss.util.NumberComparisonExamples.ComparisonExample;
import org.apache.poi.util.HexDump;
import org.junit.Test;
/**
 * Tests for {@link NumberComparer}
 *
 * @author Josh Micich
 */
public final class TestNumberComparer {

    @Test
	public void testAllComparisonExamples() {
		ComparisonExample[] examples = NumberComparisonExamples.getComparisonExamples();
		boolean success = true;

		for(int i=0;i<examples.length; i++) {
			ComparisonExample ce = examples[i];
			success &= confirm(i, ce.getA(), ce.getB(), +ce.getExpectedResult());
			success &= confirm(i, ce.getB(), ce.getA(), -ce.getExpectedResult());
			success &= confirm(i, ce.getNegA(), ce.getNegB(), -ce.getExpectedResult());
			success &= confirm(i, ce.getNegB(), ce.getNegA(), +ce.getExpectedResult());
		}
		
		assertTrue("One or more cases failed.  See stderr", success);
	}

    @Test
	public void testRoundTripOnComparisonExamples() {
		ComparisonExample[] examples = NumberComparisonExamples.getComparisonExamples();
		boolean success = true;
		for(int i=0;i<examples.length; i++) {
			ComparisonExample ce = examples[i];
			success &= confirmRoundTrip(i, ce.getA());
			success &= confirmRoundTrip(i, ce.getNegA());
			success &= confirmRoundTrip(i, ce.getB());
			success &= confirmRoundTrip(i, ce.getNegB());
		}
		
		assertTrue("One or more cases failed.  See stderr", success);
	}

	private boolean confirmRoundTrip(int i, double a) {
		return TestExpandedDouble.confirmRoundTrip(i, Double.doubleToLongBits(a));
	}

	/**
	 * The actual example from bug 47598
	 */
	@Test
	public void testSpecificExampleA() {
		double a = 0.06-0.01;
		double b = 0.05;
		//noinspection ConstantConditions
		assertFalse(a == b);
		assertEquals(0, NumberComparer.compare(a, b));
	}

	/**
	 * The example from the nabble posting
	 */
	@Test
	public void testSpecificExampleB() {
		double a = 1+1.0028-0.9973;
		double b = 1.0055;
		//noinspection ConstantConditions
		assertFalse(a == b);
		assertEquals(0, NumberComparer.compare(a, b));
	}

	private static boolean confirm(int i, double a, double b, int expRes) {
		int actRes = NumberComparer.compare(a, b);

		int sgnActRes = Integer.compare(actRes, 0);
		if (sgnActRes != expRes) {
			System.err.println("Mismatch example[" + i + "] ("
					+ formatDoubleAsHex(a) + ", " + formatDoubleAsHex(b) + ") expected "
					+ expRes + " but got " + sgnActRes);
			return false;
		}
		return true;
	}
	private static String formatDoubleAsHex(double d) {
		long l = Double.doubleToLongBits(d);
		return HexDump.longToHex(l)+'L';
	}
}
