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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.awt.font.FontRenderContext;
import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.ExceptionUtil;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

/**
 * Tests SheetUtil.
 *
 * @see org.apache.poi.ss.util.SheetUtil
 */
@SuppressWarnings("deprecation")
final class TestSheetUtil {
    @Test
    void testCellWithMerges() throws Exception {
        try (Workbook wb = new HSSFWorkbook()) {
            Sheet s = wb.createSheet();

            // Create some test data
            Row r2 = s.createRow(1);
            r2.createCell(0).setCellValue(10);
            r2.createCell(1).setCellValue(11);
            Row r3 = s.createRow(2);
            r3.createCell(0).setCellValue(20);
            r3.createCell(1).setCellValue(21);

            assertEquals(0, s.addMergedRegion(new CellRangeAddress(2, 3, 0, 0)));
            assertEquals(1, s.addMergedRegion(new CellRangeAddress(2, 2, 1, 4)));

            // With a cell that isn't defined, we'll get null
            assertNull(SheetUtil.getCellWithMerges(s, 0, 0));

            // With a cell that's not in a merged region, we'll get that
            Cell cell = SheetUtil.getCellWithMerges(s, 1, 0);
            assertNotNull(cell);
            assertEquals(10.0, cell.getNumericCellValue(), 0.01);
            cell = SheetUtil.getCellWithMerges(s, 1, 1);
            assertNotNull(cell);
            assertEquals(11.0, cell.getNumericCellValue(), 0.01);

            // With a cell that's the primary one of a merged region, we get that cell
            cell = SheetUtil.getCellWithMerges(s, 2, 0);
            assertNotNull(cell);
            assertEquals(20.0, cell.getNumericCellValue(), 0.01);
            cell = SheetUtil.getCellWithMerges(s, 2, 1);
            assertNotNull(cell);
            assertEquals(21., cell.getNumericCellValue(), 0.01);

            // With a cell elsewhere in the merged region, get top-left
            cell = SheetUtil.getCellWithMerges(s, 3, 0);
            assertNotNull(cell);
            assertEquals(20.0, cell.getNumericCellValue(), 0.01);
            cell = SheetUtil.getCellWithMerges(s, 2, 2);
            assertNotNull(cell);
            assertEquals(21.0, cell.getNumericCellValue(), 0.01);
            cell = SheetUtil.getCellWithMerges(s, 2, 3);
            assertNotNull(cell);
            assertEquals(21.0, cell.getNumericCellValue(), 0.01);
            assertNotNull(cell);
            cell = SheetUtil.getCellWithMerges(s, 2, 4);
            assertNotNull(cell);
            assertEquals(21.0, cell.getNumericCellValue(), 0.01);
        }
    }

    @Test
    void testCanComputeWidthHSSF() throws IOException {
        try (Workbook wb = new HSSFWorkbook()) {
            // cannot check on result because on some machines we get back false here!
            assertDoesNotThrow(() -> SheetUtil.canComputeColumnWidth(wb.getFontAt(0)));
        }
    }

    @Test
    void testGetCellWidthEmpty() throws IOException {
        try (Workbook wb = new HSSFWorkbook()) {
            Sheet sheet = wb.createSheet("sheet");
            Row row = sheet.createRow(0);
            Cell cell = row.createCell(0);

            // no contents: cell.setCellValue("sometext");
            //noinspection deprecation
            assertEquals(-1.0, SheetUtil.getCellWidth(cell, 1, null, true), 0.01);
            assertEquals(-1.0, SheetUtil.getCellWidth(cell, 1.0f, null, true), 0.01);

            assertEquals(-1.0, SheetUtil.getCellWidth(cell, 1.5f, null, true), 0.01);
        }
    }

    @Test
    void testGetCellWidthString() throws IOException {
        try (Workbook wb = new HSSFWorkbook()) {
            Sheet sheet = wb.createSheet("sheet");
            Row row = sheet.createRow(0);
            Cell cell = row.createCell(0);

            cell.setCellValue("sometext");

            final double width = SheetUtil.getCellWidth(cell, 1.0f, null, true);
            assertTrue(width > 0);
            //noinspection deprecation
            assertEquals(width, SheetUtil.getCellWidth(cell, 1, null, true));
        }
    }

