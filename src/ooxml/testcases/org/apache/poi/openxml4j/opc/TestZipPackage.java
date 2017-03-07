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

package org.apache.poi.openxml4j.opc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import org.apache.poi.POIDataSamples;
import org.apache.poi.POITextExtractor;
import org.apache.poi.POIXMLException;
import org.apache.poi.extractor.ExtractorFactory;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.openxml4j.OpenXML4JTestDataSamples;
import org.apache.poi.openxml4j.exceptions.NotOfficeXmlFileException;
import org.apache.poi.openxml4j.exceptions.ODFNotOfficeXmlFileException;
import org.apache.poi.sl.usermodel.SlideShow;
import org.apache.poi.sl.usermodel.SlideShowFactory;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xwpf.usermodel.XWPFRelation;
import org.apache.xmlbeans.XmlException;
import org.junit.Test;

public class TestZipPackage {
    @Test
    public void testBug56479() throws Exception {
        InputStream is = OpenXML4JTestDataSamples.openSampleStream("dcterms_bug_56479.zip");
        OPCPackage p = OPCPackage.open(is);
        
        // Check we found the contents of it
        boolean foundCoreProps = false, foundDocument = false, foundTheme1 = false;
        for (final PackagePart part : p.getParts()) {
            final String partName = part.getPartName().toString();
            final String contentType = part.getContentType();
            if ("/docProps/core.xml".equals(partName)) {
                assertEquals(ContentTypes.CORE_PROPERTIES_PART, contentType);
                foundCoreProps = true;
            }
            if ("/word/document.xml".equals(partName)) {
                assertEquals(XWPFRelation.DOCUMENT.getContentType(), contentType);
                foundDocument = true;
            }
            if ("/word/theme/theme1.xml".equals(partName)) {
                assertEquals(XWPFRelation.THEME.getContentType(), contentType);
                foundTheme1 = true;
            }
        }
        assertTrue("Core not found in " + p.getParts(), foundCoreProps);
        assertFalse("Document should not be found in " + p.getParts(), foundDocument);
        assertFalse("Theme1 should not found in " + p.getParts(), foundTheme1);
        p.close();
        is.close();
    }

    @Test
    public void testZipEntityExpansionTerminates() throws IOException {
        try {
            Workbook wb = XSSFTestDataSamples.openSampleWorkbook("poc-xmlbomb.xlsx");
            wb.close();
            fail("Should catch exception due to entity expansion limitations");
        } catch (POIXMLException e) {
            assertEntityLimitReached(e);
        }
    }

