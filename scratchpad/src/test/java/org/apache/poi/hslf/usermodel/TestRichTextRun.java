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

import static org.apache.poi.POITestCase.assertContains;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.poi.hslf.HSLFTestDataSamples;
import org.apache.poi.hslf.record.Record;
import org.apache.poi.hslf.record.SlideListWithText;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test that the friendly getters and setters on RichTextRun
 *  behave as expected.
 * (model.TestTextRun tests the other functionality)
 */
public final class TestRichTextRun {
    // SlideShow primed on the test data
    private HSLFSlideShow ss;
    private HSLFSlideShow ssRichA;
    private HSLFSlideShow ssRichB;
    private HSLFSlideShow ssRichC;
    private HSLFSlideShow ssChinese;
    private static String filenameC;

    @BeforeEach
    void setUp() throws IOException {
        // Basic (non rich) test file
        ss = HSLFTestDataSamples.getSlideShow("basic_test_ppt_file.ppt");

        // Rich test file A
        ssRichA = HSLFTestDataSamples.getSlideShow("Single_Coloured_Page.ppt");

        // Rich test file B
        ssRichB = HSLFTestDataSamples.getSlideShow("Single_Coloured_Page_With_Fonts_and_Alignments.ppt");

        // Rich test file C - has paragraph styles that run out before
        //   the character ones do
        filenameC = "ParagraphStylesShorterThanCharStyles.ppt";
        ssRichC = HSLFTestDataSamples.getSlideShow(filenameC);

        // Rich test file with Chinese + English text in it
        ssChinese = HSLFTestDataSamples.getSlideShow("54880_chinese.ppt");
    }

    @AfterEach
    void tearDown() throws IOException {
        ss.close();
        ssRichA.close();
        ssRichB.close();
        ssRichC.close();
        ssChinese.close();
    }

	/**
	 * Test the stuff about getting/setting bold
	 *  on a non rich text run
	 */
    @Test
	void testBoldNonRich() {
		HSLFSlide slideOne = ss.getSlides().get(0);
		List<List<HSLFTextParagraph>> textParass = slideOne.getTextParagraphs();
		List<HSLFTextParagraph> textParas = textParass.get(0);
		HSLFTextRun rtr = textParas.get(0).getTextRuns().get(0);

		assertNotNull(rtr.getCharacterStyle());
		assertNotNull(textParas.get(0).getParagraphStyle());
		assertFalse(rtr.isBold());

		// Now set it to not bold
		rtr.setBold(false);
		// in Pre 3.12: setting bold=false doesn't change the internal state
		// now: also allow explicitly disable styles and there aren't any non rich text runs anymore
		assertNotNull(rtr.getCharacterStyle());
		assertNotNull(textParas.get(0).getParagraphStyle());

		assertFalse(rtr.isBold());

		// And now make it bold
		rtr.setBold(true);
		assertNotNull(rtr.getCharacterStyle());
        assertNotNull(textParas.get(0).getParagraphStyle());
		assertTrue(rtr.isBold());
	}

	/**
	 * Test the stuff about getting/setting bold
	 *  on a rich text run
	 */
    @Test
	void testBoldRich() {
		HSLFSlide slideOneR = ssRichA.getSlides().get(0);
		List<List<HSLFTextParagraph>> textParass = slideOneR.getTextParagraphs();
		List<HSLFTextParagraph> textParas = textParass.get(1);
		assertEquals(3, textParas.size());

		assertTrue(textParas.get(0).getTextRuns().get(0).isBold());
		assertFalse(textParas.get(1).getTextRuns().get(0).isBold());
		assertFalse(textParas.get(2).getTextRuns().get(0).isBold());

		textParas.get(0).getTextRuns().get(0).setBold(true);
		textParas.get(1).getTextRuns().get(0).setBold(true);

		assertTrue(textParas.get(0).getTextRuns().get(0).isBold());
		assertTrue(textParas.get(1).getTextRuns().get(0).isBold());

		textParas.get(0).getTextRuns().get(0).setBold(false);
		textParas.get(1).getTextRuns().get(0).setBold(false);

		assertFalse(textParas.get(0).getTextRuns().get(0).isBold());
		assertFalse(textParas.get(1).getTextRuns().get(0).isBold());
	}

