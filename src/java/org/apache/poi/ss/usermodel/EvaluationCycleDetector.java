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

package org.apache.poi.ss.usermodel;

import java.util.ArrayList;
import java.util.List;

/**
 * Instances of this class keep track of multiple dependent cell evaluations due
 * to recursive calls to <tt>FormulaEvaluator.internalEvaluate()</tt>.
 * The main purpose of this class is to detect an attempt to evaluate a cell
 * that is already being evaluated. In other words, it detects circular
 * references in spreadsheet formulas.
 * 
 * @author Josh Micich
 */
final class EvaluationCycleDetector {

	/**
	 * Stores the parameters that identify the evaluation of one cell.<br/>
	 */
	private static final class CellEvaluationFrame {

		private final Workbook _workbook;
		private final int _sheetIndex;
		private final int _srcRowNum;
		private final int _srcColNum;

		public CellEvaluationFrame(Workbook workbook, int sheetIndex, int srcRowNum, int srcColNum) {
			if (workbook == null) {
				throw new IllegalArgumentException("workbook must not be null");
			}
			if (sheetIndex < 0) {
				throw new IllegalArgumentException("sheetIndex must not be negative");
			}
			_workbook = workbook;
			_sheetIndex = sheetIndex;
			_srcRowNum = srcRowNum;
			_srcColNum = srcColNum;
		}

		public boolean equals(Object obj) {
			CellEvaluationFrame other = (CellEvaluationFrame) obj;
			if (_workbook != other._workbook) {
				return false;
			}
			if (_sheetIndex != other._sheetIndex) {
				return false;
			}
			if (_srcRowNum != other._srcRowNum) {
				return false;
			}
			if (_srcColNum != other._srcColNum) {
				return false;
			}
			return true;
		}

		/**
		 * @return human readable string for debug purposes
		 */
		public String formatAsString() {
			return "R=" + _srcRowNum + " C=" + _srcColNum + " ShIx=" + _sheetIndex;
		}

		public String toString() {
			StringBuffer sb = new StringBuffer(64);
			sb.append(getClass().getName()).append(" [");
			sb.append(formatAsString());
			sb.append("]");
			return sb.toString();
		}
	}

	private final List _evaluationFrames;

	public EvaluationCycleDetector() {
		_evaluationFrames = new ArrayList();
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
	public boolean startEvaluate(Workbook workbook, int sheetIndex, int srcRowNum, int srcColNum) {
		CellEvaluationFrame cef = new CellEvaluationFrame(workbook, sheetIndex, srcRowNum, srcColNum);
		if (_evaluationFrames.contains(cef)) {
			return false;
		}
		_evaluationFrames.add(cef);
		return true;
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
	 */
	public void endEvaluate(Workbook workbook, int sheetIndex, int srcRowNum, int srcColNum) {
		int nFrames = _evaluationFrames.size();
		if (nFrames < 1) {
			throw new IllegalStateException("Call to endEvaluate without matching call to startEvaluate");
		}

		nFrames--;
		CellEvaluationFrame cefExpected = (CellEvaluationFrame) _evaluationFrames.get(nFrames);
		CellEvaluationFrame cefActual = new CellEvaluationFrame(workbook, sheetIndex, srcRowNum, srcColNum);
		if (!cefActual.equals(cefExpected)) {
			throw new RuntimeException("Wrong cell specified. "
					+ "Corresponding startEvaluate() call was for cell {"
					+ cefExpected.formatAsString() + "} this endEvaluate() call is for cell {"
					+ cefActual.formatAsString() + "}");
		}
		// else - no problems so pop current frame 
		_evaluationFrames.remove(nFrames);
	}
}
