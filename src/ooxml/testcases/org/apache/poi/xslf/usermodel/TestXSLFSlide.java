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

import static org.apache.poi.sl.TestCommonSL.sameColor;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.io.IOException;
import java.util.List;

import org.apache.poi.xslf.XSLFTestDataSamples;
import org.junit.Test;

/**
 * @author Yegor Kozlov
 */
public class TestXSLFSlide {
    
    @Test
    public void testReadShapes() throws IOException {
        XMLSlideShow  ppt = XSLFTestDataSamples.openSampleDocument("shapes.pptx");
        List<XSLFSlide> slides = ppt.getSlides();

        XSLFSlide slide1 = slides.get(0);
        List<XSLFShape> shapes1 = slide1.getShapes();
        assertEquals(7, shapes1.size());
        assertEquals("TextBox 3", shapes1.get(0).getShapeName());
        assertTrue(shapes1.get(0) instanceof XSLFTextBox);
        XSLFAutoShape sh0 = (XSLFAutoShape)shapes1.get(0);
        assertEquals("Learning PPTX", sh0.getText());


        assertEquals("Straight Connector 5", shapes1.get(1).getShapeName());
        assertTrue(shapes1.get(1) instanceof XSLFConnectorShape);

        assertEquals("Freeform 6", shapes1.get(2).getShapeName());
        assertTrue(shapes1.get(2) instanceof XSLFFreeformShape);
        XSLFAutoShape sh2 = (XSLFAutoShape)shapes1.get(2);
        assertEquals("Cloud", sh2.getText());

        assertEquals("Picture 1", shapes1.get(3).getShapeName());
        assertTrue(shapes1.get(3) instanceof XSLFPictureShape);

        assertEquals("Table 2", shapes1.get(4).getShapeName());
        assertTrue(shapes1.get(4) instanceof XSLFGraphicFrame);

        assertEquals("Straight Arrow Connector 7", shapes1.get(5).getShapeName());
        assertTrue(shapes1.get(5) instanceof XSLFConnectorShape);

        assertEquals("Elbow Connector 9", shapes1.get(6).getShapeName());
        assertTrue(shapes1.get(6) instanceof XSLFConnectorShape);

        // titles on slide2
        XSLFSlide slide2 = slides.get(1);
        List<XSLFShape> shapes2 = slide2.getShapes();
        assertEquals(2, shapes2.size());
        assertTrue(shapes2.get(0) instanceof XSLFAutoShape);
        assertEquals("PPTX Title", ((XSLFAutoShape)shapes2.get(0)).getText());
        assertTrue(shapes2.get(1) instanceof XSLFAutoShape);
        assertEquals("Subtitle\nAnd second line", ((XSLFAutoShape)shapes2.get(1)).getText());

        //  group shape on slide3
        XSLFSlide slide3 = slides.get(2);
        List<XSLFShape> shapes3 = slide3.getShapes();
        assertEquals(1, shapes3.size());
        assertTrue(shapes3.get(0) instanceof XSLFGroupShape);
        List<XSLFShape> groupShapes = ((XSLFGroupShape)shapes3.get(0)).getShapes();
        assertEquals(3, groupShapes.size());
        assertTrue(groupShapes.get(0) instanceof XSLFAutoShape);
        assertEquals("Rectangle 1", groupShapes.get(0).getShapeName());

        assertTrue(groupShapes.get(1) instanceof XSLFAutoShape);
        assertEquals("Oval 2", groupShapes.get(1).getShapeName());

        assertTrue(groupShapes.get(2) instanceof XSLFAutoShape);
        assertEquals("Right Arrow 3", groupShapes.get(2).getShapeName());

        XSLFSlide slide4 = slides.get(3);
        List<XSLFShape> shapes4 = slide4.getShapes();
        assertEquals(1, shapes4.size());
        assertTrue(shapes4.get(0) instanceof XSLFTable);
        XSLFTable tbl = (XSLFTable)shapes4.get(0);
        assertEquals(3, tbl.getNumberOfColumns());
        assertEquals(6, tbl.getNumberOfRows());
        
        ppt.close();
    }

