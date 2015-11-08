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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import org.apache.poi.POITextExtractor;
import org.apache.poi.POIXMLException;
import org.apache.poi.extractor.ExtractorFactory;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.openxml4j.OpenXML4JTestDataSamples;
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
        for (PackagePart part : p.getParts()) {
            if (part.getPartName().toString().equals("/docProps/core.xml")) {
                assertEquals(ContentTypes.CORE_PROPERTIES_PART, part.getContentType());
                foundCoreProps = true;
            }
            if (part.getPartName().toString().equals("/word/document.xml")) {
                assertEquals(XWPFRelation.DOCUMENT.getContentType(), part.getContentType());
                foundDocument = true;
            }
            if (part.getPartName().toString().equals("/word/theme/theme1.xml")) {
                assertEquals(XWPFRelation.THEME.getContentType(), part.getContentType());
                foundTheme1 = true;
            }
        }
        assertTrue("Core not found in " + p.getParts(), foundCoreProps);
        assertFalse("Document should not be found in " + p.getParts(), foundDocument);
        assertFalse("Theme1 should not found in " + p.getParts(), foundTheme1);
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
        assertTrue("Had: " + string, string.contains("Exceeded Entity dereference bytes limit"));
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
}
