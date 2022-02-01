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

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.poi.ss.formula.OperationEvaluationContext;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.EvaluationException;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.OperandResolver;
import org.apache.poi.ss.formula.eval.ValueEval;

/**
 * Implementation for Excel NORMINV() and NORM.INV() functions.
 * <ul>
 *   <li>https://support.microsoft.com/en-us/office/norminv-function-87981ab8-2de0-4cb0-b1aa-e21d4cb879b8</li>
 *   <li>https://support.microsoft.com/en-us/office/norm-inv-function-54b30935-fee7-493c-bedb-2278a9db7e13</li>
 * </ul>
 */
public final class NormInv extends Fixed3ArgFunction implements FreeRefFunction {

    public static final NormInv instance = new NormInv();

    static double inverse(double probability, double mean, double stdev) {
        NormalDistribution normalDistribution = new NormalDistribution(mean, stdev);
        return normalDistribution.inverseCumulativeProbability(probability);
    }

    private NormInv() {}

    @Override
    public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg1, ValueEval arg2, ValueEval arg3) {
        try {
            Double probability = evaluateValue(arg1, srcRowIndex, srcColumnIndex);
            if (probability == null) {
                return ErrorEval.VALUE_INVALID;
            } else if (probability <= 0 || probability >= 1) {
                return ErrorEval.NUM_ERROR;
            }
            Double mean = evaluateValue(arg2, srcRowIndex, srcColumnIndex);
            if (mean == null) {
                return ErrorEval.VALUE_INVALID;
            }
            Double stdev = evaluateValue(arg3, srcRowIndex, srcColumnIndex);
            if (stdev == null) {
                return ErrorEval.VALUE_INVALID;
            } else if (stdev.doubleValue() <= 0) {
                return ErrorEval.NUM_ERROR;
            }

            return new NumberEval(inverse(
                    probability.doubleValue(), mean.doubleValue(), stdev.doubleValue()));
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