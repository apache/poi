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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.util.NumberToTextConversionExamples.ExampleConversion;
import org.apache.poi.util.HexDump;
import org.apache.poi.util.HexRead;

/**
 * Creates a spreadsheet that demonstrates Excel's rendering of various IEEE double values.
 * 
 * @author Josh Micich
 */
public class NumberRenderingSpreadsheetGenerator {

	private static final class SheetWriter {

		private final HSSFSheet _sheet;
		private int _rowIndex;
		private final List<Long> _replacementNaNs;

		public SheetWriter(HSSFWorkbook wb) {
			HSSFSheet sheet = wb.createSheet("Sheet1");

			writeHeaderRow(wb, sheet);
			_sheet = sheet;
			_rowIndex = 1;
			_replacementNaNs = new ArrayList<Long>();
		}

		public void addTestRow(long rawBits, String expectedExcelRendering) {
			writeDataRow(_sheet, _rowIndex++, rawBits, expectedExcelRendering);
			if(Double.isNaN(Double.longBitsToDouble(rawBits))) {
				_replacementNaNs.add(new Long(rawBits));
			}
		}

		public long[] getReplacementNaNs() {
			int nRepls = _replacementNaNs.size();
			long[] result = new long[nRepls];
			for (int i = 0; i < nRepls; i++) {
				result[i] = _replacementNaNs.get(i).longValue();
			}
			return result;
		}

	}
	/** 0x7ff8000000000000 encoded in little endian order */
	private static final byte[] JAVA_NAN_BYTES = HexRead.readFromString("00 00 00 00 00 00 F8 7F");
	
	private static void writeHeaderCell(HSSFRow row, int i, String text, HSSFCellStyle style) {
		HSSFCell cell = row.createCell(i);
		cell.setCellValue(new HSSFRichTextString(text));
		cell.setCellStyle(style);
	}
	static void writeHeaderRow(HSSFWorkbook wb, HSSFSheet sheet) {
		sheet.setColumnWidth(0, 3000);
		sheet.setColumnWidth(1, 6000);
		sheet.setColumnWidth(2, 6000);
		sheet.setColumnWidth(3, 6000);
		sheet.setColumnWidth(4, 6000);
		sheet.setColumnWidth(5, 1600);
		sheet.setColumnWidth(6, 20000);
		HSSFRow row = sheet.createRow(0);
		HSSFCellStyle style = wb.createCellStyle();
		HSSFFont font = wb.createFont();
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		style.setFont(font);
		writeHeaderCell(row, 0, "Value", style);
		writeHeaderCell(row, 1, "Raw Long Bits", style);
		writeHeaderCell(row, 2, "JDK Double Rendering", style);
		writeHeaderCell(row, 3, "Actual Rendering", style);
		writeHeaderCell(row, 4, "Expected Rendering", style);
		writeHeaderCell(row, 5, "Match", style);  
		writeHeaderCell(row, 6, "Java Metadata", style);
	}
	static void writeDataRow(HSSFSheet sheet, int rowIx, long rawLongBits, String expectedExcelRendering) {
		double d = Double.longBitsToDouble(rawLongBits);
		HSSFRow row = sheet.createRow(rowIx);
		
		int rowNum = rowIx + 1;
		String cel0ref = "A" + rowNum;
		String rawBitsText = formatLongAsHex(rawLongBits);
		String jmExpr = "'ec(" + rawBitsText + ", ''\" & C" + rowNum + " & \"'', ''\" & D" + rowNum + " & \"''),'";
		
		// The 'Match' column will contain 'OK' if the metadata (from NumberToTextConversionExamples)
		// matches Excel's rendering.
		String matchExpr = "if(D" + rowNum + "=E" + rowNum + ", \"OK\", \"ERROR\")";
		
		row.createCell(0).setCellValue(d);
		row.createCell(1).setCellValue(new HSSFRichTextString(rawBitsText));
		row.createCell(2).setCellValue(new HSSFRichTextString(Double.toString(d)));
		row.createCell(3).setCellFormula("\"\" & " + cel0ref);
		row.createCell(4).setCellValue(new HSSFRichTextString(expectedExcelRendering));
		row.createCell(5).setCellFormula(matchExpr);
		row.createCell(6).setCellFormula(jmExpr.replaceAll("'", "\""));

		if (false) {
			// for observing arithmetic near numeric range boundaries
			row.createCell(7).setCellFormula(cel0ref + " * 1.0001");
			row.createCell(8).setCellFormula(cel0ref + " / 1.0001");
		}
	}
	
