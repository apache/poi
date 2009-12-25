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

import org.apache.poi.ss.usermodel.BaseTestArrayFormulas;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.XSSFITestDataProvider;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCellFormula;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STCellFormulaType;

/**
 * Test array formulas in XSSF
 *
 * @author Yegor Kozlov
 */
public final class TestXSSFArrayFormulas extends BaseTestArrayFormulas {

    @Override
    protected XSSFITestDataProvider getTestDataProvider(){
        return XSSFITestDataProvider.getInstance();
    }

    public void testXSSFSetArrayFormula_singleCell() {
        XSSFWorkbook workbook = getTestDataProvider().createWorkbook();
        XSSFSheet sheet = workbook.createSheet();

        // row 3 does not yet exist
        assertNull(sheet.getRow(2));
        CellRangeAddress range = new CellRangeAddress(2, 2, 2, 2);
        XSSFCell[] cells = sheet.setArrayFormula("SUM(C11:C12*D11:D12)", range);
        assertEquals(1, cells.length);

        // sheet.setArrayFormula creates rows and cells for the designated range
        assertNotNull(sheet.getRow(2));
        XSSFCell cell = sheet.getRow(2).getCell(2);
        assertNotNull(cell);

        assertTrue(cell.isPartOfArrayFormulaGroup());
        assertSame(cells[0], sheet.getFirstCellInArrayFormula(cells[0]));
        //retrieve the range and check it is the same
        assertEquals(range.formatAsString(), cell.getArrayFormulaRange().formatAsString());

        //check the CTCellFormula bean
        CTCellFormula f = cell.getCTCell().getF();
        assertEquals("SUM(C11:C12*D11:D12)", f.getStringValue());
        assertEquals("C3", f.getRef());
        assertEquals(STCellFormulaType.ARRAY, f.getT());

    }

    public void testXSSFSetArrayFormula_multiCell() {
        XSSFCell[] cells;

        XSSFWorkbook workbook = getTestDataProvider().createWorkbook();
        XSSFSheet sheet = workbook.createSheet();
        
        CellRangeAddress range = new CellRangeAddress(3, 5, 2, 2);
        assertEquals("C4:C6", range.formatAsString());
        cells = sheet.setArrayFormula("SUM(A1:A3*B1:B3)", range);
        assertEquals(3, cells.length);

        // sheet.setArrayFormula creates rows and cells for the designated range
        assertEquals("C4", cells[0].getCTCell().getR());
        assertEquals("C5", cells[1].getCTCell().getR());
        assertEquals("C6", cells[2].getCTCell().getR());
        assertSame(cells[0], sheet.getFirstCellInArrayFormula(cells[0]));

        /*
         * From the spec:
         * For a multi-cell formula, the c elements for all cells except the top-left
         * cell in that range shall not have an f element;
         */

        //the first cell has an f element
        CTCellFormula f = cells[0].getCTCell().getF();
        assertEquals("SUM(A1:A3*B1:B3)", f.getStringValue());
        assertEquals("C4:C6", f.getRef());
        assertEquals(STCellFormulaType.ARRAY, f.getT());
        //the other two cells don't have an f element
        assertNull(cells[1].getCTCell().getF());
        assertNull(cells[2].getCTCell().getF());
    }
}