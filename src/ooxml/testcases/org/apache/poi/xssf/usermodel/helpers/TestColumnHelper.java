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

package org.apache.poi.xssf.usermodel.helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCol;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCols;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorksheet;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTXf;

/**
 * Tests for {@link ColumnHelper}
 *
 */
public final class TestColumnHelper {

    @Test
    public void testCleanColumns() {
        CTWorksheet worksheet = CTWorksheet.Factory.newInstance();

        CTCols cols1 = worksheet.addNewCols();
        CTCol col1 = cols1.addNewCol();
        col1.setMin(1);
        col1.setMax(1);
        col1.setWidth(88);
        col1.setHidden(true);
        CTCol col2 = cols1.addNewCol();
        col2.setMin(2);
        col2.setMax(3);
        CTCols cols2 = worksheet.addNewCols();
        CTCol col4 = cols2.addNewCol();
        col4.setMin(13);
        col4.setMax(16384);

        // Test cleaning cols
        assertEquals(2, worksheet.sizeOfColsArray());
        int count = countColumns(worksheet);
        assertEquals(16375, count);
        // Clean columns and test a clean worksheet
        ColumnHelper helper = new ColumnHelper(worksheet);
        assertEquals(1, worksheet.sizeOfColsArray());
        count = countColumns(worksheet);
        assertEquals(16375, count);
        // Remember - POI column 0 == OOXML column 1
        assertEquals(88.0, helper.getColumn(0, false).getWidth(), 0.0);
        assertTrue(helper.getColumn(0, false).getHidden());
        assertEquals(0.0, helper.getColumn(1, false).getWidth(), 0.0);
        assertFalse(helper.getColumn(1, false).getHidden());
    }

    @Test
    public void testSortColumns() {
        CTCols cols1 = CTCols.Factory.newInstance();
        CTCol col1 = cols1.addNewCol();
        col1.setMin(1);
        col1.setMax(1);
        col1.setWidth(88);
        col1.setHidden(true);
        CTCol col2 = cols1.addNewCol();
        col2.setMin(2);
        col2.setMax(3);
        CTCol col3 = cols1.addNewCol();
        col3.setMin(13);
        col3.setMax(16750);
        assertEquals(3, cols1.sizeOfColArray());
        CTCol col4 = cols1.addNewCol();
        col4.setMin(8);
        col4.setMax(11);
        assertEquals(4, cols1.sizeOfColArray());
        CTCol col5 = cols1.addNewCol();
        col5.setMin(4);
        col5.setMax(5);
        assertEquals(5, cols1.sizeOfColArray());
        CTCol col6 = cols1.addNewCol();
        col6.setMin(8);
        col6.setMax(9);
        col6.setHidden(true);
        CTCol col7 = cols1.addNewCol();
        col7.setMin(6);
        col7.setMax(8);
        col7.setWidth(17.0);
        CTCol col8 = cols1.addNewCol();
        col8.setMin(25);
        col8.setMax(27);
        CTCol col9 = cols1.addNewCol();
        col9.setMin(20);
        col9.setMax(30);
        assertEquals(9, cols1.sizeOfColArray());
        assertEquals(20, cols1.getColArray(8).getMin());
        assertEquals(30, cols1.getColArray(8).getMax());
        ColumnHelper.sortColumns(cols1);
        assertEquals(9, cols1.sizeOfColArray());
        assertEquals(25, cols1.getColArray(8).getMin());
        assertEquals(27, cols1.getColArray(8).getMax());
    }

    @Test
    public void testCloneCol() {
        CTWorksheet worksheet = CTWorksheet.Factory.newInstance();
        ColumnHelper helper = new ColumnHelper(worksheet);

        CTCols cols = CTCols.Factory.newInstance();
        CTCol col = CTCol.Factory.newInstance();
        col.setMin(2);
        col.setMax(8);
        col.setHidden(true);
        col.setWidth(13.4);
        CTCol newCol = helper.cloneCol(cols, col);
        assertEquals(2, newCol.getMin());
        assertEquals(8, newCol.getMax());
        assertTrue(newCol.getHidden());
        assertEquals(13.4, newCol.getWidth(), 0.0);
    }

