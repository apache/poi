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

import org.apache.poi.ss.usermodel.Workbook;

/**
 * @author Josh Micich
 */
final class ExternSheetNameResolver {

	private ExternSheetNameResolver() {
		// no instances of this class
	}

	public static String prependSheetName(Workbook book, int field_1_index_extern_sheet, String cellRefText) {
		String sheetName = book.findSheetNameFromExternSheet(field_1_index_extern_sheet);
		StringBuffer sb = new StringBuffer(sheetName.length() + cellRefText.length() + 4);
		if (sheetName.length() < 1) {
			// What excel does if sheet has been deleted
			sb.append("#REF"); // note - '!' added just once below
		} else {
			SheetNameFormatter.appendFormat(sb, sheetName);
		}
			sb.append('!');
		sb.append(cellRefText);
		return sb.toString();
	}
}
