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

import static org.junit.Assume.assumeFalse;

import java.io.File;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.poi.POIDataSamples;
import org.apache.poi.xslf.util.PPTX2PNG;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test class for testing PPTX2PNG utility which renders .ppt and .pptx slideshows
 */
@RunWith(Parameterized.class)
public class TestPPTX2PNG {
    private static boolean xslfOnly;
    private static final POIDataSamples samples = POIDataSamples.getSlideShowInstance();
    private static final File basedir = null;
    private static final String files =
        "53446.ppt, alterman_security.ppt, alterman_security.pptx, KEY02.pptx, themes.pptx, " +
        "backgrounds.pptx, layouts.pptx, sample.pptx, shapes.pptx, 54880_chinese.ppt, keyframes.pptx," +
        "customGeo.pptx, customGeo.ppt, wrench.emf, santa.wmf, missing-moveto.ppt";

    @BeforeClass
    public static void checkHslf() {
        try {
            Class.forName("org.apache.poi.hslf.usermodel.HSLFSlideShow");
        } catch (Exception e) {
            xslfOnly = true;
        }
    }

    // use filename instead of File object to omit full pathname in test name
    @Parameter
    public String pptFile;

    @SuppressWarnings("ConstantConditions")
    @Parameters(name="{0}")
    public static Collection<String> data() {
        Function<String, Stream<String>> fun = (basedir == null) ? Stream::of :
            (f) -> Stream.of(basedir.listFiles(p -> p.getName().matches(f))).map(File::getName);

        return Stream.of(files.split(", ?")).flatMap(fun).collect(Collectors.toList());
    }

    @Test
    public void render() throws Exception {
        assumeFalse("ignore HSLF (.ppt) / HEMF (.emf) / HWMF (.wmf) files in no-scratchpad run", xslfOnly && pptFile.matches(".*\\.(ppt|emf|wmf)$"));

        String[] args = {
            "-format", "null", // png,gif,jpg,svg or null for test
            "-slide", "-1", // -1 for all
            "-outdir", new File("build/tmp/").getCanonicalPath(),
            "-outpat", "${basename}-${slideno}-${ext}.${format}",
            // "-dump", new File("build/tmp/", pptFile+".json").getCanonicalPath(),
            "-dump", "null",
            "-quiet",
            "-fixside", "long",
            "-scale", "800",
            // "-scale", "1.333333333",
            (basedir == null ? samples.getFile(pptFile) : new File(basedir, pptFile)).getAbsolutePath()
        };
        PPTX2PNG.main(args);
    }
}