	/**
	 * Tests getting and setting the font size on rich and non
	 *  rich text runs
	 */
    @Test
	void testFontSize() {

		HSLFSlide slideOne = ss.getSlides().get(0);
		List<List<HSLFTextParagraph>> textParass = slideOne.getTextParagraphs();
		HSLFTextRun rtr = textParass.get(0).get(0).getTextRuns().get(0);

		HSLFSlide slideOneR = ssRichB.getSlides().get(0);
		List<List<HSLFTextParagraph>> textParassR = slideOneR.getTextParagraphs();
		HSLFTextRun rtrRa = textParassR.get(0).get(0).getTextRuns().get(0);
		HSLFTextRun rtrRb = textParassR.get(1).get(0).getTextRuns().get(0);
		HSLFTextRun rtrRc = textParassR.get(1).get(3).getTextRuns().get(0);

		String defaultFont = "Arial";

		// Start off with rich one
		// First run has defaults
		assertNotNull(rtrRa.getFontSize());
		assertEquals(44, rtrRa.getFontSize(), 0);
		assertEquals(defaultFont, rtrRa.getFontFamily());

		// Second is size 20, default font
		assertNotNull(rtrRb.getFontSize());
		assertEquals(20, rtrRb.getFontSize(), 0);
		assertEquals(defaultFont, rtrRb.getFontFamily());
		// Third is size 24, alt font
		assertNotNull(rtrRc.getFontSize());
		assertEquals(24, rtrRc.getFontSize(), 0);
		assertEquals("Times New Roman", rtrRc.getFontFamily());

		// Change 2nd to different size and font
		assertEquals(2, ssRichB.getFontCollection().getChildRecords().length); // Default + TNR
		rtrRb.setFontSize(18d);
		rtrRb.setFontFamily("Courier");
		assertEquals(3, ssRichB.getFontCollection().getChildRecords().length); // Default + TNR + Courier
		assertEquals(18, rtrRb.getFontSize(), 0);
		assertEquals("Courier", rtrRb.getFontFamily());


		// Now do non rich one
		assertNotNull(rtr.getFontSize());
		assertEquals(44, rtr.getFontSize(), 0);
		assertEquals(defaultFont, rtr.getFontFamily());
		assertEquals(1, ss.getFontCollection().getChildRecords().length); // Default
		assertNotNull(rtr.getCharacterStyle());
		assertNotNull(rtr.getTextParagraph().getParagraphStyle());

		// Change Font size
		rtr.setFontSize(99d);
		assertEquals(99, rtr.getFontSize(), 0);
		assertEquals(defaultFont, rtr.getFontFamily());
		assertNotNull(rtr.getCharacterStyle());
		assertNotNull(rtr.getTextParagraph().getParagraphStyle());
		assertEquals(1, ss.getFontCollection().getChildRecords().length); // Default

		// Change Font size and name
		rtr.setFontSize(25d);
		rtr.setFontFamily("Times New Roman");
		assertEquals(25, rtr.getFontSize(), 0);
		assertEquals("Times New Roman", rtr.getFontFamily());
		assertNotNull(rtr.getCharacterStyle());
		assertNotNull(rtr.getTextParagraph().getParagraphStyle());
		assertEquals(2, ss.getFontCollection().getChildRecords().length);
	}

