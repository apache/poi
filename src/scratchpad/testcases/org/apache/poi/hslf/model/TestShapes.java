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

import static org.apache.poi.sl.TestCommonSL.sameColor;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.POIDataSamples;
import org.apache.poi.ddf.AbstractEscherOptRecord;
import org.apache.poi.ddf.EscherDgRecord;
import org.apache.poi.ddf.EscherDggRecord;
import org.apache.poi.ddf.EscherProperties;
import org.apache.poi.ddf.EscherSimpleProperty;
import org.apache.poi.hslf.usermodel.HSLFAutoShape;
import org.apache.poi.hslf.usermodel.HSLFGroupShape;
import org.apache.poi.hslf.usermodel.HSLFLine;
import org.apache.poi.hslf.usermodel.HSLFPictureData;
import org.apache.poi.hslf.usermodel.HSLFPictureShape;
import org.apache.poi.hslf.usermodel.HSLFShape;
import org.apache.poi.hslf.usermodel.HSLFSimpleShape;
import org.apache.poi.hslf.usermodel.HSLFSlide;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.HSLFTextBox;
import org.apache.poi.hslf.usermodel.HSLFTextParagraph;
import org.apache.poi.hslf.usermodel.HSLFTextRun;
import org.apache.poi.hslf.usermodel.HSLFTextShape;
import org.apache.poi.sl.usermodel.PictureData.PictureType;
import org.apache.poi.sl.usermodel.ShapeType;
import org.apache.poi.sl.usermodel.StrokeStyle.LineDash;
import org.junit.Before;
import org.junit.Test;

/**
 * Test drawing shapes via Graphics2D
 */
public final class TestShapes {
    private static POIDataSamples _slTests = POIDataSamples.getSlideShowInstance();

    private HSLFSlideShow ppt;
    private HSLFSlideShow pptB;

    @Before
    public void setUp() throws Exception {
        InputStream is1 = null, is2 = null;
        try {
            is1 = _slTests.openResourceAsStream("empty.ppt");
            ppt = new HSLFSlideShow(is1);
            is2 = _slTests.openResourceAsStream("empty_textbox.ppt");
            pptB = new HSLFSlideShow(is2);
        } finally {
            is1.close();
            is2.close();
        }
    }

    @Test
    public void graphics() throws IOException {
        HSLFSlide slide = ppt.createSlide();

        HSLFLine line = new HSLFLine();
        java.awt.Rectangle lineAnchor = new java.awt.Rectangle(100, 200, 50, 60);
        line.setAnchor(lineAnchor);
        line.setLineWidth(3);
        line.setLineDash(LineDash.DASH);
        line.setLineColor(Color.red);
        slide.addShape(line);

        HSLFAutoShape ellipse = new HSLFAutoShape(ShapeType.ELLIPSE);
        Rectangle2D ellipseAnchor = new Rectangle2D.Double(320, 154, 55, 111);
        ellipse.setAnchor(ellipseAnchor);
        ellipse.setLineWidth(2);
        ellipse.setLineDash(LineDash.SOLID);
        ellipse.setLineColor(Color.green);
        ellipse.setFillColor(Color.lightGray);
        slide.addShape(ellipse);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ppt.write(out);
        out.close();

        //read ppt from byte array

        HSLFSlideShow ppt2 = new HSLFSlideShow(new ByteArrayInputStream(out.toByteArray()));
        assertEquals(1, ppt2.getSlides().size());

        slide = ppt2.getSlides().get(0);
        List<HSLFShape> shape = slide.getShapes();
        assertEquals(2, shape.size());

        assertTrue(shape.get(0) instanceof HSLFLine); //group shape
        assertEquals(lineAnchor, shape.get(0).getAnchor()); //group shape

        assertTrue(shape.get(1) instanceof HSLFAutoShape); //group shape
        assertEquals(ellipseAnchor, shape.get(1).getAnchor()); //group shape
        
        ppt2.close();
    }

