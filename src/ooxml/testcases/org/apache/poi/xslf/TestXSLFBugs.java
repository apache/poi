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

import static junit.framework.TestCase.assertEquals;
import static org.apache.poi.POITestCase.assertContains;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URI;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.xslf.usermodel.DrawingParagraph;
import org.apache.poi.xslf.usermodel.DrawingTextBody;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFPictureData;
import org.apache.poi.xslf.usermodel.XSLFPictureShape;
import org.apache.poi.xslf.usermodel.XSLFRelation;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFSlideLayout;
import org.junit.Ignore;
import org.junit.Test;
public class TestXSLFBugs {

    @Test
    @SuppressWarnings("deprecation")
    public void bug51187() throws Exception {
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
    @Test
    public void tika705() {
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
    @Test
    @Ignore
    public void bug54916() throws Exception {
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
    
    /**
     * When the picture is not embedded but inserted only as a "link to file", 
     * there is no data available and XSLFPictureShape.getPictureData()
     * gives a NPE, see bug #56812
     */
    @Test
    public void bug56812() throws Exception {
        XMLSlideShow ppt = XSLFTestDataSamples.openSampleDocument("56812.pptx");
        
        int internalPictures = 0;
        int externalPictures = 0;
        for (XSLFSlide slide : ppt.getSlides()){
            for (XSLFShape shape : slide.getShapes()){
                assertNotNull(shape);
                
                if (shape instanceof XSLFPictureShape) {
                    XSLFPictureShape picture = (XSLFPictureShape)shape;
                    if (picture.isExternalLinkedPicture()) {
                        externalPictures++;
                        
                        assertNotNull(picture.getPictureLink());
                    } else {
                        internalPictures++;
                        
                        XSLFPictureData data = picture.getPictureData();
                        assertNotNull(data);
                        assertNotNull(data.getFileName());
                    }
                }
            }
        }
        
        assertEquals(2, internalPictures);
        assertEquals(1, externalPictures);
    }

    @Test
    @Ignore("Similar to TestFontRendering it doesn't make sense to compare images because of tiny rendering differences in windows/unix")
    public void bug54542() throws Exception {
        XMLSlideShow ss = XSLFTestDataSamples.openSampleDocument("54542_cropped_bitmap.pptx");
        
        Dimension pgsize = ss.getPageSize();
        
        XSLFSlide slide = ss.getSlides()[0];
        
        // render it
        double zoom = 1;
        AffineTransform at = new AffineTransform();
        at.setToScale(zoom, zoom);
        
        BufferedImage imgActual = new BufferedImage((int)Math.ceil(pgsize.width*zoom), (int)Math.ceil(pgsize.height*zoom), BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D graphics = imgActual.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        graphics.setTransform(at);                
        graphics.setPaint(Color.white);
        graphics.fill(new Rectangle2D.Float(0, 0, pgsize.width, pgsize.height));
        slide.draw(graphics);             
        
        ImageIO.write(imgActual, "PNG", new File("bug54542.png"));
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

    @Test
    public void bug57250() throws Exception {
        XMLSlideShow ss = new XMLSlideShow();
        for (String s : new String[]{"Slide1","Slide2"}) {
            ss.createSlide().createTextBox().setText(s);
        }
        validateSlides(ss, false, "Slide1","Slide2");

        XSLFSlide slide = ss.createSlide();
        slide.createTextBox().setText("New slide");
        validateSlides(ss, true, "Slide1","Slide2","New slide");

        // Move backward
        ss.setSlideOrder(slide, 0);
        validateSlides(ss, true, "New slide","Slide1","Slide2");

        // Move forward
        ss.setSlideOrder(slide, 1);
        validateSlides(ss, true, "Slide1","New slide","Slide2");

        // Move to end
        ss.setSlideOrder(slide, 0);
        ss.setSlideOrder(slide, 2);
        validateSlides(ss, true, "Slide1","Slide2","New slide");
    }
    
    /**
     * When working with >9 images, make sure the sorting ensures
     *  that image10.foo isn't between image1.foo and image2.foo
     */
    @Test
    public void test57552() throws Exception {
        XMLSlideShow ss = new XMLSlideShow();
        for (String s : new String[]{"Slide1","Slide2"}) {
            ss.createSlide().createTextBox().setText(s);
        }
        
        // Slide starts with just layout relation
        XSLFSlide slide = ss.getSlides()[0];
        assertEquals(0, ss.getAllPictures().size());
        assertEquals(1, slide.getShapes().length);
        
        assertEquals(1, slide.getRelations().size());
        assertRelationEquals(XSLFRelation.SLIDE_LAYOUT, slide.getRelations().get(0));
        
        // Some dummy pictures
        byte[][] pics = new byte[15][3];
        for (int i=0; i<pics.length; i++) {
            for (int j=0; j<pics[i].length; j++) {
                pics[i][j] = (byte)i;
            }
        }
        
        // Add a few pictures
        for (int i=0; i<10; i++) {
            int idx = ss.addPicture(pics[i], XSLFPictureData.PICTURE_TYPE_JPEG);
            assertEquals(i, idx);
            assertEquals(i+1, ss.getAllPictures().size());
            
            XSLFPictureShape shape = slide.createPicture(idx);
            assertNotNull(shape.getPictureData());
            assertArrayEquals(pics[i], shape.getPictureData().getData());
            assertEquals(i+2, slide.getShapes().length);
        }
        // Re-fetch the pictures and check
        for (int i=0; i<10; i++) {
            XSLFPictureShape shape = (XSLFPictureShape)slide.getShapes()[i+1];
            assertNotNull(shape.getPictureData());
            assertArrayEquals(pics[i], shape.getPictureData().getData());
        }
        
        // Add past 10
        for (int i=10; i<15; i++) {
            int idx = ss.addPicture(pics[i], XSLFPictureData.PICTURE_TYPE_JPEG);
            assertEquals(i, idx);
            assertEquals(i+1, ss.getAllPictures().size());
            
            XSLFPictureShape shape = slide.createPicture(idx);
            assertNotNull(shape.getPictureData());
            assertArrayEquals(pics[i], shape.getPictureData().getData());
            assertEquals(i+2, slide.getShapes().length);
        }
        // Check all pictures
        for (int i=0; i<15; i++) {
            XSLFPictureShape shape = (XSLFPictureShape)slide.getShapes()[i+1];
            assertNotNull(shape.getPictureData());
            assertArrayEquals(pics[i], shape.getPictureData().getData());
        }
        
        // Add a duplicate, check the right one is picked
        int idx = ss.addPicture(pics[3], XSLFPictureData.PICTURE_TYPE_JPEG);
        assertEquals(3, idx);
        assertEquals(15, ss.getAllPictures().size());
        
        XSLFPictureShape shape = slide.createPicture(idx);
        assertNotNull(shape.getPictureData());
        assertArrayEquals(pics[3], shape.getPictureData().getData());
        assertEquals(17, slide.getShapes().length);
        
        
        // Save and re-load
        ss = XSLFTestDataSamples.writeOutAndReadBack(ss);
        slide = ss.getSlides()[0];
        
        // Check the 15 individual ones added
        for (int i=0; i<15; i++) {
            shape = (XSLFPictureShape)slide.getShapes()[i+1];
            assertNotNull(shape.getPictureData());
            assertArrayEquals(pics[i], shape.getPictureData().getData());
        }
        
        // Check the duplicate
        shape = (XSLFPictureShape)slide.getShapes()[16];
        assertNotNull(shape.getPictureData());
        assertArrayEquals(pics[3], shape.getPictureData().getData());
        
        // Add another duplicate
        idx = ss.addPicture(pics[5], XSLFPictureData.PICTURE_TYPE_JPEG);
        assertEquals(5, idx);
        assertEquals(15, ss.getAllPictures().size());
        
        shape = slide.createPicture(idx);
        assertNotNull(shape.getPictureData());
        assertArrayEquals(pics[5], shape.getPictureData().getData());
        assertEquals(18, slide.getShapes().length);
    }

    private void validateSlides(XMLSlideShow ss, boolean saveAndReload, String... slideTexts) {
        if (saveAndReload) {
            ss = XSLFTestDataSamples.writeOutAndReadBack(ss);
        }

        assertEquals(slideTexts.length, ss.getSlides().length);

        for (int i = 0; i < slideTexts.length; i++) {
            XSLFSlide slide = ss.getSlides()[i];
            assertContains(getSlideText(slide), slideTexts[i]);
        }
    }
    private void assertRelationEquals(XSLFRelation expected, POIXMLDocumentPart relation) {
        assertEquals(expected.getContentType(), relation.getPackagePart().getContentType());
        assertEquals(expected.getFileName(expected.getFileNameIndex(relation)), relation.getPackagePart().getPartName().getName());
    }
}
