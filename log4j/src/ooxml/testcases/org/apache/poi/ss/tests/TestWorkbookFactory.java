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

package org.apache.poi.ss.tests;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Stream;

import org.apache.poi.EmptyFileException;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.POIDataSamples;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.SuppressForbidden;
import org.apache.poi.util.TempFile;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbookFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public final class TestWorkbookFactory {
    private static final String xls = "SampleSS.xls";
    private static final String xlsx = "SampleSS.xlsx";
    private static final String[] xls_protected = new String[]{"password.xls", "password"};
    private static final String[] xlsx_protected = new String[]{"protected_passtika.xlsx", "tika"};
    private static final String txt = "SampleSS.txt";

    private static final POILogger LOGGER = POILogFactory.getLogger(TestWorkbookFactory.class);

    /**
     * Closes the sample workbook read in from filename.
     * Throws an exception if closing the workbook results in the file on disk getting modified.
     *
     * @param filename the sample workbook to read in
     * @param wb       the workbook to close
     */
    private static void assertCloseDoesNotModifyFile(String filename, Workbook wb) throws IOException {
        final byte[] before = HSSFTestDataSamples.getTestDataFileContent(filename);
        // FIXME: replace with wb.close() when bug 58779 is resolved
        closeOrRevert(wb);
        final byte[] after = HSSFTestDataSamples.getTestDataFileContent(filename);
        assertArrayEquals(before, after, filename + " sample file was modified as a result of closing the workbook");
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
        } else if (wb instanceof XSSFWorkbook) {
            final XSSFWorkbook xwb = (XSSFWorkbook) wb;
            if (PackageAccess.READ == xwb.getPackage().getPackageAccess()) {
                xwb.close();
            } else {
                // TODO: close() re-writes the sample-file?! Resort to revert() for now to close file handle...
                LOGGER.log(POILogger.WARN,
                    "reverting XSSFWorkbook rather than closing it to avoid close() modifying the file on disk. Refer to bug 58779.");
                xwb.getPackage().revert();
            }
        } else {
            throw new RuntimeException("Unsupported workbook type");
        }
    }

    @Test
    void testCreateNative() throws Exception {
        // POIFS -> hssf
        try (Workbook wb = WorkbookFactory.create(
            new POIFSFileSystem(HSSFTestDataSamples.openSampleFileStream(xls))
        )) {
            assertNotNull(wb);
            assertTrue(wb instanceof HSSFWorkbook);
        }

        try (Workbook wb = WorkbookFactory.create(
            new POIFSFileSystem(HSSFTestDataSamples.openSampleFileStream(xls)).getRoot()
        )) {
            assertNotNull(wb);
            assertTrue(wb instanceof HSSFWorkbook);
        }

        // Package -> xssf
        try (Workbook wb = XSSFWorkbookFactory.createWorkbook(
            OPCPackage.open(HSSFTestDataSamples.openSampleFileStream(xlsx))
        )) {
            assertNotNull(wb);
            //noinspection ConstantConditions
            assertTrue(wb instanceof XSSFWorkbook);
        }
    }

    @Test
    void testCreateReadOnly() throws Exception {
        // POIFS -> hssf
        try (Workbook wb = WorkbookFactory.create(HSSFTestDataSamples.getSampleFile(xls), null, true)) {
            assertNotNull(wb);
            assertTrue(wb instanceof HSSFWorkbook);
            assertCloseDoesNotModifyFile(xls, wb);
        }

        // Package -> xssf
        try (Workbook wb = WorkbookFactory.create(HSSFTestDataSamples.getSampleFile(xlsx), null, true)) {
            assertNotNull(wb);
            assertTrue(wb instanceof XSSFWorkbook);
            assertCloseDoesNotModifyFile(xlsx, wb);
        }
    }

    /**
     * Creates the appropriate kind of Workbook, but
     * checking the mime magic at the start of the
     * InputStream, then creating what's required.
     */
    @Test
    void testCreateGeneric() throws Exception {
        // InputStream -> either
        try (Workbook wb = WorkbookFactory.create(HSSFTestDataSamples.openSampleFileStream(xls))) {
            assertNotNull(wb);
            assertTrue(wb instanceof HSSFWorkbook);
        }

        try (Workbook wb = WorkbookFactory.create(HSSFTestDataSamples.openSampleFileStream(xlsx))) {
            assertNotNull(wb);
            assertTrue(wb instanceof XSSFWorkbook);
        }

        // File -> either
        try (Workbook wb = WorkbookFactory.create(HSSFTestDataSamples.getSampleFile(xls))) {
            assertNotNull(wb);
            assertTrue(wb instanceof HSSFWorkbook);
            assertCloseDoesNotModifyFile(xls, wb);
        }

        try (Workbook wb = WorkbookFactory.create(HSSFTestDataSamples.getSampleFile(xlsx))) {
            assertNotNull(wb);
            assertTrue(wb instanceof XSSFWorkbook);
            assertCloseDoesNotModifyFile(xlsx, wb);
        }

        // Invalid type -> exception
        final byte[] before = HSSFTestDataSamples.getTestDataFileContent(txt);
        assertThrows(IOException.class, () -> WorkbookFactory.create(new File(txt)));
        final byte[] after = HSSFTestDataSamples.getTestDataFileContent(txt);
        assertArrayEquals(before, after, "Invalid type file was modified after trying to open the file as a spreadsheet");
    }

    public static Stream<Arguments> workbookPass() {
        return Stream.of(
            // Unprotected, no password given, opens normally
            Arguments.of(xls, null, false, HSSFWorkbook.class),
            Arguments.of(xlsx, null, false, XSSFWorkbook.class),
            // Unprotected, wrong password, opens normally
            Arguments.of(xls, "wrong", false, HSSFWorkbook.class),
            Arguments.of(xlsx, "wrong", false, XSSFWorkbook.class),
            // Protected, correct password, opens fine
            Arguments.of(xls_protected[0], xls_protected[1], false, HSSFWorkbook.class),
            Arguments.of(xlsx_protected[0], xlsx_protected[1], false, XSSFWorkbook.class),
            // Protected, wrong password, throws Exception
            Arguments.of(xls_protected[0], "wrong", true, HSSFWorkbook.class),
            Arguments.of(xlsx_protected[0], "wrong", true, XSSFWorkbook.class)
        );
    }

    /**
     * Check that the overloaded stream methods which take passwords work properly
     */
    @ParameterizedTest
    @MethodSource("workbookPass")
    void testCreateWithPasswordFromStream(String file, String pass, boolean fails, Class<? extends Workbook> clazz) throws Exception {
        try (InputStream is = HSSFTestDataSamples.openSampleFileStream(file)) {
            if (fails) {
                assertThrows(EncryptedDocumentException.class, () -> WorkbookFactory.create(is, pass),
                    "Shouldn't be able to open with the wrong password");
            } else {
                try (Workbook wb = WorkbookFactory.create(is, pass)) {
                    assertNotNull(wb);
                    assertTrue(clazz.isInstance(wb));
                }
            }
        }
    }

    /**
     * Check that the overloaded file methods which take passwords work properly
     */
    @ParameterizedTest
    @MethodSource("workbookPass")
    void testCreateWithPasswordFromFile(String fileName, String pass, boolean fails, Class<? extends Workbook> clazz) throws Exception {
        File file = HSSFTestDataSamples.getSampleFile(fileName);
        if (fails) {
            assertThrows(EncryptedDocumentException.class, () -> WorkbookFactory.create(file, pass),
                "Shouldn't be able to open with the wrong password");
        } else {
            try (Workbook wb = WorkbookFactory.create(file, pass)) {
                assertNotNull(wb);
                assertTrue(clazz.isInstance(wb));
                assertCloseDoesNotModifyFile(fileName, wb);
            }
        }
    }

    /**
     * Check that a helpful exception is given on an empty input stream
     */
    @Test
    void testEmptyInputStream() {
        InputStream emptyStream = new ByteArrayInputStream(new byte[0]);
        assertThrows(EmptyFileException.class, () -> WorkbookFactory.create(emptyStream));
    }

    /**
     * Check that a helpful exception is given on an empty file
     */
    @Test
    void testEmptyFile() throws Exception {
        File emptyFile = TempFile.createTempFile("empty", ".poi");
        assertThrows(EmptyFileException.class, () -> WorkbookFactory.create(emptyFile),
            "Shouldn't be able to create for an empty file");
        assertTrue(emptyFile.delete());
    }

    /**
     * Check that a helpful exception is raised on a non-existing file
     */
    @Test
    void testNonExistingFile() {
        File nonExistingFile = new File("notExistingFile");
        assertFalse(nonExistingFile.exists());
        assertThrows(FileNotFoundException.class, () -> WorkbookFactory.create(nonExistingFile, "password", true));
    }

    /**
     * See Bugzilla bug #62831 - #WorkbookFactory.create(File) needs
     * to work for sub-classes of File too, eg JFileChooser
     */
    @Test
    void testFileSubclass() throws Exception {
        File normalXLS = HSSFTestDataSamples.getSampleFile(xls);
        File normalXLSX = HSSFTestDataSamples.getSampleFile(xlsx);
        File altXLS = new TestFile(normalXLS.getAbsolutePath());
        File altXLSX = new TestFile(normalXLSX.getAbsolutePath());
        assertTrue(altXLS.exists());
        assertTrue(altXLSX.exists());

        try (Workbook wb = WorkbookFactory.create(altXLS)) {
            assertNotNull(wb);
            assertTrue(wb instanceof HSSFWorkbook);
            closeOrRevert(wb);
        }

        try (Workbook wb = WorkbookFactory.create(altXLSX)) {
            assertNotNull(wb);
            assertTrue(wb instanceof XSSFWorkbook);
            closeOrRevert(wb);
        }
    }

    private static class TestFile extends File {
        public TestFile(String file) {
            super(file);
        }
    }

    /**
     * Check that the overloaded file methods which take passwords work properly
     */
    @Test
    void testCreateEmpty() throws Exception {
        Workbook wb = WorkbookFactory.create(false);
        assertTrue(wb instanceof HSSFWorkbook);
        closeOrRevert(wb);

        wb = WorkbookFactory.create(true);
        assertTrue(wb instanceof XSSFWorkbook);
        closeOrRevert(wb);
    }

    @Test
    @SuppressForbidden("test code")
    void testOpenManyHSSF() throws Exception {
        final int size = 1000;
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        ArrayList<Future<Boolean>> futures = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            futures.add(executorService.submit(this::openHSSFFile));
        }
        for (Future<Boolean> future : futures) {
            assertTrue(future.get());
        }
    }

    @Test
    void testInvalidFormatException() throws IOException {
        String filename = "OPCCompliance_DerivedPartNameFAIL.docx";
        try (InputStream is = POIDataSamples.getOpenXML4JInstance().openResourceAsStream(filename)) {
            assertThrows(IOException.class, () -> WorkbookFactory.create(is));
        }
    }

    private boolean openHSSFFile() {
        try {
            // POIFS -> hssf
            try (InputStream is = HSSFTestDataSamples.openSampleFileStream(xls)) {
                Workbook wb = WorkbookFactory.create(new POIFSFileSystem(is));
                assertNotNull(wb);
                assertTrue(wb instanceof HSSFWorkbook);
                assertCloseDoesNotModifyFile(xls, wb);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
