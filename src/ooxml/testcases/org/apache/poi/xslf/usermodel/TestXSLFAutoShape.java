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

import org.apache.poi.util.Units;
import org.openxmlformats.schemas.drawingml.x2006.main.STTextUnderlineType;
import org.openxmlformats.schemas.drawingml.x2006.main.STTextStrikeType;

/**
 * @author Yegor Kozlov
 */
public class TestXSLFAutoShape extends TestCase {
    public void testTextBodyProperies() {
        XMLSlideShow ppt = new XMLSlideShow();
        XSLFSlide slide = ppt.createSlide();

        XSLFAutoShape shape = slide.createAutoShape();
        shape.addNewTextParagraph().addNewTextRun().setText("POI");

        // margins
        assertEquals(-1., shape.getMarginBottom());
        assertEquals(-1., shape.getMarginTop());
        assertEquals(-1., shape.getMarginLeft());
        assertEquals(-1., shape.getMarginRight());

        shape.setMarginBottom(1.0);
        assertEquals(1.0, shape.getMarginBottom());
        shape.setMarginTop(2.0);
        assertEquals(2.0, shape.getMarginTop());
        shape.setMarginLeft(3.0);
        assertEquals(3.0, shape.getMarginLeft());
        shape.setMarginRight(4.0);
        assertEquals(4.0, shape.getMarginRight());

        shape.setMarginBottom(0.0);
        assertEquals(0.0, shape.getMarginBottom());
        shape.setMarginTop(0.0);
        assertEquals(0.0, shape.getMarginTop());
        shape.setMarginLeft(0.0);
        assertEquals(0.0, shape.getMarginLeft());
        shape.setMarginRight(0.0);
        assertEquals(0.0, shape.getMarginRight());

        shape.setMarginBottom(-1);
        assertEquals(-1., shape.getMarginBottom());
        shape.setMarginTop(-1);
        assertEquals(-1.0, shape.getMarginTop());
        shape.setMarginLeft(-1);
        assertEquals(-1.0, shape.getMarginLeft());
        shape.setMarginRight(-1);
        assertEquals(-1.0, shape.getMarginRight());

        // shape
        assertFalse(shape.getWordWrap());
        shape.setWordWrap(true);
        assertTrue(shape.getWordWrap());
        shape.setWordWrap(false);
        assertFalse(shape.getWordWrap());

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
    }

    public void testTextParagraph() {
        XMLSlideShow ppt = new XMLSlideShow();
        XSLFSlide slide = ppt.createSlide();
        assertEquals(0, slide.getShapes().length);

        XSLFAutoShape shape = slide.createAutoShape();
        assertEquals(0, shape.getTextParagraphs().size());
        XSLFTextParagraph p = shape.addNewTextParagraph();
        assertEquals(1, shape.getTextParagraphs().size());

        assertEquals(0., p.getIndent());
        assertEquals(0., p.getLeftMargin());
        assertEquals(100., p.getLineSpacing());
        assertEquals(0., p.getSpaceAfter());
        assertEquals(0., p.getSpaceBefore());
        assertEquals(0, p.getLevel());

        p.setIndent(2.0);
        assertEquals(2.0, p.getIndent());
        assertTrue(p.getXmlObject().getPPr().isSetIndent());
        p.setIndent(-1);
        assertEquals(0.0, p.getIndent());
        assertFalse(p.getXmlObject().getPPr().isSetIndent());
        p.setIndent(10.0);
        assertEquals(10., p.getIndent());
        assertTrue(p.getXmlObject().getPPr().isSetIndent());


        assertFalse(p.getXmlObject().getPPr().isSetLvl());
        p.setLevel(1);
        assertEquals(1, p.getLevel());
        assertTrue(p.getXmlObject().getPPr().isSetLvl());
        p.setLevel(2);
        assertEquals(2, p.getLevel());

        p.setLeftMargin(2.0);
        assertEquals(2.0, p.getLeftMargin());
        assertTrue(p.getXmlObject().getPPr().isSetMarL());
        p.setLeftMargin(10.0);
        assertEquals(10., p.getLeftMargin());
        assertEquals(Units.toEMU(10), p.getXmlObject().getPPr().getMarL());


        assertFalse(p.getXmlObject().getPPr().isSetSpcAft());
        p.setSpaceAfter(200);
        assertEquals(200000, p.getXmlObject().getPPr().getSpcAft().getSpcPct().getVal());
        assertFalse(p.getXmlObject().getPPr().getSpcAft().isSetSpcPts());
        p.setSpaceAfter(100);
        assertEquals(100000, p.getXmlObject().getPPr().getSpcAft().getSpcPct().getVal());
        assertFalse(p.getXmlObject().getPPr().getSpcAft().isSetSpcPts());
        p.setSpaceAfter(-20);
        assertEquals(2000, p.getXmlObject().getPPr().getSpcAft().getSpcPts().getVal());
        assertFalse(p.getXmlObject().getPPr().getSpcAft().isSetSpcPct());
        p.setSpaceAfter(-10);
        assertEquals(1000, p.getXmlObject().getPPr().getSpcAft().getSpcPts().getVal());
        assertFalse(p.getXmlObject().getPPr().getSpcAft().isSetSpcPct());

        assertFalse(p.getXmlObject().getPPr().isSetSpcBef());
        p.setSpaceBefore(200);
        assertEquals(200000, p.getXmlObject().getPPr().getSpcBef().getSpcPct().getVal());
        assertFalse(p.getXmlObject().getPPr().getSpcBef().isSetSpcPts());
        p.setSpaceBefore(100);
        assertEquals(100000, p.getXmlObject().getPPr().getSpcBef().getSpcPct().getVal());
        assertFalse(p.getXmlObject().getPPr().getSpcBef().isSetSpcPts());
        p.setSpaceBefore(-20);
        assertEquals(2000, p.getXmlObject().getPPr().getSpcBef().getSpcPts().getVal());
        assertFalse(p.getXmlObject().getPPr().getSpcBef().isSetSpcPct());
        p.setSpaceBefore(-10);
        assertEquals(1000, p.getXmlObject().getPPr().getSpcBef().getSpcPts().getVal());
        assertFalse(p.getXmlObject().getPPr().getSpcBef().isSetSpcPct());

        assertFalse(p.getXmlObject().getPPr().isSetLnSpc());
        p.setLineSpacing(200);
        assertEquals(200000, p.getXmlObject().getPPr().getLnSpc().getSpcPct().getVal());
        assertFalse(p.getXmlObject().getPPr().getLnSpc().isSetSpcPts());
        p.setLineSpacing(100);
        assertEquals(100000, p.getXmlObject().getPPr().getLnSpc().getSpcPct().getVal());
        assertFalse(p.getXmlObject().getPPr().getLnSpc().isSetSpcPts());
        p.setLineSpacing(-20);
        assertEquals(2000, p.getXmlObject().getPPr().getLnSpc().getSpcPts().getVal());
        assertFalse(p.getXmlObject().getPPr().getLnSpc().isSetSpcPct());
        p.setLineSpacing(-10);
        assertEquals(1000, p.getXmlObject().getPPr().getLnSpc().getSpcPts().getVal());
        assertFalse(p.getXmlObject().getPPr().getLnSpc().isSetSpcPct());

        assertFalse(p.getXmlObject().getPPr().isSetAlgn());
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
    }

