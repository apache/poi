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
import org.apache.poi.hssf.record.formula.eval.ValueEval;
import org.apache.poi.hssf.record.formula.functions.LookupUtils.ValueVector;
/**
 * Implementation of the VLOOKUP() function.<p/>
 * 
 * VLOOKUP finds a row in a lookup table by the first column value and returns the value from another column.<br/>
 * 
 * <b>Syntax</b>:<br/>
 * <b>VLOOKUP</b>(<b>lookup_value</b>, <b>table_array</b>, <b>col_index_num</b>, range_lookup)<p/>
 * 
 * <b>lookup_value</b>  The value to be found in the first column of the table array.<br/>
 * <b>table_array</b> An area reference for the lookup data. <br/>
 * <b>col_index_num</b> a 1 based index specifying which column value of the lookup data will be returned.<br/>
 * <b>range_lookup</b> If TRUE (default), VLOOKUP finds the largest value less than or equal to 
 * the lookup_value.  If FALSE, only exact matches will be considered<br/>   
 * 
 * @author Josh Micich
 */
public final class Vlookup implements Function {
	
	private static final class ColumnVector implements ValueVector {

		private final AreaEval _tableArray;
		private final int _size;
		private final int _columnAbsoluteIndex;
		private final int _firstRowAbsoluteIndex;

		public ColumnVector(AreaEval tableArray, int columnIndex) {
			_columnAbsoluteIndex = tableArray.getFirstColumn() + columnIndex;
			if(!tableArray.containsColumn((short)_columnAbsoluteIndex)) {
				int lastColIx =  tableArray.getLastColumn() -  tableArray.getFirstColumn();
				throw new IllegalArgumentException("Specified column index (" + columnIndex 
						+ ") is outside the allowed range (0.." + lastColIx + ")");
			}
			_tableArray = tableArray;
			_size = tableArray.getLastRow() - tableArray.getFirstRow() + 1;
			if(_size < 1) {
				throw new RuntimeException("bad table array size zero");
			}
			_firstRowAbsoluteIndex = tableArray.getFirstRow();
		}

		public ValueEval getItem(int index) {
			if(index>_size) {
				throw new ArrayIndexOutOfBoundsException("Specified index (" + index 
						+ ") is outside the allowed range (0.." + (_size-1) + ")");
			}
			return _tableArray.getValueAt(_firstRowAbsoluteIndex + index, (short)_columnAbsoluteIndex);
		}
		public int getSize() {
			return _size;
		}
	}

	public Eval evaluate(Eval[] args, int srcCellRow, short srcCellCol) {
		Eval arg3 = null;
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
			// arg0 lookup_value, arg1 table_array, arg3 range_lookup, find lookup value, arg2 col_index, fetch result
			ValueEval lookupValue = OperandResolver.getSingleValue(args[0], srcCellRow, srcCellCol);
			AreaEval tableArray = LookupUtils.resolveTableArrayArg(args[1]);
			boolean isRangeLookup = LookupUtils.resolveRangeLookupArg(arg3, srcCellRow, srcCellCol);
			int rowIndex = LookupUtils.lookupIndexOfValue(lookupValue, new ColumnVector(tableArray, 0), isRangeLookup);
			ValueEval veColIndex = OperandResolver.getSingleValue(args[2], srcCellRow, srcCellCol);
			int colIndex = LookupUtils.resolveRowOrColIndexArg(veColIndex);
			ValueVector resultCol = createResultColumnVector(tableArray, colIndex);
			return resultCol.getItem(rowIndex);
		} catch (EvaluationException e) {
			return e.getErrorEval();
		}
	}


	/**
	 * Returns one column from an <tt>AreaEval</tt>
	 * 
	 * @throws EvaluationException (#VALUE!) if colIndex is negative, (#REF!) if colIndex is too high
	 */
	private ValueVector createResultColumnVector(AreaEval tableArray, int colIndex) throws EvaluationException {
		if(colIndex < 0) {
			throw EvaluationException.invalidValue();
		}
		int nCols = tableArray.getLastColumn() - tableArray.getFirstColumn() + 1;
		
		if(colIndex >= nCols) {
			throw EvaluationException.invalidRef();
		}
		return new ColumnVector(tableArray, colIndex);
	}
}
