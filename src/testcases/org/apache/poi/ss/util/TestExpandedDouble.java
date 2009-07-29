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

import java.math.BigDecimal;
import java.math.BigInteger;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.poi.util.HexDump;
/**
 * Tests for {@link ExpandedDouble}
 *
 * @author Josh Micich
 */
public final class TestExpandedDouble extends TestCase {
	private static final BigInteger BIG_POW_10 = BigInteger.valueOf(1000000000);

	public void testNegative() {
		ExpandedDouble hd = new ExpandedDouble(0xC010000000000000L);

		if (hd.getBinaryExponent() == -2046) {
			throw new AssertionFailedError("identified bug - sign bit not masked out of exponent");
		}
		assertEquals(2, hd.getBinaryExponent());
		BigInteger frac = hd.getSignificand();
		assertEquals(64, frac.bitLength());
		assertEquals(1, frac.bitCount());
	}

	public void testSubnormal() {
		ExpandedDouble hd = new ExpandedDouble(0x0000000000000001L);

		if (hd.getBinaryExponent() == -1023) {
			throw new AssertionFailedError("identified bug - subnormal numbers not decoded properly");
		}
		assertEquals(-1086, hd.getBinaryExponent());
		BigInteger frac = hd.getSignificand();
		assertEquals(64, frac.bitLength());
		assertEquals(1, frac.bitCount());
	}

	/**
	 * Tests specific values for conversion from {@link ExpandedDouble} to {@link NormalisedDecimal} and back
	 */
	public void testRoundTripShifting() {
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
		boolean success = true;
		for (int i = 0; i < rawValues.length; i++) {
			success &= confirmRoundTrip(i, rawValues[i]);
		}
		if (!success) {
			throw new AssertionFailedError("One or more test examples failed.  See stderr.");
		}
	}
	public static boolean confirmRoundTrip(int i, long rawBitsA) {
		double a = Double.longBitsToDouble(rawBitsA);
		if (a == 0.0) {
			// Can't represent 0.0 or -0.0 with NormalisedDecimal
			return true;
		}
		ExpandedDouble ed1;
		NormalisedDecimal nd2;
		ExpandedDouble ed3;
		try {
			ed1 = new ExpandedDouble(rawBitsA);
			nd2 = ed1.normaliseBaseTen();
			checkNormaliseBaseTenResult(ed1, nd2);

			ed3 = nd2.normaliseBaseTwo();
		} catch (RuntimeException e) {
			System.err.println("example[" + i + "] ("
					+ formatDoubleAsHex(a) + ") exception:");
			e.printStackTrace();
			return false;
		}
		if (ed3.getBinaryExponent() != ed1.getBinaryExponent()) {
			System.err.println("example[" + i + "] ("
					+ formatDoubleAsHex(a) + ") bin exp mismatch");
			return false;
		}
		BigInteger diff = ed3.getSignificand().subtract(ed1.getSignificand()).abs();
		if (diff.signum() == 0) {
			return true;
		}
		// original quantity only has 53 bits of precision
		// these quantities may have errors in the 64th bit, which hopefully don't make any difference

		if (diff.bitLength() < 2) {
			// errors in the 64th bit happen from time to time
			// this is well below the 53 bits of precision required
			return true;
		}

		// but bigger errors are a concern
		System.out.println("example[" + i + "] ("
				+ formatDoubleAsHex(a) + ") frac mismatch: " + diff.toString());

		for (int j=-2; j<3; j++) {
			System.out.println((j<0?"":"+") + j + ": " + getNearby(ed1, j));
		}
		for (int j=-2; j<3; j++) {
			System.out.println((j<0?"":"+") + j + ": " + getNearby(nd2, j));
		}


		return false;
	}

	public static String getBaseDecimal(ExpandedDouble hd) {
		int gg = 64 - hd.getBinaryExponent() - 1;
		BigDecimal bd = new BigDecimal(hd.getSignificand()).divide(new BigDecimal(BigInteger.ONE.shiftLeft(gg)));
		int excessPrecision = bd.precision() - 23;
		if (excessPrecision > 0) {
			bd = bd.setScale(bd.scale() - excessPrecision, BigDecimal.ROUND_HALF_UP);
		}
		return bd.unscaledValue().toString();
	}
	public static BigInteger getNearby(NormalisedDecimal md, int offset) {
		BigInteger frac = md.composeFrac();
		int be = frac.bitLength() - 24 - 1;
		int sc = frac.bitLength() - 64;
		return getNearby(frac.shiftRight(sc), be, offset);
	}

	public static BigInteger getNearby(ExpandedDouble hd, int offset) {
		return getNearby(hd.getSignificand(), hd.getBinaryExponent(), offset);
	}

	private static BigInteger getNearby(BigInteger significand, int binExp, int offset) {
		int nExtraBits = 1;
		int nDec = (int) Math.round(3.0 + (64+nExtraBits) * Math.log10(2.0));
		BigInteger newFrac = significand.shiftLeft(nExtraBits).add(BigInteger.valueOf(offset));

		int gg = 64 + nExtraBits - binExp - 1;

		BigDecimal bd = new BigDecimal(newFrac);
		if (gg > 0) {
			bd = bd.divide(new BigDecimal(BigInteger.ONE.shiftLeft(gg)));
		} else {
			BigInteger frac = newFrac;
			while (frac.bitLength() + binExp < 180) {
				frac = frac.multiply(BigInteger.TEN);
			}
			int binaryExp = binExp - newFrac.bitLength() + frac.bitLength();

			bd = new BigDecimal( frac.shiftRight(frac.bitLength()-binaryExp-1));
		}
		int excessPrecision = bd.precision() - nDec;
		if (excessPrecision > 0) {
			bd = bd.setScale(bd.scale() - excessPrecision, BigDecimal.ROUND_HALF_UP);
		}
		return bd.unscaledValue();
	}

	private static void checkNormaliseBaseTenResult(ExpandedDouble orig, NormalisedDecimal result) {
		String sigDigs = result.getSignificantDecimalDigits();
		BigInteger frac = orig.getSignificand();
		while (frac.bitLength() + orig.getBinaryExponent() < 200) {
			frac = frac.multiply(BIG_POW_10);
		}
		int binaryExp = orig.getBinaryExponent() - orig.getSignificand().bitLength();

		String origDigs = frac.shiftLeft(binaryExp+1).toString(10);

		if (!origDigs.startsWith(sigDigs)) {
			throw new AssertionFailedError("Expected '" + origDigs + "' but got '" + sigDigs + "'.");
		}

		double dO = Double.parseDouble("0." + origDigs.substring(sigDigs.length()));
		double d1 = Double.parseDouble(result.getFractionalPart().toPlainString());
		BigInteger subDigsO = BigInteger.valueOf((int) (dO * 32768 + 0.5));
		BigInteger subDigsB = BigInteger.valueOf((int) (d1 * 32768 + 0.5));

		if (subDigsO.equals(subDigsB)) {
			return;
		}
		BigInteger diff = subDigsB.subtract(subDigsO).abs();
		if (diff.intValue() > 100) {
			// 100/32768 ~= 0.003
			throw new AssertionFailedError("minor mistake");
		}
	}

	private static String formatDoubleAsHex(double d) {
		long l = Double.doubleToLongBits(d);
		StringBuilder sb = new StringBuilder(20);
		sb.append(HexDump.longToHex(l)).append('L');
		return sb.toString();
	}
}
