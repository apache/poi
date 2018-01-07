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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.poi.sl.usermodel.ShapeType;
import org.apache.poi.sl.usermodel.TextParagraph.TextAlign;
import org.apache.poi.sl.usermodel.TextShape.TextAutofit;
import org.apache.poi.sl.usermodel.TextShape.TextDirection;
import org.apache.poi.sl.usermodel.VerticalAlignment;
import org.apache.poi.util.Units;
import org.junit.Test;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextCharacterProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.STTextStrikeType;
import org.openxmlformats.schemas.drawingml.x2006.main.STTextUnderlineType;

public class TestXSLFAutoShape {
    @Test
    public void testTextBodyProperies() throws IOException {
        XMLSlideShow ppt = new XMLSlideShow();
        XSLFSlide slide = ppt.createSlide();

        XSLFAutoShape shape = slide.createAutoShape();
        shape.addNewTextParagraph().addNewTextRun().setText("POI");

        // default margins from slide master
        assertEquals(3.6, shape.getBottomInset(), 0);
        assertEquals(3.6, shape.getTopInset(), 0);
        assertEquals(7.2, shape.getLeftInset(), 0);
        assertEquals(7.2, shape.getRightInset(), 0);

        shape.setBottomInset(1.0);
        assertEquals(1.0, shape.getBottomInset(), 0);
        shape.setTopInset(2.0);
        assertEquals(2.0, shape.getTopInset(), 0);
        shape.setLeftInset(3.0);
        assertEquals(3.0, shape.getLeftInset(), 0);
        shape.setRightInset(4.0);
        assertEquals(4.0, shape.getRightInset(), 0);

        shape.setBottomInset(0.0);
        assertEquals(0.0, shape.getBottomInset(), 0);
        shape.setTopInset(0.0);
        assertEquals(0.0, shape.getTopInset(), 0);
        shape.setLeftInset(0.0);
        assertEquals(0.0, shape.getLeftInset(), 0);
        shape.setRightInset(0.0);
        assertEquals(0.0, shape.getRightInset(), 0);

        // unset to defauls
        shape.setBottomInset(-1);
        assertEquals(3.6, shape.getBottomInset(), 0);
        shape.setTopInset(-1);
        assertEquals(3.6, shape.getTopInset(), 0);
        shape.setLeftInset(-1);
        assertEquals(7.2, shape.getLeftInset(), 0);
        shape.setRightInset(-1);
        assertEquals(7.2, shape.getRightInset(), 0);

        // shape
        assertTrue(shape.getWordWrap());
        shape.setWordWrap(false);
        assertFalse(shape.getWordWrap());
        shape.setWordWrap(true);
        assertTrue(shape.getWordWrap());

        // shape
        assertEquals(TextAutofit.NORMAL, shape.getTextAutofit());
        shape.setTextAutofit(TextAutofit.NONE);
        assertEquals(TextAutofit.NONE, shape.getTextAutofit());
        shape.setTextAutofit(TextAutofit.SHAPE);
        assertEquals(TextAutofit.SHAPE, shape.getTextAutofit());
        shape.setTextAutofit(TextAutofit.NORMAL);
        assertEquals(TextAutofit.NORMAL, shape.getTextAutofit());

        assertEquals(VerticalAlignment.TOP, shape.getVerticalAlignment());
        shape.setVerticalAlignment(VerticalAlignment.BOTTOM);
        assertEquals(VerticalAlignment.BOTTOM, shape.getVerticalAlignment());
        shape.setVerticalAlignment(VerticalAlignment.MIDDLE);
        assertEquals(VerticalAlignment.MIDDLE, shape.getVerticalAlignment());
        shape.setVerticalAlignment(null);
        assertEquals(VerticalAlignment.TOP, shape.getVerticalAlignment());

        assertEquals(TextDirection.HORIZONTAL, shape.getTextDirection());
        shape.setTextDirection(TextDirection.VERTICAL);
        assertEquals(TextDirection.VERTICAL, shape.getTextDirection());
        shape.setTextDirection(null);
        assertEquals(TextDirection.HORIZONTAL, shape.getTextDirection());
        
        ppt.close();
    }

