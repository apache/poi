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

package org.apache.poi.ss.formula.atp;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.OperationEvaluationContext;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.formula.functions.FreeRefFunction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestAnalysisToolPak {
    private static class NullFunction implements FreeRefFunction {
        @Override
        public ValueEval evaluate(ValueEval[] args, OperationEvaluationContext ec) {
            return ErrorEval.DIV_ZERO;
        }
    }

    @Test
    void testOverrideKnownButUnimplemented() {
        // JIS is a known function in Excel, but it is not implemented in POI
        AnalysisToolPak.registerFunction("JIS", new NullFunction());
        try (HSSFWorkbook workbook = new HSSFWorkbook()) {
            HSSFSheet sheet = workbook.createSheet("Sheet1");
            HSSFCell cell = sheet.createRow(0).createCell(0);
            cell.setCellFormula("JIS(\"test\")");
            workbook.getCreationHelper().createFormulaEvaluator().evaluateAll();
            assertEquals(ErrorEval.DIV_ZERO.getErrorCode(), cell.getErrorCellValue());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testOverrideUnknown() {
        assertThrows(IllegalArgumentException.class, () -> {
            AnalysisToolPak.registerFunction("UNKNOWN", new NullFunction());
        });
    }

    @Test
    void testOverrideUnknownButForceAllowed() {
        AnalysisToolPak.registerFunction("FAKE", new NullFunction(), true);
        try (HSSFWorkbook workbook = new HSSFWorkbook()) {
            HSSFSheet sheet = workbook.createSheet("Sheet1");
            HSSFCell cell = sheet.createRow(0).createCell(0);
            cell.setCellFormula("FAKE(\"test\")");
            workbook.getCreationHelper().createFormulaEvaluator().evaluateAll();
            assertEquals(ErrorEval.DIV_ZERO.getErrorCode(), cell.getErrorCellValue());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
