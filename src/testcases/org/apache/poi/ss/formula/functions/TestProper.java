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

import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public final class TestProper extends TestCase {
    private Cell cell11;
    private FormulaEvaluator evaluator;

    public void testValidHSSF() {
        HSSFWorkbook wb = new HSSFWorkbook();
        evaluator = new HSSFFormulaEvaluator(wb);

        confirm(wb);
    }

    public void testValidXSSF() {
        XSSFWorkbook wb = new XSSFWorkbook();
        evaluator = new XSSFFormulaEvaluator(wb);

        confirm(wb);
    }

    private void confirm(Workbook wb) {
        Sheet sheet = wb.createSheet("new sheet");
        cell11 = sheet.createRow(0).createCell(0);
        cell11.setCellType(XSSFCell.CELL_TYPE_FORMULA);

        confirm("PROPER(\"hi there\")", "Hi There");
        confirm("PROPER(\"what's up\")", "What'S Up");
        confirm("PROPER(\"I DON'T TH!NK SO!\")", "I Don'T Th!Nk So!");
        confirm("PROPER(\"dr\u00dcb\u00f6'\u00e4 \u00e9lo\u015f|\u00eb\u00e8 \")", "Dr\u00fcb\u00f6'\u00c4 \u00c9lo\u015f|\u00cb\u00e8 ");
        confirm("PROPER(\"hi123 the123re\")", "Hi123 The123Re");
        confirm("PROPER(\"-\")", "-");
        confirm("PROPER(\"!\u00a7$\")", "!\u00a7$");
        confirm("PROPER(\"/&%\")", "/&%");
        
        // also test longer string
        StringBuilder builder = new StringBuilder("A");
        StringBuilder expected = new StringBuilder("A");
        for(int i = 1;i < 254;i++) {
            builder.append((char)(65 + (i % 26)));
            expected.append((char)(97 + (i % 26)));
        }
        confirm("PROPER(\"" + builder.toString() + "\")", expected.toString());
    }

    private void confirm(String formulaText, String expectedResult) {
        cell11.setCellFormula(formulaText);
        evaluator.clearAllCachedResultValues();
        CellValue cv = evaluator.evaluate(cell11);
        if (cv.getCellType() != Cell.CELL_TYPE_STRING) {
            throw new AssertionFailedError("Wrong result type: " + cv.formatAsString());
        }
        String actualValue = cv.getStringValue();
        assertEquals(expectedResult, actualValue);
    }
}
