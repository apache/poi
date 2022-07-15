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

import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.formula.ThreeDEval;
import org.apache.poi.ss.formula.TwoDEval;
import org.apache.poi.ss.formula.eval.BlankEval;
import org.apache.poi.ss.formula.eval.BoolEval;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.EvaluationException;
import org.apache.poi.ss.formula.eval.MissingArgEval;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.NumericValueEval;
import org.apache.poi.ss.formula.eval.OperandResolver;
import org.apache.poi.ss.formula.eval.RefEval;
import org.apache.poi.ss.formula.eval.StringValueEval;
import org.apache.poi.ss.formula.eval.ValueEval;

/**
 * This is the super class for all excel function evaluator
 * classes that take variable number of operands, and
 * where the order of operands does not matter
 */
public abstract class MultiOperandNumericFunction implements Function {
    public enum Policy {COERCE, SKIP, ERROR}

    private interface EvalConsumer<T, R> {
        void accept(T value, R receiver) throws EvaluationException;
    }

    private EvalConsumer<BoolEval, DoubleList> boolByRefConsumer;
    private EvalConsumer<BoolEval, DoubleList> boolByValueConsumer;
    private EvalConsumer<BlankEval, DoubleList> blankConsumer;
    private EvalConsumer<MissingArgEval, DoubleList> missingArgConsumer = ConsumerFactory.createForMissingArg(Policy.SKIP);

    protected MultiOperandNumericFunction(boolean isReferenceBoolCounted, boolean isBlankCounted) {
        boolByRefConsumer = ConsumerFactory.createForBoolEval(isReferenceBoolCounted ? Policy.COERCE : Policy.SKIP);
        boolByValueConsumer = ConsumerFactory.createForBoolEval(Policy.COERCE);
        blankConsumer = ConsumerFactory.createForBlank(isBlankCounted ? Policy.COERCE : Policy.SKIP);
    }

    private static final int DEFAULT_MAX_NUM_OPERANDS = SpreadsheetVersion.EXCEL2007.getMaxFunctionArgs();

    public void setMissingArgPolicy(Policy policy) {
        missingArgConsumer = ConsumerFactory.createForMissingArg(policy);
    }

    public void setBlankEvalPolicy(Policy policy) {
        blankConsumer = ConsumerFactory.createForBlank(policy);
    }

    /**
     * Functions like AVERAGEA() differ from AVERAGE() in the way they handle non-numeric cells.
     * AVERAGEA treats booleans as 1.0 (true) and 0.0 (false). String cells are treated as 0.0
     * (AVERAGE() ignores the cell altogether).
     *
     * @return whether to parse non-numeric cells
     */
    protected boolean treatStringsAsZero() {
        return false;
    }

    public final ValueEval evaluate(ValueEval[] args, int srcCellRow, int srcCellCol) {
        try {
            double[] values = getNumberArray(args);
            double d = evaluate(values);
            if (Double.isNaN(d) || Double.isInfinite(d)) {
                return ErrorEval.NUM_ERROR;
            }
            return new NumberEval(d);
        } catch (EvaluationException e) {
            return e.getErrorEval();
        }
    }

    protected abstract double evaluate(double[] values) throws EvaluationException;

    /**
     * Maximum number of operands accepted by this function.
     * Subclasses may override to change default value.
     */
    protected int getMaxNumOperands() {
        return DEFAULT_MAX_NUM_OPERANDS;
    }

    /**
     * Returns a double array that contains values for the numeric cells
     * from among the list of operands. Blanks and Blank equivalent cells
     * are ignored. Error operands or cells containing operands of type
     * that are considered invalid and would result in #VALUE! error in
     * excel cause this function to return <code>null</code>.
     *
     * @return never <code>null</code>
     */
    protected final double[] getNumberArray(ValueEval[] operands) throws EvaluationException {
        if (operands.length > getMaxNumOperands()) {
            throw EvaluationException.invalidValue();
        }
        DoubleList retval = new DoubleList();

        for (ValueEval operand : operands) {
            collectValues(operand, retval);
        }
        return retval.toArray();
    }

    /**
     * Whether to count nested subtotals.
     */
    public boolean isSubtotalCounted() {
        return true;
    }

    /**
     * @return true if values in hidden rows are counted
     * @see Subtotal
     */
    public boolean isHiddenRowCounted() {
        return true;
    }

