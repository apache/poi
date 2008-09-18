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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import junit.framework.TestCase;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xssf.model.CommentsTable;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.model.SharedStringSource;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCell;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTComment;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTComments;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTRst;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSheet;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorksheet;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STCellType;

/**
 * Tests for {@link XSSFCell}
 *
 */
public final class TestXSSFCell extends TestCase {
    
    private static final String TEST_C10_AUTHOR = "test C10 author";

	/**
     * Test setting and getting boolean values.
     */
    public void testSetGetBoolean() throws Exception {
        XSSFRow row = createParentObjects();
        XSSFCell cell = new XSSFCell(row);
        cell.setCellValue(true);
        assertEquals(Cell.CELL_TYPE_BOOLEAN, cell.getCellType());
        assertTrue(cell.getBooleanCellValue());
        cell.setCellValue(false);
        assertFalse(cell.getBooleanCellValue());
        cell.setCellType(Cell.CELL_TYPE_NUMERIC);
        try {
            cell.getBooleanCellValue();
            fail("Exception expected");
        } catch (NumberFormatException e) {
            // success
        }
    }
    
    /**
     * Test setting and getting numeric values.
     */
    public void testSetGetNumeric() throws Exception {
        XSSFRow row = createParentObjects();
        XSSFCell cell = new XSSFCell(row);
        cell.setCellValue(10d);
        assertEquals(Cell.CELL_TYPE_NUMERIC, cell.getCellType());
        assertEquals(10d, cell.getNumericCellValue());
        cell.setCellValue(-23.76);
        assertEquals(-23.76, cell.getNumericCellValue());        
    }
    
    /**
     * Test setting and getting numeric values.
     */
    public void testSetGetDate() throws Exception {
        XSSFRow row = createParentObjects();
        XSSFCell cell = new XSSFCell(row);
        Date now = new Date();
        cell.setCellValue(now);
        assertEquals(Cell.CELL_TYPE_NUMERIC, cell.getCellType());
        assertEquals(now, cell.getDateCellValue());
        
        // Test case for 1904 hack
        Calendar cal = Calendar.getInstance();
        cal.set(1903, 1, 8);
        Date before1904 = cal.getTime();
        cell.setCellValue(before1904);
        assertEquals(before1904, cell.getDateCellValue());
        
        cell.setCellType(Cell.CELL_TYPE_BOOLEAN);        
        try {
            cell.getDateCellValue();
            fail("Exception expected");
        } catch (NumberFormatException e) {
            // success
        }
    }
    
    public void testSetGetError() throws Exception {
        XSSFRow row = createParentObjects();
        XSSFCell cell = new XSSFCell(row);
        
        cell.setCellErrorValue((byte)0);
        assertEquals(Cell.CELL_TYPE_ERROR, cell.getCellType());
        assertEquals((byte)0, cell.getErrorCellValue());
        
        cell.setCellValue(2.2);
        assertEquals(Cell.CELL_TYPE_NUMERIC, cell.getCellType());
        
        cell.setCellErrorValue(Cell.ERROR_NAME);
        assertEquals(Cell.CELL_TYPE_ERROR, cell.getCellType());
        assertEquals(Cell.ERROR_NAME.getType(), cell.getErrorCellValue());
        assertEquals(Cell.ERROR_NAME.getStringRepr(), cell.getErrorCellString());
    }
    
    public void testSetGetFormula() throws Exception {
        XSSFRow row = createParentObjects();
        XSSFCell cell = new XSSFCell(row);
        String formula = "SQRT(C2^2+D2^2)";
        
        cell.setCellFormula(formula);
        assertEquals(Cell.CELL_TYPE_FORMULA, cell.getCellType());
        assertEquals(formula, cell.getCellFormula());
        
        assertTrue( Double.isNaN( cell.getNumericCellValue() ));
    }
    
    public void testSetGetStringInline() throws Exception {
        CTCell rawCell = CTCell.Factory.newInstance();
        XSSFRow row = createParentObjects();
        XSSFCell cell = new XSSFCell(row, rawCell);
        
        // Default is shared string mode, so have to do this explicitly
        rawCell.setT(STCellType.INLINE_STR);
        
        assertEquals(STCellType.INT_INLINE_STR, rawCell.getT().intValue());
        assertEquals(Cell.CELL_TYPE_STRING, cell.getCellType());
        assertEquals("", cell.getRichStringCellValue().getString());
        
        cell.setCellValue(new XSSFRichTextString("Foo"));
        assertEquals(STCellType.INT_INLINE_STR, rawCell.getT().intValue());
        assertEquals(Cell.CELL_TYPE_STRING, cell.getCellType());
        assertEquals("Foo", cell.getRichStringCellValue().getString());
        
        // To number and back to string, stops being inline
        cell.setCellValue(1.4);
        cell.setCellValue(new XSSFRichTextString("Foo2"));
        assertEquals(STCellType.INT_S, rawCell.getT().intValue());
        assertEquals(Cell.CELL_TYPE_STRING, cell.getCellType());
        assertEquals("Foo2", cell.getRichStringCellValue().getString());
    }
    
