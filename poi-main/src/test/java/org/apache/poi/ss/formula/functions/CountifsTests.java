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

import junit.framework.TestCase;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.atp.AnalysisToolPak;
import org.apache.poi.ss.usermodel.*;

public class CountifsTests extends TestCase {

    public void testCallFunction() {
        HSSFWorkbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet("test");
        Row row1 = sheet.createRow(0);
        Cell cellA1 = row1.createCell(0, Cell.CELL_TYPE_FORMULA);
        Cell cellB1 = row1.createCell(1, Cell.CELL_TYPE_NUMERIC);
        Cell cellC1 = row1.createCell(2, Cell.CELL_TYPE_NUMERIC);
        Cell cellD1 = row1.createCell(3, Cell.CELL_TYPE_NUMERIC);
        Cell cellE1 = row1.createCell(4, Cell.CELL_TYPE_NUMERIC);
        cellB1.setCellValue(1);
        cellC1.setCellValue(1);
        cellD1.setCellValue(2);
        cellE1.setCellValue(4);

        cellA1.setCellFormula("COUNTIFS(B1:C1,1, D1:E1,2)");
        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
        CellValue evaluate = evaluator.evaluate(cellA1);
        assertEquals(1.0d, evaluate.getNumberValue());
    }

    public void testCallFunction_invalidArgs() {
        HSSFWorkbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet("test");
        Row row1 = sheet.createRow(0);
        Cell cellA1 = row1.createCell(0, Cell.CELL_TYPE_FORMULA);
        cellA1.setCellFormula("COUNTIFS()");
        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
        CellValue evaluate = evaluator.evaluate(cellA1);
        assertEquals(15, evaluate.getErrorValue());
        cellA1.setCellFormula("COUNTIFS(A1:C1)");
        evaluator = workbook.getCreationHelper().createFormulaEvaluator();
        evaluate = evaluator.evaluate(cellA1);
        assertEquals(15, evaluate.getErrorValue());
        cellA1.setCellFormula("COUNTIFS(A1:C1,2,2)");
        evaluator = workbook.getCreationHelper().createFormulaEvaluator();
        evaluate = evaluator.evaluate(cellA1);
        assertEquals(15, evaluate.getErrorValue());
    }
}