	private static String formatLongAsHex(long l) {
		StringBuilder sb = new StringBuilder(20);
		sb.append(HexDump.longToHex(l)).append('L');
		return sb.toString();
	}

	public static void main(String[] args) {
		writeJavaDoc();
		
		HSSFWorkbook wb = new HSSFWorkbook();
		SheetWriter sw = new SheetWriter(wb);
		
		ExampleConversion[] exampleValues = NumberToTextConversionExamples.getExampleConversions();
		for (int i = 0; i < exampleValues.length; i++) {
			ExampleConversion example = exampleValues[i];
			sw.addTestRow(example.getRawDoubleBits(), example.getExcelRendering());
		}
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			wb.write(baos);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		byte[] fileContent = baos.toByteArray();
		replaceNaNs(fileContent, sw.getReplacementNaNs());
		
		
		File outputFile = new File("ExcelNumberRendering.xls");
		
		try {
			FileOutputStream os = new FileOutputStream(outputFile);
			os.write(fileContent);
			os.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		System.out.println("Finished writing '" + outputFile.getAbsolutePath() + "'");
	}

	public static void writeJavaDoc() {
		
		ExampleConversion[] exampleConversions = NumberToTextConversionExamples.getExampleConversions();
		for (int i = 0; i < exampleConversions.length; i++) {
			ExampleConversion ec = exampleConversions[i];
			String line = 	" * <tr><td>" 
				+ formatLongAsHex(ec.getRawDoubleBits())
				+ "</td><td>" + Double.toString(ec.getDoubleValue()) 
				+ "</td><td>" + ec.getExcelRendering() + "</td></tr>";
			
			System.out.println(line);
		}
	}

	
	
	private static void replaceNaNs(byte[] fileContent, long[] replacementNaNs) {
		int countFound = 0;
		for(int i=0; i<fileContent.length; i++) {
			if(isNaNBytes(fileContent, i)) {
				writeLong(fileContent, i, replacementNaNs[countFound]);
				countFound++;
			}
		}
		if (countFound < replacementNaNs.length) {
			throw new RuntimeException("wrong repl count");
		}
		
	}

	private static void writeLong(byte[] bb, int i, long val) {
		String oldVal = interpretLong(bb, i);
		bb[i+7] = (byte) (val >> 56);
		bb[i+6] = (byte) (val >> 48);
		bb[i+5] = (byte) (val >> 40);
		bb[i+4] = (byte) (val >> 32);
		bb[i+3] = (byte) (val >> 24);
		bb[i+2] = (byte) (val >> 16);
		bb[i+1] = (byte) (val >>  8);
		bb[i+0] = (byte) (val >>  0);
		if (false) {
			String newVal = interpretLong(bb, i);
			System.out.println("changed offset " + i + " from " + oldVal + " to " + newVal);
		}
		
	}

	private static String interpretLong(byte[] fileContent, int offset) {
		InputStream is = new ByteArrayInputStream(fileContent, offset, 8);
		long l;
		try {
			l = new DataInputStream(is).readLong();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return "0x" + Long.toHexString(l).toUpperCase();
	}

	private static boolean isNaNBytes(byte[] fileContent, int offset) {
		if(offset + JAVA_NAN_BYTES.length > fileContent.length) {
			return false;
		}
		// excel NaN bits: 0xFFFF0420003C0000L
		// java NaN bits :0x7ff8000000000000L
		return areArraySectionsEqual(fileContent, offset, JAVA_NAN_BYTES);
	}
	private static boolean areArraySectionsEqual(byte[] bb, int off, byte[] section) {
		for (int i=section.length-1; i>=0; i--) {
			if (bb[off+i] != section[i]){
				return false;
			}
		}
		return true;
	}
}
