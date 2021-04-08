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
package org.apache.poi.xssf.streaming;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Stream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.SheetUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;


/**
 * Tests the auto-sizing behaviour of {@link SXSSFSheet} when not all
 * rows fit into the memory window size etc.
 *
 * see Bug #57450 which reported the original misbehaviour
 */
class TestSXSSFSheetAutoSizeColumn {

    private static final String SHORT_CELL_VALUE = "Ben";
    private static final String LONG_CELL_VALUE = "B Be Ben Beni Benif Benify Benif Beni Ben Be B";

    // Approximate threshold to decide whether test is PASS or FAIL:
    //  shortCellValue ends up with approx column width 1_000 (on my machine),
    //  longCellValue ends up with approx. column width 10_000 (on my machine)
    //  so shortCellValue can be expected to be < 5000 for all fonts
    //  and longCellValue can be expected to be > 5000 for all fonts
    private static final int COLUMN_WIDTH_THRESHOLD_BETWEEN_SHORT_AND_LONG = 4000;
    private static final int MAX_COLUMN_WIDTH = 255*256;

    private static final SortedSet<Integer> columns;
    static {
        SortedSet<Integer>_columns = new TreeSet<>();
        _columns.add(0);
        _columns.add(1);
        _columns.add(3);
        columns = Collections.unmodifiableSortedSet(_columns);
    }


    private SXSSFSheet sheet;
    private SXSSFWorkbook workbook;

    public static Stream<Arguments> data() {
        return Stream.of(Arguments.of(false), Arguments.of(true));
    }

    @AfterEach
    void tearDownSheetAndWorkbook() throws IOException {
        if (sheet != null) {
            sheet.dispose();
        }
        if (workbook != null) {
            workbook.close();
        }
    }

    @ParameterizedTest
    @MethodSource("data")
    void test_EmptySheet_NoException(boolean useMergedCells) {
        workbook = new SXSSFWorkbook();
        sheet = workbook.createSheet();
        sheet.trackAllColumnsForAutoSizing();

        for (int i = 0; i < 10; i++) {
            int i2 = i;
            assertDoesNotThrow(() -> sheet.autoSizeColumn(i2, useMergedCells));
        }
    }

    @ParameterizedTest
    @MethodSource("data")
    void test_WindowSizeDefault_AllRowsFitIntoWindowSize(boolean useMergedCells) {
        workbook = new SXSSFWorkbook();
        sheet = workbook.createSheet();
        sheet.trackAllColumnsForAutoSizing();

        final Cell cellRow0 = createRowWithCellValues(sheet, 0, LONG_CELL_VALUE);

        assumeRequiredFontsAreInstalled(workbook, cellRow0);

        createRowWithCellValues(sheet, 1, SHORT_CELL_VALUE);

        sheet.autoSizeColumn(0, useMergedCells);

        assertColumnWidthStrictlyWithinRange(sheet.getColumnWidth(0), COLUMN_WIDTH_THRESHOLD_BETWEEN_SHORT_AND_LONG, MAX_COLUMN_WIDTH);
    }

    @ParameterizedTest
    @MethodSource("data")
    void test_WindowSizeEqualsOne_ConsiderFlushedRows(boolean useMergedCells) {
        workbook = new SXSSFWorkbook(null, 1); // Window size 1 so only last row will be in memory
        sheet = workbook.createSheet();
        sheet.trackAllColumnsForAutoSizing();

        final Cell cellRow0 = createRowWithCellValues(sheet, 0, LONG_CELL_VALUE);

        assumeRequiredFontsAreInstalled(workbook, cellRow0);

        createRowWithCellValues(sheet, 1, SHORT_CELL_VALUE);

        sheet.autoSizeColumn(0, useMergedCells);

        assertColumnWidthStrictlyWithinRange(sheet.getColumnWidth(0), COLUMN_WIDTH_THRESHOLD_BETWEEN_SHORT_AND_LONG, MAX_COLUMN_WIDTH);
    }

    @ParameterizedTest
    @MethodSource("data")
    void test_WindowSizeEqualsOne_lastRowIsNotWidest(boolean useMergedCells) {
        workbook = new SXSSFWorkbook(null, 1); // Window size 1 so only last row will be in memory
        sheet = workbook.createSheet();
        sheet.trackAllColumnsForAutoSizing();

        final Cell cellRow0 = createRowWithCellValues(sheet, 0, LONG_CELL_VALUE);

        assumeRequiredFontsAreInstalled(workbook, cellRow0);

        createRowWithCellValues(sheet, 1, SHORT_CELL_VALUE);

        sheet.autoSizeColumn(0, useMergedCells);

        assertColumnWidthStrictlyWithinRange(sheet.getColumnWidth(0), COLUMN_WIDTH_THRESHOLD_BETWEEN_SHORT_AND_LONG, MAX_COLUMN_WIDTH);
    }

