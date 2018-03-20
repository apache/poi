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

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import junit.framework.TestCase;
import org.apache.poi.POIDataSamples;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTRst;
import org.xml.sax.SAXException;

/**
 * Tests for {@link org.apache.poi.xssf.eventusermodel.XSSFReader}
 */
public final class TestReadOnlySharedStringsTable extends TestCase {
    private static POIDataSamples _ssTests = POIDataSamples.getSpreadSheetInstance();

    public void testParse() throws Exception {
		OPCPackage pkg = OPCPackage.open(_ssTests.openResourceAsStream("SampleSS.xlsx"));
        List<PackagePart> parts = pkg.getPartsByName(Pattern.compile("/xl/sharedStrings.xml"));
        assertEquals(1, parts.size());

        SharedStringsTable stbl = new SharedStringsTable(parts.get(0));
        ReadOnlySharedStringsTable rtbl = new ReadOnlySharedStringsTable(parts.get(0));

        assertEquals(stbl.getCount(), rtbl.getCount());
        assertEquals(stbl.getUniqueCount(), rtbl.getUniqueCount());

        assertEquals(stbl.getItems().size(), stbl.getUniqueCount());
        assertEquals(rtbl.getItems().size(), rtbl.getUniqueCount());
        for(int i=0; i < stbl.getUniqueCount(); i++){
            CTRst i1 = stbl.getEntryAt(i);
            String i2 = rtbl.getEntryAt(i);
            assertEquals(i1.getT(), i2);
        }

	}

	//51519
	public void testPhoneticRuns() throws Exception {
        OPCPackage pkg = OPCPackage.open(_ssTests.openResourceAsStream("51519.xlsx"));
        List<PackagePart> parts = pkg.getPartsByName(Pattern.compile("/xl/sharedStrings.xml"));
        assertEquals(1, parts.size());

        ReadOnlySharedStringsTable rtbl = new ReadOnlySharedStringsTable(parts.get(0), true);
        List<String> strings = rtbl.getItems();
        assertEquals(49, strings.size());

        assertEquals("\u30B3\u30E1\u30F3\u30C8", rtbl.getEntryAt(0));
        assertEquals("\u65E5\u672C\u30AA\u30E9\u30AF\u30EB \u30CB\u30DB\u30F3", rtbl.getEntryAt(3));

        //now do not include phonetic runs
        rtbl = new ReadOnlySharedStringsTable(parts.get(0), false);
        strings = rtbl.getItems();
        assertEquals(49, strings.size());

        assertEquals("\u30B3\u30E1\u30F3\u30C8", rtbl.getEntryAt(0));
        assertEquals("\u65E5\u672C\u30AA\u30E9\u30AF\u30EB", rtbl.getEntryAt(3));

    }

    public void testEmptySSTOnPackageObtainedViaWorkbook() throws Exception {
        XSSFWorkbook wb = new XSSFWorkbook(_ssTests.openResourceAsStream("noSharedStringTable.xlsx"));
        OPCPackage pkg = wb.getPackage();
        assertEmptySST(pkg);
        wb.close();
    }
    
    public void testEmptySSTOnPackageDirect() throws Exception {
        OPCPackage pkg = OPCPackage.open(_ssTests.openResourceAsStream("noSharedStringTable.xlsx"));
        assertEmptySST(pkg);
    }

    private void assertEmptySST(OPCPackage pkg) throws IOException, SAXException {
        ReadOnlySharedStringsTable sst = new ReadOnlySharedStringsTable(pkg);
        assertEquals(0, sst.getCount());
        assertEquals(0, sst.getUniqueCount());
        assertNull(sst.getItems()); // same state it's left in if fed a package which has no SST part.
    }
    
}
