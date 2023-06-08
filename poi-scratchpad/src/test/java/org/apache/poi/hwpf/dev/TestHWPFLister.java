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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.PrintStream;
import java.util.Arrays;

import org.apache.poi.POIDataSamples;
import org.apache.commons.io.output.NullPrintStream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class TestHWPFLister {
    private static final POIDataSamples SAMPLES = POIDataSamples.getDocumentInstance();
    private static final String[] CLEAR_PROPS = {
        "org.apache.poi.hwpf.preserveBinTables",
        "org.apache.poi.hwpf.preserveTextTable"
    };

    @ParameterizedTest
    @ValueSource(strings = {
        "",
        " --dop --textPieces --textPiecesText --chpx --chpxProperties --chpxSprms --papx --papxProperties --papxSprms --paragraphs --paragraphsText --bookmarks --escher --fields --pictures --officeDrawings --styles --writereadback"
    })
    void main(String args) {
        String fileArgs = SAMPLES.getFile("SampleDoc.doc").getAbsolutePath() + args;

        PrintStream oldStdOut = System.out;
        System.setOut(NullPrintStream.INSTANCE);
        try {

            assertDoesNotThrow(() -> HWPFLister.main(fileArgs.split(" ")));

        } finally {
            System.setOut(oldStdOut);

            // the main-method sets these properties, we need to revert them here to not affect other tests
            Arrays.stream(CLEAR_PROPS).forEach(System::clearProperty);
        }
    }
}
