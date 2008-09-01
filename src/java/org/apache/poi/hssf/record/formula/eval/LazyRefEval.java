package org.apache.poi.hssf.record.formula.eval;

import org.apache.poi.hssf.record.formula.AreaI;
import org.apache.poi.hssf.record.formula.AreaI.OffsetArea;
import org.apache.poi.hssf.record.formula.Ref3DPtg;
import org.apache.poi.hssf.record.formula.RefPtg;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

public final class LazyRefEval extends RefEvalBase {

	private final HSSFSheet _sheet;
	private final HSSFWorkbook _workbook;


	public LazyRefEval(RefPtg ptg, HSSFSheet sheet, HSSFWorkbook workbook) {
		super(ptg.getRow(), ptg.getColumn());
		_sheet = sheet;
		_workbook = workbook;
	}
	public LazyRefEval(Ref3DPtg ptg, HSSFSheet sheet, HSSFWorkbook workbook) {
		super(ptg.getRow(), ptg.getColumn());
		_sheet = sheet;
		_workbook = workbook;
	}

	public ValueEval getInnerValueEval() {
		int rowIx = getRow();
		int colIx = getColumn();
		
		HSSFRow row = _sheet.getRow(rowIx);
		if (row == null) {
			return BlankEval.INSTANCE;
		}
		HSSFCell cell = row.getCell(colIx);
		if (cell == null) {
			return BlankEval.INSTANCE;
		}
		return HSSFFormulaEvaluator.getEvalForCell(cell, _sheet, _workbook);
	}
	
	public AreaEval offset(int relFirstRowIx, int relLastRowIx, int relFirstColIx, int relLastColIx) {
		
		AreaI area = new OffsetArea(getRow(), getColumn(),
				relFirstRowIx, relLastRowIx, relFirstColIx, relLastColIx);

		return new LazyAreaEval(area, _sheet, _workbook);
	}
}
