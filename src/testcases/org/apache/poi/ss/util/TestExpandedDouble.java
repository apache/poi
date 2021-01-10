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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ExpandedDouble}
 */
final class TestExpandedDouble {
	private static final BigInteger BIG_POW_10 = BigInteger.valueOf(1000000000);

	@Test
	void testNegative() {
		ExpandedDouble hd = new ExpandedDouble(0xC010000000000000L);
		assertNotEquals(-2046, hd.getBinaryExponent(), "identified bug - sign bit not masked out of exponent");
		assertEquals(2, hd.getBinaryExponent());
		BigInteger frac = hd.getSignificand();
		assertEquals(64, frac.bitLength());
		assertEquals(1, frac.bitCount());
	}

	@Test
	void testSubnormal() {
		ExpandedDouble hd = new ExpandedDouble(0x0000000000000001L);
		assertNotEquals(-1023, hd.getBinaryExponent(), "identified bug - subnormal numbers not decoded properly");
		assertEquals(-1086, hd.getBinaryExponent());
		BigInteger frac = hd.getSignificand();
		assertEquals(64, frac.bitLength());
		assertEquals(1, frac.bitCount());
	}

	/**
	 * Tests specific values for conversion from {@link ExpandedDouble} to {@link NormalisedDecimal} and back
	 */
	@Test
	void testRoundTripShifting() {
		long[] rawValues = {
				0x4010000000000004L,
				0x7010000000000004L,
				0x1010000000000004L,
				0x0010000000000001L, // near lowest normal number
				0x0010000000000000L, // lowest normal number
				0x000FFFFFFFFFFFFFL, // highest subnormal number
				0x0008000000000000L, // subnormal number

				0xC010000000000004L,
				0xE230100010001004L,
				0x403CE0FFFFFFFFF2L,
				0x0000000000000001L, // smallest non-zero number (subnormal)
				0x6230100010000FFEL,
				0x6230100010000FFFL,
				0x6230100010001000L,
				0x403CE0FFFFFFFFF0L, // has single digit round trip error
				0x2B2BFFFF10001079L,
		};
		for (int i = 0; i < rawValues.length; i++) {
			confirmRoundTrip(i, rawValues[i]);
		}
	}

	public static void confirmRoundTrip(int i, long rawBitsA) {
		double a = Double.longBitsToDouble(rawBitsA);
		if (a == 0.0) {
			// Can't represent 0.0 or -0.0 with NormalisedDecimal
			return;
		}
		ExpandedDouble ed1 = new ExpandedDouble(rawBitsA);
		NormalisedDecimal nd2 = ed1.normaliseBaseTen();
		checkNormaliseBaseTenResult(ed1, nd2);

		ExpandedDouble ed3 = nd2.normaliseBaseTwo();
		assertEquals(ed3.getBinaryExponent(), ed1.getBinaryExponent(), "bin exp mismatch");

		BigInteger diff = ed3.getSignificand().subtract(ed1.getSignificand()).abs();
		if (diff.signum() == 0) {
			return;
		}
		// original quantity only has 53 bits of precision
		// these quantities may have errors in the 64th bit, which hopefully don't make any difference

		// errors in the 64th bit happen from time to time
		// this is well below the 53 bits of precision required
		assertTrue(diff.bitLength() < 2);
	}


	private static void checkNormaliseBaseTenResult(ExpandedDouble orig, NormalisedDecimal result) {
		String sigDigs = result.getSignificantDecimalDigits();
		BigInteger frac = orig.getSignificand();
		while (frac.bitLength() + orig.getBinaryExponent() < 200) {
			frac = frac.multiply(BIG_POW_10);
		}
		int binaryExp = orig.getBinaryExponent() - orig.getSignificand().bitLength();

		String origDigs = frac.shiftLeft(binaryExp+1).toString(10);
		assertTrue(origDigs.startsWith(sigDigs));

		double dO = Double.parseDouble("0." + origDigs.substring(sigDigs.length()));
		double d1 = Double.parseDouble(result.getFractionalPart().toPlainString());
		BigInteger subDigsO = BigInteger.valueOf((int) (dO * 32768 + 0.5));
		BigInteger subDigsB = BigInteger.valueOf((int) (d1 * 32768 + 0.5));

		if (subDigsO.equals(subDigsB)) {
			return;
		}
		BigInteger diff = subDigsB.subtract(subDigsO).abs();
		// 100/32768 ~= 0.003
		assertTrue(diff.intValue() <= 100, "minor mistake");
	}
}
