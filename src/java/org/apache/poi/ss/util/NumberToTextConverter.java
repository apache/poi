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

/**
 * Excel converts numbers to text with different rules to those of java, so
 *  <code>Double.toString(value)</tt> won't do.
 * <ul>
 * <li>No more than 15 significant figures are output (java does 18).</li>
 * <li>The sign char for the exponent is included even if positive</li>
 * <li>Special values (<tt>NaN</tt> and <tt>Infinity</tt>) get rendered like the ordinary
 * number that the bit pattern represents.</li>
 * <li>Denormalised values (between &plusmn;2<sup>-1074</sup> and &plusmn;2<sup>-1022</sup>
 *  are displayed as "0"</sup>
 * </ul>
 * IEEE 64-bit Double Rendering Comparison
 *
 * <table border="1" cellpadding="2" cellspacing="0" summary="IEEE 64-bit Double Rendering Comparison">
 * <tr><th>Raw bits</th><th>Java</th><th>Excel</th></tr>
 *
 * <tr><td>0x0000000000000000L</td><td>0.0</td><td>0</td></tr>
 * <tr><td>0x3FF0000000000000L</td><td>1.0</td><td>1</td></tr>
 * <tr><td>0x3FF00068DB8BAC71L</td><td>1.0001</td><td>1.0001</td></tr>
 * <tr><td>0x4087A00000000000L</td><td>756.0</td><td>756</td></tr>
 * <tr><td>0x401E3D70A3D70A3DL</td><td>7.56</td><td>7.56</td></tr>
 * <tr><td>0x405EDD3C07FB4C99L</td><td>123.45678901234568</td><td>123.456789012346</td></tr>
 * <tr><td>0x4132D687E3DF2180L</td><td>1234567.8901234567</td><td>1234567.89012346</td></tr>
 * <tr><td>0x3EE9E409302678BAL</td><td>1.2345678901234568E-5</td><td>1.23456789012346E-05</td></tr>
 * <tr><td>0x3F202E85BE180B74L</td><td>1.2345678901234567E-4</td><td>0.000123456789012346</td></tr>
 * <tr><td>0x3F543A272D9E0E51L</td><td>0.0012345678901234567</td><td>0.00123456789012346</td></tr>
 * <tr><td>0x3F8948B0F90591E6L</td><td>0.012345678901234568</td><td>0.0123456789012346</td></tr>
 * <tr><td>0x3EE9E409301B5A02L</td><td>1.23456789E-5</td><td>0.0000123456789</td></tr>
 * <tr><td>0x3E6E7D05BDABDE50L</td><td>5.6789012345E-8</td><td>0.000000056789012345</td></tr>
 * <tr><td>0x3E6E7D05BDAD407EL</td><td>5.67890123456E-8</td><td>5.67890123456E-08</td></tr>
 * <tr><td>0x3E6E7D06029F18BEL</td><td>5.678902E-8</td><td>0.00000005678902</td></tr>
 * <tr><td>0x2BCB5733CB32AE6EL</td><td>9.999999999999123E-98</td><td>9.99999999999912E-98</td></tr>
 * <tr><td>0x2B617F7D4ED8C59EL</td><td>1.0000000000001235E-99</td><td>1.0000000000001E-99</td></tr>
 * <tr><td>0x0036319916D67853L</td><td>1.2345678901234578E-307</td><td>1.2345678901235E-307</td></tr>
 * <tr><td>0x359DEE7A4AD4B81FL</td><td>2.0E-50</td><td>2E-50</td></tr>
 * <tr><td>0x41678C29DCD6E9E0L</td><td>1.2345678901234567E7</td><td>12345678.9012346</td></tr>
 * <tr><td>0x42A674E79C5FE523L</td><td>1.2345678901234568E13</td><td>12345678901234.6</td></tr>
 * <tr><td>0x42DC12218377DE6BL</td><td>1.2345678901234567E14</td><td>123456789012346</td></tr>
 * <tr><td>0x43118B54F22AEB03L</td><td>1.2345678901234568E15</td><td>1234567890123460</td></tr>
 * <tr><td>0x43E56A95319D63E1L</td><td>1.2345678901234567E19</td><td>12345678901234600000</td></tr>
 * <tr><td>0x441AC53A7E04BCDAL</td><td>1.2345678901234568E20</td><td>1.23456789012346E+20</td></tr>
 * <tr><td>0xC3E56A95319D63E1L</td><td>-1.2345678901234567E19</td><td>-12345678901234600000</td></tr>
 * <tr><td>0xC41AC53A7E04BCDAL</td><td>-1.2345678901234568E20</td><td>-1.23456789012346E+20</td></tr>
 * <tr><td>0x54820FE0BA17F46DL</td><td>1.2345678901234577E99</td><td>1.2345678901235E+99</td></tr>
 * <tr><td>0x54B693D8E89DF188L</td><td>1.2345678901234576E100</td><td>1.2345678901235E+100</td></tr>
 * <tr><td>0x4A611B0EC57E649AL</td><td>2.0E50</td><td>2E+50</td></tr>
 * <tr><td>0x7FEFFFFFFFFFFFFFL</td><td>1.7976931348623157E308</td><td>1.7976931348623E+308</td></tr>
 * <tr><td>0x0010000000000000L</td><td>2.2250738585072014E-308</td><td>2.2250738585072E-308</td></tr>
 * <tr><td>0x000FFFFFFFFFFFFFL</td><td>2.225073858507201E-308</td><td>0</td></tr>
 * <tr><td>0x0000000000000001L</td><td>4.9E-324</td><td>0</td></tr>
 * <tr><td>0x7FF0000000000000L</td><td>Infinity</td><td>1.7976931348623E+308</td></tr>
 * <tr><td>0xFFF0000000000000L</td><td>-Infinity</td><td>1.7976931348623E+308</td></tr>
 * <tr><td>0x441AC7A08EAD02F2L</td><td>1.234999999999999E20</td><td>1.235E+20</td></tr>
 * <tr><td>0x40FE26BFFFFFFFF9L</td><td>123499.9999999999</td><td>123500</td></tr>
 * <tr><td>0x3E4A857BFB2F2809L</td><td>1.234999999999999E-8</td><td>0.00000001235</td></tr>
 * <tr><td>0x3BCD291DEF868C89L</td><td>1.234999999999999E-20</td><td>1.235E-20</td></tr>
 * <tr><td>0x444B1AE4D6E2EF4FL</td><td>9.999999999999999E20</td><td>1E+21</td></tr>
 * <tr><td>0x412E847FFFFFFFFFL</td><td>999999.9999999999</td><td>1000000</td></tr>
 * <tr><td>0x3E45798EE2308C39L</td><td>9.999999999999999E-9</td><td>0.00000001</td></tr>
 * <tr><td>0x3C32725DD1D243ABL</td><td>9.999999999999999E-19</td><td>0.000000000000000001</td></tr>
 * <tr><td>0x3BFD83C94FB6D2ABL</td><td>9.999999999999999E-20</td><td>1E-19</td></tr>
 * <tr><td>0xC44B1AE4D6E2EF4FL</td><td>-9.999999999999999E20</td><td>-1E+21</td></tr>
 * <tr><td>0xC12E847FFFFFFFFFL</td><td>-999999.9999999999</td><td>-1000000</td></tr>
 * <tr><td>0xBE45798EE2308C39L</td><td>-9.999999999999999E-9</td><td>-0.00000001</td></tr>
 * <tr><td>0xBC32725DD1D243ABL</td><td>-9.999999999999999E-19</td><td>-0.000000000000000001</td></tr>
 * <tr><td>0xBBFD83C94FB6D2ABL</td><td>-9.999999999999999E-20</td><td>-1E-19</td></tr>
 * <tr><td>0xFFFF0420003C0000L</td><td>NaN</td><td>3.484840871308E+308</td></tr>
 * <tr><td>0x7FF8000000000000L</td><td>NaN</td><td>2.6965397022935E+308</td></tr>
 * <tr><td>0x7FFF0420003C0000L</td><td>NaN</td><td>3.484840871308E+308</td></tr>
 * <tr><td>0xFFF8000000000000L</td><td>NaN</td><td>2.6965397022935E+308</td></tr>
 * <tr><td>0xFFFF0AAAAAAAAAAAL</td><td>NaN</td><td>3.4877119413344E+308</td></tr>
 * <tr><td>0x7FF80AAAAAAAAAAAL</td><td>NaN</td><td>2.7012211948322E+308</td></tr>
 * <tr><td>0xFFFFFFFFFFFFFFFFL</td><td>NaN</td><td>3.5953862697246E+308</td></tr>
 * <tr><td>0x7FFFFFFFFFFFFFFFL</td><td>NaN</td><td>3.5953862697246E+308</td></tr>
 * <tr><td>0xFFF7FFFFFFFFFFFFL</td><td>NaN</td><td>2.6965397022935E+308</td></tr>
 * </table>
 *
 * <b>Note</b>:
 * Excel has inconsistent rules for the following numeric operations:
 * <ul>
 * <li>Conversion to string (as handled here)</li>
 * <li>Rendering numerical quantities in the cell grid.</li>
 * <li>Conversion from text</li>
 * <li>General arithmetic</li>
 * </ul>
 * Excel's text to number conversion is not a true <i>inverse</i> of this operation.  The
 * allowable ranges are different.  Some numbers that don't correctly convert to text actually
 * <b>do</b> get handled properly when used in arithmetic evaluations.
 *
 * @author Josh Micich
 */
