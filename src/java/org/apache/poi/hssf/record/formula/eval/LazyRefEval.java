package org.apache.poi.hssf.record.formula.eval;

import org.apache.poi.hssf.record.formula.AreaI;
import org.apache.poi.hssf.record.formula.AreaI.OffsetArea;
import org.apache.poi.hssf.record.formula.Ref3DPtg;
import org.apache.poi.hssf.record.formula.RefPtg;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public final class LazyRefEval extends RefEvalBase {

	private final Sheet _sheet;
	private final Workbook _workbook;


	public LazyRefEval(RefPtg ptg, Sheet sheet, Workbook workbook) {
		super(ptg.getRow(), ptg.getColumn());
		_sheet = sheet;
		_workbook = workbook;
	}
	public LazyRefEval(Ref3DPtg ptg, Sheet sheet, Workbook workbook) {
		super(ptg.getRow(), ptg.getColumn());
		_sheet = sheet;
		_workbook = workbook;
	}

	public ValueEval getInnerValueEval() {
		int rowIx = getRow();
		int colIx = getColumn();
		
		Row row = _sheet.getRow(rowIx);
		if (row == null) {
			return BlankEval.INSTANCE;
		}
		Cell cell = row.getCell(colIx);
		if (cell == null) {
			return BlankEval.INSTANCE;
		}
		return FormulaEvaluator.getEvalForCell(cell, _sheet, _workbook);
	}
	
	public AreaEval offset(int relFirstRowIx, int relLastRowIx, int relFirstColIx, int relLastColIx) {
		
		AreaI area = new OffsetArea(getRow(), getColumn(),
				relFirstRowIx, relLastRowIx, relFirstColIx, relLastColIx);

		return new LazyAreaEval(area, _sheet, _workbook);
	}
}
