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

import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaError;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Bug 65059: when the criteria argument of SUMIF, COUNTIF, AVERAGEIF, SUMIFS, COUNTIFS,
 * AVERAGEIFS, MAXIFS or MINIFS stands for several criteria, Excel evaluates the function once per
 * criterion and returns an array of the results, which the enclosing function (SUMPRODUCT, SUM,
 * MAX, INDEX ...) then consumes. That is the case for an array constant such as {@code {"a","b"}}
 * always, and for a multi-cell range in array context: inside an array-mode function such as
 * SUMPRODUCT, or in an array formula. In an ordinary cell a range criteria is reduced to the cell
 * on the formula's own row (implicit intersection), as Excel does - the Excel-generated
 * {@code FormulaEvalTestData.xls} relies on that.
 * <p>
 * POI used to sum the per-criterion results of the *IFS functions whatever the context, and to
 * reduce the criteria of the single-condition functions to one value whatever the context.
 * <pre>
 *        A(formulas)  B      C      D      E
 *   1                 1      x      x      1
 *   2                 2      x      x      2
 *   3                 3      y      x      3
 *   4                 4      y      z      4
 * </pre>
 */
class TestConditionalAggregatesWithArrayCriteria {

    private HSSFWorkbook wb;
    private Sheet sheet;
    private FormulaEvaluator fe;

    @BeforeEach
    void setUp() {
        wb = new HSSFWorkbook();
        sheet = wb.createSheet("S");
        String[] c = {"x", "x", "y", "y"};
        String[] d = {"x", "x", "x", "z"};
        for (int r = 0; r < 4; r++) {
            Row row = sheet.createRow(r);
            row.createCell(1).setCellValue(r + 1);
            row.createCell(2).setCellValue(c[r]);
            row.createCell(3).setCellValue(d[r]);
            row.createCell(4).setCellValue(r + 1);
        }
        sheet.createRow(9);
        fe = wb.getCreationHelper().createFormulaEvaluator();
    }

    @AfterEach
    void tearDown() throws IOException {
        wb.close();
    }

    private CellValue evalIn(int rowIndex, String formula) {
        Cell cell = sheet.getRow(rowIndex).createCell(0);
        cell.setCellFormula(formula);
        fe.clearAllCachedResultValues();
        return fe.evaluate(cell);
    }

    private CellValue evalAsArrayFormula(String formula) {
        sheet.setArrayFormula(formula, CellRangeAddress.valueOf("G1"));
        fe.clearAllCachedResultValues();
        CellValue cv = fe.evaluate(sheet.getRow(0).getCell(6));
        sheet.removeArrayFormula(sheet.getRow(0).getCell(6));
        return cv;
    }

    private void assertPlain(int rowIndex, double expected, String formula) {
        CellValue cv = evalIn(rowIndex, formula);
        assertEquals(CellType.NUMERIC, cv.getCellType(), formula + " on row " + (rowIndex + 1) + " -> " + cv);
        assertEquals(expected, cv.getNumberValue(), 1e-9, formula + " on row " + (rowIndex + 1));
    }

    private void assertArrayFormula(double expected, String formula) {
        CellValue cv = evalAsArrayFormula(formula);
        assertEquals(CellType.NUMERIC, cv.getCellType(), "{=" + formula + "} -> " + cv);
        assertEquals(expected, cv.getNumberValue(), 1e-9, "{=" + formula + "}");
    }

    /** the formula gives the same number on row 1, on a row below the data and as an array formula */
    private void assertNumber(double expected, String formula) {
        for (int r : new int[]{0, 9}) {
            CellValue cv = evalIn(r, formula);
            assertEquals(CellType.NUMERIC, cv.getCellType(), formula + " on row " + (r + 1) + " -> " + cv);
            assertEquals(expected, cv.getNumberValue(), 1e-9, formula + " on row " + (r + 1));
        }
        CellValue cv = evalAsArrayFormula(formula);
        assertEquals(CellType.NUMERIC, cv.getCellType(), "{=" + formula + "} -> " + cv);
        assertEquals(expected, cv.getNumberValue(), 1e-9, "{=" + formula + "}");
    }

    @Test
    void bug65059SumproductOfSumifsWithARangeAsCriteria() {
        // one SUMIFS per criterion in D1:D4: x -> 1+2, x -> 3, x -> 3, z -> 0
        assertNumber(3 + 3 + 3 + 0, "SUMPRODUCT(SUMIFS(B1:B4,C1:C4,D1:D4))");
        // x -> 3, x -> 3, y -> 3
        assertNumber(3 + 3 + 3, "SUMPRODUCT(SUMIFS(B1:B3,C1:C3,C1:C3))");
    }

    @Test
    void bug65059ReportersCase() {
        // B = 1, 2, 3; C = D = 1, 1, 1: each of the three criteria matches every row
        Sheet s = wb.createSheet("Report");
        for (int r = 0; r < 3; r++) {
            Row row = s.createRow(r);
            row.createCell(1).setCellValue(r + 1);
            row.createCell(2).setCellValue(1);
            row.createCell(3).setCellValue(1);
        }
        Cell plain = s.getRow(0).createCell(0);
        plain.setCellFormula("SUMPRODUCT(SUMIFS(B1:B3, C1:C3, D1:D3))");
        assertEquals(18, fe.evaluate(plain).getNumberValue(), 1e-9);
        s.setArrayFormula("SUMPRODUCT(SUMIFS(B1:B3, C1:C3, D1:D3))", CellRangeAddress.valueOf("A2"));
        assertEquals(18, fe.evaluate(s.getRow(1).getCell(0)).getNumberValue(), 1e-9);
    }

