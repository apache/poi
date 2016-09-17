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
 * Tests for the handling of header stories into headers, footers etc
 */
public final class TestHeaderStories extends TestCase {
	private HWPFDocument none;
	private HWPFDocument header;
	private HWPFDocument footer;
	private HWPFDocument headerFooter;
	private HWPFDocument oddEven;
	private HWPFDocument diffFirst;
	private HWPFDocument unicode;
	private HWPFDocument withFields;

	protected void setUp() {

		none = HWPFTestDataSamples.openSampleFile("NoHeadFoot.doc");
		header = HWPFTestDataSamples.openSampleFile("ThreeColHead.doc");
		footer = HWPFTestDataSamples.openSampleFile("ThreeColFoot.doc");
		headerFooter = HWPFTestDataSamples.openSampleFile("SimpleHeadThreeColFoot.doc");
		oddEven = HWPFTestDataSamples.openSampleFile("PageSpecificHeadFoot.doc");
		diffFirst = HWPFTestDataSamples.openSampleFile("DiffFirstPageHeadFoot.doc");
		unicode = HWPFTestDataSamples.openSampleFile("HeaderFooterUnicode.doc");
		withFields = HWPFTestDataSamples.openSampleFile("HeaderWithMacros.doc");
	}

	public void testNone() {
		HeaderStories hs = new HeaderStories(none);

		assertNull(hs.getPlcfHdd());
		assertEquals(0, hs.getRange().text().length());
	}

	public void testHeader() {
		HeaderStories hs = new HeaderStories(header);

		assertEquals(60, hs.getRange().text().length());

		// Should have the usual 6 separaters
		// Then all 6 of the different header/footer kinds
		// Finally a terminater
		assertEquals(13, hs.getPlcfHdd().length());

		assertEquals(215, hs.getRange().getStartOffset());

		assertEquals(0, hs.getPlcfHdd().getProperty(0).getStart());
		assertEquals(3, hs.getPlcfHdd().getProperty(1).getStart());
		assertEquals(6, hs.getPlcfHdd().getProperty(2).getStart());
		assertEquals(6, hs.getPlcfHdd().getProperty(3).getStart());
		assertEquals(9, hs.getPlcfHdd().getProperty(4).getStart());
		assertEquals(12, hs.getPlcfHdd().getProperty(5).getStart());

		assertEquals(12, hs.getPlcfHdd().getProperty(6).getStart());
		assertEquals(12, hs.getPlcfHdd().getProperty(7).getStart());
		assertEquals(59, hs.getPlcfHdd().getProperty(8).getStart());
		assertEquals(59, hs.getPlcfHdd().getProperty(9).getStart());
		assertEquals(59, hs.getPlcfHdd().getProperty(10).getStart());
		assertEquals(59, hs.getPlcfHdd().getProperty(11).getStart());

		assertEquals(59, hs.getPlcfHdd().getProperty(12).getStart());

		assertEquals("\u0003\r\r", hs.getFootnoteSeparator());
		assertEquals("\u0004\r\r", hs.getFootnoteContSeparator());
		assertEquals("", hs.getFootnoteContNote());
		assertEquals("\u0003\r\r", hs.getEndnoteSeparator());
		assertEquals("\u0004\r\r", hs.getEndnoteContSeparator());
		assertEquals("", hs.getEndnoteContNote());

		assertEquals("", hs.getFirstHeader());
		assertEquals("", hs.getEvenHeader());
		assertEquals("First header column!\tMid header Right header!\r\r", hs.getOddHeader());

		assertEquals("", hs.getFirstFooter());
		assertEquals("", hs.getEvenFooter());
		assertEquals("", hs.getOddFooter());
	}

	public void testFooter() {
		HeaderStories hs = new HeaderStories(footer);

		assertEquals("", hs.getFirstHeader());
		assertEquals("", hs.getEvenHeader());
		assertEquals("", hs.getOddHeader()); // Was \r\r but gets emptied

		assertEquals("", hs.getFirstFooter());
		assertEquals("", hs.getEvenFooter());
		assertEquals("Footer Left\tFooter Middle Footer Right\r\r", hs.getOddFooter());
	}

