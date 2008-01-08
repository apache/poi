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
package org.apache.poi.hssf.extractor;

import java.io.File;
import java.io.FileInputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import org.apache.poi.POITextExtractor;
import org.apache.poi.hssf.HSSFXML;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFXMLWorkbook;
import org.apache.poi.hxf.HXFDocument;

/**
 * Tests for HXFExcelExtractor
 */
public class TestHXFExcelExtractor extends TestCase {
	/**
	 * A very simple file
	 */
	private HSSFXML xmlA;
	/**
	 * A fairly complex file
	 */
	private HSSFXML xmlB;
	
	/**
	 * A fairly simple file - ooxml
	 */
	private HSSFXML simpleXLSX; 
	/**
	 * A fairly simple file - ole2
	 */
	private HSSFWorkbook simpleXLS;

	protected void setUp() throws Exception {
		super.setUp();
		
		File fileA = new File(
				System.getProperty("HSSF.testdata.path") +
				File.separator + "sample.xlsx"
		);
		File fileB = new File(
				System.getProperty("HSSF.testdata.path") +
				File.separator + "AverageTaxRates.xlsx"
		);
		
		File fileSOOXML = new File(
				System.getProperty("HSSF.testdata.path") +
				File.separator + "SampleSS.xlsx"
		);
		File fileSOLE2 = new File(
				System.getProperty("HSSF.testdata.path") +
				File.separator + "SampleSS.xls"
		);
		
		xmlA = new HSSFXML(HXFDocument.openPackage(fileA));
		xmlB = new HSSFXML(HXFDocument.openPackage(fileB));
		
		simpleXLSX = new HSSFXML(HXFDocument.openPackage(fileSOOXML));
		simpleXLS = new HSSFWorkbook(new FileInputStream(fileSOLE2));
	}

	/**
	 * Get text out of the simple file
	 */
	public void testGetSimpleText() throws Exception {
		new HXFExcelExtractor(xmlA.getPackage());
		new HXFExcelExtractor(new HSSFXMLWorkbook(xmlA));
		
		HXFExcelExtractor extractor = 
			new HXFExcelExtractor(xmlA.getPackage());
		extractor.getText();
		
		String text = extractor.getText();
		assertTrue(text.length() > 0);
		
		// Check sheet names
		assertTrue(text.startsWith("Sheet1"));
		assertTrue(text.endsWith("Sheet3\n"));
		
		// Now without, will have text
		extractor.setIncludeSheetNames(false);
		text = extractor.getText();
		assertEquals(
				"0\t111\n" +
				"1\t222\n" +
				"2\t333\n" +
				"3\t444\n" +
				"4\t555\n" +
				"5\t666\n" +
				"6\t777\n" +
				"7\t888\n" +
				"8\t999\n" +
				"9\t4995\n" +
				"\n\n", text);
		
		// Now get formulas not their values
		extractor.setFormulasNotResults(true);
		text = extractor.getText();
		assertEquals(
				"0\t111\n" +
				"1\t222\n" +
				"2\t333\n" +
				"3\t444\n" +
				"4\t555\n" +
				"5\t666\n" +
				"6\t777\n" +
				"7\t888\n" +
				"8\t999\n" +
				"9\tSUM(B1:B9)\n" +
				"\n\n", text);
		
		// With sheet names too
		extractor.setIncludeSheetNames(true);
		text = extractor.getText();
		assertEquals(
				"Sheet1\n" +
				"0\t111\n" +
				"1\t222\n" +
				"2\t333\n" +
				"3\t444\n" +
				"4\t555\n" +
				"5\t666\n" +
				"6\t777\n" +
				"7\t888\n" +
				"8\t999\n" +
				"9\tSUM(B1:B9)\n\n" +
				"Sheet2\n\n" +
				"Sheet3\n"
				, text);
	}
	
	public void testGetComplexText() throws Exception {
		new HXFExcelExtractor(xmlB.getPackage());
		new HXFExcelExtractor(new HSSFXMLWorkbook(xmlB));
		
		HXFExcelExtractor extractor = 
			new HXFExcelExtractor(xmlB.getPackage());
		extractor.getText();
		
		String text = extractor.getText();
		assertTrue(text.length() > 0);
		
		// Might not have all formatting it should do!
		assertTrue(text.startsWith(
						"Avgtxfull\n" +
						"3\t13\t3\t2\t2\t3\t2\t"	
		));
	}
	
	/**
	 * Test that we return pretty much the same as
	 *  ExcelExtractor does, when we're both passed
	 *  the same file, just saved as xls and xlsx
	 */
	public void testComparedToOLE2() throws Exception {
		HXFExcelExtractor ooxmlExtractor =
			new HXFExcelExtractor(simpleXLSX.getPackage());
		ExcelExtractor ole2Extractor =
			new ExcelExtractor(simpleXLS);
		
		POITextExtractor[] extractors =
			new POITextExtractor[] { ooxmlExtractor, ole2Extractor };
		for (int i = 0; i < extractors.length; i++) {
			POITextExtractor extractor = extractors[i];
			
			String text = extractor.getText().replaceAll("[\r\t]", "");
			System.out.println(text.length());
			System.out.println(text);
			assertTrue(text.startsWith("First Sheet\nTest spreadsheet\n2nd row2nd row 2nd column\n"));
			Pattern pattern = Pattern.compile(".*13(\\.0+)?\\s+Sheet3.*", Pattern.DOTALL);
			Matcher m = pattern.matcher(text);
			assertTrue(m.matches());			
		}
	}
}
