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

/**
 * Evaluator for returning cells or sheets for a range of sheets
 */
final class SheetRangeEvaluator implements SheetRange {
	private final int _firstSheetIndex;
    private final int _lastSheetIndex;
	private SheetRefEvaluator[] _sheetEvaluators;

	public SheetRangeEvaluator(int firstSheetIndex, int lastSheetIndex, SheetRefEvaluator[] sheetEvaluators) {
		if (firstSheetIndex < 0) {
			throw new IllegalArgumentException("Invalid firstSheetIndex: " + firstSheetIndex + ".");
		}
        if (lastSheetIndex < firstSheetIndex) {
            throw new IllegalArgumentException("Invalid lastSheetIndex: " + lastSheetIndex + " for firstSheetIndex: " + firstSheetIndex + ".");
        }
        _firstSheetIndex = firstSheetIndex;
        _lastSheetIndex = lastSheetIndex;
        _sheetEvaluators = sheetEvaluators.clone();
	}
    public SheetRangeEvaluator(int onlySheetIndex, SheetRefEvaluator sheetEvaluator) {
        this(onlySheetIndex, onlySheetIndex, new SheetRefEvaluator[] {sheetEvaluator});
    }
	
	public SheetRefEvaluator getSheetEvaluator(int sheetIndex) {
	    if (sheetIndex < _firstSheetIndex || sheetIndex > _lastSheetIndex) {
            throw new IllegalArgumentException("Invalid SheetIndex: " + sheetIndex +
                    " - Outside range " + _firstSheetIndex + " : " + _lastSheetIndex);
	    }
	    return _sheetEvaluators[sheetIndex-_firstSheetIndex];
	}
	
	public int getFirstSheetIndex() {
	    return _firstSheetIndex;
	}
    public int getLastSheetIndex() {
        return _lastSheetIndex;
    }

	public String getSheetName(int sheetIndex) {
	    return getSheetEvaluator(sheetIndex).getSheetName();
	}
	public String getSheetNameRange() {
	    StringBuilder sb = new StringBuilder();
	    sb.append(getSheetName(_firstSheetIndex));
	    if (_firstSheetIndex != _lastSheetIndex) {
	        sb.append(':');
	        sb.append(getSheetName(_lastSheetIndex));
	    }
	    return sb.toString();
	}

	public ValueEval getEvalForCell(int sheetIndex, int rowIndex, int columnIndex) {
        return getSheetEvaluator(sheetIndex).getEvalForCell(rowIndex, columnIndex);
	}
}
