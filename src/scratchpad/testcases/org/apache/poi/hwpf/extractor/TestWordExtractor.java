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

package org.apache.poi.hwpf.extractor;

import static org.apache.poi.POITestCase.assertContains;
import static org.apache.poi.POITestCase.assertStartsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.POIDataSamples;
import org.apache.poi.extractor.POITextExtractor;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.HWPFTestDataSamples;
import org.apache.poi.hwpf.OldWordFileFormatException;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.Entry;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.StringUtil;
import org.junit.Test;

/**
 * Test the different routes to extracting text
 */
public final class TestWordExtractor {

    private static POIDataSamples docTests = POIDataSamples.getDocumentInstance();
    
    private static void assertEqualsTrim( String expected, String actual )
    {
        String newExpected = expected.replaceAll( "\r\n", "\n" )
                .replaceAll( "\r", "\n" ).trim();
        String newActual = actual.replaceAll( "\r\n", "\n" )
                .replaceAll( "\r", "\n" ).trim();
        assertEquals( newExpected, newActual );
    }
    
    private static void assertExtractedContains(String[] extracted, String needle) {
        String endnote = StringUtil.join(extracted, "");
        assertContains(endnote, needle);
    }

	private final String[] p_text1 = new String[] {
			"This is a simple word document\r\n",
			"\r\n",
			"It has a number of paragraphs in it\r\n",
			"\r\n",
			"Some of them even feature bold, italic and underlined text\r\n",
			"\r\n",
			"\r\n",
			"This bit is in a different font and size\r\n",
			"\r\n",
			"\r\n",
			"This bit features some red text.\r\n",
			"\r\n",
			"\r\n",
			"It is otherwise very very boring.\r\n"
	};

    // Build splat'd out text version
	private final String p_text1_block = StringUtil.join(p_text1, "");

	/**
	 * Test paragraph based extraction
	 */
	@Test
	public void testExtractFromParagraphs() throws IOException {
        WordExtractor extractor = openExtractor("test2.doc");
		String[] text = extractor.getParagraphText();

		assertEquals(p_text1.length, text.length);
		for (int i = 0; i < p_text1.length; i++) {
			assertEquals(p_text1[i], text[i]);
		}
        extractor.close();

		// Lots of paragraphs with only a few lines in them
        WordExtractor extractor2 = openExtractor("test.doc");
		assertEquals(24, extractor2.getParagraphText().length);
		assertEquals("as d\r\n", extractor2.getParagraphText()[16]);
		assertEquals("as d\r\n", extractor2.getParagraphText()[17]);
		assertEquals("as d\r\n", extractor2.getParagraphText()[18]);
		extractor2.close();
	}

	/**
	 * Test the paragraph -> flat extraction
	 */
    @Test
	public void testGetText() throws IOException {
        WordExtractor extractor = openExtractor("test2.doc");
        assertEqualsTrim(p_text1_block, extractor.getText());

        // For the 2nd, should give similar answers for
        // the two methods, differing only in line endings

        // nope, they must have different results, because of garbage
        // assertEquals(
        // extractor2.getTextFromPieces().replaceAll("[\\r\\n]", ""),
        // extractor2.getText().replaceAll("[\\r\\n]", ""));
		extractor.close();
    }

	/**
	 * Test textPieces based extraction
	 */
    @Test
	public void testExtractFromTextPieces() throws IOException {
        WordExtractor extractor = openExtractor("test2.doc");
		String text = extractor.getTextFromPieces();
		assertEquals(p_text1_block, text);
		extractor.close();
	}


	/**
	 * Test that we can get data from two different embedded word documents
	 */
    @Test
	public void testExtractFromEmbeded() throws IOException {
	    InputStream is = POIDataSamples.getSpreadSheetInstance().openResourceAsStream("excel_with_embeded.xls");
		POIFSFileSystem fs = new POIFSFileSystem(is);
		is.close();

		DirectoryNode dirA = (DirectoryNode) fs.getRoot().getEntry("MBD0000A3B7");
		DirectoryNode dirB = (DirectoryNode) fs.getRoot().getEntry("MBD0000A3B2");

		// Should have WordDocument and 1Table
		assertNotNull(dirA.getEntry("1Table"));
		assertNotNull(dirA.getEntry("WordDocument"));

		assertNotNull(dirB.getEntry("1Table"));
		assertNotNull(dirB.getEntry("WordDocument"));

		// Check each in turn
        HWPFDocument docA = new HWPFDocument(dirA);
		WordExtractor extractorA = new WordExtractor(docA);

		assertNotNull(extractorA.getText());
		assertTrue(extractorA.getText().length() > 20);
		assertEqualsTrim("I am a sample document\r\nNot much on me\r\nI am document 1\r\n", extractorA.getText());
		assertEquals("Sample Doc 1", extractorA.getSummaryInformation().getTitle());
		assertEquals("Sample Test", extractorA.getSummaryInformation().getSubject());

		HWPFDocument docB = new HWPFDocument(dirB);
		WordExtractor extractorB = new WordExtractor(docB);

		assertNotNull(extractorB.getText());
		assertTrue(extractorB.getText().length() > 20);
		assertEqualsTrim("I am another sample document\r\nNot much on me\r\nI am document 2\r\n", extractorB.getText());
		assertEquals("Sample Doc 2", extractorB.getSummaryInformation().getTitle());
		assertEquals("Another Sample Test", extractorB.getSummaryInformation().getSubject());

		extractorA.close();
		docA.close();

		extractorB.close();
		docB.close();
		
		fs.close();
	}

