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
package org.apache.poi.stress;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.extractor.ExtractorFactory;
import org.apache.poi.extractor.POIOLE2TextExtractor;
import org.apache.poi.extractor.POITextExtractor;
import org.apache.poi.hpsf.extractor.HPSFPropertiesExtractor;
import org.apache.poi.hssf.extractor.EventBasedExcelExtractor;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.ss.extractor.ExcelExtractor;
import org.apache.poi.util.IOUtils;
import org.apache.xmlbeans.XmlException;

/**
 * Base class with things that can be run for any supported file handler
 * in the integration tests, mostly text-extraction related at the moment.
 */
public abstract class AbstractFileHandler implements FileHandler {
    public static final Set<String> EXPECTED_EXTRACTOR_FAILURES = new HashSet<>();
    static {
        // password protected files without password
    	// ... currently none ...

        // unsupported file-types, no supported OLE2 parts
        EXPECTED_EXTRACTOR_FAILURES.add("hmef/quick-winmail.dat");
        EXPECTED_EXTRACTOR_FAILURES.add("hmef/winmail-sample1.dat");
        EXPECTED_EXTRACTOR_FAILURES.add("hmef/bug52400-winmail-simple.dat");
        EXPECTED_EXTRACTOR_FAILURES.add("hmef/bug52400-winmail-with-attachments.dat");
        EXPECTED_EXTRACTOR_FAILURES.add("hmef/bug63955-winmail.dat");
        EXPECTED_EXTRACTOR_FAILURES.add("hpsf/Test0313rur.adm");
        EXPECTED_EXTRACTOR_FAILURES.add("poifs/Notes.ole2");
        EXPECTED_EXTRACTOR_FAILURES.add("poifs/64322.ole2");
    }

    @Override
    public void handleExtracting(File file) throws Exception {
        boolean before = ExtractorFactory.getThreadPrefersEventExtractors();
        try {
            ExtractorFactory.setThreadPrefersEventExtractors(true);
            handleExtractingInternal(file);

            ExtractorFactory.setThreadPrefersEventExtractors(false);
            handleExtractingInternal(file);
        } finally {
            ExtractorFactory.setThreadPrefersEventExtractors(before);
        }

        /* Did fail for some documents with special XML contents...
        try {
            OOXMLPrettyPrint.main(new String[] { file.getAbsolutePath(),
            		"/tmp/pretty-" + file.getName() });
        } catch (ZipException e) {
        	// ignore, not a Zip/OOXML file
        }*/
    }

    private void handleExtractingInternal(File file) throws Exception {
        long length = file.length();
        long modified = file.lastModified();

        POITextExtractor extractor = null;
        String fileAndParentName = file.getParentFile().getName() + "/" + file.getName();
        try {
            extractor = ExtractorFactory.createExtractor(file);
            assertNotNull(extractor, "Should get a POITextExtractor but had none for file " + file);

            assertNotNull(extractor.getText(), "Should get some text but had none for file " + file);

            // also try metadata
            @SuppressWarnings("resource")
            POITextExtractor metadataExtractor = extractor.getMetadataTextExtractor();
            assertNotNull(metadataExtractor.getText());

            assertFalse(EXPECTED_EXTRACTOR_FAILURES.contains(fileAndParentName),
                "Expected Extraction to fail for file " + file + " and handler " + this + ", but did not fail!");

            assertEquals(length, file.length(), "File should not be modified by extractor");
            assertEquals(modified, file.lastModified(), "File should not be modified by extractor");

            handleExtractingAsStream(file);

            if (extractor instanceof POIOLE2TextExtractor) {
                try (HPSFPropertiesExtractor hpsfExtractor = new HPSFPropertiesExtractor((POIOLE2TextExtractor) extractor)) {
                    assertNotNull(hpsfExtractor.getDocumentSummaryInformationText());
                    assertNotNull(hpsfExtractor.getSummaryInformationText());
                    String text = hpsfExtractor.getText();
                    //System.out.println(text);
                    assertNotNull(text);
                }
            }

            // test again with including formulas and cell-comments as this caused some bugs
            if(extractor instanceof ExcelExtractor &&
                    // comment-extraction and formula extraction are not well supported in event based extraction
                    !(extractor instanceof EventBasedExcelExtractor)) {
                ((ExcelExtractor)extractor).setFormulasNotResults(true);

                String text = extractor.getText();
                assertNotNull(text);
                // */

                ((ExcelExtractor) extractor).setIncludeCellComments(true);

                text = extractor.getText();
                assertNotNull(text);
            }
        } catch (IllegalArgumentException e) {
            if(!EXPECTED_EXTRACTOR_FAILURES.contains(fileAndParentName)) {
                throw e;
            }
        } catch (EncryptedDocumentException e) {
            String msg = "org.apache.poi.EncryptedDocumentException: Export Restrictions in place - please install JCE Unlimited Strength Jurisdiction Policy files";
            assumeFalse(msg.equals(e.getMessage()));
            throw e;
        } catch (IllegalStateException e) {
            if (!e.getMessage().contains("POI Scratchpad jar missing") || !Boolean.getBoolean("scratchpad.ignore")) {
                throw e;
            }
        } finally {
            IOUtils.closeQuietly(extractor);
        }
    }

    private void handleExtractingAsStream(File file) throws IOException, OpenXML4JException, XmlException {
        try (InputStream stream = new FileInputStream(file)) {
            try (POITextExtractor streamExtractor = ExtractorFactory.createExtractor(stream)) {
                assertNotNull(streamExtractor);

                assertNotNull(streamExtractor.getText());
            }
        }
    }

    @Override
    public void handleAdditional(File file) throws Exception {
        // by default we do nothing here
    }
}
