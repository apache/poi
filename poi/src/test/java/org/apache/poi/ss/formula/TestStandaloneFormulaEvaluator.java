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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.formula.eval.NotImplementedException;
import org.apache.poi.ss.usermodel.CompiledFormula;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaError;
import org.apache.poi.ss.usermodel.LightCellValue;
import org.apache.poi.ss.usermodel.StandaloneFormulaEngine;
import org.apache.poi.ss.usermodel.StandaloneFormulaEvaluator;
import org.junit.jupiter.api.Test;

class TestStandaloneFormulaEvaluator {

    private static StandaloneFormulaEngine engine(String... inputs) {
        StandaloneFormulaEngine.Builder builder = StandaloneFormulaEngine.newBuilder();
        for (String input : inputs) {
            builder.input(input);
        }
        return builder.build();
    }

    private static LightCellValue eval(String formula, String[] inputs, Object[] values) {
        StandaloneFormulaEvaluator evaluator = engine(inputs).compile(formula).newEvaluator();
        return evaluator.evaluate(values);
    }

    @Test
    void literalsAndArithmetic() {
        assertEquals(7.0, eval("1+2*3", new String[0], new Object[0]).getNumberValue());
        assertEquals(9.0, eval("(1+2)*3", new String[0], new Object[0]).getNumberValue());
        assertEquals(-4.0, eval("-2*2", new String[0], new Object[0]).getNumberValue());
        assertEquals(8.0, eval("2^3", new String[0], new Object[0]).getNumberValue());
        assertEquals(1.0, eval("50%*2", new String[0], new Object[0]).getNumberValue());
        assertEquals(3.5, eval("7/2", new String[0], new Object[0]).getNumberValue());
    }

    @Test
    void typedResults() {
        assertEquals(CellTypeAssertion.NUMERIC, CellTypeAssertion.of(eval("1", new String[0], new Object[0])));
        assertSame(LightCellValue.TRUE, eval("TRUE", new String[0], new Object[0]));
        assertSame(LightCellValue.FALSE, eval("FALSE", new String[0], new Object[0]));
        assertEquals("ab", eval("\"a\"&\"b\"", new String[0], new Object[0]).getStringValue());
        assertEquals("12", eval("1&2", new String[0], new Object[0]).getStringValue());
    }

    @Test
    void inputReferences() {
        assertEquals(6.0, eval("amount*price", new String[]{"amount", "price"}, new Object[]{2.0, 3.0}).getNumberValue());
        assertEquals("ab", eval("a&b", new String[]{"a", "b"}, new Object[]{"a", "b"}).getStringValue());
        assertEquals(11.0, eval("A1+B1", new String[]{"a", "b"}, new Object[]{5.0, 6.0}).getNumberValue());
    }

    @Test
    void comparisons() {
        assertEquals(Boolean.TRUE, eval("1<2", new String[0], new Object[0]).getBooleanValue());
        assertEquals(Boolean.TRUE, eval("2>=2", new String[0], new Object[0]).getBooleanValue());
        assertEquals(Boolean.TRUE, eval("\"a\"=\"A\"", new String[0], new Object[0]).getBooleanValue());
        assertEquals(Boolean.TRUE, eval("\"a\"<>\"b\"", new String[0], new Object[0]).getBooleanValue());
        assertEquals(Boolean.TRUE, eval("\"z\"<TRUE", new String[0], new Object[0]).getBooleanValue());
        assertEquals(Boolean.FALSE, eval("5>\"a\"", new String[0], new Object[0]).getBooleanValue());
        assertEquals(Boolean.TRUE, eval("\"a\">5", new String[0], new Object[0]).getBooleanValue());
        assertEquals(Boolean.FALSE, eval("\"1\"=1", new String[0], new Object[0]).getBooleanValue());
    }

    @Test
    void ifIsLazy() {
        assertEquals(1.0, eval("IF(TRUE,1,1/0)", new String[0], new Object[0]).getNumberValue());
        assertEquals(2.0, eval("IF(FALSE,1/0,2)", new String[0], new Object[0]).getNumberValue());
        assertEquals(Boolean.TRUE, eval("IF(1>0,TRUE,FALSE)", new String[0], new Object[0]).getBooleanValue());
        assertEquals("no", eval("IF(a=\"Y\",\"yes\",\"no\")", new String[]{"a"}, new Object[]{"N"}).getStringValue());
    }

