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

import java.io.FileInputStream;

import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import junit.framework.TestCase;

/**
 * Tests for how HSSFWorkbook behaves with XLS files
 *  with a WORKBOOK directory entry (instead of the more
 *  usual, Workbook)
 */
public class TestUppercaseWorkbook extends TestCase {
	private String dirPath;
	private String xlsA = "WORKBOOK_in_capitals.xls";

	protected void setUp() throws Exception {
		super.setUp();
		
        dirPath = System.getProperty("HSSF.testdata.path");
	}

	/**
	 * Test that we can open a file with WORKBOOK
	 */
	public void testOpen() throws Exception {
		FileInputStream is = new FileInputStream(dirPath + "/" + xlsA);
		
		POIFSFileSystem fs = new POIFSFileSystem(is);

		// Ensure that we have a WORKBOOK entry
		fs.getRoot().getEntry("WORKBOOK");
		assertTrue(true);
		
		// Try to open the workbook
		HSSFWorkbook wb = new HSSFWorkbook(fs);
	}
}