    @Test
    public void testTextParagraph() throws IOException {
        XMLSlideShow ppt = new XMLSlideShow();
        XSLFSlide slide = ppt.createSlide();
        assertTrue(slide.getShapes().isEmpty());

        XSLFAutoShape shape = slide.createAutoShape();
        assertEquals(0, shape.getTextParagraphs().size());
        XSLFTextParagraph p = shape.addNewTextParagraph();
        assertEquals(1, shape.getTextParagraphs().size());

        assertNull(p.getIndent());
        assertEquals(0, p.getLeftMargin(), 0);
        assertNull(p.getLineSpacing());
        assertNull(p.getSpaceAfter());
        assertNull(p.getSpaceBefore());
        assertEquals(0, p.getIndentLevel());

        p.setIndent(2.0);
        assertEquals(2.0, p.getIndent(), 0);
        assertTrue(p.getXmlObject().getPPr().isSetIndent());
        p.setIndent(null);
        assertNull(p.getIndent());
        assertFalse(p.getXmlObject().getPPr().isSetIndent());
        p.setIndent(10.0);
        assertEquals(10., p.getIndent(), 0);
        assertTrue(p.getXmlObject().getPPr().isSetIndent());


        assertFalse(p.getXmlObject().getPPr().isSetLvl());
        p.setIndentLevel(1);
        assertEquals(1, p.getIndentLevel());
        assertTrue(p.getXmlObject().getPPr().isSetLvl());
        p.setIndentLevel(2);
        assertEquals(2, p.getIndentLevel());

        p.setLeftMargin(2.0);
        assertEquals(2.0, p.getLeftMargin(), 0);
        assertTrue(p.getXmlObject().getPPr().isSetMarL());
        p.setLeftMargin(10.0);
        assertEquals(10., p.getLeftMargin(), 0);
        assertEquals(Units.toEMU(10), p.getXmlObject().getPPr().getMarL());


        assertFalse(p.getXmlObject().getPPr().isSetSpcAft());
        p.setSpaceAfter(200d);
        assertEquals(200000, p.getXmlObject().getPPr().getSpcAft().getSpcPct().getVal());
        assertFalse(p.getXmlObject().getPPr().getSpcAft().isSetSpcPts());
        p.setSpaceAfter(100d);
        assertEquals(100000, p.getXmlObject().getPPr().getSpcAft().getSpcPct().getVal());
        assertFalse(p.getXmlObject().getPPr().getSpcAft().isSetSpcPts());
        p.setSpaceAfter(-20d);
        assertEquals(2000, p.getXmlObject().getPPr().getSpcAft().getSpcPts().getVal());
        assertFalse(p.getXmlObject().getPPr().getSpcAft().isSetSpcPct());
        p.setSpaceAfter(-10d);
        assertEquals(1000, p.getXmlObject().getPPr().getSpcAft().getSpcPts().getVal());
        assertFalse(p.getXmlObject().getPPr().getSpcAft().isSetSpcPct());

        assertFalse(p.getXmlObject().getPPr().isSetSpcBef());
        p.setSpaceBefore(200d);
        assertEquals(200000, p.getXmlObject().getPPr().getSpcBef().getSpcPct().getVal());
        assertFalse(p.getXmlObject().getPPr().getSpcBef().isSetSpcPts());
        p.setSpaceBefore(100d);
        assertEquals(100000, p.getXmlObject().getPPr().getSpcBef().getSpcPct().getVal());
        assertFalse(p.getXmlObject().getPPr().getSpcBef().isSetSpcPts());
        p.setSpaceBefore(-20d);
        assertEquals(2000, p.getXmlObject().getPPr().getSpcBef().getSpcPts().getVal());
        assertFalse(p.getXmlObject().getPPr().getSpcBef().isSetSpcPct());
        p.setSpaceBefore(-10d);
        assertEquals(1000, p.getXmlObject().getPPr().getSpcBef().getSpcPts().getVal());
        assertFalse(p.getXmlObject().getPPr().getSpcBef().isSetSpcPct());

        assertFalse(p.getXmlObject().getPPr().isSetLnSpc());
        p.setLineSpacing(200d);
        assertEquals(200000, p.getXmlObject().getPPr().getLnSpc().getSpcPct().getVal());
        assertFalse(p.getXmlObject().getPPr().getLnSpc().isSetSpcPts());
        p.setLineSpacing(100d);
        assertEquals(100000, p.getXmlObject().getPPr().getLnSpc().getSpcPct().getVal());
        assertFalse(p.getXmlObject().getPPr().getLnSpc().isSetSpcPts());
        p.setLineSpacing(-20d);
        assertEquals(2000, p.getXmlObject().getPPr().getLnSpc().getSpcPts().getVal());
        assertFalse(p.getXmlObject().getPPr().getLnSpc().isSetSpcPct());
        p.setLineSpacing(-10d);
        assertEquals(1000, p.getXmlObject().getPPr().getLnSpc().getSpcPts().getVal());
        assertFalse(p.getXmlObject().getPPr().getLnSpc().isSetSpcPct());

        // align is set in autoshape prototype
        assertTrue(p.getXmlObject().getPPr().isSetAlgn());
        assertEquals(TextAlign.LEFT, p.getTextAlign());
        p.setTextAlign(TextAlign.LEFT);
        assertTrue(p.getXmlObject().getPPr().isSetAlgn());
        assertEquals(TextAlign.LEFT, p.getTextAlign());
        p.setTextAlign(TextAlign.RIGHT);
        assertEquals(TextAlign.RIGHT, p.getTextAlign());
        p.setTextAlign(TextAlign.JUSTIFY);
        assertEquals(TextAlign.JUSTIFY, p.getTextAlign());
        p.setTextAlign(null);
        assertEquals(TextAlign.LEFT, p.getTextAlign());
        assertFalse(p.getXmlObject().getPPr().isSetAlgn());
        
        ppt.close();
    }

