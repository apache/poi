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


import junit.framework.TestCase;
import org.apache.poi.hslf.*;
import org.apache.poi.hslf.model.*;
import org.apache.poi.POIDataSamples;

/**
 * Tests that SlideShow returns the right number of Sheets and MetaSheets
 *
 * @author Nick Burch (nick at torchbox dot com)
 */
public final class TestCounts extends TestCase {
	// SlideShow primed on the test data
	private SlideShow ss;

	public TestCounts() throws Exception {
        POIDataSamples slTests = POIDataSamples.getSlideShowInstance();
		HSLFSlideShow hss = new HSLFSlideShow(slTests.openResourceAsStream("basic_test_ppt_file.ppt"));
		ss = new SlideShow(hss);
	}

	public void testSheetsCount() {
		Slide[] slides = ss.getSlides();
		// Two sheets - master sheet is separate
		assertEquals(2, slides.length);

		// They are slides 1+2
		assertEquals(1, slides[0].getSlideNumber());
		assertEquals(2, slides[1].getSlideNumber());

		// The ref IDs are 4 and 6
		assertEquals(4, slides[0]._getSheetRefId());
		assertEquals(6, slides[1]._getSheetRefId());

		// These are slides 1+2 -> 256+257
		assertEquals(256, slides[0]._getSheetNumber());
		assertEquals(257, slides[1]._getSheetNumber());
	}

	public void testNotesCount() {
		Notes[] notes = ss.getNotes();
		// Two sheets -> two notes
		// Note: there are also notes on the slide master
		//assertEquals(3, notes.length); // When we do slide masters
		assertEquals(2, notes.length);

		// First is for master
		//assertEquals(-2147483648, notes[0]._getSheetNumber());  // When we do slide masters

		// Next two are for the two slides
		assertEquals(256, notes[0]._getSheetNumber());
		assertEquals(257, notes[1]._getSheetNumber());

		// They happen to go between the two slides in Ref terms
		assertEquals(5, notes[0]._getSheetRefId());
		assertEquals(7, notes[1]._getSheetRefId());
	}
}