    @Test
    void testGetCellWidthNumber() throws IOException {
        try (Workbook wb = new HSSFWorkbook()) {
            Sheet sheet = wb.createSheet("sheet");
            Row row = sheet.createRow(0);
            Cell cell = row.createCell(0);

            cell.setCellValue(88.234);

            //noinspection deprecation
            assertTrue(SheetUtil.getCellWidth(cell, 1, null, true) > 0);
            assertTrue(SheetUtil.getCellWidth(cell, 1.0f, null, true) > 0);
        }
    }

    @Test
    void testGetCellWidthBoolean() throws IOException {
        try (Workbook wb = new HSSFWorkbook()) {
            Sheet sheet = wb.createSheet("sheet");
            Row row = sheet.createRow(0);
            Cell cell = row.createCell(0);

            cell.setCellValue(false);

            //noinspection deprecation
            assertTrue(SheetUtil.getCellWidth(cell, 1, null, false) > 0);
            assertTrue(SheetUtil.getCellWidth(cell, 1.0f, null, false) > 0);
        }
    }

    @Test
    void testGetColumnWidthString() throws IOException {
        try (Workbook wb = new HSSFWorkbook()) {
            Sheet sheet = wb.createSheet("sheet");
            Row row = sheet.createRow(0);
            sheet.createRow(1);
            sheet.createRow(2);
            Cell cell = row.createCell(0);

            cell.setCellValue("sometext");

            assertTrue(SheetUtil.getColumnWidth(sheet, 0, true) > 0, "Having some width for rows with actual cells");
            assertEquals(-1.0, SheetUtil.getColumnWidth(sheet, 0, true, 1, 2), 0.01, "Not having any width for rows with all empty cells");
        }
    }

    @Test
    void testGetColumnWidthBlankCell() throws IOException {
        try (Workbook wb = new HSSFWorkbook()) {
            Sheet sheet = wb.createSheet("sheet");
            Row row = sheet.createRow(0);
            sheet.createRow(1);
            sheet.createRow(2);
            Cell cell = row.createCell(0);

            cell.setCellValue((String)null);

            assertEquals(-1, SheetUtil.getColumnWidth(sheet, 0, true), "Having some width for rows with actual cells");
            assertEquals(-1.0, SheetUtil.getColumnWidth(sheet, 0, true, 1, 2), 0.01, "Not having any width for rows with all empty cells");
        }
    }

    @Test
    void testGetColumnWidthemptyString() throws IOException {
        try (Workbook wb = new HSSFWorkbook()) {
            Sheet sheet = wb.createSheet("sheet");
            Row row = sheet.createRow(0);
            sheet.createRow(1);
            sheet.createRow(2);
            Cell cell = row.createCell(0);

            cell.setCellValue("");

            assertTrue(SheetUtil.getColumnWidth(sheet, 0, true) > 0, "Having some width for rows with actual cells");
            assertEquals(-1.0, SheetUtil.getColumnWidth(sheet, 0, true, 1, 2), 0.01, "Not having any width for rows with all empty cells");
        }
    }

    @Test
    void testGetColumnWidthNullString() throws IOException {
        try (Workbook wb = new HSSFWorkbook()) {
            Sheet sheet = wb.createSheet("sheet");
            Row row = sheet.createRow(0);
            sheet.createRow(1);
            sheet.createRow(2);
            Cell cell = row.createCell(0);

            cell.setCellValue((String)null);
            //noinspection deprecation
            cell.setCellType(CellType.STRING);

            assertTrue(SheetUtil.getColumnWidth(sheet, 0, true) > 0, "Having some width for rows with actual cells");
            assertEquals(-1.0, SheetUtil.getColumnWidth(sheet, 0, true, 1, 2), 0.01, "Not having any width for rows with all empty cells");
        }
    }