    /**
     * Verify that we can read TextBox shapes
     * @throws Exception
     */
    @Test
    public void textBoxRead() throws IOException {
        ppt = new HSLFSlideShow(_slTests.openResourceAsStream("with_textbox.ppt"));
        HSLFSlide sl = ppt.getSlides().get(0);
        for (HSLFShape sh : sl.getShapes()) {
            assertTrue(sh instanceof HSLFTextBox);
            HSLFTextBox txtbox = (HSLFTextBox)sh;
            String text = txtbox.getText();
            assertNotNull(text);

            assertEquals(txtbox.getTextParagraphs().get(0).getTextRuns().size(), 1);
            HSLFTextRun rt = txtbox.getTextParagraphs().get(0).getTextRuns().get(0);

            if (text.equals("Hello, World!!!")){
                assertEquals(32, rt.getFontSize(), 0);
                assertTrue(rt.isBold());
                assertTrue(rt.isItalic());
            } else if (text.equals("I am just a poor boy")){
                assertEquals(44, rt.getFontSize(), 0);
                assertTrue(rt.isBold());
            } else if (text.equals("This is Times New Roman")){
                assertEquals(16, rt.getFontSize(), 0);
                assertTrue(rt.isBold());
                assertTrue(rt.isItalic());
                assertTrue(rt.isUnderlined());
            } else if (text.equals("Plain Text")){
                assertEquals(18, rt.getFontSize(), 0);
            }
        }
    }

    @SuppressWarnings("unused")
    @Test
    public void testParagraphs() throws IOException {
        HSLFSlideShow ss = new HSLFSlideShow();
        HSLFSlide slide = ss.createSlide();
        HSLFTextBox shape = new HSLFTextBox();
        HSLFTextRun p1r1 = shape.setText("para 1 run 1. ");
        HSLFTextRun p1r2 = shape.appendText("para 1 run 2.", false);
        HSLFTextRun p2r1 = shape.appendText("para 2 run 1. ", true);
        HSLFTextRun p2r2 = shape.appendText("para 2 run 2. ", false);
        p1r1.setFontColor(Color.black);
        p1r2.setFontColor(Color.red);
        p2r1.setFontColor(Color.yellow);
        p2r2.setStrikethrough(true);
        // run 3 has same text properties as run 2 and will be merged when saving
        HSLFTextRun p2r3 = shape.appendText("para 2 run 3.", false);
        shape.setAnchor(new Rectangle2D.Double(100,100,100,10));
        slide.addShape(shape);
        shape.resizeToFitText();
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ss.write(bos);
        
        ss = new HSLFSlideShow(new ByteArrayInputStream(bos.toByteArray()));
        slide = ss.getSlides().get(0);
        HSLFTextBox tb = (HSLFTextBox)slide.getShapes().get(0);
        List<HSLFTextParagraph> para = tb.getTextParagraphs();
        HSLFTextRun tr = para.get(0).getTextRuns().get(0);
        assertEquals("para 1 run 1. ", tr.getRawText());
        assertTrue(sameColor(Color.black, tr.getFontColor()));
        tr = para.get(0).getTextRuns().get(1);
        assertEquals("para 1 run 2.\r",  tr.getRawText());
        assertTrue(sameColor(Color.red, tr.getFontColor()));
        tr = para.get(1).getTextRuns().get(0);
        assertEquals("para 2 run 1. ", tr.getRawText());
        assertTrue(sameColor(Color.yellow, tr.getFontColor()));
        tr = para.get(1).getTextRuns().get(1);
        assertEquals("para 2 run 2. para 2 run 3.", tr.getRawText());
        assertTrue(sameColor(Color.black, tr.getFontColor()));
        assertTrue(tr.isStrikethrough());
    }
        
    
    /**
     * Verify that we can add TextBox shapes to a slide
     * and set some of the style attributes
     */
    @Test
    public void textBoxWriteBytes() throws IOException {
        ppt = new HSLFSlideShow();
        HSLFSlide sl = ppt.createSlide();
        HSLFTextRun rt;

        String val = "Hello, World!";

        // Create a new textbox, and give it lots of properties
        HSLFTextBox txtbox = new HSLFTextBox();
        rt = txtbox.getTextParagraphs().get(0).getTextRuns().get(0);
        txtbox.setText(val);
        rt.setFontFamily("Arial");
        rt.setFontSize(42d);
        rt.setBold(true);
        rt.setItalic(true);
        rt.setUnderlined(false);
        rt.setFontColor(Color.red);
        sl.addShape(txtbox);

        // Check it before save
        rt = txtbox.getTextParagraphs().get(0).getTextRuns().get(0);
        assertEquals(val, rt.getRawText());
        assertEquals(42, rt.getFontSize(), 0);
        assertTrue(rt.isBold());
        assertTrue(rt.isItalic());
        assertFalse(rt.isUnderlined());
        assertEquals("Arial", rt.getFontFamily());
        assertTrue(sameColor(Color.red, rt.getFontColor()));

        // Serialize and read again
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ppt.write(out);
        out.close();

        HSLFSlideShow ppt2 = new HSLFSlideShow(new ByteArrayInputStream(out.toByteArray()));
        sl = ppt2.getSlides().get(0);

        txtbox = (HSLFTextBox)sl.getShapes().get(0);
        rt = txtbox.getTextParagraphs().get(0).getTextRuns().get(0);

        // Check after save
        assertEquals(val, rt.getRawText());
        assertEquals(42, rt.getFontSize(), 0);
        assertTrue(rt.isBold());
        assertTrue(rt.isItalic());
        assertFalse(rt.isUnderlined());
        assertEquals("Arial", rt.getFontFamily());
        assertTrue(sameColor(Color.red, rt.getFontColor()));
        
        ppt2.close();
    }

