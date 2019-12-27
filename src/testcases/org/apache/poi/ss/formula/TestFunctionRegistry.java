/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.ss.formula;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.atp.AnalysisToolPak;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.FunctionEval;
import org.apache.poi.ss.formula.eval.NotImplementedException;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.usermodel.CellValue;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestFunctionRegistry {

    HSSFWorkbook wb;
    HSSFSheet sheet;
    HSSFRow row;
    HSSFFormulaEvaluator fe;

    @Before
    public void setup() {
        wb = new HSSFWorkbook();
        sheet = wb.createSheet("Sheet1");
        row = sheet.createRow(0);
        fe = new HSSFFormulaEvaluator(wb);
    }

    @After
    public void teardown() throws IOException {
        wb.close();
        wb = null;
        sheet = null;
        row = null;
        fe = null;
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
	public void testRegisterInRuntimeA() {
        HSSFCell cellA = row.createCell(0);
        cellA.setCellFormula("FISHER(A5)");
        thrown.expect(NotImplementedException.class);
        fe.evaluate(cellA);
    }

    @Test
    public void testRegisterInRuntimeB() {
        HSSFCell cellA = row.createCell(0);
        cellA.setCellFormula("FISHER(A5)");
        FunctionEval.registerFunction("FISHER", (args, srcRowIndex, srcColumnIndex) -> ErrorEval.NA);
        CellValue cv = fe.evaluate(cellA);
        assertEquals(ErrorEval.NA.getErrorCode(), cv.getErrorValue());
    }

    @Test
    public void testRegisterInRuntimeC() {
        HSSFCell cellB = row.createCell(1);
        cellB.setCellFormula("CUBEMEMBERPROPERTY(A5)");
        thrown.expect(NotImplementedException.class);
        fe.evaluate(cellB);
    }

    @Test
    public void testRegisterInRuntimeD() {
        HSSFCell cellB = row.createCell(1);
        cellB.setCellFormula("CUBEMEMBERPROPERTY(A5)");

        AnalysisToolPak.registerFunction("CUBEMEMBERPROPERTY", (args, ec) -> ErrorEval.NUM_ERROR);

        CellValue cv = fe.evaluate(cellB);
        assertEquals(ErrorEval.NUM_ERROR.getErrorCode(), cv.getErrorValue());
	}

	private static ValueEval na(ValueEval[] args, int srcRowIndex, int srcColumnIndex) {
        return ErrorEval.NA;
    }

    @Test
    public void testExceptionsA() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("POI already implememts SUM. You cannot override POI's implementations of Excel functions");
        FunctionEval.registerFunction("SUM", TestFunctionRegistry::na);
    }

    @Test
    public void testExceptionsB() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Unknown function: SUMXXX");
        FunctionEval.registerFunction("SUMXXX", TestFunctionRegistry::na);
    }

    @Test
    public void testExceptionsC() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("ISODD is a function from the Excel Analysis Toolpack. " +
            "Use AnalysisToolpack.registerFunction(String name, FreeRefFunction func) instead.");
        FunctionEval.registerFunction("ISODD", TestFunctionRegistry::na);
    }

    private static ValueEval atpFunc(ValueEval[] args, OperationEvaluationContext ec) {
        return ErrorEval.NUM_ERROR;
    }

    @Test
    public void testExceptionsD() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("POI already implememts ISODD. You cannot override POI's implementations of Excel functions");
        AnalysisToolPak.registerFunction("ISODD", TestFunctionRegistry::atpFunc);
    }

    @Test
    public void testExceptionsE() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("ISODDXXX is not a function from the Excel Analysis Toolpack.");
        AnalysisToolPak.registerFunction("ISODDXXX", TestFunctionRegistry::atpFunc);
    }

    @Test
    public void testExceptionsF() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("SUM is a built-in Excel function. " +
             "Use FunctoinEval.registerFunction(String name, Function func) instead.");
        AnalysisToolPak.registerFunction("SUM", TestFunctionRegistry::atpFunc);
    }
}
