
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
import org.apache.poi.ss.usermodel.FormulaError;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.apache.poi.ss.util.Utils.*;

/**
 * Testcase for function XMATCH()
 */
public class TestXMatchFunction {

    //https://support.microsoft.com/en-us/office/xmatch-function-d966da31-7a6b-4a13-a1c6-5a33ed6a0312
    @Test
    void testMicrosoftExample0() throws IOException {
        try (HSSFWorkbook wb = initNumWorkbook("Grape")) {
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = wb.getSheetAt(0).getRow(2).createCell(5);
            assertDouble(fe, cell, "XMATCH(E3,C3:C7)", 2);
            assertError(fe, cell, "XMATCH(\"Gra\",C3:C7)", FormulaError.NA);
        }
    }

    @Test
    void testMicrosoftExample1() throws IOException {
        try (HSSFWorkbook wb = initNumWorkbook("Gra?")) {
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = wb.getSheetAt(0).getRow(2).createCell(5);
            assertDouble(fe, cell, "XMATCH(E3,C3:C7,1)", 2);
            assertDouble(fe, cell, "XMATCH(E3,C3:C7,-1)", 5);
            assertDouble(fe, cell, "XMATCH(\"Gra\",C3:C7,1)", 2);
            assertDouble(fe, cell, "XMATCH(\"Graz\",C3:C7,1)", 3);
            assertDouble(fe, cell, "XMATCH(\"Graz\",C3:C7,-1)", 2);
        }
    }

    @Test
    void testMicrosoftExample2() throws IOException {
        //the result in this example is correct but the description seems wrong from my testing
        //the result is based on the position and not a count
        try (HSSFWorkbook wb = initWorkbook2()) {
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = wb.getSheetAt(0).getRow(3).createCell(5);
            assertDouble(fe, cell, "XMATCH(F2,C3:C9,1)", 4);
            assertDouble(fe, cell, "XMATCH(F2,C3:C9,-1)", 5);
            assertError(fe, cell, "XMATCH(F2,C3:C9,2)", FormulaError.NA);
            assertDouble(fe, cell, "XMATCH(35000,C3:C9,1)", 2);
            assertDouble(fe, cell, "XMATCH(36000,C3:C9,1)", 1);
        }
    }

    private HSSFWorkbook initNumWorkbook(String lookup) {
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

    private HSSFWorkbook initWorkbook2() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet();
        addRow(sheet, 0);
        addRow(sheet, 1, null, "Sales Rep", "Total Sales", null, "Bonus", 15000);
        addRow(sheet, 2, null, "Michael Neipper", 42000);
        addRow(sheet, 3, null, "Jan Kotas", 35000);
        addRow(sheet, 4, null, "Nancy Freehafer", 25000);
        addRow(sheet, 5, null, "Andrew Cencini", 15901);
        addRow(sheet, 6, null, "Anne Hellung-Larsen", 13801);
        addRow(sheet, 7, null, "Nancy Freehafer", 12181);
        addRow(sheet, 8, null, "Mariya Sergienko", 9201);
        return wb;
    }

}
