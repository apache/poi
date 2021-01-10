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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.SheetUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


/**
 * Tests the auto-sizing behaviour of {@link SXSSFSheet} when not all
 * rows fit into the memory window size etc.
 *
 * see Bug #57450 which reported the original misbehaviour
 */
class TestAutoSizeColumnTracker {

    private SXSSFSheet sheet;
    private SXSSFWorkbook workbook;
    private AutoSizeColumnTracker tracker;
    private static final SortedSet<Integer> columns;
    static {
        SortedSet<Integer>_columns = new TreeSet<>();
        _columns.add(0);
        _columns.add(1);
        _columns.add(3);
        columns = Collections.unmodifiableSortedSet(_columns);
    }
    private final static String SHORT_MESSAGE = "short";
    private final static String LONG_MESSAGE = "This is a test of a long message! This is a test of a long message!";

    @BeforeEach
    void setUpSheetAndWorkbook() {
        workbook = new SXSSFWorkbook();
        sheet = workbook.createSheet();
        tracker = new AutoSizeColumnTracker(sheet);
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

    @Test
    void trackAndUntrackColumn() {
        assumeTrue(tracker.getTrackedColumns().isEmpty());
        tracker.trackColumn(0);
        Set<Integer> expected = new HashSet<>();
        expected.add(0);
        assertEquals(expected, tracker.getTrackedColumns());
        tracker.untrackColumn(0);
        assertTrue(tracker.getTrackedColumns().isEmpty());
    }

    @Test
    void trackAndUntrackColumns() {
        assumeTrue(tracker.getTrackedColumns().isEmpty());
        tracker.trackColumns(columns);
        assertEquals(columns, tracker.getTrackedColumns());
        tracker.untrackColumn(3);
        tracker.untrackColumn(0);
        tracker.untrackColumn(1);
        assertTrue(tracker.getTrackedColumns().isEmpty());
        tracker.trackColumn(0);
        tracker.trackColumns(columns);
        tracker.untrackColumn(4);
        assertEquals(columns, tracker.getTrackedColumns());
        tracker.untrackColumns(columns);
        assertTrue(tracker.getTrackedColumns().isEmpty());
    }

    @Test
    void trackAndUntrackAllColumns() {
        createColumnsAndTrackThemAll();
        tracker.untrackAllColumns();
        assertTrue(tracker.getTrackedColumns().isEmpty());
    }

    @Test
    void isColumnTracked() {
        assumeFalse(tracker.isColumnTracked(0));
        tracker.trackColumn(0);
        assertTrue(tracker.isColumnTracked(0));
        tracker.untrackColumn(0);
        assertFalse(tracker.isColumnTracked(0));
    }

    @Test
    void isColumnTrackedAndTrackAllColumns() {
        createColumnsAndTrackThemAll();
        tracker.untrackColumn(0);
        SortedSet<Integer> _newColumns = new TreeSet<>();
        _newColumns.add(1);
        _newColumns.add(3);
        SortedSet<Integer> newColumns = Collections.unmodifiableSortedSet(_newColumns);
        assertEquals(newColumns, tracker.getTrackedColumns());
        assertFalse(tracker.isColumnTracked(0));
    }

    @Test
    void getTrackedColumns() {
        assumeTrue(tracker.getTrackedColumns().isEmpty());

        for (int column : columns) {
            tracker.trackColumn(column);
        }

        assertEquals(3, tracker.getTrackedColumns().size());
        assertEquals(columns, tracker.getTrackedColumns());
    }

    @Test
    void isAllColumnsTracked() {
        assertFalse(tracker.isAllColumnsTracked());
        tracker.trackAllColumns();
        assertTrue(tracker.isAllColumnsTracked());
        tracker.untrackAllColumns();
        assertFalse(tracker.isAllColumnsTracked());
    }

    @Test
    void updateColumnWidths_and_getBestFitColumnWidth() {
        tracker.trackAllColumns();
        Row row1 = sheet.createRow(0);
        Row row2 = sheet.createRow(1);
        // A1, B1, D1
        for (int column : columns) {
            row1.createCell(column).setCellValue(LONG_MESSAGE);
            row2.createCell(column+1).setCellValue(SHORT_MESSAGE);
        }
        tracker.updateColumnWidths(row1);
        tracker.updateColumnWidths(row2);
        assertEquals(0, sheet.addMergedRegion(CellRangeAddress.valueOf("D1:E1")));

        assumeRequiredFontsAreInstalled(workbook, row1.getCell(columns.iterator().next()));

        // Excel 2013 and LibreOffice 4.2.8.2 both treat columns with merged regions as blank
        /*     A     B    C      D   E
         * 1 LONG  LONG        LONGMERGE
         * 2       SHORT SHORT     SHORT
         */

        // measured in Excel 2013. Sizes may vary.
        final int longMsgWidth = (int) (57.43*256);
        final int shortMsgWidth = (int) (4.86*256);

        checkColumnWidth(longMsgWidth, 0, true);
        checkColumnWidth(longMsgWidth, 0, false);
        checkColumnWidth(longMsgWidth, 1, true);
        checkColumnWidth(longMsgWidth, 1, false);
        checkColumnWidth(shortMsgWidth, 2, true);
        checkColumnWidth(shortMsgWidth, 2, false);
        checkColumnWidth(-1, 3, true);
        checkColumnWidth(longMsgWidth, 3, false);
        checkColumnWidth(shortMsgWidth, 4, true); //but is it really? shouldn't autosizing column E use "" from E1 and SHORT from E2?
        checkColumnWidth(shortMsgWidth, 4, false);
    }

    private void checkColumnWidth(int expectedWidth, int column, boolean useMergedCells) {
        final int bestFitWidth = tracker.getBestFitColumnWidth(column, useMergedCells);
        if (bestFitWidth < 0 && expectedWidth < 0) return;
        final double abs_error = Math.abs(bestFitWidth-expectedWidth);
        final double rel_error = abs_error / expectedWidth;
        assertTrue(rel_error <= 0.25, "check column width: " + rel_error + ", " + abs_error + ", " + expectedWidth + ", " + bestFitWidth);
    }

    private static void assumeRequiredFontsAreInstalled(final Workbook workbook, final Cell cell) {
        // autoSize will fail if required fonts are not installed, skip this test then
        Font font = workbook.getFontAt(cell.getCellStyle().getFontIndex());
        assumeTrue(SheetUtil.canComputeColumnWidth(font),
            "Cannot verify autoSizeColumn() because the necessary Fonts are not installed on this machine: " + font);
    }

    private void createColumnsAndTrackThemAll() {
        assumeTrue(tracker.getTrackedColumns().isEmpty());
        tracker.trackAllColumns();
        assertTrue(tracker.getTrackedColumns().isEmpty());

        Row row = sheet.createRow(0);
        for (int column : columns) {
            row.createCell(column);
        }
        // implicitly track the columns
        tracker.updateColumnWidths(row);
        assertEquals(columns, tracker.getTrackedColumns());
    }
}