    /**
     * Test with an empty text box
     */
    @Test
    public void emptyTextBox() {
    	assertEquals(2, pptB.getSlides().size());
    	HSLFSlide s1 = pptB.getSlides().get(0);
    	HSLFSlide s2 = pptB.getSlides().get(1);

    	// Check we can get the shapes count
    	assertEquals(2, s1.getShapes().size());
    	assertEquals(2, s2.getShapes().size());
    }

    /**
     * If you iterate over text shapes in a slide and collect them in a set
     * it must be the same as returned by Slide.getTextRuns().
     */
    @Test
    public void textBoxSet() throws IOException {
        textBoxSet("with_textbox.ppt");
        textBoxSet("basic_test_ppt_file.ppt");
        textBoxSet("next_test_ppt_file.ppt");
        textBoxSet("Single_Coloured_Page.ppt");
        textBoxSet("Single_Coloured_Page_With_Fonts_and_Alignments.ppt");
        textBoxSet("incorrect_slide_order.ppt");
    }

    private void textBoxSet(String filename) throws IOException {
        HSLFSlideShow ss = new HSLFSlideShow(_slTests.openResourceAsStream(filename));
        for (HSLFSlide sld : ss.getSlides()) {
            ArrayList<String> lst1 = new ArrayList<>();
            for (List<HSLFTextParagraph> txt : sld.getTextParagraphs()) {
                for (HSLFTextParagraph p : txt) {
                    for (HSLFTextRun r : p) {
                        lst1.add(r.getRawText());
                    }
                }
            }

            ArrayList<String> lst2 = new ArrayList<>();
            for (HSLFShape sh : sld.getShapes()) {
                if (sh instanceof HSLFTextShape){
                    HSLFTextShape tbox = (HSLFTextShape)sh;
                    for (HSLFTextParagraph p : tbox.getTextParagraphs()) {
                        for (HSLFTextRun r : p) {
                            lst2.add(r.getRawText());
                        }
                    }
                }
            }
            assertTrue(lst1.containsAll(lst2));
            assertTrue(lst2.containsAll(lst1));
        }
        ss.close();
    }

