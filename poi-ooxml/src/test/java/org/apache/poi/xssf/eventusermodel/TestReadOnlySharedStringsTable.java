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

import junit.framework.TestCase;
import org.apache.poi.POIDataSamples;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTRst;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Tests for {@link org.apache.poi.xssf.eventusermodel.XSSFReader}
 */
public final class TestReadOnlySharedStringsTable extends TestCase {
    private static POIDataSamples _ssTests = POIDataSamples.getSpreadSheetInstance();

    public void testParse() throws Exception {
		OPCPackage pkg = OPCPackage.open(_ssTests.openResourceAsStream("SampleSS.xlsx"));
        List<PackagePart> parts = pkg.getPartsByName(Pattern.compile("/xl/sharedStrings.xml"));
        assertEquals(1, parts.size());

        SharedStringsTable stbl = new SharedStringsTable(parts.get(0), null);
        ReadOnlySharedStringsTable rtbl = new ReadOnlySharedStringsTable(parts.get(0), null);

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
}
