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

import static java.util.Spliterator.NONNULL;
import static java.util.Spliterator.ORDERED;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators.AbstractSpliterator;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.imageio.ImageIO;

import org.apache.poi.common.usermodel.GenericRecord;
import org.apache.poi.sl.draw.BitmapImageRenderer;
import org.apache.poi.sl.draw.DrawPictureShape;
import org.apache.poi.sl.draw.Drawable;
import org.apache.poi.sl.draw.ImageRenderer;
import org.apache.poi.sl.usermodel.PictureData;
import org.apache.poi.sl.usermodel.Slide;
import org.apache.poi.sl.usermodel.SlideShow;
import org.apache.poi.sl.usermodel.SlideShowFactory;
import org.apache.poi.util.GenericRecordJsonWriter;

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
            "    -fixSide <side>   specify side (long,short,width,height) to fix - use <scale> as amount of pixels\n" +
            "    -slide <integer>  1-based index of a slide to render\n" +
            "    -format <type>    png,gif,jpg (,null for testing)\n" +
            "    -outdir <dir>     output directory, defaults to origin of the ppt/pptx file\n" +
            "    -outfile <file>   output filename, defaults to '"+OUTPUT_PAT_REGEX+"'\n" +
            "    -outpat <pattern> output filename pattern, defaults to '"+OUTPUT_PAT_REGEX+"'\n" +
            "                      patterns: basename, slideno, format, ext\n" +
            "    -dump <file>      dump the annotated records to a file\n" +
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
        String outPattern = OUTPUT_PAT_REGEX;
        File dumpfile = null;
        String fixSide = "scale";


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
                    outPattern = opt;
                    i++;
                    break;
                case "-quiet":
                    quiet = true;
                    break;
                case "-dump":
                    dumpfile = new File(opt);
                    i++;
                    break;
                case "-fixside":
                    fixSide = opt.toLowerCase(Locale.ROOT);
                    i++;
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

        if (!"long,short,width,height,scale".contains(fixSide)) {
            usage("<fixside> must be one of long / short / width / height");
            return;
        }

        if (!quiet) {
            System.out.println("Processing " + file);
        }


        try (MFProxy proxy = initProxy(file)) {
            final Set<Integer> slidenum = proxy.slideIndexes(slidenumStr);
            if (slidenum.isEmpty()) {
                usage("slidenum must be either -1 (for all) or within range: [1.." + proxy.getSlideCount() + "] for " + file);
                return;
            }

            final Dimension2D pgsize = proxy.getSize();

            final double lenSide;
            switch (fixSide) {
                default:
                case "scale":
                    lenSide = 1;
                    break;
                case "long":
                    lenSide = Math.max(pgsize.getWidth(), pgsize.getHeight());
                    break;
                case "short":
                    lenSide = Math.min(pgsize.getWidth(), pgsize.getHeight());
                    break;
                case "width":
                    lenSide = pgsize.getWidth();
                    break;
                case "height":
                    lenSide = pgsize.getHeight();
                    break;
            }

            final int width = (int) Math.rint(pgsize.getWidth() * scale / lenSide);
            final int height = (int) Math.rint(pgsize.getHeight() * scale / lenSide);

            for (int slideNo : slidenum) {
                proxy.setSlideNo(slideNo);
                if (!quiet) {
                    String title = proxy.getTitle();
                    System.out.println("Rendering slide " + slideNo + (title == null ? "" : ": " + title.trim()));
                }

                GenericRecord gr = proxy.getRoot();
                if (dumpfile != null) {
                    try (GenericRecordJsonWriter fw = new GenericRecordJsonWriter(dumpfile)) {
                        if (gr == null) {
                            fw.writeError(file.getName()+" doesn't support GenericRecord interface and can't be dumped to a file.");
                        } else {
                            fw.write(gr);
                        }
                    }
                }

                BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                Graphics2D graphics = img.createGraphics();

                // default rendering options
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                graphics.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
                graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
                graphics.setRenderingHint(Drawable.BUFFERED_IMAGE, new WeakReference<>(img));

                graphics.scale(scale / lenSide, scale / lenSide);

                graphics.setComposite(AlphaComposite.Clear);
                graphics.fillRect(0, 0, (int)width, (int)height);
                graphics.setComposite(AlphaComposite.SrcOver);

                // draw stuff
                proxy.draw(graphics);

                // save the result
                if (!"null".equals(format)) {
                    String inname = String.format(Locale.ROOT, "%04d|%s|%s", slideNo, format, file.getName());
                    String outpat = (proxy.getSlideCount() > 1 ? outPattern : outPattern.replaceAll("-?\\$\\{slideno\\}", ""));
                    String outname = (outfile != null) ? outfile : INPUT_PATTERN.matcher(inname).replaceAll(outpat);
                    ImageIO.write(img, format, new File(outdir, outname));
                }

                graphics.dispose();
                img.flush();
            }
        } catch (NoScratchpadException e) {
            usage("'"+file.getName()+"': Format not supported - try to include poi-scratchpad.jar into the CLASSPATH.");
            return;
        }

        if (!quiet) {
            System.out.println("Done");
        }
    }

    private static MFProxy initProxy(File file) throws IOException {
        MFProxy proxy;
        final String fileName = file.getName().toLowerCase(Locale.ROOT);
        switch (fileName.contains(".") ? fileName.substring(fileName.lastIndexOf('.')) : "") {
            case ".emf":
                proxy = new EMFHandler();
                break;
            case ".wmf":
                proxy = new WMFHandler();
                break;
            default:
                proxy = new PPTHandler();
                break;
        }

        proxy.parse(file);
        return proxy;
    }

    private interface MFProxy extends Closeable {
        void parse(File file) throws IOException;
//        Iterable<HwmfEmbedded> getEmbeddings();
        Dimension2D getSize();

        default void setSlideNo(int slideNo) {}

        String getTitle();
        void draw(Graphics2D ctx);

        default int getSlideCount() { return 1; }

        default Set<Integer> slideIndexes(String range) {
            return Collections.singleton(1);
        }

        GenericRecord getRoot();
    }

    /** Handler for ppt and pptx files */
    private static class PPTHandler implements MFProxy {
        SlideShow<?,?> ppt;
        Slide<?,?> slide;

        @Override
        public void parse(File file) throws IOException {
            try {
                ppt = SlideShowFactory.create(file, null, true);
            } catch (IOException e) {
                if (e.getMessage().contains("scratchpad")) {
                    throw new NoScratchpadException(e);
                } else {
                    throw e;
                }
            }
            slide = ppt.getSlides().get(0);
        }

        @Override
        public Dimension2D getSize() {
            return ppt.getPageSize();
        }

        @Override
        public int getSlideCount() {
            return ppt.getSlides().size();
        }

        @Override
        public void setSlideNo(int slideNo) {
            slide = ppt.getSlides().get(slideNo-1);
        }

        @Override
        public String getTitle() {
            return slide.getTitle();
        }

        private static final String RANGE_PATTERN = "(^|,)(?<from>\\d+)?(-(?<to>\\d+))?";

        @Override
        public Set<Integer> slideIndexes(String range) {
            final Matcher matcher = Pattern.compile(RANGE_PATTERN).matcher(range);
            Spliterator<Matcher> sp = new AbstractSpliterator<Matcher>(range.length(), ORDERED|NONNULL){
                @Override
                public boolean tryAdvance(Consumer<? super Matcher> action) {
                    boolean b = matcher.find();
                    if (b) {
                        action.accept(matcher);
                    }
                    return b;
                }
            };

            return StreamSupport.stream(sp, false).
                flatMap(this::range).
                collect(Collectors.toCollection(TreeSet::new));
        }

        @Override
        public void draw(Graphics2D ctx) {
            slide.draw(ctx);
        }

        @Override
        public void close() throws IOException {
            if (ppt != null) {
                ppt.close();
            }
        }

        @Override
        public GenericRecord getRoot() {
            return (ppt instanceof GenericRecord) ? (GenericRecord)ppt : null;
        }

        private Stream<Integer> range(Matcher m) {
            final int slideCount = ppt.getSlides().size();
            String fromStr = m.group("from");
            String toStr = m.group("to");
            int from = (fromStr == null || fromStr.isEmpty() ? 1 : Integer.parseInt(fromStr));
            int to = (toStr == null) ? from
                : (toStr.isEmpty() || ((fromStr == null || fromStr.isEmpty()) && "1".equals(toStr))) ? slideCount
                : Integer.parseInt(toStr);
            return IntStream.rangeClosed(from, to).filter(i -> i <= slideCount).boxed();
        }
    }

    private static class EMFHandler implements MFProxy {
        private ImageRenderer imgr = null;
        private InputStream is;

        @Override
        public void parse(File file) throws IOException {
            imgr = DrawPictureShape.getImageRenderer(null, getContentType());
            if (imgr instanceof BitmapImageRenderer) {
                throw new NoScratchpadException();
            }

            // stream needs to be kept open
            is = file.toURI().toURL().openStream();
            imgr.loadImage(is, getContentType());
        }

        protected String getContentType() {
            return PictureData.PictureType.EMF.contentType;
        }

        @Override
        public Dimension2D getSize() {
            return imgr.getDimension();
        }

        @Override
        public String getTitle() {
            return "";
        }

        @Override
        public void draw(Graphics2D ctx) {
            Dimension2D dim = getSize();
            imgr.drawImage(ctx, new Rectangle2D.Double(0, 0, dim.getWidth(), dim.getHeight()));
        }

        @Override
        public void close() throws IOException {
            if (is != null) {
                try {
                    is.close();
                } finally {
                    is = null;
                }
            }
        }

        @Override
        public GenericRecord getRoot() {
            return imgr.getGenericRecord();
        }
    }

    private static class WMFHandler extends EMFHandler {
        @Override
        protected String getContentType() {
            return PictureData.PictureType.WMF.contentType;
        }
    }

    private static class NoScratchpadException extends IOException {
        public NoScratchpadException() {
        }

        public NoScratchpadException(Throwable cause) {
            super(cause);
        }
    }
}
