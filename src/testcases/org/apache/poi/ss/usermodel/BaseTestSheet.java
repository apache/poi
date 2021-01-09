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

package org.apache.poi.ss.usermodel;

import static org.apache.poi.POITestCase.assertBetween;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.ITestDataProvider;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.PaneInformation;
import org.junit.jupiter.api.Test;

/**
 * Common superclass for testing {@link org.apache.poi.hssf.usermodel.HSSFCell},
 * {@link org.apache.poi.xssf.usermodel.XSSFCell} and
 * {@link org.apache.poi.xssf.streaming.SXSSFCell}
 */
public abstract class BaseTestSheet {
    private static final int ROW_COUNT = 40000;

    protected final ITestDataProvider _testDataProvider;

    protected BaseTestSheet(ITestDataProvider testDataProvider) {
        _testDataProvider = testDataProvider;
    }

    protected void trackColumnsForAutoSizingIfSXSSF(Sheet sheet) {
        // do nothing for Sheet base class. This will be overridden for SXSSFSheets.
    }

    @Test
    protected void createRow() throws IOException {
        try (Workbook workbook = _testDataProvider.createWorkbook()) {
            Sheet sheet = workbook.createSheet();
            assertEquals(0, sheet.getPhysicalNumberOfRows());

            //Test that we get null for undefined rownumber
            assertNull(sheet.getRow(1));

            // Test row creation with consecutive indexes
            Row row1 = sheet.createRow(0);
            Row row2 = sheet.createRow(1);
            assertEquals(0, row1.getRowNum());
            assertEquals(1, row2.getRowNum());
            Iterator<Row> it = sheet.rowIterator();
            assertTrue(it.hasNext());
            assertSame(row1, it.next());
            assertTrue(it.hasNext());
            assertSame(row2, it.next());
            assertEquals(1, sheet.getLastRowNum());

            // Test row creation with non consecutive index
            Row row101 = sheet.createRow(100);
            assertNotNull(row101);
            assertEquals(100, sheet.getLastRowNum());
            assertEquals(3, sheet.getPhysicalNumberOfRows());

            // Test overwriting an existing row
            Row row2_ovrewritten = sheet.createRow(1);
            Cell cell = row2_ovrewritten.createCell(0);
            cell.setCellValue(100);
            Iterator<Row> it2 = sheet.rowIterator();
            assertTrue(it2.hasNext());
            assertSame(row1, it2.next());
            assertTrue(it2.hasNext());
            Row row2_ovrewritten_ref = it2.next();
            assertSame(row2_ovrewritten, row2_ovrewritten_ref);
            assertEquals(100.0, row2_ovrewritten_ref.getCell(0).getNumericCellValue(), 0.0);
        }
    }

    @Test
    void createRowBeforeFirstRow() throws IOException {
        try (Workbook workbook = _testDataProvider.createWorkbook()) {
            final Sheet sh = workbook.createSheet();
            sh.createRow(0);
            // Negative rows not allowed
            assertThrows(IllegalArgumentException.class, () -> sh.createRow(-1));
        }
    }

    @Test
    void createRowAfterLastRow() throws IOException {
        final SpreadsheetVersion version = _testDataProvider.getSpreadsheetVersion();
        try (Workbook workbook = _testDataProvider.createWorkbook()) {
            final Sheet sh = workbook.createSheet();
            sh.createRow(version.getLastRowIndex());
            // Row number must be between 0 and last row
            assertThrows(IllegalArgumentException.class, () -> sh.createRow(version.getLastRowIndex() + 1));
        }
    }

