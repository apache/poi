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

import org.apache.poi.ss.formula.OperationEvaluationContext;
import org.apache.poi.ss.formula.eval.*;

/**
 * Implementation for Excel T.DIST.2T() function.
 * <p>
 * <b>Syntax</b>:<br> <b>T.DIST.2T </b>(<b>X</b>,<b>Deg_freedom</b>)<br>
 * <p>
 * Returns the two-tailed Student's t-distribution.
 *
 * The Student's t-distribution is used in the hypothesis testing of small sample data sets.
 * Use this function in place of a table of critical values for the t-distribution.
 *
 * <ul>
 *     <li>X     Required. The numeric value at which to evaluate the distribution.</li>
 *     <li>Deg_freedom     Required. An integer indicating the number of degrees of freedom.</li>
 * </ul>
 *
 * <ul>
 *     <li>If any argument is non-numeric, T.DIST.2T returns the #VALUE! error value.</li>
 *     <li>If Deg_freedom &lt; 1, T.DIST.2T returns the #NUM! error value.</li>
 *     <li>If x &lt; 0, then T.DIST.2T returns the #NUM! error value.</li>
 *     <li>The Deg_freedom argument is truncated to an integer.
 * </ul>
 *
 * https://support.microsoft.com/en-us/office/t-dist-2t-function-198e9340-e360-4230-bd21-f52f22ff5c28
 */
public final class TDist2t extends Fixed2ArgFunction implements FreeRefFunction {

    public static final TDist2t instance = new TDist2t();

    @Override
    public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg1, ValueEval arg2) {
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
            return new NumberEval(TDist.tdistTwoTails(Math.abs(number1), degreesOfFreedom));
        } catch (EvaluationException e) {
            return e.getErrorEval();
        }
    }

    @Override
    public ValueEval evaluate(ValueEval[] args, OperationEvaluationContext ec) {
         if (args.length == 2) {
            return evaluate(ec.getRowIndex(), ec.getColumnIndex(), args[0], args[1]);
        }

        return ErrorEval.VALUE_INVALID;
    }

    private static Double evaluateValue(ValueEval arg, int srcRowIndex, int srcColumnIndex) throws EvaluationException {
        ValueEval veText = OperandResolver.getSingleValue(arg, srcRowIndex, srcColumnIndex);
        String strText1 = OperandResolver.coerceValueToString(veText);
        return OperandResolver.parseDouble(strText1);
    }
}