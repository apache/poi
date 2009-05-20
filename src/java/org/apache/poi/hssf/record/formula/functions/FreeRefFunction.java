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

package org.apache.poi.hssf.record.formula.functions;

import org.apache.poi.hssf.record.formula.eval.Eval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;
import org.apache.poi.ss.formula.EvaluationWorkbook;


/**
 * For most Excel functions, involving references ((cell, area), (2d, 3d)), the references are
 * passed in as arguments, and the exact location remains fixed.  However, a select few Excel
 * functions have the ability to access cells that were not part of any reference passed as an
 * argument.<br/>
 * Two important functions with this feature are <b>INDIRECT</b> and <b>OFFSET</b><p/>
 *
 * In POI, the <tt>HSSFFormulaEvaluator</tt> evaluates every cell in each reference argument before
 * calling the function.  This means that functions using fixed references do not need access to
 * the rest of the workbook to execute.  Hence the <tt>evaluate()</tt> method on the common
 * interface <tt>Function</tt> does not take a workbook parameter.<p>
 *
 * This interface recognises the requirement of some functions to freely create and evaluate
 * references beyond those passed in as arguments.
 *
 * @author Josh Micich
 */
public interface FreeRefFunction {
	/**
	 *
	 * @param args the pre-evaluated arguments for this function. args is never <code>null</code>,
	 * 		  nor are any of its elements.
	 * @param srcCellSheet zero based sheet index of the cell containing the currently evaluating formula
	 * @param srcCellRow zero based row index of the cell containing the currently evaluating formula
	 * @param srcCellCol zero based column index of the cell containing the currently evaluating formula
	 * @param workbook is the workbook containing the formula/cell being evaluated
	 * @return never <code>null</code>. Possibly an instance of <tt>ErrorEval</tt> in the case of
	 * a specified Excel error (Exceptions are never thrown to represent Excel errors).
	 *
	 */
	ValueEval evaluate(Eval[] args, EvaluationWorkbook workbook, int srcCellSheet, int srcCellRow, int srcCellCol);
}
