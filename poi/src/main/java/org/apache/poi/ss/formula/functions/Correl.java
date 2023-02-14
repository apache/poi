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
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.EvaluationException;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.ValueEval;

import java.util.List;

import static org.apache.poi.ss.formula.functions.ArrayFunctionUtils.getNumberArrays;

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

    private Correl() {}

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
        } catch (Exception e) {
            return ErrorEval.NA;
        }
    }
}
