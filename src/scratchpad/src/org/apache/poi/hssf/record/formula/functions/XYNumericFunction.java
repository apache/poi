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
 * Created on May 29, 2005
 *
 */
package org.apache.poi.hssf.record.formula.functions;

import org.apache.poi.hssf.record.formula.eval.AreaEval;
import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.Eval;
import org.apache.poi.hssf.record.formula.eval.NumberEval;
import org.apache.poi.hssf.record.formula.eval.RefEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;
import org.apache.poi.hssf.record.formula.eval.ValueEvalToNumericXlator;

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 *
 */
public abstract class XYNumericFunction extends NumericFunction {
    protected static final int X = 0;
    protected static final int Y = 1;

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

    /**
     * this is the default impl for the factory method getXlator
     * of the super class NumericFunction. Subclasses can override this method
     * if they desire to return a different ValueEvalToNumericXlator instance
     * than the default.
     */
    protected ValueEvalToNumericXlator getXlator() {
        return DEFAULT_NUM_XLATOR;
    }
    
    protected int getMaxNumOperands() {
        return 30;
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
    protected double[][] getNumberArray(Eval[] xops, Eval[] yops, int srcRow, short srcCol) {
        double[][] retval = new double[2][30];
        int count = 0;
        
        if (xops.length > getMaxNumOperands() 
                || yops.length > getMaxNumOperands()
                || xops.length != yops.length) {
            retval = null;
        }
        else {
            
            for (int i=0, iSize=xops.length; i<iSize; i++) {
                Eval xEval = xops[i];
                Eval yEval = yops[i];
                
                if (isNumberEval(xEval) && isNumberEval(yEval)) {
                    retval[X] = ensureCapacity(retval[X], count);
                    retval[Y] = ensureCapacity(retval[Y], count);
                    retval[X][count] = getDoubleValue(xEval);
                    retval[Y][count] = getDoubleValue(yEval);
                    if (Double.isNaN(retval[X][count]) || Double.isNaN(retval[Y][count])) {
                        retval = null;
                        break;
                    }
                    count++;
                }
            }
        }
        
        if (retval != null) {
            double[][] temp = retval;
            retval[X] = trimToSize(retval[X], count);
            retval[Y] = trimToSize(retval[Y], count);
        }
        
        return retval;
    }
    
    protected double[][] getValues(Eval[] operands, int srcCellRow, short srcCellCol) {
        double[][] retval = null;
        
        outer: do {
            if (operands.length == 2) {
                Eval[] xEvals = new Eval[1];
                Eval[] yEvals = new Eval[1];
                if (operands[X] instanceof AreaEval) {
                    AreaEval ae = (AreaEval) operands[0];
                    xEvals = ae.getValues();
                }
                else if (operands[X] instanceof ErrorEval) {
                    break outer;
                }
                else {
                    xEvals[0] = operands[X];
                }
                
                if (operands[Y] instanceof AreaEval) {
                    AreaEval ae = (AreaEval) operands[Y];
                    yEvals = ae.getValues();
                }
                else if (operands[Y] instanceof ErrorEval) {
                    break outer;
                }
                else {
                    yEvals[0] = operands[Y];
                }
                
                retval = getNumberArray(xEvals, yEvals, srcCellRow, srcCellCol);
            }
        } while (false);
        
        return retval;
    }
    

    protected static double[] ensureCapacity(double[] arr, int pos) {
        double[] temp = arr;
        while (pos >= arr.length) {
            arr = new double[arr.length << 2];
        }
        if (temp.length != arr.length)
            System.arraycopy(temp, 0, arr, 0, temp.length);
        return arr;
    }
    
    protected static double[] trimToSize(double[] arr, int len) {
        double[] tarr = arr;
        if (arr.length > len) {
            tarr = new double[len];
            System.arraycopy(arr, 0, tarr, 0, len);
        }
        return tarr;
    }
    
    protected static boolean isNumberEval(Eval eval) {
        boolean retval = false;
        
        if (eval instanceof NumberEval) {
            retval = true;
        }
        else if (eval instanceof RefEval) {
            RefEval re = (RefEval) eval;
            ValueEval ve = re.getInnerValueEval();
            retval = (ve instanceof NumberEval);
        }
        
        return retval;
    }
    
    protected static double getDoubleValue(Eval eval) {
        double retval = 0;
        if (eval instanceof NumberEval) {
            NumberEval ne = (NumberEval) eval;
            retval = ne.getNumberValue();
        }
        else if (eval instanceof RefEval) {
            RefEval re = (RefEval) eval;
            ValueEval ve = re.getInnerValueEval();
                retval = (ve instanceof NumberEval)
                    ? ((NumberEval) ve).getNumberValue()
                    : Double.NaN;
        }
        else if (eval instanceof ErrorEval) {
            retval = Double.NaN;
        }
        return retval;
    }
}
