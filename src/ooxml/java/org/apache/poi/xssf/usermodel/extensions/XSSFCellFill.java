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
package org.apache.poi.xssf.usermodel.extensions;

import java.util.List;

import org.apache.poi.xssf.usermodel.IndexedColors;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTColor;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFill;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTPatternFill;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STPatternType.Enum;

public final class XSSFCellFill {
	
	private CTFill _fill;
	
	public XSSFCellFill(CTFill fill) {
		_fill = fill;
	}
	
	public XSSFCellFill() {
		_fill = CTFill.Factory.newInstance();
	}
	
	public XSSFColor getFillBackgroundColor() {
		CTColor ctColor = getPatternFill().getBgColor();
		if (ctColor == null) {
			XSSFColor result = new XSSFColor();
			result.setIndexed(IndexedColors.AUTOMATIC.getIndex());
			return result;
		}
		return new XSSFColor(ctColor);
	}

	public XSSFColor getFillForegroundColor() {
		CTColor ctColor = getPatternFill().getFgColor();
		if (ctColor == null) {
			XSSFColor result = new XSSFColor();
			result.setIndexed(IndexedColors.AUTOMATIC.getIndex());
			return result;
		}
		return new XSSFColor(ctColor);
	}

	public Enum getPatternType() {
		return getPatternFill().getPatternType();
	}
	
	/**
	 * @return the index of the just added fill
	 */
	public int putFill(List<CTFill> fills) {
		if (fills.contains(_fill)) {
			return fills.indexOf(_fill);
		}
		fills.add(_fill);
		return fills.size() - 1;
	}

	private CTPatternFill getPatternFill() {
		CTPatternFill patternFill = _fill.getPatternFill();
		if (patternFill == null) {
			patternFill = _fill.addNewPatternFill();
		}
		return patternFill;
	}

	public CTFill getCTFill() {
		return _fill;
	}
	
	public void setFillBackgroundColor(long index) {
		CTColor ctColor=getPatternFill().addNewBgColor();
		ctColor.setIndexed(index);
		_fill.getPatternFill().setBgColor(ctColor);
	}

	public void setFillForegroundColor(long index) {
		CTColor ctColor=getPatternFill().addNewFgColor();
		ctColor.setIndexed(index);
		_fill.getPatternFill().setFgColor(ctColor);
	}
	
	public void setFillBackgroundRgbColor(XSSFColor color) {
		_fill.getPatternFill().setBgColor(color.getCTColor());
	}

	public void setFillForegroundRgbColor(XSSFColor color) {
		_fill.getPatternFill().setFgColor(color.getCTColor());
	}
	
	public void setPatternType(Enum patternType) {
		getPatternFill().setPatternType(patternType);
	}
}
