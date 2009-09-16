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

package org.apache.poi.hssf.record.formula.eval;

import java.io.IOException;

import junit.framework.TestCase;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.record.formula.functions.FreeRefFunction;
import org.apache.poi.hssf.record.formula.toolpack.DefaultToolPack;
import org.apache.poi.hssf.record.formula.toolpack.MainToolPacksHandler;
import org.apache.poi.hssf.record.formula.toolpack.ToolPack;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.OperationEvaluationContext;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * 
 * @author Josh Micich
 * 
 * Modified 09/14/09 by Petr Udalau - Test of registering UDFs in workbook and
 * using ToolPacks.
 */
public final class TestExternalFunction extends TestCase {

    private static class MyFunc implements FreeRefFunction {
        public ValueEval evaluate(ValueEval[] args, OperationEvaluationContext ec) {
            if (args.length != 1 || !(args[0] instanceof StringEval)) {
                return ErrorEval.VALUE_INVALID;
            } else {
                StringEval input = (StringEval) args[0];
                return new StringEval(input.getStringValue() + "abc");
            }
        }
    }

    private static class MyFunc2 implements FreeRefFunction {
        public ValueEval evaluate(ValueEval[] args, OperationEvaluationContext ec) {
            if (args.length != 1 || !(args[0] instanceof StringEval)) {
                return ErrorEval.VALUE_INVALID;
            } else {
                StringEval input = (StringEval) args[0];
                return new StringEval(input.getStringValue() + "abc2");
            }
        }
    }

    /**
     * Creates and registers user-defined function "MyFunc()" directly with POI.
     * This is VB function defined in "testNames.xls". In future there must be
     * some parser of VBA scripts which will register UDFs.
     */
    private void registerMyFunc(Workbook workbook) {
        workbook.registerUserDefinedFunction("myFunc", new MyFunc());
    }

    /**
     * Creates example ToolPack which contains function "MyFunc2()".
     */
    private void createExampleToolPack() {
        ToolPack exampleToolPack = new DefaultToolPack();
        exampleToolPack.addFunction("myFunc2", new MyFunc2());
        MainToolPacksHandler.instance().addToolPack(exampleToolPack);
    }

    /**
     * Checks that an external function can get invoked from the formula
     * evaluator.
     * 
     * @throws IOException
     * 
     */
    public void testInvoke() {
        HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("testNames.xls");
        HSSFSheet sheet = wb.getSheetAt(0);

        registerMyFunc(wb);
        createExampleToolPack();

        HSSFRow row = sheet.getRow(0);
        HSSFCell myFuncCell = row.getCell(1); //=myFunc("_")

        HSSFCell myFunc2Cell = row.getCell(2); //=myFunc2("_")

        HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
        try {
            assertEquals("_abc", fe.evaluate(myFuncCell).getStringValue());
            assertEquals("_abc2", fe.evaluate(myFunc2Cell).getStringValue());
        } catch (Exception e) {
            assertFalse(true);
        }
    }
}
