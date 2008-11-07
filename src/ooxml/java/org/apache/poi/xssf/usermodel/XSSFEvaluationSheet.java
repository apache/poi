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

import org.apache.poi.ss.formula.EvaluationCell;
import org.apache.poi.ss.formula.EvaluationSheet;

/**
 * XSSF wrapper for a sheet under evaluation
 * 
 * @author Josh Micich
 */
final class XSSFEvaluationSheet implements EvaluationSheet {

	private final XSSFSheet _xs;

	public XSSFEvaluationSheet(XSSFSheet sheet) {
		_xs = sheet;
	}

	public XSSFSheet getXSSFSheet() {
		return _xs;
	}
	public EvaluationCell getCell(int rowIndex, int columnIndex) {
		XSSFRow row = _xs.getRow(rowIndex);
		if (row == null) {
			return null;
		}
		XSSFCell cell = row.getCell(columnIndex);
		if (cell == null) {
			return null;
		}
		return new XSSFEvaluationCell(cell, this);
	}
}
