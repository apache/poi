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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

import org.apache.poi.sl.draw.DrawFactory;
import org.apache.poi.sl.draw.geom.TestPresetGeometries;
import org.apache.poi.sl.usermodel.Placeholder;
import org.apache.poi.sl.usermodel.Slide;
import org.apache.poi.sl.usermodel.StrokeStyle.LineCap;
import org.apache.poi.sl.usermodel.StrokeStyle.LineDash;
import org.apache.poi.util.Units;
import org.apache.poi.xslf.XSLFTestDataSamples;
import org.apache.xmlbeans.XmlObject;
import org.junit.Test;
import org.openxmlformats.schemas.drawingml.x2006.main.CTEffectStyleItem;
import org.openxmlformats.schemas.drawingml.x2006.main.CTEffectStyleList;
import org.openxmlformats.schemas.drawingml.x2006.main.CTOuterShadowEffect;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPresetGeometry2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSchemeColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTShapeProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTStyleMatrix;
import org.openxmlformats.schemas.drawingml.x2006.main.STLineCap;
import org.openxmlformats.schemas.drawingml.x2006.main.STPresetLineDashVal;
import org.openxmlformats.schemas.drawingml.x2006.main.STShapeType;

public class TestXSLFSimpleShape {
    
    @Test
    public void testLineStyles() throws IOException {
        XMLSlideShow ppt = new XMLSlideShow();
        XSLFSlide slide = ppt.createSlide();

        XSLFSimpleShape shape = slide.createAutoShape();
        assertEquals(1, slide.getShapes().size());
        // line properties are not set by default
        assertFalse(getSpPr(shape).isSetLn());

        assertEquals(0., shape.getLineWidth(), 0);
        assertNull(shape.getLineColor());
        assertNull(shape.getLineDash());
        assertNull(shape.getLineCap());

        shape.setLineWidth(0);
        shape.setLineColor(null);
        shape.setLineDash(null);
        shape.setLineCap(null);

        assertTrue(getSpPr(shape).isSetLn());
        assertTrue(getSpPr(shape).getLn().isSetNoFill());

        // line width
        shape.setLineWidth(1.0);
        assertEquals(1.0, shape.getLineWidth(), 0);
        assertEquals(Units.EMU_PER_POINT, getSpPr(shape).getLn().getW());
        shape.setLineWidth(5.5);
        assertEquals(5.5, shape.getLineWidth(), 0);
        assertEquals(Units.toEMU(5.5), getSpPr(shape).getLn().getW());
        shape.setLineWidth(0.0);
        // setting line width to zero unsets the W attribute
        assertFalse(getSpPr(shape).getLn().isSetW());

        // line cap
        shape.setLineCap(LineCap.FLAT);
        assertEquals(LineCap.FLAT, shape.getLineCap());
        assertEquals(STLineCap.FLAT, getSpPr(shape).getLn().getCap());
        shape.setLineCap(LineCap.SQUARE);
        assertEquals(LineCap.SQUARE, shape.getLineCap());
        assertEquals(STLineCap.SQ, getSpPr(shape).getLn().getCap());
        shape.setLineCap(LineCap.ROUND);
        assertEquals(LineCap.ROUND, shape.getLineCap());
        assertEquals(STLineCap.RND, getSpPr(shape).getLn().getCap());
        shape.setLineCap(null);
        // setting cap to null unsets the Cap attribute
        assertFalse(getSpPr(shape).getLn().isSetCap());

        // line dash
        shape.setLineDash(LineDash.SOLID);
        assertEquals(LineDash.SOLID, shape.getLineDash());
        assertEquals(STPresetLineDashVal.SOLID, getSpPr(shape).getLn().getPrstDash().getVal());
        shape.setLineDash(LineDash.DASH_DOT);
        assertEquals(LineDash.DASH_DOT, shape.getLineDash());
        assertEquals(STPresetLineDashVal.DASH_DOT, getSpPr(shape).getLn().getPrstDash().getVal());
        shape.setLineDash(LineDash.LG_DASH_DOT);
        assertEquals(LineDash.LG_DASH_DOT, shape.getLineDash());
        assertEquals(STPresetLineDashVal.LG_DASH_DOT, getSpPr(shape).getLn().getPrstDash().getVal());
        shape.setLineDash(null);
        // setting dash width to null unsets the Dash element
        assertFalse(getSpPr(shape).getLn().isSetPrstDash());

        // line color
        assertFalse(getSpPr(shape).getLn().isSetSolidFill());
        shape.setLineColor(Color.RED);
        assertEquals(Color.RED, shape.getLineColor());
        assertTrue(getSpPr(shape).getLn().isSetSolidFill());
        shape.setLineColor(Color.BLUE);
        assertEquals(Color.BLUE, shape.getLineColor());
        assertTrue(getSpPr(shape).getLn().isSetSolidFill());
        shape.setLineColor(null);
        assertNull(shape.getLineColor());
        // setting dash width to null unsets the SolidFill element
        assertFalse(getSpPr(shape).getLn().isSetSolidFill());

        XSLFSimpleShape ln2 = slide.createAutoShape();
        ln2.setLineDash(LineDash.DOT);
        assertEquals(LineDash.DOT, ln2.getLineDash());
        ln2.setLineWidth(0.);
        assertEquals(0., ln2.getLineWidth(), 0);

        XSLFSimpleShape ln3 = slide.createAutoShape();
        ln3.setLineWidth(1.);
        assertEquals(1., ln3.getLineWidth(), 0);
        ln3.setLineDash(null);
        assertNull(ln3.getLineDash());
        ln3.setLineCap(null);
        assertNull(ln3.getLineDash());
        
        ppt.close();
    }

