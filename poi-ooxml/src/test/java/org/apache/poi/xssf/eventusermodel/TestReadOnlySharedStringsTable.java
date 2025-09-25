/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.xssf.eventusermodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.poi.POIDataSamples;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

/**
 * Tests for {@link org.apache.poi.xssf.eventusermodel.XSSFReader}
 */
public final class TestReadOnlySharedStringsTable {
    private static final POIDataSamples _ssTests = POIDataSamples.getSpreadSheetInstance();

    @Test
    void testParse() throws Exception {
        try (OPCPackage pkg = OPCPackage.open(_ssTests.openResourceAsStream("SampleSS.xlsx"))) {
            List<PackagePart> parts = pkg.getPartsByName(Pattern.compile("/xl/sharedStrings.xml"));
            assertEquals(1, parts.size());

            try (SharedStringsTable stbl = new SharedStringsTable(parts.get(0))) {
                ReadOnlySharedStringsTable rtbl = new ReadOnlySharedStringsTable(parts.get(0));
                ReadOnlySharedStringsTable rtbl2;
                try (InputStream stream = parts.get(0).getInputStream()) {
                    rtbl2 = new ReadOnlySharedStringsTable(stream);
                }

                assertEquals(stbl.getCount(), rtbl.getCount());
                assertEquals(stbl.getUniqueCount(), rtbl.getUniqueCount());
                assertEquals(stbl.getUniqueCount(), rtbl2.getUniqueCount());

                assertEquals(stbl.getCount(), stbl.getUniqueCount());
                assertEquals(rtbl.getCount(), rtbl.getUniqueCount());
                assertEquals(rtbl.getCount(), rtbl2.getUniqueCount());
                for (int i = 0; i < stbl.getUniqueCount(); i++) {
                    RichTextString i1 = stbl.getItemAt(i);
                    assertEquals(i1.getString(), rtbl.getItemAt(i).getString());
                    assertEquals(i1.getString(), rtbl2.getItemAt(i).getString());
                }

                // verify invalid indices
                assertThrows(IllegalStateException.class,
                        () -> rtbl.getItemAt(stbl.getUniqueCount()));
                assertThrows(IndexOutOfBoundsException.class,
                        () -> rtbl.getItemAt(-1));
            }
        }
    }

    @Test
    void testParseMalformedCountFile() throws Exception {
        try (OPCPackage pkg = OPCPackage.open(_ssTests.openResourceAsStream("MalformedSSTCount.xlsx"))) {
            List<PackagePart> parts = pkg.getPartsByName(Pattern.compile("/xl/sharedStrings.xml"));
            assertEquals(1, parts.size());

            try (SharedStringsTable stbl = new SharedStringsTable(parts.get(0))) {
                ReadOnlySharedStringsTable rtbl = new ReadOnlySharedStringsTable(parts.get(0));
                ReadOnlySharedStringsTable rtbl2;
                try (InputStream stream = parts.get(0).getInputStream()) {
                    rtbl2 = new ReadOnlySharedStringsTable(stream);
                }

                assertEquals(stbl.getCount(), rtbl.getCount());
                assertEquals(stbl.getUniqueCount(), rtbl.getUniqueCount());
                assertEquals(stbl.getUniqueCount(), rtbl2.getUniqueCount());
                for (int i = 0; i < stbl.getUniqueCount(); i++) {
                    RichTextString i1 = stbl.getItemAt(i);
                    assertEquals(i1.getString(), rtbl.getItemAt(i).getString());
                    assertEquals(i1.getString(), rtbl2.getItemAt(i).getString());
                }
            }
        }
    }

