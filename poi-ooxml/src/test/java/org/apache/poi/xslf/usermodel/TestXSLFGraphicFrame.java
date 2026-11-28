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
package org.apache.poi.xslf.usermodel;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.junit.jupiter.api.Test;
import org.openxmlformats.schemas.presentationml.x2006.main.CTGraphicalObjectFrame;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.junit.jupiter.api.Assertions.*;
import static org.apache.poi.xslf.XSLFTestDataSamples.openSampleDocument;
import static org.apache.poi.xslf.XSLFTestDataSamples.writeOutAndReadBack;

class TestXSLFGraphicFrame {

    private static final Pattern CHART_PARTS = Pattern.compile("/ppt/(charts|embeddings)/.*");
    private static final Pattern DIAGRAM_PARTS = Pattern.compile("/ppt/(diagrams|media)/.*");

    /**
     * Removing the frame of a chart, an embedded object or a diagram also removes the parts
     * only it used, so the chart data, embedded documents, etc. aren't written out anymore.
     */
    @Test
    void testRemoveGraphicFrameRemovesParts() throws IOException {
        try (XMLSlideShow ppt = openSampleDocument("pie-chart.pptx")) {
            XSLFSlide slide = ppt.getSlides().get(0);
            assertTrue(slide.removeShape(findFrame(slide)));
            assertThat(ppt.getCharts(), empty());
            try (XMLSlideShow ppt2 = writeOutAndReadBack(ppt)) {
                assertThat(ppt2.getPackage().getPartsByName(CHART_PARTS), empty());
                assertThat(ppt2.getCharts(), empty());
            }
        }

        try (XMLSlideShow ppt = openSampleDocument("45545_Comment.pptx")) {
            XSLFSlide slide = ppt.getSlides().get(0);
            assertTrue(slide.removeShape(findFrame(slide)));
            try (XMLSlideShow ppt2 = writeOutAndReadBack(ppt)) {
                assertThat(ppt2.getPackage().getPartsByName(Pattern.compile("/ppt/embeddings/oleObject1.bin")), empty());
                // the objects of the other slides are kept
                assertEquals(2, ppt2.getPackage().getPartsByName(Pattern.compile("/ppt/embeddings/.*")).size());
            }
        }

        try (XMLSlideShow ppt = openSampleDocument("smartart-simple.pptx")) {
            XSLFSlide slide = ppt.getSlides().get(0);
            assertTrue(slide.removeShape(findFrame(slide)));
            try (XMLSlideShow ppt2 = writeOutAndReadBack(ppt)) {
                assertThat(ppt2.getPackage().getPartsByName(DIAGRAM_PARTS), empty());
                assertEquals(1, ppt2.getSlides().get(0).getRelations().size());
            }
        }
    }

    /**
     * A related part, which another shape of the sheet still uses, is kept
     */
    @Test
    void testRemoveGraphicFrameKeepsSharedParts() throws IOException {
        try (XMLSlideShow ppt1 = openSampleDocument("pie-chart.pptx")) {
            XSLFSlide slide1 = ppt1.getSlides().get(0);
            duplicateFrame(slide1);

            try (XMLSlideShow ppt = writeOutAndReadBack(ppt1)) {
                XSLFSlide slide = ppt.getSlides().get(0);
                assertTrue(slide.removeShape(findFrame(slide)));
                assertEquals(1, ppt.getCharts().size());
                assertNotNull(findFrame(slide).getChart());

                assertTrue(slide.removeShape(findFrame(slide)));
                assertThat(ppt.getCharts(), empty());
                try (XMLSlideShow ppt2 = writeOutAndReadBack(ppt)) {
                    assertThat(ppt2.getPackage().getPartsByName(CHART_PARTS), empty());
                }
            }
        }

        // two diagram frames sharing the diagram data also share its drawing, which the data
        // and not the frame refers to
        try (XMLSlideShow ppt1 = openSampleDocument("smartart-simple.pptx")) {
            XSLFSlide slide1 = ppt1.getSlides().get(0);
            duplicateFrame(slide1);

            try (XMLSlideShow ppt = writeOutAndReadBack(ppt1)) {
                XSLFSlide slide = ppt.getSlides().get(0);
                int diagramParts = ppt.getPackage().getPartsByName(DIAGRAM_PARTS).size();
                assertTrue(slide.removeShape(findFrame(slide)));
                XSLFDiagram diagram = (XSLFDiagram) findFrame(slide);
                assertNotNull(diagram.getDiagramDrawing());
                assertNotNull(slide.getRelationId(diagram.getDiagramDrawing()));
                try (XMLSlideShow ppt2 = writeOutAndReadBack(ppt)) {
                    assertEquals(diagramParts, ppt2.getPackage().getPartsByName(DIAGRAM_PARTS).size());
                    XSLFDiagram diagram2 = (XSLFDiagram) findFrame(ppt2.getSlides().get(0));
                    assertNotNull(diagram2.getDiagramDrawing());
                    assertFalse(diagram2.getGroupShape().getShapes().isEmpty());
                }

                assertTrue(slide.removeShape(findFrame(slide)));
                try (XMLSlideShow ppt2 = writeOutAndReadBack(ppt)) {
                    assertThat(ppt2.getPackage().getPartsByName(DIAGRAM_PARTS), empty());
                }
            }
        }
    }

