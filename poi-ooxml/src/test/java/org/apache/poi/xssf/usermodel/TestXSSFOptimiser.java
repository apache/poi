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

import static org.apache.poi.POITestCase.assertContains;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link XSSFOptimiser}.
 */
class TestXSSFOptimiser {

    /**
     * Test that optimiseCellStyles remaps cells with duplicate styles to canonical style.
     */
    @Test
    void testOptimiseCellStylesRemapsDuplicates() throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Test");

            // Create 10 cells, each with a "new" style that is actually identical
            for (int i = 0; i < 10; i++) {
                Row row = sheet.createRow(i);
                Cell cell = row.createCell(0);
                cell.setCellValue("Value " + i);

                // Create a "new" style for each cell (causes style explosion)
                CellStyle style = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setFontName("Calibri");
                font.setFontHeightInPoints((short) 11);
                font.setBold(false);
                style.setFont(font);
                cell.setCellStyle(style);
            }

            int stylesBeforeOptimize = workbook.getNumCellStyles();
            assertTrue(stylesBeforeOptimize > 5,
                "Should have created multiple styles before optimization, got: " + stylesBeforeOptimize);

            // Optimize - returns number of cells remapped
            int cellsRemapped = XSSFOptimiser.optimiseCellStyles(workbook);
            assertTrue(cellsRemapped > 0,
                "Should have remapped cells to canonical styles, got: " + cellsRemapped);

            // Verify all cells now reference the same canonical style index
            int canonicalStyleIdx = sheet.getRow(0).getCell(0).getCellStyle().getIndex();
            for (int i = 1; i < 10; i++) {
                Cell cell = sheet.getRow(i).getCell(0);
                assertEquals(canonicalStyleIdx, cell.getCellStyle().getIndex(),
                    "Cell " + i + " should use canonical style index");
            }

