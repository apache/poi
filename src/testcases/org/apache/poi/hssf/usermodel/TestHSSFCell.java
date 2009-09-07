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
import java.io.FileOutputStream;

import junit.framework.AssertionFailedError;

import org.apache.poi.hssf.HSSFITestDataProvider;
import org.apache.poi.hssf.model.Sheet;
import org.apache.poi.hssf.record.DBCellRecord;
import org.apache.poi.hssf.record.FormulaRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.StringRecord;
import org.apache.poi.ss.usermodel.BaseTestCell;
import org.apache.poi.ss.usermodel.ErrorConstants;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.SpreadsheetVersion;

/**
 * Tests various functionality having to do with {@link HSSFCell}.  For instance support for
 * particular datatypes, etc.
 * @author Andrew C. Oliver (andy at superlinksoftware dot com)
 * @author  Dan Sherman (dsherman at isisph.com)
 * @author Alex Jacoby (ajacoby at gmail.com)
 */
public final class TestHSSFCell extends BaseTestCell {
	
	private static final HSSFITestDataProvider _hssfDP = HSSFITestDataProvider.getInstance();
	
	public TestHSSFCell() {
		super(HSSFITestDataProvider.getInstance());
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
		HSSFWorkbook workbook = _hssfDP.openSampleWorkbook("1900DateWindowing.xls");
		HSSFSheet sheet = workbook.getSheetAt(0);

		assertEquals("Date from file using 1900 Date Windowing",
				date.getTime(),
				sheet.getRow(0).getCell(0).getDateCellValue().getTime());
		 
		// now check a file with 1904 Date Windowing
		workbook = _hssfDP.openSampleWorkbook("1904DateWindowing.xls");
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
		wb = _hssfDP.openSampleWorkbook("1900DateWindowing.xls");
		  
		setCell(wb, 0, 1, date);
		wb = _hssfDP.writeOutAndReadBack(wb);
		  
		assertEquals("Date from file using 1900 Date Windowing",
				date.getTime(),
				readCell(wb, 0, 1).getTime());
		  
		// now check a file with 1904 Date Windowing
		wb = _hssfDP.openSampleWorkbook("1904DateWindowing.xls");
		setCell(wb, 0, 1, date);		  
		wb = _hssfDP.writeOutAndReadBack(wb);
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
		HSSFWorkbook book = _hssfDP.openSampleWorkbook("Simple.xls");
		
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
		book = _hssfDP.writeOutAndReadBack(book);

		umSheet = book.getSheetAt(0);
		s = umSheet.getSheet();
		
		assertEquals("After serialize, active cell should be in col 2",
			(short) 2, s.getActiveCellCol());
		assertEquals("After serialize, active cell should be on row 3",
			3, s.getActiveCellRow());
	}

	/**
	 * Test reading hyperlinks
	 */
	public void testWithHyperlink() {

		HSSFWorkbook wb = _hssfDP.openSampleWorkbook("WithHyperlink.xls");

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

		HSSFWorkbook wb = _hssfDP.openSampleWorkbook("WithTwoHyperLinks.xls");
		
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

    /**
     *  The maximum length of cell contents (text) is 32,767 characters.
     */
    public void testMaxTextLength(){
        HSSFSheet sheet = new HSSFWorkbook().createSheet();
        HSSFCell cell = sheet.createRow(0).createCell(0);

        int maxlen = SpreadsheetVersion.EXCEL97.getMaxTextLength();
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
    }
}

