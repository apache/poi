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
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STPatternType;
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
        CTPatternFill ptrn = _fill.getPatternFill();
        if(ptrn == null) return null;

        CTColor ctColor = ptrn.getBgColor();
		return ctColor == null ? null : new XSSFColor(ctColor);
	}

    public void setFillBackgroundColor(int index) {
        CTPatternFill ptrn = ensureCTPatternFill();
        CTColor ctColor = ptrn.isSetBgColor() ? ptrn.getBgColor() : ptrn.addNewBgColor();
        ctColor.setIndexed(index);
    }

    public void setFillBackgroundColor(XSSFColor color) {
        CTPatternFill ptrn = ensureCTPatternFill();
        ptrn.setBgColor(color.getCTColor());
    }

    public XSSFColor getFillForegroundColor() {
        CTPatternFill ptrn = _fill.getPatternFill();
        if(ptrn == null) return null;

        CTColor ctColor = ptrn.getFgColor();
        return ctColor == null ? null : new XSSFColor(ctColor);
    }

    public void setFillForegroundColor(int index) {
        CTPatternFill ptrn = ensureCTPatternFill();
        CTColor ctColor = ptrn.isSetFgColor() ? ptrn.getFgColor() : ptrn.addNewFgColor();
        ctColor.setIndexed(index);
    }

    public void setFillForegroundColor(XSSFColor color) {
        CTPatternFill ptrn = ensureCTPatternFill();
        ptrn.setFgColor(color.getCTColor());
    }

	public STPatternType.Enum getPatternType() {
        CTPatternFill ptrn = _fill.getPatternFill();
		return ptrn == null ? null : ptrn.getPatternType();
	}

    public void setPatternType(STPatternType.Enum patternType) {
        CTPatternFill ptrn = ensureCTPatternFill();
        ptrn.setPatternType(patternType);
    }

	private CTPatternFill ensureCTPatternFill() {
		CTPatternFill patternFill = _fill.getPatternFill();
		if (patternFill == null) {
			patternFill = _fill.addNewPatternFill();
		}
		return patternFill;
	}

	public CTFill getCTFill() {
		return _fill;
	}
	
    public int hashCode(){
        return _fill.toString().hashCode();
    }

    public boolean equals(Object o){
        if(!(o instanceof XSSFCellFill)) return false;

        XSSFCellFill cf = (XSSFCellFill)o;
        return _fill.toString().equals(cf.getCTFill().toString());
    }
}
