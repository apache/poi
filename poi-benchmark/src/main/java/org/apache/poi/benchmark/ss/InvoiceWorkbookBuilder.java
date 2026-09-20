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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * Builds a workbook that looks like a real invoice / order sheet, for benchmarking formula
 * evaluation. Works with any {@link Workbook} implementation.
 * <p>
 * Sheet {@code Orders} has a header row, {@code rowCount} data rows and a summary block:
 * <pre>
 *      A     B     C          D         E                  F                        G      H                                   I                                 J      K          L
 *  1   Qty   Item  UnitPrice  Discount  Net                Tax                      Total  Size                                ListPrice                         Diff   Running    Flag
 *  n   10    Bolt  4.25       0.1       =A*C*(1-D)         =ROUND(E*TaxRate,2)      =E+F   =IF(G>Params!$B$2,"large","small")  =VLOOKUP(B,Prices!$A:$B,2,FALSE)  =C-I   =K(n-1)+G  =IF(J&lt;&gt;0,"CHECK","")
 * </pre>
 * followed by totals: {@code SUM}, {@code AVERAGE}, {@code MAX}, {@code COUNTIF} over the data
 * columns, one {@code SUMIF} per item, and a grand total that depends on those. {@code TaxRate}
 * is a defined name for a cell on the {@code Params} sheet; {@code Prices} is a small lookup table.
 * <p>
 * Every formula depends on plain values in its row; F, G, H, J, K and L depend on other formulas;
 * K is a chain {@code rowCount} deep; the summary block depends on every data row.
 */
public final class InvoiceWorkbookBuilder {

    /** the items sold; also the rows of the {@code Prices} lookup table */
    private static final String[] ITEMS = {"Bolt", "Nut", "Washer", "Screw", "Bracket"};
    private static final double[] LIST_PRICES = {4.25, 1.10, 0.35, 2.80, 12.50};

    private static final int DATA_FIRST_ROW = 1; // 0-based; row 0 is the header

    private final int rowCount;
    private final long seed;

    /**
     * @param rowCount number of data rows
     * @param seed     seed for the pseudo-random plain values, so runs are repeatable
     */
    public InvoiceWorkbookBuilder(int rowCount, long seed) {
        this.rowCount = rowCount;
        this.seed = seed;
    }

