/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.apache.poi.hssf.record.formula.functions;

import org.apache.poi.hssf.record.formula.eval.AreaEval;
import org.apache.poi.hssf.record.formula.eval.BlankEval;
import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.Eval;
import org.apache.poi.hssf.record.formula.eval.NumericValueEval;
import org.apache.poi.hssf.record.formula.eval.Ref2DEval;
import org.apache.poi.hssf.record.formula.eval.RefEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;
import org.apache.poi.hssf.record.formula.eval.ValueEvalToNumericXlator;

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 * This is the super class for all excel function evaluator
 * classes that take variable number of operands, and
 * where the order of operands does not matter
 */
public abstract class MultiOperandNumericFunction implements Function {
    static final double[] EMPTY_DOUBLE_ARRAY = { };
    
    private static class DoubleList {
        private double[] _array;
        private int _count;

        public DoubleList() {
            _array = new double[8];
            _count = 0;
        }
        
        public double[] toArray() {
            if(_count < 1) {
                return EMPTY_DOUBLE_ARRAY;
            }
            double[] result = new double[_count];
            System.arraycopy(_array, 0, result, 0, _count);
            return result;
        }

        public void add(double[] values) {
            int addLen = values.length;
            ensureCapacity(_count + addLen);
            System.arraycopy(values, 0, _array, _count, addLen);
            _count += addLen;
        }

        private void ensureCapacity(int reqSize) {
            if(reqSize > _array.length) {
                int newSize = reqSize * 3 / 2; // grow with 50% extra
                double[] newArr = new double[newSize];
                System.arraycopy(_array, 0, newArr, 0, _count);
                _array = newArr;
            }
        }

        public void add(double value) {
            ensureCapacity(_count + 1);
            _array[_count] = value;
            _count++;
        }
    }
    
    private static final int DEFAULT_MAX_NUM_OPERANDS = 30;

    protected abstract ValueEvalToNumericXlator getXlator();
    
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
     * @param operands
     * @param srcRow
     * @param srcCol
     */
    protected double[] getNumberArray(Eval[] operands, int srcRow, short srcCol) {
        if (operands.length > getMaxNumOperands()) {
            return null;
        }
        DoubleList retval = new DoubleList();
        
        for (int i=0, iSize=operands.length; i<iSize; i++) {
            double[] temp = getNumberArray(operands[i], srcRow, srcCol);
            if (temp == null) {
                return null; // error occurred.
            }
            retval.add(temp);
        }
        return retval.toArray();
    }
    
    /**
     * Same as getNumberArray(Eval[], int, short) except that this
     * takes Eval instead of Eval[].
     * @param operand
     * @param srcRow
     * @param srcCol
     */
    protected double[] getNumberArray(Eval operand, int srcRow, short srcCol) {
        
        if (operand instanceof AreaEval) {
            AreaEval ae = (AreaEval) operand;
            DoubleList retval = new DoubleList();
            int width = ae.getWidth();
    		int height = ae.getHeight();
    		for (int rrIx=0; rrIx<height; rrIx++) {
    			for (int rcIx=0; rcIx<width; rcIx++) {
    				ValueEval ve1 = ae.getRelativeValue(rrIx, rcIx);
                     /*
                     * TODO: For an AreaEval, we are constructing a RefEval
                     * per element.
                     * For now this is a tempfix solution since this may
                     * require a more generic fix at the level of
                     * HSSFFormulaEvaluator where we store an array
                     * of RefEvals as the "values" array. 
                     */
                    RefEval re = new Ref2DEval(null, ve1);
                    ValueEval ve = singleOperandEvaluate(re, srcRow, srcCol);
                    
                    if (ve instanceof NumericValueEval) {
                        NumericValueEval nve = (NumericValueEval) ve;
                        retval.add(nve.getNumberValue());
                    }
                    else if (ve instanceof BlankEval) {
                        // note - blanks are ignored, so returned array will be smaller.
                    } 
                    else {
                        return null; // indicate to calling subclass that error occurred
                    }
    			}
            }
            return retval.toArray();
        }
        
        // for ValueEvals other than AreaEval
        ValueEval ve = singleOperandEvaluate(operand, srcRow, srcCol);
        
        if (ve instanceof NumericValueEval) {
            NumericValueEval nve = (NumericValueEval) ve;
            return new double[] { nve.getNumberValue(), };
        }
        
        if (ve instanceof BlankEval) {
            // ignore blanks
            return EMPTY_DOUBLE_ARRAY;
        } 
        return null;
    }
    
    /**
     * Ensures that a two dimensional array has all sub-arrays present and the same length
     * @return <code>false</code> if any sub-array is missing, or is of different length
     */
    protected static final boolean areSubArraysConsistent(double[][] values) {
        
        if (values == null || values.length < 1) {
            // TODO this doesn't seem right.  Fix or add comment.
            return true;
        }
        
        if (values[0] == null) {
            return false;
        }
        int outerMax = values.length;
        int innerMax = values[0].length;
        for (int i=1; i<outerMax; i++) { // note - 'i=1' start at second sub-array
            double[] subArr = values[i];
            if (subArr == null) {
                return false;
            }
            if (innerMax != subArr.length) {
                return false;
            }
        }
        return true;
    }
    
   
    protected final ValueEval singleOperandEvaluate(Eval eval, int srcRow, short srcCol) {

        if (eval instanceof AreaEval) {
            AreaEval ae = (AreaEval) eval;
            if (ae.contains(srcRow, srcCol)) { // circular ref!
                return ErrorEval.CIRCULAR_REF_ERROR;
            }
            if (ae.isRow()) {
                if (ae.isColumn()) {
                	return ae.getRelativeValue(0, 0);
                }
                if (ae.containsColumn(srcCol)) {
                    ValueEval ve = ae.getValueAt(ae.getFirstRow(), srcCol);
                    ve = getXlator().attemptXlateToNumeric(ve);
                    return getXlator().attemptXlateToNumeric(ve);
                }
                return ErrorEval.VALUE_INVALID;
            }
            if (ae.isColumn()) {
                if (ae.containsRow(srcRow)) {
                    ValueEval ve = ae.getValueAt(srcRow, ae.getFirstColumn());
                    return getXlator().attemptXlateToNumeric(ve);
                }
                return ErrorEval.VALUE_INVALID;
            }
            return ErrorEval.VALUE_INVALID;
        }
        return getXlator().attemptXlateToNumeric((ValueEval) eval);
    }
    
}
