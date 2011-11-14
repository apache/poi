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
import org.apache.poi.xslf.XSLFTestDataSamples;

import java.awt.Color;
import java.awt.TexturePaint;

/**
 * test reading properties from a multi-theme and multi-master document
 *
 * @author Yegor Kozlov
 */
public class TestXSLFTheme extends TestCase {
    public void testRead(){
        XMLSlideShow ppt = XSLFTestDataSamples.openSampleDocument("themes.pptx");
        XSLFSlide[] slides = ppt.getSlides();

        slide1(slides[0]);
        slide2(slides[1]);
        slide3(slides[2]);
        slide4(slides[3]);
        slide5(slides[4]);
        slide6(slides[5]);
        slide7(slides[6]);
        slide8(slides[7]);
        slide9(slides[8]);
        slide10(slides[9]);
    }

    private XSLFShape getShape(XSLFSheet sheet, String name){
        for(XSLFShape sh : sheet.getShapes()){
            if(sh.getShapeName().equals(name)) return sh;
        }
        throw new IllegalArgumentException("Shape not found: " + name);
    }

    void slide1(XSLFSlide slide){
        assertEquals(Color.white, slide.getBackground().getFillColor());

        XSLFTheme theme = slide.getTheme();
        assertEquals("Office Theme", theme.getName());

        XSLFTextShape sh1 = (XSLFTextShape)getShape(slide, "Rectangle 3");
        RenderableShape rsh1 = new RenderableShape(sh1);
        XSLFTextRun run1 = sh1.getTextParagraphs().get(0).getTextRuns().get(0);
        assertEquals(Color.white, run1.getFontColor());
        assertEquals(new Color(79, 129, 189), sh1.getFillColor());
        assertTrue(rsh1.getFillPaint(null) instanceof Color) ;   // solid fill

    }

    void slide2(XSLFSlide slide){
        // Background 2, darker 10%
        // YK: PPT shows slightly different color: new Color(221, 217, 195)
        assertEquals(new Color(214, 212, 203), slide.getBackground().getFillColor());
    }

    void slide3(XSLFSlide slide){
        assertNull(slide.getBackground().getFillColor());
        assertTrue(slide.getBackground().getPaint(null).getClass().getName().indexOf("Gradient") > 0);
    }

    void slide4(XSLFSlide slide){
        assertNull(slide.getBackground().getFillColor());
        assertTrue(slide.getBackground().getPaint(null).getClass().getName().indexOf("Gradient") > 0);

        XSLFTextShape sh1 = (XSLFTextShape)getShape(slide, "Rectangle 4");
        RenderableShape rsh1 = new RenderableShape(sh1);
        XSLFTextRun run1 = sh1.getTextParagraphs().get(0).getTextRuns().get(0);
        assertEquals(Color.white, run1.getFontColor());
        assertEquals(new Color(148, 198, 0), sh1.getFillColor());
        assertTrue(rsh1.getFillPaint(null) instanceof Color) ;   // solid fill

        XSLFTextShape sh2 = (XSLFTextShape)getShape(slide, "Title 3");
        XSLFTextRun run2 = sh2.getTextParagraphs().get(0).getTextRuns().get(0);
        assertEquals(new Color(148, 198, 0), run2.getFontColor());
        assertNull(sh2.getFillColor());  // no fill

        assertTrue(slide.getSlideLayout().getFollowMasterGraphics());
    }

    void slide5(XSLFSlide slide){
        assertTrue(slide.getBackground().getPaint(null) instanceof TexturePaint);

        XSLFTextShape sh2 = (XSLFTextShape)getShape(slide, "Title 1");
        XSLFTextRun run2 = sh2.getTextParagraphs().get(0).getTextRuns().get(0);
        assertEquals(new Color(148, 198, 0), run2.getFontColor());
        assertNull(sh2.getFillColor());  // no fill
        // font size is 40pt and scale factor is 90%
        assertEquals(36.0, run2.getFontSize());

        assertTrue(slide.getSlideLayout().getFollowMasterGraphics());
    }

    void slide6(XSLFSlide slide){

        XSLFTextShape sh1 = (XSLFTextShape)getShape(slide, "Subtitle 3");
        XSLFTextRun run1 = sh1.getTextParagraphs().get(0).getTextRuns().get(0);
        assertEquals(new Color(66, 66, 66), run1.getFontColor());
        assertNull(sh1.getFillColor());  // no fill

        XSLFTextShape sh2 = (XSLFTextShape)getShape(slide, "Title 2");
        XSLFTextRun run2 = sh2.getTextParagraphs().get(0).getTextRuns().get(0);
        assertEquals(new Color(148, 198, 0), run2.getFontColor());
        assertNull(sh2.getFillColor());  // no fill

        assertFalse(slide.getSlideLayout().getFollowMasterGraphics());
    }

    void slide7(XSLFSlide slide){

        //YK: PPT reports a slightly different color: r=189,g=239,b=87
        assertEquals(new Color(182, 218, 108), slide.getBackground().getFillColor());

        assertFalse(slide.getFollowMasterGraphics());
    }

    void slide8(XSLFSlide slide){
        assertTrue(slide.getBackground().getPaint(null) instanceof TexturePaint);
    }

    void slide9(XSLFSlide slide){
        assertTrue(slide.getBackground().getPaint(null) instanceof TexturePaint);
    }

    void slide10(XSLFSlide slide){
        assertTrue(slide.getBackground().getPaint(null).getClass().getName().indexOf("Gradient") > 0);

        XSLFTextShape sh1 = (XSLFTextShape)getShape(slide, "Title 3");
        XSLFTextRun run1 = sh1.getTextParagraphs().get(0).getTextRuns().get(0);
        assertEquals(Color.white, run1.getFontColor());
        assertNull(sh1.getFillColor());  // no fill

        XSLFTextShape sh2 = (XSLFTextShape)getShape(slide, "Subtitle 4");
        XSLFTextRun run2 = sh2.getTextParagraphs().get(0).getTextRuns().get(0);
        assertEquals(Color.white, run2.getFontColor());
        assertNull(sh2.getFillColor());  // no fill
    }
}
