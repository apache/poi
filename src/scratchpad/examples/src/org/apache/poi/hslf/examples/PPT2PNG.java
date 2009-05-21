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

import org.apache.poi.hslf.usermodel.*;
import org.apache.poi.hslf.model.*;
import org.apache.poi.hslf.record.TextHeaderAtom;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

/**
 * Demonstrates how you can use HSLF to convert each slide into a PNG image
 *
 * @author Yegor Kozlov
 */
public final class PPT2PNG {

    public static void main(String args[]) throws Exception {

        if (args.length == 0) {
            usage();
            return;
        }

        int slidenum = -1;
        float scale = 1;
        String file = null;

        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("-")) {
                if ("-scale".equals(args[i])){
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

        FileInputStream is = new FileInputStream(file);
        SlideShow ppt = new SlideShow(is);
        is.close();

        Dimension pgsize = ppt.getPageSize();
        int width = (int)(pgsize.width*scale);
        int height = (int)(pgsize.height*scale);

        Slide[] slide = ppt.getSlides();
        for (int i = 0; i < slide.length; i++) {
            if (slidenum != -1 && slidenum != (i+1)) continue;

            String title = slide[i].getTitle();
            System.out.println("Rendering slide "+slide[i].getSlideNumber() + (title == null ? "" : ": " + title));

            BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = img.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

            graphics.setPaint(Color.white);
            graphics.fill(new Rectangle2D.Float(0, 0, width, height));

            graphics.scale((double)width/pgsize.width, (double)height/pgsize.height);

            slide[i].draw(graphics);

            String fname = file.replaceAll("\\.ppt", "-" + (i+1) + ".png");
            FileOutputStream out = new FileOutputStream(fname);
            ImageIO.write(img, "png", out);
            out.close();
        }
    }

    private static void usage(){
        System.out.println("Usage: PPT2PNG [-scale <scale> -slide <num>] ppt");
    }
}
