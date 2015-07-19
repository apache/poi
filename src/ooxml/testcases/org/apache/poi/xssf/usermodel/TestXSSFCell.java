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

import java.io.IOException;

import org.apache.poi.hssf.HSSFITestDataProvider;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.BaseTestXCell;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.SXSSFITestDataProvider;
import org.apache.poi.xssf.XSSFITestDataProvider;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCell;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STCellType;

/**
 * @author Yegor Kozlov
 */
public final class TestXSSFCell extends BaseTestXCell {

    public TestXSSFCell() {
        super(XSSFITestDataProvider.instance);
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
        Sheet sheet = wb.createSheet();
        Row row = sheet.createRow(0);
        SharedStringsTable sst = wb.getSharedStringSource();
        assertEquals(0, sst.getCount());

        //case 1. cell.setCellValue(new XSSFRichTextString((String)null));
        Cell cell_0 = row.createCell(0);
        RichTextString str = new XSSFRichTextString((String)null);
        assertNull(str.getString());
        cell_0.setCellValue(str);
        assertEquals(0, sst.getCount());
        assertEquals(Cell.CELL_TYPE_BLANK, cell_0.getCellType());

        //case 2. cell.setCellValue((String)null);
        Cell cell_1 = row.createCell(1);
        cell_1.setCellValue((String)null);
        assertEquals(0, sst.getCount());
        assertEquals(Cell.CELL_TYPE_BLANK, cell_1.getCellType());
    }

    public void testFormulaString() throws IOException {
        XSSFWorkbook wb = (XSSFWorkbook)_testDataProvider.createWorkbook();
        try {
            XSSFCell cell = wb.createSheet().createRow(0).createCell(0);
            CTCell ctCell = cell.getCTCell(); //low-level bean holding cell's xml
    
            cell.setCellFormula("A2");
            assertEquals(XSSFCell.CELL_TYPE_FORMULA, cell.getCellType());
            assertEquals("A2", cell.getCellFormula());
            //the value is not set and cell's type='N' which means blank
            assertEquals(STCellType.N, ctCell.getT());
    
            //set cached formula value
            cell.setCellValue("t='str'");
            //we are still of 'formula' type
            assertEquals(XSSFCell.CELL_TYPE_FORMULA, cell.getCellType());
            assertEquals("A2", cell.getCellFormula());
            //cached formula value is set and cell's type='STR'
            assertEquals(STCellType.STR, ctCell.getT());
            assertEquals("t='str'", cell.getStringCellValue());
    
            //now remove the formula, the cached formula result remains
            cell.setCellFormula(null);
            assertEquals(XSSFCell.CELL_TYPE_STRING, cell.getCellType());
            assertEquals(STCellType.STR, ctCell.getT());
            //the line below failed prior to fix of Bug #47889
            assertEquals("t='str'", cell.getStringCellValue());
    
            //revert to a blank cell
            cell.setCellValue((String)null);
            assertEquals(XSSFCell.CELL_TYPE_BLANK, cell.getCellType());
            assertEquals(STCellType.N, ctCell.getT());
            assertEquals("", cell.getStringCellValue());
        } finally {
            wb.close();
        }
    }

    /**
     * Bug 47889: problems when calling XSSFCell.getStringCellValue() on a workbook created in Gnumeric
     */
    public void test47889() {
        XSSFWorkbook wb = (XSSFWorkbook)_testDataProvider.openSampleWorkbook("47889.xlsx");
        XSSFSheet sh = wb.getSheetAt(0);

        XSSFCell cell;

        //try a string cell
        cell = sh.getRow(0).getCell(0);
        assertEquals(XSSFCell.CELL_TYPE_STRING, cell.getCellType());
        assertEquals("a", cell.getStringCellValue());
        assertEquals("a", cell.toString());
        //Gnumeric produces spreadsheets without styles
        //make sure we return null for that instead of throwing OutOfBounds
        assertEquals(null, cell.getCellStyle());

        //try a numeric cell
        cell = sh.getRow(1).getCell(0);
        assertEquals(XSSFCell.CELL_TYPE_NUMERIC, cell.getCellType());
        assertEquals(1.0, cell.getNumericCellValue());
        assertEquals("1.0", cell.toString());
        //Gnumeric produces spreadsheets without styles
        //make sure we return null for that instead of throwing OutOfBounds
        assertEquals(null, cell.getCellStyle());
    }

    public void testMissingRAttribute() {
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet();
        XSSFRow row = sheet.createRow(0);
        XSSFCell a1 = row.createCell(0);
        a1.setCellValue("A1");
        XSSFCell a2 = row.createCell(1);
        a2.setCellValue("B1");
        XSSFCell a4 = row.createCell(4);
        a4.setCellValue("E1");
        XSSFCell a6 = row.createCell(5);
        a6.setCellValue("F1");

        assertCellsWithMissingR(row);

        a2.getCTCell().unsetR();
        a6.getCTCell().unsetR();

        assertCellsWithMissingR(row);

        XSSFWorkbook wbNew = (XSSFWorkbook)_testDataProvider.writeOutAndReadBack(wb);
        row = wbNew.getSheetAt(0).getRow(0);
        assertCellsWithMissingR(row);
    }

