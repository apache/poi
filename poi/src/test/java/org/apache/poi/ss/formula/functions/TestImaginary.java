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
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;

import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.StringEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.junit.jupiter.api.Test;

/**
 * Tests for IMAGINARY() and IMREAL()
 */
final class TestImaginary {

    private static ValueEval imaginary(String text) {
        return new Imaginary().evaluate(-1, -1, new StringEval(text));
    }

    private static ValueEval imReal(String text) {
        return new ImReal().evaluate(-1, -1, new StringEval(text));
    }

    @Test
    void testParts() {
        assertEquals("4", ((StringEval) imaginary("3+4i")).getStringValue());
        assertEquals("3", ((StringEval) imReal("3+4i")).getStringValue());
        assertEquals("-4.5", ((StringEval) imaginary("-3.25-4.5j")).getStringValue());
        assertEquals("-3.25", ((StringEval) imReal("-3.25-4.5j")).getStringValue());
        assertEquals("1", ((StringEval) imaginary("i")).getStringValue());
        assertEquals("0", ((StringEval) imaginary("12")).getStringValue());
        assertEquals("12", ((StringEval) imReal("12")).getStringValue());
        // the real part can end inside a run of digits: "1.1" and "1.1i"
        assertEquals("1.1", ((StringEval) imaginary("1.11.1i")).getStringValue());
        assertEquals("1.1", ((StringEval) imReal("1.11.1i")).getStringValue());
        assertEquals(ErrorEval.NUM_ERROR, imaginary("1x"));
        assertEquals(ErrorEval.NUM_ERROR, imReal("1+2k"));
    }

    @Test
    void testLongNonComplexText() {
        // the complex number regular expression used to backtrack over the ways to split the digits,
        // which took minutes for this text
        String text = "1".repeat(20000) + "x";
        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
            assertEquals(ErrorEval.NUM_ERROR, imaginary(text));
            assertEquals(ErrorEval.NUM_ERROR, imReal(text));
        });
    }
}
