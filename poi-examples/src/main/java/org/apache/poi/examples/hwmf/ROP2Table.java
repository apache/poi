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

package org.apache.poi.examples.hwmf;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.poi.hwmf.draw.HwmfROP2Composite;
import org.apache.poi.hwmf.record.HwmfBinaryRasterOp;
import org.apache.poi.util.Units;

/**
 * Generates an image table describing the various binary raster operations
 *
 * inspired from http://www.fengyuan.com/sample/samplech8.html
 */
public final class ROP2Table {
    private static final Color[] COLORS = {
            new Color(0,0,0),
            new Color(128,0,0),
            new Color(0,128,0),
            new Color(128,128,0),
            new Color(0,0,128),
            new Color(128,0,128),
            new Color(0,128,128),
            new Color(192,192,192),
            new Color(255,255,255),
            new Color(128,128,128),
            new Color(255,0,0),
            new Color(0,255,0),
            new Color(255,255,0),
            new Color(0,0,255),
            new Color(255,0,255),
            new Color(0,255,255)
    };

    private ROP2Table() {
    }

    public static void main(String[] args) throws IOException {
        int square = 800;
        BufferedImage bi = new BufferedImage(square + 500, square, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bi.createGraphics();
        double space = 0.2;
        double hbar = square / (COLORS.length + space);
        double vbar = square / (COLORS.length + space);
        double y = hbar * space;
        double x = vbar * space;
        double w = square - 2*x;
        double h = square - 2*y;

        Rectangle2D vrect = new Rectangle2D.Double(x, y, vbar * (1-space), h);
        for (Color c : COLORS) {
            g.setColor(c);
            g.fill(vrect);
            g.translate(vbar, 0);
        }

        g.setTransform(new AffineTransform());
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, (int) Units.pixelToPoints(hbar * 0.8)));

        Composite comp = g.getComposite();

        Rectangle2D hrect = new Rectangle2D.Double(x, y, w, hbar * (1-space));
        int idx = 0;
        for (HwmfBinaryRasterOp op : HwmfBinaryRasterOp.values()) {
            g.setComposite(comp);
            g.setColor(Color.BLACK);
            g.drawString(op.name(), (int)(square+vbar), (int)(hbar*0.8));
            g.setComposite(new HwmfROP2Composite(op));
            g.setColor(Color.RED);
            g.fill(hrect);
            g.translate(0, hbar);
            idx++;
        }

        g.dispose();
        ImageIO.write(bi, "PNG", new File("rop2.png"));
    }


}
