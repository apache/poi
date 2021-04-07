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

package org.apache.poi.xssf.binary;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.regex.Pattern;

import org.apache.poi.POIDataSamples;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.junit.jupiter.api.Test;

class TestXSSFBSharedStringsTable {
    private static POIDataSamples _ssTests = POIDataSamples.getSpreadSheetInstance();

    @Test
    void testBasic() throws Exception {
        try (OPCPackage pkg = OPCPackage.open(_ssTests.openResourceAsStream("51519.xlsb"))) {
            List<PackagePart> parts = pkg.getPartsByName(Pattern.compile("/xl/sharedStrings.bin"));
            assertEquals(1, parts.size());

            XSSFBSharedStringsTable rtbl = new XSSFBSharedStringsTable(parts.get(0));

            assertEquals("\u30B3\u30E1\u30F3\u30C8", rtbl.getItemAt(0).getString());
            assertEquals("\u65E5\u672C\u30AA\u30E9\u30AF\u30EB", rtbl.getItemAt(3).getString());
            assertEquals(55, rtbl.getCount());
            assertEquals(49, rtbl.getUniqueCount());

            //TODO: add in tests for phonetic runs
        }
    }
}
