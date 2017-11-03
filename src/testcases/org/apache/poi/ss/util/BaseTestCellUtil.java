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

import org.apache.poi.ss.ITestDataProvider;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Test;

/**
 * Tests Spreadsheet CellUtil
 *
 * @see org.apache.poi.ss.util.CellUtil
 */
public class BaseTestCellUtil {
    protected final ITestDataProvider _testDataProvider;

    protected BaseTestCellUtil(ITestDataProvider testDataProvider) {
        _testDataProvider = testDataProvider;
    }
    
    @Test
    public void setCellStyleProperty() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet s = wb.createSheet();
        Row r = s.createRow(0);
        Cell c = r.createCell(0);

        // Add a border should create a new style
        int styCnt1 = wb.getNumCellStyles();
        CellUtil.setCellStyleProperty(c, CellUtil.BORDER_BOTTOM, BorderStyle.THIN);
        int styCnt2 = wb.getNumCellStyles();
        assertEquals(styCnt1+1, styCnt2);

        // Add same border to another cell, should not create another style
        c = r.createCell(1);
        CellUtil.setCellStyleProperty(c, CellUtil.BORDER_BOTTOM, BorderStyle.THIN);
        int styCnt3 = wb.getNumCellStyles();
        assertEquals(styCnt2, styCnt3);

