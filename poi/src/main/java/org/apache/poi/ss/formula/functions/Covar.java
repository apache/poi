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

import org.apache.commons.math3.stat.correlation.Covariance;
import org.apache.poi.ss.formula.OperationEvaluationContext;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.EvaluationException;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.ValueEval;

import java.util.List;

import static org.apache.poi.ss.formula.functions.ArrayFunctionUtils.getNumberArrays;

/**
 * Implementation for Excel COVAR() and COVARIANCE.P() functions.
 * <p>
 *   <b>Syntax</b>:<br> <b>COVAR </b>(<b>array1</b>, <b>array2</b>)<br>
 * </p>
 * @see <a href="https://support.microsoft.com/en-us/office/covar-function-50479552-2c03-4daf-bd71-a5ab88b2db03">COVAR</a>
 * @see <a href="https://support.microsoft.com/en-us/office/covariance-p-function-6f0e1e6d-956d-4e4b-9943-cfef0bf9edfc">COVARIANCE.P</a>
 */
public class Covar extends Fixed2ArgFunction implements FreeRefFunction {

    public static final Covar instanceP = new Covar(false);
    public static final Covar instanceS = new Covar(true);

    private final boolean sampleBased;

    private Covar(boolean sampleBased) {
        this.sampleBased = sampleBased;
    }

    @Override
    public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0, ValueEval arg1) {
        try {
            final List<DoubleList> arrays = getNumberArrays(arg0, arg1);
            final Covariance covar = new Covariance();
            final double result = covar.covariance(
                    arrays.get(0).toArray(), arrays.get(1).toArray(), sampleBased);
            return new NumberEval(result);
        } catch (EvaluationException e) {
            return e.getErrorEval();
        } catch (Exception e) {
            return ErrorEval.NA;
        }
    }

    @Override
    public ValueEval evaluate(ValueEval[] args, OperationEvaluationContext ec) {
        if (args.length != 2) {
            return ErrorEval.VALUE_INVALID;
        }
        return evaluate(ec.getRowIndex(), ec.getColumnIndex(), args[0], args[1]);
    }
}
