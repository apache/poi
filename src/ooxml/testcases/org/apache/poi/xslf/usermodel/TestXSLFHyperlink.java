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

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.util.*;
import java.util.List;
import java.net.URI;

import org.apache.poi.xslf.XSLFTestDataSamples;
import org.apache.poi.xssf.usermodel.XSSFTable;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.TargetMode;

/**
 * @author Yegor Kozlov
 */
public class TestXSLFHyperlink extends TestCase {

    public void testRead(){
        XMLSlideShow  ppt = XSLFTestDataSamples.openSampleDocument("shapes.pptx");

        XSLFSlide slide = ppt.getSlides()[4];
        XSLFShape[] shapes = slide.getShapes();
        XSLFTable tbl = (XSLFTable)shapes[0];
        XSLFTableCell cell1 = tbl.getRows().get(1).getCells().get(0);
        assertEquals("Web Page", cell1.getText());
        XSLFHyperlink link1 = cell1.getTextParagraphs().get(0).getTextRuns().get(0).getHyperlink();
        assertNotNull(link1);
        assertEquals(URI.create("http://poi.apache.org/"), link1.getTargetURI());

        XSLFTableCell cell2 = tbl.getRows().get(2).getCells().get(0);
        assertEquals("Place in this document", cell2.getText());
        XSLFHyperlink link2 = cell2.getTextParagraphs().get(0).getTextRuns().get(0).getHyperlink();
        assertNotNull(link2);
        assertEquals(URI.create("/ppt/slides/slide2.xml"), link2.getTargetURI());

        XSLFTableCell cell3 = tbl.getRows().get(3).getCells().get(0);
        assertEquals("Email", cell3.getText());
        XSLFHyperlink link3 = cell3.getTextParagraphs().get(0).getTextRuns().get(0).getHyperlink();
        assertNotNull(link3);
        assertEquals(URI.create("mailto:dev@poi.apache.org?subject=Hi%20There"), link3.getTargetURI());
    }

    public void testCreate() throws Exception  {
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
        assertEquals(URI.create("http://poi.apache.org/"), link1.getTargetURI());
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
        link2.setAddress(slide2);
        assertEquals(URI.create("/ppt/slides/slide2.xml"), link2.getTargetURI());
        assertEquals(numRel + 2, slide1.getPackagePart().getRelationships().size());

        String id2 = link2.getXmlObject().getId();
        assertNotNull(id2);
        PackageRelationship rel2 = slide1.getPackagePart().getRelationship(id2);
        assertNotNull(rel2);
        assertEquals(id2, rel2.getId());
        assertEquals(TargetMode.INTERNAL, rel2.getTargetMode());
        assertEquals(XSLFRelation.SLIDE.getRelation(), rel2.getRelationshipType());
    }
}