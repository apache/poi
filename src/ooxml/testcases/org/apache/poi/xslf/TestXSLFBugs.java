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
package org.apache.poi.xslf;

import java.net.URI;
import java.util.List;

import org.apache.poi.POITestCase;
import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.xslf.usermodel.DrawingParagraph;
import org.apache.poi.xslf.usermodel.DrawingTextBody;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFRelation;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFSlideLayout;

public class TestXSLFBugs extends POITestCase {

    @SuppressWarnings("deprecation")
    public void test51187() throws Exception {
       XMLSlideShow ss = XSLFTestDataSamples.openSampleDocument("51187.pptx");
       
       assertEquals(1, ss.getSlides().length);
       XSLFSlide slide = ss.getSlides()[0];
       
       // Check the relations on it
       // Note - rId3 is a self reference
       PackagePart slidePart = ss._getXSLFSlideShow().getSlidePart(
             ss._getXSLFSlideShow().getSlideReferences().getSldIdArray(0)
       );
       assertEquals("/ppt/slides/slide1.xml", slidePart.getPartName().toString());
       assertEquals("/ppt/slideLayouts/slideLayout12.xml", slidePart.getRelationship("rId1").getTargetURI().toString());
       assertEquals("/ppt/notesSlides/notesSlide1.xml", slidePart.getRelationship("rId2").getTargetURI().toString());
       assertEquals("/ppt/slides/slide1.xml", slidePart.getRelationship("rId3").getTargetURI().toString());
       assertEquals("/ppt/media/image1.png", slidePart.getRelationship("rId4").getTargetURI().toString());
       
       // Save and re-load
       ss = XSLFTestDataSamples.writeOutAndReadBack(ss);
       assertEquals(1, ss.getSlides().length);
       
       slidePart = ss._getXSLFSlideShow().getSlidePart(
             ss._getXSLFSlideShow().getSlideReferences().getSldIdArray(0)
       );
       assertEquals("/ppt/slides/slide1.xml", slidePart.getPartName().toString());
       assertEquals("/ppt/slideLayouts/slideLayout12.xml", slidePart.getRelationship("rId1").getTargetURI().toString());
       assertEquals("/ppt/notesSlides/notesSlide1.xml", slidePart.getRelationship("rId2").getTargetURI().toString());
       // TODO Fix this
       assertEquals("/ppt/slides/slide1.xml", slidePart.getRelationship("rId3").getTargetURI().toString());
       assertEquals("/ppt/media/image1.png", slidePart.getRelationship("rId4").getTargetURI().toString());
    }
    
    /**
     * Slide relations with anchors in them
     */
    public void testTIKA705() {
       XMLSlideShow ss = XSLFTestDataSamples.openSampleDocument("with_japanese.pptx");
       
       // Should have one slide
       assertEquals(1, ss.getSlides().length);
       XSLFSlide slide = ss.getSlides()[0];
       
       // Check the relations from this
       List<POIXMLDocumentPart> rels = slide.getRelations();
       
       // Should have 6 relations:
       //   1 external hyperlink (skipped from list)
       //   4 internal hyperlinks
       //   1 slide layout
       assertEquals(5, rels.size());
       int layouts = 0;
       int hyperlinks = 0;
       for(POIXMLDocumentPart p : rels) {
          if(p.getPackageRelationship().getRelationshipType().equals(XSLFRelation.HYPERLINK.getRelation())) {
             hyperlinks++;
          } else if(p instanceof XSLFSlideLayout) {
             layouts++;
          }
       }
       assertEquals(1, layouts);
       assertEquals(4, hyperlinks);
       
       // Hyperlinks should all be to #_ftn1 or #ftnref1
       for(POIXMLDocumentPart p : rels) {
          if(p.getPackageRelationship().getRelationshipType().equals(XSLFRelation.HYPERLINK.getRelation())) {
             URI target = p.getPackageRelationship().getTargetURI();
             
             if(target.getFragment().equals("_ftn1") ||
                target.getFragment().equals("_ftnref1")) {
                // Good
             } else {
                fail("Invalid target " + target.getFragment() + " on " + target);
             }
          }
       }
    }
    
    /**
     * A slideshow can have more than one rID pointing to a given 
     *  slide, eg presentation.xml rID1 -> slide1.xml, but slide1.xml 
     *  rID2 -> slide3.xml
     */
    public void DISABLEDtest54916() throws Exception {
        XMLSlideShow ss = XSLFTestDataSamples.openSampleDocument("OverlappingRelations.pptx");
        XSLFSlide slide; 
        
        // Should find 4 slides
        assertEquals(4, ss.getSlides().length);
        
        // Check the text, to see we got them in order
        slide = ss.getSlides()[0];
        assertContains("POI cannot read this", getSlideText(slide));
        
        slide = ss.getSlides()[1];
        assertContains("POI can read this", getSlideText(slide));
        assertContains("Has a relationship to another slide", getSlideText(slide));
        
        slide = ss.getSlides()[2];
        assertContains("POI can read this", getSlideText(slide));
        
        slide = ss.getSlides()[3];
        assertContains("POI can read this", getSlideText(slide));
    }
    
    protected String getSlideText(XSLFSlide slide) {
        StringBuffer text = new StringBuffer();
        for(DrawingTextBody textBody : slide.getCommonSlideData().getDrawingText()) {
            for (DrawingParagraph p : textBody.getParagraphs()) {
                text.append(p.getText());
                text.append("\n");
            }
        }
        return text.toString();
    }
}
