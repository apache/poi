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

package org.apache.poi.ss.formula.eval.forked;

import org.apache.poi.hssf.record.formula.eval.BlankEval;
import org.apache.poi.hssf.record.formula.eval.BoolEval;
import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.NumberEval;
import org.apache.poi.hssf.record.formula.eval.StringEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.ss.formula.EvaluationCell;
import org.apache.poi.ss.formula.EvaluationSheet;
import org.apache.poi.ss.usermodel.Cell;

/**
 * Represents a cell being used for forked evaluation that has had a value set different from the
 * corresponding cell in the shared master workbook.
 *
 * @author Josh Micich
 */
final class ForkedEvaluationCell implements EvaluationCell {

	private final EvaluationSheet _sheet;
	/** corresponding cell from master workbook */
	private final EvaluationCell _masterCell;
	private boolean _booleanValue;
	private int _cellType;
	private int _errorValue;
	private double _numberValue;
	private String _stringValue;

	public ForkedEvaluationCell(ForkedEvaluationSheet sheet, EvaluationCell masterCell) {
		_sheet = sheet;
		_masterCell = masterCell;
		// start with value blank, but expect construction to be immediately
		setValue(BlankEval.INSTANCE); // followed by a proper call to setValue()
	}

	public Object getIdentityKey() {
		return _masterCell.getIdentityKey();
	}

	public void setValue(ValueEval value) {
		Class<? extends ValueEval> cls = value.getClass();

		if (cls == NumberEval.class) {
			_cellType = HSSFCell.CELL_TYPE_NUMERIC;
			_numberValue = ((NumberEval)value).getNumberValue();
			return;
		}
		if (cls == StringEval.class) {
			_cellType = HSSFCell.CELL_TYPE_STRING;
			_stringValue = ((StringEval)value).getStringValue();
			return;
		}
		if (cls == BoolEval.class) {
			_cellType = HSSFCell.CELL_TYPE_BOOLEAN;
			_booleanValue = ((BoolEval)value).getBooleanValue();
			return;
		}
		if (cls == ErrorEval.class) {
			_cellType = HSSFCell.CELL_TYPE_ERROR;
			_errorValue = ((ErrorEval)value).getErrorCode();
			return;
		}
		if (cls == BlankEval.class) {
			_cellType = HSSFCell.CELL_TYPE_BLANK;
			return;
		}
		throw new IllegalArgumentException("Unexpected value class (" + cls.getName() + ")");
	}
	public void copyValue(Cell destCell) {
		switch (_cellType) {
			case Cell.CELL_TYPE_BLANK:   destCell.setCellType(Cell.CELL_TYPE_BLANK);    return;
			case Cell.CELL_TYPE_NUMERIC: destCell.setCellValue(_numberValue);           return;
			case Cell.CELL_TYPE_BOOLEAN: destCell.setCellValue(_booleanValue);          return;
			case Cell.CELL_TYPE_STRING:  destCell.setCellValue(_stringValue);           return;
			case Cell.CELL_TYPE_ERROR:   destCell.setCellErrorValue((byte)_errorValue); return;
		}
		throw new IllegalStateException("Unexpected data type (" + _cellType + ")");
	}

	private void checkCellType(int expectedCellType) {
		if (_cellType != expectedCellType) {
			throw new RuntimeException("Wrong data type (" + _cellType + ")");
		}
	}
	public int getCellType() {
		return _cellType;
	}
	public boolean getBooleanCellValue() {
		checkCellType(HSSFCell.CELL_TYPE_BOOLEAN);
		return _booleanValue;
	}
	public int getErrorCellValue() {
		checkCellType(HSSFCell.CELL_TYPE_ERROR);
		return _errorValue;
	}
	public double getNumericCellValue() {
		checkCellType(HSSFCell.CELL_TYPE_NUMERIC);
		return _numberValue;
	}
	public String getStringCellValue() {
		checkCellType(HSSFCell.CELL_TYPE_STRING);
		return _stringValue;
	}
	public EvaluationSheet getSheet() {
		return _sheet;
	}
	public int getRowIndex() {
		return _masterCell.getRowIndex();
	}
	public int getColumnIndex() {
		return _masterCell.getColumnIndex();
	}
}
