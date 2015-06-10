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
import java.util.List;

import junit.framework.TestCase;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hslf.record.*;

/**
 * Tests that SlideShow adds additional sheets properly
 *
 * @author Nick Burch (nick at torchbox dot com)
 */
public final class TestAddingSlides extends TestCase {
	// An empty SlideShow
	private HSLFSlideShowImpl hss_empty;
	private HSLFSlideShow ss_empty;

	// A SlideShow with one slide
	private HSLFSlideShowImpl hss_one;
	private HSLFSlideShow ss_one;

	// A SlideShow with two slides
	private HSLFSlideShowImpl hss_two;
	private HSLFSlideShow ss_two;

	/**
	 * Create/open the slideshows
	 */
	public void setUp() throws Exception {
		hss_empty = HSLFSlideShowImpl.create();
		ss_empty = new HSLFSlideShow(hss_empty);

        POIDataSamples slTests = POIDataSamples.getSlideShowInstance();

		hss_one = new HSLFSlideShowImpl(slTests.openResourceAsStream("Single_Coloured_Page.ppt"));
		ss_one = new HSLFSlideShow(hss_one);

		hss_two = new HSLFSlideShowImpl(slTests.openResourceAsStream("basic_test_ppt_file.ppt"));
		ss_two = new HSLFSlideShow(hss_two);
	}

	/**
	 * Test adding a slide to an empty slideshow
	 */
	public void testAddSlideToEmpty() throws Exception {
		// Doesn't have any slides
		assertEquals(0, ss_empty.getSlides().size());

		// Should only have a master SLWT
		assertEquals(1, ss_empty.getDocumentRecord().getSlideListWithTexts().length);

        //grab UserEditAtom
        UserEditAtom usredit = null;
        Record[] _records = hss_empty.getRecords();
        for (int i = 0; i < _records.length; i++) {
            Record record = _records[i];
            if(record.getRecordType() == RecordTypes.UserEditAtom.typeID) {
                usredit = (UserEditAtom)record;
            }
       }
       assertNotNull(usredit);

		// Add one
		HSLFSlide slide = ss_empty.createSlide();
		assertEquals(1, ss_empty.getSlides().size());
		assertEquals(256, slide._getSheetNumber());
		assertEquals(3, slide._getSheetRefId());
		assertEquals(1, slide.getSlideNumber());
        assertEquals(usredit.getMaxPersistWritten(), slide._getSheetRefId());

		// Write out, and read back in
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		hss_empty.write(baos);
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

		HSLFSlideShowImpl hss_read = new HSLFSlideShowImpl(bais);
		HSLFSlideShow ss_read = new HSLFSlideShow(hss_read);

		// Check it now has a slide
		assertEquals(1, ss_read.getSlides().size());

		// Check it now has two SLWTs
		assertEquals(2, ss_empty.getDocumentRecord().getSlideListWithTexts().length);

		// And check it's as expected
		slide = ss_read.getSlides().get(0);
		assertEquals(256, slide._getSheetNumber());
		assertEquals(3, slide._getSheetRefId());
		assertEquals(1, slide.getSlideNumber());
	}

	/**
	 * Test adding a slide to an existing slideshow
	 */
	public void testAddSlideToExisting() throws Exception {
		// Has one slide
		assertEquals(1, ss_one.getSlides().size());
		HSLFSlide s1 = ss_one.getSlides().get(0);

		// Should have two SLTWs
		assertEquals(2, ss_one.getDocumentRecord().getSlideListWithTexts().length);

		// Check slide 1 is as expected
		assertEquals(256, s1._getSheetNumber());
		assertEquals(3, s1._getSheetRefId());
		assertEquals(1, s1.getSlideNumber());

		// Add a second one
		HSLFSlide s2 = ss_one.createSlide();
		assertEquals(2, ss_one.getSlides().size());
		assertEquals(257, s2._getSheetNumber());
		assertEquals(4, s2._getSheetRefId());
		assertEquals(2, s2.getSlideNumber());

		// Write out, and read back in
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		hss_one.write(baos);
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

		HSLFSlideShowImpl hss_read = new HSLFSlideShowImpl(bais);
		HSLFSlideShow ss_read = new HSLFSlideShow(hss_read);

		// Check it now has two slides
		assertEquals(2, ss_read.getSlides().size());

		// Should still have two SLTWs
		assertEquals(2, ss_read.getDocumentRecord().getSlideListWithTexts().length);

		// And check it's as expected
		s1 = ss_read.getSlides().get(0);
		s2 = ss_read.getSlides().get(1);
		assertEquals(256, s1._getSheetNumber());
		assertEquals(3, s1._getSheetRefId());
		assertEquals(1, s1.getSlideNumber());
		assertEquals(257, s2._getSheetNumber());
		assertEquals(4, s2._getSheetRefId());
		assertEquals(2, s2.getSlideNumber());
	}

