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
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STXstring;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTRPrElt;

import java.util.TreeMap;

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

        assertEquals("123456789", rt.getString());

        XSSFFont font1 = new XSSFFont();
        font1.setBold(true);

        rt.applyFont(2, 5, font1);

        assertEquals(4, rt.numFormattingRuns());
        assertEquals(0, rt.getIndexOfFormattingRun(0));
        assertEquals("12", rt.getCTRst().getRArray(0).getT());

        assertEquals(2, rt.getIndexOfFormattingRun(1));
        assertEquals("345", rt.getCTRst().getRArray(1).getT());

        assertEquals(5, rt.getIndexOfFormattingRun(2));
        assertEquals(2, rt.getLengthOfFormattingRun(2));
        assertEquals("67", rt.getCTRst().getRArray(2).getT());

        assertEquals(7, rt.getIndexOfFormattingRun(3));
        assertEquals(2, rt.getLengthOfFormattingRun(3));
        assertEquals("89", rt.getCTRst().getRArray(3).getT());
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

    /**
     * make sure we insert xml:space="preserve" attribute
     * if a string has leading or trailing white spaces
     */
    public void testPreserveSpaces() {
        XSSFRichTextString rt = new XSSFRichTextString("Apache");
        CTRst ct = rt.getCTRst();
        STXstring xs = ct.xgetT();
        assertEquals("<xml-fragment>Apache</xml-fragment>", xs.xmlText());
        rt.setString("  Apache");
        assertEquals("<xml-fragment xml:space=\"preserve\">  Apache</xml-fragment>", xs.xmlText());

    }

    /**
     * test that unicode representation_ xHHHH_ is properly processed
     */
    public void testUtfDecode() {
        CTRst st = CTRst.Factory.newInstance();
        st.setT("abc_x000D_2ef_x000D_");
        XSSFRichTextString rt = new XSSFRichTextString(st);
        //_x000D_ is converted into carriage return
        assertEquals("abc\r2ef\r", rt.getString());
        
    }

    public void testApplyFont_lowlevel(){
        CTRst st = CTRst.Factory.newInstance();
        String text = "Apache Software Foundation";
        XSSFRichTextString str = new XSSFRichTextString(text);
        assertEquals(26, text.length());

        st.addNewR().setT(text);

        TreeMap<Integer, CTRPrElt> formats = str.getFormatMap(st);
        assertEquals(1, formats.size());
        assertEquals(26, (int)formats.firstEntry().getKey());
        assertNull(formats.firstEntry().getValue());

        CTRPrElt fmt1 = CTRPrElt.Factory.newInstance();
        str.applyFont(formats, 0, 6, fmt1);
        assertEquals(2, formats.size());
        assertEquals("[6, 26]", formats.keySet().toString());
        Object[] runs1 = formats.values().toArray();
        assertSame(fmt1, runs1[0]);
        assertSame(null, runs1[1]);

        CTRPrElt fmt2 = CTRPrElt.Factory.newInstance();
        str.applyFont(formats, 7, 15, fmt2);
        assertEquals(4, formats.size());
        assertEquals("[6, 7, 15, 26]", formats.keySet().toString());
        Object[] runs2 = formats.values().toArray();
        assertSame(fmt1, runs2[0]);
        assertSame(null, runs2[1]);
        assertSame(fmt2, runs2[2]);
        assertSame(null, runs2[3]);

        CTRPrElt fmt3 = CTRPrElt.Factory.newInstance();
        str.applyFont(formats, 6, 7, fmt3);
        assertEquals(4, formats.size());
        assertEquals("[6, 7, 15, 26]", formats.keySet().toString());
        Object[] runs3 = formats.values().toArray();
        assertSame(fmt1, runs3[0]);
        assertSame(fmt3, runs3[1]);
        assertSame(fmt2, runs3[2]);
        assertSame(null, runs3[3]);

        CTRPrElt fmt4 = CTRPrElt.Factory.newInstance();
        str.applyFont(formats, 0, 7, fmt4);
        assertEquals(3, formats.size());
        assertEquals("[7, 15, 26]", formats.keySet().toString());
        Object[] runs4 = formats.values().toArray();
        assertSame(fmt4, runs4[0]);
        assertSame(fmt2, runs4[1]);
        assertSame(null, runs4[2]);

        CTRPrElt fmt5 = CTRPrElt.Factory.newInstance();
        str.applyFont(formats, 0, 26, fmt5);
        assertEquals(1, formats.size());
        assertEquals("[26]", formats.keySet().toString());
        Object[] runs5 = formats.values().toArray();
        assertSame(fmt5, runs5[0]);

        CTRPrElt fmt6 = CTRPrElt.Factory.newInstance();
        str.applyFont(formats, 15, 26, fmt6);
        assertEquals(2, formats.size());
        assertEquals("[15, 26]", formats.keySet().toString());
        Object[] runs6 = formats.values().toArray();
        assertSame(fmt5, runs6[0]);
        assertSame(fmt6, runs6[1]);

        str.applyFont(formats, 0, 26, null);
        assertEquals(1, formats.size());
        assertEquals("[26]", formats.keySet().toString());
        Object[] runs7 = formats.values().toArray();
        assertSame(null, runs7[0]);

        str.applyFont(formats, 15, 26, fmt6);
        assertEquals(2, formats.size());
        assertEquals("[15, 26]", formats.keySet().toString());
        Object[] runs8 = formats.values().toArray();
        assertSame(null, runs8[0]);
        assertSame(fmt6, runs8[1]);

        str.applyFont(formats, 15, 26, fmt5);
        assertEquals(2, formats.size());
        assertEquals("[15, 26]", formats.keySet().toString());
        Object[] runs9 = formats.values().toArray();
        assertSame(null, runs9[0]);
        assertSame(fmt5, runs9[1]);

        str.applyFont(formats, 2, 20, fmt6);
        assertEquals(3, formats.size());
        assertEquals("[2, 20, 26]", formats.keySet().toString());
        Object[] runs10 = formats.values().toArray();
        assertSame(null, runs10[0]);
        assertSame(fmt6, runs10[1]);
        assertSame(fmt5, runs10[2]);

        str.applyFont(formats, 22, 24, fmt4);
        assertEquals(5, formats.size());
        assertEquals("[2, 20, 22, 24, 26]", formats.keySet().toString());
        Object[] runs11 = formats.values().toArray();
        assertSame(null, runs11[0]);
        assertSame(fmt6, runs11[1]);
        assertSame(fmt5, runs11[2]);
        assertSame(fmt4, runs11[3]);
        assertSame(fmt5, runs11[4]);

        str.applyFont(formats, 0, 10, fmt1);
        assertEquals(5, formats.size());
        assertEquals("[10, 20, 22, 24, 26]", formats.keySet().toString());
        Object[] runs12 = formats.values().toArray();
        assertSame(fmt1, runs12[0]);
        assertSame(fmt6, runs12[1]);
        assertSame(fmt5, runs12[2]);
        assertSame(fmt4, runs12[3]);
        assertSame(fmt5, runs12[4]);
    }

    public void testApplyFont_usermodel(){
        String text = "Apache Software Foundation";
        XSSFRichTextString str = new XSSFRichTextString(text);
        XSSFFont font1 = new XSSFFont();
        XSSFFont font2 = new XSSFFont();
        XSSFFont font3 = new XSSFFont();
        str.applyFont(font1);
        assertEquals(1, str.numFormattingRuns());

        str.applyFont(0, 6, font1);
        str.applyFont(6, text.length(), font2);
        assertEquals(2, str.numFormattingRuns());
        assertEquals("Apache", str.getCTRst().getRArray(0).getT());
        assertEquals(" Software Foundation", str.getCTRst().getRArray(1).getT());

        str.applyFont(15, 26, font3);
        assertEquals(3, str.numFormattingRuns());
        assertEquals("Apache", str.getCTRst().getRArray(0).getT());
        assertEquals(" Software", str.getCTRst().getRArray(1).getT());
        assertEquals(" Foundation", str.getCTRst().getRArray(2).getT());

        str.applyFont(6, text.length(), font2);
        assertEquals(2, str.numFormattingRuns());
        assertEquals("Apache", str.getCTRst().getRArray(0).getT());
        assertEquals(" Software Foundation", str.getCTRst().getRArray(1).getT());
    }
}
