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
/*
 * Created on May 22, 2005
 *
 */
package org.apache.poi.hssf.record.formula.functions;

import org.apache.poi.hssf.record.formula.eval.AreaEval;
import org.apache.poi.hssf.record.formula.eval.BlankEval;
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
public abstract class MultiOperandNumericFunction extends NumericFunction {

    private static final ValueEvalToNumericXlator DEFAULT_NUM_XLATOR =
        new ValueEvalToNumericXlator((short) (
                  ValueEvalToNumericXlator.BOOL_IS_PARSED  
                | ValueEvalToNumericXlator.REF_BOOL_IS_PARSED  
                | ValueEvalToNumericXlator.EVALUATED_REF_BOOL_IS_PARSED  
              //| ValueEvalToNumericXlator.STRING_IS_PARSED  
                | ValueEvalToNumericXlator.REF_STRING_IS_PARSED  
                | ValueEvalToNumericXlator.EVALUATED_REF_STRING_IS_PARSED  
              //| ValueEvalToNumericXlator.STRING_TO_BOOL_IS_PARSED  
              //| ValueEvalToNumericXlator.REF_STRING_TO_BOOL_IS_PARSED  
              //| ValueEvalToNumericXlator.STRING_IS_INVALID_VALUE  
              //| ValueEvalToNumericXlator.REF_STRING_IS_INVALID_VALUE  
                ));
    
    private static final int DEFAULT_MAX_NUM_OPERANDS = 30;

    /**
     * this is the default impl for the factory method getXlator
     * of the super class NumericFunction. Subclasses can override this method
     * if they desire to return a different ValueEvalToNumericXlator instance
     * than the default.
     */
    protected ValueEvalToNumericXlator getXlator() {
        return DEFAULT_NUM_XLATOR;
    }
    
    /**
     * Maximum number of operands accepted by this function.
     * Subclasses may override to change default value.
     * @return
     */
    protected int getMaxNumOperands() {
        return DEFAULT_MAX_NUM_OPERANDS;
    }
    
    /**
     * Returns a double array that contains values for the numeric cells
     * from among the list of operands. Blanks and Blank equivalent cells
     * are ignored. Error operands or cells containing operands of type
     * that are considered invalid and would result in #VALUE! error in 
     * excel cause this function to return null.
     * 
     * @param operands
     * @param srcRow
     * @param srcCol
     * @return
     */
    protected double[] getNumberArray(Eval[] operands, int srcRow, short srcCol) {
        double[] retval = new double[30];
        int count = 0;
        
        outer: do { // goto simulator loop
            if (operands.length > getMaxNumOperands()) {
                break outer;
            }
            else {
                for (int i=0, iSize=operands.length; i<iSize; i++) {
                    double[] temp = getNumberArray(operands[i], srcRow, srcCol);
                    if (temp == null) {
                        retval = null; // error occurred.
                        break;
                    }
                    retval = putInArray(retval, count, temp);
                    count += temp.length;
                }
            }
        } while (false); // end goto simulator loop
        
        if (retval != null) {
            double[] temp = retval;
            retval = new double[count];
            System.arraycopy(temp, 0, retval, 0, count);
        }
        
        return retval;
    }
    
    /**
     * Same as getNumberArray(Eval[], int, short) except that this
     * takes Eval instead of Eval[].
     * @param operand
     * @param srcRow
     * @param srcCol
     * @return
     */
    protected double[] getNumberArray(Eval operand, int srcRow, short srcCol) {
        double[] retval;
        int count = 0;
        
        if (operand instanceof AreaEval) {
            AreaEval ae = (AreaEval) operand;
            ValueEval[] values = ae.getValues();
            retval = new double[values.length];
            for (int j=0, jSize=values.length; j<jSize; j++) {
                /*
                 * TODO: For an AreaEval, we are constructing a RefEval
                 * per element.
                 * For now this is a tempfix solution since this may
                 * require a more generic fix at the level of
                 * HSSFFormulaEvaluator where we store an array
                 * of RefEvals as the "values" array. 
                 */
                RefEval re = (values[j] instanceof RefEval)
                        ? new Ref2DEval(null, ((RefEval) values[j]).getInnerValueEval(), true)
                        : new Ref2DEval(null, values[j], false);
                ValueEval ve = singleOperandEvaluate(re, srcRow, srcCol);
                
                if (ve instanceof NumericValueEval) {
                    NumericValueEval nve = (NumericValueEval) ve;
                    retval = putInArray(retval, count++, nve.getNumberValue());
                }
                else if (ve instanceof BlankEval) {} // ignore operand
                else {
                    retval = null; // null => indicate to calling subclass that error occurred
                    break;
                }
            }
        }
        else { // for ValueEvals other than AreaEval
            retval = new double[1];
            ValueEval ve = singleOperandEvaluate(operand, srcRow, srcCol);
            
            if (ve instanceof NumericValueEval) {
                NumericValueEval nve = (NumericValueEval) ve;
                retval = putInArray(retval, count++, nve.getNumberValue());
            }
            else if (ve instanceof BlankEval) {} // ignore operand
            else {
                retval = null; // null => indicate to calling subclass that error occurred
            }
        }
        
        if (retval != null && retval.length >= 1) {
            double[] temp = retval;
            retval = new double[count];
            System.arraycopy(temp, 0, retval, 0, count);
        }
        
        return retval;
    }
    
    /**
     * puts d at position pos in array arr. If pos is greater than arr, the 
     * array is dynamically resized (using a simple doubling rule).
     * @param arr
     * @param pos
     * @param d
     * @return
     */
    private static double[] putInArray(double[] arr, int pos, double d) {
        double[] tarr = arr;
        while (pos >= arr.length) {
            arr = new double[arr.length << 1];
        }
        if (tarr.length != arr.length) {
            System.arraycopy(tarr, 0, arr, 0, tarr.length);
        }
        arr[pos] = d;
        return arr;
    }
    
    private static double[] putInArray(double[] arr, int pos, double[] d) {
        double[] tarr = arr;
        while (pos+d.length >= arr.length) {
            arr = new double[arr.length << 1];
        }
        if (tarr.length != arr.length) {
            System.arraycopy(tarr, 0, arr, 0, tarr.length);
        }
        for (int i=0, iSize=d.length; i<iSize; i++) {
            arr[pos+i] = d[i];
        }
        return arr;
    }
    
    protected static boolean areSubArraysConsistent(double[][] values) {
        boolean retval = false;
        
        outer: do {
            if (values != null && values.length > 0) {
                if (values[0] == null)
                    break outer;
                int len = values[0].length;
                for (int i=1, iSize=values.length; i<iSize; i++) {
                    if (values[i] == null)
                        break outer;
                    int tlen = values[i].length;
                    if (len != tlen) {
                        break outer;
                    }
                }
            }
            retval = true;
        } while (false);
        return retval;
    }
    
}
