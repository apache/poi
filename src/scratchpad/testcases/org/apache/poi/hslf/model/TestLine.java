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

import java.awt.Color;
import java.io.IOException;

import org.apache.poi.hslf.usermodel.HSLFLine;
import org.apache.poi.hslf.usermodel.HSLFSlide;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.sl.usermodel.StrokeStyle.LineCompound;
import org.apache.poi.sl.usermodel.StrokeStyle.LineDash;
import org.junit.Test;

/**
 * Test Line shape.
 */
public final class TestLine {

    @Test
    public void testCreateLines() throws IOException {
        HSLFSlideShow ppt = new HSLFSlideShow();

        HSLFSlide slide = ppt.createSlide();

        slide.addTitle().setText("Lines tester");

        HSLFLine line;

        /**
         * line styles
         */
        line = new HSLFLine();
        line.setAnchor(new java.awt.Rectangle(75, 200, 300, 0));
        line.setLineCompound(LineCompound.SINGLE);
        line.setLineColor(Color.blue);
        slide.addShape(line);

        line = new HSLFLine();
        line.setAnchor(new java.awt.Rectangle(75, 230, 300, 0));
        line.setLineCompound(LineCompound.DOUBLE);
        line.setLineWidth(3.5);
        slide.addShape(line);

        line = new HSLFLine();
        line.setAnchor(new java.awt.Rectangle(75, 260, 300, 0));
        line.setLineCompound(LineCompound.TRIPLE);
        line.setLineWidth(6);
        slide.addShape(line);

        line = new HSLFLine();
        line.setAnchor(new java.awt.Rectangle(75, 290, 300, 0));
        line.setLineCompound(LineCompound.THICK_THIN);
        line.setLineWidth(4.5);
        slide.addShape(line);

        line = new HSLFLine();
        line.setAnchor(new java.awt.Rectangle(75, 320, 300, 0));
        line.setLineCompound(LineCompound.THIN_THICK);
        line.setLineWidth(5.5);
        slide.addShape(line);

        /**
         * line dashing
         */
        line = new HSLFLine();
        line.setAnchor(new java.awt.Rectangle(450, 200, 300, 0));
        line.setLineDash(LineDash.SOLID);
        slide.addShape(line);

        line = new HSLFLine();
        line.setAnchor(new java.awt.Rectangle(450, 230, 300, 0));
        line.setLineDash(LineDash.DASH);
        slide.addShape(line);

        line = new HSLFLine();
        line.setAnchor(new java.awt.Rectangle(450, 260, 300, 0));
        line.setLineDash(LineDash.DOT);
        slide.addShape(line);

        line = new HSLFLine();
        line.setAnchor(new java.awt.Rectangle(450, 290, 300, 0));
        line.setLineDash(LineDash.DASH_DOT);
        slide.addShape(line);

        line = new HSLFLine();
        line.setAnchor(new java.awt.Rectangle(450, 320, 300, 0));
        line.setLineDash(LineDash.LG_DASH_DOT_DOT);
        slide.addShape(line);

        /**
         * Combinations
         */
        line = new HSLFLine();
        line.setAnchor(new java.awt.Rectangle(75, 400, 300, 0));
        line.setLineDash(LineDash.DASH_DOT);
        line.setLineCompound(LineCompound.TRIPLE);
        line.setLineWidth(5.0);
        slide.addShape(line);

        line = new HSLFLine();
        line.setAnchor(new java.awt.Rectangle(75, 430, 300, 0));
        line.setLineDash(LineDash.DASH);
        line.setLineCompound(LineCompound.THICK_THIN);
        line.setLineWidth(4.0);
        slide.addShape(line);

        line = new HSLFLine();
        line.setAnchor(new java.awt.Rectangle(75, 460, 300, 0));
        line.setLineDash(LineDash.DOT);
        line.setLineCompound(LineCompound.DOUBLE);
        line.setLineWidth(8.0);
        slide.addShape(line);
        
        ppt.close();
    }
}