    @Test
	public void testWithHeader() throws IOException {
		// Non-unicode
		HWPFDocument doc1 = HWPFTestDataSamples.openSampleFile("ThreeColHeadFoot.doc");
		WordExtractor extractor1 = new WordExtractor(doc1);

        //noinspection deprecation
        assertEquals("First header column!\tMid header Right header!\n", extractor1.getHeaderText());
		assertContains(extractor1.getText(), "First header column!");
		extractor1.close();
		doc1.close();

		// Unicode
		HWPFDocument doc2 = HWPFTestDataSamples.openSampleFile("HeaderFooterUnicode.doc");
		WordExtractor extractor2 = new WordExtractor(doc2);

        //noinspection deprecation
        assertEquals("This is a simple header, with a \u20ac euro symbol in it.\n\n", extractor2.getHeaderText());
		assertContains(extractor2.getText(), "This is a simple header");
		extractor2.close();
		doc2.close();
	}

    @Test
	public void testWithFooter() throws IOException {
		// Non-unicode
		HWPFDocument doc1 = HWPFTestDataSamples.openSampleFile("ThreeColHeadFoot.doc");
		WordExtractor extractor1 = new WordExtractor(doc1);

        //noinspection deprecation
        assertEquals("Footer Left\tFooter Middle Footer Right\n", extractor1.getFooterText());
		assertContains(extractor1.getText(), "Footer Left");
        extractor1.close();
        doc1.close();

		// Unicode
		HWPFDocument doc2 = HWPFTestDataSamples.openSampleFile("HeaderFooterUnicode.doc");
		WordExtractor extractor2 = new WordExtractor(doc2);

        //noinspection deprecation
        assertEquals("The footer, with Moli\u00e8re, has Unicode in it.\n", extractor2.getFooterText());
		assertContains(extractor2.getText(), "The footer, with");
        extractor2.close();
        doc2.close();
	}

    @Test
	public void testFootnote() throws IOException {
		HWPFDocument doc = HWPFTestDataSamples.openSampleFile("footnote.doc");
		WordExtractor extractor = new WordExtractor(doc);

		assertExtractedContains(extractor.getFootnoteText(), "TestFootnote");
		assertEquals(0x00, doc.getRange().getSection(0).getFootnoteNumberingFormat()); // msonfcArabic
		assertEquals(0x00, doc.getRange().getSection(0).getFootnoteRestartQualifier()); // rncCont
		assertEquals(0, doc.getRange().getSection(0).getFootnoteNumberingOffset());	    
		assertEquals(1, doc.getFootnotes().getNotesCount());
		extractor.close();
		doc.close();
	}

    @Test
	public void testEndnote() throws IOException {
		HWPFDocument doc = HWPFTestDataSamples.openSampleFile("footnote.doc");
		WordExtractor extractor = new WordExtractor(doc);

		assertExtractedContains(extractor.getEndnoteText(), "TestEndnote");
		assertEquals(0x02, doc.getRange().getSection(0).getEndnoteNumberingFormat()); // msonfcLCRoman
		assertEquals(0x00, doc.getRange().getSection(0).getEndnoteRestartQualifier()); // rncCont
		assertEquals(0, doc.getRange().getSection(0).getEndnoteNumberingOffset()); 	   
		assertEquals(1, doc.getEndnotes().getNotesCount());
		extractor.close();
		doc.close();
	}

    @Test
	public void testComments() throws IOException {
		WordExtractor extractor = openExtractor("footnote.doc");
		assertExtractedContains(extractor.getCommentsText(), "TestComment");
		extractor.close();
	}
	
    @Test(expected=OldWordFileFormatException.class)
    public void testWord95_WordExtractor() throws Exception {
        // Too old for the default
        openExtractor("Word95.doc").close();
    }
    
