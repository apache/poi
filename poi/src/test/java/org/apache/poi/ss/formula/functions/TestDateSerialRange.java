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

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaError;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.apache.poi.ss.util.Utils.assertDouble;
import static org.apache.poi.ss.util.Utils.assertError;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Excel's dates run from serial 0 (shown as 1900-01-00) to 2958465 (9999-12-31). The date functions
 * used to throw for a serial outside that range - a NullPointerException from the null that
 * DateUtil.getJavaDate returns for a negative serial, or an IllegalArgumentException from the
 * int conversion of a huge one - where Excel reports #NUM!.
 */
final class TestDateSerialRange {

    @Test
    void testMaxSerialIsTheLastDayOf9999() {
        assertEquals("9999-12-31", DateUtil.getLocalDateTime(DateUtil.MAX_EXCEL_DATE_SERIAL).toLocalDate().toString());
    }

    @Test
    void testNegativeSerial() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFCell cell = wb.createSheet().createRow(0).createCell(0);
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            assertError(fe, cell, "DAYS360(-1,5)", FormulaError.NUM);
            assertError(fe, cell, "DAYS(-1,0)", FormulaError.NUM);
            assertError(fe, cell, "EOMONTH(-1,-1)", FormulaError.NUM);
            assertError(fe, cell, "EDATE(-1,0)", FormulaError.NUM);
            assertError(fe, cell, "NETWORKDAYS(-1,-1)", FormulaError.NUM);
            assertError(fe, cell, "NETWORKDAYS(-1,5)", FormulaError.NUM);
            assertError(fe, cell, "WORKDAY(-1,-1)", FormulaError.NUM);
            assertError(fe, cell, "WORKDAY.INTL(-1,-1)", FormulaError.NUM);
            assertError(fe, cell, "YEARFRAC(-1,1)", FormulaError.NUM);
            assertError(fe, cell, "WEEKDAY(-1)", FormulaError.NUM);
            assertError(fe, cell, "DAY(-1)", FormulaError.NUM);
        }
    }

    @Test
    void testSerialBeyondTheLastDayOf9999() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFCell cell = wb.createSheet().createRow(0).createCell(0);
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            for (String serial : new String[]{"2958466", "1E10", "1E308"}) {
                assertError(fe, cell, "DAYS360(" + serial + ",1)", FormulaError.NUM);
                assertError(fe, cell, "DAYS(" + serial + ",0)", FormulaError.NUM);
                assertError(fe, cell, "EOMONTH(" + serial + ",0)", FormulaError.NUM);
                assertError(fe, cell, "EDATE(" + serial + ",0)", FormulaError.NUM);
                assertError(fe, cell, "NETWORKDAYS(1," + serial + ")", FormulaError.NUM);
                assertError(fe, cell, "WORKDAY(" + serial + ",1)", FormulaError.NUM);
                assertError(fe, cell, "WORKDAY.INTL(" + serial + ",1)", FormulaError.NUM);
                assertError(fe, cell, "YEARFRAC(1," + serial + ")", FormulaError.NUM);
                assertError(fe, cell, "WEEKDAY(" + serial + ")", FormulaError.NUM);
                assertError(fe, cell, "DAY(" + serial + ")", FormulaError.NUM);
                assertError(fe, cell, "MONTH(" + serial + ")", FormulaError.NUM);
                assertError(fe, cell, "YEAR(" + serial + ")", FormulaError.NUM);
            }
            // a result beyond the last day is #NUM! too
            assertError(fe, cell, "EOMONTH(2958465,1)", FormulaError.NUM);
            assertError(fe, cell, "EDATE(2958465,1)", FormulaError.NUM);
            assertError(fe, cell, "WORKDAY(2958465,1)", FormulaError.NUM);
            assertError(fe, cell, "WORKDAY.INTL(2958465,1)", FormulaError.NUM);
        }
    }

    @Test
    void testEdgesOfTheRange() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFCell cell = wb.createSheet().createRow(0).createCell(0);
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            // serial 0 is Excel's 1900-01-00
            assertDouble(fe, cell, "DAYS360(0,0)", 0, 0);
            assertDouble(fe, cell, "DAYS360(0,1)", 1, 0);
            assertDouble(fe, cell, "DAYS(0,0)", 0, 0);
            assertDouble(fe, cell, "EOMONTH(0,0)", 31, 0);
            assertDouble(fe, cell, "EDATE(0,0)", 0, 0);
            assertDouble(fe, cell, "NETWORKDAYS(0,0)", 0, 0);
            assertDouble(fe, cell, "WORKDAY(0,1)", 1, 0);
            // 9999-12-31, a Friday
            assertDouble(fe, cell, "DAY(2958465)", 31, 0);
            assertDouble(fe, cell, "DAY(2958465.9)", 31, 0);
            assertDouble(fe, cell, "YEAR(2958465)", 9999, 0);
            assertDouble(fe, cell, "WEEKDAY(2958465)", 6, 0);
            assertDouble(fe, cell, "EOMONTH(2958465,0)", 2958465, 0);
            assertDouble(fe, cell, "EDATE(2958465,0)", 2958465, 0);
            assertDouble(fe, cell, "WORKDAY(2958465,0)", 2958465, 0);
            assertDouble(fe, cell, "YEARFRAC(1,2958465)", 8100, 0);
            // an error argument is passed through rather than turned into #VALUE!
            assertError(fe, cell, "NETWORKDAYS(NA(),1)", FormulaError.NA);
            assertError(fe, cell, "WORKDAY(NA(),1)", FormulaError.NA);
            assertError(fe, cell, "NETWORKDAYS(\"a\",\"b\")", FormulaError.VALUE);
        }
    }
}
