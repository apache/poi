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
 * Implementation for Excel T.DIST() function.
 * <p>
 * <b>Syntax</b>:<br> <b>T.DIST </b>(<b>X</b>,<b>Deg_freedom</b>,<b>Cumulative</b>)<br>
 * <p>
 * Returns the Student's left-tailed t-distribution.
 *
 * The t-distribution is used in the hypothesis testing of small sample data sets.
 * Use this function in place of a table of critical values for the t-distribution.
 *
 * <ul>
 *     <li>X     Required. The numeric value at which to evaluate the distribution.</li>
 *     <li>Deg_freedom     Required. An integer indicating the number of degrees of freedom.</li>
 *     <li>Cumulative      Required. A logical value that determines the form of the function. If cumulative is TRUE,
 *     T.DIST returns the cumulative distribution function; if FALSE, it returns the probability density function.</li>
 * </ul>
 *
 * <ul>
 *     <li>If any argument is non-numeric, T.DIST returns the #VALUE! error value.</li>
 *     <li>If Deg_freedom &lt; 1, T.DIST returns the #NUM! error value.</li>
 *     <li>The Deg_freedom argument is truncated to an integer.
 * </ul>
 *
 * https://support.microsoft.com/en-us/office/t-dist-rt-function-20a30020-86f9-4b35-af1f-7ef6ae683eda
 */
public final class TDistLt extends Fixed3ArgFunction implements FreeRefFunction {

    public static final TDistLt instance = new TDistLt();

    private static double tdistCumulative(double x, int degreesOfFreedom) {
        TDistribution tdist = new TDistribution(null, degreesOfFreedom);
        return tdist.cumulativeProbability(x);
    }

    private static double tdistDensity(double x, int degreesOfFreedom) {
        TDistribution tdist = new TDistribution(null, degreesOfFreedom);
        return tdist.density(x);
    }

    @Override
    public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg1, ValueEval arg2, ValueEval arg3) {
        try {
            Double number1 = evaluateValue(arg1, srcRowIndex, srcColumnIndex);
            if (number1 == null) {
                return ErrorEval.VALUE_INVALID;
            }
            Double number2 = evaluateValue(arg2, srcRowIndex, srcColumnIndex);
            if (number2 == null) {
                return ErrorEval.VALUE_INVALID;
            }
            int degreesOfFreedom = number2.intValue();
            if (degreesOfFreedom < 1) {
                return ErrorEval.NUM_ERROR;
            }
            Boolean cumulativeFlag = evaluateBoolean(arg3, srcRowIndex, srcColumnIndex);
            if (cumulativeFlag == null) {
                return ErrorEval.VALUE_INVALID;
            }
            if (cumulativeFlag.booleanValue()) {
                return new NumberEval(tdistCumulative(number1, degreesOfFreedom));
            } else {
                return new NumberEval(tdistDensity(number1, degreesOfFreedom));
            }
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

    private static Boolean evaluateBoolean(ValueEval arg, int srcRowIndex, int srcColumnIndex) throws EvaluationException {
        ValueEval veText = OperandResolver.getSingleValue(arg, srcRowIndex, srcColumnIndex);
        return OperandResolver.coerceValueToBoolean(veText, false);
    }
}