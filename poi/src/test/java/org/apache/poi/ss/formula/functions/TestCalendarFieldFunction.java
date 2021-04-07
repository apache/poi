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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test for YEAR / MONTH / DAY / HOUR / MINUTE / SECOND
 */
final class TestCalendarFieldFunction {

    private HSSFCell cell11;
    private HSSFFormulaEvaluator evaluator;

    @BeforeEach
    void setUp() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet("new sheet");
        cell11 = sheet.createRow(0).createCell(0);
        evaluator = new HSSFFormulaEvaluator(wb);
    }

    @Test
    void testValid() {
        confirm("YEAR(2.26)", 1900);
        confirm("MONTH(2.26)", 1);
        confirm("DAY(2.26)", 2);
        confirm("HOUR(2.26)", 6);
        confirm("MINUTE(2.26)", 14);
        confirm("SECOND(2.26)", 24);

        confirm("YEAR(40627.4860417)", 2011);
        confirm("MONTH(40627.4860417)", 3);
        confirm("DAY(40627.4860417)", 25);
        confirm("HOUR(40627.4860417)", 11);
        confirm("MINUTE(40627.4860417)", 39);
        confirm("SECOND(40627.4860417)", 54);
    }

    @Test
    void testRounding() {
		// 41484.999994200 = 23:59:59,499
		// 41484.9999942129 = 23:59:59,500  (but sub-milliseconds are below 0.5 (0.49999453965575), XLS-second results in 59)
		// 41484.9999942130 = 23:59:59,500  (sub-milliseconds are 0.50000334065408, XLS-second results in 00)

        confirm("DAY(41484.999994200)", 29);
        confirm("SECOND(41484.999994200)", 59);

        confirm("DAY(41484.9999942129)", 29);
        confirm("HOUR(41484.9999942129)", 23);
        confirm("MINUTE(41484.9999942129)", 59);
        confirm("SECOND(41484.9999942129)", 59);

        confirm("DAY(41484.9999942130)", 30);
        confirm("HOUR(41484.9999942130)", 0);
        confirm("MINUTE(41484.9999942130)", 0);
        confirm("SECOND(41484.9999942130)", 0);
	}

    @Test
    void testDaylightSaving() {
        confirm("HOUR(41364.08263888890000)", 1);		// 31.03.2013 01:59:00,000
        confirm("HOUR(41364.08333333330000)", 2);		// 31.03.2013 02:00:00,000 (this time does not exist in TZ CET, but EXCEL does not care)
        confirm("HOUR(41364.08402777780000)", 2);		// 31.03.2013 02:01:00,000
        confirm("HOUR(41364.12430555560000)", 2);		// 31.03.2013 02:59:00,000
        confirm("HOUR(41364.12500000000000)", 3);		// 31.03.2013 03:00:00,000
	}

    @Test
    void testBugDate() {
        confirm("YEAR(0.0)", 1900);
        confirm("MONTH(0.0)", 1);
        confirm("DAY(0.0)", 0);

        confirm("YEAR(0.26)", 1900);
        confirm("MONTH(0.26)", 1);
        confirm("DAY(0.26)", 0);
        confirm("HOUR(0.26)", 6);
        confirm("MINUTE(0.26)", 14);
        confirm("SECOND(0.26)", 24);
    }

    private void confirm(String formulaText, double expectedResult) {
        cell11.setCellFormula(formulaText);
        evaluator.clearAllCachedResultValues();
        CellValue cv = evaluator.evaluate(cell11);
        assertEquals(CellType.NUMERIC, cv.getCellType(), "Wrong result type");
        double actualValue = cv.getNumberValue();
        assertEquals(expectedResult, actualValue, 0);
    }
}
