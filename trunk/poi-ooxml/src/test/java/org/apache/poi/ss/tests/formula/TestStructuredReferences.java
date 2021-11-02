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

package org.apache.poi.ss.tests.formula;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Table;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFTable;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

/**
 * Tests Excel Table expressions (structured references)
 * @see <a href="https://support.office.com/en-us/article/Using-structured-references-with-Excel-tables-F5ED2452-2337-4F71-BED3-C8AE6D2B276E">
 *         Excel Structured Reference Syntax
 *      </a>
 */
class TestStructuredReferences {

    /**
     * Test the regular expression used in INDIRECT() evaluation to recognize structured references
     */
    @Test
    void testTableExpressionSyntax() {
        assertTrue(Table.isStructuredReference.matcher("abc[col1]").matches(), "Valid structured reference syntax didn't match expression");
        assertTrue(Table.isStructuredReference.matcher("_abc[col1]").matches(), "Valid structured reference syntax didn't match expression");
        assertTrue(Table.isStructuredReference.matcher("_[col1]").matches(), "Valid structured reference syntax didn't match expression");
        assertTrue(Table.isStructuredReference.matcher("\\[col1]").matches(), "Valid structured reference syntax didn't match expression");
        assertTrue(Table.isStructuredReference.matcher("\\[col1]").matches(), "Valid structured reference syntax didn't match expression");
        assertTrue(Table.isStructuredReference.matcher("\\[#This Row]").matches(), "Valid structured reference syntax didn't match expression");
        assertTrue(Table.isStructuredReference.matcher("\\[ [col1], [col2] ]").matches(), "Valid structured reference syntax didn't match expression");

        // can't have a space between the table name and open bracket
        assertFalse(Table.isStructuredReference.matcher("\\abc [ [col1], [col2] ]").matches(), "Invalid structured reference syntax didn't fail expression");
    }

    @Test
    void testTableFormulas() throws Exception {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("StructuredReferences.xlsx")) {

            final FormulaEvaluator eval = new XSSFFormulaEvaluator(wb);
            final XSSFSheet tableSheet = wb.getSheet("Table");
            final XSSFSheet formulaSheet = wb.getSheet("Formulas");

            confirm(eval, tableSheet.getRow(5).getCell(0), 49);
            confirm(eval, formulaSheet.getRow(0).getCell(0), 209);
            confirm(eval, formulaSheet.getRow(1).getCell(0), "one");

            // test changing a table value, to see if the caches are properly cleared
            // Issue 59814

            // this test passes before the fix for 59814
            tableSheet.getRow(1).getCell(1).setCellValue("ONEA");
            confirm(eval, formulaSheet.getRow(1).getCell(0), "ONEA");

            // test adding a row to a table, issue 59814
            Row newRow = tableSheet.getRow(7);
            if (newRow == null) newRow = tableSheet.createRow(7);
            newRow.createCell(0, CellType.FORMULA).setCellFormula("\\_Prime.1[[#This Row],[@Number]]*\\_Prime.1[[#This Row],[@Number]]");
            newRow.createCell(1, CellType.STRING).setCellValue("thirteen");
            newRow.createCell(2, CellType.NUMERIC).setCellValue(13);

            // update Table
            final XSSFTable table = wb.getTable("\\_Prime.1");
            final AreaReference newArea = wb.getCreationHelper().createAreaReference(
                    table.getStartCellReference(),
                    new CellReference(table.getEndRowIndex() + 1, table.getEndColIndex()));
            String newAreaStr = newArea.formatAsString();
            table.getCTTable().setRef(newAreaStr);
            table.getCTTable().getAutoFilter().setRef(newAreaStr);
            table.updateHeaders();
            table.updateReferences();

            // these fail before the fix for 59814
            confirm(eval, tableSheet.getRow(7).getCell(0), 13 * 13);
            confirm(eval, formulaSheet.getRow(0).getCell(0), 209 + 13 * 13);

        }
    }

    private static void confirm(FormulaEvaluator fe, Cell cell, double expectedResult) {
        fe.clearAllCachedResultValues();
        CellValue cv = fe.evaluate(cell);
        assertEquals(CellType.NUMERIC, cv.getCellType(),  "expected numeric cell type but got " + cv.formatAsString());
        assertEquals(expectedResult, cv.getNumberValue(), 0.0);
    }

    private static void confirm(FormulaEvaluator fe, Cell cell, String expectedResult) {
        fe.clearAllCachedResultValues();
        CellValue cv = fe.evaluate(cell);
        assertEquals(CellType.STRING, cv.getCellType(), "expected String cell type but got " + cv.formatAsString());
        assertEquals(expectedResult, cv.getStringValue());
    }
}
