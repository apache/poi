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
import org.apache.poi.ss.formula.ptg.FuncVarPtg;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.usermodel.CellType;

/**
 * Evaluator for cells within a specific Sheet
 */
final class SheetRefEvaluator {
	private final WorkbookEvaluator _bookEvaluator;
	private final EvaluationTracker _tracker;
	private final int _sheetIndex;
	private EvaluationSheet _sheet;

	public SheetRefEvaluator(WorkbookEvaluator bookEvaluator, EvaluationTracker tracker, int sheetIndex) {
		if (sheetIndex < 0) {
			throw new IllegalArgumentException("Invalid sheetIndex: " + sheetIndex + ".");
		}
		_bookEvaluator = bookEvaluator;
		_tracker = tracker;
		_sheetIndex = sheetIndex;
	}

	public String getSheetName() {
		return _bookEvaluator.getSheetName(_sheetIndex);
	}

	public ValueEval getEvalForCell(int rowIndex, int columnIndex) {
		return _bookEvaluator.evaluateReference(getSheet(), _sheetIndex, rowIndex, columnIndex, _tracker);
	}

	private EvaluationSheet getSheet() {
		if (_sheet == null) {
			_sheet = _bookEvaluator.getSheet(_sheetIndex);
		}
		return _sheet;
	}

    /**
     * @param rowIndex 
     * @param columnIndex 
     * @return  whether cell at rowIndex and columnIndex is a subtotal
     * @see org.apache.poi.ss.formula.functions.Subtotal
     */
    public boolean isSubTotal(int rowIndex, int columnIndex){
        boolean subtotal = false;
        EvaluationCell cell = getSheet().getCell(rowIndex, columnIndex);
        if(cell != null && cell.getCellType() == CellType.FORMULA){
            EvaluationWorkbook wb = _bookEvaluator.getWorkbook();
            for(Ptg ptg : wb.getFormulaTokens(cell)){
                if(ptg instanceof FuncVarPtg){
                    FuncVarPtg f = (FuncVarPtg)ptg;
                    if("SUBTOTAL".equals(f.getName())) {
                        subtotal = true;
                        break;
                    }
                }
            }
        }
        return subtotal;
    }

    /**
     * Used by functions that calculate differently depending on row visibility, like some
     * variations of SUBTOTAL()
     * @see org.apache.poi.ss.formula.functions.Subtotal
     * @param rowIndex
     * @return true if the row is hidden
     */
    public boolean isRowHidden(int rowIndex) {
        return getSheet().isRowHidden(rowIndex);
    }
}
