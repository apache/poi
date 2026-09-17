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

import org.apache.poi.ss.formula.eval.BoolEval;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.NumericValueEval;
import org.apache.poi.ss.formula.eval.StringEval;
import org.apache.poi.ss.formula.eval.StringValueEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.Internal;

/**
 * An {@link EvaluationCell} view of one column of the current row bound on a
 * {@link VirtualEvaluationSheet}. Instances are shared across rows: the returned
 * value always reflects the value currently bound for their column.
 */
@Internal
final class VirtualEvaluationCell implements EvaluationCell {

    private final VirtualEvaluationSheet _sheet;
    private final int _columnIndex;

    VirtualEvaluationCell(VirtualEvaluationSheet sheet, int columnIndex) {
        _sheet = sheet;
        _columnIndex = columnIndex;
    }

    @Override
    public Object getIdentityKey() {
        return this;
    }

    @Override
    public EvaluationSheet getSheet() {
        return _sheet;
    }

    @Override
    public int getRowIndex() {
        return 0;
    }

    @Override
    public int getColumnIndex() {
        return _columnIndex;
    }

    @Override
    public CellType getCellType() {
        return typeOf(_sheet.valueAt(_columnIndex));
    }

    @Override
    public double getNumericCellValue() {
        ValueEval value = _sheet.valueAt(_columnIndex);
        if (value instanceof NumericValueEval numericValueEval) {
            return numericValueEval.getNumberValue();
        }
        throw new IllegalStateException("Cell " + _columnIndex + " does not hold a numeric value");
    }

    @Override
    public String getStringCellValue() {
        ValueEval value = _sheet.valueAt(_columnIndex);
        if (value instanceof StringValueEval stringValueEval) {
            return stringValueEval.getStringValue();
        }
        throw new IllegalStateException("Cell " + _columnIndex + " does not hold a string value");
    }

    @Override
    public boolean getBooleanCellValue() {
        ValueEval value = _sheet.valueAt(_columnIndex);
        if (value instanceof BoolEval boolEval) {
            return boolEval.getBooleanValue();
        }
        throw new IllegalStateException("Cell " + _columnIndex + " does not hold a boolean value");
    }

    @Override
    public int getErrorCellValue() {
        ValueEval value = _sheet.valueAt(_columnIndex);
        if (value instanceof ErrorEval errorEval) {
            return errorEval.getErrorCode();
        }
        throw new IllegalStateException("Cell " + _columnIndex + " does not hold an error value");
    }

    @Override
    public CellRangeAddress getArrayFormulaRange() {
        return null;
    }

    @Override
    public boolean isPartOfArrayFormulaGroup() {
        return false;
    }

    @Override
    public CellType getCachedFormulaResultType() {
        return getCellType();
    }

    static CellType typeOf(ValueEval value) {
        if (value == null) {
            return CellType.BLANK;
        }
        if (value instanceof NumberEval) {
            return CellType.NUMERIC;
        }
        if (value instanceof StringEval) {
            return CellType.STRING;
        }
        if (value instanceof BoolEval) {
            return CellType.BOOLEAN;
        }
        if (value instanceof ErrorEval) {
            return CellType.ERROR;
        }
        throw new IllegalStateException("Unexpected value eval " + value.getClass().getName());
    }
}
