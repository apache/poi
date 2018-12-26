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
import org.apache.poi.ss.formula.eval.EvaluationException;
import org.apache.poi.ss.formula.eval.OperandResolver;
import org.apache.poi.ss.formula.eval.StringEval;
import org.apache.poi.ss.formula.eval.ValueEval;

/**
 * Implementation for Excel Bin2Dec() function.<p>
 * <p>
 * <b>Syntax</b>:<br> <b>Bin2Dec  </b>(<b>number</b>,<b>[places]</b> )<br>
 * <p>
 * Converts a decimal number to binary.
 * <p>
 * The DEC2BIN function syntax has the following arguments:
 * <ul>
 * <li>Number    Required. The decimal integer you want to convert. If number is negative, valid place values are ignored and DEC2BIN returns a 10-character (10-bit) binary number in which the most significant bit is the sign bit. The remaining 9 bits are magnitude bits. Negative numbers are represented using two's-complement notation.</li>
 * <li>Places    Optional. The number of characters to use. If places is omitted, DEC2BIN uses the minimum number of characters necessary. Places is useful for padding the return value with leading 0s (zeros).</li>
 * </ul>
 * <p>
 * Remarks
 * <ul>
 * <li>If number < -512 or if number > 511, DEC2BIN returns the #NUM! error value.</li>
 * <li>If number is nonnumeric, DEC2BIN returns the #VALUE! error value.</li>
 * <li>If DEC2BIN requires more than places characters, it returns the #NUM! error value.</li>
 * <li>If places is not an integer, it is truncated.</li>
 * <li>If places is nonnumeric, DEC2BIN returns the #VALUE! error value.</li>
 * <li>If places is zero or negative, DEC2BIN returns the #NUM! error value.</li>
 * </ul>
 *
 * @author cedric dot walter @ gmail dot com
 */
public class Dec2Bin extends Var1or2ArgFunction implements FreeRefFunction {

    public static final FreeRefFunction instance = new Dec2Bin();

    private final static long MIN_VALUE = -512;
    private final static long MAX_VALUE =  511;
    private final static int DEFAULT_PLACES_VALUE = 10;

    public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval numberVE, ValueEval placesVE) {
        ValueEval veText1;
        try {
            veText1 = OperandResolver.getSingleValue(numberVE, srcRowIndex, srcColumnIndex);
        } catch (EvaluationException e) {
            return e.getErrorEval();
        }
        String strText1 = OperandResolver.coerceValueToString(veText1);
        Double number = OperandResolver.parseDouble(strText1);

        //If this number argument is non numeric, this function returns the #VALUE! error value.
        if (number == null) {
            return ErrorEval.VALUE_INVALID;
        }

        //If number < -512 or if number > 512, this function returns the #NUM! error value.
        if (number.longValue() < MIN_VALUE || number.longValue() > MAX_VALUE) {
            return ErrorEval.NUM_ERROR;
        }

        int placesNumber;
        if (number < 0 || placesVE == null) {
            placesNumber = DEFAULT_PLACES_VALUE;
        } else {
            ValueEval placesValueEval;
            try {
                placesValueEval = OperandResolver.getSingleValue(placesVE, srcRowIndex, srcColumnIndex);
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

            if (placesNumber < 0 || placesNumber == 0) {
                return ErrorEval.NUM_ERROR;
            }
        }
        String binary = Integer.toBinaryString(number.intValue());

        if (binary.length() > DEFAULT_PLACES_VALUE) {
            binary = binary.substring(binary.length() - DEFAULT_PLACES_VALUE);
        }
        //If DEC2BIN requires more than places characters, it returns the #NUM! error value.
        if (binary.length() > placesNumber) {
            return ErrorEval.NUM_ERROR;
        }

        return new StringEval(binary);
    }

    public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval numberVE) {
        return this.evaluate(srcRowIndex, srcColumnIndex, numberVE, null);
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
