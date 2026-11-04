/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */
package org.apache.poi.sl.tests.draw;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import org.apache.poi.sl.draw.DrawShape;
import org.apache.poi.sl.tests.SLCommonUtils;
import org.apache.poi.sl.usermodel.AutoShape;
import org.apache.poi.sl.usermodel.SlideShow;
import org.apache.poi.sl.usermodel.SlideShowFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TestDrawShape {

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testApplyTransformFlip(boolean xslf) throws IOException {
        assumeFalse(!xslf && SLCommonUtils.xslfOnly());

        Rectangle2D anchor = new Rectangle2D.Double(100, 50, 200, 100);
        Point2D topLeft = new Point2D.Double(100, 50);

        try (SlideShow<?,?> ppt = SlideShowFactory.create(xslf)) {
            AutoShape<?,?> shape = ppt.createSlide().createAutoShape();
            shape.setAnchor(anchor);

            // no flip, no rotation: the transform must be left untouched
            assertEquals(topLeft, transform(shape, topLeft));

            shape.setFlipHorizontal(true);
            assertEquals(new Point2D.Double(300, 50), transform(shape, topLeft));

            shape.setFlipHorizontal(false);
            shape.setFlipVertical(true);
            assertEquals(new Point2D.Double(100, 150), transform(shape, topLeft));

            shape.setFlipHorizontal(true);
            assertEquals(new Point2D.Double(300, 150), transform(shape, topLeft));
        }
    }

    private static Point2D transform(AutoShape<?,?> shape, Point2D point) {
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = img.createGraphics();
        try {
            new DrawShape(shape).applyTransform(graphics);
            AffineTransform tx = graphics.getTransform();
            return tx.transform(point, null);
        } finally {
            graphics.dispose();
        }
    }
}
