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

package org.apache.poi.hslf.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hslf.HSLFTestDataSamples;
import org.apache.poi.hslf.usermodel.HSLFSlide;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.HSLFTextParagraph;
import org.apache.poi.sl.usermodel.Shape;
import org.apache.poi.sl.usermodel.Slide;
import org.apache.poi.sl.usermodel.SlideShow;
import org.apache.poi.sl.usermodel.TextParagraph;
import org.apache.poi.sl.usermodel.TextShape;
import org.junit.jupiter.api.Test;

/**
 * Test {@link org.apache.poi.hslf.model.HeadersFooters} object
 */
public final class TestHeadersFooters
{

    private static POIDataSamples _slTests = POIDataSamples.getSlideShowInstance();

    @Test
    void testRead() throws IOException {
        InputStream is = _slTests.openResourceAsStream("headers_footers.ppt");
        HSLFSlideShow ppt = new HSLFSlideShow(is);
        is.close();

        HeadersFooters slideHdd = ppt.getSlideHeadersFooters();
        assertTrue(slideHdd.isFooterVisible());
        assertEquals("Global Slide Footer", slideHdd.getFooterText());
        assertTrue(slideHdd.isSlideNumberVisible());
        assertFalse(slideHdd.isHeaderVisible());
        assertNull(slideHdd.getHeaderText());
        assertFalse(slideHdd.isUserDateVisible());
        assertNull(slideHdd.getDateTimeText());


        HeadersFooters notesHdd = ppt.getNotesHeadersFooters();
        assertTrue(notesHdd.isFooterVisible());
        assertEquals("Notes Footer", notesHdd.getFooterText());
        assertTrue(notesHdd.isHeaderVisible());
        assertEquals("Notes Header", notesHdd.getHeaderText());
        assertTrue(notesHdd.isUserDateVisible());
        assertNull(notesHdd.getDateTimeText());

        List<HSLFSlide> slide = ppt.getSlides();
        //the first slide uses presentation-scope headers / footers
        HeadersFooters hd1 = slide.get(0).getHeadersFooters();
        assertEquals(slideHdd.isFooterVisible(), hd1.isFooterVisible());
        assertEquals(slideHdd.getFooterText(), hd1.getFooterText());
        assertEquals(slideHdd.isSlideNumberVisible(), hd1.isSlideNumberVisible());
        assertEquals(slideHdd.isHeaderVisible(), hd1.isHeaderVisible());
        assertEquals(slideHdd.getHeaderText(), hd1.getHeaderText());
        assertEquals(slideHdd.isUserDateVisible(), hd1.isUserDateVisible());
        assertEquals(slideHdd.getDateTimeText(), hd1.getDateTimeText());

        //the first slide uses per-slide headers / footers
        HeadersFooters hd2 = slide.get(1).getHeadersFooters();
        assertTrue(hd2.isFooterVisible());
        assertEquals("per-slide footer", hd2.getFooterText());
        assertTrue(hd2.isUserDateVisible());
        assertEquals("custom date format", hd2.getDateTimeText());

        ppt.close();
    }

    /**
     * If Headers / Footers are not set, all the getters should return <code>false</code> or <code>null</code>
     */
    @Test
    void testReadNoHeadersFooters() throws Exception {
        InputStream is = _slTests.openResourceAsStream("basic_test_ppt_file.ppt");
        HSLFSlideShow ppt = new HSLFSlideShow(is);
        is.close();

        HeadersFooters slideHdd = ppt.getSlideHeadersFooters();
        assertFalse(slideHdd.isFooterVisible());
        assertNull(slideHdd.getFooterText());
        assertFalse(slideHdd.isSlideNumberVisible());
        assertFalse(slideHdd.isHeaderVisible());
        assertNull(slideHdd.getHeaderText());
        assertFalse(slideHdd.isUserDateVisible());
        assertNull(slideHdd.getDateTimeText());


        HeadersFooters notesHdd = ppt.getNotesHeadersFooters();
        assertFalse(notesHdd.isFooterVisible());
        assertNull(notesHdd.getFooterText());
        assertFalse(notesHdd.isHeaderVisible());
        assertNull(notesHdd.getHeaderText());
        assertFalse(notesHdd.isUserDateVisible());
        assertNull(notesHdd.getDateTimeText());

        for(HSLFSlide s : ppt.getSlides()) {
            HeadersFooters hd1 = s.getHeadersFooters();
            assertFalse(hd1.isFooterVisible());
            assertNull(hd1.getFooterText());
            assertFalse(hd1.isHeaderVisible());
            assertNull(hd1.getHeaderText());
            assertFalse(hd1.isUserDateVisible());
            assertNull(hd1.getDateTimeText());
        }

        ppt.close();
    }

