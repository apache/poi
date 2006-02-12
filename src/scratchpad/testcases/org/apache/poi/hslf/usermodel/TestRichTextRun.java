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
	private SlideShow ssRich;
	private HSLFSlideShow hss;
	private HSLFSlideShow hssRich;
	
    protected void setUp() throws Exception {
		String dirname = System.getProperty("HSLF.testdata.path");
		
		// Basic (non rich) test file
		String filename = dirname + "/basic_test_ppt_file.ppt";
		hss = new HSLFSlideShow(filename);
		ss = new SlideShow(hss);
		
		// Rich test file
		filename = dirname + "/Single_Coloured_Page.ppt";
		hssRich = new HSLFSlideShow(filename);
		ssRich = new SlideShow(hssRich);
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
		Slide slideOneR = ssRich.getSlides()[0];
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
}
