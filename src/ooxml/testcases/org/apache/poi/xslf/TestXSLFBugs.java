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

import static org.apache.poi.POITestCase.assertContains;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.apache.poi.POIDataSamples;
import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.ooxml.POIXMLDocumentPart.RelationPart;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePartName;
import org.apache.poi.openxml4j.opc.PackagingURIHelper;
import org.apache.poi.sl.draw.DrawPaint;
import org.apache.poi.sl.extractor.SlideShowExtractor;
import org.apache.poi.sl.usermodel.PaintStyle;
import org.apache.poi.sl.usermodel.PaintStyle.SolidPaint;
import org.apache.poi.sl.usermodel.PaintStyle.TexturePaint;
import org.apache.poi.sl.usermodel.PictureData;
import org.apache.poi.sl.usermodel.PictureData.PictureType;
import org.apache.poi.sl.usermodel.PictureShape;
import org.apache.poi.sl.usermodel.Shape;
import org.apache.poi.sl.usermodel.ShapeType;
import org.apache.poi.sl.usermodel.Slide;
import org.apache.poi.sl.usermodel.VerticalAlignment;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFAutoShape;
import org.apache.poi.xslf.usermodel.XSLFGroupShape;
import org.apache.poi.xslf.usermodel.XSLFHyperlink;
import org.apache.poi.xslf.usermodel.XSLFNotes;
import org.apache.poi.xslf.usermodel.XSLFPictureData;
import org.apache.poi.xslf.usermodel.XSLFPictureShape;
import org.apache.poi.xslf.usermodel.XSLFRelation;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFSlideLayout;
import org.apache.poi.xslf.usermodel.XSLFSlideMaster;
import org.apache.poi.xslf.usermodel.XSLFTable;
import org.apache.poi.xslf.usermodel.XSLFTableCell;
import org.apache.poi.xslf.usermodel.XSLFTableRow;
import org.apache.poi.xslf.usermodel.XSLFTextBox;
import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.poi.xslf.usermodel.XSLFTextRun;
import org.junit.Ignore;
import org.junit.Test;
import org.openxmlformats.schemas.drawingml.x2006.main.CTOuterShadowEffect;
import org.openxmlformats.schemas.presentationml.x2006.main.CTShape;


public class TestXSLFBugs {
    private static final POIDataSamples slTests = POIDataSamples.getSlideShowInstance();


    @Test
    public void bug61589() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (XMLSlideShow src = new XMLSlideShow();
             XMLSlideShow dest = new XMLSlideShow()) {
            XSLFSlide slide = src.createSlide();
            XSLFSlide slide2 = src.createSlide();

            XSLFTextBox shape = slide.createTextBox();
            shape.setAnchor(new Rectangle2D.Double(100,100,400,100));
            XSLFTextParagraph p = shape.addNewTextParagraph();

            XSLFTextRun r = p.addNewTextRun();
            p.addLineBreak();
            r.setText("Apache POI");
            r.createHyperlink().setAddress("http://poi.apache.org");
            // create hyperlink pointing to a page, which isn't available at the time of importing the content
            r = p.addNewTextRun();
            r.setText("Slide 2");
            r.createHyperlink().linkToSlide(slide2);

            shape = slide2.createTextBox();
            shape.setAnchor(new Rectangle2D.Double(100,100,400,100));
            shape.setText("slide 2");

            dest.createSlide().importContent(slide);
            dest.createSlide().importContent(slide2);

            dest.write(bos);
        }

