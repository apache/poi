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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import org.apache.poi.hslf.HSLFTestDataSamples;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests that SlideShow returns Sheets in the right order
 */
public final class TestSlideOrdering {
	// Simple slideshow, record order matches slide order
	private HSLFSlideShow ssA;
	// Complex slideshow, record order doesn't match slide order
	private HSLFSlideShow ssB;

	@Before
	public void init() throws IOException {
		ssA = HSLFTestDataSamples.getSlideShow("basic_test_ppt_file.ppt");
		ssB = HSLFTestDataSamples.getSlideShow("incorrect_slide_order.ppt");
	}

	@After
	public void tearDown() throws IOException {
	    ssA.close();
	    ssB.close();
	}

	/**
	 * Test the simple case - record order matches slide order
	 */
	@Test
	public void testSimpleCase() {
		assertEquals(2, ssA.getSlides().size());

		HSLFSlide s1 = ssA.getSlides().get(0);
		HSLFSlide s2 = ssA.getSlides().get(1);

		String[] firstTRs = new String[] { "This is a test title", "This is the title on page 2" };

		assertEquals(firstTRs[0], HSLFTextParagraph.getRawText(s1.getTextParagraphs().get(0)));
		assertEquals(firstTRs[1], HSLFTextParagraph.getRawText(s2.getTextParagraphs().get(0)));
	}

	/**
	 * Test the complex case - record order differs from slide order
	 */
    @Test
	public void testComplexCase() {
		assertEquals(3, ssB.getSlides().size());
		int i=1;
		for (HSLFSlide s : ssB.getSlides()) {
		    assertEquals("Slide "+(i++), HSLFTextParagraph.getRawText(s.getTextParagraphs().get(0)));
		}
	}

	/**
	 * Assert that the order of slides is correct.
	 *
	 * @param filename
	 *            file name of the slide show to assert
	 * @param titles
	 *            array of reference slide titles
	 */
	protected void assertSlideOrdering(String filename, String[] titles) throws IOException {
        HSLFSlideShow ppt = HSLFTestDataSamples.getSlideShow(filename);
		List<HSLFSlide> slide = ppt.getSlides();

		assertEquals(titles.length, slide.size());
		for (int i = 0; i < slide.size(); i++) {
			String title = slide.get(i).getTitle();
			assertEquals("Wrong slide title in " + filename, titles[i], title);
		}
		ppt.close();
	}

    @Test
	public void testTitles() throws Exception {
		assertSlideOrdering("basic_test_ppt_file.ppt", new String[] {
				"This is a test title", "This is the title on page 2" });

		assertSlideOrdering("incorrect_slide_order.ppt", new String[] { "Slide 1",
				"Slide 2", "Slide 3" });

		assertSlideOrdering("next_test_ppt_file.ppt", new String[] {
				"This is a test title", "This is the title on page 2" });

		assertSlideOrdering("Single_Coloured_Page.ppt",
				new String[] { "This is a title, it" + (char) 0x2019 + "s in black" });

		assertSlideOrdering("Single_Coloured_Page_With_Fonts_and_Alignments.ppt",
				new String[] { "This is a title, it" + (char) 0x2019 + "s in black" });

		assertSlideOrdering(
				"ParagraphStylesShorterThanCharStyles.ppt",
				new String[] {
						"ROMANCE: AN ANALYSIS",
						"AGENDA",
						"You are an important supplier of various items that I need",
						'\n' + "Although The Psycho set back my relationship process, recovery is luckily enough under way",
						"Since the time that we seriously go out together, you rank highly among existing relationships",
						"Although our personal interests are mostly compatible, the greatest gap exists in Sex and Shopping",
						"Your physical characteristics are strong when compared with your competition",
						"The combination of your high physical appearance and personality rank you highly, although your sister is also a top prospect",
						"When people found out that we were going out, their responses have been mixed",
						"The benchmark of relationship lifecycles, suggests that we are on schedule",
						"In summary we can say that we are on the right track, but we must remain aware of possible roadblocks ",
						"THE ANSWER",
						"Unfortunately a huge disconnect exists between my needs and your existing service",
						"SUMMARY", });
	}
}
