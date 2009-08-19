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

import junit.framework.TestCase;

import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCol;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCols;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorksheet;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTXf;

/**
 * Tests for {@link ColumnHelper}
 *
 */
public final class TestColumnHelper extends TestCase {

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

    public void testSortColumns() {
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
        helper.sortColumns(cols1);
        assertEquals(9, cols1.sizeOfColArray());
        assertEquals(25, cols1.getColArray(8).getMin());
        assertEquals(27, cols1.getColArray(8).getMax());
    }

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

        CTCol col5 = CTCol.Factory.newInstance();
        col5.setMin(4);
        col5.setMax(5);
        helper.addCleanColIntoCols(cols1, col5);
        assertEquals(5, cols1.sizeOfColArray());

        CTCol col6 = CTCol.Factory.newInstance();
        col6.setMin(8);
        col6.setMax(11);
        col6.setHidden(true);
        helper.addCleanColIntoCols(cols1, col6);
        assertEquals(6, cols1.sizeOfColArray());

        CTCol col7 = CTCol.Factory.newInstance();
        col7.setMin(6);
        col7.setMax(8);
        col7.setWidth(17.0);
        helper.addCleanColIntoCols(cols1, col7);
        assertEquals(8, cols1.sizeOfColArray());

        CTCol col8 = CTCol.Factory.newInstance();
        col8.setMin(20);
        col8.setMax(30);
        helper.addCleanColIntoCols(cols1, col8);
        assertEquals(10, cols1.sizeOfColArray());

        CTCol col9 = CTCol.Factory.newInstance();
        col9.setMin(25);
        col9.setMax(27);
        helper.addCleanColIntoCols(cols1, col9);

        // TODO - assert something interesting
        CTCol[] colArray = cols1.getColArray();
        assertEquals(12, colArray.length);
        assertEquals(1, colArray[0].getMin());
        assertEquals(16750, colArray[11].getMax());
    }

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

    public void testGetOrCreateColumn() {
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
    }

    public void testGetSetColDefaultStyle() {
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
        XSSFCellStyle cellStyle = new XSSFCellStyle(0, 0, stylesTable);
        columnHelper.setColDefaultStyle(11, cellStyle);
        assertEquals(0, col_2.getStyle());
        assertEquals(1, columnHelper.getColDefaultStyle(10));
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
}
