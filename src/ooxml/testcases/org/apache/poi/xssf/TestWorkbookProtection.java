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

import static org.apache.poi.xssf.XSSFTestDataSamples.openSampleWorkbook;
import static org.apache.poi.xssf.XSSFTestDataSamples.writeOutAndReadBack;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.poi.poifs.crypt.CryptoFunctions;
import org.apache.poi.poifs.crypt.HashAlgorithm;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;

public class TestWorkbookProtection {

    @Test
    public void workbookAndRevisionPassword() throws Exception {
        XSSFWorkbook workbook;
        String password = "test";
        
        // validate password with an actual office file (Excel 2010)
        workbook = openSampleWorkbook("workbookProtection-workbook_password_user_range-2010.xlsx");
        assertTrue(workbook.validateWorkbookPassword(password));

        // validate with another office file (Excel 2013)
        workbook = openSampleWorkbook("workbookProtection-workbook_password-2013.xlsx");
        assertTrue(workbook.validateWorkbookPassword(password));

        
        workbook = openSampleWorkbook("workbookProtection_not_protected.xlsx");

        // setting a null password shouldn't introduce the protection element
        workbook.setWorkbookPassword(null, null);
        assertNull(workbook.getCTWorkbook().getWorkbookProtection());

        // compare the hashes
        workbook.setWorkbookPassword(password, null);
        int hashVal = CryptoFunctions.createXorVerifier1(password);
        int actualVal = Integer.parseInt(workbook.getCTWorkbook().getWorkbookProtection().xgetWorkbookPassword().getStringValue(),16);
        assertEquals(hashVal, actualVal);
        assertTrue(workbook.validateWorkbookPassword(password));
        
        // removing the password again
        workbook.setWorkbookPassword(null, null);
        assertFalse(workbook.getCTWorkbook().getWorkbookProtection().isSetWorkbookPassword());
        
        // removing the whole protection structure
        workbook.unLock();
        assertNull(workbook.getCTWorkbook().getWorkbookProtection());
        
        // setting a null password shouldn't introduce the protection element
        workbook.setRevisionsPassword(null, null);
        assertNull(workbook.getCTWorkbook().getWorkbookProtection());

        // compare the hashes
        password = "T\u0400ST\u0100passwordWhichIsLongerThan15Chars";
        workbook.setRevisionsPassword(password, null);
        hashVal = CryptoFunctions.createXorVerifier1(password);
        actualVal = Integer.parseInt(workbook.getCTWorkbook().getWorkbookProtection().xgetRevisionsPassword().getStringValue(),16);
        assertEquals(hashVal, actualVal);
        assertTrue(workbook.validateRevisionsPassword(password));
    }
    
    @Test
    public void shouldReadWorkbookProtection() throws Exception {
		XSSFWorkbook workbook = openSampleWorkbook("workbookProtection_not_protected.xlsx");
		assertFalse(workbook.isStructureLocked());
		assertFalse(workbook.isWindowsLocked());
		assertFalse(workbook.isRevisionLocked());

		workbook = openSampleWorkbook("workbookProtection_workbook_structure_protected.xlsx");
		assertTrue(workbook.isStructureLocked());
		assertFalse(workbook.isWindowsLocked());
		assertFalse(workbook.isRevisionLocked());

		workbook = openSampleWorkbook("workbookProtection_workbook_windows_protected.xlsx");
		assertTrue(workbook.isWindowsLocked());
		assertFalse(workbook.isStructureLocked());
		assertFalse(workbook.isRevisionLocked());

		workbook = openSampleWorkbook("workbookProtection_workbook_revision_protected.xlsx");
		assertTrue(workbook.isRevisionLocked());
		assertFalse(workbook.isWindowsLocked());
		assertFalse(workbook.isStructureLocked());
	}

    @Test
	public void shouldWriteStructureLock() throws Exception {
		XSSFWorkbook workbook = openSampleWorkbook("workbookProtection_not_protected.xlsx");
		assertFalse(workbook.isStructureLocked());

		workbook.lockStructure();

		assertTrue(workbook.isStructureLocked());

		workbook.unLockStructure();

		assertFalse(workbook.isStructureLocked());
	}

    @Test
	public void shouldWriteWindowsLock() throws Exception {
		XSSFWorkbook workbook = openSampleWorkbook("workbookProtection_not_protected.xlsx");
		assertFalse(workbook.isWindowsLocked());

		workbook.lockWindows();

		assertTrue(workbook.isWindowsLocked());

		workbook.unLockWindows();

		assertFalse(workbook.isWindowsLocked());
	}

    @Test
	public void shouldWriteRevisionLock() throws Exception {
		XSSFWorkbook workbook = openSampleWorkbook("workbookProtection_not_protected.xlsx");
		assertFalse(workbook.isRevisionLocked());

		workbook.lockRevision();

		assertTrue(workbook.isRevisionLocked());

		workbook.unLockRevision();

		assertFalse(workbook.isRevisionLocked());
	}

    @SuppressWarnings("resource")
    @Test
    public void testHashPassword() throws Exception {
        XSSFWorkbook wb = new XSSFWorkbook();
        wb.lockRevision();
        wb.setRevisionsPassword("test", HashAlgorithm.sha1);
        
        wb = writeOutAndReadBack(wb);
        
        assertTrue(wb.isRevisionLocked());
        assertTrue(wb.validateRevisionsPassword("test"));
    }
    
    @SuppressWarnings("resource")
    @Test
	public void testIntegration() throws Exception {
		XSSFWorkbook wb = new XSSFWorkbook();
		wb.createSheet("Testing purpose sheet");
		assertFalse(wb.isRevisionLocked());

		wb.lockRevision();
		wb.setRevisionsPassword("test", null);

		wb = writeOutAndReadBack(wb);
		
		assertTrue(wb.isRevisionLocked());
		assertTrue(wb.validateRevisionsPassword("test"));
	}
}
