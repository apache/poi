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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.util.List;

import org.apache.poi.sl.usermodel.PaintStyle;
import org.apache.poi.sl.usermodel.PaintStyle.GradientPaint;
import org.apache.poi.sl.usermodel.PaintStyle.SolidPaint;
import org.apache.poi.sl.usermodel.PaintStyle.TexturePaint;
import org.apache.poi.xslf.XSLFTestDataSamples;
import org.junit.Test;

/**
 * test reading properties from a multi-theme and multi-master document
 *
 * @author Yegor Kozlov
 */
public class TestXSLFTheme {
    @Test
    public void testRead(){
        XMLSlideShow ppt = XSLFTestDataSamples.openSampleDocument("themes.pptx");
        List<XSLFSlide> slides = ppt.getSlides();

        slide1(slides.get(0));
        slide2(slides.get(1));
        slide3(slides.get(2));
        slide4(slides.get(3));
        slide5(slides.get(4));
        slide6(slides.get(5));
        slide7(slides.get(6));
        slide8(slides.get(7));
        slide9(slides.get(8));
        slide10(slides.get(9));
    }

    private XSLFShape getShape(XSLFSheet sheet, String name){
        for(XSLFShape sh : sheet.getShapes()){
            if(sh.getShapeName().equals(name)) {
                return sh;
            }
        }
        throw new IllegalArgumentException("Shape not found: " + name);
    }

    void slide1(XSLFSlide slide){
        assertEquals(Color.WHITE, slide.getBackground().getFillColor());

        XSLFTheme theme = slide.getTheme();
        assertEquals("Office Theme", theme.getName());

        XSLFTextShape sh1 = (XSLFTextShape)getShape(slide, "Rectangle 3");
        XSLFTextRun run1 = sh1.getTextParagraphs().get(0).getTextRuns().get(0);
        assertTrue(sameColor(Color.white, run1.getFontColor()));
        assertEquals(new Color(79, 129, 189), sh1.getFillColor());
        assertTrue(sh1.getFillStyle().getPaint() instanceof SolidPaint) ;   // solid fill

    }

    void slide2(XSLFSlide slide){
        // Background 2, darker 10%
        // YK: PPT shows slightly different color: new Color(221, 217, 195)
        assertEquals(new Color(221, 217, 195), slide.getBackground().getFillColor());
    }

    void slide3(XSLFSlide slide){
        PaintStyle fs = slide.getBackground().getFillStyle().getPaint();
        assertTrue(fs instanceof GradientPaint);
    }

    void slide4(XSLFSlide slide){
        PaintStyle fs = slide.getBackground().getFillStyle().getPaint();
        assertTrue(fs instanceof GradientPaint);

        XSLFTextShape sh1 = (XSLFTextShape)getShape(slide, "Rectangle 4");
        XSLFTextRun run1 = sh1.getTextParagraphs().get(0).getTextRuns().get(0);
        assertTrue(sameColor(Color.white, run1.getFontColor()));
        assertEquals(new Color(148, 198, 0), sh1.getFillColor());
        assertTrue(sh1.getFillStyle().getPaint() instanceof SolidPaint) ;   // solid fill

        XSLFTextShape sh2 = (XSLFTextShape)getShape(slide, "Title 3");
        XSLFTextRun run2 = sh2.getTextParagraphs().get(0).getTextRuns().get(0);
        assertTrue(sameColor(new Color(148, 198, 0), run2.getFontColor()));
        assertNull(sh2.getFillColor());  // no fill

        assertTrue(slide.getSlideLayout().getFollowMasterGraphics());
    }

    void slide5(XSLFSlide slide){
        PaintStyle fs = slide.getBackground().getFillStyle().getPaint();
        assertTrue(fs instanceof TexturePaint);

        XSLFTextShape sh2 = (XSLFTextShape)getShape(slide, "Title 1");
        XSLFTextRun run2 = sh2.getTextParagraphs().get(0).getTextRuns().get(0);
        assertTrue(sameColor(new Color(148, 198, 0), run2.getFontColor()));
        assertNull(sh2.getFillColor());  // no fill
        // font size is 40pt and scale factor is 90%
        assertEquals(36.0, run2.getFontSize(), 0);

        assertTrue(slide.getSlideLayout().getFollowMasterGraphics());
    }

    void slide6(XSLFSlide slide){

        XSLFTextShape sh1 = (XSLFTextShape)getShape(slide, "Subtitle 3");
        XSLFTextRun run1 = sh1.getTextParagraphs().get(0).getTextRuns().get(0);
        assertTrue(sameColor(new Color(66, 66, 66), run1.getFontColor()));
        assertNull(sh1.getFillColor());  // no fill

        XSLFTextShape sh2 = (XSLFTextShape)getShape(slide, "Title 2");
        XSLFTextRun run2 = sh2.getTextParagraphs().get(0).getTextRuns().get(0);
        assertTrue(sameColor(new Color(148, 198, 0), run2.getFontColor()));
        assertNull(sh2.getFillColor());  // no fill

        assertFalse(slide.getSlideLayout().getFollowMasterGraphics());
    }

    void slide7(XSLFSlide slide){

        //YK: PPT reports a slightly different color: r=189,g=239,b=87
        assertEquals(new Color(189, 239, 87), slide.getBackground().getFillColor());

        assertFalse(slide.getFollowMasterGraphics());
    }

    void slide8(XSLFSlide slide){
        PaintStyle fs = slide.getBackground().getFillStyle().getPaint();
        assertTrue(fs instanceof TexturePaint);
    }

    void slide9(XSLFSlide slide){
        PaintStyle fs = slide.getBackground().getFillStyle().getPaint();
        assertTrue(fs instanceof TexturePaint);
    }

    void slide10(XSLFSlide slide){
        PaintStyle fs = slide.getBackground().getFillStyle().getPaint();
        assertTrue(fs instanceof GradientPaint);

        XSLFTextShape sh1 = (XSLFTextShape)getShape(slide, "Title 3");
        XSLFTextRun run1 = sh1.getTextParagraphs().get(0).getTextRuns().get(0);
        assertTrue(sameColor(Color.white, run1.getFontColor()));
        assertNull(sh1.getFillColor());  // no fill

        XSLFTextShape sh2 = (XSLFTextShape)getShape(slide, "Subtitle 4");
        XSLFTextRun run2 = sh2.getTextParagraphs().get(0).getTextRuns().get(0);
        assertTrue(sameColor(Color.white, run2.getFontColor()));
        assertNull(sh2.getFillColor());  // no fill
    }
}