    public void testSetGetStringShared() {
        XSSFRow row = createParentObjects();
        XSSFCell cell = new XSSFCell(row);

        cell.setCellValue(new XSSFRichTextString(""));
        assertEquals(Cell.CELL_TYPE_STRING, cell.getCellType());
        assertEquals("", cell.getRichStringCellValue().getString());

        cell.setCellValue(new XSSFRichTextString("Foo"));
        assertEquals(Cell.CELL_TYPE_STRING, cell.getCellType());
        assertEquals("Foo", cell.getRichStringCellValue().getString());
    }
    
    /**
     * Test that empty cells (no v element) return default values.
     */
    public void testGetEmptyCellValue() {
        XSSFRow row = createParentObjects();
        XSSFCell cell = new XSSFCell(row);
        cell.setCellType(Cell.CELL_TYPE_BOOLEAN);
        assertFalse(cell.getBooleanCellValue());
        cell.setCellType(Cell.CELL_TYPE_NUMERIC);
        assertTrue(Double.isNaN( cell.getNumericCellValue() ));
        assertNull(cell.getDateCellValue());
        cell.setCellType(Cell.CELL_TYPE_ERROR);
        assertEquals(0, cell.getErrorCellValue());
        cell.setCellType(Cell.CELL_TYPE_STRING);
        assertEquals("", cell.getRichStringCellValue().getString());
    }

    public void testParseCellNum() {
        assertEquals(0, XSSFCell.parseCellNum("A1"));
        assertEquals(1, XSSFCell.parseCellNum("B1"));
        assertEquals(1, XSSFCell.parseCellNum("B2"));
        assertEquals(26, XSSFCell.parseCellNum("AA1"));
        assertEquals(255, XSSFCell.parseCellNum("IV1"));
        assertEquals(255, XSSFCell.parseCellNum("IV32768"));
    }
    
    public void testFormatPosition() {
        XSSFRow row = createParentObjects();
        row.setRowNum(0);
        XSSFCell cell = new XSSFCell(row);
        cell.setCellNum((short) 0);
        assertEquals("A1", cell.formatPosition());
        cell.setCellNum((short) 25);
        assertEquals("Z1", cell.formatPosition());
        cell.setCellNum((short) 26);
        assertEquals("AA1", cell.formatPosition());
        cell.setCellNum((short) 255);
        assertEquals("IV1", cell.formatPosition());
        row.setRowNum(32767);
        assertEquals("IV32768", cell.formatPosition());
    }
    
    public static class DummySharedStringSource implements SharedStringSource {
        ArrayList<CTRst> strs = new ArrayList<CTRst>();
        public CTRst getEntryAt(int idx) {
            return strs.get(idx);
        }

        public synchronized int addEntry(CTRst s) {
            if(strs.contains(s)) {
                return strs.indexOf(s);
            }
            strs.add(s);
            return strs.size() - 1;
        }
    }
    
    public void testGetCellComment() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        CTSheet ctSheet = CTSheet.Factory.newInstance();
        CTWorksheet ctWorksheet = CTWorksheet.Factory.newInstance();
        CTComments ctComments = CTComments.Factory.newInstance();
        CommentsTable sheetComments = new CommentsTable(ctComments);
        XSSFSheet sheet = new XSSFSheet(ctSheet, ctWorksheet, workbook, sheetComments);
        assertNotNull(sheet);
        
        // Create C10 cell
        Row row = sheet.createRow(9);
        row.createCell(2);
        row.createCell(3);
        
        
        // Set a comment for C10 cell
        CTComment ctComment = ctComments.addNewCommentList().insertNewComment(0);
        ctComment.setRef("C10");
        ctComment.setAuthorId(sheetComments.findAuthor(TEST_C10_AUTHOR));
        