    @Test
    public void testAddCleanColIntoCols() {
        CTWorksheet worksheet = CTWorksheet.Factory.newInstance();
        ColumnHelper helper = new ColumnHelper(worksheet);

        CTCols cols1 = CTCols.Factory.newInstance();
        CTCol col1 = cols1.addNewCol();
        col1.setMin(1);
        col1.setMax(1);
        col1.setWidth(88);
        col1.setHidden(true);
        CTCol col2 = cols1.addNewCol();
        col2.setMin(2);
        col2.setMax(3);
        CTCol col3 = cols1.addNewCol();
        col3.setMin(13);
        col3.setMax(16750);
        assertEquals(3, cols1.sizeOfColArray());
        CTCol col4 = cols1.addNewCol();
        col4.setMin(8);
        col4.setMax(9);
        assertEquals(4, cols1.sizeOfColArray());

        // No overlap
        helper.addCleanColIntoCols(cols1, createCol(4, 5));
        assertEquals(5, cols1.sizeOfColArray());

        // Overlaps with 8 - 9 (overlap and after replacements required)
        CTCol col6 = createCol(8, 11);
        col6.setHidden(true);
        helper.addCleanColIntoCols(cols1, col6);
        assertEquals(6, cols1.sizeOfColArray());

        // Overlaps with 8 - 9 (before and overlap replacements required)
        CTCol col7 = createCol(6, 8);
        col7.setWidth(17.0);
        helper.addCleanColIntoCols(cols1, col7);
        assertEquals(8, cols1.sizeOfColArray());

        // Overlaps with 13 - 16750 (before, overlap and after replacements required)
        helper.addCleanColIntoCols(cols1, createCol(20, 30));
        assertEquals(10, cols1.sizeOfColArray());

        // Overlaps with 20 - 30 (before, overlap and after replacements required)
        helper.addCleanColIntoCols(cols1, createCol(25, 27));

        // TODO - assert something interesting
        assertEquals(12, cols1.sizeOfColArray());
        assertEquals(1, cols1.getColArray(0).getMin());
        assertEquals(16750, cols1.getColArray(11).getMax());
    }

    @Test
    public void testAddCleanColIntoColsExactOverlap() {
        CTCols cols = createHiddenAndBestFitColsWithHelper(1, 1, 1, 1);
        assertEquals(1, cols.sizeOfColArray());
        assertMinMaxHiddenBestFit(cols, 0, 1, 1, true, true);
    }

    @Test
    public void testAddCleanColIntoColsOverlapsOverhangingBothSides() {
        CTCols cols = createHiddenAndBestFitColsWithHelper(2, 2, 1, 3);
        assertEquals(3, cols.sizeOfColArray());
        assertMinMaxHiddenBestFit(cols, 0, 1, 1, false, true);
        assertMinMaxHiddenBestFit(cols, 1, 2, 2, true, true);
        assertMinMaxHiddenBestFit(cols, 2, 3, 3, false, true);
    }

    @Test
    public void testAddCleanColIntoColsOverlapsCompletelyNested() {
        CTCols cols = createHiddenAndBestFitColsWithHelper(1, 3, 2, 2);
        assertEquals(3, cols.sizeOfColArray());
        assertMinMaxHiddenBestFit(cols, 0, 1, 1, true, false);
        assertMinMaxHiddenBestFit(cols, 1, 2, 2, true, true);
        assertMinMaxHiddenBestFit(cols, 2, 3, 3, true, false);
    }

    @Test
    public void testAddCleanColIntoColsNewOverlapsOverhangingLeftNotRightExactRight() {
        CTCols cols = createHiddenAndBestFitColsWithHelper(2, 3, 1, 3);
        assertEquals(2, cols.sizeOfColArray());
        assertMinMaxHiddenBestFit(cols, 0, 1, 1, false, true);
        assertMinMaxHiddenBestFit(cols, 1, 2, 3, true, true);
    }

    @Test
    public void testAddCleanColIntoColsNewOverlapsOverhangingRightNotLeftExactLeft() {
        CTCols cols = createHiddenAndBestFitColsWithHelper(1, 2, 1, 3);
        assertEquals(2, cols.sizeOfColArray());
        assertMinMaxHiddenBestFit(cols, 0, 1, 2, true, true);
        assertMinMaxHiddenBestFit(cols, 1, 3, 3, false, true);
    }