    @Test
    void testIsFatal() {
        assertFalse(ExceptionUtil.isFatal(new RuntimeException()),
                "RuntimeException should not be regarded as 'fatal'");
        assertTrue(ExceptionUtil.isFatal(new LinkageError()),
                "LinkageError should not be regarded as 'fatal'");
        assertTrue(ExceptionUtil.isFatal(new UnsatisfiedLinkError()),
                "UnsatisfiedLinkError should not be regarded as 'fatal'");
    }

    @Test
    void testGetCharWidthWithFonts() throws IOException {
        // verify that normal call returns a useful value
        // (may fail if font-system is missing, but then many other tests fail as well)

        try (Workbook wb = new HSSFWorkbook()) {
            final float width = SheetUtil.getDefaultCharWidthAsFloat(wb);
            assertTrue(width > 0,
                    "Should get some useful char width, but had: " + width);
        }
    }

    @Test
    void testGetCharWidthWithInvalidFont() throws IOException {
        // verify that a call with an unknown font-name returns a useful value
        // (likely the font-system falls back to a default font here)
        // (may fail if font-system is missing, but then many other tests fail as well)
        try (Workbook wb = new HSSFWorkbook()) {
            wb.getFontAt(0).setFontName("invalid font");

            float width = SheetUtil.getDefaultCharWidthAsFloat(wb);
            assertTrue(width > 0,
                    "Should get some useful char width, but had: " + width);

            wb.getFontAt(0).setFontName("");

            width = SheetUtil.getDefaultCharWidthAsFloat(wb);
            assertTrue(width > 0,
                    "Should get some useful char width, but had: " + width);

            wb.getFontAt(0).setFontName(null);

            width = SheetUtil.getDefaultCharWidthAsFloat(wb);
            assertTrue(width > 0,
                    "Should get some useful char width, but had: " + width);
        }
    }

    @Test
    void testDefaultIgnoreMissingFontSystem() {
        assertTrue(SheetUtil.isIgnoreMissingFontSystem());
    }

    @Test
    void testGetCharWidthWithIgnoreEnabled() throws IOException {
        boolean previous = SheetUtil.isIgnoreMissingFontSystem();
        SheetUtil.setIgnoreMissingFontSystem(true);

        // just verify that enabling the setting "ignoreMissingFontSystem"
        // does not cause unexpected results

        try (Workbook wb = new HSSFWorkbook()) {
            final float width = SheetUtil.getDefaultCharWidthAsFloat(wb);
            assertTrue(width > 0,
                    "Should get some useful char width, but had: " + width);
        } finally {
            // restore value
            SheetUtil.setIgnoreMissingFontSystem(previous);
        }
    }

    @Test
    void testGetCharWidthWithMockedException() throws IOException {
        FontRenderContext prevCtx = SheetUtil.getFontRenderContext();
        final FontRenderContext ctx = mock(FontRenderContext.class, (Answer<Object>) invocation -> {
            // simulate an exception in some of the calls to java.awt packages
            throw new IllegalArgumentException("Test runtime exception");
        });
        SheetUtil.setFontRenderContext(ctx);

        boolean previous = SheetUtil.isIgnoreMissingFontSystem();
        SheetUtil.setIgnoreMissingFontSystem(false);

        // verify that a RuntimeException in the font-system is
        // thrown when "ignoreMissingFontSystem" is disabled

        try (Workbook wb = new HSSFWorkbook()) {
            assertThrows(IllegalArgumentException.class,
                    () -> SheetUtil.getDefaultCharWidthAsFloat(wb),
                    "Should get an exception because ignoreMissingFontSystem = false");
        } finally {
            // restore values
            SheetUtil.setFontRenderContext(prevCtx);
            SheetUtil.setIgnoreMissingFontSystem(previous);
        }
    }

