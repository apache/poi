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

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;

/**
 * An utulity to convert slides of a .pptx slide show to a PNG image
 *
 * @author Yegor Kozlov
 */
public class PPTX2PNG {

    static void usage(){
        System.out.println("Usage: PPTX2PNG [options] <pptx file>");
        System.out.println("Options:");
        System.out.println("    -scale <float>   scale factor");
        System.out.println("    -slide <integer> 1-based index of a slide to render");
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            usage();
            return;
        }

        int slidenum = -1;
        float scale = 1;
        String file = null;

        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("-")) {
                if ("-scale".equals(args[i])) {
                    scale = Float.parseFloat(args[++i]);
                } else if ("-slide".equals(args[i])) {
                    slidenum = Integer.parseInt(args[++i]);
                }
            } else {
                file = args[i];
            }
        }

        if(file == null){
            usage();
            return;
        }

        System.out.println("Processing " + file);
        XMLSlideShow ppt = new XMLSlideShow(OPCPackage.open(file));

        Dimension pgsize = ppt.getPageSize();
        int width = (int) (pgsize.width * scale);
        int height = (int) (pgsize.height * scale);

        XSLFSlide[] slide = ppt.getSlides();
        for (int i = 0; i < slide.length; i++) {
            if (slidenum != -1 && slidenum != (i + 1)) continue;

            String title = slide[i].getTitle();
            System.out.println("Rendering slide " + (i + 1) + (title == null ? "" : ": " + title));

            BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = img.createGraphics();

            // default rendering options
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

            graphics.setColor(Color.white);
            graphics.clearRect(0, 0, width, height);

            graphics.scale(scale, scale);

            // draw stuff
            slide[i].draw(graphics);

            // save the result
            int sep = file.lastIndexOf(".");
            String fname = file.substring(0, sep == -1 ? file.length() : sep) + "-" + (i + 1) +".png";
            FileOutputStream out = new FileOutputStream(fname);
            ImageIO.write(img, "png", out);
            out.close();
        }
        System.out.println("Done");
    }
}
