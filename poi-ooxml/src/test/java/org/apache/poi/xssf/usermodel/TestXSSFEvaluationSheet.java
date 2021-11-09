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
package org.apache.poi.xssf.usermodel;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.Map;

import org.apache.poi.ss.formula.EvaluationSheet;
import org.apache.poi.ss.usermodel.BaseTestXEvaluationSheet;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TestXSSFEvaluationSheet extends BaseTestXEvaluationSheet {

    @Test
    void testSheetEval() throws Exception {
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet("test");
        XSSFRow row = sheet.createRow(0);
        row.createCell(0);
        XSSFEvaluationSheet evalsheet = new XSSFEvaluationSheet(sheet);

        assertNotNull(evalsheet.getCell(0, 0), "Cell 0,0 is found");
        assertNull(evalsheet.getCell(0, 1), "Cell 0,1 is not found");
        assertNull(evalsheet.getCell(1, 0), "Cell 1,0 is not found");

        // now add Cell 0,1
        row.createCell(1);

        assertNotNull(evalsheet.getCell(0, 0), "Cell 0,0 is found");
        assertNotNull(evalsheet.getCell(0, 1), "Cell 0,1 is now also found");
        assertNull(evalsheet.getCell(1, 0), "Cell 1,0 is not found");

        // after clearing all values it also works
        row.createCell(2);
        evalsheet.clearAllCachedResultValues();

        assertNotNull(evalsheet.getCell(0, 0), "Cell 0,0 is found");
        assertNotNull(evalsheet.getCell(0, 2), "Cell 0,2 is now also found");
        assertNull(evalsheet.getCell(1, 0), "Cell 1,0 is not found");

        // other things
        assertEquals(sheet, evalsheet.getXSSFSheet());
    }

    @Test
    void testBug65675() throws IOException  {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFFormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            evaluator.setIgnoreMissingWorkbooks(true);

            XSSFSheet sheet = workbook.createSheet("sheet");
            XSSFRow row = sheet.createRow(0);
            XSSFCell cell = row.createCell(0, CellType.FORMULA);

            try {
                cell.setCellFormula("[some-workbook-that-does-not-yet-exist.xlsx]main!B:D");
                //it might be better if this succeeded but just adding this regression test for now
                fail("expected exception");
            } catch (RuntimeException re) {
                assertEquals("Book not linked for filename some-workbook-that-does-not-yet-exist.xlsx", re.getMessage());
            }
        }
    }

    @Override
    protected Map.Entry<Sheet, EvaluationSheet> getInstance() {
        XSSFSheet sheet = new XSSFWorkbook().createSheet();
        return new AbstractMap.SimpleEntry<>(sheet, new XSSFEvaluationSheet(sheet));
    }
}
