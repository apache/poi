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

package org.apache.poi.hssf.usermodel;
import org.apache.poi.hssf.model.InternalSheet;
import org.apache.poi.hssf.model.InternalWorkbook;

/**
 * Helper class for HSSF tests that aren't within the
 *  HSSF UserModel package, but need to do internal
 *  UserModel things.
 */
public class HSSFTestHelper {
	/**
	 * Lets non UserModel tests at the low level Workbook
	 */
	public static InternalWorkbook getWorkbookForTest(HSSFWorkbook wb) {
		return wb.getWorkbook();
	}
	public static InternalSheet getSheetForTest(HSSFSheet sheet) {
		return sheet.getSheet();
	}
}
