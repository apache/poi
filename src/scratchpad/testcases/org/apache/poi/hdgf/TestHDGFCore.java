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

import junit.framework.TestCase;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hdgf.extractor.VisioTextExtractor;
import org.apache.poi.hdgf.streams.PointerContainingStream;
import org.apache.poi.hdgf.streams.TrailerStream;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

public final class TestHDGFCore extends TestCase {
    private static POIDataSamples _dgTests = POIDataSamples.getDiagramInstance();

    private POIFSFileSystem fs;
    private HDGFDiagram hdgf;
    private VisioTextExtractor textExtractor;

    @Override
    protected void setUp() throws Exception {
        fs = new POIFSFileSystem(_dgTests.openResourceAsStream("Test_Visio-Some_Random_Text.vsd"));
    }
    @Override
    protected void tearDown() throws Exception {
        if (textExtractor != null) textExtractor.close();
        if (hdgf != null) hdgf.close();
    }


    public void testCreate() throws Exception {
        hdgf = new HDGFDiagram(fs);
    }

    public void testTrailer() throws Exception {
        hdgf = new HDGFDiagram(fs);
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

    /**
     * Tests that we can open a problematic file, that used to
     *  break with a negative chunk length
     */
    public void testNegativeChunkLength() throws Exception {
        fs = new POIFSFileSystem(_dgTests.openResourceAsStream("NegativeChunkLength.vsd"));

        hdgf = new HDGFDiagram(fs);
        assertNotNull(hdgf);

        // And another file
        fs = new POIFSFileSystem(_dgTests.openResourceAsStream("NegativeChunkLength2.vsd"));
        hdgf = new HDGFDiagram(fs);
        assertNotNull(hdgf);
    }

    /**
     * Tests that we can open a problematic file that triggers
     *  an ArrayIndexOutOfBoundsException when processing the
     *  chunk commands.
     * @throws Exception
     */
    public void DISABLEDtestAIOOB() throws Exception {
        fs = new POIFSFileSystem(_dgTests.openResourceAsStream("44501.vsd"));

        hdgf = new HDGFDiagram(fs);
        assertNotNull(hdgf);
    }

    public void testV5() throws Exception {
        fs = new POIFSFileSystem(_dgTests.openResourceAsStream("v5_Connection_Types.vsd"));

        hdgf = new HDGFDiagram(fs);
        assertNotNull(hdgf);

        textExtractor = new VisioTextExtractor(hdgf);
        String text = textExtractor.getText().replace("\u0000", "").trim();

        assertEquals("Static to Static\nDynamic to Static\nDynamic to Dynamic", text);
    }

    public void testV6NonUtf16LE() throws Exception {
        fs = new POIFSFileSystem(_dgTests.openResourceAsStream("v6-non-utf16le.vsd"));

        hdgf = new HDGFDiagram(fs);
        assertNotNull(hdgf);

        textExtractor = new VisioTextExtractor(hdgf);
        String text = textExtractor.getText().replace("\u0000", "").trim();

        assertEquals("Table\n\n\nPropertySheet\n\n\n\nPropertySheetField", text);
    }

    public void testUtf16LE() throws Exception {
        fs = new POIFSFileSystem(_dgTests.openResourceAsStream("Test_Visio-Some_Random_Text.vsd"));

        hdgf = new HDGFDiagram(fs);
        assertNotNull(hdgf);

        textExtractor = new VisioTextExtractor(hdgf);
        String text = textExtractor.getText().trim();

        assertEquals("text\nView\nTest View\nI am a test view\nSome random text, on a page", text);
    }
}
