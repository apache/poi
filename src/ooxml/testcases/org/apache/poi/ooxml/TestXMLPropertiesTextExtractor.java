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
package org.apache.poi.ooxml;

import static org.apache.poi.POITestCase.assertContains;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.poi.POIDataSamples;
import org.apache.poi.ooxml.extractor.POIXMLPropertiesTextExtractor;
import org.apache.poi.ooxml.util.PackageHelper;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xslf.usermodel.XSLFSlideShow;
import org.apache.poi.xssf.extractor.XSSFExcelExtractor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

public final class TestXMLPropertiesTextExtractor {
    private static final POIDataSamples _ssSamples = POIDataSamples.getSpreadSheetInstance();
    private static final POIDataSamples _slSamples = POIDataSamples.getSlideShowInstance();

	@Test
	void testGetFromMainExtractor() throws Exception {
		OPCPackage pkg = PackageHelper.open(_ssSamples.openResourceAsStream("ExcelWithAttachments.xlsm"));

		XSSFWorkbook wb = new XSSFWorkbook(pkg);

		XSSFExcelExtractor ext = new XSSFExcelExtractor(wb);
		POIXMLPropertiesTextExtractor textExt = ext.getMetadataTextExtractor();

		// Check basics
		assertNotNull(textExt);
		assertTrue(textExt.getText().length() > 0);

		// Check some of the content
		String text = textExt.getText();
		String cText = textExt.getCorePropertiesText();

		assertContains(text, "LastModifiedBy = Yury Batrakov");
		assertContains(cText, "LastModifiedBy = Yury Batrakov");

		textExt.close();
		ext.close();
	}

	@Test
	void testCore() throws Exception {
		OPCPackage pkg = PackageHelper.open(
                _ssSamples.openResourceAsStream("ExcelWithAttachments.xlsm")
		);
		XSSFWorkbook wb = new XSSFWorkbook(pkg);

		POIXMLPropertiesTextExtractor ext = new POIXMLPropertiesTextExtractor(wb);
		ext.getText();

		// Now check
		String text = ext.getText();
		String cText = ext.getCorePropertiesText();

		assertContains(text, "LastModifiedBy = Yury Batrakov");
		assertContains(cText, "LastModifiedBy = Yury Batrakov");

		ext.close();
	}

	@Test
	void testExtended() throws Exception {
		OPCPackage pkg = OPCPackage.open(
                _ssSamples.openResourceAsStream("ExcelWithAttachments.xlsm")
		);
		XSSFWorkbook wb = new XSSFWorkbook(pkg);

		POIXMLPropertiesTextExtractor ext = new POIXMLPropertiesTextExtractor(wb);
		ext.getText();

		// Now check
		String text = ext.getText();
		String eText = ext.getExtendedPropertiesText();

		assertContains(text, "Application = Microsoft Excel");
		assertContains(text, "Company = Mera");
		assertContains(eText, "Application = Microsoft Excel");
		assertContains(eText, "Company = Mera");

		ext.close();
	}

	@Test
	void testCustom() throws Exception {
      OPCPackage pkg = OPCPackage.open(
                _ssSamples.openResourceAsStream("ExcelWithAttachments.xlsm")
      );
      XSSFWorkbook wb = new XSSFWorkbook(pkg);

      POIXMLPropertiesTextExtractor ext = new POIXMLPropertiesTextExtractor(wb);
      ext.getText();

      // Now check
      String text = ext.getText();
      String cText = ext.getCustomPropertiesText();

      assertContains(text, "description = another value");
      assertContains(cText, "description = another value");

      ext.close();
	}

	/**
	 * Bug #49386 - some properties, especially
	 *  dates can be null
	 */
	@Test
	void testWithSomeNulls() throws Exception {
      OPCPackage pkg = OPCPackage.open(
            _slSamples.openResourceAsStream("49386-null_dates.pptx")
      );
      XSLFSlideShow sl = new XSLFSlideShow(pkg);

      POIXMLPropertiesTextExtractor ext = new POIXMLPropertiesTextExtractor(sl);
      ext.getText();

      String text = ext.getText();
      assertFalse(text.contains("Created =")); // With date is null
      assertContains(text, "CreatedString = "); // Via string is blank
      assertContains(text, "LastModifiedBy = IT Client Services");

      ext.close();
	}
}
