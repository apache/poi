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

/**
 *	Test to see if Range.insertBefore() works even if the Range contains a
 *	CharacterRun that uses Unicode characters.
 *
 * TODO - re-enable me when unicode paragraph stuff is fixed!
 */
public final class TestRangeInsertion extends TestCase {

	// u201c and u201d are "smart-quotes"
	private String originalText =
		"It is used to confirm that text insertion works even if Unicode characters (such as \u201c\u2014\u201d (U+2014), \u201c\u2e8e\u201d (U+2E8E), or \u201c\u2714\u201d (U+2714)) are present.\r";
	private String textToInsert = "Look at me!  I'm cool!  ";
	private int insertionPoint = 122;

	private String illustrativeDocFile;

	protected void setUp() {
		illustrativeDocFile = "testRangeInsertion.doc";
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

		Range range = daDoc.getRange();

		assertEquals(1, range.numSections());
		Section section = range.getSection(0);

		assertEquals(3, section.numParagraphs());
		Paragraph para = section.getParagraph(2);
		assertEquals(originalText, para.text());

		assertEquals(3, para.numCharacterRuns());
		String text =
			para.getCharacterRun(0).text() +
			para.getCharacterRun(1).text() +
			para.getCharacterRun(2).text()
		;

		assertEquals(originalText, text);

		assertEquals(insertionPoint, para.getStartOffset());
	}

	/**
	 * Test that we can insert text in our CharacterRun with Unicode text.
	 */
	public void testRangeInsertion() {

		HWPFDocument daDoc = HWPFTestDataSamples.openSampleFile(illustrativeDocFile);

		if (false) { // TODO - delete or resurrect this code
			Range range = daDoc.getRange();
			Section section = range.getSection(0);
			Paragraph para = section.getParagraph(2);
			String text = para.getCharacterRun(0).text() + para.getCharacterRun(1).text() +
			para.getCharacterRun(2).text();

			System.out.println(text);
		}

		Range range = new Range(insertionPoint, (insertionPoint + 2), daDoc);
		range.insertBefore(textToInsert);

		// we need to let the model re-calculate the Range before we evaluate it
		range = daDoc.getRange();

		assertEquals(1, range.numSections());
		Section section = range.getSection(0);

		assertEquals(3, section.numParagraphs());
		Paragraph para = section.getParagraph(2);
		assertEquals((textToInsert + originalText), para.text());

		assertEquals(3, para.numCharacterRuns());
		String text =
			para.getCharacterRun(0).text() +
			para.getCharacterRun(1).text() +
			para.getCharacterRun(2).text()
		;

		// System.out.println(text);

		assertEquals((textToInsert + originalText), text);
	}
}
