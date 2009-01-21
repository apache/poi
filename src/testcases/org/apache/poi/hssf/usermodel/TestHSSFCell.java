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

package org.apache.poi.hssf.usermodel;

import java.util.Date;
import java.util.GregorianCalendar;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.model.Sheet;
import org.apache.poi.hssf.record.DBCellRecord;
import org.apache.poi.hssf.record.FormulaRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.StringRecord;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.ErrorConstants;

/**
 * Tests various functionality having to do with {@link HSSFCell}.  For instance support for
 * particular datatypes, etc.
 * @author Andrew C. Oliver (andy at superlinksoftware dot com)
 * @author  Dan Sherman (dsherman at isisph.com)
 * @author Alex Jacoby (ajacoby at gmail.com)
 */
public final class TestHSSFCell extends TestCase {

	private static HSSFWorkbook openSample(String sampleFileName) {
		return HSSFTestDataSamples.openSampleWorkbook(sampleFileName);
	}
	private static HSSFWorkbook writeOutAndReadBack(HSSFWorkbook original) {
		return HSSFTestDataSamples.writeOutAndReadBack(original);
	}
	
	public void testSetValues() {
		HSSFWorkbook book = new HSSFWorkbook();
		HSSFSheet sheet = book.createSheet("test");
		HSSFRow row = sheet.createRow(0);

		HSSFCell cell = row.createCell(0);
		
		cell.setCellValue(1.2);
		assertEquals(1.2, cell.getNumericCellValue(), 0.0001);
		assertEquals(HSSFCell.CELL_TYPE_NUMERIC, cell.getCellType());
		
		cell.setCellValue(false);
		assertEquals(false, cell.getBooleanCellValue());
		assertEquals(HSSFCell.CELL_TYPE_BOOLEAN, cell.getCellType());
		
		cell.setCellValue(new HSSFRichTextString("Foo"));
		assertEquals("Foo", cell.getRichStringCellValue().getString());
		assertEquals(HSSFCell.CELL_TYPE_STRING, cell.getCellType());
		
		cell.setCellValue(new HSSFRichTextString("345"));
		assertEquals("345", cell.getRichStringCellValue().getString());
		assertEquals(HSSFCell.CELL_TYPE_STRING, cell.getCellType());
	}
	