    /**
     * A chart created after a chart was removed reuses the free part name, so the removed
     * chart must be gone from the slideshow's list by then
     */
    @Test
    void testCreateChartAfterRemoveGraphicFrame() throws IOException {
        try (XMLSlideShow ppt = openSampleDocument("pie-chart.pptx")) {
            XSLFSlide slide = ppt.getSlides().get(0);
            XSLFChart removed = findFrame(slide).getChart();
            assertTrue(slide.removeShape(findFrame(slide)));

            XSLFChart chart = ppt.createChart(slide);
            assertEquals(removed.getPackagePart().getPartName(), chart.getPackagePart().getPartName());
            assertEquals(List.of(chart), ppt.getCharts());
            try (XMLSlideShow ppt2 = writeOutAndReadBack(ppt)) {
                assertEquals(1, ppt2.getCharts().size());
                assertEquals(List.of(chart.getPackagePart().getPartName()),
                    ppt2.getPackage().getPartsByName(Pattern.compile("/ppt/charts/chart.*")).stream().map(PackagePart::getPartName).collect(Collectors.toList()));
            }
        }
    }

    /**
     * Removing a table also removes the relationships of the hyperlinks in its cells, which are
     * not relations to parts of the package, while the hyperlinks of the other shapes are kept
     */
    @Test
    void testRemoveTableRemovesHyperlinkRelationships() throws Exception {
        // the relationships of a new hyperlink are only known to the package part
        try (XMLSlideShow ppt = new XMLSlideShow()) {
            XSLFSlide slide = createSlideWithLinkedTable(ppt);
            assertEquals(List.of(TABLE_URL, TEXT_BOX_URL), hyperlinkTargets(slide));
            assertTrue(slide.removeShape(findFrame(slide)));
            assertEquals(List.of(TEXT_BOX_URL), hyperlinkTargets(slide));
            try (XMLSlideShow ppt2 = writeOutAndReadBack(ppt)) {
                assertEquals(List.of(TEXT_BOX_URL), hyperlinkTargets(ppt2.getSlides().get(0)));
            }
        }

        // the ones of a loaded slide are reference relationships of the slide
        try (XMLSlideShow ppt1 = new XMLSlideShow()) {
            createSlideWithLinkedTable(ppt1);
            try (XMLSlideShow ppt = writeOutAndReadBack(ppt1)) {
                XSLFSlide slide = ppt.getSlides().get(0);
                assertEquals(List.of(TABLE_URL, TEXT_BOX_URL), hyperlinkTargets(slide));
                assertEquals(2, slide.getReferenceRelationships().size());
                assertTrue(slide.removeShape(findFrame(slide)));
                assertEquals(List.of(TEXT_BOX_URL), hyperlinkTargets(slide));
                assertEquals(1, slide.getReferenceRelationships().size());
                try (XMLSlideShow ppt2 = writeOutAndReadBack(ppt)) {
                    assertEquals(List.of(TEXT_BOX_URL), hyperlinkTargets(ppt2.getSlides().get(0)));
                }
            }
        }
    }

    /**
     * An action hyperlink, e.g. to the next slide, has an empty relationship id, which is not
     * the id of a relation, so there is no need to scan the sheet for it when the shape is removed
     */
    @Test
    void testRelationIdsIgnoreActionHyperlinks() throws Exception {
        try (XMLSlideShow ppt = new XMLSlideShow()) {
            XSLFSlide slide = ppt.createSlide();
            XSLFTable table = slide.createTable(1, 2);
            table.getCell(0, 0).setText("next").createHyperlink().linkToNextSlide();
            assertThat(XSLFSheet.getRelationIds(table.getXmlObject()), empty());

            XSLFHyperlink link = table.getCell(0, 1).setText("url").createHyperlink();
            link.linkToUrl(TABLE_URL);
            assertEquals(Set.of(link.getXmlObject().getId()), XSLFSheet.getRelationIds(table.getXmlObject()));

            int relations = slide.getPackagePart().getRelationships().size();
            assertTrue(slide.removeShape(table));
            assertEquals(relations - 1, slide.getPackagePart().getRelationships().size());
        }
    }

    private static final String TABLE_URL = "https://poi.apache.org/table";
    private static final String TEXT_BOX_URL = "https://poi.apache.org/textbox";

    private static XSLFSlide createSlideWithLinkedTable(XMLSlideShow ppt) {
        XSLFSlide slide = ppt.createSlide();
        XSLFTable table = slide.createTable(1, 1);
        table.getCell(0, 0).setText("table").createHyperlink().linkToUrl(TABLE_URL);
        slide.createTextBox().setText("text box").createHyperlink().linkToUrl(TEXT_BOX_URL);
        return slide;
    }

    private static List<String> hyperlinkTargets(XSLFSlide slide) throws InvalidFormatException {
        List<String> targets = new ArrayList<>();
        for (PackageRelationship rel : slide.getPackagePart().getRelationshipsByType(XSLFRelation.HYPERLINK.getRelation())) {
            targets.add(rel.getTargetURI().toString());
        }
        Collections.sort(targets);
        return targets;
    }

    private static XSLFGraphicFrame findFrame(XSLFSlide slide) {
        return slide.getShapes().stream()
            .filter(XSLFGraphicFrame.class::isInstance)
            .map(XSLFGraphicFrame.class::cast)
            .findFirst().orElseThrow(AssertionError::new);
    }

    /**
     * Adds a copy of the first graphic frame, which refers to the same parts as the original
     */
    private static void duplicateFrame(XSLFSlide slide) {
        CTGraphicalObjectFrame frameXml = (CTGraphicalObjectFrame) findFrame(slide).getXmlObject();
        slide.getSpTree().addNewGraphicFrame().set(frameXml.copy());
    }

    @Test
    void testHasDiagram() throws IOException {
        try (XMLSlideShow ppt = openSampleDocument("SmartArt.pptx")) {
            XSLFSlide slide = ppt.getSlides().get(0);
            XSLFGraphicFrame gf = (XSLFGraphicFrame) slide.getShapes().get(0);

            assertTrue(gf.hasDiagram());
        }
    }
}