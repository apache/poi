/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.apache.poi.hssf.usermodel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import junit.framework.TestCase;

/**
 * Tests for how HSSFWorkbook behaves with XLS files
 *  with a WORKBOOK directory entry (instead of the more
 *  usual, Workbook)
 */
public class TestSheetHiding extends TestCase {
	private String dirPath;
	private String xlsHidden = "TwoSheetsOneHidden.xls";
	private String xlsShown  = "TwoSheetsNoneHidden.xls";

	protected void setUp() throws Exception {
		super.setUp();
		
        dirPath = System.getProperty("HSSF.testdata.path");
	}

	/**
	 * Test that we get the right number of sheets,
	 *  with the right text on them, no matter what
	 *  the hidden flags are
	 */
	public void testTextSheets() throws Exception {
		FileInputStream isH = new FileInputStream(dirPath + "/" + xlsHidden);
		POIFSFileSystem fsH = new POIFSFileSystem(isH);
		
		FileInputStream isU = new FileInputStream(dirPath + "/" + xlsShown);
		POIFSFileSystem fsU = new POIFSFileSystem(isU);

		HSSFWorkbook wbH = new HSSFWorkbook(fsH);
		HSSFWorkbook wbU = new HSSFWorkbook(fsU);
		
		// Both should have two sheets
		assertEquals(2, wbH.sheets.size());
		assertEquals(2, wbU.sheets.size());
		
		// All sheets should have one row
		assertEquals(0, wbH.getSheetAt(0).getLastRowNum());
		assertEquals(0, wbH.getSheetAt(1).getLastRowNum());
		assertEquals(0, wbU.getSheetAt(0).getLastRowNum());
		assertEquals(0, wbU.getSheetAt(1).getLastRowNum());
		
		// All rows should have one column
		assertEquals(1, wbH.getSheetAt(0).getRow(0).getLastCellNum());
		assertEquals(1, wbH.getSheetAt(1).getRow(0).getLastCellNum());
		assertEquals(1, wbU.getSheetAt(0).getRow(0).getLastCellNum());
		assertEquals(1, wbU.getSheetAt(1).getRow(0).getLastCellNum());
		
		// Text should be sheet based
		assertEquals("Sheet1A1", wbH.getSheetAt(0).getRow(0).getCell((short)0).getStringCellValue());
		assertEquals("Sheet2A1", wbH.getSheetAt(1).getRow(0).getCell((short)0).getStringCellValue());
		assertEquals("Sheet1A1", wbU.getSheetAt(0).getRow(0).getCell((short)0).getStringCellValue());
		assertEquals("Sheet2A1", wbU.getSheetAt(1).getRow(0).getCell((short)0).getStringCellValue());
	}

	/**
	 * Check that we can get and set the hidden flags
	 *  as expected
	 */
	public void testHideUnHideFlags() throws Exception {
		// TODO
	}

	/**
	 * Turn the sheet with none hidden into the one with
	 *  one hidden
	 */
	public void testHide() throws Exception {
		// TODO
	}

	/**
	 * Turn the sheet with one hidden into the one with
	 *  none hidden
	 */
	public void testUnHide() throws Exception {
		// TODO
	}
}
