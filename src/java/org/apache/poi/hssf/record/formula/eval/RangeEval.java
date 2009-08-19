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

package org.apache.poi.hssf.record.formula.eval;

import org.apache.poi.hssf.record.formula.functions.Function;


/**
 *
 * @author Josh Micich
 */
public final class RangeEval implements Function {

	public static final Function instance = new RangeEval();

	private RangeEval() {
		// enforces singleton
	}

	public ValueEval evaluate(ValueEval[] args, int srcRow, short srcCol) {
		if(args.length != 2) {
			return ErrorEval.VALUE_INVALID;
		}

		try {
			AreaEval reA = evaluateRef(args[0]);
			AreaEval reB = evaluateRef(args[1]);
			return resolveRange(reA, reB);
		} catch (EvaluationException e) {
			return e.getErrorEval();
		}
	}

	/**
	 * @return simple rectangular {@link AreaEval} which fully encloses both areas
	 * <tt>aeA</tt> and <tt>aeB</tt>
	 */
	private static AreaEval resolveRange(AreaEval aeA, AreaEval aeB) {
		int aeAfr = aeA.getFirstRow();
		int aeAfc = aeA.getFirstColumn();

		int top = Math.min(aeAfr, aeB.getFirstRow());
		int bottom = Math.max(aeA.getLastRow(), aeB.getLastRow());
		int left = Math.min(aeAfc, aeB.getFirstColumn());
		int right = Math.max(aeA.getLastColumn(), aeB.getLastColumn());

		return aeA.offset(top-aeAfr, bottom-aeAfr, left-aeAfc, right-aeAfc);
	}

	private static AreaEval evaluateRef(ValueEval arg) throws EvaluationException {
		if (arg instanceof AreaEval) {
			return (AreaEval) arg;
		}
		if (arg instanceof RefEval) {
			return ((RefEval) arg).offset(0, 0, 0, 0);
		}
		if (arg instanceof ErrorEval) {
			throw new EvaluationException((ErrorEval)arg);
		}
		throw new IllegalArgumentException("Unexpected ref arg class (" + arg.getClass().getName() + ")");
	}
}
