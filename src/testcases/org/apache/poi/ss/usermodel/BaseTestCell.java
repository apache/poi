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

package org.apache.poi.ss.usermodel;

import junit.framework.TestCase;
import junit.framework.AssertionFailedError;
import org.apache.poi.ss.ITestDataProvider;

/**
 * Common superclass for testing {@link org.apache.poi.xssf.usermodel.XSSFCell}  and
 * {@link org.apache.poi.hssf.usermodel.HSSFCell}
 */
public abstract class BaseTestCell extends TestCase {

    /**
     * @return an object that provides test data in HSSF / XSSF specific way
     */
    protected abstract ITestDataProvider getTestDataProvider();

    public void baseTestSetValues() {
        Workbook book = getTestDataProvider().createWorkbook();
        Sheet sheet = book.createSheet("test");
        Row row = sheet.createRow(0);

        CreationHelper factory = book.getCreationHelper();
        Cell cell = row.createCell(0);

        cell.setCellValue(1.2);
        assertEquals(1.2, cell.getNumericCellValue(), 0.0001);
        assertEquals(Cell.CELL_TYPE_NUMERIC, cell.getCellType());

        cell.setCellValue(false);
        assertEquals(false, cell.getBooleanCellValue());
        assertEquals(Cell.CELL_TYPE_BOOLEAN, cell.getCellType());

        cell.setCellValue(factory.createRichTextString("Foo"));
        assertEquals("Foo", cell.getRichStringCellValue().getString());
        assertEquals(Cell.CELL_TYPE_STRING, cell.getCellType());

        cell.setCellValue(factory.createRichTextString("345"));
        assertEquals("345", cell.getRichStringCellValue().getString());
        assertEquals(Cell.CELL_TYPE_STRING, cell.getCellType());
    }

    /**
     * test that Boolean and Error types (BoolErrRecord) are supported properly.
     */
    public void baseTestBoolErr() {

        Workbook	 wb	 = getTestDataProvider().createWorkbook();
        Sheet		s	  = wb.createSheet("testSheet1");
        Row		  r	  = null;
        Cell		 c	  = null;
        r = s.createRow(0);
        c=r.createCell(1);
        //c.setCellType(HSSFCell.CELL_TYPE_BOOLEAN);
        c.setCellValue(true);

        c=r.createCell(2);
        //c.setCellType(HSSFCell.CELL_TYPE_BOOLEAN);
        c.setCellValue(false);

        r = s.createRow(1);
        c=r.createCell(1);
        //c.setCellType(HSSFCell.CELL_TYPE_ERROR);
        c.setCellErrorValue((byte)0);

        c=r.createCell(2);
        //c.setCellType(HSSFCell.CELL_TYPE_ERROR);
        c.setCellErrorValue((byte)7);

        wb = getTestDataProvider().writeOutAndReadBack(wb);
        s = wb.getSheetAt(0);
        r = s.getRow(0);
        c = r.getCell(1);
        assertTrue("boolean value 0,1 = true",c.getBooleanCellValue());
        c = r.getCell(2);
        assertTrue("boolean value 0,2 = false",c.getBooleanCellValue()==false);
        r = s.getRow(1);
        c = r.getCell(1);
        assertTrue("boolean value 0,1 = 0",c.getErrorCellValue() == 0);
        c = r.getCell(2);
        assertTrue("boolean value 0,2 = 7",c.getErrorCellValue() == 7);
    }

    /**
     * test that Cell Styles being applied to formulas remain intact
     */
    public void baseTestFormulaStyle() {

        Workbook wb = getTestDataProvider().createWorkbook();
        Sheet s = wb.createSheet("testSheet1");
        Row r = null;
        Cell c = null;
        CellStyle cs = wb.createCellStyle();
        Font f = wb.createFont();
        f.setFontHeightInPoints((short) 20);
        f.setColor(IndexedColors.RED.getIndex());
        f.setBoldweight(f.BOLDWEIGHT_BOLD);
        f.setFontName("Arial Unicode MS");
        cs.setFillBackgroundColor((short)3);
        cs.setFont(f);
        cs.setBorderTop((short)1);
        cs.setBorderRight((short)1);
        cs.setBorderLeft((short)1);
        cs.setBorderBottom((short)1);

        r = s.createRow(0);
        c=r.createCell(0);
        c.setCellStyle(cs);
        c.setCellFormula("2*3");

        wb = getTestDataProvider().writeOutAndReadBack(wb);
        s = wb.getSheetAt(0);
        r = s.getRow(0);
        c = r.getCell(0);

        assertTrue("Formula Cell at 0,0", (c.getCellType()==c.CELL_TYPE_FORMULA));
        cs = c.getCellStyle();

        assertNotNull("Formula Cell Style", cs);
        assertTrue("Font Index Matches", (cs.getFontIndex() == f.getIndex()));
        assertTrue("Top Border", (cs.getBorderTop() == (short)1));
        assertTrue("Left Border", (cs.getBorderLeft() == (short)1));
        assertTrue("Right Border", (cs.getBorderRight() == (short)1));
        assertTrue("Bottom Border", (cs.getBorderBottom() == (short)1));
    }

