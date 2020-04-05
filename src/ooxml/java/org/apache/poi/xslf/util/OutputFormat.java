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

package org.apache.poi.xslf.util;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

import javax.imageio.ImageIO;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.poi.sl.draw.Drawable;
import org.apache.poi.util.Internal;
import org.apache.poi.xslf.draw.SVGPOIGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

/**
 * Output formats for PPTX2PNG
 */
@Internal
interface OutputFormat extends Closeable {

    Graphics2D getGraphics2D(double width, double height);

    void writeOut(MFProxy proxy, File outFile) throws IOException;

    class SVGFormat implements OutputFormat {
        static final String svgNS = "http://www.w3.org/2000/svg";
        private SVGGraphics2D svgGenerator;
        private final boolean textAsShapes;

        SVGFormat(boolean textAsShapes) {
            this.textAsShapes = textAsShapes;
        }

        @Override
        public Graphics2D getGraphics2D(double width, double height) {
            // Get a DOMImplementation.
            DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();

            // Create an instance of org.w3c.dom.Document.
            Document document = domImpl.createDocument(svgNS, "svg", null);
            svgGenerator = new SVGPOIGraphics2D(document, textAsShapes);
            svgGenerator.setSVGCanvasSize(new Dimension((int)width, (int)height));
            return svgGenerator;
        }

        @Override
        public void writeOut(MFProxy proxy, File outFile) throws IOException {
            svgGenerator.stream(outFile.getCanonicalPath(), true);
        }

        @Override
        public void close() throws IOException {
            svgGenerator.dispose();
        }
    }

    class BitmapFormat implements OutputFormat {
        private final String format;
        private BufferedImage img;
        private Graphics2D graphics;

        BitmapFormat(String format) {
            this.format = format;
        }

        @Override
        public Graphics2D getGraphics2D(double width, double height) {
            img = new BufferedImage((int)width, (int)height, BufferedImage.TYPE_INT_ARGB);
            graphics = img.createGraphics();
            graphics.setRenderingHint(Drawable.BUFFERED_IMAGE, new WeakReference<>(img));
            return graphics;
        }

        @Override
        public void writeOut(MFProxy proxy, File outFile) throws IOException {
            if (!"null".equals(format)) {
                ImageIO.write(img, format, outFile);
            }
        }

        @Override
        public void close() throws IOException {
            if (graphics != null) {
                graphics.dispose();
                img.flush();
            }
        }
    }
}
