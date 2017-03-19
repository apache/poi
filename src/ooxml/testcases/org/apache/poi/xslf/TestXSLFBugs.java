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

import org.apache.poi.POIDataSamples;
import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.POIXMLDocumentPart.RelationPart;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.sl.draw.DrawPaint;
import org.apache.poi.sl.usermodel.PaintStyle;
import org.apache.poi.sl.usermodel.PaintStyle.SolidPaint;
import org.apache.poi.sl.usermodel.PaintStyle.TexturePaint;
import org.apache.poi.sl.usermodel.PictureData;
import org.apache.poi.sl.usermodel.PictureData.PictureType;
import org.apache.poi.sl.usermodel.ShapeType;
import org.apache.poi.sl.usermodel.VerticalAlignment;
import org.apache.poi.xslf.extractor.XSLFPowerPointExtractor;
import org.apache.poi.xslf.usermodel.*;
import org.junit.Ignore;
import org.junit.Test;
import org.openxmlformats.schemas.drawingml.x2006.main.CTOuterShadowEffect;
import org.openxmlformats.schemas.presentationml.x2006.main.CTShape;

import javax.imageio.ImageIO;
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

import static org.apache.poi.POITestCase.assertContains;
import static org.junit.Assert.*;


public class TestXSLFBugs {
    private static final POIDataSamples slTests = POIDataSamples.getSlideShowInstance();

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
        assertNotNull(pd);
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

              //noinspection StatementWithEmptyBody
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
        return XSLFPowerPointExtractor.getText(slide, true, false, false);
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

    @Test
    public void bug55791a() throws IOException {
        XMLSlideShow ppt = XSLFTestDataSamples.openSampleDocument("45541_Footer.pptx");
        removeAndCreateSlide(ppt);
        ppt.close();
    }

    @Test
    public void bug55791b() throws IOException {
        XMLSlideShow ppt = XSLFTestDataSamples.openSampleDocument("SampleShow.pptx");
        removeAndCreateSlide(ppt);
        ppt.close();
    }

    private void removeAndCreateSlide(XMLSlideShow ppt) {
        assertTrue(ppt.getSlides().size() > 1);
        ppt.removeSlide(1);
        assertNotNull(ppt.createSlide());
    }

    @Test
    public void blibFillAlternateContent() throws IOException {
        XMLSlideShow ppt = XSLFTestDataSamples.openSampleDocument("2411-Performance_Up.pptx");
        XSLFPictureShape ps = (XSLFPictureShape)ppt.getSlides().get(4).getShapes().get(0);
        assertNotNull(ps.getPictureData());
        ppt.close();
    }

    @Test
    public void bug59434() throws IOException {
        String url1 = "http://poi.apache.org/changes.html";
        String url2 = "http://poi.apache.org/faq.html";
        XMLSlideShow ppt1 = new XMLSlideShow();
        PictureData pd1 = ppt1.addPicture(slTests.readFile("tomcat.png"), PictureType.PNG);
        PictureData pd2 = ppt1.addPicture(slTests.readFile("santa.wmf"), PictureType.WMF);
        XSLFSlide slide = ppt1.createSlide();
        XSLFPictureShape ps1 = slide.createPicture(pd1);
        ps1.setAnchor(new Rectangle2D.Double(20, 20, 100, 100));
        XSLFHyperlink hl1 = ps1.createHyperlink();
        hl1.linkToUrl(url1);
        XSLFPictureShape ps2 = slide.createPicture(pd2);
        ps2.setAnchor(new Rectangle2D.Double(120, 120, 100, 100));
        XSLFHyperlink hl2 = ps2.createHyperlink();
        hl2.linkToUrl(url2);
        XMLSlideShow ppt2 = XSLFTestDataSamples.writeOutAndReadBack(ppt1);
        ppt1.close();
        slide = ppt2.getSlides().get(0);
        ps1 = (XSLFPictureShape)slide.getShapes().get(0);
        ps2 = (XSLFPictureShape)slide.getShapes().get(1);
        assertEquals(url1, ps1.getHyperlink().getAddress());
        assertEquals(url2, ps2.getHyperlink().getAddress());
    
        ppt2.close();
    }

    @Test
    public void bug58217() throws IOException {
        Color fillColor = new Color(1f,1f,0f,0.1f);
        Color lineColor = new Color(25.3f/255f,1f,0f,0.4f);
        Color textColor = new Color(1f,1f,0f,0.6f);
        
        XMLSlideShow ppt1 = new XMLSlideShow();
        XSLFSlide sl = ppt1.createSlide();
        XSLFAutoShape as = sl.createAutoShape();
        as.setShapeType(ShapeType.STAR_10);
        as.setAnchor(new Rectangle2D.Double(100,100,300,300));
        as.setFillColor(fillColor);
        as.setLineColor(lineColor);
        as.setText("Alpha");
        as.setVerticalAlignment(VerticalAlignment.MIDDLE);
        as.setHorizontalCentered(true);
        XSLFTextRun tr = as.getTextParagraphs().get(0).getTextRuns().get(0);
        tr.setFontSize(32d);
        tr.setFontColor(textColor);
        XMLSlideShow ppt2 = XSLFTestDataSamples.writeOutAndReadBack(ppt1);
        ppt1.close();
        sl = ppt2.getSlides().get(0);
        as = (XSLFAutoShape)sl.getShapes().get(0);
        checkColor(fillColor, as.getFillStyle().getPaint());
        checkColor(lineColor, as.getStrokeStyle().getPaint());
        checkColor(textColor, as.getTextParagraphs().get(0).getTextRuns().get(0).getFontColor());
        ppt2.close();
    }
    
