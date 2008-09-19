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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.record.formula.eval.ValueEval;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;

/**
 * Performance optimisation for {@link HSSFFormulaEvaluator}. This class stores previously
 * calculated values of already visited cells, to avoid unnecessary re-calculation when the 
 * same cells are referenced multiple times
 * 
 * 
 * @author Josh Micich
 */
final class EvaluationCache {

	private static final CellEvaluationFrame[] EMPTY_CEF_ARRAY = { };
	private final Map _valuesByKey;
	private final Map _consumingCellsByDest;

	/* package */EvaluationCache() {
		_valuesByKey = new HashMap();
		_consumingCellsByDest = new HashMap();
	}

	public ValueEval getValue(int sheetIndex, int srcRowNum, int srcColNum) {
		return getValue(new CellEvaluationFrame(sheetIndex, srcRowNum, srcColNum));
	}

	/* package */ ValueEval getValue(CellEvaluationFrame key) {
		return (ValueEval) _valuesByKey.get(key);
	}

	public void setValue(int sheetIndex, int srcRowNum, int srcColNum, ValueEval value) {
		setValue(new CellEvaluationFrame(sheetIndex, srcRowNum, srcColNum), value);
	}

	/* package */ void setValue(CellEvaluationFrame key, ValueEval value) {
		if (_valuesByKey.containsKey(key)) {
			throw new RuntimeException("Already have cached value for this cell");
		}
		_valuesByKey.put(key, value);
	}

	/**
	 * Should be called whenever there are changes to input cells in the evaluated workbook.
	 */
	public void clear() {
		_valuesByKey.clear();
	}

	public void clearValue(int sheetIndex, int rowIndex, int columnIndex) {
		clearValuesRecursive(new CellEvaluationFrame(sheetIndex, rowIndex, columnIndex));
		
	}

	private void clearValuesRecursive(CellEvaluationFrame cef) {
		CellEvaluationFrame[] consumingCells = getConsumingCells(cef);
		for (int i = 0; i < consumingCells.length; i++) {
			clearValuesRecursive(consumingCells[i]);
		}
		_valuesByKey.remove(cef);
		_consumingCellsByDest.remove(cef);
	}

	private CellEvaluationFrame[] getConsumingCells(CellEvaluationFrame cef) {
		List temp = (List) _consumingCellsByDest.get(cef);
		if (temp == null) {
			return EMPTY_CEF_ARRAY;
		}
		int nItems = temp.size();
		if (temp.size() < 1) {
			return EMPTY_CEF_ARRAY;
		}
		CellEvaluationFrame[] result = new CellEvaluationFrame[nItems];
		temp.toArray(result);
		return result;
	}
}