    /**
     * Collects values from a single argument
     */
    private void collectValues(ValueEval operand, DoubleList temp) throws EvaluationException {
        if (operand instanceof ThreeDEval) {
            ThreeDEval ae = (ThreeDEval) operand;
            for (int sIx = ae.getFirstSheetIndex(); sIx <= ae.getLastSheetIndex(); sIx++) {
                int width = ae.getWidth();
                int height = ae.getHeight();
                for (int rrIx = 0; rrIx < height; rrIx++) {
                    for (int rcIx = 0; rcIx < width; rcIx++) {
                        ValueEval ve = ae.getValue(sIx, rrIx, rcIx);
                        if (!isSubtotalCounted() && ae.isSubTotal(rrIx, rcIx)) continue;
                        if (!isHiddenRowCounted() && ae.isRowHidden(rrIx)) continue;
                        collectValue(ve, !treatStringsAsZero(), temp);
                    }
                }
            }
            return;
        }
        if (operand instanceof TwoDEval) {
            TwoDEval ae = (TwoDEval) operand;
            int width = ae.getWidth();
            int height = ae.getHeight();
            for (int rrIx = 0; rrIx < height; rrIx++) {
                for (int rcIx = 0; rcIx < width; rcIx++) {
                    ValueEval ve = ae.getValue(rrIx, rcIx);
                    if (!isSubtotalCounted() && ae.isSubTotal(rrIx, rcIx)) continue;
                    collectValue(ve, !treatStringsAsZero(), temp);
                }
            }
            return;
        }
        if (operand instanceof RefEval) {
            RefEval re = (RefEval) operand;
            for (int sIx = re.getFirstSheetIndex(); sIx <= re.getLastSheetIndex(); sIx++) {
                collectValue(re.getInnerValueEval(sIx), !treatStringsAsZero(), temp);
            }
            return;
        }
        collectValue(operand, false, temp);
    }

    private void collectValue(ValueEval ve, boolean isViaReference, DoubleList temp) throws EvaluationException {
        if (ve == null) {
            throw new IllegalArgumentException("ve must not be null");
        }
        if (ve instanceof BoolEval) {
            BoolEval boolEval = (BoolEval) ve;
            if (isViaReference) {
                boolByRefConsumer.accept(boolEval, temp);
            } else {
                boolByValueConsumer.accept(boolEval, temp);
            }
            return;
        }
        if (ve instanceof NumericValueEval) {
            NumericValueEval ne = (NumericValueEval) ve;
            temp.add(ne.getNumberValue());
            return;
        }
        if (ve instanceof StringValueEval) {
            if (isViaReference) {
                // ignore all ref strings
                return;
            }
            if (treatStringsAsZero()) {
                temp.add(0.0);
            } else {
                String s = ((StringValueEval) ve).getStringValue().trim();
                Double d = OperandResolver.parseDouble(s);
                if (d == null) {
                    throw new EvaluationException(ErrorEval.VALUE_INVALID);
                } else {
                    temp.add(d.doubleValue());
                }
            }
            return;
        }
        if (ve instanceof ErrorEval) {
            throw new EvaluationException((ErrorEval) ve);
        }
        if (ve == BlankEval.instance) {
            blankConsumer.accept((BlankEval) ve, temp);
            return;
        }
        if (ve == MissingArgEval.instance) {
            missingArgConsumer.accept((MissingArgEval) ve, temp);
            return;
        }
        throw new RuntimeException("Invalid ValueEval type passed for conversion: ("
                + ve.getClass() + ")");
    }

    private static class ConsumerFactory {
        static EvalConsumer<MissingArgEval, DoubleList> createForMissingArg(Policy policy) {
            final EvalConsumer<MissingArgEval, DoubleList> coercer =
                    (MissingArgEval value, DoubleList receiver) -> receiver.add(0.0);
            return createAny(coercer, policy);
        }

        static EvalConsumer<BoolEval, DoubleList> createForBoolEval(Policy policy) {
            final EvalConsumer<BoolEval, DoubleList> coercer =
                    (BoolEval value, DoubleList receiver) -> receiver.add(value.getNumberValue());
            return createAny(coercer, policy);
        }

        static EvalConsumer<BlankEval, DoubleList> createForBlank(Policy policy) {
            final EvalConsumer<BlankEval, DoubleList> coercer =
                    (BlankEval value, DoubleList receiver) -> receiver.add(0.0);
            return createAny(coercer, policy);
        }

        private static <T> EvalConsumer<T, DoubleList> createAny(EvalConsumer<T, DoubleList> coercer, Policy policy) {
            switch (policy) {
                case COERCE:
                    return coercer;
                case SKIP:
                    return doNothing();
                case ERROR:
                    return throwValueInvalid();
                default:
                    throw new AssertionError();
            }
        }

        private static <T> EvalConsumer<T, DoubleList> doNothing() {
            return (T value, DoubleList receiver) -> {
            };
        }

        private static <T> EvalConsumer<T, DoubleList> throwValueInvalid() {
            return (T value, DoubleList receiver) -> {
                throw new EvaluationException(ErrorEval.VALUE_INVALID);
            };
        }
    }
}
