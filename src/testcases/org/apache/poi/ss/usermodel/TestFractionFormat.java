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

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.util.LocaleUtil;
import org.junit.Test;

/**
 * Tests for the Fraction Formatting part of DataFormatter.
 * Largely taken from bug #54686
 */
public final class TestFractionFormat {
    @Test
    public void testSingle() throws Exception {
        FractionFormat f = new FractionFormat("", "##");
        double val = 321.321;
        String ret = f.format(val);
        assertEquals("26027/81", ret);
    }
     
    @Test
    public void testTruthFile() throws Exception {
        File truthFile = HSSFTestDataSamples.getSampleFile("54686_fraction_formats.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(truthFile), LocaleUtil.CHARSET_1252));
        Workbook wb = HSSFTestDataSamples.openSampleWorkbook("54686_fraction_formats.xls");
        Sheet sheet = wb.getSheetAt(0);
        DataFormatter formatter = new DataFormatter();
        FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();

        // Skip over the header row
        String truthLine = reader.readLine();
        String[] headers = truthLine.split("\t");
        truthLine = reader.readLine();

        for (int i = 1; i < sheet.getLastRowNum() && truthLine != null; i++){
            Row r = sheet.getRow(i);
            String[] truths = truthLine.split("\t");
            // Intentionally ignore the last column (tika-1132), for now
            for (short j = 3; j < 12; j++){
                Cell cell = r.getCell(j, MissingCellPolicy.CREATE_NULL_AS_BLANK);
                String formatted = clean(formatter.formatCellValue(cell, evaluator));
                if (truths.length <= j){
                    continue;
                }

                String truth = clean(truths[j]);
                String testKey = truths[0]+":"+truths[1]+":"+headers[j];
                assertEquals(testKey, truth, formatted);
            }
            truthLine = reader.readLine();
        }
        wb.close();
        reader.close();
    }

    private String clean(String s){
        s = s.trim().replaceAll(" +",  " ").replaceAll("- +", "-");
        return s;
    }
}
