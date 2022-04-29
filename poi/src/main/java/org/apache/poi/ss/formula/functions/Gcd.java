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

import org.apache.commons.math3.util.ArithmeticUtils;
import org.apache.poi.ss.formula.OperationEvaluationContext;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.EvaluationException;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.OperandResolver;
import org.apache.poi.ss.formula.eval.ValueEval;

import java.util.ArrayList;

public class Gcd implements FreeRefFunction {

    public static final Gcd instance = new Gcd();

    @Override
    public ValueEval evaluate(ValueEval[] args, OperationEvaluationContext ec) {
        if (args.length < 1) {
            return ErrorEval.VALUE_INVALID;
        } else if (args.length == 1) {
            try {
                ValueEval v1 = OperandResolver.getSingleValue(args[0], ec.getRowIndex(), ec.getColumnIndex());
                long l = (long)OperandResolver.coerceValueToDouble(v1);
                if (l < 0) {
                    return ErrorEval.NUM_ERROR;
                }
                return new NumberEval(l);
            } catch (EvaluationException ee) {
                return ErrorEval.VALUE_INVALID;
            }
        } else {
            try {
                ArrayList<Long> evals = new ArrayList<>();
                for (int i = 0; i < args.length; i++) {
                    ValueEval ve = OperandResolver.getSingleValue(args[i], ec.getRowIndex(), ec.getColumnIndex());
                    long l = (long)OperandResolver.coerceValueToDouble(ve);
                    if (l < 0) {
                        return ErrorEval.NUM_ERROR;
                    }
                    evals.add(l);
                }
                long result = evals.get(0);
                for (int i = 1; i < evals.size(); i++) {
                    result = ArithmeticUtils.gcd(result, evals.get(i));
                }
                return new NumberEval(result);
            } catch (EvaluationException ee) {
                return ErrorEval.VALUE_INVALID;
            }
        }
    }
}
