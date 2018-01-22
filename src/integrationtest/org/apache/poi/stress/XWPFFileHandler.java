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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.Test;

public class XWPFFileHandler extends AbstractFileHandler {
    @Override
    public void handleFile(InputStream stream, String path) throws Exception {
        // ignore password protected files
        if (POIXMLDocumentHandler.isEncrypted(stream)) return;

        XWPFDocument doc = new XWPFDocument(stream);
        
        new POIXMLDocumentHandler().handlePOIXMLDocument(doc);
    }

    // a test-case to test this locally without executing the full TestAllFiles
    @Test
    public void test() throws Exception {
        File file = new File("test-data/document/51921-Word-Crash067.docx");

        try (InputStream stream = new BufferedInputStream(new FileInputStream(file))) {
            handleFile(stream, file.getPath());
        }

        handleExtracting(file);
    }
}