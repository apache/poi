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

package org.apache.poi.ss.formula.atp;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.poi.ss.formula.eval.AreaEvalBase;
import org.apache.poi.ss.formula.eval.EvaluationException;
import org.apache.poi.ss.formula.eval.OperandResolver;
import org.apache.poi.ss.formula.eval.StringEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.usermodel.DateUtil;

/**
 * Evaluator for formula arguments.
 * 
 * @author jfaenomoto@gmail.com
 */
final class ArgumentsEvaluator {

    public static final ArgumentsEvaluator instance = new ArgumentsEvaluator();

    private ArgumentsEvaluator() {
        // enforces singleton
    }

    /**
     * Evaluate a generic {@link ValueEval} argument to a double value that represents a date in POI.
     * 
     * @param arg {@link ValueEval} an argument.
     * @param srcCellRow number cell row.
     * @param srcCellCol number cell column.
     * @return a double representing a date in POI.
     * @throws EvaluationException exception upon argument evaluation.
     */
    public double evaluateDateArg(ValueEval arg, int srcCellRow, int srcCellCol) throws EvaluationException {
        ValueEval ve = OperandResolver.getSingleValue(arg, srcCellRow, (short) srcCellCol);

        if (ve instanceof StringEval) {
            String strVal = ((StringEval) ve).getStringValue();
            Double dVal = OperandResolver.parseDouble(strVal);
            if (dVal != null) {
                return dVal.doubleValue();
            }
            Calendar date = DateParser.parseDate(strVal);
            return DateUtil.getExcelDate(date, false);
        }
        return OperandResolver.coerceValueToDouble(ve);
    }

    /**
     * Evaluate a generic {@link ValueEval} argument to an array of double values that represents dates in POI.
     * 
     * @param arg {@link ValueEval} an argument.
     * @param srcCellRow number cell row.
     * @param srcCellCol number cell column.
     * @return an array of doubles representing dates in POI.
     * @throws EvaluationException exception upon argument evaluation.
     */
    public double[] evaluateDatesArg(ValueEval arg, int srcCellRow, int srcCellCol) throws EvaluationException {
        if (arg == null) {
            return new double[0];
        }

        if (arg instanceof StringEval) {
            return new double[]{ evaluateDateArg(arg, srcCellRow, srcCellCol) };
        } else if (arg instanceof AreaEvalBase) {
            List<Double> valuesList = new ArrayList<>();
            AreaEvalBase area = (AreaEvalBase) arg;
            for (int i = area.getFirstRow(); i <= area.getLastRow(); i++) {
                for (int j = area.getFirstColumn(); j <= area.getLastColumn(); j++) {
                    // getValue() is replaced with getAbsoluteValue() because loop variables i, j are
                    // absolute indexes values, but getValue() works with relative indexes values
                    valuesList.add(evaluateDateArg(area.getAbsoluteValue(i, j), i, j));
                }
            }
            double[] values = new double[valuesList.size()];
            for (int i = 0; i < valuesList.size(); i++) {
                values[i] = valuesList.get(i).doubleValue();
            }
            return values;
        }
        return new double[]{ OperandResolver.coerceValueToDouble(arg) };
    }

    /**
     * Evaluate a generic {@link ValueEval} argument to a double value.
     * 
     * @param arg {@link ValueEval} an argument.
     * @param srcCellRow number cell row.
     * @param srcCellCol number cell column.
     * @return a double value.
     * @throws EvaluationException exception upon argument evaluation.
     */
    public double evaluateNumberArg(ValueEval arg, int srcCellRow, int srcCellCol) throws EvaluationException {
        if (arg == null) {
            return 0f;
        }

        return OperandResolver.coerceValueToDouble(arg);
    }
}
