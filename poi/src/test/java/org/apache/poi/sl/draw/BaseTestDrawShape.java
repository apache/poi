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

package org.apache.poi.sl.draw;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import org.apache.poi.sl.usermodel.AutoShape;
import org.apache.poi.sl.usermodel.GroupShape;
import org.apache.poi.sl.usermodel.ShapeType;
import org.apache.poi.sl.usermodel.Slide;
import org.apache.poi.sl.usermodel.SlideShow;
import org.junit.jupiter.api.Test;

/**
 * Tests for the flipping and rotation applied by {@link DrawShape#applyTransform(Graphics2D)},
 * shared by the XSLF and HSLF implementations.
 */
public abstract class BaseTestDrawShape {

    private static final Point2D TOP_LEFT = new Point2D.Double(100, 50);

    protected abstract SlideShow<?,?> createSlideShow();

    /**
     * @return {@code true} if the format flips the shape before rotating it (HSLF),
     *    {@code false} if it rotates the shape before flipping it (XSLF)
     */
    protected abstract boolean isFlippedBeforeRotation();

    @Test
    void testApplyTransformFlip() throws IOException {
        try (SlideShow<?,?> ppt = createSlideShow()) {
            AutoShape<?,?> shape = ppt.createSlide().createAutoShape();
            shape.setAnchor(new Rectangle2D.Double(100, 50, 200, 100));

            // no flip, no rotation: the transform must be left untouched
            assertEquals(TOP_LEFT, transform(shape, TOP_LEFT));

            shape.setFlipHorizontal(true);
            assertEquals(new Point2D.Double(300, 50), transform(shape, TOP_LEFT));

            shape.setFlipHorizontal(false);
            shape.setFlipVertical(true);
            assertEquals(new Point2D.Double(100, 150), transform(shape, TOP_LEFT));

            shape.setFlipHorizontal(true);
            assertEquals(new Point2D.Double(300, 150), transform(shape, TOP_LEFT));

            shape.setFlipVertical(false);
            assertEquals(new Point2D.Double(300, 50), transform(shape, TOP_LEFT));

            shape.setFlipHorizontal(false);
            assertEquals(TOP_LEFT, transform(shape, TOP_LEFT));
        }
    }

    @Test
    void testApplyTransformRotatedFlip() throws IOException {
        try (SlideShow<?,?> ppt = createSlideShow()) {
            AutoShape<?,?> shape = ppt.createSlide().createAutoShape();
            // a square anchor isn't normalized for a rotation of 90 degrees, see DrawShape.getAnchor()
            shape.setAnchor(new Rectangle2D.Double(100, 50, 100, 100));
            shape.setRotation(90);

            // rotation only: clockwise around the center (150, 100)
            assertEquals(new Point2D.Double(200, 50), transform(shape, TOP_LEFT));

            // flipping and rotating don't commute for 90 degrees, so the order matters
            shape.setFlipHorizontal(true);
            Point2D expected = isFlippedBeforeRotation()
                ? new Point2D.Double(100, 50)    // flip (200, 50), then rotate
                : new Point2D.Double(200, 150);  // rotate (200, 50), then flip
            assertEquals(expected, transform(shape, TOP_LEFT));
        }
    }

    @Test
    void testDrawFlippedGroup() throws IOException {
        try (SlideShow<?,?> ppt = createSlideShow()) {
            Slide<?,?> slide = ppt.createSlide();
            GroupShape<?,?> group = slide.createGroup();
            Rectangle2D groupAnchor = new Rectangle2D.Double(100, 50, 200, 100);
            group.setAnchor(groupAnchor);
            group.setInteriorAnchor(groupAnchor);

            // the child fills the left half of the group
            AutoShape<?,?> child = group.createAutoShape();
            child.setShapeType(ShapeType.RECT);
            child.setAnchor(new Rectangle2D.Double(100, 50, 100, 100));
            child.setFillColor(Color.RED);

            Point2D leftHalf = new Point2D.Double(150, 100);
            Point2D rightHalf = new Point2D.Double(250, 100);

            BufferedImage img = draw(slide);
            assertEquals(Color.RED, getColor(img, leftHalf));
            assertEquals(Color.WHITE, getColor(img, rightHalf));

            // flipping the group moves the child to the right half
            group.setFlipHorizontal(true);
            img = draw(slide);
            assertEquals(Color.WHITE, getColor(img, leftHalf));
            assertEquals(Color.RED, getColor(img, rightHalf));

            // a flipped child inside a flipped group must not be flipped back by the group flip
            child.setFlipHorizontal(true);
            img = draw(slide);
            assertEquals(Color.WHITE, getColor(img, leftHalf));
            assertEquals(Color.RED, getColor(img, rightHalf));
        }
    }

    private static Point2D transform(AutoShape<?,?> shape, Point2D point) {
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = img.createGraphics();
        try {
            new DrawShape(shape).applyTransform(graphics);
            AffineTransform tx = graphics.getTransform();
            Point2D result = tx.transform(point, null);
            // round away floating point errors of the rotation
            return new Point2D.Double(Math.round(result.getX()), Math.round(result.getY()));
        } finally {
            graphics.dispose();
        }
    }

    private static BufferedImage draw(Slide<?,?> slide) {
        BufferedImage img = new BufferedImage(400, 200, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = img.createGraphics();
        try {
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, img.getWidth(), img.getHeight());
            slide.draw(graphics);
        } finally {
            graphics.dispose();
        }
        return img;
    }

    private static Color getColor(BufferedImage img, Point2D point) {
        return new Color(img.getRGB((int)point.getX(), (int)point.getY()));
    }
}
