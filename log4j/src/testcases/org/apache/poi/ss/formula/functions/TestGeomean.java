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

import org.apache.poi.ss.formula.eval.BlankEval;
import org.apache.poi.ss.formula.eval.BoolEval;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.MissingArgEval;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.StringEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.junit.jupiter.api.Test;

/**
 * From Excel documentation at https://support.office.com/en-us/article/geomean-function-db1ac48d-25a5-40a0-ab83-0b38980e40d5:
 * 1. Arguments can either be numbers or names, arrays, or references that contain numbers.
 * 2. Logical values and text representations of numbers that you type directly into the list of arguments are counted.
 * 3. If an array or reference argument contains text, logical values, or empty cells, those values are ignored; however, cells with the value zero are included.
 * 4. Arguments that are error values or text that cannot be translated into numbers cause errors.
 * 5. If any data point ≤ 0, GEOMEAN returns the #NUM! error value.
 *
 * Remarks:
 * Actually, 5. is not true. If an error is encountered before a 0 value, the error is returned.
 */
class TestGeomean {
    @Test
    void acceptanceTest() {
        Function geomean = getInstance();

        final ValueEval result = geomean.evaluate(new ValueEval[]{new NumberEval(2), new NumberEval(3)}, 0, 0);
        verifyNumericResult(2.449489742783178, result);
    }

    @Test
    void booleansByValueAreCoerced() {
        final ValueEval[] args = {BoolEval.TRUE};
        final ValueEval result = getInstance().evaluate(args, 0, 0);
        verifyNumericResult(1.0, result);
    }

    @Test
    void stringsByValueAreCoerced() {
        final ValueEval[] args = {new StringEval("2")};
        final ValueEval result = getInstance().evaluate(args, 0, 0);
        verifyNumericResult(2.0, result);
    }

    @Test
    void nonCoerceableStringsByValueCauseValueInvalid() {
        final ValueEval[] args = {new StringEval("foo")};
        final ValueEval result = getInstance().evaluate(args, 0, 0);
        assertEquals(ErrorEval.VALUE_INVALID, result);
    }

    @Test
    void booleansByReferenceAreSkipped() {
        final ValueEval[] args = new ValueEval[]{new NumberEval(2.0), EvalFactory.createRefEval("A1", BoolEval.TRUE)};
        final ValueEval result = getInstance().evaluate(args, 0, 0);
        verifyNumericResult(2.0, result);
    }

    @Test
    void booleansStringsAndBlanksByReferenceAreSkipped() {
        ValueEval ref = EvalFactory.createAreaEval("A1:A3", new ValueEval[]{new StringEval("foo"), BoolEval.FALSE, BlankEval.instance});
        final ValueEval[] args = {ref, new NumberEval(2.0)};
        final ValueEval result = getInstance().evaluate(args, 0, 0);
        verifyNumericResult(2.0, result);
    }

    @Test
    void stringsByValueAreCounted() {
        final ValueEval[] args = {new StringEval("2.0")};
        final ValueEval result = getInstance().evaluate(args, 0, 0);
        verifyNumericResult(2.0, result);
    }

    @Test
    void missingArgCountAsZero() {
        // and, naturally, produces a NUM_ERROR
        final ValueEval[] args = {new NumberEval(1.0), MissingArgEval.instance};
        final ValueEval result = getInstance().evaluate(args, 0, 0);
        assertEquals(ErrorEval.NUM_ERROR, result);
    }

    /**
     * Implementation-specific: the math lib returns 0 for the input [1.0, 0.0], but a NUM_ERROR should be returned.
     */
    @Test
    void sequence_1_0_shouldReturnError() {
        final ValueEval[] args = {new NumberEval(1.0), new NumberEval(0)};
        final ValueEval result = getInstance().evaluate(args, 0, 0);
        assertEquals(ErrorEval.NUM_ERROR, result);
    }

    @Test
    void minusOneShouldReturnError() {
        final ValueEval[] args = {new NumberEval(1.0), new NumberEval(-1.0)};
        final ValueEval result = getInstance().evaluate(args, 0, 0);
        assertEquals(ErrorEval.NUM_ERROR, result);
    }

    @Test
    void firstErrorPropagates() {
        final ValueEval[] args = {ErrorEval.DIV_ZERO, ErrorEval.NUM_ERROR};
        final ValueEval result = getInstance().evaluate(args, 0, 0);
        assertEquals(ErrorEval.DIV_ZERO, result);
    }

    private void verifyNumericResult(double expected, ValueEval result) {
        assertTrue(result instanceof NumberEval);
        assertEquals(expected, ((NumberEval) result).getNumberValue(), 1e-15);
    }

    private Function getInstance() {
        return AggregateFunction.GEOMEAN;
    }
}
