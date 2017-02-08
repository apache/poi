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

import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.EvaluationException;

/**
 * Calculates Modified internal rate of return. Syntax is MIRR(cash_flow_values, finance_rate, reinvest_rate)
 *
 * <p>Returns the modified internal rate of return for a series of periodic cash flows. MIRR considers both the cost
 * of the investment and the interest received on reinvestment of cash.</p>
 *
 * Values is an array or a reference to cells that contain numbers. These numbers represent a series of payments (negative values) and income (positive values) occurring at regular periods.
 * <ul>
 *     <li>Values must contain at least one positive value and one negative value to calculate the modified internal rate of return. Otherwise, MIRR returns the #DIV/0! error value.</li>
 *     <li>If an array or reference argument contains text, logical values, or empty cells, those values are ignored; however, cells with the value zero are included.</li>
 * </ul>
 *
 * Finance_rate     is the interest rate you pay on the money used in the cash flows.
 * Reinvest_rate     is the interest rate you receive on the cash flows as you reinvest them.
 *
 * @author Carlos Delgado (carlos dot del dot est at gmail dot com)
 * @author Cedric Walter (cedric dot walter at gmail dot com)
 *
 * @see <a href="http://en.wikipedia.org/wiki/MIRR">Wikipedia on MIRR</a>
 * @see <a href="http://office.microsoft.com/en-001/excel-help/mirr-HP005209180.aspx">Excel MIRR</a>
 * @see Irr
 */
public class Mirr extends MultiOperandNumericFunction {

    public Mirr() {
        super(false, false);
    }

    @Override
    protected int getMaxNumOperands() {
        return 3;
    }

    @Override
    protected double evaluate(double[] values) throws EvaluationException {

        double financeRate = values[values.length-1];
        double reinvestRate = values[values.length-2];

        double[] mirrValues = new double[values.length - 2];
        System.arraycopy(values, 0, mirrValues, 0, mirrValues.length);

        boolean mirrValuesAreAllNegatives = true;
        for (double mirrValue : mirrValues) {
            mirrValuesAreAllNegatives &= mirrValue < 0;
        }
         if (mirrValuesAreAllNegatives) {
             return -1.0d;
         }

        boolean mirrValuesAreAllPositives = true;
        for (double mirrValue : mirrValues) {
            mirrValuesAreAllPositives &= mirrValue > 0;
        }
        if (mirrValuesAreAllPositives) {
            throw new EvaluationException(ErrorEval.DIV_ZERO);
        }

        return mirr(mirrValues, financeRate, reinvestRate);
    }

    private static double mirr(double[] in, double financeRate, double reinvestRate) {
        double value = 0;
        int numOfYears = in.length - 1;
        double pv = 0;
        double fv = 0;

        int indexN = 0;
        for (double anIn : in) {
            if (anIn < 0) {
                pv += anIn / Math.pow(1 + financeRate + reinvestRate, indexN++);
            }
        }

        for (double anIn : in) {
            if (anIn > 0) {
                fv += anIn * Math.pow(1 + financeRate, numOfYears - indexN++);
            }
        }

        if (fv != 0 && pv != 0) {
            value = Math.pow(-fv / pv, 1d / numOfYears) - 1;
        }
        return value;
    }
}
