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

package org.apache.poi.hwmf;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import org.apache.poi.hwmf.draw.HwmfGraphics;
import org.junit.jupiter.api.Test;

class TestHwmfGraphics {

    /**
     * An unset or unknown raster operation is drawn like SRCCOPY
     */
    @Test
    void drawImageWithoutRasterOp() {
        BufferedImage canvas = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = canvas.createGraphics();
        try {
            Rectangle2D bounds = new Rectangle2D.Double(0, 0, 10, 10);
            HwmfGraphics hg = new HwmfGraphics(g, bounds);

            BufferedImage img = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
            Graphics2D ig = img.createGraphics();
            ig.setColor(Color.RED);
            ig.fillRect(0, 0, 2, 2);
            ig.dispose();

            hg.drawImage(img, new Rectangle2D.Double(0, 0, 2, 2), bounds);
        } finally {
            g.dispose();
        }
        assertEquals(Color.RED.getRGB(), canvas.getRGB(5, 5));
    }
}
