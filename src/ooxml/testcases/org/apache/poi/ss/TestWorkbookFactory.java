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

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileNotFoundException;

import org.apache.poi.EmptyFileException;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.TempFile;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.xssf.usermodel.XSSFWorkbookFactory;
import org.junit.Test;

public final class TestWorkbookFactory {
    private static final String xls = "SampleSS.xls";
    private static final String xlsx = "SampleSS.xlsx";
    private static final String[] xls_prot = new String[] {"password.xls", "password"};
    private static final String[] xlsx_prot = new String[]{"protected_passtika.xlsx", "tika"};
    private static final String txt = "SampleSS.txt";
    
    private static final POILogger LOGGER = POILogFactory.getLogger(TestWorkbookFactory.class);
    
    /**
     * Closes the sample workbook read in from filename.
     * Throws an exception if closing the workbook results in the file on disk getting modified.
     *
     * @param filename the sample workbook to read in
     * @param wb the workbook to close
     */
    private static void assertCloseDoesNotModifyFile(String filename, Workbook wb) throws IOException {
        final byte[] before = HSSFTestDataSamples.getTestDataFileContent(filename);
        // FIXME: replace with wb.close() when bug 58779 is resolved
        closeOrRevert(wb);
        final byte[] after = HSSFTestDataSamples.getTestDataFileContent(filename);
        assertArrayEquals(filename + " sample file was modified as a result of closing the workbook",
                before, after);
    }
    
    /**
     * bug 58779: Closing an XSSFWorkbook that was created with WorkbookFactory modifies the file
     * FIXME: replace this method with wb.close() when bug 58779 is resolved.
     *
     * @param wb the workbook to close or revert
     */
    private static void closeOrRevert(Workbook wb) throws IOException {
        if (wb instanceof HSSFWorkbook) {
            wb.close();
        }
        else if (wb instanceof XSSFWorkbook) {
            final XSSFWorkbook xwb = (XSSFWorkbook) wb;
            if (PackageAccess.READ == xwb.getPackage().getPackageAccess()) {
                xwb.close();
            }
            else {
                // TODO: close() re-writes the sample-file?! Resort to revert() for now to close file handle...
                LOGGER.log(POILogger.WARN,
                        "reverting XSSFWorkbook rather than closing it to avoid close() modifying the file on disk. " +
                        "Refer to bug 58779.");
                xwb.getPackage().revert();
            }
        } else {
            throw new RuntimeException("Unsupported workbook type");
        }
    }

    @Test
    public void testCreateNative() throws Exception {
        Workbook wb;

        // POIFS -> hssf
        wb = WorkbookFactory.create(
                new POIFSFileSystem(HSSFTestDataSamples.openSampleFileStream(xls))
        );
        assertNotNull(wb);
        assertTrue(wb instanceof HSSFWorkbook);
        assertCloseDoesNotModifyFile(xls, wb);

        // Package -> xssf
        wb = XSSFWorkbookFactory.create(
                OPCPackage.open(
                        HSSFTestDataSamples.openSampleFileStream(xlsx))
        );
        assertNotNull(wb);
        //noinspection ConstantConditions
        assertTrue(wb instanceof XSSFWorkbook);
        assertCloseDoesNotModifyFile(xlsx, wb);
    }

    @Test
    public void testCreateReadOnly() throws Exception {
        Workbook wb;

        // POIFS -> hssf
        wb = WorkbookFactory.create(HSSFTestDataSamples.getSampleFile(xls), null, true);
        assertNotNull(wb);
        assertTrue(wb instanceof HSSFWorkbook);
        assertCloseDoesNotModifyFile(xls, wb);

        // Package -> xssf
        wb = WorkbookFactory.create(HSSFTestDataSamples.getSampleFile(xlsx), null, true);
        assertNotNull(wb);
        assertTrue(wb instanceof XSSFWorkbook);
        assertCloseDoesNotModifyFile(xlsx, wb);
    }

