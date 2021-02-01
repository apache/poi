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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import org.apache.poi.hssf.HSSFITestDataProvider;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.model.InternalSheet;
import org.apache.poi.hssf.record.DBCellRecord;
import org.apache.poi.hssf.record.FormulaRecord;
import org.apache.poi.hssf.record.StringRecord;
import org.apache.poi.ss.usermodel.BaseTestCell;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FormulaError;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.LocaleUtil;
import org.junit.jupiter.api.Test;

/**
 * Tests various functionality having to do with {@link HSSFCell}.  For instance support for
 * particular datatypes, etc.
 */
final class TestHSSFCell extends BaseTestCell {

	public TestHSSFCell() {
		super(HSSFITestDataProvider.instance);
	}

	/**
	 * Checks that the recognition of files using 1904 date windowing
	 *  is working properly. Conversion of the date is also an issue,
	 *  but there's a separate unit test for that.
	 */
	@Test
	void testDateWindowingRead() throws IOException {
	    Calendar cal = LocaleUtil.getLocaleCalendar(2000, 0, 1, 0, 0, 0);// Jan. 1, 2000
		Date date = cal.getTime();

		// first check a file with 1900 Date Windowing
		try (HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("1900DateWindowing.xls")) {
			HSSFSheet sheet = wb.getSheetAt(0);

			assertEquals(date.getTime(),
						 sheet.getRow(0).getCell(0).getDateCellValue().getTime(),
						 "Date from file using 1900 Date Windowing");
		}

		// now check a file with 1904 Date Windowing
		try (HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("1904DateWindowing.xls")) {
			HSSFSheet sheet = wb.getSheetAt(0);

			assertEquals(date.getTime(),
						 sheet.getRow(0).getCell(0).getDateCellValue().getTime(),
						 "Date from file using 1904 Date Windowing");
		}
	}



