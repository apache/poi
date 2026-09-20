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

import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.util.Internal;

/**
 * The single virtual sheet backing a {@link StandaloneFormulaEvaluator}. The current
 * row is stored as a flat array of {@link ValueEval}s, one slot per declared input.
 * Cell {@code (0, i)} is the {@code i}-th input; cell {@code (0, inputCount)} is a
 * blank placeholder standing for the formula's own position (the evaluation engine
 * requires a cell to exist there); every other position is empty.
 */
@Internal
final class VirtualEvaluationSheet implements EvaluationSheet {

    private final ValueEval[] _values;
    private final VirtualEvaluationCell[] _cells;
    private final VirtualEvaluationCell _formulaCell;

    VirtualEvaluationSheet(int inputCount) {
        _values = new ValueEval[inputCount];
        _cells = new VirtualEvaluationCell[inputCount];
        for (int i = 0; i < inputCount; i++) {
            _cells[i] = new VirtualEvaluationCell(this, i);
        }
        _formulaCell = new VirtualEvaluationCell(this, inputCount);
    }

    ValueEval[] values() {
        return _values;
    }

    ValueEval valueAt(int columnIndex) {
        if (columnIndex < 0 || columnIndex >= _values.length) {
            return null;
        }
        return _values[columnIndex];
    }

    @Override
    public EvaluationCell getCell(int rowIndex, int columnIndex) {
        if (rowIndex != 0) {
            return null;
        }
        if (columnIndex >= 0 && columnIndex < _cells.length) {
            return _cells[columnIndex];
        }
        if (columnIndex == _values.length) {
            return _formulaCell;
        }
        return null;
    }

    @Override
    public void clearAllCachedResultValues() {
        // values are read directly from the bound row, nothing is cached
    }

    @Override
    public int getLastRowNum() {
        return 0;
    }

    @Override
    public boolean isRowHidden(int rowIndex) {
        return false;
    }
}
