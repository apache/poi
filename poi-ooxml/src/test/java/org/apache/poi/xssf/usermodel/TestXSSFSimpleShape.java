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
package org.apache.poi.xssf.usermodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xddf.usermodel.text.XDDFBodyProperties;
import org.apache.poi.xddf.usermodel.text.XDDFNoAutoFit;
import org.apache.poi.xddf.usermodel.text.XDDFNormalAutoFit;
import org.apache.poi.xddf.usermodel.text.XDDFShapeAutoFit;
import org.apache.poi.xddf.usermodel.text.XDDFTextBody;
import org.apache.poi.xddf.usermodel.text.XDDFTextParagraph;
import org.junit.jupiter.api.Test;

class TestXSSFSimpleShape {
    @Test
    void testXSSFTextParagraph() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sheet = wb.createSheet();
            XSSFDrawing drawing = sheet.createDrawingPatriarch();

            XSSFTextBox shape = drawing.createTextbox(new XSSFClientAnchor(0, 0, 0, 0, 2, 2, 3, 4));

            XSSFRichTextString rt = new XSSFRichTextString("Test String");

            XSSFFont font = wb.createFont();
            Color color = new Color(0, 255, 255);
            font.setColor(new XSSFColor(color, wb.getStylesSource().getIndexedColors()));
            font.setFontName("Arial");
            rt.applyFont(font);

            shape.setText(rt);

            assertNotNull(shape.getCTShape());
            assertNotNull(shape.iterator());
            assertNotNull(XSSFSimpleShape.prototype());

            for (ListAutoNumber nr : ListAutoNumber.values()) {
                shape.getTextParagraphs().get(0).setBullet(nr);
                assertNotNull(shape.getText());
            }

            shape.getTextParagraphs().get(0).setBullet(false);
            assertNotNull(shape.getText());

            shape.setText("testtext");
            assertEquals("testtext", shape.getText());

            shape.setText(new XSSFRichTextString());
            assertEquals("null", shape.getText());

            shape.addNewTextParagraph();
            shape.addNewTextParagraph("test-other-text");
            shape.addNewTextParagraph(new XSSFRichTextString("rtstring"));
            shape.addNewTextParagraph(new XSSFRichTextString());
            assertEquals("null\n\ntest-other-text\nrtstring\nnull", shape.getText());

            XDDFTextBody body = shape.getTextBody();
            assertNotNull(body);
            List<XDDFTextParagraph> paragraphs = body.getParagraphs();
            assertEquals(5, paragraphs.size());
            assertEquals("null", body.getParagraph(0).getText());
            assertEquals("", body.getParagraph(1).getText());
            assertEquals("test-other-text", body.getParagraph(2).getText());
            assertEquals("rtstring", body.getParagraph(3).getText());
            assertEquals("null", body.getParagraph(4).getText());

            assertEquals(TextHorizontalOverflow.OVERFLOW, shape.getTextHorizontalOverflow());
            shape.setTextHorizontalOverflow(TextHorizontalOverflow.CLIP);
            assertEquals(TextHorizontalOverflow.CLIP, shape.getTextHorizontalOverflow());
            shape.setTextHorizontalOverflow(TextHorizontalOverflow.OVERFLOW);
            assertEquals(TextHorizontalOverflow.OVERFLOW, shape.getTextHorizontalOverflow());
            shape.setTextHorizontalOverflow(null);
            assertEquals(TextHorizontalOverflow.OVERFLOW, shape.getTextHorizontalOverflow());
            shape.setTextHorizontalOverflow(null);
            assertEquals(TextHorizontalOverflow.OVERFLOW, shape.getTextHorizontalOverflow());

            assertEquals(TextVerticalOverflow.OVERFLOW, shape.getTextVerticalOverflow());
            shape.setTextVerticalOverflow(TextVerticalOverflow.CLIP);
            assertEquals(TextVerticalOverflow.CLIP, shape.getTextVerticalOverflow());
            shape.setTextVerticalOverflow(TextVerticalOverflow.OVERFLOW);
            assertEquals(TextVerticalOverflow.OVERFLOW, shape.getTextVerticalOverflow());
            shape.setTextVerticalOverflow(null);
            assertEquals(TextVerticalOverflow.OVERFLOW, shape.getTextVerticalOverflow());
            shape.setTextVerticalOverflow(null);
            assertEquals(TextVerticalOverflow.OVERFLOW, shape.getTextVerticalOverflow());

            assertEquals(VerticalAlignment.TOP, shape.getVerticalAlignment());
            shape.setVerticalAlignment(VerticalAlignment.BOTTOM);
            assertEquals(VerticalAlignment.BOTTOM, shape.getVerticalAlignment());
            shape.setVerticalAlignment(VerticalAlignment.TOP);
            assertEquals(VerticalAlignment.TOP, shape.getVerticalAlignment());
            shape.setVerticalAlignment(null);
            assertEquals(VerticalAlignment.TOP, shape.getVerticalAlignment());
            shape.setVerticalAlignment(null);
            assertEquals(VerticalAlignment.TOP, shape.getVerticalAlignment());

