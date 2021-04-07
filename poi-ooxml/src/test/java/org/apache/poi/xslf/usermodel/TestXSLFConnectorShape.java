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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.apache.poi.xslf.usermodel.TestXSLFSimpleShape.getSpPr;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.List;

import org.apache.poi.sl.usermodel.LineDecoration.DecorationShape;
import org.apache.poi.sl.usermodel.LineDecoration.DecorationSize;
import org.apache.poi.sl.usermodel.ShapeType;
import org.apache.poi.xslf.XSLFTestDataSamples;
import org.junit.jupiter.api.Test;
import org.openxmlformats.schemas.drawingml.x2006.main.CTConnection;
import org.openxmlformats.schemas.drawingml.x2006.main.CTNonVisualConnectorProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.STLineEndLength;
import org.openxmlformats.schemas.drawingml.x2006.main.STLineEndType;
import org.openxmlformats.schemas.drawingml.x2006.main.STLineEndWidth;
import org.openxmlformats.schemas.drawingml.x2006.main.STShapeType;
import org.openxmlformats.schemas.presentationml.x2006.main.CTConnector;

class TestXSLFConnectorShape {

    @Test
    void testLineDecorations() throws IOException {
        try (XMLSlideShow ppt = new XMLSlideShow()) {
            XSLFSlide slide = ppt.createSlide();

            XSLFConnectorShape shape = slide.createConnector();
            assertEquals(1, slide.getShapes().size());

            assertFalse(getSpPr(shape).getLn().isSetHeadEnd());
            assertFalse(getSpPr(shape).getLn().isSetTailEnd());

            // line decorations
            assertEquals(DecorationShape.NONE, shape.getLineHeadDecoration());
            assertEquals(DecorationShape.NONE, shape.getLineTailDecoration());
            shape.setLineHeadDecoration(null);
            shape.setLineTailDecoration(null);
            assertEquals(DecorationShape.NONE, shape.getLineHeadDecoration());
            assertEquals(DecorationShape.NONE, shape.getLineTailDecoration());
            assertFalse(getSpPr(shape).getLn().getHeadEnd().isSetType());
            assertFalse(getSpPr(shape).getLn().getTailEnd().isSetType());

            shape.setLineHeadDecoration(DecorationShape.ARROW);
            shape.setLineTailDecoration(DecorationShape.DIAMOND);
            assertEquals(DecorationShape.ARROW, shape.getLineHeadDecoration());
            assertEquals(DecorationShape.DIAMOND, shape.getLineTailDecoration());
            assertEquals(STLineEndType.ARROW, getSpPr(shape).getLn().getHeadEnd().getType());
            assertEquals(STLineEndType.DIAMOND, getSpPr(shape).getLn().getTailEnd().getType());

            shape.setLineHeadDecoration(DecorationShape.DIAMOND);
            shape.setLineTailDecoration(DecorationShape.ARROW);
            assertEquals(DecorationShape.DIAMOND, shape.getLineHeadDecoration());
            assertEquals(DecorationShape.ARROW, shape.getLineTailDecoration());
            assertEquals(STLineEndType.DIAMOND, getSpPr(shape).getLn().getHeadEnd().getType());
            assertEquals(STLineEndType.ARROW, getSpPr(shape).getLn().getTailEnd().getType());

            // line end width
            assertEquals(DecorationSize.MEDIUM, shape.getLineHeadWidth());
            assertEquals(DecorationSize.MEDIUM, shape.getLineTailWidth());
            shape.setLineHeadWidth(null);
            shape.setLineHeadWidth(null);
            assertEquals(DecorationSize.MEDIUM, shape.getLineHeadWidth());
            assertEquals(DecorationSize.MEDIUM, shape.getLineTailWidth());
            assertFalse(getSpPr(shape).getLn().getHeadEnd().isSetW());
            assertFalse(getSpPr(shape).getLn().getTailEnd().isSetW());
            shape.setLineHeadWidth(DecorationSize.LARGE);
            shape.setLineTailWidth(DecorationSize.MEDIUM);
            assertEquals(DecorationSize.LARGE, shape.getLineHeadWidth());
            assertEquals(DecorationSize.MEDIUM, shape.getLineTailWidth());
            assertEquals(STLineEndWidth.LG, getSpPr(shape).getLn().getHeadEnd().getW());
            assertEquals(STLineEndWidth.MED, getSpPr(shape).getLn().getTailEnd().getW());
            shape.setLineHeadWidth(DecorationSize.MEDIUM);
            shape.setLineTailWidth(DecorationSize.LARGE);
            assertEquals(DecorationSize.MEDIUM, shape.getLineHeadWidth());
            assertEquals(DecorationSize.LARGE, shape.getLineTailWidth());
            assertEquals(STLineEndWidth.MED, getSpPr(shape).getLn().getHeadEnd().getW());
            assertEquals(STLineEndWidth.LG, getSpPr(shape).getLn().getTailEnd().getW());

            // line end length
            assertEquals(DecorationSize.MEDIUM, shape.getLineHeadLength());
            assertEquals(DecorationSize.MEDIUM, shape.getLineTailLength());
            shape.setLineHeadLength(null);
            shape.setLineTailLength(null);
            assertEquals(DecorationSize.MEDIUM, shape.getLineHeadLength());
            assertEquals(DecorationSize.MEDIUM, shape.getLineTailLength());
            assertFalse(getSpPr(shape).getLn().getHeadEnd().isSetLen());
            assertFalse(getSpPr(shape).getLn().getTailEnd().isSetLen());
            shape.setLineHeadLength(DecorationSize.LARGE);
            shape.setLineTailLength(DecorationSize.MEDIUM);
            assertEquals(DecorationSize.LARGE, shape.getLineHeadLength());
            assertEquals(DecorationSize.MEDIUM, shape.getLineTailLength());
            assertEquals(STLineEndLength.LG, getSpPr(shape).getLn().getHeadEnd().getLen());
            assertEquals(STLineEndLength.MED, getSpPr(shape).getLn().getTailEnd().getLen());
            shape.setLineHeadLength(DecorationSize.MEDIUM);
            shape.setLineTailLength(DecorationSize.LARGE);
            assertEquals(DecorationSize.MEDIUM, shape.getLineHeadLength());
            assertEquals(DecorationSize.LARGE, shape.getLineTailLength());
            assertEquals(STLineEndLength.MED, getSpPr(shape).getLn().getHeadEnd().getLen());
            assertEquals(STLineEndLength.LG, getSpPr(shape).getLn().getTailEnd().getLen());
        }
    }

