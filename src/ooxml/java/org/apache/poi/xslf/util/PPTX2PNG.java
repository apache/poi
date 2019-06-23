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
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.poi.sl.draw.Drawable;
import org.apache.poi.sl.usermodel.Slide;
import org.apache.poi.sl.usermodel.SlideShow;
import org.apache.poi.sl.usermodel.SlideShowFactory;

/**
 * An utility to convert slides of a .pptx slide show to a PNG image
 */
public class PPTX2PNG {

    private static final String INPUT_PAT_REGEX =
        "(?<slideno>[^|]+)\\|(?<format>[^|]+)\\|(?<basename>.+)\\.(?<ext>[^.]++)";

    private static final Pattern INPUT_PATTERN = Pattern.compile(INPUT_PAT_REGEX);

    private static final String OUTPUT_PAT_REGEX = "${basename}-${slideno}.${format}";


    private static void usage(String error){
        String msg =
            "Usage: PPTX2PNG [options] <ppt or pptx file>\n" +
            (error == null ? "" : ("Error: "+error+"\n")) +
            "Options:\n" +
            "    -scale <float>    scale factor\n" +
            "    -slide <integer>  1-based index of a slide to render\n" +
            "    -format <type>    png,gif,jpg (,null for testing)\n" +
            "    -outdir <dir>     output directory, defaults to origin of the ppt/pptx file\n" +
            "    -outfile <file>   output filename, defaults to '"+OUTPUT_PAT_REGEX+"'\n" +
            "    -outpat <pattern> output filename pattern, defaults to '"+OUTPUT_PAT_REGEX+"'\n" +
            "                      patterns: basename, slideno, format, ext\n" +
            "    -quiet            do not write to console (for normal processing)";

        System.out.println(msg);
        // no System.exit here, as we also run in junit tests!
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            usage(null);
            return;
        }

        String slidenumStr = "-1";
        float scale = 1;
        File file = null;
        String format = "png";
        File outdir = null;
        String outfile = null;
        boolean quiet = false;
        String outpattern = OUTPUT_PAT_REGEX;

        for (int i = 0; i < args.length; i++) {
            String opt = (i+1 < args.length) ? args[i+1] : null;
            switch (args[i]) {
                case "-scale":
                    scale = Float.parseFloat(opt);
                    i++;
                    break;
                case "-slide":
                    slidenumStr = opt;
                    i++;
                    break;
                case "-format":
                    format = opt;
                    i++;
                    break;
                case "-outdir":
                    outdir = new File(opt);
                    i++;
                    break;
                case "-outfile":
                    outfile = opt;
                    i++;
                    break;
                case "-outpat":
                    outpattern = opt;
                    i++;
                    break;
                case "-quiet":
                    quiet = true;
                    break;
                default:
                    file = new File(args[i]);
                    break;
            }
        }

        if (file == null || !file.exists()) {
            usage("File not specified or it doesn't exist");
            return;
        }

        if (format == null || !format.matches("^(png|gif|jpg|null)$")) {
            usage("Invalid format given");
            return;
        }

        if (outdir == null) {
            outdir = file.getParentFile();
        }

        if (!"null".equals(format) && (outdir == null || !outdir.exists() || !outdir.isDirectory())) {
            usage("Output directory doesn't exist");
            return;
        }

        if (scale < 0) {
            usage("Invalid scale given");
            return;
        }

        if (!quiet) {
            System.out.println("Processing " + file);
        }
        try (SlideShow<?, ?> ss = SlideShowFactory.create(file, null, true)) {
            List<? extends Slide<?, ?>> slides = ss.getSlides();

            Set<Integer> slidenum = slideIndexes(slides.size(), slidenumStr);

            if (slidenum.isEmpty()) {
                usage("slidenum must be either -1 (for all) or within range: [1.." + slides.size() + "] for " + file);
                return;
            }

            Dimension pgsize = ss.getPageSize();
            int width = (int) (pgsize.width * scale);
            int height = (int) (pgsize.height * scale);

            for (Integer slideNo : slidenum) {
                Slide<?, ?> slide = slides.get(slideNo);
                String title = slide.getTitle();
                if (!quiet) {
                    System.out.println("Rendering slide " + (slideNo+1) + (title == null ? "" : ": " + title.trim()));
                }

                BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                Graphics2D graphics = img.createGraphics();

                // default rendering options
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
                graphics.setRenderingHint(Drawable.BUFFERED_IMAGE, new WeakReference<>(img));

                graphics.scale(scale, scale);

                // draw stuff
                slide.draw(graphics);

                // save the result
                if (!"null".equals(format)) {
                    String inname = String.format(Locale.ROOT, "%04d|%s|%s", slideNo+1, format, file.getName());
                    String outname = (outfile != null) ? outfile : INPUT_PATTERN.matcher(inname).replaceAll(outpattern);
                    ImageIO.write(img, format, new File(outdir, outname));
                }

                graphics.dispose();
                img.flush();
            }
        }

        if (!quiet) {
            System.out.println("Done");
        }
    }

    private static Set<Integer> slideIndexes(final int slideCount, String range) {
        Set<Integer> slideIdx = new TreeSet<>();
        if ("-1".equals(range)) {
            for (int i=0; i<slideCount; i++) {
                slideIdx.add(i);
            }
        } else {
            for (String subrange : range.split(",")) {
                String[] idx = subrange.split("-");
                switch (idx.length) {
                default:
                case 0: break;
                case 1: {
                    int subidx = Integer.parseInt(idx[0]);
                    if (subrange.contains("-")) {
                        int startIdx = subrange.startsWith("-") ? 0 : subidx;
                        int endIdx = subrange.endsWith("-") ? slideCount : Math.min(subidx,slideCount);
                        for (int i=Math.max(startIdx,1); i<endIdx; i++) {
                            slideIdx.add(i-1);
                        }
                    } else {
                        slideIdx.add(Math.max(subidx,1)-1);
                    }
                    break;
                }
                case 2: {
                    int startIdx = Math.min(Integer.parseInt(idx[0]), slideCount);
                    int endIdx = Math.min(Integer.parseInt(idx[1]), slideCount);
                    for (int i=Math.max(startIdx,1); i<endIdx; i++) {
                        slideIdx.add(i-1);
                    }
                    break;
                }
                }
            }
        }
        return slideIdx;
    }
}
