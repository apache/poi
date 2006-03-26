
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
        


package org.apache.poi.hslf.usermodel;


import junit.framework.TestCase;
import org.apache.poi.hslf.*;
import org.apache.poi.hslf.model.*;

/**
 * Tests that SlideShow returns the right number of Sheets and MetaSheets
 *
 * @author Nick Burch (nick at torchbox dot com)
 */
public class TestCounts extends TestCase {
	// SlideShow primed on the test data
	private SlideShow ss;

    public TestCounts() throws Exception {
		String dirname = System.getProperty("HSLF.testdata.path");
		String filename = dirname + "/basic_test_ppt_file.ppt";
		HSLFSlideShow hss = new HSLFSlideShow(filename);
		ss = new SlideShow(hss);
    }

    public void testSheetsCount() throws Exception {
		Slide[] slides = ss.getSlides();
		// Two sheets - master sheet is seperate
		assertEquals(2, slides.length);
		
		// They are slides 1+2
		assertEquals(1, slides[0].getSlideNumber());
		assertEquals(2, slides[1].getSlideNumber());
		
		// The internal IDs are 4 and 6
		assertEquals(4, slides[0].getSheetNumber());
		assertEquals(6, slides[1].getSheetNumber());
	}

    public void testNotesCount() throws Exception {
		Notes[] notes = ss.getNotes();
		// Two sheets -> two notes, plus the notes on the slide master
		assertEquals(3, notes.length);
		
		// First is for master
		assertEquals(-2147483648, notes[0].getSlideInternalNumber());
		
		// Next two are for the two slides
		assertEquals(256, notes[1].getSlideInternalNumber());
		assertEquals(257, notes[2].getSlideInternalNumber());
		
		// They go between the slides
		assertEquals(5, notes[1].getSheetNumber());
		assertEquals(7, notes[2].getSheetNumber());
	}
}
