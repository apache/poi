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
import org.apache.poi.ss.formula.eval.BlankEval;
import org.apache.poi.ss.formula.eval.RefEvalBase;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.util.Internal;

/**
 * A {@link RefEvalBase} over a {@link VirtualEvaluationSheet}. The referenced
 * value is read directly from the sheet's backing array, bypassing the
 * {@link SheetRangeEvaluator}/{@link LazyRefEval}/{@code evaluateReference}
 * machinery used by the classic workbook-based evaluator. This is only valid
 * while the referenced sheet is a single-row {@link VirtualEvaluationSheet}.
 */
@Internal
final class VirtualRefEval extends RefEvalBase {

    private final VirtualEvaluationSheet _sheet;
    private final int _columnIndex;

    VirtualRefEval(VirtualEvaluationSheet sheet, int columnIndex) {
        super(0, 0, columnIndex);
        _sheet = sheet;
        _columnIndex = columnIndex;
    }

    @Override
    public ValueEval getInnerValueEval(int sheetIndex) {
        ValueEval value = _sheet.valueAt(_columnIndex);
        return value == null ? BlankEval.instance : value;
    }

    @Override
    public AreaEval offset(int relFirstRowIx, int relLastRowIx, int relFirstColIx, int relLastColIx) {
        int firstCol = _columnIndex + relFirstColIx;
        int lastCol = _columnIndex + relLastColIx;
        return new VirtualAreaEval(_sheet, relFirstRowIx, firstCol, relLastRowIx, lastCol);
    }
}