    @Test
    public void testAddCleanColIntoColsNewOverlapsOverhangingLeftNotRight() {
        CTCols cols = createHiddenAndBestFitColsWithHelper(2, 3, 1, 2);
        assertEquals(3, cols.sizeOfColArray());
        assertMinMaxHiddenBestFit(cols, 0, 1, 1, false, true);
        assertMinMaxHiddenBestFit(cols, 1, 2, 2, true, true);
        assertMinMaxHiddenBestFit(cols, 2, 3, 3, true, false);
    }

    @Test
    public void testAddCleanColIntoColsNewOverlapsOverhangingRightNotLeft() {
        CTCols cols = createHiddenAndBestFitColsWithHelper(1, 2, 2, 3);
        assertEquals(3, cols.sizeOfColArray());
        assertMinMaxHiddenBestFit(cols, 0, 1, 1, true, false);
        assertMinMaxHiddenBestFit(cols, 1, 2, 2, true, true);
        assertMinMaxHiddenBestFit(cols, 2, 3, 3, false, true);
    }

    /**
     * Creates and adds a hidden column and then a best fit column with the given min/max pairs.
     * Suitable for testing handling of overlap. 
     */
    private static CTCols createHiddenAndBestFitColsWithHelper(int hiddenMin, int hiddenMax, int bestFitMin, int bestFitMax) {
        CTWorksheet worksheet = CTWorksheet.Factory.newInstance();
        ColumnHelper helper = new ColumnHelper(worksheet);
        CTCols cols = worksheet.getColsArray(0);

        CTCol hidden = createCol(hiddenMin, hiddenMax);
        hidden.setHidden(true);
        helper.addCleanColIntoCols(cols, hidden);

        CTCol bestFit = createCol(bestFitMin, bestFitMax);
        bestFit.setBestFit(true);
        helper.addCleanColIntoCols(cols, bestFit);
        return cols;
    }

    private static void assertMinMaxHiddenBestFit(CTCols cols, int index, int min, int max, boolean hidden, boolean bestFit) {
        CTCol col = cols.getColArray(index);
        assertEquals(min, col.getMin());
        assertEquals(max, col.getMax());
        assertEquals(hidden, col.getHidden());
        assertEquals(bestFit, col.getBestFit());
    }

    private static CTCol createCol(int min, int max) {
        CTCol col = CTCol.Factory.newInstance();
        col.setMin(min);
        col.setMax(max);
        return col;
    }

    @Test
    public void testGetColumn() {
        CTWorksheet worksheet = CTWorksheet.Factory.newInstance();

        CTCols cols1 = worksheet.addNewCols();
        CTCol col1 = cols1.addNewCol();
        col1.setMin(1);
        col1.setMax(1);
        col1.setWidth(88);
        col1.setHidden(true);
        CTCol col2 = cols1.addNewCol();
        col2.setMin(2);
        col2.setMax(3);
        CTCols cols2 = worksheet.addNewCols();
        CTCol col4 = cols2.addNewCol();
        col4.setMin(3);
        col4.setMax(6);

        // Remember - POI column 0 == OOXML column 1
        ColumnHelper helper = new ColumnHelper(worksheet);
        assertNotNull(helper.getColumn(0, false));
        assertNotNull(helper.getColumn(1, false));
        assertEquals(88.0, helper.getColumn(0, false).getWidth(), 0.0);
        assertEquals(0.0, helper.getColumn(1, false).getWidth(), 0.0);
        assertTrue(helper.getColumn(0, false).getHidden());
        assertFalse(helper.getColumn(1, false).getHidden());
        assertNull(helper.getColumn(99, false));
        assertNotNull(helper.getColumn(5, false));
    }

    @Test
    public void testSetColumnAttributes() {
        CTCol col = CTCol.Factory.newInstance();
        col.setWidth(12);
        col.setHidden(true);
        CTCol newCol = CTCol.Factory.newInstance();
        assertEquals(0.0, newCol.getWidth(), 0.0);
        assertFalse(newCol.getHidden());
        ColumnHelper helper = new ColumnHelper(CTWorksheet.Factory
                .newInstance());
        helper.setColumnAttributes(col, newCol);
        assertEquals(12.0, newCol.getWidth(), 0.0);
        assertTrue(newCol.getHidden());
    }

