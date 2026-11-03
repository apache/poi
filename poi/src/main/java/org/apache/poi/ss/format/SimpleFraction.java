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
*/
package org.apache.poi.ss.format;

import org.apache.poi.util.MathUtil;

public class SimpleFraction {

    /**
     * The most denominator digits a fraction can have: 10^9 - 1 is the largest all-nines denominator that fits in an int
     *
     * @since 6.0.1
     */
    public static final int MAX_DENOMINATOR_DIGITS = 9;

    /** The denominator. */
    private final int denominator;

    /** The numerator. */
    private final int numerator;
    /**
     * Create a fraction given a double value and a denominator.
     *
     * @param val double value of fraction
     * @param exactDenom the exact denominator
     * @return a SimpleFraction with the given values set.
     */
    public static SimpleFraction buildFractionExactDenominator(double val, int exactDenom){
        int num =  MathUtil.safeDoubleToInt(Math.round(val*exactDenom));
        return new SimpleFraction(num,exactDenom);
    }

    /**
     * Create a fraction given the double value and either the maximum error
     * allowed or the maximum number of denominator digits.
     *
     * @param value the double value to convert to a fraction.
     * @param maxDenominator maximum denominator value allowed.
     *
     * @throws IllegalStateException if the continued fraction failed to
     *      converge.
     * @throws IllegalArgumentException if value &gt; Integer.MAX_VALUE
     */
    public static SimpleFraction buildFractionMaxDenominator(double value, int maxDenominator){
        return buildFractionMaxDenominator(value, 0, maxDenominator, 100, 0, Long.MAX_VALUE);
    }

    /**
     * Create a fraction for the part of an improper fraction after the whole number, stopping before the
     * numerator of the improper fraction, {@code wholeNumber * denominator + numerator}, exceeds a maximum.
     *
     * @param value the part after the whole number, from 0 up to (not including) 1
     * @param maxDenominator maximum denominator value allowed.
     * @param wholeNumber the whole number, which must be below 2^31
     * @param maxNumerator maximum numerator allowed for the improper fraction
     */
    static SimpleFraction buildFractionMaxDenominator(double value, int maxDenominator, long wholeNumber, long maxNumerator) {
        return buildFractionMaxDenominator(value, 0, maxDenominator, 100, wholeNumber, maxNumerator);
    }
    /**
     * Create a fraction given the double value and either the maximum error
     * allowed or the maximum number of denominator digits.
     * <p>
     * References:
     * <ul>
     * <li><a href="http://mathworld.wolfram.com/ContinuedFraction.html">
     * Continued Fraction</a> equations (11) and (22)-(26)</li>
     * </ul>
     *
     *  Based on org.apache.commons.math.fraction.Fraction from Apache Commons-Math.
     *  YK: The only reason of having this class is to avoid dependency on the Commons-Math jar.
     *
     * @param value the double value to convert to a fraction.
     * @param epsilon maximum error allowed.  The resulting fraction is within
     *        {@code epsilon} of {@code value}, in absolute terms.
     * @param maxDenominator maximum denominator value allowed.
     * @param maxIterations maximum number of convergents
     * @param wholeNumber the whole number of an improper fraction that {@code value} is the rest of
     * @param maxNumerator maximum value of {@code wholeNumber * denominator + numerator}
     * @throws IllegalStateException if the continued fraction failed to
     *         converge.
     * @throws IllegalArgumentException if value > Integer.MAX_VALUE
     */
    private static SimpleFraction buildFractionMaxDenominator(double value, double epsilon, int maxDenominator, int maxIterations,
            long wholeNumber, long maxNumerator)
    {
        long overflow = Integer.MAX_VALUE;
        double r0 = value;
        long a0 = (long)Math.floor(r0);
        if (a0 > overflow) {
            throw new IllegalArgumentException("Overflow trying to convert "+value+" to fraction ("+a0+"/"+ 1L +")");
        }

        // check for (almost) integer arguments, which should not go
        // to iterations.
        if (Math.abs(a0 - value) < epsilon) {
            return new SimpleFraction(Math.toIntExact(a0), 1);
        }

        long p0 = 1;
        long q0 = 0;
        long p1 = a0;
        long q1 = 1;

        long p2;
        long q2;

        int n = 0;
        boolean stop = false;
        do {
            ++n;
            double r1 = 1.0 / (r0 - a0);
            if (r1 > overflow) {
                // a zero or tiny remainder: the next convergent would overflow, and p1/q1 is already the closest
                return new SimpleFraction(Math.toIntExact(p1), Math.toIntExact(q1));
            }
            long a1 = (long)Math.floor(r1);
            p2 = (a1 * p1) + p0;
            q2 = (a1 * q1) + q0;
            //MATH-996/POI-55419
            if (epsilon == 0.0f && maxDenominator > 0 && Math.abs(q2) > maxDenominator &&
                    Math.abs(q1) < maxDenominator){

                return new SimpleFraction(Math.toIntExact(p1), Math.toIntExact(q1));
            }
            // wholeNumber * q2 + p2 > maxNumerator, without overflowing a long
            if (wholeNumber > 0 && q2 > (maxNumerator - p2) / wholeNumber) {
                return new SimpleFraction(Math.toIntExact(p1), Math.toIntExact(q1));
            }
            if ((Math.abs(p2) > overflow) || (q2 > overflow)) {
                if (epsilon == 0.0f && maxDenominator > 0) {
                    // an improper fraction's numerator can overflow before the denominator reaches its maximum
                    return new SimpleFraction(Math.toIntExact(p1), Math.toIntExact(q1));
                }
                throw new IllegalStateException("Overflow trying to convert "+value+" to fraction ("+p2+"/"+q2+")");
            }

            double convergent = (double)p2 / (double)q2;
            if (n < maxIterations && Math.abs(convergent - value) > epsilon && q2 < maxDenominator) {
                p0 = p1;
                p1 = p2;
                q0 = q1;
                q1 = q2;
                a0 = a1;
                r0 = r1;
            } else {
                stop = true;
            }
        } while (!stop);

        if (n >= maxIterations) {
            throw new IllegalStateException("Unable to convert "+value+" to fraction after "+maxIterations+" iterations");
        }

        if (q2 < maxDenominator) {
            return new SimpleFraction(Math.toIntExact(p2), Math.toIntExact(q2));
        } else {
            return new SimpleFraction(Math.toIntExact(p1), Math.toIntExact(q1));
        }

    }

    /**
     * Create a fraction given a numerator and denominator.
     * @param numerator the numerator
     * @param denominator maxDenominator The maximum allowed value for denominator
     */
    public SimpleFraction(int numerator, int denominator)
    {
        this.numerator = numerator;
        this.denominator = denominator;
    }

    /**
     * Access the denominator.
     * @return the denominator.
     */
    public int getDenominator() {
        return denominator;
    }

    /**
     * Access the numerator.
     * @return the numerator.
     */
    public int getNumerator() {
        return numerator;
    }

}

