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
import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.Eval;
import org.apache.poi.hssf.record.formula.eval.EvaluationException;
import org.apache.poi.hssf.record.formula.eval.NumberEval;
import org.apache.poi.hssf.record.formula.eval.RefEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 *
 */
public abstract class XYNumericFunction implements Function {
    protected static final int X = 0;
    protected static final int Y = 1;
    
    protected static final class DoubleArrayPair {

		private final double[] _xArray;
		private final double[] _yArray;

		public DoubleArrayPair(double[] xArray, double[] yArray) {
			_xArray = xArray;
			_yArray = yArray;
		}
		public double[] getXArray() {
			return _xArray;
		}
		public double[] getYArray() {
			return _yArray;
		}
    }


    public final Eval evaluate(Eval[] args, int srcCellRow, short srcCellCol) {
    	if(args.length != 2) {
    		return ErrorEval.VALUE_INVALID;
    	}
    	
        double[][] values;
		try {
			values = getValues(args[0], args[1]);
		} catch (EvaluationException e) {
			return e.getErrorEval();
		}
        if (values==null 
                || values[X] == null || values[Y] == null
                || values[X].length == 0 || values[Y].length == 0
                || values[X].length != values[Y].length) {
            return ErrorEval.VALUE_INVALID;
        }
        
        double d = evaluate(values[X], values[Y]);
        if (Double.isNaN(d) || Double.isInfinite(d)) {
			return ErrorEval.NUM_ERROR;
		}
		return new NumberEval(d);
    }    
    protected abstract double evaluate(double[] xArray, double[] yArray);

    /**
     * Returns a double array that contains values for the numeric cells
     * from among the list of operands. Blanks and Blank equivalent cells
     * are ignored. Error operands or cells containing operands of type
     * that are considered invalid and would result in #VALUE! error in 
     * excel cause this function to return null.
     */
    private static double[][] getNumberArray(Eval[] xops, Eval[] yops) throws EvaluationException {
    	
    	// check for errors first: size mismatch, value errors in x, value errors in y
    	
    	int nArrayItems = xops.length;
		if(nArrayItems != yops.length) {
    		throw new EvaluationException(ErrorEval.NA);
    	}
		for (int i = 0; i < xops.length; i++) {
			Eval eval = xops[i];
			if (eval instanceof ErrorEval) {
				throw new EvaluationException((ErrorEval) eval);
			}
		}
		for (int i = 0; i < yops.length; i++) {
			Eval eval = yops[i];
			if (eval instanceof ErrorEval) {
				throw new EvaluationException((ErrorEval) eval);
			}
		}
		
        double[] xResult = new double[nArrayItems];
        double[] yResult = new double[nArrayItems];
    	
        int count = 0;
        
		for (int i=0, iSize=nArrayItems; i<iSize; i++) {
		    Eval xEval = xops[i];
		    Eval yEval = yops[i];
		    
		    if (isNumberEval(xEval) && isNumberEval(yEval)) {
		    	xResult[count] = getDoubleValue(xEval);
		    	yResult[count] = getDoubleValue(yEval);
		        if (Double.isNaN(xResult[count]) || Double.isNaN(xResult[count])) {
		            throw new EvaluationException(ErrorEval.NUM_ERROR);
		        }
		        count++;
		    }
		}
        
		return new double[][] {
        	trimToSize(xResult, count),
            trimToSize(yResult, count),
		};
    }
    
    private static double[][] getValues(Eval argX, Eval argY) throws EvaluationException {
    	
    	if (argX instanceof ErrorEval) {
			throw new EvaluationException((ErrorEval) argX);
		}
    	if (argY instanceof ErrorEval) {
			throw new EvaluationException((ErrorEval) argY);
		}
    	
        Eval[] xEvals;
		Eval[] yEvals;
		if (argX instanceof AreaEval) {
		    AreaEval ae = (AreaEval) argX;
		    xEvals = ae.getValues();
		} else {
		    xEvals = new Eval[] { argX, };
		}
		
		if (argY instanceof AreaEval) {
		    AreaEval ae = (AreaEval) argY;
		    yEvals = ae.getValues();
		} else {
		    yEvals = new Eval[] { argY, };
		}
		
		return getNumberArray(xEvals, yEvals);
    }
    
    private static double[] trimToSize(double[] arr, int len) {
        double[] tarr = arr;
        if (arr.length > len) {
            tarr = new double[len];
            System.arraycopy(arr, 0, tarr, 0, len);
        }
        return tarr;
    }
    
    private static boolean isNumberEval(Eval eval) {
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
    
    private static double getDoubleValue(Eval eval) {
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
