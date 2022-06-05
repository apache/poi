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

import org.apache.poi.ss.formula.OperationEvaluationContext;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.EvaluationException;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.OperandResolver;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.util.NumberToTextConverter;

/**
 * Implementation for Excel SQRTPI() function.
 * <p>
 *   <b>Syntax</b>:<br> <b>SQRTPI </b>(<b>number</b>)<br>
 * </p>
 * <p>
 *   Returns the square root of (number * pi).
 * </p>
 * <p>
 *   See https://support.microsoft.com/en-us/office/sqrtpi-function-1fb4e63f-9b51-46d6-ad68-b3e7a8b519b4
 * </p>
 */
public class Sqrtpi implements FreeRefFunction {

    public static final Sqrtpi instance = new Sqrtpi();

    @Override
    public ValueEval evaluate(final ValueEval[] args, final OperationEvaluationContext ec) {
        if (args.length != 1) {
            return ErrorEval.VALUE_INVALID;
        }
        return evaluate(ec.getRowIndex(), ec.getColumnIndex(), args[0]);
    }

    private ValueEval evaluate(final int srcRowIndex, final int srcColumnIndex, final ValueEval arg0) {
        try {
            final ValueEval v1 = OperandResolver.getSingleValue(arg0, srcRowIndex, srcColumnIndex);
            final double d = OperandResolver.coerceValueToDouble(v1);
            if (isInvalidInput(d)) {
                return ErrorEval.NUM_ERROR;
            }
            final double result = Math.sqrt(Math.PI * d);
            //NumberToTextConverter reduces the precision to what Excel uses internally
            //without this conversion, `result` is too precise
            return new NumberEval(Double.parseDouble(NumberToTextConverter.toText(result)));
        } catch (EvaluationException e) {
            return e.getErrorEval();
        }
    }

    private boolean isInvalidInput(double d) {
        return (d < 0);
    }
}
