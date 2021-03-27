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
package org.apache.poi.ooxml;

import org.apache.poi.POIDataSamples;
import org.apache.poi.extractor.ExtractorFactory;
import org.apache.poi.extractor.POITextExtractor;
import org.apache.poi.sl.draw.Drawable;
import org.apache.poi.sl.usermodel.Slide;
import org.apache.poi.sl.usermodel.SlideShow;
import org.apache.poi.ss.extractor.EmbeddedData;
import org.apache.poi.ss.extractor.EmbeddedExtractor;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.commons.util.ExceptionUtils;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test to trigger code-execution of various parts so
 * that all required elements are inclueded in the ooxml-schema-lite package
 */
class TestTriggerCoverage {
    private static final Set<String> FAILING = new HashSet<>();
    static {
        FAILING.add("stress025.docx");
    }

    public static Stream<Arguments> files() {
        String dataDirName = System.getProperty(POIDataSamples.TEST_PROPERTY);
        if(dataDirName == null) {
            dataDirName = "test-data";
        }

        List<Arguments> files = new ArrayList<>();
        findFile(files, dataDirName + "/integration");

        return files.stream();
    }

    private static void findFile(List<Arguments> list, String dir) {
        String[] files = new File(dir).list();
        assertNotNull(files, "Did not find any files in directory " + dir);

        for(String file : files) {
            list.add(Arguments.of(new File(dir, file)));
        }
    }

    @ParameterizedTest
    @MethodSource("files")
    void testFile(File file) throws Exception {
        try (InputStream stream = new FileInputStream(file)) {
            if (file.getName().endsWith(".docx")) {
                try (XWPFDocument doc = new XWPFDocument(stream)) {
                    assertNotNull(doc);
                }
            } else if (file.getName().endsWith(".xlsx")) {
                try (XSSFWorkbook xls = new XSSFWorkbook(stream)) {
                    assertNotNull(xls);
                    extractEmbedded(xls);
                }
            } else if (file.getName().endsWith(".pptx")) {
                try (XMLSlideShow ppt = new XMLSlideShow(stream)) {
                    assertNotNull(ppt);
                    renderSlides(ppt);
                }
            } else {
                throw new IllegalArgumentException("Don't know how to handle file " + file);
            }
        } catch (Exception e) {
            Assumptions.assumeFalse(FAILING.contains(file.getName()),
                    "File " + file + " is expected to fail");

            throw e;
        }

        try (InputStream stream = new FileInputStream(file)) {
            try (POITextExtractor extractor = ExtractorFactory.createExtractor(stream)) {
                assertNotNull(extractor.getText());
            }
        }
    }

    private void extractEmbedded(Workbook wb) throws IOException {
        EmbeddedExtractor ee = new EmbeddedExtractor();

        for (Sheet s : wb) {
            for (EmbeddedData ed : ee.extractAll(s)) {
                assertNotNull(ed.getFilename());
                assertNotNull(ed.getEmbeddedData());
                assertNotNull(ed.getShape());
            }
        }
    }

    private void renderSlides(SlideShow<?,?> ss) {
        Dimension pgSize = ss.getPageSize();

        for (Slide<?,?> s : ss.getSlides()) {
            BufferedImage img = new BufferedImage(pgSize.width, pgSize.height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = img.createGraphics();

            // default rendering options
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            graphics.setRenderingHint(Drawable.BUFFERED_IMAGE, new WeakReference<>(img));

            try {
                // draw stuff
                s.draw(graphics);
            } catch (ArrayIndexOutOfBoundsException e) {
                // We saw exceptions with JDK 8 on Windows in the Jenkins CI which
                // seem to only be triggered by some font (maybe Calibri?!)
                // We cannot avoid this, so let's try to not make the tests fail in this case
                Assumptions.assumeFalse(
                        e.getMessage().equals("-1") &&
                                ExceptionUtils.readStackTrace(e).contains("ExtendedTextSourceLabel.getJustificationInfos"),
                        "JDK sometimes fails at this point on some fonts on Windows machines, but we " +
                                "should not fail the build because of this: " + ExceptionUtils.readStackTrace(e));

                throw e;
            }

            graphics.dispose();
            img.flush();
        }
    }
}
