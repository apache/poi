/* ====================================================================
   Copyright 2002-2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.hslf.model;

import junit.framework.TestCase;
import org.apache.poi.hslf.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.SlideShow;

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;

/**
 * Test adding new slides to a ppt.
 * 
 * Note - uses the same empty PPT file as the core "new Slideshow" 
 *  stuff does
 * @author Yegor Kozlov
 */
public class TestSlides extends TestCase {

    /**
     * Add 1 slide to an empty ppt.
     * @throws Exception
     */
    public void testAddSlides1() throws Exception {
        SlideShow ppt = new SlideShow(new HSLFSlideShow( TestSlides.class.getResourceAsStream("/org/apache/poi/hslf/data/empty.ppt") ));
        assertTrue(ppt.getSlides().length == 0);
        ppt.createSlide();
        assertTrue(ppt.getSlides().length == 1);

        //serialize and read again
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ppt.write(out);
        out.close();

        ppt = new SlideShow(new HSLFSlideShow(new ByteArrayInputStream(out.toByteArray())));
        assertTrue(ppt.getSlides().length == 1);
    }

    /**
     * Add 2 slides to an empty ppt
     * @throws Exception
     */
    public void testAddSlides2() throws Exception {
        SlideShow ppt = new SlideShow(new HSLFSlideShow( TestSlides.class.getResourceAsStream("/org/apache/poi/hslf/data/empty.ppt") ));
        assertTrue(ppt.getSlides().length == 0);
        
        Slide s1 = ppt.createSlide();
        assertTrue(ppt.getSlides().length == 1);
        
        Slide s2 = ppt.createSlide();
        assertTrue(ppt.getSlides().length == 2);

        //serialize and read again
         ByteArrayOutputStream out = new ByteArrayOutputStream();
        ppt.write(out);
        out.close();

        ppt = new SlideShow(new HSLFSlideShow(new ByteArrayInputStream(out.toByteArray())));
        assertTrue(ppt.getSlides().length == 2);
    }

    /**
     * Add 3 slides to an empty ppt
     * @throws Exception
     */
    public void testAddSlides3() throws Exception {
        SlideShow ppt = new SlideShow(new HSLFSlideShow( TestSlides.class.getResourceAsStream("/org/apache/poi/hslf/data/empty.ppt") ));
        assertTrue(ppt.getSlides().length == 0);
        
        Slide s1 = ppt.createSlide();
        assertTrue(ppt.getSlides().length == 1);
        
        Slide s2 = ppt.createSlide();
        assertTrue(ppt.getSlides().length == 2);

        Slide s3 = ppt.createSlide();
        assertTrue(ppt.getSlides().length == 3);

        //serialize and read again
         ByteArrayOutputStream out = new ByteArrayOutputStream();
        ppt.write(out);
        out.close();

        ppt = new SlideShow(new HSLFSlideShow(new ByteArrayInputStream(out.toByteArray())));
        assertTrue(ppt.getSlides().length == 3);
    }

    /**
     * Add slides to ppt which already has two slides
     */
    public void testAddSlides2to3() throws Exception {
		String dirname = System.getProperty("HSLF.testdata.path");
        SlideShow ppt = new SlideShow(new HSLFSlideShow(dirname + "/basic_test_ppt_file.ppt"));
        
        assertTrue(ppt.getSlides().length == 2);
        ppt.createSlide();
        assertTrue(ppt.getSlides().length == 3);

        //serialize and read again
         ByteArrayOutputStream out = new ByteArrayOutputStream();
        ppt.write(out);
        out.close();

        ppt = new SlideShow(new HSLFSlideShow(new ByteArrayInputStream(out.toByteArray())));
        assertTrue(ppt.getSlides().length == 3);
    }

}
