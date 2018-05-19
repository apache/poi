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


import static org.apache.poi.xslf.usermodel.TestXSLFSimpleShape.getSpPr;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import org.junit.Test;
import org.mockito.AdditionalAnswers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class TestXSLFFreeformShape {

    @Test
    public void testSetPath() throws IOException {
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
    public void testZeroWidth() throws IOException {
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
                Graphics2D graphicsMock = Mockito.mock(Graphics2D.class, AdditionalAnswers.delegatesTo(graphics));
                slide.draw(graphicsMock);

                ArgumentCaptor<Path2D.Double> captor = ArgumentCaptor.forClass(Path2D.Double.class);
                verify(graphicsMock, times(1)).draw(captor.capture());

                Path2D.Double actual = captor.getValue();
                PathIterator pi = actual.getPathIterator(new AffineTransform());
                comparePoint(pi, PathIterator.SEG_MOVETO, 100, 150);
                pi.next();
                comparePoint(pi, PathIterator.SEG_LINETO, 100, 300);
            } finally {
                graphics.dispose();
            }
        }
    }

    private void comparePoint(PathIterator pi, int type, double x0, double y0) {
        double points[] = new double[6];
        int piType = pi.currentSegment(points);
        assertEquals(type, piType);
        assertEquals(x0, points[0], 0);
        assertEquals(y0, points[1], 0);
    }
}