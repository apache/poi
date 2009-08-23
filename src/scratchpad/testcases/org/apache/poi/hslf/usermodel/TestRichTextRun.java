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

package org.apache.poi.hslf.usermodel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;

import junit.framework.TestCase;

import org.apache.poi.hslf.HSLFSlideShow;
import org.apache.poi.hslf.model.Slide;
import org.apache.poi.hslf.model.TextBox;
import org.apache.poi.hslf.model.TextRun;
import org.apache.poi.hslf.record.Record;
import org.apache.poi.hslf.record.SlideListWithText;
import org.apache.poi.POIDataSamples;

/**
 * Test that the friendly getters and setters on RichTextRun
 *  behave as expected.
 * (model.TestTextRun tests the other functionality)
 * @author Nick Burch (nick at torchbox dot com)
 */
public final class TestRichTextRun extends TestCase {
    private static POIDataSamples _slTests = POIDataSamples.getSlideShowInstance();

	// SlideShow primed on the test data
	private SlideShow ss;
	private SlideShow ssRichA;
	private SlideShow ssRichB;
	private SlideShow ssRichC;
	private HSLFSlideShow hss;
	private HSLFSlideShow hssRichA;
	private HSLFSlideShow hssRichB;
	private HSLFSlideShow hssRichC;
	private static String filenameC;

	protected void setUp() throws Exception {

		// Basic (non rich) test file
        hss = new HSLFSlideShow(_slTests.openResourceAsStream("basic_test_ppt_file.ppt"));
		ss = new SlideShow(hss);

		// Rich test file A
		hssRichA = new HSLFSlideShow(_slTests.openResourceAsStream("Single_Coloured_Page.ppt"));
		ssRichA = new SlideShow(hssRichA);

		// Rich test file B
		hssRichB = new HSLFSlideShow(_slTests.openResourceAsStream("Single_Coloured_Page_With_Fonts_and_Alignments.ppt"));
		ssRichB = new SlideShow(hssRichB);

		// Rich test file C - has paragraph styles that run out before
		//   the character ones do
		filenameC = "ParagraphStylesShorterThanCharStyles.ppt";
        hssRichC = new HSLFSlideShow(_slTests.openResourceAsStream(filenameC));
		ssRichC = new SlideShow(hssRichC);
	}

	/**
	 * Test the stuff about getting/setting bold
	 *  on a non rich text run
	 */
	public void testBoldNonRich() {
		Slide slideOne = ss.getSlides()[0];
		TextRun[] textRuns = slideOne.getTextRuns();
		RichTextRun rtr = textRuns[0].getRichTextRuns()[0];

		assertNull(rtr._getRawCharacterStyle());
		assertNull(rtr._getRawParagraphStyle());
		assertFalse(rtr.isBold());

		// Now set it to not bold
		rtr.setBold(false);
		//setting bold=false doesn't change the internal state
		assertNull(rtr._getRawCharacterStyle());
		assertNull(rtr._getRawParagraphStyle());

		assertFalse(rtr.isBold());

		// And now make it bold
		rtr.setBold(true);
		assertNotNull(rtr._getRawCharacterStyle());
		assertNotNull(rtr._getRawParagraphStyle());
		assertTrue(rtr.isBold());
	}

	/**
	 * Test the stuff about getting/setting bold
	 *  on a rich text run
	 */
	public void testBoldRich() {
		Slide slideOneR = ssRichA.getSlides()[0];
		TextRun[] textRunsR = slideOneR.getTextRuns();
		RichTextRun[] rtrs = textRunsR[1].getRichTextRuns();
		assertEquals(3, rtrs.length);

		assertTrue(rtrs[0].isBold());
		assertFalse(rtrs[1].isBold());
		assertFalse(rtrs[2].isBold());

		rtrs[0].setBold(true);
		rtrs[1].setBold(true);

		assertTrue(rtrs[0].isBold());
		assertTrue(rtrs[1].isBold());

		rtrs[0].setBold(false);
		rtrs[1].setBold(false);

		assertFalse(rtrs[0].isBold());
		assertFalse(rtrs[1].isBold());
	}