    @Test
	void testChangeWriteRead() throws IOException {
		for(HSLFSlideShow h : new HSLFSlideShow[] { ss, ssRichA, ssRichB }) {
			// Change
			HSLFSlide slideOne = h.getSlides().get(0);
			List<List<HSLFTextParagraph>> textParass = slideOne.getTextParagraphs();
			HSLFTextRun rtr = textParass.get(0).get(0).getTextRuns().get(0);

			rtr.setBold(true);
			rtr.setFontSize(18d);
			rtr.setFontFamily("Courier");
            HSLFTextParagraph.storeText(textParass.get(0));

			// Check it took those
			assertTrue(rtr.isBold());
			assertNotNull(rtr.getFontSize());
			assertEquals(18., rtr.getFontSize(), 0);
			assertEquals("Courier", rtr.getFontFamily());

			// Write out and back in
			HSLFSlideShow readS = HSLFTestDataSamples.writeOutAndReadBack(h);

			// Tweak existing one again, to ensure really worked
			rtr.setBold(false);
			rtr.setFontSize(17d);
			rtr.setFontFamily("CourierZZ");

			// Check it took those changes
			assertFalse(rtr.isBold());
			assertEquals(17., rtr.getFontSize(), 0);
			assertEquals("CourierZZ", rtr.getFontFamily());


			// Now, look at the one we changed, wrote out, and read back in
			// Ensure it does contain our original modifications
			HSLFSlide slideOneRR = readS.getSlides().get(0);
			List<List<HSLFTextParagraph>> textParassRR = slideOneRR.getTextParagraphs();
			HSLFTextRun rtrRRa = textParassRR.get(0).get(0).getTextRuns().get(0);

			assertTrue(rtrRRa.isBold());
			assertNotNull(rtrRRa.getFontSize());
			assertEquals(18., rtrRRa.getFontSize(), 0);
			assertEquals("Courier", rtrRRa.getFontFamily());
			readS.close();
		}
	}

	/**
	 * Test that we can do the right things when the paragraph styles
	 *  run out before the character styles do
	 */
    @Test
	void testParagraphStylesShorterTheCharStyles() {
		// Check we have the right number of sheets
		List<HSLFSlide> slides = ssRichC.getSlides();
		assertEquals(14, slides.size());

		// Check the number of text runs on interesting sheets
		HSLFSlide slideThreeC = ssRichC.getSlides().get(2);
		HSLFSlide slideSevenC = ssRichC.getSlides().get(6);
		assertEquals(4, slideThreeC.getTextParagraphs().size());
		assertEquals(5, slideSevenC.getTextParagraphs().size());

		// On slide three, we should have:
		// TR:
		//   You are an important supplier of various items that I need
		//   .
		// TR:
		//   Source: Internal focus groups
		// TR:
		//   Illustrative Example
		//   .

        List<List<HSLFTextParagraph>> s3tr = slideThreeC.getTextParagraphs();
		List<HSLFTextRun> s3rtr0 = s3tr.get(0).get(0).getTextRuns();
		List<HSLFTextRun> s3rtr1 = s3tr.get(2).get(0).getTextRuns();
		List<HSLFTextRun> s3rtr2 = s3tr.get(3).get(0).getTextRuns();

		assertEquals(2, s3rtr0.size());
		assertEquals(1, s3rtr1.size());
		assertEquals(2, s3rtr2.size());

		assertEquals("You are an important supplier of various items that I need", s3rtr0.get(0).getRawText());
		assertEquals("", s3rtr0.get(1).getRawText());
		assertEquals("Source: Internal focus groups", s3rtr1.get(0).getRawText());
		assertEquals("Illustrative Example", s3rtr2.get(0).getRawText());
		assertEquals("", s3rtr2.get(1).getRawText());

		// On slide seven, we have:
		// TR:
		//  (text)
		// TR:
		//  <ps>(text a)</ps><ps>(text a)(text b)</ps>
		// TR:
		//  (text)
		List<List<HSLFTextParagraph>> s7tr = slideSevenC.getTextParagraphs();
		List<HSLFTextParagraph> s7rtr0 = s7tr.get(0);
		List<HSLFTextParagraph> s7rtr1 = s7tr.get(1);
		List<HSLFTextParagraph> s7rtr2 = s7tr.get(2);

		assertEquals(1, s7rtr0.size());
		assertEquals(8, s7rtr1.size());
		assertEquals(1, s7rtr2.size());
	}

