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
import org.apache.poi.ss.formula.eval.OperandResolver;
import org.apache.poi.ss.formula.eval.StringEval;
import org.apache.poi.ss.formula.eval.ValueEval;

/**
 * Implementation for Excel Roman() function.
 * <p>
 * <b>Syntax</b>:<br> <b>Roman  </b>(<b>number</b>,<b>form</b>)<br>
 * <p>
 * Converts an arabic numeral to roman, as text.
 * <p>
 * Number  Required. The Arabic numeral you want converted.<p>
 * Form    Optional. A number specifying the type of roman numeral you want.
 *         The roman numeral style ranges from Classic to Simplified, becoming more concise as the value of form increases.
 * <p>
 * Return_type     a roman numeral, as text
 */
public class Roman extends Fixed2ArgFunction {

    //M (1000), CM (900), D (500), CD (400), C (100), XC (90), L (50), XL (40), X (10), IX (9), V (5), IV (4) and I (1).
    private static final int[] VALUES = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};

    private static final String[] ROMAN = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};

    private static final String[][] REPLACEMENTS = {
        { // form > 0
            "XLV",  "VL",   //  45
            "XCV",  "VC",   //  95
            "CDL",  "LD",   // 450
            "CML",  "LM",   // 950
            "CMVC", "LMVL"  // 995
        },{ // Form == 1 only
            "CDXC", "LDXL", // 490
            "CDVC", "LDVL", // 495
            "CMXC", "LMXL", // 990
            "XCIX", "VCIV", //  99
            "XLIX", "VLIV"  //  49
        },{ // form > 1
            "XLIX", "IL",   //  49
            "XCIX", "IC",   //  99
            "CDXC", "XD",   // 490
            "CDVC", "XDV",  // 495
            "CDIC", "XDIX", // 499
            "LMVL", "XMV",  // 995
            "CMIC", "XMIX", // 999
            "CMXC", "XM"    // 990
        },{ // form > 2
            "XDV",  "VD",   // 495
            "XDIX", "VDIV", // 499
            "XMV",  "VM",   // 995
            "XMIX", "VMIV"  // 999
        },{ // form == 4
            "VDIV", "ID",   // 499
            "VMIV", "IM"    // 999
        }
    };



    @Override
    public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval numberVE, ValueEval formVE) {
        final int number;
        try {
            ValueEval ve = OperandResolver.getSingleValue(numberVE, srcRowIndex, srcColumnIndex);
            number = OperandResolver.coerceValueToInt(ve);
        } catch (EvaluationException e) {
            return ErrorEval.VALUE_INVALID;
        }
        if (number < 0) {
            return ErrorEval.VALUE_INVALID;
        }
        if (number > 3999) {
            return ErrorEval.VALUE_INVALID;
        }
        if (number == 0) {
            return new StringEval("");
        }

        final int form;
        try {
            ValueEval ve = OperandResolver.getSingleValue(formVE, srcRowIndex, srcColumnIndex);
            form = OperandResolver.coerceValueToInt(ve);
        } catch (EvaluationException e) {
            return ErrorEval.NUM_ERROR;
        }

        if (form > 4 || form < 0) {
            return ErrorEval.VALUE_INVALID;
        }

        String result = this.integerToRoman(number);

        if (form == 0) {
            return new StringEval(result);
        }

        return new StringEval(makeConcise(result, form));
    }

    /**
     * Classic conversion.
     *
     * @param number the number
     */
    private String integerToRoman(int number) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < 13; i++) {
            while (number >= VALUES[i]) {
                number -= VALUES[i];
                result.append(ROMAN[i]);
            }
        }
        return result.toString();
    }

    /**
     * Use conversion rule to factor some parts and make them more concise
     *
     * @param input the input string
     * @param form the level of conciseness [0..4] with 4 being most concise and simplified
     */
    public String makeConcise(final String input, final int form) {
        String result = input;
        for (int i=0; i<=form && i<=4 && form > 0; i++) {
            if (i==1 && form>1) {
                // Replacement[1] is only meant for form == 1
                continue;
            }
            String[] repl = REPLACEMENTS[i];
            for (int j=0; j<repl.length; j+=2) {
                result = result.replace(repl[j],repl[j+1]);
            }
        }
        return result;
    }
}