public final class NumberToTextConverter {

	private static final long expMask  = 0x7FF0000000000000L;
	private static final long FRAC_MASK= 0x000FFFFFFFFFFFFFL;
	private static final int  EXPONENT_SHIFT = 52;
	private static final int  FRAC_BITS_WIDTH = EXPONENT_SHIFT;
	private static final int  EXPONENT_BIAS  = 1023;
	private static final long FRAC_ASSUMED_HIGH_BIT = ( 1L<<EXPONENT_SHIFT );

	private static final long EXCEL_NAN_BITS = 0xFFFF0420003C0000L;
	private static final int MAX_TEXT_LEN = 20;

	private static final int DEFAULT_COUNT_SIGNIFICANT_DIGITS = 15;
	private static final int MAX_EXTRA_ZEROS = MAX_TEXT_LEN - DEFAULT_COUNT_SIGNIFICANT_DIGITS;
	private static final float LOG2_10 = 3.32F;


	private NumberToTextConverter() {
		// no instances of this class
	}

	/**
	 * Converts the supplied <tt>value</tt> to the text representation that Excel would give if
	 * the value were to appear in an unformatted cell, or as a literal number in a formula.<br/>
	 * Note - the results from this method differ slightly from those of <tt>Double.toString()</tt>
	 * In some special cases Excel behaves quite differently.  This function attempts to reproduce
	 * those results.
	 */
	public static String toText(double value) {
		return rawDoubleBitsToText(Double.doubleToLongBits(value));
	}
	/* package */ static String rawDoubleBitsToText(long pRawBits) {

		long rawBits = pRawBits;
		boolean isNegative = rawBits < 0; // sign bit is in the same place for long and double
		if (isNegative) {
			rawBits &= 0x7FFFFFFFFFFFFFFFL;
		}

		int biasedExponent = (int) ((rawBits & expMask) >> EXPONENT_SHIFT);
		if (biasedExponent == 0) {
			// value is 'denormalised' which means it is less than 2^-1022
			// excel displays all these numbers as zero, even though calculations work OK
			return isNegative ? "-0" : "0";
		}

		int exponent = biasedExponent - EXPONENT_BIAS;

		long fracBits = FRAC_ASSUMED_HIGH_BIT | rawBits & FRAC_MASK;


		// Start by converting double value to BigDecimal
		BigDecimal bd;
		if (biasedExponent == 0x07FF) {
			// Special number NaN /Infinity
			if(rawBits == EXCEL_NAN_BITS) {
				return "3.484840871308E+308";
			}
			// This is where excel really gets it wrong
			// Special numbers like Infinity and Nan are interpreted according to
			// the standard rules below.
			isNegative = false; // except that the sign bit is ignored
		}
		bd = convertToBigDecimal(exponent, fracBits);

		return formatBigInteger(isNegative, bd.unscaledValue(), bd.scale());
	}