    @Test
    public void testGetOrCreateColumn() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Sheet 1");
        ColumnHelper columnHelper = sheet.getColumnHelper();

        // Check POI 0 based, OOXML 1 based
        CTCol col = columnHelper.getOrCreateColumn1Based(3, false);
        assertNotNull(col);
        assertNull(columnHelper.getColumn(1, false));
        assertNotNull(columnHelper.getColumn(2, false));
        assertNotNull(columnHelper.getColumn1Based(3, false));
        assertNull(columnHelper.getColumn(3, false));

        CTCol col2 = columnHelper.getOrCreateColumn1Based(30, false);
        assertNotNull(col2);
        assertNull(columnHelper.getColumn(28, false));
        assertNotNull(columnHelper.getColumn(29, false));
        assertNotNull(columnHelper.getColumn1Based(30, false));
        assertNull(columnHelper.getColumn(30, false));

        workbook.close();
    }

    @Test
    public void testGetSetColDefaultStyle() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet();
        CTWorksheet ctWorksheet = sheet.getCTWorksheet();
        ColumnHelper columnHelper = sheet.getColumnHelper();

        // POI column 3, OOXML column 4
        CTCol col = columnHelper.getOrCreateColumn1Based(4, false);

        assertNotNull(col);
        assertNotNull(columnHelper.getColumn(3, false));
        columnHelper.setColDefaultStyle(3, 2);
        assertEquals(2, columnHelper.getColDefaultStyle(3));
        assertEquals(-1, columnHelper.getColDefaultStyle(4));
        StylesTable stylesTable = workbook.getStylesSource();
        CTXf cellXf = CTXf.Factory.newInstance();
        cellXf.setFontId(0);
        cellXf.setFillId(0);
        cellXf.setBorderId(0);
        cellXf.setNumFmtId(0);
        cellXf.setXfId(0);
        stylesTable.putCellXf(cellXf);
        CTCol col_2 = ctWorksheet.getColsArray(0).addNewCol();
        col_2.setMin(10);
        col_2.setMax(12);
        col_2.setStyle(1);
        assertEquals(1, columnHelper.getColDefaultStyle(11));
        XSSFCellStyle cellStyle = new XSSFCellStyle(0, 0, stylesTable, null);
        columnHelper.setColDefaultStyle(11, cellStyle);
        assertEquals(0, col_2.getStyle());
        assertEquals(1, columnHelper.getColDefaultStyle(10));
        
        workbook.close();
    }

    private static int countColumns(CTWorksheet worksheet) {
        int count;
        count = 0;
        for (int i = 0; i < worksheet.sizeOfColsArray(); i++) {
            for (int y = 0; y < worksheet.getColsArray(i).sizeOfColArray(); y++) {
                for (long k = worksheet.getColsArray(i).getColArray(y).getMin(); k <= worksheet
                        .getColsArray(i).getColArray(y).getMax(); k++) {
                    count++;
                }
            }
        }
        return count;
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testColumnsCollapsed() {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("test");
        Row row = sheet.createRow(0);
        row.createCell(0);
        row.createCell(1);
        row.createCell(2);

        sheet.setColumnWidth(0, 10);
        sheet.setColumnWidth(1, 10);
        sheet.setColumnWidth(2, 10);

        sheet.groupColumn(0, 1);
        sheet.setColumnGroupCollapsed(0, true);

        CTCols ctCols = ((XSSFSheet) sheet).getCTWorksheet().getColsArray()[0];
        assertEquals(3, ctCols.sizeOfColArray());
        assertTrue(ctCols.getColArray(0).isSetCollapsed());
        assertTrue(ctCols.getColArray(1).isSetCollapsed());
        assertTrue(ctCols.getColArray(2).isSetCollapsed());

        ColumnHelper helper = new ColumnHelper(CTWorksheet.Factory.newInstance());
        helper.setColumnAttributes(ctCols.getColArray(1), ctCols.getColArray(2));

        ctCols = ((XSSFSheet) sheet).getCTWorksheet().getColsArray()[0];
        assertTrue(ctCols.getColArray(0).isSetCollapsed());
        assertTrue(ctCols.getColArray(1).isSetCollapsed());
        assertTrue(ctCols.getColArray(2).isSetCollapsed());
    }
}
