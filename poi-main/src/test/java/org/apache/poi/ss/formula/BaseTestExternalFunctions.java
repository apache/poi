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

import junit.framework.TestCase;
import org.apache.poi.ss.ITestDataProvider;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.StringEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.formula.functions.FreeRefFunction;
import org.apache.poi.ss.formula.udf.DefaultUDFFinder;
import org.apache.poi.ss.formula.udf.UDFFinder;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * Test setting / evaluating of Analysis Toolpack and user-defined functions
 *
 * @author Yegor Kozlov
 */
public class BaseTestExternalFunctions extends TestCase {
    // define two custom user-defined functions
    private static class MyFunc implements FreeRefFunction {
        public MyFunc() {
            //
        }

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


    protected final ITestDataProvider _testDataProvider;

    /**
     * @param testDataProvider an object that provides test data in HSSF / XSSF specific way
     */
    protected BaseTestExternalFunctions(ITestDataProvider testDataProvider) {
        _testDataProvider = testDataProvider;
    }

    public void testExternalFunctions() {
        Workbook wb = _testDataProvider.createWorkbook();

        Sheet sh = wb.createSheet();

        Cell cell1 = sh.createRow(0).createCell(0);
        cell1.setCellFormula("ISODD(1)+ISEVEN(2)"); // functions from the Excel Analysis Toolpack
        assertEquals("ISODD(1)+ISEVEN(2)", cell1.getCellFormula());

        Cell cell2 = sh.createRow(1).createCell(0);
        try {
            cell2.setCellFormula("MYFUNC(\"B1\")");
            fail("Should fail because MYFUNC is an unknown function");
        } catch (FormulaParseException e){
            ; //expected
        }

        wb.addToolPack(customToolpack);

        cell2.setCellFormula("MYFUNC(\"B1\")");
        assertEquals("MYFUNC(\"B1\")", cell2.getCellFormula());

        Cell cell3 = sh.createRow(2).createCell(0);
        cell3.setCellFormula("MYFUNC2(\"C1\")&\"-\"&A2");  //where A2 is defined above
        assertEquals("MYFUNC2(\"C1\")&\"-\"&A2", cell3.getCellFormula());

        FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
        assertEquals(2.0, evaluator.evaluate(cell1).getNumberValue());
        assertEquals("B1abc", evaluator.evaluate(cell2).getStringValue());
        assertEquals("C1abc2-B1abc", evaluator.evaluate(cell3).getStringValue());

    }

    /**
     * test invoking saved ATP functions
     *
     * @param testFile  either atp.xls or atp.xlsx
     */
    public void baseTestInvokeATP(String testFile){
        Workbook wb = _testDataProvider.openSampleWorkbook(testFile);
        FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();

        Sheet sh  = wb.getSheetAt(0);
        // these two are not imlemented in r
        assertEquals("DELTA(1.3,1.5)", sh.getRow(0).getCell(1).getCellFormula());
        assertEquals("COMPLEX(2,4)", sh.getRow(1).getCell(1).getCellFormula());

        Cell cell2 = sh.getRow(2).getCell(1);
        assertEquals("ISODD(2)", cell2.getCellFormula());
        assertEquals(false, evaluator.evaluate(cell2).getBooleanValue());
        assertEquals(Cell.CELL_TYPE_BOOLEAN, evaluator.evaluateFormulaCell(cell2));

        Cell cell3 = sh.getRow(3).getCell(1);
        assertEquals("ISEVEN(2)", cell3.getCellFormula());
        assertEquals(true, evaluator.evaluate(cell3).getBooleanValue());
        assertEquals(Cell.CELL_TYPE_BOOLEAN, evaluator.evaluateFormulaCell(cell3));

    }

}
