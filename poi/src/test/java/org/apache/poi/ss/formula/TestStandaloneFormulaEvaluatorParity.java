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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;

import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.eval.NotImplementedException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.CompiledFormula;
import org.apache.poi.ss.usermodel.LightCellValue;
import org.apache.poi.ss.usermodel.StandaloneFormulaEngine;
import org.apache.poi.ss.usermodel.StandaloneFormulaEvaluator;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Asserts that the standalone formula engine (virtual single-row sheet) returns
 * exactly the same results as workbook-backed formula evaluation (HSSF), proving
 * both share the same evaluation engine.
 */
class TestStandaloneFormulaEvaluatorParity {

    private record Case(String formula, Object[] values) {
        static Case of(String formula, Object... values) {
            return new Case(formula, values);
        }
    }

    static Stream<Case> cases() {
        return Stream.of(
                Case.of("A1+B1*C1", 1.0, 2.0, 3.0),
                Case.of("A2*2", 5.0),
                Case.of("A2+B1", 5.0, 7.0),
                Case.of("SUM(A2:B2)", 1.0, 2.0),
                Case.of("(A1+B1)*C1", 1.0, 2.0, 3.0),
                Case.of("-A1", 5.0),
                Case.of("2^10", new Object[0]),
                Case.of("50%*3", new Object[0]),
                Case.of("A1&B1&C1", "a", "b", "c"),
                Case.of("A1&B1", "x=", 12.0),
                Case.of("\"total: \"&A1", "sum", null),
                Case.of("A1=B1", "hop", "HOP"),
                Case.of("A1<>B1", "hop", "poi"),
                Case.of("A1<B1", 1.0, 2.0),
                Case.of("A1<=1", 1.0),
                Case.of("A1>B1", "z", "a"),
                Case.of("\"z\"<TRUE", new Object[0]),
                Case.of("IF(A1=\"Y\",1,0)", "Y", null),
                Case.of("IF(A1=\"Y\",1,0)", "N", null),
                Case.of("IF(TRUE,1,1/0)", new Object[0]),
                Case.of("IF(FALSE,1/0,2)", new Object[0]),
                Case.of("IF(A1>10,\"big\",IF(A1>5,\"medium\",\"small\"))", 7.0, null),
                Case.of("OR(A1=\"a\",A1=\"b\",A1=\"c\")", "c", null),
                Case.of("AND(A1>0,B1>0)", 1.0, 2.0),
                Case.of("NOT(A1)", Boolean.TRUE, null),
                Case.of("ABS(A1)", -3.0, null),
                Case.of("LEN(A1)", "abcd", null),
                Case.of("TRIM(A1)", "  a  b  ", null),
                Case.of("LEFT(A1,2)", "abcd", null),
                Case.of("RIGHT(A1,2)", "abcd", null),
                Case.of("MID(A1,2,2)", "abcd", null),
                Case.of("ROUND(A1,2)", 2.567, null),
                Case.of("INT(A1)", 2.9, null),
                Case.of("MOD(A1,B1)", 5.0, 2.0),
                Case.of("SUM(A1:B1)", 1.0, 2.0),
                Case.of("MIN(A1:B1)", 3.0, -1.0),
                Case.of("MAX(A1:B1)", 3.0, -1.0),
                Case.of("COUNT(A1:B1)", 1.0, null),
                Case.of("SUM(A1:B1)*2", 10.0, 20.0),
                Case.of("ISBLANK(A1)", new Object[]{null}),
                Case.of("ISNUMBER(A1)", 1.0, null),
                Case.of("ISTEXT(A1)", "x", null),
                Case.of("ISERROR(A1)", new Object[]{null}),
                Case.of("A1*2", new Object[]{null}),
                Case.of("A1&\"\"", new Object[]{null}),
                Case.of("ISNA(A1)", new Object[]{null}),
                Case.of("CHOOSE(2,\"a\",\"b\")", new Object[0]),
                Case.of("EXACT(A1,B1)", "a", "A"),
                Case.of("UPPER(A1)&LOWER(B1)", "aB", "Cd"),
                Case.of("A1+1", "x", null),
                Case.of("1/0", new Object[0]),
                Case.of("NA()", new Object[0]),
                Case.of("DATEDIF(DATE(2020,1,1),DATE(2020,3,1),\"d\")", new Object[0])
        );
    }

