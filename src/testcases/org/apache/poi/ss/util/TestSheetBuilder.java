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

package org.apache.poi.ss.util;

import java.util.Date;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import junit.framework.TestCase;

/**
 * Tests SheetBuilder.
 *
 * @see org.apache.poi.ss.util.SheetBuilder
 */
public final class TestSheetBuilder extends TestCase {

    private static Object[][] testData = new Object[][]{
            {1, 2, 3},
            {new Date(), null, null},
            {"one", "two", "=A1+B2"}
    };

    public void testNotCreateEmptyCells() {
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = new SheetBuilder(wb, testData).build();

        assertEquals(sheet.getPhysicalNumberOfRows(), 3);

        Row firstRow = sheet.getRow(0);
        Cell firstCell = firstRow.getCell(0);

        assertEquals(firstCell.getCellType(), CellType.NUMERIC);
        assertEquals(1.0, firstCell.getNumericCellValue(), 0.00001);


        Row secondRow = sheet.getRow(1);
        assertNotNull(secondRow.getCell(0));
        assertNull(secondRow.getCell(2));

        Row thirdRow = sheet.getRow(2);
        assertEquals(CellType.STRING, thirdRow.getCell(0).getCellType());
        String cellValue = thirdRow.getCell(0).getStringCellValue();
        assertEquals(testData[2][0].toString(), cellValue);

        assertEquals(CellType.FORMULA, thirdRow.getCell(2).getCellType());
        assertEquals("A1+B2", thirdRow.getCell(2).getCellFormula());
    }

    public void testEmptyCells() {
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = new SheetBuilder(wb, testData).setCreateEmptyCells(true).build();

        Cell emptyCell = sheet.getRow(1).getCell(1);
        assertNotNull(emptyCell);
        assertEquals(CellType.BLANK, emptyCell.getCellType());
    }

    public void testSheetName() {
        final String sheetName = "TEST SHEET NAME";
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = new SheetBuilder(wb, testData).setSheetName(sheetName).build();
        assertEquals(sheetName, sheet.getSheetName());
    }
}