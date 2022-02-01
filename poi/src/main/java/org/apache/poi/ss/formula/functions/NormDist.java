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
 * Implementation for Excel NORMDIST() and NORM.DIST() functions.
 * <ul>
 *   <li>https://support.microsoft.com/en-us/office/normdist-function-126db625-c53e-4591-9a22-c9ff422d6d58</li>
 *   <li>https://support.microsoft.com/en-us/office/norm-dist-function-edb1cc14-a21c-4e53-839d-8082074c9f8d</li>
 * </ul>
 */
public final class NormDist extends Fixed4ArgFunction implements FreeRefFunction {

    public static final NormDist instance = new NormDist();

    static double probability(double x, double mean, double stdev, boolean cumulative) {
        NormalDistribution normalDistribution = new NormalDistribution(mean, stdev);
        return cumulative ? normalDistribution.cumulativeProbability(x) : normalDistribution.density(x);
    }

    private NormDist() {}

    @Override
    public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg1, ValueEval arg2,
                              ValueEval arg3, ValueEval arg4) {
        try {
            Double xval = evaluateValue(arg1, srcRowIndex, srcColumnIndex);
            if (xval == null) {
                return ErrorEval.VALUE_INVALID;
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
            Boolean cumulative = OperandResolver.coerceValueToBoolean(arg4, false);
            if (cumulative == null) {
                return ErrorEval.VALUE_INVALID;
            }

            return new NumberEval(probability(
                    xval.doubleValue(), mean.doubleValue(), stdev.doubleValue(), cumulative.booleanValue()));
        } catch (EvaluationException e) {
            return e.getErrorEval();
        }
    }

    @Override
    public ValueEval evaluate(ValueEval[] args, OperationEvaluationContext ec) {
        if (args.length == 4) {
            return evaluate(ec.getRowIndex(), ec.getColumnIndex(), args[0], args[1], args[2], args[3]);
        }

        return ErrorEval.VALUE_INVALID;
    }

    private static Double evaluateValue(ValueEval arg, int srcRowIndex, int srcColumnIndex) throws EvaluationException {
        ValueEval veText = OperandResolver.getSingleValue(arg, srcRowIndex, srcColumnIndex);
        String strText1 = OperandResolver.coerceValueToString(veText);
        return OperandResolver.parseDouble(strText1);
    }
}