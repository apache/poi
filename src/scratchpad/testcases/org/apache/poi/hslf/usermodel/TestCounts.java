
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
		// Two sheets, plus some crap related to the master sheet
		assertEquals(3, slides.length);
	}

    public void testNotesCount() throws Exception {
		Notes[] notes = ss.getNotes();
		// Two sheets -> two notes, plus the notes on the slide master
		assertEquals(3, notes.length);
	}
}