    public void testTextRun() {
        XMLSlideShow ppt = new XMLSlideShow();
        XSLFSlide slide = ppt.createSlide();

        XSLFAutoShape shape = slide.createAutoShape();
        assertEquals(0, shape.getTextParagraphs().size());
        XSLFTextParagraph p = shape.addNewTextParagraph();
        assertEquals(1, shape.getTextParagraphs().size());
        assertEquals(0, p.getTextRuns().size());
        XSLFTextRun r = p.addNewTextRun();
        assertEquals(1, p.getTextRuns().size());
        assertSame(r, p.getTextRuns().get(0));

        assertEquals(-1.0, r.getFontSize());
        assertFalse(r.getXmlObject().getRPr().isSetSz());
        r.setFontSize(10.0);
        assertTrue(r.getXmlObject().isSetRPr());
        assertEquals(1000, r.getXmlObject().getRPr().getSz());
        r.setFontSize(12.5);
        assertEquals(1250, r.getXmlObject().getRPr().getSz());
        r.setFontSize(-1);
        assertFalse(r.getXmlObject().getRPr().isSetSz());

        assertFalse(r.getXmlObject().getRPr().isSetLatin());
        assertNull(r.getFontFamily());
        r.setFontFamily(null);
        assertNull(r.getFontFamily());
        r.setFontFamily("Arial");
        assertEquals("Arial", r.getFontFamily());
        assertEquals("Arial", r.getXmlObject().getRPr().getLatin().getTypeface());
        r.setFontFamily("Symbol");
        assertEquals("Symbol", r.getFontFamily());
        assertEquals("Symbol", r.getXmlObject().getRPr().getLatin().getTypeface());
        r.setFontFamily(null);
        assertNull(r.getFontFamily());
        assertFalse(r.getXmlObject().getRPr().isSetLatin());

        assertFalse(r.isStrikethrough());
        assertFalse(r.getXmlObject().getRPr().isSetStrike());
        r.setStrikethrough(true);
        assertTrue(r.isStrikethrough());
        assertEquals(STTextStrikeType.SNG_STRIKE, r.getXmlObject().getRPr().getStrike());

        assertFalse(r.isBold());
        assertFalse(r.getXmlObject().getRPr().isSetB());
        r.setBold(true);
        assertTrue(r.isBold());
        assertEquals(true, r.getXmlObject().getRPr().getB());

        assertFalse(r.isItalic());
        assertFalse(r.getXmlObject().getRPr().isSetI());
        r.setItalic(true);
        assertTrue(r.isItalic());
        assertEquals(true, r.getXmlObject().getRPr().getI());

        assertFalse(r.isUnderline());
        assertFalse(r.getXmlObject().getRPr().isSetU());
        r.setUnderline(true);
        assertTrue(r.isUnderline());
        assertEquals(STTextUnderlineType.SNG, r.getXmlObject().getRPr().getU());

        r.setText("Apache");
        assertEquals("Apache", r.getText());
        r.setText("POI");
        assertEquals("POI", r.getText());
        r.setText(null);
        assertNull(r.getText());
    }
}