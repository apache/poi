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

import java.text.DecimalFormat;
import java.text.Format;
import java.util.Iterator;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.ss.usermodel.Cell;

import junit.framework.TestCase;

/**
 * Unit tests for HSSFDataFormatter.java
 *
 * @author James May (james dot may at fmr dot com)
 *
 */
public final class TestHSSFDataFormatter extends TestCase {

	private final HSSFDataFormatter formatter;
	private final HSSFWorkbook wb;

	public TestHSSFDataFormatter() {
		// create the formatter to test
		formatter = new HSSFDataFormatter();

		// create a workbook to test with
		wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet();
		HSSFDataFormat format = wb.createDataFormat();

		// create a row and put some cells in it
		HSSFRow row = sheet.createRow(0);

		// date value for July 8 1901 1:19 PM
		double dateNum = 555.555;

		//valid date formats -- all should have "Jul" in output
		String[] goodDatePatterns ={
			"[$-F800]dddd\\,\\ mmmm\\ dd\\,\\ yyyy",
			"mmm/d/yy\\ h:mm PM;@",
			"mmmm/d/yy\\ h:mm;@",
			"mmmm/d;@",
			"mmmm/d/yy;@",
			"mmm/dd/yy;@",
			"[$-409]d\\-mmm;@",
			"[$-409]d\\-mmm\\-yy;@",
			"[$-409]dd\\-mmm\\-yy;@",
			"[$-409]mmm\\-yy;@",
			"[$-409]mmmm\\-yy;@",
			"[$-409]mmmm\\ d\\,\\ yyyy;@",
			"[$-409]mmm/d/yy\\ h:mm:ss;@",
			"[$-409]mmmm/d/yy\\ h:mm:ss am;@",
			"[$-409]mmmmm;@",
			"[$-409]mmmmm\\-yy;@",
			"mmmm/d/yyyy;@",
			"[$-409]d\\-mmm\\-yyyy;@"
		};

		// valid number formats
		String[] goodNumPatterns = {
				"#,##0.0000",
				"#,##0;[Red]#,##0",
				"(#,##0.00_);(#,##0.00)",
				"($#,##0.00_);[Red]($#,##0.00)",
				"$#,##0.00",
				"[$-809]#,##0.00", // international format
				"[$-2]#,##0.00", // international format
				"0000.00000%",
				"0.000E+00",
				"0.00E+00",
		};

		// invalid date formats -- will throw exception in DecimalFormat ctor
		String[] badNumPatterns = {
				"#,#$'#0.0000",
				"'#','#ABC#0;##,##0",
				"000 '123 4'5'6 000",
				"#''0#0'1#10L16EE"
		};

		// create cells with good date patterns
		for (int i = 0; i < goodDatePatterns.length; i++) {
			HSSFCell cell = row.createCell(i);
			cell.setCellValue(dateNum);
			HSSFCellStyle cellStyle = wb.createCellStyle();
			cellStyle.setDataFormat(format.getFormat(goodDatePatterns[i]));
			cell.setCellStyle(cellStyle);
		}
		row = sheet.createRow(1);

		// create cells with num patterns
		for (int i = 0; i < goodNumPatterns.length; i++) {
			HSSFCell cell = row.createCell(i);
			cell.setCellValue(-1234567890.12345);
			HSSFCellStyle cellStyle = wb.createCellStyle();
			cellStyle.setDataFormat(format.getFormat(goodNumPatterns[i]));
			cell.setCellStyle(cellStyle);
		}
		row = sheet.createRow(2);

		// create cells with bad num patterns
		for (int i = 0; i < badNumPatterns.length; i++) {
			HSSFCell cell = row.createCell(i);
			cell.setCellValue(1234567890.12345);
			HSSFCellStyle cellStyle = wb.createCellStyle();
			cellStyle.setDataFormat(format.getFormat(badNumPatterns[i]));
			cell.setCellStyle(cellStyle);
		}

		// Built in formats

		{ // Zip + 4 format
			row = sheet.createRow(3);
			HSSFCell cell = row.createCell(0);
			cell.setCellValue(123456789);
			HSSFCellStyle cellStyle = wb.createCellStyle();
			cellStyle.setDataFormat(format.getFormat("00000-0000"));
			cell.setCellStyle(cellStyle);
		}

		{ // Phone number format
			row = sheet.createRow(4);
			HSSFCell cell = row.createCell(0);
			cell.setCellValue(5551234567D);
			HSSFCellStyle cellStyle = wb.createCellStyle();
			cellStyle.setDataFormat(format.getFormat("[<=9999999]###-####;(###) ###-####"));
			cell.setCellStyle(cellStyle);
		}

		{ // SSN format
			row = sheet.createRow(5);
			HSSFCell cell = row.createCell(0);
			cell.setCellValue(444551234);
			HSSFCellStyle cellStyle = wb.createCellStyle();
			cellStyle.setDataFormat(format.getFormat("000-00-0000"));
			cell.setCellStyle(cellStyle);
		}

		{ // formula cell
			row = sheet.createRow(6);
			HSSFCell cell = row.createCell(0);
			cell.setCellType(HSSFCell.CELL_TYPE_FORMULA);
			cell.setCellFormula("SUM(12.25,12.25)/100");
			HSSFCellStyle cellStyle = wb.createCellStyle();
			cellStyle.setDataFormat(format.getFormat("##.00%;"));
			cell.setCellStyle(cellStyle);
		}
	}