    /**
     * Test adding shapes to <code>ShapeGroup</code>
     */
    @Test
    public void shapeGroup() throws IOException {
        HSLFSlideShow ss = new HSLFSlideShow();

        HSLFSlide slide = ss.createSlide();
        Dimension pgsize = ss.getPageSize();

        HSLFGroupShape group = new HSLFGroupShape();

        group.setAnchor(new Rectangle2D.Double(0, 0, pgsize.getWidth(), pgsize.getHeight()));
        slide.addShape(group);

        HSLFPictureData data = ss.addPicture(_slTests.readFile("clock.jpg"), PictureType.JPEG);
        HSLFPictureShape pict = new HSLFPictureShape(data, group);
        pict.setAnchor(new Rectangle2D.Double(0, 0, 200, 200));
        group.addShape(pict);

        HSLFLine line = new HSLFLine(group);
        line.setAnchor(new Rectangle2D.Double(300, 300, 500, 0));
        group.addShape(line);

        //serialize and read again.
        ByteArrayOutputStream  out = new ByteArrayOutputStream();
        ss.write(out);
        out.close();
        ss.close();

        ByteArrayInputStream is = new ByteArrayInputStream(out.toByteArray());
        ss = new HSLFSlideShow(is);
        is.close();

        slide = ss.getSlides().get(0);

        List<HSLFShape> shape = slide.getShapes();
        assertEquals(1, shape.size());
        assertTrue(shape.get(0) instanceof HSLFGroupShape);

        group = (HSLFGroupShape)shape.get(0);
        List<HSLFShape> grshape = group.getShapes();
        assertEquals(2, grshape.size());
        assertTrue(grshape.get(0) instanceof HSLFPictureShape);
        assertTrue(grshape.get(1) instanceof HSLFLine);

        pict = (HSLFPictureShape)grshape.get(0);
        assertEquals(new Rectangle2D.Double(0, 0, 200, 200), pict.getAnchor());

        line = (HSLFLine)grshape.get(1);
        assertEquals(new Rectangle2D.Double(300, 300, 500, 0), line.getAnchor());
        
        ss.close();
    }

    /**
     * Test functionality of Sheet.removeShape(Shape shape)
     */
    @Test
    public void removeShapes() throws IOException {
        String file = "with_textbox.ppt";
        HSLFSlideShow ss = new HSLFSlideShow(_slTests.openResourceAsStream(file));
        HSLFSlide sl = ss.getSlides().get(0);
        List<HSLFShape> sh = sl.getShapes();
        assertEquals("expected four shaped in " + file, 4, sh.size());
        //remove all
        for (int i = 0; i < sh.size(); i++) {
            boolean ok = sl.removeShape(sh.get(i));
            assertTrue("Failed to delete shape #" + i, ok);
        }
        //now Slide.getShapes() should return an empty array
        assertEquals("expected 0 shaped in " + file, 0, sl.getShapes().size());

        //serialize and read again. The file should be readable and contain no shapes
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ss.write(out);
        out.close();
        ss.close();

        ss = new HSLFSlideShow(new ByteArrayInputStream(out.toByteArray()));
        sl = ss.getSlides().get(0);
        assertEquals("expected 0 shaped in " + file, 0, sl.getShapes().size());
        ss.close();
    }

    @Test
    public void lineWidth() {
        HSLFSimpleShape sh = new HSLFAutoShape(ShapeType.RT_TRIANGLE);

        AbstractEscherOptRecord opt = sh.getEscherOptRecord();
        EscherSimpleProperty prop = HSLFSimpleShape.getEscherProperty(opt, EscherProperties.LINESTYLE__LINEWIDTH);
        assertNull(prop);
        assertEquals(HSLFSimpleShape.DEFAULT_LINE_WIDTH, sh.getLineWidth(), 0);

        sh.setLineWidth(1.0);
        prop = HSLFSimpleShape.getEscherProperty(opt, EscherProperties.LINESTYLE__LINEWIDTH);
        assertNotNull(prop);
        assertEquals(1.0, sh.getLineWidth(), 0);
    }

