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

import org.apache.poi.ss.formula.FormulaCellCache.IEntryOperation;
import org.apache.poi.ss.formula.FormulaUsedBlankCellSet.BookSheetKey;
import org.apache.poi.ss.formula.PlainCellCache.Loc;
import org.apache.poi.ss.formula.eval.BlankEval;
import org.apache.poi.ss.formula.eval.BoolEval;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.StringEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.usermodel.CellType;

/**
 * Performance optimisation for {@link org.apache.poi.ss.usermodel.FormulaEvaluator}.
 * This class stores previously calculated values of already visited cells,
 * to avoid unnecessary re-calculation when the same cells are referenced multiple times
 *
 * @author Josh Micich
 */
final class EvaluationCache {

	private final PlainCellCache _plainCellCache;
	private final FormulaCellCache _formulaCellCache;
	/** only used for testing. <code>null</code> otherwise */
	final IEvaluationListener _evaluationListener;

	/* package */EvaluationCache(IEvaluationListener evaluationListener) {
		_evaluationListener = evaluationListener;
		_plainCellCache = new PlainCellCache();
		_formulaCellCache = new FormulaCellCache();
	}

	public void notifyUpdateCell(int bookIndex, int sheetIndex, EvaluationCell cell) {
		FormulaCellCacheEntry fcce = _formulaCellCache.get(cell);

		int rowIndex = cell.getRowIndex();
		int columnIndex = cell.getColumnIndex();
		Loc loc = new Loc(bookIndex, sheetIndex, rowIndex, columnIndex);
		PlainValueCellCacheEntry pcce = _plainCellCache.get(loc);

		if (cell.getCellType() == CellType.FORMULA) {
			if (fcce == null) {
				fcce = new FormulaCellCacheEntry();
				if (pcce == null) {
					if (_evaluationListener != null) {
						_evaluationListener.onChangeFromBlankValue(sheetIndex, rowIndex,
								columnIndex, cell, fcce);
					}
					updateAnyBlankReferencingFormulas(bookIndex, sheetIndex, rowIndex,
							columnIndex);
				}
				_formulaCellCache.put(cell, fcce);
			} else {
				fcce.recurseClearCachedFormulaResults(_evaluationListener);
				fcce.clearFormulaEntry();
			}
			if (pcce == null) {
				// was formula cell before - no change of type
			} else {
				// changing from plain cell to formula cell
				pcce.recurseClearCachedFormulaResults(_evaluationListener);
				_plainCellCache.remove(loc);
			}
		} else {
			ValueEval value = WorkbookEvaluator.getValueFromNonFormulaCell(cell);
			if (pcce == null) {
				if (value != BlankEval.instance) {
					// only cache non-blank values in the plain cell cache
					// (dependencies on blank cells are managed by
					// FormulaCellCacheEntry._usedBlankCellGroup)
					pcce = new PlainValueCellCacheEntry(value);
					if (fcce == null) {
						if (_evaluationListener != null) {
							_evaluationListener.onChangeFromBlankValue(sheetIndex, rowIndex, columnIndex, cell, pcce);
						}
						updateAnyBlankReferencingFormulas(bookIndex, sheetIndex,
								rowIndex, columnIndex);
					}
					_plainCellCache.put(loc, pcce);
				}
			} else {
				if (pcce.updateValue(value)) {
					pcce.recurseClearCachedFormulaResults(_evaluationListener);
				}
				if (value == BlankEval.instance) {
					_plainCellCache.remove(loc);
				}
			}
			if (fcce == null) {
				// was plain cell before - no change of type
			} else {
				// was formula cell before - now a plain value
				_formulaCellCache.remove(cell);
				fcce.setSensitiveInputCells(null);
				fcce.recurseClearCachedFormulaResults(_evaluationListener);
			}
		}
	}

	private void updateAnyBlankReferencingFormulas(int bookIndex, int sheetIndex,
			final int rowIndex, final int columnIndex) {
		final BookSheetKey bsk = new BookSheetKey(bookIndex, sheetIndex);
		_formulaCellCache.applyOperation(new IEntryOperation() {

			public void processEntry(FormulaCellCacheEntry entry) {
				entry.notifyUpdatedBlankCell(bsk, rowIndex, columnIndex, _evaluationListener);
			}
		});
	}

	public PlainValueCellCacheEntry getPlainValueEntry(int bookIndex, int sheetIndex,
			int rowIndex, int columnIndex, ValueEval value) {

		Loc loc = new Loc(bookIndex, sheetIndex, rowIndex, columnIndex);
		PlainValueCellCacheEntry result = _plainCellCache.get(loc);
		if (result == null) {
			result = new PlainValueCellCacheEntry(value);
			_plainCellCache.put(loc, result);
			if (_evaluationListener != null) {
				_evaluationListener.onReadPlainValue(sheetIndex, rowIndex, columnIndex, result);
			}
		} else {
			// TODO - if we are confident that this sanity check is not required, we can remove 'value' from plain value cache entry
			if (!areValuesEqual(result.getValue(), value)) {
				throw new IllegalStateException("value changed");
			}
			if (_evaluationListener != null) {
				_evaluationListener.onCacheHit(sheetIndex, rowIndex, columnIndex, value);
			}
		}
		return result;
	}
	private boolean areValuesEqual(ValueEval a, ValueEval b) {
		if (a == null) {
			return false;
		}
		Class<?> cls = a.getClass();
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

	public FormulaCellCacheEntry getOrCreateFormulaCellEntry(EvaluationCell cell) {
		FormulaCellCacheEntry result = _formulaCellCache.get(cell);
		if (result == null) {

			result = new FormulaCellCacheEntry();
			_formulaCellCache.put(cell, result);
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
		_plainCellCache.clear();
		_formulaCellCache.clear();
	}
	public void notifyDeleteCell(int bookIndex, int sheetIndex, EvaluationCell cell) {

		if (cell.getCellType() == CellType.FORMULA) {
			FormulaCellCacheEntry fcce = _formulaCellCache.remove(cell);
			if (fcce == null) {
				// formula cell has not been evaluated yet
			} else {
				fcce.setSensitiveInputCells(null);
				fcce.recurseClearCachedFormulaResults(_evaluationListener);
			}
		} else {
			Loc loc = new Loc(bookIndex, sheetIndex, cell.getRowIndex(), cell.getColumnIndex());
			PlainValueCellCacheEntry pcce = _plainCellCache.get(loc);

			if (pcce == null) {
				// cache entry doesn't exist. nothing to do
			} else {
				pcce.recurseClearCachedFormulaResults(_evaluationListener);
			}
		}
	}
}
