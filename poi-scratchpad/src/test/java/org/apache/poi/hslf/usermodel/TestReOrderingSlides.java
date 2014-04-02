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


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import junit.framework.TestCase;
import org.apache.poi.hslf.*;
import org.apache.poi.hslf.model.*;
import org.apache.poi.POIDataSamples;

/**
 * Tests that SlideShow can re-order slides properly
 *
 * @author Nick Burch (nick at torchbox dot com)
 */
public final class TestReOrderingSlides extends TestCase {
	// A SlideShow with one slide
	private HSLFSlideShow hss_one;
	private SlideShow ss_one;

	// A SlideShow with two slides
	private HSLFSlideShow hss_two;
	private SlideShow ss_two;

	// A SlideShow with three slides
	private HSLFSlideShow hss_three;
	private SlideShow ss_three;

	/**
	 * Create/open the slideshows
	 */
	public void setUp() throws Exception {
        POIDataSamples slTests = POIDataSamples.getSlideShowInstance();

		hss_one = new HSLFSlideShow(slTests.openResourceAsStream("Single_Coloured_Page.ppt"));
		ss_one = new SlideShow(hss_one);

		hss_two = new HSLFSlideShow(slTests.openResourceAsStream("basic_test_ppt_file.ppt"));
		ss_two = new SlideShow(hss_two);

		hss_three = new HSLFSlideShow(slTests.openResourceAsStream("incorrect_slide_order.ppt"));
		ss_three = new SlideShow(hss_three);
	}

	/**
	 * Test that we can "re-order" a slideshow with only 1 slide on it
	 */
	public void testReOrder1() throws Exception {
		// Has one slide
		assertEquals(1, ss_one.getSlides().length);
		Slide s1 = ss_one.getSlides()[0];

		// Check slide 1 is as expected
		assertEquals(256, s1._getSheetNumber());
		assertEquals(3, s1._getSheetRefId());
		assertEquals(1, s1.getSlideNumber());

		// Now move it to one
		ss_one.reorderSlide(1, 1);

		// Write out, and read back in
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		hss_one.write(baos);
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

		HSLFSlideShow hss_read = new HSLFSlideShow(bais);
		SlideShow ss_read = new SlideShow(hss_read);

		// Check it still has 1 slide
		assertEquals(1, ss_read.getSlides().length);

		// And check it's as expected
		s1 = ss_read.getSlides()[0];
		assertEquals(256, s1._getSheetNumber());
		assertEquals(3, s1._getSheetRefId());
		assertEquals(1, s1.getSlideNumber());
	}

	/**
	 * Test doing a dummy re-order on a slideshow with
	 *  two slides in it
	 */
	public void testReOrder2() throws Exception {
		// Has two slides
		assertEquals(2, ss_two.getSlides().length);
		Slide s1 = ss_two.getSlides()[0];
		Slide s2 = ss_two.getSlides()[1];

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
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		hss_two.write(baos);
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

		HSLFSlideShow hss_read = new HSLFSlideShow(bais);
		SlideShow ss_read = new SlideShow(hss_read);

		// Check it still has 2 slides
		assertEquals(2, ss_read.getSlides().length);

		// And check it's as expected
		s1 = ss_read.getSlides()[0];
		s2 = ss_read.getSlides()[1];
		assertEquals(256, s1._getSheetNumber());
		assertEquals(4, s1._getSheetRefId());
		assertEquals(1, s1.getSlideNumber());
		assertEquals(257, s2._getSheetNumber());
		assertEquals(6, s2._getSheetRefId());
		assertEquals(2, s2.getSlideNumber());
	}

	/**
	 * Test re-ordering slides in a slideshow with 2 slides on it
	 */
	public void testReOrder2swap() throws Exception {
		// Has two slides
		assertEquals(2, ss_two.getSlides().length);
		Slide s1 = ss_two.getSlides()[0];
		Slide s2 = ss_two.getSlides()[1];

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
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		hss_two.write(baos);
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

		HSLFSlideShow hss_read = new HSLFSlideShow(bais);
		SlideShow ss_read = new SlideShow(hss_read);

		// Check it still has 2 slides
		assertEquals(2, ss_read.getSlides().length);

		// And check it's as expected
		s1 = ss_read.getSlides()[0];
		s2 = ss_read.getSlides()[1];
		assertEquals(257, s1._getSheetNumber());
		assertEquals(6, s1._getSheetRefId());
		assertEquals(1, s1.getSlideNumber());
		assertEquals(256, s2._getSheetNumber());
		assertEquals(4, s2._getSheetRefId());
		assertEquals(2, s2.getSlideNumber());
	}

	/**
	 * Test doing a dummy re-order on a slideshow with
	 *  three slides in it
	 */
	public void testReOrder3() throws Exception {
		// Has three slides
		assertEquals(3, ss_three.getSlides().length);
		Slide s1 = ss_three.getSlides()[0];
		Slide s2 = ss_three.getSlides()[1];
		Slide s3 = ss_three.getSlides()[2];

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
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		hss_three.write(baos);
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

		HSLFSlideShow hss_read = new HSLFSlideShow(bais);
		SlideShow ss_read = new SlideShow(hss_read);

		// Check it still has 3 slides
		assertEquals(3, ss_read.getSlides().length);

		// And check it's as expected
		s1 = ss_read.getSlides()[0];
		s2 = ss_read.getSlides()[1];
		s3 = ss_read.getSlides()[2];

		assertEquals(256, s1._getSheetNumber());
		assertEquals(3, s1._getSheetRefId());
		assertEquals(1, s1.getSlideNumber());
		assertEquals(258, s2._getSheetNumber());
		assertEquals(5, s2._getSheetRefId());
		assertEquals(2, s2.getSlideNumber());
		assertEquals(257, s3._getSheetNumber());
		assertEquals(4, s3._getSheetRefId());
		assertEquals(3, s3.getSlideNumber());
	}

	/**
	 * Test re-ordering slides in a slideshow with 3 slides on it
	 */
	public void testReOrder3swap() throws Exception {
		// Has three slides
		assertEquals(3, ss_three.getSlides().length);
		Slide s1 = ss_three.getSlides()[0];
		Slide s2 = ss_three.getSlides()[1];
		Slide s3 = ss_three.getSlides()[2];

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

		// Write out, and read back in
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		hss_three.write(baos);
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

		HSLFSlideShow hss_read = new HSLFSlideShow(bais);
		SlideShow ss_read = new SlideShow(hss_read);

		// Check it still has 3 slides
		assertEquals(3, ss_read.getSlides().length);

		// And check it's as expected
		Slide _s1 = ss_read.getSlides()[0];
		Slide _s2 = ss_read.getSlides()[1];
		Slide _s3 = ss_read.getSlides()[2];

        // 1 --> 3
        assertEquals(s1._getSheetNumber(), _s3._getSheetNumber());
		assertEquals(s1._getSheetRefId(), _s3._getSheetRefId());
		assertEquals(1, s1.getSlideNumber());

        // 2nd slide is not updated
        assertEquals(s2._getSheetNumber(), _s2._getSheetNumber());
        assertEquals(s2._getSheetRefId(), _s2._getSheetRefId());
		assertEquals(2, s2.getSlideNumber());

        // 3 --> 1
        assertEquals(s3._getSheetNumber(), _s1._getSheetNumber());
        assertEquals(s3._getSheetRefId(), _s1._getSheetRefId());
		assertEquals(3, s3.getSlideNumber());
	}
}