    @Test
    void testAddConnector() throws IOException {
        try (XMLSlideShow pptx = new XMLSlideShow()) {
            XSLFSlide slide = pptx.createSlide();

            XSLFAutoShape rect1 = slide.createAutoShape();
            rect1.setShapeType(ShapeType.RECT);
            rect1.setAnchor(new Rectangle2D.Double(100, 100, 100, 100));
            rect1.setFillColor(Color.blue);

            XSLFAutoShape rect2 = slide.createAutoShape();
            rect2.setShapeType(ShapeType.RECT);
            rect2.setAnchor(new Rectangle2D.Double(300, 300, 100, 100));
            rect2.setFillColor(Color.red);


            XSLFConnectorShape connector1 = slide.createConnector();
            Rectangle2D r1 = new Rectangle2D.Double(200, 150, 100, 200);
            connector1.setAnchor(r1);

            CTConnector ctConnector = (CTConnector) connector1.getXmlObject();
            ctConnector.getSpPr().getPrstGeom().setPrst(STShapeType.BENT_CONNECTOR_3);
            CTNonVisualConnectorProperties cx = ctConnector.getNvCxnSpPr().getCNvCxnSpPr();
            // connection start
            CTConnection stCxn = cx.addNewStCxn();
            stCxn.setId(rect1.getShapeId());
            // side of the rectangle to attach the connector: left=1, bottom=2,right=3, top=4
            stCxn.setIdx(2);

            CTConnection end = cx.addNewEndCxn();
            end.setId(rect2.getShapeId());
            // side of the rectangle to attach the connector: left=1, bottom=2,right=3, top=4
            end.setIdx(3);

            try (XMLSlideShow ppt2 = XSLFTestDataSamples.writeOutAndReadBack(pptx)) {
                XSLFSlide s1 = ppt2.getSlides().get(0);
                List<XSLFShape> shapes = s1.getShapes();
                XSLFConnectorShape c1 = (XSLFConnectorShape)shapes.get(2);
                assertEquals(r1, c1.getAnchor());
            }
        }
    }
}