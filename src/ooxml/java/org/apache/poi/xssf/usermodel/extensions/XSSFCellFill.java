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

import java.util.LinkedList;

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
	
	public long putFill(LinkedList<CTFill> fills) {
		if (fills.contains(fill)) {
			return fills.indexOf(fill);
		}
		fills.add(fill);
		return fills.size() - 1;
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