    @Test
    public void testCreateSlide() throws IOException {
        XMLSlideShow  ppt = new XMLSlideShow();
        assertEquals(0, ppt.getSlides().size());

        XSLFSlide slide = ppt.createSlide();
        assertTrue(slide.getFollowMasterGraphics());
        slide.setFollowMasterGraphics(false);
        assertFalse(slide.getFollowMasterGraphics());
        slide.setFollowMasterGraphics(true);
        assertTrue(slide.getFollowMasterGraphics());
        
        ppt.close();
    }

    @Test
    public void testImportContent() throws IOException {
        XMLSlideShow ppt = new XMLSlideShow();

        XMLSlideShow  src = XSLFTestDataSamples.openSampleDocument("themes.pptx");

        // create a blank slide and import content from the 4th slide of themes.pptx
        XSLFSlide slide1 = ppt.createSlide().importContent(src.getSlides().get(3));
        List<XSLFShape> shapes1 = slide1.getShapes();
        assertEquals(2, shapes1.size());

        XSLFTextShape sh1 = (XSLFTextShape)shapes1.get(0);
        assertEquals("Austin Theme", sh1.getText());
        XSLFTextRun r1 = sh1.getTextParagraphs().get(0).getTextRuns().get(0);
        assertEquals("Century Gothic", r1.getFontFamily());
        assertEquals(40.0, r1.getFontSize(), 0);
        assertTrue(r1.isBold());
        assertTrue(r1.isItalic());
        assertTrue(sameColor(new Color(148, 198, 0), r1.getFontColor()));
        assertNull(sh1.getFillColor());
        assertNull(sh1.getLineColor());

        XSLFTextShape sh2 = (XSLFTextShape)shapes1.get(1);
        assertEquals(
                "Text in a autoshape is white\n" +
                "Fill: RGB(148, 198,0)", sh2.getText());
        XSLFTextRun r2 = sh2.getTextParagraphs().get(0).getTextRuns().get(0);
        assertEquals("Century Gothic", r2.getFontFamily());
        assertEquals(18.0, r2.getFontSize(), 0);
        assertFalse(r2.isBold());
        assertFalse(r2.isItalic());
        assertTrue(sameColor(Color.white, r2.getFontColor()));
        assertEquals(new Color(148, 198, 0), sh2.getFillColor());
        assertEquals(new Color(148, 198, 0), sh2.getLineColor()); // slightly different from PowerPoint!

        // the 5th slide has a picture and a texture fill
        XSLFSlide slide2 = ppt.createSlide().importContent(src.getSlides().get(4));
        List<XSLFShape> shapes2 = slide2.getShapes();
        assertEquals(2, shapes2.size());

        XSLFTextShape sh3 = (XSLFTextShape)shapes2.get(0);
        assertEquals("This slide overrides master background with a texture fill", sh3.getText());
        XSLFTextRun r3 = sh3.getTextParagraphs().get(0).getTextRuns().get(0);
        assertEquals("Century Gothic", r3.getFontFamily());
        //assertEquals(32.4.0, r3.getFontSize());
        assertTrue(r3.isBold());
        assertTrue(r3.isItalic());
        assertTrue(sameColor(new Color(148, 198, 0), r3.getFontColor()));
        assertNull(sh3.getFillColor());
        assertNull(sh3.getLineColor());

        XSLFPictureShape sh4 = (XSLFPictureShape)shapes2.get(1);
        XSLFPictureShape srcPic = (XSLFPictureShape)src.getSlides().get(4).getShapes().get(1);
        assertArrayEquals(sh4.getPictureData().getData(), srcPic.getPictureData().getData());
        
        ppt.close();
    }

    @Test
    public void testMergeSlides() throws IOException {
        XMLSlideShow ppt = new XMLSlideShow();
        String[] pptx = {"shapes.pptx", "themes.pptx", "layouts.pptx", "backgrounds.pptx"};

        for(String arg : pptx){
            XMLSlideShow  src = XSLFTestDataSamples.openSampleDocument(arg);

            for(XSLFSlide srcSlide : src.getSlides()){
                ppt.createSlide().importContent(srcSlide);
            }
        }
        assertEquals(30, ppt.getSlides().size());
        
        ppt.close();
    }    
}