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


/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 * This class is an extension to the standard math library
 * provided by java.lang.Math class. It follows the Math class
 * in that it has a private constructor and all static methods.
 */
final class MathX {

    private MathX() {
        // no instances of this class
    }


    /**
     * Returns a value rounded to p digits after decimal.
     * If p is negative, then the number is rounded to
     * places to the left of the decimal point. eg.
     * 10.23 rounded to -1 will give: 10. If p is zero,
     * the returned value is rounded to the nearest integral
     * value.
     * <p>If n is negative, the resulting value is obtained
     * as the round value of absolute value of n multiplied
     * by the sign value of n (@see MathX.sign(double d)).
     * Thus, -0.6666666 rounded to p=0 will give -1 not 0.
     * <p>If n is NaN, returned value is NaN.
     * @param n
     * @param p
     */
    public static double round(double n, int p) {
        double retval;

        if (Double.isNaN(n) || Double.isInfinite(n)) {
            retval = Double.NaN;
        }
        else {
            if (p != 0) {
                double temp = Math.pow(10, p);
                retval = Math.round(n*temp)/temp;
            }
            else {
                retval = Math.round(n);
            }
        }

        return retval;
    }

    /**
     * Returns a value rounded-up to p digits after decimal.
     * If p is negative, then the number is rounded to
     * places to the left of the decimal point. eg.
     * 10.23 rounded to -1 will give: 20. If p is zero,
     * the returned value is rounded to the nearest integral
     * value.
     * <p>If n is negative, the resulting value is obtained
     * as the round-up value of absolute value of n multiplied
     * by the sign value of n (@see MathX.sign(double d)).
     * Thus, -0.2 rounded-up to p=0 will give -1 not 0.
     * <p>If n is NaN, returned value is NaN.
     * @param n
     * @param p
     */
    public static double roundUp(double n, int p) {
        double retval;

        if (Double.isNaN(n) || Double.isInfinite(n)) {
            retval = Double.NaN;
        }
        else {
            if (p != 0) {
                double temp = Math.pow(10, p);
                double nat = Math.abs(n*temp);

                retval = sign(n) *
                    ((nat == (long) nat)
                            ? nat / temp
                            : Math.round(nat + 0.5) / temp);
            }
            else {
                double na = Math.abs(n);
                retval = sign(n) *
                    ((na == (long) na)
                        ? na
                        : (long) na + 1);
            }
        }

        return retval;
    }

    /**
     * Returns a value rounded to p digits after decimal.
     * If p is negative, then the number is rounded to
     * places to the left of the decimal point. eg.
     * 10.23 rounded to -1 will give: 10. If p is zero,
     * the returned value is rounded to the nearest integral
     * value.
     * <p>If n is negative, the resulting value is obtained
     * as the round-up value of absolute value of n multiplied
     * by the sign value of n (@see MathX.sign(double d)).
     * Thus, -0.8 rounded-down to p=0 will give 0 not -1.
     * <p>If n is NaN, returned value is NaN.
     * @param n
     * @param p
     */
    public static double roundDown(double n, int p) {
        double retval;

        if (Double.isNaN(n) || Double.isInfinite(n)) {
            retval = Double.NaN;
        }
        else {
            if (p != 0) {
                double temp = Math.pow(10, p);
                retval = sign(n) * Math.round((Math.abs(n)*temp) - 0.5)/temp;
            }
            else {
                retval = (long) n;
            }
        }

        return retval;
    }


    /**
     * If d < 0, returns short -1
     * <br/>
     * If d > 0, returns short 1
     * <br/>
     * If d == 0, returns short 0
     * <p> If d is NaN, then 1 will be returned. It is the responsibility
     * of caller to check for d isNaN if some other value is desired.
     * @param d
     */
    public static short sign(double d) {
        return (short) ((d == 0)
                ? 0
                : (d < 0)
                        ? -1
                        : 1);
    }

    /**
     * average of all values
     * @param values
     */
    public static double average(double[] values) {
        double ave = 0;
        double sum = 0;
        for (int i=0, iSize=values.length; i<iSize; i++) {
            sum += values[i];
        }
        ave = sum / values.length;
        return ave;
    }


    /**
     * sum of all values
     * @param values
     */
    public static double sum(double[] values) {
        double sum = 0;
        for (int i=0, iSize=values.length; i<iSize; i++) {
            sum += values[i];
        }
        return sum;
    }

    /**
     * sum of squares of all values
     * @param values
     */
    public static double sumsq(double[] values) {
        double sumsq = 0;
        for (int i=0, iSize=values.length; i<iSize; i++) {
            sumsq += values[i]*values[i];
        }
        return sumsq;
    }


    /**
     * product of all values
     * @param values
     */
    public static double product(double[] values) {
        double product = 0;
        if (values!=null && values.length > 0) {
            product = 1;
            for (int i=0, iSize=values.length; i<iSize; i++) {
                product *= values[i];
            }
        }
        return product;
    }

    /**
     * min of all values. If supplied array is zero length,
     * Double.POSITIVE_INFINITY is returned.
     * @param values
     */
    public static double min(double[] values) {
        double min = Double.POSITIVE_INFINITY;
        for (int i=0, iSize=values.length; i<iSize; i++) {
            min = Math.min(min, values[i]);
        }
        return min;
    }

