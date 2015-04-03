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

import java.io.IOException;
import java.util.Calendar;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.ITestDataProvider;
import org.apache.poi.ss.SpreadsheetVersion;

/**
 * Common superclass for testing implementations of
 *  {@link org.apache.poi.ss.usermodel.Cell}
 */
public abstract class BaseTestCell extends TestCase {

	protected final ITestDataProvider _testDataProvider;

	/**
	 * @param testDataProvider an object that provides test data in HSSF / XSSF specific way
	 */
	protected BaseTestCell(ITestDataProvider testDataProvider) {
		_testDataProvider = testDataProvider;
	}

	public void testSetValues() {
		Workbook book = _testDataProvider.createWorkbook();
		Sheet sheet = book.createSheet("test");
		Row row = sheet.createRow(0);

		CreationHelper factory = book.getCreationHelper();
		Cell cell = row.createCell(0);

		cell.setCellValue(1.2);
		assertEquals(1.2, cell.getNumericCellValue(), 0.0001);
		assertEquals(Cell.CELL_TYPE_NUMERIC, cell.getCellType());
		assertProhibitedValueAccess(cell, Cell.CELL_TYPE_BOOLEAN, Cell.CELL_TYPE_STRING,
				Cell.CELL_TYPE_FORMULA, Cell.CELL_TYPE_ERROR);

		cell.setCellValue(false);
		assertEquals(false, cell.getBooleanCellValue());
		assertEquals(Cell.CELL_TYPE_BOOLEAN, cell.getCellType());
		cell.setCellValue(true);
		assertEquals(true, cell.getBooleanCellValue());
		assertProhibitedValueAccess(cell, Cell.CELL_TYPE_NUMERIC, Cell.CELL_TYPE_STRING,
				Cell.CELL_TYPE_FORMULA, Cell.CELL_TYPE_ERROR);

		cell.setCellValue(factory.createRichTextString("Foo"));
		assertEquals("Foo", cell.getRichStringCellValue().getString());
		assertEquals("Foo", cell.getStringCellValue());
		assertEquals(Cell.CELL_TYPE_STRING, cell.getCellType());
		assertProhibitedValueAccess(cell, Cell.CELL_TYPE_NUMERIC, Cell.CELL_TYPE_BOOLEAN,
				Cell.CELL_TYPE_FORMULA, Cell.CELL_TYPE_ERROR);

		cell.setCellValue("345");
		assertEquals("345", cell.getRichStringCellValue().getString());
		assertEquals("345", cell.getStringCellValue());
		assertEquals(Cell.CELL_TYPE_STRING, cell.getCellType());
		assertProhibitedValueAccess(cell, Cell.CELL_TYPE_NUMERIC, Cell.CELL_TYPE_BOOLEAN,
				Cell.CELL_TYPE_FORMULA, Cell.CELL_TYPE_ERROR);

		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(123456789);
		cell.setCellValue(c.getTime());
		assertEquals(c.getTime().getTime(), cell.getDateCellValue().getTime());
		assertEquals(Cell.CELL_TYPE_NUMERIC, cell.getCellType());
		assertProhibitedValueAccess(cell, Cell.CELL_TYPE_BOOLEAN, Cell.CELL_TYPE_STRING,
				Cell.CELL_TYPE_FORMULA, Cell.CELL_TYPE_ERROR);

		cell.setCellValue(c);
		assertEquals(c.getTime().getTime(), cell.getDateCellValue().getTime());
		assertEquals(Cell.CELL_TYPE_NUMERIC, cell.getCellType());
		assertProhibitedValueAccess(cell, Cell.CELL_TYPE_BOOLEAN, Cell.CELL_TYPE_STRING,
				Cell.CELL_TYPE_FORMULA, Cell.CELL_TYPE_ERROR);

		cell.setCellErrorValue(FormulaError.NA.getCode());
		assertEquals(FormulaError.NA.getCode(), cell.getErrorCellValue());
		assertEquals(Cell.CELL_TYPE_ERROR, cell.getCellType());
		assertProhibitedValueAccess(cell, Cell.CELL_TYPE_NUMERIC, Cell.CELL_TYPE_BOOLEAN,
				Cell.CELL_TYPE_FORMULA, Cell.CELL_TYPE_STRING);
	}