        assertNotNull(sheet.getRow(9).getCell((short)2));
        assertNotNull(sheet.getRow(9).getCell((short)2).getCellComment());
        assertEquals(TEST_C10_AUTHOR, sheet.getRow(9).getCell((short)2).getCellComment().getAuthor());
        assertNull(sheet.getRow(9).getCell((short)3).getCellComment());
    }
    
    public void testSetCellComment() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        CTSheet ctSheet = CTSheet.Factory.newInstance();
        CTWorksheet ctWorksheet = CTWorksheet.Factory.newInstance();
        CTComments ctComments = CTComments.Factory.newInstance();
        CommentsTable comments = new CommentsTable(ctComments);
        XSSFSheet sheet = new XSSFSheet(ctSheet, ctWorksheet, workbook, comments);
        assertNotNull(sheet);
        
        // Create C10 cell
        Row row = sheet.createRow(9);
        Cell cell = row.createCell(2);
        row.createCell(3);
        
        // Create a comment
        Comment comment = comments.addComment();
        comment.setAuthor(TEST_C10_AUTHOR);
        
        // Set a comment for C10 cell
        cell.setCellComment(comment);
        
        CTCell ctCell = ctWorksheet.getSheetData().getRowArray(0).getCArray(0);
		assertNotNull(ctCell);
		assertEquals("C10", ctCell.getR());
		long authorId = ctComments.getCommentList().getCommentArray(0).getAuthorId();
		assertEquals(TEST_C10_AUTHOR, comments.getAuthor(authorId));
    }
    
    public void testSetAsActiveCell() {
    	Workbook workbook = new XSSFWorkbook();
    	CTSheet ctSheet = CTSheet.Factory.newInstance();
    	CTWorksheet ctWorksheet = CTWorksheet.Factory.newInstance();
    	XSSFSheet sheet = new XSSFSheet(ctSheet, ctWorksheet, (XSSFWorkbook) workbook);
    	Cell cell = sheet.createRow(0).createCell((short)0);
    	cell.setAsActiveCell();
    	
    	assertEquals("A1", ctWorksheet.getSheetViews().getSheetViewArray(0).getSelectionArray(0).getActiveCell());
    }
    
    
    /**
     * Tests that cell formatting stuff works as expected
     */
    public void testCellFormatting() {
    	Workbook workbook = new XSSFWorkbook();
    	Sheet sheet = workbook.createSheet();
    	CreationHelper creationHelper = workbook.getCreationHelper();
    	
    	CellStyle cs = workbook.createCellStyle();
    	assertNotNull(cs);
    	
    	assertNotNull(creationHelper);
    	assertNotNull(creationHelper.createDataFormat());
    	
    	cs.setDataFormat(
    			creationHelper.createDataFormat().getFormat("yyyy/mm/dd")
    	);
    	Cell cell = sheet.createRow(0).createCell((short)0);
    	cell.setCellValue(new Date(654321));
    	
    	assertNull(cell.getCellStyle());
    	cell.setCellStyle(cs);
    	
    	assertEquals(new Date(654321), cell.getDateCellValue());
    	assertNotNull(cell.getCellStyle());
    	assertEquals("yyyy/mm/dd", cell.getCellStyle().getDataFormatString());
    	
    	
    	// Save, re-load, and test again
    	Workbook wb2 = XSSFTestDataSamples.writeOutAndReadBack(workbook);
    	Cell c2 = wb2.getSheetAt(0).getRow(0).getCell(0);
    	assertEquals(new Date(654321), c2.getDateCellValue());
    	assertEquals("yyyy/mm/dd", c2.getCellStyle().getDataFormatString());
    }

    private static XSSFRow createParentObjects() {
        XSSFWorkbook wb = new XSSFWorkbook();
        wb.setSharedStringSource(new SharedStringsTable());
        XSSFSheet sheet = new XSSFSheet(wb);
        XSSFRow row = new XSSFRow(sheet);
        return row;
    }

    /**
     * Test to ensure we can only assign cell styles that belong
     *  to our workbook, and not those from other workbooks.
     */
    public void testCellStyleWorkbookMatch() {
    	XSSFWorkbook wbA = new XSSFWorkbook();
    	XSSFWorkbook wbB = new XSSFWorkbook();
    	
    	XSSFCellStyle styA = wbA.createCellStyle();
    	XSSFCellStyle styB = wbB.createCellStyle();
    	
    	styA.verifyBelongsToStylesSource(wbA.getStylesSource());
    	styB.verifyBelongsToStylesSource(wbB.getStylesSource());
    	try {
    		styA.verifyBelongsToStylesSource(wbB.getStylesSource());
    		fail();
    	} catch(IllegalArgumentException e) {}
    	try {
    		styB.verifyBelongsToStylesSource(wbA.getStylesSource());
    		fail();
    	} catch(IllegalArgumentException e) {}
    	
    	Cell cellA = wbA.createSheet().createRow(0).createCell((short)0);
    	Cell cellB = wbB.createSheet().createRow(0).createCell((short)0);
    	
    	cellA.setCellStyle(styA);
    	cellB.setCellStyle(styB);
    	try {
        	cellA.setCellStyle(styB);
    		fail();
    	} catch(IllegalArgumentException e) {}
    	try {
        	cellB.setCellStyle(styA);
    		fail();
    	} catch(IllegalArgumentException e) {}
    }
}
