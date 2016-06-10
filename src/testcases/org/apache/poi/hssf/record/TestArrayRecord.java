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

package org.apache.poi.hssf.record;

import junit.framework.TestCase;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellRangeAddress8Bit;
import org.apache.poi.ss.formula.Formula;
import org.apache.poi.ss.formula.FormulaParser;
import org.apache.poi.ss.formula.FormulaRenderer;
import org.apache.poi.ss.formula.FormulaType;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.util.HexDump;
import org.apache.poi.util.HexRead;

public final class TestArrayRecord extends TestCase {

    public void testRead() {
        String hex =
                "21 02 25 00 01 00 01 00 01 01 00 00 00 00 00 00 " +
                "17 00 65 00 00 01 00 02 C0 02 C0 65 00 00 01 00 " +
                "03 C0 03 C0 04 62 01 07 00";
        byte[] data = HexRead.readFromString(hex);
        RecordInputStream in = TestcaseRecordInputStream.create(data);
        ArrayRecord r1 = new ArrayRecord(in);
        CellRangeAddress8Bit range = r1.getRange();
        assertEquals(1, range.getFirstColumn());
        assertEquals(1, range.getLastColumn());
        assertEquals(1, range.getFirstRow());
        assertEquals(1, range.getLastRow());

        Ptg[] ptg = r1.getFormulaTokens();
        assertEquals("MAX(C1:C2-D1:D2)", FormulaRenderer.toFormulaString(null, ptg));

        //construct a new ArrayRecord with the same contents as r1
        Ptg[] fmlaPtg = FormulaParser.parse("MAX(C1:C2-D1:D2)", null, FormulaType.ARRAY, 0);
        ArrayRecord r2 = new ArrayRecord(Formula.create(fmlaPtg), new CellRangeAddress8Bit(1, 1, 1, 1));
        byte[] ser = r2.serialize();
        //serialize and check that the data is the same as in r1
        assertEquals(HexDump.toHex(data), HexDump.toHex(ser));
    }

    public void testBug57231() {
        HSSFWorkbook wb = HSSFTestDataSamples
                .openSampleWorkbook("57231_MixedGasReport.xls");
        HSSFSheet sheet = wb.getSheet("master");

        HSSFSheet newSheet = wb.cloneSheet(wb.getSheetIndex(sheet));
        int idx = wb.getSheetIndex(newSheet);
        wb.setSheetName(idx, "newName");

        // Write the output to a file
        HSSFWorkbook wbBack = HSSFTestDataSamples.writeOutAndReadBack(wb);
        assertNotNull(wbBack);

        assertNotNull(wbBack.getSheet("master"));
        assertNotNull(wbBack.getSheet("newName"));
    }
}
