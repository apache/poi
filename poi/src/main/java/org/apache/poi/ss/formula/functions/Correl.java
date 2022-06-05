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

import java.util.Arrays;
import java.util.List;

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
            final List<DoubleList> arrays = getNumberArrays(arg0, arg1);
            final PearsonsCorrelation pc = new PearsonsCorrelation();
            final double correl = pc.correlation(
                    arrays.get(0).toArray(), arrays.get(1).toArray());
            return new NumberEval(correl);
        } catch (EvaluationException e) {
            return e.getErrorEval();
        }
    }

    private List<DoubleList> getNumberArrays(ValueEval operand0, ValueEval operand1) throws EvaluationException {
        double[] retval0 = collectValuesWithBlanks(operand0).toArray();
        double[] retval1 = collectValuesWithBlanks(operand1).toArray();
        DoubleList filtered0 = new DoubleList();
        DoubleList filtered1 = new DoubleList();
        for (int i = 0; i < retval0.length; i++) {
            if (Double.isNaN(retval0[i]) || Double.isNaN(retval1[i])) {
                //ignore
            } else {
                filtered0.add(retval0[i]);
                filtered1.add(retval1[i]);
            }
        }
        return Arrays.asList(filtered0, filtered1);
    }

    private DoubleList collectValuesWithBlanks(ValueEval operand) throws EvaluationException {
        DoubleList doubleList = new DoubleList();
        if (operand instanceof ThreeDEval) {
            ThreeDEval ae = (ThreeDEval) operand;
            for (int sIx = ae.getFirstSheetIndex(); sIx <= ae.getLastSheetIndex(); sIx++) {
                int width = ae.getWidth();
                int height = ae.getHeight();
                for (int rrIx = 0; rrIx < height; rrIx++) {
                    for (int rcIx = 0; rcIx < width; rcIx++) {
                        ValueEval ve = ae.getValue(sIx, rrIx, rcIx);
                        Double d = collectValue(ve);
                        if (d == null) {
                            doubleList.add(Double.NaN);
                        } else {
                            doubleList.add(d.doubleValue());
                        }
                    }
                }
            }
            return doubleList;
        }
        if (operand instanceof TwoDEval) {
            TwoDEval ae = (TwoDEval) operand;
            int width = ae.getWidth();
            int height = ae.getHeight();
            for (int rrIx = 0; rrIx < height; rrIx++) {
                for (int rcIx = 0; rcIx < width; rcIx++) {
                    ValueEval ve = ae.getValue(rrIx, rcIx);
                    Double d = collectValue(ve);
                    if (d == null) {
                        doubleList.add(Double.NaN);
                    } else {
                        doubleList.add(d.doubleValue());
                    }
                }
            }
            return doubleList;
        }
        if (operand instanceof RefEval) {
            RefEval re = (RefEval) operand;
            for (int sIx = re.getFirstSheetIndex(); sIx <= re.getLastSheetIndex(); sIx++) {
                Double d = collectValue(re.getInnerValueEval(sIx));
                if (d == null) {
                    doubleList.add(Double.NaN);
                } else {
                    doubleList.add(d.doubleValue());
                }
            }
            return doubleList;
        }
        Double d = collectValue(operand);
        if (d == null) {
            doubleList.add(Double.NaN);
        } else {
            doubleList.add(d.doubleValue());
        }
        return doubleList;
    }

    private Double collectValue(ValueEval ve) throws EvaluationException {
        if (ve == null) {
            throw new IllegalArgumentException("ve must not be null");
        }
        if (ve instanceof NumericValueEval) {
            NumericValueEval ne = (NumericValueEval) ve;
            return ne.getNumberValue();
        }
        if (ve instanceof StringValueEval) {
            String s = ((StringValueEval) ve).getStringValue().trim();
            return OperandResolver.parseDouble(s);
        }
        if (ve instanceof ErrorEval) {
            throw new EvaluationException((ErrorEval) ve);
        }
        if (ve == BlankEval.instance) {
            return null;
        }
        throw new RuntimeException("Invalid ValueEval type passed for conversion: ("
                + ve.getClass() + ")");
    }

}
