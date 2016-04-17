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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Test;

/**
 * Tests Spreadsheet CellUtil
 *
 * @see org.apache.poi.ss.util.CellUtil
 */
public final class TestCellUtil {
    @Test
    public void setCellStyleProperty() throws IOException {
        Workbook wb = new HSSFWorkbook();
        Sheet s = wb.createSheet();
        Row r = s.createRow(0);
        Cell c = r.createCell(0);

        // Add a border should create a new style
        int styCnt1 = wb.getNumCellStyles();
        CellUtil.setCellStyleProperty(c, CellUtil.BORDER_BOTTOM, BorderStyle.THIN);
        int styCnt2 = wb.getNumCellStyles();
        assertEquals(styCnt2, styCnt1+1);

        // Add same border to another cell, should not create another style
        c = r.createCell(1);
        CellUtil.setCellStyleProperty(c, CellUtil.BORDER_BOTTOM, BorderStyle.THIN);
        int styCnt3 = wb.getNumCellStyles();
        assertEquals(styCnt3, styCnt2);

        wb.close();
    }

    @Test
    public void setCellStyleProperties() throws IOException {
        Workbook wb = new HSSFWorkbook();
        Sheet s = wb.createSheet();
        Row r = s.createRow(0);
        Cell c = r.createCell(0);

        // Add multiple border properties to cell should create a single new style
        int styCnt1 = wb.getNumCellStyles();
        Map<String, Object> props = new HashMap<String, Object>();
        props.put(CellUtil.BORDER_TOP, BorderStyle.THIN);
        props.put(CellUtil.BORDER_BOTTOM, BorderStyle.THIN);
        props.put(CellUtil.BORDER_LEFT, BorderStyle.THIN);
        props.put(CellUtil.BORDER_RIGHT, BorderStyle.THIN);
        CellUtil.setCellStyleProperties(c, props);
        int styCnt2 = wb.getNumCellStyles();
        assertEquals("Only one additional style should have been created", styCnt1 + 1, styCnt2);

        // Add same border another to same cell, should not create another style
        c = r.createCell(1);
        CellUtil.setCellStyleProperties(c, props);
        int styCnt3 = wb.getNumCellStyles();
        assertEquals(styCnt2, styCnt3);

        wb.close();
    }

    @Test
    public void getRow() throws IOException {
        Workbook wb = new HSSFWorkbook();
        Sheet sh = wb.createSheet();
        Row row1 = sh.createRow(0);
        
        // Get row that already exists
        Row r1 = CellUtil.getRow(0, sh);
        assertNotNull(r1);
        assertSame("An existing row should not be recreated", row1, r1);

        // Get row that does not exist yet
        assertNotNull(CellUtil.getRow(1, sh));

        wb.close();
    }

    @Test
    public void getCell() throws IOException {
        Workbook wb = new HSSFWorkbook();
        Sheet sh = wb.createSheet();
        Row row = sh.createRow(0);
        Cell A1 = row.createCell(0);

        // Get cell that already exists
        Cell a1 = CellUtil.getCell(row, 0);
        assertNotNull(a1);
        assertSame("An existing cell should not be recreated", A1, a1);

        // Get cell that does not exist yet
        assertNotNull(CellUtil.getCell(row, 1));

        wb.close();
    }

    @Test
    public void createCell() throws IOException {
        Workbook wb = new HSSFWorkbook();
        Sheet sh = wb.createSheet();
        Row row = sh.createRow(0);

        CellStyle style = wb.createCellStyle();
        style.setWrapText(true);

        // calling createCell on a non-existing cell should create a cell and set the cell value and style.
        Cell F1 = CellUtil.createCell(row, 5, "Cell Value", style);

        assertSame(row.getCell(5), F1);
        assertEquals("Cell Value", F1.getStringCellValue());
        assertEquals(style, F1.getCellStyle());
        // should be assertSame, but a new HSSFCellStyle is returned for each getCellStyle() call.
        // HSSFCellStyle wraps an underlying style record, and the underlying
        // style record is the same between multiple getCellStyle() calls.

        // calling createCell on an existing cell should return the existing cell and modify the cell value and style.
        Cell f1 = CellUtil.createCell(row, 5, "Overwritten cell value", null);
        assertSame(row.getCell(5), f1);
        assertSame(F1, f1);
        assertEquals("Overwritten cell value", f1.getStringCellValue());
        assertEquals("Overwritten cell value", F1.getStringCellValue());
        assertEquals("cell style should be unchanged with createCell(..., null)", style, f1.getCellStyle());
        assertEquals("cell style should be unchanged with createCell(..., null)", style, F1.getCellStyle());

        // test createCell(row, column, value) (no CellStyle)
        f1 = CellUtil.createCell(row, 5, "Overwritten cell with default style");
        assertSame(F1, f1);

        wb.close();

    }

