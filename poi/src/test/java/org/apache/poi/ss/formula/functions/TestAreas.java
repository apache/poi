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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.junit.jupiter.api.Test;

final class TestAreas {

    @Test
    void testAreas() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFCell cell = wb.createSheet().createRow(0).createCell(0);
        HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);

        String formulaText = "AREAS(B1)";
        confirmResult(fe, cell, formulaText,1.0);

        formulaText = "AREAS(B2:D4)";
        confirmResult(fe, cell, formulaText,1.0);

        formulaText = "AREAS((B2:D4,E5,F6:I9))";
        confirmResult(fe, cell, formulaText,3.0);

        formulaText = "AREAS((B2:D4,E5,C3,E4))";
        confirmResult(fe, cell, formulaText,4.0);

        formulaText = "AREAS((I9))";
        confirmResult(fe, cell, formulaText,1.0);
    }

    private static void confirmResult(HSSFFormulaEvaluator fe, HSSFCell cell, String formulaText,Double expectedResult) {
        cell.setCellFormula(formulaText);
        fe.notifyUpdateCell(cell);
        CellValue result = fe.evaluate(cell);
        assertEquals(result.getCellType(), CellType.NUMERIC);
        assertEquals(expectedResult, result.getNumberValue(), 0);
    }
}