    @Test
    void logicalFunctions() {
        assertEquals(Boolean.TRUE, eval("AND(TRUE,1=1)", new String[0], new Object[0]).getBooleanValue());
        assertEquals(Boolean.TRUE, eval("OR(FALSE,a=\"x\")", new String[]{"a"}, new Object[]{"x"}).getBooleanValue());
        assertEquals(Boolean.TRUE, eval("NOT(FALSE)", new String[0], new Object[0]).getBooleanValue());
        assertEquals(Boolean.FALSE, eval("AND(TRUE,FALSE)", new String[0], new Object[0]).getBooleanValue());
    }

    @Test
    void commonFunctions() {
        assertEquals(3.0, eval("ABS(-3)", new String[0], new Object[0]).getNumberValue());
        assertEquals(3.0, eval("LEN(\"abc\")", new String[0], new Object[0]).getNumberValue());
        assertEquals("a b", eval("TRIM(\"  a  b  \")", new String[0], new Object[0]).getStringValue());
        assertEquals("ab", eval("LEFT(\"abcd\",2)", new String[0], new Object[0]).getStringValue());
        assertEquals("cd", eval("RIGHT(\"abcd\",2)", new String[0], new Object[0]).getStringValue());
        assertEquals("bc", eval("MID(\"abcd\",2,2)", new String[0], new Object[0]).getStringValue());
        assertEquals(2.57, eval("ROUND(2.567,2)", new String[0], new Object[0]).getNumberValue());
        assertEquals(2.0, eval("INT(2.9)", new String[0], new Object[0]).getNumberValue());
        assertEquals(1.0, eval("MOD(5,2)", new String[0], new Object[0]).getNumberValue());
        assertEquals("b", eval("CHOOSE(2,\"a\",\"b\",\"c\")", new String[0], new Object[0]).getStringValue());
        assertEquals(16.0, eval("CONCATENATE(\"1\",2)+4", new String[0], new Object[0]).getNumberValue());
        assertEquals(2020.0, eval("YEAR(DATE(2020,3,15))", new String[0], new Object[0]).getNumberValue());
    }

    @Test
    void rangeFunctions() {
        assertEquals(3.0, eval("SUM(A1:B1)", new String[]{"a", "b"}, new Object[]{1.0, 2.0}).getNumberValue());
        assertEquals(1.0, eval("MIN(A1:B1)", new String[]{"a", "b"}, new Object[]{1.0, 2.0}).getNumberValue());
        assertEquals(2.0, eval("MAX(A1:B1)", new String[]{"a", "b"}, new Object[]{1.0, 2.0}).getNumberValue());
        assertEquals(2.0, eval("COUNT(A1:B1)", new String[]{"a", "b"}, new Object[]{1.0, 2.0}).getNumberValue());
        assertEquals(6.0, eval("SUM(A1:B1)*2", new String[]{"a", "b"}, new Object[]{1.0, 2.0}).getNumberValue());
    }

    @Test
    void typedSetters() {
        StandaloneFormulaEvaluator evaluator = engine("n", "s", "b", "d", "e").compile("n&s&b").newEvaluator();
        evaluator.setNumber(0, 1.5);
        evaluator.setString(1, "x");
        evaluator.setBoolean(2, true);
        assertEquals("1.5xTRUE", evaluator.evaluate().getStringValue());

        Date date = Date.from(LocalDate.of(2020, 3, 15).atStartOfDay(ZoneOffset.UTC).toInstant());
        StandaloneFormulaEvaluator dateEvaluator = engine("d").compile("d").newEvaluator();
        dateEvaluator.setDate(0, date);
        assertEquals(DateUtil.getExcelDate(date), dateEvaluator.evaluate().getNumberValue());

        StandaloneFormulaEvaluator errorEvaluator = engine("e").compile("e").newEvaluator();
        errorEvaluator.setError(0, FormulaError.NA);
        assertEquals(FormulaError.NA.getCode(), errorEvaluator.evaluate().getErrorValue());
        assertEquals(FormulaError.NA.getCode(), errorEvaluator.setError(0, FormulaError.NA).evaluate().getErrorValue());

        StandaloneFormulaEvaluator blankEvaluator = engine("x").compile("ISBLANK(x)").newEvaluator();
        assertEquals(Boolean.TRUE, blankEvaluator.setBlank(0).evaluate().getBooleanValue());
    }