        try (XMLSlideShow ppt = new XMLSlideShow(new ByteArrayInputStream(bos.toByteArray()))) {
            XSLFSlide slide = ppt.getSlides().get(0);
            XSLFTextBox shape = (XSLFTextBox)slide.getShapes().get(0);
            XSLFTextParagraph p = shape.getTextParagraphs().get(1);
            XSLFHyperlink h1 = p.getTextRuns().get(0).getHyperlink();
            assertNotNull(h1);
            assertEquals("http://poi.apache.org", h1.getAddress());
            XSLFHyperlink h2 = p.getTextRuns().get(2).getHyperlink();
            assertNotNull(h2);
            // relative url will be resolved to an absolute url, therefore this doesn't equals to "slide2.xml"
            assertEquals("/ppt/slides/slide2.xml", h2.getAddress());
            RelationPart sldRef = slide.getRelationPartById(h2.getXmlObject().getId());
            assertTrue(sldRef.getDocumentPart() instanceof XSLFSlide);
        }
    }

    @Test
    public void bug62587() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (XMLSlideShow ppt = new XMLSlideShow()) {
            Slide slide = ppt.createSlide();
            XSLFPictureData pd = ppt.addPicture(slTests.getFile("wrench.emf"), PictureType.EMF);
            PictureShape ps = slide.createPicture(pd);
            ps.setAnchor(new Rectangle2D.Double(100,100,100,100));
            ppt.write(bos);
        }

        Object[][] pics = {
            {"santa.wmf", PictureType.WMF, XSLFRelation.IMAGE_WMF},
            {"tomcat.png",PictureType.PNG, XSLFRelation.IMAGE_PNG},
            {"clock.jpg", PictureType.JPEG, XSLFRelation.IMAGE_JPEG}
        };

        try (XMLSlideShow ppt = new XMLSlideShow(new ByteArrayInputStream(bos.toByteArray()))) {
            XSLFSlide s1 = ppt.getSlides().get(0);

            for (Object[] p : pics) {
                XSLFSlide s2 = ppt.createSlide();
                s2.importContent(s1);

                XSLFPictureData pd = ppt.addPicture(slTests.getFile((String)p[0]), (PictureType)p[1]);
                XSLFPictureShape ps = (XSLFPictureShape) s2.getShapes().get(0);
                Rectangle2D anchor = ps.getAnchor();
                s2.removeShape(ps);
                ps = s2.createPicture(pd);
                ps.setAnchor(anchor);
            }

            bos.reset();
            ppt.write(bos);
        }

        try (XMLSlideShow ppt = new XMLSlideShow(new ByteArrayInputStream(bos.toByteArray()))) {
            for (XSLFSlide sl : ppt.getSlides()) {
                List<RelationPart> rels = sl.getRelationParts();
                assertEquals(2, rels.size());
                RelationPart rel0 = rels.get(0);
                assertEquals("rId1", rel0.getRelationship().getId());
                assertEquals(XSLFRelation.SLIDE_LAYOUT.getRelation(), rel0.getRelationship().getRelationshipType());
                RelationPart rel1 = rels.get(1);
                assertEquals("rId2", rel1.getRelationship().getId());
                assertEquals(XSLFRelation.IMAGES.getRelation(), rel1.getRelationship().getRelationshipType());
            }
        }
    }


    @Test
    public void bug60499() throws IOException, InvalidFormatException {
        InputStream is = slTests.openResourceAsStream("bug60499.pptx");
        byte buf[] = IOUtils.toByteArray(is);
        is.close();

        PackagePartName ppn = PackagingURIHelper.createPartName("/ppt/media/image1.png");
        
        XMLSlideShow ppt1 = new XMLSlideShow(new ByteArrayInputStream(buf));
        XSLFSlide slide1 = ppt1.getSlides().get(0);
        
        Optional<XSLFShape> shapeToDelete1 =
            slide1.getShapes().stream().filter(s -> s instanceof XSLFPictureShape).findFirst();
        
        assertTrue(shapeToDelete1.isPresent());
        slide1.removeShape(shapeToDelete1.get());
        assertTrue(slide1.getRelationParts().stream()
            .allMatch(rp -> "rId1,rId3".contains(rp.getRelationship().getId()) ));
        
        assertNotNull(ppt1.getPackage().getPart(ppn));
        ppt1.close();

        XMLSlideShow ppt2 = new XMLSlideShow(new ByteArrayInputStream(buf));
        XSLFSlide slide2 = ppt2.getSlides().get(0);

        Optional<XSLFShape> shapeToDelete2 =
            slide2.getShapes().stream().filter(s -> s instanceof XSLFPictureShape).skip(1).findFirst();
        assertTrue(shapeToDelete2.isPresent());
        slide2.removeShape(shapeToDelete2.get());
        assertTrue(slide2.getRelationParts().stream()
            .allMatch(rp -> "rId1,rId2".contains(rp.getRelationship().getId()) ));
        assertNotNull(ppt2.getPackage().getPart(ppn));
        ppt2.close();

        XMLSlideShow ppt3 = new XMLSlideShow(new ByteArrayInputStream(buf));
        XSLFSlide slide3 = ppt3.getSlides().get(0);
        slide3.getShapes().stream()
            .filter(s -> s instanceof XSLFPictureShape)
            .collect(Collectors.toList())
            .forEach(slide3::removeShape);
        assertNull(ppt3.getPackage().getPart(ppn));
        ppt3.close();
    }
    
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
    public void bug54916() throws IOException {
        try (XMLSlideShow ss = XSLFTestDataSamples.openSampleDocument("OverlappingRelations.pptx")) {
            XSLFSlide slide;

            // Should find 4 slides
            assertEquals(4, ss.getSlides().size());

            // Check the text, to see we got them in order
            slide = ss.getSlides().get(0);
            assertContains(getSlideText(ss, slide), "POI cannot read this");

            slide = ss.getSlides().get(1);
            assertContains(getSlideText(ss, slide), "POI can read this");
            assertContains(getSlideText(ss, slide), "Has a relationship to another slide");

            slide = ss.getSlides().get(2);
            assertContains(getSlideText(ss, slide), "POI can read this");

            slide = ss.getSlides().get(3);
            assertContains(getSlideText(ss, slide), "POI can read this");
        }
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

    private String getSlideText(XMLSlideShow ppt, XSLFSlide slide) throws IOException {
        try (SlideShowExtractor<XSLFShape,XSLFTextParagraph> extr = new SlideShowExtractor<>(ppt)) {
            // do not auto-close the slideshow
            extr.setFilesystem(null);
            extr.setSlidesByDefault(true);
            extr.setNotesByDefault(false);
            extr.setMasterByDefault(false);
            return extr.getText(slide);
        }
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

        final XSLFRelation expected = XSLFRelation.SLIDE_LAYOUT;
        final POIXMLDocumentPart relation = slide.getRelations().get(0);

        assertEquals(expected.getContentType(), relation.getPackagePart().getContentType());
        assertEquals(expected.getFileName(expected.getFileNameIndex(relation)), relation.getPackagePart().getPartName().getName());

        // Some dummy pictures
        byte[][] pics = new byte[15][3];
        for (int i=0; i<pics.length; i++) {
            for (int j=0; j<pics[i].length; j++) {
                pics[i][j] = (byte)i;
            }
        }

        // Add a few pictures
        addPictures(ss, slide, pics, 0, 10);

        // Re-fetch the pictures and check
        for (int i=0; i<10; i++) {
            XSLFPictureShape shape = (XSLFPictureShape)slide.getShapes().get(i+1);
            assertNotNull(shape.getPictureData());
            assertArrayEquals(pics[i], shape.getPictureData().getData());
        }

        // Add past 10
        addPictures(ss, slide, pics, 10, 15);

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

    private void addPictures(XMLSlideShow ss, XSLFSlide slide, byte[][] pics, int start, int end) {
        for (int i = start; i< end; i++) {
            XSLFPictureData data = ss.addPicture(pics[i], PictureType.JPEG);
            assertEquals(i, data.getIndex());
            assertEquals(i+1, ss.getPictureData().size());

            XSLFPictureShape shape = slide.createPicture(data);
            assertNotNull(shape.getPictureData());
            assertArrayEquals(pics[i], shape.getPictureData().getData());
            assertEquals(i+2, slide.getShapes().size());
        }
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
            assertContains(getSlideText(ss, slide), slideTexts[i]);
        }
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

        ppt.close();
    }

    @Test
    public void test60042() throws IOException {
        XMLSlideShow ppt = XSLFTestDataSamples.openSampleDocument("60042.pptx");
        ppt.removeSlide(0);
        ppt.createSlide();
        ppt.close();
    }

    @Test
    public void test61515() throws IOException {
        XMLSlideShow ppt = XSLFTestDataSamples.openSampleDocument("61515.pptx");
        ppt.removeSlide(0);
        assertEquals(1, ppt.createSlide().getRelations().size());
        try {
            XMLSlideShow saved = XSLFTestDataSamples.writeOutAndReadBack(ppt);
            assertEquals(1, saved.getSlides().size());
            XSLFSlide slide = saved.getSlides().get(0);
            assertEquals(1, slide.getRelations().size());
            saved.close();
        } catch (IOException e) {
            fail("Could not read back saved presentation.");
        }
        ppt.close();
    }

    @Test
    public void testAptia() throws IOException {
        try (XMLSlideShow ppt = XSLFTestDataSamples.openSampleDocument("aptia.pptx");
             XMLSlideShow saved = XSLFTestDataSamples.writeOutAndReadBack(ppt)) {
            assertEquals(ppt.getSlides().size(), saved.getSlides().size());
        }
    }

    @Ignore
    @Test
    public void testDivinoRevelado() throws IOException {
        try (XMLSlideShow ppt = XSLFTestDataSamples.openSampleDocument("Divino_Revelado.pptx");
             XMLSlideShow saved = XSLFTestDataSamples.writeOutAndReadBack(ppt)){
            assertEquals(ppt.getSlides().size(), saved.getSlides().size());
        }
    }

    @Test
    public void bug62051() throws IOException {
        final Function<List<XSLFShape>, int[]> ids = (shapes) ->
            shapes.stream().mapToInt(Shape::getShapeId).toArray();

        try (final XMLSlideShow ppt = new XMLSlideShow()) {
            final XSLFSlide slide = ppt.createSlide();
            final List<XSLFShape> shapes = new ArrayList<>();
            shapes.add(slide.createAutoShape());
            final XSLFGroupShape g1 = slide.createGroup();
            shapes.add(g1);
            final XSLFGroupShape g2 = g1.createGroup();
            shapes.add(g2);
            shapes.add(g2.createAutoShape());
            shapes.add(slide.createAutoShape());
            shapes.add(g2.createAutoShape());
            shapes.add(g1.createAutoShape());
            assertArrayEquals(new int[]{ 2,3,4,5,6,7,8 }, ids.apply(shapes));
            g1.removeShape(g2);
            shapes.remove(5);
            shapes.remove(3);
            shapes.remove(2);
            shapes.add(g1.createAutoShape());
            assertArrayEquals(new int[]{ 2,3,6,8,4 }, ids.apply(shapes));

        }
    }
}
