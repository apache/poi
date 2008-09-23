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

import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;
import org.apache.poi.hssf.usermodel.HSSFCell;

/**
 * Instances of this class keep track of multiple dependent cell evaluations due
 * to recursive calls to {@link WorkbookEvaluator#evaluate(HSSFCell)}
 * The main purpose of this class is to detect an attempt to evaluate a cell
 * that is already being evaluated. In other words, it detects circular
 * references in spreadsheet formulas.
 * 
 * @author Josh Micich
 */
final class EvaluationTracker {

	private final List _evaluationFrames;
	private final Set _currentlyEvaluatingCells;
	private final EvaluationCache _cache;

	public EvaluationTracker(EvaluationCache cache) {
		_cache = cache;
		_evaluationFrames = new ArrayList();
		_currentlyEvaluatingCells = new HashSet();
	}

	/**
	 * Notifies this evaluation tracker that evaluation of the specified cell is
	 * about to start.<br/>
	 * 
	 * In the case of a <code>true</code> return code, the caller should
	 * continue evaluation of the specified cell, and also be sure to call
	 * <tt>endEvaluate()</tt> when complete.<br/>
	 * 
	 * In the case of a <code>false</code> return code, the caller should
	 * return an evaluation result of
	 * <tt>ErrorEval.CIRCULAR_REF_ERROR<tt>, and not call <tt>endEvaluate()</tt>.  
	 * <br/>
	 * @return <code>true</code> if the specified cell has not been visited yet in the current 
	 * evaluation. <code>false</code> if the specified cell is already being evaluated.
	 */
	public ValueEval startEvaluate(CellLocation cellLoc) {
		if (cellLoc == null) {
			throw new IllegalArgumentException("cellLoc must not be null");
		}
		if (_currentlyEvaluatingCells.contains(cellLoc)) {
			return ErrorEval.CIRCULAR_REF_ERROR;
		}
		ValueEval result = _cache.getValue(cellLoc);
		if (result == null) {
			_currentlyEvaluatingCells.add(cellLoc);
			_evaluationFrames.add(new CellEvaluationFrame(cellLoc));
		}
		return result;
	}

	/**
	 * Notifies this evaluation tracker that the evaluation of the specified
	 * cell is complete. <p/>
	 * 
	 * Every successful call to <tt>startEvaluate</tt> must be followed by a
	 * call to <tt>endEvaluate</tt> (recommended in a finally block) to enable
	 * proper tracking of which cells are being evaluated at any point in time.<p/>
	 * 
	 * Assuming a well behaved client, parameters to this method would not be
	 * required. However, they have been included to assert correct behaviour,
	 * and form more meaningful error messages.
	 * @param result 
	 */
	public void endEvaluate(CellLocation cellLoc, ValueEval result, boolean isPlainValueCell) {

		int nFrames = _evaluationFrames.size();
		if (nFrames < 1) {
			throw new IllegalStateException("Call to endEvaluate without matching call to startEvaluate");
		}

		nFrames--;
		CellEvaluationFrame frame = (CellEvaluationFrame) _evaluationFrames.get(nFrames);
		CellLocation coordinates = frame.getCoordinates();
		if (!coordinates.equals(cellLoc)) {
			throw new RuntimeException("Wrong cell specified. "
					+ "Corresponding startEvaluate() call was for cell {"
					+ coordinates.formatAsString() + "} this endEvaluate() call is for cell {"
					+ cellLoc.formatAsString() + "}");
		}
		// else - no problems so pop current frame 
		_evaluationFrames.remove(nFrames);
		_currentlyEvaluatingCells.remove(coordinates);

		// TODO - don't cache results of volatile formulas 
		_cache.setValue(coordinates, isPlainValueCell, frame.getUsedCells(), result);
	}
	/**
	 * Tells the currently evaluating cell frame that it has a dependency on the specified 
	 * <tt>usedCell<tt> 
	 * @param usedCell location of cell which is referenced (and used) by the current cell.
	 */
	public void acceptDependency(CellLocation usedCell) {
		int prevFrameIndex = _evaluationFrames.size()-1;
		if (prevFrameIndex < 0) {
			throw new IllegalStateException("Call to acceptDependency without prior call to startEvaluate");
		}
		CellEvaluationFrame consumingFrame = (CellEvaluationFrame) _evaluationFrames.get(prevFrameIndex);
		consumingFrame.addUsedCell(usedCell);
	}

}
