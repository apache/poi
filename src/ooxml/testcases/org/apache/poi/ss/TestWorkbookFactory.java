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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

import org.apache.poi.EmptyFileException;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.util.TempFile;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;

import junit.framework.TestCase;

public final class TestWorkbookFactory extends TestCase {
    private String xls;
    private String xlsx;
    private String[] xls_prot;
    private String[] xlsx_prot;
    private String txt;

    protected void setUp() {
        xls = "SampleSS.xls";
        xlsx = "SampleSS.xlsx";
        xls_prot = new String[] {"password.xls", "password"};
        xlsx_prot = new String[]{"protected_passtika.xlsx", "tika"};
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
        wb.close();

        // Package -> xssf
        wb = WorkbookFactory.create(
                OPCPackage.open(
                        HSSFTestDataSamples.openSampleFileStream(xlsx))
        );
        assertNotNull(wb);
        assertTrue(wb instanceof XSSFWorkbook);
        // TODO: this re-writes the sample-file?! wb.close();
    }

    public void testCreateReadOnly() throws Exception {
        Workbook wb;

        // POIFS -> hssf
        wb = WorkbookFactory.create(HSSFTestDataSamples.getSampleFile(xls), null, true);
        assertNotNull(wb);
        assertTrue(wb instanceof HSSFWorkbook);
        wb.close();

        // Package -> xssf
        wb = WorkbookFactory.create(HSSFTestDataSamples.getSampleFile(xlsx), null, true);
        assertNotNull(wb);
        assertTrue(wb instanceof XSSFWorkbook);
        wb.close();
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
        wb.close();

        wb = WorkbookFactory.create(
                HSSFTestDataSamples.openSampleFileStream(xlsx)
        );
        assertNotNull(wb);
        assertTrue(wb instanceof XSSFWorkbook);
        // TODO: this re-writes the sample-file?! wb.close();

        // File -> either
        wb = WorkbookFactory.create(
                HSSFTestDataSamples.getSampleFile(xls)
        );
        assertNotNull(wb);
        assertTrue(wb instanceof HSSFWorkbook);
        wb.close();

        wb = WorkbookFactory.create(
                HSSFTestDataSamples.getSampleFile(xlsx)
        );
        assertNotNull(wb);
        assertTrue(wb instanceof XSSFWorkbook);

        // TODO: close() re-writes the sample-file?! Resort to revert() for now to close file handle...
        ((XSSFWorkbook)wb).getPackage().revert();

        // Invalid type -> exception
        try {
            InputStream stream = HSSFTestDataSamples.openSampleFileStream(txt);
            try {
                wb = WorkbookFactory.create(stream);
            } finally {
                stream.close();
            }
            fail();
        } catch(InvalidFormatException e) {
            // Good
        }
    }

    /**
     * Check that the overloaded stream methods which take passwords work properly
     */
    public void testCreateWithPasswordFromStream() throws Exception {
        Workbook wb;


        // Unprotected, no password given, opens normally
        wb = WorkbookFactory.create(
                HSSFTestDataSamples.openSampleFileStream(xls), null
        );
        assertNotNull(wb);
        assertTrue(wb instanceof HSSFWorkbook);
        wb.close();

        wb = WorkbookFactory.create(
                HSSFTestDataSamples.openSampleFileStream(xlsx), null
        );
        assertNotNull(wb);
        assertTrue(wb instanceof XSSFWorkbook);


        // Unprotected, wrong password, opens normally
        wb = WorkbookFactory.create(
                HSSFTestDataSamples.openSampleFileStream(xls), "wrong"
        );
        assertNotNull(wb);
        assertTrue(wb instanceof HSSFWorkbook);
        wb.close();

        wb = WorkbookFactory.create(
                HSSFTestDataSamples.openSampleFileStream(xlsx), "wrong"
        );
        assertNotNull(wb);
        assertTrue(wb instanceof XSSFWorkbook);


        // Protected, correct password, opens fine
        wb = WorkbookFactory.create(
                HSSFTestDataSamples.openSampleFileStream(xls_prot[0]), xls_prot[1]
        );
        assertNotNull(wb);
        assertTrue(wb instanceof HSSFWorkbook);
        wb.close();

        wb = WorkbookFactory.create(
                HSSFTestDataSamples.openSampleFileStream(xlsx_prot[0]), xlsx_prot[1]
        );
        assertNotNull(wb);
        assertTrue(wb instanceof XSSFWorkbook);


        // Protected, wrong password, throws Exception
        try {
            wb = WorkbookFactory.create(
                    HSSFTestDataSamples.openSampleFileStream(xls_prot[0]), "wrong"
            );
            fail("Shouldn't be able to open with the wrong password");
        } catch (EncryptedDocumentException e) {}

        try {
            wb = WorkbookFactory.create(
                    HSSFTestDataSamples.openSampleFileStream(xlsx_prot[0]), "wrong"
            );
            fail("Shouldn't be able to open with the wrong password");
        } catch (EncryptedDocumentException e) {}
    }