	/**
	 * Test that we can do the right things when the paragraph styles
	 *  run out before the character styles do, when we tweak something
	 *  and write back out.
	 */
    @Test
	@SuppressWarnings("unused")
    void testParagraphStylesShorterTheCharStylesWrite() throws IOException {
		assertMatchesSLTWC(ssRichC);
		assertMatchesFileC(ssRichC);

		HSLFSlide slideSevenC = ssRichC.getSlides().get(6);
		List<List<HSLFTextParagraph>> s7tr = slideSevenC.getTextParagraphs();
		List<HSLFTextRun> s7rtr0 = s7tr.get(0).get(0).getTextRuns();
		List<HSLFTextRun> s7rtr1 = s7tr.get(1).get(0).getTextRuns();
		List<HSLFTextRun> s7rtr2 = s7tr.get(2).get(0).getTextRuns();

		String oldText;

		// Reset the text on the last run
		// Need to ensure it's a run that really has styles!
		oldText = s7rtr2.get(0).getRawText();
		s7rtr2.get(0).setText( oldText );
		HSLFTextParagraph.storeText(s7tr.get(2));
		assertEquals(oldText, s7rtr2.get(0).getRawText());
		assertEquals(oldText, HSLFTextParagraph.getRawText(s7tr.get(2)));
		assertEquals(oldText.length() + 1, s7rtr2.get(0).getCharacterStyle().getCharactersCovered());
		assertEquals(oldText.length() + 1, s7rtr2.get(0).getTextParagraph().getParagraphStyle().getCharactersCovered());
		assertMatchesSLTWC(ssRichC);
		assertMatchesFileC(ssRichC);

		// Reset the text on a shared paragraph
		oldText = s7rtr1.get(0).getRawText();
		s7rtr1.get(0).setText( oldText );
        HSLFTextParagraph.storeText(s7tr.get(1));
		assertEquals(oldText, s7rtr1.get(0).getRawText());
		assertEquals(oldText.length(), s7rtr1.get(0).getCharacterStyle().getCharactersCovered());
		assertMatchesSLTWC(ssRichC);
		assertMatchesFileC(ssRichC);

		// Reset the text on a shared paragraph+character
		s7rtr1.get(0).setText( s7rtr1.get(0).getRawText() );
		HSLFTextParagraph.storeText(s7tr.get(1));
		assertMatchesSLTWC(ssRichC);
		assertMatchesFileC(ssRichC);
	}

	/**
	 * Opens a new copy of SlideShow C, writes the active
	 *  SlideListWithText out, and compares it to the write
	 *  out of the supplied SlideShow. Also compares the
	 *  contents.
	 */
	private void assertMatchesSLTWC(HSLFSlideShow s) throws IOException {
		// Grab a new copy of slideshow C
		HSLFSlideShow refC = HSLFTestDataSamples.getSlideShow(filenameC);

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
			assertArrayEquals(r_rb, s_rb);
		}

