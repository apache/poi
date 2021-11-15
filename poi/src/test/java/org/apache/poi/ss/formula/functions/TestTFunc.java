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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.poi.ss.formula.eval.AreaEval;
import org.apache.poi.ss.formula.eval.BlankEval;
import org.apache.poi.ss.formula.eval.BoolEval;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.StringEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.junit.jupiter.api.Test;

/**
 * Test cases for Excel function T()
 */
final class TestTFunc {

    /**
     * @return the result of calling function T() with the specified argument
     */
    private static ValueEval invokeT(ValueEval arg) {
        ValueEval[] args = { arg, };
        ValueEval result = new T().evaluate(args, -1, (short)-1);
        assertNotNull(result, "result may never be null");
        return result;
    }

    /**
     * Simulates call: T(A1)
     * where cell A1 has the specified innerValue
     */
    private ValueEval invokeTWithReference(ValueEval innerValue) {
        ValueEval arg = EvalFactory.createRefEval("$B$2", innerValue);
        return invokeT(arg);
    }

    private static void confirmText(String text) {
        ValueEval arg = new StringEval(text);
        ValueEval eval = invokeT(arg);
        StringEval se = (StringEval) eval;
        assertEquals(text, se.getStringValue());
    }

    @Test
    void testTextValues() {
        confirmText("abc");
        confirmText("");
        confirmText(" ");
        confirmText("~");
        confirmText("123");
        confirmText("TRUE");
    }

    private static void confirmError(ValueEval arg) {
        ValueEval eval = invokeT(arg);
        assertSame(arg, eval);
    }

    @Test
    void testErrorValues() {

        confirmError(ErrorEval.VALUE_INVALID);
        confirmError(ErrorEval.NA);
        confirmError(ErrorEval.REF_INVALID);
    }

    private static void confirmString(ValueEval eval, String expected) {
        assertTrue(eval instanceof StringEval);
        assertEquals(expected, ((StringEval)eval).getStringValue());
    }

    private static void confirmOther(ValueEval arg) {
        ValueEval eval = invokeT(arg);
        confirmString(eval, "");
    }

    @Test
    void testOtherValues() {
        confirmOther(new NumberEval(2));
        confirmOther(BoolEval.FALSE);
        confirmOther(BlankEval.instance);  // can this particular case be verified?
    }

    @Test
    void testRefValues() {
        ValueEval eval;

        eval = invokeTWithReference(new StringEval("def"));
        confirmString(eval, "def");
        eval = invokeTWithReference(new StringEval(" "));
        confirmString(eval, " ");

        eval = invokeTWithReference(new NumberEval(2));
        confirmString(eval, "");
        eval = invokeTWithReference(BoolEval.TRUE);
        confirmString(eval, "");

        eval = invokeTWithReference(ErrorEval.NAME_INVALID);
        assertSame(ErrorEval.NAME_INVALID, eval);
    }

    @Test
    void testAreaArg() {
        ValueEval[] areaValues = new ValueEval[] {
            new StringEval("abc"), new StringEval("def"),
            new StringEval("ghi"), new StringEval("jkl"),
        };
        AreaEval ae = EvalFactory.createAreaEval("C10:D11", areaValues);

        ValueEval ve = invokeT(ae);
        confirmString(ve, "abc");

        areaValues[0] = new NumberEval(5.0);
        ve = invokeT(ae);
        confirmString(ve, "");
    }
}