    @Test
    public void testFill() throws IOException {
        XMLSlideShow ppt = new XMLSlideShow();
        XSLFSlide slide = ppt.createSlide();

        XSLFAutoShape shape = slide.createAutoShape();
        // line properties are not set by default
        assertFalse(getSpPr(shape).isSetSolidFill());

        assertNull(shape.getFillColor());
        shape.setFillColor(null);
        assertNull(shape.getFillColor());
        assertFalse(getSpPr(shape).isSetSolidFill());

        shape.setFillColor(Color.RED);
        assertEquals(Color.RED, shape.getFillColor());
        shape.setFillColor(Color.DARK_GRAY);
        assertEquals(Color.DARK_GRAY, shape.getFillColor());
        assertTrue(getSpPr(shape).isSetSolidFill());

        shape.setFillColor(null);
        assertNull(shape.getFillColor());
        assertFalse(getSpPr(shape).isSetSolidFill());
        ppt.close();
    }

    @Test
    public void testDefaultProperties() throws IOException {
        XMLSlideShow ppt = XSLFTestDataSamples.openSampleDocument("shapes.pptx");

        XSLFSlide slide6 = ppt.getSlides().get(5);
        List<XSLFShape> shapes = slide6.getShapes();
        for(XSLFShape xs : shapes){
            XSLFSimpleShape s = (XSLFSimpleShape)xs;
            // all shapes have a theme color="accent1"
            assertEquals("accent1", s.getSpStyle().getFillRef().getSchemeClr().getVal().toString());
            assertEquals(2.0, s.getLineWidth(), 0);
            assertEquals(LineCap.FLAT, s.getLineCap());
            assertEquals(new Color(79,129,189), s.getLineColor());
        }

        XSLFSimpleShape s0 = (XSLFSimpleShape) shapes.get(0);
        // fill is not set
        assertNull(getSpPr(s0).getSolidFill());
        //assertEquals(slide6.getTheme().getColor("accent1").getColor(), s0.getFillColor());
        assertEquals(new Color(79, 129, 189), s0.getFillColor());

        // lighter 80%
        XSLFSimpleShape s1 = (XSLFSimpleShape)shapes.get(1);
        CTSchemeColor ref1 = getSpPr(s1).getSolidFill().getSchemeClr();
        assertEquals(1, ref1.sizeOfLumModArray());
        assertEquals(1, ref1.sizeOfLumOffArray());
        assertEquals(20000, ref1.getLumModArray(0).getVal());
        assertEquals(80000, ref1.getLumOffArray(0).getVal());
        assertEquals("accent1", ref1.getVal().toString());
        assertEquals(new Color(220, 230, 242), s1.getFillColor());

        // lighter 60%
        XSLFSimpleShape s2 = (XSLFSimpleShape)shapes.get(2);
        CTSchemeColor ref2 = getSpPr(s2).getSolidFill().getSchemeClr();
        assertEquals(1, ref2.sizeOfLumModArray());
        assertEquals(1, ref2.sizeOfLumOffArray());
        assertEquals(40000, ref2.getLumModArray(0).getVal());
        assertEquals(60000, ref2.getLumOffArray(0).getVal());
        assertEquals("accent1", ref2.getVal().toString());
        assertEquals(new Color(185, 205, 229), s2.getFillColor());

        // lighter 40%
        XSLFSimpleShape s3 = (XSLFSimpleShape)shapes.get(3);
        CTSchemeColor ref3 = getSpPr(s3).getSolidFill().getSchemeClr();
        assertEquals(1, ref3.sizeOfLumModArray());
        assertEquals(1, ref3.sizeOfLumOffArray());
        assertEquals(60000, ref3.getLumModArray(0).getVal());
        assertEquals(40000, ref3.getLumOffArray(0).getVal());
        assertEquals("accent1", ref3.getVal().toString());
        assertEquals(new Color(149, 179, 215), s3.getFillColor());

        // darker 25%
        XSLFSimpleShape s4 = (XSLFSimpleShape)shapes.get(4);
        CTSchemeColor ref4 = getSpPr(s4).getSolidFill().getSchemeClr();
        assertEquals(1, ref4.sizeOfLumModArray());
        assertEquals(0, ref4.sizeOfLumOffArray());
        assertEquals(75000, ref4.getLumModArray(0).getVal());
        assertEquals("accent1", ref3.getVal().toString());
        assertEquals(new Color(55, 96, 146), s4.getFillColor());

        XSLFSimpleShape s5 = (XSLFSimpleShape)shapes.get(5);
        CTSchemeColor ref5 = getSpPr(s5).getSolidFill().getSchemeClr();
        assertEquals(1, ref5.sizeOfLumModArray());
        assertEquals(0, ref5.sizeOfLumOffArray());
        assertEquals(50000, ref5.getLumModArray(0).getVal());
        assertEquals("accent1", ref5.getVal().toString());
        assertEquals(new Color(37, 64, 97), s5.getFillColor());
        
        ppt.close();
    }

