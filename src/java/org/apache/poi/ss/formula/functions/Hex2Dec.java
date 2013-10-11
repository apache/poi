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
import org.apache.poi.ss.formula.eval.*;

import java.math.BigInteger;

/**
 * Implementation for Excel HEX2DEC() function.<p/>
 * <p/>
 * <b>Syntax</b>:<br/> <b>HEX2DEC  </b>(<b>number</b>)<br/>
 * <p/>
 * Converts a hexadecimal number to decimal.
 * <p/>
 * Number     is the hexadecimal number you want to convert. Number cannot contain more than 10 characters (40 bits).
 * The most significant bit of number is the sign bit.
 * The remaining 39 bits are magnitude bits. Negative numbers are represented using two's-complement notation.
 * Remark
 * If number is not a valid hexadecimal number, HEX2DEC returns the #NUM! error value.
 *
 * @author cedric dot walter @ gmail dot com
 */
public class Hex2Dec extends Fixed1ArgFunction implements FreeRefFunction {

    public static final FreeRefFunction instance = new Hex2Dec();

    public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval numberVE) {
        String number = OperandResolver.coerceValueToString(numberVE);
        if (number.length() > 10) {
            return ErrorEval.NUM_ERROR;
        }

        String unsigned;
        boolean isPositive = false;
        boolean isNegative = false;
        if (number.length() < 10) {
            unsigned = number;
            isPositive = true;
        } else {
            //remove sign bit
            unsigned = number.substring(1);
            isNegative =
                    number.startsWith("8") || number.startsWith("9") ||
                            number.startsWith("A") || number.startsWith("B") ||
                            number.startsWith("C") || number.startsWith("D") ||
                            number.startsWith("E") || number.startsWith("F");
        }

        long decimal;
        if (isPositive) {
            try {
                decimal = Integer.parseInt(unsigned, 16);
            } catch (NumberFormatException ee) {
                // number is not a valid hexadecimal number
                return ErrorEval.NUM_ERROR;
            }
        } else {
            if (isNegative) {
                BigInteger temp = new BigInteger(unsigned, 16);
                BigInteger subtract = BigInteger.ONE.shiftLeft(unsigned.length() * 4);
                temp = temp.subtract(subtract);
                decimal = temp.longValue();
            } else {
                try {
                    decimal = Integer.parseInt(unsigned, 16);
                } catch (NumberFormatException ee) {
                    // number is not a valid hexadecimal number
                    return ErrorEval.NUM_ERROR;
                }
            }
        }

        return new NumberEval(decimal);
    }

    public ValueEval evaluate(ValueEval[] args, OperationEvaluationContext ec) {
        if (args.length != 1) {
            return ErrorEval.VALUE_INVALID;
        }
        return evaluate(ec.getRowIndex(), ec.getColumnIndex(), args[0]);
    }
}
