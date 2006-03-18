package org.apache.poi.hslf.usermodel;

import org.apache.poi.hslf.HSLFSlideShow;
import org.apache.poi.hslf.model.Slide;
import org.apache.poi.hslf.model.TextRun;

import junit.framework.TestCase;

/**
 * Test that the friendly getters and setters on RichTextRun
 *  behave as expected.
 * (model.TestTextRun tests the other functionality)
 * @author Nick Burch (nick at torchbox dot com)
 */
public class TestRichTextRun extends TestCase {
	// SlideShow primed on the test data
	private SlideShow ss;
	private SlideShow ssRichA;
	private SlideShow ssRichB;
	private HSLFSlideShow hss;
	private HSLFSlideShow hssRichA;
	private HSLFSlideShow hssRichB;
	
    protected void setUp() throws Exception {
		String dirname = System.getProperty("HSLF.testdata.path");
		
		// Basic (non rich) test file
		String filename = dirname + "/basic_test_ppt_file.ppt";
		hss = new HSLFSlideShow(filename);
		ss = new SlideShow(hss);
		
		// Rich test file A
		filename = dirname + "/Single_Coloured_Page.ppt";
		hssRichA = new HSLFSlideShow(filename);
		ssRichA = new SlideShow(hssRichA);
		
		// Rich test file B
		filename = dirname + "/Single_Coloured_Page_With_Fonts_and_Alignments.ppt";
		hssRichB = new HSLFSlideShow(filename);
		ssRichB = new SlideShow(hssRichB);
	}

	/**
	 * Test the stuff about getting/setting bold
	 *  on a non rich text run
	 */
	public void testBoldNonRich() throws Exception {
		Slide slideOne = ss.getSlides()[0];
		TextRun[] textRuns = slideOne.getTextRuns();
		RichTextRun rtr = textRuns[0].getRichTextRuns()[0];
		
		assertNull(rtr._getRawCharacterStyle());
		assertNull(rtr._getRawParagraphStyle());
		assertFalse(rtr.isBold());
		
		// Now set it to not bold
		rtr.setBold(false);
		assertNotNull(rtr._getRawCharacterStyle());
		assertNotNull(rtr._getRawParagraphStyle());
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
	public void testBoldRich() throws Exception {
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
	public void testFontSize() throws Exception {
		Slide slideOne = ss.getSlides()[0];
		TextRun[] textRuns = slideOne.getTextRuns();
		RichTextRun rtr = textRuns[0].getRichTextRuns()[0];
		
		Slide slideOneR = ssRichB.getSlides()[0];
		TextRun[] textRunsR = slideOneR.getTextRuns();
		RichTextRun rtrRa = textRunsR[0].getRichTextRuns()[0];
		RichTextRun rtrRb = textRunsR[1].getRichTextRuns()[0];
		RichTextRun rtrRc = textRunsR[1].getRichTextRuns()[3];

		// Start off with rich one
		// First run has defaults
		assertEquals(-1, rtrRa.getFontSize());
		assertEquals(null, rtrRa.getFontName());
		// Second is size 20, default font
		assertEquals(20, rtrRb.getFontSize());
		assertEquals(null, rtrRb.getFontName());
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
		assertEquals(-1, rtr.getFontSize());
		assertEquals(null, rtr.getFontName());
		assertEquals(1, ss.getFontCollection().getChildRecords().length); // Default
		assertNull(rtr._getRawCharacterStyle());
		assertNull(rtr._getRawParagraphStyle());
		
		// Change Font size
		rtr.setFontSize(99);
		assertEquals(99, rtr.getFontSize());
		assertEquals(null, rtr.getFontName());
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
}