        wb.close();
    }
    
    @Test(expected=RuntimeException.class)
    public void setCellStylePropertyWithInvalidValue() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Sheet s = wb.createSheet();
            Row r = s.createRow(0);
            Cell c = r.createCell(0);

            // An invalid BorderStyle constant
            CellUtil.setCellStyleProperty(c, CellUtil.BORDER_BOTTOM, 42);
        }
    }
    
    @Test()
    public void setCellStylePropertyBorderWithShortAndEnum() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet s = wb.createSheet();
        Row r = s.createRow(0);
        Cell c = r.createCell(0);

        // A valid BorderStyle constant, as a Short
        CellUtil.setCellStyleProperty(c, CellUtil.BORDER_BOTTOM, BorderStyle.DASH_DOT.getCode());
        assertEquals(BorderStyle.DASH_DOT, c.getCellStyle().getBorderBottom());
        
        // A valid BorderStyle constant, as an Enum
        CellUtil.setCellStyleProperty(c, CellUtil.BORDER_TOP, BorderStyle.MEDIUM_DASH_DOT);
        assertEquals(BorderStyle.MEDIUM_DASH_DOT, c.getCellStyle().getBorderTop());
        
        wb.close();
    }

    @Test
    public void setCellStyleProperties() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet s = wb.createSheet();
        Row r = s.createRow(0);
        Cell c = r.createCell(0);

        // Add multiple border properties to cell should create a single new style
        int styCnt1 = wb.getNumCellStyles();
        Map<String, Object> props = new HashMap<>();
        props.put(CellUtil.BORDER_TOP, BorderStyle.THIN);
        props.put(CellUtil.BORDER_BOTTOM, BorderStyle.THIN);
        props.put(CellUtil.BORDER_LEFT, BorderStyle.THIN);
        props.put(CellUtil.BORDER_RIGHT, BorderStyle.THIN);
        props.put(CellUtil.ALIGNMENT, HorizontalAlignment.CENTER.getCode()); // try it both with a Short (deprecated)
        props.put(CellUtil.VERTICAL_ALIGNMENT, VerticalAlignment.CENTER); // and with an enum
        CellUtil.setCellStyleProperties(c, props);
        int styCnt2 = wb.getNumCellStyles();
        assertEquals("Only one additional style should have been created", styCnt1 + 1, styCnt2);

        // Add same border another to same cell, should not create another style
        c = r.createCell(1);
        CellUtil.setCellStyleProperties(c, props);
        int styCnt3 = wb.getNumCellStyles();
        assertEquals("No additional styles should have been created", styCnt2, styCnt3);

        wb.close();
        
    }

    @Test
    public void getRow() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();
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
        Workbook wb = _testDataProvider.createWorkbook();
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
        Workbook wb = _testDataProvider.createWorkbook();
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

    /**
     * @deprecated by {@link #setAlignmentEnum()}
     */
    @Deprecated
    @SuppressWarnings("deprecated")
    @Test
    public void setAlignment() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet sh = wb.createSheet();
        Row row = sh.createRow(0);
        Cell A1 = row.createCell(0);
        Cell B1 = row.createCell(1);

        // Assumptions
        assertEquals(A1.getCellStyle(), B1.getCellStyle());
        // should be assertSame, but a new HSSFCellStyle is returned for each getCellStyle() call. 
        // HSSFCellStyle wraps an underlying style record, and the underlying
        // style record is the same between multiple getCellStyle() calls.
        assertEquals(HorizontalAlignment.GENERAL, A1.getCellStyle().getAlignment());
        assertEquals(HorizontalAlignment.GENERAL, B1.getCellStyle().getAlignment());

        // get/set alignment modifies the cell's style
        CellUtil.setAlignment(A1, HorizontalAlignment.RIGHT);
        assertEquals(HorizontalAlignment.RIGHT, A1.getCellStyle().getAlignment());

        // get/set alignment doesn't affect the style of cells with
        // the same style prior to modifying the style
        assertNotEquals(A1.getCellStyle(), B1.getCellStyle());
        assertEquals(HorizontalAlignment.GENERAL, B1.getCellStyle().getAlignment());

        wb.close();
    }
    
    @Test
    public void setAlignmentEnum() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet sh = wb.createSheet();
        Row row = sh.createRow(0);
        Cell A1 = row.createCell(0);
        Cell B1 = row.createCell(1);

        // Assumptions
        assertEquals(A1.getCellStyle(), B1.getCellStyle());
        // should be assertSame, but a new HSSFCellStyle is returned for each getCellStyle() call. 
        // HSSFCellStyle wraps an underlying style record, and the underlying
        // style record is the same between multiple getCellStyle() calls.
        assertEquals(HorizontalAlignment.GENERAL, A1.getCellStyle().getAlignment());
        assertEquals(HorizontalAlignment.GENERAL, B1.getCellStyle().getAlignment());

        // get/set alignment modifies the cell's style
        CellUtil.setAlignment(A1, HorizontalAlignment.RIGHT);
        assertEquals(HorizontalAlignment.RIGHT, A1.getCellStyle().getAlignment());

        // get/set alignment doesn't affect the style of cells with
        // the same style prior to modifying the style
        assertNotEquals(A1.getCellStyle(), B1.getCellStyle());
        assertEquals(HorizontalAlignment.GENERAL, B1.getCellStyle().getAlignment());

        wb.close();
    }
    
    @Test
    public void setVerticalAlignmentEnum() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet sh = wb.createSheet();
        Row row = sh.createRow(0);
        Cell A1 = row.createCell(0);
        Cell B1 = row.createCell(1);

        // Assumptions
        assertEquals(A1.getCellStyle(), B1.getCellStyle());
        // should be assertSame, but a new HSSFCellStyle is returned for each getCellStyle() call. 
        // HSSFCellStyle wraps an underlying style record, and the underlying
        // style record is the same between multiple getCellStyle() calls.
        assertEquals(VerticalAlignment.BOTTOM, A1.getCellStyle().getVerticalAlignment());
        assertEquals(VerticalAlignment.BOTTOM, B1.getCellStyle().getVerticalAlignment());

        // get/set alignment modifies the cell's style
        CellUtil.setVerticalAlignment(A1, VerticalAlignment.TOP);
        assertEquals(VerticalAlignment.TOP, A1.getCellStyle().getVerticalAlignment());

        // get/set alignment doesn't affect the style of cells with
        // the same style prior to modifying the style
        assertNotEquals(A1.getCellStyle(), B1.getCellStyle());
        assertEquals(VerticalAlignment.BOTTOM, B1.getCellStyle().getVerticalAlignment());

        wb.close();
    }

    @Test
    public void setFont() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();
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
        Workbook wb1 = _testDataProvider.createWorkbook();
        Workbook wb2 = _testDataProvider.createWorkbook();
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
            // one specific message is expected
            if (!e.getMessage().startsWith("Font does not belong to this workbook")) {
                throw e;
            }
        } finally {
            wb1.close();
            wb2.close();
        }
    }

    /**
     * bug 55555
     * @since POI 3.15 beta 3
     */
    @Test
    public void setFillForegroundColorBeforeFillBackgroundColorEnum() throws IOException {
        Workbook wb1 = _testDataProvider.createWorkbook();
        Cell A1 = wb1.createSheet().createRow(0).createCell(0);
        Map<String, Object> properties = new HashMap<>();
        properties.put(CellUtil.FILL_PATTERN, FillPatternType.BRICKS);
        properties.put(CellUtil.FILL_FOREGROUND_COLOR, IndexedColors.BLUE.index);
        properties.put(CellUtil.FILL_BACKGROUND_COLOR, IndexedColors.RED.index);
        
        CellUtil.setCellStyleProperties(A1, properties);
        CellStyle style = A1.getCellStyle();
        assertEquals("fill pattern", FillPatternType.BRICKS, style.getFillPattern());
        assertEquals("fill foreground color", IndexedColors.BLUE, IndexedColors.fromInt(style.getFillForegroundColor()));
        assertEquals("fill background color", IndexedColors.RED, IndexedColors.fromInt(style.getFillBackgroundColor()));

        wb1.close();
    }
}