    private void assertCellsWithMissingR(XSSFRow row){
        XSSFCell a1 = row.getCell(0);
        assertNotNull(a1);
        XSSFCell a2 = row.getCell(1);
        assertNotNull(a2);
        XSSFCell a5 = row.getCell(4);
        assertNotNull(a5);
        XSSFCell a6 = row.getCell(5);
        assertNotNull(a6);

        assertEquals(6, row.getLastCellNum());
        assertEquals(4, row.getPhysicalNumberOfCells());

        assertEquals("A1", a1.getStringCellValue());
        assertEquals("B1", a2.getStringCellValue());
        assertEquals("E1", a5.getStringCellValue());
        assertEquals("F1", a6.getStringCellValue());

        // even if R attribute is not set,
        // POI is able to re-construct it from column and row indexes
        assertEquals("A1", a1.getReference());
        assertEquals("B1", a2.getReference());
        assertEquals("E1", a5.getReference());
        assertEquals("F1", a6.getReference());
    }

    public void testMissingRAttributeBug54288() {
        // workbook with cells missing the R attribute
        XSSFWorkbook wb = (XSSFWorkbook)_testDataProvider.openSampleWorkbook("54288.xlsx");
        // same workbook re-saved in Excel 2010, the R attribute is updated for every cell with the right value.
        XSSFWorkbook wbRef = (XSSFWorkbook)_testDataProvider.openSampleWorkbook("54288-ref.xlsx");

        XSSFSheet sheet = wb.getSheetAt(0);
        XSSFSheet sheetRef = wbRef.getSheetAt(0);
        assertEquals(sheetRef.getPhysicalNumberOfRows(), sheet.getPhysicalNumberOfRows());

        // Test idea: iterate over cells in the reference worksheet, they all have the R attribute set.
        // For each cell from the reference sheet find the corresponding cell in the problematic file (with missing R)
        // and assert that POI reads them equally:
        DataFormatter formater = new DataFormatter();
        for(Row r : sheetRef){
            XSSFRow rowRef = (XSSFRow)r;
            XSSFRow row = sheet.getRow(rowRef.getRowNum());

            assertEquals("number of cells in row["+row.getRowNum()+"]",
                    rowRef.getPhysicalNumberOfCells(), row.getPhysicalNumberOfCells());

            for(Cell c :  rowRef){
                XSSFCell cellRef = (XSSFCell)c;
                XSSFCell cell = row.getCell(cellRef.getColumnIndex());

                assertEquals(cellRef.getColumnIndex(), cell.getColumnIndex());
                assertEquals(cellRef.getReference(), cell.getReference());

                if(!cell.getCTCell().isSetR()){
                    assertTrue("R must e set in cellRef", cellRef.getCTCell().isSetR());

                    String valRef = formater.formatCellValue(cellRef);
                    String val = formater.formatCellValue(cell);
                    assertEquals(valRef, val);
                }

            }
        }
    }
    
    public void test56170() throws IOException {
        final Workbook wb = XSSFTestDataSamples.openSampleWorkbook("56170.xlsx");
        final XSSFSheet sheet = (XSSFSheet) wb.getSheetAt(0);

        Workbook wbRead = XSSFTestDataSamples.writeOutAndReadBack(wb);
        Cell cell;
        
        // add some contents to table so that the table will need expansion
        Row row = sheet.getRow(0);
        wbRead = XSSFTestDataSamples.writeOutAndReadBack(wb);
        cell = row.createCell(0);
        wbRead = XSSFTestDataSamples.writeOutAndReadBack(wb);
        cell.setCellValue("demo1");
        wbRead = XSSFTestDataSamples.writeOutAndReadBack(wb);
        cell = row.createCell(1);
        wbRead = XSSFTestDataSamples.writeOutAndReadBack(wb);
        cell.setCellValue("demo2");
        wbRead = XSSFTestDataSamples.writeOutAndReadBack(wb);
        cell = row.createCell(2);
        wbRead = XSSFTestDataSamples.writeOutAndReadBack(wb);
        cell.setCellValue("demo3");

        wbRead = XSSFTestDataSamples.writeOutAndReadBack(wb);
        
        row = sheet.getRow(1);
        cell = row.createCell(0);
        cell.setCellValue("demo1");
        cell = row.createCell(1);
        cell.setCellValue("demo2");
        cell = row.createCell(2);
        cell.setCellValue("demo3");

        wbRead = XSSFTestDataSamples.writeOutAndReadBack(wb);
        
        // expand table
        XSSFTable table = sheet.getTables().get(0);
        final CellReference startRef = table.getStartCellReference();
        final CellReference endRef = table.getEndCellReference();
        table.getCTTable().setRef(new CellRangeAddress(startRef.getRow(), 1, startRef.getCol(), endRef.getCol()).formatAsString());

        wbRead = XSSFTestDataSamples.writeOutAndReadBack(wb);
        assertNotNull(wbRead);

        /*FileOutputStream stream = new FileOutputStream("c:\\temp\\output.xlsx");
        workbook.write(stream);
        stream.close();*/
    }
    