    /**tests the toString() method of HSSFCell*/
    public void baseTestToString() {
        Workbook wb = getTestDataProvider().createWorkbook();
        Row r = wb.createSheet("Sheet1").createRow(0);
        CreationHelper factory = wb.getCreationHelper();

        r.createCell(0).setCellValue(true);
        r.createCell(1).setCellValue(1.5);
        r.createCell(2).setCellValue(factory.createRichTextString("Astring"));
        r.createCell(3).setCellErrorValue((byte)ErrorConstants.ERROR_DIV_0);
        r.createCell(4).setCellFormula("A1+B1");

        assertEquals("Boolean", "TRUE", r.getCell(0).toString());
        assertEquals("Numeric", "1.5", r.getCell(1).toString());
        assertEquals("String", "Astring", r.getCell(2).toString());
        assertEquals("Error", "#DIV/0!", r.getCell(3).toString());
        assertEquals("Formula", "A1+B1", r.getCell(4).toString());

        //Write out the file, read it in, and then check cell values
        wb = getTestDataProvider().writeOutAndReadBack(wb);

        r = wb.getSheetAt(0).getRow(0);
        assertEquals("Boolean", "TRUE", r.getCell(0).toString());
        assertEquals("Numeric", "1.5", r.getCell(1).toString());
        assertEquals("String", "Astring", r.getCell(2).toString());
        assertEquals("Error", "#DIV/0!", r.getCell(3).toString());
        assertEquals("Formula", "A1+B1", r.getCell(4).toString());
    }

    /**
     *  Test that setting cached formula result keeps the cell type
     */
    public void baseTestSetFormulaValue() {
        Workbook wb = getTestDataProvider().createWorkbook();
        Sheet s = wb.createSheet();
        Row r = s.createRow(0);

        Cell c1 = r.createCell(0);
        c1.setCellFormula("NA()");
        assertEquals(0.0, c1.getNumericCellValue());
        assertEquals(Cell.CELL_TYPE_NUMERIC, c1.getCachedFormulaResultType());
        c1.setCellValue(10);
        assertEquals(10.0, c1.getNumericCellValue());
        assertEquals(Cell.CELL_TYPE_FORMULA, c1.getCellType());
        assertEquals(Cell.CELL_TYPE_NUMERIC, c1.getCachedFormulaResultType());

        Cell c2 = r.createCell(1);
        c2.setCellFormula("NA()");
        assertEquals(0.0, c2.getNumericCellValue());
        assertEquals(Cell.CELL_TYPE_NUMERIC, c2.getCachedFormulaResultType());
        c2.setCellValue("I changed!");
        assertEquals("I changed!", c2.getStringCellValue());
        assertEquals(Cell.CELL_TYPE_FORMULA, c2.getCellType());
        assertEquals(Cell.CELL_TYPE_STRING, c2.getCachedFormulaResultType());
    }

    public void baseTestChangeTypeStringToBool(Cell cell) {

		cell.setCellValue("TRUE");
		assertEquals(Cell.CELL_TYPE_STRING, cell.getCellType());
		try {
			cell.setCellType(Cell.CELL_TYPE_BOOLEAN);
		} catch (ClassCastException e) {
			throw new AssertionFailedError(
					"Identified bug in conversion of cell from text to boolean");
		}

		assertEquals(Cell.CELL_TYPE_BOOLEAN, cell.getCellType());
		assertEquals(true, cell.getBooleanCellValue());
		cell.setCellType(Cell.CELL_TYPE_STRING);
		assertEquals("TRUE", cell.getRichStringCellValue().getString());

		// 'false' text to bool and back
		cell.setCellValue("FALSE");
		cell.setCellType(Cell.CELL_TYPE_BOOLEAN);
		assertEquals(Cell.CELL_TYPE_BOOLEAN, cell.getCellType());
		assertEquals(false, cell.getBooleanCellValue());
		cell.setCellType(Cell.CELL_TYPE_STRING);
		assertEquals("FALSE", cell.getRichStringCellValue().getString());
	}

    public void baseTestChangeTypeBoolToString(Cell cell) {
        cell.setCellValue(true);
        try {
            cell.setCellType(Cell.CELL_TYPE_STRING);
        } catch (IllegalStateException e) {
            if (e.getMessage().equals("Cannot get a text value from a boolean cell")) {
                throw new AssertionFailedError(
                        "Identified bug in conversion of cell from boolean to text");
            }
            throw e;
        }
        assertEquals("TRUE", cell.getRichStringCellValue().getString());
    }

    public void baseTestChangeTypeErrorToNumber(Cell cell) {
        cell.setCellErrorValue((byte)ErrorConstants.ERROR_NAME);
        try {
            cell.setCellValue(2.5);
        } catch (ClassCastException e) {
            throw new AssertionFailedError("Identified bug 46479b");
        }
        assertEquals(2.5, cell.getNumericCellValue(), 0.0);
    }

    public void baseTestChangeTypeErrorToBoolean(Cell cell) {
        cell.setCellErrorValue((byte)ErrorConstants.ERROR_NAME);
        cell.setCellValue(true);
        try {
            cell.getBooleanCellValue();
        } catch (IllegalStateException e) {
            if (e.getMessage().equals("Cannot get a boolean value from a error cell")) {

                throw new AssertionFailedError("Identified bug 46479c");
            }
            throw e;
        }
        assertEquals(true, cell.getBooleanCellValue());
    }

    /**
     * Test for bug in convertCellValueToBoolean to make sure that formula results get converted
     */
    public void baseTestChangeTypeFormulaToBoolean(Cell cell) {
        cell.setCellFormula("1=1");
        cell.setCellValue(true);
        cell.setCellType(Cell.CELL_TYPE_BOOLEAN);
        if (cell.getBooleanCellValue() == false) {
            throw new AssertionFailedError("Identified bug 46479d");
        }
        assertEquals(true, cell.getBooleanCellValue());
    }

}