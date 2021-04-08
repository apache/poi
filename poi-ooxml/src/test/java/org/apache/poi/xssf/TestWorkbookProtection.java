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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.crypt.CryptoFunctions;
import org.apache.poi.poifs.crypt.Decryptor;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.crypt.EncryptionMode;
import org.apache.poi.poifs.crypt.Encryptor;
import org.apache.poi.poifs.crypt.HashAlgorithm;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.TempFile;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

class TestWorkbookProtection {

    @Test
    void workbookAndRevisionPassword() throws Exception {
        String password = "test";

        // validate password with an actual office file (Excel 2010)
        try (XSSFWorkbook workbook = openSampleWorkbook("workbookProtection-workbook_password_user_range-2010.xlsx")) {
            assertTrue(workbook.validateWorkbookPassword(password));
        }

        // validate with another office file (Excel 2013)
        try (XSSFWorkbook workbook = openSampleWorkbook("workbookProtection-workbook_password-2013.xlsx")){
            assertTrue(workbook.validateWorkbookPassword(password));
        }


        try (XSSFWorkbook workbook = openSampleWorkbook("workbookProtection_not_protected.xlsx")) {
            // setting a null password shouldn't introduce the protection element
            workbook.setWorkbookPassword(null, null);
            assertNull(workbook.getCTWorkbook().getWorkbookProtection());

            // compare the hashes
            workbook.setWorkbookPassword(password, null);
            int hashVal = CryptoFunctions.createXorVerifier1(password);
            int actualVal = Integer.parseInt(workbook.getCTWorkbook().getWorkbookProtection().xgetWorkbookPassword().getStringValue(), 16);
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
            actualVal = Integer.parseInt(workbook.getCTWorkbook().getWorkbookProtection().xgetRevisionsPassword().getStringValue(), 16);
            assertEquals(hashVal, actualVal);
            assertTrue(workbook.validateRevisionsPassword(password));
        }
    }

    @Test
    void shouldReadWorkbookProtection() throws Exception {
        try (XSSFWorkbook workbook = openSampleWorkbook("workbookProtection_not_protected.xlsx")) {
            assertFalse(workbook.isStructureLocked());
            assertFalse(workbook.isWindowsLocked());
            assertFalse(workbook.isRevisionLocked());
        }

        try (XSSFWorkbook workbook = openSampleWorkbook("workbookProtection_workbook_structure_protected.xlsx")) {
            assertTrue(workbook.isStructureLocked());
            assertFalse(workbook.isWindowsLocked());
            assertFalse(workbook.isRevisionLocked());
        }

        try (XSSFWorkbook workbook = openSampleWorkbook("workbookProtection_workbook_windows_protected.xlsx")) {
            assertTrue(workbook.isWindowsLocked());
            assertFalse(workbook.isStructureLocked());
            assertFalse(workbook.isRevisionLocked());
        }

        try (XSSFWorkbook workbook = openSampleWorkbook("workbookProtection_workbook_revision_protected.xlsx")) {
            assertTrue(workbook.isRevisionLocked());
            assertFalse(workbook.isWindowsLocked());
            assertFalse(workbook.isStructureLocked());
        }
    }

    @Test
    void shouldWriteStructureLock() throws Exception {
        try (XSSFWorkbook workbook = openSampleWorkbook("workbookProtection_not_protected.xlsx")) {
            assertFalse(workbook.isStructureLocked());

            workbook.lockStructure();

            assertTrue(workbook.isStructureLocked());

            workbook.unLockStructure();

            assertFalse(workbook.isStructureLocked());
        }
    }

    @Test
    void shouldWriteWindowsLock() throws Exception {
        try (XSSFWorkbook workbook = openSampleWorkbook("workbookProtection_not_protected.xlsx")) {
            assertFalse(workbook.isWindowsLocked());

            workbook.lockWindows();

            assertTrue(workbook.isWindowsLocked());

            workbook.unLockWindows();

            assertFalse(workbook.isWindowsLocked());
        }
    }

    @Test
    void shouldWriteRevisionLock() throws Exception {
        try (XSSFWorkbook workbook = openSampleWorkbook("workbookProtection_not_protected.xlsx")) {
            assertFalse(workbook.isRevisionLocked());

            workbook.lockRevision();

            assertTrue(workbook.isRevisionLocked());

            workbook.unLockRevision();

            assertFalse(workbook.isRevisionLocked());
        }
    }

    @SuppressWarnings("resource")
    @Test
    void testHashPassword() throws Exception {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            wb.lockRevision();
            wb.setRevisionsPassword("test", HashAlgorithm.sha1);

            try (XSSFWorkbook wbBack = writeOutAndReadBack(wb)) {

                assertTrue(wbBack.isRevisionLocked());
                assertTrue(wbBack.validateRevisionsPassword("test"));
            }
        }
    }

    @SuppressWarnings("resource")
    @Test
    void testIntegration() throws Exception {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            wb.createSheet("Testing purpose sheet");
            assertFalse(wb.isRevisionLocked());

            wb.lockRevision();
            wb.setRevisionsPassword("test", null);

            try (XSSFWorkbook wbBack = writeOutAndReadBack(wb)) {

                assertTrue(wbBack.isRevisionLocked());
                assertTrue(wbBack.validateRevisionsPassword("test"));
            }
        }
    }

    @Test
    void testEncryptDecrypt() throws Exception {
        final String password = "abc123";
        final String sheetName = "TestSheet1";
        final String cellValue = "customZipEntrySource";
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet1 = workbook.createSheet(sheetName);
            XSSFRow row1 = sheet1.createRow(1);
            XSSFCell cell1 = row1.createCell(1);
            cell1.setCellValue(cellValue);
            File tf1 = TempFile.createTempFile("poitest", ".xlsx");
            FileOutputStream fos1 = new FileOutputStream(tf1);
            workbook.write(fos1);
            IOUtils.closeQuietly(fos1);
            POIFSFileSystem poiFileSystem = new POIFSFileSystem();
            EncryptionInfo encryptionInfo = new EncryptionInfo(EncryptionMode.agile);
            Encryptor enc = encryptionInfo.getEncryptor();
            enc.confirmPassword(password);
            FileInputStream fis = new FileInputStream(tf1);
            OPCPackage opc = OPCPackage.open(fis);
            IOUtils.closeQuietly(fis);
            try {
                OutputStream os = enc.getDataStream(poiFileSystem);
                opc.save(os);
                IOUtils.closeQuietly(os);
            } finally {
                IOUtils.closeQuietly(opc);
            }
            assertTrue(tf1.delete());
            FileOutputStream fos2 = new FileOutputStream(tf1);
            poiFileSystem.writeFilesystem(fos2);
            IOUtils.closeQuietly(fos2);
            workbook.close();
            fis = new FileInputStream(tf1);
            POIFSFileSystem poiFileSystem2 = new POIFSFileSystem(fis);
            IOUtils.closeQuietly(fis);
            EncryptionInfo encryptionInfo2 = new EncryptionInfo(poiFileSystem2);
            Decryptor decryptor = encryptionInfo2.getDecryptor();
            decryptor.verifyPassword(password);
            XSSFWorkbook workbook2 = new XSSFWorkbook(decryptor.getDataStream(poiFileSystem2));
            workbook2.close();
            assertTrue(tf1.delete());
        }
    }
}
