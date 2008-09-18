package org.apache.poi.ss.formula;

public interface WorkbookDependentFormula {
	String toFormulaString(FormulaRenderingWorkbook book);
}