    @Test
    void setValueDispatch() {
        assertEquals(4.0, eval("A1*2", new String[]{"a"}, new Object[]{Integer.valueOf(2)}).getNumberValue());
        assertEquals(4.0, eval("A1*2", new String[]{"a"}, new Object[]{java.math.BigDecimal.valueOf(2)}).getNumberValue());
        assertEquals(Boolean.TRUE, eval("A1", new String[]{"a"}, new Object[]{Boolean.TRUE}).getBooleanValue());
        assertEquals(Boolean.TRUE, eval("ISBLANK(A1)", new String[]{"a"}, new Object[]{null}).getBooleanValue());
        Date date = Date.from(LocalDate.of(2021, 1, 1).atStartOfDay(ZoneOffset.UTC).toInstant());
        assertEquals(DateUtil.getExcelDate(date), eval("A1", new String[]{"a"}, new Object[]{date}).getNumberValue());
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> eval("A1", new String[]{"a"}, new Object[]{new Object()}));
        assertTrue(e.getMessage().contains("Unsupported value type"));
    }

    @Test
    void blankSemantics() {
        assertEquals(0.0, eval("A1", new String[]{"a"}, new Object[1]).getNumberValue());
        assertEquals(Boolean.TRUE, eval("A1=0", new String[]{"a"}, new Object[1]).getBooleanValue());
        assertEquals(Boolean.TRUE, eval("A1=\"\"", new String[]{"a"}, new Object[1]).getBooleanValue());
        assertEquals(Boolean.TRUE, eval("ISBLANK(Z99)", new String[]{"a"}, new Object[1]).getBooleanValue());
        assertEquals("", eval("A1&\"\"", new String[]{"a"}, new Object[1]).getStringValue());
    }

    @Test
    void valuesAreNotCachedBetweenEvaluations() {
        StandaloneFormulaEvaluator evaluator = engine("a").compile("a*2").newEvaluator();
        evaluator.setNumber(0, 1.0);
        assertEquals(2.0, evaluator.evaluate().getNumberValue());
        evaluator.setNumber(0, 5.0);
        assertEquals(10.0, evaluator.evaluate().getNumberValue());
        evaluator.setBlank(0);
        assertEquals(0.0, evaluator.evaluate().getNumberValue());
    }

    @Test
    void resetClearsAllInputs() {
        StandaloneFormulaEvaluator evaluator = engine("a", "b").compile("ISBLANK(a)&ISBLANK(b)").newEvaluator();
        evaluator.setValues(1.0, 2.0);
        assertEquals("FALSEFALSE", evaluator.evaluate().getStringValue());
        assertEquals("TRUETRUE", evaluator.reset().evaluate().getStringValue());
    }

    @Test
    void bulkAndOneShotEvaluation() {
        StandaloneFormulaEvaluator evaluator = engine("a", "b").compile("a*b").newEvaluator();
        evaluator.setValues(2.0, 3.0);
        assertEquals(6.0, evaluator.evaluate().getNumberValue());
        assertEquals(20.0, evaluator.evaluate(4.0, 5.0).getNumberValue());
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> evaluator.evaluate(1.0, 2.0, 3.0));
        assertTrue(e.getMessage().contains("values"));
    }

    @Test
    void errorPropagation() {
        LightCellValue result = eval("1/0", new String[0], new Object[0]);
        assertEquals(CellTypeAssertion.ERROR, CellTypeAssertion.of(result));
        assertEquals(FormulaError.DIV0.getCode(), result.getErrorValue());

        StandaloneFormulaEvaluator isNa = engine("a").compile("ISNA(a)").newEvaluator();
        assertEquals(Boolean.FALSE, isNa.evaluate().getBooleanValue());
        assertEquals(Boolean.TRUE, isNa.setError(0, FormulaError.NA).evaluate().getBooleanValue());

        StandaloneFormulaEvaluator na = engine("a").compile("a+1").newEvaluator();
        na.setError(0, FormulaError.NA);
        assertEquals(FormulaError.NA.getCode(), na.evaluate().getErrorValue());
    }

    @Test
    void unimplementedFunctionThrows() {
        CompiledFormula compiled = engine().compile("DATEDIF(DATE(2020,1,1),DATE(2020,3,1),\"d\")");
        assertThrows(NotImplementedException.class, compiled.newEvaluator()::evaluate);
    }

    @Test
    void unknownFunctionsResolveThroughUdfFinder() {
        assertEquals("fallback", eval("IFERROR(1/0,\"fallback\")", new String[0], new Object[0]).getStringValue());
        CompiledFormula unknown = engine().compile("NOTAFUNCTION(1)");
        assertThrows(NotImplementedException.class, unknown.newEvaluator()::evaluate);
    }

    @Test
    void unknownIdentifierFailsAtCompile() {
        assertThrows(FormulaParseException.class, () -> engine("a").compile("a+unknownName"));
    }

    @Test
    void sheetQualifiedReferenceFailsAtCompile() {
        assertThrows(UnsupportedOperationException.class, () -> engine("a").compile("Sheet2!A1"));
    }

    @Test
    void indirectOnVirtualSheet() {
        assertEquals(5.0, eval("INDIRECT(\"Sheet1!A1\")", new String[]{"a"}, new Object[]{5.0}).getNumberValue());
        LightCellValue missing = eval("INDIRECT(\"Nope!A1\")", new String[]{"a"}, new Object[]{5.0});
        assertEquals(CellTypeAssertion.ERROR, CellTypeAssertion.of(missing));
    }

    @Test
    void wholeColumnRange() {
        StandaloneFormulaEngine ex97 = StandaloneFormulaEngine.newBuilder()
                .inputs("a", "b")
                .spreadsheetVersion(SpreadsheetVersion.EXCEL97)
                .build();
        StandaloneFormulaEvaluator evaluator = ex97.compile("SUM(A:B)").newEvaluator();
        evaluator.setValues(1.0, 2.0);
        assertEquals(3.0, evaluator.evaluate().getNumberValue());
    }

    @Test
    void builderValidation() {
        IllegalArgumentException duplicate = assertThrows(IllegalArgumentException.class,
                () -> StandaloneFormulaEngine.newBuilder().inputs("a", "A").build());
        assertTrue(duplicate.getMessage().contains("Duplicate"));
        IllegalArgumentException blank = assertThrows(IllegalArgumentException.class,
                () -> StandaloneFormulaEngine.newBuilder().inputs("a", " ").build());
        assertTrue(blank.getMessage().contains("blank") || blank.getMessage().contains("null"));

        StandaloneFormulaEngine.Builder tooMany = StandaloneFormulaEngine.newBuilder()
                .spreadsheetVersion(SpreadsheetVersion.EXCEL97);
        for (int i = 0; i <= SpreadsheetVersion.EXCEL97.getMaxColumns(); i++) {
            tooMany.input("col" + i);
        }
        assertTrue(assertThrows(IllegalArgumentException.class, tooMany::build).getMessage().contains("Too many"));
    }

    @Test
    void invalidInputNamesRejected() {
        // names that would be parsed as cell references instead of resolving to the input
        for (String name : new String[]{"Q1", "FY2024", "H2", "A1", "IV65536", "XFD1"}) {
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> StandaloneFormulaEngine.newBuilder().inputs("x", name).build(),
                    () -> "name " + name);
            assertTrue(e.getMessage().contains("'" + name + "'"), name + " not in: " + e.getMessage());
        }
        // names that would be parsed as literals or violate the defined-name rules
        for (String name : new String[]{"true", "FALSE", "True", "R", "c", "2024", "1048576",
                "My Name", "A$1", "a-b", "x*y", ".Data", ""}) {
            assertThrows(IllegalArgumentException.class,
                    () -> StandaloneFormulaEngine.newBuilder().inputs(name).build(),
                    () -> "name " + name);
        }
        // eager rejection, before build()
        StandaloneFormulaEngine.Builder builder = StandaloneFormulaEngine.newBuilder();
        assertThrows(IllegalArgumentException.class, () -> builder.input("My Name"));
        // 256 characters
        StringBuilder longName = new StringBuilder("a");
        for (int i = 0; i < 255; i++) {
            longName.append('x');
        }
        assertThrows(IllegalArgumentException.class, () -> builder.input(longName.toString()));
    }

    @Test
    void validInputNamesAccepted() {
        StandaloneFormulaEngine engine = StandaloneFormulaEngine.newBuilder()
                .inputs("Q1_2024", "Total2024", "XFD", "a", "true1", "caf\u00e9", "_priv", "\\abs")
                .build();
        CompiledFormula compiled = engine.compile("Q1_2024*Total2024+a");
        StandaloneFormulaEvaluator evaluator = compiled.newEvaluator();
        evaluator.setValues(2.0, 3.0, 0.0, 4.0);
        assertEquals(10.0, evaluator.evaluate().getNumberValue());
        assertEquals(2, compiled.getInputIndex("XFD"));
        assertEquals(-1, compiled.getInputIndex("Nope"));
    }

    @Test
    void cellReferenceNamesAreVersionDependent() {
        // IW123 is a cell of EXCEL2007 (column IW exists) but a valid name under EXCEL97
        assertThrows(IllegalArgumentException.class,
                () -> StandaloneFormulaEngine.newBuilder().inputs("IW123").build());
        StandaloneFormulaEngine ex97 = StandaloneFormulaEngine.newBuilder()
                .inputs("IW123")
                .spreadsheetVersion(SpreadsheetVersion.EXCEL97)
                .build();
        StandaloneFormulaEvaluator evaluator = ex97.compile("IW123+1").newEvaluator();
        evaluator.setValues(41.0);
        assertEquals(42.0, evaluator.evaluate().getNumberValue());
    }

    @Test
    void compileRequiresBuild() {
        StandaloneFormulaEngineImpl unbuilt = (StandaloneFormulaEngineImpl) StandaloneFormulaEngine.newBuilder().input("a");
        assertThrows(IllegalStateException.class, () -> unbuilt.compile("a"));
    }

    @Test
    void inputIndexLookupAndBounds() {
        CompiledFormula compiled = engine("Alpha", "beta").compile("A1+B1");
        assertEquals(0, compiled.getInputIndex("alpha"));
        assertEquals(1, compiled.getInputIndex("BETA"));
        assertEquals(-1, compiled.getInputIndex("gamma"));
        assertEquals(List.of("Alpha", "beta"), compiled.getInputNames());
        assertEquals("A1+B1", compiled.getFormula());

        StandaloneFormulaEvaluator evaluator = compiled.newEvaluator();
        assertThrows(IndexOutOfBoundsException.class, () -> evaluator.setNumber(-1, 1.0));
        assertThrows(IndexOutOfBoundsException.class, () -> evaluator.setNumber(2, 1.0));
        assertThrows(IndexOutOfBoundsException.class, () -> evaluator.setString(2, "x"));
        assertThrows(IndexOutOfBoundsException.class, () -> evaluator.setValue(9, 1.0));
    }

    @Test
    void nullStringInputRejected() {
        StandaloneFormulaEvaluator evaluator = engine("s").compile("s").newEvaluator();
        assertThrows(NullPointerException.class, () -> evaluator.setString(0, null));
    }

    @Test
    void invalidDateRejected() {
        StandaloneFormulaEvaluator evaluator = engine("d").compile("d").newEvaluator();
        assertThrows(IllegalArgumentException.class, () -> evaluator.setDate(0, new Date(Long.MIN_VALUE)));
    }

    @Test
    void concurrentCompilationIsThreadSafe() throws Exception {
        StandaloneFormulaEngine standaloneEngine = engine("a", "b");
        int threads = 8;
        ThreadFactory threadFactory = r -> new Thread(r, "standalone-formula-compile-test");
        ExecutorService executor = new ThreadPoolExecutor(threads, threads, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(), threadFactory);
        try {
            List<Future<Double>> results = new ArrayList<>();
            for (int i = 0; i < threads; i++) {
                results.add(executor.submit(() -> standaloneEngine.compile("a*b+1").newEvaluator().evaluate(2.0, 3.0).getNumberValue()));
            }
            for (Future<Double> result : results) {
                assertEquals(7.0, result.get(10, TimeUnit.SECONDS));
            }
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void engineWithoutInputs() {
        assertEquals(42.0, engine().compile("6*7").newEvaluator().evaluate().getNumberValue());
    }

    private enum CellTypeAssertion {
        NUMERIC, STRING, BOOLEAN, ERROR;

        static CellTypeAssertion of(LightCellValue value) {
            return switch (value.getCellType()) {
                case NUMERIC -> NUMERIC;
                case STRING -> STRING;
                case BOOLEAN -> BOOLEAN;
                case ERROR -> ERROR;
                default -> throw new IllegalStateException("Unexpected " + value.getCellType());
            };
        }
    }
}
