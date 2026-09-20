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

package org.apache.poi.ss.formula;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFEvaluationWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaError;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * An operator with an area operand is evaluated element-wise only when its result ends up in a
 * function that works in array mode (SUMPRODUCT, INDEX, XLOOKUP ...); otherwise the area is
 * reduced to the value on the formula's own row. {@link WorkbookEvaluator#findArrayModeOperands}
 * decides this for every token of a formula in one pass over the tokens.
 * <p>
 * Sheet: A1:A3 = 1, 2, 3; B1:B3 = 10, 20, 30; C1:C3 = 100, 200, 300. Formulas are put into row 2.
 */
class TestArrayModeOperands {

    private HSSFWorkbook wb;
    private Sheet sheet;
    private Cell target;
    private FormulaEvaluator fe;

    @BeforeEach
    void setUp() {
        wb = new HSSFWorkbook();
        sheet = wb.createSheet("S");
        for (int r = 0; r < 3; r++) {
            Row row = sheet.createRow(r);
            row.createCell(0).setCellValue(r + 1);
            row.createCell(1).setCellValue((r + 1) * 10);
            row.createCell(2).setCellValue((r + 1) * 100);
        }
        target = sheet.getRow(1).createCell(4); // E2
        fe = wb.getCreationHelper().createFormulaEvaluator();
    }

    @AfterEach
    void tearDown() throws IOException {
        wb.close();
    }

    private CellValue eval(String formula) {
        target.setCellFormula(formula);
        fe.clearAllCachedResultValues();
        return fe.evaluate(target);
    }

    private void assertNumber(double expected, String formula) {
        CellValue cv = eval(formula);
        assertEquals(CellType.NUMERIC, cv.getCellType(), formula + " -> " + cv);
        assertEquals(expected, cv.getNumberValue(), 1e-9, formula);
    }

    private void assertError(FormulaError expected, String formula) {
        CellValue cv = eval(formula);
        assertEquals(CellType.ERROR, cv.getCellType(), formula + " -> " + cv);
        assertEquals(expected, FormulaError.forInt(cv.getErrorValue()), formula);
    }

    @Test
    void operatorFeedingAnArrayModeFunctionIsElementWise() {
        assertNumber(1 * 10 + 2 * 20 + 3 * 30, "SUMPRODUCT(A1:A3*B1:B3)");
        assertNumber(20 + 30, "SUMPRODUCT((A1:A3>1)*B1:B3)");
        assertNumber(30, "INDEX(A1:A3*10,3)");
        // ... also through an enclosing operator or a non-array function on the way
        assertNumber(2 * (1 + 2 + 3), "SUMPRODUCT(SUM(A1:A3*2),1)");
        assertNumber(1 * 100 + 2 * 200 + 3 * 300, "SUMPRODUCT((A1:A3*B1:B3)/B1:B3*C1:C3)");
    }

    @Test
    void ifFeedingAnArrayModeFunctionIsEvaluatedElementWise() {
        // IF is normally short-circuited on the single value of its condition; inside an
        // ArrayMode function it works element-wise, as it does in an array formula and in Excel
        assertNumber(20 + 30, "SUMPRODUCT(IF(A1:A3>1,B1:B3,0))");
        assertNumber(20 + 30, "SUMPRODUCT(IF(A1:A3>1,B1:B3))");
        assertNumber(20 + 30, "SUMPRODUCT(IF(A1:A3>1,1,0)*B1:B3)");
        assertNumber(100, "INDEX(IF(A1:A3>1,B1:B3,C1:C3),1)");
        assertNumber(200 + 300, "SUMPRODUCT(IF(A1:A3>1,IF(B1:B3>10,C1:C3,0),0))");
        // a scalar condition picks the whole branch
        assertNumber(10 + 20 + 30, "SUMPRODUCT(IF(A2>1,B1:B3,0))");
        assertNumber(0, "SUMPRODUCT(IF(A2>5,B1:B3,0))");

        // outside an ArrayMode function IF keeps its shortcut: the condition is the formula row's
        assertNumber(20, "IF(A1:A3>1,B2,0)");
        assertNumber(2 * 20, "IF(A1:A3>1,B1:B3,0)*2");
        // CHOOSE's jumps are not touched, whether next to or inside an array-mode IF
        assertNumber(7 * 2, "SUMPRODUCT(CHOOSE(2,5,7)*IF(A1:A3>1,1,0))");
        assertNumber(7 * 2, "SUMPRODUCT(IF(A1:A3>1,CHOOSE(2,5,7),0))");
    }

    @Test
    void operatorOutsideAnArrayModeFunctionUsesTheFormulaRow() {
        // the formula is on row 2, so A1:A3 reduces to A2
        assertNumber(2 * 2, "A1:A3*2");
        assertNumber(2 * 2, "SUM(A1:A3*2)");
        // an array function elsewhere in the formula makes no difference to an operator it does
        // not consume
        assertNumber(2 * 2 + (10 * 100 + 20 * 200 + 30 * 300), "A1:A3*2+SUMPRODUCT(B1:B3,C1:C3)");
        assertNumber((1 + 2 + 3) * 2 + 2 * 3, "SUMPRODUCT(A1:A3*2)+A1:A3*3");
        assertNumber(2 * 20, "SUMPRODUCT(1,1)*0+A1:A3*B1:B3");
    }

    @Test
    void operatorOutsideAnArrayModeFunctionOnARowOutsideTheAreaIsAnError() {
        target = sheet.createRow(5).createCell(4); // E6
        assertError(FormulaError.VALUE, "A1:A3*2");
        assertNumber(1 * 10 + 2 * 20 + 3 * 30, "SUMPRODUCT(A1:A3*B1:B3)");
    }

    @Test
    void futureFunctionsCalledByNameAreRecognised() {
        // XLOOKUP is a "future function": an external FuncVarPtg whose first operand is the name
        assertNumber(200, "XLOOKUP(1,(A1:A3=2)*(B1:B3=20),C1:C3)");
        assertNumber(2, "XLOOKUP(TRUE,(A1:A3>1)*(B1:B3>10)=1,A1:A3)");
    }

    @Test
    void flagsAreComputedPerToken() {
        HSSFEvaluationWorkbook ewb = HSSFEvaluationWorkbook.create(wb);
        WorkbookEvaluator we = new WorkbookEvaluator(ewb, null, null);

        // SUMPRODUCT(A1:A3*2)+A1:A3*3 : A1:A3 2 * SUMPRODUCT A1:A3 3 * +
        Ptg[] ptgs = FormulaParser.parse("SUMPRODUCT(A1:A3*2)+A1:A3*3", ewb, FormulaType.CELL, 0, 1);
        assertEquals(8, ptgs.length);
        assertArrayEquals(new boolean[]{true, true, true, false, false, false, false, false},
                we.findArrayModeOperands(ptgs));

        // SUMPRODUCT(SUM(A1:A3*2),1) : A1:A3 2 * SUM 1 SUMPRODUCT
        ptgs = FormulaParser.parse("SUMPRODUCT(SUM(A1:A3*2),1)", ewb, FormulaType.CELL, 0, 1);
        assertEquals(6, ptgs.length);
        assertArrayEquals(new boolean[]{true, true, true, true, true, false}, we.findArrayModeOperands(ptgs));

        // a formula without any function: nothing is in array mode
        ptgs = FormulaParser.parse("A1:A3*2+1", ewb, FormulaType.CELL, 0, 1);
        assertArrayEquals(new boolean[ptgs.length], we.findArrayModeOperands(ptgs));
    }
}