    @Test
    public void testWord95() throws Exception {
        // Can work with the special one
        InputStream is = docTests.openResourceAsStream("Word95.doc");
        Word6Extractor w6e = new Word6Extractor(is);
        is.close();

        String text = w6e.getText();

        assertContains(text, "The quick brown fox jumps over the lazy dog");
        assertContains(text, "Paragraph 2");
        assertContains(text, "Paragraph 3. Has some RED text and some BLUE BOLD text in it");
        assertContains(text, "Last (4th) paragraph");
        
        @SuppressWarnings("deprecation")
        String[] tp = w6e.getParagraphText();
        assertEquals(7, tp.length);
        assertEquals("The quick brown fox jumps over the lazy dog\r\n", tp[0]);
        assertEquals("\r\n", tp[1]);
        assertEquals("Paragraph 2\r\n", tp[2]);
        assertEquals("\r\n", tp[3]);
        assertEquals("Paragraph 3. Has some RED text and some BLUE BOLD text in it.\r\n", tp[4]);
        assertEquals("\r\n", tp[5]);
        assertEquals("Last (4th) paragraph.\r\n", tp[6]);
        w6e.close();
    }

    @Test(expected=OldWordFileFormatException.class)
    public void testWord6_WordExtractor() throws IOException {
        // Too old for the default
        openExtractor("Word6.doc").close();
    }
    
    @Test
    public void testWord6() throws Exception {
        try (InputStream is = docTests.openResourceAsStream("Word6.doc");
            Word6Extractor w6e = new Word6Extractor(is)) {
            String text = w6e.getText();

            assertContains(text, "The quick brown fox jumps over the lazy dog");

            @SuppressWarnings("deprecation")
            String[] tp = w6e.getParagraphText();
            assertEquals(1, tp.length);
            assertEquals("The quick brown fox jumps over the lazy dog\r\n", tp[0]);
        }
    }

    @Test
    public void testFastSaved() throws Exception {
        WordExtractor extractor = openExtractor("rasp.doc");

        String text = extractor.getText();
        assertContains(text, "\u0425\u0425\u0425\u0425\u0425");
        assertContains(text, "\u0423\u0423\u0423\u0423\u0423");
        
        extractor.close();
    }

    @Test
    public void testFirstParagraphFix() throws Exception {
        WordExtractor extractor = openExtractor("Bug48075.doc");

        String text = extractor.getText();

        assertStartsWith(text, "\u041f\u0440\u0438\u043b\u043e\u0436\u0435\u043d\u0438\u0435");
        extractor.close();
    }
    
    /**
     * Tests that we can work with both {@link POIFSFileSystem}
     *  and {@link POIFSFileSystem}
     */
    @Test
    public void testDifferentPOIFS() throws Exception {
       // Open the two filesystems
       File file = docTests.getFile("test2.doc");
       try (POIFSFileSystem npoifs = new POIFSFileSystem(file, true)) {

           DirectoryNode dir = npoifs.getRoot();

           // Open directly
           @SuppressWarnings("resource")
           WordExtractor extractor1 = new WordExtractor(dir);
           assertEqualsTrim(p_text1_block, extractor1.getText());
           // extractor.close();

           // Open via a HWPFDocument
           try (HWPFDocument doc = new HWPFDocument(dir);
                WordExtractor extractor2 = new WordExtractor(doc)) {
               assertEqualsTrim(p_text1_block, extractor2.getText());
           }

       }
    }

    /**
     * [RESOLVED FIXED] Bug 51686 - Update to POI 3.8 beta 4 causes
     * ConcurrentModificationException in Tika's OfficeParser
     */
    @Test
    public void testBug51686() throws IOException {
        InputStream is = docTests.openResourceAsStream( "Bug51686.doc" );
        POIFSFileSystem fs = new POIFSFileSystem(is);
        is.close();

        String text = null;

        for (Entry entry : fs.getRoot()) {
            if ("WordDocument".equals(entry.getName())) {
                try (WordExtractor ex = new WordExtractor(fs)) {
                    text = ex.getText();
                }
            }
        }

        assertNotNull(text);
        fs.close();
    }

    @Test
    public void testExtractorFromWord6Extractor() throws Exception {
        try (InputStream is = POIDataSamples.getHPSFInstance().openResourceAsStream("TestMickey.doc");
             POIFSFileSystem fs = new POIFSFileSystem(is);
             Word6Extractor wExt = new Word6Extractor(fs);
             POITextExtractor ext = wExt.getMetadataTextExtractor()) {
            // Now overall
            String text = ext.getText();
            assertContains(text, "TEMPLATE = Normal");
            assertContains(text, "SUBJECT = sample subject");
            assertContains(text, "MANAGER = sample manager");
            assertContains(text, "COMPANY = sample company");
        }
    }
    
    private WordExtractor openExtractor(String fileName) throws IOException {
        try (InputStream is = docTests.openResourceAsStream(fileName)) {
            return new WordExtractor(is);
        }
    }
}