		// Check the bytes are the same
		assertArrayEquals(raw_slwt, s_slwt);
	}

	/**
	 * Checks that the supplied slideshow still matches the bytes
	 *  of slideshow c
	 */
	private static void assertMatchesFileC(HSLFSlideShow s) throws IOException {
		// Grab the bytes of the file
	    POIFSFileSystem fs = new POIFSFileSystem(HSLFTestDataSamples.openSampleFileStream(filenameC));
	    InputStream is = fs.createDocumentInputStream(HSLFSlideShow.POWERPOINT_DOCUMENT);
	    byte[] raw_file = IOUtils.toByteArray(is);
	    is.close();
	    fs.close();

		// Now write out the slideshow
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
		s.write(baos);
		fs = new POIFSFileSystem(new ByteArrayInputStream(baos.toByteArray()));
		is = fs.createDocumentInputStream(HSLFSlideShow.POWERPOINT_DOCUMENT);
		byte[] raw_ss = IOUtils.toByteArray(is);
        is.close();
        fs.close();

		// different paragraph mask, because of sanitizing
		raw_ss[169030] = 0x0a;

		// Ensure they're the same
		assertArrayEquals(raw_file, raw_ss);
	}

	private byte[] writeRecord( org.apache.poi.hslf.record.Record r) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		r.writeOut(baos);
		return baos.toByteArray();
	}

    @Test
	void testIndentationLevel() throws Exception {
		HSLFSlideShow ppt = HSLFTestDataSamples.getSlideShow("ParagraphStylesShorterThanCharStyles.ppt");
		for (HSLFSlide sl : ppt.getSlides()) {
			for (List<HSLFTextParagraph> txt : sl.getTextParagraphs()) {
				for (HSLFTextParagraph p : txt) {
					int indent = p.getIndentLevel();
					assertTrue(indent >= 0 && indent <= 4 );
				}

			}
		}
		ppt.close();
	}

    @Test
	void testReadParagraphStyles() throws Exception {
		try (HSLFSlideShow ppt = HSLFTestDataSamples.getSlideShow("bullets.ppt")) {
			HSLFTextParagraph rt;
			List<List<HSLFTextParagraph>> txt;
			List<HSLFSlide> slide = ppt.getSlides();
			assertEquals(2, slide.size());

			txt = slide.get(0).getTextParagraphs();
			assertEquals(2, txt.size());

			assertEquals("Title text", HSLFTextParagraph.getRawText(txt.get(0)));
			assertEquals(1, txt.get(0).size());
			rt = txt.get(0).get(0);
			assertFalse(rt.isBullet());

			String expected =
				"This is a text placeholder that \r" +
					"follows the design pattern\r" +
					"Defined in the slide master\r" +
					"and has bullets by default";
			assertEquals(expected, HSLFTextParagraph.getRawText(txt.get(1)));
			assertEquals(4, txt.get(1).size());
			rt = txt.get(1).get(0);
			assertNotNull(rt.getBulletChar());
			assertEquals('\u2022', (char) rt.getBulletChar());
			assertTrue(rt.isBullet());


			txt = slide.get(1).getTextParagraphs();
			assertEquals(2, txt.size());

			expected =
				"I\u2019m a text box\r" +
					"With bullets\r" +
					"That follow the design pattern\r" +
					"From the slide master";
			assertEquals(expected, HSLFTextParagraph.getRawText(txt.get(0)));
			assertEquals(4, txt.get(0).size());
			rt = txt.get(0).get(0);
			assertTrue(rt.isBullet());
			assertNotNull(rt.getBulletChar());
			assertEquals('\u2022', (char) rt.getBulletChar());

			expected =
				"I\u2019m a text box with user-defined\r" +
					"bullet character";
			assertEquals(expected, HSLFTextParagraph.getRawText(txt.get(1)));
			assertEquals(2, txt.get(1).size());
			rt = txt.get(1).get(0);
			assertTrue(rt.isBullet());
			assertNotNull(rt.getBulletChar());
			assertEquals('\u263A', (char) rt.getBulletChar());
		}
	}

    @Test
	void testSetParagraphStyles() throws IOException {
		HSLFSlideShow ppt1 = new HSLFSlideShow();

		HSLFSlide slide = ppt1.createSlide();

		HSLFTextBox shape = new HSLFTextBox();
		shape.setText(
				"Hello, World!\r" +
				"This should be\r" +
				"Multiline text");
        HSLFTextParagraph rt = shape.getTextParagraphs().get(0);
        HSLFTextRun tr = rt.getTextRuns().get(0);
		tr.setFontSize(42d);
		rt.setBullet(true);
		rt.setLeftMargin(50d);
		rt.setIndent(0d);
		rt.setBulletChar('\u263A');
		slide.addShape(shape);

		assertNotNull(tr.getFontSize());
		assertEquals(42.0, tr.getFontSize(), 0);
		assertTrue(rt.isBullet());
		assertNotNull(rt.getLeftMargin());
		assertEquals(50.0, rt.getLeftMargin(), 0);
		assertNotNull(rt.getIndent());
		assertEquals(0, rt.getIndent(), 0);
		assertNotNull(rt.getBulletChar());
		assertEquals('\u263A', (char)rt.getBulletChar());

		shape.setAnchor(new java.awt.Rectangle(50, 50, 500, 300));
		slide.addShape(shape);

		//serialize and read again
		HSLFSlideShow ppt2 = HSLFTestDataSamples.writeOutAndReadBack(ppt1);
		slide = ppt2.getSlides().get(0);
		shape = (HSLFTextBox)slide.getShapes().get(0);
		rt = shape.getTextParagraphs().get(0);
		tr = rt.getTextRuns().get(0);
		assertNotNull(tr.getFontSize());
		assertEquals(42.0, tr.getFontSize(), 0);
		assertTrue(rt.isBullet());
		assertNotNull(rt.getLeftMargin());
		assertEquals(50.0, rt.getLeftMargin(), 0);
		assertNotNull(rt.getIndent());
		assertEquals(0, rt.getIndent(), 0);
		assertNotNull(rt.getBulletChar());
		assertEquals('\u263A', (char)rt.getBulletChar());
		ppt2.close();
		ppt1.close();
	}

    @Test
	void testAddText() throws Exception {
		try (HSLFSlideShow ppt1 = HSLFTestDataSamples.getSlideShow("bullets.ppt")) {

			HSLFTextParagraph rt;
			HSLFTextRun tr;
			List<List<HSLFTextParagraph>> txt;
			List<HSLFSlide> slides = ppt1.getSlides();

			assertEquals(2, slides.size());
			txt = slides.get(0).getTextParagraphs();
			assertEquals(2, txt.size());

			assertEquals("Title text", HSLFTextParagraph.getRawText(txt.get(0)));
			assertEquals(1, txt.get(0).size());
			rt = txt.get(0).get(0);
			assertFalse(rt.isBullet());

			// Add some new text
			HSLFTextParagraph.appendText(txt.get(0), "Foo! I'm new!", true);
			assertEquals(2, txt.get(0).size());

			rt = txt.get(0).get(0);
			tr = rt.getTextRuns().get(0);
			assertFalse(tr.isBold());
			assertEquals("Title text\r", tr.getRawText());
			rt = txt.get(0).get(1);
			tr = rt.getTextRuns().get(0);
			assertFalse(tr.isBold());
			assertEquals("Foo! I'm new!", tr.getRawText());
			tr.setBold(true);
			HSLFTextParagraph.storeText(txt.get(0));

			// And some more, attributes will be copied from previous run
			HSLFTextParagraph.appendText(txt.get(0), "Me too!", true);
			HSLFTextParagraph.storeText(txt.get(0));
			assertEquals(3, txt.get(0).size());
			rt = txt.get(0).get(0);
			tr = rt.getTextRuns().get(0);
			assertFalse(tr.isBold());
			assertEquals("Title text\r", tr.getRawText());
			rt = txt.get(0).get(1);
			tr = rt.getTextRuns().get(0);
			assertTrue(tr.isBold());
			assertEquals("Foo! I'm new!\r", tr.getRawText());
			rt = txt.get(0).get(2);
			tr = rt.getTextRuns().get(0);
			assertTrue(tr.isBold());
			assertEquals("Me too!", tr.getRawText());

			// Save and re-open
			try (HSLFSlideShow ppt2 = HSLFTestDataSamples.writeOutAndReadBack(ppt1)) {
				slides = ppt2.getSlides();

				assertEquals(2, slides.size());

				txt = slides.get(0).getTextParagraphs();
				assertEquals(2, txt.size());
				assertEquals(3, txt.get(0).size());
				rt = txt.get(0).get(0);
				tr = rt.getTextRuns().get(0);
				assertFalse(tr.isBold());
				assertEquals("Title text\r", tr.getRawText());
				rt = txt.get(0).get(1);
				tr = rt.getTextRuns().get(0);
				assertTrue(tr.isBold());
				assertEquals("Foo! I'm new!\r", tr.getRawText());
				rt = txt.get(0).get(2);
				tr = rt.getTextRuns().get(0);
				assertTrue(tr.isBold());
				assertEquals("Me too!", tr.getRawText());
			}
		}
	}

    @Test
	void testChineseParagraphs() {
      List<HSLFTextRun> rts;
      HSLFTextRun rt;
      List<List<HSLFTextParagraph>> txt;
      List<HSLFSlide> slides = ssChinese.getSlides();

      // One slide
      assertEquals(1, slides.size());

      // One block of text within that
      txt = slides.get(0).getTextParagraphs();
      assertEquals(1, txt.size());

      // One rich block of text in that - text is all the same style
      // TODO Is this completely correct?
      rts = txt.get(0).get(0).getTextRuns();
      assertEquals(1, rts.size());
      rt = rts.get(0);

      // Check we can get the english text out of that
      String text = rt.getRawText();
      assertContains(text, "Single byte");
      // And the chinese
      assertContains(txt.get(0).get(3).getTextRuns().get(0).getRawText(), "\uff8a\uff9d\uff76\uff78");

      // It isn't bold or italic
      assertFalse(rt.isBold());
      assertFalse(rt.isItalic());

      // Font is Calibri
      assertEquals("Calibri", rt.getFontFamily());
	}
}
