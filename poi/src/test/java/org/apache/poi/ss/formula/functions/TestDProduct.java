
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
package org.apache.poi.ss.formula.functions;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.apache.poi.ss.util.Utils.addRow;
import static org.apache.poi.ss.util.Utils.assertDouble;

/**
 * Testcase for function DPRODUCT()
 */
public class TestDProduct {

    //https://support.microsoft.com/en-us/office/dproduct-function-4f96b13e-d49c-47a7-b769-22f6d017cb31
    @Test
    void testMicrosoftExample1() throws IOException {
        try (HSSFWorkbook wb = initWorkbook1(false)) {
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = wb.getSheetAt(0).getRow(0).createCell(12);
            assertDouble(fe, cell, "DPRODUCT(A5:E11, \"Yield\", A1:F3)", 800, 0.0000000001);
            assertDouble(fe, cell, "DPRODUCT(A5:E11, \"Yield\", A13:A14)", 720, 0.0000000001);
            assertDouble(fe, cell, "DPRODUCT(A5:E11, \"Yield\", B13:C14)", 7560, 0.0000000001);
        }
    }

    @Test
    void testNoMatch() throws IOException {
        try (HSSFWorkbook wb = initWorkbook1(true)) {
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = wb.getSheetAt(0).getRow(0).createCell(12);
            assertDouble(fe, cell, "DPRODUCT(A5:E11, \"Yield\", A1:A2)", 0);
            assertDouble(fe, cell, "DPRODUCT(A5:E11, \"Yield\", A1:A3)", 604800);
        }
    }

    private HSSFWorkbook initWorkbook1(boolean noMatch) {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet();
        addRow(sheet, 0, "Tree", "Height", "Age", "Yield", "Profit", "Height");
        if (noMatch) {
            addRow(sheet, 1, "=NoMatch");
            addRow(sheet, 2);
        } else {
            addRow(sheet, 1, "=Apple", ">10", null, null, null, "<16");
            addRow(sheet, 2, "=Pear");
        }
        addRow(sheet, 3);
        addRow(sheet, 4, "Tree", "Height", "Age", "Yield", "Profit");
        addRow(sheet, 5, "Apple", 18, 20, 14, 105);
        addRow(sheet, 6, "Pear", 12, 12, 10, 96);
        addRow(sheet, 7, "Cherry", 13, 14, 9, 105);
        addRow(sheet, 8, "Apple", 14, 15, 10, 75);
        addRow(sheet, 9, "Pear", 9, 8, 8, 77);
        addRow(sheet, 10, "Apple", 8, 9, 6, 45);
        addRow(sheet, 11);
        addRow(sheet, 12, "Tree", "Height", "Height");
        addRow(sheet, 13, "<>Apple", "<>12", "<>9");
        return wb;
    }
}