            // Verify cells still have correct values
            for (int i = 0; i < 10; i++) {
                Cell cell = sheet.getRow(i).getCell(0);
                assertEquals("Value " + i, cell.getStringCellValue());
            }
        }
    }

    /**
     * Test that optimiseCellStyles handles unused styles gracefully.
     */
    @Test
    void testOptimiseCellStylesHandlesUnusedStyles() throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Test");

            // Create a cell with one style
            Row row = sheet.createRow(0);
            Cell cell = row.createCell(0);
            cell.setCellValue("Used style");
            CellStyle usedStyle = workbook.createCellStyle();
            usedStyle.setAlignment(HorizontalAlignment.CENTER);
            cell.setCellStyle(usedStyle);

            // Create several unused styles
            for (int i = 0; i < 5; i++) {
                CellStyle unusedStyle = workbook.createCellStyle();
                unusedStyle.setAlignment(HorizontalAlignment.LEFT);
                // Never apply this style to any cell
            }

            int stylesBeforeOptimize = workbook.getNumCellStyles();
            assertTrue(stylesBeforeOptimize >= 6,
                "Should have at least 6 styles (1 default + 1 used + 5 unused), got: " + stylesBeforeOptimize);

            // Optimize - should not throw, returns 0 (no duplicates to remap)
            int cellsRemapped = XSSFOptimiser.optimiseCellStyles(workbook);
            assertEquals(0, cellsRemapped, "No duplicate styles to remap");

            // Verify the used cell still has correct alignment
            CellStyle cellStyle = cell.getCellStyle();
            assertEquals(HorizontalAlignment.CENTER, cellStyle.getAlignment(),
                "Cell should still have CENTER alignment after optimization");
        }
    }

    /**
     * Test that optimiseCellStyles preserves different styles.
     */
    @Test
    void testOptimiseCellStylesPreservesDifferentStyles() throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Test");

            // Cell with bold style
            Row row1 = sheet.createRow(0);
            Cell cell1 = row1.createCell(0);
            cell1.setCellValue("Bold");
            CellStyle boldStyle = workbook.createCellStyle();
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            boldStyle.setFont(boldFont);
            cell1.setCellStyle(boldStyle);

            // Cell with italic style
            Row row2 = sheet.createRow(1);
            Cell cell2 = row2.createCell(0);
            cell2.setCellValue("Italic");
            CellStyle italicStyle = workbook.createCellStyle();
            Font italicFont = workbook.createFont();
            italicFont.setItalic(true);
            italicStyle.setFont(italicFont);
            cell2.setCellStyle(italicStyle);

            // Cell with number format
            Row row3 = sheet.createRow(2);
            Cell cell3 = row3.createCell(0);
            cell3.setCellValue(1234.56);
            CellStyle numStyle = workbook.createCellStyle();
            DataFormat format = workbook.createDataFormat();
            numStyle.setDataFormat(format.getFormat("#,##0.00"));
            cell3.setCellStyle(numStyle);

            // Optimize
            XSSFOptimiser.optimiseCellStyles(workbook);

            // Verify each cell retains its distinct formatting
            Font font1 = workbook.getFontAt(cell1.getCellStyle().getFontIndex());
            assertTrue(font1.getBold(), "Cell 1 should still be bold");

            Font font2 = workbook.getFontAt(cell2.getCellStyle().getFontIndex());
            assertTrue(font2.getItalic(), "Cell 2 should still be italic");

            String numFormat = cell3.getCellStyle().getDataFormatString();
            assertContains(numFormat, "#,##0");
        }
    }

    /**
     * Test that optimiseFonts remaps style font references to canonical fonts.
     */
    @Test
    void testOptimiseFontsRemapsDuplicates() throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Test");

            // Create cells with duplicate fonts
            for (int i = 0; i < 10; i++) {
                Row row = sheet.createRow(i);
                Cell cell = row.createCell(0);
                cell.setCellValue("Value " + i);

                // Create a "new" font for each cell (identical fonts)
                CellStyle style = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setFontName("Arial");
                font.setFontHeightInPoints((short) 12);
                font.setBold(true);
                style.setFont(font);
                cell.setCellStyle(style);
            }

            int fontsBeforeOptimize = workbook.getNumberOfFonts();
            assertTrue(fontsBeforeOptimize > 5,
                "Should have created multiple fonts before optimization, got: " + fontsBeforeOptimize);

            // Optimize fonts - returns number of duplicates found
            int duplicatesFound = XSSFOptimiser.optimiseFonts(workbook);
            assertTrue(duplicatesFound > 0,
                "Should have found duplicate fonts, got: " + duplicatesFound);

            // Verify cells still have bold Arial font (formatting preserved)
            for (int i = 0; i < 10; i++) {
                Cell cell = sheet.getRow(i).getCell(0);
                Font font = workbook.getFontAt(cell.getCellStyle().getFontIndex());
                assertEquals("Arial", font.getFontName(), "Font name should be preserved");
                assertTrue(font.getBold(), "Font should still be bold");
            }
        }
    }

    /**
     * Test that workbook is saveable after optimization.
     */
    @Test
    void testWorkbookSaveableAfterOptimization() throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Test");

            // Create style explosion
            for (int i = 0; i < 20; i++) {
                Row row = sheet.createRow(i);
                Cell cell = row.createCell(0);
                cell.setCellValue("Value " + i);

                CellStyle style = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setFontName("Calibri");
                style.setFont(font);
                cell.setCellStyle(style);
            }

            // Optimize
            XSSFOptimiser.optimiseCellStyles(workbook);
            XSSFOptimiser.optimiseFonts(workbook);

            // Save to byte array (should not throw XmlValueDisconnectedException)
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            byte[] savedBytes = baos.toByteArray();

            assertTrue(savedBytes.length > 0, "Saved workbook should have content");

            // Verify we can read it back
            try (XSSFWorkbook reloaded = new XSSFWorkbook(new ByteArrayInputStream(savedBytes))) {
                XSSFSheet reloadedSheet = reloaded.getSheetAt(0);
                assertEquals("Value 0", reloadedSheet.getRow(0).getCell(0).getStringCellValue());
                assertEquals("Value 19", reloadedSheet.getRow(19).getCell(0).getStringCellValue());
            }
        }
    }

    /**
     * Test that optimization preserves formulas.
     */
    @Test
    void testOptimizationPreservesFormulas() throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Test");

            // Create cells with values
            for (int i = 0; i < 5; i++) {
                Row row = sheet.createRow(i);
                Cell cell = row.createCell(0);
                cell.setCellValue(i + 1);

                CellStyle style = workbook.createCellStyle();
                style.setAlignment(HorizontalAlignment.RIGHT);
                cell.setCellStyle(style);
            }

            // Create formula cell
            Row sumRow = sheet.createRow(5);
            Cell sumCell = sumRow.createCell(0);
            sumCell.setCellFormula("SUM(A1:A5)");
            CellStyle sumStyle = workbook.createCellStyle();
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            sumStyle.setFont(boldFont);
            sumCell.setCellStyle(sumStyle);

            // Optimize
            XSSFOptimiser.optimiseCellStyles(workbook);

            // Verify formula is preserved
            Cell formulaCell = sheet.getRow(5).getCell(0);
            assertEquals(CellType.FORMULA, formulaCell.getCellType());
            assertEquals("SUM(A1:A5)", formulaCell.getCellFormula());

            // Verify bold style is preserved on formula cell
            Font font = workbook.getFontAt(formulaCell.getCellStyle().getFontIndex());
            assertTrue(font.getBold(), "Formula cell should still be bold");
        }
    }
}
