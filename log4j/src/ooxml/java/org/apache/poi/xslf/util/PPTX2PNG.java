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

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Dimension2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.poi.common.usermodel.GenericRecord;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.sl.draw.Drawable;
import org.apache.poi.sl.draw.EmbeddedExtractor.EmbeddedPart;
import org.apache.poi.util.Dimension2DDouble;
import org.apache.poi.util.GenericRecordJsonWriter;
import org.apache.poi.util.LocaleUtil;

/**
 * An utility to convert slides of a .pptx slide show to a PNG image
 */
public final class PPTX2PNG {

    private static final String INPUT_PAT_REGEX =
        "(?<slideno>[^|]+)\\|(?<format>[^|]+)\\|(?<basename>.+)\\.(?<ext>[^.]++)";

    private static final Pattern INPUT_PATTERN = Pattern.compile(INPUT_PAT_REGEX);

    private static final String OUTPUT_PAT_REGEX = "${basename}-${slideno}.${format}";

    private static void usage(String error){
        String msg =
            "Usage: PPTX2PNG [options] <.ppt/.pptx/.emf/.wmf file or 'stdin'>\n" +
            (error == null ? "" : ("Error: "+error+"\n")) +
            "Options:\n" +
            "    -scale <float>    scale factor\n" +
            "    -fixSide <side>   specify side (long,short,width,height) to fix - use <scale> as amount of pixels\n" +
            "    -slide <integer>  1-based index of a slide to render\n" +
            "    -format <type>    png,gif,jpg,svg,pdf (,log,null for testing)\n" +
            "    -outdir <dir>     output directory, defaults to origin of the ppt/pptx file\n" +
            "    -outfile <file>   output filename, defaults to '"+OUTPUT_PAT_REGEX+"'\n" +
            "    -outpat <pattern> output filename pattern, defaults to '"+OUTPUT_PAT_REGEX+"'\n" +
            "                      patterns: basename, slideno, format, ext\n" +
            "    -dump <file>      dump the annotated records to a file\n" +
            "    -quiet            do not write to console (for normal processing)\n" +
            "    -ignoreParse      ignore parsing error and continue with the records read until the error\n" +
            "    -extractEmbedded  extract embedded parts\n" +
            "    -inputType <type> default input file type (OLE2,WMF,EMF), default is OLE2 = Powerpoint\n" +
            "                      some files (usually wmf) don't have a header, i.e. an identifiable file magic\n" +
            "    -textAsShapes     text elements are saved as shapes in SVG, necessary for variable spacing\n" +
            "                      often found in math formulas\n" +
            "    -charset <cs>     sets the default charset to be used, defaults to Windows-1252\n" +
            "    -emfHeaderBounds  force the usage of the emf header bounds to calculate the bounding box\n" +
            "    -fontdir <dir>    (PDF only) font directories separated by \";\" - use $HOME for current users home dir\n" +
            "                      defaults to the usual plattform directories\n" +
            "    -fontTtf <regex>  (PDF only) regex to match the .ttf filenames\n" +
            "    -fontMap <map>    \";\"-separated list of font mappings <typeface from>:<typeface to>";

        System.out.println(msg);
        // no System.exit here, as we also run in junit tests!
    }

    public static void main(String[] args) throws Exception {
        PPTX2PNG p2p = new PPTX2PNG();

        if (p2p.parseCommandLine(args)) {
            p2p.processFile();
        }
    }

    private String slidenumStr = "-1";
    private float scale = 1;
    private File file = null;
    private String format = "png";
    private File outdir = null;
    private String outfile = null;
    private boolean quiet = false;
    private String outPattern = OUTPUT_PAT_REGEX;
    private File dumpfile = null;
    private String fixSide = "scale";
    private boolean ignoreParse = false;
    private boolean extractEmbedded = false;
    private FileMagic defaultFileType = FileMagic.OLE2;
    private boolean textAsShapes = false;
    private Charset charset = LocaleUtil.CHARSET_1252;
    private boolean emfHeaderBounds = false;
    private String fontDir = null;
    private String fontTtf = null;
    private String fontMap = null;

    private PPTX2PNG() {
    }

