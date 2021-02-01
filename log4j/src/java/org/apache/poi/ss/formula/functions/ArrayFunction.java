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

import org.apache.poi.ss.formula.CacheAreaEval;
import org.apache.poi.ss.formula.FormulaParseException;
import org.apache.poi.ss.formula.eval.*;

import java.util.function.BiFunction;

/**
 * @author Robert Hulbert
 * Common Interface for any excel built-in function that has implemented array formula functionality.
 */

public interface ArrayFunction {
    
    /**
     * @param args the evaluated function arguments.  Empty values are represented with
     * {@link BlankEval} or {@link MissingArgEval}, never <code>null</code>.
     * @param srcRowIndex row index of the cell containing the formula under evaluation
     * @param srcColumnIndex column index of the cell containing the formula under evaluation
     * @return The evaluated result, possibly an {@link ErrorEval}, never <code>null</code>.
     * <b>Note</b> - Excel uses the error code <i>#NUM!</i> instead of IEEE <i>NaN</i>, so when
     * numeric functions evaluate to {@link Double#NaN} be sure to translate the result to {@link
     * ErrorEval#NUM_ERROR}.
     */

    ValueEval evaluateArray(ValueEval[] args, int srcRowIndex, int srcColumnIndex);

    /**
     * Evaluate an array function with two arguments.
     *
     * @param arg0 the first function argument. Empty values are represented with
     *        {@link BlankEval} or {@link MissingArgEval}, never <code>null</code>
     * @param arg1 the first function argument. Empty values are represented with
     *      @link BlankEval} or {@link MissingArgEval}, never <code>null</code>
     *
     * @param srcRowIndex row index of the cell containing the formula under evaluation
     * @param srcColumnIndex column index of the cell containing the formula under evaluation
     * @return The evaluated result, possibly an {@link ErrorEval}, never <code>null</code>.
     * <b>Note</b> - Excel uses the error code <i>#NUM!</i> instead of IEEE <i>NaN</i>, so when
     * numeric functions evaluate to {@link Double#NaN} be sure to translate the result to {@link
     * ErrorEval#NUM_ERROR}.
     */
    default ValueEval evaluateTwoArrayArgs(ValueEval arg0, ValueEval arg1, int srcRowIndex, int srcColumnIndex,
                                           BiFunction<ValueEval, ValueEval, ValueEval> evalFunc) {
        int w1, w2, h1, h2;
        int a1FirstCol = 0, a1FirstRow = 0;
        if (arg0 instanceof AreaEval) {
            AreaEval ae = (AreaEval)arg0;
            w1 = ae.getWidth();
            h1 = ae.getHeight();
            a1FirstCol = ae.getFirstColumn();
            a1FirstRow = ae.getFirstRow();
        } else if (arg0 instanceof RefEval){
            RefEval ref = (RefEval)arg0;
            w1 = 1;
            h1 = 1;
            a1FirstCol = ref.getColumn();
            a1FirstRow = ref.getRow();
        } else {
            w1 = 1;
            h1 = 1;
        }
        int a2FirstCol = 0, a2FirstRow = 0;
        if (arg1 instanceof AreaEval) {
            AreaEval ae = (AreaEval)arg1;
            w2 = ae.getWidth();
            h2 = ae.getHeight();
            a2FirstCol = ae.getFirstColumn();
            a2FirstRow = ae.getFirstRow();
        } else if (arg1 instanceof RefEval){
            RefEval ref = (RefEval)arg1;
            w2 = 1;
            h2 = 1;
            a2FirstCol = ref.getColumn();
            a2FirstRow = ref.getRow();
        } else {
            w2 = 1;
            h2 = 1;
        }

        int width = Math.max(w1, w2);
        int height = Math.max(h1, h2);

        ValueEval[] vals = new ValueEval[height * width];

        int idx = 0;
        for(int i = 0; i < height; i++){
            for(int j = 0; j < width; j++){
                ValueEval vA;
                try {
                    vA = OperandResolver.getSingleValue(arg0, a1FirstRow + i, a1FirstCol + j);
                } catch (FormulaParseException e) {
                    vA = ErrorEval.NAME_INVALID;
                } catch (EvaluationException e) {
                    vA = e.getErrorEval();
                } catch (RuntimeException e) {
                    if(e.getMessage().startsWith("Don't know how to evaluate name")){
                        vA = ErrorEval.NAME_INVALID;
                    } else {
                        throw e;
                    }
                }
                ValueEval vB;
                try {
                    vB = OperandResolver.getSingleValue(arg1, a2FirstRow + i, a2FirstCol + j);
                } catch (FormulaParseException e) {
                    vB = ErrorEval.NAME_INVALID;
                } catch (EvaluationException e) {
                    vB = e.getErrorEval();
                } catch (RuntimeException e) {
                    if(e.getMessage().startsWith("Don't know how to evaluate name")){
                        vB = ErrorEval.NAME_INVALID;
                    } else {
                        throw e;
                    }
                }
                if(vA instanceof ErrorEval){
                    vals[idx++] = vA;
                } else if (vB instanceof ErrorEval) {
                    vals[idx++] = vB;
                } else {
                    vals[idx++] = evalFunc.apply(vA, vB);
                }

            }
        }

        if (vals.length == 1) {
            return vals[0];
        }

        return new CacheAreaEval(srcRowIndex, srcColumnIndex, srcRowIndex + height - 1, srcColumnIndex + width - 1, vals);
    }

    default ValueEval evaluateOneArrayArg(ValueEval arg0, int srcRowIndex, int srcColumnIndex,
                                          java.util.function.Function<ValueEval, ValueEval> evalFunc){
        int w1, w2, h1, h2;
        int a1FirstCol = 0, a1FirstRow = 0;
        if (arg0 instanceof AreaEval) {
            AreaEval ae = (AreaEval)arg0;
            w1 = ae.getWidth();
            h1 = ae.getHeight();
            a1FirstCol = ae.getFirstColumn();
            a1FirstRow = ae.getFirstRow();
        } else if (arg0 instanceof RefEval){
            RefEval ref = (RefEval)arg0;
            w1 = 1;
            h1 = 1;
            a1FirstCol = ref.getColumn();
            a1FirstRow = ref.getRow();
        } else {
            w1 = 1;
            h1 = 1;
        }
        w2 = 1;
        h2 = 1;

        int width = Math.max(w1, w2);
        int height = Math.max(h1, h2);

        ValueEval[] vals = new ValueEval[height * width];

        int idx = 0;
        for(int i = 0; i < height; i++){
            for(int j = 0; j < width; j++){
                ValueEval vA;
                try {
                    vA = OperandResolver.getSingleValue(arg0, a1FirstRow + i, a1FirstCol + j);
                } catch (FormulaParseException e) {
                    vA = ErrorEval.NAME_INVALID;
                } catch (EvaluationException e) {
                    vA = e.getErrorEval();
                } catch (RuntimeException e) {
                    if(e.getMessage().startsWith("Don't know how to evaluate name")){
                        vA = ErrorEval.NAME_INVALID;
                    } else {
                        throw e;
                    }
                }
                vals[idx++] = evalFunc.apply(vA);
            }
        }

        if (vals.length == 1) {
            return vals[0];
        }

        return new CacheAreaEval(srcRowIndex, srcColumnIndex, srcRowIndex + height - 1, srcColumnIndex + width - 1, vals);

    }

}
