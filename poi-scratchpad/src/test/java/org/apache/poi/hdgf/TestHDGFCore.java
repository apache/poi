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

package org.apache.poi.hdgf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hdgf.extractor.VisioTextExtractor;
import org.apache.poi.hdgf.streams.PointerContainingStream;
import org.apache.poi.hdgf.streams.TrailerStream;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.junit.jupiter.api.Test;

public final class TestHDGFCore {
    private static final POIDataSamples SAMPLES = POIDataSamples.getDiagramInstance();

    @Test
    void testCreate() throws Exception {
        try (POIFSFileSystem fs = openFS("Test_Visio-Some_Random_Text.vsd");
            HDGFDiagram hdgf = new HDGFDiagram(fs)) {
            assertNotNull(hdgf);
        }
    }

    @Test
    void testTrailer() throws Exception {
        try (POIFSFileSystem fs = openFS("Test_Visio-Some_Random_Text.vsd");
            HDGFDiagram hdgf = new HDGFDiagram(fs)) {
            assertNotNull(hdgf);
            assertNotNull(hdgf.getTrailerStream());

            // Check it has what we'd expect
            TrailerStream trailer = hdgf.getTrailerStream();
            assertEquals(0x8a94, trailer.getPointer().getOffset());

            assertNotNull(trailer.getPointedToStreams());
            assertEquals(20, trailer.getPointedToStreams().length);

            assertEquals(20, hdgf.getTopLevelStreams().length);

            // 9th one should have children
            assertNotNull(trailer.getPointedToStreams()[8]);
            assertNotNull(trailer.getPointedToStreams()[8].getPointer());
            PointerContainingStream ps8 = (PointerContainingStream)
                trailer.getPointedToStreams()[8];
            assertNotNull(ps8.getPointedToStreams());
            assertEquals(8, ps8.getPointedToStreams().length);
        }
    }

    /**
     * Tests that we can open a problematic file, that used to
     *  break with a negative chunk length
     */
    @Test
    void testNegativeChunkLength() throws Exception {
        try (POIFSFileSystem fs = openFS("NegativeChunkLength.vsd");
             HDGFDiagram hdgf = new HDGFDiagram(fs)) {
            assertNotNull(hdgf);
        }

        // And another file
        try (POIFSFileSystem fs = openFS("NegativeChunkLength2.vsd");
            HDGFDiagram hdgf = new HDGFDiagram(fs)) {
            assertNotNull(hdgf);
        }
    }

    /**
     * Tests that we can open a problematic file that triggers
     *  an ArrayIndexOutOfBoundsException when processing the
     *  chunk commands.
     */
    @Test
    void testAIOOB() throws Exception {
        try (POIFSFileSystem fs = openFS("44501.vsd");
             HDGFDiagram hdgf = new HDGFDiagram(fs)) {
            assertNotNull(hdgf);
        }
    }

    @Test
    void testV5() throws Exception {
        try (POIFSFileSystem fs = openFS("v5_Connection_Types.vsd");
             HDGFDiagram hdgf = new HDGFDiagram(fs)) {
            assertNotNull(hdgf);

            try (VisioTextExtractor textExtractor = new VisioTextExtractor(hdgf)) {
                String text = textExtractor.getText().replace("\u0000", "").trim();
                assertEquals("Static to Static\nDynamic to Static\nDynamic to Dynamic", text);
            }
        }
    }

    @Test
    void testV6NonUtf16LE() throws Exception {
        try (POIFSFileSystem fs = openFS("v6-non-utf16le.vsd");
             HDGFDiagram hdgf = new HDGFDiagram(fs)) {
            assertNotNull(hdgf);

            try (VisioTextExtractor textExtractor = new VisioTextExtractor(hdgf)) {
                String text = textExtractor.getText().replace("\u0000", "").trim();
                assertEquals("Table\n\n\nPropertySheet\n\n\n\nPropertySheetField", text);
            }
        }
    }

    @Test
    void testUtf16LE() throws Exception {
        try (POIFSFileSystem fs = openFS("Test_Visio-Some_Random_Text.vsd");
             HDGFDiagram hdgf = new HDGFDiagram(fs)) {
            assertNotNull(hdgf);

            try (VisioTextExtractor textExtractor = new VisioTextExtractor(hdgf)) {
                String text = textExtractor.getText().trim();
                assertEquals("text\nView\nTest View\nI am a test view\nSome random text, on a page", text);
            }
        }
    }

    private POIFSFileSystem openFS(String file) throws IOException {
        try (InputStream is = SAMPLES.openResourceAsStream(file)) {
            return new POIFSFileSystem(is);
        }
    }
}
