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

package org.apache.poi.xssf.usermodel;

import org.apache.poi.ss.usermodel.BaseTestHyperlink;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.XSSFITestDataProvider;
import org.apache.poi.xssf.XSSFTestDataSamples;

public final class TestXSSFHyperlink extends BaseTestHyperlink {
	@Override
	protected XSSFITestDataProvider getTestDataProvider() {
		return XSSFITestDataProvider.getInstance();
	}

	@Override
	protected void setUp() {
		// Use system out logger
		System.setProperty(
				"org.apache.poi.util.POILogger",
				"org.apache.poi.util.SystemOutLogger"
		);
	}

	public void testLoadExisting() {
		XSSFWorkbook workbook = XSSFTestDataSamples.openSampleWorkbook("WithMoreVariousData.xlsx");
		assertEquals(3, workbook.getNumberOfSheets());

		XSSFSheet sheet = workbook.getSheetAt(0);

		// Check the hyperlinks
		assertEquals(4, sheet.getNumHyperlinks());
		doTestHyperlinkContents(sheet);
	}

	public void testLoadSave() {
		XSSFWorkbook workbook = XSSFTestDataSamples.openSampleWorkbook("WithMoreVariousData.xlsx");
		CreationHelper createHelper = workbook.getCreationHelper();
		assertEquals(3, workbook.getNumberOfSheets());
		XSSFSheet sheet = workbook.getSheetAt(0);

		// Check hyperlinks
		assertEquals(4, sheet.getNumHyperlinks());
		doTestHyperlinkContents(sheet);


		// Write out, and check

		// Load up again, check all links still there
		XSSFWorkbook wb2 = XSSFTestDataSamples.writeOutAndReadBack(workbook);
		assertEquals(3, wb2.getNumberOfSheets());
		assertNotNull(wb2.getSheetAt(0));
		assertNotNull(wb2.getSheetAt(1));
		assertNotNull(wb2.getSheetAt(2));

		sheet = wb2.getSheetAt(0);


		// Check hyperlinks again
		assertEquals(4, sheet.getNumHyperlinks());
		doTestHyperlinkContents(sheet);


		// Add one more, and re-check
		Row r17 = sheet.createRow(17);
		Cell r17c = r17.createCell(2);

		Hyperlink hyperlink = createHelper.createHyperlink(Hyperlink.LINK_URL);
		hyperlink.setAddress("http://poi.apache.org/spreadsheet/");
		hyperlink.setLabel("POI SS Link");
		r17c.setHyperlink(hyperlink);

		assertEquals(5, sheet.getNumHyperlinks());
		doTestHyperlinkContents(sheet);

		assertEquals(Hyperlink.LINK_URL,
				sheet.getRow(17).getCell(2).getHyperlink().getType());
		assertEquals("POI SS Link",
				sheet.getRow(17).getCell(2).getHyperlink().getLabel());
		assertEquals("http://poi.apache.org/spreadsheet/",
				sheet.getRow(17).getCell(2).getHyperlink().getAddress());


		// Save and re-load once more

		XSSFWorkbook wb3 = XSSFTestDataSamples.writeOutAndReadBack(wb2);
		assertEquals(3, wb3.getNumberOfSheets());
		assertNotNull(wb3.getSheetAt(0));
		assertNotNull(wb3.getSheetAt(1));
		assertNotNull(wb3.getSheetAt(2));

		sheet = wb3.getSheetAt(0);

		assertEquals(5, sheet.getNumHyperlinks());
		doTestHyperlinkContents(sheet);

		assertEquals(Hyperlink.LINK_URL,
				sheet.getRow(17).getCell(2).getHyperlink().getType());
		assertEquals("POI SS Link",
				sheet.getRow(17).getCell(2).getHyperlink().getLabel());
		assertEquals("http://poi.apache.org/spreadsheet/",
				sheet.getRow(17).getCell(2).getHyperlink().getAddress());
	}

	/**
	 * Only for WithMoreVariousData.xlsx !
	 */
	private void doTestHyperlinkContents(XSSFSheet sheet) {
		assertNotNull(sheet.getRow(3).getCell(2).getHyperlink());
		assertNotNull(sheet.getRow(14).getCell(2).getHyperlink());
		assertNotNull(sheet.getRow(15).getCell(2).getHyperlink());
		assertNotNull(sheet.getRow(16).getCell(2).getHyperlink());

		// First is a link to poi
		assertEquals(Hyperlink.LINK_URL,
				sheet.getRow(3).getCell(2).getHyperlink().getType());
		assertEquals(null,
				sheet.getRow(3).getCell(2).getHyperlink().getLabel());
		assertEquals("http://poi.apache.org/",
				sheet.getRow(3).getCell(2).getHyperlink().getAddress());

		// Next is an internal doc link
		assertEquals(Hyperlink.LINK_DOCUMENT,
				sheet.getRow(14).getCell(2).getHyperlink().getType());
		assertEquals("Internal hyperlink to A2",
				sheet.getRow(14).getCell(2).getHyperlink().getLabel());
		assertEquals("Sheet1!A2",
				sheet.getRow(14).getCell(2).getHyperlink().getAddress());

		// Next is a file
		assertEquals(Hyperlink.LINK_FILE,
				sheet.getRow(15).getCell(2).getHyperlink().getType());
		assertEquals(null,
				sheet.getRow(15).getCell(2).getHyperlink().getLabel());
		assertEquals("WithVariousData.xlsx",
				sheet.getRow(15).getCell(2).getHyperlink().getAddress());

		// Last is a mailto
		assertEquals(Hyperlink.LINK_EMAIL,
				sheet.getRow(16).getCell(2).getHyperlink().getType());
		assertEquals(null,
				sheet.getRow(16).getCell(2).getHyperlink().getLabel());
		assertEquals("mailto:dev@poi.apache.org?subject=XSSF%20Hyperlinks",
				sheet.getRow(16).getCell(2).getHyperlink().getAddress());
	}
}
