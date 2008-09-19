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
import java.util.List;

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
	private final EvaluationCache _cache;

	public EvaluationTracker(EvaluationCache cache) {
		_cache = cache;
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
	public ValueEval startEvaluate(int sheetIndex, int srcRowNum, int srcColNum) {
		CellEvaluationFrame cef = new CellEvaluationFrame(sheetIndex, srcRowNum, srcColNum);
		if (_evaluationFrames.contains(cef)) {
			return ErrorEval.CIRCULAR_REF_ERROR;
		}
		ValueEval result = _cache.getValue(cef);
		if (result == null) {
			_evaluationFrames.add(cef);
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
	public void endEvaluate(int sheetIndex, int srcRowNum, int srcColNum, ValueEval result) {
		int nFrames = _evaluationFrames.size();
		if (nFrames < 1) {
			throw new IllegalStateException("Call to endEvaluate without matching call to startEvaluate");
		}

		nFrames--;
		CellEvaluationFrame cefExpected = (CellEvaluationFrame) _evaluationFrames.get(nFrames);
		CellEvaluationFrame cefActual = new CellEvaluationFrame(sheetIndex, srcRowNum, srcColNum);
		if (!cefActual.equals(cefExpected)) {
			throw new RuntimeException("Wrong cell specified. "
					+ "Corresponding startEvaluate() call was for cell {"
					+ cefExpected.formatAsString() + "} this endEvaluate() call is for cell {"
					+ cefActual.formatAsString() + "}");
		}
		// else - no problems so pop current frame 
		_evaluationFrames.remove(nFrames);
		
		_cache.setValue(cefActual, result);
	}
}
