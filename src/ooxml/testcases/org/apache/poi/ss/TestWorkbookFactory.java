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

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.openxml4j.opc.OPCPackage;

import junit.framework.TestCase;

public final class TestWorkbookFactory extends TestCase {
	private String xls;
	private String xlsx;
	private String txt;

	protected void setUp() {
		xls = "SampleSS.xls";
		xlsx = "SampleSS.xlsx";
		txt = "SampleSS.txt";
	}

	public void testCreateNative() throws Exception {
		Workbook wb;

		// POIFS -> hssf
		wb = WorkbookFactory.create(
				new POIFSFileSystem(HSSFTestDataSamples.openSampleFileStream(xls))
		);
		assertNotNull(wb);
		assertTrue(wb instanceof HSSFWorkbook);

		// Package -> xssf
		wb = WorkbookFactory.create(
				OPCPackage.open(
                        HSSFTestDataSamples.openSampleFileStream(xlsx))
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
				HSSFTestDataSamples.openSampleFileStream(xls)
		);
		assertNotNull(wb);
		assertTrue(wb instanceof HSSFWorkbook);

		wb = WorkbookFactory.create(
				HSSFTestDataSamples.openSampleFileStream(xlsx)
		);
		assertNotNull(wb);
		assertTrue(wb instanceof XSSFWorkbook);

		try {
			wb = WorkbookFactory.create(
					HSSFTestDataSamples.openSampleFileStream(txt)
			);
			fail();
		} catch(IllegalArgumentException e) {
			// Good
		}
	}
}
