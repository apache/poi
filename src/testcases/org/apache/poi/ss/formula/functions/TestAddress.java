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
import org.apache.poi.ss.usermodel.CellValue;

import junit.framework.TestCase;
import org.apache.poi.ss.util.CellReference;

public final class TestAddress extends TestCase {

    public void testAddress() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFCell cell = wb.createSheet().createRow(0).createCell(0);
        HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);

        String formulaText = "ADDRESS(1,2)";
        confirmResult(fe, cell, formulaText, "$B$1");

        formulaText = "ADDRESS(22,44)";
        confirmResult(fe, cell, formulaText, "$AR$22");

        formulaText = "ADDRESS(1,1)";
        confirmResult(fe, cell, formulaText, "$A$1");

        formulaText = "ADDRESS(1,128)";
        confirmResult(fe, cell, formulaText, "$DX$1");

        formulaText = "ADDRESS(1,512)";
        confirmResult(fe, cell, formulaText, "$SR$1");

        formulaText = "ADDRESS(1,1000)";
        confirmResult(fe, cell, formulaText, "$ALL$1");

        formulaText = "ADDRESS(1,10000)";
        confirmResult(fe, cell, formulaText, "$NTP$1");

        formulaText = "ADDRESS(2,3)";
        confirmResult(fe, cell, formulaText, "$C$2");

        formulaText = "ADDRESS(2,3,2)";
        confirmResult(fe, cell, formulaText, "C$2");

        formulaText = "ADDRESS(2,3,2,,\"EXCEL SHEET\")";
        confirmResult(fe, cell, formulaText, "'EXCEL SHEET'!C$2");

        formulaText = "ADDRESS(2,3,3,TRUE,\"[Book1]Sheet1\")";
        confirmResult(fe, cell, formulaText, "'[Book1]Sheet1'!$C2");
    }

    private static void confirmResult(HSSFFormulaEvaluator fe, HSSFCell cell, String formulaText,
                                      String expectedResult) {
        cell.setCellFormula(formulaText);
        fe.notifyUpdateCell(cell);
        CellValue result = fe.evaluate(cell);
        assertEquals(result.getCellType(), HSSFCell.CELL_TYPE_STRING);
        assertEquals(expectedResult, result.getStringValue());
    }
}
