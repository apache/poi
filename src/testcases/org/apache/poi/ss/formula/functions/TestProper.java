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

import org.apache.poi.ss.formula.eval.StringEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestProper {
    @Test
    public void test() {
        checkProper("", "");
        checkProper("a", "A");
        checkProper("abc", "Abc");
        checkProper("abc abc", "Abc Abc");
        checkProper("abc/abc", "Abc/Abc");
        checkProper("ABC/ABC", "Abc/Abc");
        checkProper("aBc/ABC", "Abc/Abc");
        checkProper("aBc@#$%^&*()_+=-ABC", "Abc@#$%^&*()_+=-Abc");
        checkProper("aBc25aerg/ABC", "Abc25Aerg/Abc");
        checkProper("aBc/\u00C4\u00F6\u00DF\u00FC/ABC", "Abc/\u00C4\u00F6\u00DF\u00FC/Abc");  // Some German umlauts with uppercase first letter is not changed
        checkProper("\u00FC", "\u00DC");
        checkProper("\u00DC", "\u00DC");
        checkProper("\u00DF", "SS");    // German "scharfes s" is uppercased to "SS"
        checkProper("aBc/\u00FC\u00C4\u00F6\u00DF\u00FC/ABC", "Abc/\u00DC\u00E4\u00F6\u00DF\u00FC/Abc");  // Some German umlauts with lowercase first letter is changed to uppercase
    }

    @Test
    public void testMicroBenchmark() {
        ValueEval strArg = new StringEval("some longer text that needs a number of replacements to check for runtime of different implementations");
        long start = System.currentTimeMillis();
        for(int i = 0;i < 300000;i++) {
            final ValueEval ret = TextFunction.PROPER.evaluate(new ValueEval[]{strArg}, 0, 0);
            assertEquals("Some Longer Text That Needs A Number Of Replacements To Check For Runtime Of Different Implementations", ((StringEval)ret).getStringValue());
        }
        // Took aprox. 600ms on a decent Laptop in July 2016
        System.out.println("Took: " + (System.currentTimeMillis() - start) + "ms");
    }

    private void checkProper(String input, String expected) {
        ValueEval strArg = new StringEval(input);
        final ValueEval ret = TextFunction.PROPER.evaluate(new ValueEval[]{strArg}, 0, 0);
        assertEquals(expected, ((StringEval)ret).getStringValue());
    }
}
