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

import junit.framework.TestCase;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.HWPFTestDataSamples;
import org.apache.poi.hwpf.OldWordFileFormatException;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * Test the different routes to extracting text
 *
 * @author Nick Burch (nick at torchbox dot com)
 */
public final class TestWordExtractor extends TestCase {
	private String[] p_text1 = new String[] {
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
	private String p_text1_block = "";

	// Well behaved document
	private WordExtractor extractor;
	// Slightly iffy document
	private WordExtractor extractor2;
	// A word doc embeded in an excel file
	private String filename3;

	// With header and footer
	private String filename4;
	// With unicode header and footer
	private String filename5;
	// With footnote
	private String filename6;

	protected void setUp() throws Exception {

		String filename = "test2.doc";
		String filename2 = "test.doc";
		filename3 = "excel_with_embeded.xls";
		filename4 = "ThreeColHeadFoot.doc";
		filename5 = "HeaderFooterUnicode.doc";
		filename6 = "footnote.doc";
        POIDataSamples docTests = POIDataSamples.getDocumentInstance();
		extractor = new WordExtractor(docTests.openResourceAsStream(filename));
		extractor2 = new WordExtractor(docTests.openResourceAsStream(filename2));

		// Build splat'd out text version
		for(int i=0; i<p_text1.length; i++) {
			p_text1_block += p_text1[i];
		}
	}

	/**
	 * Test paragraph based extraction
	 */
	public void testExtractFromParagraphs() {
		String[] text = extractor.getParagraphText();

		assertEquals(p_text1.length, text.length);
		for (int i = 0; i < p_text1.length; i++) {
			assertEquals(p_text1[i], text[i]);
		}

		// Lots of paragraphs with only a few lines in them
		assertEquals(24, extractor2.getParagraphText().length);
		assertEquals("as d\r\n", extractor2.getParagraphText()[16]);
      assertEquals("as d\r\n", extractor2.getParagraphText()[17]);
      assertEquals("as d\r\n", extractor2.getParagraphText()[18]);
	}

	/**
	 * Test the paragraph -> flat extraction
	 */
	public void testGetText() {
		assertEquals(p_text1_block, extractor.getText());

		// For the 2nd, should give similar answers for
		//  the two methods, differing only in line endings
		assertEquals(
		      extractor2.getTextFromPieces().replaceAll("[\\r\\n]", ""), 
		      extractor2.getText().replaceAll("[\\r\\n]", ""));
	}

	/**
	 * Test textPieces based extraction
	 */
	public void testExtractFromTextPieces() {
		String text = extractor.getTextFromPieces();
		assertEquals(p_text1_block, text);
	}


	/**
	 * Test that we can get data from two different
	 *  embeded word documents
	 * @throws Exception
	 */
	public void testExtractFromEmbeded() throws Exception {
		POIFSFileSystem fs = new POIFSFileSystem(POIDataSamples.getSpreadSheetInstance().openResourceAsStream(filename3));
		HWPFDocument doc;
		WordExtractor extractor3;

		DirectoryNode dirA = (DirectoryNode) fs.getRoot().getEntry("MBD0000A3B7");
		DirectoryNode dirB = (DirectoryNode) fs.getRoot().getEntry("MBD0000A3B2");

		// Should have WordDocument and 1Table
		assertNotNull(dirA.getEntry("1Table"));
		assertNotNull(dirA.getEntry("WordDocument"));

		assertNotNull(dirB.getEntry("1Table"));
		assertNotNull(dirB.getEntry("WordDocument"));

		// Check each in turn
		doc = new HWPFDocument(dirA, fs);
		extractor3 = new WordExtractor(doc);

		assertNotNull(extractor3.getText());
		assertTrue(extractor3.getText().length() > 20);
		assertEquals("I am a sample document\r\nNot much on me\r\nI am document 1\r\n", extractor3
				.getText());
		assertEquals("Sample Doc 1", extractor3.getSummaryInformation().getTitle());
		assertEquals("Sample Test", extractor3.getSummaryInformation().getSubject());

		doc = new HWPFDocument(dirB, fs);
		extractor3 = new WordExtractor(doc);

		assertNotNull(extractor3.getText());
		assertTrue(extractor3.getText().length() > 20);
		assertEquals("I am another sample document\r\nNot much on me\r\nI am document 2\r\n",
				extractor3.getText());
		assertEquals("Sample Doc 2", extractor3.getSummaryInformation().getTitle());
		assertEquals("Another Sample Test", extractor3.getSummaryInformation().getSubject());
	}

	public void testWithHeader() {
		// Non-unicode
		HWPFDocument doc = HWPFTestDataSamples.openSampleFile(filename4);
		extractor = new WordExtractor(doc);

		assertEquals("First header column!\tMid header Right header!\n", extractor.getHeaderText());

		String text = extractor.getText();
		assertTrue(text.indexOf("First header column!") > -1);

		// Unicode
		doc = HWPFTestDataSamples.openSampleFile(filename5);
		extractor = new WordExtractor(doc);

		assertEquals("This is a simple header, with a \u20ac euro symbol in it.\n\n", extractor
				.getHeaderText());
		text = extractor.getText();
		assertTrue(text.indexOf("This is a simple header") > -1);
	}

	public void testWithFooter() {
		// Non-unicode
		HWPFDocument doc = HWPFTestDataSamples.openSampleFile(filename4);
		extractor = new WordExtractor(doc);

		assertEquals("Footer Left\tFooter Middle Footer Right\n", extractor.getFooterText());

		String text = extractor.getText();
		assertTrue(text.indexOf("Footer Left") > -1);

		// Unicode
		doc = HWPFTestDataSamples.openSampleFile(filename5);
		extractor = new WordExtractor(doc);

		assertEquals("The footer, with Moli\u00e8re, has Unicode in it.\n", extractor
				.getFooterText());
		text = extractor.getText();
		assertTrue(text.indexOf("The footer, with") > -1);
	}

	public void testFootnote() {
		HWPFDocument doc = HWPFTestDataSamples.openSampleFile(filename6);
		extractor = new WordExtractor(doc);

		String[] text = extractor.getFootnoteText();
		StringBuffer b = new StringBuffer();
		for (int i = 0; i < text.length; i++) {
			b.append(text[i]);
		}

		assertTrue(b.toString().contains("TestFootnote"));
	}

	public void testEndnote() {
		HWPFDocument doc = HWPFTestDataSamples.openSampleFile(filename6);
		extractor = new WordExtractor(doc);

		String[] text = extractor.getEndnoteText();
		StringBuffer b = new StringBuffer();
		for (int i = 0; i < text.length; i++) {
			b.append(text[i]);
		}

		assertTrue(b.toString().contains("TestEndnote"));
	}

	public void testComments() {
		HWPFDocument doc = HWPFTestDataSamples.openSampleFile(filename6);
		extractor = new WordExtractor(doc);

		String[] text = extractor.getCommentsText();
		StringBuffer b = new StringBuffer();
		for (int i = 0; i < text.length; i++) {
			b.append(text[i]);
		}

		assertTrue(b.toString().contains("TestComment"));
	}
	
	public void testWord95() throws Exception {
	    // Too old for the default
	    try {
    		extractor = new WordExtractor(
    				POIDataSamples.getDocumentInstance().openResourceAsStream("Word95.doc")
    		);
    		fail();
	    } catch(OldWordFileFormatException e) {}
		
		// Can work with the special one
	    Word6Extractor w6e = new Word6Extractor(
                POIDataSamples.getDocumentInstance().openResourceAsStream("Word95.doc")
        );
		String text = w6e.getText();
		
		assertTrue(text.contains("The quick brown fox jumps over the lazy dog"));
        assertTrue(text.contains("Paragraph 2"));
        assertTrue(text.contains("Paragraph 3. Has some RED text and some BLUE BOLD text in it"));
        assertTrue(text.contains("Last (4th) paragraph"));
        
        String[] tp = w6e.getParagraphText();
        assertEquals(7, tp.length);
        assertEquals("The quick brown fox jumps over the lazy dog\r\n", tp[0]);
        assertEquals("\r\n", tp[1]);
        assertEquals("Paragraph 2\r\n", tp[2]);
        assertEquals("\r\n", tp[3]);
        assertEquals("Paragraph 3. Has some RED text and some BLUE BOLD text in it.\r\n", tp[4]);
        assertEquals("\r\n", tp[5]);
        assertEquals("Last (4th) paragraph.\r\n", tp[6]);
	}
	
	public void testWord6() throws Exception {
        // Too old for the default
        try {
    		extractor = new WordExtractor(
    				POIDataSamples.getDocumentInstance().openResourceAsStream("Word6.doc")
    		);
            fail();
        } catch(OldWordFileFormatException e) {}
        
        Word6Extractor w6e = new Word6Extractor(
                POIDataSamples.getDocumentInstance().openResourceAsStream("Word6.doc")
        );
        String text = w6e.getText();
        
        assertTrue(text.contains("The quick brown fox jumps over the lazy dog"));
        
        String[] tp = w6e.getParagraphText();
        assertEquals(1, tp.length);
        assertEquals("The quick brown fox jumps over the lazy dog\r\n", tp[0]);
	}

    public void testFastSaved() throws Exception {
        extractor = new WordExtractor(
                POIDataSamples.getDocumentInstance().openResourceAsStream("rasp.doc")
        );

        String text = extractor.getText();
        assertTrue(text.contains("\u0425\u0425\u0425\u0425\u0425"));
        assertTrue(text.contains("\u0423\u0423\u0423\u0423\u0423"));
    }

    public void testFirstParagraphFix() throws Exception {
        extractor = new WordExtractor(
                POIDataSamples.getDocumentInstance().openResourceAsStream("MBD001D0B89.doc")
        );

        String text = extractor.getText();

        assertTrue(text.startsWith("\u041f\u0440\u0438\u043b\u043e\u0436\u0435\u043d\u0438\u0435"));
    }
}
