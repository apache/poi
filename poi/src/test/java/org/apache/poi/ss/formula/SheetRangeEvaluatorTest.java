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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.poi.hssf.usermodel.HSSFEvaluationWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.formula.eval.BlankEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.usermodel.Sheet;
import org.junit.jupiter.api.Test;

class SheetRangeEvaluatorTest {
    @Test
    public void testConstruct() {
        SheetRangeEvaluator eval = new SheetRangeEvaluator(0, 1, new SheetRefEvaluator[0]);
        assertEquals(0, eval.getFirstSheetIndex());
        assertEquals(1, eval.getLastSheetIndex());
    }

    @Test
    public void testConstruct2() {
        SheetRangeEvaluator eval = new SheetRangeEvaluator(1, new SheetRefEvaluator(null, null, 0));
        assertEquals(1, eval.getFirstSheetIndex());
        assertEquals(1, eval.getLastSheetIndex());
        assertNotNull(eval.getSheetEvaluator(1));
    }

    @Test
    public void testGetSheetName() {
        HSSFWorkbook wb = new HSSFWorkbook();
        wb.createSheet("abc");
        wb.createSheet("def");
        WorkbookEvaluator book = new WorkbookEvaluator(HSSFEvaluationWorkbook.create(wb),
                IStabilityClassifier.TOTALLY_IMMUTABLE, null);
        SheetRangeEvaluator eval = new SheetRangeEvaluator(1, new SheetRefEvaluator(book, null, 1));
        assertEquals("def", eval.getSheetName(1));
        assertEquals("def", eval.getSheetNameRange());
    }

    @Test
    public void testGetSheetNameRange() {
        SheetRangeEvaluator eval = createWithTwoSheets();
        assertEquals("abc", eval.getSheetName(0));
        assertEquals("def", eval.getSheetName(1));

        assertEquals("abc:def", eval.getSheetNameRange());
    }

    private SheetRangeEvaluator createWithTwoSheets() {
        HSSFWorkbook wb = new HSSFWorkbook();
        wb.createSheet("abc");
        wb.createSheet("def");
        WorkbookEvaluator book = new WorkbookEvaluator(HSSFEvaluationWorkbook.create(wb),
                IStabilityClassifier.TOTALLY_IMMUTABLE, null);
        return new SheetRangeEvaluator(0, 1,
                new SheetRefEvaluator[] {
                        new SheetRefEvaluator(book, null, 0),
                        new SheetRefEvaluator(book, null, 1)
                });
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void testInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> new SheetRangeEvaluator(-1, 0, null));
        assertThrows(IllegalArgumentException.class, () -> new SheetRangeEvaluator(1, 0, null));

        SheetRangeEvaluator eval = new SheetRangeEvaluator(1, new SheetRefEvaluator(null, null, 0));
        assertThrows(IllegalArgumentException.class, () -> eval.getSheetEvaluator(-1));
        assertThrows(IllegalArgumentException.class, () -> eval.getSheetEvaluator(0));
        assertThrows(IllegalArgumentException.class, () -> eval.getSheetEvaluator(2));
    }

    @Test
    public void testGetEval() {
        SheetRangeEvaluator eval = createWithTwoSheets();
        ValueEval valueEval = eval.getEvalForCell(1, 0, 0);
        assertTrue(valueEval instanceof BlankEval,
                "Had: " + valueEval);
    }

    @Test
    public void testAdjustRowNumber() {
        HSSFWorkbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet("abc");
        sheet.createRow(0);
        sheet.createRow(10);
        WorkbookEvaluator book = new WorkbookEvaluator(HSSFEvaluationWorkbook.create(wb),
                IStabilityClassifier.TOTALLY_IMMUTABLE, null);
        SheetRangeEvaluator eval = new SheetRangeEvaluator(0, new SheetRefEvaluator(book, null, 0));

        assertEquals(0, eval.adjustRowNumber(0),
                "Should stay if input is lower than max row");
        assertEquals(1, eval.adjustRowNumber(1),
                "Should stay if input is lower than max row");
        assertEquals(8, eval.adjustRowNumber(8),
                "Should stay if input is lower than max row");
        assertEquals(9, eval.adjustRowNumber(9),
                "Should stay if input is lower than max row");
        assertEquals(10, eval.adjustRowNumber(10),
                "Should stay if input is equal than max row");
        assertEquals(11, eval.adjustRowNumber(11),
                "Should be adjusted to 9 as this is the max row-index");
        assertEquals(10, eval.adjustRowNumber(Integer.MAX_VALUE),
                "Should be adjusted to 9 as this is the max row-index");
        assertEquals(10, eval.adjustRowNumber(SpreadsheetVersion.EXCEL97.getLastRowIndex()),
                "Should be adjusted to 9 as this is the max row-index");
    }

    @Test
    public void testAdjustRowNumberEmpty() {
        SheetRangeEvaluator eval = createWithTwoSheets();
        assertEquals(0, eval.adjustRowNumber(0),
                "Should stay at zero as there is no row in workbook");
        assertEquals(1, eval.adjustRowNumber(1),
                "Should be adjusted to zero as there is no row in workbook");
        assertEquals(0, eval.adjustRowNumber(SpreadsheetVersion.EXCEL97.getLastRowIndex()),
                "Should be adjusted to 9 as this is the max row-index");
        assertEquals(0, eval.adjustRowNumber(Integer.MAX_VALUE),
                "Should be adjusted to zero as there is no row in workbook");
    }
}