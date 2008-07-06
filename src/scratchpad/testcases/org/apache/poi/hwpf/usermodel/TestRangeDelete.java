
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

package org.apache.poi.hwpf.usermodel;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.util.List;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.model.PicturesTable;
import org.apache.poi.hwpf.usermodel.Picture;

import junit.framework.TestCase;

/**
 *	Test to see if Range.delete() works even if the Range contains a
 *	CharacterRun that uses Unicode characters.
 */
public class TestRangeDelete extends TestCase {

	// u201c and u201d are "smart-quotes"
	private String originalText =
		"It is used to confirm that text delete works even if Unicode characters (such as \u201c\u2014\u201d (U+2014), \u201c\u2e8e\u201d (U+2E8E), or \u201c\u2714\u201d (U+2714)) are present.  Everybody should be thankful to the ${organization} ${delete} and all the POI contributors for their assistance in this matter.\r";
	private String searchText = "${delete}";
	private String expectedText1 = " This is an MS-Word 97 formatted document created using NeoOffice v. 2.2.4 Patch 0 (OpenOffice.org v. 2.2.1).\r";
	private String expectedText2 =
		"It is used to confirm that text delete works even if Unicode characters (such as \u201c\u2014\u201d (U+2014), \u201c\u2e8e\u201d (U+2E8E), or \u201c\u2714\u201d (U+2714)) are present.  Everybody should be thankful to the ${organization}  and all the POI contributors for their assistance in this matter.\r";
	private String expectedText3 = "Thank you, ${organization} !\r";

	private String illustrativeDocFile;

	protected void setUp() throws Exception {

		String dirname = System.getProperty("HWPF.testdata.path");

		illustrativeDocFile = dirname + "/testRangeDelete.doc";
	}

	/**
	 * Test just opening the files
	 */
	public void testOpen() throws Exception {

		HWPFDocument docA = new HWPFDocument(new FileInputStream(illustrativeDocFile));
	}

	/**
	 * Test (more "confirm" than test) that we have the general structure that we expect to have.
	 */
	public void testDocStructure() throws Exception {

		HWPFDocument daDoc = new HWPFDocument(new FileInputStream(illustrativeDocFile));

		Range range = daDoc.getRange();

		assertEquals(1, range.numSections());
		Section section = range.getSection(0);

		assertEquals(5, section.numParagraphs());
		Paragraph para = section.getParagraph(2);

		assertEquals(5, para.numCharacterRuns());

		assertEquals(originalText, para.text());
	}

	/**
	 * Test that we can delete text (one instance) from our Range with Unicode text.
	 */
	public void testRangeDeleteOne() throws Exception {

		HWPFDocument daDoc = new HWPFDocument(new FileInputStream(illustrativeDocFile));

		Range range = daDoc.getRange();
		assertEquals(1, range.numSections());

		Section section = range.getSection(0);
		assertEquals(5, section.numParagraphs());

		Paragraph para = section.getParagraph(2);

		String text = para.text();
		assertEquals(originalText, text);

		int offset = text.indexOf(searchText);
		assertEquals(192, offset);

		int absOffset = para.getStartOffset() + offset;
		if (para.usesUnicode())
			absOffset = para.getStartOffset() + (offset * 2);

		Range subRange = new Range(absOffset, (absOffset + searchText.length()), para.getDocument());
		if (subRange.usesUnicode())
			subRange = new Range(absOffset, (absOffset + (searchText.length() * 2)), para.getDocument());

		assertEquals(searchText, subRange.text());

		subRange.delete();

		// we need to let the model re-calculate the Range before we evaluate it
		range = daDoc.getRange();

		assertEquals(1, range.numSections());
		section = range.getSection(0);

		assertEquals(5, section.numParagraphs());
		para = section.getParagraph(2);

		text = para.text();
		assertEquals(expectedText2, text);

		// this can lead to a StringBufferOutOfBoundsException, so we will add it
		// even though we don't have an assertion for it
		Range daRange = daDoc.getRange();
		daRange.text();
	}

	/**
	 * Test that we can delete text (all instances of) from our Range with Unicode text.
	 */
	public void testRangeDeleteAll() throws Exception {

		HWPFDocument daDoc = new HWPFDocument(new FileInputStream(illustrativeDocFile));

		Range range = daDoc.getRange();
		assertEquals(1, range.numSections());

		Section section = range.getSection(0);
		assertEquals(5, section.numParagraphs());

		Paragraph para = section.getParagraph(2);

		String text = para.text();
		assertEquals(originalText, text);

		boolean keepLooking = true;
		while (keepLooking) {

			int offset = range.text().indexOf(searchText);
			if (offset >= 0) {

				int absOffset = range.getStartOffset() + offset;
				if (range.usesUnicode())
					absOffset = range.getStartOffset() + (offset * 2);

				Range subRange = new Range(
					absOffset, (absOffset + searchText.length()), range.getDocument());
				if (subRange.usesUnicode())
					subRange = new Range(
						absOffset, (absOffset + (searchText.length() * 2)), range.getDocument());

				assertEquals(searchText, subRange.text());

				subRange.delete();

			} else
				keepLooking = false;
		}

		// we need to let the model re-calculate the Range before we use it
		range = daDoc.getRange();

		assertEquals(1, range.numSections());
		section = range.getSection(0);

		assertEquals(5, section.numParagraphs());

		para = section.getParagraph(1);
		text = para.text();
		assertEquals(expectedText1, text);

		para = section.getParagraph(2);
		text = para.text();
		assertEquals(expectedText2, text);

		para = section.getParagraph(3);
		text = para.text();
		assertEquals(expectedText3, text);
	}
}