	private static BigDecimal convertToBigDecimal(int exponent, long fracBits) {
		byte[] joob = {
				(byte) (fracBits >> 48),
				(byte) (fracBits >> 40),
				(byte) (fracBits >> 32),
				(byte) (fracBits >> 24),
				(byte) (fracBits >> 16),
				(byte) (fracBits >>  8),
				(byte) (fracBits >>  0),
		};

		BigInteger bigInt = new BigInteger(joob);
		int lastSigBitIndex = exponent-FRAC_BITS_WIDTH;
		if(lastSigBitIndex < 0) {
			BigInteger shifto = new BigInteger("1").shiftLeft(-lastSigBitIndex);
			int scale = 1 -(int) (lastSigBitIndex/LOG2_10);
			BigDecimal bd1 = new BigDecimal(bigInt);
			BigDecimal bdShifto = new BigDecimal(shifto);
			return bd1.divide(bdShifto, scale, BigDecimal.ROUND_HALF_UP);
		}
		BigInteger sl = bigInt.shiftLeft(lastSigBitIndex);
		return new BigDecimal(sl);
	}

	private static String formatBigInteger(boolean isNegative, BigInteger unscaledValue, int scale) {

		if (scale < 0) {
			throw new RuntimeException("negative scale");
		}

		StringBuffer sb = new StringBuffer(unscaledValue.toString());
		int numberOfLeadingZeros = -1;

		int unscaledLength = sb.length();
		if (scale > 0 && scale >= unscaledLength) {
			// less than one
			numberOfLeadingZeros = scale-unscaledLength;
			formatLessThanOne(sb, numberOfLeadingZeros+1);
		} else {
			int decimalPointIndex = unscaledLength - scale;
			formatGreaterThanOne(sb, decimalPointIndex);
		}
		if(isNegative) {
			sb.insert(0, '-');
		}
		return sb.toString();
	}