	/**
	 * Checks that dates are properly written to both types of files:
	 * those with 1900 and 1904 date windowing.  Note that if the
	 * previous test ({@link #testDateWindowingRead}) fails, the
	 * results of this test are meaningless.
	 */
	@Test
	void testDateWindowingWrite() throws IOException {
	    Calendar cal = LocaleUtil.getLocaleCalendar(2000,0,1,0,0,0); // Jan. 1, 2000
		Date date = cal.getTime();

		// first check a file with 1900 Date Windowing
		try (HSSFWorkbook wb1 = HSSFTestDataSamples.openSampleWorkbook("1900DateWindowing.xls")) {

			setCell(wb1, 0, 1, date);
			try (HSSFWorkbook wb2 = HSSFTestDataSamples.writeOutAndReadBack(wb1)) {

				assertEquals(date.getTime(),
							 readCell(wb2, 0, 1).getTime(),
							 "Date from file using 1900 Date Windowing");
			}
		}

		// now check a file with 1904 Date Windowing
		try (HSSFWorkbook wb1 = HSSFTestDataSamples.openSampleWorkbook("1904DateWindowing.xls")) {
			setCell(wb1, 0, 1, date);
			try (HSSFWorkbook wb2 = HSSFTestDataSamples.writeOutAndReadBack(wb1)) {
				assertEquals(date.getTime(),
							 readCell(wb2, 0, 1).getTime(),
							 "Date from file using 1900 Date Windowing");
			}
		}
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
	@Test
	void testActiveCell() throws IOException {
		//read in sample
		try (HSSFWorkbook wb1 = HSSFTestDataSamples.openSampleWorkbook("Simple.xls")) {

			//check initial position
			HSSFSheet umSheet = wb1.getSheetAt(0);
			InternalSheet s = umSheet.getSheet();
			assertEquals((short) 0, s.getActiveCellCol(), "Initial active cell should be in col 0");
			assertEquals(1, s.getActiveCellRow(), "Initial active cell should be on row 1");

			//modify position through HSSFCell
			HSSFCell cell = umSheet.createRow(3).createCell(2);
			cell.setAsActiveCell();
			assertEquals((short) 2, s.getActiveCellCol(), "After modify, active cell should be in col 2");
			assertEquals(3, s.getActiveCellRow(), "After modify, active cell should be on row 3");

			//write book to temp file; read and verify that position is serialized
			try (HSSFWorkbook wb2 = HSSFTestDataSamples.writeOutAndReadBack(wb1)) {
				umSheet = wb2.getSheetAt(0);
				s = umSheet.getSheet();

				assertEquals((short) 2, s.getActiveCellCol(), "After serialize, active cell should be in col 2");
				assertEquals(3, s.getActiveCellRow(), "After serialize, active cell should be on row 3");
			}
		}
	}


	@Test
	void testActiveCellBug56114() throws IOException {
	    try (Workbook wb = new HSSFWorkbook()) {
			Sheet sh = wb.createSheet();

			sh.createRow(0);
			sh.createRow(1);
			sh.createRow(2);
			sh.createRow(3);

			Cell cell = sh.getRow(1).createCell(3);
			sh.getRow(3).createCell(3);

			assertEquals(0, ((HSSFSheet) wb.getSheetAt(0)).getSheet().getActiveCellRow());
			assertEquals(0, ((HSSFSheet) wb.getSheetAt(0)).getSheet().getActiveCellCol());

			cell.setAsActiveCell();
			cell.setCellValue("this should be active");

			assertEquals(1, ((HSSFSheet) wb.getSheetAt(0)).getSheet().getActiveCellRow());
			assertEquals(3, ((HSSFSheet) wb.getSheetAt(0)).getSheet().getActiveCellCol());

			try (Workbook wbBack = _testDataProvider.writeOutAndReadBack(wb)) {
				assertEquals(1, ((HSSFSheet) wbBack.getSheetAt(0)).getSheet().getActiveCellRow());
				assertEquals(3, ((HSSFSheet) wbBack.getSheetAt(0)).getSheet().getActiveCellCol());

				wbBack.getSheetAt(0).getRow(3).getCell(3).setAsActiveCell();

				assertEquals(3, ((HSSFSheet) wbBack.getSheetAt(0)).getSheet().getActiveCellRow());
				assertEquals(3, ((HSSFSheet) wbBack.getSheetAt(0)).getSheet().getActiveCellCol());

				try (Workbook wbBack2 = _testDataProvider.writeOutAndReadBack(wbBack)) {
					assertEquals(3, ((HSSFSheet) wbBack2.getSheetAt(0)).getSheet().getActiveCellRow());
					assertEquals(3, ((HSSFSheet) wbBack2.getSheetAt(0)).getSheet().getActiveCellCol());
				}
			}
		}
	}

	/**
	 * Test reading hyperlinks
	 */
	@Test
	void testWithHyperlink() throws IOException {
		try (HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("WithHyperlink.xls")) {

			HSSFSheet sheet = wb.getSheetAt(0);
			HSSFCell cell = sheet.getRow(4).getCell(0);
			HSSFHyperlink link = cell.getHyperlink();
			assertNotNull(link);

			assertEquals("Foo", link.getLabel());
			assertEquals("http://poi.apache.org/", link.getAddress());
			assertEquals(4, link.getFirstRow());
			assertEquals(0, link.getFirstColumn());
		}
	}

	/**
	 * Test reading hyperlinks
	 */
	@Test
	void testWithTwoHyperlinks() throws IOException {
		try (HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("WithTwoHyperLinks.xls")) {

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
	}

	/**
	 * Test to ensure we can only assign cell styles that belong
	 *  to our workbook, and not those from other workbooks.
	 */
	@Test
	void testCellStyleWorkbookMatch() throws IOException {
		try (HSSFWorkbook wbA = new HSSFWorkbook();
			HSSFWorkbook wbB = new HSSFWorkbook()) {

			HSSFCellStyle styA = wbA.createCellStyle();
			HSSFCellStyle styB = wbB.createCellStyle();

			styA.verifyBelongsToWorkbook(wbA);
			styB.verifyBelongsToWorkbook(wbB);
			assertThrows(IllegalArgumentException.class, () -> styA.verifyBelongsToWorkbook(wbB));
			assertThrows(IllegalArgumentException.class, () -> styB.verifyBelongsToWorkbook(wbA));

			Cell cellA = wbA.createSheet().createRow(0).createCell(0);
			Cell cellB = wbB.createSheet().createRow(0).createCell(0);

			cellA.setCellStyle(styA);
			cellB.setCellStyle(styB);
			assertThrows(IllegalArgumentException.class, () -> cellA.setCellStyle(styB));
			assertThrows(IllegalArgumentException.class, () -> cellB.setCellStyle(styA));
		}
	}

	/**
	 * Test for small bug observable around r736460 (prior to version 3.5).  POI fails to remove
	 * the {@link StringRecord} following the {@link FormulaRecord} after the result type had been
	 * changed to number/boolean/error.  Excel silently ignores the extra record, but some POI
	 * versions (prior to bug 46213 / r717883) crash instead.
	 */
	@Test
	void testCachedTypeChange() throws IOException {
		try (HSSFWorkbook wb = new HSSFWorkbook()) {
			HSSFSheet sheet = wb.createSheet("Sheet1");
			Cell cell = sheet.createRow(0).createCell(0);
			cell.setCellFormula("A1");
			cell.setCellValue("abc");
			confirmStringRecord(sheet, true);
			cell.setCellValue(123);

			final List<org.apache.poi.hssf.record.Record> recs = new ArrayList<>();
			sheet.getSheet().visitContainedRecords(recs::add, 0);
			assertFalse(recs.size() == 28 && recs.get(23) instanceof StringRecord, "Identified bug - leftover StringRecord");
			confirmStringRecord(sheet, false);

			// string to error code
			cell.setCellValue("abc");
			confirmStringRecord(sheet, true);
			cell.setCellErrorValue(FormulaError.REF.getCode());
			confirmStringRecord(sheet, false);

			// string to boolean
			cell.setCellValue("abc");
			confirmStringRecord(sheet, true);
			cell.setCellValue(false);
			confirmStringRecord(sheet, false);
		}
	}

	private static void confirmStringRecord(HSSFSheet sheet, boolean isPresent) {
		List<org.apache.poi.hssf.record.Record> recs = new ArrayList<>();
		sheet.getSheet().visitContainedRecords(recs::add, 0);
		assertEquals(isPresent ? 28 : 27, recs.size());

		ListIterator<org.apache.poi.hssf.record.Record> iter = recs.listIterator(22);
		assertEquals(FormulaRecord.class, iter.next().getClass());
		if (isPresent) {
			assertEquals(StringRecord.class, iter.next().getClass());
		}
		assertEquals(DBCellRecord.class, iter.next().getClass());
	}

    /**
     * HSSF prior to version 3.7 had a bug: it could write a NaN but could not read such a file back.
     */
	@Test
	void testReadNaN() throws IOException {
        try (HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("49761.xls")) {
			assertNotNull(wb);
		}
    }

	@Test
	void testHSSFCell() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
			HSSFSheet sheet = wb.createSheet();
			HSSFRow row = sheet.createRow(0);
			row.createCell(0);
			HSSFCell cell = new HSSFCell(wb, sheet, 0, (short) 0);
			assertNotNull(cell);
		}
    }