    private boolean parseCommandLine(String[] args) {
        if (args.length == 0) {
            usage(null);
            return false;
        }

        for (int i = 0; i < args.length; i++) {
            String opt = (i+1 < args.length) ? args[i+1] : null;
            switch (args[i].toLowerCase(Locale.ROOT)) {
                case "-scale":
                    if (opt != null) {
                        scale = Float.parseFloat(opt);
                        i++;
                    }
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
                    if (opt != null) {
                        outdir = new File(opt);
                        i++;
                    }
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
                    if (opt != null) {
                        dumpfile = new File(opt);
                        i++;
                    } else {
                        dumpfile = new File("pptx2png.dump");
                    }
                    break;
                case "-fixside":
                    if (opt != null) {
                        fixSide = opt.toLowerCase(Locale.ROOT);
                        i++;
                    } else {
                        fixSide = "long";
                    }
                    break;
                case "-inputtype":
                    if (opt != null) {
                        defaultFileType = FileMagic.valueOf(opt);
                        i++;
                    } else {
                        defaultFileType = FileMagic.OLE2;
                    }
                    break;
                case "-textasshapes":
                    textAsShapes = true;
                    break;
                case "-ignoreparse":
                    ignoreParse = true;
                    break;
                case "-extractembedded":
                    extractEmbedded = true;
                    break;
                case "-charset":
                    if (opt != null) {
                        charset = Charset.forName(opt);
                        i++;
                    } else {
                        charset = LocaleUtil.CHARSET_1252;
                    }
                    break;
                case "-emfheaderbounds":
                    emfHeaderBounds = true;
                    break;
                case "-fontdir":
                    if (opt != null) {
                        fontDir = opt;
                        i++;
                    } else {
                        fontDir = null;
                    }
                    break;
                case "-fontttf":
                    if (opt != null) {
                        fontTtf = opt;
                        i++;
                    } else {
                        fontTtf = null;
                    }
                    break;
                case "-fontmap":
                    if (opt != null) {
                        fontMap = opt;
                        i++;
                    }  else {
                        fontMap = null;
                    }
                    break;
                default:
                    file = new File(args[i]);
                    break;
            }
        }

        final boolean isStdin = file != null && "stdin".equalsIgnoreCase(file.getName());

        if (!isStdin && (file == null || !file.exists())) {
            usage("File not specified or it doesn't exist");
            return false;
        }

        if (format == null || !format.matches("^(png|gif|jpg|null|svg|pdf|log)$")) {
            usage("Invalid format given");
            return false;
        }

        if (outdir == null) {
            if (isStdin) {
                usage("When reading from STDIN, you need to specify an outdir.");
                return false;
            } else {
                outdir = file.getAbsoluteFile().getParentFile();
            }
        }
        if (!outdir.exists()) {
            usage("Outdir doesn't exist");
            return false;
        }

        if (!"null".equals(format) && (outdir == null || !outdir.exists() || !outdir.isDirectory())) {
            usage("Output directory doesn't exist");
            return false;
        }

        if (scale < 0) {
            usage("Invalid scale given");
            return false;
        }

        if (!"long,short,width,height,scale".contains(fixSide)) {
            usage("<fixside> must be one of long / short / width / height");
            return false;
        }

        return true;
    }

    private void processFile() throws IOException {
        if (!quiet) {
            System.out.println("Processing " + file);
        }



        try (MFProxy proxy = initProxy(file)) {
            final Set<Integer> slidenum = proxy.slideIndexes(slidenumStr);
            if (slidenum.isEmpty()) {
                usage("slidenum must be either -1 (for all) or within range: [1.." + proxy.getSlideCount() + "] for " + file);
                return;
            }

            final Dimension2D dim = new Dimension2DDouble();
            final double lenSide = getDimensions(proxy, dim);
            final int width = Math.max((int)Math.rint(dim.getWidth()),1);
            final int height = Math.max((int)Math.rint(dim.getHeight()),1);

            try (OutputFormat outputFormat = getOutput()) {
                for (int slideNo : slidenum) {
                    proxy.setSlideNo(slideNo);
                    if (!quiet) {
                        String title = proxy.getTitle();
                        System.out.println("Rendering slide " + slideNo + (title == null ? "" : ": " + title.trim()));
                    }

                    dumpRecords(proxy);

                    extractEmbedded(proxy, slideNo);

                    Graphics2D graphics = outputFormat.addSlide(width, height);

                    // default rendering options
                    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    graphics.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
                    graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                    graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
                    graphics.setRenderingHint(Drawable.DEFAULT_CHARSET, getDefaultCharset());
                    graphics.setRenderingHint(Drawable.EMF_FORCE_HEADER_BOUNDS, emfHeaderBounds);
                    if (fontMap != null) {
                        Map<String,String> fmap = Arrays.stream(fontMap.split(";"))
                            .map(s -> s.split(":"))
                            .collect(Collectors.toMap(s -> s[0], s -> s[1]));
                        graphics.setRenderingHint(Drawable.FONT_MAP, fmap);
                    }

                    graphics.scale(scale / lenSide, scale / lenSide);

                    graphics.setComposite(AlphaComposite.Clear);
                    graphics.fillRect(0, 0, width, height);
                    graphics.setComposite(AlphaComposite.SrcOver);

                    // draw stuff
                    proxy.draw(graphics);

                    outputFormat.writeSlide(proxy, new File(outdir, calcOutFile(proxy, slideNo)));
                }

                outputFormat.writeDocument(proxy, new File(outdir, calcOutFile(proxy, 0)));
            }

        } catch (NoScratchpadException e) {
            usage("'"+file.getName()+"': Format not supported - try to include poi-scratchpad.jar into the CLASSPATH.");
            return;
        }

        if (!quiet) {
            System.out.println("Done");
        }
    }

