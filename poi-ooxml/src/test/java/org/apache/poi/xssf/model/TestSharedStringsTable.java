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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTRElt;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTRPrElt;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTRst;

/**
 * Test {@link SharedStringsTable}, the cache of strings in a workbook
 */
public final class TestSharedStringsTable {
    @Test
    void testCreateNew() {
        SharedStringsTable sst = new SharedStringsTable();

        CTRst st;
        int idx;

        // Check defaults
        assertEquals(0, sst.getCount());
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
        assertEquals("Hello, World!", sst.getItemAt(0).toString());
        assertEquals("Second string", sst.getItemAt(1).toString());
        assertEquals("Second string", sst.getItemAt(2).toString());
    }

    @Test
    void testCreateUsingRichTextStrings() {
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

    @Test
    void testReadWrite() throws IOException {
        XSSFWorkbook wb1 = XSSFTestDataSamples.openSampleWorkbook("sample.xlsx");
        SharedStringsTable sst1 = wb1.getSharedStringSource();

        //serialize, read back and compare with the original
        XSSFWorkbook wb2 = XSSFTestDataSamples.writeOutAndReadBack(wb1);
        SharedStringsTable sst2 = wb2.getSharedStringSource();

        assertEquals(sst1.getCount(), sst2.getCount());
        assertEquals(sst1.getUniqueCount(), sst2.getUniqueCount());

        assertEquals(sst1.getCount(), sst2.getCount());

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
     */
    @Test
    void testBug48936() throws IOException {
        Workbook w1 = new XSSFWorkbook();
        Sheet s = w1.createSheet();
        int i = 0;

        Path path = XSSFTestDataSamples.getSampleFile("48936-strings.txt").toPath();

        final List<String> lst;
        try (Stream<String> lines = Files.lines(path, StandardCharsets.UTF_8)) {
            lst = lines
                    .map(String::trim)
                    .filter(((Predicate<String>) String::isEmpty).negate())
                    .collect(Collectors.toList());
        }

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
}