    /**
     * Test extraction of headers / footers from PPTs saved in Office 2007
     */
    @Test
    void testRead2007() throws IOException {
        InputStream is = _slTests.openResourceAsStream("headers_footers_2007.ppt");
        HSLFSlideShow ppt = new HSLFSlideShow(is);
        is.close();

        HeadersFooters slideHdd = ppt.getSlideHeadersFooters();
        assertTrue(slideHdd.isFooterVisible());
        assertEquals("THE FOOTER TEXT", slideHdd.getFooterText());
        assertTrue(slideHdd.isSlideNumberVisible());
        assertFalse(slideHdd.isHeaderVisible());
        assertNull(slideHdd.getHeaderText());
        assertTrue(slideHdd.isUserDateVisible());
        assertEquals("Wednesday, August 06, 2008", slideHdd.getDateTimeText());


        HeadersFooters notesHdd = ppt.getNotesHeadersFooters();
        assertTrue(notesHdd.isFooterVisible());
        assertEquals("THE NOTES FOOTER TEXT", notesHdd.getFooterText());
        assertTrue(notesHdd.isHeaderVisible());
        assertEquals("THE NOTES HEADER TEXT", notesHdd.getHeaderText());
        assertTrue(notesHdd.isUserDateVisible());
        assertTrue(notesHdd.isDateTimeVisible());
        //TODO: depending on the formatId getDateTimeText() should return formatted date
        //assertEquals("08/12/08", notesHdd.getDateTimeText());

        //per-slide headers / footers
        List<HSLFSlide> slide = ppt.getSlides();
        //the first slide uses presentation-scope headers / footers
        HeadersFooters hd1 = slide.get(0).getHeadersFooters();
        assertTrue(hd1.isFooterVisible());
        assertEquals("THE FOOTER TEXT", hd1.getFooterText());
        assertTrue(hd1.isSlideNumberVisible());
        assertFalse(hd1.isHeaderVisible());
        assertNull(hd1.getHeaderText());
        assertTrue(hd1.isUserDateVisible());
        assertTrue(hd1.isDateTimeVisible());
        assertEquals("Wednesday, August 06, 2008", hd1.getDateTimeText());

        //the second slide uses custom per-slide headers / footers
        HeadersFooters hd2 = slide.get(1).getHeadersFooters();
        assertTrue(hd2.isFooterVisible());
        assertEquals("THE FOOTER TEXT FOR SLIDE 2", hd2.getFooterText());
        assertTrue(hd2.isSlideNumberVisible());
        assertFalse(hd2.isHeaderVisible());
        assertNull(hd2.getHeaderText());
        assertTrue(hd2.isUserDateVisible());
        assertTrue(hd2.isDateTimeVisible());
        assertEquals("August 06, 2008", hd2.getDateTimeText());

        //the third slide uses per-slide headers / footers
        HeadersFooters hd3 = slide.get(2).getHeadersFooters();
        assertTrue(hd3.isFooterVisible());
        assertEquals("THE FOOTER TEXT", hd3.getFooterText());
        assertTrue(hd3.isSlideNumberVisible());
        assertFalse(hd3.isHeaderVisible());
        assertNull(hd3.getHeaderText());
        assertTrue(hd3.isUserDateVisible());
        assertTrue(hd3.isDateTimeVisible());
        assertEquals("Wednesday, August 06, 2008", hd3.getDateTimeText());

        ppt.close();
    }

