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
import org.apache.poi.openxml4j.opc.ContentTypes;
import org.apache.poi.sl.usermodel.ColorStyle;
import org.apache.poi.sl.usermodel.PaintStyle;
import org.apache.poi.sl.usermodel.TextParagraph.TextAlign;
import org.apache.poi.util.LocaleUtil;
import org.apache.poi.xslf.XSLFTestDataSamples;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class TestXSLFDiagram {

    private static final String SIMPLE_DIAGRAM = "smartart-simple.pptx";
    private static final String ROTATED_TEXT_DIAGRAM = "smartart-rotated-text.pptx";

    private static List<XSLFDiagram> extractDiagrams(XMLSlideShow slideShow) {
        return slideShow.getSlides()
                .stream()
                .flatMap(s -> extractDiagrams(s).stream())
                .collect(Collectors.toList());
    }

    private static List<XSLFDiagram> extractDiagrams(XSLFSlide slide) {
        return slide.getShapes()
                .stream()
                .filter(s -> s instanceof XSLFDiagram)
                .map(s -> (XSLFDiagram) s)
                .collect(Collectors.toList());
    }

    private static String colorToHex(Color color) {
        return String.format(LocaleUtil.getUserLocale(), "#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    private static Color hexToColor(String hex) {
        return Color.decode(hex);
    }

    @Test
    public void testHasDiagram() throws IOException {
        try (XMLSlideShow inputPptx = XSLFTestDataSamples.openSampleDocument(SIMPLE_DIAGRAM)) {
            List<XSLFDiagram> diagrams = extractDiagrams(inputPptx);
            assertEquals(1, diagrams.size());

            XSLFDiagram diagram = diagrams.get(0);
            assertTrue(diagram.hasDiagram());
        }
    }

    @Test
    public void testHasDiagramReadOnlyFile() throws IOException, InvalidFormatException {
        try (XMLSlideShow inputPptx = XSLFTestDataSamples.openSampleDocumentReadOnly(SIMPLE_DIAGRAM)) {
            List<XSLFDiagram> diagrams = extractDiagrams(inputPptx);
            assertEquals(1, diagrams.size());

            XSLFDiagram diagram = diagrams.get(0);
            assertTrue(diagram.hasDiagram());
        }
    }

    @Test
    public void testDiagramContainsShapes() throws IOException {
        try (XMLSlideShow inputPptx = XSLFTestDataSamples.openSampleDocument(SIMPLE_DIAGRAM)) {
            List<XSLFDiagram> diagrams = extractDiagrams(inputPptx);
            assertEquals(1, diagrams.size());

            XSLFDiagram diagram = diagrams.get(0);
            XSLFGroupShape groupShape = diagram.getGroupShape();
            assertNotNull(groupShape);

            // The Group gets the same positioning as the SmartArt. This can be much wider/taller than the content inside.
            assertEquals(groupShape.getAnchor().getWidth(), 113.375, 1E-4);
            assertEquals(groupShape.getAnchor().getHeight(), 74, 1E-4);
            assertEquals(groupShape.getAnchor().getX(), -16.75, 1E-4);
            assertEquals(groupShape.getAnchor().getY(), 5.5, 1E-4);

            List<XSLFShape> shapes = groupShape.getShapes();
            // 4 shapes, 3 text boxes, one shape does not have any text inside it
            assertEquals(7, shapes.size());

            // Shape 1 - Yellow Circle - "abc" center aligned
            String accent4Hex = "#ffc000"; // yellow
            XSLFAutoShape yellowCircle = (XSLFAutoShape) shapes.get(0);
            assertTrue(yellowCircle.getText().isEmpty());
            assertEquals(accent4Hex, colorToHex(yellowCircle.getFillColor()));

            XSLFTextBox yellowCircleText = (XSLFTextBox) shapes.get(1);
            assertEquals(yellowCircleText.getText(), "abc");
            assertEquals(TextAlign.CENTER, yellowCircleText.getTextParagraphs().get(0).getTextAlign());

            // Shape 2 - Gradient Blue & Purple - "def" left aligned
            XSLFAutoShape gradientCircle = (XSLFAutoShape) shapes.get(2);
            assertTrue(gradientCircle.getFillPaint() instanceof PaintStyle.GradientPaint);
            assertTrue(gradientCircle.getText().isEmpty());

            XSLFTextBox gradientCircleText = (XSLFTextBox) shapes.get(3);
            assertEquals(gradientCircleText.getText(), "def");
            // Even with left justification, the text is rendered on the right side of the circle because SmartArt defines
            // a better visual placement for the textbox inside the txXfrm property.
            assertEquals(1, gradientCircleText.getTextParagraphs().size());
            XSLFTextParagraph paragraph = gradientCircleText.getTextParagraphs().get(0);
            assertEquals(TextAlign.LEFT, paragraph.getTextAlign());
            assertEquals(1, paragraph.getTextRuns().size());
            XSLFTextRun textRun = paragraph.getTextRuns().get(0);
            assertTrue(textRun.isBold());
            assertTrue(textRun.isItalic());

            // Shape 3 - Green Circle with theme color - "ghi" right aligned
            XSLFAutoShape greenCircle = (XSLFAutoShape) shapes.get(4);
            ColorStyle greenCircleColorStyle = ((PaintStyle.SolidPaint) greenCircle.getFillPaint()).getSolidColor();
            // The circle uses the yellow accent4 color but has HSL adjustments that make it green
            assertEquals(hexToColor(accent4Hex), greenCircleColorStyle.getColor());
            assertEquals(50004, greenCircleColorStyle.getAlpha()); // 50% transparency
            assertEquals(6533927, greenCircleColorStyle.getHueOff());
            assertEquals(6405, greenCircleColorStyle.getLumOff());
            assertEquals(-27185, greenCircleColorStyle.getSatOff());

            XSLFTextBox greenCircleText = (XSLFTextBox) shapes.get(5);
            assertEquals(greenCircleText.getText(), "ghi");
            assertEquals(TextAlign.RIGHT, greenCircleText.getTextParagraphs().get(0).getTextAlign());

            // Shape 4 - Circle with Picture Fill - no text
            XSLFAutoShape pictureShape = (XSLFAutoShape) shapes.get(6);
            assertTrue(pictureShape.getText().isEmpty(), "text is empty?");
            XSLFTexturePaint texturePaint = (XSLFTexturePaint) pictureShape.getFillPaint();
            assertEquals(ContentTypes.IMAGE_JPEG, texturePaint.getContentType());
        }
    }

    @Test
    public void testTextRotationOnShape() throws IOException {
        try (XMLSlideShow inputPptx = XSLFTestDataSamples.openSampleDocument(ROTATED_TEXT_DIAGRAM)) {
            List<XSLFDiagram> diagrams = extractDiagrams(inputPptx);
            assertEquals(1, diagrams.size());

            XSLFDiagram diagram = diagrams.get(0);
            XSLFGroupShape groupShape = diagram.getGroupShape();

            List<XSLFShape> shapes = groupShape.getShapes();

            // Text shapes have separate rotation calculation
            XSLFTextBox abcText = (XSLFTextBox) shapes.get(1);
            assertEquals(-41.3187, abcText.getRotation(), 1E-4);

            XSLFTextBox defText = (XSLFTextBox) shapes.get(5);
            assertEquals(49.1812, defText.getRotation(), 1E-4);

            XSLFTextBox ghiText = (XSLFTextBox) shapes.get(9);
            assertEquals(0.0, ghiText.getRotation(), 1E-4);

            XSLFTextBox jklText = (XSLFTextBox) shapes.get(11);
            assertEquals(0.0, jklText.getRotation(), 1E-4);
        }
    }
}
