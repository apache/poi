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

package org.apache.poi.xdgf.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.poi.util.Dimension2DDouble;
import org.apache.poi.xdgf.usermodel.XDGFPage;
import org.apache.poi.xdgf.usermodel.XmlVisioDocument;
import org.apache.poi.xdgf.usermodel.shape.ShapeDebuggerRenderer;
import org.apache.poi.xdgf.usermodel.shape.ShapeRenderer;

/**
 * Converts a Visio diagram to a PNG file.
 *
 * As more elements and styles are added/supported the output will get
 * better, but it's very rough right now.
 */
public class VsdxToPng {

    public static void renderToPng(XDGFPage page, String outFilename,
            double scale, ShapeRenderer renderer) throws IOException {
        renderToPng(page, new File(outFilename), scale, renderer);
    }

    public static void renderToPngDir(XDGFPage page, File outDir, double scale,
            ShapeRenderer renderer) throws IOException {

        File pageFile = new File(outDir, "page" + page.getPageNumber() + "-"
                + Util.sanitizeFilename(page.getName()) + ".png");
        System.out.println("** Writing image to " + pageFile);

        renderToPng(page, pageFile, scale, renderer);

    }

    public static void renderToPng(XDGFPage page, File outFile, double scale,
            ShapeRenderer renderer) throws IOException {

        Dimension2DDouble sz = page.getPageSize();

        int width = (int) (scale * sz.getWidth());
        int height = (int) (scale * sz.getHeight());

        BufferedImage img = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);
        final Graphics2D graphics = img.createGraphics();

        // default rendering options
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                RenderingHints.VALUE_FRACTIONALMETRICS_ON);

        graphics.setColor(Color.black);
        graphics.setBackground(Color.white);
        graphics.clearRect(0, 0, width, height);

        // Visio's coordinate system is flipped, so flip the image vertically
        graphics.translate(0, img.getHeight());
        graphics.scale(scale, -scale);

        // toplevel shapes only
        renderer.setGraphics(graphics);
        page.getContent().visitShapes(renderer);

        graphics.dispose();

        try (FileOutputStream out = new FileOutputStream(outFile)) {
            ImageIO.write(img, "png", out);
        }
    }

    public static void renderToPng(XmlVisioDocument document,
            String outDirname, double scale, ShapeRenderer renderer)
                    throws IOException {

        File outDir = new File(outDirname);

        for (XDGFPage page : document.getPages()) {
            renderToPngDir(page, outDir, scale, renderer);
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length > 2) {
            System.err.println("Usage: [--debug] in.vsdx outdir");
            System.exit(1);
        }

        ShapeRenderer renderer = new ShapeRenderer();

        String inFilename = args[0];
        String pngDir = args[1];

        if (args[0].equals("--debug")) {
            inFilename = args[1];
            pngDir = args[2];
            renderer = new ShapeDebuggerRenderer();
        }

        try (FileInputStream is = new FileInputStream(inFilename)) {
            XmlVisioDocument doc = new XmlVisioDocument(is);
            renderToPng(doc, pngDir, 2000 / 11.0, renderer);
        }
    }
}