    /**
     * Fills the given (empty) workbook.
     *
     * @return the workbook passed in
     */
    public <W extends Workbook> W build(W wb) {
        Random rnd = new Random(seed);

        Sheet params = wb.createSheet("Params");
        Row p1 = params.createRow(0);
        p1.createCell(0).setCellValue("TaxRate");
        p1.createCell(1).setCellValue(0.2);
        Row p2 = params.createRow(1);
        p2.createCell(0).setCellValue("LargeOrder");
        p2.createCell(1).setCellValue(500);
        Row p3 = params.createRow(2);
        p3.createCell(0).setCellValue("Shipping");
        p3.createCell(1).setCellValue(9.99);

        Name taxRate = wb.createName();
        taxRate.setNameName("TaxRate");
        taxRate.setRefersToFormula("Params!$B$1");

        Sheet prices = wb.createSheet("Prices");
        Row ph = prices.createRow(0);
        ph.createCell(0).setCellValue("Item");
        ph.createCell(1).setCellValue("ListPrice");
        for (int i = 0; i < ITEMS.length; i++) {
            Row r = prices.createRow(i + 1);
            r.createCell(0).setCellValue(ITEMS[i]);
            r.createCell(1).setCellValue(LIST_PRICES[i]);
        }
        String priceRange = "Prices!$A$2:$B$" + (ITEMS.length + 1);

        Sheet orders = wb.createSheet("Orders");
        Row header = orders.createRow(0);
        String[] titles = {"Qty", "Item", "UnitPrice", "Discount", "Net", "Tax", "Total",
                "Size", "ListPrice", "Diff", "Running", "Flag"};
        for (int c = 0; c < titles.length; c++) {
            header.createCell(c).setCellValue(titles[c]);
        }

        int lastDataRow = DATA_FIRST_ROW + rowCount - 1;
        for (int r = DATA_FIRST_ROW; r <= lastDataRow; r++) {
            Row row = orders.createRow(r);
            int item = rnd.nextInt(ITEMS.length);
            int excelRow = r + 1;

            row.createCell(0).setCellValue(1 + rnd.nextInt(50));
            row.createCell(1).setCellValue(ITEMS[item]);
            // mostly the list price, sometimes a manual override so that Diff/Flag vary
            double unitPrice = rnd.nextInt(10) == 0 ? LIST_PRICES[item] * 1.1 : LIST_PRICES[item];
            row.createCell(2).setCellValue(unitPrice);
            row.createCell(3).setCellValue(rnd.nextInt(4) * 0.05);

            row.createCell(4).setCellFormula("A" + excelRow + "*C" + excelRow + "*(1-D" + excelRow + ")");
            row.createCell(5).setCellFormula("ROUND(E" + excelRow + "*TaxRate,2)");
            row.createCell(6).setCellFormula("E" + excelRow + "+F" + excelRow);
            row.createCell(7).setCellFormula("IF(G" + excelRow + ">Params!$B$2,\"large\",\"small\")");
            row.createCell(8).setCellFormula("VLOOKUP(B" + excelRow + "," + priceRange + ",2,FALSE)");
            row.createCell(9).setCellFormula("C" + excelRow + "-I" + excelRow);
            if (r == DATA_FIRST_ROW) {
                row.createCell(10).setCellFormula("G" + excelRow);
            } else {
                row.createCell(10).setCellFormula("K" + (excelRow - 1) + "+G" + excelRow);
            }
            row.createCell(11).setCellFormula("IF(J" + excelRow + "<>0,\"CHECK\",\"\")");
        }

        // summary block, one blank row below the data
        String first = String.valueOf(DATA_FIRST_ROW + 1);
        String last = String.valueOf(lastDataRow + 1);
        int s = lastDataRow + 2;
        Row totals = orders.createRow(s);
        totals.createCell(0).setCellFormula("SUM(A" + first + ":A" + last + ")");
        totals.createCell(4).setCellFormula("SUM(E" + first + ":E" + last + ")");
        totals.createCell(5).setCellFormula("SUM(F" + first + ":F" + last + ")");
        totals.createCell(6).setCellFormula("SUM(G" + first + ":G" + last + ")");
        totals.createCell(7).setCellFormula("COUNTIF(H" + first + ":H" + last + ",\"large\")");
        totals.createCell(11).setCellFormula("COUNTIF(L" + first + ":L" + last + ",\"CHECK\")");

        Row stats = orders.createRow(s + 1);
        stats.createCell(6).setCellFormula("AVERAGE(G" + first + ":G" + last + ")");
        stats.createCell(7).setCellFormula("MAX(G" + first + ":G" + last + ")");
        stats.createCell(8).setCellFormula("MIN(G" + first + ":G" + last + ")");

        // one SUMIF per item
        for (int i = 0; i < ITEMS.length; i++) {
            Row r = orders.createRow(s + 3 + i);
            r.createCell(1).setCellValue(ITEMS[i]);
            r.createCell(6).setCellFormula("SUMIF(B" + first + ":B" + last + ",B" + (s + 3 + i + 1)
                    + ",G" + first + ":G" + last + ")");
        }

        Row grand = orders.createRow(s + 3 + ITEMS.length + 1);
        int totalsExcelRow = s + 1;
        grand.createCell(1).setCellValue("Grand total");
        // volume discount on the total, plus shipping unless the order is big enough
        grand.createCell(6).setCellFormula("IF(G" + totalsExcelRow + ">10000,G" + totalsExcelRow + "*0.95,G"
                + totalsExcelRow + ")+IF(G" + totalsExcelRow + ">Params!$B$2*10,0,Params!$B$3)");

        return wb;
    }

    /** @return every formula cell in the workbook, in sheet/row/column order */
    public static List<Cell> formulaCells(Workbook wb) {
        List<Cell> result = new ArrayList<>();
        for (Sheet sheet : wb) {
            for (Row row : sheet) {
                for (Cell cell : row) {
                    if (cell.getCellType() == CellType.FORMULA) {
                        result.add(cell);
                    }
                }
            }
        }
        return result;
    }
}
