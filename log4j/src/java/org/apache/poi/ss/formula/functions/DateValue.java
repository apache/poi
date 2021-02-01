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

import org.apache.poi.ss.util.DateParser;
import org.apache.poi.ss.formula.eval.BlankEval;
import org.apache.poi.ss.formula.eval.EvaluationException;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.OperandResolver;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.usermodel.DateUtil;

/**
 * Implementation for the DATEVALUE() Excel function.<p>
 *
 * <b>Syntax:</b><br>
 * <b>DATEVALUE</b>(<b>date_text</b>)<p>
 * <p>
 * The <b>DATEVALUE</b> function converts a date that is stored as text to a serial number that Excel
 * recognizes as a date. For example, the formula <b>=DATEVALUE("1/1/2008")</b> returns 39448, the
 * serial number of the date 1/1/2008. Remember, though, that your computer's system date setting may
 * cause the results of a <b>DATEVALUE</b> function to vary from this example
 * <p>
 * The <b>DATEVALUE</b> function is helpful in cases where a worksheet contains dates in a text format
 * that you want to filter, sort, or format as dates, or use in date calculations.
 * <p>
 * To view a date serial number as a date, you must apply a date format to the cell. Find links to more
 * information about displaying numbers as dates in the See Also section.
 *
 * @author Milosz Rembisz
 */
public class DateValue extends Fixed1ArgFunction {

    @Override
    public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval dateTextArg) {
        try {
            String dateText = OperandResolver.coerceValueToString(
                    OperandResolver.getSingleValue(dateTextArg, srcRowIndex, srcColumnIndex));

            if (dateText == null || dateText.isEmpty()) {
                return BlankEval.instance;
            }

            return new NumberEval(DateUtil.getExcelDate(DateParser.parseLocalDate(dateText)));
        } catch (EvaluationException e) {
            return e.getErrorEval();
        }
    }
}
