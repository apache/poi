
/* ====================================================================
   Copyright 2002-2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
        


package org.apache.poi.hslf.model;


import junit.framework.TestCase;

import org.apache.poi.hslf.HSLFSlideShow;
import org.apache.poi.hslf.record.TextBytesAtom;
import org.apache.poi.hslf.record.TextCharsAtom;
import org.apache.poi.hslf.record.TextHeaderAtom;
import org.apache.poi.hslf.usermodel.SlideShow;

/**
 * Tests for TextRuns
 *
 * @author Nick Burch (nick at torchbox dot com)
 */
public class TestTextRun extends TestCase {
	// SlideShow primed on the test data
	private SlideShow ss;
	private HSLFSlideShow hss;

    public TestTextRun() throws Exception {
		String dirname = System.getProperty("HSLF.testdata.path");
		String filename = dirname + "/basic_test_ppt_file.ppt";
		HSLFSlideShow hss = new HSLFSlideShow(filename);
		ss = new SlideShow(hss);
    }

    /**
     * Test to ensure that getting the text works correctly
     */
    public void testGetText() throws Exception {
		Slide slideOne = ss.getSlides()[0];
		TextRun[] textRuns = slideOne.getTextRuns();

		assertEquals(2, textRuns.length);
		
		// Get text works with \n
		assertEquals("This is a test title", textRuns[0].getText());
		assertEquals("This is a test subtitle\nThis is on page 1", textRuns[1].getText());
		
		// Raw text has \r instead
		assertEquals("This is a test title", textRuns[0].getRawText());
		assertEquals("This is a test subtitle\rThis is on page 1", textRuns[1].getRawText());
    }
    
    /**
     * Test to ensure changing non rich text bytes->bytes works correctly
     */
    public void testSetText() throws Exception {
		Slide slideOne = ss.getSlides()[0];
		TextRun[] textRuns = slideOne.getTextRuns();
		TextRun run = textRuns[0];

		// Check current text
		assertEquals("This is a test title", run.getText());
		
		// Change
		String changeTo = "New test title";
		run.setText(changeTo);
		assertEquals(changeTo, run.getText());
		
		// Ensure trailing \n's get stripped
		run.setText(changeTo + "\n");
		assertEquals(changeTo, run.getText());
    }

    /**
     * Test to ensure that changing non rich text between bytes and
     *  chars works correctly
     */
    public void testAdvancedSetText() throws Exception {
		Slide slideOne = ss.getSlides()[0];
		TextRun run = slideOne.getTextRuns()[0];
		
		TextHeaderAtom tha = run._headerAtom;
		TextBytesAtom tba = run._byteAtom;
		TextCharsAtom tca = run._charAtom;

    	// Bytes -> Bytes
		assertNull(tca);
		assertNotNull(tba);
		assertFalse(run._isUnicode);
		assertEquals("This is a test title", run.getText());
		
		String changeBytesOnly = "New Test Title";
		run.setText(changeBytesOnly);
		tba = run._byteAtom;
		tca = run._charAtom;
		
		assertEquals(changeBytesOnly, run.getText());
		assertFalse(run._isUnicode);
		assertNull(tca);
		assertNotNull(tba);
		
    	// Bytes -> Chars
		assertNull(tca);
		assertNotNull(tba);
		assertFalse(run._isUnicode);
		assertEquals(changeBytesOnly, run.getText());
		
		String changeByteChar = "This is a test title with a '\u0121' g with a dot";
		run.setText(changeByteChar);
		tba = run._byteAtom;
		tca = run._charAtom;
		
		assertEquals(changeByteChar, run.getText());
		assertTrue(run._isUnicode);
		assertNotNull(tca);
		assertNull(tba);
		
    	// Chars -> Chars
		assertNull(tba);
		assertNotNull(tca);
		assertTrue(run._isUnicode);
		assertEquals(changeByteChar, run.getText());
		
		String changeCharChar = "This is a test title with a '\u0147' N with a hat";
		run.setText(changeCharChar);
		tba = run._byteAtom;
		tca = run._charAtom;
		
		assertEquals(changeCharChar, run.getText());
		assertTrue(run._isUnicode);
		assertNotNull(tca);
		assertNull(tba);
    }
    
    /**
     * Tests to ensure that non rich text has the right default rich text run
     *  set up for it
     */
    public void testGetRichTextNonRich() throws Exception {
    	// TODO
    }
    
    /**
     * Tests to ensure that the rich text runs are built up correctly
     */
    public void testGetRichText() throws Exception {
    	// TODO
    }
    
    /**
     * Tests to ensure that setting the text where the text is rich
     *  sets everything to the same styling
     */
    public void testSetTextWhereRich() throws Exception {
    	// TODO
    }
    
    /**
     * Test to ensure the right stuff happens if we change the text
     *  in a rich text run, that doesn't happen to actually be rich
     */
    public void testChangeTextInRichTextRunNonRich() throws Exception {
    	// TODO
    }

    /**
     * Tests to ensure changing the text within rich text runs works
     *  correctly
     */
    public void testChangeTextInRichTextRun() throws Exception {
    	// TODO
    }
}
