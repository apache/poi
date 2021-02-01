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
import org.apache.poi.ss.formula.eval.AreaEval;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.EvaluationException;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.OperandResolver;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;

/**
 * @author Robert Hulbert
 */
public abstract class MatrixFunction implements Function{
    
    public static void checkValues(double[] results) throws EvaluationException {
        for (double result : results) {
            if (Double.isNaN(result) || Double.isInfinite(result)) {
                throw new EvaluationException(ErrorEval.NUM_ERROR);
            }
        }
    }
    
    protected final double singleOperandEvaluate(ValueEval arg, int srcCellRow, int srcCellCol) throws EvaluationException {
        ValueEval ve = OperandResolver.getSingleValue(arg, srcCellRow, srcCellCol);
        return OperandResolver.coerceValueToDouble(ve);
    }
    
    /* converts 1D array to 2D array for calculations */
    private static double[][] fillDoubleArray(double[] vector, int rows, int cols) throws EvaluationException {
        int i = 0, j = 0;
        
        if (rows < 1 || cols < 1 || vector.length < 1) {
            throw new EvaluationException(ErrorEval.VALUE_INVALID);
        }
        
        double[][] matrix = new double[rows][cols];

        for (double aVector : vector) {
            if (j < matrix.length) {
                if (i == matrix[0].length) {
                    i = 0;
                    j++;
                }
                if (j < matrix.length) matrix[j][i++] = aVector;
            }
        }
        
        return matrix;
    }
    
    /* retrieves 1D array from 2D array after calculations */
    private static double[] extractDoubleArray(double[][] matrix) throws EvaluationException {
        int idx = 0;
        
        if (matrix == null || matrix.length < 1 || matrix[0].length < 1) {
            throw new EvaluationException(ErrorEval.VALUE_INVALID);
        }
        
        double[] vector = new double[matrix.length * matrix[0].length];

        for (double[] aMatrix : matrix) {
            for (int i = 0; i < matrix[0].length; i++) {
                vector[idx++] = aMatrix[i];
            }
        }
        return vector;
    }
    
    public static abstract class OneArrayArg extends Fixed1ArgFunction {
        protected OneArrayArg() {
            //no fields to initialize
        }
        
        @Override
        public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0) {
            if (arg0 instanceof AreaEval) {
                double[] result;
                double[][] resultArray;
                int width, height;
                
                try {
                    double[] values = collectValues(arg0);
                    double[][] array = fillDoubleArray(values, ((AreaEval) arg0).getHeight(), ((AreaEval) arg0).getWidth());
                    resultArray = evaluate(array);
                    width = resultArray[0].length;
                    height = resultArray.length;
                    result = extractDoubleArray(resultArray);
                    
                    checkValues(result);
                }
                catch(EvaluationException e){
                    return e.getErrorEval();
                }

                ValueEval[] vals = new ValueEval[result.length];
                
                for (int idx = 0; idx < result.length; idx++) {
                    vals[idx] = new NumberEval(result[idx]);
                }
                                
                if (result.length == 1) {
                    return vals[0];
                }
                else {
                    /* find a better solution */
                    return new CacheAreaEval(((AreaEval) arg0).getFirstRow(), ((AreaEval) arg0).getFirstColumn(), 
                                            ((AreaEval) arg0).getFirstRow() + height - 1, 
                                            ((AreaEval) arg0).getFirstColumn() + width - 1, vals);
                }
            }
            else {
                double[][] result;
                try {
                    double value = NumericFunction.singleOperandEvaluate(arg0, srcRowIndex, srcColumnIndex);
                    double[][] temp = {{value}};
                    result = evaluate(temp);
                    NumericFunction.checkValue(result[0][0]);
                }
                catch (EvaluationException e) {
                    return e.getErrorEval();
                }
                
                return new NumberEval(result[0][0]);
            }
        }
        