	/**
	 * Test getting formatted values from numeric and date cells.
	 */
	public void testGetFormattedCellValueHSSFCell() {
		// Valid date formats -- cell values should be date formatted & not "555.555"
		HSSFRow row = wb.getSheetAt(0).getRow(0);
		Iterator<Cell> it = row.cellIterator();
		log("==== VALID DATE FORMATS ====");
		while (it.hasNext()) {
			Cell cell = it.next();
			log(formatter.formatCellValue(cell));

			// should not be equal to "555.555"
			assertTrue( ! "555.555".equals(formatter.formatCellValue(cell)));

			// should contain "Jul" in the String
			assertTrue( formatter.formatCellValue(cell).indexOf("Jul") > -1);
		}

		// test number formats
		row = wb.getSheetAt(0).getRow(1);
		it = row.cellIterator();
		log("\n==== VALID NUMBER FORMATS ====");
		while (it.hasNext()) {
			HSSFCell cell = (HSSFCell) it.next();
			log(formatter.formatCellValue(cell));

			// should not be equal to "1234567890.12345"
			assertTrue( ! "1234567890.12345".equals(formatter.formatCellValue(cell)));
		}

		// test bad number formats
		row = wb.getSheetAt(0).getRow(2);
		it = row.cellIterator();
		log("\n==== INVALID NUMBER FORMATS ====");
		while (it.hasNext()) {
			HSSFCell cell = (HSSFCell) it.next();
			log(formatter.formatCellValue(cell));
			// should be equal to "1234567890.12345"
			assertEquals("1234567890.12345", formatter.formatCellValue(cell));
		}

		// test Zip+4 format
		row = wb.getSheetAt(0).getRow(3);
		HSSFCell cell = row.getCell(0);
		log("\n==== ZIP FORMAT ====");
		log(formatter.formatCellValue(cell));
		assertEquals("12345-6789", formatter.formatCellValue(cell));

		// test phone number format
		row = wb.getSheetAt(0).getRow(4);
		cell = row.getCell(0);
		log("\n==== PHONE FORMAT ====");
		log(formatter.formatCellValue(cell));
		assertEquals("(555) 123-4567", formatter.formatCellValue(cell));

		// test SSN format
		row = wb.getSheetAt(0).getRow(5);
		cell = row.getCell(0);
		log("\n==== SSN FORMAT ====");
		log(formatter.formatCellValue(cell));
		assertEquals("444-55-1234", formatter.formatCellValue(cell));

		// null test-- null cell should result in empty String
		assertEquals(formatter.formatCellValue(null), "");

		// null test-- null cell should result in empty String
		assertEquals(formatter.formatCellValue(null), "");
	}

	public void testGetFormattedCellValueHSSFCellHSSFFormulaEvaluator() {
		// test formula format
		HSSFRow row = wb.getSheetAt(0).getRow(6);
		HSSFCell cell = row.getCell(0);
		log("\n==== FORMULA CELL ====");

		// first without a formula evaluator
		log(formatter.formatCellValue(cell) + "\t (without evaluator)");
		assertEquals("SUM(12.25,12.25)/100", formatter.formatCellValue(cell));

		// now with a formula evaluator
		HSSFFormulaEvaluator evaluator = new HSSFFormulaEvaluator(wb);
		log(formatter.formatCellValue(cell, evaluator) + "\t\t\t (with evaluator)");
		assertEquals("24.50%", formatter.formatCellValue(cell,evaluator));
	}

	/**
	 * Test using a default number format. The format should be used when a
	 * format pattern cannot be parsed by DecimalFormat.
	 */
	public void testSetDefaultNumberFormat() {
		HSSFRow row = wb.getSheetAt(0).getRow(2);
		Iterator<Cell> it = row.cellIterator();
		Format defaultFormat = new DecimalFormat("Balance $#,#00.00 USD;Balance -$#,#00.00 USD");
		formatter.setDefaultNumberFormat(defaultFormat);

		log("\n==== DEFAULT NUMBER FORMAT ====");
		while (it.hasNext()) {
			Cell cell = it.next();
			cell.setCellValue(cell.getNumericCellValue() * Math.random() / 1000000 - 1000);
			log(formatter.formatCellValue(cell));
			assertTrue(formatter.formatCellValue(cell).startsWith("Balance "));
			assertTrue(formatter.formatCellValue(cell).endsWith(" USD"));
		}
	}
	
	/**
	 * A format of "@" means use the general format
	 */
	public void testGeneralAtFormat() {
		HSSFWorkbook workbook = HSSFTestDataSamples.openSampleWorkbook("47154.xls");
		HSSFSheet sheet = workbook.getSheetAt(0);
		HSSFRow row = sheet.getRow(0);
		HSSFCell cellA1 = row.getCell(0);

		assertEquals(HSSFCell.CELL_TYPE_NUMERIC, cellA1.getCellType());
		assertEquals(2345.0, cellA1.getNumericCellValue(), 0.0001);
		assertEquals("@", cellA1.getCellStyle().getDataFormatString());

		HSSFDataFormatter f = new HSSFDataFormatter();

		assertEquals("2345", f.formatCellValue(cellA1));
	}

	private static void log(String msg) {
		if (false) { // successful tests should be silent
			System.out.println(msg);
		}
	}
}
