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

import static org.junit.Assert.*;

import java.awt.Color;
import java.io.IOException;
import java.util.List;

import org.junit.Test;

public class TestXSSFTextRun {
    @Test
    public void testXSSFTextParagraph() throws IOException {
        XSSFWorkbook wb = new XSSFWorkbook();
        try {
            XSSFSheet sheet = wb.createSheet();
            XSSFDrawing drawing = sheet.createDrawingPatriarch();
    
            XSSFTextBox shape = drawing.createTextbox(new XSSFClientAnchor(0, 0, 0, 0, 2, 2, 3, 4));

            XSSFTextParagraph para = shape.addNewTextParagraph();
            para.addNewTextRun().setText("Line 1");

            List<XSSFTextRun> runs = para.getTextRuns();
            assertEquals(1, runs.size());
            XSSFTextRun run = runs.get(0);
            assertEquals("Line 1", run.getText());
            
            assertNotNull(run.getParentParagraph());
            assertNotNull(run.getXmlObject());
            assertNotNull(run.getRPr());
            
            assertEquals(new Color(0,0,0), run.getFontColor());
            
            Color color = new Color(0, 255, 255);
            run.setFontColor(color);
            assertEquals(color, run.getFontColor());
            
            assertEquals(11.0, run.getFontSize(), 0.01);
            run.setFontSize(12.32);
            assertEquals(12.32, run.getFontSize(), 0.01);
            run.setFontSize(-1.0);
            assertEquals(11.0, run.getFontSize(), 0.01);
            run.setFontSize(-1.0);
            assertEquals(11.0, run.getFontSize(), 0.01);
            try {
                run.setFontSize(0.9);
                fail("Should fail");
            } catch (IllegalArgumentException e) {
                assertTrue(e.getMessage().contains("0.9"));
            }
            assertEquals(11.0, run.getFontSize(), 0.01);
            
            assertEquals(0.0, run.getCharacterSpacing(), 0.01);
            run.setCharacterSpacing(12.31);
            assertEquals(12.31, run.getCharacterSpacing(), 0.01);
            run.setCharacterSpacing(0.0);
            assertEquals(0.0, run.getCharacterSpacing(), 0.01);
            run.setCharacterSpacing(0.0);
            assertEquals(0.0, run.getCharacterSpacing(), 0.01);
            
            assertEquals("Calibri", run.getFontFamily());
            run.setFontFamily("Arial", (byte)1, (byte)1, false);
            assertEquals("Arial", run.getFontFamily());
            run.setFontFamily("Arial", (byte)-1, (byte)1, false);
            assertEquals("Arial", run.getFontFamily());
            run.setFontFamily("Arial", (byte)1, (byte)-1, false);
            assertEquals("Arial", run.getFontFamily());
            run.setFontFamily("Arial", (byte)1, (byte)1, true);
            assertEquals("Arial", run.getFontFamily());
            run.setFontFamily(null, (byte)1, (byte)1, false);
            assertEquals("Calibri", run.getFontFamily());
            run.setFontFamily(null, (byte)1, (byte)1, false);
            assertEquals("Calibri", run.getFontFamily());

            run.setFont("Arial");
            assertEquals("Arial", run.getFontFamily());
            
            assertEquals((byte)0, run.getPitchAndFamily());
            run.setFont(null);
            assertEquals((byte)0, run.getPitchAndFamily());
            
            assertFalse(run.isStrikethrough());
            run.setStrikethrough(true);
            assertTrue(run.isStrikethrough());
            run.setStrikethrough(false);
            assertFalse(run.isStrikethrough());

            assertFalse(run.isSuperscript());
            run.setSuperscript(true);
            assertTrue(run.isSuperscript());
            run.setSuperscript(false);
            assertFalse(run.isSuperscript());

            assertFalse(run.isSubscript());
            run.setSubscript(true);
            assertTrue(run.isSubscript());
            run.setSubscript(false);
            assertFalse(run.isSubscript());
            
            assertEquals(TextCap.NONE, run.getTextCap());

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
            run.setUnderline(true);
            assertTrue(run.isUnderline());
            run.setUnderline(false);
            assertFalse(run.isUnderline());
            
            assertNotNull(run.toString());
        } finally {
            wb.close();
        }
    }
}