	/**
	 * Tests getting and setting the font size on rich and non
	 *  rich text runs
	 */
	public void testFontSize() {

		Slide slideOne = ss.getSlides()[0];
		TextRun[] textRuns = slideOne.getTextRuns();
		RichTextRun rtr = textRuns[0].getRichTextRuns()[0];

		Slide slideOneR = ssRichB.getSlides()[0];
		TextRun[] textRunsR = slideOneR.getTextRuns();
		RichTextRun rtrRa = textRunsR[0].getRichTextRuns()[0];
		RichTextRun rtrRb = textRunsR[1].getRichTextRuns()[0];
		RichTextRun rtrRc = textRunsR[1].getRichTextRuns()[3];

		String defaultFont = "Arial";

		// Start off with rich one
		// First run has defaults
		assertEquals(44, rtrRa.getFontSize());
		assertEquals(defaultFont, rtrRa.getFontName());

		// Second is size 20, default font
		assertEquals(20, rtrRb.getFontSize());
		assertEquals(defaultFont, rtrRb.getFontName());
		// Third is size 24, alt font
		assertEquals(24, rtrRc.getFontSize());
		assertEquals("Times New Roman", rtrRc.getFontName());

		// Change 2nd to different size and font
		assertEquals(2, ssRichB.getFontCollection().getChildRecords().length); // Default + TNR
		rtrRb.setFontSize(18);
		rtrRb.setFontName("Courier");
		assertEquals(3, ssRichB.getFontCollection().getChildRecords().length); // Default + TNR + Courier
		assertEquals(18, rtrRb.getFontSize());
		assertEquals("Courier", rtrRb.getFontName());


		// Now do non rich one
		assertEquals(44, rtr.getFontSize());
		assertEquals(defaultFont, rtr.getFontName());
		assertEquals(1, ss.getFontCollection().getChildRecords().length); // Default
		assertNull(rtr._getRawCharacterStyle());
		assertNull(rtr._getRawParagraphStyle());

		// Change Font size
		rtr.setFontSize(99);
		assertEquals(99, rtr.getFontSize());
		assertEquals(defaultFont, rtr.getFontName());
		assertNotNull(rtr._getRawCharacterStyle());
		assertNotNull(rtr._getRawParagraphStyle());
		assertEquals(1, ss.getFontCollection().getChildRecords().length); // Default

		// Change Font size and name
		rtr.setFontSize(25);
		rtr.setFontName("Times New Roman");
		assertEquals(25, rtr.getFontSize());
		assertEquals("Times New Roman", rtr.getFontName());
		assertNotNull(rtr._getRawCharacterStyle());
		assertNotNull(rtr._getRawParagraphStyle());
		assertEquals(2, ss.getFontCollection().getChildRecords().length);
	}

	public void testChangeWriteRead() throws Exception {
		HSLFSlideShow[] h = new HSLFSlideShow[] { hss, hssRichA, hssRichB };
		Slide[] s = new Slide[] { ss.getSlides()[0], ssRichA.getSlides()[0], ssRichB.getSlides()[0] };

		for(int i=0; i<h.length; i++) {
			// Change
			Slide slideOne = s[i];
			TextRun[] textRuns = slideOne.getTextRuns();
			RichTextRun rtr = textRuns[0].getRichTextRuns()[0];

			rtr.setBold(true);
			rtr.setFontSize(18);
			rtr.setFontName("Courier");

			// Check it took those
			assertEquals(true, rtr.isBold());
			assertEquals(18, rtr.getFontSize());
			assertEquals("Courier", rtr.getFontName());

			// Write out and back in
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			h[i].write(baos);
			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

			HSLFSlideShow readHSLF = new HSLFSlideShow(bais);
			SlideShow readS = new SlideShow(readHSLF);

			// Tweak existing one again, to ensure really worked
			rtr.setBold(false);
			rtr.setFontSize(17);
			rtr.setFontName("CourierZZ");

			// Check it took those changes
			assertEquals(false, rtr.isBold());
			assertEquals(17, rtr.getFontSize());
			assertEquals("CourierZZ", rtr.getFontName());


			// Now, look at the one we changed, wrote out, and read back in
			// Ensure it does contain our original modifications
			Slide slideOneRR = readS.getSlides()[0];
			TextRun[] textRunsRR = slideOneRR.getTextRuns();
			RichTextRun rtrRRa = textRunsRR[0].getRichTextRuns()[0];

			assertEquals(true, rtrRRa.isBold());
			assertEquals(18, rtrRRa.getFontSize());
			assertEquals("Courier", rtrRRa.getFontName());
		}
	}

