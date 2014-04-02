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
 * Tests that SlideShow returns Sheets which have the right text in them
 *
 * @author Nick Burch (nick at torchbox dot com)
 */
public final class TestSheetText extends TestCase {
	// SlideShow primed on the test data
	private SlideShow ss;

	public TestSheetText() throws Exception {
        POIDataSamples slTests = POIDataSamples.getSlideShowInstance();
		HSLFSlideShow hss = new HSLFSlideShow(slTests.openResourceAsStream("basic_test_ppt_file.ppt"));
		ss = new SlideShow(hss);
	}

	public void testSheetOne() {
		Sheet slideOne = ss.getSlides()[0];

		String[] expectText = new String[] {"This is a test title","This is a test subtitle\nThis is on page 1"};
		assertEquals(expectText.length, slideOne.getTextRuns().length);
		for(int i=0; i<expectText.length; i++) {
			assertEquals(expectText[i], slideOne.getTextRuns()[i].getText());
		}
	}

	public void testSheetTwo() {
		Sheet slideTwo = ss.getSlides()[1];
		String[] expectText = new String[] {"This is the title on page 2","This is page two\nIt has several blocks of text\nNone of them have formatting"};
		assertEquals(expectText.length, slideTwo.getTextRuns().length);
		for(int i=0; i<expectText.length; i++) {
			assertEquals(expectText[i], slideTwo.getTextRuns()[i].getText());
		}
	}

	/**
	 * Check we can still get the text from a file where the
	 *  TextProps don't have enough data.
	 * (Make sure we don't screw up / throw an exception etc)
	 */
	public void testWithShortTextPropData() throws Exception {
        POIDataSamples slTests = POIDataSamples.getSlideShowInstance();
		HSLFSlideShow hss = new HSLFSlideShow(slTests.openResourceAsStream("iisd_report.ppt"));
		SlideShow sss = new SlideShow(hss);

		// Should come out with 10 slides, no notes
		assertEquals(10, sss.getSlides().length);
		assertEquals(0, sss.getNotes().length);

		// Check text on first slide
		Slide s = sss.getSlides()[0];
		String exp =
			"Realizing the Development Dividend:\n" +
			"Community Capacity Building and CDM.\n" +
			"Can they co-exist?\n\n" +
			"Gay Harley\n" +
			"Clean Development Alliance\n" +
			"COP 11 \u2013 MOP 1\n" + // special long hyphen
			"December 5, 2005\n";

		assertEquals(1, s.getTextRuns().length);
		assertEquals(exp, s.getTextRuns()[0].getText());
	}
}
