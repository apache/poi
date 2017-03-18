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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.poi.POIOLE2TextExtractor;
import org.apache.poi.POITextExtractor;
import org.apache.poi.extractor.ExtractorFactory;
import org.apache.poi.hpsf.extractor.HPSFPropertiesExtractor;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.xmlbeans.XmlException;

public abstract class AbstractFileHandler implements FileHandler {
    public static final Set<String> EXPECTED_EXTRACTOR_FAILURES = new HashSet<String>();
    static {
        // password protected files
        EXPECTED_EXTRACTOR_FAILURES.add("document/bug53475-password-is-pass.docx");
        EXPECTED_EXTRACTOR_FAILURES.add("poifs/extenxls_pwd123.xlsx");
        EXPECTED_EXTRACTOR_FAILURES.add("poifs/protect.xlsx");
        EXPECTED_EXTRACTOR_FAILURES.add("poifs/protected_agile.docx");
        EXPECTED_EXTRACTOR_FAILURES.add("poifs/protected_sha512.xlsx");
        
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
        
        POITextExtractor extractor = ExtractorFactory.createExtractor(file);
        try  {
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
            	HPSFPropertiesExtractor hpsfExtractor = new HPSFPropertiesExtractor((POIOLE2TextExtractor)extractor);
            	try {
                	assertNotNull(hpsfExtractor.getDocumentSummaryInformationText());
                	assertNotNull(hpsfExtractor.getSummaryInformationText());
                	String text = hpsfExtractor.getText();
                	//System.out.println(text);
                	assertNotNull(text);
            	} finally {
            		hpsfExtractor.close();
            	}
            }
        } catch (IllegalArgumentException e) {
            if(!EXPECTED_EXTRACTOR_FAILURES.contains(file.getParentFile().getName() + "/" + file.getName())) {
                throw e;
            }
        } finally {
            extractor.close();
        }
    }

    private void handleExtractingAsStream(File file) throws IOException, OpenXML4JException, XmlException {
        InputStream stream = new FileInputStream(file);
        try {
            POITextExtractor streamExtractor = ExtractorFactory.createExtractor(stream);
            try {
                assertNotNull(streamExtractor);
   
                assertNotNull(streamExtractor.getText());
            } finally {
                streamExtractor.close();
            }
        } finally {
            stream.close();
        }
    }

    @Override
    public void handleAdditional(File file) throws Exception {
        // by default we do nothing here
    }
}
