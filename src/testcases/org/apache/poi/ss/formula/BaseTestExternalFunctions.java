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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.apache.poi.ss.ITestDataProvider;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.NotImplementedException;
import org.apache.poi.ss.formula.eval.NotImplementedFunctionException;
import org.apache.poi.ss.formula.eval.StringEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.formula.functions.FreeRefFunction;
import org.apache.poi.ss.formula.udf.DefaultUDFFinder;
import org.apache.poi.ss.formula.udf.UDFFinder;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Test;

/**
 * Test setting / evaluating of Analysis Toolpack and user-defined functions
 */
public abstract class BaseTestExternalFunctions {
    // define two custom user-defined functions
    private static class MyFunc implements FreeRefFunction {
        public MyFunc() {
            //
        }

        @Override
        public ValueEval evaluate(ValueEval[] args, OperationEvaluationContext ec) {
            if (args.length != 1 || !(args[0] instanceof StringEval)) {
                return ErrorEval.VALUE_INVALID;
            }
            StringEval input = (StringEval) args[0];
            return new StringEval(input.getStringValue() + "abc");
        }
    }

    private static class MyFunc2 implements FreeRefFunction {
        public MyFunc2() {
            //
        }

        @Override
        public ValueEval evaluate(ValueEval[] args, OperationEvaluationContext ec) {
            if (args.length != 1 || !(args[0] instanceof StringEval)) {
                return ErrorEval.VALUE_INVALID;
            }
            StringEval input = (StringEval) args[0];
            return new StringEval(input.getStringValue() + "abc2");
        }
    }

    /**
     * register the two test UDFs in a UDF finder, to be passed to the workbook
     */
    private static UDFFinder customToolpack = new DefaultUDFFinder(
            new String[] { "myFunc", "myFunc2"},
            new FreeRefFunction[] { new MyFunc(), new MyFunc2()}
    );


    private final ITestDataProvider _testDataProvider;
    private final String atpFile;

    /**
     * @param testDataProvider an object that provides test data in HSSF / XSSF specific way
     */
    protected BaseTestExternalFunctions(ITestDataProvider testDataProvider, String atpFile) {
        _testDataProvider = testDataProvider;
        this.atpFile = atpFile;
    }

    @Test
    public void testExternalFunctions() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();
        FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();

        Sheet sh = wb.createSheet();

        Cell cell1 = sh.createRow(0).createCell(0);
        // functions from the Excel Analysis Toolpack
        cell1.setCellFormula("ISODD(1)+ISEVEN(2)");
        assertEquals("ISODD(1)+ISEVEN(2)", cell1.getCellFormula());

        Cell cell2 = sh.createRow(1).createCell(0);
        // unregistered functions are parseable and renderable, but may not be evaluateable
        cell2.setCellFormula("MYFUNC(\"B1\")"); 
        try {
            evaluator.evaluate(cell2);
            fail("Expected NotImplementedFunctionException/NotImplementedException");
        } catch (final NotImplementedException e) {
            assertTrue(e.getCause() instanceof NotImplementedFunctionException);
            // Alternatively, a future implementation of evaluate could return #NAME? error to align behavior with Excel
            // assertEquals(ErrorEval.NAME_INVALID, ErrorEval.valueOf(evaluator.evaluate(cell2).getErrorValue()));
        }

        wb.addToolPack(customToolpack);

        cell2.setCellFormula("MYFUNC(\"B1\")");
        assertEquals("MYFUNC(\"B1\")", cell2.getCellFormula());

        Cell cell3 = sh.createRow(2).createCell(0);
        cell3.setCellFormula("MYFUNC2(\"C1\")&\"-\"&A2");  //where A2 is defined above
        assertEquals("MYFUNC2(\"C1\")&\"-\"&A2", cell3.getCellFormula());

        assertEquals(2.0, evaluator.evaluate(cell1).getNumberValue(), 0);
        assertEquals("B1abc", evaluator.evaluate(cell2).getStringValue());
        assertEquals("C1abc2-B1abc", evaluator.evaluate(cell3).getStringValue());

        wb.close();
    }

    /**
     * test invoking saved ATP functions
     *
     * @param testFile  either atp.xls or atp.xlsx
     */
    @Test
    public void baseTestInvokeATP() throws IOException {
        Workbook wb = _testDataProvider.openSampleWorkbook(atpFile);
        FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();

        Sheet sh  = wb.getSheetAt(0);
        // these two are not implemented in r
        assertEquals("DELTA(1.3,1.5)", sh.getRow(0).getCell(1).getCellFormula());
        assertEquals("COMPLEX(2,4)", sh.getRow(1).getCell(1).getCellFormula());

        Cell cell2 = sh.getRow(2).getCell(1);
        assertEquals("ISODD(2)", cell2.getCellFormula());
        assertEquals(false, evaluator.evaluate(cell2).getBooleanValue());
        assertEquals(CellType.BOOLEAN, evaluator.evaluateFormulaCell(cell2));

        Cell cell3 = sh.getRow(3).getCell(1);
        assertEquals("ISEVEN(2)", cell3.getCellFormula());
        assertEquals(true, evaluator.evaluate(cell3).getBooleanValue());
        assertEquals(CellType.BOOLEAN, evaluator.evaluateFormulaCell(cell3));

        wb.close();
    }

}
