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
import java.io.IOException;
import java.net.URI;
import java.util.Collection;

import javax.imageio.ImageIO;

import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.POIXMLDocumentPart.RelationPart;
import org.apache.poi.sl.usermodel.PictureData.PictureType;
import org.apache.poi.xslf.usermodel.DrawingParagraph;
import org.apache.poi.xslf.usermodel.DrawingTextBody;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFPictureData;
import org.apache.poi.xslf.usermodel.XSLFPictureShape;
import org.apache.poi.xslf.usermodel.XSLFRelation;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFSlideLayout;
import org.apache.poi.xslf.usermodel.XSLFSlideMaster;
import org.junit.Ignore;
import org.junit.Test;


public class TestXSLFBugs {

    @Test
    public void bug51187() throws Exception {
       XMLSlideShow ss1 = XSLFTestDataSamples.openSampleDocument("51187.pptx");
       
       assertEquals(1, ss1.getSlides().size());
       
       // Check the relations on it
       // Note - rId3 is a self reference
       XSLFSlide slide0 = ss1.getSlides().get(0);
       
       assertRelation(slide0, "/ppt/slides/slide1.xml", null);
       assertRelation(slide0, "/ppt/slideLayouts/slideLayout12.xml", "rId1");
       assertRelation(slide0, "/ppt/notesSlides/notesSlide1.xml", "rId2");
       assertRelation(slide0, "/ppt/slides/slide1.xml", "rId3");
       assertRelation(slide0, "/ppt/media/image1.png", "rId4");
       
       // Save and re-load
       XMLSlideShow ss2 = XSLFTestDataSamples.writeOutAndReadBack(ss1);
       ss1.close();
       assertEquals(1, ss2.getSlides().size());
       
       slide0 = ss2.getSlides().get(0);
       assertRelation(slide0, "/ppt/slides/slide1.xml", null);
       assertRelation(slide0, "/ppt/slideLayouts/slideLayout12.xml", "rId1");
       assertRelation(slide0, "/ppt/notesSlides/notesSlide1.xml", "rId2");
       // TODO Fix this
       assertRelation(slide0, "/ppt/slides/slide1.xml", "rId3");
       assertRelation(slide0, "/ppt/media/image1.png", "rId4");
       
       ss2.close();
    }
    
    private static void assertRelation(XSLFSlide slide, String exp, String rId) {
        POIXMLDocumentPart pd = (rId != null) ? slide.getRelationById(rId) : slide;
        assertEquals(exp, pd.getPackagePart().getPartName().getName());
    }
    
    /**
     * Slide relations with anchors in them
     */
    @Test
    public void tika705() throws Exception {
       XMLSlideShow ss = XSLFTestDataSamples.openSampleDocument("with_japanese.pptx");
       
       // Should have one slide
       assertEquals(1, ss.getSlides().size());
       XSLFSlide slide = ss.getSlides().get(0);
       
       // Check the relations from this
       Collection<RelationPart> rels = slide.getRelationParts();
       
       // Should have 6 relations:
       //   1 external hyperlink (skipped from list)
       //   4 internal hyperlinks
       //   1 slide layout
       assertEquals(5, rels.size());
       int layouts = 0;
       int hyperlinks = 0;
       for(RelationPart p : rels) {
          if(p.getRelationship().getRelationshipType().equals(XSLFRelation.HYPERLINK.getRelation())) {
             hyperlinks++;
          } else if(p.getDocumentPart() instanceof XSLFSlideLayout) {
             layouts++;
          }
       }
       assertEquals(1, layouts);
       assertEquals(4, hyperlinks);
       
       // Hyperlinks should all be to #_ftn1 or #ftnref1
       for(RelationPart p : rels) {
          if(p.getRelationship().getRelationshipType().equals(XSLFRelation.HYPERLINK.getRelation())) {
             URI target = p.getRelationship().getTargetURI();
             
             if(target.getFragment().equals("_ftn1") ||
                target.getFragment().equals("_ftnref1")) {
                // Good
             } else {
                fail("Invalid target " + target.getFragment() + " on " + target);
             }
          }
       }
       ss.close();
    }
    
    /**
     * A slideshow can have more than one rID pointing to a given 
     *  slide, eg presentation.xml rID1 -> slide1.xml, but slide1.xml 
     *  rID2 -> slide3.xml
     */
    @Test
    public void bug54916() throws Exception {
        XMLSlideShow ss = XSLFTestDataSamples.openSampleDocument("OverlappingRelations.pptx");
        XSLFSlide slide; 
        
        // Should find 4 slides
        assertEquals(4, ss.getSlides().size());
        
        // Check the text, to see we got them in order
        slide = ss.getSlides().get(0);
        assertContains(getSlideText(slide), "POI cannot read this");
        
        slide = ss.getSlides().get(1);
        assertContains(getSlideText(slide), "POI can read this");
        assertContains(getSlideText(slide), "Has a relationship to another slide");
        
        slide = ss.getSlides().get(2);
        assertContains(getSlideText(slide), "POI can read this");
        
        slide = ss.getSlides().get(3);
        assertContains(getSlideText(slide), "POI can read this");
        
        ss.close();
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
        ppt.close();
    }

