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

import org.apache.poi.ss.formula.eval.*;
import org.apache.poi.ss.util.NumberToTextConverter;
import org.apache.poi.util.Internal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of 'the Excel function PERCENTRANK()
 *
 * <b>Syntax</b>:<br>
 * <b>PERCENTRANK</b>(<b>array</b>, <b>X</b>, <b>[significance]</b>)<p>
 *
 * <b>array</b>  The array or range of data with numeric values that defines relative standing.<br>
 * <b>X</b>  The value for which you want to know the rank.<br>
 * <b>significance</b>  Optional. A value that identifies the number of significant digits for the returned percentage value.
 * If omitted, PERCENTRANK uses three digits (0.xxx).<br>
 * <br>
 * Returns a number between 0 and 1 representing a percentage.
 *
 * @since POI 5.1.0
 */
public final class PercentRank implements Function {

    public static final Function instance = new PercentRank();

    private PercentRank() {
        // Enforce singleton
    }

    public ValueEval evaluate(ValueEval[] args, int srcRowIndex, int srcColumnIndex) {
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
            List<ValueEval> values = getValues(args[0], srcRowIndex, srcColumnIndex);
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
        if (recurse) {
            for (Double d : numbers) {
                if (d <= x && d > closestMatchBelow) closestMatchBelow = d;
                if (d > x && d < closestMatchAbove) closestMatchAbove = d;
            }
        }
        if (!recurse || closestMatchBelow == x || closestMatchAbove == x) {
            int lessThanCount = 0;
            int greaterThanCount = 0;
            for (Double d : numbers) {
                if (d < x) lessThanCount++;
                else if (d > x) greaterThanCount++;
            }
            if (greaterThanCount == numbers.size() || lessThanCount == numbers.size()) {
                return ErrorEval.NA;
            }
            if (lessThanCount + greaterThanCount == 0) {
                return new NumberEval(0);
            } else {
                BigDecimal result = BigDecimal.valueOf((double)lessThanCount / (double)(lessThanCount + greaterThanCount));
                return new NumberEval(round(result, significance));
            }
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
            return interpolate(x, closestMatchBelow, closestMatchAbove, (NumberEval)belowRank, (NumberEval)aboveRank, significance);
        }
    }

    @Internal
    public static NumberEval interpolate(double x, double closestMatchBelow, double closestMatchAbove,
                                         NumberEval belowRank, NumberEval aboveRank, int significance) {
        double diff = closestMatchAbove - closestMatchBelow;
        double pos = x - closestMatchBelow;
        BigDecimal rankDiff = new BigDecimal(NumberToTextConverter.toText(aboveRank.getNumberValue() - belowRank.getNumberValue()));
        BigDecimal result = BigDecimal.valueOf(belowRank.getNumberValue()).add(rankDiff.multiply(BigDecimal.valueOf(pos / diff)));
        return new NumberEval(round(result, significance));
    }

    @Internal
    public static double round(BigDecimal bd, int significance) {
        //the rounding in https://support.microsoft.com/en-us/office/percentrank-function-f1b5836c-9619-4847-9fc9-080ec9024442
        //is very inconsistent, this hodge podge of rounding modes is the only way to match Excel results
        BigDecimal bd2 = bd.setScale(significance + 3, RoundingMode.HALF_UP);
        return bd2.setScale(significance, RoundingMode.DOWN).doubleValue();
    }

    @Internal
    public static List<ValueEval> getValues(ValueEval eval, int srcRowIndex, int srcColumnIndex) throws EvaluationException {
        if (eval instanceof AreaEval) {
            AreaEval ae = (AreaEval)eval;
            List<ValueEval> list = new ArrayList<>();
            for (int r = ae.getFirstRow(); r <= ae.getLastRow(); r++) {
                for (int c = ae.getFirstColumn(); c <= ae.getLastColumn(); c++) {
                    list.add(OperandResolver.getSingleValue(ae.getAbsoluteValue(r, c), r, c));
                }
            }
            return list;
        } else {
            return Collections.singletonList(OperandResolver.getSingleValue(eval, srcRowIndex, srcColumnIndex));
        }
    }
}
