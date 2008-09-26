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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Iterator;
import junit.framework.TestCase;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.Region;
import org.apache.poi.xssf.model.CommentsTable;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.helpers.ColumnHelper;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.openxml4j.opc.Package;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCol;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCols;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTComment;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTComments;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTRow;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSheet;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorksheet;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTXf;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STPane;


public class TestXSSFSheet extends TestCase {
    
    public void testRowIterator() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet 1");
        Row row1 = sheet.createRow(0);
        Row row2 = sheet.createRow(1);
        Iterator<Row> it = sheet.rowIterator();
        assertNotNull(it);
        assertTrue(it.hasNext());
        assertEquals(row1, it.next());
        assertTrue(it.hasNext());
        assertEquals(row2, it.next());
        assertFalse(it.hasNext());
    }
    
    public void testGetRow() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet 1");
        Row row1 = sheet.createRow(0);
        Cell cell = row1.createCell((short) 0);
        cell.setCellType(Cell.CELL_TYPE_NUMERIC);
        cell.setCellValue((double) 1000);
        
        // Test getting a row and check its cell's value
        Row row_got = sheet.getRow(0);
        Cell cell_got = row_got.getCell((short) 0);
        assertEquals((double) 1000, cell_got.getNumericCellValue());
    }
    
    public void testCreateRow() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet 1");
        
        // Test row creation with consecutive indexes
        Row row1 = sheet.createRow(0);
        Row row2 = sheet.createRow(1);
        assertEquals(0, row1.getRowNum());
        assertEquals(1, row2.getRowNum());
        Iterator<Row> it = sheet.rowIterator();
        assertTrue(it.hasNext());
        assertEquals(row1, it.next());
        assertTrue(it.hasNext());
        assertEquals(row2, it.next());
        
        // Test row creation with non consecutive index
        Row row101 = sheet.createRow(100);
        assertNotNull(row101);
        
        // Test overwriting an existing row
        Row row2_ovrewritten = sheet.createRow(1);
        Cell cell = row2_ovrewritten.createCell((short) 0);
        cell.setCellType(Cell.CELL_TYPE_NUMERIC);
        cell.setCellValue((double) 100);
        Iterator<Row> it2 = sheet.rowIterator();
        assertTrue(it2.hasNext());
        assertEquals(row1, it2.next());
        assertTrue(it2.hasNext());
        Row row2_overwritten_copy = it2.next();
        assertEquals(row2_ovrewritten, row2_overwritten_copy);
        assertEquals(row2_overwritten_copy.getCell((short) 0).getNumericCellValue(), (double) 100);
    }
    
    public void testRemoveRow() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet 1");
        Row row1 = sheet.createRow(1);
        Row row2 = sheet.createRow(2);
        assertNotNull(sheet.getRow(1));
        sheet.removeRow(row2);
        assertNull(sheet.getRow(0));
        assertNull(sheet.getRow(2));
        assertNotNull(sheet.getRow(1));
    }
    
    public void testGetSetDefaultRowHeight() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet 1");
        // Test that default height set by the constructor
        assertEquals((short) 300, sheet.getDefaultRowHeight());
        assertEquals((float) 15, sheet.getDefaultRowHeightInPoints());
        // Set a new default row height in twips and test getting the value in points
        sheet.setDefaultRowHeight((short) 360);
        assertEquals((float) 18, sheet.getDefaultRowHeightInPoints());
        // Test that defaultRowHeight is a truncated short: E.G. 360inPoints -> 18; 361inPoints -> 18
        sheet.setDefaultRowHeight((short) 361);
        assertEquals((float) 18, sheet.getDefaultRowHeightInPoints());
        // Set a new default row height in points and test getting the value in twips
        sheet.setDefaultRowHeightInPoints((short) 17);
        assertEquals((short) 340, sheet.getDefaultRowHeight());
    }
    
    public void testGetSetDefaultColumnWidth() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet 1");
        // Test that default column width set by the constructor
        assertEquals((short) 0, sheet.getDefaultColumnWidth());
        // Set a new default column width and get its value
        sheet.setDefaultColumnWidth((short) 14);
        assertEquals((short) 14, sheet.getDefaultColumnWidth());
    }
    
    public void testGetFirstLastRowNum() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet 1");
        Row row10 = sheet.createRow(9);
        Row row1 = sheet.createRow(0);
        Row row2 = sheet.createRow(1);
        assertEquals(0, sheet.getFirstRowNum());
        assertEquals(9, sheet.getLastRowNum());    
    }
    
    public void testGetPhysicalNumberOfRows() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet 1");
        Row row10 = sheet.createRow(9);
        Row row1 = sheet.createRow(0);
        Row row2 = sheet.createRow(1);
        assertEquals(3, sheet.getPhysicalNumberOfRows());
    }
    
    public void testGetSetRowBreaks() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet 1");
        assertEquals(0, sheet.getRowBreaks().length);
        sheet.setRowBreak(1);
        sheet.setRowBreak(15);
        assertNotNull(sheet.getRowBreaks());
        assertEquals(1, sheet.getRowBreaks()[0]);
        assertEquals(15, sheet.getRowBreaks()[1]);
        sheet.setRowBreak(1);
        assertEquals(2, sheet.getRowBreaks().length);
    }
    
    public void testRemoveRowBreak() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet 1");
        sheet.setRowBreak(1);
        assertEquals(1, sheet.getRowBreaks().length);
        sheet.setRowBreak(2);
        assertEquals(2, sheet.getRowBreaks().length);
        sheet.removeRowBreak(1);
        assertEquals(1, sheet.getRowBreaks().length);
    }
    
    public void testGetSetColumnBreaks() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet 1");
        assertEquals(0, sheet.getColumnBreaks().length);
        sheet.setColumnBreak((short) 11);
        assertNotNull(sheet.getColumnBreaks());
        assertEquals(11, sheet.getColumnBreaks()[0]);
        sheet.setColumnBreak((short) 11223);
        assertEquals(2, sheet.getColumnBreaks().length);
    }
    
    public void testRemoveColumnBreak() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet 1");
        assertEquals(0, sheet.getColumnBreaks().length);
        sheet.setColumnBreak((short) 11);
        assertNotNull(sheet.getColumnBreaks());
        sheet.setColumnBreak((short) 12);
        assertEquals(2, sheet.getColumnBreaks().length);
        sheet.removeColumnBreak((short) 11);
        assertEquals(1, sheet.getColumnBreaks().length);
        sheet.removeColumnBreak((short) 15);
        assertEquals(1, sheet.getColumnBreaks().length);
    }
    
    public void testIsRowColumnBroken() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet 1");
        assertFalse(sheet.isRowBroken(0));
        sheet.setRowBreak(3);
        assertTrue(sheet.isRowBroken(3));
        assertFalse(sheet.isColumnBroken((short) 0));
        sheet.setColumnBreak((short) 3);
        assertTrue(sheet.isColumnBroken((short) 3));
    }
    
    public void testGetSetAutoBreaks() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet 1");
        assertTrue(sheet.getAutobreaks());
        sheet.setAutobreaks(false);
        assertFalse(sheet.getAutobreaks());
    }
    
    public void testIsSetFitToPage() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet 1");
        assertFalse(sheet.getFitToPage());
        sheet.setFitToPage(true);
        assertTrue(sheet.getFitToPage());
        sheet.setFitToPage(false);
        assertFalse(sheet.getFitToPage());
    }
    
    public void testGetSetMargin() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet 1");
        assertEquals(0.7, sheet.getMargin((short) 0));
        sheet.setMargin((short) 0, 10);
        assertEquals((double) 10, sheet.getMargin((short) 0));
        assertEquals((double) 10, sheet.getMargin((short) 1));
        assertEquals((double) 10, sheet.getMargin((short) 2));
        assertEquals((double) 10, sheet.getMargin((short) 3));
        assertEquals((double) 10, sheet.getMargin((short) 4));
        assertEquals((double) 10, sheet.getMargin((short) 5));
        sheet.setMargin((short) 1, 11);
        assertEquals((double) 11, sheet.getMargin((short) 1));
        assertEquals((double) 11, sheet.getMargin((short) 2));
        assertEquals((double) 11, sheet.getMargin((short) 3));
        assertEquals((double) 11, sheet.getMargin((short) 4));
        assertEquals((double) 11, sheet.getMargin((short) 5));
        sheet.setMargin((short) 2, 12);
        assertEquals((double) 12, sheet.getMargin((short) 2));
        assertEquals((double) 12, sheet.getMargin((short) 3));
        assertEquals((double) 12, sheet.getMargin((short) 4));
        assertEquals((double) 12, sheet.getMargin((short) 5));
        sheet.setMargin((short) 3, 13);
        assertEquals((double) 13, sheet.getMargin((short) 3));
        assertEquals((double) 13, sheet.getMargin((short) 4));
        assertEquals((double) 13, sheet.getMargin((short) 5));
        sheet.setMargin((short) 4, 14);
        assertEquals((double) 14, sheet.getMargin((short) 4));
        assertEquals((double) 14, sheet.getMargin((short) 5));
        sheet.setMargin((short) 5, 15);
        assertEquals((double) 15, sheet.getMargin((short) 5));
        
        // Test that nothing happens if another margin constant is given (E.G. 65)
        sheet.setMargin((short) 65, 15);
        assertEquals((double) 10, sheet.getMargin((short) 0));
        assertEquals((double) 11, sheet.getMargin((short) 1));
        assertEquals((double) 12, sheet.getMargin((short) 2));
        assertEquals((double) 13, sheet.getMargin((short) 3));
        assertEquals((double) 14, sheet.getMargin((short) 4));
        assertEquals((double) 15, sheet.getMargin((short) 5));
    }
    
    public void testGetFooter() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = (XSSFSheet)workbook.createSheet("Sheet 1");
        assertNotNull(sheet.getFooter());
        sheet.getFooter().setCenter("test center footer");
        assertEquals("test center footer", sheet.getFooter().getCenter());
        
        // Default is odd footer
        assertNotNull(sheet.getOddFooter());
        assertEquals("test center footer", sheet.getOddFooter().getCenter());
    }
    
    public void testExistingHeaderFooter() throws Exception {
		File xml = new File(
				System.getProperty("HSSF.testdata.path") +
				File.separator + "45540_classic_Header.xlsx"
		);
		assertTrue(xml.exists());
    	
		XSSFWorkbook workbook = new XSSFWorkbook(xml.toString());
		XSSFOddHeader hdr;
		XSSFOddFooter ftr;
		
		// Sheet 1 has a header with center and right text
		XSSFSheet s1 = (XSSFSheet)workbook.getSheetAt(0);
		assertNotNull(s1.getHeader());
		assertNotNull(s1.getFooter());
		hdr = (XSSFOddHeader)s1.getHeader(); 
		ftr = (XSSFOddFooter)s1.getFooter(); 
		
		assertEquals("&Ctestdoc&Rtest phrase", hdr.getText());
		assertEquals(null, ftr.getText());
		
		assertEquals("", hdr.getLeft());
		assertEquals("testdoc", hdr.getCenter());
		assertEquals("test phrase", hdr.getRight());
		
		assertEquals("", ftr.getLeft());
		assertEquals("", ftr.getCenter());
		assertEquals("", ftr.getRight());
		
		
		// Sheet 2 has a footer, but it's empty
		XSSFSheet s2 = (XSSFSheet)workbook.getSheetAt(1);
		assertNotNull(s2.getHeader());
		assertNotNull(s2.getFooter());
		hdr = (XSSFOddHeader)s2.getHeader(); 
		ftr = (XSSFOddFooter)s2.getFooter(); 
		
		assertEquals(null, hdr.getText());
		assertEquals("&L&F", ftr.getText());
		
		assertEquals("", hdr.getLeft());
		assertEquals("", hdr.getCenter());
		assertEquals("", hdr.getRight());
		
		assertEquals("&F", ftr.getLeft());
		assertEquals("", ftr.getCenter());
		assertEquals("", ftr.getRight());
		
		
		// Save and reload
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		workbook.write(baos);
		XSSFWorkbook wb = new XSSFWorkbook(Package.open(
				new ByteArrayInputStream(baos.toByteArray())
		));
		
		hdr = (XSSFOddHeader)wb.getSheetAt(0).getHeader();
		ftr = (XSSFOddFooter)wb.getSheetAt(0).getFooter(); 
		
		assertEquals("", hdr.getLeft());
		assertEquals("testdoc", hdr.getCenter());
		assertEquals("test phrase", hdr.getRight());
		
		assertEquals("", ftr.getLeft());
		assertEquals("", ftr.getCenter());
		assertEquals("", ftr.getRight());
    }
    
    public void testGetAllHeadersFooters() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = (XSSFSheet) workbook.createSheet("Sheet 1");
        assertNotNull(sheet.getOddFooter());
        assertNotNull(sheet.getEvenFooter());
        assertNotNull(sheet.getFirstFooter());
        assertNotNull(sheet.getOddHeader());
        assertNotNull(sheet.getEvenHeader());
        assertNotNull(sheet.getFirstHeader());
        
        assertEquals("", sheet.getOddFooter().getLeft());
        sheet.getOddFooter().setLeft("odd footer left");
        assertEquals("odd footer left", sheet.getOddFooter().getLeft());
        
        assertEquals("", sheet.getEvenFooter().getLeft());
        sheet.getEvenFooter().setLeft("even footer left");
        assertEquals("even footer left", sheet.getEvenFooter().getLeft());
        
        assertEquals("", sheet.getFirstFooter().getLeft());
        sheet.getFirstFooter().setLeft("first footer left");
        assertEquals("first footer left", sheet.getFirstFooter().getLeft());
        
        assertEquals("", sheet.getOddHeader().getLeft());
        sheet.getOddHeader().setLeft("odd header left");
        assertEquals("odd header left", sheet.getOddHeader().getLeft());
        
        assertEquals("", sheet.getOddHeader().getRight());
        sheet.getOddHeader().setRight("odd header right");
        assertEquals("odd header right", sheet.getOddHeader().getRight());
        
        assertEquals("", sheet.getOddHeader().getCenter());
        sheet.getOddHeader().setCenter("odd header center");
        assertEquals("odd header center", sheet.getOddHeader().getCenter());

        // Defaults are odd
        assertEquals("odd footer left", sheet.getFooter().getLeft());
        assertEquals("odd header center", sheet.getHeader().getCenter());
    }
    
    public void testGetSetColumnWidth() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet 1");
        sheet.setColumnWidth((short) 1,(short)  22);
        assertEquals(22, sheet.getColumnWidth((short) 1));
        
        // Now check the low level stuff, and check that's all
        //  been set correctly
        XSSFSheet xs = (XSSFSheet)sheet;
        CTWorksheet cts = xs.getWorksheet();
        
        CTCols[] cols_s = cts.getColsArray();
        assertEquals(1, cols_s.length);
        CTCols cols = cols_s[0];
        assertEquals(1, cols.sizeOfColArray());
        CTCol col = cols.getColArray(0);
        
        // XML is 1 based, POI is 0 based
        assertEquals(2, col.getMin());
        assertEquals(2, col.getMax());
        assertEquals(22.0, col.getWidth());
        
        
        // Now set another
        sheet.setColumnWidth((short) 3,(short)  33);
        
        cols_s = cts.getColsArray();
        assertEquals(1, cols_s.length);
        cols = cols_s[0];
        assertEquals(2, cols.sizeOfColArray());
        
        col = cols.getColArray(0);
        assertEquals(2, col.getMin()); // POI 1
        assertEquals(2, col.getMax());
        assertEquals(22.0, col.getWidth());
        
        col = cols.getColArray(1);
        assertEquals(4, col.getMin()); // POI 3
        assertEquals(4, col.getMax());
        assertEquals(33.0, col.getWidth());
    }
    
    public void testGetSetColumnHidden() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet 1");
        sheet.setColumnHidden((short) 2, true);
        assertTrue(sheet.isColumnHidden((short) 2));
    }
    
    public void testAutoSizeColumn() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = (XSSFSheet) workbook.createSheet("Sheet 1");
        ColumnHelper columnHelper = sheet.getColumnHelper();
        CTCol col = columnHelper.getColumn(13, false);
        assertNull(col);
        sheet.autoSizeColumn((short)13);
        col = columnHelper.getColumn(13, false);
        assertNotNull(col);
        assertTrue(col.getBestFit());	
    }
    
    public void testGetDialog() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Sheet 1");
        assertFalse(sheet.getDialog());
        XSSFSheet dialogsheet = workbook.createDialogsheet("Dialogsheet 1", null);
        assertTrue(dialogsheet.getDialog());
    	
    }
    
    public void testGetSetHorizontallyCentered() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = (XSSFSheet) workbook.createSheet("Sheet 1");
        assertFalse(sheet.getHorizontallyCenter());
        sheet.setHorizontallyCenter(true);
        assertTrue(sheet.getHorizontallyCenter());
        sheet.setHorizontallyCenter(false);
        assertFalse(sheet.getHorizontallyCenter());
    }
    
    public void testGetSetVerticallyCentered() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = (XSSFSheet) workbook.createSheet("Sheet 1");
        assertFalse(sheet.getVerticallyCenter());
        sheet.setVerticallyCenter(true);
        assertTrue(sheet.getVerticallyCenter());
        sheet.setVerticallyCenter(false);
        assertFalse(sheet.getVerticallyCenter());
    }
    
    public void testIsSetPrintGridlines() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = (XSSFSheet) workbook.createSheet("Sheet 1");
        assertFalse(sheet.isPrintGridlines());
        sheet.setPrintGridlines(true);
        assertTrue(sheet.isPrintGridlines());
    }
    
    public void testIsSetDisplayFormulas() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = (XSSFSheet) workbook.createSheet("Sheet 1");
        assertFalse(sheet.isDisplayFormulas());
        sheet.setDisplayFormulas(true);
        assertTrue(sheet.isDisplayFormulas());
    }
    
    public void testIsSetDisplayGridLines() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = (XSSFSheet) workbook.createSheet("Sheet 1");
        assertTrue(sheet.isDisplayGridlines());
        sheet.setDisplayGridlines(false);
        assertFalse(sheet.isDisplayGridlines());
    }
    
    public void testIsSetDisplayRowColHeadings() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = (XSSFSheet) workbook.createSheet("Sheet 1");
        assertTrue(sheet.isDisplayRowColHeadings());
        sheet.setDisplayRowColHeadings(false);
        assertFalse(sheet.isDisplayRowColHeadings());
    }
    
    public void testGetScenarioProtect() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = (XSSFSheet) workbook.createSheet("Sheet 1");
        assertFalse(sheet.getScenarioProtect());
    }
    
    public void testTopRowLeftCol() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = (XSSFSheet) workbook.createSheet("Sheet 1");
        sheet.showInPane((short)1, (short)1);
        assertEquals((short) 1, sheet.getTopRow());
        assertEquals((short) 1, sheet.getLeftCol());
        sheet.showInPane((short)2, (short)26);
        assertEquals((short) 2, sheet.getTopRow());
        assertEquals((short) 26, sheet.getLeftCol());
    }
    
    public void testShiftRows() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        
        XSSFSheet sheet = (XSSFSheet) createSheet(workbook, "Sheet 1");
    	sheet.shiftRows(1, 2, 4, true, false);
    	assertEquals((short) 1, sheet.getRow(5).getHeight());
    	assertEquals((short) 2, sheet.getRow(6).getHeight());
    	assertNull(sheet.getRow(1));
    	assertNull(sheet.getRow(2));
    	assertEquals(8, sheet.getPhysicalNumberOfRows());

        XSSFSheet sheet2 = (XSSFSheet) createSheet(workbook, "Sheet 2");
    	sheet2.shiftRows(1, 5, 3, true, false);
    	assertEquals((short) 1, sheet2.getRow(4).getHeight());
    	assertEquals((short) 2, sheet2.getRow(5).getHeight());
    	assertEquals((short) 3, sheet2.getRow(6).getHeight());
    	assertEquals((short) 4, sheet2.getRow(7).getHeight());
    	assertEquals((short) 5, sheet2.getRow(8).getHeight());
    	assertNull(sheet2.getRow(1));
    	assertNull(sheet2.getRow(2));
    	assertNull(sheet2.getRow(3));
    	assertEquals(7, sheet2.getPhysicalNumberOfRows());

        XSSFSheet sheet3 = (XSSFSheet) createSheet(workbook, "Sheet 3");
    	sheet3.shiftRows(5, 7, -3, true, false);
    	assertEquals(5, sheet3.getRow(2).getHeight());
    	assertEquals(6, sheet3.getRow(3).getHeight());
    	assertEquals(7, sheet3.getRow(4).getHeight());
    	assertNull(sheet3.getRow(5));
    	assertNull(sheet3.getRow(6));
    	assertNull(sheet3.getRow(7));
    	assertEquals(7, sheet3.getPhysicalNumberOfRows());

        XSSFSheet sheet4 = (XSSFSheet) createSheet(workbook, "Sheet 4");
    	sheet4.shiftRows(5, 7, -2, true, false);
    	assertEquals(5, sheet4.getRow(3).getHeight());
    	assertEquals(6, sheet4.getRow(4).getHeight());
    	assertEquals(7, sheet4.getRow(5).getHeight());
    	assertNull(sheet4.getRow(6));
    	assertNull(sheet4.getRow(7));
    	assertEquals(8, sheet4.getPhysicalNumberOfRows());

    	// Test without copying rowHeight
        XSSFSheet sheet5 = (XSSFSheet) createSheet(workbook, "Sheet 5");
    	sheet5.shiftRows(5, 7, -2, false, false);
    	assertEquals(-1, sheet5.getRow(3).getHeight());
    	assertEquals(-1, sheet5.getRow(4).getHeight());
    	assertEquals(-1, sheet5.getRow(5).getHeight());
    	assertNull(sheet5.getRow(6));
    	assertNull(sheet5.getRow(7));
    	assertEquals(8, sheet5.getPhysicalNumberOfRows());

    	// Test without copying rowHeight and resetting to default height
        XSSFSheet sheet6 = (XSSFSheet) createSheet(workbook, "Sheet 6");
        sheet6.setDefaultRowHeight((short) 200);
    	sheet6.shiftRows(5, 7, -2, false, true);
    	assertEquals(200, sheet6.getRow(3).getHeight());
    	assertEquals(200, sheet6.getRow(4).getHeight());
    	assertEquals(200, sheet6.getRow(5).getHeight());
    	assertNull(sheet6.getRow(6));
    	assertNull(sheet6.getRow(7));
    	assertEquals(8, sheet6.getPhysicalNumberOfRows());
    }
    
    public void testGetCellComment() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        CTSheet ctSheet = CTSheet.Factory.newInstance();
        CTWorksheet ctWorksheet = CTWorksheet.Factory.newInstance();
        CTComments ctComments = CTComments.Factory.newInstance();
        CommentsTable sheetComments = new CommentsTable(ctComments);
        XSSFSheet sheet = new XSSFSheet(ctSheet, ctWorksheet, workbook, sheetComments);
        assertNotNull(sheet);
        
        CTComment ctComment = ctComments.addNewCommentList().insertNewComment(0);
        ctComment.setRef("C10");
        ctComment.setAuthorId(sheetComments.findAuthor("test C10 author"));
        
        assertNotNull(sheet.getCellComment(9, 2));
        assertEquals("test C10 author", sheet.getCellComment(9, 2).getAuthor());
    }
    
    public void testSetCellComment() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        CTSheet ctSheet = CTSheet.Factory.newInstance();
        CTWorksheet ctWorksheet = CTWorksheet.Factory.newInstance();
        XSSFSheet sheet = new XSSFSheet(ctSheet, ctWorksheet, workbook);
        Cell cell = sheet.createRow(0).createCell((short)0);
        CTComments ctComments = CTComments.Factory.newInstance();
        CommentsTable comments = new CommentsTable(ctComments);
        XSSFComment comment = comments.addComment();
        
        sheet.setCellComment("A1", comment);
        assertEquals("A1", ctComments.getCommentList().getCommentArray(0).getRef());
        comment.setAuthor("test A1 author");
        assertEquals("test A1 author", comments.getAuthor(ctComments.getCommentList().getCommentArray(0).getAuthorId()));
    }
    
    public void testGetActiveCell() {
    	XSSFWorkbook workbook = new XSSFWorkbook();
    	XSSFSheet sheet = workbook.createSheet();
    	sheet.setActiveCell("R5");

    	assertEquals("R5", sheet.getActiveCell());
    	
    }
    
    public void testCreateFreezePane() {
    	Workbook workbook = new XSSFWorkbook();
    	CTSheet ctSheet = CTSheet.Factory.newInstance();
    	CTWorksheet ctWorksheet = CTWorksheet.Factory.newInstance();
    	XSSFSheet sheet = new XSSFSheet(ctSheet, ctWorksheet, (XSSFWorkbook) workbook);
    	sheet.createFreezePane(2, 4);
    	assertEquals((double)2, ctWorksheet.getSheetViews().getSheetViewArray(0).getPane().getXSplit());
    	assertEquals(STPane.BOTTOM_RIGHT, ctWorksheet.getSheetViews().getSheetViewArray(0).getPane().getActivePane());
    	sheet.createFreezePane(3, 6, 10, 10);
    	assertEquals((double)3, ctWorksheet.getSheetViews().getSheetViewArray(0).getPane().getXSplit());
    	assertEquals(10, sheet.getTopRow());
    	assertEquals(10, sheet.getLeftCol());
    	sheet.createSplitPane(4, 8, 12, 12, 1);
    	assertEquals((double)8, ctWorksheet.getSheetViews().getSheetViewArray(0).getPane().getYSplit());
    	assertEquals(STPane.BOTTOM_RIGHT, ctWorksheet.getSheetViews().getSheetViewArray(0).getPane().getActivePane());
    }
    
    public void testNewMergedRegionAt() {
    	Workbook workbook = new XSSFWorkbook();
    	CTSheet ctSheet = CTSheet.Factory.newInstance();
    	CTWorksheet ctWorksheet = CTWorksheet.Factory.newInstance();
    	XSSFSheet sheet = new XSSFSheet(ctSheet, ctWorksheet, (XSSFWorkbook) workbook);
    	Region region = new Region("B2:D4");
    	sheet.addMergedRegion(region);
    	assertEquals("B2:D4", sheet.getMergedRegionAt(0).getRegionRef());
    }
    
    public void testGetNumMergedRegions() {
    	Workbook workbook = new XSSFWorkbook();
    	CTSheet ctSheet = CTSheet.Factory.newInstance();
    	CTWorksheet ctWorksheet = CTWorksheet.Factory.newInstance();
    	XSSFSheet sheet = new XSSFSheet(ctSheet, ctWorksheet, (XSSFWorkbook) workbook);
    	assertEquals(0, sheet.getNumMergedRegions());
    	Region region = new Region("B2:D4");
    	sheet.addMergedRegion(region);
    	assertEquals(1, sheet.getNumMergedRegions());
    }
    
    public void testRemoveMergedRegion() {
    	Workbook workbook = new XSSFWorkbook();
    	CTSheet ctSheet = CTSheet.Factory.newInstance();
    	CTWorksheet ctWorksheet = CTWorksheet.Factory.newInstance();
    	XSSFSheet sheet = new XSSFSheet(ctSheet, ctWorksheet, (XSSFWorkbook) workbook);
    	Region region_1 = new Region("A1:B2");
    	Region region_2 = new Region("C3:D4");
    	Region region_3 = new Region("E5:F6");
    	sheet.addMergedRegion(region_1);
    	sheet.addMergedRegion(region_2);
    	sheet.addMergedRegion(region_3);
    	assertEquals("C3:D4", ctWorksheet.getMergeCells().getMergeCellArray(1).getRef());
    	assertEquals(3, sheet.getNumMergedRegions());
    	sheet.removeMergedRegion(1);
    	assertEquals("E5:F6", ctWorksheet.getMergeCells().getMergeCellArray(1).getRef());
    	assertEquals(2, sheet.getNumMergedRegions());
    	sheet.removeMergedRegion(1);
    	sheet.removeMergedRegion(0);
    	assertEquals(0, sheet.getNumMergedRegions());
    }
    
    public void testSetDefaultColumnStyle() {
    	XSSFWorkbook workbook = new XSSFWorkbook();
    	CTSheet ctSheet = CTSheet.Factory.newInstance();
    	CTWorksheet ctWorksheet = CTWorksheet.Factory.newInstance();
    	XSSFSheet sheet = new XSSFSheet(ctSheet, ctWorksheet, (XSSFWorkbook) workbook);
    	StylesTable stylesTable = (StylesTable) workbook.getStylesSource();
    	XSSFFont font = new XSSFFont();
    	font.setFontName("Cambria");
    	stylesTable.putFont(font);
    	CTXf cellStyleXf = CTXf.Factory.newInstance();
    	cellStyleXf.setFontId(1);
    	cellStyleXf.setFillId(0);
    	cellStyleXf.setBorderId(0);
    	cellStyleXf.setNumFmtId(0);
    	stylesTable.putCellStyleXf(cellStyleXf);
    	CTXf cellXf = CTXf.Factory.newInstance();
    	cellXf.setXfId(1);
    	stylesTable.putCellXf(cellXf);
    	XSSFCellStyle cellStyle = new XSSFCellStyle(1, 1, stylesTable);
    	assertEquals(1, cellStyle.getFontIndex());
    	
    	sheet.setDefaultColumnStyle((short) 3, cellStyle);
    	assertEquals(1, ctWorksheet.getColsArray(0).getColArray(0).getStyle());
    	XSSFRow row = (XSSFRow) sheet.createRow(0);
    	XSSFCell cell = (XSSFCell) sheet.getRow(0).createCell(3);
    	
    }
    

	private XSSFSheet createSheet(XSSFWorkbook workbook, String name) {
        XSSFSheet sheet = (XSSFSheet) workbook.createSheet(name);
    	Row row0 = sheet.createRow(0);
    	row0.setHeight((short) 1);
    	Row row1 = sheet.createRow(1);
    	row1.setHeight((short) 1);
    	Row row2 = sheet.createRow(2);
    	row2.setHeight((short) 2);
    	Row row3 = sheet.createRow(3);
    	row3.setHeight((short) 3);
    	Row row4 = sheet.createRow(4);
    	row4.setHeight((short) 4);
    	Row row5 = sheet.createRow(5);
    	row5.setHeight((short) 5);
    	Row row6 = sheet.createRow(6);
    	row6.setHeight((short) 6);
    	Row row7 = sheet.createRow(7);
    	row7.setHeight((short) 7);
    	Row row8 = sheet.createRow(8);
    	row8.setHeight((short) 8);
    	Row row9 = sheet.createRow(9);
    	row9.setHeight((short) 9);
    	return sheet;
	}
	
	
	
	   public void testGroupUngroupColumn() {
	    	Workbook workbook = new XSSFWorkbook();
	    	CTSheet ctSheet = CTSheet.Factory.newInstance();
	    	CTWorksheet ctWorksheet = CTWorksheet.Factory.newInstance();
	    	XSSFSheet sheet = new XSSFSheet(ctSheet, ctWorksheet, (XSSFWorkbook) workbook);

	    	//one level
	    	sheet.groupColumn((short)2,(short)7);
	    	sheet.groupColumn((short)10,(short)11);
	    	CTCols cols=sheet.getWorksheet().getColsArray(0);
	    	assertEquals(2,cols.sizeOfColArray());
	    	CTCol[]colArray=cols.getColArray();
	    	assertNotNull(colArray);
	    	assertEquals(2+1,colArray[0].getMin()); // 1 based
	    	assertEquals(7+1,colArray[0].getMax()); // 1 based
	    	assertEquals(1, colArray[0].getOutlineLevel());

	    	//two level  
	    	sheet.groupColumn((short)1,(short)2);
	    	cols=sheet.getWorksheet().getColsArray(0);
	    	assertEquals(4,cols.sizeOfColArray());
	    	colArray=cols.getColArray();
	    	assertEquals(2, colArray[1].getOutlineLevel());

	    	//three level
	    	sheet.groupColumn((short)6,(short)8);
	    	sheet.groupColumn((short)2,(short)3);
	    	cols=sheet.getWorksheet().getColsArray(0);
	    	assertEquals(7,cols.sizeOfColArray());
	    	colArray=cols.getColArray();
	    	assertEquals(3, colArray[1].getOutlineLevel());
	    	assertEquals(3,sheet.getSheetTypeSheetFormatPr().getOutlineLevelCol());

	    	sheet.ungroupColumn((short)8,(short) 10);
	    	colArray=cols.getColArray();
	    	//assertEquals(3, colArray[1].getOutlineLevel());

	    	sheet.ungroupColumn((short)4,(short)6);
	    	sheet.ungroupColumn((short)2,(short)2);
	    	colArray=cols.getColArray();
	    	assertEquals(4, colArray.length);
	    	assertEquals(2,sheet.getSheetTypeSheetFormatPr().getOutlineLevelCol());
	    }

	    
	    public void testGroupUngroupRow() {
	    	Workbook workbook = new XSSFWorkbook();
	    	CTSheet ctSheet = CTSheet.Factory.newInstance();
	    	CTWorksheet ctWorksheet = CTWorksheet.Factory.newInstance();
	    	XSSFSheet sheet = new XSSFSheet(ctSheet, ctWorksheet, (XSSFWorkbook) workbook);

	    	//one level
	    	sheet.groupRow(9,10);
	    	assertEquals(2,sheet.rows.size());
	    	CTRow[]rowArray=sheet.getWorksheet().getSheetData().getRowArray();    	
	    	assertEquals(2,rowArray.length);
	    	CTRow ctrow=rowArray[0];

	    	assertNotNull(ctrow);
	    	assertEquals(9,ctrow.getR());
	    	assertEquals(1, ctrow.getOutlineLevel());
	    	assertEquals(1,sheet.getSheetTypeSheetFormatPr().getOutlineLevelRow());

	    	//two level    	
	    	sheet.groupRow(10,13);
	    	rowArray=sheet.getWorksheet().getSheetData().getRowArray();    	
	    	assertEquals(5,rowArray.length);
	    	assertEquals(5,sheet.rows.size());
	    	ctrow=rowArray[1];
	    	assertNotNull(ctrow);
	    	assertEquals(10,ctrow.getR());
	    	assertEquals(2, ctrow.getOutlineLevel());
	    	assertEquals(2,sheet.getSheetTypeSheetFormatPr().getOutlineLevelRow());

	    	
	    	sheet.ungroupRow(8, 10);
	    	rowArray=sheet.getWorksheet().getSheetData().getRowArray();    	
	    	assertEquals(4,rowArray.length);
	    	assertEquals(1,sheet.getSheetTypeSheetFormatPr().getOutlineLevelRow());

	    	sheet.ungroupRow(10,10);
	    	rowArray=sheet.getWorksheet().getSheetData().getRowArray();    	
	    	assertEquals(3,rowArray.length);
	    	assertEquals(3,sheet.rows.size());

	    	assertEquals(1,sheet.getSheetTypeSheetFormatPr().getOutlineLevelRow());
	    }
            
            public void testSetZoom() {
                Workbook workBook = new XSSFWorkbook();
                XSSFSheet sheet1 = (XSSFSheet) workBook.createSheet("new sheet");
                sheet1.setZoom(3,4);   // 75 percent magnification
                long zoom = sheet1.getSheetTypeSheetView().getZoomScale();
                assertEquals(zoom, 75);
            }

    public void testOutlineProperties() {
        XSSFWorkbook wb = new XSSFWorkbook();

        XSSFSheet sheet = wb.createSheet();

        assertTrue(sheet.getRowSumsBelow());
        assertTrue(sheet.getRowSumsRight());

        sheet.setRowSumsBelow(false);
        sheet.setRowSumsRight(false);

        assertFalse(sheet.getRowSumsBelow());
        assertFalse(sheet.getRowSumsRight());
    }

}
