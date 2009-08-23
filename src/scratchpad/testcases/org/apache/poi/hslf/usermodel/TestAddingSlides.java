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
import java.io.FileInputStream;
import java.io.File;

import junit.framework.TestCase;
import org.apache.poi.hslf.*;
import org.apache.poi.hslf.record.Record;
import org.apache.poi.hslf.record.RecordTypes;
import org.apache.poi.hslf.record.UserEditAtom;
import org.apache.poi.hslf.record.Document;
import org.apache.poi.hslf.model.*;
import org.apache.poi.POIDataSamples;

/**
 * Tests that SlideShow adds additional sheets properly
 *
 * @author Nick Burch (nick at torchbox dot com)
 */
public final class TestAddingSlides extends TestCase {
	// An empty SlideShow
	private HSLFSlideShow hss_empty;
	private SlideShow ss_empty;

	// A SlideShow with one slide
	private HSLFSlideShow hss_one;
	private SlideShow ss_one;

	// A SlideShow with two slides
	private HSLFSlideShow hss_two;
	private SlideShow ss_two;

	/**
	 * Create/open the slideshows
	 */
	public void setUp() throws Exception {
		hss_empty = HSLFSlideShow.create();
		ss_empty = new SlideShow(hss_empty);

        POIDataSamples slTests = POIDataSamples.getSlideShowInstance();

		hss_one = new HSLFSlideShow(slTests.openResourceAsStream("Single_Coloured_Page.ppt"));
		ss_one = new SlideShow(hss_one);

		hss_two = new HSLFSlideShow(slTests.openResourceAsStream("basic_test_ppt_file.ppt"));
		ss_two = new SlideShow(hss_two);
	}

	/**
	 * Test adding a slide to an empty slideshow
	 */
	public void testAddSlideToEmpty() throws Exception {
		// Doesn't have any slides
		assertEquals(0, ss_empty.getSlides().length);

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
		Slide slide = ss_empty.createSlide();
		assertEquals(1, ss_empty.getSlides().length);
		assertEquals(256, slide._getSheetNumber());
		assertEquals(3, slide._getSheetRefId());
		assertEquals(1, slide.getSlideNumber());
        assertEquals(usredit.getMaxPersistWritten(), slide._getSheetRefId());

		// Write out, and read back in
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		hss_empty.write(baos);
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

		HSLFSlideShow hss_read = new HSLFSlideShow(bais);
		SlideShow ss_read = new SlideShow(hss_read);

		// Check it now has a slide
		assertEquals(1, ss_read.getSlides().length);

		// Check it now has two SLWTs
		assertEquals(2, ss_empty.getDocumentRecord().getSlideListWithTexts().length);

		// And check it's as expected
		slide = ss_read.getSlides()[0];
		assertEquals(256, slide._getSheetNumber());
		assertEquals(3, slide._getSheetRefId());
		assertEquals(1, slide.getSlideNumber());
	}

	/**
	 * Test adding a slide to an existing slideshow
	 */
	public void testAddSlideToExisting() throws Exception {
		// Has one slide
		assertEquals(1, ss_one.getSlides().length);
		Slide s1 = ss_one.getSlides()[0];

		// Should have two SLTWs
		assertEquals(2, ss_one.getDocumentRecord().getSlideListWithTexts().length);

		// Check slide 1 is as expected
		assertEquals(256, s1._getSheetNumber());
		assertEquals(3, s1._getSheetRefId());
		assertEquals(1, s1.getSlideNumber());

		// Add a second one
		Slide s2 = ss_one.createSlide();
		assertEquals(2, ss_one.getSlides().length);
		assertEquals(257, s2._getSheetNumber());
		assertEquals(4, s2._getSheetRefId());
		assertEquals(2, s2.getSlideNumber());

		// Write out, and read back in
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		hss_one.write(baos);
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

		HSLFSlideShow hss_read = new HSLFSlideShow(bais);
		SlideShow ss_read = new SlideShow(hss_read);

		// Check it now has two slides
		assertEquals(2, ss_read.getSlides().length);

		// Should still have two SLTWs
		assertEquals(2, ss_read.getDocumentRecord().getSlideListWithTexts().length);

		// And check it's as expected
		s1 = ss_read.getSlides()[0];
		s2 = ss_read.getSlides()[1];
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

		// Add a third one
		Slide s3 = ss_two.createSlide();
		assertEquals(3, ss_two.getSlides().length);
		assertEquals(258, s3._getSheetNumber());
		assertEquals(8, s3._getSheetRefId()); // lots of notes before us
		assertEquals(3, s3.getSlideNumber());
        assertEquals(usredit.getMaxPersistWritten(), s3._getSheetRefId());

		// Write out, and read back in
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		hss_two.write(baos);
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

		HSLFSlideShow hss_read = new HSLFSlideShow(bais);
		SlideShow ss_read = new SlideShow(hss_read);

		// Check it now has three slides
		assertEquals(3, ss_read.getSlides().length);

		// And check it's as expected
		s1 = ss_read.getSlides()[0];
		s2 = ss_read.getSlides()[1];
		s3 = ss_read.getSlides()[2];
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
        SlideShow ppt = new SlideShow();
        Slide slide1 = ppt.createSlide();
        Slide slide2 = ppt.createSlide();

        Slide[] s1 = ppt.getSlides();
        assertEquals(2, s1.length);
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

        Slide removedSlide = ppt.removeSlide(0);
        Slide[] s2 = ppt.getSlides();
        assertEquals(1, s2.length);
        assertSame(slide1, removedSlide);
        assertSame(slide2, s2[0]);

        assertEquals(0, slide2.getSlideNumber());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ppt.write(out);

        ppt = new SlideShow(new ByteArrayInputStream(out.toByteArray()));

        Slide[] s3 = ppt.getSlides();
        assertEquals(1, s3.length);
    }


    public void test47261() throws Exception {
        POIDataSamples slTests = POIDataSamples.getSlideShowInstance();
        SlideShow ppt = new SlideShow(slTests.openResourceAsStream("47261.ppt"));
        Slide[] slides = ppt.getSlides();
        Document doc = ppt.getDocumentRecord();
        assertNotNull(doc.getSlideSlideListWithText());
        assertEquals(14, ppt.getSlides().length);
        int notesId = slides[0].getSlideRecord().getSlideAtom().getNotesID();
        assertTrue(notesId > 0);
        assertNotNull(doc.getNotesSlideListWithText());
        assertEquals(14, doc.getNotesSlideListWithText().getSlideAtomsSets().length);

        //remove all slides, corresponding notes should be removed too
        for (int i = 0; i < slides.length; i++) {
            ppt.removeSlide(0);
        }
        assertEquals(0, ppt.getSlides().length);
        assertEquals(0, ppt.getNotes().length);
        assertNull(doc.getSlideSlideListWithText());
        assertNull(doc.getNotesSlideListWithText());

    }
}
