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

/*
 * Notes:
 * Duplicate x values don't work most of the time because of the way the
 * math library handles multiple regression.
 * The math library currently fails when the number of x variables is >=
 * the sample size (see https://github.com/Hipparchus-Math/hipparchus/issues/13).
 */

package org.apache.poi.ss.formula.functions;

import org.apache.poi.ss.formula.CacheAreaEval;
import org.apache.poi.ss.formula.eval.AreaEval;
import org.apache.poi.ss.formula.eval.BoolEval;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.EvaluationException;
import org.apache.poi.ss.formula.eval.MissingArgEval;
import org.apache.poi.ss.formula.eval.NotImplementedException;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.NumericValueEval;
import org.apache.poi.ss.formula.eval.RefEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.commons.math3.linear.SingularMatrixException;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;

import java.util.Arrays;


/**
 * Implementation for the Excel function TREND<p>
 * 
 * Syntax:<br>
 * TREND(known_y's, known_x's, new_x's, constant)
 *    <table border="0" cellpadding="1" cellspacing="0" summary="Parameter descriptions">
 *      <tr><th>known_y's, known_x's, new_x's</th><td>typically area references, possibly cell references or scalar values</td></tr>
 *      <tr><th>constant</th><td><b>TRUE</b> or <b>FALSE</b>:
 *      determines whether the regression line should include an intercept term</td></tr>
 *    </table><br>
 * If <b>known_x's</b> is not given, it is assumed to be the default array {1, 2, 3, ...}
 * of the same size as <b>known_y's</b>.<br>
 * If <b>new_x's</b> is not given, it is assumed to be the same as <b>known_x's</b><br>
 * If <b>constant</b> is omitted, it is assumed to be <b>TRUE</b>
 * </p>
 */

public final class Trend implements Function {
    MatrixFunction.MutableValueCollector collector = new MatrixFunction.MutableValueCollector(false, false);
    private static final class TrendResults {
        public double[] vals;
        public int resultWidth;
        public int resultHeight;

        public TrendResults(double[] vals, int resultWidth, int resultHeight) {
            this.vals = vals;
            this.resultWidth = resultWidth;
            this.resultHeight = resultHeight;
        }
    }

    public ValueEval evaluate(ValueEval[] args, int srcRowIndex, int srcColumnIndex) {
        if (args.length < 1 || args.length > 4) {
            return ErrorEval.VALUE_INVALID;
        }
        try {
            TrendResults tr = getNewY(args);
            ValueEval[] vals = new ValueEval[tr.vals.length];
            for (int i = 0; i < tr.vals.length; i++) {
                vals[i] = new NumberEval(tr.vals[i]);
            }
            if (tr.vals.length == 1) {
                return vals[0];
            }
            return new CacheAreaEval(srcRowIndex, srcColumnIndex, srcRowIndex + tr.resultHeight - 1, srcColumnIndex + tr.resultWidth - 1, vals);
        } catch (EvaluationException e) {
            return e.getErrorEval();
        }
    }

    private static double[][] evalToArray(ValueEval arg) throws EvaluationException {
        double[][] ar;
        ValueEval eval;
        if (arg instanceof MissingArgEval) {
            return new double[0][0];
        }
        if (arg instanceof RefEval) {
            RefEval re = (RefEval) arg;
            if (re.getNumberOfSheets() > 1) {
                throw new EvaluationException(ErrorEval.VALUE_INVALID);
            }
            eval = re.getInnerValueEval(re.getFirstSheetIndex());
        } else {
            eval = arg;
        }
        if (eval == null) {
            throw new RuntimeException("Parameter may not be null.");
        }

        if (eval instanceof AreaEval) {
            AreaEval ae = (AreaEval) eval;
            int w = ae.getWidth();
            int h = ae.getHeight();
            ar = new double[h][w];
            for (int i = 0; i < h; i++) {
                for (int j = 0; j < w; j++) {
                    ValueEval ve = ae.getRelativeValue(i, j);
                    if (!(ve instanceof NumericValueEval)) {
                        throw new EvaluationException(ErrorEval.VALUE_INVALID);
                    }
                    ar[i][j] = ((NumericValueEval)ve).getNumberValue();
                }
            }
        } else if (eval instanceof NumericValueEval) {
            ar = new double[1][1];
            ar[0][0] = ((NumericValueEval)eval).getNumberValue();
        } else {
            throw new EvaluationException(ErrorEval.VALUE_INVALID);
        }
        
        return ar;
    }

