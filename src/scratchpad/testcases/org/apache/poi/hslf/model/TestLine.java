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

package org.apache.poi.hslf.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.Arrays;

import org.apache.poi.hslf.HSLFTestDataSamples;
import org.apache.poi.hslf.usermodel.HSLFLine;
import org.apache.poi.hslf.usermodel.HSLFShape;
import org.apache.poi.hslf.usermodel.HSLFSlide;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.sl.usermodel.StrokeStyle.LineCompound;
import org.apache.poi.sl.usermodel.StrokeStyle.LineDash;
import org.junit.jupiter.api.Test;

/**
 * Test Line shape.
 */
public final class TestLine {

    private static final Object[][] lines = {
        // line styles
        { 75, 200, LineCompound.SINGLE, Color.blue },
        { 75, 230, LineCompound.DOUBLE, 3.5 },
        { 75, 260, LineCompound.TRIPLE, 6d },
        { 75, 290, LineCompound.THICK_THIN, 4.5d },
        { 75, 320, LineCompound.THIN_THICK, 5.5d },
        // line dashing
        { 450, 200, LineDash.SOLID },
        { 450, 230, LineDash.DASH },
        { 450, 260, LineDash.DOT },
        { 450, 290, LineDash.DASH_DOT },
        { 450, 320, LineDash.LG_DASH_DOT_DOT },
        // Combinations
        { 75, 400, LineDash.DASH_DOT, LineCompound.TRIPLE, 5d },
        { 75, 430, LineDash.DASH, LineCompound.THICK_THIN, 4d },
        { 75, 460, LineDash.DOT, LineCompound.DOUBLE, 8d }
    };


    @Test
    void testCreateLines() throws IOException {

        try (HSLFSlideShow ppt1 = new HSLFSlideShow()) {
            HSLFSlide slide1 = ppt1.createSlide();
            slide1.addTitle().setText("Lines tester");

            for (Object[] line : lines) {
                HSLFLine hslfLine = new HSLFLine();
                hslfLine.setAnchor(new Rectangle((Integer)line[0], (Integer)line[1], 300, 0));
                for (Object attr : Arrays.copyOfRange(line, 2, line.length)) {
                    if (attr instanceof LineCompound) {
                        hslfLine.setLineCompound((LineCompound)attr);
                    } else if (attr instanceof Double) {
                        hslfLine.setLineWidth((Double)attr);
                    } else if (attr instanceof Color) {
                        hslfLine.setLineColor((Color)attr);
                    } else if (attr instanceof LineDash) {
                        hslfLine.setLineDash((LineDash)attr);
                    }
                }
                slide1.addShape(hslfLine);
            }

            try (HSLFSlideShow ppt2 = HSLFTestDataSamples.writeOutAndReadBack(ppt1)) {
                HSLFSlide slide2 = ppt2.getSlides().get(0);

                int idx = 0;
                for (HSLFShape shape : slide2.getShapes().subList(1,14)) {
                    HSLFLine hslfLine = (HSLFLine)shape;
                    Object[] line = lines[idx++];
                    Rectangle2D anchor = hslfLine.getAnchor();
                    assertEquals(line[0], (int)anchor.getX());
                    assertEquals(line[1], (int)anchor.getY());
                    for (Object attr : Arrays.copyOfRange(line, 2, line.length)) {
                        if (attr instanceof LineCompound) {
                            assertEquals(attr, hslfLine.getLineCompound());
                        } else if (attr instanceof Double) {
                            assertEquals(attr, hslfLine.getLineWidth());
                        } else if (attr instanceof Color) {
                            assertEquals(attr, hslfLine.getLineColor());
                        } else if (attr instanceof LineDash) {
                            assertEquals(attr, hslfLine.getLineDash());
                        }
                    }
                }
            }
        }
    }
}