	/**
	 * Test that we can do the right things when the paragraph styles
	 *  run out before the character styles do
	 */
	public void testParagraphStylesShorterTheCharStyles() {
		// Check we have the right number of sheets
		Slide[] slides = ssRichC.getSlides();
		assertEquals(14, slides.length);

		// Check the number of text runs on interesting sheets
		Slide slideThreeC = ssRichC.getSlides()[2];
		Slide slideSevenC = ssRichC.getSlides()[6];
		assertEquals(3, slideThreeC.getTextRuns().length);
		assertEquals(5, slideSevenC.getTextRuns().length);

		// On slide three, we should have:
		// TR:
		//   You are an important supplier of various items that I need
		//   .
		// TR:
		//   Source: Internal focus groups
		// TR:
		//   Illustrative Example
		//   .

		TextRun[] s3tr = slideThreeC.getTextRuns();
		RichTextRun[] s3rtr0 = s3tr[0].getRichTextRuns();
		RichTextRun[] s3rtr1 = s3tr[1].getRichTextRuns();
		RichTextRun[] s3rtr2 = s3tr[2].getRichTextRuns();

		assertEquals(2, s3rtr0.length);
		assertEquals(1, s3rtr1.length);
		assertEquals(2, s3rtr2.length);

		assertEquals("You are an important supplier of various items that I need", s3rtr0[0].getText());
		assertEquals("", s3rtr0[1].getText());
		assertEquals("Source: Internal focus groups", s3rtr1[0].getText());
		assertEquals("Illustrative Example", s3rtr2[0].getText());
		assertEquals("", s3rtr2[1].getText());

		assertTrue(s3rtr0[0]._isParagraphStyleShared());
		assertTrue(s3rtr0[1]._isParagraphStyleShared());
		assertFalse(s3rtr1[0]._isParagraphStyleShared());
		assertTrue(s3rtr2[0]._isParagraphStyleShared());
		assertTrue(s3rtr2[1]._isParagraphStyleShared());

		assertFalse(s3rtr0[0]._isCharacterStyleShared());
		assertFalse(s3rtr0[1]._isCharacterStyleShared());
		assertFalse(s3rtr1[0]._isCharacterStyleShared());
		assertFalse(s3rtr2[0]._isCharacterStyleShared());
		assertFalse(s3rtr2[1]._isCharacterStyleShared());

		// On slide seven, we have:
		// TR:
		//  (text)
		// TR:
		//  <ps>(text a)</ps><ps>(text a)(text b)</ps>
		// TR:
		//  (text)
		TextRun[] s7tr = slideSevenC.getTextRuns();
		RichTextRun[] s7rtr0 = s7tr[0].getRichTextRuns();
		RichTextRun[] s7rtr1 = s7tr[1].getRichTextRuns();
		RichTextRun[] s7rtr2 = s7tr[2].getRichTextRuns();

		assertEquals(1, s7rtr0.length);
		assertEquals(3, s7rtr1.length);
		assertEquals(1, s7rtr2.length);

		assertFalse(s7rtr0[0]._isParagraphStyleShared());
		assertFalse(s7rtr1[0]._isParagraphStyleShared());
		assertTrue(s7rtr1[1]._isParagraphStyleShared());
		assertTrue(s7rtr1[2]._isParagraphStyleShared());
		assertFalse(s7rtr2[0]._isParagraphStyleShared());

		assertFalse(s7rtr0[0]._isCharacterStyleShared());
		assertTrue(s7rtr1[0]._isCharacterStyleShared());
		assertTrue(s7rtr1[1]._isCharacterStyleShared());
		assertFalse(s7rtr1[2]._isCharacterStyleShared());
		assertFalse(s7rtr2[0]._isCharacterStyleShared());
	}

