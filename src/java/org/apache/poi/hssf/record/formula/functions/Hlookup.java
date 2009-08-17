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
import org.apache.poi.hssf.record.formula.eval.EvaluationException;
import org.apache.poi.hssf.record.formula.eval.OperandResolver;
import org.apache.poi.hssf.record.formula.eval.ValueEval;
import org.apache.poi.hssf.record.formula.functions.LookupUtils.ValueVector;
/**
 * Implementation of the HLOOKUP() function.<p/>
 *
 * HLOOKUP finds a column in a lookup table by the first row value and returns the value from another row.<br/>
 *
 * <b>Syntax</b>:<br/>
 * <b>HLOOKUP</b>(<b>lookup_value</b>, <b>table_array</b>, <b>row_index_num</b>, range_lookup)<p/>
 *
 * <b>lookup_value</b>  The value to be found in the first column of the table array.<br/>
 * <b>table_array</b> An area reference for the lookup data. <br/>
 * <b>row_index_num</b> a 1 based index specifying which row value of the lookup data will be returned.<br/>
 * <b>range_lookup</b> If TRUE (default), HLOOKUP finds the largest value less than or equal to
 * the lookup_value.  If FALSE, only exact matches will be considered<br/>
 *
 * @author Josh Micich
 */
public final class Hlookup implements Function {

	public ValueEval evaluate(ValueEval[] args, int srcCellRow, short srcCellCol) {
		ValueEval arg3 = null;
		switch(args.length) {
			case 4:
				arg3 = args[3]; // important: assumed array element is never null
			case 3:
				break;
			default:
				// wrong number of arguments
				return ErrorEval.VALUE_INVALID;
		}
		try {
			// Evaluation order:
			// arg0 lookup_value, arg1 table_array, arg3 range_lookup, find lookup value, arg2 row_index, fetch result
			ValueEval lookupValue = OperandResolver.getSingleValue(args[0], srcCellRow, srcCellCol);
			AreaEval tableArray = LookupUtils.resolveTableArrayArg(args[1]);
			boolean isRangeLookup = LookupUtils.resolveRangeLookupArg(arg3, srcCellRow, srcCellCol);
			int colIndex = LookupUtils.lookupIndexOfValue(lookupValue, LookupUtils.createRowVector(tableArray, 0), isRangeLookup);
			int rowIndex = LookupUtils.resolveRowOrColIndexArg(args[2], srcCellRow, srcCellCol);
			ValueVector resultCol = createResultColumnVector(tableArray, rowIndex);
			return resultCol.getItem(colIndex);
		} catch (EvaluationException e) {
			return e.getErrorEval();
		}
	}


	/**
	 * Returns one column from an <tt>AreaEval</tt>
	 *
	 * @param rowIndex assumed to be non-negative
	 *
	 * @throws EvaluationException (#REF!) if colIndex is too high
	 */
	private ValueVector createResultColumnVector(AreaEval tableArray, int rowIndex) throws EvaluationException {
		if(rowIndex >= tableArray.getHeight()) {
			throw EvaluationException.invalidRef();
		}
		return LookupUtils.createRowVector(tableArray, rowIndex);
	}
}
