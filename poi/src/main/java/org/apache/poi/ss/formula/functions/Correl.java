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

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.poi.ss.formula.ThreeDEval;
import org.apache.poi.ss.formula.TwoDEval;
import org.apache.poi.ss.formula.eval.BlankEval;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.EvaluationException;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.NumericValueEval;
import org.apache.poi.ss.formula.eval.OperandResolver;
import org.apache.poi.ss.formula.eval.RefEval;
import org.apache.poi.ss.formula.eval.StringValueEval;
import org.apache.poi.ss.formula.eval.ValueEval;

/**
 * Implementation for Excel CORREL() function.
 * <p>
 *   <b>Syntax</b>:<br> <b>CORREL </b>(<b>array1</b>, <b>array2</b>)<br>
 * </p>
 * <p>
 *   The CORREL function returns the correlation coefficient of two cell ranges.
 *   Use the correlation coefficient to determine the relationship between two properties.
 *   For example, you can examine the relationship between a location's average temperature and the use of air conditioners.
 * </p>
 * <p>
 *   See https://support.microsoft.com/en-us/office/correl-function-995dcef7-0c0a-4bed-a3fb-239d7b68ca92
 * </p>
 */
public class Correl extends Fixed2ArgFunction {

    public static final Correl instance = new Correl();

    @Override
    public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0, ValueEval arg1) {
        try {
            final PearsonsCorrelation pc = new PearsonsCorrelation();
            final double correl = pc.correlation(
                    getNumberArray(arg0), getNumberArray(arg1));
            return new NumberEval(correl);
        } catch (EvaluationException e) {
            return e.getErrorEval();
        }
    }

    private double[] getNumberArray(ValueEval operand) throws EvaluationException {
        DoubleList retval = new DoubleList();
        collectValues(operand, retval);
        return retval.toArray();
    }

    private void collectValues(ValueEval operand, DoubleList temp) throws EvaluationException {
        if (operand instanceof ThreeDEval) {
            ThreeDEval ae = (ThreeDEval) operand;
            for (int sIx = ae.getFirstSheetIndex(); sIx <= ae.getLastSheetIndex(); sIx++) {
                int width = ae.getWidth();
                int height = ae.getHeight();
                for (int rrIx = 0; rrIx < height; rrIx++) {
                    for (int rcIx = 0; rcIx < width; rcIx++) {
                        ValueEval ve = ae.getValue(sIx, rrIx, rcIx);
                        collectValue(ve, temp);
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
                    collectValue(ve, temp);
                }
            }
            return;
        }
        if (operand instanceof RefEval) {
            RefEval re = (RefEval) operand;
            for (int sIx = re.getFirstSheetIndex(); sIx <= re.getLastSheetIndex(); sIx++) {
                collectValue(re.getInnerValueEval(sIx), temp);
            }
            return;
        }
        collectValue(operand, temp);
    }

    private void collectValue(ValueEval ve, DoubleList temp) throws EvaluationException {
        if (ve == null) {
            throw new IllegalArgumentException("ve must not be null");
        }
        if (ve instanceof NumericValueEval) {
            NumericValueEval ne = (NumericValueEval) ve;
            temp.add(ne.getNumberValue());
            return;
        }
        if (ve instanceof StringValueEval) {
            String s = ((StringValueEval) ve).getStringValue().trim();
            Double d = OperandResolver.parseDouble(s);
            if (d == null) {
                throw new EvaluationException(ErrorEval.VALUE_INVALID);
            } else {
                temp.add(d.doubleValue());
            }
            return;
        }
        if (ve instanceof ErrorEval) {
            throw new EvaluationException((ErrorEval) ve);
        }
        if (ve == BlankEval.instance) {
            temp.add(0.0);
            return;
        }
        throw new RuntimeException("Invalid ValueEval type passed for conversion: ("
                + ve.getClass() + ")");
    }

}
