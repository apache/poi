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

import java.util.Calendar;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.poi.ss.ITestDataProvider;

/**
 * Common superclass for testing implementatiosn of
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

	public final void testSetValues() {
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
	public final void testBoolErr() {

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
	public final void testFormulaStyle() {

		Workbook wb = _testDataProvider.createWorkbook();
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

		wb = _testDataProvider.writeOutAndReadBack(wb);
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
	public final void testToString() {
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
	public final void testSetFormulaValue() {
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


	public final void testChangeTypeStringToBool() {
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

	public final void testChangeTypeBoolToString() {
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

	public final void testChangeTypeErrorToNumber() {
		Cell cell = createACell();
		cell.setCellErrorValue((byte)ErrorConstants.ERROR_NAME);
		try {
			cell.setCellValue(2.5);
		} catch (ClassCastException e) {
			throw new AssertionFailedError("Identified bug 46479b");
		}
		assertEquals(2.5, cell.getNumericCellValue(), 0.0);
	}

	public final void testChangeTypeErrorToBoolean() {
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
	public final void testConvertStringFormulaCell() {
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
	public final void testSetTypeStringOnFormulaCell() {
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

	public final void testSetStringInFormulaCell_bug44606() {
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
}
