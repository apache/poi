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

package org.apache.poi.xssf.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.POIDataSamples;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTPhoneticRun;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTRElt;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTRPrElt;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTRst;

import junit.framework.TestCase;

/**
 * Test {@link SharedStringsTable}, the cache of strings in a workbook
 *
 * @author Yegor Kozlov
 */
public final class TestSharedStringsTable extends TestCase {

    @SuppressWarnings("deprecation")
    public void testCreateNew() {
        SharedStringsTable sst = new SharedStringsTable();

        CTRst st;
        int idx;

        // Check defaults
        assertNotNull(sst.getItems());
        assertEquals(0, sst.getItems().size());
        assertEquals(0, sst.getCount());
        assertEquals(0, sst.getUniqueCount());

        st = CTRst.Factory.newInstance();
        st.setT("Hello, World!");

        idx = sst.addEntry(st);
        assertEquals(0, idx);
        assertEquals(1, sst.getCount());
        assertEquals(1, sst.getUniqueCount());

        //add the same entry again
        idx = sst.addEntry(st);
        assertEquals(0, idx);
        assertEquals(2, sst.getCount());
        assertEquals(1, sst.getUniqueCount());

        //and again
        idx = sst.addEntry(st);
        assertEquals(0, idx);
        assertEquals(3, sst.getCount());
        assertEquals(1, sst.getUniqueCount());

        st = CTRst.Factory.newInstance();
        st.setT("Second string");

        idx = sst.addEntry(st);
        assertEquals(1, idx);
        assertEquals(4, sst.getCount());
        assertEquals(2, sst.getUniqueCount());

        //add the same entry again
        idx = sst.addEntry(st);
        assertEquals(1, idx);
        assertEquals(5, sst.getCount());
        assertEquals(2, sst.getUniqueCount());

        st = CTRst.Factory.newInstance();
        CTRElt r = st.addNewR();
        CTRPrElt pr = r.addNewRPr();
        pr.addNewColor().setRgb(new byte[]{(byte)0xFF, 0, 0}); //red
        pr.addNewI().setVal(true);  //bold
        pr.addNewB().setVal(true);  //italic
        r.setT("Second string");

        idx = sst.addEntry(st);
        assertEquals(2, idx);
        assertEquals(6, sst.getCount());
        assertEquals(3, sst.getUniqueCount());

        idx = sst.addEntry(st);
        assertEquals(2, idx);
        assertEquals(7, sst.getCount());
        assertEquals(3, sst.getUniqueCount());

        //OK. the sst table is filled, check the contents
        assertEquals(3, sst.getItems().size());
        assertEquals("Hello, World!", new XSSFRichTextString(sst.getEntryAt(0)).toString());
        assertEquals("Second string", new XSSFRichTextString(sst.getEntryAt(1)).toString());
        assertEquals("Second string", new XSSFRichTextString(sst.getEntryAt(2)).toString());
    }

    public void testCreateUsingRichTextStrings() {
        SharedStringsTable sst = new SharedStringsTable();

        // Check defaults
        assertNotNull(sst.getSharedStringItems());
        assertEquals(0, sst.getSharedStringItems().size());
        assertEquals(0, sst.getCount());
        assertEquals(0, sst.getUniqueCount());

        int idx;

        XSSFRichTextString rts = new XSSFRichTextString("Hello, World!");

        idx = sst.addSharedStringItem(rts);
        assertEquals(0, idx);
        assertEquals(1, sst.getCount());
        assertEquals(1, sst.getUniqueCount());

        //add the same entry again
        idx = sst.addSharedStringItem(rts);
        assertEquals(0, idx);
        assertEquals(2, sst.getCount());
        assertEquals(1, sst.getUniqueCount());

        //and again
        idx = sst.addSharedStringItem(rts);
        assertEquals(0, idx);
        assertEquals(3, sst.getCount());
        assertEquals(1, sst.getUniqueCount());

        rts = new XSSFRichTextString("Second string");

        idx = sst.addSharedStringItem(rts);
        assertEquals(1, idx);
        assertEquals(4, sst.getCount());
        assertEquals(2, sst.getUniqueCount());

        //add the same entry again
        idx = sst.addSharedStringItem(rts);
        assertEquals(1, idx);
        assertEquals(5, sst.getCount());
        assertEquals(2, sst.getUniqueCount());

        rts = new XSSFRichTextString("Second string");
        XSSFFont font = new XSSFFont();
        font.setFontName("Arial");
        font.setBold(true);
        rts.applyFont(font);

        idx = sst.addSharedStringItem(rts);
        assertEquals(2, idx);
        assertEquals(6, sst.getCount());
        assertEquals(3, sst.getUniqueCount());

        idx = sst.addSharedStringItem(rts);
        assertEquals(2, idx);
        assertEquals(7, sst.getCount());
        assertEquals(3, sst.getUniqueCount());

        //OK. the sst table is filled, check the contents
        assertEquals(3, sst.getSharedStringItems().size());
        assertEquals("Hello, World!", sst.getItemAt(0).toString());
        assertEquals("Second string", sst.getItemAt(1).toString());
        assertEquals("Second string", sst.getItemAt(2).toString());
    }