    @Test
    public void shapeId() throws IOException {
        HSLFSlideShow ss = new HSLFSlideShow();
        HSLFSlide slide = ss.createSlide();
        HSLFShape shape = null;

        //EscherDgg is a document-level record which keeps track of the drawing groups
        EscherDggRecord dgg = ss.getDocumentRecord().getPPDrawingGroup().getEscherDggRecord();
        EscherDgRecord dg = slide.getSheetContainer().getPPDrawing().getEscherDgRecord();

        int dggShapesUsed = dgg.getNumShapesSaved();   //total number of shapes in the ppt
        int dggMaxId = dgg.getShapeIdMax();            //max number of shapeId

        int dgMaxId = dg.getLastMSOSPID();   //max shapeId in the slide
        int dgShapesUsed = dg.getNumShapes();          // number of shapes in the slide
        //insert 3 shapes and make sure the Ids are properly incremented
        for (int i = 0; i < 3; i++) {
            shape = new HSLFLine();
            assertEquals(0, shape.getShapeId());
            slide.addShape(shape);
            assertTrue(shape.getShapeId() > 0);

            //check that EscherDgRecord is updated
            assertEquals(shape.getShapeId(), dg.getLastMSOSPID());
            assertEquals(dgMaxId + 1, dg.getLastMSOSPID());
            assertEquals(dgShapesUsed + 1, dg.getNumShapes());

            //check that EscherDggRecord is updated
            assertEquals("mismatch @"+i, shape.getShapeId() + 1, dgg.getShapeIdMax());
            assertEquals("mismatch @"+i, dggMaxId + 1, dgg.getShapeIdMax());
            assertEquals("mismatch @"+i, dggShapesUsed + 1, dgg.getNumShapesSaved());

            dggShapesUsed = dgg.getNumShapesSaved();
            dggMaxId = dgg.getShapeIdMax();
            dgMaxId = dg.getLastMSOSPID();
            dgShapesUsed = dg.getNumShapes();
        }


        //For each drawing group PPT allocates clusters with size=1024
        //if the number of shapes is greater that 1024 a new cluster is allocated
        //make sure it is so
        int numClusters = dgg.getNumIdClusters();
        for (int i = 0; i < 1025; i++) {
            shape = new HSLFLine();
            slide.addShape(shape);
        }
        assertEquals(numClusters + 1, dgg.getNumIdClusters());
        ss.close();
    }

    @Test
    public void lineColor() throws IOException {
        HSLFSlideShow ss = new HSLFSlideShow(_slTests.openResourceAsStream("51731.ppt"));
        List<HSLFShape> shape = ss.getSlides().get(0).getShapes();

        assertEquals(4, shape.size());

        HSLFTextShape sh1 = (HSLFTextShape)shape.get(0);
        assertEquals("Hello Apache POI", sh1.getText());
        assertNull(sh1.getLineColor());

        HSLFTextShape sh2 = (HSLFTextShape)shape.get(1);
        assertEquals("Why are you showing this border?", sh2.getText());
        assertNull(sh2.getLineColor());

        HSLFTextShape sh3 = (HSLFTextShape)shape.get(2);
        assertEquals("Text in a black border", sh3.getText());
        assertEquals(Color.black, sh3.getLineColor());
        assertEquals(0.75, sh3.getLineWidth(), 0);

        HSLFTextShape sh4 = (HSLFTextShape)shape.get(3);
        assertEquals("Border width is 5 pt", sh4.getText());
        assertEquals(Color.black, sh4.getLineColor());
        assertEquals(5.0, sh4.getLineWidth(), 0);
        
        ss.close();
    }
}
