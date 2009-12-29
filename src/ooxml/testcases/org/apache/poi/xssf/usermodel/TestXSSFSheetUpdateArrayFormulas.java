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

import junit.framework.AssertionFailedError;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.XSSFITestDataProvider;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCell;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCellFormula;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STCellFormulaType;
/**
 * Test array formulas in XSSF
 *
 * @author Yegor Kozlov
 * @author Josh Micich
 */
public final class TestXSSFSheetUpdateArrayFormulas extends BaseTestSheetUpdateArrayFormulas {

    public TestXSSFSheetUpdateArrayFormulas() {
        super(XSSFITestDataProvider.instance);
    }

    // Test methods common with HSSF are in superclass
    // Local methods here test XSSF-specific details of updating array formulas

    public void testXSSFSetArrayFormula_singleCell() {
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
    }

    public void testXSSFSetArrayFormula_multiCell() {
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
    }

    private static void confirmArrayFormulaCell(XSSFCell c, String cellRef) {
        confirmArrayFormulaCell(c, cellRef, null, null);
    }
    private static void confirmArrayFormulaCell(XSSFCell c, String cellRef, String formulaText, String arrayRangeRef) {
        if (c == null) {
            throw new AssertionFailedError("Cell should not be null.");
        }
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
