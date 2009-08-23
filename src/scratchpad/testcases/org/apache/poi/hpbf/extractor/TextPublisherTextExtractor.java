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

import java.io.File;
import java.io.FileInputStream;

import org.apache.poi.hpbf.HPBFDocument;
import org.apache.poi.POIDataSamples;

import junit.framework.TestCase;

public final class TextPublisherTextExtractor extends TestCase {
    private static final POIDataSamples _samples = POIDataSamples.getPublisherInstance();

	public void testBasics() throws Exception {
		HPBFDocument doc = new HPBFDocument(
				_samples.openResourceAsStream("Sample.pub")
		);

		PublisherTextExtractor ext =
			new PublisherTextExtractor(doc);
		ext.getText();

		ext = new PublisherTextExtractor(
                _samples.openResourceAsStream("Simple.pub")
		);
		ext.getText();
	}

	public void testContents() throws Exception {
		HPBFDocument doc = new HPBFDocument(
                _samples.openResourceAsStream("Sample.pub")
		);

		PublisherTextExtractor ext =
			new PublisherTextExtractor(doc);
		String text = ext.getText();

		assertEquals(
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
"Within doc to page 1\n"
				, text
		);

		// Now a simpler one
		ext = new PublisherTextExtractor(
                _samples.openResourceAsStream("Simple.pub")
		);
		text = ext.getText();
		assertEquals(
"0123456789\n" +
"0123456789abcdef\n" +
"0123456789abcdef0123456789abcdef\n" +
"0123456789\n" +
"0123456789abcdef\n" +
"0123456789abcdef0123456789abcdef\n" +
"0123456789abcdef0123456789abcdef0123456789abcdef\n"
				, text
		);
	}

	/**
	 * We have the same file saved for Publisher 98, Publisher
	 *  2000 and Publisher 2007. Check they all agree.
	 * @throws Exception
	 */
	public void testMultipleVersions() throws Exception {
		File f;
		HPBFDocument doc;

		doc = new HPBFDocument(
                _samples.openResourceAsStream("Sample.pub")
		);
		String s2007 = (new PublisherTextExtractor(doc)).getText();

		doc = new HPBFDocument(
                _samples.openResourceAsStream("Sample2000.pub")
		);
		String s2000 = (new PublisherTextExtractor(doc)).getText();

		doc = new HPBFDocument(
                _samples.openResourceAsStream("Sample98.pub")
		);
		String s98 = (new PublisherTextExtractor(doc)).getText();

		// Check they all agree
		assertEquals(s2007, s2000);
		assertEquals(s2007, s98);
	}

	/**
	 * Test that the hyperlink extraction stuff works as well
	 *  as we can hope it to.
	 */
	public void testWithHyperlinks() throws Exception {
		HPBFDocument doc = new HPBFDocument(
                _samples.openResourceAsStream("LinkAt10.pub")
		);

		PublisherTextExtractor ext =
			new PublisherTextExtractor(doc);
		ext.getText();

		// Default is no hyperlinks
		assertEquals("1234567890LINK\n", ext.getText());

		// Turn on
		ext.setHyperlinksByDefault(true);
		assertEquals("1234567890LINK\n<http://poi.apache.org/>\n", ext.getText());


		// Now a much more complex document
		ext = new PublisherTextExtractor(
                _samples.openResourceAsStream("Sample.pub")
        );
		ext.setHyperlinksByDefault(true);
		String text = ext.getText();

		assertTrue(text.endsWith(
				"<http://poi.apache.org/>\n" +
				"<C:\\Documents and Settings\\Nick\\My Documents\\Booleans.xlsx>\n" +
				"<>\n" +
				"<mailto:dev@poi.apache.org?subject=HPBF>\n" +
				"<mailto:dev@poi.apache.org?subject=HPBF>\n"
		));
	}
}
