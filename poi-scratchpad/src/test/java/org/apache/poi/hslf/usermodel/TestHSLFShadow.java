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
package org.apache.poi.hslf.usermodel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.IOException;

import org.apache.poi.ddf.AbstractEscherOptRecord;
import org.apache.poi.ddf.EscherPropertyTypes;
import org.apache.poi.ddf.EscherSimpleProperty;
import org.apache.poi.sl.draw.DrawSimpleShape;
import org.apache.poi.sl.usermodel.Shadow;
import org.apache.poi.sl.usermodel.SimpleShape;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Binary slide shows have no rotWithShape property, so the shadow keeps its direction on the page.
 */
class TestHSLFShadow {
    private static final int OFFSET_EMU = 254000;

    @ParameterizedTest
    @CsvSource({
        "0,  false, false",
        "0,  true,  false",
        "0,  false, true",
        "0,  true,  true",
        "30, false, false",
        "30, true,  false",
        "30, false, true",
        "30, true,  true",
    })
    void shadowOfShape(double rotation, boolean flipH, boolean flipV) throws IOException {
        try (HSLFSlideShow ppt = new HSLFSlideShow()) {
            HSLFAutoShape shape = ppt.createSlide().createAutoShape();
            addShadow(shape);
            shape.setRotation(rotation);
            flip(shape, flipH, flipV);

            // HSLF flips before it rotates, see DrawShape.applyTransform
            AffineTransform tx = AffineTransform.getScaleInstance(flipH ? -1 : 1, flipV ? -1 : 1);
            tx.rotate(Math.toRadians(rotation));

            assertPageDirection(tx, shape);
        }
    }

    @ParameterizedTest
    @CsvSource({
        "false, false",
        "true,  false",
        "false, true",
        "true,  true",
    })
    void shadowOfShapeInFlippedGroup(boolean flipH, boolean flipV) throws IOException {
        try (HSLFSlideShow ppt = new HSLFSlideShow()) {
            HSLFGroupShape group = ppt.createSlide().createGroup();
            flip(group, flipH, flipV);
            HSLFAutoShape shape = group.createAutoShape();
            addShadow(shape);

            AffineTransform tx = AffineTransform.getScaleInstance(flipH ? -1 : 1, flipV ? -1 : 1);

            assertPageDirection(tx, shape);
        }
    }

    private static void flip(HSLFShape shape, boolean flipH, boolean flipV) {
        if (flipH) {
            shape.setFlipHorizontal(true);
        }
        if (flipV) {
            shape.setFlipVertical(true);
        }
    }

    private static void addShadow(HSLFSimpleShape shape) {
        AbstractEscherOptRecord opt = shape.getEscherOptRecord();
        opt.setEscherProperty(new EscherSimpleProperty(EscherPropertyTypes.SHADOWSTYLE__TYPE, 0));
        opt.setEscherProperty(new EscherSimpleProperty(EscherPropertyTypes.SHADOWSTYLE__OFFSETX, OFFSET_EMU));
        opt.setEscherProperty(new EscherSimpleProperty(EscherPropertyTypes.SHADOWSTYLE__OFFSETY, OFFSET_EMU));
    }

    private static void assertPageDirection(AffineTransform shapeTransform, HSLFSimpleShape shape) {
        Shadow<?,?> shadow = shape.getShadow();
        double expected = shadow.getDistance() * Math.cos(Math.toRadians(45));
        Point2D onPage = shapeTransform.deltaTransform(new ShadowOffset(shape).get(), null);
        assertEquals(expected, onPage.getX(), 1e-6);
        assertEquals(expected, onPage.getY(), 1e-6);
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
