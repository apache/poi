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
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.poi.sl.draw.Drawable;
import org.apache.poi.sl.usermodel.Slide;
import org.apache.poi.sl.usermodel.SlideShow;
import org.apache.poi.sl.usermodel.SlideShowFactory;
import org.apache.poi.util.JvmBugs;

/**
 * An utulity to convert slides of a .pptx slide show to a PNG image
 *
 * @author Yegor Kozlov
 */
public class PPTX2PNG {

    static void usage(String error){
        String msg =
            "Usage: PPTX2PNG [options] <ppt or pptx file>\n" +
            (error == null ? "" : ("Error: "+error+"\n")) +
            "Options:\n" +
            "    -scale <float>   scale factor\n" +
            "    -slide <integer> 1-based index of a slide to render\n" +
            "    -format <type>   png,gif,jpg (,null for testing)" +
            "    -outdir <dir>    output directory, defaults to origin of the ppt/pptx file" +
            "    -quite           do not write to console (for normal processing)";

        System.out.println(msg);
        // no System.exit here, as we also run in junit tests!
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            usage(null);
            return;
        }

        int slidenum = -1;
        float scale = 1;
        File file = null;
        String format = "png";
        File outdir = null;
        boolean quite = false;

        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("-")) {
                if ("-scale".equals(args[i])) {
                    scale = Float.parseFloat(args[++i]);
                } else if ("-slide".equals(args[i])) {
                    slidenum = Integer.parseInt(args[++i]);
                } else if ("-format".equals(args[i])) {
                    format = args[++i];
                } else if ("-outdir".equals(args[i])) {
                    outdir = new File(args[++i]);
                } else if ("-quite".equals(args[i])) {
                    quite = true;
                }
            } else {
                file = new File(args[i]);
            }
        }

        if (file == null || !file.exists()) {
            usage("File not specified or it doesn't exist");
            return;
        }

        if (outdir == null) {
            outdir = file.getParentFile();
        }
        
        if (outdir == null || !outdir.exists() || !outdir.isDirectory()) {
            usage("Output directory doesn't exist");
            return;
        }

        if (scale < 0) {
            usage("Invalid scale given");
            return;
        }
        
        if (format == null || !format.matches("^(png|gif|jpg|null)$")) {
            usage("Invalid format given");
            return;
        }
    
        if (!quite) {
            System.out.println("Processing " + file);
        }
        SlideShow<?,?> ss = SlideShowFactory.create(file, null, true);
        List<? extends Slide<?,?>> slides = ss.getSlides();

        
        if (slidenum < -1 || slidenum == 0 || slidenum > slides.size()) {
            usage("slidenum must be either -1 (for all) or within range: [1.."+slides.size()+"] for "+file);
            return;
        }
        
        Dimension pgsize = ss.getPageSize();
        int width = (int) (pgsize.width * scale);
        int height = (int) (pgsize.height * scale);

        int slideNo=1;
        for(Slide<?,?> slide : slides) {
            if (slidenum == -1 || slideNo == slidenum) {
                String title = slide.getTitle();
                if (!quite) {
                    System.out.println("Rendering slide " + slideNo + (title == null ? "" : ": " + title));
                }

                BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                Graphics2D graphics = img.createGraphics();
                fixFonts(graphics);
            
                // default rendering options
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

                graphics.scale(scale, scale);

                // draw stuff
                slide.draw(graphics);

                // save the result
                if (!"null".equals(format)) {
                    String outname = file.getName().replaceFirst(".pptx?", "");
                    outname = String.format(Locale.ROOT, "%1$s-%2$04d.%3$s", outname, slideNo, format);
                    File outfile = new File(outdir, outname);
                    ImageIO.write(img, format, outfile);
                }
            }                
            slideNo++;
        }
        
        if (!quite) {
            System.out.println("Done");
        }
    }

    @SuppressWarnings("unchecked")
    private static void fixFonts(Graphics2D graphics) {
        if (!JvmBugs.hasLineBreakMeasurerBug()) return;
        Map<String,String> fontMap = (Map<String,String>)graphics.getRenderingHint(Drawable.FONT_MAP);
        if (fontMap == null) fontMap = new HashMap<String,String>();
        fontMap.put("Calibri", "Lucida Sans");
        fontMap.put("Cambria", "Lucida Bright");
        graphics.setRenderingHint(Drawable.FONT_MAP, fontMap);        
    }
}
