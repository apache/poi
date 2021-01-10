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

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Color;
import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;

class TestXSSFTextParagraph {
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

            List<XSSFTextParagraph> paras = shape.getTextParagraphs();
            assertEquals(1, paras.size());

            XSSFTextParagraph text = paras.get(0);
            assertEquals("Test String", text.getText());

            assertFalse(text.isBullet());
            assertNotNull(text.getXmlObject());
            assertEquals(shape.getCTShape(), text.getParentShape());
            assertNotNull(text.iterator());
            assertNotNull(text.addLineBreak());

            assertNotNull(text.getTextRuns());
            assertEquals(2, text.getTextRuns().size());
            text.addNewTextRun();
            assertEquals(3, text.getTextRuns().size());

            assertEquals(TextAlign.LEFT, text.getTextAlign());
            text.setTextAlign(null);
            assertEquals(TextAlign.LEFT, text.getTextAlign());
            text.setTextAlign(TextAlign.CENTER);
            assertEquals(TextAlign.CENTER, text.getTextAlign());
            text.setTextAlign(TextAlign.RIGHT);
            assertEquals(TextAlign.RIGHT, text.getTextAlign());
            text.setTextAlign(null);
            assertEquals(TextAlign.LEFT, text.getTextAlign());

            text.setTextFontAlign(TextFontAlign.BASELINE);
            assertEquals(TextFontAlign.BASELINE, text.getTextFontAlign());
            text.setTextFontAlign(TextFontAlign.BOTTOM);
            assertEquals(TextFontAlign.BOTTOM, text.getTextFontAlign());
            text.setTextFontAlign(null);
            assertEquals(TextFontAlign.BASELINE, text.getTextFontAlign());
            text.setTextFontAlign(null);
            assertEquals(TextFontAlign.BASELINE, text.getTextFontAlign());

            assertNull(text.getBulletFont());
            text.setBulletFont("Arial");
            assertEquals("Arial", text.getBulletFont());

            assertNull(text.getBulletCharacter());
            text.setBulletCharacter(".");
            assertEquals(".", text.getBulletCharacter());

            assertNull(text.getBulletFontColor());
            text.setBulletFontColor(color);
            assertEquals(color, text.getBulletFontColor());

            assertEquals(100.0, text.getBulletFontSize(), 0.01);
            text.setBulletFontSize(1.0);
            assertEquals(1.0, text.getBulletFontSize(), 0.01);
            text.setBulletFontSize(1.0);
            assertEquals(1.0, text.getBulletFontSize(), 0.01);
            text.setBulletFontSize(-9.0);
            assertEquals(-9.0, text.getBulletFontSize(), 0.01);
            text.setBulletFontSize(-9.0);
            assertEquals(-9.0, text.getBulletFontSize(), 0.01);
            text.setBulletFontSize(1.0);
            assertEquals(1.0, text.getBulletFontSize(), 0.01);
            text.setBulletFontSize(-9.0);
            assertEquals(-9.0, text.getBulletFontSize(), 0.01);

            assertEquals(0.0, text.getIndent(), 0.01);
            text.setIndent(2.0);
            assertEquals(2.0, text.getIndent(), 0.01);
            text.setIndent(-1.0);
            assertEquals(0.0, text.getIndent(), 0.01);
            text.setIndent(-1.0);
            assertEquals(0.0, text.getIndent(), 0.01);

            assertEquals(0.0, text.getLeftMargin(), 0.01);
            text.setLeftMargin(3.0);
            assertEquals(3.0, text.getLeftMargin(), 0.01);
            text.setLeftMargin(-1.0);
            assertEquals(0.0, text.getLeftMargin(), 0.01);
            text.setLeftMargin(-1.0);
            assertEquals(0.0, text.getLeftMargin(), 0.01);

            assertEquals(0.0, text.getRightMargin(), 0.01);
            text.setRightMargin(4.5);
            assertEquals(4.5, text.getRightMargin(), 0.01);
            text.setRightMargin(-1.0);
            assertEquals(0.0, text.getRightMargin(), 0.01);
            text.setRightMargin(-1.0);
            assertEquals(0.0, text.getRightMargin(), 0.01);

            assertEquals(0.0, text.getDefaultTabSize(), 0.01);

            assertEquals(0.0, text.getTabStop(0), 0.01);
            text.addTabStop(3.14);
            assertEquals(3.14, text.getTabStop(0), 0.01);

            assertEquals(100.0, text.getLineSpacing(), 0.01);
            text.setLineSpacing(3.15);
            assertEquals(3.15, text.getLineSpacing(), 0.01);
            text.setLineSpacing(-2.13);
            assertEquals(-2.13, text.getLineSpacing(), 0.01);

            assertEquals(0.0, text.getSpaceBefore(), 0.01);
            text.setSpaceBefore(3.17);
            assertEquals(3.17, text.getSpaceBefore(), 0.01);
            text.setSpaceBefore(-4.7);
            assertEquals(-4.7, text.getSpaceBefore(), 0.01);

            assertEquals(0.0, text.getSpaceAfter(), 0.01);
            text.setSpaceAfter(6.17);
            assertEquals(6.17, text.getSpaceAfter(), 0.01);
            text.setSpaceAfter(-8.17);
            assertEquals(-8.17, text.getSpaceAfter(), 0.01);

            assertEquals(0, text.getLevel());
            text.setLevel(1);
            assertEquals(1, text.getLevel());
            text.setLevel(4);
            assertEquals(4, text.getLevel());

            assertTrue(text.isBullet());
            assertFalse(text.isBulletAutoNumber());
            text.setBullet(false);
            text.setBullet(false);
            assertFalse(text.isBullet());
            assertFalse(text.isBulletAutoNumber());
            text.setBullet(true);
            assertTrue(text.isBullet());
            assertFalse(text.isBulletAutoNumber());
            assertEquals(0, text.getBulletAutoNumberStart());
            assertEquals(ListAutoNumber.ARABIC_PLAIN, text.getBulletAutoNumberScheme());

            text.setBullet(false);
            assertFalse(text.isBullet());
            text.setBullet(ListAutoNumber.CIRCLE_NUM_DB_PLAIN);
            assertTrue(text.isBullet());
            assertTrue(text.isBulletAutoNumber());
            assertEquals(0, text.getBulletAutoNumberStart());
            assertEquals(ListAutoNumber.CIRCLE_NUM_DB_PLAIN, text.getBulletAutoNumberScheme());
            text.setBullet(false);
            assertFalse(text.isBullet());
            assertFalse(text.isBulletAutoNumber());
            text.setBullet(ListAutoNumber.CIRCLE_NUM_WD_BLACK_PLAIN, 10);
            assertTrue(text.isBullet());
            assertTrue(text.isBulletAutoNumber());
            assertEquals(10, text.getBulletAutoNumberStart());
            assertEquals(ListAutoNumber.CIRCLE_NUM_WD_BLACK_PLAIN, text.getBulletAutoNumberScheme());


            assertNotNull(text.toString());

            new XSSFTextParagraph(text.getXmlObject(), shape.getCTShape());
        }
    }
}
