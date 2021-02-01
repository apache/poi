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

package org.apache.poi.hpbf.extractor;

import static org.apache.poi.POITestCase.assertEndsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hpbf.HPBFDocument;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.junit.jupiter.api.Test;

public final class TestPublisherTextExtractor {
    private static final POIDataSamples _samples = POIDataSamples.getPublisherInstance();

    private static final String SAMPLE_TEXT =
        "This is some text on the first page\n" +
        "It\u2019s in times new roman, font size 10, all normal\n" +
        "" +
        "This is in bold and italic\n" +
        "It\u2019s Arial, 20 point font\n" +
        "It\u2019s in the second textbox on the first page\n" +
        "" +
        "This is the second page\n\n" +
        "" +
        "It is also times new roman, 10 point\n" +
        "" +
        "Table on page 2\nTop right\n" +
        "P2 table left\nP2 table right\n" +
        "Bottom Left\nBottom Right\n" +
        "" +
        "This text is on page two\n" +
        "#This is a link to Apache POI\n" +
        "More normal text\n" +
        "Link to a file\n" +
        "" +
        "More text, more hyperlinks\n" +
        "email link\n" +
        "Final hyperlink\n" +
        "Within doc to page 1\n";

    private static final String SIMPLE_TEXT =
        "0123456789\n" +
        "0123456789abcdef\n" +
        "0123456789abcdef0123456789abcdef\n" +
        "0123456789\n" +
        "0123456789abcdef\n" +
        "0123456789abcdef0123456789abcdef\n" +
        "0123456789abcdef0123456789abcdef0123456789abcdef\n";

    @Test
    void testBasics() throws IOException {
        InputStream sample = _samples.openResourceAsStream("Sample.pub");
        HPBFDocument doc = new HPBFDocument(sample);
        PublisherTextExtractor ext = new PublisherTextExtractor(doc);
        assertNotNull(ext.getText());
        ext.close();
        doc.close();
        sample.close();

        InputStream simple = _samples.openResourceAsStream("Simple.pub");
        ext = new PublisherTextExtractor(simple);
        assertNotNull(ext.getText());
        ext.close();
        simple.close();
    }

    @Test
    void testContents() throws IOException {
        // Check this complicated file using POIFS
        InputStream sample = _samples.openResourceAsStream("Sample.pub");
        HPBFDocument docOPOIFS = new HPBFDocument(sample);
        PublisherTextExtractor ext = new PublisherTextExtractor(docOPOIFS);
        assertEquals(SAMPLE_TEXT, ext.getText());
        ext.close();
        docOPOIFS.close();
        sample.close();

        // And with NPOIFS
        sample = _samples.openResourceAsStream("Sample.pub");
        POIFSFileSystem fs = new POIFSFileSystem(sample);
        HPBFDocument docPOIFS = new HPBFDocument(fs);
        ext = new PublisherTextExtractor(docPOIFS);
        assertEquals(SAMPLE_TEXT, ext.getText());
        ext.close();
        docPOIFS.close();
        fs.close();
        sample.close();

        // Now a simpler file
        InputStream simple = _samples.openResourceAsStream("Simple.pub");
        ext = new PublisherTextExtractor(simple);
        assertEquals(SIMPLE_TEXT, ext.getText());
        ext.close();
    }

    /**
     * We have the same file saved for Publisher 98, Publisher 2000 and
     * Publisher 2007. Check they all agree.
     */
    @Test
    void testMultipleVersions() throws Exception {
        InputStream sample = _samples.openResourceAsStream("Sample.pub");
        HPBFDocument doc = new HPBFDocument(sample);
        PublisherTextExtractor ext = new PublisherTextExtractor(doc);
        String s2007 = ext.getText();
        ext.close();
        doc.close();
        sample.close();

        InputStream sample2000 = _samples.openResourceAsStream("Sample2000.pub");
        doc = new HPBFDocument(sample2000);
        ext = new PublisherTextExtractor(doc);
        String s2000 = ext.getText();
        ext.close();
        doc.close();
        sample2000.close();

        InputStream sample98 = _samples.openResourceAsStream("Sample98.pub");
        doc = new HPBFDocument(sample98);
        ext = new PublisherTextExtractor(doc);
        String s98 = ext.getText();
        ext.close();
        doc.close();
        sample98.close();

        // Check they all agree
        assertEquals(s2007, s2000);
        assertEquals(s2007, s98);
    }

    /**
     * Test that the hyperlink extraction stuff works as well as we can hope it
     * to.
     */
    @Test
    void testWithHyperlinks() throws Exception {
        InputStream linkAt = _samples.openResourceAsStream("LinkAt10.pub");
        HPBFDocument doc = new HPBFDocument(linkAt);

        PublisherTextExtractor ext = new PublisherTextExtractor(doc);

        // Default is no hyperlinks
        assertEquals("1234567890LINK\n", ext.getText());

        // Turn on
        ext.setHyperlinksByDefault(true);
        assertEquals("1234567890LINK\n<http://poi.apache.org/>\n", ext.getText());
        ext.close();
        doc.close();
        linkAt.close();

        // Now a much more complex document
        InputStream sample = _samples.openResourceAsStream("Sample.pub");
        ext = new PublisherTextExtractor(sample);
        ext.setHyperlinksByDefault(true);
        String text = ext.getText();
        ext.close();
        sample.close();

        assertEndsWith(text, "<http://poi.apache.org/>\n"
                + "<C:\\Documents and Settings\\Nick\\My Documents\\Booleans.xlsx>\n"
                + "<>\n" + "<mailto:dev@poi.apache.org?subject=HPBF>\n"
                + "<mailto:dev@poi.apache.org?subject=HPBF>\n");
    }
}
