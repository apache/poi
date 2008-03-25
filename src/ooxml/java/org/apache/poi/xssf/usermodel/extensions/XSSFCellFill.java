package org.apache.poi.xssf.usermodel.extensions;

import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFill;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTPatternFill;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STPatternType.Enum;

public class XSSFCellFill {
	
	private CTFill fill;
	
	public XSSFCellFill(CTFill fill) {
		this.fill = fill;
	}
	
	public XSSFCellFill() {
		this.fill = CTFill.Factory.newInstance();
	}
	
	public XSSFColor getFillBackgroundColor() {
		return new XSSFColor(getPatternFill().getBgColor());
	}

	public XSSFColor getFillForegroundColor() {
		return new XSSFColor(getPatternFill().getFgColor());
	}

	public Enum getPatternType() {
		return getPatternFill().getPatternType();
	}

	private CTPatternFill getPatternFill() {
		CTPatternFill patternFill = fill.getPatternFill();
		if (patternFill == null) {
			patternFill = fill.addNewPatternFill();
		}
		return patternFill;
	}

	public CTFill getCTFill() {
		return this.fill;
	}

}