    /**
     * min of all values. If supplied array is zero length,
     * Double.NEGATIVE_INFINITY is returned.
     * @param values
     */
    public static double max(double[] values) {
        double max = Double.NEGATIVE_INFINITY;
        for (int i=0, iSize=values.length; i<iSize; i++) {
            max = Math.max(max, values[i]);
        }
        return max;
    }

    /**
     * Note: this function is different from java.lang.Math.floor(..).
     * <p>
     * When n and s are "valid" arguments, the returned value is: Math.floor(n/s) * s;
     * <br/>
     * n and s are invalid if any of following conditions are true:
     * <ul>
     * <li>s is zero</li>
     * <li>n is negative and s is positive</li>
     * <li>n is positive and s is negative</li>
     * </ul>
     * In all such cases, Double.NaN is returned.
     * @param n
     * @param s
     */
    public static double floor(double n, double s) {
        double f;

        if ((n<0 && s>0) || (n>0 && s<0) || (s==0 && n!=0)) {
            f = Double.NaN;
        }
        else {
            f = (n==0 || s==0) ? 0 : Math.floor(n/s) * s;
        }

        return f;
    }

    /**
     * Note: this function is different from java.lang.Math.ceil(..).
     * <p>
     * When n and s are "valid" arguments, the returned value is: Math.ceiling(n/s) * s;
     * <br/>
     * n and s are invalid if any of following conditions are true:
     * <ul>
     * <li>s is zero</li>
     * <li>n is negative and s is positive</li>
     * <li>n is positive and s is negative</li>
     * </ul>
     * In all such cases, Double.NaN is returned.
     * @param n
     * @param s
     */
    public static double ceiling(double n, double s) {
        double c;

        if ((n<0 && s>0) || (n>0 && s<0)) {
            c = Double.NaN;
        }
        else {
            c = (n == 0 || s == 0) ? 0 : Math.ceil(n/s) * s;
        }

        return c;
    }

    /**
     * <br/> for all n >= 1; factorial n = n * (n-1) * (n-2) * ... * 1
     * <br/> else if n == 0; factorial n = 1
     * <br/> else if n < 0; factorial n = Double.NaN
     * <br/> Loss of precision can occur if n is large enough.
     * If n is large so that the resulting value would be greater
     * than Double.MAX_VALUE; Double.POSITIVE_INFINITY is returned.
     * If n < 0, Double.NaN is returned.
     * @param n
     */
    public static double factorial(int n) {
        double d = 1;

        if (n >= 0) {
            if (n <= 170) {
                for (int i=1; i<=n; i++) {
                    d *= i;
                }
            }
            else {
                d = Double.POSITIVE_INFINITY;
            }
        }
        else {
            d = Double.NaN;
        }
        return d;
    }


    /**
     * returns the remainder resulting from operation:
     * n / d.
     * <br/> The result has the sign of the divisor.
     * <br/> Examples:
     * <ul>
     * <li>mod(3.4, 2) = 1.4</li>
     * <li>mod(-3.4, 2) = 0.6</li>
     * <li>mod(-3.4, -2) = -1.4</li>
     * <li>mod(3.4, -2) = -0.6</li>
     * </ul>
     * If d == 0, result is NaN
     * @param n
     * @param d
     */
    public static double mod(double n, double d) {
        double result = 0;

        if (d == 0) {
            result = Double.NaN;
        }
        else if (sign(n) == sign(d)) {
            result = n % d;
        }
        else {
            result = ((n % d) + d) % d;
        }

        return result;
    }

    /**
     * inverse hyperbolic cosine
     * @param d
     */
    public static double acosh(double d) {
        return Math.log(Math.sqrt(Math.pow(d, 2) - 1) + d);
    }

    /**
     * inverse hyperbolic sine
     * @param d
     */
    public static double asinh(double d) {
        return Math.log(Math.sqrt(d*d + 1) + d);
    }

    /**
     * inverse hyperbolic tangent
     * @param d
     */
    public static double atanh(double d) {
        return Math.log((1 + d)/(1 - d)) / 2;
    }

    /**
     * hyperbolic cosine
     * @param d
     */
    public static double cosh(double d) {
        double ePowX = Math.pow(Math.E, d);
        double ePowNegX = Math.pow(Math.E, -d);
        return (ePowX + ePowNegX) / 2;
    }

    /**
     * hyperbolic sine
     * @param d
     */
    public static double sinh(double d) {
        double ePowX = Math.pow(Math.E, d);
        double ePowNegX = Math.pow(Math.E, -d);
        return (ePowX - ePowNegX) / 2;
    }

    /**
     * hyperbolic tangent
     * @param d
     */
    public static double tanh(double d) {
        double ePowX = Math.pow(Math.E, d);
        double ePowNegX = Math.pow(Math.E, -d);
        return (ePowX - ePowNegX) / (ePowX + ePowNegX);
    }


    /**
     * returns the total number of combinations possible when
     * k items are chosen out of total of n items. If the number
     * is too large, loss of precision may occur (since returned
     * value is double). If the returned value is larger than
     * Double.MAX_VALUE, Double.POSITIVE_INFINITY is returned.
     * If either of the parameters is negative, Double.NaN is returned.
     * @param n
     * @param k
     */
    public static double nChooseK(int n, int k) {
        double d = 1;
        if (n<0 || k<0 || n<k) {
            d= Double.NaN;
        }
        else {
            int minnk = Math.min(n-k, k);
            int maxnk = Math.max(n-k, k);
            for (int i=maxnk; i<n; i++) {
                d *= i+1;
            }
            d /= factorial(minnk);
        }

        return d;
    }

}
