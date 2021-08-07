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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of 'Analysis Toolpak' the Excel function PERCENTRANK()
 *
 * <b>Syntax</b>:<br>
 * <b>PERCENTRANK</b>(<b>array</b>, <b>X</b>, <b>[significance]</b>)<p>
 *
 * <b>array</b>  The array or range of data with numeric values that defines relative standing.<br>
 * <b>X</b>  The value for which you want to know the rank.<br>
 * <b>significance</b>  TOptional. A value that identifies the number of significant digits for the returned percentage value.
 * If omitted, PERCENTRANK uses three digits (0.xxx).<br>
 * <br>
 * Returns a number between 0 and 1 representing a percentage.
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
            BigDecimal result = new BigDecimal((double)lessThanCount / (double)(lessThanCount + greaterThanCount));
            return new NumberEval(round(result, significance, RoundingMode.DOWN));
        } else {
            ValueEval belowRank = calculateRank(numbers, closestMatchBelow, significance, false);
            if (!(belowRank instanceof NumberEval)) {
                return belowRank;
            }
            ValueEval aboveRank = calculateRank(numbers, closestMatchAbove, significance, false);
            if (!(aboveRank instanceof NumberEval)) {
                return aboveRank;
            }
            NumberEval below = (NumberEval)belowRank;
            NumberEval above = (NumberEval)aboveRank;
            double diff = closestMatchAbove - closestMatchBelow;
            double pos = x - closestMatchBelow;
            double rankDiff = above.getNumberValue() - below.getNumberValue();
            BigDecimal result = new BigDecimal(below.getNumberValue() + (rankDiff * (pos / diff)));
            return new NumberEval(round(result, significance, RoundingMode.HALF_UP));
        }
    }

    private double round(BigDecimal bd, int significance, RoundingMode rounding) {
        //the rounding in https://support.microsoft.com/en-us/office/percentrank-function-f1b5836c-9619-4847-9fc9-080ec9024442
        //is very inconsistent, this hodge podge of rounding modes is the only way to match Excel results
        return bd.setScale(significance, rounding).doubleValue();
    }

    private List<ValueEval> getValues(ValueEval eval, int srcRowIndex, int srcColumnIndex) throws EvaluationException {
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
