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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.awt.Dimension;
import java.io.IOException;
import java.util.List;

import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.xslf.XSLFTestDataSamples;
import org.junit.jupiter.api.Test;

/**
 * @author Yegor Kozlov
 */
class TestXSLFSlideShow {
    @Test
    void testCreateSlide() throws IOException {
        XMLSlideShow  ppt = new XMLSlideShow();
        assertEquals(0, ppt.getSlides().size());

        XSLFSlide slide1 = ppt.createSlide();
        assertEquals(1, ppt.getSlides().size());
        assertSame(slide1, ppt.getSlides().get(0));

        List<POIXMLDocumentPart> rels =  slide1.getRelations();
        assertEquals(1, rels.size());
        assertEquals(slide1.getSlideMaster().getLayout(SlideLayout.BLANK), rels.get(0));

        XSLFSlide slide2 = ppt.createSlide();
        assertEquals(2, ppt.getSlides().size());
        assertSame(slide2, ppt.getSlides().get(1));

        ppt.setSlideOrder(slide2, 0);
        assertSame(slide2, ppt.getSlides().get(0));
        assertSame(slide1, ppt.getSlides().get(1));

        XMLSlideShow ppt2 = XSLFTestDataSamples.writeOutAndReadBack(ppt);
        assertEquals(2, ppt2.getSlides().size());
        rels =  ppt2.getSlides().get(0).getRelations();

        ppt2.close();
        ppt.close();
    }

    @Test
    void testRemoveSlide() throws IOException {
        XMLSlideShow  ppt = new XMLSlideShow();
        assertEquals(0, ppt.getSlides().size());

        XSLFSlide slide1 = ppt.createSlide();
        XSLFSlide slide2 = ppt.createSlide();

        assertEquals(2, ppt.getSlides().size());
        assertSame(slide1, ppt.getSlides().get(0));
        assertSame(slide2, ppt.getSlides().get(1));

        XSLFSlide removedSlide = ppt.removeSlide(0);
        assertSame(slide1, removedSlide);

        assertEquals(1, ppt.getSlides().size());
        assertSame(slide2, ppt.getSlides().get(0));

        XMLSlideShow ppt2 = XSLFTestDataSamples.writeOutAndReadBack(ppt);
        assertEquals(1, ppt2.getSlides().size());

        ppt2.close();
        ppt.close();
    }

    @Test
    void testDimension() throws IOException {
        XMLSlideShow  ppt = new XMLSlideShow();
        Dimension sz = ppt.getPageSize();
        assertEquals(720, sz.width);
        assertEquals(540, sz.height);
        ppt.setPageSize(new Dimension(792, 612));
        sz = ppt.getPageSize();
        assertEquals(792, sz.width);
        assertEquals(612, sz.height);
        ppt.close();
    }

    @Test
    void testSlideMasters() throws IOException {
        XMLSlideShow  ppt = new XMLSlideShow();
        List<XSLFSlideMaster> masters = ppt.getSlideMasters();
        assertEquals(1, masters.size());

        XSLFSlide slide = ppt.createSlide();
        assertSame(masters.get(0), slide.getSlideMaster());
        ppt.close();
    }

    @Test
    void testSlideLayout() throws IOException {
        XMLSlideShow  ppt = new XMLSlideShow();
        List<XSLFSlideMaster> masters = ppt.getSlideMasters();
        assertEquals(1, masters.size());

        XSLFSlide slide = ppt.createSlide();
        XSLFSlideLayout layout = slide.getSlideLayout();
        assertNotNull(layout);

        assertSame(masters.get(0), layout.getSlideMaster());
        ppt.close();
    }

    @Test
    void testSlideLayoutNames() throws IOException {
        final String[] names = {
                "Blank", "Title Only", "Section Header", "Picture with Caption", "Title and Content"
                , "Title Slide", "Title and Vertical Text", "Vertical Title and Text", "Comparison"
                , "Two Content", "Content with Caption"
        };
        XMLSlideShow ppt = XSLFTestDataSamples.openSampleDocument("layouts.pptx");
        for (String name : names) {
            assertNotNull(ppt.findLayout(name));
        }
        final SlideLayout[] layTypes = {
                SlideLayout.BLANK, SlideLayout.TITLE_ONLY, SlideLayout.SECTION_HEADER
                , SlideLayout.PIC_TX, SlideLayout.TITLE_AND_CONTENT, SlideLayout.TITLE
                , SlideLayout.VERT_TX, SlideLayout.VERT_TITLE_AND_TX, SlideLayout.TWO_TX_TWO_OBJ
                , SlideLayout.TWO_OBJ, SlideLayout.OBJ_TX
        };
        for (SlideLayout sl : layTypes){
            assertNotNull(ppt.getSlideMasters().get(0).getLayout(sl));
        }
        ppt.close();
    }
}