    //51519
    @Test
    void testPhoneticRuns() throws Exception {
        try (OPCPackage pkg = OPCPackage.open(_ssTests.openResourceAsStream("51519.xlsx"))) {
            List<PackagePart> parts = pkg.getPartsByName(Pattern.compile("/xl/sharedStrings.xml"));
            assertEquals(1, parts.size());

            ReadOnlySharedStringsTable rtbl = new ReadOnlySharedStringsTable(parts.get(0), true);
            assertEquals(49, rtbl.getUniqueCount());

            assertEquals("\u30B3\u30E1\u30F3\u30C8", rtbl.getItemAt(0).getString());
            assertEquals("\u65E5\u672C\u30AA\u30E9\u30AF\u30EB \u30CB\u30DB\u30F3", rtbl.getItemAt(3).getString());

            //now do not include phonetic runs
            rtbl = new ReadOnlySharedStringsTable(parts.get(0), false);
            assertEquals(49, rtbl.getUniqueCount());

            assertEquals("\u30B3\u30E1\u30F3\u30C8", rtbl.getItemAt(0).getString());
            assertEquals("\u65E5\u672C\u30AA\u30E9\u30AF\u30EB", rtbl.getItemAt(3).getString());
        }
    }

    @Test
    void testEmptySSTOnPackageObtainedViaWorkbook() throws Exception {
        XSSFWorkbook wb = new XSSFWorkbook(_ssTests.openResourceAsStream("noSharedStringTable.xlsx"));
        OPCPackage pkg = wb.getPackage();
        assertEmptySST(pkg);
        wb.close();
    }

    @Test
    void testEmptySSTOnPackageDirect() throws Exception {
        try (OPCPackage pkg = OPCPackage.open(_ssTests.openResourceAsStream("noSharedStringTable.xlsx"))) {
            assertEmptySST(pkg);
        }
    }

    @Test
    void testNullPointerException() throws Exception {
        try (OPCPackage pkg = OPCPackage.open(_ssTests.openResourceAsStream("clusterfuzz-testcase-minimized-XLSX2CSVFuzzer-5025401116950528.xlsx"))) {
            assertEmptySST(pkg);
        }

        try (OPCPackage pkg = OPCPackage.open(_ssTests.openResourceAsStream("clusterfuzz-testcase-minimized-XLSX2CSVFuzzer-5025401116950528.xlsx"))) {
            List<PackagePart> parts = pkg.getPartsByName(Pattern.compile("/xl/sharedStrings.xml"));
            assertEquals(1, parts.size());

            //noinspection resource
            assertThrows(IOException.class,
                    () -> new SharedStringsTable(parts.get(0)));
        }
    }

    private void assertEmptySST(OPCPackage pkg) throws IOException, SAXException {
        ReadOnlySharedStringsTable sst = new ReadOnlySharedStringsTable(pkg);
        assertEquals(0, sst.getCount());
        assertEquals(0, sst.getUniqueCount());
    }

    private static final String MINIMAL_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
            "<sst xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" count=\"55\" uniqueCount=\"49\">" +
            "<si>" +
            "<t>bla</t>" +
            "<phoneticPr fontId=\"1\"/>" +
            "</si>" +
            "</sst>";

    @Test
    void testMinimalTable() throws IOException, SAXException {
        ReadOnlySharedStringsTable tbl = new ReadOnlySharedStringsTable(
                new ByteArrayInputStream(MINIMAL_XML.getBytes(StandardCharsets.UTF_8)));
        assertNotNull(tbl);
        assertEquals(49, tbl.getUniqueCount());
        assertEquals(55, tbl.getCount());
        assertTrue(tbl.includePhoneticRuns);
        assertEquals("bla", tbl.getItemAt(0).getString());
        assertThrows(IllegalStateException.class,
                () -> tbl.getItemAt(1).getString());
    }

    @Test
    void testHugeUniqueCount() throws IOException, SAXException {
        ReadOnlySharedStringsTable tbl = new ReadOnlySharedStringsTable(
                new ByteArrayInputStream(MINIMAL_XML.replace("49", "99999999999999999").
                        getBytes(StandardCharsets.UTF_8)));
        assertNotNull(tbl);
        assertEquals(1569325055, tbl.getUniqueCount());
        assertEquals(55, tbl.getCount());
        assertTrue(tbl.includePhoneticRuns);
        assertEquals("bla", tbl.getItemAt(0).getString());
        assertThrows(IllegalStateException.class,
                () -> tbl.getItemAt(1).getString());
    }
}