    @Test
    void removeRow() throws IOException {
        try (Workbook workbook = _testDataProvider.createWorkbook()) {
            Sheet sheet1 = workbook.createSheet();
            assertEquals(0, sheet1.getPhysicalNumberOfRows());
            assertEquals(-1, sheet1.getFirstRowNum());
            assertEquals(-1, sheet1.getLastRowNum());

            Row row0 = sheet1.createRow(0);
            assertEquals(1, sheet1.getPhysicalNumberOfRows());
            assertEquals(0, sheet1.getFirstRowNum());
            assertEquals(0, sheet1.getLastRowNum());
            sheet1.removeRow(row0);
            assertEquals(0, sheet1.getPhysicalNumberOfRows());
            assertEquals(-1, sheet1.getFirstRowNum());
            assertEquals(-1, sheet1.getLastRowNum());

            sheet1.createRow(1);
            Row row2 = sheet1.createRow(2);
            assertEquals(2, sheet1.getPhysicalNumberOfRows());
            assertEquals(1, sheet1.getFirstRowNum());
            assertEquals(2, sheet1.getLastRowNum());

            assertNotNull(sheet1.getRow(1));
            assertNotNull(sheet1.getRow(2));
            sheet1.removeRow(row2);
            assertNotNull(sheet1.getRow(1));
            assertNull(sheet1.getRow(2));
            assertEquals(1, sheet1.getPhysicalNumberOfRows());
            assertEquals(1, sheet1.getFirstRowNum());
            assertEquals(1, sheet1.getLastRowNum());

            Row row3 = sheet1.createRow(3);
            Sheet sheet2 = workbook.createSheet();

            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> sheet2.removeRow(row3)
            );
            assertEquals("Specified row does not belong to this sheet", ex.getMessage());
        }
    }

    @Test
    protected void cloneSheet() throws IOException {
        try (Workbook workbook = _testDataProvider.createWorkbook()) {
            CreationHelper factory = workbook.getCreationHelper();
            Sheet sheet = workbook.createSheet("Test Clone");
            Row row = sheet.createRow(0);
            Cell cell = row.createCell(0);
            Cell cell2 = row.createCell(1);
            cell.setCellValue(factory.createRichTextString("clone_test"));
            cell2.setCellFormula("SIN(1)");

            Sheet clonedSheet = workbook.cloneSheet(0);
            Row clonedRow = clonedSheet.getRow(0);

            //Check for a good clone
            assertEquals(clonedRow.getCell(0).getRichStringCellValue().getString(), "clone_test");

            //Check that the cells are not somehow linked
            cell.setCellValue(factory.createRichTextString("Difference Check"));
            cell2.setCellFormula("cos(2)");

            assertNotEquals("Difference Check", clonedRow.getCell(0).getRichStringCellValue().getString(),
                "string cell not properly cloned");

            assertNotEquals("COS(2)", clonedRow.getCell(1).getCellFormula(), "formula cell not properly cloned");

            assertEquals(clonedRow.getCell(0).getRichStringCellValue().getString(), "clone_test");
            assertEquals(clonedRow.getCell(1).getCellFormula(), "SIN(1)");
        }
    }

    /** tests that the sheet name for multiple clones of the same sheet is unique
     * BUG 37416
     */
    @Test
    protected void cloneSheetMultipleTimes() throws IOException {
        try (Workbook workbook = _testDataProvider.createWorkbook()) {
            CreationHelper factory = workbook.getCreationHelper();
            Sheet sheet = workbook.createSheet("Test Clone");
            Row row = sheet.createRow(0);
            Cell cell = row.createCell(0);
            cell.setCellValue(factory.createRichTextString("clone_test"));
            //Clone the sheet multiple times
            workbook.cloneSheet(0);
            workbook.cloneSheet(0);

            assertNotNull(workbook.getSheet("Test Clone"));
            assertNotNull(workbook.getSheet("Test Clone (2)"));
            assertEquals("Test Clone (3)", workbook.getSheetName(2));
            assertNotNull(workbook.getSheet("Test Clone (3)"));

            workbook.removeSheetAt(0);
            workbook.removeSheetAt(0);
            workbook.removeSheetAt(0);
            workbook.createSheet("abc ( 123)");
            workbook.cloneSheet(0);
            assertEquals("abc (124)", workbook.getSheetName(1));
        }
    }

    /**
     * Setting landscape and portrait stuff on new sheets
     */
    @Test
    void printSetupLandscapeNew() throws IOException {
        try (Workbook wb1 = _testDataProvider.createWorkbook()) {
            Sheet sheetL = wb1.createSheet("LandscapeS");
            Sheet sheetP = wb1.createSheet("LandscapeP");

            // Check two aspects of the print setup
            assertFalse(sheetL.getPrintSetup().getLandscape());
            assertFalse(sheetP.getPrintSetup().getLandscape());
            assertEquals(1, sheetL.getPrintSetup().getCopies());
            assertEquals(1, sheetP.getPrintSetup().getCopies());

            // Change one on each
            sheetL.getPrintSetup().setLandscape(true);
            sheetP.getPrintSetup().setCopies((short) 3);

            // Check taken
            assertTrue(sheetL.getPrintSetup().getLandscape());
            assertFalse(sheetP.getPrintSetup().getLandscape());
            assertEquals(1, sheetL.getPrintSetup().getCopies());
            assertEquals(3, sheetP.getPrintSetup().getCopies());

            // Save and re-load, and check still there
            try (Workbook wb2 = _testDataProvider.writeOutAndReadBack(wb1)) {
                sheetL = wb2.getSheet("LandscapeS");
                sheetP = wb2.getSheet("LandscapeP");

                assertTrue(sheetL.getPrintSetup().getLandscape());
                assertFalse(sheetP.getPrintSetup().getLandscape());
                assertEquals(1, sheetL.getPrintSetup().getCopies());
                assertEquals(3, sheetP.getPrintSetup().getCopies());
            }
        }
    }

    /**
     * Disallow creating wholly or partially overlapping merged regions
     * as this results in a corrupted workbook
     */
    @Test
    void addOverlappingMergedRegions() throws IOException {
        try (final Workbook wb = _testDataProvider.createWorkbook()) {
            final Sheet sheet = wb.createSheet();

            final CellRangeAddress baseRegion = new CellRangeAddress(0, 1, 0, 1); //A1:B2
            assertEquals(0, sheet.addMergedRegion(baseRegion));

            final CellRangeAddress duplicateRegion = new CellRangeAddress(0, 1, 0, 1); //A1:B2
            assertThrows(
                IllegalStateException.class,
                () -> sheet.addMergedRegion(duplicateRegion),
                "Should not be able to add a merged region (" + duplicateRegion.formatAsString() + ") " +
                "if sheet already contains the same merged region (" + baseRegion.formatAsString() + ")"
            );

            final CellRangeAddress partiallyOverlappingRegion = new CellRangeAddress(1, 2, 1, 2); //B2:C3
            assertThrows(
                IllegalStateException.class,
                () -> sheet.addMergedRegion(partiallyOverlappingRegion),
                "Should not be able to add a merged region (" + partiallyOverlappingRegion.formatAsString() + ") " +
                "if it partially overlaps with an existing merged region (" + baseRegion.formatAsString() + ")"
            );

            final CellRangeAddress subsetRegion = new CellRangeAddress(0, 1, 0, 0); //A1:A2
            assertThrows(
                IllegalStateException.class,
                () -> sheet.addMergedRegion(subsetRegion),
                "Should not be able to add a merged region (" + subsetRegion.formatAsString() + ") " +
                "if it is a formal subset of an existing merged region (" + baseRegion.formatAsString() + ")"
            );

            final CellRangeAddress supersetRegion = new CellRangeAddress(0, 2, 0, 2); //A1:C3
            assertThrows(
                IllegalStateException.class,
                () -> sheet.addMergedRegion(supersetRegion),
                "Should not be able to add a merged region (" + supersetRegion.formatAsString() + ") " +
                "if it is a formal superset of an existing merged region (" + baseRegion.formatAsString() + ")"
            );

            final CellRangeAddress disjointRegion = new CellRangeAddress(10, 11, 10, 11);
            assertEquals(1, sheet.addMergedRegion(disjointRegion));
        }
    }

    /*
     * Bug 56345: Reject single-cell merged regions
     */
    @Test
    void addMergedRegionWithSingleCellShouldFail() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {

            final Sheet sheet = wb.createSheet();
            final CellRangeAddress region = CellRangeAddress.valueOf("A1:A1");
            assertThrows(
                IllegalArgumentException.class,
                () -> sheet.addMergedRegion(region),
                "Should not be able to add a single-cell merged region (" + region.formatAsString() + ")"
            );
        }
    }

    /**
     * Test adding merged regions. If the region's bounds are outside of the allowed range
     * then an IllegalArgumentException should be thrown
     *
     */
    @Test
    void addMerged() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Sheet sheet = wb.createSheet();
            assertEquals(0, sheet.getNumMergedRegions());
            SpreadsheetVersion ssVersion = _testDataProvider.getSpreadsheetVersion();

            CellRangeAddress region = new CellRangeAddress(0, 1, 0, 1);
            assertEquals(0, sheet.addMergedRegion(region));
            assertEquals(1, sheet.getNumMergedRegions());

            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> sheet.addMergedRegion(new CellRangeAddress(-1, -1, -1, -1))
            );
            assertEquals("Merged region : must contain 2 or more cells", ex.getMessage());

            ex = assertThrows(
                IllegalArgumentException.class,
                () -> sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, ssVersion.getLastColumnIndex() + 1))
            );
            assertEquals("Maximum column number is " + ssVersion.getLastColumnIndex(), ex.getMessage());

            ex = assertThrows(
                IllegalArgumentException.class,
                () -> sheet.addMergedRegion(new CellRangeAddress(0, ssVersion.getLastRowIndex() + 1, 0, 1))
            );
            assertEquals("Maximum row number is " + ssVersion.getLastRowIndex(), ex.getMessage());

            assertEquals(1, sheet.getNumMergedRegions());
        }
    }

    /**
     * When removing one merged region, it would break
     *
     */
    @Test
    void removeMerged() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Sheet sheet = wb.createSheet();
            CellRangeAddress region = new CellRangeAddress(0, 1, 0, 1);
            assertEquals(0, sheet.addMergedRegion(region));
            region = new CellRangeAddress(2, 3, 0, 1);
            assertEquals(1, sheet.addMergedRegion(region));

            sheet.removeMergedRegion(0);

            region = sheet.getMergedRegion(0);
            assertEquals(2, region.getFirstRow(), "Left over region should be starting at row 2");

            sheet.removeMergedRegion(0);

            assertEquals(0, sheet.getNumMergedRegions(), "there should be no merged regions left!");

            //an, add, remove, get(0) would null pointer
            assertEquals(0, sheet.addMergedRegion(region));
            assertEquals(1, sheet.getNumMergedRegions(), "there should now be one merged region!");
            sheet.removeMergedRegion(0);
            assertEquals(0, sheet.getNumMergedRegions(), "there should now be zero merged regions!");
            //add it again!
            region.setLastRow(4);

            assertEquals(0, sheet.addMergedRegion(region));
            assertEquals(1, sheet.getNumMergedRegions(), "there should now be one merged region!");

            //should exist now!
            assertTrue(1 <= sheet.getNumMergedRegions(), "there isn't more than one merged region in there");
            region = sheet.getMergedRegion(0);
            assertEquals(4, region.getLastRow(), "the merged row to doesn't match the one we put in ");
        }
    }

    /**
     * Remove multiple merged regions
     */
    @Test
    void removeMergedRegions() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Sheet sheet = wb.createSheet();

            Map<Integer, CellRangeAddress> mergedRegions = new HashMap<>();
            for (int r = 0; r < 10; r++) {
                CellRangeAddress region = new CellRangeAddress(r, r, 0, 1);
                mergedRegions.put(r, region);
                assertEquals(r, sheet.addMergedRegion(region));
            }
            assertCollectionEquals(mergedRegions.values(), sheet.getMergedRegions());

            Collection<Integer> removed = Arrays.asList(0, 2, 3, 6, 8);
            mergedRegions.keySet().removeAll(removed);
            sheet.removeMergedRegions(removed);
            assertCollectionEquals(mergedRegions.values(), sheet.getMergedRegions());
        }
    }

    private static <T> void assertCollectionEquals(Collection<T> expected, Collection<T> actual) {
        Set<T> e = new HashSet<>(expected);
        Set<T> a = new HashSet<>(actual);
        assertEquals(e, a);
    }

    @Test
    protected void shiftMerged() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            CreationHelper factory = wb.getCreationHelper();
            Sheet sheet = wb.createSheet();
            Row row = sheet.createRow(0);
            Cell cell = row.createCell(0);
            cell.setCellValue(factory.createRichTextString("first row, first cell"));

            row = sheet.createRow(1);
            cell = row.createCell(1);
            cell.setCellValue(factory.createRichTextString("second row, second cell"));

            CellRangeAddress region = CellRangeAddress.valueOf("A2:B2");
            assertEquals(0, sheet.addMergedRegion(region));

            sheet.shiftRows(1, 1, 1);

            region = sheet.getMergedRegion(0);

            CellRangeAddress expectedRegion = CellRangeAddress.valueOf("A3:B3");
            assertEquals(expectedRegion, region, "Merged region should shift down a row");
        }
    }

    /**
     * bug 58885: checking for overlapping merged regions when
     * adding a merged region is safe, but runs in O(n).
     * the check for merged regions when adding a merged region
     * can be skipped (unsafe) and run in O(1).
     */
    @Test
    void addMergedRegionUnsafe() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Sheet sh = wb.createSheet();
            CellRangeAddress region1 = CellRangeAddress.valueOf("A1:B2");
            CellRangeAddress region2 = CellRangeAddress.valueOf("B2:C3");
            CellRangeAddress region3 = CellRangeAddress.valueOf("C3:D4");
            CellRangeAddress region4 = CellRangeAddress.valueOf("J10:K11");
            assumeTrue(region1.intersects(region2));
            assumeTrue(region2.intersects(region3));

            assertEquals(0, sh.addMergedRegionUnsafe(region1));
            assertTrue(sh.getMergedRegions().contains(region1));

            // adding a duplicate or overlapping merged region should not
            // raise an exception with the unsafe version of addMergedRegion.

            assertEquals(1, sh.addMergedRegionUnsafe(region2));

            // the safe version of addMergedRegion should throw when trying to add a merged region that overlaps an existing region
            assertTrue(sh.getMergedRegions().contains(region2));
            assertThrows(
                IllegalStateException.class,
                () -> sh.addMergedRegion(region3),
                "region3 overlaps already added merged region2."
            );
            assertFalse(sh.getMergedRegions().contains(region3));

            // addMergedRegion should not re-validate previously-added merged regions
            assertEquals(2, sh.addMergedRegion(region4));

            // validation methods should detect a problem with previously added merged regions (runs in O(n^2) time)
            assertThrows(
                IllegalStateException.class,
                sh::validateMergedRegions,
                "Sheet contains merged regions A1:B2 and B2:C3, which overlap at B2."
            );
        }
    }

    /**
     * Tests the display of gridlines, formulas, and rowcolheadings.
     */
    @Test
    void displayOptions() throws IOException {
        try (Workbook wb1 = _testDataProvider.createWorkbook()) {
            Sheet sheet = wb1.createSheet();

            assertTrue(sheet.isDisplayGridlines());
            assertTrue(sheet.isDisplayRowColHeadings());
            assertFalse(sheet.isDisplayFormulas());
            assertTrue(sheet.isDisplayZeros());

            sheet.setDisplayGridlines(false);
            sheet.setDisplayRowColHeadings(false);
            sheet.setDisplayFormulas(true);
            sheet.setDisplayZeros(false);

            try (Workbook wb2 = _testDataProvider.writeOutAndReadBack(wb1)) {
                sheet = wb2.getSheetAt(0);

                assertFalse(sheet.isDisplayGridlines());
                assertFalse(sheet.isDisplayRowColHeadings());
                assertTrue(sheet.isDisplayFormulas());
                assertFalse(sheet.isDisplayZeros());
            }
        }
    }

    @Test
    void columnWidth() throws IOException {
        try (Workbook wb1 = _testDataProvider.createWorkbook()) {
            Sheet sheet1 = wb1.createSheet();

            //default column width measured in characters
            sheet1.setDefaultColumnWidth(10);
            assertEquals(10, sheet1.getDefaultColumnWidth());
            //columns A-C have default width
            assertEquals(256 * 10, sheet1.getColumnWidth(0));
            assertEquals(256 * 10, sheet1.getColumnWidth(1));
            assertEquals(256 * 10, sheet1.getColumnWidth(2));

            //set custom width for D-F
            for (char i = 'D'; i <= 'F'; i++) {
                //Sheet#setColumnWidth accepts the width in units of 1/256th of a character width
                int w = 256 * 12;
                sheet1.setColumnWidth(i, w);
                assertEquals(w, sheet1.getColumnWidth(i));
            }
            //reset the default column width, columns A-C change, D-F still have custom width
            sheet1.setDefaultColumnWidth(20);
            assertEquals(20, sheet1.getDefaultColumnWidth());
            assertEquals(256 * 20, sheet1.getColumnWidth(0));
            assertEquals(256 * 20, sheet1.getColumnWidth(1));
            assertEquals(256 * 20, sheet1.getColumnWidth(2));
            for (char i = 'D'; i <= 'F'; i++) {
                int w = 256 * 12;
                assertEquals(w, sheet1.getColumnWidth(i));
            }

            // check for 16-bit signed/unsigned error:
            sheet1.setColumnWidth(10, 40000);
            assertEquals(40000, sheet1.getColumnWidth(10));

            //The maximum column width for an individual cell is 255 characters
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> sheet1.setColumnWidth(9, 256 * 256)
            );
            assertEquals("The maximum column width for an individual cell is 255 characters.", ex.getMessage());

            //serialize and read again
            try (Workbook wb2 = _testDataProvider.writeOutAndReadBack(wb1)) {
                Sheet sheet2 = wb2.getSheetAt(0);
                assertEquals(20, sheet2.getDefaultColumnWidth());
                //columns A-C have default width
                assertEquals(256 * 20, sheet2.getColumnWidth(0));
                assertEquals(256 * 20, sheet2.getColumnWidth(1));
                assertEquals(256 * 20, sheet2.getColumnWidth(2));
                //columns D-F have custom width
                for (char i = 'D'; i <= 'F'; i++) {
                    short w = (256 * 12);
                    assertEquals(w, sheet2.getColumnWidth(i));
                }
                assertEquals(40000, sheet2.getColumnWidth(10));
            }
        }
    }


    @Test
    void defaultRowHeight() throws IOException {
        try (Workbook workbook = _testDataProvider.createWorkbook()) {
            Sheet sheet = workbook.createSheet();
            sheet.setDefaultRowHeightInPoints(15);
            assertEquals((short) 300, sheet.getDefaultRowHeight());
            assertEquals(15.0F, sheet.getDefaultRowHeightInPoints(), 0F);

            Row row = sheet.createRow(1);
            // new row inherits  default height from the sheet
            assertEquals(sheet.getDefaultRowHeight(), row.getHeight());

            // Set a new default row height in twips and test getting the value in points
            sheet.setDefaultRowHeight((short) 360);
            assertEquals(18.0f, sheet.getDefaultRowHeightInPoints(), 0F);
            assertEquals((short) 360, sheet.getDefaultRowHeight());

            // Test that defaultRowHeight is a truncated short: E.G. 360inPoints -> 18; 361inPoints -> 18
            sheet.setDefaultRowHeight((short) 361);
            assertEquals((float) 361 / 20, sheet.getDefaultRowHeightInPoints(), 0F);
            assertEquals((short) 361, sheet.getDefaultRowHeight());

            // Set a new default row height in points and test getting the value in twips
            sheet.setDefaultRowHeightInPoints(17.5f);
            assertEquals(17.5f, sheet.getDefaultRowHeightInPoints(), 0F);
            assertEquals((short) (17.5f * 20), sheet.getDefaultRowHeight());
        }
    }

    /** cell with formula becomes null on cloning a sheet*/
    @Test
    protected void bug35084() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Sheet s = wb.createSheet("Sheet1");
            Row r = s.createRow(0);
            r.createCell(0).setCellValue(1);
            r.createCell(1).setCellFormula("A1*2");
            Sheet s1 = wb.cloneSheet(0);
            r = s1.getRow(0);
            // sanity check
            assertEquals(r.getCell(0).getNumericCellValue(), 1, 0, "double");
            assertNotNull(r.getCell(1));
            assertEquals(r.getCell(1).getCellFormula(), "A1*2", "formula");
        }
    }

    /** test that new default column styles get applied */
    @Test
    protected void defaultColumnStyle() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            CellStyle style = wb.createCellStyle();
            Sheet sheet = wb.createSheet();
            sheet.setDefaultColumnStyle(0, style);
            assertNotNull(sheet.getColumnStyle(0));
            assertEquals(style.getIndex(), sheet.getColumnStyle(0).getIndex());

            Row row = sheet.createRow(0);
            Cell cell = row.createCell(0);
            CellStyle style2 = cell.getCellStyle();
            assertNotNull(style2);
            assertEquals(style.getIndex(), style2.getIndex(), "style should match");
        }
    }

    @Test
    void outlineProperties() throws IOException {
        try (Workbook wb1 = _testDataProvider.createWorkbook()) {
            Sheet sheet = wb1.createSheet();

            //TODO defaults are different in HSSF and XSSF
            //assertTrue(sheet.getRowSumsBelow());
            //assertTrue(sheet.getRowSumsRight());

            sheet.setRowSumsBelow(false);
            sheet.setRowSumsRight(false);

            assertFalse(sheet.getRowSumsBelow());
            assertFalse(sheet.getRowSumsRight());

            sheet.setRowSumsBelow(true);
            sheet.setRowSumsRight(true);

            assertTrue(sheet.getRowSumsBelow());
            assertTrue(sheet.getRowSumsRight());

            try (Workbook wb2 = _testDataProvider.writeOutAndReadBack(wb1)) {
                sheet = wb2.getSheetAt(0);
                assertTrue(sheet.getRowSumsBelow());
                assertTrue(sheet.getRowSumsRight());
            }
        }
    }

    /**
     * Test basic display and print properties
     */
    @Test
    void sheetProperties() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Sheet sheet = wb.createSheet();

            assertFalse(sheet.getHorizontallyCenter());
            sheet.setHorizontallyCenter(true);
            assertTrue(sheet.getHorizontallyCenter());
            sheet.setHorizontallyCenter(false);
            assertFalse(sheet.getHorizontallyCenter());

            assertFalse(sheet.getVerticallyCenter());
            sheet.setVerticallyCenter(true);
            assertTrue(sheet.getVerticallyCenter());
            sheet.setVerticallyCenter(false);
            assertFalse(sheet.getVerticallyCenter());

            assertFalse(sheet.isPrintGridlines());
            sheet.setPrintGridlines(true);
            assertTrue(sheet.isPrintGridlines());

            assertFalse(sheet.isPrintRowAndColumnHeadings());
            sheet.setPrintRowAndColumnHeadings(true);
            assertTrue(sheet.isPrintRowAndColumnHeadings());

            assertFalse(sheet.isDisplayFormulas());
            sheet.setDisplayFormulas(true);
            assertTrue(sheet.isDisplayFormulas());

            assertTrue(sheet.isDisplayGridlines());
            sheet.setDisplayGridlines(false);
            assertFalse(sheet.isDisplayGridlines());

            //TODO: default "guts" is different in HSSF and XSSF
            //assertTrue(sheet.getDisplayGuts());
            sheet.setDisplayGuts(false);
            assertFalse(sheet.getDisplayGuts());

            assertTrue(sheet.isDisplayRowColHeadings());
            sheet.setDisplayRowColHeadings(false);
            assertFalse(sheet.isDisplayRowColHeadings());

            //TODO: default "autobreaks" is different in HSSF and XSSF
            //assertTrue(sheet.getAutobreaks());
            sheet.setAutobreaks(false);
            assertFalse(sheet.getAutobreaks());

            assertFalse(sheet.getScenarioProtect());

            //TODO: default "fit-to-page" is different in HSSF and XSSF
            //assertFalse(sheet.getFitToPage());
            sheet.setFitToPage(true);
            assertTrue(sheet.getFitToPage());
            sheet.setFitToPage(false);
            assertFalse(sheet.getFitToPage());
        }
    }

    @Test
    void testGetSetMargin() throws IOException {
        double[] defaultMargins = (getClass().getName().contains("xssf"))
            ? new double[]{0.7, 0.7, 0.75, 0.75, 0.3, 0.3}
            : new double[]{0.75, 0.75, 1.0, 1.0, 0.3, 0.3};

        double marginLeft = defaultMargins[0];
        double marginRight = defaultMargins[1];
        double marginTop = defaultMargins[2];
        double marginBottom = defaultMargins[3];
        //double marginHeader = defaultMargins[4];
        //double marginFooter = defaultMargins[5];

        try (Workbook workbook = _testDataProvider.createWorkbook()) {
            Sheet sheet = workbook.createSheet("Sheet 1");
            assertEquals(marginLeft, sheet.getMargin(Sheet.LeftMargin), 0.0);
            sheet.setMargin(Sheet.LeftMargin, 10.0);
            //left margin is custom, all others are default
            assertEquals(10.0, sheet.getMargin(Sheet.LeftMargin), 0.0);
            assertEquals(marginRight, sheet.getMargin(Sheet.RightMargin), 0.0);
            assertEquals(marginTop, sheet.getMargin(Sheet.TopMargin), 0.0);
            assertEquals(marginBottom, sheet.getMargin(Sheet.BottomMargin), 0.0);
            sheet.setMargin(Sheet.RightMargin, 11.0);
            assertEquals(11.0, sheet.getMargin(Sheet.RightMargin), 0.0);
            sheet.setMargin(Sheet.TopMargin, 12.0);
            assertEquals(12.0, sheet.getMargin(Sheet.TopMargin), 0.0);
            sheet.setMargin(Sheet.BottomMargin, 13.0);
            assertEquals(13.0, sheet.getMargin(Sheet.BottomMargin), 0.0);

            sheet.setMargin(Sheet.FooterMargin, 5.6);
            assertEquals(5.6, sheet.getMargin(Sheet.FooterMargin), 0.0);
            sheet.setMargin(Sheet.HeaderMargin, 11.5);
            assertEquals(11.5, sheet.getMargin(Sheet.HeaderMargin), 0.0);

            // incorrect margin constant
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> sheet.setMargin((short) 65, 15)
            );
            assertEquals("Unknown margin constant:  65", ex.getMessage());
        }
    }

    @Test
    void rowBreaks() throws IOException {
        try (Workbook workbook = _testDataProvider.createWorkbook()) {
            Sheet sheet = workbook.createSheet();
            //Sheet#getRowBreaks() returns an empty array if no row breaks are defined
            assertNotNull(sheet.getRowBreaks());
            assertEquals(0, sheet.getRowBreaks().length);

            sheet.setRowBreak(1);
            assertEquals(1, sheet.getRowBreaks().length);
            sheet.setRowBreak(15);
            assertEquals(2, sheet.getRowBreaks().length);
            assertEquals(1, sheet.getRowBreaks()[0]);
            assertEquals(15, sheet.getRowBreaks()[1]);
            sheet.setRowBreak(1);
            assertEquals(2, sheet.getRowBreaks().length);
            assertTrue(sheet.isRowBroken(1));
            assertTrue(sheet.isRowBroken(15));

            //now remove the created breaks
            sheet.removeRowBreak(1);
            assertEquals(1, sheet.getRowBreaks().length);
            sheet.removeRowBreak(15);
            assertEquals(0, sheet.getRowBreaks().length);

            assertFalse(sheet.isRowBroken(1));
            assertFalse(sheet.isRowBroken(15));
        }
    }

    @Test
    void columnBreaks() throws IOException {
        try (Workbook workbook = _testDataProvider.createWorkbook()) {
            Sheet sheet = workbook.createSheet();
            assertNotNull(sheet.getColumnBreaks());
            assertEquals(0, sheet.getColumnBreaks().length);

            assertFalse(sheet.isColumnBroken(0));

            sheet.setColumnBreak(11);
            assertNotNull(sheet.getColumnBreaks());
            assertEquals(11, sheet.getColumnBreaks()[0]);
            sheet.setColumnBreak(12);
            assertEquals(2, sheet.getColumnBreaks().length);
            assertTrue(sheet.isColumnBroken(11));
            assertTrue(sheet.isColumnBroken(12));

            sheet.removeColumnBreak((short) 11);
            assertEquals(1, sheet.getColumnBreaks().length);
            sheet.removeColumnBreak((short) 15); //remove non-existing
            assertEquals(1, sheet.getColumnBreaks().length);
            sheet.removeColumnBreak((short) 12);
            assertEquals(0, sheet.getColumnBreaks().length);

            assertFalse(sheet.isColumnBroken(11));
            assertFalse(sheet.isColumnBroken(12));
        }
    }

    @Test
    void getFirstLastRowNum() throws IOException {
        try (Workbook workbook = _testDataProvider.createWorkbook()) {
            Sheet sheet = workbook.createSheet("Sheet 1");
            sheet.createRow(9);
            sheet.createRow(0);
            sheet.createRow(1);
            assertEquals(0, sheet.getFirstRowNum());
            assertEquals(9, sheet.getLastRowNum());
        }
    }

    @Test
    void getFooter() throws IOException {
        try (Workbook workbook = _testDataProvider.createWorkbook()) {
            Sheet sheet = workbook.createSheet("Sheet 1");
            assertNotNull(sheet.getFooter());
            sheet.getFooter().setCenter("test center footer");
            assertEquals("test center footer", sheet.getFooter().getCenter());
        }
    }

    @Test
    void getSetColumnHidden() throws IOException {
        try (Workbook workbook = _testDataProvider.createWorkbook()) {
            Sheet sheet = workbook.createSheet("Sheet 1");
            sheet.setColumnHidden(2, true);
            assertTrue(sheet.isColumnHidden(2));
        }
    }

    @Test
    void protectSheet() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Sheet sheet = wb.createSheet();
            assertFalse(sheet.getProtect());
            sheet.protectSheet("Test");
            assertTrue(sheet.getProtect());
            sheet.protectSheet(null);
            assertFalse(sheet.getProtect());
        }
    }

    @Test
    void createFreezePane() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            // create a workbook
            Sheet sheet = wb.createSheet();
            assertNull(sheet.getPaneInformation());
            sheet.createFreezePane(0, 0);
            // still null
            assertNull(sheet.getPaneInformation());

            sheet.createFreezePane(2, 3);

            PaneInformation info = sheet.getPaneInformation();


            assertEquals(PaneInformation.PANE_LOWER_RIGHT, info.getActivePane());
            assertEquals(3, info.getHorizontalSplitPosition());
            assertEquals(3, info.getHorizontalSplitTopRow());
            assertEquals(2, info.getVerticalSplitLeftColumn());
            assertEquals(2, info.getVerticalSplitPosition());

            sheet.createFreezePane(0, 0);
            // If both colSplit and rowSplit are zero then the existing freeze pane is removed
            assertNull(sheet.getPaneInformation());

            sheet.createFreezePane(0, 3);

            info = sheet.getPaneInformation();

            assertEquals(PaneInformation.PANE_LOWER_LEFT, info.getActivePane());
            assertEquals(3, info.getHorizontalSplitPosition());
            assertEquals(3, info.getHorizontalSplitTopRow());
            assertEquals(0, info.getVerticalSplitLeftColumn());
            assertEquals(0, info.getVerticalSplitPosition());

            sheet.createFreezePane(3, 0);

            info = sheet.getPaneInformation();

            assertEquals(PaneInformation.PANE_UPPER_RIGHT, info.getActivePane());
            assertEquals(0, info.getHorizontalSplitPosition());
            assertEquals(0, info.getHorizontalSplitTopRow());
            assertEquals(3, info.getVerticalSplitLeftColumn());
            assertEquals(3, info.getVerticalSplitPosition());

            sheet.createFreezePane(0, 0);
            // If both colSplit and rowSplit are zero then the existing freeze pane is removed
            assertNull(sheet.getPaneInformation());
        }
    }


    @Test
    void getRepeatingRowsAndColumns() throws IOException {
        try (Workbook wb = _testDataProvider.openSampleWorkbook(
            "RepeatingRowsCols."
            + _testDataProvider.getStandardFileNameExtension())) {

            checkRepeatingRowsAndColumns(wb.getSheetAt(0), null, null);
            checkRepeatingRowsAndColumns(wb.getSheetAt(1), "1:1", null);
            checkRepeatingRowsAndColumns(wb.getSheetAt(2), null, "A:A");
            checkRepeatingRowsAndColumns(wb.getSheetAt(3), "2:3", "A:B");
        }
    }


    @Test
    void setRepeatingRowsAndColumnsBug47294() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Sheet sheet1 = wb.createSheet();
            sheet1.setRepeatingRows(CellRangeAddress.valueOf("1:4"));
            assertEquals("1:4", sheet1.getRepeatingRows().formatAsString());

            //must handle sheets with quotas, see Bugzilla #47294
            Sheet sheet2 = wb.createSheet("My' Sheet");
            sheet2.setRepeatingRows(CellRangeAddress.valueOf("1:4"));
            assertEquals("1:4", sheet2.getRepeatingRows().formatAsString());
        }
    }

    @Test
    void setRepeatingRowsAndColumns() throws IOException {
        try (Workbook wb1 = _testDataProvider.createWorkbook()) {
            Sheet sheet1 = wb1.createSheet("Sheet1");
            Sheet sheet2 = wb1.createSheet("Sheet2");
            Sheet sheet3 = wb1.createSheet("Sheet3");

            checkRepeatingRowsAndColumns(sheet1, null, null);

            sheet1.setRepeatingRows(CellRangeAddress.valueOf("4:5"));
            sheet2.setRepeatingColumns(CellRangeAddress.valueOf("A:C"));
            sheet3.setRepeatingRows(CellRangeAddress.valueOf("1:4"));
            sheet3.setRepeatingColumns(CellRangeAddress.valueOf("A:A"));

            checkRepeatingRowsAndColumns(sheet1, "4:5", null);
            checkRepeatingRowsAndColumns(sheet2, null, "A:C");
            checkRepeatingRowsAndColumns(sheet3, "1:4", "A:A");

            // write out, read back, and test refrain...
            try (Workbook wb2 = _testDataProvider.writeOutAndReadBack(wb1)) {
                sheet1 = wb2.getSheetAt(0);
                sheet2 = wb2.getSheetAt(1);
                sheet3 = wb2.getSheetAt(2);

                checkRepeatingRowsAndColumns(sheet1, "4:5", null);
                checkRepeatingRowsAndColumns(sheet2, null, "A:C");
                checkRepeatingRowsAndColumns(sheet3, "1:4", "A:A");

                // check removing repeating rows and columns
                sheet3.setRepeatingRows(null);
                checkRepeatingRowsAndColumns(sheet3, null, "A:A");

                sheet3.setRepeatingColumns(null);
                checkRepeatingRowsAndColumns(sheet3, null, null);
            }
        }
    }

    private void checkRepeatingRowsAndColumns(
        Sheet s, String expectedRows, String expectedCols) {
        if (expectedRows == null) {
            assertNull(s.getRepeatingRows());
        } else {
            assertEquals(expectedRows, s.getRepeatingRows().formatAsString());
        }
        if (expectedCols == null) {
            assertNull(s.getRepeatingColumns());
        } else {
            assertEquals(expectedCols, s.getRepeatingColumns().formatAsString());
        }
    }

    @Test
    void baseZoom() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Sheet sheet = wb.createSheet();

            // here we can only verify that setting some zoom values works,
            // range-checking is different between the implementations
            sheet.setZoom(75);
        }
    }

    @Test
    void baseShowInPane() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Sheet sheet = wb.createSheet();
            sheet.showInPane(2, 3);
        }
    }

    @Test
    void bug55723() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Sheet sheet = wb.createSheet();

            CellRangeAddress range = CellRangeAddress.valueOf("A:B");
            AutoFilter filter = sheet.setAutoFilter(range);
            assertNotNull(filter);
            // there seems to be currently no generic way to check the setting...

            range = CellRangeAddress.valueOf("B:C");
            filter = sheet.setAutoFilter(range);
            assertNotNull(filter);
            // there seems to be currently no generic way to check the setting...
        }
    }

    @Test
    void bug55723_Rows() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Sheet sheet = wb.createSheet();

            CellRangeAddress range = CellRangeAddress.valueOf("A4:B55000");
            AutoFilter filter = sheet.setAutoFilter(range);
            assertNotNull(filter);
        }
    }

    @Test
    void bug55723d_RowsOver65k() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Sheet sheet = wb.createSheet();

            CellRangeAddress range = CellRangeAddress.valueOf("A4:B75000");
            AutoFilter filter = sheet.setAutoFilter(range);
            assertNotNull(filter);
        }
    }

    /**
     * XSSFSheet autoSizeColumn() on empty RichTextString fails
     */
    @Test
    void bug48325() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Sheet sheet = wb.createSheet("Test");
            trackColumnsForAutoSizingIfSXSSF(sheet);
            CreationHelper factory = wb.getCreationHelper();

            Row row = sheet.createRow(0);
            Cell cell = row.createCell(0);

            Font font = wb.createFont();
            RichTextString rts = factory.createRichTextString("");
            rts.applyFont(font);
            cell.setCellValue(rts);

            sheet.autoSizeColumn(0);

            assertNotNull(_testDataProvider.writeOutAndReadBack(wb));
        }
    }

    @Test
    protected void getCellComment() throws IOException {
        try (Workbook workbook = _testDataProvider.createWorkbook()) {
            Sheet sheet = workbook.createSheet();
            Drawing<?> dg = sheet.createDrawingPatriarch();
            Comment comment = dg.createCellComment(workbook.getCreationHelper().createClientAnchor());
            Cell cell = sheet.createRow(9).createCell(2);
            comment.setAuthor("test C10 author");
            cell.setCellComment(comment);

            CellAddress ref = new CellAddress(9, 2);
            assertNotNull(sheet.getCellComment(ref));
            assertEquals("test C10 author", sheet.getCellComment(ref).getAuthor());

            assertNotNull(_testDataProvider.writeOutAndReadBack(workbook));
        }
    }

    @Test
    void getCellComments() throws IOException {
        try (Workbook wb1 = _testDataProvider.createWorkbook()) {
            Sheet sheet = wb1.createSheet("TEST");

            // a sheet with no cell comments should return an empty map (not null or raise NPE).
            assertTrue(sheet.getCellComments().isEmpty());

            Drawing<?> dg = sheet.createDrawingPatriarch();
            ClientAnchor anchor = wb1.getCreationHelper().createClientAnchor();

            int nRows = 5;
            int nCols = 6;

            for (int r = 0; r < nRows; r++) {
                sheet.createRow(r);
                // Create columns in reverse order
                for (int c = nCols - 1; c >= 0; c--) {
                    // When the comment box is visible, have it show in a 1x3 space
                    anchor.setCol1(c);
                    anchor.setCol2(c);
                    anchor.setRow1(r);
                    anchor.setRow2(r);

                    // Create the comment and set the text-author
                    Comment comment = dg.createCellComment(anchor);
                    Cell cell = sheet.getRow(r).createCell(c);
                    comment.setAuthor("Author " + r);
                    RichTextString text = wb1.getCreationHelper().createRichTextString("Test comment at row=" + r + ", column=" + c);
                    comment.setString(text);

                    // Assign the comment to the cell
                    cell.setCellComment(comment);
                }
            }

            try (Workbook wb2 = _testDataProvider.writeOutAndReadBack(wb1)) {
                Sheet sh = wb2.getSheet("TEST");
                Map<CellAddress, ? extends Comment> cellComments = sh.getCellComments();
                assertEquals(nRows * nCols, cellComments.size());

                for (Entry<CellAddress, ? extends Comment> e : cellComments.entrySet()) {
                    CellAddress ref = e.getKey();
                    Comment aComment = e.getValue();
                    assertEquals("Author " + ref.getRow(), aComment.getAuthor());
                    String text = "Test comment at row=" + ref.getRow() + ", column=" + ref.getColumn();
                    assertEquals(text, aComment.getString().getString());
                }
            }
        }
    }

    @Test
    void getHyperlink() throws IOException {
        try (Workbook workbook = _testDataProvider.createWorkbook()) {
            Hyperlink hyperlink = workbook.getCreationHelper().createHyperlink(HyperlinkType.URL);
            hyperlink.setAddress("https://poi.apache.org/");

            Sheet sheet = workbook.createSheet();
            Cell cell = sheet.createRow(5).createCell(1);

            assertEquals(0, sheet.getHyperlinkList().size(), "list size before add");
            cell.setHyperlink(hyperlink);
            assertEquals(1, sheet.getHyperlinkList().size(), "list size after add");

            assertEquals(hyperlink, sheet.getHyperlinkList().get(0), "list");
            CellAddress B6 = new CellAddress(5, 1);
            assertEquals(hyperlink, sheet.getHyperlink(5, 1), "row, col");
            assertEquals(hyperlink, sheet.getHyperlink(B6), "addr");
            assertNull(sheet.getHyperlink(CellAddress.A1), "no hyperlink at A1");
        }
    }

    @Test
    void removeAllHyperlinks() throws IOException {
        try (Workbook workbook = _testDataProvider.createWorkbook()) {
            Hyperlink hyperlink = workbook.getCreationHelper().createHyperlink(HyperlinkType.URL);
            hyperlink.setAddress("https://poi.apache.org/");
            Sheet sheet = workbook.createSheet();
            Cell cell = sheet.createRow(5).createCell(1);
            cell.setHyperlink(hyperlink);

            assertEquals(1, workbook.getSheetAt(0).getHyperlinkList().size());

            // Save a workbook with a hyperlink
            try (Workbook workbook2 = _testDataProvider.writeOutAndReadBack(workbook)) {
                assertEquals(1, workbook2.getSheetAt(0).getHyperlinkList().size());

                // Remove all hyperlinks from a saved workbook
                workbook2.getSheetAt(0).getRow(5).getCell(1).removeHyperlink();
                assertEquals(0, workbook2.getSheetAt(0).getHyperlinkList().size());

                // Verify that hyperlink was removed from workbook after writing out
                try (Workbook workbook3 = _testDataProvider.writeOutAndReadBack(workbook2)) {
                    assertEquals(0, workbook3.getSheetAt(0).getHyperlinkList().size());
                }
            }
        }
    }


    @Test
    void newMergedRegionAt() throws IOException {
        try (Workbook workbook = _testDataProvider.createWorkbook()) {
            Sheet sheet = workbook.createSheet();
            CellRangeAddress region = CellRangeAddress.valueOf("B2:D4");
            assertEquals(0, sheet.addMergedRegion(region));
            assertEquals("B2:D4", sheet.getMergedRegion(0).formatAsString());
            assertEquals(1, sheet.getNumMergedRegions());

            assertNotNull(_testDataProvider.writeOutAndReadBack(workbook));
        }
    }

    @Test
    void showInPaneManyRowsBug55248() throws IOException {
        try (Workbook wb1 = _testDataProvider.createWorkbook()) {
            Sheet sheet = wb1.createSheet("Sheet 1");

            sheet.showInPane(0, 0);

            for (int i = ROW_COUNT / 2; i < ROW_COUNT; i++) {
                sheet.createRow(i);
                sheet.showInPane(i, 0);
                // this one fails: sheet.showInPane((short)i, 0);
            }

            int i = 0;
            sheet.showInPane(i, i);

            try (Workbook wb2 = _testDataProvider.writeOutAndReadBack(wb1)) {
                checkRowCount(wb2);
            }
        }
    }

    private void checkRowCount(Workbook wb) {
        assertNotNull(wb);
        final Sheet sh = wb.getSheet("Sheet 1");
        assertNotNull(sh);
        assertEquals(ROW_COUNT-1, sh.getLastRowNum());
    }


    @Test
    void testRightToLeft() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Sheet sheet = wb.createSheet();

            assertFalse(sheet.isRightToLeft());
            sheet.setRightToLeft(true);
            assertTrue(sheet.isRightToLeft());
            sheet.setRightToLeft(false);
            assertFalse(sheet.isRightToLeft());
        }
    }

    @Test
    void testNoMergedRegionsIsEmptyList() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Sheet sheet = wb.createSheet();
            assertTrue(sheet.getMergedRegions().isEmpty());
        }
    }

    /**
     * Tests that the setAsActiveCell and getActiveCell function pairs work together
     */
    @Test
    void setActiveCell() throws IOException {
        try (Workbook wb1 = _testDataProvider.createWorkbook()) {
            Sheet sheet = wb1.createSheet();
            CellAddress B42 = new CellAddress("B42");

            // active cell behavior is undefined if not set.
            // HSSFSheet defaults to A1 active cell, while XSSFSheet defaults to null.
            CellAddress ac = sheet.getActiveCell();
            assertTrue(ac == null || CellAddress.A1.equals(ac), "If not set, active cell should default to null or A1");

            sheet.setActiveCell(B42);

            try (Workbook wb2 = _testDataProvider.writeOutAndReadBack(wb1)) {
                sheet = wb2.getSheetAt(0);
                assertEquals(B42, sheet.getActiveCell());
            }
        }
    }


    @Test
    void autoSizeDate() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Sheet s = wb.createSheet("Sheet1");
            Row r = s.createRow(0);
            r.createCell(0).setCellValue(1);
            r.createCell(1).setCellValue(123456);

            // for the streaming-variant we need to enable autosize-tracking to make it work
            trackColumnsForAutoSizingIfSXSSF(s);

            // Will be sized fairly small
            s.autoSizeColumn((short) 0);
            s.autoSizeColumn((short) 1);

            // Size ranges due to different fonts on different machines
            assertBetween("Single number column width", s.getColumnWidth(0), 350, 570);
            assertBetween("6 digit number column width", s.getColumnWidth(1), 1500, 2100);

            // Set a date format
            CellStyle cs = wb.createCellStyle();
            DataFormat f = wb.createDataFormat();
            cs.setDataFormat(f.getFormat("yyyy-mm-dd MMMM hh:mm:ss"));
            r.getCell(0).setCellStyle(cs);
            r.getCell(1).setCellStyle(cs);

            assertTrue(DateUtil.isCellDateFormatted(r.getCell(0)));
            assertTrue(DateUtil.isCellDateFormatted(r.getCell(1)));

            // Should get much bigger now
            s.autoSizeColumn((short) 0);
            s.autoSizeColumn((short) 1);

            assertBetween("Date column width", s.getColumnWidth(0), 4750, 7300);
            assertBetween("Date column width", s.getColumnWidth(1), 4750, 7300);
        }
    }
}
