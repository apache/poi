/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.ss.usermodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.poi.ss.ITestDataProvider;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
public abstract class BaseTestRangeCopier {
    protected Sheet sheet1;
    protected Sheet sheet2;
    protected Workbook workbook;
    protected RangeCopier rangeCopier;
    protected RangeCopier transSheetRangeCopier;
    protected ITestDataProvider testDataProvider;

    protected void initSheets() {
        sheet1 = workbook.getSheet("sheet1");
        sheet2 = workbook.getSheet("sheet2");
    }

    @Test
    void copySheetRangeWithoutFormulas() {
        CellRangeAddress rangeToCopy = CellRangeAddress.valueOf("B1:C2");   //2x2
        CellRangeAddress destRange = CellRangeAddress.valueOf("C2:D3");     //2x2
        rangeCopier.copyRange(rangeToCopy, destRange);
        assertEquals("1.1", sheet1.getRow(2).getCell(2).toString());
        assertEquals("2.1", sheet1.getRow(2).getCell(3).toString());
    }

    @Test
    void tileTheRangeAway() {
        CellRangeAddress tileRange = CellRangeAddress.valueOf("C4:D5");
        CellRangeAddress destRange = CellRangeAddress.valueOf("F4:K5");
        rangeCopier.copyRange(tileRange, destRange);
        assertEquals("1.3", getCellContent(sheet1, "H4"));
        assertEquals("1.3", getCellContent(sheet1, "J4"));
        assertEquals("$C1+G$2", getCellContent(sheet1, "G5"));
        assertEquals("SUM(G3:I3)", getCellContent(sheet1, "H5"));
        assertEquals("$C1+I$2", getCellContent(sheet1, "I5"));
        assertEquals("", getCellContent(sheet1, "L5"));  //out of borders
        assertEquals("", getCellContent(sheet1, "G7")); //out of borders
    }

    @Test
    void tileTheRangeOver() {
        CellRangeAddress tileRange = CellRangeAddress.valueOf("C4:D5");
        CellRangeAddress destRange = CellRangeAddress.valueOf("A4:C5");
        rangeCopier.copyRange(tileRange, destRange);
        assertEquals("1.3", getCellContent(sheet1, "A4"));
        assertEquals("$C1+B$2", getCellContent(sheet1, "B5"));
        assertEquals("SUM(B3:D3)", getCellContent(sheet1, "C5"));
    }

    @Test
    void copyRangeToOtherSheet() {
        Sheet destSheet = sheet2;
        CellRangeAddress tileRange = CellRangeAddress.valueOf("C4:D5"); // on sheet1
        CellRangeAddress destRange = CellRangeAddress.valueOf("F4:J6"); // on sheet2
        transSheetRangeCopier.copyRange(tileRange, destRange);
        assertEquals("1.3", getCellContent(destSheet, "H4"));
        assertEquals("1.3", getCellContent(destSheet, "J4"));
        assertEquals("$C1+G$2", getCellContent(destSheet, "G5"));
        assertEquals("SUM(G3:I3)", getCellContent(destSheet, "H5"));
        assertEquals("$C1+I$2", getCellContent(destSheet, "I5"));
    }

    @Test
    void testEmptyRow() {
        // leave some rows empty in-between
        Row row = sheet1.createRow(23);
        row.createCell(0).setCellValue(1.2);

        Sheet destSheet = sheet2;
        CellRangeAddress tileRange = CellRangeAddress.valueOf("A1:A100"); // on sheet1
        CellRangeAddress destRange = CellRangeAddress.valueOf("G1:G100"); // on sheet2
        transSheetRangeCopier.copyRange(tileRange, destRange);

        assertEquals("1.2", getCellContent(destSheet, "G24"));
    }

    @Test
    void testSameSheet() {
        // leave some rows empty in-between
        Row row = sheet1.createRow(23);
        row.createCell(0).setCellValue(1.2);

        CellRangeAddress tileRange = CellRangeAddress.valueOf("A1:A100"); // on sheet1
        CellRangeAddress destRange = CellRangeAddress.valueOf("G1:G100"); // on sheet2

        // use the a RangeCopier with the same Sheet for source and dest
        rangeCopier.copyRange(tileRange, destRange);

        assertEquals("1.2", getCellContent(sheet1, "G24"));
    }

    @Test
    void testCopyStyles() {
        String cellContent = "D6 aligned to the right";
        HorizontalAlignment toTheRight = HorizontalAlignment.RIGHT;
        // create cell with content aligned to the right
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(toTheRight);
        Cell cell = sheet1.createRow(5).createCell(3);
        cell.setCellValue(cellContent);
        cell.setCellStyle(style);

        Sheet destSheet = sheet2;
        CellRangeAddress tileRange = CellRangeAddress.valueOf("D6:D6"); // on sheet1
        CellRangeAddress destRange = CellRangeAddress.valueOf("J6:J6"); // on sheet2
        transSheetRangeCopier.copyRange(tileRange, destRange, true, false);
        assertEquals(cellContent, getCellContent(destSheet, "J6"));
        assertEquals(toTheRight, getCell(destSheet, "J6").getCellStyle().getAlignment());
    }

    @Test
    void testMergedRanges() {
        String cellContent = "D6 merged to E7";

        // create cell merged from D6 to E7
        CellRangeAddress mergedRangeAddress = new CellRangeAddress(5,6,3,4);
        Cell cell = sheet1.createRow(5).createCell(3);
        cell.setCellValue(cellContent);
        sheet1.addMergedRegion(mergedRangeAddress);

        Sheet destSheet = sheet2;
        CellRangeAddress tileRange = CellRangeAddress.valueOf("D6:E7"); // on sheet1
        transSheetRangeCopier.copyRange(tileRange, tileRange, false, true);
        assertEquals(cellContent, getCellContent(destSheet, "D6"));
        assertFalse(destSheet.getMergedRegions().isEmpty());
        destSheet.getMergedRegions().forEach((mergedRegion) -> {
            assertTrue(mergedRegion.equals(mergedRangeAddress));
        });
    }

   protected static String getCellContent(Sheet sheet, String coordinates) {
        Cell cell = getCell(sheet, coordinates);
        return cell == null ? "" : cell.toString();
   }

   protected static Cell getCell(Sheet sheet, String coordinates) {
        try {
            CellReference p = new CellReference(coordinates);
            return sheet.getRow(p.getRow()).getCell(p.getCol());
        }
        catch (NullPointerException e) { // row or cell does not exist
            return null;
        }
    }
}
