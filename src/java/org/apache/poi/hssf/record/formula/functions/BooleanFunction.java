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

import org.apache.poi.hssf.record.formula.eval.AreaEval;
import org.apache.poi.hssf.record.formula.eval.BoolEval;
import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;
import org.apache.poi.hssf.record.formula.eval.EvaluationException;
import org.apache.poi.hssf.record.formula.eval.OperandResolver;
import org.apache.poi.hssf.record.formula.eval.RefEval;

/**
 * Here are the general rules concerning Boolean functions:
 * <ol>
 * <li> Blanks are ignored (not either true or false) </li>
 * <li> Strings are ignored if part of an area ref or cell ref, otherwise they must be 'true' or 'false'</li>
 * <li> Numbers: 0 is false. Any other number is TRUE </li>
 * <li> Areas: *all* cells in area are evaluated according to the above rules</li>
 * </ol>
 *
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 */
public abstract class BooleanFunction implements Function {

	public final ValueEval evaluate(ValueEval[] args, int srcRow, short srcCol) {
		if (args.length < 1) {
			return ErrorEval.VALUE_INVALID;
		}
		boolean boolResult;
		try {
			boolResult = calculate(args);
		} catch (EvaluationException e) {
			return e.getErrorEval();
		}
		return BoolEval.valueOf(boolResult);
	}

	private boolean calculate(ValueEval[] args) throws EvaluationException {

		boolean result = getInitialResultValue();
		boolean atleastOneNonBlank = false;

		/*
		 * Note: no short-circuit boolean loop exit because any ErrorEvals will override the result
		 */
		for (int i=0, iSize=args.length; i<iSize; i++) {
			ValueEval arg = args[i];
			if (arg instanceof AreaEval) {
				AreaEval ae = (AreaEval) arg;
				int height = ae.getHeight();
				int width = ae.getWidth();
				for (int rrIx=0; rrIx<height; rrIx++) {
					for (int rcIx=0; rcIx<width; rcIx++) {
						ValueEval ve = ae.getRelativeValue(rrIx, rcIx);
						Boolean tempVe = OperandResolver.coerceValueToBoolean(ve, true);
						if (tempVe != null) {
							result = partialEvaluate(result, tempVe.booleanValue());
							atleastOneNonBlank = true;
						}
					}
				}
				continue;
			}
			Boolean tempVe;
			if (arg instanceof RefEval) {
				ValueEval ve = ((RefEval) arg).getInnerValueEval();
				tempVe = OperandResolver.coerceValueToBoolean(ve, true);
			} else {
				tempVe = OperandResolver.coerceValueToBoolean(arg, false);
			}


			if (tempVe != null) {
				result = partialEvaluate(result, tempVe.booleanValue());
				atleastOneNonBlank = true;
			}
		}

		if (!atleastOneNonBlank) {
			throw new EvaluationException(ErrorEval.VALUE_INVALID);
		}
		return result;
	}


	protected abstract boolean getInitialResultValue();
	protected abstract boolean partialEvaluate(boolean cumulativeResult, boolean currentValue);
}