    /**
     * Creates the appropriate kind of Workbook, but
     *  checking the mime magic at the start of the
     *  InputStream, then creating what's required.
     */
    @Test
    public void testCreateGeneric() throws Exception {
        Workbook wb;

        // InputStream -> either
        wb = WorkbookFactory.create(
                HSSFTestDataSamples.openSampleFileStream(xls)
        );
        assertNotNull(wb);
        assertTrue(wb instanceof HSSFWorkbook);
        assertCloseDoesNotModifyFile(xls, wb);

        wb = WorkbookFactory.create(
                HSSFTestDataSamples.openSampleFileStream(xlsx)
        );
        assertNotNull(wb);
        assertTrue(wb instanceof XSSFWorkbook);
        assertCloseDoesNotModifyFile(xlsx, wb);

        // File -> either
        wb = WorkbookFactory.create(
                HSSFTestDataSamples.getSampleFile(xls)
        );
        assertNotNull(wb);
        assertTrue(wb instanceof HSSFWorkbook);
        assertCloseDoesNotModifyFile(xls, wb);

        wb = WorkbookFactory.create(
                HSSFTestDataSamples.getSampleFile(xlsx)
        );
        assertNotNull(wb);
        assertTrue(wb instanceof XSSFWorkbook);
        assertCloseDoesNotModifyFile(xlsx, wb);

        // Invalid type -> exception
        final byte[] before = HSSFTestDataSamples.getTestDataFileContent(txt);
        try {
            try (InputStream stream = HSSFTestDataSamples.openSampleFileStream(txt)) {
                wb = WorkbookFactory.create(stream);
                assertNotNull(wb);
            }
            fail();
        } catch(IOException e) {
            // Good
        }
        final byte[] after = HSSFTestDataSamples.getTestDataFileContent(txt);
        assertArrayEquals("Invalid type file was modified after trying to open the file as a spreadsheet",
                before, after);
    }

    /**
     * Check that the overloaded stream methods which take passwords work properly
     */
    @Test
    public void testCreateWithPasswordFromStream() throws Exception {
        Workbook wb;


        // Unprotected, no password given, opens normally
        wb = WorkbookFactory.create(
                HSSFTestDataSamples.openSampleFileStream(xls), null
        );
        assertNotNull(wb);
        assertTrue(wb instanceof HSSFWorkbook);
        assertCloseDoesNotModifyFile(xls, wb);

        wb = WorkbookFactory.create(
                HSSFTestDataSamples.openSampleFileStream(xlsx), null
        );
        assertNotNull(wb);
        assertTrue(wb instanceof XSSFWorkbook);
        assertCloseDoesNotModifyFile(xlsx, wb);


        // Unprotected, wrong password, opens normally
        wb = WorkbookFactory.create(
                HSSFTestDataSamples.openSampleFileStream(xls), "wrong"
        );
        assertNotNull(wb);
        assertTrue(wb instanceof HSSFWorkbook);
        assertCloseDoesNotModifyFile(xls, wb);

        wb = WorkbookFactory.create(
                HSSFTestDataSamples.openSampleFileStream(xlsx), "wrong"
        );
        assertNotNull(wb);
        assertTrue(wb instanceof XSSFWorkbook);
        assertCloseDoesNotModifyFile(xlsx, wb);


        // Protected, correct password, opens fine
        wb = WorkbookFactory.create(
                HSSFTestDataSamples.openSampleFileStream(xls_prot[0]), xls_prot[1]
        );
        assertNotNull(wb);
        assertTrue(wb instanceof HSSFWorkbook);
        assertCloseDoesNotModifyFile(xls_prot[0], wb);

        wb = WorkbookFactory.create(
                HSSFTestDataSamples.openSampleFileStream(xlsx_prot[0]), xlsx_prot[1]
        );
        assertNotNull(wb);
        assertTrue(wb instanceof XSSFWorkbook);
        assertCloseDoesNotModifyFile(xlsx_prot[0], wb);


        // Protected, wrong password, throws Exception
        try {
            wb = WorkbookFactory.create(
                    HSSFTestDataSamples.openSampleFileStream(xls_prot[0]), "wrong"
            );
            assertCloseDoesNotModifyFile(xls_prot[0], wb);
            fail("Shouldn't be able to open with the wrong password");
        } catch (EncryptedDocumentException e) {
            // expected here
        }

        try {
            wb = WorkbookFactory.create(
                    HSSFTestDataSamples.openSampleFileStream(xlsx_prot[0]), "wrong"
            );
            assertCloseDoesNotModifyFile(xlsx_prot[0], wb);
            fail("Shouldn't be able to open with the wrong password");
        } catch (EncryptedDocumentException e) {
            // expected here
        }
    }

