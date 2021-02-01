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

import org.apache.poi.ss.formula.eval.BlankEval;
import org.apache.poi.ss.formula.eval.BoolEval;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.StringEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.formula.IEvaluationListener.ICacheEntry;

/**
 * Stores the parameters that identify the evaluation of one cell.<br>
 */
abstract class CellCacheEntry implements ICacheEntry {
	public static final CellCacheEntry[] EMPTY_ARRAY = { };

	private final FormulaCellCacheEntrySet _consumingCells;
	private ValueEval _value;


	protected CellCacheEntry() {
		_consumingCells = new FormulaCellCacheEntrySet();
	}
	protected final void clearValue() {
		_value = null;
	}

	public final boolean updateValue(ValueEval value) {
		if (value == null) {
			throw new IllegalArgumentException("Did not expect to update to null");
		}
		boolean result = !areValuesEqual(_value, value);
		_value = value;
		return result;
	}
	public final ValueEval getValue() {
		return _value;
	}

	private static boolean areValuesEqual(ValueEval a, ValueEval b) {
		if (a == null) {
			return false;
		}
		Class<? extends ValueEval> cls = a.getClass();
		if (cls != b.getClass()) {
			// value type is changing
			return false;
		}
		if (a == BlankEval.instance) {
			return b == a;
		}
		if (cls == NumberEval.class) {
			return ((NumberEval)a).getNumberValue() == ((NumberEval)b).getNumberValue();
		}
		if (cls == StringEval.class) {
			return ((StringEval)a).getStringValue().equals(((StringEval)b).getStringValue());
		}
		if (cls == BoolEval.class) {
			return ((BoolEval)a).getBooleanValue() == ((BoolEval)b).getBooleanValue();
		}
		if (cls == ErrorEval.class) {
			return ((ErrorEval)a).getErrorCode() == ((ErrorEval)b).getErrorCode();
		}
		throw new IllegalStateException("Unexpected value class (" + cls.getName() + ")");
	}

	public final void addConsumingCell(FormulaCellCacheEntry cellLoc) {
		_consumingCells.add(cellLoc);

	}
	public final FormulaCellCacheEntry[] getConsumingCells() {
		return _consumingCells.toArray();
	}

	public final void clearConsumingCell(FormulaCellCacheEntry cce) {
		if(!_consumingCells.remove(cce)) {
			throw new IllegalStateException("Specified formula cell is not consumed by this cell");
		}
	}
	public final void recurseClearCachedFormulaResults(IEvaluationListener listener) {
		if (listener == null) {
			recurseClearCachedFormulaResults();
		} else {
			listener.onClearCachedValue(this);
			recurseClearCachedFormulaResults(listener, 1);
		}
	}

	/**
	 * Calls formulaCell.setFormulaResult(null, null) recursively all the way up the tree of
	 * dependencies. Calls usedCell.clearConsumingCell(fc) for each child of a cell that is
	 * cleared along the way.
	 * @param formulaCells
	 */
	protected final void recurseClearCachedFormulaResults() {
		FormulaCellCacheEntry[] formulaCells = getConsumingCells();

		for (int i = 0; i < formulaCells.length; i++) {
			FormulaCellCacheEntry fc = formulaCells[i];
			fc.clearFormulaEntry();
			fc.recurseClearCachedFormulaResults();
		}
	}

	/**
	 * Identical to {@link #recurseClearCachedFormulaResults()} except for the listener call-backs
	 */
	protected final void recurseClearCachedFormulaResults(IEvaluationListener listener, int depth) {
		FormulaCellCacheEntry[] formulaCells = getConsumingCells();

		listener.sortDependentCachedValues(formulaCells);
		for (int i = 0; i < formulaCells.length; i++) {
			FormulaCellCacheEntry fc = formulaCells[i];
			listener.onClearDependentCachedValue(fc, depth);
			fc.clearFormulaEntry();
			fc.recurseClearCachedFormulaResults(listener, depth+1);
		}
	}
}
