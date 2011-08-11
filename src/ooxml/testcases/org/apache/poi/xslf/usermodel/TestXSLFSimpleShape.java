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
import org.apache.poi.xslf.usermodel.LineCap;
import org.apache.poi.xslf.usermodel.LineDash;
import org.openxmlformats.schemas.drawingml.x2006.main.STLineCap;
import org.openxmlformats.schemas.drawingml.x2006.main.STPresetLineDashVal;

import java.awt.*;

/**
 * @author Yegor Kozlov
 */
public class TestXSLFSimpleShape extends TestCase {
    public void testLineStyles() {
        XMLSlideShow ppt = new XMLSlideShow();
        XSLFSlide slide = ppt.createSlide();

        XSLFSimpleShape shape = slide.createAutoShape();
        assertEquals(1, slide.getShapes().length);
        // line properties are not set by default
        assertFalse(shape.getSpPr().isSetLn());

        assertEquals(0., shape.getLineWidth());
        assertEquals(null, shape.getLineColor());
        assertEquals(null, shape.getLineDash());
        assertEquals(null, shape.getLineCap());

        shape.setLineWidth(0);
        shape.setLineColor(null);
        shape.setLineDash(null);
        shape.setLineCap(null);

        // still no line properties
        assertFalse(shape.getSpPr().isSetLn());

        // line width
        shape.setLineWidth(1.0);
        assertEquals(1.0, shape.getLineWidth());
        assertEquals(Units.EMU_PER_POINT, shape.getSpPr().getLn().getW());
        shape.setLineWidth(5.5);
        assertEquals(5.5, shape.getLineWidth());
        assertEquals(Units.toEMU(5.5), shape.getSpPr().getLn().getW());
        shape.setLineWidth(0.0);
        // setting line width to zero unsets the W attribute
        assertFalse(shape.getSpPr().getLn().isSetW());

        // line cap
        shape.setLineCap(LineCap.FLAT);
        assertEquals(LineCap.FLAT, shape.getLineCap());
        assertEquals(STLineCap.FLAT, shape.getSpPr().getLn().getCap());
        shape.setLineCap(LineCap.SQUARE);
        assertEquals(LineCap.SQUARE, shape.getLineCap());
        assertEquals(STLineCap.SQ, shape.getSpPr().getLn().getCap());
        shape.setLineCap(LineCap.ROUND);
        assertEquals(LineCap.ROUND, shape.getLineCap());
        assertEquals(STLineCap.RND, shape.getSpPr().getLn().getCap());
        shape.setLineCap(null);
        // setting cap to null unsets the Cap attribute
        assertFalse(shape.getSpPr().getLn().isSetCap());

        // line dash
        shape.setLineDash(LineDash.SOLID);
        assertEquals(LineDash.SOLID, shape.getLineDash());
        assertEquals(STPresetLineDashVal.SOLID, shape.getSpPr().getLn().getPrstDash().getVal());
        shape.setLineDash(LineDash.DASH_DOT);
        assertEquals(LineDash.DASH_DOT, shape.getLineDash());
        assertEquals(STPresetLineDashVal.DASH_DOT, shape.getSpPr().getLn().getPrstDash().getVal());
        shape.setLineDash(LineDash.LG_DASH_DOT);
        assertEquals(LineDash.LG_DASH_DOT, shape.getLineDash());
        assertEquals(STPresetLineDashVal.LG_DASH_DOT, shape.getSpPr().getLn().getPrstDash().getVal());
        shape.setLineDash(null);
        // setting dash width to null unsets the Dash element
        assertFalse(shape.getSpPr().getLn().isSetPrstDash());

        // line color
        assertFalse(shape.getSpPr().getLn().isSetSolidFill());
        shape.setLineColor(Color.RED);
        assertEquals(Color.RED, shape.getLineColor());
        assertTrue(shape.getSpPr().getLn().isSetSolidFill());
        shape.setLineColor(Color.BLUE);
        assertEquals(Color.BLUE, shape.getLineColor());
        assertTrue(shape.getSpPr().getLn().isSetSolidFill());
        shape.setLineColor(null);
        assertEquals(null, shape.getLineColor());
        // setting dash width to null unsets the SolidFill element
        assertFalse(shape.getSpPr().getLn().isSetSolidFill());
    }

    public void testFill() {
        XMLSlideShow ppt = new XMLSlideShow();
        XSLFSlide slide = ppt.createSlide();

        XSLFAutoShape shape = slide.createAutoShape();
        // line properties are not set by default
        assertFalse(shape.getSpPr().isSetSolidFill());

        assertNull(shape.getFillColor());
        shape.setFillColor(null);
        assertNull(shape.getFillColor());
        assertFalse(shape.getSpPr().isSetSolidFill());

        shape.setFillColor(Color.RED);
        assertEquals(Color.RED, shape.getFillColor());
        shape.setFillColor(Color.DARK_GRAY);
        assertEquals(Color.DARK_GRAY, shape.getFillColor());
        assertTrue(shape.getSpPr().isSetSolidFill());

        shape.setFillColor(null);
        assertNull(shape.getFillColor());
        assertFalse(shape.getSpPr().isSetSolidFill());
    }

}