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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;

import org.apache.poi.hslf.HSLFTestDataSamples;
import org.apache.poi.hslf.record.Document;
import org.apache.poi.hslf.record.Record;
import org.apache.poi.hslf.record.RecordTypes;
import org.apache.poi.hslf.record.UserEditAtom;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests that SlideShow adds additional sheets properly
 */
public final class TestAddingSlides {
    // An empty SlideShow
    private HSLFSlideShow ss_empty;

    // A SlideShow with one slide
    private HSLFSlideShow ss_one;

    // A SlideShow with two slides
    private HSLFSlideShow ss_two;

    /**
     * Create/open the slideshows
     */
    @BeforeEach
    void setUp() throws IOException {
        ss_empty = new HSLFSlideShow();
        ss_one = HSLFTestDataSamples.getSlideShow("Single_Coloured_Page.ppt");
        ss_two = HSLFTestDataSamples.getSlideShow("basic_test_ppt_file.ppt");
    }

    @AfterEach
    void tearDown() throws IOException {
        ss_two.close();
        ss_one.close();
        ss_empty.close();
    }

    /**
     * Test adding a slide to an empty slideshow
     */
    @Test
    void testAddSlideToEmpty() throws IOException {
        // Doesn't have any slides
        assertEquals(0, ss_empty.getSlides().size());

        // Should only have a master SLWT
        assertEquals(1,
                ss_empty.getDocumentRecord().getSlideListWithTexts().length);

        // grab UserEditAtom
        UserEditAtom usredit = null;
        Record[] _records = ss_empty.getSlideShowImpl().getRecords();
        for ( org.apache.poi.hslf.record.Record record : _records) {
            if (record.getRecordType() == RecordTypes.UserEditAtom.typeID) {
                usredit = (UserEditAtom) record;
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
        HSLFSlideShow ss_read = HSLFTestDataSamples
                .writeOutAndReadBack(ss_empty);

        // Check it now has a slide
        assertEquals(1, ss_read.getSlides().size());

        // Check it now has two SLWTs
        assertEquals(2,
                ss_empty.getDocumentRecord().getSlideListWithTexts().length);

        // And check it's as expected
        slide = ss_read.getSlides().get(0);
        assertEquals(256, slide._getSheetNumber());
        assertEquals(3, slide._getSheetRefId());
        assertEquals(1, slide.getSlideNumber());
        ss_read.close();
    }

    /**
     * Test adding a slide to an existing slideshow
     */
    @Test
    void testAddSlideToExisting() throws IOException {
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
        HSLFSlideShow ss_read = HSLFTestDataSamples.writeOutAndReadBack(ss_one);

        // Check it now has two slides
        assertEquals(2, ss_read.getSlides().size());

        // Should still have two SLTWs
        assertEquals(2,
                ss_read.getDocumentRecord().getSlideListWithTexts().length);

        // And check it's as expected
        s1 = ss_read.getSlides().get(0);
        s2 = ss_read.getSlides().get(1);
        assertEquals(256, s1._getSheetNumber());
        assertEquals(3, s1._getSheetRefId());
        assertEquals(1, s1.getSlideNumber());
        assertEquals(257, s2._getSheetNumber());
        assertEquals(4, s2._getSheetRefId());
        assertEquals(2, s2.getSlideNumber());
        ss_read.close();
    }

    /**
     * Test adding a slide to an existing slideshow, with two slides already
     */
    @Test
    void testAddSlideToExisting2() throws IOException {
        // grab UserEditAtom
        UserEditAtom usredit = null;
        Record[] _records = ss_two.getSlideShowImpl().getRecords();
        for ( org.apache.poi.hslf.record.Record record : _records) {
            if (record.getRecordType() == RecordTypes.UserEditAtom.typeID) {
                usredit = (UserEditAtom) record;
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
        HSLFSlideShow ss_read = HSLFTestDataSamples.writeOutAndReadBack(ss_two);

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
        ss_read.close();
    }

    /**
     * Test SlideShow#removeSlide
     */
    @Test
    void testRemoving() throws IOException {
        HSLFSlide slide1 = ss_empty.createSlide();
        HSLFSlide slide2 = ss_empty.createSlide();

        List<HSLFSlide> s1 = ss_empty.getSlides();
        assertEquals(2, s1.size());
        assertThrows(Exception.class, () -> ss_empty.removeSlide(-1));
        assertThrows(Exception.class, () -> ss_empty.removeSlide(2));

        assertEquals(1, slide1.getSlideNumber());

        HSLFSlide removedSlide = ss_empty.removeSlide(0);
        List<HSLFSlide> s2 = ss_empty.getSlides();
        assertEquals(1, s2.size());
        assertSame(slide1, removedSlide);
        assertSame(slide2, s2.get(0));

        assertEquals(0, slide2.getSlideNumber());

        try (HSLFSlideShow ss_read = HSLFTestDataSamples.writeOutAndReadBack(ss_empty)) {
            assertEquals(1, ss_read.getSlides().size());
        }
    }

    @Test
    void test47261() throws IOException {
        HSLFSlideShow ppt = HSLFTestDataSamples.getSlideShow("47261.ppt");
        List<HSLFSlide> slides = ppt.getSlides();
        Document doc = ppt.getDocumentRecord();
        assertNotNull(doc.getSlideSlideListWithText());
        assertEquals(14, ppt.getSlides().size());
        int notesId = slides.get(0).getSlideRecord().getSlideAtom()
                .getNotesID();
        assertTrue(notesId > 0);
        assertNotNull(doc.getNotesSlideListWithText());
        assertEquals(14, doc.getNotesSlideListWithText().getSlideAtomsSets().length);

        // remove all slides, corresponding notes should be removed too
        for (int i = slides.size(); i > 0; i--) {
            ppt.removeSlide(0);
        }
        assertEquals(0, ppt.getSlides().size());
        assertEquals(0, ppt.getNotes().size());
        assertNull(doc.getSlideSlideListWithText());
        assertNull(doc.getNotesSlideListWithText());
        ppt.close();
    }
}
