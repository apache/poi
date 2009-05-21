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

/**
 * Tests that SlideShow returns Sheets in the right order
 *
 * @author Nick Burch (nick at torchbox dot com)
 */
public final class TestSlideOrdering extends TestCase {
	// Simple slideshow, record order matches slide order
	private SlideShow ssA;
	// Complex slideshow, record order doesn't match slide order
	private SlideShow ssB;

    public TestSlideOrdering() throws Exception {
		String dirname = System.getProperty("HSLF.testdata.path");

		String filenameA = dirname + "/basic_test_ppt_file.ppt";
		HSLFSlideShow hssA = new HSLFSlideShow(filenameA);
		ssA = new SlideShow(hssA);

		String filenameB = dirname + "/incorrect_slide_order.ppt";
		HSLFSlideShow hssB = new HSLFSlideShow(filenameB);
		ssB = new SlideShow(hssB);
    }

    /**
     * Test the simple case - record order matches slide order
     */
    public void testSimpleCase() throws Exception {
    	assertEquals(2, ssA.getSlides().length);

    	Slide s1 = ssA.getSlides()[0];
    	Slide s2 = ssA.getSlides()[1];

    	String[] firstTRs = new String[] {
    			"This is a test title",
    			"This is the title on page 2"
    	};

    	assertEquals(firstTRs[0], s1.getTextRuns()[0].getText());
    	assertEquals(firstTRs[1], s2.getTextRuns()[0].getText());
    }

    /**
     * Test the complex case - record order differs from slide order
     */
    public void testComplexCase() throws Exception {
    	assertEquals(3, ssB.getSlides().length);

    	Slide s1 = ssB.getSlides()[0];
    	Slide s2 = ssB.getSlides()[1];
    	Slide s3 = ssB.getSlides()[2];

    	String[] firstTRs = new String[] {
    			"Slide 1",
    			"Slide 2",
    			"Slide 3"
    	};

    	assertEquals(firstTRs[0], s1.getTextRuns()[0].getText());
    	assertEquals(firstTRs[1], s2.getTextRuns()[0].getText());
    	assertEquals(firstTRs[2], s3.getTextRuns()[0].getText());
    }

    /**
     * Assert that the order of slides is correct.
     *
     * @param filename  file name of the slide show to assert
     * @param titles    array of reference slide titles
     */
    protected void assertSlideOrdering(String filename, String[] titles) throws Exception {
        SlideShow ppt = new SlideShow(new HSLFSlideShow(filename));
        Slide[] slide = ppt.getSlides();

        assertEquals(titles.length, slide.length);
        for (int i = 0; i < slide.length; i++) {
            String title = slide[i].getTitle();
            assertEquals("Wrong slide title in " + filename, titles[i], title);
        }
    }

    public void testTitles() throws Exception{
        String dirname = System.getProperty("HSLF.testdata.path");

        assertSlideOrdering(dirname + "/basic_test_ppt_file.ppt",
                new String[]{
                    "This is a test title",
                    "This is the title on page 2"
                });

        assertSlideOrdering(dirname + "/incorrect_slide_order.ppt",
                new String[]{
                    "Slide 1",
                    "Slide 2",
                    "Slide 3"
                });

        assertSlideOrdering(dirname + "/next_test_ppt_file.ppt",
                new String[]{
                    "This is a test title",
                    "This is the title on page 2"
                });

        assertSlideOrdering(dirname + "/Single_Coloured_Page.ppt",
                new String[]{
                    "This is a title, it" + (char)0x2019 +"s in black"
                });

        assertSlideOrdering(dirname + "/Single_Coloured_Page_With_Fonts_and_Alignments.ppt",
                new String[]{
                    "This is a title, it"+ (char)0x2019 +"s in black"
                });

        assertSlideOrdering(dirname + "/ParagraphStylesShorterThanCharStyles.ppt",
                new String[]{
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
                    "SUMMARY",
                });
    }
}