    public void test56170Reproduce() throws IOException {
        final Workbook wb = new XSSFWorkbook();
        try {
            final Sheet sheet = wb.createSheet();
            Row row = sheet.createRow(0);
            
            // by creating Cells out of order we trigger the handling in onDocumentWrite()
            Cell cell1 = row.createCell(1);
            Cell cell2 = row.createCell(0);
    
            validateRow(row);
            
            validateRow(row);
    
            // once again with removing one cell
            row.removeCell(cell1);
    
            validateRow(row);
    
            // once again with removing one cell
            row.removeCell(cell1);
    
            // now check again
            validateRow(row);
    
            // once again with removing one cell
            row.removeCell(cell2);
    
            // now check again
            validateRow(row);
        } finally {
            wb.close();
        }
    }

    private void validateRow(Row row) {
        // trigger bug with CArray handling
        ((XSSFRow)row).onDocumentWrite();
        
        for(Cell cell : row) {
            cell.toString();
        }
    }    

    public void testBug56644ReturnNull() throws IOException {
        Workbook wb = XSSFTestDataSamples.openSampleWorkbook("56644.xlsx");
        try {
            wb.setMissingCellPolicy(Row.RETURN_BLANK_AS_NULL);
            Sheet sheet = wb.getSheet("samplelist");
            Row row = sheet.getRow(20);
            row.createCell(2);
        } finally {
            wb.close();
        }
    }

    public void testBug56644ReturnBlank() throws IOException {
        Workbook wb = XSSFTestDataSamples.openSampleWorkbook("56644.xlsx");
        try {
            wb.setMissingCellPolicy(Row.RETURN_NULL_AND_BLANK);
            Sheet sheet = wb.getSheet("samplelist");
            Row row = sheet.getRow(20);
            row.createCell(2);
        } finally {
            wb.close();
        }
    }

    public void testBug56644CreateBlank() throws IOException {
        Workbook wb = XSSFTestDataSamples.openSampleWorkbook("56644.xlsx");
        try {
            wb.setMissingCellPolicy(Row.CREATE_NULL_AS_BLANK);
            Sheet sheet = wb.getSheet("samplelist");
            Row row = sheet.getRow(20);
            row.createCell(2);
        } finally {
            wb.close();
        }
    }

    public void testEncodingbeloAscii(){
        StringBuffer sb = new StringBuffer();
        // test all possible characters
        for(int i = 0; i < Character.MAX_VALUE; i++) {
        	sb.append((char)i);
        }

        String strAll = sb.toString();

        // process in chunks as we have a limit on size of column now
        int pos = 0;
        while(pos < strAll.length()) {
        	String str = strAll.substring(pos, Math.min(strAll.length(), pos+SpreadsheetVersion.EXCEL2007.getMaxTextLength()));
        	
            Workbook wb = HSSFITestDataProvider.instance.createWorkbook();
            Cell cell = wb.createSheet().createRow(0).createCell(0);
            
            Workbook xwb = XSSFITestDataProvider.instance.createWorkbook();
            Cell xCell = xwb.createSheet().createRow(0).createCell(0);

            Workbook swb = SXSSFITestDataProvider.instance.createWorkbook();
            Cell sCell = swb.createSheet().createRow(0).createCell(0);

        	cell.setCellValue(str);
        	assertEquals(str, cell.getStringCellValue());
        	xCell.setCellValue(str);
        	assertEquals(str, xCell.getStringCellValue());
        	sCell.setCellValue(str);
        	assertEquals(str, sCell.getStringCellValue());
        	
        	Workbook wbBack = HSSFITestDataProvider.instance.writeOutAndReadBack(wb);
        	Workbook xwbBack = XSSFITestDataProvider.instance.writeOutAndReadBack(xwb);
        	Workbook swbBack = SXSSFITestDataProvider.instance.writeOutAndReadBack(swb);
        	cell = wbBack.getSheetAt(0).createRow(0).createCell(0);
        	xCell = xwbBack.getSheetAt(0).createRow(0).createCell(0);
        	sCell = swbBack.getSheetAt(0).createRow(0).createCell(0);
        	
        	assertEquals(cell.getStringCellValue(), xCell.getStringCellValue());
        	assertEquals(cell.getStringCellValue(), sCell.getStringCellValue());
        	
        	pos += SpreadsheetVersion.EXCEL97.getMaxTextLength();
        }
    }
}
