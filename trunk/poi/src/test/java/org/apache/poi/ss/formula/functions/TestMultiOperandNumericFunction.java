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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.formula.eval.EvaluationException;
import org.apache.poi.ss.formula.eval.MissingArgEval;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.junit.jupiter.api.Test;

class TestMultiOperandNumericFunction {

    @Test
    void testSettings() {
        MultiOperandNumericFunction fun = new MultiOperandNumericFunction(true, true) {
            @Override
            protected double evaluate(double[] values) throws EvaluationException {
                return 0;
            }

        };
        assertEquals(SpreadsheetVersion.EXCEL2007.getMaxFunctionArgs(), fun.getMaxNumOperands());
    }

    @Test
    void missingArgEvalsAreCountedAsZeroIfPolicyIsCoerce() {
        MultiOperandNumericFunction instance = new Stub(true, true, MultiOperandNumericFunction.Policy.COERCE);
        ValueEval result = instance.evaluate(new ValueEval[]{MissingArgEval.instance}, 0, 0);
        assertTrue(result instanceof NumberEval);
        assertEquals(0.0, ((NumberEval)result).getNumberValue(), 0);
    }

    @Test
    void missingArgEvalsAreSkippedIfZeroIfPolicyIsSkipped() {
        MultiOperandNumericFunction instance = new Stub(true, true, MultiOperandNumericFunction.Policy.SKIP);
        ValueEval result = instance.evaluate(new ValueEval[]{new NumberEval(1), MissingArgEval.instance}, 0, 0);
        assertTrue(result instanceof NumberEval);
        assertEquals(1.0, ((NumberEval)result).getNumberValue(), 0);
    }

    private static class Stub extends MultiOperandNumericFunction {
        protected Stub(
                boolean isReferenceBoolCounted, boolean isBlankCounted, MultiOperandNumericFunction.Policy missingArgEvalPolicy) {
            super(isReferenceBoolCounted, isBlankCounted);
            setMissingArgPolicy(missingArgEvalPolicy);
        }

        @Override
        protected double evaluate(double[] values) throws EvaluationException {
            return values[0];
        }
    }
}
