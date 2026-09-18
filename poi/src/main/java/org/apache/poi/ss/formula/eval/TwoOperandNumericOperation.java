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

package org.apache.poi.ss.formula.eval;

import java.math.BigDecimal;
import java.math.MathContext;

import org.apache.poi.ss.formula.functions.ArrayFunction;
import org.apache.poi.ss.formula.functions.Fixed2ArgFunction;
import org.apache.poi.ss.formula.functions.Function;
import org.apache.poi.ss.util.ExcelArithmetic;
import org.apache.poi.ss.util.NumberToTextConverter;

public abstract class TwoOperandNumericOperation extends Fixed2ArgFunction implements ArrayFunction {

    private static final long MAX15 = 999_999_999_999_999L;
    private static final long[] FIVE_POW = new long[22];
    private static final long SIGN_MASK = 0x8000000000000000L;
    private static final long EXP_MASK = 0x7FF;
    private static final int EXP_SHIFT = 52;
    private static final long FRACTION_MASK = 0x000FFFFFFFFFFFFFL;
    private static final long IMPLICIT_BIT = 0x0010000000000000L;

    static {
        FIVE_POW[0] = 1;
        for (int i = 1; i < FIVE_POW.length; i++) {
            FIVE_POW[i] = FIVE_POW[i - 1] * 5;
        }
    }

    protected final double singleOperandEvaluate(ValueEval arg, int srcCellRow, int srcCellCol) throws EvaluationException {
        ValueEval ve = OperandResolver.getSingleValue(arg, srcCellRow, srcCellCol);
        return OperandResolver.coerceValueToDouble(ve);
    }

    @Override
    public ValueEval evaluateArray(ValueEval[] args, int srcRowIndex, int srcColumnIndex) {
        if (args.length != 2) {
            return ErrorEval.VALUE_INVALID;
        }
        //return new ArrayEval().evaluate(srcRowIndex, srcColumnIndex, args[0], args[1]);

        return evaluateTwoArrayArgs(args[0], args[1], srcRowIndex, srcColumnIndex,
                (vA, vB) -> {
                    try {
                        double d0 = OperandResolver.coerceValueToDouble(vA);
                        double d1 = OperandResolver.coerceValueToDouble(vB);
                        return toValueEval(evaluate(d0, d1));
                    } catch (EvaluationException e){
                        return e.getErrorEval();
                    }
                });

    }

    @Override
    public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0, ValueEval arg1) {
        try {
            double d0 = singleOperandEvaluate(arg0, srcRowIndex, srcColumnIndex);
            double d1 = singleOperandEvaluate(arg1, srcRowIndex, srcColumnIndex);
            return toValueEval(evaluate(d0, d1));
        } catch (EvaluationException e) {
            return e.getErrorEval();
        }
    }

    private ValueEval toValueEval(double result) {
        if (result == 0.0) { // this '==' matches +0.0 and -0.0
            // Excel converts -0.0 to +0.0 for '*', '/', '%', '+' and '^'
            if (!(this instanceof SubtractEvalClass)) {
                return NumberEval.ZERO;
            }
        } else if (Math.abs(result) < Double.MIN_NORMAL) {
            // Excel does not support subnormal numbers: underflow gives 0
            return NumberEval.ZERO;
        }
        if (Double.isNaN(result) || Double.isInfinite(result)) {
            return ErrorEval.NUM_ERROR;
        }
        return new NumberEval(result);
    }

    /**
     * Returns {@code true} if the exact binary64 value of {@code d} is a decimal
     * number with at most 15 significant digits (including its trailing zeros).
     * For such operands the plain IEEE-754 operation is bit-identical to the
     * result of the BigDecimal-based evaluation, so the faster path can be taken
     * without changing semantics.
     */
    static boolean isExactShortDecimal(double d) {
        long bits = Double.doubleToRawLongBits(d);
        int expField = (int) ((bits >>> EXP_SHIFT) & EXP_MASK);
        if (expField == EXP_MASK) {
            return false;
        }
        if ((bits & Long.MAX_VALUE) == 0L) {
            return (bits & SIGN_MASK) == 0L;
        }
        long odd;
        int k;
        if (expField == 0) {
            odd = bits & FRACTION_MASK;
            k = -1074;
        } else {
            odd = (bits & FRACTION_MASK) | IMPLICIT_BIT;
            k = expField - 1075;
        }
        int tz = Long.numberOfTrailingZeros(odd);
        odd >>>= tz;
        k += tz;
        if (k >= 0) {
            if (k > 62) {
                return false;
            }
            long v = odd << k;
            if ((v >> k) != odd) {
                return false;
            }
            while (v % 10 == 0) {
                v /= 10;
            }
            return v <= MAX15;
        }
        int n = -k;
        if (n > 21) {
            return false;
        }
        return odd <= MAX15 / FIVE_POW[n];
    }

    protected abstract double evaluate(double d0, double d1) throws EvaluationException;

    public static final Function AddEval = new TwoOperandNumericOperation() {
        @Override
        protected double evaluate(double d0, double d1) {
            return ExcelArithmetic.approxAdd(d0, d1);
        }
    };
public static final Function DivideEval = new TwoOperandNumericOperation() {
        @Override
        protected double evaluate(double d0, double d1) throws EvaluationException {
            if (d1 == 0.0) {
                throw new EvaluationException(ErrorEval.DIV_ZERO);
            }
            if (d0 == 0.0) {
                return 0.0;
            }
            if (isExactShortDecimal(d0) && isExactShortDecimal(d1)) {
                return d0 / d1;
            }
            BigDecimal bd0 = new BigDecimal(NumberToTextConverter.toText(d0));
            BigDecimal bd1 = new BigDecimal(NumberToTextConverter.toText(d1));
            return bd0.divide(bd1, MathContext.DECIMAL128).doubleValue();
        }
    };
public static final Function MultiplyEval = new TwoOperandNumericOperation() {
        @Override
        protected double evaluate(double d0, double d1) {
            if (isExactShortDecimal(d0) && isExactShortDecimal(d1)) {
                return d0 == 0.0 || d1 == 0.0 ? 0.0 : d0 * d1;
            }
            BigDecimal bd0 = new BigDecimal(NumberToTextConverter.toText(d0));
            BigDecimal bd1 = new BigDecimal(NumberToTextConverter.toText(d1));
            return bd0.multiply(bd1).doubleValue();
        }
    };
    public static final Function PowerEval = new TwoOperandNumericOperation() {
        @Override
        protected double evaluate(double d0, double d1) throws EvaluationException {
            if (d0 == 0.0) {
                // Excel: 0^0 is #NUM! and 0^negative is #DIV/0! (Math.pow gives 1 and Infinity)
                if (d1 == 0.0) {
                    throw new EvaluationException(ErrorEval.NUM_ERROR);
                }
                if (d1 < 0.0) {
                    throw new EvaluationException(ErrorEval.DIV_ZERO);
                }
            }
            // a negative base with a non-integer exponent is NaN, hence #NUM!, as in Excel (and POWER)
            return Math.pow(d0, d1);
        }
    };
    private static final class SubtractEvalClass extends TwoOperandNumericOperation {
        public SubtractEvalClass() {
            //
        }
        @Override
        protected double evaluate(double d0, double d1) {
            return ExcelArithmetic.approxSub(d0, d1);
        }
    }
    public static final Function SubtractEval = new SubtractEvalClass();
}
