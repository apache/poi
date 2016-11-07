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

package org.apache.poi.xwpf.usermodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.poi.xwpf.XWPFTestDataSamples;
import org.junit.Ignore;
import org.junit.Test;

public final class TestXWPFSDT {

    /**
     * Test simple tag and title extraction from SDT
     *
     * @throws Exception
     */
    @Test
    public void testTagTitle() throws Exception {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("Bug54849.docx");
        String tag = null;
        String title = null;
        List<AbstractXWPFSDT> sdts = extractAllSDTs(doc);
        for (AbstractXWPFSDT sdt : sdts) {
            if (sdt.getContent().toString().equals("Rich_text")) {
                tag = "MyTag";
                title = "MyTitle";
                break;
            }

        }
        assertEquals("controls size", 13, sdts.size());

        assertEquals("tag", "MyTag", tag);
        assertEquals("title", "MyTitle", title);
    }

    @Test
    public void testGetSDTs() throws Exception {
        String[] contents = new String[]{
                "header_rich_text",
                "Rich_text",
                "Rich_text_pre_table\nRich_text_cell1\t\t\t\n\t\t\t\n\t\t\t\n\nRich_text_post_table",
                "Plain_text_no_newlines",
                "Plain_text_with_newlines1\nplain_text_with_newlines2",
                "Watermelon",
                "Dirt",
                "4/16/2013",
                "Rich_text_in_cell",
                "rich_text_in_paragraph_in_cell",
                "Footer_rich_text",
                "Footnote_sdt",
                "Endnote_sdt"

        };
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("Bug54849.docx");
        List<AbstractXWPFSDT> sdts = extractAllSDTs(doc);

        assertEquals("number of sdts", contents.length, sdts.size());

        for (int i = 0; i < contents.length; i++) {
            AbstractXWPFSDT sdt = sdts.get(i);
            assertEquals(i + ": " + contents[i], contents[i], sdt.getContent().toString());
        }
    }

    /**
     * POI-54771 and TIKA-1317
     */
    @Test
    public void testSDTAsCell() throws Exception {
        //Bug54771a.docx and Bug54771b.docx test slightly 
        //different recursion patterns. Keep both!
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("Bug54771a.docx");
        List<AbstractXWPFSDT> sdts = extractAllSDTs(doc);
        String text = sdts.get(0).getContent().getText();
        assertEquals(2, sdts.size());
        assertTrue(text.indexOf("Test") > -1);

        text = sdts.get(1).getContent().getText();
        assertTrue(text.indexOf("Test Subtitle") > -1);
        assertTrue(text.indexOf("Test User") > -1);
        assertTrue(text.indexOf("Test") < text.indexOf("Test Subtitle"));

        doc = XWPFTestDataSamples.openSampleDocument("Bug54771b.docx");
        sdts = extractAllSDTs(doc);
        assertEquals(3, sdts.size());
        assertTrue(sdts.get(0).getContent().getText().indexOf("Test") > -1);

        assertTrue(sdts.get(1).getContent().getText().indexOf("Test Subtitle") > -1);
        assertTrue(sdts.get(2).getContent().getText().indexOf("Test User") > -1);

    }

    /**
     * POI-55142 and Tika 1130
     */
    @Test
    public void testNewLinesBetweenRuns() throws Exception {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("Bug55142.docx");
        List<AbstractXWPFSDT> sdts = extractAllSDTs(doc);
        List<String> targs = new ArrayList<String>();
        //these test newlines and tabs in paragraphs/body elements
        targs.add("Rich-text1 abcdefghi");
        targs.add("Rich-text2 abcd\t\tefgh");
        targs.add("Rich-text3 abcd\nefg");
        targs.add("Rich-text4 abcdefg");
        targs.add("Rich-text5 abcdefg\nhijk");
        targs.add("Plain-text1 abcdefg");
        targs.add("Plain-text2 abcdefg\nhijk\nlmnop");
        //this tests consecutive runs within a cell (not a paragraph)
        //this test case was triggered by Tika-1130
        targs.add("sdt_incell2 abcdefg");

        for (int i = 0; i < sdts.size(); i++) {
            AbstractXWPFSDT sdt = sdts.get(i);
            assertEquals(targs.get(i), targs.get(i), sdt.getContent().getText());
        }
    }

    @Test
    public void test60341() throws IOException {
        //handle sdtbody without an sdtpr
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("Bug60341.docx");
        List<AbstractXWPFSDT> sdts = extractAllSDTs(doc);
        assertEquals(1, sdts.size());
        assertEquals("", sdts.get(0).getTag());
        assertEquals("", sdts.get(0).getTitle());
    }

    private List<AbstractXWPFSDT> extractAllSDTs(XWPFDocument doc) {

        List<AbstractXWPFSDT> sdts = new ArrayList<AbstractXWPFSDT>();

        List<XWPFHeader> headers = doc.getHeaderList();
        for (XWPFHeader header : headers) {
            sdts.addAll(extractSDTsFromBodyElements(header.getBodyElements()));
        }
        sdts.addAll(extractSDTsFromBodyElements(doc.getBodyElements()));

        List<XWPFFooter> footers = doc.getFooterList();
        for (XWPFFooter footer : footers) {
            sdts.addAll(extractSDTsFromBodyElements(footer.getBodyElements()));
        }

        for (XWPFFootnote footnote : doc.getFootnotes()) {
            sdts.addAll(extractSDTsFromBodyElements(footnote.getBodyElements()));
        }
        for (Map.Entry<Integer, XWPFFootnote> e : doc.endnotes.entrySet()) {
            sdts.addAll(extractSDTsFromBodyElements(e.getValue().getBodyElements()));
        }
        return sdts;
    }

    private List<AbstractXWPFSDT> extractSDTsFromBodyElements(List<IBodyElement> elements) {
        List<AbstractXWPFSDT> sdts = new ArrayList<AbstractXWPFSDT>();
        for (IBodyElement e : elements) {
            if (e instanceof XWPFSDT) {
                XWPFSDT sdt = (XWPFSDT) e;
                sdts.add(sdt);
            } else if (e instanceof XWPFParagraph) {

                XWPFParagraph p = (XWPFParagraph) e;
                for (IRunElement e2 : p.getIRuns()) {
                    if (e2 instanceof XWPFSDT) {
                        XWPFSDT sdt = (XWPFSDT) e2;
                        sdts.add(sdt);
                    }
                }
            } else if (e instanceof XWPFTable) {
                XWPFTable table = (XWPFTable) e;
                sdts.addAll(extractSDTsFromTable(table));
            }
        }
        return sdts;
    }

    private List<AbstractXWPFSDT> extractSDTsFromTable(XWPFTable table) {

        List<AbstractXWPFSDT> sdts = new ArrayList<AbstractXWPFSDT>();
        for (XWPFTableRow r : table.getRows()) {
            for (ICell c : r.getTableICells()) {
                if (c instanceof XWPFSDTCell) {
                    sdts.add((XWPFSDTCell) c);
                } else if (c instanceof XWPFTableCell) {
                    sdts.addAll(extractSDTsFromBodyElements(((XWPFTableCell) c).getBodyElements()));
                }
            }
        }
        return sdts;
    }
}
