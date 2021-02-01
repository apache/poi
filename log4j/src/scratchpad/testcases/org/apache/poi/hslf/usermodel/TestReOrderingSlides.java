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

import org.apache.poi.POIDataSamples;
import org.apache.poi.hslf.HSLFTestDataSamples;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests that SlideShow can re-order slides properly
 */
public final class TestReOrderingSlides {
	// A SlideShow with one slide
	private HSLFSlideShowImpl hss_one;
	private HSLFSlideShow ss_one;

	// A SlideShow with two slides
	private HSLFSlideShowImpl hss_two;
	private HSLFSlideShow ss_two;

	// A SlideShow with three slides
	private HSLFSlideShowImpl hss_three;
	private HSLFSlideShow ss_three;

	/**
	 * Create/open the slideshows
	 */
	@BeforeEach
	void setUp() throws Exception {
        POIDataSamples slTests = POIDataSamples.getSlideShowInstance();

		hss_one = new HSLFSlideShowImpl(slTests.openResourceAsStream("Single_Coloured_Page.ppt"));
		ss_one = new HSLFSlideShow(hss_one);

		hss_two = new HSLFSlideShowImpl(slTests.openResourceAsStream("basic_test_ppt_file.ppt"));
		ss_two = new HSLFSlideShow(hss_two);

		hss_three = new HSLFSlideShowImpl(slTests.openResourceAsStream("incorrect_slide_order.ppt"));
		ss_three = new HSLFSlideShow(hss_three);
	}

	/**
	 * Test that we can "re-order" a slideshow with only 1 slide on it
	 */
	@Test
	void testReOrder1() throws IOException {
		// Has one slide
		assertEquals(1, ss_one.getSlides().size());
		HSLFSlide s1 = ss_one.getSlides().get(0);

		// Check slide 1 is as expected
		assertEquals(256, s1._getSheetNumber());
		assertEquals(3, s1._getSheetRefId());
		assertEquals(1, s1.getSlideNumber());

		// Now move it to one
		ss_one.reorderSlide(1, 1);

		// Write out, and read back in
        HSLFSlideShow ss_read = HSLFTestDataSamples.writeOutAndReadBack(ss_one);

		// Check it still has 1 slide
		assertEquals(1, ss_read.getSlides().size());

		// And check it's as expected
		s1 = ss_read.getSlides().get(0);
		assertEquals(256, s1._getSheetNumber());
		assertEquals(3, s1._getSheetRefId());
		assertEquals(1, s1.getSlideNumber());

		ss_read.close();
	}

	/**
	 * Test doing a dummy re-order on a slideshow with
	 *  two slides in it
	 */
    @Test
	void testReOrder2() throws IOException {
		// Has two slides
		assertEquals(2, ss_two.getSlides().size());
		HSLFSlide s1 = ss_two.getSlides().get(0);
		HSLFSlide s2 = ss_two.getSlides().get(1);

		// Check slide 1 is as expected
		assertEquals(256, s1._getSheetNumber());
		assertEquals(4, s1._getSheetRefId()); // master has notes
		assertEquals(1, s1.getSlideNumber());
		// Check slide 2 is as expected
		assertEquals(257, s2._getSheetNumber());
		assertEquals(6, s2._getSheetRefId()); // master and 1 have notes
		assertEquals(2, s2.getSlideNumber());

		// Don't swap them around
		ss_two.reorderSlide(2, 2);

		// Write out, and read back in
        HSLFSlideShow ss_read = HSLFTestDataSamples.writeOutAndReadBack(ss_two);

		// Check it still has 2 slides
		assertEquals(2, ss_read.getSlides().size());

		// And check it's as expected
		s1 = ss_read.getSlides().get(0);
		s2 = ss_read.getSlides().get(1);
		assertEquals(256, s1._getSheetNumber());
		assertEquals(4, s1._getSheetRefId());
		assertEquals(1, s1.getSlideNumber());
		assertEquals(257, s2._getSheetNumber());
		assertEquals(6, s2._getSheetRefId());
		assertEquals(2, s2.getSlideNumber());

        ss_read.close();
    }

	/**
	 * Test re-ordering slides in a slideshow with 2 slides on it
	 */
    @Test
	void testReOrder2swap() throws IOException {
		// Has two slides
		assertEquals(2, ss_two.getSlides().size());
		HSLFSlide s1 = ss_two.getSlides().get(0);
		HSLFSlide s2 = ss_two.getSlides().get(1);

		// Check slide 1 is as expected
		assertEquals(256, s1._getSheetNumber());
		assertEquals(4, s1._getSheetRefId()); // master has notes
		assertEquals(1, s1.getSlideNumber());
		// Check slide 2 is as expected
		assertEquals(257, s2._getSheetNumber());
		assertEquals(6, s2._getSheetRefId()); // master and 1 have notes
		assertEquals(2, s2.getSlideNumber());

		// Swap them around
		ss_two.reorderSlide(2, 1);

		// Write out, and read back in
        HSLFSlideShow ss_read = HSLFTestDataSamples.writeOutAndReadBack(ss_two);

		// Check it still has 2 slides
		assertEquals(2, ss_read.getSlides().size());

		// And check it's as expected
		s1 = ss_read.getSlides().get(0);
		s2 = ss_read.getSlides().get(1);
		assertEquals(257, s1._getSheetNumber());
		assertEquals(6, s1._getSheetRefId());
		assertEquals(1, s1.getSlideNumber());
		assertEquals(256, s2._getSheetNumber());
		assertEquals(4, s2._getSheetRefId());
		assertEquals(2, s2.getSlideNumber());

		ss_read.close();
    }