    @Test
    void testDeprecatedMethods() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
			HSSFSheet sheet = wb.createSheet();
			HSSFRow row = sheet.createRow(0);
			HSSFCell cell = row.createCell(0);

			// cover some deprecated methods and other smaller stuff...
			assertEquals(wb.getWorkbook(), cell.getBoundWorkbook());

			assertThrows(IllegalStateException.class, cell::getCachedFormulaResultType);

			cell.removeCellComment();
			cell.removeCellComment();
		}
    }

    @Test
    void testCellType() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
			Sheet sheet = wb.createSheet();
			Row row = sheet.createRow(0);
			Cell cell = row.createCell(0);

			cell.setBlank();
			assertNull(cell.getDateCellValue());
			assertFalse(cell.getBooleanCellValue());
			assertEquals("", cell.toString());

			cell.setCellType(CellType.STRING);
			assertEquals("", cell.toString());
			cell.setCellValue(1.2);
			assertEquals("1.2", cell.toString());
			cell.setCellType(CellType.BOOLEAN);
			assertEquals("TRUE", cell.toString());
			cell.setCellType(CellType.BOOLEAN);
			cell.setCellValue("" + FormulaError.VALUE.name());
			assertEquals(CellType.STRING, cell.getCellType());
			cell.setCellType(CellType.BOOLEAN);
			assertEquals("FALSE", cell.toString());
			cell.setCellValue(1.2);
			assertEquals("1.2", cell.toString());
			cell.setCellType(CellType.BOOLEAN);
			cell.setCellType(CellType.STRING);
			cell.setCellType(CellType.ERROR);
			cell.setCellType(CellType.STRING);
			cell.setCellValue(1.2);
			cell.setCellType(CellType.STRING);
			assertEquals("1.2", cell.toString());

			cell.setCellValue((String) null);
			cell.setCellValue((RichTextString) null);
		}
    }

	@Test
	void getErrorCellValue_throwsISE_onABlankCell() throws IOException {
		try (HSSFWorkbook wb = new HSSFWorkbook()) {
			Cell cell = wb.createSheet().createRow(0).createCell(0);
			assertThrows(IllegalStateException.class, cell::getErrorCellValue);
		}
	}
}
