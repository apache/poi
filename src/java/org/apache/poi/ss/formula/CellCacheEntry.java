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

import java.util.HashSet;
import java.util.Set;

import org.apache.poi.hssf.record.formula.eval.BoolEval;
import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.NumberEval;
import org.apache.poi.hssf.record.formula.eval.StringEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;

/**
 * Stores the parameters that identify the evaluation of one cell.<br/>
 */
final class CellCacheEntry {
	private ValueEval _value;
	private final Set _consumingCells;
	private CellLocation[] _usedCells;
	private boolean _isPlainValueCell;
	
	public CellCacheEntry() {
		_consumingCells = new HashSet();
	}
	public boolean updatePlainValue(ValueEval value) {
		boolean wasChanged = false;
		if (!_isPlainValueCell) {
			wasChanged = true;
		}
		if (!areValuesEqual(_value, value)) {
			wasChanged = true;
		}
		_isPlainValueCell = true;
		_value = value;
		_usedCells = null;
		return wasChanged;
	}
	private boolean areValuesEqual(ValueEval a, ValueEval b) {
		if (a == null) {
			return false;
		}
		Class cls = a.getClass();
		if (cls != b.getClass()) {
			// value type is changing
			return false;
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
	public void setFormulaResult(ValueEval value, CellLocation[] usedCells) {
		_isPlainValueCell = false;
		_value = value;
		_usedCells = usedCells;
	}
	public void addConsumingCell(CellLocation cellLoc) {
		_consumingCells.add(cellLoc);
		
	}
	public CellLocation[] getConsumingCells() {
		int nItems = _consumingCells.size();
		if (nItems < 1) {
			return CellLocation.EMPTY_ARRAY;
		}
		CellLocation[] result = new CellLocation[nItems];
		_consumingCells.toArray(result);
		return result;
	}
	public CellLocation[] getUsedCells() {
		return _usedCells;
	}
	public void clearConsumingCell(CellLocation fc) {
		if(!_consumingCells.remove(fc)) {
			throw new IllegalStateException("Cell '" + fc.formatAsString() + "' does not use this cell");
		}
		
	}
	public ValueEval getValue() {
		ValueEval result = _value;
		if (result == null) {
			if (_isPlainValueCell) {
				throw new IllegalStateException("Plain value cell should always have a value");
				
			}
		}
		return result;
	}
}