	/**
	 * Test doing a dummy re-order on a slideshow with
	 *  three slides in it
	 */
    @Test
	void testReOrder3() throws IOException {
		// Has three slides
		assertEquals(3, ss_three.getSlides().size());
		HSLFSlide s1 = ss_three.getSlides().get(0);
		HSLFSlide s2 = ss_three.getSlides().get(1);
		HSLFSlide s3 = ss_three.getSlides().get(2);

		// Check slide 1 is as expected
		assertEquals(256, s1._getSheetNumber());
		assertEquals(3, s1._getSheetRefId()); // no notes on master
		assertEquals(1, s1.getSlideNumber());
		// Check slide 2 is as expected (was re-ordered from 3)
		assertEquals(258, s2._getSheetNumber());
		assertEquals(5, s2._getSheetRefId()); // no notes on slide
		assertEquals(2, s2.getSlideNumber());
		// Check slide 3 is as expected (was re-ordered from 2)
		assertEquals(257, s3._getSheetNumber());
		assertEquals(4, s3._getSheetRefId()); // no notes on slide
		assertEquals(3, s3.getSlideNumber());

		// Don't swap them around
		ss_three.reorderSlide(2, 2);

		// Write out, and read back in
        HSLFSlideShow ss_read = HSLFTestDataSamples.writeOutAndReadBack(ss_three);

		// Check it still has 3 slides
		assertEquals(3, ss_read.getSlides().size());

		// And check it's as expected
		s1 = ss_read.getSlides().get(0);
		s2 = ss_read.getSlides().get(1);
		s3 = ss_read.getSlides().get(2);

		assertEquals(256, s1._getSheetNumber());
		assertEquals(3, s1._getSheetRefId());
		assertEquals(1, s1.getSlideNumber());
		assertEquals(258, s2._getSheetNumber());
		assertEquals(5, s2._getSheetRefId());
		assertEquals(2, s2.getSlideNumber());
		assertEquals(257, s3._getSheetNumber());
		assertEquals(4, s3._getSheetRefId());
		assertEquals(3, s3.getSlideNumber());

        ss_read.close();
    }

	/**
	 * Test re-ordering slides in a slideshow with 3 slides on it
	 */
    @Test
	void testReOrder3swap() throws IOException {
		// Has three slides
		assertEquals(3, ss_three.getSlides().size());
		HSLFSlide s1 = ss_three.getSlides().get(0);
		HSLFSlide s2 = ss_three.getSlides().get(1);
		HSLFSlide s3 = ss_three.getSlides().get(2);

		// Check slide 1 is as expected
		assertEquals(256, s1._getSheetNumber());
		assertEquals(3, s1._getSheetRefId()); // no notes on master
		assertEquals(1, s1.getSlideNumber());
		// Check slide 2 is as expected (was re-ordered from 3)
		assertEquals(258, s2._getSheetNumber());
		assertEquals(5, s2._getSheetRefId()); // no notes on slide
		assertEquals(2, s2.getSlideNumber());
		// Check slide 3 is as expected (was re-ordered from 2)
		assertEquals(257, s3._getSheetNumber());
		assertEquals(4, s3._getSheetRefId()); // no notes on slide
		assertEquals(3, s3.getSlideNumber());

		// Put 3 in place of 1
		// (1 -> 2, 2 -> 3)
		ss_three.reorderSlide(3, 1);

		// refresh the slides
		s1 = ss_three.getSlides().get(0);
        s2 = ss_three.getSlides().get(1);
        s3 = ss_three.getSlides().get(2);

		assertEquals(1, s1.getSlideNumber());
        assertEquals(2, s2.getSlideNumber());
        assertEquals(3, s3.getSlideNumber());

        assertEquals("Slide 3", ((HSLFTextShape)s1.getShapes().get(0)).getText());
        assertEquals("Slide 1", ((HSLFTextShape)s3.getShapes().get(0)).getText());

		// Write out, and read back in
        HSLFSlideShow ss_read = HSLFTestDataSamples.writeOutAndReadBack(ss_three);

		// Check it still has 3 slides
		assertEquals(3, ss_read.getSlides().size());

		// And check it's as expected
		HSLFSlide _s1 = ss_read.getSlides().get(0);
		HSLFSlide _s2 = ss_read.getSlides().get(1);
		HSLFSlide _s3 = ss_read.getSlides().get(2);

        // 1 --> 3
        assertEquals(s1._getSheetNumber(), _s1._getSheetNumber());
		assertEquals(s1._getSheetRefId(), _s1._getSheetRefId());
		assertEquals(1, s1.getSlideNumber());

        // 2nd slide is not updated
        assertEquals(s2._getSheetNumber(), _s2._getSheetNumber());
        assertEquals(s2._getSheetRefId(), _s2._getSheetRefId());
		assertEquals(2, s2.getSlideNumber());

        // 3 --> 1
        assertEquals(s3._getSheetNumber(), _s3._getSheetNumber());
        assertEquals(s3._getSheetRefId(), _s3._getSheetRefId());
		assertEquals(3, s3.getSlideNumber());

		ss_read.close();
	}
}
