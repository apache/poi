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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import org.apache.poi.examples.hpsf.CopyCompare;
import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hpsf.HPSFPropertiesOnlyDocument;
import org.apache.poi.hpsf.PropertySet;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.hpsf.extractor.HPSFPropertiesExtractor;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.TempFile;
import org.junit.jupiter.api.Test;

public class HPSFFileHandler extends POIFSFileHandler {
    private static final ThreadLocal<File> copyOutput = ThreadLocal.withInitial(HPSFFileHandler::getTempFile);

    static final Set<String> EXCLUDES_HANDLE_ADD = StressTestUtils.unmodifiableHashSet(
        "spreadsheet/45290.xls",
        "spreadsheet/46904.xls",
        "spreadsheet/55982.xls",
        "spreadsheet/testEXCEL_3.xls",
        "spreadsheet/testEXCEL_4.xls",
        "hpsf/Test_Humor-Generation.ppt",
        "document/word2.doc"
    );

    @Override
    public void handleExtracting(File file) throws Exception {
        if (!Boolean.getBoolean("scratchpad.ignore")) {
            super.handleExtracting(file);
            return;
        }

        long length = file.length();
        long modified = file.lastModified();

        try (POIFSFileSystem poifs = new POIFSFileSystem(file);
             HPSFPropertiesExtractor extractor = new HPSFPropertiesExtractor(poifs)) {

            String fileAndParentName = file.getParentFile().getName() + "/" + file.getName();
            String relPath = file.getPath().replaceAll(".*test-data", "test-data").replace('\\', '/');

            assertFalse(EXPECTED_EXTRACTOR_FAILURES.contains(fileAndParentName),
                "Expected Extraction to fail for file " + relPath + " and handler " + this + ", but did not fail!");
            assertNotNull(extractor.getDocumentSummaryInformationText());
            assertNotNull(extractor.getSummaryInformationText());
            String text = extractor.getText();
            //System.out.println(text);
            assertNotNull(text);
        }

        assertEquals(length, file.length(), "File should not be modified by extractor");
        assertEquals(modified, file.lastModified(), "File should not be modified by extractor");
    }

    @Override
    public void handleFile(InputStream stream, String path) throws Exception {
        POIFSFileSystem poifs = new POIFSFileSystem(stream);
        HPSFPropertiesOnlyDocument hpsf = new HPSFPropertiesOnlyDocument(poifs);
        DocumentSummaryInformation dsi = hpsf.getDocumentSummaryInformation();
        SummaryInformation si = hpsf.getSummaryInformation();
        boolean hasDSI = hasPropertyStream(poifs, DocumentSummaryInformation.DEFAULT_STREAM_NAME);
        boolean hasSI = hasPropertyStream(poifs, SummaryInformation.DEFAULT_STREAM_NAME);

        assertEquals(hasDSI, dsi != null);
        assertEquals(hasSI, si != null);

        handlePOIDocument(hpsf);
    }

    private static boolean hasPropertyStream(POIFSFileSystem poifs, String streamName) throws IOException {
        DirectoryNode root = poifs.getRoot();
        if (!root.hasEntryCaseInsensitive(streamName)) {
            return false;
        }
        try (DocumentInputStream dis = root.createDocumentInputStream(streamName)) {
            return PropertySet.isPropertySetStream(dis);
        }
    }

    private static File getTempFile() {
        File f = null;
        try {
            f = TempFile.createTempFile("hpsfCopy", "out");
        } catch (IOException e) {
            fail(e);
        }
        f.deleteOnExit();
        return f;
    }

    @Override
    public void handleAdditional(File file) throws Exception {
        assumeFalse(EXCLUDES_HANDLE_ADD.contains(file.getParentFile().getName()+"/"+file.getName()));

        boolean result = CopyCompare.compare(file.getAbsolutePath(), copyOutput.get().getAbsolutePath());
        assertTrue(result);
    }


    // a test-case to test this locally without executing the full TestAllFiles
    @Override
    @Test
    @SuppressWarnings("java:S2699")
    void test() throws Exception {
        String path = "test-data/diagram/44501.vsd";
        try (InputStream stream = new FileInputStream(path)) {
            handleFile(stream, path);
        }

        File file = new File(path);
        assertDoesNotThrow(() -> handleExtracting(file));

        handleAdditional(file);
    }
}
