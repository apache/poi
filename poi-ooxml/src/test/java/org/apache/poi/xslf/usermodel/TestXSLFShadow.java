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
package org.apache.poi.xslf.usermodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.IOException;

import org.apache.poi.sl.draw.DrawSimpleShape;
import org.apache.poi.sl.usermodel.SimpleShape;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openxmlformats.schemas.drawingml.x2006.main.CTOuterShadowEffect;
import org.openxmlformats.schemas.presentationml.x2006.main.CTShape;

/**
 * The expected angles were observed by rendering flipped and rotated shapes in PowerPoint.
 */
class TestXSLFShadow {
    private static final long DIST_EMU = 254000;
    private static final int DIR_45_DEGREES = 2700000;
    private static final double DIST_POINTS = 20;

    @ParameterizedTest
    @CsvSource({
        "false, 0,  false, false, 45",
        "false, 0,  true,  false, 45",
        "false, 0,  false, true,  45",
        "false, 0,  true,  true,  45",
        "false, 30, false, false, 45",
        "false, 30, true,  false, 45",
        "false, 30, false, true,  45",
        "false, 30, true,  true,  45",
        "true,  0,  false, false, 45",
        "true,  0,  true,  false, 135",
        "true,  0,  false, true,  -45",
        "true,  0,  true,  true,  225",
        "true,  30, false, false, 75",
        "true,  30, true,  false, 165",
        "true,  30, false, true,  -15",
        "true,  30, true,  true,  255",
    })
    void shadowOfShape(boolean rotWithShape, double rotation, boolean flipH, boolean flipV, double pageAngle)
    throws IOException {
        try (XMLSlideShow ppt = new XMLSlideShow()) {
            XSLFAutoShape shape = ppt.createSlide().createAutoShape();
            addShadow(shape).setRotWithShape(rotWithShape);
            shape.setRotation(rotation);
            shape.setFlipHorizontal(flipH);
            shape.setFlipVertical(flipV);

            AffineTransform tx = new AffineTransform();
            tx.rotate(Math.toRadians(rotation));
            tx.scale(flipH ? -1 : 1, flipV ? -1 : 1);

            assertOffset(pageAngle, tx, shape);
        }
    }

    @ParameterizedTest
    @CsvSource({
        "false, false, false, 45",
        "false, true,  false, 45",
        "false, false, true,  45",
        "false, true,  true,  45",
        "true,  false, false, 45",
        "true,  true,  false, 135",
        "true,  false, true,  -45",
        "true,  true,  true,  225",
    })
    void shadowOfShapeInFlippedGroup(boolean rotWithShape, boolean flipH, boolean flipV, double pageAngle)
    throws IOException {
        try (XMLSlideShow ppt = new XMLSlideShow()) {
            XSLFGroupShape group = ppt.createSlide().createGroup();
            group.setFlipHorizontal(flipH);
            group.setFlipVertical(flipV);
            XSLFAutoShape shape = group.createAutoShape();
            addShadow(shape).setRotWithShape(rotWithShape);

            AffineTransform tx = AffineTransform.getScaleInstance(flipH ? -1 : 1, flipV ? -1 : 1);

            assertOffset(pageAngle, tx, shape);
        }
    }

    @Test
    void rotateWithShapeDefaultsToTrue() throws IOException {
        try (XMLSlideShow ppt = new XMLSlideShow()) {
            XSLFAutoShape shape = ppt.createSlide().createAutoShape();
            CTOuterShadowEffect shadow = addShadow(shape);
            assertTrue(shape.getShadow().isRotateWithShape(), "unset rotWithShape");

            shadow.setRotWithShape(false);
            assertFalse(shape.getShadow().isRotateWithShape(), "rotWithShape=0");
        }
    }

    private static CTOuterShadowEffect addShadow(XSLFSimpleShape shape) {
        CTOuterShadowEffect shadow = ((CTShape)shape.getXmlObject()).getSpPr().addNewEffectLst().addNewOuterShdw();
        shadow.setDist(DIST_EMU);
        shadow.setDir(DIR_45_DEGREES);
        return shadow;
    }

    private static void assertOffset(double pageAngle, AffineTransform shapeTransform, XSLFSimpleShape shape) {
        Point2D onPage = shapeTransform.deltaTransform(new ShadowOffset(shape).get(), null);
        assertEquals(DIST_POINTS * Math.cos(Math.toRadians(pageAngle)), onPage.getX(), 1e-6);
        assertEquals(DIST_POINTS * Math.sin(Math.toRadians(pageAngle)), onPage.getY(), 1e-6);
    }

    private static final class ShadowOffset extends DrawSimpleShape {
        ShadowOffset(SimpleShape<?,?> shape) {
            super(shape);
        }

        Point2D get() {
            return getShadowOffset(getShape().getShadow());
        }
    }
}