        protected abstract double[][] evaluate(double[][] d1) throws EvaluationException;
        protected abstract double[] collectValues(ValueEval arg) throws EvaluationException;
    }
    
    public static abstract class TwoArrayArg extends Fixed2ArgFunction {
        protected TwoArrayArg() {
            //no fields to initialize
        }
        
        @Override
        public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0, ValueEval arg1) {
            double[] result;
            int width, height;

            try {
                double[][] array0;
                double[][] array1;
                double[][] resultArray;

                if (arg0 instanceof AreaEval) {
                    try {
                        double[] values = collectValues(arg0);
                        array0 = fillDoubleArray(values, ((AreaEval) arg0).getHeight(), ((AreaEval) arg0).getWidth());
                    }
                    catch(EvaluationException e) {
                        return e.getErrorEval();
                    }
                }
                else {
                    try {
                        double value = NumericFunction.singleOperandEvaluate(arg0, srcRowIndex, srcColumnIndex);
                        array0 = new double[][] {{value}};
                    }
                    catch (EvaluationException e) {
                        return e.getErrorEval();
                    }
                }
                    
                if (arg1 instanceof AreaEval) {
                   try {
                       double[] values = collectValues(arg1);
                      array1 = fillDoubleArray(values, ((AreaEval) arg1).getHeight(),((AreaEval) arg1).getWidth());
                   }
                   catch (EvaluationException e) {
                      return e.getErrorEval();
                   }
                }
                else {
                    try {
                        double value = NumericFunction.singleOperandEvaluate(arg1, srcRowIndex, srcColumnIndex);
                        array1 = new double[][] {{value}};
                    }
                    catch (EvaluationException e) {
                        return e.getErrorEval();
                    }
                }
             
                resultArray = evaluate(array0, array1);
                width = resultArray[0].length;
                height = resultArray.length;
                result = extractDoubleArray(resultArray);
                checkValues(result);
            }
            catch (EvaluationException e) {
                return e.getErrorEval();
            }
            catch (IllegalArgumentException e) {
                return ErrorEval.VALUE_INVALID;
            }


            ValueEval[] vals = new ValueEval[result.length];
            
            for (int idx = 0; idx < result.length; idx++) {
                vals[idx] = new NumberEval(result[idx]);
            }
            
            if (result.length == 1)
                return vals[0];
            else {
                return new CacheAreaEval(((AreaEval) arg0).getFirstRow(), ((AreaEval) arg0).getFirstColumn(), 
                        ((AreaEval) arg0).getFirstRow() + height - 1, 
                        ((AreaEval) arg0).getFirstColumn() + width - 1, vals);
            }
      
        }
        
        protected abstract double[][] evaluate(double[][] d1, double[][] d2) throws EvaluationException;
        protected abstract double[] collectValues(ValueEval arg) throws EvaluationException;

    }
    
    public static final class MutableValueCollector extends MultiOperandNumericFunction {
        public MutableValueCollector(boolean isReferenceBoolCounted, boolean isBlankCounted) {
            super(isReferenceBoolCounted, isBlankCounted);
        }
        public double[] collectValues(ValueEval...operands) throws EvaluationException {
            return getNumberArray(operands);
        }
        protected double evaluate(double[] values) {
            throw new IllegalStateException("should not be called");
        }
    }
    
    public static final Function MINVERSE = new OneArrayArg() {
        private final MutableValueCollector instance = new MutableValueCollector(false, false);
        
        protected double[] collectValues(ValueEval arg) throws EvaluationException {
            double[] values = instance.collectValues(arg);
            
            /* handle case where MDETERM is operating on an array that that is not completely filled*/
            if (arg instanceof AreaEval && values.length == 1)
                throw new EvaluationException(ErrorEval.VALUE_INVALID);
            
            return values;
        }
        
        protected double[][] evaluate(double[][] d1) throws EvaluationException {
            if (d1.length != d1[0].length) {
                throw new EvaluationException(ErrorEval.VALUE_INVALID);
            }
            
            Array2DRowRealMatrix temp = new Array2DRowRealMatrix(d1);
            return MatrixUtils.inverse(temp).getData();
        }
    };
    
    public static final Function TRANSPOSE = new OneArrayArg() {
        private final MutableValueCollector instance = new MutableValueCollector(false, true);
        
        protected double[] collectValues(ValueEval arg) throws EvaluationException {
            return instance.collectValues(arg);
        }
        
        protected double[][] evaluate(double[][] d1) throws EvaluationException {
            
            Array2DRowRealMatrix temp = new Array2DRowRealMatrix(d1);
            return temp.transpose().getData();
        }
    };
    
    public static final Function MDETERM = new Mdeterm();

    private static class Mdeterm extends OneArrayArg {
        private final MutableValueCollector instance;

        public Mdeterm() {
            instance = new MutableValueCollector(false, false);
            instance.setBlankEvalPolicy(MultiOperandNumericFunction.Policy.ERROR);
        }

        protected double[] collectValues(ValueEval arg) throws EvaluationException {
            double[] values = instance.collectValues(arg);
            
            /* handle case where MDETERM is operating on an array that that is not completely filled*/
            if (arg instanceof AreaEval && values.length == 1)
                throw new EvaluationException(ErrorEval.VALUE_INVALID);

            return instance.collectValues(arg);
        }
        
        protected double[][] evaluate(double[][] d1) throws EvaluationException {
            if (d1.length != d1[0].length) {
                throw new EvaluationException(ErrorEval.VALUE_INVALID);
            }

            double[][] result = new double[1][1];
            Array2DRowRealMatrix temp = new Array2DRowRealMatrix(d1);
            result[0][0] = (new LUDecomposition(temp)).getDeterminant();
            return result;
        }
    }
    
    public static final Function MMULT = new TwoArrayArg() {
        private final MutableValueCollector instance = new MutableValueCollector(false, false);
        
        protected double[] collectValues(ValueEval arg) throws EvaluationException {
            double[] values = instance.collectValues(arg);
            
            /* handle case where MMULT is operating on an array that is not completely filled*/
            if (arg instanceof AreaEval && values.length == 1)
                throw new EvaluationException(ErrorEval.VALUE_INVALID);
            
            return values;
        }
        
        protected double[][] evaluate(double[][] d1, double[][] d2) throws EvaluationException{
            Array2DRowRealMatrix first = new Array2DRowRealMatrix(d1);
            Array2DRowRealMatrix second = new Array2DRowRealMatrix(d2);
            
            try {
                MatrixUtils.checkMultiplicationCompatible(first, second);
            }
            catch (DimensionMismatchException e) {
                throw new EvaluationException(ErrorEval.VALUE_INVALID);
            }
                
            return first.multiply(second).getData();    
        }
    };
}
