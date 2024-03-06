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

package org.apache.poi.xslf.usermodel;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.POIDataSamples;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.xslf.util.PPTX2PNG;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.internal.util.io.IOUtil;

/**
 * Test class for testing PPTX2PNG utility which renders .ppt and .pptx slideshows
 */
@SuppressWarnings("ConstantConditions")
class TestPPTX2PNG {
    private static Closeable archive;
    private static boolean xslfOnly;
    private static final POIDataSamples samples = POIDataSamples.getSlideShowInstance();

    private static final Pattern XSLF_EXT = Pattern.compile("(?i).*\\.pptx$");
    private static final Pattern ALL_EXT = Pattern.compile("(?i).*\\.(ppt.?|emf|wmf)$");

    private static final File basedir = null;

    private static final String files =
        "bug64693.pptx, 53446.ppt, alterman_security.ppt, alterman_security.pptx, KEY02.pptx, themes.pptx, " +
        "backgrounds.pptx, layouts.pptx, sample.pptx, shapes.pptx, 54880_chinese.ppt, keyframes.pptx," +
        "customGeo.pptx, customGeo.ppt, wrench.emf, santa.wmf, missing-moveto.ppt, " +
        "64716_image1.wmf, 64716_image2.wmf, 64716_image3.wmf, 54542_cropped_bitmap.pptx";

    private static final String svgFiles =
        "bug64693.pptx";

    private static final String pdfFiles =
        "alterman_security.ppt";

    private static InputStream defStdin;

    @BeforeAll
    public static void init() {
        try {
            Class.forName("org.apache.poi.hslf.usermodel.HSLFSlideShow");
        } catch (Exception e) {
            xslfOnly = true;
        }
        defStdin = System.in;
    }

    @AfterAll
    public static void resetStdin() {
        System.setIn(defStdin);
        IOUtil.closeQuietly(archive);
    }

    public static Stream<Arguments> data() throws IOException {
        // Junit closes all closable arguments after the usage
        // therefore we need to wrap the archive in non-closable arrays
        Stream<Arguments> result;
        if (basedir != null && basedir.getName().endsWith(".zip")) {
            ZipFile zipFile = new ZipFile(basedir);
            archive = zipFile;
            result = zipFile.stream().
                map(f -> Arguments.of(new File(f.getName()).getName(), f, new ZipFile[]{zipFile}));
        } else if (basedir != null && basedir.getName().endsWith(".7z")) {
            SevenZFile sevenZFile = SevenZFile.builder().setFile(basedir).get();
            archive = sevenZFile;
            result = ((ArrayList<SevenZArchiveEntry>)sevenZFile.getEntries()).stream().
                filter(f -> !f.isDirectory()).
                map(f -> Arguments.of(f.getName(), f, new SevenZFile[]{sevenZFile}));
        } else {
            result = Stream.of(files.split(", ?")).
                map(basedir == null ? samples::getFile : f -> new File(basedir, f)).
                map(f -> Arguments.of(f.getName(), f, f.getParentFile()));
        }

        return result.filter(args -> (xslfOnly ? XSLF_EXT : ALL_EXT).matcher((String)args.get()[0]).find());

    }

    // use filename instead of File object to omit full pathname in test name
    @ParameterizedTest(name = "{0} ({index})")
    @MethodSource("data")
    void render(String fileName, Object fileObj, Object fileContainer) throws Exception {
        PPTX2PNG.main(getArgs(fileName, fileObj, fileContainer, "null"));
        if (svgFiles.contains(fileName)) {
            PPTX2PNG.main(getArgs(fileName, fileObj, fileContainer, "svg"));
        }
        if (pdfFiles.contains(fileName)) {
            PPTX2PNG.main(getArgs(fileName, fileObj, fileContainer, "pdf"));
        }
        if (System.in != defStdin) {
            System.in.close();
        }
    }

    private String[] getArgs(String fileName, Object fileObj, Object fileContainer, String format) throws IOException {
        File tmpDir = new File("build/tmp/");

        // fix maven build errors
        if (!tmpDir.exists()) {
            assertTrue(tmpDir.mkdirs());
        }

        final List<String> args = new ArrayList<>(asList(
                "-format", format, // png,gif,jpg,svg,pdf or null for test
                "-slide", "-1", // -1 for all
                "-outdir", tmpDir.getCanonicalPath(),
                // "-dump", new File("build/tmp/", fileName+".json").getCanonicalPath(),
                "-dump", "null",
                "-quiet",
                "-ignoreParse",
                // "-charset", "GBK",
                // "-emfHeaderBounds",
                // "-textAsShapes",
                // "-extractEmbedded",
                "-fixside", "long",
                "-scale", "800"
        ));

        String lName = fileName.toLowerCase(Locale.ROOT);
        FileMagic inputType = null;
        if (lName.endsWith(".emf")) {
            inputType = FileMagic.EMF;
        } else if (lName.endsWith(".wmf")) {
            inputType = FileMagic.WMF;
        }

        if (inputType != null) {
            args.add("-inputtype");
            args.add(inputType.toString());
        }

        if (fileName.endsWith("bug64693.pptx")) {
            args.add("-charset");
            args.add("GBK");
        }

        final String basename = FilenameUtils.getBaseName(fileName);
        final String ext = FilenameUtils.getExtension(fileName);

        if (fileObj instanceof ZipEntry) {
            ZipEntry ze = (ZipEntry)fileObj;
            ZipFile zf = ((ZipFile[])fileContainer)[0];
            System.setIn(zf.getInputStream(ze));
            args.add("-outpat");
            args.add(basename+"-${slideno}-"+ext+".${format}");
            args.add("stdin");
        } else if (fileObj instanceof SevenZArchiveEntry) {
            SevenZArchiveEntry ze = (SevenZArchiveEntry)fileObj;
            SevenZFile zf = ((SevenZFile[])fileContainer)[0];
            System.setIn(zf.getInputStream(ze));
            args.add("-outpat");
            args.add(basename+"-${slideno}-"+ext+".${format}");
            args.add("stdin");
        } else if (fileObj instanceof File) {
            args.add("-outpat");
            args.add("${basename}-${slideno}-${ext}.${format}");
            args.add(((File)fileObj).getAbsolutePath());
        }

        return args.toArray(new String[0]);
    }
}