	/**
	 * Test that we can do the right things when the paragraph styles
	 *  run out before the character styles do, when we tweak something
	 *  and write back out.
	 */
	public void testParagraphStylesShorterTheCharStylesWrite() throws Exception {
		assertMatchesSLTWC(ssRichC);
		assertMatchesFileC(ssRichC);

		Slide slideSevenC = ssRichC.getSlides()[6];
		TextRun[] s7tr = slideSevenC.getTextRuns();
		RichTextRun[] s7rtr0 = s7tr[0].getRichTextRuns();
		RichTextRun[] s7rtr1 = s7tr[1].getRichTextRuns();
		RichTextRun[] s7rtr2 = s7tr[2].getRichTextRuns();

		String oldText;

		// Reset the text on the last run
		// Need to ensure it's a run that really has styles!
		oldText = s7rtr2[0].getRawText();
		s7rtr2[0].setText( oldText );
		assertEquals(oldText, s7rtr2[0].getText());
		assertEquals(oldText, s7tr[2].getText());
		assertEquals(oldText.length() + 1, s7rtr2[0]._getRawCharacterStyle().getCharactersCovered());
		assertEquals(oldText.length() + 1, s7rtr2[0]._getRawParagraphStyle().getCharactersCovered());
		assertMatchesSLTWC(ssRichC);
		assertMatchesFileC(ssRichC);

		// Reset the text on a shared paragraph
		oldText = s7rtr1[2].getRawText();
		s7rtr1[2].setText( oldText );
		assertEquals(oldText, s7rtr1[2].getText());
		assertEquals(oldText.length() + 1, s7rtr1[2]._getRawCharacterStyle().getCharactersCovered());
		assertMatchesSLTWC(ssRichC);
		assertMatchesFileC(ssRichC);

		// Reset the text on a shared paragraph+character
		s7rtr1[1].setText( s7rtr1[1].getRawText() );
		assertMatchesSLTWC(ssRichC);
		assertMatchesFileC(ssRichC);
	}

	/**
	 * Opens a new copy of SlideShow C, writes the active
	 *  SlideListWithText out, and compares it to the write
	 *  out of the supplied SlideShow. Also compares the
	 *  contents.
	 * @param s
	 */
	private void assertMatchesSLTWC(SlideShow s) throws Exception {
		// Grab a new copy of slideshow C
		SlideShow refC = new SlideShow(_slTests.openResourceAsStream(filenameC));

		// Write out the 2nd SLWT in the active document
		SlideListWithText refSLWT = refC.getDocumentRecord().getSlideListWithTexts()[1];
		byte[] raw_slwt = writeRecord(refSLWT);

		// Write out the same for the supplied slideshow
		SlideListWithText s_SLWT = s.getDocumentRecord().getSlideListWithTexts()[1];
		byte[] s_slwt = writeRecord(s_SLWT);

		// Check the records are the same
		assertEquals(refSLWT.getChildRecords().length, s_SLWT.getChildRecords().length);
		for(int i=0; i<refSLWT.getChildRecords().length; i++) {
			Record ref_r = refSLWT.getChildRecords()[i];
			Record s_r = s_SLWT.getChildRecords()[i];

			byte[] r_rb = writeRecord(ref_r);
			byte[] s_rb = writeRecord(s_r);
			assertEquals(r_rb.length, s_rb.length);
			for(int j=0; j<r_rb.length; j++) {
				assertEquals(r_rb[j],s_rb[j]);
			}
		}

		// Check the bytes are the same
		assertEquals(raw_slwt.length, s_slwt.length);
		for(int i=0; i<raw_slwt.length; i++) {
			assertEquals(raw_slwt[i], s_slwt[i]);
		}
	}