    @Test
    public void testAnchor() throws IOException {
        XMLSlideShow ppt = XSLFTestDataSamples.openSampleDocument("shapes.pptx");
        List<XSLFSlide> slide = ppt.getSlides();

        XSLFSlide slide2 = slide.get(1);
        XSLFSlideLayout layout2 = slide2.getSlideLayout();
        List<XSLFShape> shapes2 = slide2.getShapes();
        XSLFTextShape sh1 = (XSLFTextShape)shapes2.get(0);
        assertEquals(Placeholder.CENTERED_TITLE, sh1.getTextType());
        assertEquals("PPTX Title", sh1.getText());
        assertFalse(getSpPr(sh1).isSetXfrm()); // xfrm is not set, the query is delegated to the slide layout
        assertEquals(sh1.getAnchor(), layout2.getTextShapeByType(Placeholder.CENTERED_TITLE).getAnchor());

        XSLFTextShape sh2 = (XSLFTextShape)shapes2.get(1);
        assertEquals("Subtitle\nAnd second line", sh2.getText());
        assertEquals(Placeholder.SUBTITLE, sh2.getTextType());
        assertFalse(getSpPr(sh2).isSetXfrm()); // xfrm is not set, the query is delegated to the slide layout
        assertEquals(sh2.getAnchor(), layout2.getTextShapeByType(Placeholder.SUBTITLE).getAnchor());

        XSLFSlide slide5 = slide.get(4);
        XSLFSlideLayout layout5 = slide5.getSlideLayout();
        XSLFTextShape shTitle = slide5.getTextShapeByType(Placeholder.TITLE);
        assertEquals("Hyperlinks", shTitle.getText());
        // xfrm is not set, the query is delegated to the slide layout
        assertFalse(getSpPr(shTitle).isSetXfrm());
        // xfrm is not set, the query is delegated to the slide master
        assertFalse(getSpPr(layout5.getTextShapeByType(Placeholder.TITLE)).isSetXfrm());
        assertTrue(getSpPr(layout5.getSlideMaster().getTextShapeByType(Placeholder.TITLE)).isSetXfrm());
        assertEquals(shTitle.getAnchor(), layout5.getSlideMaster().getTextShapeByType(Placeholder.TITLE).getAnchor());

        ppt.close();
    }

