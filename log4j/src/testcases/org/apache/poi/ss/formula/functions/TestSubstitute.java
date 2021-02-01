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
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.StringEval;
import org.apache.poi.ss.formula.eval.StringValueEval;
import org.apache.poi.ss.usermodel.FormulaError;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestSubstitute {
    @Test
    void testSubstitute() {
        Substitute fun = new Substitute();
        assertEquals("ADEFC", ((StringValueEval)fun.evaluate(0, 1,
                new StringEval("ABC"), new StringEval("B"), new StringEval("DEF"))).getStringValue());

        assertEquals("ACDEC", ((StringValueEval)fun.evaluate(0, 1,
                new StringEval("ABC"), new StringEval("B"), new StringEval("CDE"))).getStringValue());

        assertEquals("ACDECCDEA", ((StringValueEval)fun.evaluate(0, 1,
                new StringEval("ABCBA"), new StringEval("B"), new StringEval("CDE"))).getStringValue());
    }

    @Test
    void testSubstituteInvalidArg() {
        Substitute fun = new Substitute();
        assertEquals(ErrorEval.valueOf(FormulaError.VALUE.getLongCode()),
                fun.evaluate(0, 1,
                ErrorEval.valueOf(FormulaError.VALUE.getLongCode()), new StringEval("B"), new StringEval("DEF")));

        assertEquals(ErrorEval.valueOf(FormulaError.VALUE.getLongCode()),
                fun.evaluate(0, 1,
                ErrorEval.valueOf(FormulaError.VALUE.getLongCode()), new StringEval("B"), new StringEval("DEF"),
                        new NumberEval(1)));

        // fails on occurrence below 1
        assertEquals(ErrorEval.valueOf(FormulaError.VALUE.getLongCode()),
                fun.evaluate(0, 1,
                new StringEval("ABC"), new StringEval("B"), new StringEval("CDE"), new NumberEval(0)));
    }

    @Test
    void testSubstituteOne() {
        Substitute fun = new Substitute();
        assertEquals("ADEFC", ((StringValueEval)fun.evaluate(0, 1,
                new StringEval("ABC"), new StringEval("B"), new StringEval("DEF"), new NumberEval(1))).getStringValue());

        assertEquals("ACDEC", ((StringValueEval)fun.evaluate(0, 1,
                new StringEval("ABC"), new StringEval("B"), new StringEval("CDE"), new NumberEval(1))).getStringValue());
    }

    @Test
    void testSubstituteNotFound() {
        Substitute fun = new Substitute();
        assertEquals("ABC", ((StringValueEval)fun.evaluate(0, 1,
                new StringEval("ABC"), new StringEval("B"), new StringEval("DEF"), new NumberEval(12))).getStringValue());

        assertEquals("ABC", ((StringValueEval)fun.evaluate(0, 1,
                new StringEval("ABC"), new StringEval("B"), new StringEval("CDE"), new NumberEval(2))).getStringValue());
    }

    @Test
    void testSearchEmpty() {
        Substitute fun = new Substitute();
        assertEquals("ABC", ((StringValueEval)fun.evaluate(0, 1,
                new StringEval("ABC"), new StringEval(""), new StringEval("CDE"))).getStringValue());
        assertEquals("ABC", ((StringValueEval)fun.evaluate(0, 1,
                new StringEval("ABC"), new StringEval(""), new StringEval("CDE"), new NumberEval(1))).getStringValue());
    }
}