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

package org.apache.poi.ss.formula.functions;

import org.apache.poi.ss.formula.eval.BoolEval;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.EvaluationException;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.ValueEval;

public class Poisson {

    private static final double DEFAULT_RETURN_RESULT =1;

    /** All long-representable factorials */
    private static final long[] FACTORIALS = {
        1L,                  1L,                   2L,
        6L,                 24L,                 120L,
        720L,               5040L,               40320L,
        362880L,            3628800L,            39916800L,
        479001600L,         6227020800L,         87178291200L,
        1307674368000L,     20922789888000L,     355687428096000L,
        6402373705728000L, 121645100408832000L, 2432902008176640000L };

    /**
     * This checks is x = 0 and the mean = 0.
     * Excel currently returns the value 1 where as the
     * maths common implementation will error.
     * @param x  The number.
     * @param mean The mean.
     * @return If a default value should be returned.
     */
    private static boolean isDefaultResult(double x, double mean) {
        return x == 0 && mean == 0;
    }

    private static void checkArgument(double aDouble) throws EvaluationException {
        NumericFunction.checkValue(aDouble);

        // make sure that the number is positive
        if (aDouble < 0) {
            throw new EvaluationException(ErrorEval.NUM_ERROR);
        }
    }

    private static double probability(int k, double lambda) {
        return Math.pow(lambda, k) * Math.exp(-lambda) / factorial(k);
    }

    private static double cumulativeProbability(int x, double lambda) {
        double result = 0;
        for(int k = 0; k <= x; k++){
            result += probability(k, lambda);
        }
        return result;
    }

    private static long factorial(final int n) {
        if (n < 0 || n > 20) {
            throw new IllegalArgumentException("Valid argument should be in the range [0..20]");
        }
        return FACTORIALS[n];
    }

    public static ValueEval evaluate(ValueEval[] args, int srcRowIndex, int srcColumnIndex) {
        if (args.length != 3) {
            return ErrorEval.VALUE_INVALID;
        }
        ValueEval arg0 = args[0];
        ValueEval arg1 = args[1];
        ValueEval arg2 = args[2];

        try {
            // arguments/result for this function
            double x = NumericFunction.singleOperandEvaluate(arg0, srcRowIndex, srcColumnIndex);
            double mean = NumericFunction.singleOperandEvaluate(arg1, srcRowIndex, srcColumnIndex);

            // check for default result : excel implementation for 0,0
            // is different to Math Common.
            if (isDefaultResult(x,mean)) {
                return new NumberEval(DEFAULT_RETURN_RESULT);
            }
            // check the arguments : as per excel function def
            checkArgument(x);
            checkArgument(mean);

            // truncate x : as per excel function def
            boolean cumulative = ((BoolEval)arg2).getBooleanValue();
            double result = cumulative ? cumulativeProbability((int) x, mean) : probability((int) x, mean);

            // check the result
            NumericFunction.checkValue(result);

            return new NumberEval(result);
        } catch (EvaluationException e) {
            return e.getErrorEval();
        }
    }

}
