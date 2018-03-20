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
import java.util.List;

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.hssf.HSSFITestDataProvider;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.formula.FormulaParseException;
import org.apache.poi.ss.usermodel.BaseTestXCell;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellCopyPolicy;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.SXSSFITestDataProvider;
import org.apache.poi.xssf.XSSFITestDataProvider;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.junit.Test;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCell;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCellFormula;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STCellFormulaType;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STCellType;

import static org.junit.Assert.*;

public final class TestXSSFCell extends BaseTestXCell {

    public TestXSSFCell() {
        super(XSSFITestDataProvider.instance);
    }

    /**
     * Bug 47026: trouble changing cell type when workbook doesn't contain
     * Shared String Table
     */
    @Test
    public void test47026_1() throws IOException {
        Workbook wb = _testDataProvider.openSampleWorkbook("47026.xlsm");
        Sheet sheet = wb.getSheetAt(0);
        Row row = sheet.getRow(0);
        Cell cell = row.getCell(0);
        cell.setCellType(CellType.STRING);
        cell.setCellValue("456");
        wb.close();
    }

    @Test
    public void test47026_2() throws IOException {
        Workbook wb = _testDataProvider.openSampleWorkbook("47026.xlsm");
        Sheet sheet = wb.getSheetAt(0);
        Row row = sheet.getRow(0);
        Cell cell = row.getCell(0);
        cell.setCellFormula(null);
        cell.setCellValue("456");
        wb.close();
    }

    /**
     * Test that we can read inline strings that are expressed directly in the cell definition
     * instead of implementing the shared string table.
     *
     * Some programs, for example, Microsoft Excel Driver for .xlsx insert inline string
     * instead of using the shared string table. See bug 47206
     */
    @Test
    public void testInlineString() throws IOException {
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
        wb.close();
    }

    /**
     *  Bug 47278 -  xsi:nil attribute for <t> tag caused Excel 2007 to fail to open workbook
     */
    @Test
    public void test47278() throws IOException {
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
        assertEquals(CellType.BLANK, cell_0.getCellType());

        //case 2. cell.setCellValue((String)null);
        Cell cell_1 = row.createCell(1);
        cell_1.setCellValue((String)null);
        assertEquals(0, sst.getCount());
        assertEquals(CellType.BLANK, cell_1.getCellType());
        wb.close();
    }

    @Test
    public void testFormulaString() throws IOException {
        try (XSSFWorkbook wb = (XSSFWorkbook) _testDataProvider.createWorkbook()) {
            XSSFCell cell = wb.createSheet().createRow(0).createCell(0);
            CTCell ctCell = cell.getCTCell(); //low-level bean holding cell's xml

            cell.setCellFormula("A2");
            assertEquals(CellType.FORMULA, cell.getCellType());
            assertEquals("A2", cell.getCellFormula());
            //the value is not set and cell's type='N' which means blank
            assertEquals(STCellType.N, ctCell.getT());

            //set cached formula value
            cell.setCellValue("t='str'");
            //we are still of 'formula' type
            assertEquals(CellType.FORMULA, cell.getCellType());
            assertEquals("A2", cell.getCellFormula());
            //cached formula value is set and cell's type='STR'
            assertEquals(STCellType.STR, ctCell.getT());
            assertEquals("t='str'", cell.getStringCellValue());

            //now remove the formula, the cached formula result remains
            cell.setCellFormula(null);
            assertEquals(CellType.STRING, cell.getCellType());
            assertEquals(STCellType.STR, ctCell.getT());
            //the line below failed prior to fix of Bug #47889
            assertEquals("t='str'", cell.getStringCellValue());

            //revert to a blank cell
            cell.setCellValue((String) null);
            assertEquals(CellType.BLANK, cell.getCellType());
            assertEquals(STCellType.N, ctCell.getT());
            assertEquals("", cell.getStringCellValue());

            // check behavior with setCellFormulaValidation
            final String invalidFormula = "A", validFormula = "A2";

            // check that default is true
            assertTrue(wb.getCellFormulaValidation());

            // check that valid formula does not throw exception
            cell.setCellFormula(validFormula);

            // check that invalid formula does throw exception
            try {
                cell.setCellFormula(invalidFormula);
                fail("Should catch exception here");
            } catch (FormulaParseException e) {
                // expected here
            }

            // set cell formula validation to false
            wb.setCellFormulaValidation(false);
            assertFalse(wb.getCellFormulaValidation());

            // check that neither valid nor invalid formula throw an exception
            cell.setCellFormula(validFormula);
            cell.setCellFormula(invalidFormula);
        }
    }

