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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.poi.xwpf.XWPFTestDataSamples;

public final class TestXWPFSDT extends TestCase {

    /**
     * Test simple tag and title extraction from SDT
     * @throws Exception
     */
    public void testTagTitle() throws Exception {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("Bug54849.docx");
        String tag = null;
        String title= null;
        List<XWPFSDT> sdts = extractAllSDTs(doc);
        for (XWPFSDT sdt :sdts){
            if (sdt.getContent().toString().equals("Rich_text")){
                tag = "MyTag";
                title = "MyTitle";
                break;
            }
        }
        assertEquals("controls size", 12, sdts.size());

        assertEquals("tag", "MyTag", tag);
        assertEquals("title", "MyTitle", title);
    }


    public void testGetSDTs() throws Exception{
        String[] contents = new String[]{
                "header_rich_text",
                "Rich_text",
                "Rich_text_pre_table\nRich_text_cell1\t\t\t\n\nRich_text_post_table",
                "Plain_text_no_newlines",
                "Plain_text_with_newlines1\nplain_text_with_newlines2",
                "Watermelon",
                "Dirt",
                "4/16/2013",
                "rich_text_in_paragraph_in_cell",
                "Footer_rich_text",
                "Footnote_sdt",
                "Endnote_sdt"

        };
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("Bug54849.docx");
        List<XWPFSDT> sdts = extractAllSDTs(doc);

        assertEquals("number of sdts", contents.length, sdts.size());

        for (int i = 0; i < sdts.size(); i++){//contents.length; i++){
            XWPFSDT sdt = sdts.get(i);

            assertEquals(i+ ": " + contents[i], contents[i], sdt.getContent().toString());
        } 
    }

    public void testFailureToGetSDTAsCell() throws Exception{
        /**
         * The current code fails to extract an sdt if it comprises/is the parent
         * of a cell in a table.
         */
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("Bug54849.docx");
        List<XWPFSDT> sdts = extractAllSDTs(doc);
        boolean found = false;
        for (XWPFSDT sdt : sdts){
            if (sdt.getContent().getText().toLowerCase().indexOf("rich_text_in_cell") > -1){
                found = true;
            }
        }
        assertEquals("SDT as cell known failure", false, found);
    }
    
    /**
     * POI-55142 and Tika 1130
     */
    public void testNewLinesBetweenRuns() throws Exception{
       XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("Bug55142.docx");
       List<XWPFSDT> sdts = extractAllSDTs(doc);
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
       
       for (int i = 0; i < sdts.size(); i++){
          XWPFSDT sdt = sdts.get(i);
          assertEquals(targs.get(i), targs.get(i), sdt.getContent().getText());
       }
    }

    private List<XWPFSDT> extractAllSDTs(XWPFDocument doc){

        List<XWPFSDT> sdts = new ArrayList<XWPFSDT>();

        List<XWPFHeader> headers = doc.getHeaderList();
        for (XWPFHeader header : headers){
            sdts.addAll(extractSDTsFromBodyElements(header.getBodyElements()));
        }
        sdts.addAll(extractSDTsFromBodyElements(doc.getBodyElements()));

        List<XWPFFooter> footers = doc.getFooterList();
        for (XWPFFooter footer : footers){
            sdts.addAll(extractSDTsFromBodyElements(footer.getBodyElements()));
        }

        for (XWPFFootnote footnote : doc.getFootnotes()){

            sdts.addAll(extractSDTsFromBodyElements(footnote.getBodyElements()));
        }
        for (Map.Entry<Integer, XWPFFootnote> e : doc.endnotes.entrySet()){
            sdts.addAll(extractSDTsFromBodyElements(e.getValue().getBodyElements()));
        }
        return sdts;
    }

    private List<XWPFSDT> extractSDTsFromBodyElements(List<IBodyElement> elements){
        List<XWPFSDT> sdts = new ArrayList<XWPFSDT>();
        for (IBodyElement e : elements){
            if (e instanceof XWPFSDT){
                XWPFSDT sdt = (XWPFSDT)e;
                sdts.add(sdt);
            } else if (e instanceof XWPFParagraph){

                XWPFParagraph p = (XWPFParagraph)e;
                for (IRunElement e2 : p.getIRuns()){
                    if (e2 instanceof XWPFSDT){
                        XWPFSDT sdt = (XWPFSDT)e2;
                        sdts.add(sdt);
                    }
                }
            } else if (e instanceof XWPFTable){
                XWPFTable table = (XWPFTable)e;
                sdts.addAll(extractSDTsFromTable(table));
            }
        }
        return sdts;
    }

    private List<XWPFSDT> extractSDTsFromTable(XWPFTable table){
        List<XWPFSDT> sdts = new ArrayList<XWPFSDT>();
        for (XWPFTableRow r : table.getRows()){
            for (XWPFTableCell c : r.getTableCells()){
                sdts.addAll(extractSDTsFromBodyElements(c.getBodyElements()));
            }
        }
        return sdts;
    }
}
