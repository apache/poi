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
package org.apache.poi.xddf.usermodel.text;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.apache.poi.util.LocaleUtil;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFTextBox;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;

public class TestXDDFTextRun {

    @Test
    public void testTextRunPropertiesInSlide() throws IOException {
        try (XMLSlideShow ppt = new XMLSlideShow()) {
            XSLFSlide slide = ppt.createSlide();
            XSLFTextShape sh = slide.createAutoShape();
            sh.addNewTextParagraph();

            XDDFTextBody body = sh.getTextBody();
            XDDFTextParagraph para = body.getParagraph(0);
            XDDFTextRun r = para.appendRegularRun("text");
            assertEquals(LocaleUtil.getUserLocale().toLanguageTag(), r.getLanguage().toLanguageTag());

            assertNull(r.getCharacterSpacing());
            r.setCharacterSpacing(3.0);
            assertEquals(3., r.getCharacterSpacing(), 0);
            r.setCharacterSpacing(-3.0);
            assertEquals(-3., r.getCharacterSpacing(), 0);
            r.setCharacterSpacing(0.0);
            assertEquals(0., r.getCharacterSpacing(), 0);

            assertEquals(11.0, r.getFontSize(), 0);
            r.setFontSize(13.0);
            assertEquals(13.0, r.getFontSize(), 0);

            assertFalse(r.isSuperscript());
            r.setSuperscript(0.8);
            assertTrue(r.isSuperscript());
            r.setSuperscript(null);
            assertFalse(r.isSuperscript());

            assertFalse(r.isSubscript());
            r.setSubscript(0.7);
            assertTrue(r.isSubscript());
            r.setSubscript(null);
            assertFalse(r.isSubscript());

            r.setBaseline(0.9);
            assertTrue(r.isSuperscript());
            r.setBaseline(-0.6);
            assertTrue(r.isSubscript());
        }
    }

    @Test
    public void testTextRunPropertiesInSheet() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sheet = wb.createSheet();
            XSSFDrawing drawing = sheet.createDrawingPatriarch();

            XSSFTextBox shape = drawing.createTextbox(new XSSFClientAnchor(0, 0, 0, 0, 2, 2, 3, 4));

            shape.addNewTextParagraph().addNewTextRun().setText("Line 1");

            XDDFTextBody body = shape.getTextBody();
            XDDFTextParagraph para = body.getParagraph(1);
            List<XDDFTextRun> runs = para.getTextRuns();
            assertEquals(1, runs.size());

            XDDFTextRun run = runs.get(0);
            assertEquals("Line 1", run.getText());

            assertFalse(run.isStrikeThrough());
            run.setStrikeThrough(StrikeType.SINGLE_STRIKE);
            assertTrue(run.isStrikeThrough());
            run.setStrikeThrough(StrikeType.NO_STRIKE);
            assertFalse(run.isStrikeThrough());

            assertFalse(run.isCapitals());
            run.setCapitals(CapsType.SMALL);
            assertTrue(run.isCapitals());
            run.setCapitals(CapsType.NONE);
            assertFalse(run.isCapitals());

            assertFalse(run.isBold());
            run.setBold(true);
            assertTrue(run.isBold());
            run.setBold(false);
            assertFalse(run.isBold());

            assertFalse(run.isItalic());
            run.setItalic(true);
            assertTrue(run.isItalic());
            run.setItalic(false);
            assertFalse(run.isItalic());

            assertFalse(run.isUnderline());
            run.setUnderline(UnderlineType.WAVY_DOUBLE);
            assertTrue(run.isUnderline());
            run.setUnderline(UnderlineType.NONE);
            assertFalse(run.isUnderline());

            assertNotNull(run.getText());
        }
    }
}