	private static int getNumberOfSignificantFiguresDisplayed(int exponent) {
		int nLostDigits; // number of significand digits lost due big exponents
		if(exponent > 99) {
			// any exponent greater than 99 has 3 digits instead of 2
			nLostDigits = 1;
		} else if (exponent < -98) {
			// For some weird reason on the negative side
			// step is occurs from -98 to -99 (not from -99 to -100)
			nLostDigits = 1;
		} else {
			nLostDigits = 0;
		}
		return DEFAULT_COUNT_SIGNIFICANT_DIGITS - nLostDigits;
	}

	private static boolean needsScientificNotation(int nDigits) {
		return nDigits > MAX_TEXT_LEN;
	}

	private static void formatGreaterThanOne(StringBuffer sb, int nIntegerDigits) {

		int maxSigFigs = getNumberOfSignificantFiguresDisplayed(nIntegerDigits);
		int decimalPointIndex = nIntegerDigits;
		boolean roundCausedCarry = performRound(sb, 0, maxSigFigs);

		int endIx = Math.min(maxSigFigs, sb.length()-1);

		int nSigFigures;
		if(roundCausedCarry) {
			sb.insert(0, '1');
			decimalPointIndex++;
			nSigFigures = 1;
		} else {
			nSigFigures = countSignifantDigits(sb, endIx);
		}

		if(needsScientificNotation(decimalPointIndex)) {
			sb.setLength(nSigFigures);
			if (nSigFigures > 1) {
				sb.insert(1, '.');
			}
			sb.append("E+");
			appendExp(sb, decimalPointIndex-1);
			return;
		}
		if(isAllZeros(sb, decimalPointIndex, maxSigFigs)) {
			sb.setLength(decimalPointIndex);
			return;
		}
		// else some sig-digits after the decimal point
		sb.setLength(nSigFigures);
		sb.insert(decimalPointIndex, '.');
	}