	private static void assertProhibitedValueAccess(Cell cell, int ... types){
		for(int type : types){
			try {
				switch (type) {
					case Cell.CELL_TYPE_NUMERIC:
						cell.getNumericCellValue();
						break;
					case Cell.CELL_TYPE_STRING:
						cell.getStringCellValue();
						break;
					case Cell.CELL_TYPE_BOOLEAN:
						cell.getBooleanCellValue();
						break;
					case Cell.CELL_TYPE_FORMULA:
						cell.getCellFormula();
						break;
					case Cell.CELL_TYPE_ERROR:
						cell.getErrorCellValue();
						break;
				}
				fail("Should get exception when reading cell type (" + type + ").");
			} catch (IllegalStateException e){
				// expected during successful test
				assertTrue(e.getMessage().startsWith("Cannot get a"));
			}
		}
	}

	/**
	 * test that Boolean and Error types (BoolErrRecord) are supported properly.
	 */
	public void testBoolErr() {

		Workbook wb = _testDataProvider.createWorkbook();
		Sheet s = wb.createSheet("testSheet1");
		Row r;
		Cell c;
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

		wb = _testDataProvider.writeOutAndReadBack(wb);
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
	public void testFormulaStyle() {

		Workbook wb = _testDataProvider.createWorkbook();
		Sheet s = wb.createSheet("testSheet1");
		Row r = null;
		Cell c = null;
		CellStyle cs = wb.createCellStyle();
		Font f = wb.createFont();
		f.setFontHeightInPoints((short) 20);
		f.setColor(IndexedColors.RED.getIndex());
		f.setBoldweight(Font.BOLDWEIGHT_BOLD);
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

		wb = _testDataProvider.writeOutAndReadBack(wb);
		s = wb.getSheetAt(0);
		r = s.getRow(0);
		c = r.getCell(0);

		assertTrue("Formula Cell at 0,0", (c.getCellType()==Cell.CELL_TYPE_FORMULA));
		cs = c.getCellStyle();

		assertNotNull("Formula Cell Style", cs);
		assertTrue("Font Index Matches", (cs.getFontIndex() == f.getIndex()));
		assertTrue("Top Border", (cs.getBorderTop() == (short)1));
		assertTrue("Left Border", (cs.getBorderLeft() == (short)1));
		assertTrue("Right Border", (cs.getBorderRight() == (short)1));
		assertTrue("Bottom Border", (cs.getBorderBottom() == (short)1));
	}

	/**tests the toString() method of HSSFCell*/
	public void testToString() {
		Workbook wb = _testDataProvider.createWorkbook();
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
		wb = _testDataProvider.writeOutAndReadBack(wb);

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
	public void testSetFormulaValue() {
		Workbook wb = _testDataProvider.createWorkbook();
		Sheet s = wb.createSheet();
		Row r = s.createRow(0);

		Cell c1 = r.createCell(0);
		c1.setCellFormula("NA()");
		assertEquals(0.0, c1.getNumericCellValue(), 0.0);
		assertEquals(Cell.CELL_TYPE_NUMERIC, c1.getCachedFormulaResultType());
		c1.setCellValue(10);
		assertEquals(10.0, c1.getNumericCellValue(), 0.0);
		assertEquals(Cell.CELL_TYPE_FORMULA, c1.getCellType());
		assertEquals(Cell.CELL_TYPE_NUMERIC, c1.getCachedFormulaResultType());

		Cell c2 = r.createCell(1);
		c2.setCellFormula("NA()");
		assertEquals(0.0, c2.getNumericCellValue(), 0.0);
		assertEquals(Cell.CELL_TYPE_NUMERIC, c2.getCachedFormulaResultType());
		c2.setCellValue("I changed!");
		assertEquals("I changed!", c2.getStringCellValue());
		assertEquals(Cell.CELL_TYPE_FORMULA, c2.getCellType());
		assertEquals(Cell.CELL_TYPE_STRING, c2.getCachedFormulaResultType());

        //calglin Cell.setCellFormula(null) for a non-formula cell
        Cell c3 = r.createCell(2);
        c3.setCellFormula(null);
        assertEquals(Cell.CELL_TYPE_BLANK, c3.getCellType());

    }
	private Cell createACell() {
		return _testDataProvider.createWorkbook().createSheet("Sheet1").createRow(0).createCell(0);
	}


	public void testChangeTypeStringToBool() {
		Cell cell = createACell();

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

	public void testChangeTypeBoolToString() {
		Cell cell = createACell();

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

	public void testChangeTypeErrorToNumber() {
		Cell cell = createACell();
		cell.setCellErrorValue((byte)ErrorConstants.ERROR_NAME);
		try {
			cell.setCellValue(2.5);
		} catch (ClassCastException e) {
			throw new AssertionFailedError("Identified bug 46479b");
		}
		assertEquals(2.5, cell.getNumericCellValue(), 0.0);
	}

	public void testChangeTypeErrorToBoolean() {
		Cell cell = createACell();
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
	 * Test for a bug observed around svn r886733 when using
	 * {@link FormulaEvaluator#evaluateInCell(Cell)} with a
	 * string result type.
	 */
	public void testConvertStringFormulaCell() {
		Cell cellA1 = createACell();
		cellA1.setCellFormula("\"abc\"");

		// default cached formula result is numeric zero
		assertEquals(0.0, cellA1.getNumericCellValue(), 0.0);

		FormulaEvaluator fe = cellA1.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();

		fe.evaluateFormulaCell(cellA1);
		assertEquals("abc", cellA1.getStringCellValue());

		fe.evaluateInCell(cellA1);
		if (cellA1.getStringCellValue().equals("")) {
			throw new AssertionFailedError("Identified bug with writing back formula result of type string");
		}
		assertEquals("abc", cellA1.getStringCellValue());
	}
	/**
	 * similar to {@link #testConvertStringFormulaCell()} but  checks at a
	 * lower level that {#link {@link Cell#setCellType(int)} works properly
	 */
	public void testSetTypeStringOnFormulaCell() {
		Cell cellA1 = createACell();
		FormulaEvaluator fe = cellA1.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();

		cellA1.setCellFormula("\"DEF\"");
		fe.clearAllCachedResultValues();
		fe.evaluateFormulaCell(cellA1);
		assertEquals("DEF", cellA1.getStringCellValue());
		cellA1.setCellType(Cell.CELL_TYPE_STRING);
		assertEquals("DEF", cellA1.getStringCellValue());

		cellA1.setCellFormula("25.061");
		fe.clearAllCachedResultValues();
		fe.evaluateFormulaCell(cellA1);
		confirmCannotReadString(cellA1);
		assertEquals(25.061, cellA1.getNumericCellValue(), 0.0);
		cellA1.setCellType(Cell.CELL_TYPE_STRING);
		assertEquals("25.061", cellA1.getStringCellValue());

		cellA1.setCellFormula("TRUE");
		fe.clearAllCachedResultValues();
		fe.evaluateFormulaCell(cellA1);
		confirmCannotReadString(cellA1);
		assertEquals(true, cellA1.getBooleanCellValue());
		cellA1.setCellType(Cell.CELL_TYPE_STRING);
		assertEquals("TRUE", cellA1.getStringCellValue());

		cellA1.setCellFormula("#NAME?");
		fe.clearAllCachedResultValues();
		fe.evaluateFormulaCell(cellA1);
		confirmCannotReadString(cellA1);
		assertEquals(ErrorConstants.ERROR_NAME, cellA1.getErrorCellValue());
		cellA1.setCellType(Cell.CELL_TYPE_STRING);
		assertEquals("#NAME?", cellA1.getStringCellValue());
	}

	private static void confirmCannotReadString(Cell cell) {
		assertProhibitedValueAccess(cell, Cell.CELL_TYPE_STRING);
	}

	/**
	 * Test for bug in convertCellValueToBoolean to make sure that formula results get converted
	 */
	public void testChangeTypeFormulaToBoolean() {
		Cell cell = createACell();
		cell.setCellFormula("1=1");
		cell.setCellValue(true);
		cell.setCellType(Cell.CELL_TYPE_BOOLEAN);
		if (cell.getBooleanCellValue() == false) {
			throw new AssertionFailedError("Identified bug 46479d");
		}
		assertEquals(true, cell.getBooleanCellValue());
	}

	/**
	 * Bug 40296:	  HSSFCell.setCellFormula throws
	 *   ClassCastException if cell is created using HSSFRow.createCell(short column, int type)
	 */
	public void test40296() {
		Workbook wb = _testDataProvider.createWorkbook();
		Sheet workSheet = wb.createSheet("Sheet1");
		Cell cell;
		Row row = workSheet.createRow(0);

		cell = row.createCell(0, Cell.CELL_TYPE_NUMERIC);
		cell.setCellValue(1.0);
		assertEquals(Cell.CELL_TYPE_NUMERIC, cell.getCellType());
		assertEquals(1.0, cell.getNumericCellValue(), 0.0);

		cell = row.createCell(1, Cell.CELL_TYPE_NUMERIC);
		cell.setCellValue(2.0);
		assertEquals(Cell.CELL_TYPE_NUMERIC, cell.getCellType());
		assertEquals(2.0, cell.getNumericCellValue(), 0.0);

		cell = row.createCell(2, Cell.CELL_TYPE_FORMULA);
		cell.setCellFormula("SUM(A1:B1)");
		assertEquals(Cell.CELL_TYPE_FORMULA, cell.getCellType());
		assertEquals("SUM(A1:B1)", cell.getCellFormula());

		//serialize and check again
		wb = _testDataProvider.writeOutAndReadBack(wb);
		row = wb.getSheetAt(0).getRow(0);
		cell = row.getCell(0);
		assertEquals(Cell.CELL_TYPE_NUMERIC, cell.getCellType());
		assertEquals(1.0, cell.getNumericCellValue(), 0.0);

		cell = row.getCell(1);
		assertEquals(Cell.CELL_TYPE_NUMERIC, cell.getCellType());
		assertEquals(2.0, cell.getNumericCellValue(), 0.0);

		cell = row.getCell(2);
		assertEquals(Cell.CELL_TYPE_FORMULA, cell.getCellType());
		assertEquals("SUM(A1:B1)", cell.getCellFormula());
	}

	public void testSetStringInFormulaCell_bug44606() {
		Workbook wb = _testDataProvider.createWorkbook();
		Cell cell = wb.createSheet("Sheet1").createRow(0).createCell(0);
		cell.setCellFormula("B1&C1");
		try {
			cell.setCellValue(wb.getCreationHelper().createRichTextString("hello"));
		} catch (ClassCastException e) {
			throw new AssertionFailedError("Identified bug 44606");
		}
	}

    /**
     *  Make sure that cell.setCellType(Cell.CELL_TYPE_BLANK) preserves the cell style
     */
    public void testSetBlank_bug47028() {
        Workbook wb = _testDataProvider.createWorkbook();
        CellStyle style = wb.createCellStyle();
        Cell cell = wb.createSheet("Sheet1").createRow(0).createCell(0);
        cell.setCellStyle(style);
        int i1 = cell.getCellStyle().getIndex();
        cell.setCellType(Cell.CELL_TYPE_BLANK);
        int i2 = cell.getCellStyle().getIndex();
        assertEquals(i1, i2);
    }

    /**
     * Excel's implementation of floating number arithmetic does not fully adhere to IEEE 754:
     *
     * From http://support.microsoft.com/kb/78113:
     *
     * <ul>
     * <li> Positive/Negative Infinities:
     *   Infinities occur when you divide by 0. Excel does not support infinities, rather,
     *   it gives a #DIV/0! error in these cases.
     * </li>
     * <li>
     *   Not-a-Number (NaN):
     *   NaN is used to represent invalid operations (such as infinity/infinity, 
     *   infinity-infinity, or the square root of -1). NaNs allow a program to
     *   continue past an invalid operation. Excel instead immediately generates
     *   an error such as #NUM! or #DIV/0!.
     * </li>
     * </ul>
     */
    public void testNanAndInfinity() {
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet workSheet = wb.createSheet("Sheet1");
        Row row = workSheet.createRow(0);

        Cell cell0 = row.createCell(0);
        cell0.setCellValue(Double.NaN);
        assertEquals("Double.NaN should change cell type to CELL_TYPE_ERROR", Cell.CELL_TYPE_ERROR, cell0.getCellType());
        assertEquals("Double.NaN should change cell value to #NUM!", ErrorConstants.ERROR_NUM, cell0.getErrorCellValue());

        Cell cell1 = row.createCell(1);
        cell1.setCellValue(Double.POSITIVE_INFINITY);
        assertEquals("Double.POSITIVE_INFINITY should change cell type to CELL_TYPE_ERROR", Cell.CELL_TYPE_ERROR, cell1.getCellType());
        assertEquals("Double.POSITIVE_INFINITY should change cell value to #DIV/0!", ErrorConstants.ERROR_DIV_0, cell1.getErrorCellValue());

        Cell cell2 = row.createCell(2);
        cell2.setCellValue(Double.NEGATIVE_INFINITY);
        assertEquals("Double.NEGATIVE_INFINITY should change cell type to CELL_TYPE_ERROR", Cell.CELL_TYPE_ERROR, cell2.getCellType());
        assertEquals("Double.NEGATIVE_INFINITY should change cell value to #DIV/0!", ErrorConstants.ERROR_DIV_0, cell2.getErrorCellValue());

        wb = _testDataProvider.writeOutAndReadBack(wb);
        row = wb.getSheetAt(0).getRow(0);

        cell0 = row.getCell(0);
        assertEquals(Cell.CELL_TYPE_ERROR, cell0.getCellType());
        assertEquals(ErrorConstants.ERROR_NUM, cell0.getErrorCellValue());

        cell1 = row.getCell(1);
        assertEquals(Cell.CELL_TYPE_ERROR, cell1.getCellType());
        assertEquals(ErrorConstants.ERROR_DIV_0, cell1.getErrorCellValue());

        cell2 = row.getCell(2);
        assertEquals(Cell.CELL_TYPE_ERROR, cell2.getCellType());
        assertEquals(ErrorConstants.ERROR_DIV_0, cell2.getErrorCellValue());
    }

    public void testDefaultStyleProperties() {
        Workbook wb = _testDataProvider.createWorkbook();

        Cell cell = wb.createSheet("Sheet1").createRow(0).createCell(0);
        CellStyle style = cell.getCellStyle();

        assertTrue(style.getLocked());
        assertFalse(style.getHidden());
        assertEquals(0, style.getIndention());
        assertEquals(0, style.getFontIndex());
        assertEquals(0, style.getAlignment());
        assertEquals(0, style.getDataFormat());
        assertEquals(false, style.getWrapText());

        CellStyle style2 = wb.createCellStyle();
        assertTrue(style2.getLocked());
        assertFalse(style2.getHidden());
        style2.setLocked(false);
        style2.setHidden(true);
        assertFalse(style2.getLocked());
        assertTrue(style2.getHidden());

        wb = _testDataProvider.writeOutAndReadBack(wb);
        cell = wb.getSheetAt(0).getRow(0).getCell(0);
        style = cell.getCellStyle();
        assertFalse(style2.getLocked());
        assertTrue(style2.getHidden());

        style2.setLocked(true);
        style2.setHidden(false);
        assertTrue(style2.getLocked());
        assertFalse(style2.getHidden());
    }

    public void testBug55658SetNumericValue(){
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet sh = wb.createSheet();
        Row row = sh.createRow(0);
        Cell cell = row.createCell(0);
        cell.setCellValue(Integer.valueOf(23));
        
        cell.setCellValue("some");

        cell = row.createCell(1);
        cell.setCellValue(Integer.valueOf(23));
        
        cell.setCellValue("24");

        wb = _testDataProvider.writeOutAndReadBack(wb);

        assertEquals("some", wb.getSheetAt(0).getRow(0).getCell(0).getStringCellValue());
        assertEquals("24", wb.getSheetAt(0).getRow(0).getCell(1).getStringCellValue());
    }

    public void testRemoveHyperlink(){
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet sh = wb.createSheet("test");
        Row row = sh.createRow(0);
        CreationHelper helper = wb.getCreationHelper();

        Cell cell1 = row.createCell(1);
        Hyperlink link1 = helper.createHyperlink(Hyperlink.LINK_URL);
        cell1.setHyperlink(link1);
        assertNotNull(cell1.getHyperlink());
        cell1.removeHyperlink();
        assertNull(cell1.getHyperlink());

        Cell cell2 = row.createCell(0);
        Hyperlink link2 = helper.createHyperlink(Hyperlink.LINK_URL);
        cell2.setHyperlink(link2);
        assertNotNull(cell2.getHyperlink());
        cell2.setHyperlink(null);
        assertNull(cell2.getHyperlink());

        Cell cell3 = row.createCell(2);
        Hyperlink link3 = helper.createHyperlink(Hyperlink.LINK_URL);
        link3.setAddress("http://poi.apache.org/");
        cell3.setHyperlink(link3);
        assertNotNull(cell3.getHyperlink());

        Workbook wbBack = _testDataProvider.writeOutAndReadBack(wb);
        assertNotNull(wbBack);
        
        cell1 = wbBack.getSheet("test").getRow(0).getCell(1);
        assertNull(cell1.getHyperlink());
        cell2 = wbBack.getSheet("test").getRow(0).getCell(0);
        assertNull(cell2.getHyperlink());
        cell3 = wbBack.getSheet("test").getRow(0).getCell(2);
        assertNotNull(cell3.getHyperlink());
    }

    /**
     * Cell with the formula that returns error must return error code(There was
     * an problem that cell could not return error value form formula cell).
     * @throws IOException 
     */
    public void testGetErrorCellValueFromFormulaCell() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();
        try {
            Sheet sheet = wb.createSheet();
            Row row = sheet.createRow(0);
            Cell cell = row.createCell(0);
            cell.setCellFormula("SQRT(-1)");
            wb.getCreationHelper().createFormulaEvaluator().evaluateFormulaCell(cell);
            assertEquals(36, cell.getErrorCellValue());
        } finally {
            wb.close();
        }
    }
    
    public void testSetRemoveStyle() throws Exception {
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet sheet = wb.createSheet();
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        
        // different default style indexes for HSSF and XSSF/SXSSF
        CellStyle defaultStyle = wb.getCellStyleAt(wb instanceof HSSFWorkbook ? (short)15 : (short)0);
        
        // Starts out with the default style
        assertEquals(defaultStyle, cell.getCellStyle());
        
        // Create some styles, no change
        CellStyle style1 = wb.createCellStyle();
        CellStyle style2 = wb.createCellStyle();
        style1.setDataFormat((short)2);
        style2.setDataFormat((short)3);
        
        assertEquals(defaultStyle, cell.getCellStyle());
        
        // Apply one, changes
        cell.setCellStyle(style1);
        assertEquals(style1, cell.getCellStyle());
        
        // Apply the other, changes
        cell.setCellStyle(style2);
        assertEquals(style2, cell.getCellStyle());
        
        // Remove, goes back to default
        cell.setCellStyle(null);
        assertEquals(defaultStyle, cell.getCellStyle());
        
        // Add back, returns
        cell.setCellStyle(style2);
        assertEquals(style2, cell.getCellStyle());
        
        wb.close();
    }

	public void test57008() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();
		Sheet sheet = wb.createSheet();
		
		Row row0 = sheet.createRow(0);
		Cell cell0 = row0.createCell(0);
		cell0.setCellValue("row 0, cell 0 _x0046_ without changes");
		
		Cell cell1 = row0.createCell(1);
		cell1.setCellValue("row 0, cell 1 _x005fx0046_ with changes");
		
		Cell cell2 = row0.createCell(2);
		cell2.setCellValue("hgh_x0041_**_x0100_*_x0101_*_x0190_*_x0200_*_x0300_*_x0427_*");

		checkUnicodeValues(wb);
		
//		String fname = "/tmp/Test_xNNNN_inCell" + (wb instanceof HSSFWorkbook ? ".xls" : ".xlsx");
//		FileOutputStream out = new FileOutputStream(fname);
//		try {
//			wb.write(out);
//		} finally {
//			out.close();
//		}
		
		Workbook wbBack = _testDataProvider.writeOutAndReadBack(wb);
		checkUnicodeValues(wbBack);
	}

	private void checkUnicodeValues(Workbook wb) {
		assertEquals((wb instanceof HSSFWorkbook ? "row 0, cell 0 _x0046_ without changes" : "row 0, cell 0 F without changes"), 
				wb.getSheetAt(0).getRow(0).getCell(0).toString());
		assertEquals((wb instanceof HSSFWorkbook ? "row 0, cell 1 _x005fx0046_ with changes" : "row 0, cell 1 _x005fx0046_ with changes"), 
				wb.getSheetAt(0).getRow(0).getCell(1).toString());
		assertEquals((wb instanceof HSSFWorkbook ? "hgh_x0041_**_x0100_*_x0101_*_x0190_*_x0200_*_x0300_*_x0427_*" : "hghA**\u0100*\u0101*\u0190*\u0200*\u0300*\u0427*"), 
				wb.getSheetAt(0).getRow(0).getCell(2).toString());
	}

	/**
	 *  The maximum length of cell contents (text) is 32,767 characters.
	 * @throws IOException 
	 */
	public void testMaxTextLength() throws IOException{
		Workbook wb = _testDataProvider.createWorkbook();
        Sheet sheet = wb.createSheet();
		Cell cell = sheet.createRow(0).createCell(0);

		int maxlen = wb instanceof HSSFWorkbook ? 
				SpreadsheetVersion.EXCEL97.getMaxTextLength()
				: SpreadsheetVersion.EXCEL2007.getMaxTextLength();
		assertEquals(32767, maxlen);

		StringBuffer b = new StringBuffer() ;

		// 32767 is okay
		for( int i = 0 ; i < maxlen ; i++ )
		{
			b.append( "X" ) ;
		}
		cell.setCellValue(b.toString());

		b.append("X");
		// 32768 produces an invalid XLS file
		try {
			cell.setCellValue(b.toString());
			fail("Expected exception");
		} catch (IllegalArgumentException e){
			assertEquals("The maximum length of cell contents (text) is 32,767 characters", e.getMessage());
		}
		wb.close();
	}
}
