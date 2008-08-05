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
package org.apache.poi.xssf.extractor;

import java.io.File;
import java.io.FileInputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import org.apache.poi.POITextExtractor;
import org.apache.poi.hssf.extractor.ExcelExtractor;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Tests for XSSFExcelExtractor
 */
public class TestXSSFExcelExtractor extends TestCase {
	/**
	 * A very simple file
	 */
	private File xmlA;
	/**
	 * A fairly complex file
	 */
	private File xmlB;
	
	/**
	 * A fairly simple file - ooxml
	 */
	private File simpleXLSX; 
	/**
	 * A fairly simple file - ole2
	 */
	private File simpleXLS;

	protected void setUp() throws Exception {
		super.setUp();
		
		xmlA = new File(
				System.getProperty("HSSF.testdata.path") +
				File.separator + "sample.xlsx"
		);
		assertTrue(xmlA.exists());
		xmlB = new File(
				System.getProperty("HSSF.testdata.path") +
				File.separator + "AverageTaxRates.xlsx"
		);
		assertTrue(xmlB.exists());
		
		simpleXLSX = new File(
				System.getProperty("HSSF.testdata.path") +
				File.separator + "SampleSS.xlsx"
		);
		simpleXLS = new File(
				System.getProperty("HSSF.testdata.path") +
				File.separator + "SampleSS.xls"
		);
		assertTrue(simpleXLS.exists());
		assertTrue(simpleXLSX.exists());
	}

	/**
	 * Get text out of the simple file
	 */
	public void testGetSimpleText() throws Exception {
		new XSSFExcelExtractor(xmlA.toString());
		new XSSFExcelExtractor(new XSSFWorkbook(xmlA.toString()));
		
		XSSFExcelExtractor extractor = 
			new XSSFExcelExtractor(xmlA.toString());
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
				"Lorem\t111\n" +
				"ipsum\t222\n" +
				"dolor\t333\n" +
				"sit\t444\n" +
				"amet\t555\n" +
				"consectetuer\t666\n" +
				"adipiscing\t777\n" +
				"elit\t888\n" +
				"Nunc\t999\n" +
				"at\t4995\n", text);
		
		// Now get formulas not their values
		extractor.setFormulasNotResults(true);
		text = extractor.getText();
		assertEquals(
				"Lorem\t111\n" +
				"ipsum\t222\n" +
				"dolor\t333\n" +
				"sit\t444\n" +
				"amet\t555\n" +
				"consectetuer\t666\n" +
				"adipiscing\t777\n" +
				"elit\t888\n" +
				"Nunc\t999\n" +
				"at\tSUM(B1:B9)\n", text);
		
		// With sheet names too
		extractor.setIncludeSheetNames(true);
		text = extractor.getText();
		assertEquals(
				"Sheet1\n" +
				"Lorem\t111\n" +
				"ipsum\t222\n" +
				"dolor\t333\n" +
				"sit\t444\n" +
				"amet\t555\n" +
				"consectetuer\t666\n" +
				"adipiscing\t777\n" +
				"elit\t888\n" +
				"Nunc\t999\n" +
				"at\tSUM(B1:B9)\n" +
				"Sheet2\n" +
				"Sheet3\n"
				, text);
	}
	
	public void testGetComplexText() throws Exception {
		new XSSFExcelExtractor(xmlB.toString());
		
		XSSFExcelExtractor extractor = 
			new XSSFExcelExtractor(new XSSFWorkbook(xmlB.toString()));
		extractor.getText();
		
		String text = extractor.getText();
		assertTrue(text.length() > 0);
		
		// Might not have all formatting it should do!
		// TODO decide if we should really have the "null" in there
		assertTrue(text.startsWith(
						"Avgtxfull\n" +
						"null\t(iii) AVERAGE TAX RATES ON ANNUAL"	
		));
	}
	
	/**
	 * Test that we return pretty much the same as
	 *  ExcelExtractor does, when we're both passed
	 *  the same file, just saved as xls and xlsx
	 */
	public void testComparedToOLE2() throws Exception {
		XSSFExcelExtractor ooxmlExtractor =
			new XSSFExcelExtractor(simpleXLSX.toString());
		ExcelExtractor ole2Extractor =
			new ExcelExtractor(new HSSFWorkbook(
					new FileInputStream(simpleXLS)));
		
		POITextExtractor[] extractors =
			new POITextExtractor[] { ooxmlExtractor, ole2Extractor };
		for (int i = 0; i < extractors.length; i++) {
			POITextExtractor extractor = extractors[i];
			
			String text = extractor.getText().replaceAll("[\r\t]", "");
			//System.out.println(text.length());
			//System.out.println(text);
			assertTrue(text.startsWith("First Sheet\nTest spreadsheet\n2nd row2nd row 2nd column\n"));
			Pattern pattern = Pattern.compile(".*13(\\.0+)?\\s+Sheet3.*", Pattern.DOTALL);
			Matcher m = pattern.matcher(text);
			assertTrue(m.matches());			
		}
	}
	
	/**
	 * From bug #45540
	 */
	public void testHeaderFooter() throws Exception {
		String[] files = new String[] {
			"45540_classic_Header.xlsx", "45540_form_Header.xlsx",
			"45540_classic_Footer.xlsx", "45540_form_Footer.xlsx",
		};
		for(String file : files) {
			File xml = new File(
					System.getProperty("HSSF.testdata.path") +
					File.separator + file
			);
			assertTrue(xml.exists());
			
			XSSFExcelExtractor extractor = 
				new XSSFExcelExtractor(new XSSFWorkbook(xml.toString()));
			String text = extractor.getText();
			
			assertTrue("Unable to find expected word in text from " + file + "\n" + text, text.contains("testdoc"));
	        assertTrue("Unable to find expected word in text\n" + text, text.contains("test phrase")); 
		}
	}

	/**
	 * From bug #45544
	 */
	public void testComments() throws Exception {
		File xml = new File(
				System.getProperty("HSSF.testdata.path") +
				File.separator + "45544.xlsx"
		);
		assertTrue(xml.exists());
		
		XSSFExcelExtractor extractor = 
			new XSSFExcelExtractor(new XSSFWorkbook(xml.toString()));
		String text = extractor.getText();

		// No comments there yet
		assertFalse("Unable to find expected word in text\n" + text, text.contains("testdoc"));
		assertFalse("Unable to find expected word in text\n" + text, text.contains("test phrase"));

		// Turn on comment extraction, will then be
		extractor.setIncludeCellComments(true);
		text = extractor.getText();
		assertTrue("Unable to find expected word in text\n" + text, text.contains("testdoc"));
		assertTrue("Unable to find expected word in text\n" + text, text.contains("test phrase"));
	}
}
