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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFEvaluationWorkbook;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.eval.AreaEval;
import org.apache.poi.ss.formula.eval.AreaEvalBase;
import org.apache.poi.ss.formula.eval.BlankEval;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.RefEvalBase;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.junit.jupiter.api.Test;

/**
 * Checks that {@link OperationEvaluationContext} can be subclassed to resolve references from
 * somewhere other than the sheet, and that {@link WorkbookEvaluator#evaluateFormula} honours
 * the subclass - the shape used by an evaluator that reads inputs from a backing array
 * instead of a real sheet.
 */
class TestOperationEvaluationContextSubclass {

    /**
     * Resolves references to row 0 from a backing array, the way a virtual single-row
     * evaluation sheet would. Everything else is left to the default implementation.
     */
    private static final class VirtualRowContext extends OperationEvaluationContext {
        private final ValueEval[] _row;

        VirtualRowContext(WorkbookEvaluator evaluator, EvaluationWorkbook workbook, ValueEval[] row) {
            super(evaluator, workbook, 0, 0, row.length, evaluator.createEvaluationTracker());
            _row = row;
        }

        private ValueEval valueAt(int rowIndex, int columnIndex) {
            if (rowIndex != 0 || columnIndex >= _row.length || _row[columnIndex] == null) {
                return BlankEval.instance;
            }
            return _row[columnIndex];
        }

        @Override
        public ValueEval getRefEval(int rowIndex, int columnIndex) {
            if (rowIndex != 0 || columnIndex >= _row.length) {
                return super.getRefEval(rowIndex, columnIndex);
            }
            return new VirtualRefEval(rowIndex, columnIndex);
        }

        @Override
        public ValueEval getAreaEval(int firstRowIndex, int firstColumnIndex, int lastRowIndex, int lastColumnIndex) {
            if (firstRowIndex != 0 || lastRowIndex != 0 || lastColumnIndex >= _row.length) {
                return super.getAreaEval(firstRowIndex, firstColumnIndex, lastRowIndex, lastColumnIndex);
            }
            return new VirtualAreaEval(firstRowIndex, firstColumnIndex, lastRowIndex, lastColumnIndex);
        }

        private final class VirtualRefEval extends RefEvalBase {
            VirtualRefEval(int rowIndex, int columnIndex) {
                super(getSheetIndex(), rowIndex, columnIndex);
            }

            @Override
            public ValueEval getInnerValueEval(int sheetIndex) {
                return valueAt(getRow(), getColumn());
            }

            @Override
            public AreaEval offset(int relFirstRowIx, int relLastRowIx, int relFirstColIx, int relLastColIx) {
                return new VirtualAreaEval(getRow() + relFirstRowIx, getColumn() + relFirstColIx,
                        getRow() + relLastRowIx, getColumn() + relLastColIx);
            }
        }

        private final class VirtualAreaEval extends AreaEvalBase {
            VirtualAreaEval(int firstRow, int firstColumn, int lastRow, int lastColumn) {
                super(firstRow, firstColumn, lastRow, lastColumn);
            }

            @Override
            public ValueEval getRelativeValue(int relativeRowIndex, int relativeColumnIndex) {
                return valueAt(getFirstRow() + relativeRowIndex, getFirstColumn() + relativeColumnIndex);
            }

            @Override
            public ValueEval getRelativeValue(int sheetIndex, int relativeRowIndex, int relativeColumnIndex) {
                return getRelativeValue(relativeRowIndex, relativeColumnIndex);
            }

            @Override
            public TwoDEval getRow(int rowIndex) {
                int absRow = getFirstRow() + rowIndex;
                return new VirtualAreaEval(absRow, getFirstColumn(), absRow, getLastColumn());
            }

            @Override
            public TwoDEval getColumn(int columnIndex) {
                int absCol = getFirstColumn() + columnIndex;
                return new VirtualAreaEval(getFirstRow(), absCol, getLastRow(), absCol);
            }

            @Override
            public AreaEval offset(int relFirstRowIx, int relLastRowIx, int relFirstColIx, int relLastColIx) {
                return new VirtualAreaEval(getFirstRow() + relFirstRowIx, getFirstColumn() + relFirstColIx,
                        getLastRow() + relLastRowIx, getLastColumn() + relLastColIx);
            }
        }
    }

    private static double evaluate(WorkbookEvaluator we, OperationEvaluationContext ec, Ptg[] ptgs) {
        ValueEval v = we.evaluateFormula(ec, ptgs);
        return assertInstanceOf(NumberEval.class, v).getNumberValue();
    }

    @Test
    void subclassResolvesReferences() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet sheet = wb.createSheet();
            HSSFRow row0 = sheet.createRow(0);
            row0.createCell(0).setCellValue(1);
            row0.createCell(1).setCellValue(2);
            sheet.createRow(4).createCell(0).setCellValue(7);

            HSSFEvaluationWorkbook ewb = HSSFEvaluationWorkbook.create(wb);
            WorkbookEvaluator we = new WorkbookEvaluator(ewb, null, null);
            Ptg[] sum = FormulaParser.parse("A1+B1", ewb, FormulaType.CELL, 0, 0);
            Ptg[] area = FormulaParser.parse("SUM(A1:B1)", ewb, FormulaType.CELL, 0, 0);
            Ptg[] otherRow = FormulaParser.parse("A5", ewb, FormulaType.CELL, 0, 0);

            // default context: values come from the sheet
            OperationEvaluationContext plain = new OperationEvaluationContext(we, ewb, 0, 0, 2);
            assertEquals(3, evaluate(we, plain, sum), 0);
            assertEquals(3, evaluate(we, plain, area), 0);
            assertEquals(7, evaluate(we, plain, otherRow), 0);

            // subclass: row 0 references come from the backing array,
            // everything else still comes from the sheet
            ValueEval[] virtualRow = { new NumberEval(10), new NumberEval(20) };
            OperationEvaluationContext virtual = new VirtualRowContext(we, ewb, virtualRow);
            assertEquals(30, evaluate(we, virtual, sum), 0);
            assertEquals(30, evaluate(we, virtual, area), 0);
            assertEquals(7, evaluate(we, virtual, otherRow), 0);

            // the backing array can be updated in place and the same context re-used
            virtualRow[1] = new NumberEval(5);
            assertEquals(15, evaluate(we, virtual, sum), 0);
            assertEquals(15, evaluate(we, virtual, area), 0);
        }
    }
}