	public void testHeaderFooter() {
		HeaderStories hs = new HeaderStories(headerFooter);

		assertEquals("", hs.getFirstHeader());
		assertEquals("", hs.getEvenHeader());
		assertEquals("I am some simple header text here\r\r\r", hs.getOddHeader());

		assertEquals("", hs.getFirstFooter());
		assertEquals("", hs.getEvenFooter());
		assertEquals("Footer Left\tFooter Middle Footer Right\r\r", hs.getOddFooter());
	}

	public void testOddEven() {
		HeaderStories hs = new HeaderStories(oddEven);

		assertEquals("", hs.getFirstHeader());
		assertEquals("[This is an Even Page, with a Header]\u0007August 20, 2008\u0007\u0007\r\r",
				hs.getEvenHeader());
		assertEquals("August 20, 2008\u0007[ODD Page Header text]\u0007\u0007\r\r", hs
				.getOddHeader());

		assertEquals("", hs.getFirstFooter());
		assertEquals(
				"\u0007Page \u0013 PAGE  \\* MERGEFORMAT \u00142\u0015\u0007\u0007\u0007\u0007\u0007\u0007\u0007This is a simple footer on the second page\r\r",
				hs.getEvenFooter());
		assertEquals("Footer Left\tFooter Middle Footer Right\r\r", hs.getOddFooter());

		assertEquals("Footer Left\tFooter Middle Footer Right\r\r", hs.getFooter(1));
		assertEquals(
				"\u0007Page \u0013 PAGE  \\* MERGEFORMAT \u00142\u0015\u0007\u0007\u0007\u0007\u0007\u0007\u0007This is a simple footer on the second page\r\r",
				hs.getFooter(2));
		assertEquals("Footer Left\tFooter Middle Footer Right\r\r", hs.getFooter(3));
	}

	public void testFirst() {
		HeaderStories hs = new HeaderStories(diffFirst);

		assertEquals("I am the header on the first page, and I\u2019m nice and simple\r\r", hs
				.getFirstHeader());
		assertEquals("", hs.getEvenHeader());
		assertEquals("First header column!\tMid header Right header!\r\r", hs.getOddHeader());

		assertEquals("The footer of the first page\r\r", hs.getFirstFooter());
		assertEquals("", hs.getEvenFooter());
		assertEquals("Footer Left\tFooter Middle Footer Right\r\r", hs.getOddFooter());

		assertEquals("The footer of the first page\r\r", hs.getFooter(1));
		assertEquals("Footer Left\tFooter Middle Footer Right\r\r", hs.getFooter(2));
		assertEquals("Footer Left\tFooter Middle Footer Right\r\r", hs.getFooter(3));
	}

	public void testUnicode() {
		HeaderStories hs = new HeaderStories(unicode);

		assertEquals("", hs.getFirstHeader());
		assertEquals("", hs.getEvenHeader());
		assertEquals("This is a simple header, with a \u20ac euro symbol in it.\r\r\r", hs
				.getOddHeader());

		assertEquals("", hs.getFirstFooter());
		assertEquals("", hs.getEvenFooter());
		assertEquals("The footer, with Moli\u00e8re, has Unicode in it.\r\r", hs.getOddFooter());
	}

	public void testWithFields() {
		HeaderStories hs = new HeaderStories(withFields);
		assertFalse(hs.areFieldsStripped());

		assertEquals(
				"HEADER GOES HERE. 8/12/2008 \u0013 AUTHOR   \\* MERGEFORMAT \u0014Eric Roch\u0015\r\r\r",
				hs.getOddHeader());

		// Now turn on stripping
		hs.setAreFieldsStripped(true);
		assertEquals("HEADER GOES HERE. 8/12/2008 Eric Roch\r\r\r", hs.getOddHeader());
	}
}
