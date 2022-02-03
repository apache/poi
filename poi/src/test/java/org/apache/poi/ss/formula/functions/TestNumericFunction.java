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
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.jupiter.api.Test;

import static org.apache.poi.ss.util.Utils.assertDouble;
import static org.apache.poi.ss.util.Utils.assertString;

final class TestNumericFunction {

    @Test
    void testINT() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFCell cell = wb.createSheet().createRow(0).createCell(0);
        HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
        assertDouble(fe, cell, "INT(880000000.0001)", 880000000.0, 0);
        //the following INT(-880000000.0001) resulting in -880000001.0 has been observed in excel
        //see also https://support.microsoft.com/en-us/office/int-function-a6c4af9e-356d-4369-ab6a-cb1fd9d343ef
        assertDouble(fe, cell, "INT(-880000000.0001)", -880000001.0, 0);
        assertDouble(fe, cell, "880000000*0.00849", 7471200.0, 0);
        assertDouble(fe, cell, "880000000*0.00849/3", 2490400.0, 0);
        assertDouble(fe, cell, "INT(880000000*0.00849/3)", 2490400.0, 0);
    }

    @Test
    void testSIGN() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFCell cell = wb.createSheet().createRow(0).createCell(0);
        HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
        //https://support.microsoft.com/en-us/office/sign-function-109c932d-fcdc-4023-91f1-2dd0e916a1d8
        assertDouble(fe, cell, "SIGN(10)", 1.0, 0);
        assertDouble(fe, cell, "SIGN(4-4)", 0.0, 0);
        assertDouble(fe, cell, "SIGN(-0.00001)", -1.0, 0);
    }

    @Test
    void testDOLLAR() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFCell cell = wb.createSheet().createRow(0).createCell(0);
        HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
        //https://support.microsoft.com/en-us/office/dollar-function-a6cd05d9-9740-4ad3-a469-8109d18ff611
        assertString(fe, cell, "DOLLAR(1234.567,2)", "$1,234.57");
        assertString(fe, cell, "DOLLAR(-1234.567,0)", "($1,235)");
        //TODO need to fix code to handle next case
        //assertString(fe, cell, "DOLLAR(-1234.567,-2)", "($1,200)");
        assertString(fe, cell, "DOLLAR(-0.123,4)", "($0.1230)");
        assertString(fe, cell, "DOLLAR(99.888)", "$99.89");
        assertString(fe, cell, "DOLLAR(123456789.567,2)", "$123,456,789.57");
    }
}