    @ParameterizedTest
    @MethodSource("data")
    void test_WindowSizeEqualsOne_lastRowIsWidest(boolean useMergedCells) {
        workbook = new SXSSFWorkbook(null, 1); // Window size 1 so only last row will be in memory
        sheet = workbook.createSheet();
        sheet.trackAllColumnsForAutoSizing();

        final Cell cellRow0 = createRowWithCellValues(sheet, 0, SHORT_CELL_VALUE);

        assumeRequiredFontsAreInstalled(workbook, cellRow0);

        createRowWithCellValues(sheet, 1, LONG_CELL_VALUE);

        sheet.autoSizeColumn(0, useMergedCells);

        assertColumnWidthStrictlyWithinRange(sheet.getColumnWidth(0), COLUMN_WIDTH_THRESHOLD_BETWEEN_SHORT_AND_LONG, MAX_COLUMN_WIDTH);
    }

    // fails only for useMergedCell=true
    @ParameterizedTest
    @MethodSource("data")
    void test_WindowSizeEqualsOne_flushedRowHasMergedCell(boolean useMergedCells) {
        workbook = new SXSSFWorkbook(null, 1); // Window size 1 so only last row will be in memory
        sheet = workbook.createSheet();
        sheet.trackAllColumnsForAutoSizing();

        Cell a1 = createRowWithCellValues(sheet, 0, LONG_CELL_VALUE);

        assumeRequiredFontsAreInstalled(workbook, a1);
        assertEquals(0, sheet.addMergedRegion(CellRangeAddress.valueOf("A1:B1")));

        createRowWithCellValues(sheet, 1, SHORT_CELL_VALUE, SHORT_CELL_VALUE);

        /*
         *    A      B
         * 1 LONGMERGED
         * 2 SHORT SHORT
         */

        sheet.autoSizeColumn(0, useMergedCells);
        sheet.autoSizeColumn(1, useMergedCells);

        if (useMergedCells) {
            // Excel and LibreOffice behavior: ignore merged cells for auto-sizing.
            // POI behavior: evenly distribute the column width among the merged columns.
            //               each column must be auto-sized in order for the column widths
            //               to add up to the best fit width.
            final int colspan = 2;
            final int expectedWidth = (10000 + 1000)/colspan; //average of 1_000 and 10_000
            final int minExpectedWidth = expectedWidth / 2;
            final int maxExpectedWidth = expectedWidth * 3 / 2;
            assertColumnWidthStrictlyWithinRange(sheet.getColumnWidth(0), minExpectedWidth, maxExpectedWidth); //short
        } else {
            assertColumnWidthStrictlyWithinRange(sheet.getColumnWidth(0), COLUMN_WIDTH_THRESHOLD_BETWEEN_SHORT_AND_LONG, MAX_COLUMN_WIDTH); //long
        }
        assertColumnWidthStrictlyWithinRange(sheet.getColumnWidth(1), 0, COLUMN_WIDTH_THRESHOLD_BETWEEN_SHORT_AND_LONG); //short
    }

    @ParameterizedTest
    @MethodSource("data")
    void autoSizeColumn_trackColumnForAutoSizing(boolean useMergedCells) {
        workbook = new SXSSFWorkbook();
        sheet = workbook.createSheet();
        sheet.trackColumnForAutoSizing(0);

        SortedSet<Integer> expected = new TreeSet<>();
        expected.add(0);
        assertEquals(expected, sheet.getTrackedColumnsForAutoSizing());

        sheet.autoSizeColumn(0, useMergedCells);
        assertThrows(IllegalStateException.class, () -> sheet.autoSizeColumn(1, useMergedCells),
            "Should not be able to auto-size an untracked column");
    }

    @ParameterizedTest
    @MethodSource("data")
    void autoSizeColumn_trackColumnsForAutoSizing(boolean useMergedCells) {
        workbook = new SXSSFWorkbook();
        sheet = workbook.createSheet();

        sheet.trackColumnsForAutoSizing(columns);
        SortedSet<Integer> sorted = new TreeSet<>(columns);
        assertEquals(sorted, sheet.getTrackedColumnsForAutoSizing());

        sheet.autoSizeColumn(sorted.first(), useMergedCells);
        assumeFalse(columns.contains(5));

        assertThrows(IllegalStateException.class, () -> sheet.autoSizeColumn(5, useMergedCells),
            "Should not be able to auto-size an untracked column");
    }

