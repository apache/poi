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

package org.apache.poi.hssf.usermodel.examples;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;

/**
 * File for HSSF testing/examples
 *
 * THIS IS NOT THE MAIN HSSF FILE!! This is a utility for testing functionality.
 * It does contain sample API usage that may be educational to regular API
 * users.
 *
 * @see #main
 * @author Andrew Oliver (acoliver at apache dot org)
 */
public final class HSSFReadWrite {

	/**
	 * creates an {@link HSSFWorkbook} the specified OS filename.
	 */
	private static HSSFWorkbook readFile(String filename) throws IOException {
		return new HSSFWorkbook(new FileInputStream(filename));
	}

	/**
	 * given a filename this outputs a sample sheet with just a set of
	 * rows/cells.
	 */
	private static void testCreateSampleSheet(String outputFilename) throws IOException {
		int rownum;
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet s = wb.createSheet();
		HSSFCellStyle cs = wb.createCellStyle();
		HSSFCellStyle cs2 = wb.createCellStyle();
		HSSFCellStyle cs3 = wb.createCellStyle();
		HSSFFont f = wb.createFont();
		HSSFFont f2 = wb.createFont();

		f.setFontHeightInPoints((short) 12);
		f.setColor((short) 0xA);
		f.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		f2.setFontHeightInPoints((short) 10);
		f2.setColor((short) 0xf);
		f2.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		cs.setFont(f);
		cs.setDataFormat(HSSFDataFormat.getBuiltinFormat("($#,##0_);[Red]($#,##0)"));
		cs2.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		cs2.setFillPattern((short) 1); // fill w fg
		cs2.setFillForegroundColor((short) 0xA);
		cs2.setFont(f2);
		wb.setSheetName(0, "HSSF Test");
		for (rownum = 0; rownum < 300; rownum++) {
			HSSFRow r = s.createRow(rownum);
			if ((rownum % 2) == 0) {
				r.setHeight((short) 0x249);
			}

			for (int cellnum = 0; cellnum < 50; cellnum += 2) {
				HSSFCell c = r.createCell(cellnum);
				c.setCellValue(rownum * 10000 + cellnum
						+ (((double) rownum / 1000) + ((double) cellnum / 10000)));
				if ((rownum % 2) == 0) {
					c.setCellStyle(cs);
				}
				c = r.createCell(cellnum + 1);
				c.setCellValue(new HSSFRichTextString("TEST"));
				// 50 characters divided by 1/20th of a point
				s.setColumnWidth(cellnum + 1, (int) (50 * 8 / 0.05));
				if ((rownum % 2) == 0) {
					c.setCellStyle(cs2);
				}
			}
		}

		// draw a thick black border on the row at the bottom using BLANKS
		rownum++;
		rownum++;
		HSSFRow r = s.createRow(rownum);
		cs3.setBorderBottom(HSSFCellStyle.BORDER_THICK);
		for (int cellnum = 0; cellnum < 50; cellnum++) {
			HSSFCell c = r.createCell(cellnum);
			c.setCellStyle(cs3);
		}
		s.addMergedRegion(new CellRangeAddress(0, 3, 0, 3));
		s.addMergedRegion(new CellRangeAddress(100, 110, 100, 110));

		// end draw thick black border
		// create a sheet, set its title then delete it
		s = wb.createSheet();
		wb.setSheetName(1, "DeletedSheet");
		wb.removeSheetAt(1);

		// end deleted sheet
		FileOutputStream out = new FileOutputStream(outputFilename);
		wb.write(out);
		out.close();
	}

	/**
     * Method main
     *
     * Given 1 argument takes that as the filename, inputs it and dumps the
     * cell values/types out to sys.out.<br/>
     *
     * given 2 arguments where the second argument is the word "write" and the
     * first is the filename - writes out a sample (test) spreadsheet
     * see {@link HSSFReadWrite#testCreateSampleSheet(String)}.<br/>
     *
     * given 2 arguments where the first is an input filename and the second
     * an output filename (not write), attempts to fully read in the
     * spreadsheet and fully write it out.<br/>
     *
     * given 3 arguments where the first is an input filename and the second an
     * output filename (not write) and the third is "modify1", attempts to read in the
     * spreadsheet, deletes rows 0-24, 74-99.  Changes cell at row 39, col 3 to
     * "MODIFIED CELL" then writes it out.  Hence this is "modify test 1".  If you
     * take the output from the write test, you'll have a valid scenario.
     */
	public static void main(String[] args) {
		if (args.length < 1) {
			System.err.println("At least one argument expected");
			return;
		}

		String fileName = args[0];
		try {
			if (args.length < 2) {

				HSSFWorkbook wb = HSSFReadWrite.readFile(fileName);

				System.out.println("Data dump:\n");

				for (int k = 0; k < wb.getNumberOfSheets(); k++) {
					HSSFSheet sheet = wb.getSheetAt(k);
					int rows = sheet.getPhysicalNumberOfRows();
					System.out.println("Sheet " + k + " \"" + wb.getSheetName(k) + "\" has " + rows
							+ " row(s).");
					for (int r = 0; r < rows; r++) {
						HSSFRow row = sheet.getRow(r);
						if (row == null) {
							continue;
						}

						int cells = row.getPhysicalNumberOfCells();
						System.out.println("\nROW " + row.getRowNum() + " has " + cells
								+ " cell(s).");
						for (int c = 0; c < cells; c++) {
							HSSFCell cell = row.getCell(c);
							String value = null;

							switch (cell.getCellType()) {

								case HSSFCell.CELL_TYPE_FORMULA:
									value = "FORMULA value=" + cell.getCellFormula();
									break;

								case HSSFCell.CELL_TYPE_NUMERIC:
									value = "NUMERIC value=" + cell.getNumericCellValue();
									break;

								case HSSFCell.CELL_TYPE_STRING:
									value = "STRING value=" + cell.getStringCellValue();
									break;

								default:
							}
							System.out.println("CELL col=" + cell.getColumnIndex() + " VALUE="
									+ value);
						}
					}
				}
			} else if (args.length == 2) {
				if (args[1].toLowerCase().equals("write")) {
					System.out.println("Write mode");
					long time = System.currentTimeMillis();
					HSSFReadWrite.testCreateSampleSheet(fileName);

					System.out.println("" + (System.currentTimeMillis() - time)
							+ " ms generation time");
				} else {
					System.out.println("readwrite test");
					HSSFWorkbook wb = HSSFReadWrite.readFile(fileName);
					FileOutputStream stream = new FileOutputStream(args[1]);

					wb.write(stream);
					stream.close();
				}
			} else if (args.length == 3 && args[2].toLowerCase().equals("modify1")) {
				// delete row 0-24, row 74 - 99 && change cell 3 on row 39 to string "MODIFIED CELL!!"

				HSSFWorkbook wb = HSSFReadWrite.readFile(fileName);
				FileOutputStream stream = new FileOutputStream(args[1]);
				HSSFSheet sheet = wb.getSheetAt(0);

				for (int k = 0; k < 25; k++) {
					HSSFRow row = sheet.getRow(k);

					sheet.removeRow(row);
				}
				for (int k = 74; k < 100; k++) {
					HSSFRow row = sheet.getRow(k);

					sheet.removeRow(row);
				}
				HSSFRow row = sheet.getRow(39);
				HSSFCell cell = row.getCell(3);
				cell.setCellValue("MODIFIED CELL!!!!!");

				wb.write(stream);
				stream.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
