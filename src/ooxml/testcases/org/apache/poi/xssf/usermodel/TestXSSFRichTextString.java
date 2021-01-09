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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.TreeMap;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xssf.model.StylesTable;
import org.junit.jupiter.api.Test;
import org.openxmlformats.schemas.officeDocument.x2006.sharedTypes.STXstring;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFont;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTRPrElt;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTRst;

/**
 * Tests functionality of the XSSFRichTextRun object
 */
public final class TestXSSFRichTextString {

    @Test
    void testCreate() {
        XSSFRichTextString rt = new XSSFRichTextString("Apache POI");
        assertEquals("Apache POI", rt.getString());
        assertFalse(rt.hasFormatting());

        CTRst st = rt.getCTRst();
        assertTrue(st.isSetT());
        assertEquals("Apache POI", st.getT());
        assertFalse(rt.hasFormatting());

        rt.append(" is cool stuff");
        assertEquals(2, st.sizeOfRArray());
        assertFalse(st.isSetT());

        assertEquals("Apache POI is cool stuff", rt.getString());
        assertFalse(rt.hasFormatting());
    }

    @Test
    void testEmpty() {
        XSSFRichTextString rt = new XSSFRichTextString();
        assertEquals(0, rt.getIndexOfFormattingRun(9999));
        assertEquals(-1, rt.getLengthOfFormattingRun(9999));
        assertNull(rt.getFontAtIndex(9999));
    }

    @Test
    void testApplyFont() {
        XSSFRichTextString rt = new XSSFRichTextString();
        rt.append("123");
        rt.append("4567");
        rt.append("89");

        assertEquals("123456789", rt.getString());
        assertFalse(rt.hasFormatting());

        XSSFFont font1 = new XSSFFont();
        font1.setBold(true);

        rt.applyFont(2, 5, font1);
        assertTrue(rt.hasFormatting());

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


        assertEquals(-1, rt.getIndexOfFormattingRun(9999));
        assertEquals(-1, rt.getLengthOfFormattingRun(9999));
        assertNull(rt.getFontAtIndex(9999));
    }

    @Test
    void testApplyFontIndex() {
        XSSFRichTextString rt = new XSSFRichTextString("Apache POI");
        rt.applyFont(0, 10, (short)1);

        rt.applyFont((short)1);

        assertNotNull(rt.getFontAtIndex(0));
    }

    @Test
    void testApplyFontWithStyles() {
        XSSFRichTextString rt = new XSSFRichTextString("Apache POI");

        StylesTable tbl = new StylesTable();
        rt.setStylesTableReference(tbl);
        assertThrows(IndexOutOfBoundsException.class, () -> rt.applyFont(0, 10, (short)1), "Fails without styles in the table");

        tbl.putFont(new XSSFFont());
        rt.applyFont(0, 10, (short)1);
        rt.applyFont((short)1);
    }

    @Test
    void testApplyFontException() {
        XSSFRichTextString rt = new XSSFRichTextString("Apache POI");

        rt.applyFont(0, 0, (short)1);
        IllegalArgumentException e;
        e = assertThrows(IllegalArgumentException.class, () -> rt.applyFont(11, 10, (short)1));
        assertTrue(e.getMessage().contains("11"));

        e = assertThrows(IllegalArgumentException.class, () -> rt.applyFont(-1, 10, (short)1));
        assertTrue(e.getMessage().contains("-1"));

        e = assertThrows(IllegalArgumentException.class, () -> rt.applyFont(0, 555, (short)1));
        assertTrue(e.getMessage().contains("555"));
    }