    /**
     * Check that the overloaded file methods which take passwords work properly
     */
    @Test
    public void testCreateWithPasswordFromFile() throws Exception {
        Workbook wb;

        // Unprotected, no password given, opens normally
        wb = WorkbookFactory.create(
                HSSFTestDataSamples.getSampleFile(xls), null
        );
        assertNotNull(wb);
        assertTrue(wb instanceof HSSFWorkbook);
        assertCloseDoesNotModifyFile(xls, wb);

        wb = WorkbookFactory.create(
                HSSFTestDataSamples.getSampleFile(xlsx), null
        );
        assertNotNull(wb);
        assertTrue(wb instanceof XSSFWorkbook);
        assertCloseDoesNotModifyFile(xlsx, wb);

        // Unprotected, wrong password, opens normally
        wb = WorkbookFactory.create(
                HSSFTestDataSamples.getSampleFile(xls), "wrong"
        );
        assertNotNull(wb);
        assertTrue(wb instanceof HSSFWorkbook);
        assertCloseDoesNotModifyFile(xls, wb);

        wb = WorkbookFactory.create(
                HSSFTestDataSamples.getSampleFile(xlsx), "wrong"
        );
        assertNotNull(wb);
        assertTrue(wb instanceof XSSFWorkbook);
        assertCloseDoesNotModifyFile(xlsx, wb);

        // Protected, correct password, opens fine
        wb = WorkbookFactory.create(
                HSSFTestDataSamples.getSampleFile(xls_prot[0]), xls_prot[1]
        );
        assertNotNull(wb);
        assertTrue(wb instanceof HSSFWorkbook);
        assertCloseDoesNotModifyFile(xls_prot[0], wb);

        wb = WorkbookFactory.create(
                HSSFTestDataSamples.getSampleFile(xlsx_prot[0]), xlsx_prot[1]
        );
        assertNotNull(wb);
        assertTrue(wb instanceof XSSFWorkbook);
        assertTrue(wb.getNumberOfSheets() > 0);
        assertNotNull(wb.getSheetAt(0));
        assertNotNull(wb.getSheetAt(0).getRow(0));
        assertCloseDoesNotModifyFile(xlsx_prot[0], wb);

        // Protected, wrong password, throws Exception
        try {
            wb = WorkbookFactory.create(
                    HSSFTestDataSamples.getSampleFile(xls_prot[0]), "wrong"
            );
            assertCloseDoesNotModifyFile(xls_prot[0], wb);
            fail("Shouldn't be able to open with the wrong password");
        } catch (EncryptedDocumentException e) {
            // expected here
        }

        try {
            wb = WorkbookFactory.create(
                    HSSFTestDataSamples.getSampleFile(xlsx_prot[0]), "wrong"
            );
            assertCloseDoesNotModifyFile(xlsx_prot[0], wb);
            fail("Shouldn't be able to open with the wrong password");
        } catch (EncryptedDocumentException e) {
            // expected here
        }
    }
    
    /**
     * Check that a helpful exception is given on an empty input stream
     */
    @Test
    public void testEmptyInputStream() throws Exception {
        InputStream emptyStream = new ByteArrayInputStream(new byte[0]);
        try {
            WorkbookFactory.create(emptyStream);
            fail("Shouldn't be able to create for an empty stream");
        } catch (final EmptyFileException expected) {}
    }
    
    /**
     * Check that a helpful exception is given on an empty file
     */
    @Test
    public void testEmptyFile() throws Exception {
        File emptyFile = TempFile.createTempFile("empty", ".poi");
        try {
            WorkbookFactory.create(emptyFile);
            fail("Shouldn't be able to create for an empty file");
        } catch (final EmptyFileException expected) {
            // expected here
        }

        assertTrue(emptyFile.delete());
    }

    /**
      * Check that a helpful exception is raised on a non-existing file
      */
    @Test
    public void testNonExistingFile() throws Exception {
        File nonExistingFile = new File("notExistingFile");
        assertFalse(nonExistingFile.exists());

        try {
            WorkbookFactory.create(nonExistingFile, "password", true);
            fail("Should not be able to create for a non-existing file");
        } catch (final FileNotFoundException e) {
            // expected
        }
    }

}
