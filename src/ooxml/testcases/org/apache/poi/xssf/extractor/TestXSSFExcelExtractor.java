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

import static org.apache.poi.POITestCase.assertContains;
import static org.apache.poi.POITestCase.assertEndsWith;
import static org.apache.poi.POITestCase.assertNotContained;
import static org.apache.poi.POITestCase.assertStartsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.extractor.POITextExtractor;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.extractor.ExcelExtractor;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link XSSFExcelExtractor}
 */
class TestXSSFExcelExtractor {
	protected XSSFExcelExtractor getExtractor(String sampleName) {
		return new XSSFExcelExtractor(XSSFTestDataSamples.openSampleWorkbook(sampleName));
	}

	/**
	 * Get text out of the simple file
	 */
	@Test
	void testGetSimpleText() throws IOException {
		// a very simple file
		XSSFExcelExtractor extractor = getExtractor("sample.xlsx");

		String text = extractor.getText();
		assertTrue(text.length() > 0);

		// Check sheet names
		assertStartsWith(text, "Sheet1");
		assertEndsWith(text, "Sheet3\n");

		// Now without, will have text
		extractor.setIncludeSheetNames(false);
		text = extractor.getText();
		String CHUNK1 =
			"Lorem\t111\n" +
    		"ipsum\t222\n" +
    		"dolor\t333\n" +
    		"sit\t444\n" +
    		"amet\t555\n" +
    		"consectetuer\t666\n" +
    		"adipiscing\t777\n" +
    		"elit\t888\n" +
    		"Nunc\t999\n";
		String CHUNK2 =
			"The quick brown fox jumps over the lazy dog\n" +
			"hello, xssf	hello, xssf\n" +
			"hello, xssf	hello, xssf\n" +
			"hello, xssf	hello, xssf\n" +
			"hello, xssf	hello, xssf\n";
		assertEquals(
				CHUNK1 +
				"at\t4995\n" +
				CHUNK2
				, text);

		// Now get formulas not their values
		extractor.setFormulasNotResults(true);
		text = extractor.getText();
		assertEquals(
				CHUNK1 +
				"at\tSUM(B1:B9)\n" +
				CHUNK2, text);

		// With sheet names too
		extractor.setIncludeSheetNames(true);
		text = extractor.getText();
		assertEquals(
				"Sheet1\n" +
				CHUNK1 +
				"at\tSUM(B1:B9)\n" +
				"rich test\n" +
				CHUNK2 +
				"Sheet3\n"
				, text);

		extractor.close();
	}

	@Test
	void testGetComplexText() throws IOException {
		// A fairly complex file
		XSSFExcelExtractor extractor = getExtractor("AverageTaxRates.xlsx");

		String text = extractor.getText();
		assertTrue(text.length() > 0);

		// Might not have all formatting it should do!
		assertStartsWith(text,
						"Avgtxfull\n" +
						"\t(iii) AVERAGE TAX RATES ON ANNUAL"
		);

		extractor.close();
	}

	/**
	 * Test that we return pretty much the same as
	 *  ExcelExtractor does, when we're both passed
	 *  the same file, just saved as xls and xlsx
	 */
	@Test
	void testComparedToOLE2() throws IOException {
		// A fairly simple file - ooxml
		XSSFExcelExtractor ooxmlExtractor = getExtractor("SampleSS.xlsx");

		ExcelExtractor ole2Extractor =
			new ExcelExtractor(HSSFTestDataSamples.openSampleWorkbook("SampleSS.xls"));

		Map<String, POITextExtractor> extractors = new HashMap<>();
		extractors.put("SampleSS.xlsx", ooxmlExtractor);
		extractors.put("SampleSS.xls", ole2Extractor);

		for (final Entry<String, POITextExtractor> e : extractors.entrySet()) {
			String filename = e.getKey();
			POITextExtractor extractor = e.getValue();
			String text = extractor.getText().replaceAll("[\r\t]", "");
			assertStartsWith(filename, text, "First Sheet\nTest spreadsheet\n2nd row2nd row 2nd column\n");
			Pattern pattern = Pattern.compile(".*13(\\.0+)?\\s+Sheet3.*", Pattern.DOTALL);
			Matcher m = pattern.matcher(text);
			assertTrue(m.matches(), filename);
		}

		ole2Extractor.close();
		ooxmlExtractor.close();
	}

	/**
	 * From bug #45540
	 */
	@Test
	void testHeaderFooter() throws IOException {
		String[] files = new String[] {
			"45540_classic_Header.xlsx", "45540_form_Header.xlsx",
			"45540_classic_Footer.xlsx", "45540_form_Footer.xlsx",
		};
		for(String sampleName : files) {
			XSSFExcelExtractor extractor = getExtractor(sampleName);
			String text = extractor.getText();

			assertContains(sampleName, text, "testdoc");
			assertContains(sampleName, text, "test phrase");

			extractor.close();
		}
	}

	/**
	 * From bug #45544
	 */
	@Test
	void testComments() throws IOException {
		XSSFExcelExtractor extractor = getExtractor("45544.xlsx");
		String text = extractor.getText();

		// No comments there yet
		assertNotContained(text, "testdoc");
		assertNotContained(text, "test phrase");

		// Turn on comment extraction, will then be
		extractor.setIncludeCellComments(true);
		text = extractor.getText();
		assertContains(text, "testdoc");
		assertContains(text, "test phrase");

		extractor.close();
	}

	@Test
	void testInlineStrings() throws IOException {
      XSSFExcelExtractor extractor = getExtractor("InlineStrings.xlsx");
      extractor.setFormulasNotResults(true);
      String text = extractor.getText();

      // Numbers
      assertContains(text, "43");
      assertContains(text, "22");

      // Strings
      assertContains(text, "ABCDE");
      assertContains(text, "Long Text");

      // Inline Strings
      assertContains(text, "1st Inline String");
      assertContains(text, "And More");

      // Formulas
      assertContains(text, "A2");
      assertContains(text, "A5-A$2");

      extractor.close();
	}

	/**
	 * Simple test for text box text
	 */
	@Test
	void testTextBoxes() throws IOException {
        try (XSSFExcelExtractor extractor = getExtractor("WithTextBox.xlsx")) {
            extractor.setFormulasNotResults(true);
            String text = extractor.getText();
            assertContains(text, "Line 1");
            assertContains(text, "Line 2");
            assertContains(text, "Line 3");
        }
	}

	@Test
	void testPhoneticRuns() throws Exception {
        try (XSSFExcelExtractor extractor = getExtractor("51519.xlsx")) {
            String text = extractor.getText();
            assertContains(text, "\u8C4A\u7530");
            //this shows up only as a phonetic run and should not appear
            //in the extracted text
            assertNotContained(text, "\u30CB\u30DB\u30F3");
        }

	}
}
