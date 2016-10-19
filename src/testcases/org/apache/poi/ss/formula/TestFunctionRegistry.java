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

import junit.framework.TestCase;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.formula.atp.AnalysisToolPak;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.FunctionEval;
import org.apache.poi.ss.formula.eval.NotImplementedException;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.formula.functions.FreeRefFunction;
import org.apache.poi.ss.formula.functions.Function;
import org.apache.poi.ss.usermodel.CellValue;

/**
 *
 * @author Yegor Kozlov
 */
public class TestFunctionRegistry extends TestCase {

	public void testRegisterInRuntime() {
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("Sheet1");
		HSSFRow row = sheet.createRow(0);
        HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);

		HSSFCell cellA = row.createCell(0);
		cellA.setCellFormula("FISHER(A5)");
		CellValue cv;
		try {
			cv = fe.evaluate(cellA);
            fail("expectecd exception");
		} catch (NotImplementedException e) {
        }

        FunctionEval.registerFunction("FISHER", new Function() {
            @Override
            public ValueEval evaluate(ValueEval[] args, int srcRowIndex, int srcColumnIndex) {
                return ErrorEval.NA;
            }
        });

        cv = fe.evaluate(cellA);
        assertEquals(ErrorEval.NA.getErrorCode(), cv.getErrorValue());

        HSSFCell cellB = row.createCell(1);
        cellB.setCellFormula("CUBEMEMBERPROPERTY(A5)");
        try {
            cv = fe.evaluate(cellB);
            fail("expectecd exception");
        } catch (NotImplementedException e) {
        }

        AnalysisToolPak.registerFunction("CUBEMEMBERPROPERTY", new FreeRefFunction() {
            @Override
            public ValueEval evaluate(ValueEval[] args, OperationEvaluationContext ec) {
                return ErrorEval.NUM_ERROR;
            }
        });

        cv = fe.evaluate(cellB);
        assertEquals(ErrorEval.NUM_ERROR.getErrorCode(), cv.getErrorValue());
	}

    public void testExceptions() {
        Function func = new Function() {
            @Override
            public ValueEval evaluate(ValueEval[] args, int srcRowIndex, int srcColumnIndex) {
                return ErrorEval.NA;
            }
        };
        try {
            FunctionEval.registerFunction("SUM", func);
            fail("expectecd exception");
        } catch (IllegalArgumentException e){
            assertEquals("POI already implememts SUM" +
                    ". You cannot override POI's implementations of Excel functions", e.getMessage());
        }
        try {
            FunctionEval.registerFunction("SUMXXX", func);
            fail("expectecd exception");
        } catch (IllegalArgumentException e){
            assertEquals("Unknown function: SUMXXX", e.getMessage());
        }
        try {
            FunctionEval.registerFunction("ISODD", func);
            fail("expectecd exception");
        } catch (IllegalArgumentException e){
            assertEquals("ISODD is a function from the Excel Analysis Toolpack. " +
                    "Use AnalysisToolpack.registerFunction(String name, FreeRefFunction func) instead.", e.getMessage());
        }

        FreeRefFunction atpFunc = new FreeRefFunction() {
            @Override
            public ValueEval evaluate(ValueEval[] args, OperationEvaluationContext ec) {
                return ErrorEval.NUM_ERROR;
            }
        };
        try {
            AnalysisToolPak.registerFunction("ISODD", atpFunc);
            fail("expectecd exception");
        } catch (IllegalArgumentException e){
            assertEquals("POI already implememts ISODD" +
                    ". You cannot override POI's implementations of Excel functions", e.getMessage());
        }
        try {
            AnalysisToolPak.registerFunction("ISODDXXX", atpFunc);
            fail("expectecd exception");
        } catch (IllegalArgumentException e){
            assertEquals("ISODDXXX is not a function from the Excel Analysis Toolpack.", e.getMessage());
        }
        try {
            AnalysisToolPak.registerFunction("SUM", atpFunc);
            fail("expectecd exception");
        } catch (IllegalArgumentException e){
            assertEquals("SUM is a built-in Excel function. " +
                    "Use FunctoinEval.registerFunction(String name, Function func) instead.", e.getMessage());
        }
    }
}
