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

import java.util.Calendar;
import java.util.Date;

import junit.framework.TestCase;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCell;
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
        XSSFCell cell = row.createCell(0);
        //for blank cells getBooleanCellValue returns false
        assertFalse(cell.getBooleanCellValue());

        cell.setCellValue(true);
        assertEquals(Cell.CELL_TYPE_BOOLEAN, cell.getCellType());
        assertTrue(cell.getBooleanCellValue());
        cell.setCellValue(false);
        assertFalse(cell.getBooleanCellValue());

        cell.setCellType(Cell.CELL_TYPE_NUMERIC);
        try {
            cell.getBooleanCellValue();
            fail("Exception expected");
        } catch (IllegalStateException e) {
            // success
            assertEquals("Cannot get a boolean value from a numeric cell", e.getMessage());
        }

        cell.setCellValue("1");
        assertEquals(Cell.CELL_TYPE_STRING, cell.getCellType());
        try {
            cell.getBooleanCellValue();
            fail("Exception expected");
        } catch (IllegalStateException e) {
            // success
            assertEquals("Cannot get a boolean value from a text cell", e.getMessage());
        }

        //reverted to a blank cell
        cell.setCellType(Cell.CELL_TYPE_BLANK);
        assertFalse(cell.getBooleanCellValue());


    }
    
    /**
     * Test setting and getting numeric values.
     */
    public void testSetGetNumeric() throws Exception {
        XSSFRow row = createParentObjects();
        XSSFCell cell = row.createCell(0);
        assertEquals(0.0, cell.getNumericCellValue());

        cell.setCellValue(10.0);
        assertEquals(Cell.CELL_TYPE_NUMERIC, cell.getCellType());
        assertEquals(10.0, cell.getNumericCellValue());
        cell.setCellValue(-23.76);
        assertEquals(-23.76, cell.getNumericCellValue());        

        cell.setCellValue("string");
        try {
            cell.getNumericCellValue();
            fail("Exception expected");
        } catch (IllegalStateException e) {
            // success
            assertEquals("Cannot get a numeric value from a text cell", e.getMessage());
        }

        cell.setCellValue(true);
        try {
            cell.getNumericCellValue();
            fail("Exception expected");
        } catch (IllegalStateException e) {
            // success
            assertEquals("Cannot get a numeric value from a boolean cell", e.getMessage());
        }

        //reverted to a blank cell
        cell.setCellType(Cell.CELL_TYPE_BLANK);
        assertEquals(0.0, cell.getNumericCellValue());

        //setting numeric value for a formula cell does not change the cell type
        XSSFCell fcell = row.createCell(1);
        fcell.setCellFormula("SUM(C4:E4)");
        assertEquals(Cell.CELL_TYPE_FORMULA, fcell.getCellType());
        fcell.setCellValue(36.6);
        assertEquals(Cell.CELL_TYPE_FORMULA, fcell.getCellType());
        assertEquals(36.6, fcell.getNumericCellValue());

        //the said above is true for error cells
        fcell.setCellType(Cell.CELL_TYPE_ERROR);
        assertEquals(36.6, fcell.getNumericCellValue());
        fcell.setCellValue(16.6);
        assertEquals(Cell.CELL_TYPE_FORMULA, fcell.getCellType());
        assertEquals(16.6, fcell.getNumericCellValue());
     }
    
    /**
     * Test setting and getting date values.
     */
    public void testSetGetDate() throws Exception {
        XSSFRow row = createParentObjects();
        XSSFCell cell = row.createCell(0);
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
        } catch (IllegalStateException e) {
            // success
            assertEquals("Cannot get a numeric value from a boolean cell", e.getMessage());
        }
        
        cell.setCellValue(cal);
        assertEquals(before1904,cell.getDateCellValue());
        
    }
    
    /**
     * Test setting and getting date values.
     */
    public void testSetGetType() throws Exception {
        XSSFRow row = createParentObjects();
        XSSFCell cell = row.createCell(0);
        cell.setCellType(Cell.CELL_TYPE_BLANK);
        assertEquals(Cell.CELL_TYPE_BLANK, cell.getCellType());
        cell.setCellType(Cell.CELL_TYPE_STRING);
        assertEquals(Cell.CELL_TYPE_STRING, cell.getCellType());
        cell.setCellType(Cell.CELL_TYPE_FORMULA);
        assertEquals(Cell.CELL_TYPE_FORMULA, cell.getCellType());
        cell.setCellFormula(null);

        //number cell w/o value is treated as a Blank cell
        cell.setCellType(Cell.CELL_TYPE_NUMERIC);
        assertFalse(cell.getCTCell().isSetV());
        assertEquals(Cell.CELL_TYPE_BLANK, cell.getCellType());

        //normal number cells have set values
        cell.setCellType(Cell.CELL_TYPE_NUMERIC);
        cell.getCTCell().setV("0");
        assertEquals(Cell.CELL_TYPE_NUMERIC, cell.getCellType());

        cell.setCellType(Cell.CELL_TYPE_BOOLEAN);
        assertEquals(Cell.CELL_TYPE_BOOLEAN, cell.getCellType());
    }

    public void testSetGetError() throws Exception {
        XSSFRow row = createParentObjects();
        XSSFCell cell = row.createCell(0);
        
        cell.setCellErrorValue((byte)0);
        assertEquals(Cell.CELL_TYPE_ERROR, cell.getCellType());
        assertEquals((byte)0, cell.getErrorCellValue());

        //YK setting numeric value of a error cell does not change the cell type
        cell.setCellValue(2.2);
        assertEquals(Cell.CELL_TYPE_ERROR, cell.getCellType());
        
        cell.setCellErrorValue(FormulaError.NAME);
        assertEquals(Cell.CELL_TYPE_ERROR, cell.getCellType());
        assertEquals(FormulaError.NAME.getCode(), cell.getErrorCellValue());
        assertEquals(FormulaError.NAME.getString(), cell.getErrorCellString());
    }
    
    public void testSetGetFormula() throws Exception {
        XSSFRow row = createParentObjects();
        XSSFCell cell = row.createCell(0);
        String formula = "SQRT(C2^2+D2^2)";
        
        cell.setCellFormula(formula);
        assertEquals(Cell.CELL_TYPE_FORMULA, cell.getCellType());
        assertEquals(formula, cell.getCellFormula());
        
        assertEquals(0.0, cell.getNumericCellValue());

        cell.setCellValue(44.5); //set precalculated value
        assertEquals(Cell.CELL_TYPE_FORMULA, cell.getCellType());
        assertEquals(44.5, cell.getNumericCellValue());

        cell.setCellValue(""); //set precalculated value
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
        XSSFCell cell = row.createCell(0);
        //we return empty string for blank cells
        assertEquals("", cell.getStringCellValue());

        cell.setCellValue(new XSSFRichTextString("test"));
        assertEquals(Cell.CELL_TYPE_STRING, cell.getCellType());
        assertEquals("test", cell.getRichStringCellValue().getString());

        cell.setCellValue(new XSSFRichTextString("Foo"));
        assertEquals(Cell.CELL_TYPE_STRING, cell.getCellType());
        assertEquals("Foo", cell.getRichStringCellValue().getString());

        cell.setCellValue((String)null);
        assertEquals(Cell.CELL_TYPE_BLANK, cell.getCellType());

        XSSFCell fcell = row.createCell(1);
        fcell.setCellFormula("SUM(C4:E4)");
        assertEquals(Cell.CELL_TYPE_FORMULA, fcell.getCellType());
        fcell.setCellValue("36.6");
        assertEquals(Cell.CELL_TYPE_FORMULA, fcell.getCellType());
        assertEquals("36.6", fcell.getStringCellValue());

    }
    
    /**
     * Test that empty cells (no v element) return default values.
     */
    public void testGetEmptyCellValue() {
        XSSFRow row = createParentObjects();
        XSSFCell cell = row.createCell(0);
        cell.setCellType(Cell.CELL_TYPE_BOOLEAN);
        assertFalse(cell.getBooleanCellValue());
        cell.setCellType(Cell.CELL_TYPE_NUMERIC);
        assertEquals(0.0, cell.getNumericCellValue() );
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
    
    public void testSetCellReference() {
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet();
        XSSFRow row = sheet.createRow(0);
        XSSFCell cell = row.createCell(0);
        assertEquals("A1", cell.getCTCell().getR());

        row = sheet.createRow(100);
        cell = row.createCell(100);
        assertEquals("CW101", cell.getCTCell().getR());

        row = sheet.createRow(XSSFRow.MAX_ROW_NUMBER);
        cell = row.createCell(100);
        assertEquals("CW1048577", cell.getCTCell().getR());

        row = sheet.createRow(XSSFRow.MAX_ROW_NUMBER);
        cell = row.createCell(XSSFCell.MAX_COLUMN_NUMBER);
        assertEquals("XFE1048577", cell.getCTCell().getR());

        try {
            sheet.createRow(XSSFRow.MAX_ROW_NUMBER + 1);
            fail("expecting exception when rownum > XSSFRow.MAX_ROW_NUMBER");
        } catch(IllegalArgumentException e){
            ;
        }

        try {
            row = sheet.createRow(100);
            row.createCell(XSSFCell.MAX_COLUMN_NUMBER + 1);
            fail("expecting exception when columnIndex > XSSFCell.MAX_COLUMN_NUMBER");
        } catch(IllegalArgumentException e){
            ;
        }
    }

    public void testGetCellComment() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet();
        assertNotNull(sheet);

        XSSFComment comment = sheet.createComment();
        comment.setAuthor(TEST_C10_AUTHOR);
        sheet.setCellComment("C10", comment);

        // Create C10 cell
        Row row = sheet.createRow(9);
        row.createCell(2);
        row.createCell(3);
        
        assertNotNull(sheet.getRow(9).getCell((short)2));
        assertNotNull(sheet.getRow(9).getCell((short)2).getCellComment());
        assertEquals(TEST_C10_AUTHOR, sheet.getRow(9).getCell((short)2).getCellComment().getAuthor());
        assertNull(sheet.getRow(9).getCell((short)3).getCellComment());
    }
    
    public void testSetCellComment() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet();
        assertNotNull(sheet);

        XSSFComment comment = sheet.createComment();
        comment.setAuthor(TEST_C10_AUTHOR);

        // Create C10 cell
        XSSFRow row = sheet.createRow(9);
        XSSFCell cell = row.createCell(2);
        row.createCell(3);

        // Set a comment for C10 cell
        cell.setCellComment(comment);
        
        CTCell ctCell = cell.getCTCell();
		assertNotNull(ctCell);
		assertEquals("C10", ctCell.getR());
		assertEquals(TEST_C10_AUTHOR, comment.getAuthor());
    }
    
    public void testSetAsActiveCell() {
    	XSSFWorkbook workbook = new XSSFWorkbook();
    	XSSFSheet sheet = workbook.createSheet();
    	Cell cell = sheet.createRow(0).createCell((short)0);
    	cell.setAsActiveCell();
    	
    	assertEquals("A1", sheet.getCTWorksheet().getSheetViews().getSheetViewArray(0).getSelectionArray(0).getActiveCell());
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
        assertTrue(cs.getIndex() > 0);

    	assertNotNull(creationHelper);
    	assertNotNull(creationHelper.createDataFormat());
    	
    	cs.setDataFormat(
    			creationHelper.createDataFormat().getFormat("yyyy/mm/dd")
    	);
    	Cell cell = sheet.createRow(0).createCell((short)0);
        assertNotNull(cell.getCellStyle());
        assertEquals(0, cell.getCellStyle().getIndex());
    	cell.setCellValue(new Date(654321));
    	
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
        XSSFSheet sheet = wb.createSheet();
        XSSFRow row = sheet.createRow(0);
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
    
    
    public void testHSSFXSSFToString(){
	Workbook xwb = new XSSFWorkbook();
    	Sheet xsheet = xwb.createSheet();
    	XSSFCell xcell = (XSSFCell) xsheet.createRow(0).createCell((short)0);
    	
    	Workbook hwb=new HSSFWorkbook();
    	Sheet hsheet=hwb.createSheet();
    	HSSFCell hcell = (HSSFCell) hsheet.createRow(0).createCell((short)0);

    	//BLANK
    	assertEquals(hcell.toString(),xcell.toString());
    	//BOOLEAN
    	xcell.setCellValue(true);
    	xcell.setCellType(Cell.CELL_TYPE_BOOLEAN);
    	hcell.setCellValue(true);
    	hcell.setCellType(Cell.CELL_TYPE_BOOLEAN);
    	assertEquals(hcell.toString(),xcell.toString());
    	
	//NUMERIC

    	xcell.setCellValue(1234);
    	xcell.setCellType(Cell.CELL_TYPE_NUMERIC);
    	hcell.setCellValue(1234);
    	hcell.setCellType(Cell.CELL_TYPE_NUMERIC);
    	assertEquals(hcell.toString(),xcell.toString());
    	
    	//DATE ********************
    	
    	Calendar cal = Calendar.getInstance();
        cal.set(1903, 1, 8);
    	xcell.setCellValue(cal.getTime());
    	CellStyle xstyle=xwb.createCellStyle();
        DataFormat format = xwb.createDataFormat();
    	xstyle.setDataFormat(format.getFormat("YYYY-MM-DD"));
    	xcell.setCellStyle(xstyle);

    	hcell.setCellValue(cal.getTime());
    	CellStyle hstyle=hwb.createCellStyle();
        DataFormat hformat = hwb.createDataFormat();
    	hstyle.setDataFormat(hformat.getFormat("YYYY-MM-DD"));
    	hcell.setCellStyle(hstyle);
    	
    	assertEquals(hcell.toString(),xcell.toString());
    	
    	
    	//STRING
    	xcell.setCellValue(new XSSFRichTextString("text string"));
    	xcell.setCellType(Cell.CELL_TYPE_STRING);
    	hcell.setCellValue(new HSSFRichTextString("text string"));
    	hcell.setCellType(Cell.CELL_TYPE_STRING);
    	assertEquals(hcell.toString(),xcell.toString());
    	
    	//ERROR
    	xcell.setCellErrorValue(FormulaError.VALUE);
    	xcell.setCellType(Cell.CELL_TYPE_ERROR);

    	hcell.setCellErrorValue((byte)0);
    	hcell.setCellType(Cell.CELL_TYPE_ERROR);

    	assertEquals(hcell.toString(),xcell.toString());
    	
    	//FORMULA
    	xcell.setCellFormula("A1+B2");
    	hcell.setCellValue("A1+B2");
    	assertEquals(hcell.toString(),xcell.toString());
    	
    }
    
    
}