    @Test
    void testClearFormatting() {

        XSSFRichTextString rt = new XSSFRichTextString("Apache POI");
        assertEquals("Apache POI", rt.getString());
        assertFalse(rt.hasFormatting());

        rt.clearFormatting();

        CTRst st = rt.getCTRst();
        assertTrue(st.isSetT());
        assertEquals("Apache POI", rt.getString());
        assertEquals(0, rt.numFormattingRuns());
        assertFalse(rt.hasFormatting());

        XSSFFont font = new XSSFFont();
        font.setBold(true);

        rt.applyFont(7, 10, font);
        assertEquals(2, rt.numFormattingRuns());
        assertTrue(rt.hasFormatting());

        rt.clearFormatting();

        assertEquals("Apache POI", rt.getString());
        assertEquals(0, rt.numFormattingRuns());
        assertFalse(rt.hasFormatting());
    }

    @Test
    void testGetFonts() {

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
    @Test
    void testPreserveSpaces() {
        XSSFRichTextString rt = new XSSFRichTextString("Apache");
        CTRst ct = rt.getCTRst();
        STXstring xs = ct.xgetT();
        assertEquals("<xml-fragment>Apache</xml-fragment>", xs.xmlText());
        rt.setString("  Apache");
        assertEquals("<xml-fragment xml:space=\"preserve\">  Apache</xml-fragment>", xs.xmlText());

        rt.append(" POI");
        rt.append(" ");
        assertEquals("  Apache POI ", rt.getString());
        assertEquals("<xml-fragment xml:space=\"preserve\">  Apache</xml-fragment>", rt.getCTRst().getRArray(0).xgetT().xmlText());
        assertEquals("<xml-fragment xml:space=\"preserve\"> POI</xml-fragment>", rt.getCTRst().getRArray(1).xgetT().xmlText());
        assertEquals("<xml-fragment xml:space=\"preserve\"> </xml-fragment>", rt.getCTRst().getRArray(2).xgetT().xmlText());
    }

    /**
     * test that unicode representation_ xHHHH_ is properly processed
     */
    @Test
    void testUtfDecode() {
        CTRst st = CTRst.Factory.newInstance();
        st.setT("abc_x000D_2ef_x000D_");
        XSSFRichTextString rt = new XSSFRichTextString(st);
        //_x000D_ is converted into carriage return
        assertEquals("abc\r2ef\r", rt.getString());

        // Test Lowercase case
        CTRst st2 = CTRst.Factory.newInstance();
        st2.setT("abc_x000d_2ef_x000d_");
        XSSFRichTextString rt2 = new XSSFRichTextString(st2);
        assertEquals("abc\r2ef\r", rt2.getString());
    }

    @Test
    void testApplyFont_lowlevel(){
        CTRst st = CTRst.Factory.newInstance();
        String text = "Apache Software Foundation";
        XSSFRichTextString str = new XSSFRichTextString(text);
        assertEquals(26, text.length());

        st.addNewR().setT(text);

        TreeMap<Integer, CTRPrElt> formats = str.getFormatMap(st);
        assertEquals(1, formats.size());
        assertEquals(26, (int)formats.firstKey());
        assertNull(formats.get( formats.firstKey() ));

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

    @Test
    void testApplyFont_usermodel(){
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

    @Test
    void testLineBreaks_bug48877() {

        XSSFFont font = new XSSFFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        XSSFRichTextString str;
        STXstring t1, t2, t3;

        str = new XSSFRichTextString("Incorrect\nLine-Breaking");
        str.applyFont(0, 8, font);
        t1 = str.getCTRst().getRArray(0).xgetT();
        t2 = str.getCTRst().getRArray(1).xgetT();
        assertEquals("<xml-fragment>Incorrec</xml-fragment>", t1.xmlText());
        assertEquals("<xml-fragment>t\nLine-Breaking</xml-fragment>", t2.xmlText());

        str = new XSSFRichTextString("Incorrect\nLine-Breaking");
        str.applyFont(0, 9, font);
        t1 = str.getCTRst().getRArray(0).xgetT();
        t2 = str.getCTRst().getRArray(1).xgetT();
        assertEquals("<xml-fragment>Incorrect</xml-fragment>", t1.xmlText());
        assertEquals("<xml-fragment xml:space=\"preserve\">\nLine-Breaking</xml-fragment>", t2.xmlText());

        str = new XSSFRichTextString("Incorrect\n Line-Breaking");
        str.applyFont(0, 9, font);
        t1 = str.getCTRst().getRArray(0).xgetT();
        t2 = str.getCTRst().getRArray(1).xgetT();
        assertEquals("<xml-fragment>Incorrect</xml-fragment>", t1.xmlText());
        assertEquals("<xml-fragment xml:space=\"preserve\">\n Line-Breaking</xml-fragment>", t2.xmlText());

        str = new XSSFRichTextString("Tab\tseparated\n");
        t1 = str.getCTRst().xgetT();
        // trailing \n causes must be preserved
        assertEquals("<xml-fragment xml:space=\"preserve\">Tab\tseparated\n</xml-fragment>", t1.xmlText());

        str.applyFont(0, 3, font);
        t1 = str.getCTRst().getRArray(0).xgetT();
        t2 = str.getCTRst().getRArray(1).xgetT();
        assertEquals("<xml-fragment>Tab</xml-fragment>", t1.xmlText());
        assertEquals("<xml-fragment xml:space=\"preserve\">\tseparated\n</xml-fragment>", t2.xmlText());

        str = new XSSFRichTextString("Tab\tseparated\n");
        str.applyFont(0, 4, font);
        t1 = str.getCTRst().getRArray(0).xgetT();
        t2 = str.getCTRst().getRArray(1).xgetT();
        assertEquals("<xml-fragment xml:space=\"preserve\">Tab\t</xml-fragment>", t1.xmlText());
        assertEquals("<xml-fragment xml:space=\"preserve\">separated\n</xml-fragment>", t2.xmlText());

        str = new XSSFRichTextString("\n\n\nNew Line\n\n");
        str.applyFont(0, 3, font);
        str.applyFont(11, 13, font);
        t1 = str.getCTRst().getRArray(0).xgetT();
        t2 = str.getCTRst().getRArray(1).xgetT();
        t3 = str.getCTRst().getRArray(2).xgetT();
        // YK: don't know why, but XmlBeans converts leading tab characters to spaces
        assertEquals("<xml-fragment xml:space=\"preserve\">\n\n\n</xml-fragment>", t1.xmlText());
        assertEquals("<xml-fragment>New Line</xml-fragment>", t2.xmlText());
        assertEquals("<xml-fragment xml:space=\"preserve\">\n\n</xml-fragment>", t3.xmlText());
    }

    @Test
    void testBug56511() throws IOException {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("56511.xlsx")) {
            for (Sheet sheet : wb) {
                int lastRow = sheet.getLastRowNum();
                for (int rowIdx = sheet.getFirstRowNum(); rowIdx <= lastRow; rowIdx++) {
                    Row row = sheet.getRow(rowIdx);
                    if (row != null) {
                        int lastCell = row.getLastCellNum();

                        for (int cellIdx = row.getFirstCellNum(); cellIdx <= lastCell; cellIdx++) {

                            Cell cell = row.getCell(cellIdx);
                            if (cell != null) {
                                //System.out.println("row " + rowIdx + " column " + cellIdx + ": " + cell.getCellType() + ": " + cell.toString());

                                XSSFRichTextString richText = (XSSFRichTextString) cell.getRichStringCellValue();
                                int anzFormattingRuns = richText.numFormattingRuns();
                                for (int run = 0; run < anzFormattingRuns; run++) {
                                    /*XSSFFont font =*/ richText.getFontOfFormattingRun(run);
                                    //System.out.println("  run " + run
                                    //        + " font " + (font == null ? "<null>" : font.getFontName()));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    void testBug56511_values() throws IOException {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("56511.xlsx")) {
            Sheet sheet = wb.getSheetAt(0);
            Row row = sheet.getRow(0);

            // verify the values to ensure future changes keep the returned information equal
            XSSFRichTextString rt = (XSSFRichTextString) row.getCell(0).getRichStringCellValue();
            assertEquals(0, rt.numFormattingRuns());
            assertNull(rt.getFontOfFormattingRun(0));
            assertEquals(-1, rt.getLengthOfFormattingRun(0));

            rt = (XSSFRichTextString) row.getCell(1).getRichStringCellValue();
            assertEquals(0, row.getCell(1).getRichStringCellValue().numFormattingRuns());
            assertNull(rt.getFontOfFormattingRun(1));
            assertEquals(-1, rt.getLengthOfFormattingRun(1));

            rt = (XSSFRichTextString) row.getCell(2).getRichStringCellValue();
            assertEquals(2, rt.numFormattingRuns());
            assertNotNull(rt.getFontOfFormattingRun(0));
            assertEquals(4, rt.getLengthOfFormattingRun(0));

            assertNotNull(rt.getFontOfFormattingRun(1));
            assertEquals(9, rt.getLengthOfFormattingRun(1));

            assertNull(rt.getFontOfFormattingRun(2));

            rt = (XSSFRichTextString) row.getCell(3).getRichStringCellValue();
            assertEquals(3, rt.numFormattingRuns());
            assertNull(rt.getFontOfFormattingRun(0));
            assertEquals(1, rt.getLengthOfFormattingRun(0));

            assertNotNull(rt.getFontOfFormattingRun(1));
            assertEquals(3, rt.getLengthOfFormattingRun(1));

            assertNotNull(rt.getFontOfFormattingRun(2));
            assertEquals(9, rt.getLengthOfFormattingRun(2));
        }
    }

    @Test
    void testToString() {
        XSSFRichTextString rt = new XSSFRichTextString("Apache POI");
        assertNotNull(rt.toString());

        rt = new XSSFRichTextString();
        assertEquals("", rt.toString());
    }

    @Test
    void test59008Font() {
        XSSFFont font = new XSSFFont(CTFont.Factory.newInstance());

        XSSFRichTextString rts = new XSSFRichTextString();
        rts.append("This is correct ");
        int s1 = rts.length();
        rts.append("This is Bold Red", font);
        int s2 = rts.length();
        rts.append(" This uses the default font rather than the cell style font");
        int s3 = rts.length();

        assertEquals("<xml-fragment/>", rts.getFontAtIndex(s1-1).toString());
        assertEquals(font, rts.getFontAtIndex(s2-1));
        assertEquals("<xml-fragment/>", rts.getFontAtIndex(s3-1).toString());
    }

    @Test
    void test60289UtfDecode() throws IOException {
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("60289.xlsx");
        assertEquals("Rich Text\r\nTest", wb.getSheetAt(0).getRow(1).getCell(1).getRichStringCellValue().getString());
        wb.close();
    }

    @Test
    void testUtfDecode_withApplyFont() {
        XSSFFont font = new XSSFFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);

        CTRst st = CTRst.Factory.newInstance();
        st.setT("abc_x000D_2ef_x000D_");
        XSSFRichTextString rt = new XSSFRichTextString(st);
        rt.applyFont(font);
        assertEquals("abc\r2ef\r", rt.getString());
    }

    @Test
    void testUtfLength() {
        assertEquals(0, XSSFRichTextString.utfLength(null));
        assertEquals(0, XSSFRichTextString.utfLength(""));

        assertEquals(3, XSSFRichTextString.utfLength("abc"));
        assertEquals(3, XSSFRichTextString.utfLength("ab_x0032_"));
        assertEquals(3, XSSFRichTextString.utfLength("a_x0032__x0032_"));
        assertEquals(3, XSSFRichTextString.utfLength("_x0032_a_x0032_"));
        assertEquals(3, XSSFRichTextString.utfLength("_x0032__x0032_a"));
    }
}
