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

import java.util.regex.Matcher;

import org.apache.poi.ss.formula.OperationEvaluationContext;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.EvaluationException;
import org.apache.poi.ss.formula.eval.OperandResolver;
import org.apache.poi.ss.formula.eval.StringEval;
import org.apache.poi.ss.formula.eval.ValueEval;

/**
 * Implementation for Excel ImReal() function.<p>
 * <p>
 * <b>Syntax</b>:<br> <b>ImReal  </b>(<b>Inumber</b>)<br>
 * <p>
 * Returns the real coefficient of a complex number in x + yi or x + yj text format.
 * <p>
 * Inumber     A complex number for which you want the real coefficient.
 * <p>
 * Remarks
 * <ul>
 * <li>If inumber is not in the form x + yi or x + yj, this function returns the #NUM! error value.</li>
 * <li>Use COMPLEX to convert real and imaginary coefficients into a complex number.</li>
 * </ul>
 *
 * @author cedric dot walter @ gmail dot com
 */
public class ImReal extends Fixed1ArgFunction implements FreeRefFunction {

    public static final FreeRefFunction instance = new ImReal();

    public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval inumberVE) {
        ValueEval veText1;
        try {
            veText1 = OperandResolver.getSingleValue(inumberVE, srcRowIndex, srcColumnIndex);
        } catch (EvaluationException e) {
            return e.getErrorEval();
        }
        String iNumber = OperandResolver.coerceValueToString(veText1);

        Matcher m = Imaginary.COMPLEX_NUMBER_PATTERN.matcher(iNumber);
        boolean result = m.matches();

        String real = "";
        if (result) {
            String realGroup = m.group(2);
            boolean hasRealPart = realGroup.length() != 0;

            if (realGroup.length() == 0) {
                return new StringEval(String.valueOf(0));
            }

            if (hasRealPart) {
                String sign = "";
                String realSign = m.group(Imaginary.GROUP1_REAL_SIGN);
                if (realSign.length() != 0 && !(realSign.equals("+"))) {
                    sign = realSign;
                }

                String groupRealNumber = m.group(Imaginary.GROUP2_IMAGINARY_INTEGER_OR_DOUBLE);
                if (groupRealNumber.length() != 0) {
                    real = sign + groupRealNumber;
                } else {
                    real = sign + "1";
                }
            }
        } else {
            return ErrorEval.NUM_ERROR;
        }

        return new StringEval(real);
    }

    public ValueEval evaluate(ValueEval[] args, OperationEvaluationContext ec) {
        if (args.length != 1) {
            return ErrorEval.VALUE_INVALID;
        }
        return evaluate(ec.getRowIndex(), ec.getColumnIndex(), args[0]);
    }
}
