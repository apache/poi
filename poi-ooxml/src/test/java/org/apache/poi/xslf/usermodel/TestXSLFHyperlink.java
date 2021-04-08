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

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.List;

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.TargetMode;
import org.apache.poi.xslf.XSLFTestDataSamples;
import org.junit.jupiter.api.Test;

class TestXSLFHyperlink {

    @Test
    void testRead() throws IOException{
        XMLSlideShow  ppt = XSLFTestDataSamples.openSampleDocument("shapes.pptx");

        XSLFSlide slide = ppt.getSlides().get(4);
        List<XSLFShape> shapes = slide.getShapes();
        XSLFTable tbl = (XSLFTable)shapes.get(0);
        XSLFTableCell cell1 = tbl.getRows().get(1).getCells().get(0);
        assertEquals("Web Page", cell1.getText());
        XSLFHyperlink link1 = cell1.getTextParagraphs().get(0).getTextRuns().get(0).getHyperlink();
        assertNotNull(link1);
        assertEquals("http://poi.apache.org/", link1.getAddress());

        XSLFTableCell cell2 = tbl.getRows().get(2).getCells().get(0);
        assertEquals("Place in this document", cell2.getText());
        XSLFHyperlink link2 = cell2.getTextParagraphs().get(0).getTextRuns().get(0).getHyperlink();
        assertNotNull(link2);
        assertEquals("/ppt/slides/slide2.xml", link2.getAddress());

        XSLFTableCell cell3 = tbl.getRows().get(3).getCells().get(0);
        assertEquals("Email", cell3.getText());
        XSLFHyperlink link3 = cell3.getTextParagraphs().get(0).getTextRuns().get(0).getHyperlink();
        assertNotNull(link3);
        assertEquals("mailto:dev@poi.apache.org?subject=Hi%20There", link3.getAddress());

        ppt.close();
    }

    @Test
    void testCreate() throws IOException, InvalidFormatException  {
        XMLSlideShow ppt = new XMLSlideShow();
        XSLFSlide slide1 = ppt.createSlide();
        XSLFSlide slide2 = ppt.createSlide();

        int numRel = slide1.getPackagePart().getRelationships().size();
        assertEquals(1, numRel);
        XSLFTextBox sh1 = slide1.createTextBox();
        XSLFTextRun r1 = sh1.addNewTextParagraph().addNewTextRun();
        r1.setText("Web Page");
        XSLFHyperlink link1 = r1.createHyperlink();
        link1.setAddress("http://poi.apache.org/");
        assertEquals("http://poi.apache.org/", link1.getAddress());
        assertEquals(numRel + 1, slide1.getPackagePart().getRelationships().size());

        String id1 = link1.getXmlObject().getId();
        assertNotNull(id1);
        PackageRelationship rel1 = slide1.getPackagePart().getRelationship(id1);
        assertNotNull(rel1);
        assertEquals(id1, rel1.getId());
        assertEquals(TargetMode.EXTERNAL, rel1.getTargetMode());
        assertEquals(XSLFRelation.HYPERLINK.getRelation(), rel1.getRelationshipType());

        XSLFTextBox sh2 = slide1.createTextBox();
        XSLFTextRun r2 = sh2.addNewTextParagraph().addNewTextRun();
        r2.setText("Place in this document");
        XSLFHyperlink link2 = r2.createHyperlink();
        link2.linkToSlide(slide2);
        assertEquals("/ppt/slides/slide2.xml", link2.getAddress());
        assertEquals(numRel + 2, slide1.getPackagePart().getRelationships().size());

        String id2 = link2.getXmlObject().getId();
        assertNotNull(id2);
        PackageRelationship rel2 = slide1.getPackagePart().getRelationship(id2);
        assertNotNull(rel2);
        assertEquals(id2, rel2.getId());
        assertEquals(TargetMode.INTERNAL, rel2.getTargetMode());
        assertEquals(XSLFRelation.SLIDE.getRelation(), rel2.getRelationshipType());

        ppt.close();
    }