	/**
	 * test that Boolean and Error types (BoolErrRecord) are supported properly.
	 */
	public void testBoolErr() {

		HSSFWorkbook	 wb	 = new HSSFWorkbook();
		HSSFSheet		s	  = wb.createSheet("testSheet1");
		HSSFRow		  r	  = null;
		HSSFCell		 c	  = null;
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

		wb = writeOutAndReadBack(wb);
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
	 * Checks that the recognition of files using 1904 date windowing
	 *  is working properly. Conversion of the date is also an issue,
	 *  but there's a separate unit test for that.
	 */
	public void testDateWindowingRead() {
		GregorianCalendar cal = new GregorianCalendar(2000,0,1); // Jan. 1, 2000
		Date date = cal.getTime();

		// first check a file with 1900 Date Windowing
		HSSFWorkbook workbook = openSample("1900DateWindowing.xls");
		HSSFSheet sheet = workbook.getSheetAt(0);

		assertEquals("Date from file using 1900 Date Windowing",
				date.getTime(),
				sheet.getRow(0).getCell(0).getDateCellValue().getTime());
		 
		// now check a file with 1904 Date Windowing
		workbook = openSample("1904DateWindowing.xls");
		sheet	= workbook.getSheetAt(0);

		assertEquals("Date from file using 1904 Date Windowing",
				date.getTime(),
				sheet.getRow(0).getCell(0).getDateCellValue().getTime());
	}

	/**
	 * Checks that dates are properly written to both types of files:
	 * those with 1900 and 1904 date windowing.  Note that if the
	 * previous test ({@link #testDateWindowingRead}) fails, the
	 * results of this test are meaningless.
	 */
	public void testDateWindowingWrite() {
		GregorianCalendar cal = new GregorianCalendar(2000,0,1); // Jan. 1, 2000
		Date date = cal.getTime();

		// first check a file with 1900 Date Windowing
		HSSFWorkbook wb;
		wb = openSample("1900DateWindowing.xls");
		  
		setCell(wb, 0, 1, date);
		wb = writeOutAndReadBack(wb);
		  
		assertEquals("Date from file using 1900 Date Windowing",
				date.getTime(),
				readCell(wb, 0, 1).getTime());
		  
		// now check a file with 1904 Date Windowing
		wb = openSample("1904DateWindowing.xls");
		setCell(wb, 0, 1, date);		  
		wb = writeOutAndReadBack(wb);
		assertEquals("Date from file using 1900 Date Windowing",
				date.getTime(),
				readCell(wb, 0, 1).getTime());
	}

	private static void setCell(HSSFWorkbook workbook, int rowIdx, int colIdx, Date date) {
		HSSFSheet sheet = workbook.getSheetAt(0);
		HSSFRow row = sheet.getRow(rowIdx);
		HSSFCell cell = row.getCell(colIdx);
		  
		if (cell == null) {
			cell = row.createCell(colIdx);
		}
		cell.setCellValue(date);
	}
	  
	private static Date readCell(HSSFWorkbook workbook, int rowIdx, int colIdx) {
		HSSFSheet sheet = workbook.getSheetAt(0);
		HSSFRow row = sheet.getRow(rowIdx);
		HSSFCell cell = row.getCell(colIdx);
		return cell.getDateCellValue();
	}
	  
	/**
	 * Tests that the active cell can be correctly read and set
	 */
	public void testActiveCell() {
		//read in sample
		HSSFWorkbook book = openSample("Simple.xls");
		
		//check initial position
		HSSFSheet umSheet = book.getSheetAt(0);
		Sheet s = umSheet.getSheet();
		assertEquals("Initial active cell should be in col 0",
			(short) 0, s.getActiveCellCol());
		assertEquals("Initial active cell should be on row 1",
			1, s.getActiveCellRow());
		
		//modify position through HSSFCell
		HSSFCell cell = umSheet.createRow(3).createCell(2);
		cell.setAsActiveCell();
		assertEquals("After modify, active cell should be in col 2",
			(short) 2, s.getActiveCellCol());
		assertEquals("After modify, active cell should be on row 3",
			3, s.getActiveCellRow());
		
		//write book to temp file; read and verify that position is serialized
		book = writeOutAndReadBack(book);

		umSheet = book.getSheetAt(0);
		s = umSheet.getSheet();
		
		assertEquals("After serialize, active cell should be in col 2",
			(short) 2, s.getActiveCellCol());
		assertEquals("After serialize, active cell should be on row 3",
			3, s.getActiveCellRow());
	}

	/**
	 * test that Cell Styles being applied to formulas remain intact
	 */
	public void testFormulaStyle() {

		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet s = wb.createSheet("testSheet1");
		HSSFRow r = null;
		HSSFCell c = null;
		HSSFCellStyle cs = wb.createCellStyle();
		HSSFFont f = wb.createFont();
		f.setFontHeightInPoints((short) 20);
		f.setColor(HSSFColor.RED.index);
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
		
		wb = writeOutAndReadBack(wb);
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

	/**
	 * Test reading hyperlinks
	 */
	public void testWithHyperlink() {

		HSSFWorkbook wb = openSample("WithHyperlink.xls");

		HSSFSheet sheet = wb.getSheetAt(0);
		HSSFCell cell = sheet.getRow(4).getCell(0);
		HSSFHyperlink link = cell.getHyperlink();
		assertNotNull(link);

		assertEquals("Foo", link.getLabel());
		assertEquals("http://poi.apache.org/", link.getAddress());
		assertEquals(4, link.getFirstRow());
		assertEquals(0, link.getFirstColumn());
	}
	
	/**
	 * Test reading hyperlinks
	 */
	public void testWithTwoHyperlinks() {

		HSSFWorkbook wb = openSample("WithTwoHyperLinks.xls");
		
		HSSFSheet sheet = wb.getSheetAt(0);

		HSSFCell cell1 = sheet.getRow(4).getCell(0);
		HSSFHyperlink link1 = cell1.getHyperlink();
		assertNotNull(link1);
		assertEquals("Foo", link1.getLabel());
		assertEquals("http://poi.apache.org/", link1.getAddress());
		assertEquals(4, link1.getFirstRow());
		assertEquals(0, link1.getFirstColumn());

		HSSFCell cell2 = sheet.getRow(8).getCell(1);
		HSSFHyperlink link2 = cell2.getHyperlink();
		assertNotNull(link2);
		assertEquals("Bar", link2.getLabel());
		assertEquals("http://poi.apache.org/hssf/", link2.getAddress());
		assertEquals(8, link2.getFirstRow());
		assertEquals(1, link2.getFirstColumn());
	}
	
	/**tests the toString() method of HSSFCell*/
	public void testToString() {
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFRow r = wb.createSheet("Sheet1").createRow(0);

		r.createCell(0).setCellValue(true);
		r.createCell(1).setCellValue(1.5);
		r.createCell(2).setCellValue(new HSSFRichTextString("Astring"));
		r.createCell(3).setCellErrorValue((byte)HSSFErrorConstants.ERROR_DIV_0);
		r.createCell(4).setCellFormula("A1+B1");

		assertEquals("Boolean", "TRUE", r.getCell(0).toString());
		assertEquals("Numeric", "1.5", r.getCell(1).toString());
		assertEquals("String", "Astring", r.getCell(2).toString());
		assertEquals("Error", "#DIV/0!", r.getCell(3).toString());
		assertEquals("Formula", "A1+B1", r.getCell(4).toString());

		//Write out the file, read it in, and then check cell values
		wb = writeOutAndReadBack(wb);

		r = wb.getSheetAt(0).getRow(0);
		assertEquals("Boolean", "TRUE", r.getCell(0).toString());
		assertEquals("Numeric", "1.5", r.getCell(1).toString());
		assertEquals("String", "Astring", r.getCell(2).toString());
		assertEquals("Error", "#DIV/0!", r.getCell(3).toString());
		assertEquals("Formula", "A1+B1", r.getCell(4).toString());
	}
	
	public void testSetStringInFormulaCell_bug44606() {
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFCell cell = wb.createSheet("Sheet1").createRow(0).createCell(0);
		cell.setCellFormula("B1&C1");
		try {
			cell.setCellValue(new HSSFRichTextString("hello"));
		} catch (ClassCastException e) {
			throw new AssertionFailedError("Identified bug 44606");
		}
	}

	/**
	 * Test to ensure we can only assign cell styles that belong
	 *  to our workbook, and not those from other workbooks.
	 */
	public void testCellStyleWorkbookMatch() {
		HSSFWorkbook wbA = new HSSFWorkbook();
		HSSFWorkbook wbB = new HSSFWorkbook();

		HSSFCellStyle styA = wbA.createCellStyle();
		HSSFCellStyle styB = wbB.createCellStyle();

		styA.verifyBelongsToWorkbook(wbA);
		styB.verifyBelongsToWorkbook(wbB);
		try {
			styA.verifyBelongsToWorkbook(wbB);
			fail();
		} catch (IllegalArgumentException e) {
			// expected during successful test
		}
		try {
			styB.verifyBelongsToWorkbook(wbA);
			fail();
		} catch (IllegalArgumentException e) {
			// expected during successful test
		}

		HSSFCell cellA = wbA.createSheet().createRow(0).createCell(0);
		HSSFCell cellB = wbB.createSheet().createRow(0).createCell(0);

		cellA.setCellStyle(styA);
		cellB.setCellStyle(styB);
		try {
			cellA.setCellStyle(styB);
			fail();
		} catch (IllegalArgumentException e) {
			// expected during successful test
		}
		try {
			cellB.setCellStyle(styA);
			fail();
		} catch (IllegalArgumentException e) {
			// expected during successful test
		}
	}

	public void testChangeTypeStringToBool() {
		HSSFCell cell = new HSSFWorkbook().createSheet("Sheet1").createRow(0).createCell(0);

		cell.setCellValue(new HSSFRichTextString("TRUE"));
		assertEquals(HSSFCell.CELL_TYPE_STRING, cell.getCellType());
		try {
			cell.setCellType(HSSFCell.CELL_TYPE_BOOLEAN);
		} catch (ClassCastException e) {
			throw new AssertionFailedError(
					"Identified bug in conversion of cell from text to boolean");
		}

		assertEquals(HSSFCell.CELL_TYPE_BOOLEAN, cell.getCellType());
		assertEquals(true, cell.getBooleanCellValue());
		cell.setCellType(HSSFCell.CELL_TYPE_STRING);
		assertEquals("TRUE", cell.getRichStringCellValue().getString());

		// 'false' text to bool and back
		cell.setCellValue(new HSSFRichTextString("FALSE"));
		cell.setCellType(HSSFCell.CELL_TYPE_BOOLEAN);
		assertEquals(HSSFCell.CELL_TYPE_BOOLEAN, cell.getCellType());
		assertEquals(false, cell.getBooleanCellValue());
		cell.setCellType(HSSFCell.CELL_TYPE_STRING);
		assertEquals("FALSE", cell.getRichStringCellValue().getString());
	}

	public void testChangeTypeBoolToString() {
		HSSFCell cell = new HSSFWorkbook().createSheet("Sheet1").createRow(0).createCell(0);
		cell.setCellValue(true);
		try {
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
		} catch (IllegalStateException e) {
			if (e.getMessage().equals("Cannot get a text value from a boolean cell")) {
				throw new AssertionFailedError(
						"Identified bug in conversion of cell from boolean to text");
			}
			throw e;
		}
		assertEquals("TRUE", cell.getRichStringCellValue().getString());
	}

	public void testChangeTypeErrorToNumber_bug46479() {
		HSSFCell cell = new HSSFWorkbook().createSheet("Sheet1").createRow(0).createCell(0);
		cell.setCellErrorValue((byte)HSSFErrorConstants.ERROR_NAME);
		try {
			cell.setCellValue(2.5);
		} catch (ClassCastException e) {
			throw new AssertionFailedError("Identified bug 46479b");
		}
		assertEquals(2.5, cell.getNumericCellValue(), 0.0);
	}

	public void testChangeTypeErrorToBoolean_bug46479() {
		HSSFCell cell = new HSSFWorkbook().createSheet("Sheet1").createRow(0).createCell(0);
		cell.setCellErrorValue((byte)HSSFErrorConstants.ERROR_NAME);
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
	public void testChangeTypeFormulaToBoolean_bug46479() {
		HSSFCell cell = new HSSFWorkbook().createSheet("Sheet1").createRow(0).createCell(0);
		cell.setCellFormula("1=1");
		cell.setCellValue(true);
		cell.setCellType(HSSFCell.CELL_TYPE_BOOLEAN);
		if (cell.getBooleanCellValue() == false) {
			throw new AssertionFailedError("Identified bug 46479d");
		}
		assertEquals(true, cell.getBooleanCellValue());
	}
	
	/**
	 * Test for small bug observable around r736460 (prior to version 3.5).  POI fails to remove
	 * the {@link StringRecord} following the {@link FormulaRecord} after the result type had been 
	 * changed to number/boolean/error.  Excel silently ignores the extra record, but some POI
	 * versions (prior to bug 46213 / r717883) crash instead.
	 */
	public void testCachedTypeChange() {
		HSSFSheet sheet = new HSSFWorkbook().createSheet("Sheet1");
		HSSFCell cell = sheet.createRow(0).createCell(0);
		cell.setCellFormula("A1");
		cell.setCellValue("abc");
		confirmStringRecord(sheet, true);
		cell.setCellValue(123);
		Record[] recs = RecordInspector.getRecords(sheet, 0);
		if (recs.length == 28 && recs[23] instanceof StringRecord) {
			throw new AssertionFailedError("Identified bug - leftover StringRecord");
		}
		confirmStringRecord(sheet, false);
		
		// string to error code
		cell.setCellValue("abc");
		confirmStringRecord(sheet, true);
		cell.setCellErrorValue((byte)ErrorConstants.ERROR_REF);
		confirmStringRecord(sheet, false);
		
		// string to boolean
		cell.setCellValue("abc");
		confirmStringRecord(sheet, true);
		cell.setCellValue(false);
		confirmStringRecord(sheet, false);
	}

	private static void confirmStringRecord(HSSFSheet sheet, boolean isPresent) {
		Record[] recs = RecordInspector.getRecords(sheet, 0);
		assertEquals(isPresent ? 28 : 27, recs.length);
		int index = 22;
		Record fr = recs[index++];
		assertEquals(FormulaRecord.class, fr.getClass());
		if (isPresent) {
			assertEquals(StringRecord.class, recs[index++].getClass());
		} else {
			assertFalse(StringRecord.class == recs[index].getClass());
		}
		Record dbcr = recs[index++];
		assertEquals(DBCellRecord.class, dbcr.getClass());
	}
}