    private static double[][] getDefaultArrayOneD(int w) {
        double[][] array = new double[w][1];
        for (int i = 0; i < w; i++) {
            array[i][0] = i + 1;
        }
        return array;
    }

    private static double[] flattenArray(double[][] twoD) {
        if (twoD.length < 1) {
            return new double[0];
        }
        double[] oneD = new double[twoD.length * twoD[0].length];
        for (int i = 0; i < twoD.length; i++) {
            for (int j = 0; j < twoD[0].length; j++) {
                oneD[i * twoD[0].length + j] = twoD[i][j];
            }
        }
        return oneD;
    }

    private static double[][] flattenArrayToRow(double[][] twoD) {
        if (twoD.length < 1) {
            return new double[0][0];
        }
        double[][] oneD = new double[twoD.length * twoD[0].length][1];
        for (int i = 0; i < twoD.length; i++) {
            for (int j = 0; j < twoD[0].length; j++) {
                oneD[i * twoD[0].length + j][0] = twoD[i][j];
            }
        }
        return oneD;
    }

    private static double[][] switchRowsColumns(double[][] array) {
        double[][] newArray = new double[array[0].length][array.length];
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[0].length; j++) {
                newArray[j][i] = array[i][j];
            }
        }
        return newArray;
    }

    /**
     * Check if all columns in a matrix contain the same values.
     * Return true if the number of distinct values in each column is 1.
     *
     * @param matrix  column-oriented matrix. A Row matrix should be transposed to column .
     * @return  true if all columns contain the same value
     */
    private static boolean isAllColumnsSame(double[][] matrix){
        if(matrix.length == 0) return false;

        boolean[] cols = new boolean[matrix[0].length];
        for (int j = 0; j < matrix[0].length; j++) {
            double prev = Double.NaN;
            for (int i = 0; i < matrix.length; i++) {
                double v = matrix[i][j];
                if(i > 0 && v != prev) {
                    cols[j] = true;
                    break;
                }
                prev = v;
            }
        }
        boolean allEquals = true;
        for (boolean x : cols) {
            if(x) {
                allEquals = false;
                break;
            }
        }
        return allEquals;

    }

    private static TrendResults getNewY(ValueEval[] args) throws EvaluationException {
        double[][] xOrig;
        double[][] x;
        double[][] yOrig;
        double[] y;
        double[][] newXOrig;
        double[][] newX;
        double[][] resultSize;
        boolean passThroughOrigin = false;
        switch (args.length) {
        case 1:
            yOrig = evalToArray(args[0]);
            xOrig = new double[0][0];
            newXOrig = new double[0][0];
            break;
        case 2:
            yOrig = evalToArray(args[0]);
            xOrig = evalToArray(args[1]);
            newXOrig = new double[0][0];
            break;
        case 3:
            yOrig = evalToArray(args[0]);
            xOrig = evalToArray(args[1]);
            newXOrig = evalToArray(args[2]);
            break;
        case 4:
            yOrig = evalToArray(args[0]);
            xOrig = evalToArray(args[1]);
            newXOrig = evalToArray(args[2]);
            if (!(args[3] instanceof BoolEval)) {
                throw new EvaluationException(ErrorEval.VALUE_INVALID);
            }
            // The argument in Excel is false when it *should* pass through the origin.
            passThroughOrigin = !((BoolEval)args[3]).getBooleanValue();
            break;
        default:
            throw new EvaluationException(ErrorEval.VALUE_INVALID);
        }

        if (yOrig.length < 1) {
            throw new EvaluationException(ErrorEval.VALUE_INVALID);
        }
        y = flattenArray(yOrig);
        newX = newXOrig;

        if (newXOrig.length > 0) {
            resultSize = newXOrig;
        } else {
            resultSize = new double[1][1];
        }

        if (y.length == 1) {
            /* See comment at top of file
            if (xOrig.length > 0 && !(xOrig.length == 1 || xOrig[0].length == 1)) {
                throw new EvaluationException(ErrorEval.REF_INVALID);
            } else if (xOrig.length < 1) {
                x = new double[1][1];
                x[0][0] = 1;
            } else {
                x = new double[1][];
                x[0] = flattenArray(xOrig);
                if (newXOrig.length < 1) {
                    resultSize = xOrig;
                }
            }*/
            throw new NotImplementedException("Sample size too small");
        } else if (yOrig.length == 1 || yOrig[0].length == 1) {
            if (xOrig.length < 1) {
                x = getDefaultArrayOneD(y.length);
                if (newXOrig.length < 1) {
                    resultSize = yOrig;
                }
            } else {
                x = xOrig;
                if (xOrig[0].length > 1 && yOrig.length == 1) {
                    x = switchRowsColumns(x);
                }
                if (newXOrig.length < 1) {
                    resultSize = xOrig;
                }
            }
            if (newXOrig.length > 0 && (x.length == 1 || x[0].length == 1)) {
                newX = flattenArrayToRow(newXOrig);
            }
        } else {
            if (xOrig.length < 1) {
                x = getDefaultArrayOneD(y.length);
                if (newXOrig.length < 1) {
                    resultSize = yOrig;
                }
            } else {
                x = flattenArrayToRow(xOrig);
                if (newXOrig.length < 1) {
                    resultSize = xOrig;
                }
            }
            if (newXOrig.length > 0) {
                newX = flattenArrayToRow(newXOrig);
            }
            if (y.length != x.length || yOrig.length != xOrig.length) {
                throw new EvaluationException(ErrorEval.REF_INVALID);
            }
        }

        if (newXOrig.length < 1) {
            newX = x;
        } else if (newXOrig.length == 1 && newXOrig[0].length > 1 && xOrig.length > 1 && xOrig[0].length == 1) {
            newX = switchRowsColumns(newXOrig);
        }
        
        if (newX[0].length != x[0].length) {
            throw new EvaluationException(ErrorEval.REF_INVALID);
        }
        
        if (x[0].length >= x.length) {
            /* See comment at top of file */
            throw new NotImplementedException("Sample size too small");
        }

        int resultHeight = resultSize.length;
        int resultWidth = resultSize[0].length;

        if(isAllColumnsSame(x)){
            double[] result = new double[newX.length];
            double avg = Arrays.stream(y).average().orElse(0);
            for(int i = 0; i < result.length; i++) result[i] = avg;
            return new TrendResults(result, resultWidth, resultHeight);
        }

        OLSMultipleLinearRegression reg = new OLSMultipleLinearRegression();
        if (passThroughOrigin) {
            reg.setNoIntercept(true);
        }

        try {
            reg.newSampleData(y, x);
        } catch (IllegalArgumentException e) {
            throw new EvaluationException(ErrorEval.REF_INVALID);
        }
        double[] par;
        try {
            par = reg.estimateRegressionParameters();
        } catch (SingularMatrixException e) {
            throw new NotImplementedException("Singular matrix in input");
        }

        double[] result = new double[newX.length];
        for (int i = 0; i < newX.length; i++) {
            result[i] = 0;
            if (passThroughOrigin) {
                for (int j = 0; j < par.length; j++) {
                    result[i] += par[j] * newX[i][j];
                }
            } else {
                result[i] = par[0];
                for (int j = 1; j < par.length; j++) {
                    result[i] += par[j] * newX[i][j - 1];
                }
            }
        }
        return new TrendResults(result, resultWidth, resultHeight);
    }
}
