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

package org.apache.poi.ss.usermodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.util.LocaleUtil;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Tests for the Fraction Formatting part of DataFormatter.
 * Largely taken from bug #54686
 */
final class TestFractionFormat {
    @Test
    void testSingle() {
        FractionFormat f = new FractionFormat("", "##");
        double val = 321.321;
        String ret = f.format(val);
        assertEquals("26027/81", ret);
    }

    @Test
    void testInvalid() {
        assertThrows(IllegalStateException.class, () -> new FractionFormat("", "9999999999999999999999999999"));
    }

    @Disabled("Runs for some longer time")
    @Test
    void microBenchmark() {
        FractionFormat f = new FractionFormat("", "##");
        double val = 321.321;
        for(int i = 0;i < 1000000;i++) {
            String ret = f.format(val);
            assertEquals("26027/81", ret);
        }
    }

    @Test
    void testWithBigWholePart() {
        FractionFormat f = new FractionFormat("#", "???/???");

        assertEquals("10100136259702", f.format(10100136259702d));
        assertEquals("-10100136259702", f.format(-10100136259702d));

        // Excel displays fraction: 51/512
        assertEquals("10100136259702 10/100", f.format(10100136259702.1d));
        assertEquals("-10100136259702 10/100", f.format(-10100136259702.1d));

        // Excel displays fraction: 461/512
        assertEquals("10100136259702 90/100", f.format(10100136259702.9d));
        assertEquals("-10100136259702 90/100", f.format(-10100136259702.9d));
    }

    @Test
    void testTruthFile() throws Exception {
        File truthFile = HSSFTestDataSamples.getSampleFile("54686_fraction_formats.txt");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(truthFile), LocaleUtil.CHARSET_1252))) {
            Workbook wb = HSSFTestDataSamples.openSampleWorkbook("54686_fraction_formats.xls");
            Sheet sheet = wb.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();
            FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();

            // Skip over the header row
            String truthLine = reader.readLine();
            String[] headers = truthLine.split("\t");
            truthLine = reader.readLine();

            for (int i = 1; i < sheet.getLastRowNum() && truthLine != null; i++) {
                Row r = sheet.getRow(i);
                String[] truths = truthLine.split("\t");
                // Intentionally ignore the last column (tika-1132), for now
                for (short j = 3; j < 12; j++) {
                    Cell cell = r.getCell(j, MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    String formatted = clean(formatter.formatCellValue(cell, evaluator));
                    if (truths.length <= j) {
                        continue;
                    }

                    String truth = clean(truths[j]);
                    String testKey = truths[0] + ":" + truths[1] + ":" + headers[j];
                    assertEquals(truth, formatted, testKey);
                }
                truthLine = reader.readLine();
            }
            wb.close();
        }
    }

    private String clean(String s){
        s = s.trim().replaceAll(" +",  " ").replaceAll("- +", "-");
        return s;
    }
}
