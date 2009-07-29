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

package org.apache.poi.ss.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.util.NumberComparisonExamples.ComparisonExample;
import org.apache.poi.util.HexDump;

/**
 * Creates a spreadsheet that checks Excel's comparison of various IEEE double values.
 * The class {@link NumberComparisonExamples} contains specific comparison examples
 * (along with their expected results) that get encoded into rows of the spreadsheet.
 * Each example is checked with a formula (in column I) that displays either "OK" or
 * "ERROR" depending on whether actual results match those expected.
 *
 * @author Josh Micich
 */
public class NumberComparingSpreadsheetGenerator {

	private static final class SheetWriter {

		private final HSSFSheet _sheet;
		private int _rowIndex;

		public SheetWriter(HSSFWorkbook wb) {
			HSSFSheet sheet = wb.createSheet("Sheet1");

			writeHeaderRow(wb, sheet);
			_sheet = sheet;
			_rowIndex = 1;
		}

		public void addTestRow(double a, double b, int expResult) {
			writeDataRow(_sheet, _rowIndex++, a, b, expResult);
		}
	}

	private static void writeHeaderCell(HSSFRow row, int i, String text, HSSFCellStyle style) {
		HSSFCell cell = row.createCell(i);
		cell.setCellValue(new HSSFRichTextString(text));
		cell.setCellStyle(style);
	}
	static void writeHeaderRow(HSSFWorkbook wb, HSSFSheet sheet) {
		sheet.setColumnWidth(0, 6000);
		sheet.setColumnWidth(1, 6000);
		sheet.setColumnWidth(2, 3600);
		sheet.setColumnWidth(3, 3600);
		sheet.setColumnWidth(4, 2400);
		sheet.setColumnWidth(5, 2400);
		sheet.setColumnWidth(6, 2400);
		sheet.setColumnWidth(7, 2400);
		sheet.setColumnWidth(8, 2400);
		HSSFRow row = sheet.createRow(0);
		HSSFCellStyle style = wb.createCellStyle();
		HSSFFont font = wb.createFont();
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		style.setFont(font);
		writeHeaderCell(row, 0, "Raw Long Bits A", style);
		writeHeaderCell(row, 1, "Raw Long Bits B", style);
		writeHeaderCell(row, 2, "Value A", style);
		writeHeaderCell(row, 3, "Value B", style);
		writeHeaderCell(row, 4, "Exp Cmp", style);
		writeHeaderCell(row, 5, "LT", style);
		writeHeaderCell(row, 6, "EQ", style);
		writeHeaderCell(row, 7, "GT", style);
		writeHeaderCell(row, 8, "Check", style);
	}
	/**
	 * Fills a spreadsheet row with one comparison example. The two numeric values are written to
	 * columns C and D. Columns (F, G and H) respectively get formulas ("v0<v1", "v0=v1", "v0>v1"),
	 * which will be evaluated by Excel. Column D gets the expected comparison result. Column I
	 * gets a formula to check that Excel's comparison results match that predicted in column D.
	 *
	 * @param v0 the first value to be compared
	 * @param v1 the second value to be compared
	 * @param expRes expected comparison result (-1, 0, or +1)
	 */
	static void writeDataRow(HSSFSheet sheet, int rowIx, double v0, double v1, int expRes) {
		HSSFRow row = sheet.createRow(rowIx);

		int rowNum = rowIx + 1;


		row.createCell(0).setCellValue(formatDoubleAsHex(v0));
		row.createCell(1).setCellValue(formatDoubleAsHex(v1));
		row.createCell(2).setCellValue(v0);
		row.createCell(3).setCellValue(v1);
		row.createCell(4).setCellValue(expRes < 0 ? "LT" : expRes > 0 ? "GT" : "EQ");
		row.createCell(5).setCellFormula("C" + rowNum + "<" + "D" + rowNum);
		row.createCell(6).setCellFormula("C" + rowNum + "=" + "D" + rowNum);
		row.createCell(7).setCellFormula("C" + rowNum + ">" + "D" + rowNum);
		// TODO - bug elsewhere in POI - something wrong with encoding of NOT() function
		String frm = "if(or(" +
			"and(E#='LT', F#      , G#=FALSE, H#=FALSE)," +
			"and(E#='EQ', F#=FALSE, G#      , H#=FALSE)," +
			"and(E#='GT', F#=FALSE, G#=FALSE, H#      )" +
			"), 'OK', 'error')"	;
		row.createCell(8).setCellFormula(frm.replaceAll("#", String.valueOf(rowNum)).replace('\'', '"'));
	}

	private static String formatDoubleAsHex(double d) {
		long l = Double.doubleToLongBits(d);
		StringBuilder sb = new StringBuilder(20);
		sb.append(HexDump.longToHex(l)).append('L');
		return sb.toString();
	}

	public static void main(String[] args) {

		HSSFWorkbook wb = new HSSFWorkbook();
		SheetWriter sw = new SheetWriter(wb);
		ComparisonExample[] ces = NumberComparisonExamples.getComparisonExamples();
		for (int i = 0; i < ces.length; i++) {
			ComparisonExample ce = ces[i];
			sw.addTestRow(ce.getA(), ce.getB(), ce.getExpectedResult());
		}


		File outputFile = new File("ExcelNumberCompare.xls");

		try {
			FileOutputStream os = new FileOutputStream(outputFile);
			wb.write(os);
			os.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		System.out.println("Finished writing '" + outputFile.getAbsolutePath() + "'");
	}
}
