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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hslf.usermodel.*;
import org.junit.Test;

/**
 * Test adding new slides to a ppt.
 *
 * Note - uses the same empty PPT file as the core "new Slideshow"
 *  stuff does
 * @author Yegor Kozlov
 */
public final class TestSlides {

    /**
     * Add 1 slide to an empty ppt.
     * @throws Exception
     */
    @Test
    public void testAddSlides1() throws Exception {
        HSLFSlideShow ppt = new HSLFSlideShow(new HSLFSlideShowImpl( TestSlides.class.getResourceAsStream("/org/apache/poi/hslf/data/empty.ppt") ));
        assertTrue(ppt.getSlides().isEmpty());

        HSLFSlide s1 = ppt.createSlide();
        assertEquals(1, ppt.getSlides().size());
        assertEquals(3, s1._getSheetRefId());
        assertEquals(256, s1._getSheetNumber());
        assertEquals(1, s1.getSlideNumber());

        //serialize and read again
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ppt.write(out);
        out.close();

        ppt = new HSLFSlideShow(new HSLFSlideShowImpl(new ByteArrayInputStream(out.toByteArray())));
        assertEquals(1, ppt.getSlides().size());
    }

    /**
     * Add 2 slides to an empty ppt
     * @throws Exception
     */
    @Test
    public void testAddSlides2() throws Exception {
        HSLFSlideShow ppt = new HSLFSlideShow(new HSLFSlideShowImpl( TestSlides.class.getResourceAsStream("/org/apache/poi/hslf/data/empty.ppt") ));
        assertTrue(ppt.getSlides().isEmpty());

        HSLFSlide s1 = ppt.createSlide();
        assertEquals(1, ppt.getSlides().size());
        assertEquals(3, s1._getSheetRefId());
        assertEquals(256, s1._getSheetNumber());
        assertEquals(1, s1.getSlideNumber());

        HSLFSlide s2 = ppt.createSlide();
        assertEquals(2, ppt.getSlides().size());
        assertEquals(4, s2._getSheetRefId());
        assertEquals(257, s2._getSheetNumber());
        assertEquals(2, s2.getSlideNumber());

        //serialize and read again
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ppt.write(out);
        out.close();

        ppt = new HSLFSlideShow(new HSLFSlideShowImpl(new ByteArrayInputStream(out.toByteArray())));
        assertEquals(2, ppt.getSlides().size());
    }

    /**
     * Add 3 slides to an empty ppt
     * @throws Exception
     */
    @Test
    public void testAddSlides3() throws Exception {
        HSLFSlideShow ppt = new HSLFSlideShow(new HSLFSlideShowImpl( TestSlides.class.getResourceAsStream("/org/apache/poi/hslf/data/empty.ppt") ));
        assertTrue(ppt.getSlides().isEmpty());

        HSLFSlide s1 = ppt.createSlide();
        assertEquals(1, ppt.getSlides().size());
        assertEquals(3, s1._getSheetRefId());
        assertEquals(256, s1._getSheetNumber());
        assertEquals(1, s1.getSlideNumber());

        HSLFSlide s2 = ppt.createSlide();
        assertEquals(2, ppt.getSlides().size());
        assertEquals(4, s2._getSheetRefId());
        assertEquals(257, s2._getSheetNumber());
        assertEquals(2, s2.getSlideNumber());

        HSLFSlide s3 = ppt.createSlide();
        assertEquals(3, ppt.getSlides().size());
        assertEquals(5, s3._getSheetRefId());
        assertEquals(258, s3._getSheetNumber());
        assertEquals(3, s3.getSlideNumber());


        //serialize and read again
         ByteArrayOutputStream out = new ByteArrayOutputStream();
        ppt.write(out);
        out.close();

        ppt = new HSLFSlideShow(new HSLFSlideShowImpl(new ByteArrayInputStream(out.toByteArray())));
        assertEquals(3, ppt.getSlides().size());

        // Check IDs are still right
        s1 = ppt.getSlides().get(0);
        assertEquals(256, s1._getSheetNumber());
        assertEquals(3, s1._getSheetRefId());
        s2 = ppt.getSlides().get(1);
        assertEquals(257, s2._getSheetNumber());
        assertEquals(4, s2._getSheetRefId());
        s3 = ppt.getSlides().get(2);
        assertEquals(3, ppt.getSlides().size());
        assertEquals(258, s3._getSheetNumber());
        assertEquals(5, s3._getSheetRefId());
    }

    /**
     * Add slides to ppt which already has two slides
     */
    @Test
    public void testAddSlides2to3() throws Exception {
        POIDataSamples slTests = POIDataSamples.getSlideShowInstance();
        HSLFSlideShow ppt = new HSLFSlideShow(slTests.openResourceAsStream("basic_test_ppt_file.ppt"));

        assertEquals(2, ppt.getSlides().size());

        // First slide is 256 / 4
        HSLFSlide s1 = ppt.getSlides().get(0);
        assertEquals(256, s1._getSheetNumber());
        assertEquals(4, s1._getSheetRefId());

        // Last slide is 257 / 6
        HSLFSlide s2 = ppt.getSlides().get(1);
        assertEquals(257, s2._getSheetNumber());
        assertEquals(6, s2._getSheetRefId());

        // Add another slide, goes in at the end
        HSLFSlide s3 = ppt.createSlide();
        assertEquals(3, ppt.getSlides().size());
        assertEquals(258, s3._getSheetNumber());
        assertEquals(8, s3._getSheetRefId());


        // Serialize and read again
         ByteArrayOutputStream out = new ByteArrayOutputStream();
        ppt.write(out);
        out.close();

        ppt = new HSLFSlideShow(new HSLFSlideShowImpl(new ByteArrayInputStream(out.toByteArray())));
        assertEquals(3, ppt.getSlides().size());


        // Check IDs are still right
        s1 = ppt.getSlides().get(0);
        assertEquals(256, s1._getSheetNumber());
        assertEquals(4, s1._getSheetRefId());
        s2 = ppt.getSlides().get(1);
        assertEquals(257, s2._getSheetNumber());
        assertEquals(6, s2._getSheetRefId());
        s3 = ppt.getSlides().get(2);
        assertEquals(3, ppt.getSlides().size());
        assertEquals(258, s3._getSheetNumber());
        assertEquals(8, s3._getSheetRefId());
    }

}
