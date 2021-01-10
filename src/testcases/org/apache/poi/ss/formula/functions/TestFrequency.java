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

import static org.apache.poi.ss.formula.functions.Frequency.histogram;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellRange;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.junit.jupiter.api.Test;

/**
 * Testcase for the function FREQUENCY(data, bins)
 *
 * @author Yegor Kozlov
 */
class TestFrequency {

    @Test
    void testHistogram() {
        assertArrayEquals(new int[]{3, 2, 2, 0, 1, 1},
                histogram(
                        new double[]{11, 12, 13, 21, 29, 36, 40, 58, 69},
                        new double[]{20, 30, 40, 50, 60})
        );

        assertArrayEquals(new int[]{1, 1, 1, 1, 1, 0},
                histogram(
                        new double[]{20, 30, 40, 50, 60},
                        new double[]{20, 30, 40, 50, 60})

        );

        assertArrayEquals(new int[]{2, 3},
                histogram(
                        new double[]{20, 30, 40, 50, 60},
                        new double[]{30})

        );
    }

    @Test
    void testEvaluate() {
        Workbook wb = new HSSFWorkbook();
        FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();

        int[] data = {1, 1, 2, 3, 4, 4, 5, 7, 8, 9, 9, 11, 3, 5, 8};
        int[] bins = {3, 6, 9};
        Sheet sheet = wb.createSheet();
        Row dataRow = sheet.createRow(0); // A1:O1
        for (int i = 0; i < data.length; i++) {
            dataRow.createCell(i).setCellValue(data[i]);
        }
        Row binsRow = sheet.createRow(1);
        for (int i = 0; i < bins.length; i++) { // A2:C2
            binsRow.createCell(i).setCellValue(bins[i]);
        }
        Row fmlaRow = sheet.createRow(2);
        CellRange<? extends Cell> arrayFmla = sheet.setArrayFormula("FREQUENCY(A1:O1,A2:C2)", CellRangeAddress.valueOf("A3:A6"));
        Cell b3 = fmlaRow.createCell(1); // B3
        b3.setCellFormula("COUNT(FREQUENCY(A1:O1,A2:C2))"); // frequency returns a vertical array of bins+1

        Cell c3 = fmlaRow.createCell(2);
        c3.setCellFormula("SUM(FREQUENCY(A1:O1,A2:C2))"); // sum of the frequency bins should add up to the number of data values

        assertEquals(5, (int) evaluator.evaluate(arrayFmla.getFlattenedCells()[0]).getNumberValue());
        assertEquals(4, (int) evaluator.evaluate(arrayFmla.getFlattenedCells()[1]).getNumberValue());
        assertEquals(5, (int) evaluator.evaluate(arrayFmla.getFlattenedCells()[2]).getNumberValue());
        assertEquals(1, (int) evaluator.evaluate(arrayFmla.getFlattenedCells()[3]).getNumberValue());

        assertEquals(4, (int) evaluator.evaluate(b3).getNumberValue());
        assertEquals(15, (int) evaluator.evaluate(c3).getNumberValue());

    }
}