    @Test
    void theResultIsAnArrayNotASum() {
        // weighted by E: 3*1 + 3*2 + 3*3 + 0*4
        assertNumber(3 + 6 + 9, "SUMPRODUCT(SUMIFS(B1:B4,C1:C4,D1:D4),E1:E4)");
        // COUNTIFS per criterion: 2, 2, 2, 0
        assertArrayFormula(2, "MAX(COUNTIFS(C1:C4,D1:D4))");
        assertArrayFormula(0, "MIN(COUNTIFS(C1:C4,D1:D4))");
        assertArrayFormula(6, "SUM(COUNTIFS(C1:C4,D1:D4))");
        // AVERAGEIFS per criterion: 1.5, 1.5, 1.5 and #DIV/0! for z, which has no match
        assertNumber(1.5, "INDEX(AVERAGEIFS(B1:B4,C1:C4,D1:D4),2)");
        assertArrayFormula(4.5, "SUM(AVERAGEIFS(B1:B3,C1:C3,D1:D3))");
        assertNumber(3 + 4, "INDEX(SUMIFS(B1:B4,C1:C4,{\"x\",\"y\"}),2)");
    }

    @Test
    void arrayConstantsAsCriteria() {
        // SUM(COUNTIFS(range,{...})) - bug 70005 - keeps working, as do the other aggregates
        assertNumber(2 + 2, "SUM(COUNTIFS(C1:C4,{\"x\",\"y\"}))");
        assertNumber(3 + 7, "SUM(SUMIFS(B1:B4,C1:C4,{\"x\",\"y\"}))");
        assertNumber(7, "MAX(SUMIFS(B1:B4,C1:C4,{\"x\",\"y\"}))");
        assertNumber(3 + 7, "SUM(SUMIF(C1:C4,{\"x\",\"y\"},B1:B4))");
        assertNumber(2 + 2, "SUM(COUNTIF(C1:C4,{\"x\",\"y\"}))");
        // a criterion with no match contributes 0 / an empty count
        assertNumber(3, "SUM(SUMIF(C1:C4,{\"x\",\"q\"},B1:B4))");
    }

    @Test
    void singleConditionFunctionsWithARangeAsCriteria() {
        // SUMIF per criterion in D: x -> 3, x -> 3, x -> 3, z -> 0
        assertNumber(9, "SUMPRODUCT(SUMIF(C1:C4,D1:D4,B1:B4))");
        assertNumber(3 * 2, "SUMPRODUCT(COUNTIF(C1:C4,D1:D4))");
        assertArrayFormula(1.5, "MAX(AVERAGEIF(C1:C3,D1:D3,B1:B3))");
        // SUMIF without a sum range sums the tested range itself: E is numeric, criteria from E
        assertNumber(1 + 2 + 3 + 4, "SUMPRODUCT(SUMIF(E1:E4,E1:E4))");
    }

    @Test
    void severalArrayCriteriaArePairedElementWise() {
        // (C=x and D=x) -> 2, (C=x and D=x) -> 2, (C=y and D=x) -> 1, (C=y and D=z) -> 1
        assertNumber(2 + 2 + 1 + 1, "SUMPRODUCT(COUNTIFS(C1:C4,C1:C4,D1:D4,D1:D4))");
        assertArrayFormula(2, "MAX(COUNTIFS(C1:C4,C1:C4,D1:D4,D1:D4))");
        // arrays of different shapes cannot be paired
        CellValue cv = evalIn(9, "SUMPRODUCT(COUNTIFS(C1:C4,C1:C4,D1:D4,{\"x\",\"z\"}))");
        assertEquals(CellType.ERROR, cv.getCellType());
        assertEquals(FormulaError.VALUE, FormulaError.forInt(cv.getErrorValue()));
    }

    @Test
    void aRangeCriteriaInAnOrdinaryCellIsImplicitlyIntersected() {
        // outside array context Excel reduces the criteria range to the formula's row: D1 = x
        assertPlain(0, 3, "SUMIFS(B1:B4,C1:C4,D1:D4)");
        assertPlain(3, 0, "SUMIFS(B1:B4,C1:C4,D1:D4)");
        assertPlain(1, 3, "SUMIF(C1:C4,D1:D4,B1:B4)");
        assertPlain(0, 2, "SUM(COUNTIFS(C1:C4,D1:D4))");
        // ... and on a row the range does not cover the criterion is the #VALUE! error, which for
        // SUMIF matches only cells holding that error (FormulaEvalTestData.xls, SUMIF row)
        assertPlain(9, 0, "SUMIF(C1:C4,D1:D4,B1:B4)");
        // the same formulas in array context see every criterion
        assertArrayFormula(9, "SUMPRODUCT(SUMIFS(B1:B4,C1:C4,D1:D4))");
        assertArrayFormula(6, "SUM(COUNTIFS(C1:C4,D1:D4))");
    }

    @Test
    void scalarCriteriaAreUnchanged() {
        assertNumber(3, "SUMIFS(B1:B4,C1:C4,\"x\")");
        assertNumber(2, "COUNTIFS(C1:C4,\"x\",D1:D4,\"x\")");
        assertNumber(3, "SUMIF(C1:C4,\"x\",B1:B4)");
        assertNumber(3, "SUMIF(C1:C4,D1,B1:B4)");
        assertNumber(1.5, "AVERAGEIFS(B1:B4,C1:C4,\"x\",D1:D4,\"x\")");
        assertNumber(4, "MAXIFS(B1:B4,C1:C4,\"y\")");
        assertNumber(3, "MINIFS(B1:B4,C1:C4,\"y\")");
    }
}
