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

import org.apache.commons.math3.distribution.TDistribution;
import org.apache.poi.ss.formula.OperationEvaluationContext;
import org.apache.poi.ss.formula.eval.*;

/**
 * Implementation for Excel TDIST() function.
 * <p>
 * <b>Syntax</b>:<br> <b>TDIST </b>(<b>X</b>,<b>Deg_freedom</b>,<b>Tails</b>)<br>
 * <p>
 * Returns the Percentage Points (probability) for the Student t-distribution where a numeric value (x)
 * is a calculated value of t for which the Percentage Points are to be computed. The t-distribution is used
 * in the hypothesis testing of small sample data sets. Use this function in place of a table of critical values
 * for the t-distribution.
 *
 * <ul>
 *     <li>X     Required. The numeric value at which to evaluate the distribution.</li>
 *     <li>Deg_freedom     Required. An integer indicating the number of degrees of freedom.</li>
 *     <li>Tails     Required. Specifies the number of distribution tails to return. If Tails = 1, TDIST returns the
 *     one-tailed distribution. If Tails = 2, TDIST returns the two-tailed distribution.</li>
 * </ul>
 *
 * <ul>
 *     <li>If any argument is non-numeric, TDIST returns the #VALUE! error value.</li>
 *     <li>If Deg_freedom &lt; 1, TDIST returns the #NUM! error value.</li>
 *     <li>The Deg_freedom and Tails arguments are truncated to integers.
 *     <li>If Tails is any value other than 1 or 2, TDIST returns the #NUM! error value.</li>
 *     <li>If x &lt; 0, then TDIST returns the #NUM! error value.</li>
 * </ul>
 *
 * https://support.microsoft.com/en-us/office/tdist-function-630a7695-4021-4853-9468-4a1f9dcdd192
 */
public final class TDist extends Fixed3ArgFunction implements FreeRefFunction {

    public static final TDist instance = new TDist();

    static double tdistOneTail(double x, int degreesOfFreedom) {
        TDistribution tdist = new TDistribution(null, degreesOfFreedom);
        return 1.0 - tdist.cumulativeProbability(x);
    }

    static double tdistTwoTails(double x, int degreesOfFreedom) {
        return 2 * tdistOneTail(x, degreesOfFreedom);
    }

    @Override
    public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg1, ValueEval arg2, ValueEval arg3) {
        try {
            Double number1 = evaluateValue(arg1, srcRowIndex, srcColumnIndex);
            if (number1 == null) {
                return ErrorEval.VALUE_INVALID;
            } else if (number1 < 0) {
                return ErrorEval.NUM_ERROR;
            }
            Double number2 = evaluateValue(arg2, srcRowIndex, srcColumnIndex);
            if (number2 == null) {
                return ErrorEval.VALUE_INVALID;
            }
            int degreesOfFreedom = number2.intValue();
            if (degreesOfFreedom < 1) {
                return ErrorEval.NUM_ERROR;
            }
            Double number3 = evaluateValue(arg3, srcRowIndex, srcColumnIndex);
            if (number3 == null) {
                return ErrorEval.VALUE_INVALID;
            }
            int tails = number3.intValue();
            if (!(tails == 1 || tails == 2)) {
                return ErrorEval.NUM_ERROR;
            }

            if (tails == 2) {
                return new NumberEval(tdistTwoTails(number1, degreesOfFreedom));
            }

            return new NumberEval(tdistOneTail(number1, degreesOfFreedom));
        } catch (EvaluationException e) {
            return e.getErrorEval();
        }
    }

    @Override
    public ValueEval evaluate(ValueEval[] args, OperationEvaluationContext ec) {
         if (args.length == 3) {
            return evaluate(ec.getRowIndex(), ec.getColumnIndex(), args[0], args[1], args[2]);
        }

        return ErrorEval.VALUE_INVALID;
    }

    private static Double evaluateValue(ValueEval arg, int srcRowIndex, int srcColumnIndex) throws EvaluationException {
        ValueEval veText = OperandResolver.getSingleValue(arg, srcRowIndex, srcColumnIndex);
        String strText1 = OperandResolver.coerceValueToString(veText);
        return OperandResolver.parseDouble(strText1);
    }
}