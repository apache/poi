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

import java.awt.Dimension;
import java.util.List;

import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.xslf.XSLFTestDataSamples;

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
        assertEquals(slide1.getMasterSheet().getLayout("blank"), rels.get(0));

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
        assertSame(masters[0], slide.getMasterSheet());
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
}
