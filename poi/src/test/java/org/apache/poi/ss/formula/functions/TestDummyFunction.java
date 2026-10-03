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
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.StringEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link DummyFunction}
 */
final class TestDummyFunction {

    private static final OperationEvaluationContext ec = new OperationEvaluationContext(null, null, 0, 0, 0, null);

    @Test
    void testReturnsCachedNumericValue() {
        ValueEval[] args = {
            new StringEval("QUERY(A1:D10,\"SELECT A\",1)"),
            new NumberEval(42.0)
        };
        ValueEval result = DummyFunction.instance.evaluate(args, ec);
        assertEquals(NumberEval.class, result.getClass());
        assertEquals(42.0, ((NumberEval) result).getNumberValue(), 0.0);
    }

    @Test
    void testReturnsCachedStringValue() {
        ValueEval[] args = {
            new StringEval("GOOGLEFINANCE(\"GOOG\",\"price\")"),
            new StringEval("hello")
        };
        ValueEval result = DummyFunction.instance.evaluate(args, ec);
        assertEquals(StringEval.class, result.getClass());
        assertEquals("hello", ((StringEval) result).getStringValue());
    }

    @Test
    void testNoArgsReturnsValueInvalid() {
        ValueEval[] args = {};
        ValueEval result = DummyFunction.instance.evaluate(args, ec);
        assertEquals(ErrorEval.class, result.getClass());
        assertEquals(ErrorEval.VALUE_INVALID, result);
    }

    @Test
    void testSingleArgReturnsValueInvalid() {
        ValueEval[] args = {
            new StringEval("IMPORTRANGE(\"url\",\"Sheet1!A1\")")
        };
        ValueEval result = DummyFunction.instance.evaluate(args, ec);
        assertEquals(ErrorEval.class, result.getClass());
        assertEquals(ErrorEval.VALUE_INVALID, result);
    }
}
