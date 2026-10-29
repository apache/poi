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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.apache.poi.ss.usermodel.BaseTestSheetUpdateArrayFormulas;
import org.apache.poi.ss.usermodel.CellRange;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.XSSFITestDataProvider;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.junit.jupiter.api.Test;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCell;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCellFormula;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STCellFormulaType;

/**
 * Test array formulas in XSSF
 */
public final class TestXSSFSheetUpdateArrayFormulas extends BaseTestSheetUpdateArrayFormulas {

    public TestXSSFSheetUpdateArrayFormulas() {
        super(XSSFITestDataProvider.instance);
    }

    // Test methods common with HSSF are in superclass
    // Local methods here test XSSF-specific details of updating array formulas
    @Test
    void testXSSFSetArrayFormula_singleCell() throws IOException {
        CellRange<XSSFCell> cells;

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet();

        // 1. single-cell array formula
        String formula1 = "123";
        CellRangeAddress range = CellRangeAddress.valueOf("C3:C3");
        cells = sheet.setArrayFormula(formula1, range);
        assertEquals(1, cells.size());

        // check getFirstCell...
        XSSFCell firstCell = cells.getTopLeftCell();
        assertSame(firstCell, sheet.getFirstCellInArrayFormula(firstCell));
        //retrieve the range and check it is the same
        assertEquals(range.formatAsString(), firstCell.getArrayFormulaRange().formatAsString());
        confirmArrayFormulaCell(firstCell, "C3", formula1, "C3");

        workbook.close();
    }

    @Test
    void testXSSFSetArrayFormula_multiCell() throws IOException {
        CellRange<XSSFCell> cells;

        String formula2 = "456";
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet();

        CellRangeAddress range = CellRangeAddress.valueOf("C4:C6");
        cells = sheet.setArrayFormula(formula2, range);
        assertEquals(3, cells.size());

        // sheet.setArrayFormula creates rows and cells for the designated range
        /*
         * From the spec:
         * For a multi-cell formula, the c elements for all cells except the top-left
         * cell in that range shall not have an f element;
         */
        // Check that each cell exists and that the formula text is set correctly on the first cell
        XSSFCell firstCell = cells.getTopLeftCell();
        confirmArrayFormulaCell(firstCell, "C4", formula2, "C4:C6");
        confirmArrayFormulaCell(cells.getCell(1, 0), "C5");
        confirmArrayFormulaCell(cells.getCell(2, 0), "C6");

        assertSame(firstCell, sheet.getFirstCellInArrayFormula(firstCell));
        workbook.close();
    }

    @Test
    void testManyArrayFormulas() throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet();
            // one 2-row array formula per pair of rows in column B, plus a tall one in column D
            for (int row = 0; row < 1000; row += 2) {
                sheet.setArrayFormula("1", new CellRangeAddress(row, row + 1, 1, 1));
            }
            sheet.setArrayFormula("2", CellRangeAddress.valueOf("D1:D1000"));

            try (XSSFWorkbook read = XSSFTestDataSamples.writeOutAndReadBack(workbook)) {
                XSSFSheet readSheet = read.getSheetAt(0);
                for (int row = 0; row < 1000; row++) {
                    XSSFCell cell = readSheet.getRow(row).getCell(1);
                    assertTrue(cell.isPartOfArrayFormulaGroup());
                    int firstRow = row - row % 2;
                    assertEquals(new CellRangeAddress(firstRow, firstRow + 1, 1, 1), cell.getArrayFormulaRange());
                    assertEquals("D1:D1000", readSheet.getRow(row).getCell(3).getArrayFormulaRange().formatAsString());
                }
                assertFalse(readSheet.getRow(500).createCell(2).isPartOfArrayFormulaGroup());

                readSheet.removeArrayFormula(readSheet.getRow(501).getCell(1));
                assertFalse(readSheet.getRow(500).getCell(1).isPartOfArrayFormulaGroup());
                assertFalse(readSheet.getRow(501).getCell(1).isPartOfArrayFormulaGroup());
                assertTrue(readSheet.getRow(502).getCell(1).isPartOfArrayFormulaGroup());
                assertTrue(readSheet.getRow(501).getCell(3).isPartOfArrayFormulaGroup());
            }
        }
    }

    private static void confirmArrayFormulaCell(XSSFCell c, String cellRef) {
        confirmArrayFormulaCell(c, cellRef, null, null);
    }
    private static void confirmArrayFormulaCell(XSSFCell c, String cellRef, String formulaText, String arrayRangeRef) {
        assertNotNull(c, "Cell should not be null.");
        CTCell ctCell = c.getCTCell();
        assertEquals(cellRef, ctCell.getR());
        if (formulaText == null) {
            assertFalse(ctCell.isSetF());
            assertNull(ctCell.getF());
        } else {
            CTCellFormula f = ctCell.getF();
            assertEquals(arrayRangeRef, f.getRef());
            assertEquals(formulaText, f.getStringValue());
            assertEquals(STCellFormulaType.ARRAY, f.getT());
        }
    }

}
