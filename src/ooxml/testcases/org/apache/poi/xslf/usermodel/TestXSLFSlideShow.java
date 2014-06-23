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
package org.apache.poi.xslf.usermodel;

import junit.framework.TestCase;
import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.xslf.XSLFTestDataSamples;

import java.awt.Dimension;
import java.util.Arrays;
import java.util.List;

/**
 * @author Yegor Kozlov
 */
public class TestXSLFSlideShow extends TestCase {
    public void testCreateSlide(){
        XMLSlideShow  ppt = new XMLSlideShow();
        assertEquals(0, ppt.getSlides().length);

        XSLFSlide slide1 = ppt.createSlide();
        assertEquals(1, ppt.getSlides().length);
        assertSame(slide1, ppt.getSlides()[0]);

        List<POIXMLDocumentPart> rels =  slide1.getRelations();
        assertEquals(1, rels.size());
        assertEquals(slide1.getSlideMaster().getLayout(SlideLayout.BLANK), rels.get(0));

        XSLFSlide slide2 = ppt.createSlide();
        assertEquals(2, ppt.getSlides().length);
        assertSame(slide2, ppt.getSlides()[1]);

        ppt.setSlideOrder(slide2, 0);
        assertSame(slide2, ppt.getSlides()[0]);
        assertSame(slide1, ppt.getSlides()[1]);

        ppt = XSLFTestDataSamples.writeOutAndReadBack(ppt);
        assertEquals(2, ppt.getSlides().length);
        rels =  ppt.getSlides()[0].getRelations();
    }

    public void testRemoveSlide(){
        XMLSlideShow  ppt = new XMLSlideShow();
        assertEquals(0, ppt.getSlides().length);

        XSLFSlide slide1 = ppt.createSlide();
        XSLFSlide slide2 = ppt.createSlide();

        assertEquals(2, ppt.getSlides().length);
        assertSame(slide1, ppt.getSlides()[0]);
        assertSame(slide2, ppt.getSlides()[1]);

        XSLFSlide removedSlide = ppt.removeSlide(0);
        assertSame(slide1, removedSlide);

        assertEquals(1, ppt.getSlides().length);
        assertSame(slide2, ppt.getSlides()[0]);

        ppt = XSLFTestDataSamples.writeOutAndReadBack(ppt);
        assertEquals(1, ppt.getSlides().length);
    }

    public void testDimension(){
        XMLSlideShow  ppt = new XMLSlideShow();
        Dimension sz = ppt.getPageSize();
        assertEquals(720, sz.width);
        assertEquals(540, sz.height);
        ppt.setPageSize(new Dimension(792, 612));
        sz = ppt.getPageSize();
        assertEquals(792, sz.width);
        assertEquals(612, sz.height);
    }

    public void testSlideMasters(){
        XMLSlideShow  ppt = new XMLSlideShow();
        XSLFSlideMaster[] masters = ppt.getSlideMasters();
        assertEquals(1, masters.length);

        XSLFSlide slide = ppt.createSlide();
        assertSame(masters[0], slide.getSlideMaster());
    }

    public void testSlideLayout(){
        XMLSlideShow  ppt = new XMLSlideShow();
        XSLFSlideMaster[] masters = ppt.getSlideMasters();
        assertEquals(1, masters.length);

        XSLFSlide slide = ppt.createSlide();
        XSLFSlideLayout layout = slide.getSlideLayout();
        assertNotNull(layout);

        assertSame(masters[0], layout.getSlideMaster());
    }

    public void testCreateRemoveSlideMasters() {
        XMLSlideShow ppt = new XMLSlideShow();
        XSLFSlideMaster[] masters = ppt.getSlideMasters();
        assertEquals(1, masters.length);
        XSLFSlideMaster baseSlideMaster = masters[0];

        XSLFSlideMaster newSlideMaster = ppt.createSlideMaster("new slide master");
        XSLFSlideMaster[] masters2 = ppt.getSlideMasters();
        assertEquals(2, masters2.length);
        assertEquals(baseSlideMaster, masters2[0]);
        assertEquals(newSlideMaster, masters2[1]);

        ppt.removeSlideMaster(baseSlideMaster);
        XSLFSlideMaster[] masters3 = ppt.getSlideMasters();
        assertEquals(1, masters3.length);
        assertEquals(newSlideMaster, masters3[0]);
    }

    public void testCreateRemoveSlideLayout() {
        XMLSlideShow ppt = new XMLSlideShow();
        XSLFSlideMaster[] masters = ppt.getSlideMasters();
        XSLFSlideMaster master = masters[0];

        XSLFSlideLayout[] slideLayouts = master.getSlideLayouts();
        assertEquals(11, slideLayouts.length);

        XSLFSlideLayout newLayout = master.createLayout("new Layout");
        XSLFSlideLayout[] slideLayouts2 = master.getSlideLayouts();
        assertEquals(12, slideLayouts2.length);

        assertFalse(Arrays.asList(slideLayouts).contains(newLayout));
        assertTrue(Arrays.asList(slideLayouts2).contains(newLayout));

        master.removeLayout(slideLayouts[0]);
        XSLFSlideLayout[] slideLayouts3 = master.getSlideLayouts();
        assertEquals(11, slideLayouts3.length);

        assertTrue(Arrays.asList(slideLayouts).contains(slideLayouts[0]));
        assertTrue(Arrays.asList(slideLayouts2).contains(slideLayouts[0]));
        assertFalse(Arrays.asList(slideLayouts3).contains(slideLayouts[0]));
        assertTrue(Arrays.asList(slideLayouts3).contains(newLayout));
    }
}
