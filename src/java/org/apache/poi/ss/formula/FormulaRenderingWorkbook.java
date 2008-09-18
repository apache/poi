package org.apache.poi.ss.formula;

import org.apache.poi.hssf.record.formula.NamePtg;
import org.apache.poi.hssf.record.formula.NameXPtg;

public interface FormulaRenderingWorkbook {

	String getSheetNameByExternSheet(int externSheetIndex);
	String resolveNameXText(NameXPtg nameXPtg);
	String getNameText(NamePtg namePtg);
}
