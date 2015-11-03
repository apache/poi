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

import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import junit.framework.TestCase;

/**
 * Tests SheetUtil.
 *
 * @see org.apache.poi.ss.util.SheetUtil
 */
public final class TestSheetUtil extends TestCase {
    public void testCellWithMerges() throws Exception {
        Workbook wb = new HSSFWorkbook();
        Sheet s = wb.createSheet();
        
        // Create some test data
        Row r2 = s.createRow(1);
        r2.createCell(0).setCellValue(10);
        r2.createCell(1).setCellValue(11);
        Row r3 = s.createRow(2);
        r3.createCell(0).setCellValue(20);
        r3.createCell(1).setCellValue(21);
        
        s.addMergedRegion(new CellRangeAddress(2, 3, 0, 0));
        s.addMergedRegion(new CellRangeAddress(2, 2, 1, 4));
        
        // With a cell that isn't defined, we'll get null
        assertEquals(null, SheetUtil.getCellWithMerges(s, 0, 0));
        
        // With a cell that's not in a merged region, we'll get that
        assertEquals(10.0, SheetUtil.getCellWithMerges(s, 1, 0).getNumericCellValue());
        assertEquals(11.0, SheetUtil.getCellWithMerges(s, 1, 1).getNumericCellValue());
        
        // With a cell that's the primary one of a merged region, we get that cell
        assertEquals(20.0, SheetUtil.getCellWithMerges(s, 2, 0).getNumericCellValue());
        assertEquals(21., SheetUtil.getCellWithMerges(s, 2, 1).getNumericCellValue());
        
        // With a cell elsewhere in the merged region, get top-left
        assertEquals(20.0, SheetUtil.getCellWithMerges(s, 3, 0).getNumericCellValue());
        assertEquals(21.0, SheetUtil.getCellWithMerges(s, 2, 2).getNumericCellValue());
        assertEquals(21.0, SheetUtil.getCellWithMerges(s, 2, 3).getNumericCellValue());
        assertEquals(21.0, SheetUtil.getCellWithMerges(s, 2, 4).getNumericCellValue());
        
        wb.close();
    }
    
    public void testCanComputeWidthHSSF() throws IOException {
        Workbook wb = new HSSFWorkbook();
        
        // cannot check on result because on some machines we get back false here!
        SheetUtil.canComputeColumnWidth(wb.getFontAt((short)0));

        wb.close();        
    }

    public void testGetCellWidthEmpty() throws IOException {
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet("sheet");
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        
        // no contents: cell.setCellValue("sometext");
        
        assertEquals(-1.0, SheetUtil.getCellWidth(cell, 1, null, true));
        
        wb.close();
    }

    public void testGetCellWidthString() throws IOException {
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet("sheet");
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        
        cell.setCellValue("sometext");
        
        assertTrue(SheetUtil.getCellWidth(cell, 1, null, true) > 0);
        
        wb.close();
    }

    public void testGetCellWidthNumber() throws IOException {
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet("sheet");
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        
        cell.setCellValue(88.234);
        
        assertTrue(SheetUtil.getCellWidth(cell, 1, null, true) > 0);
        
        wb.close();
    }

    public void testGetCellWidthBoolean() throws IOException {
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet("sheet");
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        
        cell.setCellValue(false);
        
        assertTrue(SheetUtil.getCellWidth(cell, 1, null, false) > 0);
        
        wb.close();
    }

    public void testGetColumnWidthString() throws IOException {
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet("sheet");
        Row row = sheet.createRow(0);
        sheet.createRow(1);
        sheet.createRow(2);
        Cell cell = row.createCell(0);
        
        cell.setCellValue("sometext");
        
        assertTrue("Having some width for rows with actual cells", 
                SheetUtil.getColumnWidth(sheet, 0, true) > 0);
        assertEquals("Not having any widht for rows with all empty cells", 
                -1.0, SheetUtil.getColumnWidth(sheet, 0, true, 1, 2));
        
        wb.close();
    }
}