    @SuppressWarnings("unused")
    @Test
    public void testShadowEffects() throws IOException{
        XMLSlideShow ppt = new XMLSlideShow();
        XSLFSlide slide = ppt.createSlide();
        CTStyleMatrix styleMatrix = slide.getTheme().getXmlObject().getThemeElements().getFmtScheme();
        CTEffectStyleList lst = styleMatrix.getEffectStyleLst();
        assertNotNull(lst);
        for(CTEffectStyleItem ef : lst.getEffectStyleArray()){
            CTOuterShadowEffect obj = ef.getEffectLst().getOuterShdw();
        }
        ppt.close();
    }
    
    @Test
    public void testValidGeometry() throws Exception {
        XMLSlideShow ppt = new XMLSlideShow();
        XSLFSlide slide = ppt.createSlide();

        XSLFSimpleShape shape = slide.createAutoShape();
        CTShapeProperties spPr = getSpPr(shape);
        
        CTPresetGeometry2D prstGeom = CTPresetGeometry2D.Factory.newInstance();
        prstGeom.setPrst(STShapeType.Enum.forInt(1));
        
        assertNotNull(prstGeom.getPrst());
        assertNotNull(prstGeom.getPrst().toString());
        assertNotNull(spPr.getPrstGeom());
        spPr.setPrstGeom(prstGeom);
        assertNotNull(spPr.getPrstGeom().getPrst());
        assertNotNull(spPr.getPrstGeom().getPrst().toString());
        
        assertNotNull(shape.getGeometry());
        
        ppt.close();
    }

    
    @Test
    public void testInvalidGeometry() throws Exception {
        XMLSlideShow ppt = new XMLSlideShow();
        XSLFSlide slide = ppt.createSlide();

        XSLFSimpleShape shape = slide.createAutoShape();
        CTShapeProperties spPr = getSpPr(shape);
        
        CTPresetGeometry2D prstGeom = CTPresetGeometry2D.Factory.newInstance();
        prstGeom.setPrst(STShapeType.Enum.forInt(1));
        
        assertNotNull(prstGeom.getPrst());
        assertNotNull(prstGeom.getPrst().toString());
        assertNotNull(spPr.getPrstGeom());
        spPr.setPrstGeom(prstGeom);
        assertNotNull(spPr.getPrstGeom().getPrst());
        assertNotNull(spPr.getPrstGeom().getPrst().toString());
        
        try {
            // cause the geometries to be not found
            TestPresetGeometries.clearPreset();
            try {
                shape.getGeometry();
                fail("Should fail without the geometry");
            } catch (IllegalStateException e) {
                assertTrue(e.getMessage(), e.getMessage().contains("line"));
            }
        } finally {
            // reset to not affect other tests
            TestPresetGeometries.resetPreset();
        }
        
        ppt.close();
    }

    @SuppressWarnings("Duplicates")
    @Test
    public void testArrayStoreException() throws IOException {
        XMLSlideShow ppt = XSLFTestDataSamples.openSampleDocument("aascu.org_workarea_downloadasset.aspx_id=5864.pptx");
        Dimension pgsize = ppt.getPageSize();

        for (Slide<?,?> s : ppt.getSlides()) {
            //System.out.println("Slide: " + s);

            BufferedImage img = new BufferedImage(pgsize.width, pgsize.height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = img.createGraphics();
            DrawFactory.getInstance(graphics).fixFonts(graphics);

            // default rendering options
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

            // draw stuff
            s.draw(graphics);

            graphics.dispose();
            img.flush();
        }
        ppt.close();
    }
    
    static CTShapeProperties getSpPr(XSLFShape shape) {
        XmlObject xo = shape.getShapeProperties();
        assertTrue(xo instanceof CTShapeProperties);
        return (CTShapeProperties)xo;
    }
}