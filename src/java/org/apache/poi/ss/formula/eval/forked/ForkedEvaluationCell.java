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

import org.apache.poi.ss.formula.EvaluationCell;
import org.apache.poi.ss.formula.EvaluationSheet;
import org.apache.poi.ss.formula.eval.BlankEval;
import org.apache.poi.ss.formula.eval.BoolEval;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.StringEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.Removal;


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
	private CellType _cellType;
	private int _errorValue;
	private double _numberValue;
	private String _stringValue;

	public ForkedEvaluationCell(ForkedEvaluationSheet sheet, EvaluationCell masterCell) {
		_sheet = sheet;
		_masterCell = masterCell;
		// start with value blank, but expect construction to be immediately
		setValue(BlankEval.instance); // followed by a proper call to setValue()
	}

	@Override
	public Object getIdentityKey() {
		return _masterCell.getIdentityKey();
	}

	public void setValue(ValueEval value) {
		Class<? extends ValueEval> cls = value.getClass();

		if (cls == NumberEval.class) {
			_cellType = CellType.NUMERIC;
			_numberValue = ((NumberEval)value).getNumberValue();
			return;
		}
		if (cls == StringEval.class) {
			_cellType = CellType.STRING;
			_stringValue = ((StringEval)value).getStringValue();
			return;
		}
		if (cls == BoolEval.class) {
			_cellType = CellType.BOOLEAN;
			_booleanValue = ((BoolEval)value).getBooleanValue();
			return;
		}
		if (cls == ErrorEval.class) {
			_cellType = CellType.ERROR;
			_errorValue = ((ErrorEval)value).getErrorCode();
			return;
		}
		if (cls == BlankEval.class) {
			_cellType = CellType.BLANK;
			return;
		}
		throw new IllegalArgumentException("Unexpected value class (" + cls.getName() + ")");
	}
	public void copyValue(Cell destCell) {
		switch (_cellType) {
			case BLANK:   destCell.setBlank();                           return;
			case NUMERIC: destCell.setCellValue(_numberValue);           return;
			case BOOLEAN: destCell.setCellValue(_booleanValue);          return;
			case STRING:  destCell.setCellValue(_stringValue);           return;
			case ERROR:   destCell.setCellErrorValue((byte)_errorValue); return;
			default: throw new IllegalStateException("Unexpected data type (" + _cellType + ")");
		}
	}

	private void checkCellType(CellType expectedCellType) {
		if (_cellType != expectedCellType) {
			throw new RuntimeException("Wrong data type (" + _cellType + ")");
		}
	}

	@Override
	public CellType getCellType() {
		return _cellType;
	}
	/**
	 * @since POI 3.15 beta 3
	 * @deprecated POI 3.15 beta 3.
	 * Will be deleted when we make the CellType enum transition. See bug 59791.
	 */
	@Deprecated
    @Removal(version = "4.2")
	@Override
	public CellType getCellTypeEnum() {
		return getCellType();
	}
	@Override
	public boolean getBooleanCellValue() {
		checkCellType(CellType.BOOLEAN);
		return _booleanValue;
	}
	@Override
	public int getErrorCellValue() {
		checkCellType(CellType.ERROR);
		return _errorValue;
	}
	@Override
	public double getNumericCellValue() {
		checkCellType(CellType.NUMERIC);
		return _numberValue;
	}
	@Override
	public String getStringCellValue() {
		checkCellType(CellType.STRING);
		return _stringValue;
	}
	@Override
	public EvaluationSheet getSheet() {
		return _sheet;
	}
	@Override
	public int getRowIndex() {
		return _masterCell.getRowIndex();
	}
	@Override
	public int getColumnIndex() {
		return _masterCell.getColumnIndex();
	}
	
	@Override
	public CellRangeAddress getArrayFormulaRange() {
		return _masterCell.getArrayFormulaRange();
	}
	
	@Override
	public boolean isPartOfArrayFormulaGroup() {
		return _masterCell.isPartOfArrayFormulaGroup();
	}
	/**
	 * @return cell type of cached formula result
	 */
	@Override
	public CellType getCachedFormulaResultType() {
		return _masterCell.getCachedFormulaResultType();
	}

	/**
	 * @since POI 3.15 beta 3
	 * @deprecated POI 3.15 beta 3.
	 * Will be deleted when we make the CellType enum transition. See bug 59791.
	 */
	@Deprecated
	@Removal(version = "4.2")
	@Override
	public CellType getCachedFormulaResultTypeEnum() {
		return getCachedFormulaResultType();
	}

}