    @ParameterizedTest
    @MethodSource("data")
    void autoSizeColumn_untrackColumnForAutoSizing(boolean useMergedCells) {
        workbook = new SXSSFWorkbook();
        sheet = workbook.createSheet();

        sheet.trackColumnsForAutoSizing(columns);
        sheet.untrackColumnForAutoSizing(columns.first());

        assumeTrue(sheet.getTrackedColumnsForAutoSizing().contains(columns.last()));
        sheet.autoSizeColumn(columns.last(), useMergedCells);
        assumeFalse(sheet.getTrackedColumnsForAutoSizing().contains(columns.first()));

        assertThrows(IllegalStateException.class, () -> sheet.autoSizeColumn(columns.first(), useMergedCells),
            "Should not be able to auto-size an untracked column");
    }

    @ParameterizedTest
    @MethodSource("data")
    void autoSizeColumn_untrackColumnsForAutoSizing(boolean useMergedCells) {
        workbook = new SXSSFWorkbook();
        sheet = workbook.createSheet();

        sheet.trackColumnForAutoSizing(15);
        sheet.trackColumnsForAutoSizing(columns);
        sheet.untrackColumnsForAutoSizing(columns);

        assumeTrue(sheet.getTrackedColumnsForAutoSizing().contains(15));
        sheet.autoSizeColumn(15, useMergedCells);
        assumeFalse(sheet.getTrackedColumnsForAutoSizing().contains(columns.first()));

        assertThrows(IllegalStateException.class, () -> sheet.autoSizeColumn(columns.first(), useMergedCells),
            "Should not be able to auto-size an untracked column");
    }

    @Test
    void autoSizeColumn_isColumnTrackedForAutoSizing() {
        workbook = new SXSSFWorkbook();
        sheet = workbook.createSheet();

        sheet.trackColumnsForAutoSizing(columns);
        for (int column : columns) {
            assertTrue(sheet.isColumnTrackedForAutoSizing(column));

            assumeFalse(columns.contains(column+10));
            assertFalse(sheet.isColumnTrackedForAutoSizing(column+10));
        }
    }

    @ParameterizedTest
    @MethodSource("data")
    void autoSizeColumn_trackAllColumns(boolean useMergedCells) {
        workbook = new SXSSFWorkbook();
        sheet = workbook.createSheet();

        sheet.trackAllColumnsForAutoSizing();
        sheet.autoSizeColumn(0, useMergedCells);

        sheet.untrackAllColumnsForAutoSizing();
        assertThrows(IllegalStateException.class, () -> sheet.autoSizeColumn(0, useMergedCells),
            "Should not be able to auto-size an implicitly untracked column");
    }

    @ParameterizedTest
    @MethodSource("data")
    void autoSizeColumn_trackAllColumns_explicitUntrackColumn(boolean useMergedCells) {
        workbook = new SXSSFWorkbook();
        sheet = workbook.createSheet();

        sheet.trackColumnsForAutoSizing(columns);
        sheet.trackAllColumnsForAutoSizing();

        boolean untracked = sheet.untrackColumnForAutoSizing(0);
        assertTrue(untracked);
        assertThrows(IllegalStateException.class, () -> sheet.autoSizeColumn(0, useMergedCells),
            "Should not be able to auto-size an explicitly untracked column");
    }


    private static void assumeRequiredFontsAreInstalled(final Workbook workbook, final Cell cell) {
        // autoSize will fail if required fonts are not installed, skip this test then
        Font font = workbook.getFontAt(cell.getCellStyle().getFontIndex());
        assumeTrue(SheetUtil.canComputeColumnWidth(font),
            "Cannot verify autoSizeColumn() because the necessary Fonts are not installed on this machine: " + font);
    }

    private static Cell createRowWithCellValues(final Sheet sheet, final int rowNumber, final String... cellValues) {
        Row row = sheet.createRow(rowNumber);
        int cellIndex = 0;
        Cell firstCell = null;
        for (final String cellValue : cellValues) {
            Cell cell = row.createCell(cellIndex++);
            if (firstCell == null) {
                firstCell = cell;
            }
            cell.setCellValue(cellValue);
        }
        return firstCell;
    }

    private static void assertColumnWidthStrictlyWithinRange(final int actualColumnWidth, final int lowerBoundExclusive, final int upperBoundExclusive) {
        assertTrue(actualColumnWidth > lowerBoundExclusive,
            "Expected a column width greater than " + lowerBoundExclusive + " but found " + actualColumnWidth);
        assertTrue(actualColumnWidth < upperBoundExclusive,
            "Expected column width less than " + upperBoundExclusive + " but found " + actualColumnWidth);
    }

}
