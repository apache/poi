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

import static org.apache.poi.ss.util.IEEEDouble.*;

/**
 * Excel compares numbers using different rules to those of java, so
 *  {@link Double#compare(double, double)} won't do.
 *
 *
 * @author Josh Micich
 */
public final class NumberComparer {

	/**
	 * This class attempts to reproduce Excel's behaviour for comparing numbers.  Results are
	 * mostly the same as those from {@link Double#compare(double, double)} but with some
	 * rounding.  For numbers that are very close, this code converts to a format having 15
	 * decimal digits of precision and a decimal exponent, before completing the comparison.
	 * <p/>
	 * In Excel formula evaluation, expressions like "(0.06-0.01)=0.05" evaluate to "TRUE" even
	 * though the equivalent java expression is <code>false</code>.  In examples like this,
	 * Excel achieves the effect by having additional logic for comparison operations.
	 * <p/>
	 * <p/>
	 * Note - Excel also gives special treatment to expressions like "0.06-0.01-0.05" which
	 * evaluates to "0" (in java, rounding anomalies give a result of 6.9E-18).  The special
	 * behaviour here is for different reasons to the example above:  If the last operator in a
	 * cell formula is '+' or '-' and the result is less than 2<sup>50</sup> times smaller than
	 * first operand, the result is rounded to zero.
	 * Needless to say, the two rules are not consistent and it is relatively easy to find
	 * examples that satisfy<br/>
	 * "A=B" is "TRUE" but "A-B" is not "0"<br/>
	 * and<br/>
	 * "A=B" is "FALSE" but "A-B" is "0"<br/>
	 * <br/>
	 * This rule (for rounding the result of a final addition or subtraction), has not been
	 * implemented in POI (as of Jul-2009).
	 *
	 * @return <code>negative, 0, or positive</code> according to the standard Excel comparison
	 * of values <tt>a</tt> and <tt>b</tt>.
	 */
	public static int compare(double a, double b) {
		long rawBitsA = Double.doubleToLongBits(a);
		long rawBitsB = Double.doubleToLongBits(b);

		int biasedExponentA = getBiasedExponent(rawBitsA);
		int biasedExponentB = getBiasedExponent(rawBitsB);

		if (biasedExponentA == BIASED_EXPONENT_SPECIAL_VALUE) {
			throw new IllegalArgumentException("Special double values are not allowed: " + toHex(a));
		}
		if (biasedExponentB == BIASED_EXPONENT_SPECIAL_VALUE) {
			throw new IllegalArgumentException("Special double values are not allowed: " + toHex(a));
		}

		int cmp;

		// sign bit is in the same place for long and double:
		boolean aIsNegative = rawBitsA < 0;
		boolean bIsNegative = rawBitsB < 0;

		// compare signs
		if (aIsNegative != bIsNegative) {
			// Excel seems to have 'normal' comparison behaviour around zero (no rounding)
			// even -0.0 < +0.0 (which is not quite the initial conclusion of bug 47198)
			return aIsNegative ? -1 : +1;
		}

		// then compare magnitudes (IEEE 754 has exponent bias specifically to allow this)
		cmp = biasedExponentA - biasedExponentB;
		int absExpDiff = Math.abs(cmp);
		if (absExpDiff > 1) {
			return aIsNegative ? -cmp : cmp;
		}

		if (absExpDiff == 1) {
			// special case exponent differs by 1.  There is still a chance that with rounding the two quantities could end up the same

		} else {
			// else - sign and exponents equal
			if (rawBitsA == rawBitsB) {
				// fully equal - exit here
				return 0;
			}
		}
		if (biasedExponentA == 0) {
			if (biasedExponentB == 0) {
				return compareSubnormalNumbers(rawBitsA & FRAC_MASK, rawBitsB & FRAC_MASK, aIsNegative);
			}
			// else biasedExponentB is 1
			return -compareAcrossSubnormalThreshold(rawBitsB, rawBitsA, aIsNegative);
		}
		if (biasedExponentB == 0) {
			// else biasedExponentA is 1
			return +compareAcrossSubnormalThreshold(rawBitsA, rawBitsB, aIsNegative);
		}

		// sign and exponents same, but fractional bits are different

		ExpandedDouble edA = ExpandedDouble.fromRawBitsAndExponent(rawBitsA, biasedExponentA - EXPONENT_BIAS);
		ExpandedDouble edB = ExpandedDouble.fromRawBitsAndExponent(rawBitsB, biasedExponentB - EXPONENT_BIAS);
		NormalisedDecimal ndA = edA.normaliseBaseTen().roundUnits();
		NormalisedDecimal ndB = edB.normaliseBaseTen().roundUnits();
		cmp = ndA.compareNormalised(ndB);
		if (aIsNegative) {
			return -cmp;
		}
		return cmp;
	}

	/**
	 * If both numbers are subnormal, Excel seems to use standard comparison rules
	 */
	private static int compareSubnormalNumbers(long fracA, long fracB, boolean isNegative) {
		int cmp = fracA > fracB ? +1 : fracA < fracB ? -1 : 0;

		return isNegative ? -cmp : cmp;
	}



	/**
	 * Usually any normal number is greater (in magnitude) than any subnormal number.
	 * However there are some anomalous cases around the threshold where Excel produces screwy results
	 * @param isNegative both values are either negative or positive. This parameter affects the sign of the comparison result
	 * @return usually <code>isNegative ? -1 : +1</code>
	 */
	private static int compareAcrossSubnormalThreshold(long normalRawBitsA, long subnormalRawBitsB, boolean isNegative) {
		long fracB = subnormalRawBitsB & FRAC_MASK;
		if (fracB == 0) {
			// B is zero, so A is definitely greater than B
			return isNegative ? -1 : +1;
		}
		long fracA = normalRawBitsA & FRAC_MASK;
		if (fracA <= 0x0000000000000007L && fracB >= 0x000FFFFFFFFFFFFAL) {
			// Both A and B close to threshold - weird results
			if (fracA == 0x0000000000000007L && fracB == 0x000FFFFFFFFFFFFAL) {
				// special case
				return 0;
			}
			// exactly the opposite
			return isNegative ? +1 : -1;
		}
		// else - typical case A and B is not close to threshold
		return isNegative ? -1 : +1;
	}



	/**
	 * for formatting double values in error messages
	 */
	private static String toHex(double a) {
		return "0x" + Long.toHexString(Double.doubleToLongBits(a)).toUpperCase();
	}
}
