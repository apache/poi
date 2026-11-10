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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellReference;
import org.junit.jupiter.api.Test;

/**
 * The benchmark workbook must evaluate cleanly, otherwise the benchmarks measure error handling.
 */
class TestLookupWorkbookBuilder {

    private static final int ROWS = 50;
    private static final int TABLE = 200;

    @Test
    void everyFormulaEvaluatesToTheExpectedValue() throws IOException {
        try (HSSFWorkbook wb = new LookupWorkbookBuilder(ROWS, TABLE, 42L).build(new HSSFWorkbook())) {
            List<Cell> formulas = InvoiceWorkbookBuilder.formulaCells(wb);
            assertEquals(ROWS * 5 + 4, formulas.size());

            FormulaEvaluator fe = wb.getCreationHelper().createFormulaEvaluator();
            for (Cell cell : formulas) {
                CellValue cv = fe.evaluate(cell);
                assertNotEquals(CellType.ERROR, cv.getCellType(),
                        () -> new CellReference(cell).formatAsString() + " =" + cell.getCellFormula() + " -> " + cv);
            }

            // work out the lookups and aggregates independently
            Sheet prices = wb.getSheet("Prices");
            Map<String, Double> priceByCode = new HashMap<>();
            for (int i = 1; i <= TABLE; i++) {
                Row r = prices.getRow(i);
                priceByCode.put(r.getCell(0).getStringCellValue(), r.getCell(1).getNumericCellValue());
            }
            Sheet orders = wb.getSheet("Orders");
            Map<String, Double> qtyByCode = new HashMap<>();
            Map<String, Integer> countByCode = new HashMap<>();
            double totalValue = 0;
            for (int i = 1; i <= ROWS; i++) {
                Row r = orders.getRow(i);
                String code = r.getCell(0).getStringCellValue();
                double qty = r.getCell(1).getNumericCellValue();
                qtyByCode.merge(code, qty, Double::sum);
                countByCode.merge(code, 1, Integer::sum);
                totalValue += qty * priceByCode.get(code);
            }
            for (int i = 1; i <= ROWS; i++) {
                Row r = orders.getRow(i);
                String code = r.getCell(0).getStringCellValue();
                double qty = r.getCell(1).getNumericCellValue();
                assertEquals(priceByCode.get(code), fe.evaluate(r.getCell(2)).getNumberValue(), 1e-9, "price " + code);
                assertEquals(qtyByCode.get(code), fe.evaluate(r.getCell(4)).getNumberValue(), 1e-9, "qty for " + code);
                assertEquals(countByCode.get(code), fe.evaluate(r.getCell(5)).getNumberValue(), 1e-9, "orders for " + code);
                assertEquals(qty / qtyByCode.get(code), fe.evaluate(r.getCell(6)).getNumberValue(), 1e-9, "share " + code);
            }
            Row totals = orders.getRow(ROWS + 2);
            assertEquals(totalValue, fe.evaluate(totals.getCell(3)).getNumberValue(), 1e-6);
            assertEquals(totalValue, fe.evaluate(totals.getCell(4)).getNumberValue(), 1e-6);
            // the shares of each code add up to one, so the sum of shares is the number of codes used
            assertEquals(countByCode.size(), fe.evaluate(totals.getCell(6)).getNumberValue(), 1e-9);
        }
    }

    @Test
    void sameSeedGivesSameWorkbook() throws IOException {
        try (HSSFWorkbook a = new LookupWorkbookBuilder(20, 30, 7L).build(new HSSFWorkbook());
             HSSFWorkbook b = new LookupWorkbookBuilder(20, 30, 7L).build(new HSSFWorkbook())) {
            FormulaEvaluator fa = a.getCreationHelper().createFormulaEvaluator();
            FormulaEvaluator fb = b.getCreationHelper().createFormulaEvaluator();
            Cell ta = a.getSheet("Orders").getRow(22).getCell(3);
            Cell tb = b.getSheet("Orders").getRow(22).getCell(3);
            assertEquals(fa.evaluate(ta).getNumberValue(), fb.evaluate(tb).getNumberValue(), 0);
        }
    }
}
