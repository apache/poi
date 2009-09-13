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

import junit.framework.TestCase;

import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTRst;

/**
 * Tests functionality of the XSSFRichTextRun object
 *
 * @author Yegor Kozlov
 */
public final class TestXSSFRichTextString extends TestCase {

    public void testCreate() {

        XSSFRichTextString rt = new XSSFRichTextString("Apache POI");
        assertEquals("Apache POI", rt.getString());

        CTRst st = rt.getCTRst();
        assertTrue(st.isSetT());
        assertEquals("Apache POI", st.getT());

        rt.append(" is cool stuff");
        assertEquals(2, st.sizeOfRArray());
        assertFalse(st.isSetT());

        assertEquals("Apache POI is cool stuff", rt.getString());
    }


    public void testApplyFont() {

        XSSFRichTextString rt = new XSSFRichTextString();
        rt.append("123");
        rt.append("4567");
        rt.append("89");

        XSSFFont font1 = new XSSFFont();
        font1.setBold(true);

        rt.applyFont(2, 5, font1);

        assertEquals(5, rt.numFormattingRuns());
        assertEquals(0, rt.getIndexOfFormattingRun(0));
        assertEquals(2, rt.getLengthOfFormattingRun(0));

        assertEquals(2, rt.getIndexOfFormattingRun(1));
        assertEquals(3, rt.getLengthOfFormattingRun(1));

        assertEquals(5, rt.getIndexOfFormattingRun(2));
        assertEquals(3, rt.getLengthOfFormattingRun(2));

        assertEquals(8, rt.getIndexOfFormattingRun(3));
        assertEquals(1, rt.getLengthOfFormattingRun(3));
    }

    public void testClearFormatting() {

        XSSFRichTextString rt = new XSSFRichTextString("Apache POI");
        assertEquals("Apache POI", rt.getString());

        rt.clearFormatting();

        CTRst st = rt.getCTRst();
        assertTrue(st.isSetT());
        assertEquals("Apache POI", rt.getString());
        assertEquals(0, rt.numFormattingRuns());

        XSSFFont font = new XSSFFont();
        font.setBold(true);

        rt.applyFont(7, 10, font);
        assertEquals(2, rt.numFormattingRuns());
        rt.clearFormatting();
        assertEquals("Apache POI", rt.getString());
        assertEquals(0, rt.numFormattingRuns());
    }

    public void testGetFonts() {

        XSSFRichTextString rt = new XSSFRichTextString();

        XSSFFont font1 = new XSSFFont();
        font1.setFontName("Arial");
        font1.setItalic(true);
        rt.append("The quick", font1);

        XSSFFont font1$ = rt.getFontOfFormattingRun(0);
        assertEquals(font1.getItalic(), font1$.getItalic());
        assertEquals(font1.getFontName(), font1$.getFontName());

        XSSFFont font2 = new XSSFFont();
        font2.setFontName("Courier");
        font2.setBold(true);
        rt.append(" brown fox", font2);

        XSSFFont font2$ = rt.getFontOfFormattingRun(1);
        assertEquals(font2.getBold(), font2$.getBold());
        assertEquals(font2.getFontName(), font2$.getFontName());
    }
}
