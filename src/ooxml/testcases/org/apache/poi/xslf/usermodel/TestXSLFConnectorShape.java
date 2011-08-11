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
import org.openxmlformats.schemas.drawingml.x2006.main.STLineEndType;
import org.openxmlformats.schemas.drawingml.x2006.main.STLineEndWidth;
import org.openxmlformats.schemas.drawingml.x2006.main.STLineEndLength;

import java.awt.*;

/**
 * @author Yegor Kozlov
 */
public class TestXSLFConnectorShape extends TestCase {

    public void testLineDecorations() {
        XMLSlideShow ppt = new XMLSlideShow();
        XSLFSlide slide = ppt.createSlide();

        XSLFConnectorShape shape = slide.createConnector();
        assertEquals(1, slide.getShapes().length);

        assertFalse(shape.getSpPr().getLn().isSetHeadEnd());
        assertFalse(shape.getSpPr().getLn().isSetTailEnd());

        // line decorations
        assertEquals(LineDecoration.NONE, shape.getLineHeadDecoration());
        assertEquals(LineDecoration.NONE, shape.getLineTailDecoration());
        shape.setLineHeadDecoration(null);
        shape.setLineTailDecoration(null);
        assertEquals(LineDecoration.NONE, shape.getLineHeadDecoration());
        assertEquals(LineDecoration.NONE, shape.getLineTailDecoration());
        assertFalse(shape.getSpPr().getLn().getHeadEnd().isSetType());
        assertFalse(shape.getSpPr().getLn().getTailEnd().isSetType());

        shape.setLineHeadDecoration(LineDecoration.ARROW);
        shape.setLineTailDecoration(LineDecoration.DIAMOND);
        assertEquals(LineDecoration.ARROW, shape.getLineHeadDecoration());
        assertEquals(LineDecoration.DIAMOND, shape.getLineTailDecoration());
        assertEquals(STLineEndType.ARROW, shape.getSpPr().getLn().getHeadEnd().getType());
        assertEquals(STLineEndType.DIAMOND, shape.getSpPr().getLn().getTailEnd().getType());

        shape.setLineHeadDecoration(LineDecoration.DIAMOND);
        shape.setLineTailDecoration(LineDecoration.ARROW);
        assertEquals(LineDecoration.DIAMOND, shape.getLineHeadDecoration());
        assertEquals(LineDecoration.ARROW, shape.getLineTailDecoration());
        assertEquals(STLineEndType.DIAMOND, shape.getSpPr().getLn().getHeadEnd().getType());
        assertEquals(STLineEndType.ARROW, shape.getSpPr().getLn().getTailEnd().getType());

        // line end width
        assertEquals(null, shape.getLineHeadWidth());
        assertEquals(null, shape.getLineTailWidth());
        shape.setLineHeadWidth(null);
        shape.setLineHeadWidth(null);
        assertEquals(null, shape.getLineHeadWidth());
        assertEquals(null, shape.getLineTailWidth());
        assertFalse(shape.getSpPr().getLn().getHeadEnd().isSetW());
        assertFalse(shape.getSpPr().getLn().getTailEnd().isSetW());
        shape.setLineHeadWidth(LineEndWidth.LARGE);
        shape.setLineTailWidth(LineEndWidth.MEDIUM);
        assertEquals(LineEndWidth.LARGE, shape.getLineHeadWidth());
        assertEquals(LineEndWidth.MEDIUM, shape.getLineTailWidth());
        assertEquals(STLineEndWidth.LG, shape.getSpPr().getLn().getHeadEnd().getW());
        assertEquals(STLineEndWidth.MED, shape.getSpPr().getLn().getTailEnd().getW());
        shape.setLineHeadWidth(LineEndWidth.MEDIUM);
        shape.setLineTailWidth(LineEndWidth.LARGE);
        assertEquals(LineEndWidth.MEDIUM, shape.getLineHeadWidth());
        assertEquals(LineEndWidth.LARGE, shape.getLineTailWidth());
        assertEquals(STLineEndWidth.MED, shape.getSpPr().getLn().getHeadEnd().getW());
        assertEquals(STLineEndWidth.LG, shape.getSpPr().getLn().getTailEnd().getW());

        // line end length
        assertEquals(null, shape.getLineHeadLength());
        assertEquals(null, shape.getLineTailLength());
        shape.setLineHeadLength(null);
        shape.setLineTailLength(null);
        assertEquals(null, shape.getLineHeadLength());
        assertEquals(null, shape.getLineTailLength());
        assertFalse(shape.getSpPr().getLn().getHeadEnd().isSetLen());
        assertFalse(shape.getSpPr().getLn().getTailEnd().isSetLen());
        shape.setLineHeadLength(LineEndLength.LARGE);
        shape.setLineTailLength(LineEndLength.MEDIUM);
        assertEquals(LineEndLength.LARGE, shape.getLineHeadLength());
        assertEquals(LineEndLength.MEDIUM, shape.getLineTailLength());
        assertEquals(STLineEndLength.LG, shape.getSpPr().getLn().getHeadEnd().getLen());
        assertEquals(STLineEndLength.MED, shape.getSpPr().getLn().getTailEnd().getLen());
        shape.setLineHeadLength(LineEndLength.MEDIUM);
        shape.setLineTailLength(LineEndLength.LARGE);
        assertEquals(LineEndLength.MEDIUM, shape.getLineHeadLength());
        assertEquals(LineEndLength.LARGE, shape.getLineTailLength());
        assertEquals(STLineEndLength.MED, shape.getSpPr().getLn().getHeadEnd().getLen());
        assertEquals(STLineEndLength.LG, shape.getSpPr().getLn().getTailEnd().getLen());

    }

}