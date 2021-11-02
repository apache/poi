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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;


/**
 * IfNa unit tests.
 */
class TestIfna {
    
    HSSFWorkbook wb;
    HSSFCell cell;
    HSSFFormulaEvaluator fe;

    @BeforeEach
    void setup() {
        wb = new HSSFWorkbook();
        cell = wb.createSheet().createRow(0).createCell(0);
        fe = new HSSFFormulaEvaluator(wb);
    }

    @Test
    void testNumbericArgsWorkCorrectly() {
        confirmResult(fe, cell, "IFNA(-1,42)", new CellValue(-1.0));
        confirmResult(fe, cell, "IFNA(NA(),42)", new CellValue(42.0));
    }

    @Test
    void testStringArgsWorkCorrectly() {
        confirmResult(fe, cell, "IFNA(\"a1\",\"a2\")", new CellValue("a1"));
        confirmResult(fe, cell, "IFNA(NA(),\"a2\")", new CellValue("a2"));      
    }

    @Test
    void testUsageErrorsThrowErrors() {
        confirmError(fe, cell, "IFNA(1)", ErrorEval.VALUE_INVALID);
        confirmError(fe, cell, "IFNA(1,2,3)", ErrorEval.VALUE_INVALID);
    }

    @Test
    void testErrorInArgSelectsNAResult() {
        confirmError(fe, cell, "IFNA(1/0,42)", ErrorEval.DIV_ZERO);
    }

    @Test
    void testErrorFromNAArgPassesThrough() {
        confirmError(fe, cell, "IFNA(NA(),1/0)", ErrorEval.DIV_ZERO);
    }

    @Test
    void testNaArgNotEvaledIfUnneeded() {
        confirmResult(fe, cell, "IFNA(42,1/0)", new CellValue(42.0));
    }

    private static void confirmResult(HSSFFormulaEvaluator fe, HSSFCell cell, String formulaText,
                                      CellValue expectedResult) {
        fe.setDebugEvaluationOutputForNextEval(true);
        cell.setCellFormula(formulaText);
        fe.notifyUpdateCell(cell);
        CellValue result = fe.evaluate(cell);
        assertEquals(expectedResult.getCellType(), result.getCellType(), "Testing result type for: " + formulaText);
        assertEquals(expectedResult.formatAsString(), result.formatAsString(), "Testing result for: " + formulaText);
    }

    private static void confirmError(HSSFFormulaEvaluator fe, HSSFCell cell, String formulaText,
                                     ErrorEval expectedError) {
        fe.setDebugEvaluationOutputForNextEval(true);
        cell.setCellFormula(formulaText);
        fe.notifyUpdateCell(cell);
        CellValue result = fe.evaluate(cell);
        assertEquals(CellType.ERROR, result.getCellType(), "Testing result type for: " + formulaText);
        assertEquals(expectedError.getErrorString(), result.formatAsString(), "Testing error type for: " + formulaText);
    }
}
