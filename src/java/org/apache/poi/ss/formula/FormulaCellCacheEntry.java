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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.poi.hssf.record.formula.eval.ValueEval;
import org.apache.poi.ss.formula.FormulaUsedBlankCellSet.BookSheetKey;


/**
 * Stores the cached result of a formula evaluation, along with the set of sensitive input cells
 * 
 * @author Josh Micich
 */
final class FormulaCellCacheEntry extends CellCacheEntry {
	
	/**
	 * Cells 'used' in the current evaluation of the formula corresponding to this cache entry
	 *
	 * If any of the following cells change, this cache entry needs to be cleared
	 */
	private CellCacheEntry[] _sensitiveInputCells;

	private FormulaUsedBlankCellSet _usedBlankCellGroup;

	public FormulaCellCacheEntry() {
		// leave fields un-set
	}
	
	public boolean isInputSensitive() {
		if (_sensitiveInputCells != null) {
			if (_sensitiveInputCells.length > 0 ) {
				return true;
			}
		}
		return _usedBlankCellGroup == null ? false : !_usedBlankCellGroup.isEmpty();
	}

	public void setSensitiveInputCells(CellCacheEntry[] sensitiveInputCells) {
		// need to tell all cells that were previously used, but no longer are, 
		// that they are not consumed by this cell any more
		changeConsumingCells(sensitiveInputCells == null ? CellCacheEntry.EMPTY_ARRAY : sensitiveInputCells);
		_sensitiveInputCells = sensitiveInputCells;
	}

	public void clearFormulaEntry() {
		CellCacheEntry[] usedCells = _sensitiveInputCells;
		if (usedCells != null) {
			for (int i = usedCells.length-1; i>=0; i--) {
				usedCells[i].clearConsumingCell(this);
			}
		}
		_sensitiveInputCells = null;
		clearValue();
	}
	
	private void changeConsumingCells(CellCacheEntry[] usedCells) {

		CellCacheEntry[] prevUsedCells = _sensitiveInputCells;
		int nUsed = usedCells.length;
		for (int i = 0; i < nUsed; i++) {
			usedCells[i].addConsumingCell(this);
		}
		if (prevUsedCells == null) {
			return;
		}
		int nPrevUsed = prevUsedCells.length;
		if (nPrevUsed < 1) {
			return;
		}
		Set<CellCacheEntry> usedSet;
		if (nUsed < 1) {
			usedSet = Collections.emptySet();
		} else {
			usedSet = new HashSet<CellCacheEntry>(nUsed * 3 / 2);
			for (int i = 0; i < nUsed; i++) {
				usedSet.add(usedCells[i]);
			}
		}
		for (int i = 0; i < nPrevUsed; i++) {
			CellCacheEntry prevUsed = prevUsedCells[i];
			if (!usedSet.contains(prevUsed)) {
				// previously was used by cellLoc, but not anymore
				prevUsed.clearConsumingCell(this);
			}
		}
	}

	public void updateFormulaResult(ValueEval result, CellCacheEntry[] sensitiveInputCells, FormulaUsedBlankCellSet usedBlankAreas) {
		updateValue(result);
		setSensitiveInputCells(sensitiveInputCells);
		_usedBlankCellGroup = usedBlankAreas;
	}

	public void notifyUpdatedBlankCell(BookSheetKey bsk, int rowIndex, int columnIndex, IEvaluationListener evaluationListener) {
		if (_usedBlankCellGroup != null) {
			if (_usedBlankCellGroup.containsCell(bsk, rowIndex, columnIndex)) {
				clearFormulaEntry();
				recurseClearCachedFormulaResults(evaluationListener);
			}
		}
	}
}