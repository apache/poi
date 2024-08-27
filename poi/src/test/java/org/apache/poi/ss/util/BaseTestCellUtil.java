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

import org.apache.poi.ss.ITestDataProvider;
import org.apache.poi.ss.usermodel.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests Spreadsheet CellUtil
 *
 * @see org.apache.poi.ss.util.CellUtil
 */
public abstract class BaseTestCellUtil {
    protected final ITestDataProvider _testDataProvider;

    protected BaseTestCellUtil(ITestDataProvider testDataProvider) {
        _testDataProvider = testDataProvider;
    }

    @Test
    void setCellStylePropertyByEnum() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Sheet s = wb.createSheet();
            Row r = s.createRow(0);
            Cell c = r.createCell(0);

            // Add a border should create a new style
            int styCnt1 = wb.getNumCellStyles();
            CellUtil.setCellStyleProperty(c, CellPropertyType.BORDER_BOTTOM, BorderStyle.THIN);
            int styCnt2 = wb.getNumCellStyles();
            assertEquals(styCnt1 + 1, styCnt2);

            // Add same border to another cell, should not create another style
            c = r.createCell(1);
            CellUtil.setCellStyleProperty(c, CellPropertyType.BORDER_BOTTOM, BorderStyle.THIN);
            int styCnt3 = wb.getNumCellStyles();
            assertEquals(styCnt2, styCnt3);
        }
    }

    @Test
    void setCellStyleProperty() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Sheet s = wb.createSheet();
            Row r = s.createRow(0);
            Cell c = r.createCell(0);

            // Add a border should create a new style
            int styCnt1 = wb.getNumCellStyles();
            CellUtil.setCellStyleProperty(c, CellUtil.BORDER_BOTTOM, BorderStyle.THIN);
            int styCnt2 = wb.getNumCellStyles();
            assertEquals(styCnt1 + 1, styCnt2);

            // Add same border to another cell, should not create another style
            c = r.createCell(1);
            CellUtil.setCellStyleProperty(c, CellUtil.BORDER_BOTTOM, BorderStyle.THIN);
            int styCnt3 = wb.getNumCellStyles();
            assertEquals(styCnt2, styCnt3);
        }
    }

    @Test
    void setCellStylePropertyWithInvalidValueByEnum() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Sheet s = wb.createSheet();
            Row r = s.createRow(0);
            Cell c = r.createCell(0);

            // An invalid BorderStyle constant
            assertThrows(RuntimeException.class, () -> CellUtil.setCellStyleProperty(c, CellPropertyType.BORDER_BOTTOM, 42));
        }
    }


    @Test
    void setCellStylePropertyWithInvalidValue() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Sheet s = wb.createSheet();
            Row r = s.createRow(0);
            Cell c = r.createCell(0);

            // An invalid BorderStyle constant
            assertThrows(RuntimeException.class, () -> CellUtil.setCellStyleProperty(c, CellUtil.BORDER_BOTTOM, 42));
        }
    }

    @Test()
    void setCellStylePropertyBorderWithShortAndEnum() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Sheet s = wb.createSheet();
            Row r = s.createRow(0);
            Cell c = r.createCell(0);

            // A valid BorderStyle constant, as a Short
            CellUtil.setCellStyleProperty(c, CellUtil.BORDER_BOTTOM, BorderStyle.DASH_DOT.getCode());
            assertEquals(BorderStyle.DASH_DOT, c.getCellStyle().getBorderBottom());

            // A valid BorderStyle constant, as an Enum
            CellUtil.setCellStyleProperty(c, CellUtil.BORDER_TOP, BorderStyle.MEDIUM_DASH_DOT);
            assertEquals(BorderStyle.MEDIUM_DASH_DOT, c.getCellStyle().getBorderTop());
        }
    }

    @Test()
    void setCellStylePropertyBorderWithShortAndEnumByEnum() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Sheet s = wb.createSheet();
            Row r = s.createRow(0);
            Cell c = r.createCell(0);

            // A valid BorderStyle constant, as a Short
            CellUtil.setCellStyleProperty(c, CellPropertyType.BORDER_BOTTOM, BorderStyle.DASH_DOT.getCode());
            assertEquals(BorderStyle.DASH_DOT, c.getCellStyle().getBorderBottom());

            // A valid BorderStyle constant, as an Enum
            CellUtil.setCellStyleProperty(c, CellPropertyType.BORDER_TOP, BorderStyle.MEDIUM_DASH_DOT);
            assertEquals(BorderStyle.MEDIUM_DASH_DOT, c.getCellStyle().getBorderTop());
        }
    }

    @Test()
    void setCellStylePropertyWithShrinkToFitByEnum() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Sheet s = wb.createSheet();
            Row r = s.createRow(0);
            Cell c = r.createCell(0);

            // Assert that the default shrinkToFit is false
            assertFalse(c.getCellStyle().getShrinkToFit());

            // Set shrinkToFit to true
            CellUtil.setCellStyleProperty(c, CellPropertyType.SHRINK_TO_FIT, true);
            assertTrue(c.getCellStyle().getShrinkToFit());
        }
    }

    @Test()
    void setCellStylePropertyWithShrinkToFit() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Sheet s = wb.createSheet();
            Row r = s.createRow(0);
            Cell c = r.createCell(0);

            // Assert that the default shrinkToFit is false
            assertFalse(c.getCellStyle().getShrinkToFit());

            // Set shrinkToFit to true
            CellUtil.setCellStyleProperty(c, CellUtil.SHRINK_TO_FIT, true);
            assertTrue(c.getCellStyle().getShrinkToFit());
        }
    }

    @Test()
    void setCellStylePropertyWithQuotePrefixedByEnum() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Sheet s = wb.createSheet();
            Row r = s.createRow(0);
            Cell c = r.createCell(0);

            // Assert that the default quotePrefixed is false
            assertFalse(c.getCellStyle().getQuotePrefixed());

            // Set quotePrefixed to true
            CellUtil.setCellStyleProperty(c, CellPropertyType.QUOTE_PREFIXED, true);
            assertTrue(c.getCellStyle().getQuotePrefixed());
        }
    }

    @Test()
    void setCellStylePropertyWithQuotePrefixed() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Sheet s = wb.createSheet();
            Row r = s.createRow(0);
            Cell c = r.createCell(0);

            // Assert that the default quotePrefixed is false
            assertFalse(c.getCellStyle().getQuotePrefixed());

            // Set quotePrefixed to true
            CellUtil.setCellStyleProperty(c, CellUtil.QUOTE_PREFIXED, true);
            assertTrue(c.getCellStyle().getQuotePrefixed());
        }
    }

    @Test()
    void setCellStylePropertyWithExistingStyles() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Sheet s = wb.createSheet();
            Row r = s.createRow(0);
            Cell c = r.createCell(0);
            Cell c2 = r.createCell(1);
            Font f = wb.createFont();
            f.setBold(true);

            // Assert that the default cell style is not the same as the one being changed
            assertNotEquals(HorizontalAlignment.CENTER, c.getCellStyle().getAlignment());
            assertNotEquals(BorderStyle.THIN, c.getCellStyle().getBorderBottom());
            assertNotEquals(BorderStyle.THIN, c.getCellStyle().getBorderLeft());
            assertNotEquals(BorderStyle.THIN, c.getCellStyle().getBorderRight());
            assertNotEquals(BorderStyle.THIN, c.getCellStyle().getBorderTop());
            assertNotEquals(IndexedColors.RED.index, c.getCellStyle().getBottomBorderColor());
            assertNotEquals(IndexedColors.RED.index, c.getCellStyle().getLeftBorderColor());
            assertNotEquals(IndexedColors.RED.index, c.getCellStyle().getRightBorderColor());
            assertNotEquals(IndexedColors.RED.index, c.getCellStyle().getTopBorderColor());
            assertNotEquals(wb.createDataFormat().getFormat("#,##0"), c.getCellStyle().getDataFormat());
            assertNotEquals(IndexedColors.BLUE.index, c.getCellStyle().getFillForegroundColor());
            assertNotEquals(IndexedColors.BLUE.index, c.getCellStyle().getFillBackgroundColor());
            assertNotEquals(FillPatternType.DIAMONDS, c.getCellStyle().getFillPattern());
            assertNotEquals(f.getIndex(), c.getCellStyle().getFontIndex());
            assertFalse(c.getCellStyle().getHidden());
            assertNotEquals((short) 3, c.getCellStyle().getIndention());
            assertTrue(c.getCellStyle().getLocked());
            assertNotEquals((short) 45, c.getCellStyle().getRotation());
            assertNotEquals(VerticalAlignment.CENTER, c.getCellStyle().getVerticalAlignment());
            assertFalse(c.getCellStyle().getWrapText());
            assertFalse(c.getCellStyle().getShrinkToFit());
            assertFalse(c.getCellStyle().getQuotePrefixed());

            // Set all styles
            CellStyle cs = wb.createCellStyle();
            cs.setAlignment(HorizontalAlignment.CENTER);
            cs.setBorderBottom(BorderStyle.THIN);
            cs.setBorderLeft(BorderStyle.THIN);
            cs.setBorderRight(BorderStyle.THIN);
            cs.setBorderTop(BorderStyle.THIN);
            cs.setBottomBorderColor(IndexedColors.RED.index);
            cs.setLeftBorderColor(IndexedColors.RED.index);
            cs.setRightBorderColor(IndexedColors.RED.index);
            cs.setTopBorderColor(IndexedColors.RED.index);
            cs.setDataFormat(wb.createDataFormat().getFormat("#,##0"));
            cs.setFillForegroundColor(IndexedColors.BLUE.index);
            cs.setFillBackgroundColor(IndexedColors.BLUE.index);
            cs.setFillPattern(FillPatternType.DIAMONDS);
            cs.setFont(f);
            cs.setHidden(true);
            cs.setIndention((short) 3);
            cs.setLocked(false);
            cs.setRotation((short) 45);
            cs.setVerticalAlignment(VerticalAlignment.CENTER);
            cs.setWrapText(true);
            cs.setShrinkToFit(true);
            cs.setQuotePrefixed(true);
            c.setCellStyle(cs);
            c2.setCellStyle(cs);

            // Set BorderBottom from THIN to DOUBLE with setCellStyleProperty()
            CellUtil.setCellStyleProperty(c, CellPropertyType.BORDER_BOTTOM, BorderStyle.DOUBLE);
            CellUtil.setCellStyleProperty(c2, CellUtil.BORDER_BOTTOM, BorderStyle.DOUBLE);

            // Assert that only BorderBottom has been changed and no others.
            assertEquals(BorderStyle.DOUBLE, c.getCellStyle().getBorderBottom());
            assertEquals(BorderStyle.DOUBLE, c2.getCellStyle().getBorderBottom());
            assertEquals(HorizontalAlignment.CENTER, c.getCellStyle().getAlignment());
            assertEquals(BorderStyle.THIN, c.getCellStyle().getBorderLeft());
            assertEquals(BorderStyle.THIN, c.getCellStyle().getBorderRight());
            assertEquals(BorderStyle.THIN, c.getCellStyle().getBorderTop());
            assertEquals(IndexedColors.RED.index, c.getCellStyle().getBottomBorderColor());
            assertEquals(IndexedColors.RED.index, c.getCellStyle().getLeftBorderColor());
            assertEquals(IndexedColors.RED.index, c.getCellStyle().getRightBorderColor());
            assertEquals(IndexedColors.RED.index, c.getCellStyle().getTopBorderColor());
            assertEquals(wb.createDataFormat().getFormat("#,##0"), c.getCellStyle().getDataFormat());
            assertEquals(IndexedColors.BLUE.index, c.getCellStyle().getFillForegroundColor());
            assertEquals(IndexedColors.BLUE.index, c.getCellStyle().getFillBackgroundColor());
            assertEquals(FillPatternType.DIAMONDS, c.getCellStyle().getFillPattern());
            assertEquals(f.getIndex(), c.getCellStyle().getFontIndex());
            assertTrue(c.getCellStyle().getHidden());
            assertEquals((short) 3, c.getCellStyle().getIndention());
            assertFalse(c.getCellStyle().getLocked());
            assertEquals((short) 45, c.getCellStyle().getRotation());
            assertEquals(VerticalAlignment.CENTER, c.getCellStyle().getVerticalAlignment());
            assertTrue(c.getCellStyle().getWrapText());
            assertTrue(c.getCellStyle().getShrinkToFit());
            assertTrue(c.getCellStyle().getQuotePrefixed());
        }
    }

    @Test
    void setCellStyleProperties() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
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
            assertEquals(styCnt1 + 1, styCnt2, "Only one additional style should have been created");

            // Add same border another to same cell, should not create another style
            c = r.createCell(1);
            CellUtil.setCellStyleProperties(c, props);
            int styCnt3 = wb.getNumCellStyles();
            assertEquals(styCnt2, styCnt3, "No additional styles should have been created");
        }
    }

    @Test
    void setCellStylePropertiesEnum() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Sheet s = wb.createSheet();
            Row r = s.createRow(0);
            Cell c = r.createCell(0);

            // Add multiple border properties to cell should create a single new style
            int styCnt1 = wb.getNumCellStyles();
            Map<CellPropertyType, Object> props = new HashMap<>();
            props.put(CellPropertyType.BORDER_TOP, BorderStyle.THIN);
            props.put(CellPropertyType.BORDER_BOTTOM, BorderStyle.THIN);
            props.put(CellPropertyType.BORDER_LEFT, BorderStyle.THIN);
            props.put(CellPropertyType.BORDER_RIGHT, BorderStyle.THIN);
            props.put(CellPropertyType.ALIGNMENT, HorizontalAlignment.CENTER.getCode()); // try it both with a Short (deprecated)
            props.put(CellPropertyType.VERTICAL_ALIGNMENT, VerticalAlignment.CENTER); // and with an enum
            CellUtil.setCellStylePropertiesEnum(c, props);
            int styCnt2 = wb.getNumCellStyles();
            assertEquals(styCnt1 + 1, styCnt2, "Only one additional style should have been created");

            // Add same border another to same cell, should not create another style
            c = r.createCell(1);
            CellUtil.setCellStylePropertiesEnum(c, props);
            int styCnt3 = wb.getNumCellStyles();
            assertEquals(styCnt2, styCnt3, "No additional styles should have been created");
        }
    }

    @Test
    void getRow() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Sheet sh = wb.createSheet();
            Row row1 = sh.createRow(0);

            // Get row that already exists
            Row r1 = CellUtil.getRow(0, sh);
            assertNotNull(r1);
            assertSame(row1, r1, "An existing row should not be recreated");

            // Get row that does not exist yet
            assertNotNull(CellUtil.getRow(1, sh));
        }
    }

    @Test
    void getCell() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Sheet sh = wb.createSheet();
            Row row = sh.createRow(0);
            Cell A1 = row.createCell(0);

            // Get cell that already exists
            Cell a1 = CellUtil.getCell(row, 0);
            assertNotNull(a1);
            assertSame(A1, a1, "An existing cell should not be recreated");

            // Get cell that does not exist yet
            assertNotNull(CellUtil.getCell(row, 1));
        }
    }

    @Test
    void createCell() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
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
            assertEquals(style, f1.getCellStyle(), "cell style should be unchanged with createCell(..., null)");
            assertEquals(style, F1.getCellStyle(), "cell style should be unchanged with createCell(..., null)");

            // test createCell(row, column, value) (no CellStyle)
            f1 = CellUtil.createCell(row, 5, "Overwritten cell with default style");
            assertSame(F1, f1);
        }
    }

    /**
     * @deprecated by {@link #setAlignmentEnum()}
     */
    @Deprecated
    @SuppressWarnings("deprecated")
    @Test
    void setAlignment() throws IOException {
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
    void setAlignmentEnum() throws IOException {
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
    void setVerticalAlignmentEnum() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
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
        }
    }

    @Test
    void setFont() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Sheet sh = wb.createSheet();
            Row row = sh.createRow(0);
            Cell A1 = row.createCell(0);
            Cell B1 = row.createCell(1);
            final int defaultFontIndex = 0;
            Font font = wb.createFont();
            font.setItalic(true);
            final int customFontIndex = font.getIndex();

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
        }
    }

    @Test
    void setFontFromDifferentWorkbook() throws IOException {
        try (Workbook wb1 = _testDataProvider.createWorkbook();
             Workbook wb2 = _testDataProvider.createWorkbook()) {
            Font font1 = wb1.createFont();
            Font font2 = wb2.createFont();
            // do something to make font1 and font2 different
            // so they are not same or equal.
            font1.setItalic(true);
            Cell A1 = wb1.createSheet().createRow(0).createCell(0);

            // okay
            CellUtil.setFont(A1, font1);

            // font belongs to different workbook
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> CellUtil.setFont(A1, font2));
            assertTrue(e.getMessage().startsWith("Font does not belong to this workbook"));
        }
    }

    /**
     * bug 55555
     *
     * @since POI 3.15 beta 3
     */
    @Test
    protected void setFillForegroundColorBeforeFillBackgroundColorEnumByEnum() throws IOException {
        try (Workbook wb1 = _testDataProvider.createWorkbook()) {
            Cell A1 = wb1.createSheet().createRow(0).createCell(0);
            Map<CellPropertyType, Object> properties = new HashMap<>();
            properties.put(CellPropertyType.FILL_PATTERN, FillPatternType.BRICKS);
            properties.put(CellPropertyType.FILL_FOREGROUND_COLOR, IndexedColors.BLUE.index);
            properties.put(CellPropertyType.FILL_BACKGROUND_COLOR, IndexedColors.RED.index);

            CellUtil.setCellStylePropertiesEnum(A1, properties);
            CellStyle style = A1.getCellStyle();
            assertEquals(FillPatternType.BRICKS, style.getFillPattern(), "fill pattern");
            assertEquals(IndexedColors.BLUE, IndexedColors.fromInt(style.getFillForegroundColor()), "fill foreground color");
            assertEquals(IndexedColors.RED, IndexedColors.fromInt(style.getFillBackgroundColor()), "fill background color");
        }
    }

    /**
     * bug 55555
     *
     * @since POI 3.15 beta 3
     */
    @Test
    protected void setFillForegroundColorBeforeFillBackgroundColorEnum() throws IOException {
        try (Workbook wb1 = _testDataProvider.createWorkbook()) {
            Cell A1 = wb1.createSheet().createRow(0).createCell(0);
            Map<String, Object> properties = new HashMap<>();
            properties.put(CellUtil.FILL_PATTERN, FillPatternType.BRICKS);
            properties.put(CellUtil.FILL_FOREGROUND_COLOR, IndexedColors.BLUE.index);
            properties.put(CellUtil.FILL_BACKGROUND_COLOR, IndexedColors.RED.index);

            CellUtil.setCellStyleProperties(A1, properties);
            CellStyle style = A1.getCellStyle();
            assertEquals(FillPatternType.BRICKS, style.getFillPattern(), "fill pattern");
            assertEquals(IndexedColors.BLUE, IndexedColors.fromInt(style.getFillForegroundColor()), "fill foreground color");
            assertEquals(IndexedColors.RED, IndexedColors.fromInt(style.getFillBackgroundColor()), "fill background color");
        }
    }

    /**
     * bug 63268
     *
     * @since POI 4.1.0
     */
    @Test
    void setFontShouldNotCreateDuplicateStyle() throws IOException {
        Workbook wb1 = _testDataProvider.createWorkbook();
        Cell c = wb1.createSheet().createRow(1).createCell(1);
        Font f = wb1.createFont();

        CellUtil.setFont(c, f);
        int num1 = wb1.getNumCellStyles();

        CellUtil.setFont(c, f);
        int num2 = wb1.getNumCellStyles();
        assertEquals(num1, num2);
        wb1.close();
    }
}
