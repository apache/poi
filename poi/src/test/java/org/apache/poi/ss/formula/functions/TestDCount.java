
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
 * Testcase for function DCOUNT()
 */
public class TestDCount {

    //https://support.microsoft.com/en-us/office/dcount-function-c1fc7b93-fb0d-4d8d-97db-8d5f076eaeb1
    @Test
    void testMicrosoftExample1() throws IOException {
        try (HSSFWorkbook wb = initWorkbook1()) {
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = wb.getSheetAt(0).getRow(0).createCell(100);
            assertDouble(fe, cell, "DCOUNT(A5:E11,,A1:A2)", 3);
            assertDouble(fe, cell, "DCOUNT(A5:E11, \"Age\", A1:A2)", 2);
            assertDouble(fe, cell, "DCOUNT(A5:E11, \"Age\", A1:F2)", 1);
            assertDouble(fe, cell, "DCOUNT(A5:E11, 3, A1:F2)", 1);
            assertDouble(fe, cell, "DCOUNT(A5:E11, 2, A1:F3)", 4);
            assertDouble(fe, cell, "DCOUNT(A5:E11, 3, A1:F3)", 3);
            assertDouble(fe, cell, "DCOUNT(A5:E11, 5, A1:F3)", 3);
            assertDouble(fe, cell, "DCOUNT(A5:E11, 5, A13:A14)", 2);
            assertDouble(fe, cell, "DCOUNT(A5:E11, 5, B13:B14)", 3);
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
        addRow(sheet, 8, "Apple", 14, null, 10, 75);
        addRow(sheet, 9, "Pear", 9, 8, 8, "$77");
        addRow(sheet, 10, "Apple", 12, 11, 6, 45);
        addRow(sheet, 11);
        addRow(sheet, 12, "Tree", "Height");
        addRow(sheet, 13, "<>Apple", "<>12");
        return wb;
    }
}
