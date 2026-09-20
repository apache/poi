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

package org.apache.poi.ss.usermodel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.apache.poi.ss.ITestDataProvider;
import org.apache.poi.ss.util.CellReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * Shared fixture for tests that modify a workbook after its formulas have been evaluated and
 * check that the {@link FormulaEvaluator} produces the right results afterwards.
 * <p>
 * Every test runs against the same workbook, built by {@link #setUp()}. Sheet {@code Data} has a
 * header row, a block of plain data rows, a blank spare row and a block of formulas where formula
 * cells depend on other formula cells, plain cells, a lookup into a second sheet and a defined name:
 * <pre>
 *        A             B                         C                       D
 *  1   Qty           Item                      Cost                    Price
 *  2   10            Widget                    5                       =VLOOKUP(B2,Prices!$A$2:$B$4,2,FALSE)
 *  3   20            Gadget                    7                       =VLOOKUP(B3,Prices!$A$2:$B$4,2,FALSE)
 *  4   30            Gizmo                     9                       =VLOOKUP(B4,Prices!$A$2:$B$4,2,FALSE)
 *  5
 *  6   =SUM(A2:A4)                             =SUM(C2:C4)             =SUMPRODUCT(A2:A4,D2:D4)
 *  7   =A6*2         =A7+C6                    =COUNTIF(A2:A4,"&gt;15")
 *  8   =B7/3         =IF(A8&gt;40,"big","small")  =CONCATENATE(B2,"-",B8)
 *  9   =A8+A7        =Total+C7
 * </pre>
 * {@code Total} is a workbook-scoped name for {@code Data!$A$6}. Sheet {@code Prices} holds:
 * <pre>
 *        A         B
 *  1   Item      Price
 *  2   Widget    1.5
 *  3   Gadget    2.5
 *  4   Gizmo     3.5
 * </pre>
 * The header rows, the spare row 5 and the ranges over the data block are there so that row
 * insertion and deletion can be exercised against the same fixture.
 */
public abstract class BaseTestFormulaEvaluatorFixture {

    protected static final double DELTA = 1e-9;

    protected final ITestDataProvider _testDataProvider;

    protected Workbook wb;
    protected Sheet data;
    protected Sheet prices;
    protected FormulaEvaluator fe;

    protected BaseTestFormulaEvaluatorFixture(ITestDataProvider testDataProvider) {
        _testDataProvider = testDataProvider;
    }

    @BeforeEach
    void setUp() {
        wb = _testDataProvider.createWorkbook();
        data = wb.createSheet("Data");
        prices = wb.createSheet("Prices");

        Row ph = prices.createRow(0);
        ph.createCell(0).setCellValue("Item");
        ph.createCell(1).setCellValue("Price");
        Row p2 = prices.createRow(1);
        p2.createCell(0).setCellValue("Widget");
        p2.createCell(1).setCellValue(1.5);
        Row p3 = prices.createRow(2);
        p3.createCell(0).setCellValue("Gadget");
        p3.createCell(1).setCellValue(2.5);
        Row p4 = prices.createRow(3);
        p4.createCell(0).setCellValue("Gizmo");
        p4.createCell(1).setCellValue(3.5);

        Name total = wb.createName();
        total.setNameName("Total");
        total.setRefersToFormula("Data!$A$6");

        Row h = data.createRow(0);
        h.createCell(0).setCellValue("Qty");
        h.createCell(1).setCellValue("Item");
        h.createCell(2).setCellValue("Cost");
        h.createCell(3).setCellValue("Price");

        Row r2 = data.createRow(1);
        r2.createCell(0).setCellValue(10);
        r2.createCell(1).setCellValue("Widget");
        r2.createCell(2).setCellValue(5);
        r2.createCell(3).setCellFormula("VLOOKUP(B2,Prices!$A$2:$B$4,2,FALSE)");

        Row r3 = data.createRow(2);
        r3.createCell(0).setCellValue(20);
        r3.createCell(1).setCellValue("Gadget");
        r3.createCell(2).setCellValue(7);
        r3.createCell(3).setCellFormula("VLOOKUP(B3,Prices!$A$2:$B$4,2,FALSE)");

        Row r4 = data.createRow(3);
        r4.createCell(0).setCellValue(30);
        r4.createCell(1).setCellValue("Gizmo");
        r4.createCell(2).setCellValue(9);
        r4.createCell(3).setCellFormula("VLOOKUP(B4,Prices!$A$2:$B$4,2,FALSE)");

        // row 5 is intentionally left empty

        Row r6 = data.createRow(5);
        r6.createCell(0).setCellFormula("SUM(A2:A4)");
        r6.createCell(2).setCellFormula("SUM(C2:C4)");
        r6.createCell(3).setCellFormula("SUMPRODUCT(A2:A4,D2:D4)");

        Row r7 = data.createRow(6);
        r7.createCell(0).setCellFormula("A6*2");
        r7.createCell(1).setCellFormula("A7+C6");
        r7.createCell(2).setCellFormula("COUNTIF(A2:A4,\">15\")");

        Row r8 = data.createRow(7);
        r8.createCell(0).setCellFormula("B7/3");
        r8.createCell(1).setCellFormula("IF(A8>40,\"big\",\"small\")");
        r8.createCell(2).setCellFormula("CONCATENATE(B2,\"-\",B8)");

        Row r9 = data.createRow(8);
        r9.createCell(0).setCellFormula("A8+A7");
        r9.createCell(1).setCellFormula("Total+C7");

        fe = wb.getCreationHelper().createFormulaEvaluator();
        // populate the evaluator's cache with every formula result
        fe.evaluateAll();
        assertInitialValues();
    }

    @AfterEach
    void tearDown() throws IOException {
        wb.close();
    }

    /** @param ref an A1-style reference, optionally sheet-qualified; unqualified means the Data sheet */
    protected Cell cell(String ref) {
        CellReference cr = new CellReference(ref);
        Sheet sheet = cr.getSheetName() == null ? data : wb.getSheet(cr.getSheetName());
        return sheet.getRow(cr.getRow()).getCell(cr.getCol());
    }

    protected double num(String ref) {
        CellValue cv = fe.evaluate(cell(ref));
        assertEquals(CellType.NUMERIC, cv.getCellType(), ref + " should evaluate to a number");
        return cv.getNumberValue();
    }

    protected String str(String ref) {
        CellValue cv = fe.evaluate(cell(ref));
        assertEquals(CellType.STRING, cv.getCellType(), ref + " should evaluate to a string");
        return cv.getStringValue();
    }

    protected FormulaError err(String ref) {
        CellValue cv = fe.evaluate(cell(ref));
        assertEquals(CellType.ERROR, cv.getCellType(), ref + " should evaluate to an error");
        return FormulaError.forInt(cv.getErrorValue());
    }

    /** asserts every formula result of the untouched fixture */
    protected void assertInitialValues() {
        assertEquals(1.5, num("D2"), DELTA);
        assertEquals(2.5, num("D3"), DELTA);
        assertEquals(3.5, num("D4"), DELTA);
        assertEquals(60, num("A6"), DELTA);
        assertEquals(21, num("C6"), DELTA);
        assertEquals(170, num("D6"), DELTA);
        assertEquals(120, num("A7"), DELTA);
        assertEquals(141, num("B7"), DELTA);
        assertEquals(2, num("C7"), DELTA);
        assertEquals(47, num("A8"), DELTA);
        assertEquals("big", str("B8"));
        assertEquals("Widget-big", str("C8"));
        assertEquals(167, num("A9"), DELTA);
        assertEquals(62, num("B9"), DELTA);
    }
}
