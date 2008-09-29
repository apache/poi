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

package org.apache.poi.hssf.record.formula;

import org.apache.poi.ss.formula.FormulaRenderingWorkbook;
import org.apache.poi.ss.formula.EvaluationWorkbook.ExternalSheet;

/**
 * @author Josh Micich
 */
final class ExternSheetNameResolver {

	private ExternSheetNameResolver() {
		// no instances of this class
	}

	public static String prependSheetName(FormulaRenderingWorkbook book, int field_1_index_extern_sheet, String cellRefText) {
		ExternalSheet externalSheet = book.getExternalSheet(field_1_index_extern_sheet);
		StringBuffer sb;
		if (externalSheet != null) {
			String wbName = externalSheet.getWorkbookName();
			String sheetName = externalSheet.getSheetName();
			sb = new StringBuffer(wbName.length() + sheetName.length() + cellRefText.length() + 4);
			SheetNameFormatter.appendFormat(sb, wbName, sheetName);
		} else {
			String sheetName = book.getSheetNameByExternSheet(field_1_index_extern_sheet);
			sb = new StringBuffer(sheetName.length() + cellRefText.length() + 4);
			if (sheetName.length() < 1) {
				// What excel does if sheet has been deleted
				sb.append("#REF"); // note - '!' added just once below
			} else {
				SheetNameFormatter.appendFormat(sb, sheetName);
			}
		}
   		sb.append('!');
		sb.append(cellRefText);
		return sb.toString();
	}
}