    private void assertEntityLimitReached(Exception e) throws UnsupportedEncodingException {
        ByteArrayOutputStream str = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(str, "UTF-8"));
        try {
            e.printStackTrace(writer);
        } finally {
            writer.close();
        }
        String string = new String(str.toByteArray(), "UTF-8");
        assertTrue("Had: " + string, string.contains("The parser has encountered more than"));
    }

    @Test
    public void testZipEntityExpansionExceedsMemory() throws Exception {
        try {
            Workbook wb = WorkbookFactory.create(XSSFTestDataSamples.openSamplePackage("poc-xmlbomb.xlsx"));
            wb.close();
            fail("Should catch exception due to entity expansion limitations");
        } catch (POIXMLException e) {
            assertEntityLimitReached(e);
        }

        try {
            POITextExtractor extractor = ExtractorFactory.createExtractor(HSSFTestDataSamples.getSampleFile("poc-xmlbomb.xlsx"));
            try  {
                assertNotNull(extractor);
    
                try {
                    extractor.getText();
                } catch (IllegalStateException e) {
                    // expected due to shared strings expansion
                }
            } finally {
                extractor.close();
            }
        } catch (POIXMLException e) {
            assertEntityLimitReached(e);
        }
    }

    @Test
    public void testZipEntityExpansionSharedStringTable() throws Exception {
        Workbook wb = WorkbookFactory.create(XSSFTestDataSamples.openSamplePackage("poc-shared-strings.xlsx"));
        wb.close();
        
        POITextExtractor extractor = ExtractorFactory.createExtractor(HSSFTestDataSamples.getSampleFile("poc-shared-strings.xlsx"));
        try  {
            assertNotNull(extractor);

            try {
                extractor.getText();
            } catch (IllegalStateException e) {
                // expected due to shared strings expansion
            }
        } finally {
            extractor.close();
        }
    }

    @Test
    public void testZipEntityExpansionSharedStringTableEvents() throws Exception {
        boolean before = ExtractorFactory.getThreadPrefersEventExtractors();
        ExtractorFactory.setThreadPrefersEventExtractors(true);
        try {
            POITextExtractor extractor = ExtractorFactory.createExtractor(HSSFTestDataSamples.getSampleFile("poc-shared-strings.xlsx"));
            try  {
                assertNotNull(extractor);
    
                try {
                    extractor.getText();
                } catch (IllegalStateException e) {
                    // expected due to shared strings expansion
                }
            } finally {
                extractor.close();
            }
        } catch (XmlException e) {
            assertEntityLimitReached(e);
        } finally {
            ExtractorFactory.setThreadPrefersEventExtractors(before);
        }
    }
    
    @Test
    public void unparseableCentralDirectory() throws IOException {
        File f = OpenXML4JTestDataSamples.getSampleFile("at.pzp.www_uploads_media_PP_Scheinecker-jdk6error.pptx");
        SlideShow<?,?> ppt = SlideShowFactory.create(f, null, true);
        ppt.close();
    }

    @Test
    public void testClosingStreamOnException() throws IOException {
        InputStream is = OpenXML4JTestDataSamples.openSampleStream("dcterms_bug_56479.zip");
        File tmp = File.createTempFile("poi-test-truncated-zip", "");
        // create a corrupted zip file by truncating a valid zip file to the first 100 bytes
        OutputStream os = new FileOutputStream(tmp);
        for (int i = 0; i < 100; i++) {
            os.write(is.read());
        }
        os.flush();
        os.close();
        is.close();

        // feed the corrupted zip file to OPCPackage
        try {
            OPCPackage.open(tmp, PackageAccess.READ);
        } catch (Exception e) {
            // expected: the zip file is invalid
            // this test does not care if open() throws an exception or not.
        }
        // If the stream is not closed on exception, it will keep a file descriptor to tmp,
        // and requests to the OS to delete the file will fail.
        assertTrue("Can't delete tmp file", tmp.delete());
    }
    
    /**
     * If ZipPackage is passed an invalid file, a call to close
     *  (eg from the OPCPackage open method) should tidy up the
     *  stream / file the broken file is being read from.
     * See bug #60128 for more
     */
    @Test
    public void testTidyStreamOnInvalidFile() throws Exception {
        // Spreadsheet has a good mix of alternate file types
        POIDataSamples files = POIDataSamples.getSpreadSheetInstance();
        
        File[] notValidF = new File[] {
                files.getFile("SampleSS.ods"), files.getFile("SampleSS.txt")
        };
        InputStream[] notValidS = new InputStream[] {
                files.openResourceAsStream("SampleSS.ods"), files.openResourceAsStream("SampleSS.txt")
        };

        for (File notValid : notValidF) {
            ZipPackage pkg = new ZipPackage(notValid, PackageAccess.READ);
            assertNotNull(pkg.getZipArchive());
            assertFalse(pkg.getZipArchive().isClosed());
            try {
                pkg.getParts();
                fail("Shouldn't work");
            } catch (ODFNotOfficeXmlFileException e) {
            } catch (NotOfficeXmlFileException ne) {}
            pkg.close();
            
            assertNotNull(pkg.getZipArchive());
            assertTrue(pkg.getZipArchive().isClosed());
        }
        for (InputStream notValid : notValidS) {
            ZipPackage pkg = new ZipPackage(notValid, PackageAccess.READ);
            assertNotNull(pkg.getZipArchive());
            assertFalse(pkg.getZipArchive().isClosed());
            try {
                pkg.getParts();
                fail("Shouldn't work");
            } catch (ODFNotOfficeXmlFileException e) {
            } catch (NotOfficeXmlFileException ne) {}
            pkg.close();
            
            assertNotNull(pkg.getZipArchive());
            assertTrue(pkg.getZipArchive().isClosed());
        }
    }
}
