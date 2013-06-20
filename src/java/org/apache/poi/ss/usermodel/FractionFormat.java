/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.poi.ss.usermodel;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.formula.eval.NotImplementedException;

/**
 * <p>Format class that handles Excel style fractions, such as "# #/#" and "#/###"</p>
 * 
 * <p>As of this writing, this is still not 100% accurate, but it does a reasonable job
 * of trying to mimic Excel's fraction calculations.  It does not currently
 * maintain Excel's spacing.</p>
 * 
 * <p>This class relies on a method lifted nearly verbatim from org.apache.math.fraction.
 *  If further uses for Commons Math are found, we will consider adding it as a dependency.
 *  For now, we have in-lined the one method to keep things simple.</p>
 */
/* One question remains...is the value of epsilon in calcFractionMaxDenom reasonable? */
@SuppressWarnings("serial")
public class FractionFormat extends Format {
    private final static Pattern DENOM_FORMAT_PATTERN = Pattern.compile("(?:(#+)|(\\d+))");

    //this was chosen to match the earlier limitation of max denom power
    //it can be expanded to get closer to Excel's calculations
    //with custom formats # #/#########
    //but as of this writing, the numerators and denominators
    //with formats of that nature on very small values were quite
    //far from Excel's calculations
    private final static int MAX_DENOM_POW = 4;

    //there are two options:
    //a) an exact denominator is specified in the formatString
    //b) the maximum denominator can be calculated from the formatString
    private final int exactDenom;
    private final int maxDenom;

    private final String wholePartFormatString;
    /**
     * Single parameter ctor
     * @param denomFormatString The format string for the denominator
     */
    public FractionFormat(String wholePartFormatString, String denomFormatString) {
        this.wholePartFormatString = wholePartFormatString;
        //init exactDenom and maxDenom
        Matcher m = DENOM_FORMAT_PATTERN.matcher(denomFormatString);
        int tmpExact = -1;
        int tmpMax = -1;
        if (m.find()){
            if (m.group(2) != null){
                try{
                    tmpExact = Integer.parseInt(m.group(2));
                    //if the denom is 0, fall back to the default: tmpExact=100
                    
                    if (tmpExact == 0){
                        tmpExact = -1;
                    }
                } catch (NumberFormatException e){
                    //should never happen
                }
            } else if (m.group(1) != null) {
                int len = m.group(1).length();
                len = len > MAX_DENOM_POW ? MAX_DENOM_POW : len;
                tmpMax = (int)Math.pow(10, len);
            } else {
                tmpExact = 100;
            }
        }
        if (tmpExact <= 0 && tmpMax <= 0){
            //use 100 as the default denom if something went horribly wrong
            tmpExact = 100;
        }
        exactDenom = tmpExact;
        maxDenom = tmpMax;
    }

    public String format(Number num) {

        double doubleValue = num.doubleValue();
        
        boolean isNeg = (doubleValue < 0.0f) ? true : false;
        double absDoubleValue = Math.abs(doubleValue);
        
        double wholePart = Math.floor(absDoubleValue);
        double decPart = absDoubleValue - wholePart;
        if (wholePart + decPart == 0) {
            return "0";
        }
        
        //if the absolute value is smaller than 1 over the exact or maxDenom
        //you can stop here and return "0"
        if (absDoubleValue < (1/Math.max(exactDenom,  maxDenom))){
            return "0";
        }
        
        //this is necessary to prevent overflow in the maxDenom calculation
        //stink1
        if (wholePart+(int)decPart == wholePart+decPart){
            
            StringBuilder sb = new StringBuilder();
            if (isNeg){
                sb.append("-");
            }
            sb.append(Integer.toString((int)wholePart));
            return sb.toString();
        }
        
        SimpleFraction fract = null;
        try{
            //this should be the case because of the constructor
            if (exactDenom > 0){
                fract = calcFractionExactDenom(decPart, exactDenom);
            } else {
                fract = calcFractionMaxDenom(decPart, maxDenom);
            }
        } catch (SimpleFractionException e){
            e.printStackTrace();
            return Double.toString(doubleValue);
        }

        StringBuilder sb = new StringBuilder();
        
        //now format the results
        if (isNeg){
            sb.append("-");
        }
        
        //if whole part has to go into the numerator
        if ("".equals(wholePartFormatString)){
            int trueNum = (fract.getDenominator()*(int)wholePart)+fract.getNumerator();
            sb.append(trueNum).append("/").append(fract.getDenominator());
            return sb.toString();
        }
        
        
        //short circuit if fraction is 0 or 1
        if (fract.getNumerator() == 0){
            sb.append(Integer.toString((int)wholePart));
            return sb.toString();
        } else if (fract.getNumerator() == fract.getDenominator()){
            sb.append(Integer.toString((int)wholePart+1));
            return sb.toString();
        }
       //as mentioned above, this ignores the exact space formatting in Excel
        if (wholePart > 0){
            sb.append(Integer.toString((int)wholePart)).append(" ");
        }
        sb.append(fract.getNumerator()).append("/").append(fract.getDenominator());
        return sb.toString();
    }

    public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
        return toAppendTo.append(format((Number)obj));
    }

    public Object parseObject(String source, ParsePosition pos) {
        throw new NotImplementedException("Reverse parsing not supported");
    }

    private SimpleFraction calcFractionMaxDenom(double value, int maxDenominator) 
            throws SimpleFractionException{
        /*
         * Lifted wholesale from org.apache.math.fraction.Fraction 2.2
         */
        double epsilon = 0.000000000001f;
        int maxIterations = 100;
        long overflow = Integer.MAX_VALUE;
        double r0 = value;
        long a0 = (long)Math.floor(r0);
        if (Math.abs(a0) > overflow) {
            throw new SimpleFractionException(
                    String.format("value > Integer.MAX_VALUE: %d.", a0));
        }

        // check for (almost) integer arguments, which should not go
        // to iterations.
        if (Math.abs(a0 - value) < epsilon) {
            return new SimpleFraction((int) a0, 1);
        }

        long p0 = 1;
        long q0 = 0;
        long p1 = a0;
        long q1 = 1;

        long p2 = 0;
        long q2 = 1;

        int n = 0;
        boolean stop = false;
        do {
            ++n;
            double r1 = 1.0 / (r0 - a0);
            long a1 = (long)Math.floor(r1);
            p2 = (a1 * p1) + p0;
            q2 = (a1 * q1) + q0;
            if ((Math.abs(p2) > overflow) || (Math.abs(q2) > overflow)) {
                throw new SimpleFractionException(
                        String.format("Greater than overflow in loop %f, %d, %d", value, p2, q2));
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
            throw new SimpleFractionException("n greater than max iterations " + value + " : " + maxIterations);
        }

        if (q2 < maxDenominator) {
            return new SimpleFraction((int) p2, (int) q2);
        } else {
            return new SimpleFraction((int) p1, (int) q1);
        }
    }

    private SimpleFraction calcFractionExactDenom(double val, int exactDenom){
        int num =  (int)Math.round(val*(double)exactDenom);
        return new SimpleFraction(num,exactDenom);
    }

    private class SimpleFraction {
        private final int num;
        private final int denom;

        public SimpleFraction(int num, int denom) {
            this.num = num;
            this.denom = denom;
        }

        public int getNumerator() {
            return num;
        }
        public int getDenominator() {
            return denom;
        }
    }
    private class SimpleFractionException extends Throwable{
        private SimpleFractionException(String message){
            super(message);
        }
    }
}
