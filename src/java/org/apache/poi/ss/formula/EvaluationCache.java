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

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

	private final Map _entriesByLocation;
	private final Map _consumingCellsByUsedCells;
	/** only used for testing. <code>null</code> otherwise */
	private final IEvaluationListener _evaluationListener;

	/* package */EvaluationCache(IEvaluationListener evaluationListener) {
		_evaluationListener = evaluationListener;
		_entriesByLocation = new HashMap();
		_consumingCellsByUsedCells = new HashMap();
	}

	/**
	 * @param cellLoc never <code>null</code>
	 * @return only ever <code>null</code> for formula cells that have had their cached value cleared
	 */
	public ValueEval getValue(CellLocation cellLoc) {
		return getEntry(cellLoc).getValue();
	}

	/**
	 * @param cellLoc
	 * @param usedCells never <code>null</code>, (possibly zero length) array of all cells actually 
	 * directly used when evaluating the formula
	 * @param isPlainValue pass <code>true</code> if cellLoc refers to a plain value (non-formula) 
	 * cell, <code>false</code> for a formula cell.
	 * @param value the value of a non-formula cell or the result of evaluating the cell formula
	 * Pass <code>null</code> to signify clearing the cached result of a formula cell) 
	 */
	public void setValue(CellLocation cellLoc, boolean isPlainValue, 
			CellLocation[] usedCells, ValueEval value) {

		CellCacheEntry existingEntry = (CellCacheEntry) _entriesByLocation.get(cellLoc);
		if (existingEntry != null && existingEntry.getValue() != null) {
			if (isPlainValue) {
				// can set a plain cell cached value any time
			} else if (value == null) {
				// or clear the cached value of a formula cell at any time
			} else {
				// but a formula cached value would only be getting updated if the cache didn't have a value to start with
				throw new IllegalStateException("Already have cached value for this cell: "
						+ cellLoc.formatAsString());
			}
		}
		if (_evaluationListener == null) {
			// optimisation - don't bother sorting if there is no listener.
		} else {
			// for testing
			// make order of callbacks to listener more deterministic
			Arrays.sort(usedCells, CellLocationComparator);
		}
		CellCacheEntry entry = getEntry(cellLoc);
		CellLocation[] consumingFormulaCells = entry.getConsumingCells();
		CellLocation[] prevUsedCells = entry.getUsedCells();
		if (isPlainValue) {
			
			if(!entry.updatePlainValue(value)) {
				return;
			}
		} else {
			entry.setFormulaResult(value, usedCells);
			for (int i = 0; i < usedCells.length; i++) {
				getEntry(usedCells[i]).addConsumingCell(cellLoc);
			}
		}
		// need to tell all cells that were previously used, but no longer are, 
		// that they are not consumed by this cell any more
		unlinkConsumingCells(prevUsedCells, usedCells, cellLoc);
		
		// clear all cells that directly or indirectly depend on this one
		recurseClearCachedFormulaResults(consumingFormulaCells, 0);
	}

	private void unlinkConsumingCells(CellLocation[] prevUsedCells, CellLocation[] usedCells,
			CellLocation cellLoc) {
		if (prevUsedCells == null) {
			return;
		}
		int nPrevUsed = prevUsedCells.length;
		if (nPrevUsed < 1) {
			return;
		}
		int nUsed = usedCells.length;
		Set usedSet;
		if (nUsed < 1) {
			usedSet = Collections.EMPTY_SET;
		} else {
			usedSet = new HashSet(nUsed * 3 / 2);
			for (int i = 0; i < nUsed; i++) {
				usedSet.add(usedCells[i]);
			}
		}
		for (int i = 0; i < nPrevUsed; i++) {
			CellLocation prevUsed = prevUsedCells[i];
			if (!usedSet.contains(prevUsed)) {
				// previously was used by cellLoc, but not anymore
				getEntry(prevUsed).clearConsumingCell(cellLoc);
				if (_evaluationListener != null) {
					//TODO _evaluationListener.onUnconsume(prevUsed.getSheetIndex(), etc)
				}
			}
		}
		
	}

	/**
	 * Calls formulaCell.setFormulaResult(null, null) recursively all the way up the tree of 
	 * dependencies. Calls usedCell.clearConsumingCell(fc) for each child of a cell that is
	 * cleared along the way.
	 * @param formulaCells
	 */
	private void recurseClearCachedFormulaResults(CellLocation[] formulaCells, int depth) {
		int nextDepth = depth+1;
		for (int i = 0; i < formulaCells.length; i++) {
			CellLocation fc = formulaCells[i];
			CellCacheEntry formulaCell = getEntry(fc);
			CellLocation[] usedCells = formulaCell.getUsedCells();
			if (usedCells != null) {
				for (int j = 0; j < usedCells.length; j++) {
					CellCacheEntry usedCell = getEntry(usedCells[j]);
					usedCell.clearConsumingCell(fc);
				}
			}
			if (_evaluationListener != null) {
				ValueEval value = formulaCell.getValue();
				_evaluationListener.onClearDependentCachedValue(fc.getSheetIndex(), fc.getRowIndex(), fc.getColumnIndex(), value, nextDepth);
			}
			formulaCell.setFormulaResult(null, null);
			recurseClearCachedFormulaResults(formulaCell.getConsumingCells(), nextDepth);
		}
	}

	private CellCacheEntry getEntry(CellLocation cellLoc) {
		CellCacheEntry result = (CellCacheEntry)_entriesByLocation.get(cellLoc);
		if (result == null) {
			result = new CellCacheEntry();
			_entriesByLocation.put(cellLoc, result);
		}
		return result;
	}
	/**
	 * Should be called whenever there are changes to input cells in the evaluated workbook.
	 */
	public void clear() {
		if(_evaluationListener != null) {
			_evaluationListener.onClearWholeCache();
		}
		_entriesByLocation.clear();
		_consumingCellsByUsedCells.clear();
	}


	private static final Comparator CellLocationComparator = new Comparator() {

		public int compare(Object a, Object b) {
			CellLocation clA = (CellLocation) a;
			CellLocation clB = (CellLocation) b;
			
			int cmp;
			cmp = clA.getSheetIndex() - clB.getSheetIndex();
			if (cmp != 0) {
				return cmp;
			}
			cmp = clA.getRowIndex() - clB.getRowIndex();
			if (cmp != 0) {
				return cmp;
			}
			cmp = clA.getColumnIndex() - clB.getColumnIndex();
			
			return cmp;
		}
		
	};
}
