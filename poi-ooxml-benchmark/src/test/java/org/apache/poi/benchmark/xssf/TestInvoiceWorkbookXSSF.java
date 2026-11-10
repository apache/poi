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

package org.apache.poi.benchmark.xssf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.IOException;
import java.util.List;

import org.apache.poi.benchmark.ss.InvoiceWorkbookBuilder;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

/**
 * The benchmark workbook must evaluate cleanly as an XSSFWorkbook too.
 */
class TestInvoiceWorkbookXSSF {

    private static final int ROWS = 100;

    @Test
    void everyFormulaEvaluatesWithoutError() throws IOException {
        try (XSSFWorkbook wb = new InvoiceWorkbookBuilder(ROWS, 42L).build(new XSSFWorkbook())) {
            List<Cell> formulas = InvoiceWorkbookBuilder.formulaCells(wb);
            assertEquals(ROWS * 8 + 6 + 3 + 5 + 1, formulas.size());

            FormulaEvaluator fe = wb.getCreationHelper().createFormulaEvaluator();
            for (Cell cell : formulas) {
                CellValue cv = fe.evaluate(cell);
                assertNotEquals(CellType.ERROR, cv.getCellType(),
                        () -> new CellReference(cell).formatAsString() + " =" + cell.getCellFormula() + " -> " + cv);
            }

            Sheet orders = wb.getSheet("Orders");
            double runningTotal = fe.evaluate(orders.getRow(ROWS).getCell(10)).getNumberValue();
            double sumTotal = fe.evaluate(orders.getRow(ROWS + 2).getCell(6)).getNumberValue();
            assertEquals(sumTotal, runningTotal, 1e-6);
        }
    }
}
