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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeFalse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.extractor.POIOLE2TextExtractor;
import org.apache.poi.extractor.POITextExtractor;
import org.apache.poi.ooxml.extractor.ExtractorFactory;
import org.apache.poi.hpsf.extractor.HPSFPropertiesExtractor;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.util.IOUtils;
import org.apache.xmlbeans.XmlException;

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
        EXPECTED_EXTRACTOR_FAILURES.add("hpsf/Test0313rur.adm");
        EXPECTED_EXTRACTOR_FAILURES.add("poifs/Notes.ole2");
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
        try  {
            extractor = ExtractorFactory.createExtractor(file);
            assertNotNull("Should get a POITextExtractor but had none for file " + file, extractor);

            assertNotNull("Should get some text but had none for file " + file, extractor.getText());
            
            // also try metadata
            @SuppressWarnings("resource")
            POITextExtractor metadataExtractor = extractor.getMetadataTextExtractor();
            assertNotNull(metadataExtractor.getText());

            assertFalse("Expected Extraction to fail for file " + file + " and handler " + this + ", but did not fail!", 
                    EXPECTED_EXTRACTOR_FAILURES.contains(file.getParentFile().getName() + "/" + file.getName()));
            
            assertEquals("File should not be modified by extractor", length, file.length());
            assertEquals("File should not be modified by extractor", modified, file.lastModified());
            
            handleExtractingAsStream(file);
            
            if(extractor instanceof POIOLE2TextExtractor) {
                try (HPSFPropertiesExtractor hpsfExtractor = new HPSFPropertiesExtractor((POIOLE2TextExtractor) extractor)) {
                    assertNotNull(hpsfExtractor.getDocumentSummaryInformationText());
                    assertNotNull(hpsfExtractor.getSummaryInformationText());
                    String text = hpsfExtractor.getText();
                    //System.out.println(text);
                    assertNotNull(text);
                }
            }
        } catch (IllegalArgumentException e) {
            if(!EXPECTED_EXTRACTOR_FAILURES.contains(file.getParentFile().getName() + "/" + file.getName())) {
                throw e;
            }
        } catch (EncryptedDocumentException e) {
            String msg = "org.apache.poi.EncryptedDocumentException: Export Restrictions in place - please install JCE Unlimited Strength Jurisdiction Policy files";
            assumeFalse(msg.equals(e.getMessage()));
            throw e;
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