            assertEquals(TextDirection.HORIZONTAL, shape.getTextDirection());
            shape.setTextDirection(TextDirection.STACKED);
            assertEquals(TextDirection.STACKED, shape.getTextDirection());
            shape.setTextDirection(TextDirection.HORIZONTAL);
            assertEquals(TextDirection.HORIZONTAL, shape.getTextDirection());
            shape.setTextDirection(null);
            assertEquals(TextDirection.HORIZONTAL, shape.getTextDirection());
            shape.setTextDirection(null);
            assertEquals(TextDirection.HORIZONTAL, shape.getTextDirection());

            XDDFBodyProperties props = body.getBodyProperties();
            assertNotNull(props);

            assertEquals(3.6, shape.getBottomInset(), 0.01);
            assertNull(props.getBottomInset());
            shape.setBottomInset(12.32);
            assertEquals(12.32, shape.getBottomInset(), 0.01);
            assertEquals(12.32, props.getBottomInset(), 0.01);
            shape.setBottomInset(-1);
            assertEquals(3.6, shape.getBottomInset(), 0.01);
            assertNull(props.getBottomInset());
            shape.setBottomInset(-1);
            assertEquals(3.6, shape.getBottomInset(), 0.01);
            assertNull(props.getBottomInset());

            assertEquals(3.6, shape.getLeftInset(), 0.01);
            assertNull(props.getLeftInset());
            shape.setLeftInset(12.31);
            assertEquals(12.31, shape.getLeftInset(), 0.01);
            assertEquals(12.31, props.getLeftInset(), 0.01);
            shape.setLeftInset(-1);
            assertEquals(3.6, shape.getLeftInset(), 0.01);
            assertNull(props.getLeftInset());
            shape.setLeftInset(-1);
            assertEquals(3.6, shape.getLeftInset(), 0.01);
            assertNull(props.getLeftInset());

            assertEquals(3.6, shape.getRightInset(), 0.01);
            assertNull(props.getRightInset());
            shape.setRightInset(13.31);
            assertEquals(13.31, shape.getRightInset(), 0.01);
            assertEquals(13.31, props.getRightInset(), 0.01);
            shape.setRightInset(-1);
            assertEquals(3.6, shape.getRightInset(), 0.01);
            assertNull(props.getRightInset());
            shape.setRightInset(-1);
            assertEquals(3.6, shape.getRightInset(), 0.01);
            assertNull(props.getRightInset());

            assertEquals(3.6, shape.getTopInset(), 0.01);
            assertNull(props.getTopInset());
            shape.setTopInset(23.31);
            assertEquals(23.31, shape.getTopInset(), 0.01);
            assertEquals(23.31, props.getTopInset(), 0.01);
            shape.setTopInset(-1);
            assertEquals(3.6, shape.getTopInset(), 0.01);
            assertNull(props.getTopInset());
            shape.setTopInset(-1);
            assertEquals(3.6, shape.getTopInset(), 0.01);
            assertNull(props.getTopInset());

            assertTrue(shape.getWordWrap());
            shape.setWordWrap(false);
            assertFalse(shape.getWordWrap());
            shape.setWordWrap(true);
            assertTrue(shape.getWordWrap());

            assertEquals(TextAutofit.NORMAL, shape.getTextAutofit());
            assertTrue(props.getAutoFit() instanceof XDDFNormalAutoFit);
            shape.setTextAutofit(TextAutofit.NORMAL);
            assertEquals(TextAutofit.NORMAL, shape.getTextAutofit());
            assertTrue(props.getAutoFit() instanceof XDDFNormalAutoFit);
            shape.setTextAutofit(TextAutofit.SHAPE);
            assertEquals(TextAutofit.SHAPE, shape.getTextAutofit());
            assertTrue(props.getAutoFit() instanceof XDDFShapeAutoFit);
            shape.setTextAutofit(TextAutofit.NONE);
            assertEquals(TextAutofit.NONE, shape.getTextAutofit());
            assertTrue(props.getAutoFit() instanceof XDDFNoAutoFit);

            assertEquals(5, shape.getShapeType());
            shape.setShapeType(23);
            assertEquals(23, shape.getShapeType());

            // TODO: should this be supported?
            // shape.setShapeType(-1);
            // assertEquals(-1, shape.getShapeType());
            // shape.setShapeType(-1);
            // assertEquals(-1, shape.getShapeType());

            assertNotNull(shape.getShapeProperties());
        }
    }
}