    @Test
    void testCreateSlideFooters() throws IOException {
        HSLFSlideShow ppt1 = new HSLFSlideShow();
        HeadersFooters hdd = ppt1.getSlideHeadersFooters();
        hdd.setFootersText("My slide footer");
        hdd.setSlideNumberVisible(true);

        HSLFSlideShow ppt2 = HSLFTestDataSamples.writeOutAndReadBack(ppt1);
        HeadersFooters hdd2 = ppt2.getSlideHeadersFooters();
        assertTrue(hdd2.isSlideNumberVisible());
        assertTrue(hdd2.isFooterVisible());
        assertEquals("My slide footer", hdd2.getFooterText());

        ppt2.close();
        ppt1.close();
    }

    @Test
    void testCreateNotesFooters() throws IOException {
        HSLFSlideShow ppt1 = new HSLFSlideShow();
        HeadersFooters hdd = ppt1.getNotesHeadersFooters();
        hdd.setFootersText("My notes footer");
        hdd.setHeaderText("My notes header");
        hdd.setSlideNumberVisible(true);

        HSLFSlideShow ppt2 = HSLFTestDataSamples.writeOutAndReadBack(ppt1);
        HeadersFooters hdd2 = ppt2.getNotesHeadersFooters();
        assertTrue(hdd2.isSlideNumberVisible());
        assertTrue(hdd2.isFooterVisible());
        assertEquals("My notes footer", hdd2.getFooterText());
        assertTrue(hdd2.isHeaderVisible());
        assertEquals("My notes header", hdd2.getHeaderText());

        ppt2.close();
        ppt1.close();
    }
    @Test
    void bug58144a() throws IOException {
        try (InputStream is = _slTests.openResourceAsStream("bug58144-headers-footers-2003.ppt");
             SlideShow<?,?> ppt = new HSLFSlideShow(is)) {
            HSLFSlide sl = (HSLFSlide) ppt.getSlides().get(0);
            HeadersFooters hfs = sl.getHeadersFooters();
            assertNull(hfs.getHeaderText());
            assertEquals("Confidential", hfs.getFooterText());
            List<List<HSLFTextParagraph>> llp = sl.getTextParagraphs();
            assertEquals("Test", HSLFTextParagraph.getText(llp.get(0)));
            assertFalse(llp.get(0).get(0).isHeaderOrFooter());
        }
    }

    @Test
    void bug58144b() throws IOException {
        try (InputStream is = _slTests.openResourceAsStream("bug58144-headers-footers-2007.ppt");
             SlideShow<?,?> ppt = new HSLFSlideShow(is)) {
            Slide<?, ?> sl = ppt.getSlides().get(0);
            HeadersFooters hfs2 = ((HSLFSlide) sl).getHeadersFooters();
            assertNull(hfs2.getHeaderText());
            assertEquals("Slide footer", hfs2.getFooterText());

            testSlideShow(ppt);
        }
    }

    // copied from org.apache.poi.sl.TestHeadersFooters because of scratchpad.ignore option
    private void testSlideShow(SlideShow<?,?> ppt) {
        Slide<?,?> sl =  ppt.getSlides().get(0);

        List<? extends Shape<?,?>> shapes = sl.getShapes();
        TextShape<?,?> ts0 = (TextShape<?,?>)shapes.get(0);
        assertEquals("Test file", ts0.getText());
        TextShape<?,?> ts1 = (TextShape<?,?>)shapes.get(1);
        assertEquals("Has some text in the headers and footers", ts1.getText());
        TextShape<?,?> ts2 = (TextShape<?,?>)shapes.get(2);
        assertEquals("Slide footer", ts2.getText());
        List<? extends TextParagraph<?,?,?>> ltp = ts2.getTextParagraphs();
        assertTrue(ltp.get(0).isHeaderOrFooter());
    }
}