	/**
	 * Test adding a slide to an existing slideshow,
	 *  with two slides already
	 */
	@SuppressWarnings("unused")
    public void testAddSlideToExisting2() throws Exception {
        //grab UserEditAtom
        UserEditAtom usredit = null;
        Record[] _records = hss_two.getRecords();
        for (int i = 0; i < _records.length; i++) {
            Record record = _records[i];
            if(_records[i].getRecordType() == RecordTypes.UserEditAtom.typeID) {
                usredit = (UserEditAtom)_records[i];
            }
       }
       assertNotNull(usredit);

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

		// Add a third one
		HSLFSlide s3 = ss_two.createSlide();
		assertEquals(3, ss_two.getSlides().size());
		assertEquals(258, s3._getSheetNumber());
		assertEquals(8, s3._getSheetRefId()); // lots of notes before us
		assertEquals(3, s3.getSlideNumber());
        assertEquals(usredit.getMaxPersistWritten(), s3._getSheetRefId());

		// Write out, and read back in
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		hss_two.write(baos);
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

		HSLFSlideShowImpl hss_read = new HSLFSlideShowImpl(bais);
		HSLFSlideShow ss_read = new HSLFSlideShow(hss_read);

		// Check it now has three slides
		assertEquals(3, ss_read.getSlides().size());

		// And check it's as expected
		s1 = ss_read.getSlides().get(0);
		s2 = ss_read.getSlides().get(1);
		s3 = ss_read.getSlides().get(2);
		assertEquals(256, s1._getSheetNumber());
		assertEquals(4, s1._getSheetRefId());
		assertEquals(1, s1.getSlideNumber());
		assertEquals(257, s2._getSheetNumber());
		assertEquals(6, s2._getSheetRefId());
		assertEquals(2, s2.getSlideNumber());
		assertEquals(258, s3._getSheetNumber());
		assertEquals(8, s3._getSheetRefId());
		assertEquals(3, s3.getSlideNumber());
	}

    /**
     * Test SlideShow#removeSlide
     */
    public void testRemoving() throws Exception {
        HSLFSlideShow ppt = new HSLFSlideShow();
        HSLFSlide slide1 = ppt.createSlide();
        HSLFSlide slide2 = ppt.createSlide();

        List<HSLFSlide> s1 = ppt.getSlides();
        assertEquals(2, s1.size());
        try {
            ppt.removeSlide(-1);
            fail("expected exception");
        } catch (Exception e){
            ;
        }

        try {
            ppt.removeSlide(2);
            fail("expected exception");
        } catch (Exception e){
            ;
        }

        assertEquals(1, slide1.getSlideNumber());

        HSLFSlide removedSlide = ppt.removeSlide(0);
        List<HSLFSlide> s2 = ppt.getSlides();
        assertEquals(1, s2.size());
        assertSame(slide1, removedSlide);
        assertSame(slide2, s2.get(0));

        assertEquals(0, slide2.getSlideNumber());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ppt.write(out);

        ppt = new HSLFSlideShow(new ByteArrayInputStream(out.toByteArray()));

        List<HSLFSlide> s3 = ppt.getSlides();
        assertEquals(1, s3.size());
    }


    public void test47261() throws Exception {
        POIDataSamples slTests = POIDataSamples.getSlideShowInstance();
        HSLFSlideShow ppt = new HSLFSlideShow(slTests.openResourceAsStream("47261.ppt"));
        List<HSLFSlide> slides = ppt.getSlides();
        Document doc = ppt.getDocumentRecord();
        assertNotNull(doc.getSlideSlideListWithText());
        assertEquals(14, ppt.getSlides().size());
        int notesId = slides.get(0).getSlideRecord().getSlideAtom().getNotesID();
        assertTrue(notesId > 0);
        assertNotNull(doc.getNotesSlideListWithText());
        assertEquals(14, doc.getNotesSlideListWithText().getSlideAtomsSets().length);

        //remove all slides, corresponding notes should be removed too
        for (int i = slides.size(); i > 0; i--) {
            ppt.removeSlide(0);
        }
        assertEquals(0, ppt.getSlides().size());
        assertEquals(0, ppt.getNotes().size());
        assertNull(doc.getSlideSlideListWithText());
        assertNull(doc.getNotesSlideListWithText());

    }
}
