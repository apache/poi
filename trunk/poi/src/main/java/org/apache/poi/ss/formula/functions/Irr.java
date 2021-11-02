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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.EvaluationException;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.ValueEval;

/**
 * Calculates the internal rate of return.
 *
 * Syntax is IRR(values) or IRR(values,guess)
 *
 * @see <a href="http://en.wikipedia.org/wiki/Internal_rate_of_return#Numerical_solution">Wikipedia on IRR</a>
 * @see <a href="http://office.microsoft.com/en-us/excel-help/irr-HP005209146.aspx">Excel IRR</a>
 */
public final class Irr implements Function {
    private static final int MAX_ITERATION_COUNT = 1000;
    private static final double ABSOLUTE_ACCURACY = 1E-7;
    private static final Logger LOGGER = LogManager.getLogger(Irr.class);


    public ValueEval evaluate(final ValueEval[] args, final int srcRowIndex, final int srcColumnIndex) {
        if(args.length == 0 || args.length > 2) {
            // Wrong number of arguments
            return ErrorEval.VALUE_INVALID;
        }

        try {
            double[] values = AggregateFunction.ValueCollector.collectValues(args[0]);
            double guess;
            if(args.length == 2) {
                guess = NumericFunction.singleOperandEvaluate(args[1], srcRowIndex, srcColumnIndex);
            } else {
                guess = 0.1d;
            }
            double result = irr(values, guess);
            NumericFunction.checkValue(result);
            return new NumberEval(result);
        } catch (EvaluationException e){
            return e.getErrorEval();
        }
    }

    /**
     * Computes the internal rate of return using an estimated irr of 10 percent.
     *
     * @param income the income values.
     * @return the irr.
     */
    public static double irr(double[] income) {
        return irr(income, 0.1d);
    }


    /**
     * Calculates IRR using the Newton-Raphson Method.
     * <p>
     * Starting with the guess, the method cycles through the calculation until the result
     * is accurate within 0.00001 percent. If IRR can't find a result that works
     * after 1000 tries, the {@code Double.NaN} is returned.
     *
     * <p>
     *   The implementation is inspired by the NewtonSolver from the Apache Commons-Math library,
     *   @see <a href="http://commons.apache.org">http://commons.apache.org</a>
     *
     *
     * @param values        the income values.
     * @param guess         the initial guess of irr.
     * @return the irr value. The method returns {@code Double.NaN}
     *  if the maximum iteration count is exceeded
     *
     * @see <a href="http://en.wikipedia.org/wiki/Internal_rate_of_return#Numerical_solution">
     *     http://en.wikipedia.org/wiki/Internal_rate_of_return#Numerical_solution</a>
     * @see <a href="http://en.wikipedia.org/wiki/Newton%27s_method">
     *     http://en.wikipedia.org/wiki/Newton%27s_method</a>
     */
    public static double irr(double[] values, double guess) {

        double x0 = guess;

        for (int i = 0; i < MAX_ITERATION_COUNT; i++) {

            // the value of the function (NPV) and its derivation can be calculated in the same loop
            final double factor = 1.0 + x0;
            double denominator = factor;
            if (denominator == 0) {
                LOGGER.atWarn().log("Returning NaN because IRR has found an denominator of 0");
                return Double.NaN;
            }

            double fValue = values[0];
            double fDerivative = 0;
            for (int k = 1; k < values.length; k++) {
                final double value = values[k];
                fValue += value / denominator;
                denominator *= factor;
                fDerivative -= k * value / denominator;
            }

            // the essence of the Newton-Raphson Method
            if (fDerivative == 0) {
                LOGGER.atWarn().log("Returning NaN because IRR has found an fDerivative of 0");
                return Double.NaN;
            }
            double x1 =  x0 - fValue/fDerivative;

            if (Math.abs(x1 - x0) <= ABSOLUTE_ACCURACY) {
                return x1;
            }

            x0 = x1;
        }
        // maximum number of iterations is exceeded
        LOGGER.atWarn().log("Returning NaN because IRR has reached max number of iterations allowed: {}", MAX_ITERATION_COUNT);
        return Double.NaN;
    }
}
