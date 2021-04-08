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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.List;

import org.apache.poi.hslf.HSLFTestDataSamples;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests that SlideShow returns Sheets which have the right text in them
 */
public final class TestSheetText {
	// SlideShow primed on the test data
	private HSLFSlideShow ss;

	@BeforeEach
	void init() throws IOException {
		ss = HSLFTestDataSamples.getSlideShow("basic_test_ppt_file.ppt");
	}

	@AfterEach
	void tearDown() throws IOException {
	    ss.close();
	}

	@Test
	void testSheetOne() {
		HSLFSheet slideOne = ss.getSlides().get(0);

		String[] expectText = new String[] {"This is a test title","This is a test subtitle\rThis is on page 1"};
		assertEquals(expectText.length, slideOne.getTextParagraphs().size());
		int i = 0;
		for(List<HSLFTextParagraph> textParas : slideOne.getTextParagraphs()) {
			assertEquals(expectText[i++], HSLFTextParagraph.getRawText(textParas));
		}
	}

	void testSheetTwo() {
		HSLFSheet slideTwo = ss.getSlides().get(1);
		String[] expectText = new String[] {"This is the title on page 2","This is page two\rIt has several blocks of text\rNone of them have formatting"};
		assertEquals(expectText.length, slideTwo.getTextParagraphs().size());
        int i = 0;
        for(List<HSLFTextParagraph> textParas : slideTwo.getTextParagraphs()) {
            assertEquals(expectText[i++], HSLFTextParagraph.getRawText(textParas));
        }
	}

	/**
	 * Check we can still get the text from a file where the
	 *  TextProps don't have enough data.
	 * (Make sure we don't screw up / throw an exception etc)
	 */
	void testWithShortTextPropData() throws IOException {
		HSLFSlideShow sss = HSLFTestDataSamples.getSlideShow("iisd_report.ppt");

		// Should come out with 10 slides, no notes
		assertEquals(10, sss.getSlides().size());
		assertEquals(0, sss.getNotes().size());

		// Check text on first slide
		HSLFSlide s = sss.getSlides().get(0);
		String exp =
			"Realizing the Development Dividend:\n" +
			"Community Capacity Building and CDM.\n" +
			"Can they co-exist?\n\n" +
			"Gay Harley\n" +
			"Clean Development Alliance\n" +
			"COP 11 \u2013 MOP 1\n" + // special long hyphen
			"December 5, 2005\n";

		assertEquals(1, s.getTextParagraphs().size());
		assertEquals(exp, HSLFTextParagraph.getRawText(s.getTextParagraphs().get(0)));
		sss.close();
	}
}
