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

import static org.apache.poi.ss.formula.eval.ErrorEval.VALUE_INVALID;

import org.apache.poi.ss.formula.eval.*;
import org.apache.poi.util.LocaleUtil;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public abstract class NumericFunction implements Function {

    private static final double ZERO = 0.0;
    private static final double TEN = 10.0;
    private static final double LOG_10_TO_BASE_e = Math.log(TEN);
    private static final long PARITY_MASK = 0xFFFFFFFFFFFFFFFEL;


    protected static double singleOperandEvaluate(ValueEval arg, int srcRowIndex, int srcColumnIndex) throws EvaluationException {
        if (arg == null) {
            throw new IllegalArgumentException("arg must not be null");
        }
        ValueEval ve = OperandResolver.getSingleValue(arg, srcRowIndex, srcColumnIndex);
        double result = OperandResolver.coerceValueToDouble(ve);
        checkValue(result);
        return result;
    }

    /**
     * @throws EvaluationException (#NUM!) if {@code result} is {@code NaN} or {@code Infinity}
     */
    public static void checkValue(double result) throws EvaluationException {
        if (Double.isNaN(result) || Double.isInfinite(result)) {
            throw new EvaluationException(ErrorEval.NUM_ERROR);
        }
    }

    @Override
    public final ValueEval evaluate(ValueEval[] args, int srcCellRow, int srcCellCol) {
        double result;
        try {
            result = eval(args, srcCellRow, srcCellCol);
            checkValue(result);
        } catch (EvaluationException e) {
            return e.getErrorEval();
        }
        return new NumberEval(result);
    }

    protected abstract double eval(ValueEval[] args, int srcCellRow, int srcCellCol) throws EvaluationException;

    public static final Function ABS = oneDouble(Math::abs);
    public static final Function ACOS = oneDouble(Math::acos);
    public static final Function ACOSH = oneDouble(MathX::acosh);
    public static final Function ASIN = oneDouble(Math::asin);
    public static final Function ASINH = oneDouble(MathX::asinh);
    public static final Function ATAN = oneDouble(Math::atan);
    public static final Function ATANH = oneDouble(MathX::atanh);
    public static final Function COS = oneDouble(Math::cos);
    public static final Function COSH = oneDouble(MathX::cosh);
    public static final Function DEGREES = oneDouble(Math::toDegrees);
    public static final Function DOLLAR = NumericFunction::evaluateDollar;

    private static ValueEval evaluateDollar(ValueEval[] args, int srcRowIndex, int srcColumnIndex) {
        if (args.length != 1 && args.length != 2) {
            return ErrorEval.VALUE_INVALID;
        }
        try {
            double val = singleOperandEvaluate(args[0], srcRowIndex, srcColumnIndex);
            double d1 = args.length == 1 ? 2.0 : singleOperandEvaluate(args[1], srcRowIndex, srcColumnIndex);

            // second arg converts to int by truncating toward zero
            int nPlaces = (int)d1;

            if (nPlaces > 127) {
                return VALUE_INVALID;
            }

            if (nPlaces < 0) {
                BigDecimal divisor = BigDecimal.valueOf(Math.pow(10, -nPlaces));
                BigInteger bigInt = BigDecimal.valueOf(val).divide(divisor, MathContext.DECIMAL128)
                        .toBigInteger().multiply(divisor.toBigInteger());
                val = bigInt.doubleValue();
            }

            DecimalFormat nf = (DecimalFormat) NumberFormat.getCurrencyInstance(LocaleUtil.getUserLocale());
            int decimalPlaces = Math.max(nPlaces, 0);
            if (LocaleUtil.getUserLocale().getCountry().equalsIgnoreCase("US")) {
                nf.setNegativePrefix("(" + nf.getDecimalFormatSymbols().getCurrencySymbol());
                nf.setNegativeSuffix(")");
            }
            nf.setMinimumFractionDigits(decimalPlaces);
            nf.setMaximumFractionDigits(decimalPlaces);

            return new StringEval(nf.format(val).replace("\u00a0"," "));
        } catch (EvaluationException e) {
            return e.getErrorEval();
        }
    }

    public static final Function EXP = oneDouble(d -> Math.pow(Math.E, d));
    public static final Function FACT = oneDouble(MathX::factorial);
    //https://support.microsoft.com/en-us/office/int-function-a6c4af9e-356d-4369-ab6a-cb1fd9d343ef
    public static final Function INT = oneDouble(d -> Math.round(d-0.5));
    public static final Function LN = oneDouble(Math::log);
    public static final Function LOG10 = oneDouble(d -> Math.log(d) / LOG_10_TO_BASE_e);
    public static final Function RADIANS = oneDouble(Math::toRadians);
    public static final Function SIGN = oneDouble(MathX::sign);
    public static final Function SIN = oneDouble(Math::sin);
    public static final Function SINH = oneDouble(MathX::sinh);
    public static final Function SQRT = oneDouble(Math::sqrt);
    public static final Function TAN = oneDouble(Math::tan);
    public static final Function TANH = oneDouble(MathX::tanh);

    /* -------------------------------------------------------------------------- */

    public static final Function ATAN2 = twoDouble((d0, d1) ->
        (d0 == ZERO && d1 == ZERO) ? ErrorEval.DIV_ZERO : Math.atan2(d1, d0)
    );

    public static final Function CEILING = twoDouble(MathX::ceiling);

    public static final Function COMBIN = twoDouble((d0, d1) ->
        (d0 > Integer.MAX_VALUE || d1 > Integer.MAX_VALUE) ? ErrorEval.NUM_ERROR : MathX.nChooseK((int) d0, (int) d1));

    public static final Function FLOOR = twoDouble((d0, d1) ->
        (d1 == ZERO) ? (d0 == ZERO ? ZERO : ErrorEval.DIV_ZERO) : MathX.floor(d0, d1));

    public static final Function MOD = twoDouble((d0, d1) ->
        (d1 == ZERO) ? ErrorEval.DIV_ZERO : MathX.mod(d0, d1));

    public static final Function POWER = twoDouble(Math::pow);

    public static final Function ROUND = twoDouble(MathX::round);
    public static final Function ROUNDDOWN = twoDouble(MathX::roundDown);
    public static final Function ROUNDUP = twoDouble(MathX::roundUp);
    public static final Function TRUNC = NumericFunction::evaluateTrunc;

    private static ValueEval evaluateTrunc(ValueEval[] args, int srcRowIndex, int srcColumnIndex) {
        if (args.length != 1 && args.length != 2) {
            return ErrorEval.VALUE_INVALID;
        }
        try {
            double d0 = singleOperandEvaluate(args[0], srcRowIndex, srcColumnIndex);
            double d1 = args.length == 1 ? 0 : singleOperandEvaluate(args[1], srcRowIndex, srcColumnIndex);
            double result = MathX.roundDown(d0, d1);
            checkValue(result);
            return new NumberEval(result);
        }catch (EvaluationException e) {
            return e.getErrorEval();
        }
    }

    /* -------------------------------------------------------------------------- */

    public static final Function LOG = Log::evaluate;

    static final NumberEval PI_EVAL = new NumberEval(Math.PI);
    public static final Function PI = NumericFunction::evaluatePI;

    private static ValueEval evaluatePI(ValueEval[] args, int srcRowIndex, int srcColumnIndex) {
        return (args.length != 0) ? ErrorEval.VALUE_INVALID : PI_EVAL;
    }

    public static final Function RAND = NumericFunction::evaluateRand;

    private static ValueEval evaluateRand(ValueEval[] args, int srcRowIndex, int srcColumnIndex) {
        return (args.length != 0) ? ErrorEval.VALUE_INVALID : new NumberEval(Math.random());
    }

    public static final Function POISSON = Poisson::evaluate;

    public static final Function ODD = oneDouble(NumericFunction::evaluateOdd);

    private static double evaluateOdd(double d) {
        if (d==0) {
            return 1;
        }
        double dpm = Math.abs(d)+1;
        long x = ((long) dpm) & PARITY_MASK;
        return (double) MathX.sign(d) * ((Double.compare(x, dpm) == 0) ? x-1 : x+1);
    }


    public static final Function EVEN = oneDouble(NumericFunction::evaluateEven);

    private static double evaluateEven(double d) {
        if (d==0) {
            return 0;
        }

        double dpm = Math.abs(d);
        long x = ((long) dpm) & PARITY_MASK;
        return (double) MathX.sign(d) * ((Double.compare(x, dpm) == 0) ? x : (x + 2));
    }


    private interface OneDoubleIf {
        double apply(double d);
    }

    private static Function oneDouble(OneDoubleIf doubleFun) {
        return (args, srcCellRow, srcCellCol) -> {
            if (args.length != 1) {
                return VALUE_INVALID;
            }
            try {
                double d = singleOperandEvaluate(args[0], srcCellRow, srcCellCol);
                double res = doubleFun.apply(d);
                return (Double.isNaN(res) || Double.isInfinite(res)) ? ErrorEval.NUM_ERROR : new NumberEval(res);
            } catch (EvaluationException e) {
                return e.getErrorEval();
            }
        };
    }

    private interface TwoDoubleIf {
        Object apply(double d1, double d2);
    }

    private static Function twoDouble(TwoDoubleIf doubleFun) {
        return (args, srcCellRow, srcCellCol) -> {
            if (args.length != 2) {
                return VALUE_INVALID;
            }
            try {
                double d1 = singleOperandEvaluate(args[0], srcCellRow, srcCellCol);
                double d2 = singleOperandEvaluate(args[1], srcCellRow, srcCellCol);
                Object res = doubleFun.apply(d1, d2);
                if (res instanceof ErrorEval) {
                    return (ErrorEval)res;
                }
                assert(res instanceof Double);
                double d = (Double)res;
                return (Double.isNaN(d) || Double.isInfinite(d)) ? ErrorEval.NUM_ERROR : new NumberEval(d);
            } catch (EvaluationException e) {
                return e.getErrorEval();
            }
        };
    }
}
