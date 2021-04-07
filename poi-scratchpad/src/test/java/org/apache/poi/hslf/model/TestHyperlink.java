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

import static org.apache.poi.hslf.usermodel.HSLFTextParagraph.getRawText;
import static org.apache.poi.hslf.usermodel.HSLFTextParagraph.toExternalString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.POIDataSamples;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.hslf.HSLFTestDataSamples;
import org.apache.poi.hslf.record.InteractiveInfoAtom;
import org.apache.poi.hslf.usermodel.HSLFHyperlink;
import org.apache.poi.hslf.usermodel.HSLFSlide;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.HSLFTextBox;
import org.apache.poi.hslf.usermodel.HSLFTextParagraph;
import org.apache.poi.hslf.usermodel.HSLFTextRun;
import org.junit.jupiter.api.Test;

/**
 * Test Hyperlink.
 */
public final class TestHyperlink {
    private static POIDataSamples _slTests = POIDataSamples.getSlideShowInstance();

    @Test
    void testTextRunHyperlinks() throws Exception {
        HSLFSlideShow ppt = new HSLFSlideShow(_slTests.openResourceAsStream("WithLinks.ppt"));

        HSLFSlide slide = ppt.getSlides().get(0);
        List<HSLFTextParagraph> para = slide.getTextParagraphs().get(1);

        String rawText = toExternalString(getRawText(para), para.get(0).getRunType());
        String expected =
            "This page has two links:\n"+
            "http://jakarta.apache.org/poi/\n"+
            "\n"+
            "http://slashdot.org/\n"+
            "\n"+
            "In addition, its notes has one link";
        assertEquals(expected, rawText);

        List<HSLFHyperlink> links = findHyperlinks(para);
        assertEquals(2, links.size());

        assertEquals("http://jakarta.apache.org/poi/", links.get(0).getLabel());
        assertEquals("http://jakarta.apache.org/poi/", links.get(0).getAddress());
        assertEquals("http://jakarta.apache.org/poi/", rawText.substring(links.get(0).getStartIndex(), links.get(0).getEndIndex()-1));

        assertEquals("http://slashdot.org/", links.get(1).getLabel());
        assertEquals("http://slashdot.org/", links.get(1).getAddress());
        assertEquals("http://slashdot.org/", rawText.substring(links.get(1).getStartIndex(), links.get(1).getEndIndex()-1));

        slide = ppt.getSlides().get(1);
        para = slide.getTextParagraphs().get(1);
        rawText = toExternalString(getRawText(para), para.get(0).getRunType());
        expected =
            "I have the one link:\n" +
            "Jakarta HSSF";
        assertEquals(expected, rawText);

        links.clear();

        links = findHyperlinks(para);
        assertNotNull(links);
        assertEquals(1, links.size());

        assertEquals("Open Jakarta POI HSSF module test  ", links.get(0).getLabel());
        assertEquals("http://jakarta.apache.org/poi/hssf/", links.get(0).getAddress());
        assertEquals("Jakarta HSSF", rawText.substring(links.get(0).getStartIndex(), links.get(0).getEndIndex()-1));

        ppt.close();
    }

    @Test
    void bug47291() throws IOException {
        HSLFSlideShow ppt1 = new HSLFSlideShow();
        HSLFSlide slide1 = ppt1.createSlide();
        HSLFTextRun r1 = slide1.createTextBox().setText("page1");
        HSLFHyperlink hl1 = r1.createHyperlink();
        hl1.linkToEmail("dev@poi.apache.org");
        HSLFTextRun r2 = ppt1.createSlide().createTextBox().setText("page2");
        HSLFHyperlink hl2 = r2.createHyperlink();
        hl2.linkToLastSlide();
        HSLFSlide sl1 = ppt1.createSlide();
        HSLFTextBox tb1 = sl1.createTextBox();
        tb1.setAnchor(new Rectangle2D.Double(100,100,100,100));
        tb1.appendText("text1 ", false);
        HSLFTextRun r3 = tb1.appendText("lin\u000bk", false);
        tb1.appendText(" text2", false);
        HSLFHyperlink hl3 = r3.createHyperlink();
        hl3.linkToSlide(slide1);
        HSLFTextRun r4 = ppt1.createSlide().createTextBox().setText("page4");
        HSLFHyperlink hl4 = r4.createHyperlink();
        hl4.linkToUrl("https://poi.apache.org");
        HSLFTextBox tb5 = ppt1.createSlide().createTextBox();
        tb5.setText("page5");
        HSLFHyperlink hl5 = tb5.createHyperlink();
        hl5.linkToFirstSlide();

        HSLFSlideShow ppt2 = HSLFTestDataSamples.writeOutAndReadBack(ppt1);
        ppt1.close();

        List<HSLFSlide> slides = ppt2.getSlides();
        tb1 = (HSLFTextBox)slides.get(0).getShapes().get(0);
        hl1 = tb1.getTextParagraphs().get(0).getTextRuns().get(0).getHyperlink();
        assertNotNull(hl1);
        assertEquals("dev@poi.apache.org", hl1.getLabel());
        assertEquals(HyperlinkType.EMAIL, hl1.getType());

        HSLFTextBox tb2 = (HSLFTextBox)slides.get(1).getShapes().get(0);
        hl2 = tb2.getTextParagraphs().get(0).getTextRuns().get(0).getHyperlink();
        assertNotNull(hl2);
        assertEquals(InteractiveInfoAtom.LINK_LastSlide, hl2.getInfo().getInteractiveInfoAtom().getHyperlinkType());
        assertEquals(HyperlinkType.DOCUMENT, hl2.getType());

        HSLFTextBox tb3 = (HSLFTextBox)slides.get(2).getShapes().get(0);
        hl3 = tb3.getTextParagraphs().get(0).getTextRuns().get(1).getHyperlink();
        assertNotNull(hl3);
        assertEquals(ppt2.getSlides().get(0)._getSheetNumber(), Integer.parseInt(hl3.getAddress().split(",")[0]));
        assertEquals(HyperlinkType.DOCUMENT, hl3.getType());

        HSLFTextBox tb4 = (HSLFTextBox)slides.get(3).getShapes().get(0);
        hl4 = tb4.getTextParagraphs().get(0).getTextRuns().get(0).getHyperlink();
        assertNotNull(hl4);
        assertEquals("https://poi.apache.org", hl4.getLabel());
        assertEquals(HyperlinkType.URL, hl4.getType());

        tb5 = (HSLFTextBox)slides.get(4).getShapes().get(0);
        hl5 = tb5.getHyperlink();
        assertNotNull(hl5);
        assertEquals(InteractiveInfoAtom.LINK_FirstSlide, hl5.getInfo().getInteractiveInfoAtom().getHyperlinkType());
        assertEquals(HyperlinkType.DOCUMENT, hl5.getType());

        ppt2.close();
    }

    private static List<HSLFHyperlink> findHyperlinks(List<HSLFTextParagraph> paras) {
        List<HSLFHyperlink> links = new ArrayList<>();
        for (HSLFTextParagraph p : paras) {
            for (HSLFTextRun r : p) {
                HSLFHyperlink hl = r.getHyperlink();
                if (hl != null) {
                    links.add(hl);
                }
            }
        }
        return links;
    }
}
