
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

import java.io.*;
import org.apache.poi.hslf.usermodel.SlideShow;

import junit.framework.TestCase;

/**
 * Test {@link org.apache.poi.hslf.model.HeadersFooters} object
 */
public class TestHeadersFooters extends TestCase
{

    public static final String cwd = System.getProperty("HSLF.testdata.path");

    public void testRead() throws Exception
    {
        File file = new File(cwd, "headers_footers.ppt");
        FileInputStream is = new FileInputStream(file);
        SlideShow ppt = new SlideShow(is);
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

        Slide[] slide = ppt.getSlides();
        //the first slide uses presentation-scope headers / footers
        HeadersFooters hd1 = slide[0].getHeadersFooters();
        assertEquals(slideHdd.isFooterVisible(), hd1.isFooterVisible());
        assertEquals(slideHdd.getFooterText(), hd1.getFooterText());
        assertEquals(slideHdd.isSlideNumberVisible(), hd1.isSlideNumberVisible());
        assertEquals(slideHdd.isHeaderVisible(), hd1.isHeaderVisible());
        assertEquals(slideHdd.getHeaderText(), hd1.getHeaderText());
        assertEquals(slideHdd.isUserDateVisible(), hd1.isUserDateVisible());
        assertEquals(slideHdd.getDateTimeText(), hd1.getDateTimeText());

        //the first slide uses per-slide headers / footers
        HeadersFooters hd2 = slide[1].getHeadersFooters();
        assertEquals(true, hd2.isFooterVisible());
        assertEquals("per-slide footer", hd2.getFooterText());
        assertEquals(true, hd2.isUserDateVisible());
        assertEquals("custom date format", hd2.getDateTimeText());
    }

    public void testCreateSlideFooters() throws Exception
    {
        SlideShow ppt = new SlideShow();
        HeadersFooters hdd = ppt.getSlideHeadersFooters();
        hdd.setFootersText("My slide footer");
        hdd.setSlideNumberVisible(true);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ppt.write(out);
        byte[] b = out.toByteArray();

        SlideShow ppt2 = new SlideShow(new ByteArrayInputStream(b));
        HeadersFooters hdd2 = ppt2.getSlideHeadersFooters();
        assertTrue(hdd2.isSlideNumberVisible());
        assertTrue(hdd2.isFooterVisible());
        assertEquals("My slide footer", hdd2.getFooterText());
    }

    public void testCreateNotesFooters() throws Exception
    {
        SlideShow ppt = new SlideShow();
        HeadersFooters hdd = ppt.getNotesHeadersFooters();
        hdd.setFootersText("My notes footer");
        hdd.setHeaderText("My notes header");
        hdd.setSlideNumberVisible(true);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ppt.write(out);
        byte[] b = out.toByteArray();

        SlideShow ppt2 = new SlideShow(new ByteArrayInputStream(b));
        HeadersFooters hdd2 = ppt2.getNotesHeadersFooters();
        assertTrue(hdd2.isSlideNumberVisible());
        assertTrue(hdd2.isFooterVisible());
        assertEquals("My notes footer", hdd2.getFooterText());
        assertTrue(hdd2.isHeaderVisible());
        assertEquals("My notes header", hdd2.getHeaderText());
    }
}