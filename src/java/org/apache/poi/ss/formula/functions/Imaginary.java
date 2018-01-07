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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation for Excel IMAGINARY() function.<p>
 * <p>
 * <b>Syntax</b>:<br> <b>IMAGINARY  </b>(<b>Inumber</b>)<br>
 * <p>
 * Returns the imaginary coefficient of a complex number in x + yi or x + yj text format.
 * <p>
 * Inumber     is a complex number for which you want the imaginary coefficient.
 * <p>
 * Remarks
 * <ul>
 * <li>Use COMPLEX to convert real and imaginary coefficients into a complex number.</li>
 * </ul>
 *
 * @author cedric dot walter @ gmail dot com
 */
public class Imaginary extends Fixed1ArgFunction implements FreeRefFunction {

    public static final FreeRefFunction instance = new Imaginary();

    public static final String GROUP1_REAL_SIGN_REGEX = "([+-]?)";
    public static final String GROUP2_REAL_INTEGER_OR_DOUBLE_REGEX = "([0-9]+\\.[0-9]+|[0-9]*)";
    public static final String GROUP3_IMAGINARY_SIGN_REGEX = "([+-]?)";
    public static final String GROUP4_IMAGINARY_INTEGER_OR_DOUBLE_REGEX = "([0-9]+\\.[0-9]+|[0-9]*)";
    public static final String GROUP5_IMAGINARY_GROUP_REGEX = "([ij]?)";

    public static final Pattern COMPLEX_NUMBER_PATTERN
            = Pattern.compile(GROUP1_REAL_SIGN_REGEX + GROUP2_REAL_INTEGER_OR_DOUBLE_REGEX +
            GROUP3_IMAGINARY_SIGN_REGEX + GROUP4_IMAGINARY_INTEGER_OR_DOUBLE_REGEX + GROUP5_IMAGINARY_GROUP_REGEX);

    public static final int GROUP1_REAL_SIGN = 1;
    public static final int GROUP2_IMAGINARY_INTEGER_OR_DOUBLE = 2;
    public static final int GROUP3_IMAGINARY_SIGN = 3;
    public static final int GROUP4_IMAGINARY_INTEGER_OR_DOUBLE = 4;

    public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval inumberVE) {
        ValueEval veText1;
        try {
            veText1 = OperandResolver.getSingleValue(inumberVE, srcRowIndex, srcColumnIndex);
        } catch (EvaluationException e) {
            return e.getErrorEval();
        }
        String iNumber = OperandResolver.coerceValueToString(veText1);

        Matcher m = COMPLEX_NUMBER_PATTERN.matcher(iNumber);
        boolean result = m.matches();

        String imaginary = "";
        if (result) {
            String imaginaryGroup = m.group(5);
            boolean hasImaginaryPart = imaginaryGroup.equals("i") || imaginaryGroup.equals("j");

            if (imaginaryGroup.length() == 0) {
                return new StringEval(String.valueOf(0));
            }

            if (hasImaginaryPart) {
                String sign = "";
                String imaginarySign = m.group(GROUP3_IMAGINARY_SIGN);
                if (imaginarySign.length() != 0 && !(imaginarySign.equals("+"))) {
                    sign = imaginarySign;
                }

                String groupImaginaryNumber = m.group(GROUP4_IMAGINARY_INTEGER_OR_DOUBLE);
                if (groupImaginaryNumber.length() != 0) {
                    imaginary = sign + groupImaginaryNumber;
                } else {
                    imaginary = sign + "1";
                }
            }
        } else {
            return ErrorEval.NUM_ERROR;
        }

        return new StringEval(imaginary);
    }

    public ValueEval evaluate(ValueEval[] args, OperationEvaluationContext ec) {
        if (args.length != 1) {
            return ErrorEval.VALUE_INVALID;
        }
        return evaluate(ec.getRowIndex(), ec.getColumnIndex(), args[0]);
    }
}