    @ParameterizedTest
    @MethodSource("cases")
    void parityWithHssfWorkbookEvaluation(Case testCase) {
        CellValue workbookResult = evaluateWithWorkbook(testCase.formula(), testCase.values());
        LightCellValue standaloneResult = evaluateStandalone(testCase.formula(), testCase.values());
        if (workbookResult == null && standaloneResult == null) {
            return;
        }

        assertEquals(workbookResult.getCellType(), standaloneResult.getCellType(),
                () -> testCase.formula() + ": cell type mismatch");
        switch (workbookResult.getCellType()) {
            case NUMERIC:
                assertEquals(workbookResult.getNumberValue(), standaloneResult.getNumberValue(), 1e-12,
                        () -> testCase.formula() + ": value mismatch");
                break;
            case STRING:
                assertEquals(workbookResult.getStringValue(), standaloneResult.getStringValue(),
                        () -> testCase.formula() + ": value mismatch");
                break;
            case BOOLEAN:
                assertEquals(workbookResult.getBooleanValue(), standaloneResult.getBooleanValue(),
                        () -> testCase.formula() + ": value mismatch");
                break;
            case ERROR:
                assertEquals(workbookResult.getErrorValue(), standaloneResult.getErrorValue(),
                        () -> testCase.formula() + ": error code mismatch");
                break;
            default:
                throw new IllegalStateException("Unexpected " + workbookResult.getCellType());
        }
    }

    /**
     * Evaluates the formula on a real one-row workbook: the input values fill cells
     * A1..N1, the formula sits in the cell after the last input.
     */
    private CellValue evaluateWithWorkbook(String formula, Object[] values) {
        try (HSSFWorkbook workbook = new HSSFWorkbook()) {
            HSSFSheet sheet = workbook.createSheet("Sheet1");
            HSSFRow row = sheet.createRow(0);
            for (int i = 0; i < values.length; i++) {
                Object value = values[i];
                if (value == null) {
                    continue;
                }
                Cell cell = row.createCell(i);
                if (value instanceof Number number) {
                    cell.setCellValue(number.doubleValue());
                } else if (value instanceof String string) {
                    cell.setCellValue(string);
                } else if (value instanceof Boolean bool) {
                    cell.setCellValue(bool);
                } else {
                    throw new IllegalArgumentException("Unsupported " + value);
                }
            }
            Cell formulaCell = row.createCell(values.length);
            if (isDATEDIF(formula)) {
                // both paths fail at evaluation time, but the workbook path decorates
                // the exception with the cell reference - only check the type here
                assertThrows(NotImplementedException.class, () -> evaluateCell(workbook, formulaCell, formula));
                return null;
            }
            return evaluateCell(workbook, formulaCell, formula);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private CellValue evaluateCell(HSSFWorkbook workbook, Cell formulaCell, String formula) {
        formulaCell.setCellFormula(formula);
        return new HSSFFormulaEvaluator(workbook).evaluate(formulaCell);
    }

    private LightCellValue evaluateStandalone(String formula, Object[] values) {
        StandaloneFormulaEngine.Builder builder = StandaloneFormulaEngine.newBuilder();
        for (int i = 0; i < values.length; i++) {
            builder.input("input" + i);
        }
        CompiledFormula compiled = builder.build().compile(formula);
        StandaloneFormulaEvaluator evaluator = compiled.newEvaluator();
        evaluator.setValues(values);
        if (isDATEDIF(formula)) {
            assertThrows(NotImplementedException.class, evaluator::evaluate);
            return null;
        }
        return evaluator.evaluate();
    }

    private boolean isDATEDIF(String formula) {
        return formula.startsWith("DATEDIF");
    }
}
