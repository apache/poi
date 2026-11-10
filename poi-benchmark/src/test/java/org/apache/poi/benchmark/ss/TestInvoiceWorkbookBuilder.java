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

package org.apache.poi.benchmark.ss;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellReference;
import org.junit.jupiter.api.Test;

/**
 * The benchmark workbook must evaluate cleanly, otherwise the benchmarks measure error handling.
 */
class TestInvoiceWorkbookBuilder {

    private static final int ROWS = 100;

    @Test
    void everyFormulaEvaluatesWithoutError() throws IOException {
        try (HSSFWorkbook wb = new InvoiceWorkbookBuilder(ROWS, 42L).build(new HSSFWorkbook())) {
            List<Cell> formulas = InvoiceWorkbookBuilder.formulaCells(wb);
            // 8 formulas per data row, 6 totals, 3 stats, one SUMIF per item, the grand total
            assertEquals(ROWS * 8 + 6 + 3 + 5 + 1, formulas.size());

            FormulaEvaluator fe = wb.getCreationHelper().createFormulaEvaluator();
            for (Cell cell : formulas) {
                CellValue cv = fe.evaluate(cell);
                assertNotEquals(CellType.ERROR, cv.getCellType(),
                        () -> new CellReference(cell).formatAsString() + " =" + cell.getCellFormula() + " -> " + cv);
            }

            Sheet orders = wb.getSheet("Orders");
            // the running total of the last data row equals the total of the Total column
            double runningTotal = fe.evaluate(orders.getRow(ROWS).getCell(10)).getNumberValue();
            double sumTotal = fe.evaluate(orders.getRow(ROWS + 2).getCell(6)).getNumberValue();
            assertEquals(sumTotal, runningTotal, 1e-6);
            assertTrue(sumTotal > 0);
            // the SUMIFs partition the total
            double sumIfs = 0;
            for (int i = 0; i < 5; i++) {
                sumIfs += fe.evaluate(orders.getRow(ROWS + 5 + i).getCell(6)).getNumberValue();
            }
            assertEquals(sumTotal, sumIfs, 1e-6);
        }
    }

    @Test
    void sameSeedGivesSameWorkbook() throws IOException {
        try (HSSFWorkbook a = new InvoiceWorkbookBuilder(20, 7L).build(new HSSFWorkbook());
             HSSFWorkbook b = new InvoiceWorkbookBuilder(20, 7L).build(new HSSFWorkbook())) {
            FormulaEvaluator fa = a.getCreationHelper().createFormulaEvaluator();
            FormulaEvaluator fb = b.getCreationHelper().createFormulaEvaluator();
            Cell ga = a.getSheet("Orders").getRow(22).getCell(6);
            Cell gb = b.getSheet("Orders").getRow(22).getCell(6);
            assertEquals(fa.evaluate(ga).getNumberValue(), fb.evaluate(gb).getNumberValue(), 0);
        }
    }
}