    @Test
    public void testTextRun() throws IOException {
        XMLSlideShow ppt = new XMLSlideShow();
        XSLFSlide slide = ppt.createSlide();

        XSLFAutoShape shape = slide.createAutoShape();
        assertEquals(0, shape.getTextParagraphs().size());
        XSLFTextParagraph p = shape.addNewTextParagraph();
        assertEquals(1, shape.getTextParagraphs().size());
        assertEquals(0, p.getTextRuns().size());
        XSLFTextRun r = p.addNewTextRun();
        CTTextCharacterProperties rPr = r.getRPr(false);
        assertNotNull(rPr);
        assertEquals(1, p.getTextRuns().size());
        assertSame(r, p.getTextRuns().get(0));

        
        assertEquals(18.0, r.getFontSize(), 0); // default font size for text boxes
        assertFalse(rPr.isSetSz());
        r.setFontSize(10.0);
        assertEquals(1000, rPr.getSz());
        r.setFontSize(12.5);
        assertEquals(1250, rPr.getSz());
        r.setFontSize(null);
        assertFalse(rPr.isSetSz());

        assertFalse(rPr.isSetLatin());
        assertEquals("Calibri", r.getFontFamily()); // comes from the slide master
        r.setFontFamily(null);
        assertEquals("Calibri", r.getFontFamily()); // comes from the slide master
        r.setFontFamily("Arial");
        assertEquals("Arial", r.getFontFamily());
        assertEquals("Arial", rPr.getLatin().getTypeface());
        r.setFontFamily("Symbol");
        assertEquals("Symbol", r.getFontFamily());
        assertEquals("Symbol", rPr.getLatin().getTypeface());
        r.setFontFamily(null);
        assertEquals("Calibri", r.getFontFamily()); // comes from the slide master
        assertFalse(rPr.isSetLatin());

        assertFalse(r.isStrikethrough());
        assertFalse(rPr.isSetStrike());
        r.setStrikethrough(true);
        assertTrue(r.isStrikethrough());
        assertEquals(STTextStrikeType.SNG_STRIKE, rPr.getStrike());

        assertFalse(r.isBold());
        assertFalse(rPr.isSetB());
        r.setBold(true);
        assertTrue(r.isBold());
        assertEquals(true, rPr.getB());

        assertFalse(r.isItalic());
        assertFalse(rPr.isSetI());
        r.setItalic(true);
        assertTrue(r.isItalic());
        assertEquals(true, rPr.getI());

        assertFalse(r.isUnderlined());
        assertFalse(rPr.isSetU());
        r.setUnderlined(true);
        assertTrue(r.isUnderlined());
        assertEquals(STTextUnderlineType.SNG, rPr.getU());

        r.setText("Apache");
        assertEquals("Apache", r.getRawText());
        r.setText("POI");
        assertEquals("POI", r.getRawText());
        r.setText(null);
        assertNull(r.getRawText());
        
        ppt.close();
    }

    @Test
    public void testShapeType() throws IOException {
        XMLSlideShow ppt = new XMLSlideShow();
        XSLFSlide slide = ppt.createSlide();

        XSLFAutoShape shape = slide.createAutoShape();
        assertEquals(ShapeType.RECT, shape.getShapeType());

        shape.setShapeType(ShapeType.TRIANGLE);
        assertEquals(ShapeType.TRIANGLE, shape.getShapeType());

        for(ShapeType tp : ShapeType.values()) {
            if (tp.ooxmlId == -1 || tp == ShapeType.SEAL) continue;
            shape.setShapeType(tp);
            assertEquals(tp, shape.getShapeType());
        }
        
        ppt.close();
    }
}