    private OutputFormat getOutput() {
        switch (format) {
            case "svg":
                return new SVGFormat(textAsShapes);
            case "pdf":
                return new PDFFormat(textAsShapes,fontDir,fontTtf);
            case "log":
                return new DummyFormat();
            default:
                return new BitmapFormat(format);
        }
    }

    private double getDimensions(MFProxy proxy, Dimension2D dim) {
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

        dim.setSize(pgsize.getWidth() * scale / lenSide, pgsize.getHeight() * scale / lenSide);
        return lenSide;
    }

    private void dumpRecords(MFProxy proxy) throws IOException {
        if (dumpfile == null || "null".equals(dumpfile.getPath())) {
            return;
        }
        GenericRecord gr = proxy.getRoot();
        try (GenericRecordJsonWriter fw = new GenericRecordJsonWriter(dumpfile) {
            protected boolean printBytes(String name, Object o) {
                return false;
            }
        }) {
            if (gr == null) {
                fw.writeError(file.getName()+" doesn't support GenericRecord interface and can't be dumped to a file.");
            } else {
                fw.write(gr);
            }
        }
    }

    private void extractEmbedded(MFProxy proxy, int slideNo) throws IOException {
        if (!extractEmbedded) {
            return;
        }
        for (EmbeddedPart ep : proxy.getEmbeddings(slideNo)) {
            String filename = ep.getName();
            // do some sanitizing for creative filenames ...
            filename = new File(filename == null ? "dummy.dat" : filename).getName();
            filename = calcOutFile(proxy, slideNo).replaceFirst("\\.\\w+$", "")+"_"+filename;
            try (FileOutputStream fos = new FileOutputStream(new File(outdir, filename))) {
                fos.write(ep.getData().get());
            }
        }
    }

    private interface ProxyConsumer {
        void parse(MFProxy proxy) throws IOException;
    }

    @SuppressWarnings({"resource", "squid:S2095"})
    private MFProxy initProxy(File file) throws IOException {
        MFProxy proxy;
        final String fileName = file.getName().toLowerCase(Locale.ROOT);
        FileMagic fm;
        ProxyConsumer con;
        if ("stdin".equals(fileName)) {
            InputStream bis = FileMagic.prepareToCheckMagic(System.in);
            fm = FileMagic.valueOf(bis);
            con = (p) -> p.parse(bis);
        } else {
            fm = FileMagic.valueOf(file);
            con = (p) -> p.parse(file);
        }

        if (fm == FileMagic.UNKNOWN) {
            fm = defaultFileType;
        }
        switch (fm) {
            case EMF:
                proxy = new EMFHandler();
                break;
            case WMF:
                proxy = new WMFHandler();
                break;
            default:
                proxy = new PPTHandler();
                break;
        }
        proxy.setIgnoreParse(ignoreParse);
        proxy.setQuite(quiet);
        con.parse(proxy);
        proxy.setDefaultCharset(charset);

        return proxy;
    }

    private String calcOutFile(MFProxy proxy, int slideNo) {
        if (outfile != null) {
            return outfile;
        }
        String inname = String.format(Locale.ROOT, "%04d|%s|%s", slideNo, format, file.getName());
        String outpat = (proxy.getSlideCount() > 1 && slideNo > 0 ? outPattern : outPattern.replaceAll("-?\\$\\{slideno}", ""));
        return INPUT_PATTERN.matcher(inname).replaceAll(outpat);
    }

    private Charset getDefaultCharset() {
        return charset;
    }

    static class NoScratchpadException extends IOException {
        NoScratchpadException() {
        }

        NoScratchpadException(Throwable cause) {
            super(cause);
        }
    }
}
