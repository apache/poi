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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.poi.ss.formula.eval.BlankEval;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.ValueEval;

/**
 * Instances of this class keep track of multiple dependent cell evaluations due
 * to recursive calls to {@link WorkbookEvaluator#evaluate(EvaluationCell)}}
 * The main purpose of this class is to detect an attempt to evaluate a cell
 * that is already being evaluated. In other words, it detects circular
 * references in spreadsheet formulas.
 *
 * @author Josh Micich
 */
final class EvaluationTracker {
	// TODO - consider deleting this class and letting CellEvaluationFrame take care of itself
	private final List<CellEvaluationFrame> _evaluationFrames;
	private final Set<FormulaCellCacheEntry> _currentlyEvaluatingCells;
	private final EvaluationCache _cache;

	public EvaluationTracker(EvaluationCache cache) {
		_cache = cache;
		_evaluationFrames = new ArrayList<>();
		_currentlyEvaluatingCells = new HashSet<>();
	}

	/**
	 * Notifies this evaluation tracker that evaluation of the specified cell is
	 * about to start.<br>
	 *
	 * In the case of a <code>true</code> return code, the caller should
	 * continue evaluation of the specified cell, and also be sure to call
	 * <tt>endEvaluate()</tt> when complete.<br>
	 *
	 * In the case of a <code>null</code> return code, the caller should
	 * return an evaluation result of
	 * <tt>ErrorEval.CIRCULAR_REF_ERROR<tt>, and not call <tt>endEvaluate()</tt>.
	 * <br>
	 * @return <code>false</code> if the specified cell is already being evaluated
	 */
	public boolean startEvaluate(FormulaCellCacheEntry cce) {
		if (cce == null) {
			throw new IllegalArgumentException("cellLoc must not be null");
		}
		if (_currentlyEvaluatingCells.contains(cce)) {
			return false;
		}
		_currentlyEvaluatingCells.add(cce);
		_evaluationFrames.add(new CellEvaluationFrame(cce));
		return true;
	}

	public void updateCacheResult(ValueEval result) {

		int nFrames = _evaluationFrames.size();
		if (nFrames < 1) {
			throw new IllegalStateException("Call to endEvaluate without matching call to startEvaluate");
		}
		CellEvaluationFrame frame = _evaluationFrames.get(nFrames-1);
		if (result == ErrorEval.CIRCULAR_REF_ERROR && nFrames > 1) {
			// Don't cache a circular ref error result if this cell is not the top evaluated cell.
			// A true circular ref error will propagate all the way around the loop.  However, it's
			// possible to have parts of the formula tree (/ parts of the loop) to evaluate to
			// CIRCULAR_REF_ERROR, and that value not get used in the final cell result (see the
			// unit tests for a simple example). Thus, the only CIRCULAR_REF_ERROR result that can
			// safely be cached is that of the top evaluated cell.
			return;
		}

		frame.updateFormulaResult(result);
	}

	/**
	 * Notifies this evaluation tracker that the evaluation of the specified cell is complete. <p>
	 *
	 * Every successful call to <tt>startEvaluate</tt> must be followed by a call to <tt>endEvaluate</tt> (recommended in a finally block) to enable
	 * proper tracking of which cells are being evaluated at any point in time.<p>
	 *
	 * Assuming a well behaved client, parameters to this method would not be
	 * required. However, they have been included to assert correct behaviour,
	 * and form more meaningful error messages.
	 */
	public void endEvaluate(CellCacheEntry cce) {

		int nFrames = _evaluationFrames.size();
		if (nFrames < 1) {
			throw new IllegalStateException("Call to endEvaluate without matching call to startEvaluate");
		}

		nFrames--;
		CellEvaluationFrame frame = _evaluationFrames.get(nFrames);
		if (cce != frame.getCCE()) {
			throw new IllegalStateException("Wrong cell specified. ");
		}
		// else - no problems so pop current frame
		_evaluationFrames.remove(nFrames);
		_currentlyEvaluatingCells.remove(cce);
	}

	public void acceptFormulaDependency(CellCacheEntry cce) {
		// Tell the currently evaluating cell frame that it has a dependency on the specified
		int prevFrameIndex = _evaluationFrames.size()-1;
		if (prevFrameIndex < 0) {
			// Top level frame, there is no 'cell' above this frame that is using the current cell
		} else {
			CellEvaluationFrame consumingFrame = _evaluationFrames.get(prevFrameIndex);
			consumingFrame.addSensitiveInputCell(cce);
		}
	}

	public void acceptPlainValueDependency(EvaluationWorkbook evalWorkbook, int bookIndex, int sheetIndex,
			int rowIndex, int columnIndex, ValueEval value) {
		// Tell the currently evaluating cell frame that it has a dependency on the specified
		int prevFrameIndex = _evaluationFrames.size() - 1;
		if (prevFrameIndex < 0) {
			// Top level frame, there is no 'cell' above this frame that is using the current cell
		} else {
			CellEvaluationFrame consumingFrame = _evaluationFrames.get(prevFrameIndex);
			if (value == BlankEval.instance) {
				consumingFrame.addUsedBlankCell(evalWorkbook, bookIndex, sheetIndex, rowIndex, columnIndex);
			} else {
				PlainValueCellCacheEntry cce = _cache.getPlainValueEntry(bookIndex, sheetIndex,
						rowIndex, columnIndex, value);
				consumingFrame.addSensitiveInputCell(cce);
			}
		}
	}
}
