/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.ss.formula.functions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.poi.ss.formula.OperationEvaluationContext;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.NumericValueEval;
import org.apache.poi.ss.formula.eval.StringEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.junit.jupiter.api.Test;

/**
 * Test cases for AVERAGEIFS()
 */
final class TestAverageifs {

    private static final OperationEvaluationContext EC = new OperationEvaluationContext(null, null, 0, 1, 0, null);

    private static ValueEval invokeAverageifs(ValueEval[] args) {
        return new Averageifs().evaluate(args, EC);
    }

    private static void confirmDouble(double expected, ValueEval actualEval) {
        assertTrue(actualEval instanceof NumericValueEval, "Expected numeric result");
        NumericValueEval nve = (NumericValueEval)actualEval;
        assertEquals(expected, nve.getNumberValue(), 0);
    }

    private static void confirm(double expectedResult, ValueEval[] args) {
        confirmDouble(expectedResult, invokeAverageifs(args));
    }

    private static void confirmError(ErrorEval errorEval, ValueEval[] args) {
        ValueEval actualEval = invokeAverageifs(args);
        assertEquals(errorEval, actualEval);
    }

    /**
     *  Example 1 from
     *  https://support.microsoft.com/en-us/office/maxifs-function-dfd611e6-da2c-488a-919b-9b6376b28883
     */
    @Test
    void testExample1() {
        ValueEval[] b2b5 = new ValueEval[] {
                new StringEval("Quiz"),
                new StringEval("Grade"),
                new NumberEval(75),
                new NumberEval(94)
        };

        ValueEval[] args;
        // "=AVERAGEIFS(B2:B5, B2:B5, ">70", B2:B5, "<90")"
        args = new ValueEval[]{
                EvalFactory.createAreaEval("B2:B5", b2b5),
                EvalFactory.createAreaEval("B2:B5", b2b5),
                new StringEval(">70"),
                EvalFactory.createAreaEval("B2:B5", b2b5),
                new StringEval("<90")
        };
        confirm(75.0, args);

        ValueEval[] c2c5 = new ValueEval[] {
                new StringEval("Quiz"),
                new StringEval("Grade"),
                new NumberEval(85),
                new NumberEval(80)
        };
        // "=AVERAGEIFS(C2:C5, C2:C5, ">95")"
        args = new ValueEval[]{
                EvalFactory.createAreaEval("C2:C5", c2c5),
                EvalFactory.createAreaEval("C2:C5", c2c5),
                new StringEval(">95")
        };
        confirmError(ErrorEval.DIV_ZERO, args);

    }

}
