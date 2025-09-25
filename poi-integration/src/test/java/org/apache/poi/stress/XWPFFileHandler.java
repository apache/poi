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
import java.util.Set;

import org.apache.commons.io.output.NullOutputStream;
import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Test;

public class XWPFFileHandler extends AbstractFileHandler {
    private static final Set<String> EXPECTED_FAILURES = StressTestUtils.unmodifiableHashSet(
            "document/truncated62886.docx", "document/ExternalEntityInText.docx"
    );

    @Override
    public void handleFile(InputStream stream, String path) throws Exception {
        // ignore password protected files
        if (POIXMLDocumentHandler.isEncrypted(stream)) return;

        if (StressTestUtils.excludeFile(path, EXPECTED_FAILURES)) return;

        try (XWPFDocument doc = new XWPFDocument(stream)) {
            new POIXMLDocumentHandler().handlePOIXMLDocument(doc);
            POIXMLDocumentHandler.cursorRecursive(doc.getDocument());

            doc.write(NullOutputStream.INSTANCE);
        } catch (POIXMLException e) {
            Exception cause = (Exception)e.getCause();
            throw cause == null ? e : cause;
        }
    }

    // a test-case to test this locally without executing the full TestAllFiles
    @Test
    @SuppressWarnings("java:S2699")
    void test() throws Exception {
        File file = new File("test-data/document/51921-Word-Crash067.docx");

        try (InputStream stream = new BufferedInputStream(new FileInputStream(file))) {
            handleFile(stream, file.getPath());
        }

        handleExtracting(file);
        handleAdditional(file);
    }
}