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

package org.apache.poi.ss.formula.functions;

import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.formula.OperationEvaluationContext;


/**
 * For most Excel functions, involving references ((cell, area), (2d, 3d)), the references are
 * passed in as arguments, and the exact location remains fixed.  However, a select few Excel
 * functions have the ability to access cells that were not part of any reference passed as an
 * argument.<br>
 * Two important functions with this feature are <b>INDIRECT</b> and <b>OFFSET</b><p>
 *
 * When POI evaluates formulas, each reference argument is capable of evaluating any cell inside
 * its range.  Actually, even cells outside the reference range but on the same sheet can be
 * evaluated.  This allows <b>OFFSET</b> to be implemented like most other functions - taking only
 * the arguments, and source cell coordinates.
 *
 * For the moment this interface only exists to serve the <b>INDIRECT</b> which can decode
 * arbitrary text into cell references, and evaluate them..
 *
 * @author Josh Micich
 */
public interface FreeRefFunction {
	/**
	 * @param args the pre-evaluated arguments for this function. args is never <code>null</code>,
	 *             nor are any of its elements.
	 * @param ec primarily used to identify the source cell containing the formula being evaluated.
	 *             may also be used to dynamically create reference evals.
	 * @return never <code>null</code>. Possibly an instance of <tt>ErrorEval</tt> in the case of
	 * a specified Excel error (Exceptions are never thrown to represent Excel errors).
	 */
	ValueEval evaluate(ValueEval[] args, OperationEvaluationContext ec);
}
