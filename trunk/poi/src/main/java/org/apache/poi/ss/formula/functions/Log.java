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
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.ValueEval;

public class Log {
    private static final double TEN = 10.0;
    private static final double LOG_10_TO_BASE_e = Math.log(TEN);

    public static ValueEval evaluate(ValueEval[] args, int srcRowIndex, int srcColumnIndex) {
        if (args.length != 1 && args.length != 2) {
            return ErrorEval.VALUE_INVALID;
        }

        try {
            double d0 = NumericFunction.singleOperandEvaluate(args[0], srcRowIndex, srcColumnIndex);
            double result;
            if (args.length == 1) {
                result = Math.log(d0) / LOG_10_TO_BASE_e;
            } else {
                double d1 = NumericFunction.singleOperandEvaluate(args[1], srcRowIndex, srcColumnIndex);
                double logE = Math.log(d0);
                result = (Double.compare(d1, Math.E) == 0) ? logE : (logE / Math.log(d1));
            }
            NumericFunction.checkValue(result);
            return new NumberEval(result);
        } catch (EvaluationException e) {
            return e.getErrorEval();
        }
    }
}