    @Test
    void bug47291() throws IOException {
        Rectangle2D anchor = new Rectangle2D.Double(100,100,100,100);
        XMLSlideShow ppt1 = new XMLSlideShow();
        XSLFSlide slide1 = ppt1.createSlide();
        XSLFTextBox tb1 = slide1.createTextBox();
        tb1.setAnchor(anchor);
        XSLFTextRun r1 = tb1.setText("page1");
        XSLFHyperlink hl1 = r1.createHyperlink();
        hl1.linkToEmail("dev@poi.apache.org");
        XSLFTextBox tb2 = ppt1.createSlide().createTextBox();
        tb2.setAnchor(anchor);
        XSLFTextRun r2 = tb2.setText("page2");
        XSLFHyperlink hl2 = r2.createHyperlink();
        hl2.linkToLastSlide();
        XSLFSlide sl3 = ppt1.createSlide();
        XSLFTextBox tb3 = sl3.createTextBox();
        tb3.setAnchor(anchor);
        tb3.setText("text1 ");
        tb3.appendText("lin\u000bk", false);
        tb3.appendText(" text2", false);
        List<XSLFTextRun> tb3runs = tb3.getTextParagraphs().get(0).getTextRuns();
        tb3runs.get(1).createHyperlink().linkToSlide(slide1); // "lin"
        tb3runs.get(3).createHyperlink().linkToSlide(slide1); // "k"
        XSLFTextBox tb4 = ppt1.createSlide().createTextBox();
        tb4.setAnchor(anchor);
        XSLFTextRun r4 = tb4.setText("page4");
        XSLFHyperlink hl4 = r4.createHyperlink();
        hl4.linkToUrl("http://poi.apache.org");
        XSLFTextBox tb5 = ppt1.createSlide().createTextBox();
        tb5.setAnchor(anchor);
        tb5.setText("page5");
        XSLFHyperlink hl5 = tb5.createHyperlink();
        hl5.linkToFirstSlide();

        XMLSlideShow ppt2 = XSLFTestDataSamples.writeOutAndReadBack(ppt1);
        ppt1.close();

        List<XSLFSlide> slides = ppt2.getSlides();
        tb1 = (XSLFTextBox)slides.get(0).getShapes().get(0);
        hl1 = tb1.getTextParagraphs().get(0).getTextRuns().get(0).getHyperlink();
        assertNotNull(hl1);
        assertEquals("dev@poi.apache.org", hl1.getLabel());
        assertEquals(HyperlinkType.EMAIL, hl1.getType());

        tb2 = (XSLFTextBox)slides.get(1).getShapes().get(0);
        hl2 = tb2.getTextParagraphs().get(0).getTextRuns().get(0).getHyperlink();
        assertNotNull(hl2);
        assertEquals("lastslide", hl2.getXmlObject().getAction().split("=")[1]);
        assertEquals(HyperlinkType.DOCUMENT, hl2.getType());

        tb3 = (XSLFTextBox)slides.get(2).getShapes().get(0);
        XSLFHyperlink hl3 = tb3.getTextParagraphs().get(0).getTextRuns().get(1).getHyperlink();
        assertNotNull(hl3);
        hl3 = tb3.getTextParagraphs().get(0).getTextRuns().get(3).getHyperlink();
        assertNotNull(hl3);
        assertEquals("/ppt/slides/slide1.xml", hl3.getAddress());
        assertEquals(HyperlinkType.DOCUMENT, hl3.getType());

        tb4 = (XSLFTextBox)slides.get(3).getShapes().get(0);
        hl4 = tb4.getTextParagraphs().get(0).getTextRuns().get(0).getHyperlink();
        assertNotNull(hl4);
        assertEquals("http://poi.apache.org", hl4.getLabel());
        assertEquals(HyperlinkType.URL, hl4.getType());

        tb5 = (XSLFTextBox)slides.get(4).getShapes().get(0);
        hl5 = tb5.getHyperlink();
        assertNotNull(hl5);
        assertEquals("firstslide", hl5.getXmlObject().getAction().split("=")[1]);
        assertEquals(HyperlinkType.DOCUMENT, hl5.getType());

        ppt2.close();
    }
}