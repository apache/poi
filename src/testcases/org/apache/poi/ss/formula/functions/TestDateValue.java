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

import org.apache.poi.ss.formula.eval.BlankEval;
import org.apache.poi.ss.formula.eval.BoolEval;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.StringEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests for Excel function DATEVALUE()
 *
 * @author Milosz Rembisz
 */
public final class TestDateValue {

    @Test
    public void testDateValue() {
        confirmDateValue(new StringEval("2020-02-01"), 43862);
        confirmDateValue(new StringEval("01-02-2020"), 43862);
        confirmDateValue(new StringEval("2020-FEB-01"), 43862);
        confirmDateValue(new StringEval("2020-Feb-01"), 43862);
        confirmDateValue(new StringEval("2020-FEBRUARY-01"), 43862);
        confirmDateValue(new StringEval("FEB-01"), 43862);
        confirmDateValue(new StringEval("2/1/2020"), 43862);
        confirmDateValue(new StringEval("2/1"), 43862);
        confirmDateValue(new StringEval("2020/2/1"), 43862);
        confirmDateValue(new StringEval("2020/FEB/1"), 43862);
        confirmDateValue(new StringEval("FEB/1/2020"), 43862);
        confirmDateValue(new StringEval("2020/02/01"), 43862);

        confirmDateValue(new StringEval(""), BlankEval.instance);
        confirmDateValue(BlankEval.instance, BlankEval.instance);

        confirmDateValue(new StringEval("non-date text"), ErrorEval.VALUE_INVALID);
    }

    private ValueEval invokeDateValue(ValueEval text) {
        return new DateValue().evaluate(0, 0, text);
    }

    private void confirmDateValue(ValueEval text, double expected) {
        ValueEval result = invokeDateValue(text);
        assertEquals(NumberEval.class, result.getClass());
        assertEquals(expected, ((NumberEval) result).getNumberValue(), 0.0001);
    }

    private void confirmDateValue(ValueEval text, BlankEval expected) {
        ValueEval result = invokeDateValue(text);
        assertEquals(BlankEval.class, result.getClass());
    }

    private void confirmDateValue(ValueEval text, ErrorEval expected) {
        ValueEval result = invokeDateValue(text);
        assertEquals(ErrorEval.class, result.getClass());
        assertEquals(expected.getErrorCode(), ((ErrorEval) result).getErrorCode());
    }
}