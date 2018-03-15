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

import java.util.Locale;

import org.apache.poi.ss.formula.OperationEvaluationContext;
import org.apache.poi.ss.formula.eval.*;

/**
 * Implementation for Excel DELTA() function.<p>
 * <p>
 * <b>Syntax</b>:<br> <b>DEC2HEX  </b>(<b>number</b>,<b>places</b> )<br>
 * <p>
 * Converts a decimal number to hexadecimal.
 *
 * The decimal integer you want to convert. If number is negative, places is ignored
 * and this function returns a 10-character (40-bit) hexadecimal number in which the
 * most significant bit is the sign bit. The remaining 39 bits are magnitude bits.
 * Negative numbers are represented using two's-complement notation.
 *
 * <ul>
 * <li>If number < -549,755,813,888 or if number > 549,755,813,887, this function returns the #NUM! error value.</li>
 * <li>If number is nonnumeric, this function returns the #VALUE! error value.</li>
 * </ul>
 *
 * <h2>places</h2>
 *
 * The number of characters to use. The places argument is useful for padding the
 * return value with leading 0s (zeros).
 *
 * <ul>
 * <li>If this argument is omitted, this function uses the minimum number of characters necessary.</li>
 * <li>If this function requires more than places characters, it returns the #NUM! error value.</li>
 * <li>If this argument is non numeric, this function returns the #VALUE! error value.</li>
 * <li>If this argument is negative, this function returns the #NUM! error value.</li>
 * <li>If this argument contains a decimal value, this function ignores the numbers to the right side of the decimal point.</li>
 * </ul>
 */
public final class Dec2Hex extends Var1or2ArgFunction implements FreeRefFunction {

    public static final FreeRefFunction instance = new Dec2Hex();

    private final static long MIN_VALUE = Long.parseLong("-549755813888");
    private final static long MAX_VALUE = Long.parseLong("549755813887");
    private final static int DEFAULT_PLACES_VALUE = 10;

    public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval number, ValueEval places) {
        ValueEval veText1;
        try {
            veText1 = OperandResolver.getSingleValue(number, srcRowIndex, srcColumnIndex);
        } catch (EvaluationException e) {
            return e.getErrorEval();
        }
        String strText1 = OperandResolver.coerceValueToString(veText1);
        Double number1 = OperandResolver.parseDouble(strText1);

        //If this number argument is non numeric, this function returns the #VALUE! error value.
        if (number1 == null) {
            return ErrorEval.VALUE_INVALID;
        }

        //If number < -549,755,813,888 or if number > 549,755,813,887, this function returns the #NUM! error value.
        if (number1.longValue() < MIN_VALUE || number1.longValue() > MAX_VALUE)  {
            return ErrorEval.NUM_ERROR;
        }

        int placesNumber = 0;
        if (number1 < 0) {
            placesNumber = DEFAULT_PLACES_VALUE;
        }
        else if (places != null) {
            ValueEval placesValueEval;
            try {
                placesValueEval = OperandResolver.getSingleValue(places, srcRowIndex, srcColumnIndex);
            } catch (EvaluationException e) {
                return e.getErrorEval();
            }
            String placesStr = OperandResolver.coerceValueToString(placesValueEval);
            Double placesNumberDouble = OperandResolver.parseDouble(placesStr);

            //non numeric value
            if (placesNumberDouble == null) {
                return ErrorEval.VALUE_INVALID;
            }

            //If this argument contains a decimal value, this function ignores the numbers to the right side of the decimal point.
            placesNumber = placesNumberDouble.intValue();

            if (placesNumber < 0)  {
                return ErrorEval.NUM_ERROR;
            }
        }

        String hex;
        if (placesNumber != 0) {
            hex = String.format(Locale.ROOT, "%0"+placesNumber+"X", number1.intValue());
        }
        else {
            hex = Long.toHexString(number1.longValue());
        }

        if (number1 < 0) {
            hex =  "FF"+  hex.substring(2);
        }

        return new StringEval(hex.toUpperCase(Locale.ROOT));
    }

    public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0) {
        return this.evaluate(srcRowIndex, srcColumnIndex, arg0, null);
    }

    public ValueEval evaluate(ValueEval[] args, OperationEvaluationContext ec) {
        if (args.length == 1) {
            return evaluate(ec.getRowIndex(), ec.getColumnIndex(), args[0]);
        }
        if (args.length == 2) {
            return evaluate(ec.getRowIndex(), ec.getColumnIndex(), args[0], args[1]);
        }
        return ErrorEval.VALUE_INVALID;
    }
}
