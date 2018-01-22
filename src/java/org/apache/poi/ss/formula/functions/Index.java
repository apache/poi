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

import org.apache.poi.ss.formula.eval.BlankEval;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.EvaluationException;
import org.apache.poi.ss.formula.eval.MissingArgEval;
import org.apache.poi.ss.formula.eval.OperandResolver;
import org.apache.poi.ss.formula.eval.RefEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.formula.TwoDEval;

/**
 * Implementation for the Excel function INDEX
 * <p>
 *
 * Syntax : <br>
 *  INDEX ( reference, row_num[, column_num [, area_num]])</br>
 *  INDEX ( array, row_num[, column_num])
 *    <table border="0" cellpadding="1" cellspacing="0" summary="Parameter descriptions">
 *      <tr><th>reference</th><td>typically an area reference, possibly a union of areas</td></tr>
 *      <tr><th>array</th><td>a literal array value (currently not supported)</td></tr>
 *      <tr><th>row_num</th><td>selects the row within the array or area reference</td></tr>
 *      <tr><th>column_num</th><td>selects column within the array or area reference. default is 1</td></tr>
 *      <tr><th>area_num</th><td>used when reference is a union of areas</td></tr>
 *    </table>
 * </p>
 *
 * @author Josh Micich
 */
public final class Index implements Function2Arg, Function3Arg, Function4Arg, ArrayMode {

	public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0, ValueEval arg1) {
		TwoDEval reference = convertFirstArg(arg0);

		int columnIx = 0;
		try {
			int rowIx = resolveIndexArg(arg1, srcRowIndex, srcColumnIndex);

			if (!reference.isColumn()) {
				if (!reference.isRow()) {
					// always an error with 2-D area refs
					// Note - the type of error changes if the pRowArg is negative
					return ErrorEval.REF_INVALID;
				}
				// When the two-arg version of INDEX() has been invoked and the reference
				// is a single column ref, the row arg seems to get used as the column index
				columnIx = rowIx;
				rowIx = 0;
			}

			return getValueFromArea(reference, rowIx, columnIx);
		} catch (EvaluationException e) {
			return e.getErrorEval();
		}
	}
	public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0, ValueEval arg1,
			ValueEval arg2) {
		TwoDEval reference = convertFirstArg(arg0);

		try {
			int columnIx = resolveIndexArg(arg2, srcRowIndex, srcColumnIndex);
			int rowIx = resolveIndexArg(arg1, srcRowIndex, srcColumnIndex);
			return getValueFromArea(reference, rowIx, columnIx);
		} catch (EvaluationException e) {
			return e.getErrorEval();
		}
	}
	public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0, ValueEval arg1,
			ValueEval arg2, ValueEval arg3) {
		throw new RuntimeException("Incomplete code"
				+ " - don't know how to support the 'area_num' parameter yet)");
		// Excel expression might look like this "INDEX( (A1:B4, C3:D6, D2:E5 ), 1, 2, 3)
		// In this example, the 3rd area would be used i.e. D2:E5, and the overall result would be E2
		// Token array might be encoded like this: MemAreaPtg, AreaPtg, AreaPtg, UnionPtg, UnionPtg, ParenthesesPtg
		// The formula parser doesn't seem to support this yet. Not sure if the evaluator does either
	}

	private static TwoDEval convertFirstArg(ValueEval arg0) {
        if (arg0 instanceof RefEval) {
			// convert to area ref for simpler code in getValueFromArea()
			return ((RefEval) arg0).offset(0, 0, 0, 0);
		}
		if((arg0 instanceof TwoDEval)) {
			return (TwoDEval) arg0;
		}
		// else the other variation of this function takes an array as the first argument
		// it seems like interface 'ArrayEval' does not even exist yet
		throw new RuntimeException("Incomplete code - cannot handle first arg of type ("
				+ arg0.getClass().getName() + ")");

	}

	public ValueEval evaluate(ValueEval[] args, int srcRowIndex, int srcColumnIndex) {
		switch (args.length) {
			case 2:
				return evaluate(srcRowIndex, srcColumnIndex, args[0], args[1]);
			case 3:
				return evaluate(srcRowIndex, srcColumnIndex, args[0], args[1], args[2]);
			case 4:
				return evaluate(srcRowIndex, srcColumnIndex, args[0], args[1], args[2], args[3]);
		}
		return ErrorEval.VALUE_INVALID;
	}

	private static ValueEval getValueFromArea(TwoDEval ae, int pRowIx, int pColumnIx)
			throws EvaluationException {
		assert pRowIx >= 0;
		assert pColumnIx >= 0;

		TwoDEval result = ae;

		if (pRowIx != 0) {
			// Slightly irregular logic for bounds checking errors
			if (pRowIx > ae.getHeight()) {
				// high bounds check fail gives #REF! if arg was explicitly passed
				throw new EvaluationException(ErrorEval.REF_INVALID);
			}
			result = result.getRow(pRowIx-1);
		}

		if (pColumnIx != 0) {
			// Slightly irregular logic for bounds checking errors
			if (pColumnIx > ae.getWidth()) {
				// high bounds check fail gives #REF! if arg was explicitly passed
				throw new EvaluationException(ErrorEval.REF_INVALID);
			}
			result = result.getColumn(pColumnIx-1);
		}
		return result;
	}


	/**
	 * @param arg a 1-based index.
	 * @return the resolved 1-based index. Zero if the arg was missing or blank
	 * @throws EvaluationException if the arg is an error value evaluates to a negative numeric value
	 */
	private static int resolveIndexArg(ValueEval arg, int srcCellRow, int srcCellCol) throws EvaluationException {

		ValueEval ev = OperandResolver.getSingleValue(arg, srcCellRow, srcCellCol);
		if (ev == MissingArgEval.instance) {
			return 0;
		}
		if (ev == BlankEval.instance) {
			return 0;
		}
		int result = OperandResolver.coerceValueToInt(ev);
		if (result < 0) {
			throw new EvaluationException(ErrorEval.VALUE_INVALID);
		}
		return result;
	}
}