    @Test
    void testGetCharWidthWithMockedUnsatisfiedLinkError() throws IOException {
        FontRenderContext prevCtx = SheetUtil.getFontRenderContext();
        final FontRenderContext ctx = mock(FontRenderContext.class, (Answer<Object>) invocation -> {
            // simulate an exception in some of the calls to java.awt packages
            throw new UnsatisfiedLinkError("Test runtime exception");
        });
        SheetUtil.setFontRenderContext(ctx);

        boolean previous = SheetUtil.isIgnoreMissingFontSystem();
        SheetUtil.setIgnoreMissingFontSystem(false);

        // verify that a UnsatisfiedLinkError in the font-system is
        // thrown when "ignoreMissingFontSystem" is disabled

        try (Workbook wb = new HSSFWorkbook()) {
            assertThrows(UnsatisfiedLinkError.class,
                    () -> SheetUtil.getDefaultCharWidthAsFloat(wb),
                    "Should get an exception because ignoreMissingFontSystem = false");
        } finally {
            // restore values
            SheetUtil.setFontRenderContext(prevCtx);
            SheetUtil.setIgnoreMissingFontSystem(previous);
        }
    }

    @Test
    void testGetCharWidthWithMockedExceptionAndIgnore() throws IOException {
        FontRenderContext prevCtx = SheetUtil.getFontRenderContext();
        final FontRenderContext ctx = mock(FontRenderContext.class, (Answer<Object>) invocation -> {
            // simulate an exception in some of the calls to java.awt packages
            throw new IllegalArgumentException("Test runtime exception");
        });
        SheetUtil.setFontRenderContext(ctx);

        boolean previous = SheetUtil.isIgnoreMissingFontSystem();
        SheetUtil.setIgnoreMissingFontSystem(true);

        // verify that a RuntimeException in the font-system is
        // ignored when "ignoreMissingFontSystem" is enabled

        try (Workbook wb = new HSSFWorkbook()) {
            final float width = SheetUtil.getDefaultCharWidthAsFloat(wb);
            assertEquals(SheetUtil.DEFAULT_CHAR_WIDTH, width,
                    "Should get default char width because ignoreMissingFontSystem = true, but had: " + width);
        } finally {
            // restore values
            SheetUtil.setFontRenderContext(prevCtx);
            SheetUtil.setIgnoreMissingFontSystem(previous);
        }
    }

    @Test
    void testGetCharWidthWithMockedUnsatisfiedLinkErrorAndIgnore() throws IOException {
        FontRenderContext prevCtx = SheetUtil.getFontRenderContext();
        final FontRenderContext ctx = mock(FontRenderContext.class, (Answer<Object>) invocation -> {
            // simulate an exception in some of the calls to java.awt packages
            throw new UnsatisfiedLinkError("Test runtime exception");
        });
        SheetUtil.setFontRenderContext(ctx);

        boolean previous = SheetUtil.isIgnoreMissingFontSystem();
        SheetUtil.setIgnoreMissingFontSystem(true);

        // verify that a UnsatisfiedLinkError in the font-system is
        // ignored when "ignoreMissingFontSystem" is enabled

        try (Workbook wb = new HSSFWorkbook()) {
            final float width = SheetUtil.getDefaultCharWidthAsFloat(wb);
            assertEquals(SheetUtil.DEFAULT_CHAR_WIDTH, width,
                    "Should get default char width because ignoreMissingFontSystem = true, but had: " + width);
        } finally {
            // restore values
            SheetUtil.setFontRenderContext(prevCtx);
            SheetUtil.setIgnoreMissingFontSystem(previous);
        }
    }

    @Test
    void testGetDefaultCharWidthAsFloat() throws IOException {
        try (Workbook wb = new HSSFWorkbook()) {
            float width = SheetUtil.getDefaultCharWidthAsFloat(new HSSFWorkbook());
            assertTrue(width > 0,
                    "Expected a non-zero and positive font width");

            //Font font = wb.createFont();
            Font font = wb.getFontAt(0);
            assertNotNull(font);

            // verify that the system falls back to some default font for unknown/incorrect font-families

            font.setFontName("not-existing");
            float range = SheetUtil.getDefaultCharWidthAsFloat(wb);
            assertTrue(range > 5.5);
            assertTrue(range < 6.7);

            font.setFontName("");
            range = SheetUtil.getDefaultCharWidthAsFloat(wb);
            assertTrue(range > 5.5);
            assertTrue(range < 6.7);

            font.setFontName(null);
            range = SheetUtil.getDefaultCharWidthAsFloat(wb);
            assertTrue(range > 5.5);
            assertTrue(range < 6.7);
        }
    }
}
