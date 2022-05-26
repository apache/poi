
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
 * Testcase for function DSTDEV() and DSTDEVP()
 */
public class TestDStdev {

    //https://support.microsoft.com/en-us/office/dstdev-function-026b8c73-616d-4b5e-b072-241871c4ab96
    @Test
    void testMicrosoftExample1() throws IOException {
        try (HSSFWorkbook wb = initWorkbook1()) {
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = wb.getSheetAt(0).getRow(0).createCell(12);
            assertDouble(fe, cell, "DSTDEV(A5:E11, \"Yield\", A1:A3)", 2.96647939483827, 0.0000000001);
        }
    }

    //https://support.microsoft.com/en-us/office/dstdevp-function-04b78995-da03-4813-bbd9-d74fd0f5d94b
    @Test
    void testDSTDEVPMicrosoftExample1() throws IOException {
        try (HSSFWorkbook wb = initWorkbook1()) {
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = wb.getSheetAt(0).getRow(0).createCell(12);
            assertDouble(fe, cell, "DSTDEVP(A5:E11, \"Yield\", A1:A3)", 2.65329983228432, 0.0000000001);
        }
    }

    private HSSFWorkbook initWorkbook1() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet();
        addRow(sheet, 0, "Tree", "Height", "Age", "Yield", "Profit", "Height");
        addRow(sheet, 1, "=Apple", ">10", null, null, null, "<16");
        addRow(sheet, 2, "=Pear");
        addRow(sheet, 3);
        addRow(sheet, 4, "Tree", "Height", "Age", "Yield", "Profit");
        addRow(sheet, 5, "Apple", 18, 20, 14, 105);
        addRow(sheet, 6, "Pear", 12, 12, 10, 96);
        addRow(sheet, 7, "Cherry", 13, 14, 9, 105);
        addRow(sheet, 8, "Apple", 14, 15, 10, 75);
        addRow(sheet, 9, "Pear", 9, 8, 8, 77);
        addRow(sheet, 10, "Apple", 8, 9, 6, 45);
        return wb;
    }
}
