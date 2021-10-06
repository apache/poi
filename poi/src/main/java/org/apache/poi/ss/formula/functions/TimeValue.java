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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.formula.eval.*;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.util.DateParser;

import java.time.DateTimeException;
import java.time.LocalDate;

/**
 * Implementation for the TIMEVALUE() Excel function.<p>
 *
 * <b>Syntax:</b><br>
 * <b>TIMEVALUE</b>(<b>date_text</b>)
 * <p>
 * The <b>TIMEVALUE</b> function converts a time that is stored as text to a serial number that Excel
 * recognizes as a date/time. For example, the formula <b>=TIMEVALUE("1/1/2008 12:00")</b> returns 0.5, the
 * serial number of the time 12:00. The date element is ignored (see {@link DateValue}).
 * Remember, though, that your computer's system date setting may
 * cause the results of a <b>TIMEVALUE</b> function to vary from this example.
 * <p>
 * The <b>TIMEVALUE</b> function is helpful in cases where a worksheet contains dates/times in a text format
 * that you want to filter, sort, or format as times, or use in time calculations.
 * <p>
 * To view a date serial number as a time, you must apply a times format to the cell.
 */
public class TimeValue extends Fixed1ArgFunction {

    private static final Logger LOG = LogManager.getLogger(TimeValue.class);

    @Override
    public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval dateTimeTextArg) {
        try {
            String dateTimeText = OperandResolver.coerceValueToString(
                    OperandResolver.getSingleValue(dateTimeTextArg, srcRowIndex, srcColumnIndex));

            if (dateTimeText == null || dateTimeText.isEmpty()) {
                return BlankEval.instance;
            }

            try {
                return parseTimeFromDateTime(dateTimeText);
            } catch (Exception e) {
                try {
                    //this could be a time (with no date part) - prepend a dummy date because
                    //parseTimeFromDateTime needs it
                    return parseTimeFromDateTime("1/01/2000 " + dateTimeText);
                } catch (Exception e2) {
                    LocalDate ld = DateParser.parseLocalDate(dateTimeText);
                    //return 0 as this is a pure date with no time element
                    return new NumberEval(0);
                }
            }
        } catch (DateTimeException dte) {
            LOG.atInfo().log("Failed to parse date/time", dte);
            return ErrorEval.VALUE_INVALID;
        } catch (EvaluationException e) {
            return e.getErrorEval();
        }
    }

    private NumberEval parseTimeFromDateTime(String dateTimeText) throws EvaluationException {
        double dateTimeValue = DateUtil.parseDateTime(dateTimeText);
        return new NumberEval(dateTimeValue - DateUtil.getExcelDate(DateParser.parseLocalDate(dateTimeText)));
    }
}
