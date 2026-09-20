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

import java.util.Random;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * Builds a workbook whose formulas read the same large ranges over and over: the shape that
 * makes the evaluator's plain value cache matter. Compare {@link InvoiceWorkbookBuilder}, where
 * almost every formula reads only the cells of its own row.
 * <p>
 * The {@code Prices} sheet is a lookup table of {@code tableRows} items (code in A, price in B).
 * The {@code Orders} sheet has {@code rowCount} data rows; each row holds a code drawn from the
 * table and a quantity, and five formulas:
 * <pre>
 *      A          B      C                                           D        E                                     F                              G
 *  1   Code       Qty    Price                                       Value    QtyForCode                            OrdersForCode                  Share
 *  n   ITEM-0042  7      =VLOOKUP(A,Prices!$A$2:$B$T,2,FALSE)      =B*C     =SUMIF($A$2:$A$N,A,$B$2:$B$N)   =COUNTIF($A$2:$A$N,A)       =B/E
 * </pre>
 * followed by {@code SUM} and {@code SUMPRODUCT} totals. Every {@code VLOOKUP} scans the key
 * column of the table until it finds its code, and every {@code SUMIF}/{@code COUNTIF} reads the
 * whole Code column (and {@code SUMIF} the matching Qty cells), so one pass over {@code N} rows
 * reads roughly {@code N * (T / 2 + 2 * N)} plain cells, of which only {@code 2 * (T + N)} are
 * distinct.
 */
public final class LookupWorkbookBuilder {

    private static final int DATA_FIRST_ROW = 1; // 0-based; row 0 is the header

    private final int rowCount;
    private final int tableRows;
    private final long seed;

    /**
     * @param rowCount  number of data rows on the Orders sheet
     * @param tableRows number of items in the Prices lookup table
     * @param seed      seed for the pseudo-random plain values, so runs are repeatable
     */
    public LookupWorkbookBuilder(int rowCount, int tableRows, long seed) {
        this.rowCount = rowCount;
        this.tableRows = tableRows;
        this.seed = seed;
    }

    /** the code of the {@code index}-th item (0-based) of the Prices table */
    public static String itemCode(int index) {
        return String.format(java.util.Locale.ROOT, "ITEM-%05d", index);
    }

    /**
     * Fills the given (empty) workbook.
     *
     * @return the workbook passed in
     */
    public <W extends Workbook> W build(W wb) {
        Random rnd = new Random(seed);

        Sheet prices = wb.createSheet("Prices");
        Row ph = prices.createRow(0);
        ph.createCell(0).setCellValue("Code");
        ph.createCell(1).setCellValue("Price");
        for (int i = 0; i < tableRows; i++) {
            Row r = prices.createRow(i + 1);
            r.createCell(0).setCellValue(itemCode(i));
            r.createCell(1).setCellValue(Math.round(rnd.nextDouble() * 10000) / 100.0);
        }
        String priceRange = "Prices!$A$2:$B$" + (tableRows + 1);

        Sheet orders = wb.createSheet("Orders");
        Row header = orders.createRow(0);
        String[] titles = {"Code", "Qty", "Price", "Value", "QtyForCode", "OrdersForCode", "Share"};
        for (int c = 0; c < titles.length; c++) {
            header.createCell(c).setCellValue(titles[c]);
        }

        int lastDataRow = DATA_FIRST_ROW + rowCount - 1;
        String first = String.valueOf(DATA_FIRST_ROW + 1);
        String last = String.valueOf(lastDataRow + 1);
        String codes = "$A$" + first + ":$A$" + last;
        String qtys = "$B$" + first + ":$B$" + last;
        for (int r = DATA_FIRST_ROW; r <= lastDataRow; r++) {
            Row row = orders.createRow(r);
            int excelRow = r + 1;
            row.createCell(0).setCellValue(itemCode(rnd.nextInt(tableRows)));
            row.createCell(1).setCellValue(1 + rnd.nextInt(50));
            row.createCell(2).setCellFormula("VLOOKUP(A" + excelRow + "," + priceRange + ",2,FALSE)");
            row.createCell(3).setCellFormula("B" + excelRow + "*C" + excelRow);
            row.createCell(4).setCellFormula("SUMIF(" + codes + ",A" + excelRow + "," + qtys + ")");
            row.createCell(5).setCellFormula("COUNTIF(" + codes + ",A" + excelRow + ")");
            row.createCell(6).setCellFormula("B" + excelRow + "/E" + excelRow);
        }

        // totals, one blank row below the data
        Row totals = orders.createRow(lastDataRow + 2);
        totals.createCell(1).setCellFormula("SUM(B" + first + ":B" + last + ")");
        totals.createCell(3).setCellFormula("SUM(D" + first + ":D" + last + ")");
        totals.createCell(4).setCellFormula("SUMPRODUCT(B" + first + ":B" + last + ",C" + first + ":C" + last + ")");
        totals.createCell(6).setCellFormula("SUM(G" + first + ":G" + last + ")");

        return wb;
    }
}