    /**
     * Check that the overloaded file methods which take passwords work properly
     */
    public void testCreateWithPasswordFromFile() throws Exception {
        Workbook wb;

        // Unprotected, no password given, opens normally
        wb = WorkbookFactory.create(
                HSSFTestDataSamples.getSampleFile(xls), null
        );
        assertNotNull(wb);
        assertTrue(wb instanceof HSSFWorkbook);
        wb.close();

        wb = WorkbookFactory.create(
                HSSFTestDataSamples.getSampleFile(xlsx), null
        );
        assertNotNull(wb);
        assertTrue(wb instanceof XSSFWorkbook);


        // Unprotected, wrong password, opens normally
        wb = WorkbookFactory.create(
                HSSFTestDataSamples.getSampleFile(xls), "wrong"
        );
        assertNotNull(wb);
        assertTrue(wb instanceof HSSFWorkbook);
        wb.close();

        wb = WorkbookFactory.create(
                HSSFTestDataSamples.getSampleFile(xlsx), "wrong"
        );
        assertNotNull(wb);
        assertTrue(wb instanceof XSSFWorkbook);


        // Protected, correct password, opens fine
        wb = WorkbookFactory.create(
                HSSFTestDataSamples.getSampleFile(xls_prot[0]), xls_prot[1]
        );
        assertNotNull(wb);
        assertTrue(wb instanceof HSSFWorkbook);
        wb.close();

        wb = WorkbookFactory.create(
                HSSFTestDataSamples.getSampleFile(xlsx_prot[0]), xlsx_prot[1]
        );
        assertNotNull(wb);
        assertTrue(wb instanceof XSSFWorkbook);


        // Protected, wrong password, throws Exception
        try {
            wb = WorkbookFactory.create(
                    HSSFTestDataSamples.getSampleFile(xls_prot[0]), "wrong"
            );
            fail("Shouldn't be able to open with the wrong password");
        } catch (EncryptedDocumentException e) {}

        try {
            wb = WorkbookFactory.create(
                    HSSFTestDataSamples.getSampleFile(xlsx_prot[0]), "wrong"
            );
            fail("Shouldn't be able to open with the wrong password");
        } catch (EncryptedDocumentException e) {}
    }
    
    /**
     * Check that a helpful exception is given on an empty file / stream
     */
    public void testEmptyFile() throws Exception {
        InputStream emptyStream = new ByteArrayInputStream(new byte[0]);
        File emptyFile = TempFile.createTempFile("empty", ".poi");
        
        try {
            WorkbookFactory.create(emptyStream);
            fail("Shouldn't be able to create for an empty stream");
        } catch (EmptyFileException e) {
        }
        
        try {
            WorkbookFactory.create(emptyFile);
            fail("Shouldn't be able to create for an empty file");
        } catch (EmptyFileException e) {
        }
        emptyFile.delete();
    }
}
