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


import org.apache.poi.sl.draw.SLGraphics;
import org.junit.jupiter.api.Test;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPath2D;
import org.openxmlformats.schemas.presentationml.x2006.main.CTGroupShape;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import static org.apache.poi.xslf.usermodel.TestXSLFSimpleShape.getSpPr;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestXSLFFreeformShape {

    @Test
    void testSetPath() throws IOException {
        XMLSlideShow ppt = new XMLSlideShow();
        XSLFSlide slide = ppt.createSlide();
        XSLFFreeformShape shape1 = slide.createFreeform();
        // comples path consisting of a rectangle and an ellipse inside it
        Path2D.Double path1 = new Path2D.Double(new Rectangle2D.Double(150, 150, 300, 300));
        path1.append(new Ellipse2D.Double(200, 200, 100, 50), false);
        shape1.setPath(path1);

        Path2D.Double path2 = shape1.getPath();

        // YK: how to compare the original path1 and the value returned by XSLFFreeformShape.getPath() ?
        // one way is to create another XSLFFreeformShape from path2 and compare the resulting xml
        assertEquals(path1.getBounds2D(), path2.getBounds2D());

        XSLFFreeformShape shape2 = slide.createFreeform();
        shape2.setPath(path2);

        assertEquals(getSpPr(shape1).getCustGeom().toString(), getSpPr(shape2).getCustGeom().toString());

        ppt.close();
    }

    @Test
    void testZeroWidth() throws IOException {
        // see #61633
        try (XMLSlideShow ppt = new XMLSlideShow()) {
            XSLFSlide slide = ppt.createSlide();
            XSLFFreeformShape shape1 = slide.createFreeform();
            Path2D.Double path1 = new Path2D.Double(new Line2D.Double(100, 150, 100, 300));
            shape1.setPath(path1);
            shape1.setLineColor(Color.BLUE);
            shape1.setLineWidth(1);

            BufferedImage img = new BufferedImage(300, 300, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = img.createGraphics();
            try {
                // previously we used Mockito here, but since JDK 11 mocking the Graphics2D does
                // not work any longer
                Graphics2D graphicsMock = new SLGraphics(new XSLFGroupShape(CTGroupShape.Factory.newInstance(), slide)) {
                    boolean called;

                    @Override
                    public void draw(Shape shape) {
                        if(called) {
                            throw new IllegalStateException("Should only be called once, but was called a second time");
                        }
                        called = true;

                        if(!(shape instanceof Path2D.Double)) {
                            throw new IllegalStateException("Expecting a shape of type Path2D.Double, but had " + shape.getClass());
                        }

                        Path2D.Double actual = (Path2D.Double) shape;
                        PathIterator pi = actual.getPathIterator(new AffineTransform());
                        comparePoint(pi, PathIterator.SEG_MOVETO, 100, 150);
                        pi.next();
                        comparePoint(pi, PathIterator.SEG_LINETO, 100, 300);

                        super.draw(shape);
                    }
                };
                slide.draw(graphicsMock);
            } finally {
                graphics.dispose();
            }
        }
    }

    /**
     * Bug 69522: the quadBezTo element was missing from poi-ooxml-lite, so a path with a
     * quadratic curve failed with a ClassCastException in poi-ooxml-lite based setups
     */
    @Test
    void testQuadraticCurveBug69522() throws IOException {
        Path2D.Double path = new Path2D.Double();
        path.moveTo(10, 20);
        path.lineTo(110, 20);
        path.quadTo(160, 70, 110, 120);
        path.curveTo(80, 150, 40, 150, 10, 120);
        path.closePath();

        try (XMLSlideShow ppt = new XMLSlideShow()) {
            XSLFSlide slide = ppt.createSlide();
            XSLFFreeformShape shape = slide.createFreeform();
            // 1 + 1 + 2 + 3 points, plus 1 for the close
            assertEquals(8, shape.setPath(path));

            CTPath2D ctPath = getSpPr(shape).getCustGeom().getPathLst().getPathArray(0);
            assertEquals(1, ctPath.sizeOfQuadBezToArray());
            assertEquals(1, ctPath.sizeOfCubicBezToArray());

            PathIterator pi = shape.getPath().getPathIterator(new AffineTransform());
            comparePoint(pi, PathIterator.SEG_MOVETO, 10, 20);
            pi.next();
            comparePoint(pi, PathIterator.SEG_LINETO, 110, 20);
            pi.next();
            comparePoint(pi, PathIterator.SEG_QUADTO, 160, 70, 110, 120);
            pi.next();
            comparePoint(pi, PathIterator.SEG_CUBICTO, 80, 150, 40, 150, 10, 120);
            pi.next();
            comparePoint(pi, PathIterator.SEG_CLOSE);
            pi.next();
            assertTrue(pi.isDone());

            // the reporter's route: a Graphics2D fill on a group shape
            XSLFGroupShape group = slide.createGroup();
            group.setAnchor(new Rectangle2D.Double(0, 0, 200, 200));
            group.setInteriorAnchor(new Rectangle2D.Double(0, 0, 200, 200));
            Graphics2D graphics = new SLGraphics(group);
            graphics.fill(path);
            graphics.dispose();
            assertEquals(1, group.getShapes().size());
            assertInstanceOf(XSLFFreeformShape.class, group.getShapes().get(0));
        }
    }

    private void comparePoint(PathIterator pi, int type, double... coords) {
        double[] points = new double[6];
        int piType = pi.currentSegment(points);
        assertEquals(type, piType);
        for (int i = 0; i < coords.length; i++) {
            assertEquals(coords[i], points[i], 0);
        }
    }

}