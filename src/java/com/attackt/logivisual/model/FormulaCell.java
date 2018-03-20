package com.attackt.logivisual.model;

import java.util.List;
/**
 * 单元格对象
 * 
 * @author hanbin
 *
 */
public class FormulaCell extends CellInfo{
	// 拆分信息集合
	private List<FormulaSplitInfo> values;

	public FormulaCell(String sheetName, int sheetIndex, String address, String cellValue) {
		super(sheetName, sheetIndex, address, cellValue);
	}

	public List<FormulaSplitInfo> getValues() {
		return values;
	}

	public void setValues(List<FormulaSplitInfo> values) {
		this.values = values;
	}

}
