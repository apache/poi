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

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.HWPFTestCase;
import org.apache.poi.hwpf.HWPFTestDataSamples;
import org.apache.poi.hwpf.model.StyleSheet;

/**
 * Test various problem documents
 *
 * @author Nick Burch (nick at torchbox dot com)
 */
public final class TestProblems extends HWPFTestCase {

	/**
	 * ListEntry passed no ListTable
	 */
	public void testListEntryNoListTable() {
		HWPFDocument doc = HWPFTestDataSamples.openSampleFile("ListEntryNoListTable.doc");

		Range r = doc.getRange();
		StyleSheet styleSheet = doc.getStyleSheet();
		for (int x = 0; x < r.numSections(); x++) {
			Section s = r.getSection(x);
			for (int y = 0; y < s.numParagraphs(); y++) {
				Paragraph paragraph = s.getParagraph(y);
				// System.out.println(paragraph.getCharacterRun(0).text());
			}
		}
	}

	/**
	 * AIOOB for TableSprmUncompressor.unCompressTAPOperation
	 */
	public void testSprmAIOOB() {
		HWPFDocument doc = HWPFTestDataSamples.openSampleFile("AIOOB-Tap.doc");

		Range r = doc.getRange();
		StyleSheet styleSheet = doc.getStyleSheet();
		for (int x = 0; x < r.numSections(); x++) {
			Section s = r.getSection(x);
			for (int y = 0; y < s.numParagraphs(); y++) {
				Paragraph paragraph = s.getParagraph(y);
				// System.out.println(paragraph.getCharacterRun(0).text());
			}
		}
	}

	/**
	 * Test for TableCell not skipping the last paragraph. Bugs #45062 and
	 * #44292
	 */
	public void testTableCellLastParagraph() {
		HWPFDocument doc = HWPFTestDataSamples.openSampleFile("Bug44292.doc");
		Range r = doc.getRange();
		assertEquals(6, r.numParagraphs());
		assertEquals(0, r.getStartOffset());
		assertEquals(87, r.getEndOffset());

		// Paragraph with table
		Paragraph p = r.getParagraph(0);
		assertEquals(0, p.getStartOffset());
		assertEquals(20, p.getEndOffset());

		// Get the table
		Table t = r.getTable(p);

		// get the only row
		assertEquals(1, t.numRows());
		TableRow row = t.getRow(0);

		// get the first cell
		TableCell cell = row.getCell(0);
		// First cell should have one paragraph
		assertEquals(1, cell.numParagraphs());
		assertEquals("One paragraph is ok\7", cell.getParagraph(0).text());

		// get the second
		cell = row.getCell(1);
		// Second cell should be detected as having two paragraphs
		assertEquals(2, cell.numParagraphs());
		assertEquals("First para is ok\r", cell.getParagraph(0).text());
		assertEquals("Second paragraph is skipped\7", cell.getParagraph(1).text());

		// get the last cell
		cell = row.getCell(2);
		// Last cell should have one paragraph
		assertEquals(1, cell.numParagraphs());
		assertEquals("One paragraph is ok\7", cell.getParagraph(0).text());
	}

	public void testRangeDelete() {
		HWPFDocument doc = HWPFTestDataSamples.openSampleFile("Bug28627.doc");

		Range range = doc.getRange();
		int numParagraphs = range.numParagraphs();

		int totalLength = 0, deletedLength = 0;

		for (int i = 0; i < numParagraphs; i++) {
			Paragraph para = range.getParagraph(i);
			String text = para.text();

			totalLength += text.length();
			if (text.indexOf("{delete me}") > -1) {
				para.delete();
				deletedLength = text.length();
			}
		}

		// check the text length after deletion
		int newLength = 0;
		range = doc.getRange();
		numParagraphs = range.numParagraphs();

		for (int i = 0; i < numParagraphs; i++) {
			Paragraph para = range.getParagraph(i);
			String text = para.text();

			newLength += text.length();
		}

		assertEquals(newLength, totalLength - deletedLength);
	}

	/**
	 * With an encrypted file, we should give a suitable exception, and not OOM
	 */
	public void testEncryptedFile() {
		try {
			HWPFTestDataSamples.openSampleFile("PasswordProtected.doc");
			fail();
		} catch (EncryptedDocumentException e) {
			// Good
		}
	}

	public void testWriteProperties() {
		HWPFDocument doc = HWPFTestDataSamples.openSampleFile("SampleDoc.doc");
		assertEquals("Nick Burch", doc.getSummaryInformation().getAuthor());

		// Write and read
		HWPFDocument doc2 = writeOutAndRead(doc);
		assertEquals("Nick Burch", doc2.getSummaryInformation().getAuthor());
	}
}
