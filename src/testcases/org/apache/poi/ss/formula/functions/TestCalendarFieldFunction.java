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

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;

/**
 * Test for YEAR / MONTH / DAY / HOUR / MINUTE / SECOND
 */
public final class TestCalendarFieldFunction extends TestCase {

    private HSSFCell cell11;
    private HSSFFormulaEvaluator evaluator;

    public void setUp() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet("new sheet");
        cell11 = sheet.createRow(0).createCell(0);
        cell11.setCellType(HSSFCell.CELL_TYPE_FORMULA);
        evaluator = new HSSFFormulaEvaluator(wb);
    }

    public void testValid() {
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

    public void testBugDate() {
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
        if (cv.getCellType() != Cell.CELL_TYPE_NUMERIC) {
            throw new AssertionFailedError("Wrong result type: " + cv.formatAsString());
        }
        double actualValue = cv.getNumberValue();
        assertEquals(expectedResult, actualValue, 0);
    }
}
