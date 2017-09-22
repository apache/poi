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

package org.apache.poi.hslf.examples;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.io.FileOutputStream;

import org.apache.poi.hslf.model.PPGraphics2D;
import org.apache.poi.hslf.usermodel.HSLFGroupShape;
import org.apache.poi.hslf.usermodel.HSLFSlide;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;

/**
 * Demonstrates how to draw into a slide using the HSLF Graphics2D driver.
 *
 * @author Yegor Kozlov
 */
public final class Graphics2DDemo {

    /**
     * A simple bar chart demo
     */
    public static void main(String[] args) throws Exception {
        try (HSLFSlideShow ppt = new HSLFSlideShow()) {
            //bar chart data. The first value is the bar color, the second is the width
            Object[] def = new Object[]{
                    Color.yellow, 40,
                    Color.green, 60,
                    Color.gray, 30,
                    Color.red, 80,
            };

            HSLFSlide slide = ppt.createSlide();

            HSLFGroupShape group = new HSLFGroupShape();
            //define position of the drawing in the slide
            Rectangle bounds = new Rectangle(200, 100, 350, 300);
            group.setAnchor(bounds);
            group.setInteriorAnchor(new Rectangle(0, 0, 100, 100));
            slide.addShape(group);
            Graphics2D graphics = new PPGraphics2D(group);

            //draw a simple bar graph
            int x = 10, y = 10;
            graphics.setFont(new Font("Arial", Font.BOLD, 10));
            for (int i = 0, idx = 1; i < def.length; i += 2, idx++) {
                graphics.setColor(Color.black);
                int width = ((Integer) def[i + 1]).intValue();
                graphics.drawString("Q" + idx, x - 5, y + 10);
                graphics.drawString(width + "%", x + width + 3, y + 10);
                graphics.setColor((Color) def[i]);
                graphics.fill(new Rectangle(x, y, width, 10));
                y += 15;
            }
            graphics.setColor(Color.black);
            graphics.setFont(new Font("Arial", Font.BOLD, 14));
            graphics.draw(group.getInteriorAnchor());
            graphics.drawString("Performance", x + 30, y + 10);

            try (FileOutputStream out = new FileOutputStream("hslf-graphics.ppt")) {
                ppt.write(out);
            }
        }
    }
}
