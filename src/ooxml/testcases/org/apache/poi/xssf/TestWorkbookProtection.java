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
package org.apache.poi.xssf;

import java.io.File;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import junit.framework.TestCase;

import org.apache.poi.util.TempFile;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class TestWorkbookProtection extends TestCase {

	public void testShouldReadWorkbookProtection() throws Exception {
		XSSFWorkbook workbook = XSSFTestDataSamples.openSampleWorkbook("workbookProtection_not_protected.xlsx");
		assertFalse(workbook.isStructureLocked());
		assertFalse(workbook.isWindowsLocked());
		assertFalse(workbook.isRevisionLocked());

		workbook = XSSFTestDataSamples.openSampleWorkbook("workbookProtection_workbook_structure_protected.xlsx");
		assertTrue(workbook.isStructureLocked());
		assertFalse(workbook.isWindowsLocked());
		assertFalse(workbook.isRevisionLocked());

		workbook = XSSFTestDataSamples.openSampleWorkbook("workbookProtection_workbook_windows_protected.xlsx");
		assertTrue(workbook.isWindowsLocked());
		assertFalse(workbook.isStructureLocked());
		assertFalse(workbook.isRevisionLocked());

		workbook = XSSFTestDataSamples.openSampleWorkbook("workbookProtection_workbook_revision_protected.xlsx");
		assertTrue(workbook.isRevisionLocked());
		assertFalse(workbook.isWindowsLocked());
		assertFalse(workbook.isStructureLocked());
	}

	public void testShouldWriteStructureLock() throws Exception {
		XSSFWorkbook workbook = XSSFTestDataSamples.openSampleWorkbook("workbookProtection_not_protected.xlsx");
		assertFalse(workbook.isStructureLocked());

		workbook.lockStructure();

		assertTrue(workbook.isStructureLocked());

		workbook.unLockStructure();

		assertFalse(workbook.isStructureLocked());
	}

	public void testShouldWriteWindowsLock() throws Exception {
		XSSFWorkbook workbook = XSSFTestDataSamples.openSampleWorkbook("workbookProtection_not_protected.xlsx");
		assertFalse(workbook.isWindowsLocked());

		workbook.lockWindows();

		assertTrue(workbook.isWindowsLocked());

		workbook.unLockWindows();

		assertFalse(workbook.isWindowsLocked());
	}

	public void testShouldWriteRevisionLock() throws Exception {
		XSSFWorkbook workbook = XSSFTestDataSamples.openSampleWorkbook("workbookProtection_not_protected.xlsx");
		assertFalse(workbook.isRevisionLocked());

		workbook.lockRevision();

		assertTrue(workbook.isRevisionLocked());

		workbook.unLockRevision();

		assertFalse(workbook.isRevisionLocked());
	}

	public void testIntegration() throws Exception {
		XSSFWorkbook wb = new XSSFWorkbook();
		wb.createSheet("Testing purpose sheet");
		assertFalse(wb.isRevisionLocked());

		wb.lockRevision();

		File tempFile = TempFile.createTempFile("workbookProtection", ".xlsx");
		FileOutputStream out = new FileOutputStream(tempFile);
		wb.write(out);
		out.close();

		FileInputStream inputStream = new FileInputStream(tempFile);
		XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
		inputStream.close();

		assertTrue(workbook.isRevisionLocked());
	}
}
