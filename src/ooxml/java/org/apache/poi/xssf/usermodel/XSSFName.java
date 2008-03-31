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
package org.apache.poi.xssf.usermodel;

import org.apache.poi.ss.usermodel.Name;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDefinedName;

/**
 * XSSF Implementation of a Named Range
 */
public class XSSFName implements Name {
	private XSSFWorkbook workbook;
	private CTDefinedName ctName;
	
	protected XSSFName(XSSFWorkbook workbook) {
		this.workbook = workbook;
		this.ctName = CTDefinedName.Factory.newInstance();
	}
	protected XSSFName(CTDefinedName name, XSSFWorkbook workbook) {
		this.workbook = workbook;
		this.ctName = name;
	}

	/**
	 * Returns the underlying named range object
	 */
	protected CTDefinedName getCTName() {
		return ctName;
	}
	
	public String getNameName() {
		return ctName.getName();
	}
	public void setNameName(String nameName) {
		ctName.setName(nameName);
	}

	public String getReference() {
		return ctName.getStringValue();
	}
	public void setReference(String ref) {
		ctName.setStringValue(ref);
	}
	
	public String getSheetName() {
		long sheetId = ctName.getLocalSheetId();
		if(sheetId >= 0) {
			return workbook.getSheetName((int)sheetId);
		}
		return null;
	}

	public String getComment() {
		return ctName.getComment();
	}
	public void setComment(String comment) {
		ctName.setComment(comment);
	}
}
