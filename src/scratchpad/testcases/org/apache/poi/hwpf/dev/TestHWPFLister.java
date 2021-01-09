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
package org.apache.poi.hwpf.dev;

import java.io.File;
import java.io.PrintStream;

import org.apache.poi.POIDataSamples;
import org.apache.poi.util.NullPrintStream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TestHWPFLister {
    private static PrintStream oldStdOut;

    @BeforeAll
    public static void muteStdout() {
        oldStdOut = System.out;
        System.setOut(new NullPrintStream());
    }

    @AfterAll
    public static void restoreStdout() {
        System.setOut(oldStdOut);
    }

    @AfterEach
    void tearDown() {
        // the main-method sets these properties, we need to revert them here to not affect other tests
        System.clearProperty("org.apache.poi.hwpf.preserveBinTables");
        System.clearProperty("org.apache.poi.hwpf.preserveTextTable");
    }

    @Test
    void main() throws Exception {
        File file = POIDataSamples.getDocumentInstance().getFile("SampleDoc.doc");
        HWPFLister.main(new String[] { file.getAbsolutePath() });
    }

    @Test
    void mainAll() throws Exception {
        File file = POIDataSamples.getDocumentInstance().getFile("SampleDoc.doc");
        HWPFLister.main(new String[] {
            file.getAbsolutePath(),
                "--dop", "--textPieces", "--textPiecesText",
                "--chpx", "--chpxProperties", "--chpxSprms",
                "--papx", "--papxProperties", "--papxSprms",
                "--paragraphs", "--paragraphsText",
                "--bookmarks", "--escher",
                "--fields", "--pictures",
                "--officeDrawings", "--styles",
                "--writereadback"
        });
    }
}
