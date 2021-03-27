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
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.poi.POIDataSamples;
import org.apache.poi.xslf.util.PPTX2PNG;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Test class for testing PPTX2PNG utility which renders .ppt and .pptx slideshows
 */
@SuppressWarnings("ConstantConditions")
class TestPPTX2PNG {
    private static boolean xslfOnly;
    private static final POIDataSamples samples = POIDataSamples.getSlideShowInstance();
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

    @BeforeAll
    public static void checkHslf() {
        try {
            Class.forName("org.apache.poi.hslf.usermodel.HSLFSlideShow");
        } catch (Exception e) {
            xslfOnly = true;
        }
    }

    public static Stream<Arguments> data() {
        Function<String, Stream<Arguments>> fun = (basedir == null)
            ? (f) -> Stream.of(Arguments.of(f))
            : (f) -> Stream.of(basedir.listFiles(p -> p.getName().matches(f))).map(File::getName).map(Arguments::of);

        return Stream.of(files.split(", ?")).flatMap(fun);
    }

    // use filename instead of File object to omit full pathname in test name
    @ParameterizedTest
    @MethodSource("data")
    void render(String pptFile) throws Exception {
        assumeFalse(xslfOnly && pptFile.matches(".*\\.(ppt|emf|wmf)$"), "ignore HSLF (.ppt) / HEMF (.emf) / HWMF (.wmf) files in no-scratchpad run");
        PPTX2PNG.main(getArgs(pptFile, "null"));
        if (svgFiles.contains(pptFile)) {
            PPTX2PNG.main(getArgs(pptFile, "svg"));
        }
        if (pdfFiles.contains(pptFile)) {
            PPTX2PNG.main(getArgs(pptFile, "pdf"));
        }
    }

    private String[] getArgs(String pptFile, String format) throws IOException {
        File tmpDir = new File("build/tmp/");

        // fix maven build errors
        if (!tmpDir.exists()) {
            assertTrue(tmpDir.mkdirs());
        }

        final List<String> args = new ArrayList<>(asList(
                "-format", format, // png,gif,jpg,svg,pdf or null for test
                "-slide", "-1", // -1 for all
                "-outdir", tmpDir.getCanonicalPath(),
                "-outpat", "${basename}-${slideno}-${ext}.${format}",
                // "-dump", new File("build/tmp/", pptFile+".json").getCanonicalPath(),
                "-dump", "null",
                "-quiet",
                // "-charset", "GBK",
                // "-emfHeaderBounds",
                // "-textAsShapes",
                "-fixside", "long",
                "-scale", "800"
        ));

        if ("bug64693.pptx".equals(pptFile)) {
            args.addAll(asList(
                    "-charset", "GBK"
            ));
        }

        args.add((basedir == null ? samples.getFile(pptFile) : new File(basedir, pptFile)).getAbsolutePath());

        return args.toArray(new String[0]);
    }
}
