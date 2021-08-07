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

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.formula.eval.*;
import org.apache.poi.util.LocaleUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Locale;

import static org.apache.poi.ss.util.Utils.assertDouble;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for Excel function TIMEVALUE()
 */
final class TestTimeValue {

    @BeforeAll
    public static void init() {
        LocaleUtil.setUserLocale(Locale.US);
    }

    @AfterAll
    public static void clear() {
        LocaleUtil.setUserLocale(null);
    }

    @Test
    void testTimeValue() {
        LocaleUtil.setUserLocale(Locale.ENGLISH);
        try {
            confirmTimeValue(new StringEval(""));
            confirmTimeValue(BlankEval.instance);

            confirmTimeValueError(new StringEval("non-date text"));

            // // EXCEL
            confirmTimeValue(new StringEval("8/22/2011"), 0); // Serial number of a time entered as text.
            confirmTimeValue(new StringEval("8/22/2011 12:00"), 0.5); // Serial number of a time entered as text.
            confirmTimeValue(new StringEval("1/01/2000 06:00"), 0.25); // Serial number of a time entered as text.
            confirmTimeValue(new StringEval("1/01/2000 6:00 PM"), 0.75); // Serial number of a time entered as text.
            confirmTimeValue(new StringEval("12:00"), 0.5); // Serial number of a time entered as text.
            confirmTimeValue(new StringEval("6:00 PM"), 0.75); // Serial number of a time entered as text.
            confirmTimeValue(new StringEval("12:03:45"), 0.5026041666642413); // Serial number of a time entered as text.

            // this is not yet right as the milliseconds are not counted - but this used to cause a parse issue before
            confirmTimeValue(new StringEval("12:03:45.386"), 0.5026041666642413);
        } finally {
            LocaleUtil.setUserLocale(null);
        }
    }

    @Test
    void testInvalidTimeValue() {
        assertEquals(ErrorEval.VALUE_INVALID, invokeTimeValue(new StringEval("not-date")),
                "not-date evals to invalid");
        assertEquals(ErrorEval.VALUE_INVALID, invokeTimeValue(BoolEval.FALSE),
                "false evals to invalid");
        assertEquals(ErrorEval.VALUE_INVALID, invokeTimeValue(new NumberEval(Math.E)),
                "Math.E evals to invalid");
    }

    @Test
    void testTimeValueInHSSF() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFSheet sheet = wb.createSheet();
            HSSFRow row = sheet.createRow(0);
            row.createCell(0).setCellValue("8/22/2011 12:00");
            HSSFCell cell = row.createCell(1);
            assertDouble(fe, cell, "TIMEVALUE(A1)", 0.5);
        }
    }

    private ValueEval invokeTimeValue(ValueEval text) {
        return new TimeValue().evaluate(0, 0, text);
    }

    private void confirmTimeValue(ValueEval text, double expected) {
        ValueEval result = invokeTimeValue(text);
        assertEquals(NumberEval.class, result.getClass());
        assertEquals(expected, ((NumberEval) result).getNumberValue(), 0.0001);
    }

    private void confirmTimeValue(ValueEval text) {
        ValueEval result = invokeTimeValue(text);
        assertEquals(BlankEval.class, result.getClass());
    }

    private void confirmTimeValueError(ValueEval text) {
        ValueEval result = invokeTimeValue(text);
        assertEquals(ErrorEval.class, result.getClass());
        assertEquals(ErrorEval.VALUE_INVALID.getErrorCode(), ((ErrorEval) result).getErrorCode());
    }
}
