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
package org.apache.poi.ss;

import java.io.File;
import java.io.FileInputStream;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.openxml4j.opc.Package;

import junit.framework.TestCase;

public class TestWorkbookFactory extends TestCase {
	private File xls;
	private File xlsx;
	private File txt;

	protected void setUp() throws Exception {
		xls = new File(
				System.getProperty("HSSF.testdata.path") +
				File.separator + "SampleSS.xls"
		);
		xlsx = new File(
				System.getProperty("HSSF.testdata.path") +
				File.separator + "SampleSS.xlsx"
		);
		txt = new File(
				System.getProperty("HSSF.testdata.path") +
				File.separator + "SampleSS.txt"
		);
		assertTrue(xls.exists());
		assertTrue(xlsx.exists());
		assertTrue(txt.exists());
	}
	
	public void testCreateNative() throws Exception {
		Workbook wb;
		
		// POIFS -> hssf
		wb = WorkbookFactory.create(
				new POIFSFileSystem(new FileInputStream(xls))
		);
		assertNotNull(wb);
		assertTrue(wb instanceof HSSFWorkbook);
		
		// Package -> xssf
		wb = WorkbookFactory.create(
				Package.open(xlsx.toString())
		);
		assertNotNull(wb);
		assertTrue(wb instanceof XSSFWorkbook);
	}
		
	/**
	 * Creates the appropriate kind of Workbook, but
	 *  checking the mime magic at the start of the
	 *  InputStream, then creating what's required.
	 */
	public void testCreateGeneric() throws Exception {
		Workbook wb;
		
		// InputStream -> either
		wb = WorkbookFactory.create(
				new FileInputStream(xls)
		);
		assertNotNull(wb);
		assertTrue(wb instanceof HSSFWorkbook);
		
		wb = WorkbookFactory.create(
				new FileInputStream(xlsx)
		);
		assertNotNull(wb);
		assertTrue(wb instanceof XSSFWorkbook);
		
		try {
			wb = WorkbookFactory.create(
					new FileInputStream(txt)
			);
			fail();
		} catch(IllegalArgumentException e) {
			// Good
		}
	}
}