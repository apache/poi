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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * @author Pavel Krupets (pkrupets at palmtreebusiness dot com)
 */
final class TestDate {

    private HSSFCell cell11;
    private HSSFFormulaEvaluator evaluator;

    @BeforeEach
    void setUp() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet("new sheet");
        cell11 = sheet.createRow(0).createCell(0);
        evaluator = new HSSFFormulaEvaluator(wb);
    }

    /**
     * Test disabled pending a fix in the formula evaluator
     * TODO - create MissingArgEval and modify the formula evaluator to handle this
     */
    @Test
    @Disabled
    void testSomeArgumentsMissing() {
        confirm("DATE(, 1, 0)", 0.0);
        confirm("DATE(, 1, 1)", 1.0);
    }

    @Test
    void testValid() {
        confirm("DATE(1900, 1, 1)", 1);
        confirm("DATE(1900, 1, 32)", 32);
        confirm("DATE(1900, 222, 1)", 6727);
        confirm("DATE(1900, 2, 0)", 31);
        confirm("DATE(2000, 1, 222)", 36747.00);
        confirm("DATE(2007, 1, 1)", 39083);
    }

    @Test
    void testBugDate() {
        confirm("DATE(1900, 2, 29)", 60);
        confirm("DATE(1900, 2, 30)", 61);
        confirm("DATE(1900, 1, 222)", 222);
        confirm("DATE(1900, 1, 2222)", 2222);
        confirm("DATE(1900, 1, 22222)", 22222);
    }

    @Test
    void testPartYears() {
        confirm("DATE(4, 1, 1)", 1462.00);
        confirm("DATE(14, 1, 1)", 5115.00);
        confirm("DATE(104, 1, 1)", 37987.00);
        confirm("DATE(1004, 1, 1)", 366705.00);
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
