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

import org.apache.poi.ss.formula.functions.ArrayFunction;
import org.apache.poi.ss.formula.functions.Fixed2ArgFunction;
import org.apache.poi.ss.formula.functions.Function;
import org.apache.poi.ss.util.NumberToTextConverter;

import java.math.BigDecimal;
import java.math.MathContext;

public abstract class TwoOperandNumericOperation extends Fixed2ArgFunction implements ArrayFunction {

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
                        double result = evaluate(d0, d1);
                        return new NumberEval(result);
                    } catch (EvaluationException e){
                        return e.getErrorEval();
                    }
                });

    }

    @Override
    public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0, ValueEval arg1) {
        double result;
        try {
            double d0 = singleOperandEvaluate(arg0, srcRowIndex, srcColumnIndex);
            double d1 = singleOperandEvaluate(arg1, srcRowIndex, srcColumnIndex);
            result = evaluate(d0, d1);
            if (result == 0.0) { // this '==' matches +0.0 and -0.0
                // Excel converts -0.0 to +0.0 for '*', '/', '%', '+' and '^'
                if (!(this instanceof SubtractEvalClass)) {
                    return NumberEval.ZERO;
                }
            }
            if (Double.isNaN(result) || Double.isInfinite(result)) {
                return ErrorEval.NUM_ERROR;
            }
        } catch (EvaluationException e) {
            return e.getErrorEval();
        }
        return new NumberEval(result);
    }

    protected abstract double evaluate(double d0, double d1) throws EvaluationException;

    public static final Function AddEval = new TwoOperandNumericOperation() {
        @Override
        protected double evaluate(double d0, double d1) {
            return d0+d1;
        }
    };
    public static final Function DivideEval = new TwoOperandNumericOperation() {
        @Override
        protected double evaluate(double d0, double d1) throws EvaluationException {
            if (d1 == 0.0) {
                throw new EvaluationException(ErrorEval.DIV_ZERO);
            }
            BigDecimal bd0 = new BigDecimal(NumberToTextConverter.toText(d0));
            BigDecimal bd1 = new BigDecimal(NumberToTextConverter.toText(d1));
            return bd0.divide(bd1, MathContext.DECIMAL128).doubleValue();
        }
    };
    public static final Function MultiplyEval = new TwoOperandNumericOperation() {
        @Override
        protected double evaluate(double d0, double d1) {
            BigDecimal bd0 = new BigDecimal(NumberToTextConverter.toText(d0));
            BigDecimal bd1 = new BigDecimal(NumberToTextConverter.toText(d1));
            return bd0.multiply(bd1).doubleValue();
        }
    };
    public static final Function PowerEval = new TwoOperandNumericOperation() {
        @Override
        protected double evaluate(double d0, double d1) {
            if(d0 < 0 && Math.abs(d1) > 0.0 && Math.abs(d1) < 1.0) {
                return -1 * Math.pow(d0 * -1, d1);
            }
            return Math.pow(d0, d1);
        }
    };
    private static final class SubtractEvalClass extends TwoOperandNumericOperation {
        public SubtractEvalClass() {
            //
        }
        @Override
        protected double evaluate(double d0, double d1) {
            return d0-d1;
        }
    }
    public static final Function SubtractEval = new SubtractEvalClass();
}
