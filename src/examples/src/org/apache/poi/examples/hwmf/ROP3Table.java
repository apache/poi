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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.TexturePaint;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.poi.hwmf.draw.HwmfROP3Composite;
import org.apache.poi.hwmf.record.HwmfTernaryRasterOp;

/**
 * Generates an image table describing the various ternary raster operations
 *
 * inspired from http://www.evmsoft.net/en/roptest.html
 */
public final class ROP3Table {
    private ROP3Table() {
    }

    private static byte[] PATTERN = {
        1, 0, 1, 0, 1, 0, 1, 0,
        0, 1, 0, 1, 0, 1, 0, 1,
        1, 0, 1, 1, 1, 0, 1, 1,
        0, 1, 0, 1, 0, 1, 0, 1,
        1, 0, 1, 0, 1, 0, 1, 0,
        0, 1, 0, 1, 0, 1, 0, 1,
        1, 0, 1, 1, 1, 0, 1, 1,
        0, 1, 0, 1, 0, 1, 0, 1,
    };

    private static final HwmfTernaryRasterOp[] OPS = HwmfTernaryRasterOp.values();
    private static final int COLS = 16;
    private static final double BOX = 100, SCALE = 1, HEADER = 1.1;

    private static final Rectangle2D RECT = new Rectangle2D.Double(0.05* BOX, 0.05* BOX, 0.90* BOX, 0.90* BOX);
    private static final Shape CIRCLE_BIG = new Ellipse2D.Double(0.15* BOX, 0.15* BOX, 0.70* BOX, 0.70* BOX);
    private static final Shape CIRCLE_SMALL = new Ellipse2D.Double(0.40* BOX, 0.40* BOX, 0.20* BOX, 0.20* BOX);
    private static final Shape LABEL_BOX = new Rectangle.Double(0.06* BOX, 0.85* BOX, 0.88* BOX, 0.10* BOX);

    private static final AlphaComposite SRC_OVER = AlphaComposite.getInstance(AlphaComposite.SRC_OVER);

    private static final AffineTransform INIT_AT = AffineTransform.getScaleInstance(SCALE, SCALE);

    public static void main(String[] args) throws IOException {
        BufferedImage pattern = getPattern();
        BufferedImage source = getSource();

        BufferedImage dest = new BufferedImage(
                (int)(BOX * COLS * SCALE),
                (int)(BOX *(Math.max(OPS.length/COLS,1) + HEADER)* SCALE),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = dest.createGraphics();

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));

        g.setTransform(INIT_AT);
        g.setColor(Color.BLACK);

        for (int i=0; i<3; i++) {
            String str = new String[]{"Dest:","Source:","Pattern:"}[i];
            TextLayout t = new TextLayout(str, g.getFont(), g.getFontRenderContext());
            Rectangle2D b = t.getBounds();
            g.drawString(str, (float)(((i*2+0.95)*BOX - b.getWidth())), (float)(0.55 * BOX));
        }

        g.translate(BOX, 0);
        fillDest(g);
        g.translate(2*BOX, 0);
        g.drawImage(source, 0, 0, null);
        g.translate(2*BOX, 0);
        g.setPaint(new TexturePaint(pattern, RECT));
        g.fill(RECT);

        int idx=0;
        for (HwmfTernaryRasterOp op : OPS) {
            g.setTransform(INIT_AT);
            g.translate(0, HEADER * BOX);
            g.translate(BOX*(idx%COLS), BOX*(idx/COLS));

            fillDest(g);
            fillPattern(g, op, pattern, source);
            fillLabel(g, op);
            idx++;
        }

        g.dispose();
        ImageIO.write(dest, "PNG", new File("rop3.png"));
    }

    private static BufferedImage getPattern() {
        byte[] bw = { 0, -1 };
        BufferedImage pattern = new BufferedImage(8, 8, BufferedImage.TYPE_BYTE_INDEXED, new IndexColorModel(1, 2, bw, bw, bw));
        pattern.getRaster().setDataElements(0, 0, 8, 8, PATTERN);
        return pattern;
    }

    private static BufferedImage getSource() {
        BufferedImage checker = new BufferedImage((int) BOX, (int) BOX, BufferedImage.TYPE_INT_ARGB);
        Graphics2D cg = checker.createGraphics();
        cg.setColor(Color.PINK);
        cg.fill(new Rectangle2D.Double(0.05* BOX, 0.05* BOX, 0.90* BOX, 0.90* BOX));
        cg.setColor(new Color(0xE6E6FA, false));
        cg.fill(new Rectangle2D.Double(0.05* BOX, 0.05* BOX, 0.45* BOX, 0.45* BOX));
        cg.fill(new Rectangle2D.Double(0.50* BOX, 0.50* BOX, 0.45* BOX, 0.45* BOX));
        cg.dispose();
        return checker;
    }

    private static void fillDest(Graphics2D g) {
        g.setComposite(SRC_OVER);
        g.setColor(Color.LIGHT_GRAY);
        g.fill(RECT);
        g.setColor(new Color(0xDAA520, false));
        g.fill(CIRCLE_BIG);
        g.setColor(Color.RED);
        g.fill(CIRCLE_SMALL);
    }

    private static void fillPattern(Graphics2D g, HwmfTernaryRasterOp op, BufferedImage pattern, BufferedImage source) {
        g.setComposite(new HwmfROP3Composite(g.getTransform(), RECT, op, pattern, Color.YELLOW, Color.BLUE));
        g.setClip(RECT);
        g.drawImage(source, 0, 0, null);
        g.setClip(null);
        g.setComposite(SRC_OVER);
    }

        private static void fillLabel(Graphics2D g, HwmfTernaryRasterOp op) {
        g.setColor(Color.WHITE);
        g.fill(LABEL_BOX);
        g.setColor(Color.BLACK);

        TextLayout t = new TextLayout(op.name(), g.getFont(), g.getFontRenderContext());
        Rectangle2D b = t.getBounds();
        g.drawString(op.name(), (float)((BOX -b.getWidth())/2.), (float)(0.94* BOX));

    }

}