	/**
	 * Checks that the supplied slideshow still matches the bytes
	 *  of slideshow c
	 */
	private static void assertMatchesFileC(SlideShow s) throws Exception {
		if (true) { // TODO - test is disabled, pending fix of bug #39800
			// System.err.println("Skipping test, as would be marked as failed due to bug #39800"); //
			return;
		}
if(false) {
		// Grab the bytes of the file
		FileInputStream fin = new FileInputStream(filenameC);
		ByteArrayOutputStream fb = new ByteArrayOutputStream();
		byte[] b = new byte[4096];
		int read = 0;
		while(read != -1) {
			read = fin.read(b);
			if(read > 0) {
				fb.write(b, 0, read);
			}
		}
		byte[] raw_file = fb.toByteArray();

		// Now write out the slideshow
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		s.write(baos);
		byte[] raw_ss = baos.toByteArray();

		// Ensure they're the same
		assertEquals(raw_file.length, raw_ss.length);
		for(int i=0; i<raw_file.length; i++) {
			assertEquals(raw_file[i], raw_ss[i]);
		}
}
	}

	private byte[] writeRecord(Record r) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		r.writeOut(baos);
		return baos.toByteArray();
	}

	public void testIndentationLevel() throws Exception {
		SlideShow ppt = new SlideShow(_slTests.openResourceAsStream("ParagraphStylesShorterThanCharStyles.ppt"));
		Slide[] sl = ppt.getSlides();
		for (int i = 0; i < sl.length; i++) {
			TextRun[] txt = sl[i].getTextRuns();
			for (int j = 0; j < txt.length; j++) {
				RichTextRun[] rt = txt[j].getRichTextRuns();
				for (int k = 0; k < rt.length; k++) {
					int indent = rt[k].getIndentLevel();
					assertTrue(indent >= 0 && indent <= 4 );
				}

			}
		}
	}

	public void testReadParagraphStyles() throws Exception {
		SlideShow ppt = new SlideShow(_slTests.openResourceAsStream("bullets.ppt"));
		assertTrue("No Exceptions while reading file", true);

		RichTextRun rt;
		TextRun[] txt;
		Slide[] slide = ppt.getSlides();
		assertEquals(2, slide.length);

		txt = slide[0].getTextRuns();
		assertEquals(2, txt.length);

		assertEquals("Title text", txt[0].getRawText());
		assertEquals(1, txt[0].getRichTextRuns().length);
		rt = txt[0].getRichTextRuns()[0];
		assertFalse(rt.isBullet());

		assertEquals(
				"This is a text placeholder that \r" +
				"follows the design pattern\r" +
				"Defined in the slide master\r" +
				"and has bullets by default", txt[1].getRawText());
		assertEquals(1, txt[1].getRichTextRuns().length);
		rt = txt[1].getRichTextRuns()[0];
		assertEquals('\u2022', rt.getBulletChar());
		assertTrue(rt.isBullet());


		txt = slide[1].getTextRuns();
		assertEquals(2, txt.length);

		assertEquals(
				"I\u2019m a text box\r" +
				"With bullets\r" +
				"That follow the design pattern\r" +
				"From the slide master", txt[0].getRawText());
		assertEquals(1, txt[0].getRichTextRuns().length);
		rt = txt[0].getRichTextRuns()[0];
		assertTrue(rt.isBullet());
		assertEquals('\u2022', rt.getBulletChar());

		assertEquals(
				"I\u2019m a text box with user-defined\r" +
				"bullet character", txt[1].getRawText());
		assertEquals(1, txt[1].getRichTextRuns().length);
		rt = txt[1].getRichTextRuns()[0];
		assertTrue(rt.isBullet());
		assertEquals('\u263A', rt.getBulletChar());
	}

	public void testSetParagraphStyles() throws Exception {
		SlideShow ppt = new SlideShow();

		Slide slide = ppt.createSlide();

		TextBox shape = new TextBox();
		RichTextRun rt = shape.getTextRun().getRichTextRuns()[0];
		shape.setText(
				"Hello, World!\r" +
				"This should be\r" +
				"Multiline text");
		rt.setFontSize(42);
		rt.setBullet(true);
		rt.setTextOffset(50);
		rt.setBulletOffset(0);
		rt.setBulletChar('\u263A');
		slide.addShape(shape);

		assertEquals(42, rt.getFontSize());
		assertEquals(true, rt.isBullet());
		assertEquals(50, rt.getTextOffset());
		assertEquals(0, rt.getBulletOffset());
		assertEquals('\u263A', rt.getBulletChar());

		shape.setAnchor(new java.awt.Rectangle(50, 50, 500, 300));
		slide.addShape(shape);

		//serialize and read again
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ppt.write(out);
		out.close();

		ppt = new SlideShow(new ByteArrayInputStream(out.toByteArray()));
		slide = ppt.getSlides()[0];
		shape = (TextBox)slide.getShapes()[0];
		rt = shape.getTextRun().getRichTextRuns()[0];
		assertEquals(42, rt.getFontSize());
		assertEquals(true, rt.isBullet());
		assertEquals(50, rt.getTextOffset());
		assertEquals(0, rt.getBulletOffset());
		assertEquals('\u263A', rt.getBulletChar());
	}

	public void testAddText() throws Exception {
		SlideShow ppt = new SlideShow(_slTests.openResourceAsStream("bullets.ppt"));
		assertTrue("No Exceptions while reading file", true);

		RichTextRun rt;
		TextRun[] txt;
		Slide[] slides = ppt.getSlides();

		assertEquals(2, slides.length);
		txt = slides[0].getTextRuns();
		assertEquals(2, txt.length);

		assertEquals("Title text", txt[0].getRawText());
		assertEquals(1, txt[0].getRichTextRuns().length);
		rt = txt[0].getRichTextRuns()[0];
		assertFalse(rt.isBullet());

		// Add some new text
		txt[0].appendText("Foo! I'm new!");
		assertEquals(2, txt[0].getRichTextRuns().length);

		rt = txt[0].getRichTextRuns()[0];
		assertFalse(rt.isBold());
		assertEquals("Title text", rt.getText());
		rt = txt[0].getRichTextRuns()[1];
		assertFalse(rt.isBold());
		assertEquals("Foo! I'm new!", rt.getText());
		rt.setBold(true);

		// And some more
		txt[0].appendText("Me too!");
		assertEquals(3, txt[0].getRichTextRuns().length);
		rt = txt[0].getRichTextRuns()[0];
		assertFalse(rt.isBold());
		assertEquals("Title text", rt.getText());
		rt = txt[0].getRichTextRuns()[1];
		assertTrue(rt.isBold());
		assertEquals("Foo! I'm new!", rt.getText());
		rt = txt[0].getRichTextRuns()[2];
		assertFalse(rt.isBold());
		assertEquals("Me too!", rt.getText());

		// Save and re-open
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ppt.write(out);
		out.close();

		ppt = new SlideShow(new ByteArrayInputStream(out.toByteArray()));
		slides = ppt.getSlides();

		assertEquals(2, slides.length);

		txt = slides[0].getTextRuns();
		assertEquals(2, txt.length);
		assertEquals(3, txt[0].getRichTextRuns().length);
		rt = txt[0].getRichTextRuns()[0];
		assertFalse(rt.isBold());
		assertEquals("Title text", rt.getText());
		rt = txt[0].getRichTextRuns()[1];
		assertTrue(rt.isBold());
		assertEquals("Foo! I'm new!", rt.getText());
		rt = txt[0].getRichTextRuns()[2];
		assertFalse(rt.isBold());
		assertEquals("Me too!", rt.getText());

//		FileOutputStream fout = new FileOutputStream("/tmp/foo.ppt");
//		ppt.write(fout);
	}
}
