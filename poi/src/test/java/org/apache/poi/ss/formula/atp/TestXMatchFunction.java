
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
package org.apache.poi.ss.formula.atp;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.apache.poi.ss.util.Utils.addRow;
import static org.apache.poi.ss.util.Utils.assertDouble;

/**
 * Testcase for function XMATCH()
 */
public class TestXMatchFunction {

    //https://support.microsoft.com/en-us/office/xmatch-function-d966da31-7a6b-4a13-a1c6-5a33ed6a0312
    @Test
    void testMicrosoftExample0() throws IOException {
        try (HSSFWorkbook wb = initWorkbook("Grape")) {
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = wb.getSheetAt(0).getRow(2).createCell(5);
            assertDouble(fe, cell, "XMATCH(E3,C3:C7)", 2);
        }
    }

    @Test
    void testMicrosoftExample1() throws IOException {
        try (HSSFWorkbook wb = initWorkbook("Gra?")) {
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = wb.getSheetAt(0).getRow(2).createCell(5);
            //TODO investigate issue
            //assertDouble(fe, cell, "XMATCH(E3,C3:C7,1)", 2);
        }
    }

    private HSSFWorkbook initWorkbook(String lookup) {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet();
        addRow(sheet, 0);
        addRow(sheet, 1, null, null, "Product", null, "Product", "Position");
        addRow(sheet, 2, null, null, "Apple", null, lookup);
        addRow(sheet, 3, null, null, "Grape");
        addRow(sheet, 4, null, null, "Pear");
        addRow(sheet, 5, null, null, "Banana");
        addRow(sheet, 6, null, null, "Cherry");
        return wb;
    }

}