    private static void checkColor(Color expected, PaintStyle actualStyle) {
        assertTrue(actualStyle instanceof SolidPaint);
        SolidPaint ps = (SolidPaint)actualStyle;
        Color actual = DrawPaint.applyColorTransform(ps.getSolidColor());
        float expRGB[] = expected.getRGBComponents(null);
        float actRGB[] = actual.getRGBComponents(null);
        assertArrayEquals(expRGB, actRGB, 0.0001f);
    }

    @Test
    public void bug55714() throws IOException {
        XMLSlideShow srcPptx = XSLFTestDataSamples.openSampleDocument("pptx2svg.pptx");
        XMLSlideShow newPptx = new XMLSlideShow();
        XSLFSlide srcSlide = srcPptx.getSlides().get(0);
        XSLFSlide newSlide = newPptx.createSlide();

        XSLFSlideLayout srcSlideLayout = srcSlide.getSlideLayout();
        XSLFSlideLayout newSlideLayout = newSlide.getSlideLayout();
        newSlideLayout.importContent(srcSlideLayout);

        XSLFSlideMaster srcSlideMaster = srcSlide.getSlideMaster();
        XSLFSlideMaster newSlideMaster = newSlide.getSlideMaster();
        newSlideMaster.importContent(srcSlideMaster); 

        newSlide.importContent(srcSlide);
        XMLSlideShow rwPptx = XSLFTestDataSamples.writeOutAndReadBack(newPptx);
        
        PaintStyle ps = rwPptx.getSlides().get(0).getBackground().getFillStyle().getPaint();
        assertTrue(ps instanceof TexturePaint);
        
        rwPptx.close();
        newPptx.close();
        srcPptx.close();
    }

    @Test
    public void bug59273() throws IOException {
        XMLSlideShow ppt = XSLFTestDataSamples.openSampleDocument("bug59273.potx");
        ppt.getPackage().replaceContentType(
            XSLFRelation.PRESENTATIONML_TEMPLATE.getContentType(), 
            XSLFRelation.MAIN.getContentType()
        );

        XMLSlideShow rwPptx = XSLFTestDataSamples.writeOutAndReadBack(ppt);
        OPCPackage pkg = rwPptx.getPackage();
        int size = pkg.getPartsByContentType(XSLFRelation.MAIN.getContentType()).size();
        assertEquals(1, size);
        size = pkg.getPartsByContentType(XSLFRelation.PRESENTATIONML_TEMPLATE.getContentType()).size();
        assertEquals(0, size);
        
        rwPptx.close();
        ppt.close();
    }


    @Test
    public void bug60373() throws IOException {
        XMLSlideShow ppt = new XMLSlideShow();
        XSLFSlide sl = ppt.createSlide();
        XSLFTable t = sl.createTable();
        XSLFTableRow r = t.addRow();
        bug60373_addCell(r);
        bug60373_addCell(r);
        r = t.addRow();
        XSLFTableCell c = bug60373_addCell(r);
        // call getTextHeight, when table is not fully populated
        double th = c.getTextHeight();
        assertTrue(th > 10);
        ppt.close();
    }

    private static XSLFTableCell bug60373_addCell(XSLFTableRow r) {
        XSLFTableCell cell = r.addCell();
        XSLFTextParagraph p = cell.addNewTextParagraph();
        XSLFTextRun tr = p.addNewTextRun();
        tr.setText("t");
        return cell;
    }

    @Test
    public void bug60715() throws IOException {
        XMLSlideShow ppt = XSLFTestDataSamples.openSampleDocument("bug60715.pptx");
        ppt.createSlide();
        ppt.close();
    }

    @Test
    public void bug60662() throws IOException {
        XMLSlideShow src = new XMLSlideShow();
        XSLFSlide sl = src.createSlide();
        XSLFGroupShape gs = sl.createGroup();
        gs.setAnchor(new Rectangle2D.Double(100,100,100,100));
        gs.setInteriorAnchor(new Rectangle2D.Double(0,0,100,100));
        XSLFAutoShape as = gs.createAutoShape();
        as.setAnchor(new Rectangle2D.Double(0,0,100,100));
        as.setShapeType(ShapeType.STAR_24);
        as.setFillColor(Color.YELLOW);
        CTShape csh = (CTShape)as.getXmlObject();
        CTOuterShadowEffect shadow = csh.getSpPr().addNewEffectLst().addNewOuterShdw();
        shadow.setDir(270000);
        shadow.setDist(100000);
        shadow.addNewSrgbClr().setVal(new byte[] {0x00, (byte)0xFF, 0x00});
        
        XMLSlideShow dst = new XMLSlideShow();
        XSLFSlide sl2 = dst.createSlide();
        sl2.importContent(sl);
        XSLFGroupShape gs2 = (XSLFGroupShape)sl2.getShapes().get(0);
        XSLFAutoShape as2 = (XSLFAutoShape)gs2.getShapes().get(0);
        CTShape csh2 = (CTShape)as2.getXmlObject();
        assertTrue(csh2.getSpPr().isSetEffectLst());
        
        dst.close();
        src.close();
    }

    @Test
    public void test60810() throws IOException {
        XMLSlideShow ppt = XSLFTestDataSamples.openSampleDocument("60810.pptx");
        for(XSLFSlide slide : ppt.getSlides()) {
            XSLFNotes notesSlide = ppt.getNotesSlide(slide);
            assertNotNull(notesSlide);
        }

        /*OutputStream stream = new FileOutputStream("/tmp/test.pptx");
        try {
            ppt.write(stream);
        } finally {
            stream.close();
        }*/

        ppt.close();
    }

    @Test
    public void test60042() {
        XMLSlideShow ppt = XSLFTestDataSamples.openSampleDocument("60042.pptx");
        ppt.removeSlide(0);
        ppt.createSlide();
    }
}