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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.apache.poi.POITextExtractor;
import org.apache.poi.extractor.ExtractorFactory;

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
        EXPECTED_EXTRACTOR_FAILURES.add("hsmf/attachment_msg_pdf.msg");
        EXPECTED_EXTRACTOR_FAILURES.add("poifs/Notes.ole2");
        EXPECTED_EXTRACTOR_FAILURES.add("slideshow/testPPT.thmx");
    }

    public void handleExtracting(File file) throws Exception {
        POITextExtractor extractor = ExtractorFactory.createExtractor(file);
        try  {
            assertNotNull(extractor);

            assertNotNull(extractor.getText());
            
            // also try metadata
            POITextExtractor metadataExtractor = extractor.getMetadataTextExtractor();
            assertNotNull(metadataExtractor.getText());

            assertFalse("Expected Extraction to fail for file " + file + " and handler " + this + ", but did not fail!", 
                    EXPECTED_EXTRACTOR_FAILURES.contains(file));
        } catch (IllegalArgumentException e) {
            if(!EXPECTED_EXTRACTOR_FAILURES.contains(file)) {
                throw new Exception("While handling " + file, e);
            }
        } finally {
            extractor.close();
        }
    }
}
