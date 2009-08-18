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

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.XSSFITestDataProvider;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STCellType;

/**
 * @author Yegor Kozlov
 */
public final class TestXSSFCell extends BaseTestCell {

	public TestXSSFCell() {
		super(XSSFITestDataProvider.getInstance());
	}

    /**
     * Bug 47026: trouble changing cell type when workbook doesn't contain
     * Shared String Table
     */
    public void test47026_1() {
        Workbook source = _testDataProvider.openSampleWorkbook("47026.xlsm");
        Sheet sheet = source.getSheetAt(0);
        Row row = sheet.getRow(0);
        Cell cell = row.getCell(0);
        cell.setCellType(Cell.CELL_TYPE_STRING);
        cell.setCellValue("456");
    }

    public void test47026_2() {
        Workbook source = _testDataProvider.openSampleWorkbook("47026.xlsm");
        Sheet sheet = source.getSheetAt(0);
        Row row = sheet.getRow(0);
        Cell cell = row.getCell(0);
        cell.setCellFormula(null);
        cell.setCellValue("456");
    }

    /**
     * Test that we can read inline strings that are expressed directly in the cell definition
     * instead of implementing the shared string table.
     *
     * Some programs, for example, Microsoft Excel Driver for .xlsx insert inline string
     * instead of using the shared string table. See bug 47206
     */
    public void testInlineString() {
        XSSFWorkbook wb = (XSSFWorkbook)_testDataProvider.openSampleWorkbook("xlsx-jdbc.xlsx");
        XSSFSheet sheet = wb.getSheetAt(0);
        XSSFRow row = sheet.getRow(1);

        XSSFCell cell_0 = row.getCell(0);
        assertEquals(STCellType.INT_INLINE_STR, cell_0.getCTCell().getT().intValue());
        assertTrue(cell_0.getCTCell().isSetIs());
        assertEquals("A Very large string in column 1 AAAAAAAAAAAAAAAAAAAAA", cell_0.getStringCellValue());

        XSSFCell cell_1 = row.getCell(1);
        assertEquals(STCellType.INT_INLINE_STR, cell_1.getCTCell().getT().intValue());
        assertTrue(cell_1.getCTCell().isSetIs());
        assertEquals("foo", cell_1.getStringCellValue());

        XSSFCell cell_2 = row.getCell(2);
        assertEquals(STCellType.INT_INLINE_STR, cell_2.getCTCell().getT().intValue());
        assertTrue(cell_2.getCTCell().isSetIs());
        assertEquals("bar", row.getCell(2).getStringCellValue());
    }

    /**
     *  Bug 47278 -  xsi:nil attribute for <t> tag caused Excel 2007 to fail to open workbook
     */
    public void test47278() {
        XSSFWorkbook wb = (XSSFWorkbook)_testDataProvider.createWorkbook();
        XSSFSheet sheet = wb.createSheet();
        XSSFRow row = sheet.createRow(0);
        SharedStringsTable sst = wb.getSharedStringSource();
        assertEquals(0, sst.getCount());

        //case 1. cell.setCellValue(new XSSFRichTextString((String)null));
        XSSFCell cell_0 = row.createCell(0);
        XSSFRichTextString str = new XSSFRichTextString((String)null);
        assertNull(str.getString());
        cell_0.setCellValue(str);
        assertEquals(0, sst.getCount());
        assertEquals(XSSFCell.CELL_TYPE_BLANK, cell_0.getCellType());

        //case 2. cell.setCellValue((String)null);
        XSSFCell cell_1 = row.createCell(1);
        cell_1.setCellValue((String)null);
        assertEquals(0, sst.getCount());
        assertEquals(XSSFCell.CELL_TYPE_BLANK, cell_1.getCellType());
    }
}