    @Test
    public void setAlignment() throws IOException {
        Workbook wb = new HSSFWorkbook();
        Sheet sh = wb.createSheet();
        Row row = sh.createRow(0);
        Cell A1 = row.createCell(0);
        Cell B1 = row.createCell(1);

        // Assumptions
        assertEquals(A1.getCellStyle(), B1.getCellStyle());
        // should be assertSame, but a new HSSFCellStyle is returned for each getCellStyle() call. 
        // HSSFCellStyle wraps an underlying style record, and the underlying
        // style record is the same between multiple getCellStyle() calls.
        assertEquals(CellStyle.ALIGN_GENERAL, A1.getCellStyle().getAlignment());
        assertEquals(CellStyle.ALIGN_GENERAL, B1.getCellStyle().getAlignment());

        // get/set alignment modifies the cell's style
        CellUtil.setAlignment(A1, CellStyle.ALIGN_RIGHT);
        assertEquals(CellStyle.ALIGN_RIGHT, A1.getCellStyle().getAlignment());

        // get/set alignment doesn't affect the style of cells with
        // the same style prior to modifying the style
        assertNotEquals(A1.getCellStyle(), B1.getCellStyle());
        assertEquals(CellStyle.ALIGN_GENERAL, B1.getCellStyle().getAlignment());

        wb.close();
    }

    @Test
    public void setFont() throws IOException {
        Workbook wb = new HSSFWorkbook();
        Sheet sh = wb.createSheet();
        Row row = sh.createRow(0);
        Cell A1 = row.createCell(0);
        Cell B1 = row.createCell(1);
        final short defaultFontIndex = 0;
        Font font = wb.createFont();
        font.setItalic(true);
        final short customFontIndex = font.getIndex();

        // Assumptions
        assertNotEquals(defaultFontIndex, customFontIndex);
        assertEquals(A1.getCellStyle(), B1.getCellStyle());
        // should be assertSame, but a new HSSFCellStyle is returned for each getCellStyle() call. 
        // HSSFCellStyle wraps an underlying style record, and the underlying
        // style record is the same between multiple getCellStyle() calls.
        assertEquals(defaultFontIndex, A1.getCellStyle().getFontIndex());
        assertEquals(defaultFontIndex, B1.getCellStyle().getFontIndex());

        // get/set alignment modifies the cell's style
        CellUtil.setFont(A1, font);
        assertEquals(customFontIndex, A1.getCellStyle().getFontIndex());

        // get/set alignment doesn't affect the style of cells with
        // the same style prior to modifying the style
        assertNotEquals(A1.getCellStyle(), B1.getCellStyle());
        assertEquals(defaultFontIndex, B1.getCellStyle().getFontIndex());

        wb.close();
    }

    @Test
    public void setFontFromDifferentWorkbook() throws IOException {
        Workbook wb1 = new HSSFWorkbook();
        Workbook wb2 = new HSSFWorkbook();
        Font font1 = wb1.createFont();
        Font font2 = wb2.createFont();
        // do something to make font1 and font2 different
        // so they are not same or equal.
        font1.setItalic(true);
        Cell A1 = wb1.createSheet().createRow(0).createCell(0);
        
        // okay
        CellUtil.setFont(A1, font1);

        // font belongs to different workbook
        try {
            CellUtil.setFont(A1, font2);
            fail("setFont not allowed if font belongs to a different workbook");
        } catch (final IllegalArgumentException e) {
            if (e.getMessage().startsWith("Font does not belong to this workbook")) {
                // expected
            }
            else {
                throw e;
            }
        } finally {
            wb1.close();
            wb2.close();
        }
    }
}