    /**
     * Bug 47889: problems when calling XSSFCell.getStringCellValue() on a workbook created in Gnumeric
     */
    @Test
    public void test47889() throws IOException {
        XSSFWorkbook wb = (XSSFWorkbook)_testDataProvider.openSampleWorkbook("47889.xlsx");
        XSSFSheet sh = wb.getSheetAt(0);

        XSSFCell cell;

        //try a string cell
        cell = sh.getRow(0).getCell(0);
        assertEquals(CellType.STRING, cell.getCellType());
        assertEquals("a", cell.getStringCellValue());
        assertEquals("a", cell.toString());
        //Gnumeric produces spreadsheets without styles
        //make sure we return null for that instead of throwing OutOfBounds
        assertEquals(null, cell.getCellStyle());

        //try a numeric cell
        cell = sh.getRow(1).getCell(0);
        assertEquals(CellType.NUMERIC, cell.getCellType());
        assertEquals(1.0, cell.getNumericCellValue(), 0);
        assertEquals("1.0", cell.toString());
        //Gnumeric produces spreadsheets without styles
        //make sure we return null for that instead of throwing OutOfBounds
        assertEquals(null, cell.getCellStyle());
        wb.close();
    }

    @Test
    public void testMissingRAttribute() throws IOException {
        XSSFWorkbook wb1 = new XSSFWorkbook();
        XSSFSheet sheet = wb1.createSheet();
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

        XSSFWorkbook wb2 = (XSSFWorkbook)_testDataProvider.writeOutAndReadBack(wb1);
        row = wb2.getSheetAt(0).getRow(0);
        assertCellsWithMissingR(row);
        
        wb2.close();
        wb1.close();
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

    @Test
    public void testMissingRAttributeBug54288() throws IOException {
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
        wbRef.close();
        wb.close();
    }
    
    @Test
    public void test56170() throws IOException {
        final Workbook wb1 = XSSFTestDataSamples.openSampleWorkbook("56170.xlsx");
        final XSSFSheet sheet = (XSSFSheet) wb1.getSheetAt(0);

        Workbook wb2 = XSSFTestDataSamples.writeOutAndReadBack(wb1);
        Cell cell;
        
        // add some contents to table so that the table will need expansion
        Row row = sheet.getRow(0);
        Workbook wb3 = XSSFTestDataSamples.writeOutAndReadBack(wb2);
        cell = row.createCell(0);
        Workbook wb4 = XSSFTestDataSamples.writeOutAndReadBack(wb3);
        cell.setCellValue("demo1");
        Workbook wb5 = XSSFTestDataSamples.writeOutAndReadBack(wb4);
        cell = row.createCell(1);
        Workbook wb6 = XSSFTestDataSamples.writeOutAndReadBack(wb5);
        cell.setCellValue("demo2");
        Workbook wb7 = XSSFTestDataSamples.writeOutAndReadBack(wb6);
        cell = row.createCell(2);
        Workbook wb8 = XSSFTestDataSamples.writeOutAndReadBack(wb7);
        cell.setCellValue("demo3");

        Workbook wb9 = XSSFTestDataSamples.writeOutAndReadBack(wb8);
        
        row = sheet.getRow(1);
        cell = row.createCell(0);
        cell.setCellValue("demo1");
        cell = row.createCell(1);
        cell.setCellValue("demo2");
        cell = row.createCell(2);
        cell.setCellValue("demo3");

        Workbook wb10 = XSSFTestDataSamples.writeOutAndReadBack(wb9);
        
        // expand table
        XSSFTable table = sheet.getTables().get(0);
        final CellReference startRef = table.getStartCellReference();
        final CellReference endRef = table.getEndCellReference();
        table.getCTTable().setRef(new CellRangeAddress(startRef.getRow(), 1, startRef.getCol(), endRef.getCol()).formatAsString());

        Workbook wb11 = XSSFTestDataSamples.writeOutAndReadBack(wb10);
        assertNotNull(wb11);

        wb11.close();
        wb10.close();
        wb9.close();
        wb8.close();
        wb7.close();
        wb6.close();
        wb5.close();
        wb4.close();
        wb3.close();
        wb2.close();
        wb1.close();
    }
    
    @Test
    public void test56170Reproduce() throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
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
        }
    }

    private void validateRow(Row row) {
        // trigger bug with CArray handling
        ((XSSFRow)row).onDocumentWrite();
        
        for(Cell cell : row) {
            assertNotNull(cell.toString());
        }
    }    

    @Test
    public void testBug56644ReturnNull() throws IOException {
        try (Workbook wb = XSSFTestDataSamples.openSampleWorkbook("56644.xlsx")) {
            wb.setMissingCellPolicy(MissingCellPolicy.RETURN_BLANK_AS_NULL);
            Sheet sheet = wb.getSheet("samplelist");
            Row row = sheet.getRow(20);
            row.createCell(2);
        }
    }

    @Test
    public void testBug56644ReturnBlank() throws IOException {
        try (Workbook wb = XSSFTestDataSamples.openSampleWorkbook("56644.xlsx")) {
            wb.setMissingCellPolicy(MissingCellPolicy.RETURN_NULL_AND_BLANK);
            Sheet sheet = wb.getSheet("samplelist");
            Row row = sheet.getRow(20);
            row.createCell(2);
        }
    }

    @Test
    public void testBug56644CreateBlank() throws IOException {
        try (Workbook wb = XSSFTestDataSamples.openSampleWorkbook("56644.xlsx")) {
            wb.setMissingCellPolicy(MissingCellPolicy.CREATE_NULL_AS_BLANK);
            Sheet sheet = wb.getSheet("samplelist");
            Row row = sheet.getRow(20);
            row.createCell(2);
        }
    }

    @Test
    public void testEncodingBelowAscii() throws IOException {
        StringBuilder sb = new StringBuilder();
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

        	swbBack.close();
        	xwbBack.close();
        	wbBack.close();
        	swb.close();
        	xwb.close();
        	wb.close();
        }
    }

    private XSSFCell srcCell, destCell; //used for testCopyCellFrom_CellCopyPolicy
    
    @Test
    public final void testCopyCellFrom_CellCopyPolicy_default() {
        setUp_testCopyCellFrom_CellCopyPolicy();
        
        // default copy policy
        final CellCopyPolicy policy = new CellCopyPolicy();
        destCell.copyCellFrom(srcCell, policy);
        
        assertEquals(CellType.FORMULA, destCell.getCellType());
        assertEquals("2+3", destCell.getCellFormula());
        assertEquals(srcCell.getCellStyle(), destCell.getCellStyle());
    }
    
    @Test
    public final void testCopyCellFrom_CellCopyPolicy_value() {
        setUp_testCopyCellFrom_CellCopyPolicy();
        
        // Paste values only
        final CellCopyPolicy policy = new CellCopyPolicy.Builder().cellFormula(false).build();
        destCell.copyCellFrom(srcCell, policy);
        assertEquals(CellType.NUMERIC, destCell.getCellType());
    }
    
    @Test
    public final void testCopyCellFrom_CellCopyPolicy_formulaWithUnregisteredUDF() {
        setUp_testCopyCellFrom_CellCopyPolicy();
        
        srcCell.setCellFormula("MYFUNC2(123, $A5, Sheet1!$B7)");
        
        // Copy formula verbatim (no shifting). This is okay because copyCellFrom is Internal.
        // Users should use higher-level copying functions to row- or column-shift formulas.
        final CellCopyPolicy policy = new CellCopyPolicy.Builder().cellFormula(true).build();
        destCell.copyCellFrom(srcCell, policy);
        assertEquals("MYFUNC2(123, $A5, Sheet1!$B7)", destCell.getCellFormula());
    }
    
    @Test
    public final void testCopyCellFrom_CellCopyPolicy_style() {
        setUp_testCopyCellFrom_CellCopyPolicy();
        srcCell.setCellValue((String) null);
        
        // Paste styles only
        final CellCopyPolicy policy = new CellCopyPolicy.Builder().cellValue(false).build();
        destCell.copyCellFrom(srcCell, policy);
        assertEquals(srcCell.getCellStyle(), destCell.getCellStyle());
        
        // Old cell value should not have been overwritten
        assertNotEquals(CellType.BLANK, destCell.getCellType());
        assertEquals(CellType.BOOLEAN, destCell.getCellType());
        assertEquals(true, destCell.getBooleanCellValue());
    }
    
    @Test
    public final void testCopyCellFrom_CellCopyPolicy_copyHyperlink() throws IOException {
        setUp_testCopyCellFrom_CellCopyPolicy();
        final Workbook wb = srcCell.getSheet().getWorkbook();
        final CreationHelper createHelper = wb.getCreationHelper();

        srcCell.setCellValue("URL LINK");
        Hyperlink link = createHelper.createHyperlink(HyperlinkType.URL);
        link.setAddress("http://poi.apache.org/");
        srcCell.setHyperlink(link);

        // Set link cell style (optional)
        CellStyle hlinkStyle = wb.createCellStyle();
        Font hlinkFont = wb.createFont();
        hlinkFont.setUnderline(Font.U_SINGLE);
        hlinkFont.setColor(IndexedColors.BLUE.getIndex());
        hlinkStyle.setFont(hlinkFont);
        srcCell.setCellStyle(hlinkStyle);

        // Copy hyperlink
        final CellCopyPolicy policy = new CellCopyPolicy.Builder().copyHyperlink(true).mergeHyperlink(false).build();
        destCell.copyCellFrom(srcCell, policy);
        assertNotNull(destCell.getHyperlink());

        assertSame("unit test assumes srcCell and destCell are on the same sheet",
                srcCell.getSheet(), destCell.getSheet());

        final List<XSSFHyperlink> links = srcCell.getSheet().getHyperlinkList();
        assertEquals("number of hyperlinks on sheet", 2, links.size());
        assertEquals("source hyperlink",
                new CellReference(srcCell).formatAsString(), links.get(0).getCellRef());
        assertEquals("destination hyperlink",
                new CellReference(destCell).formatAsString(), links.get(1).getCellRef());
        
        wb.close();
    }
    
    @Test
    public final void testCopyCellFrom_CellCopyPolicy_mergeHyperlink() throws IOException {
        setUp_testCopyCellFrom_CellCopyPolicy();
        final Workbook wb = srcCell.getSheet().getWorkbook();
        final CreationHelper createHelper = wb.getCreationHelper();

        srcCell.setCellValue("URL LINK");
        Hyperlink link = createHelper.createHyperlink(HyperlinkType.URL);
        link.setAddress("http://poi.apache.org/");
        destCell.setHyperlink(link);

        // Set link cell style (optional)
        CellStyle hlinkStyle = wb.createCellStyle();
        Font hlinkFont = wb.createFont();
        hlinkFont.setUnderline(Font.U_SINGLE);
        hlinkFont.setColor(IndexedColors.BLUE.getIndex());
        hlinkStyle.setFont(hlinkFont);
        destCell.setCellStyle(hlinkStyle);
        
        // Pre-condition assumptions. This test is broken if either of these fail.
        assertSame("unit test assumes srcCell and destCell are on the same sheet",
                srcCell.getSheet(), destCell.getSheet());
        assertNull(srcCell.getHyperlink());

        // Merge hyperlink - since srcCell doesn't have a hyperlink, destCell's hyperlink is not overwritten (cleared).
        final CellCopyPolicy policy = new CellCopyPolicy.Builder().mergeHyperlink(true).copyHyperlink(false).build();
        destCell.copyCellFrom(srcCell, policy);
        assertNull(srcCell.getHyperlink());
        assertNotNull(destCell.getHyperlink());
        assertSame(link, destCell.getHyperlink());

        List<XSSFHyperlink> links;
        links = srcCell.getSheet().getHyperlinkList();
        assertEquals("number of hyperlinks on sheet", 1, links.size());
        assertEquals("source hyperlink",
                new CellReference(destCell).formatAsString(), links.get(0).getCellRef());
        
        // Merge destCell's hyperlink to srcCell. Since destCell does have a hyperlink, this should copy destCell's hyperlink to srcCell.
        srcCell.copyCellFrom(destCell, policy);
        assertNotNull(srcCell.getHyperlink());
        assertNotNull(destCell.getHyperlink());
        
        links = srcCell.getSheet().getHyperlinkList();
        assertEquals("number of hyperlinks on sheet", 2, links.size());
        assertEquals("dest hyperlink",
                new CellReference(destCell).formatAsString(), links.get(0).getCellRef());
        assertEquals("source hyperlink",
                new CellReference(srcCell).formatAsString(), links.get(1).getCellRef());
        
        wb.close();
    }
    
    private void setUp_testCopyCellFrom_CellCopyPolicy() {
        @SuppressWarnings("resource")
        final XSSFWorkbook wb = new XSSFWorkbook();
        final XSSFRow row = wb.createSheet("Sheet1").createRow(0);
        srcCell = row.createCell(0);
        destCell = row.createCell(1);
        
        srcCell.setCellFormula("2+3");
        
        final CellStyle style = wb.createCellStyle();
        style.setBorderTop(BorderStyle.THICK);
        style.setFillBackgroundColor((short) 5);
        srcCell.setCellStyle(style);
        
        destCell.setCellValue(true);
    }

    /**
     * Bug 61869: updating a shared formula produces an unreadable file
     */
    @Test
    public void test61869() throws Exception {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("61869.xlsx")) {
            XSSFSheet sheet = wb.getSheetAt(0);
            XSSFCell c2 = sheet.getRow(1).getCell(2);
            assertEquals("SUM(A2,B2)", c2.getCellFormula());
            assertEquals(STCellFormulaType.SHARED, c2.getCTCell().getF().getT());
            assertEquals(0, c2.getCTCell().getF().getSi());
            XSSFCell c3 = sheet.getRow(2).getCell(2);
            assertEquals(STCellFormulaType.SHARED, c3.getCTCell().getF().getT());
            assertEquals(0, c3.getCTCell().getF().getSi());
            assertEquals("SUM(A3,B3)", c3.getCellFormula());

            assertEquals("SUM(A2,B2)", sheet.getSharedFormula(0).getStringValue());

            c2.setCellFormula("SUM(A2:B2)");
            assertEquals(STCellFormulaType.SHARED, c2.getCTCell().getF().getT()); // c2 remains the master formula

            assertEquals("SUM(A2:B2)", sheet.getSharedFormula(0).getStringValue());
            assertEquals(STCellFormulaType.SHARED, c3.getCTCell().getF().getT());
            assertEquals(0, c3.getCTCell().getF().getSi());
            assertEquals("SUM(A3:B3)", c3.getCellFormula());  // formula in the follower cell is rebuilt

        }

    }

    @Test
    public void testBug58106RemoveSharedFormula() throws Exception {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("58106.xlsx")) {
            XSSFSheet sheet = wb.getSheetAt(0);
            XSSFRow row = sheet.getRow(12);
            XSSFCell cell = row.getCell(1);
            CTCellFormula f = cell.getCTCell().getF();
            assertEquals("B13:G13", f.getRef());
            assertEquals("SUM(B1:B3)", f.getStringValue());
            assertEquals(0, f.getSi());
            assertEquals(STCellFormulaType.SHARED, f.getT());
            for(char i = 'C'; i <= 'G'; i++){
                XSSFCell sc =row.getCell(i-'A');
                CTCellFormula sf = sc.getCTCell().getF();
                assertFalse(sf.isSetRef());
                assertEquals("", sf.getStringValue());
                assertEquals(0, sf.getSi());
                assertEquals(STCellFormulaType.SHARED, sf.getT());
            }
            assertEquals("B13:G13", sheet.getSharedFormula(0).getRef());

            cell.setCellType(CellType.NUMERIC);

            assertFalse(cell.getCTCell().isSetF());

            XSSFCell nextFormulaMaster = row.getCell(2);
            assertEquals("C13:G13", nextFormulaMaster.getCTCell().getF().getRef());
            assertEquals("SUM(C1:C3)", nextFormulaMaster.getCTCell().getF().getStringValue());
            assertEquals(0, nextFormulaMaster.getCTCell().getF().getSi());
            for(char i = 'D'; i <= 'G'; i++){
                XSSFCell sc =row.getCell(i-'A');
                CTCellFormula sf = sc.getCTCell().getF();
                assertFalse(sf.isSetRef());
                assertEquals("", sf.getStringValue());
                assertEquals(0, sf.getSi());
                assertEquals(STCellFormulaType.SHARED, sf.getT());
            }
            assertEquals("C13:G13", sheet.getSharedFormula(0).getRef());

        }

    }
}