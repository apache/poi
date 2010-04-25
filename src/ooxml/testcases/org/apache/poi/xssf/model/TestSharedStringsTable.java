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

import java.util.List;
import java.util.ArrayList;
import java.io.*;

import junit.framework.TestCase;

import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.POIDataSamples;
import org.apache.poi.POIXMLException;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTRElt;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTRPrElt;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTRst;

/**
 * Test {@link SharedStringsTable}, the cache of strings in a workbook
 *
 * @author Yegor Kozlov
 */
public final class TestSharedStringsTable extends TestCase {

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

    public void testReadWrite() {
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("sample.xlsx");
        SharedStringsTable sst1 = wb.getSharedStringSource();

        //serialize, read back and compare with the original
        SharedStringsTable sst2 = XSSFTestDataSamples.writeOutAndReadBack(wb).getSharedStringSource();

        assertEquals(sst1.getCount(), sst2.getCount());
        assertEquals(sst1.getUniqueCount(), sst2.getUniqueCount());

        List<CTRst> items1 = sst1.getItems();
        List<CTRst> items2 = sst2.getItems();
        assertEquals(items1.size(), items2.size());
        for (int i = 0; i < items1.size(); i++) {
            CTRst st1 = items1.get(i);
            CTRst st2 = items2.get(i);
            assertEquals(st1.toString(), st2.toString());
        }
    }

    /**
     * Test for Bugzilla 48936
     *
     * A specific sequence of strings can result in broken CDATA section in sharedStrings.xml file.
     *
     * @author Philippe Laflamme
     */
    public void testBug48936() throws IOException {
        Workbook w = new XSSFWorkbook();
        Sheet s = w.createSheet();
        int i = 0;
        List<String> lst = readStrings("48936-strings.txt");
        for (String str : lst) {
            s.createRow(i++).createCell(0).setCellValue(str);
        }

        try {
            w = XSSFTestDataSamples.writeOutAndReadBack(w);
        } catch (POIXMLException e){
            fail("Detected Bug #48936");        
        }
        s = w.getSheetAt(0);
        i = 0;
        for (String str : lst) {
            String val = s.getRow(i++).getCell(0).getStringCellValue();
            assertEquals(str, val);
        }
    }

    private List<String> readStrings(String filename) throws IOException {
        List<String> strs = new ArrayList<String>();
        POIDataSamples samples = POIDataSamples.getSpreadSheetInstance();
        BufferedReader br = new BufferedReader(
                new InputStreamReader(samples.openResourceAsStream(filename)));
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