    @Test
    @Ignore("Similar to TestFontRendering it doesn't make sense to compare images because of tiny rendering differences in windows/unix")
    public void bug54542() throws Exception {
        XMLSlideShow ss = XSLFTestDataSamples.openSampleDocument("54542_cropped_bitmap.pptx");
        
        Dimension pgsize = ss.getPageSize();
        
        XSLFSlide slide = ss.getSlides().get(0);
        
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
        ss.close();
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
        ss.close();
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
        XSLFSlide slide = ss.getSlides().get(0);
        assertEquals(0, ss.getPictureData().size());
        assertEquals(1, slide.getShapes().size());
        
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
            XSLFPictureData data = ss.addPicture(pics[i], PictureType.JPEG);
            assertEquals(i, data.getIndex());
            assertEquals(i+1, ss.getPictureData().size());
            
            XSLFPictureShape shape = slide.createPicture(data);
            assertNotNull(shape.getPictureData());
            assertArrayEquals(pics[i], shape.getPictureData().getData());
            assertEquals(i+2, slide.getShapes().size());
        }
        // Re-fetch the pictures and check
        for (int i=0; i<10; i++) {
            XSLFPictureShape shape = (XSLFPictureShape)slide.getShapes().get(i+1);
            assertNotNull(shape.getPictureData());
            assertArrayEquals(pics[i], shape.getPictureData().getData());
        }
        
        // Add past 10
        for (int i=10; i<15; i++) {
            XSLFPictureData data = ss.addPicture(pics[i], PictureType.JPEG);
            assertEquals(i, data.getIndex());
            assertEquals(i+1, ss.getPictureData().size());
            
            XSLFPictureShape shape = slide.createPicture(data);
            assertNotNull(shape.getPictureData());
            assertArrayEquals(pics[i], shape.getPictureData().getData());
            assertEquals(i+2, slide.getShapes().size());
        }
        // Check all pictures
        for (int i=0; i<15; i++) {
            XSLFPictureShape shape = (XSLFPictureShape)slide.getShapes().get(i+1);
            assertNotNull(shape.getPictureData());
            assertArrayEquals(pics[i], shape.getPictureData().getData());
        }
        
        // Add a duplicate, check the right one is picked
        XSLFPictureData data = ss.addPicture(pics[3], PictureType.JPEG);
        assertEquals(3, data.getIndex());
        assertEquals(15, ss.getPictureData().size());
        
        XSLFPictureShape shape = slide.createPicture(data);
        assertNotNull(shape.getPictureData());
        assertArrayEquals(pics[3], shape.getPictureData().getData());
        assertEquals(17, slide.getShapes().size());
        
        
        // Save and re-load
        XMLSlideShow ss2 = XSLFTestDataSamples.writeOutAndReadBack(ss);
        slide = ss2.getSlides().get(0);
        
        // Check the 15 individual ones added
        for (int i=0; i<15; i++) {
            shape = (XSLFPictureShape)slide.getShapes().get(i+1);
            assertNotNull(shape.getPictureData());
            assertArrayEquals(pics[i], shape.getPictureData().getData());
        }
        
        // Check the duplicate
        shape = (XSLFPictureShape)slide.getShapes().get(16);
        assertNotNull(shape.getPictureData());
        assertArrayEquals(pics[3], shape.getPictureData().getData());
        
        // Add another duplicate
        data = ss2.addPicture(pics[5], PictureType.JPEG);
        assertEquals(5, data.getIndex());
        assertEquals(15, ss2.getPictureData().size());
        
        shape = slide.createPicture(data);
        assertNotNull(shape.getPictureData());
        assertArrayEquals(pics[5], shape.getPictureData().getData());
        assertEquals(18, slide.getShapes().size());
        
        ss2.close();
        ss.close();
    }

    private void validateSlides(XMLSlideShow ss, boolean saveAndReload, String... slideTexts) throws IOException {
        if (saveAndReload) {
            XMLSlideShow ss2 = XSLFTestDataSamples.writeOutAndReadBack(ss);
            validateSlides(ss, slideTexts);
            ss2.close();
        } else {
            validateSlides(ss, slideTexts);
        }
    }
    
    private void validateSlides(XMLSlideShow ss, String... slideTexts) throws IOException {
        assertEquals(slideTexts.length, ss.getSlides().size());

        for (int i = 0; i < slideTexts.length; i++) {
            XSLFSlide slide = ss.getSlides().get(i);
            assertContains(getSlideText(slide), slideTexts[i]);
        }
    }
    
    private void assertRelationEquals(XSLFRelation expected, POIXMLDocumentPart relation) {
        assertEquals(expected.getContentType(), relation.getPackagePart().getContentType());
        assertEquals(expected.getFileName(expected.getFileNameIndex(relation)), relation.getPackagePart().getPartName().getName());
    }
    
    @Test
    public void bug58205() throws IOException {
        XMLSlideShow ss = XSLFTestDataSamples.openSampleDocument("themes.pptx");
       
        int i = 1;
        for (XSLFSlideMaster sm : ss.getSlideMasters()) {
            assertEquals("rId"+(i++), ss.getRelationId(sm));
        }
        
        ss.close();
    }
}
