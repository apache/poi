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

package org.apache.poi.ss.usermodel;

import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.hssf.usermodel.*;

/**
 * This is a JDK 1.4 compatible interface for HSSFWorkbook.
 * If you are using JDK 1.5 or later, use the other set of interfaces,
 *  which work properly for both HSSFWorkbook and XSSFWorkbook
 */
public interface Workbook {

    int getActiveSheetIndex();
    void setActiveSheet(int sheetIndex);

    int getFirstVisibleTab();
    void setFirstVisibleTab(int sheetIndex);

    int getNumberOfSheets();
    short getNumberOfFonts();
    int getNumberOfNames();

    HSSFName createName();
    HSSFName getNameAt(int index);
    int getNameIndex(String name);
    String getNameName(int index);

    String getSheetName(int sheet);
    HSSFSheet getSheetAt(int index);
    int getSheetIndex(String name);
    int getSheetIndex(Sheet sheet);

    CreationHelper getCreationHelper();

	/**
	 * Retrieves the current policy on what to do when
	 *  getting missing or blank cells from a row.
	 * The default is to return blank and null cells.
	 *  {@link MissingCellPolicy}
	 */
	MissingCellPolicy getMissingCellPolicy();
	/**
	 * Sets the policy on what to do when
	 *  getting missing or blank cells from a row.
	 * This will then apply to all calls to 
	 *  {@link Row.getCell()}. See
	 *  {@link MissingCellPolicy}
	 */
	void setMissingCellPolicy(MissingCellPolicy missingCellPolicy);
}