    @SuppressWarnings("deprecation")
    public void testReadWrite() throws IOException {
        XSSFWorkbook wb1 = XSSFTestDataSamples.openSampleWorkbook("sample.xlsx");
        SharedStringsTable sst1 = wb1.getSharedStringSource();

        //serialize, read back and compare with the original
        XSSFWorkbook wb2 = XSSFTestDataSamples.writeOutAndReadBack(wb1);
        SharedStringsTable sst2 = wb2.getSharedStringSource();

        assertEquals(sst1.getCount(), sst2.getCount());
        assertEquals(sst1.getUniqueCount(), sst2.getUniqueCount());

        List<CTRst> items1 = sst1.getItems();
        List<CTRst> items2 = sst2.getItems();
        assertEquals(items1.size(), items2.size());
        for (int i = 0; i < items1.size(); i++) {
            CTRst st1 = items1.get(i);
            CTRst st2 = items2.get(i);
            assertEquals(st1.toString(), st2.toString());
            // ensure that CTPhoneticRun is loaded by the ooxml test suite so that it is included in poi-ooxml-schemas
            List<CTPhoneticRun> phList = st1.getRPhList();
            assertEquals(phList, st2.getRPhList());
            // this code is required to make sure all the necessary classes are loaded
            CTPhoneticRun run = CTPhoneticRun.Factory.newInstance();
            run.setEb(12);
            assertEquals(12, run.getEb());
        }

        XSSFWorkbook wb3 = XSSFTestDataSamples.writeOutAndReadBack(wb2);
        assertNotNull(wb3);
        wb3.close();
        wb2.close();
        wb1.close();
    }

    /**
     * Test for Bugzilla 48936
     *
     * A specific sequence of strings can result in broken CDATA section in sharedStrings.xml file.
     *
     * @author Philippe Laflamme
     */
    public void testBug48936() throws IOException {
        Workbook w1 = new XSSFWorkbook();
        Sheet s = w1.createSheet();
        int i = 0;
        List<String> lst = readStrings("48936-strings.txt");
        for (String str : lst) {
            s.createRow(i++).createCell(0).setCellValue(str);
        }

        Workbook w2 = XSSFTestDataSamples.writeOutAndReadBack(w1);
        w1.close();
        s = w2.getSheetAt(0);
        i = 0;
        for (String str : lst) {
            String val = s.getRow(i++).getCell(0).getStringCellValue();
            assertEquals(str, val);
        }
        
        Workbook w3 = XSSFTestDataSamples.writeOutAndReadBack(w2);
        w2.close();
        assertNotNull(w3);
        w3.close();
    }

    private List<String> readStrings(String filename) throws IOException {
        List<String> strs = new ArrayList<>();
        POIDataSamples samples = POIDataSamples.getSpreadSheetInstance();
        BufferedReader br = new BufferedReader(
                new InputStreamReader(samples.openResourceAsStream(filename), "UTF-8"));
        String s;
        while ((s = br.readLine()) != null) {
            if (s.trim().length() > 0) {
                strs.add(s.trim());
            }
        }
        br.close();
        return strs;
    }

}
