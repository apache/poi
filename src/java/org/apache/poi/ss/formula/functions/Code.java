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

import org.apache.poi.ss.formula.eval.*;

/**
 * Implementation for Excel CODE () function.<p>
 * <p>
 * <b>Syntax</b>:<br> <b>CODE   </b>(<b>text</b> )<br>
 * <p>
 * Returns a numeric code for the first character in a text string. The returned code corresponds to the character set used by your computer.
 * <p>
 * text The text for which you want the code of the first character.
 *
 * @author cedric dot walter @ gmail dot com
 */
public class Code extends Fixed1ArgFunction {

    public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval textArg) {

        ValueEval veText1;
        try {
            veText1 = OperandResolver.getSingleValue(textArg, srcRowIndex, srcColumnIndex);
        } catch (EvaluationException e) {
            return e.getErrorEval();
        }
        String text = OperandResolver.coerceValueToString(veText1);

        if (text.length() == 0) {
            return ErrorEval.VALUE_INVALID;
        }

        int code = text.charAt(0);

        return new StringEval(String.valueOf(code));
    }
}

