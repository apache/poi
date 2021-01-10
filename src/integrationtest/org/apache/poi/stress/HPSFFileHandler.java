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
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.poi.examples.hpsf.CopyCompare;
import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hpsf.HPSFPropertiesOnlyDocument;
import org.apache.poi.hpsf.PropertySet;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.TempFile;
import org.junit.jupiter.api.Test;

class HPSFFileHandler extends POIFSFileHandler {
    private static final String NL = System.getProperty("line.separator");

    private static File copyOutput;

    static final Set<String> EXCLUDES_HANDLE_ADD = unmodifiableHashSet(
        "spreadsheet/45290.xls",
        "spreadsheet/46904.xls",
        "spreadsheet/55982.xls",
        "spreadsheet/testEXCEL_3.xls",
        "spreadsheet/testEXCEL_4.xls",
        "hpsf/Test_Humor-Generation.ppt",
        "document/word2.doc"
    );

    static final Set<String> EXCLUDES_HANDLE_FILE = unmodifiableHashSet(
        "hpsf/Test_Humor-Generation.ppt",
        "slideshow/missing-moveto.ppt" // POIFS properties corrupted
    );


    private static Set<String> unmodifiableHashSet(String... a) {
        return Collections.unmodifiableSet(new HashSet<>(Arrays.asList(a)));
    }


    @Override
    public void handleFile(InputStream stream, String path) throws Exception {
        assumeFalse(EXCLUDES_HANDLE_FILE.contains(path));
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
	    if (!root.hasEntry(streamName)) {
	        return false;
	    }
        try (DocumentInputStream dis = root.createDocumentInputStream(streamName)) {
            return PropertySet.isPropertySetStream(dis);
        }
	}

    @Override
    public void handleAdditional(File file) throws Exception {
        assumeFalse(EXCLUDES_HANDLE_ADD.contains(file.getParentFile().getName()+"/"+file.getName()));
        if (copyOutput == null) {
            copyOutput = TempFile.createTempFile("hpsfCopy", "out");
            copyOutput.deleteOnExit();
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PrintStream psNew = new PrintStream(bos, true, "ISO-8859-1");
        PrintStream ps = System.out;
        try {
            System.setOut(psNew);
            CopyCompare.main(new String[]{file.getAbsolutePath(), copyOutput.getAbsolutePath()});
            assertEquals("Equal" + NL, bos.toString(StandardCharsets.UTF_8.name()));
        } finally {
            System.setOut(ps);
        }
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
	}

    // a test-case to test this locally without executing the full TestAllFiles
    @Test
    void testExtractor() {
        File file = new File("test-data/hpsf/TestBug44375.xls");
        assertDoesNotThrow(() -> handleExtracting(file));
    }
}
