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

import org.apache.poi.hssf.HSSFXML;
import org.apache.poi.hssf.usermodel.HSSFXMLWorkbook;
import org.apache.poi.hxf.HXFDocument;

import junit.framework.TestCase;

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
		
		xmlA = new HSSFXML(HXFDocument.openPackage(fileA));
		xmlB = new HSSFXML(HXFDocument.openPackage(fileB));
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
		System.err.println(text);
		
		// Check sheet names
		assertTrue(text.startsWith("Sheet1"));
		assertTrue(text.endsWith("Sheet3\n"));
	}
}
