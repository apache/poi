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
 * Implementation for Excel WeekNum() function.<p>
 * <p>
 * <b>Syntax</b>:<br> <b>WeekNum  </b>(<b>Serial_num</b>,<b>Return_type</b>)<br>
 * <p>
 * Returns a number that indicates where the week falls numerically within a year.
 * <p>
 * <p>
 * Serial_num     is a date within the week. Dates should be entered by using the DATE function,
 * or as results of other formulas or functions. For example, use DATE(2008,5,23)
 * for the 23rd day of May, 2008. Problems can occur if dates are entered as text.
 * Return_type     is a number that determines on which day the week begins. The default is 1.
 * 1	Week begins on Sunday. Weekdays are numbered 1 through 7.
 * 2	Week begins on Monday. Weekdays are numbered 1 through 7.
 *
 * @author cedric dot walter @ gmail dot com
 */
public class Roman extends Fixed2ArgFunction {

    //M (1000), CM (900), D (500), CD (400), C (100), XC (90), L (50), XL (40), X (10), IX (9), V (5), IV (4) and I (1).
    public static final int[] VALUES = new int[]{1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
    public static final String[] ROMAN = new String[]
            {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};


    public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval numberVE, ValueEval formVE) {
        int number = 0;
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

        int form = 0;
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
     * @param number
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
     * @param result
     * @param form
     */
    public String makeConcise(String result, int form) {
        if (form > 0) {
            result = result.replaceAll("XLV", "VL"); //45
            result = result.replaceAll("XCV", "VC"); //95
            result = result.replaceAll("CDL", "LD"); //450
            result = result.replaceAll("CML", "LM"); //950
            result = result.replaceAll("CMVC", "LMVL"); //995
        }
        if (form == 1) {
            result = result.replaceAll("CDXC", "LDXL"); //490
            result = result.replaceAll("CDVC", "LDVL"); //495
            result = result.replaceAll("CMXC", "LMXL"); //990
            result = result.replaceAll("XCIX", "VCIV"); //99
            result = result.replaceAll("XLIX", "VLIV"); //49
        }
        if (form > 1) {
            result = result.replaceAll("XLIX", "IL"); //49
            result = result.replaceAll("XCIX", "IC"); //99
            result = result.replaceAll("CDXC", "XD"); //490
            result = result.replaceAll("CDVC", "XDV"); //495
            result = result.replaceAll("CDIC", "XDIX"); //499
            result = result.replaceAll("LMVL", "XMV"); //995
            result = result.replaceAll("CMIC", "XMIX"); //999
            result = result.replaceAll("CMXC", "XM"); // 990
        }
        if (form > 2) {
            result = result.replaceAll("XDV", "VD");  //495
            result = result.replaceAll("XDIX", "VDIV"); //499
            result = result.replaceAll("XMV", "VM"); // 995
            result = result.replaceAll("XMIX", "VMIV"); //999
        }
        if (form == 4) {
            result = result.replaceAll("VDIV", "ID"); //499
            result = result.replaceAll("VMIV", "IM"); //999
        }

        return result;
    }
}
