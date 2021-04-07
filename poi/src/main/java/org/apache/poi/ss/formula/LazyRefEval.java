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
import org.apache.poi.ss.formula.eval.RefEvalBase;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.formula.ptg.AreaI;
import org.apache.poi.ss.formula.ptg.AreaI.OffsetArea;
import org.apache.poi.ss.util.CellReference;

/**
 * Provides Lazy Evaluation to a 3D Reference
 */
public final class LazyRefEval extends RefEvalBase {
	private final SheetRangeEvaluator _evaluator;

	public LazyRefEval(int rowIndex, int columnIndex, SheetRangeEvaluator sre) {
		super(sre, rowIndex, columnIndex);
		_evaluator = sre;
	}

	public ValueEval getInnerValueEval(int sheetIndex) {
		return _evaluator.getEvalForCell(sheetIndex, getRow(), getColumn());
	}

	public AreaEval offset(int relFirstRowIx, int relLastRowIx, int relFirstColIx, int relLastColIx) {

		AreaI area = new OffsetArea(getRow(), getColumn(),
				relFirstRowIx, relLastRowIx, relFirstColIx, relLastColIx);

		return new LazyAreaEval(area, _evaluator);
	}

	/**
	 * @return true if the cell is a subtotal
	 */
	public boolean isSubTotal() {
		SheetRefEvaluator sheetEvaluator = _evaluator.getSheetEvaluator(getFirstSheetIndex());
		return sheetEvaluator.isSubTotal(getRow(), getColumn());
	}
    
    /**
     * @return whether the row at rowIndex is hidden
     */
    public boolean isRowHidden() {
        // delegate the query to the sheet evaluator which has access to internal ptgs
        SheetRefEvaluator _sre = _evaluator.getSheetEvaluator(_evaluator.getFirstSheetIndex());
        return _sre.isRowHidden(getRow());
    }

	public String toString() {
		CellReference cr = new CellReference(getRow(), getColumn());
		return getClass().getName() + "[" +
				_evaluator.getSheetNameRange() +
				'!' +
				cr.formatAsString() +
				"]";
	}
}
