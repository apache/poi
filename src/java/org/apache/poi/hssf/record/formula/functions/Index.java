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
import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.Eval;
import org.apache.poi.hssf.record.formula.eval.EvaluationException;
import org.apache.poi.hssf.record.formula.eval.OperandResolver;
import org.apache.poi.hssf.record.formula.eval.RefEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;

/**
 * Implementation for the Excel function INDEX
 * <p>
 * 
 * Syntax : <br/>
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
public final class Index implements Function {

	// TODO - javadoc for interface method
	public Eval evaluate(Eval[] args, int srcCellRow, short srcCellCol) {
		int nArgs = args.length;
		if(nArgs < 2) {
			// too few arguments
			return ErrorEval.VALUE_INVALID;
		}
		Eval firstArg = args[0];
		if (firstArg instanceof RefEval) {
			// convert to area ref for simpler code in getValueFromArea()
			firstArg = ((RefEval)firstArg).offset(0, 0, 0, 0);
		}
		if(!(firstArg instanceof AreaEval)) {
			
			// else the other variation of this function takes an array as the first argument
			// it seems like interface 'ArrayEval' does not even exist yet
			
			throw new RuntimeException("Incomplete code - cannot handle first arg of type ("
					+ firstArg.getClass().getName() + ")");
		}
		AreaEval reference = (AreaEval) firstArg;
		
		int rowIx = 0;
		int columnIx = 0;
		int areaIx = 0;
		try {	
			switch(nArgs) {
				case 4:
					areaIx = convertIndexArgToZeroBase(args[3], srcCellRow, srcCellCol);
					throw new RuntimeException("Incomplete code" +
							" - don't know how to support the 'area_num' parameter yet)");
					// Excel expression might look like this "INDEX( (A1:B4, C3:D6, D2:E5 ), 1, 2, 3)
					// In this example, the 3rd area would be used i.e. D2:E5, and the overall result would be E2
					// Token array might be encoded like this: MemAreaPtg, AreaPtg, AreaPtg, UnionPtg, UnionPtg, ParenthesesPtg
					// The formula parser doesn't seem to support this yet. Not sure if the evaluator does either
					
				case 3:
					columnIx = convertIndexArgToZeroBase(args[2], srcCellRow, srcCellCol);
				case 2:
					rowIx = convertIndexArgToZeroBase(args[1], srcCellRow, srcCellCol);
					break;
				default:
					// too many arguments
					return ErrorEval.VALUE_INVALID;
			}
			return getValueFromArea(reference, rowIx, columnIx, nArgs);
		} catch (EvaluationException e) {
			return e.getErrorEval();
		}
	}
	
	/**
	 * @param nArgs - needed because error codes are slightly different when only 2 args are passed 
	 */
	private static ValueEval getValueFromArea(AreaEval ae, int pRowIx, int pColumnIx, int nArgs) throws EvaluationException {
		int rowIx;
		int columnIx;
		
		// when the area ref is a single row or a single column,
		// there are special rules for conversion of rowIx and columnIx
		if (ae.isRow()) {
			if (ae.isColumn()) {
				rowIx = pRowIx == -1 ? 0 : pRowIx;
				columnIx = pColumnIx == -1 ? 0 : pColumnIx;
			} else {
				if (nArgs == 2) {
					rowIx = 0;
					columnIx = pRowIx;
				} else {
					rowIx = pRowIx == -1 ? 0 : pRowIx;
					columnIx = pColumnIx;
				}
			}
			if (rowIx < -1 || columnIx < -1) {
				throw new EvaluationException(ErrorEval.VALUE_INVALID);
			}
		} else if (ae.isColumn()) {
			if (nArgs == 2) {
				rowIx = pRowIx;
				columnIx = 0;
			} else {
				rowIx = pRowIx;
				columnIx = pColumnIx == -1 ? 0 : pColumnIx;
			}
			if (rowIx < -1 || columnIx < -1) {
				throw new EvaluationException(ErrorEval.VALUE_INVALID);
			}
		} else {
			if (nArgs == 2) {
				// always an error with 2-D area refs
				if (pRowIx < -1) {
					throw new EvaluationException(ErrorEval.VALUE_INVALID);
				}
				throw new EvaluationException(ErrorEval.REF_INVALID);
			}
			// Normal case - area ref is 2-D, and both index args were provided
			rowIx = pRowIx;
			columnIx = pColumnIx;
		}
		
		int width = ae.getWidth();
		int height = ae.getHeight();
		// Slightly irregular logic for bounds checking errors
		if (rowIx >= height || columnIx >= width) {
			throw new EvaluationException(ErrorEval.REF_INVALID);
		}
		if (rowIx < 0 || columnIx < 0) {
			throw new EvaluationException(ErrorEval.VALUE_INVALID);
		}
		return ae.getRelativeValue(rowIx, columnIx);
	}

	/**
	 * takes a NumberEval representing a 1-based index and returns the zero-based int value
	 */
	private static int convertIndexArgToZeroBase(Eval arg, int srcCellRow, short srcCellCol) throws EvaluationException {
		
		ValueEval ev = OperandResolver.getSingleValue(arg, srcCellRow, srcCellCol);
		int oneBasedVal = OperandResolver.coerceValueToInt(ev);
		return oneBasedVal - 1;
	}
}
