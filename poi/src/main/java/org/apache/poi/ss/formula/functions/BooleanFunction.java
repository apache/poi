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

import org.apache.poi.ss.formula.TwoDEval;
import org.apache.poi.ss.formula.eval.BoolEval;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.EvaluationException;
import org.apache.poi.ss.formula.eval.MissingArgEval;
import org.apache.poi.ss.formula.eval.OperandResolver;
import org.apache.poi.ss.formula.eval.RefEval;
import org.apache.poi.ss.formula.eval.ValueEval;

/**
 * Here are the general rules concerning Boolean functions:
 * <ol>
 * <li> Blanks are ignored (not either true or false) </li>
 * <li> Strings are ignored if part of an area ref or cell ref, otherwise they must be 'true' or 'false'</li>
 * <li> Numbers: 0 is false. Any other number is TRUE </li>
 * <li> Areas: *all* cells in area are evaluated according to the above rules</li>
 * </ol>
 */
public abstract class BooleanFunction implements Function,ArrayFunction {

    public final ValueEval evaluate(ValueEval[] args, int srcRow, int srcCol) {
        if (args.length < 1) {
            return ErrorEval.VALUE_INVALID;
        }
        boolean boolResult;
        try {
            boolResult = calculate(args);
        } catch (EvaluationException e) {
            return e.getErrorEval();
        }
        return BoolEval.valueOf(boolResult);
    }

    private boolean calculate(ValueEval[] args) throws EvaluationException {

        boolean result = getInitialResultValue();
        boolean atLeastOneNonBlank = false;

        /*
         * Note: no short-circuit boolean loop exit because any ErrorEvals will override the result
         */
        for (final ValueEval arg : args) {
            Boolean tempVe;
            if (arg instanceof TwoDEval) {
                TwoDEval ae = (TwoDEval) arg;
                int height = ae.getHeight();
                int width = ae.getWidth();
                for (int rrIx=0; rrIx<height; rrIx++) {
                    for (int rcIx=0; rcIx<width; rcIx++) {
                        ValueEval ve = ae.getValue(rrIx, rcIx);
                        tempVe = OperandResolver.coerceValueToBoolean(ve, true);
                        if (tempVe != null) {
                            result = partialEvaluate(result, tempVe);
                            atLeastOneNonBlank = true;
                        }
                    }
                }
                continue;
            }
            if (arg instanceof RefEval) {
                RefEval re = (RefEval) arg;
                final int firstSheetIndex = re.getFirstSheetIndex();
                final int lastSheetIndex = re.getLastSheetIndex();
                for (int sIx = firstSheetIndex; sIx <= lastSheetIndex; sIx++) {
                    ValueEval ve = re.getInnerValueEval(sIx);
                    tempVe = OperandResolver.coerceValueToBoolean(ve, true);
                    if (tempVe != null) {
                        result = partialEvaluate(result, tempVe);
                        atLeastOneNonBlank = true;
                    }
                }
                continue;
            }

            if (arg == MissingArgEval.instance) {
                tempVe = false;     // missing parameters are treated as FALSE
            } else {
                tempVe = OperandResolver.coerceValueToBoolean(arg, false);
            }

            if (tempVe != null) {
                result = partialEvaluate(result, tempVe);
                atLeastOneNonBlank = true;
            }
        }

        if (!atLeastOneNonBlank) {
            throw new EvaluationException(ErrorEval.VALUE_INVALID);
        }
        return result;
    }


    protected abstract boolean getInitialResultValue();
    protected abstract boolean partialEvaluate(boolean cumulativeResult, boolean currentValue);


    public static final Function AND = new BooleanFunction() {
        protected boolean getInitialResultValue() {
            return true;
        }
        protected boolean partialEvaluate(boolean cumulativeResult, boolean currentValue) {
            return cumulativeResult && currentValue;
        }
    };
    public static final Function OR = new BooleanFunction() {
        protected boolean getInitialResultValue() {
            return false;
        }
        protected boolean partialEvaluate(boolean cumulativeResult, boolean currentValue) {
            return cumulativeResult || currentValue;
        }
    };

    public static final Function FALSE = BooleanFunction::evaluateFalse;

    public static final Function TRUE = BooleanFunction::evaluateTrue;

    public static final Function NOT = BooleanFunction::evaluateNot;

    @Override
    public ValueEval evaluateArray(ValueEval[] args, int srcRowIndex, int srcColumnIndex) {
        return evaluate(args, srcRowIndex, srcColumnIndex);
    }

    private static ValueEval evaluateFalse(ValueEval[] args, int srcRowIndex, int srcColumnIndex) {
        return args.length != 0 ? ErrorEval.VALUE_INVALID : BoolEval.FALSE;
    }

    private static ValueEval evaluateTrue(ValueEval[] args, int srcRowIndex, int srcColumnIndex) {
        return args.length != 0 ? ErrorEval.VALUE_INVALID : BoolEval.TRUE;
    }

    private static ValueEval evaluateNot(ValueEval[] args, int srcRowIndex, int srcColumnIndex) {
        if (args.length != 1) {
            return ErrorEval.VALUE_INVALID;
        }
        java.util.function.Function<ValueEval, ValueEval> notInner = (va) -> {
            try {
                ValueEval ve = OperandResolver.getSingleValue(va, srcRowIndex, srcColumnIndex);
                Boolean b = OperandResolver.coerceValueToBoolean(ve, false);
                boolean boolArgVal = b != null && b;
                return BoolEval.valueOf(!boolArgVal);
            } catch (EvaluationException e) {
                return e.getErrorEval();
            }
        };

        return ArrayFunction._evaluateOneArrayArg(args[0], srcRowIndex, srcColumnIndex, notInner);
    }
}