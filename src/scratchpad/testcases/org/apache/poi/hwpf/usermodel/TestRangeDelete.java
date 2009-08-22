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

import junit.framework.TestCase;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.HWPFTestDataSamples;
import org.apache.poi.hwpf.model.PAPX;

/**
 *	Test to see if Range.delete() works even if the Range contains a
 *	CharacterRun that uses Unicode characters.
 */
public final class TestRangeDelete extends TestCase {

	// u201c and u201d are "smart-quotes"
	private String introText =
		"Introduction\r";
	private String fillerText =
		"${delete} This is an MS-Word 97 formatted document created using NeoOffice v. 2.2.4 Patch 0 (OpenOffice.org v. 2.2.1).\r";
	private String originalText =
		"It is used to confirm that text delete works even if Unicode characters (such as \u201c\u2014\u201d (U+2014), \u201c\u2e8e\u201d (U+2E8E), or \u201c\u2714\u201d (U+2714)) are present.  Everybody should be thankful to the ${organization} ${delete} and all the POI contributors for their assistance in this matter.\r";
	private String lastText =
		"Thank you, ${organization} ${delete}!\r";
	private String searchText = "${delete}";
	private String expectedText1 = " This is an MS-Word 97 formatted document created using NeoOffice v. 2.2.4 Patch 0 (OpenOffice.org v. 2.2.1).\r";
	private String expectedText2 =
		"It is used to confirm that text delete works even if Unicode characters (such as \u201c\u2014\u201d (U+2014), \u201c\u2e8e\u201d (U+2E8E), or \u201c\u2714\u201d (U+2714)) are present.  Everybody should be thankful to the ${organization}  and all the POI contributors for their assistance in this matter.\r";
	private String expectedText3 = "Thank you, ${organization} !\r";

	private String illustrativeDocFile;

	protected void setUp() {
		illustrativeDocFile = "testRangeDelete.doc";
	}

	/**
	 * Test just opening the files
	 */
	public void testOpen() {

		HWPFTestDataSamples.openSampleFile(illustrativeDocFile);
	}

	/**
	 * Test (more "confirm" than test) that we have the general structure that we expect to have.
	 */
	public void testDocStructure() {

		HWPFDocument daDoc = HWPFTestDataSamples.openSampleFile(illustrativeDocFile);
		Range range;
		Section section;
		Paragraph para;
		PAPX paraDef;

		// First, check overall
		range = daDoc.getOverallRange();
		assertEquals(1, range.numSections());
		assertEquals(5, range.numParagraphs());


		// Now, onto just the doc bit
		range = daDoc.getRange();

		assertEquals(1, range.numSections());
		assertEquals(1, daDoc.getSectionTable().getSections().size());
		section = range.getSection(0);

		assertEquals(5, section.numParagraphs());

		para = section.getParagraph(0);
		assertEquals(1, para.numCharacterRuns());
		assertEquals(introText, para.text());

		para = section.getParagraph(1);
		assertEquals(5, para.numCharacterRuns());
		assertEquals(fillerText, para.text());


		paraDef = (PAPX)daDoc.getParagraphTable().getParagraphs().get(2);
		assertEquals(132, paraDef.getStart());
		assertEquals(400, paraDef.getEnd());

		para = section.getParagraph(2);
		assertEquals(5, para.numCharacterRuns());
		assertEquals(originalText, para.text());


		paraDef = (PAPX)daDoc.getParagraphTable().getParagraphs().get(3);
		assertEquals(400, paraDef.getStart());
		assertEquals(438, paraDef.getEnd());

		para = section.getParagraph(3);
		assertEquals(1, para.numCharacterRuns());
		assertEquals(lastText, para.text());


		// Check things match on text length
		assertEquals(439, range.text().length());
		assertEquals(439, section.text().length());
		assertEquals(439,
				section.getParagraph(0).text().length() +
				section.getParagraph(1).text().length() +
				section.getParagraph(2).text().length() +
				section.getParagraph(3).text().length() +
				section.getParagraph(4).text().length()
		);
	}

	/**
	 * Test that we can delete text (one instance) from our Range with Unicode text.
	 */
	public void testRangeDeleteOne() {

		HWPFDocument daDoc = HWPFTestDataSamples.openSampleFile(illustrativeDocFile);

		Range range = daDoc.getOverallRange();
		assertEquals(1, range.numSections());

		Section section = range.getSection(0);
		assertEquals(5, section.numParagraphs());

		Paragraph para = section.getParagraph(2);

		String text = para.text();
		assertEquals(originalText, text);

		int offset = text.indexOf(searchText);
		assertEquals(192, offset);

		int absOffset = para.getStartOffset() + offset;
		Range subRange = new Range(absOffset, (absOffset + searchText.length()), para.getDocument());

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
	public void testRangeDeleteAll() {

		HWPFDocument daDoc = HWPFTestDataSamples.openSampleFile(illustrativeDocFile);

		Range range = daDoc.getRange();
		assertEquals(1, range.numSections());

		Section section = range.getSection(0);
		assertEquals(5, section.numParagraphs());

		Paragraph para = section.getParagraph(2);

		String text = para.text();
		assertEquals(originalText, text);

		boolean keepLooking = true;
		while (keepLooking) {
			// Reload the range every time
			range = daDoc.getRange();
			int offset = range.text().indexOf(searchText);
			if (offset >= 0) {

				int absOffset = range.getStartOffset() + offset;

				Range subRange = new Range(
					absOffset, (absOffset + searchText.length()), range.getDocument());

				assertEquals(searchText, subRange.text());

				subRange.delete();

			} else {
				keepLooking = false;
			}
		}

		// we need to let the model re-calculate the Range before we use it
		range = daDoc.getRange();

		assertEquals(1, range.numSections());
		section = range.getSection(0);

		assertEquals(5, section.numParagraphs());

		para = section.getParagraph(0);
		text = para.text();
		assertEquals(introText, text);

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
