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

package org.apache.poi.ss.formula.ptg;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.IOException;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.record.TestcaseRecordInputStream;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.util.LittleEndianInput;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link RefPtg}.
 */
final class TestReferencePtg {
    /**
     * Tests reading a file containing this ptg.
     */
    @Test
    void testReading() {
        HSSFWorkbook workbook = HSSFTestDataSamples.openSampleWorkbook("ReferencePtg.xls");
        HSSFSheet sheet = workbook.getSheetAt(0);

        // First row
        assertEquals(55.0, sheet.getRow(0).getCell(0).getNumericCellValue(), 0.0,
                     "Wrong numeric value for original number");
        assertEquals(55.0, sheet.getRow(0).getCell(1).getNumericCellValue(), 0.0,
                     "Wrong numeric value for referemce");
        assertEquals("A1", sheet.getRow(0).getCell(1).getCellFormula(), "Wrong formula string for reference");

        // Now moving over the 2**15 boundary
        // (Remember that excel row (n) is poi row (n-1)
        assertEquals(32767.0, sheet.getRow(32766).getCell(0).getNumericCellValue(), 0.0, "Wrong numeric value for original number");
        assertEquals(32767.0, sheet.getRow(32766).getCell(1).getNumericCellValue(), 0.0, "Wrong numeric value for referemce");
        assertEquals("A32767", sheet.getRow(32766).getCell(1).getCellFormula(), "Wrong formula string for reference");

        assertEquals(32768.0, sheet.getRow(32767).getCell(0).getNumericCellValue(), 0.0, "Wrong numeric value for original number");
        assertEquals(32768.0, sheet.getRow(32767).getCell(1).getNumericCellValue(), 0.0, "Wrong numeric value for referemce");
        assertEquals("A32768", sheet.getRow(32767).getCell(1).getCellFormula(), "Wrong formula string for reference");

        assertEquals(32769.0, sheet.getRow(32768).getCell(0).getNumericCellValue(), 0.0, "Wrong numeric value for original number");
        assertEquals(32769.0, sheet.getRow(32768).getCell(1).getNumericCellValue(), 0.0, "Wrong numeric value for referemce");
        assertEquals("A32769", sheet.getRow(32768).getCell(1).getCellFormula(), "Wrong formula string for reference");

        assertEquals(32770.0, sheet.getRow(32769).getCell(0).getNumericCellValue(), 0.0, "Wrong numeric value for original number");
        assertEquals(32770.0, sheet.getRow(32769).getCell(1).getNumericCellValue(), 0.0, "Wrong numeric value for referemce");
        assertEquals("A32770", sheet.getRow(32769).getCell(1).getCellFormula(), "Wrong formula string for reference");
    }

    @Test
    void testBug44921() throws IOException {
        try (HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("ex44921-21902.xls")) {
            assertDoesNotThrow(() -> HSSFTestDataSamples.writeOutAndReadBack(wb));
        }
    }

    private static final byte[] tRefN_data = {
    	0x2C, 33, 44, 55, 66,
    };

    @Test
    void testReadWrite_tRefN_bug45091() {
    	LittleEndianInput in = TestcaseRecordInputStream.createLittleEndian(tRefN_data);
        Ptg[] ptgs = Ptg.readTokens(tRefN_data.length, in);
        byte[] outData = new byte[5];
        Ptg.serializePtgs(ptgs, outData, 0);
        assertNotEquals(0x24, outData[0], "Identified bug 45091");
        assertArrayEquals(tRefN_data, outData);
    }

    /**
     * test that RefPtgBase can handle references with column index greater than 255,
     * see Bugzilla 50096
     */
    @Test
    void testColumnGreater255() {
        RefPtgBase ptg;
        ptg = new RefPtg("IW1");
        assertEquals(256, ptg.getColumn());
        assertEquals("IW1", ptg.formatReferenceAsString());

        ptg = new RefPtg("JA1");
        assertEquals(260, ptg.getColumn());
        assertEquals("JA1", ptg.formatReferenceAsString());
    }
}