	/**
	 * @param sb initially contains just the significant digits
	 * @param pAbsExponent to be inserted (after "0.") at the start of the number
	 */
	private static void formatLessThanOne(StringBuffer sb, int pAbsExponent) {
		if (sb.charAt(0) == 0) {
			throw new IllegalArgumentException("First digit of significand should be non-zero");
		}
		if (pAbsExponent < 1) {
			throw new IllegalArgumentException("abs(exponent) must be positive");
		}

		int numberOfLeadingZeros = pAbsExponent-1;
		int absExponent = pAbsExponent;
		int maxSigFigs = getNumberOfSignificantFiguresDisplayed(-absExponent);

		boolean roundCausedCarry = performRound(sb, 0, maxSigFigs);
		int nRemainingSigFigs;
		if(roundCausedCarry) {
			absExponent--;
			numberOfLeadingZeros--;
			nRemainingSigFigs = 1;
			sb.setLength(0);
			sb.append("1");
		} else {
			nRemainingSigFigs = countSignifantDigits(sb, 0 + maxSigFigs);
			sb.setLength(nRemainingSigFigs);
		}

		int normalLength = 2 + numberOfLeadingZeros + nRemainingSigFigs; // 2 == "0.".length()

		if (needsScientificNotation(normalLength)) {
			if (sb.length()>1) {
				sb.insert(1, '.');
			}
			sb.append('E');
			sb.append('-');
			appendExp(sb, absExponent);
		} else {
			sb.insert(0, "0.");
			for(int i=numberOfLeadingZeros; i>0; i--) {
				sb.insert(2, '0');
			}
		}
	}

	private static int countSignifantDigits(StringBuffer sb, int startIx) {
		int result=startIx;
		while(sb.charAt(result) == '0') {
			result--;
			if(result < 0) {
				throw new RuntimeException("No non-zero digits found");
			}
		}
		return result + 1;
	}

	private static void appendExp(StringBuffer sb, int val) {
		if(val < 10) {
			sb.append('0');
			sb.append((char)('0' + val));
			return;
		}
		sb.append(val);

	}


	private static boolean isAllZeros(StringBuffer sb, int startIx, int endIx) {
		for(int i=startIx; i<=endIx && i<sb.length(); i++) {
			if(sb.charAt(i) != '0') {
				return false;
			}
		}
		return true;
	}

	/**
	 * @return <code>true</code> if carry (out of the MS digit) occurred
	 */
	private static boolean performRound(StringBuffer sb, int firstSigFigIx, int nSigFigs) {
		int nextDigitIx = firstSigFigIx + nSigFigs;
		if(nextDigitIx == sb.length()) {
			return false; // nothing to do - digit to be rounded is at the end of the buffer
		}
		if(nextDigitIx > sb.length()) {
			throw new RuntimeException("Buffer too small to fit all significant digits");
		}
		boolean hadCarryOutOfFirstDigit;
		if(sb.charAt(nextDigitIx) < '5') {
			// change to digit
			hadCarryOutOfFirstDigit = false;
		} else {
			hadCarryOutOfFirstDigit = roundAndCarry(sb, nextDigitIx);
		}
		// clear out the rest of the digits after the rounded digit
		// (at least the nearby digits)
		int endIx = Math.min(nextDigitIx + MAX_EXTRA_ZEROS, sb.length());
		for(int i = nextDigitIx; i<endIx; i++) {
			sb.setCharAt(i, '0');
		}
		return hadCarryOutOfFirstDigit;
	}

	private static boolean roundAndCarry(StringBuffer sb, int nextDigitIx) {

		int changeDigitIx = nextDigitIx - 1;
		while(sb.charAt(changeDigitIx) == '9') {
			sb.setCharAt(changeDigitIx, '0');
			changeDigitIx--;
			// All nines, rounded up.  Notify caller
			if(changeDigitIx < 0) {
				return true;
			}
		}
		// no more '9's to round up.
		// Last digit to be changed is still inside sb
		char prevDigit = sb.charAt(changeDigitIx);
		sb.setCharAt(changeDigitIx, (char) (prevDigit + 1));
		return false;
	}
}
