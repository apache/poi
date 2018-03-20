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


import org.apache.poi.POIDataSamples;

import junit.framework.TestCase;

/**
 * Tests that SlideShow returns MetaSheets which have the right text in them
 *
 * @author Nick Burch (nick at torchbox dot com)
 */
public final class TestNotesText extends TestCase {
	// SlideShow primed on the test data
	private final HSLFSlideShow ss;

	public TestNotesText() throws Exception {
        POIDataSamples slTests = POIDataSamples.getSlideShowInstance();
		HSLFSlideShowImpl hss = new HSLFSlideShowImpl(slTests.openResourceAsStream("basic_test_ppt_file.ppt"));
		ss = new HSLFSlideShow(hss);
	}

	public void testNotesOne() {
		HSLFNotes notes = ss.getNotes().get(0);

		String[] expectText = new String[] {"These are the notes for page 1"};
		assertEquals(expectText.length, notes.getTextParagraphs().size());
		for(int i=0; i<expectText.length; i++) {
			assertEquals(expectText[i], HSLFTextParagraph.getRawText(notes.getTextParagraphs().get(i)));
		}
	}

	public void testNotesTwo() {
		HSLFNotes notes = ss.getNotes().get(1);
		String[] expectText = new String[] {"These are the notes on page two, again lacking formatting"};
		assertEquals(expectText.length, notes.getTextParagraphs().size());
		for(int i=0; i<expectText.length; i++) {
			assertEquals(expectText[i], HSLFTextParagraph.getRawText(notes.getTextParagraphs().get(i)));
		}
	}
}
