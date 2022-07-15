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

package org.apache.poi.ss.formula.atp;

import org.apache.poi.ss.formula.OperationEvaluationContext;
import org.apache.poi.ss.formula.eval.*;
import org.apache.poi.ss.formula.functions.FreeRefFunction;
import org.apache.poi.ss.formula.functions.PercentRank;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of 'Analysis Toolpak' the Excel function PERCENTRANK.EXC()
 *
 * <b>Syntax</b>:<br>
 * <b>PERCENTRANK.EXC</b>(<b>array</b>, <b>X</b>, <b>[significance]</b>)<p>
 *
 * <b>array</b>  The array or range of data with numeric values that defines relative standing.<br>
 * <b>X</b>  The value for which you want to know the rank.<br>
 * <b>significance</b>  Optional. A value that identifies the number of significant digits for the returned percentage value.
 * If omitted, PERCENTRANK.EXC uses three digits (0.xxx).<br>
 * <br>
 * Returns a number between 0 and 1 (exclusive) representing a percentage. PERCENTRANK.INC returns value between 0 and 1 (inclusive).
 *
 * @see PercentRank
 * @see PercentRankIncFunction
 * @since POI 5.1.0
 */
final class PercentRankExcFunction implements FreeRefFunction {

    public static final FreeRefFunction instance = new PercentRankExcFunction(ArgumentsEvaluator.instance);

    private ArgumentsEvaluator evaluator;

    private PercentRankExcFunction(ArgumentsEvaluator anEvaluator) {
        // enforces singleton
        this.evaluator = anEvaluator;
    }

    public ValueEval evaluate(ValueEval[] args, OperationEvaluationContext ec) {
        return evaluate(args, ec.getRowIndex(), ec.getColumnIndex());
    }

    private ValueEval evaluate(ValueEval[] args, int srcRowIndex, int srcColumnIndex) {
        if (args.length < 2) {
            return ErrorEval.VALUE_INVALID;
        }
        double x;
        try {
            ValueEval ev = OperandResolver.getSingleValue(args[1], srcRowIndex, srcColumnIndex);
            x = OperandResolver.coerceValueToDouble(ev);
        } catch (EvaluationException e) {
            ValueEval error = e.getErrorEval();
            if (error == ErrorEval.NUM_ERROR) {
                return error;
            }
            return ErrorEval.NUM_ERROR;
        }

        ArrayList<Double> numbers = new ArrayList<>();
        try {
            List<ValueEval> values = PercentRank.getValues(args[0], srcRowIndex, srcColumnIndex);
            for (ValueEval ev : values) {
                if (ev instanceof BlankEval || ev instanceof MissingArgEval) {
                    //skip
                } else {
                    numbers.add(OperandResolver.coerceValueToDouble(ev));
                }
            }
        } catch (EvaluationException e) {
            ValueEval error = e.getErrorEval();
            if (error != ErrorEval.NA) {
                return error;
            }
            return ErrorEval.NUM_ERROR;
        }

        if (numbers.isEmpty()) {
            return ErrorEval.NUM_ERROR;
        }

        int significance = 3;
        if (args.length > 2) {
            try {
                ValueEval ev = OperandResolver.getSingleValue(args[2], srcRowIndex, srcColumnIndex);
                significance = OperandResolver.coerceValueToInt(ev);
                if (significance < 1) {
                    return ErrorEval.NUM_ERROR;
                }
            } catch (EvaluationException e) {
                return e.getErrorEval();
            }
        }

        return calculateRank(numbers, x, significance, true);
    }

    private ValueEval calculateRank(List<Double> numbers, double x, int significance, boolean recurse) {
        double closestMatchBelow = Double.MIN_VALUE;
        double closestMatchAbove = Double.MAX_VALUE;
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        if (recurse) {
            for (Double d : numbers) {
                if (d <= x && d > closestMatchBelow) closestMatchBelow = d;
                if (d > x && d < closestMatchAbove) closestMatchAbove = d;
                if (d < min) min = d;
                if (d > max) max = d;
            }
            if (x < min || x > max) {
                return ErrorEval.NA;
            }
        }
        if (!recurse || closestMatchBelow == x || closestMatchAbove == x) {
            int lessThanCount = 0;
            for (Double d : numbers) {
                if (d < x) lessThanCount++;
            }
            BigDecimal result = BigDecimal.valueOf((double)(lessThanCount + 1) / (double)(numbers.size() + 1));
            return new NumberEval(PercentRank.round(result, significance));
        } else {
            int intermediateSignificance = significance < 5 ? 8 : significance + 3;
            ValueEval belowRank = calculateRank(numbers, closestMatchBelow, intermediateSignificance, false);
            if (!(belowRank instanceof NumberEval)) {
                return belowRank;
            }
            ValueEval aboveRank = calculateRank(numbers, closestMatchAbove, intermediateSignificance, false);
            if (!(aboveRank instanceof NumberEval)) {
                return aboveRank;
            }
            return PercentRank.interpolate(x, closestMatchBelow, closestMatchAbove,
                    (NumberEval)belowRank, (NumberEval)aboveRank, significance);
        }
    }
}
