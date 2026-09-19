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

import org.apache.poi.ss.formula.eval.AreaEval;
import org.apache.poi.ss.formula.eval.AreaEvalBase;
import org.apache.poi.ss.formula.eval.BlankEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.util.Internal;

/**
 * An {@link AreaEval} over a {@link VirtualEvaluationSheet}. Values are read
 * directly from the sheet's backing array, bypassing the
 * {@link SheetRangeEvaluator}/{@link LazyAreaEval} machinery used by the classic
 * workbook-based evaluator. This is only valid while the referenced sheet is a
 * single-row {@link VirtualEvaluationSheet}.
 */
@Internal
final class VirtualAreaEval extends AreaEvalBase {

    private final VirtualEvaluationSheet _sheet;

    VirtualAreaEval(VirtualEvaluationSheet sheet, int firstRow, int firstColumn,
                    int lastRow, int lastColumn) {
        super(firstRow, firstColumn, lastRow, lastColumn);
        _sheet = sheet;
    }

    @Override
    public ValueEval getRelativeValue(int relativeRowIndex, int relativeColumnIndex) {
        if (relativeRowIndex + getFirstRow() != 0) {
            return BlankEval.instance;
        }
        ValueEval value = _sheet.valueAt(relativeColumnIndex + getFirstColumn());
        return value == null ? BlankEval.instance : value;
    }

    @Override
    public ValueEval getRelativeValue(int sheetIndex, int relativeRowIndex, int relativeColumnIndex) {
        if (relativeRowIndex + getFirstRow() != 0) {
            return BlankEval.instance;
        }
        ValueEval value = _sheet.valueAt(relativeColumnIndex + getFirstColumn());
        return value == null ? BlankEval.instance : value;
    }

    @Override
    public VirtualAreaEval getRow(int rowIndex) {
        if (rowIndex >= getHeight()) {
            throw new IllegalArgumentException("Invalid rowIndex " + rowIndex
                    + ".  Allowable range is (0.." + getHeight() + ").");
        }
        int absRowIx = getFirstRow() + rowIndex;
        return new VirtualAreaEval(_sheet, absRowIx, getFirstColumn(), absRowIx, getLastColumn());
    }

    @Override
    public VirtualAreaEval getColumn(int columnIndex) {
        if (columnIndex >= getWidth()) {
            throw new IllegalArgumentException("Invalid columnIndex " + columnIndex
                    + ".  Allowable range is (0.." + getWidth() + ").");
        }
        int absColIx = getFirstColumn() + columnIndex;
        return new VirtualAreaEval(_sheet, getFirstRow(), absColIx, getLastRow(), absColIx);
    }

    @Override
    public AreaEval offset(int relFirstRowIx, int relLastRowIx, int relFirstColIx, int relLastColIx) {
        int firstRow = getFirstRow() + relFirstRowIx;
        int lastRow = getLastRow() + relLastRowIx;
        int firstCol = getFirstColumn() + relFirstColIx;
        int lastCol = getLastColumn() + relLastColIx;
        return new VirtualAreaEval(_sheet, firstRow, firstCol, lastRow, lastCol);
    }
}