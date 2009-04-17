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

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.XSSFITestDataProvider;

/**
 * @author Yegor Kozlov
 */
public final class TestXSSFCell extends BaseTestCell {

	public TestXSSFCell() {
		super(XSSFITestDataProvider.getInstance());
	}

    /**
     * Bug 47026: trouble changing cell type when workbook doesn't contain
     * Shared String Table
     */
    public void test47026_1() throws Exception {
        Workbook source = _testDataProvider.openSampleWorkbook("47026.xlsm");
        Sheet sheet = source.getSheetAt(0);
        Row row = sheet.getRow(0);
        Cell cell = row.getCell(0);
        cell.setCellType(Cell.CELL_TYPE_STRING);
        cell.setCellValue("456");
    }

    public void test47026_2() throws Exception {
        Workbook source = _testDataProvider.openSampleWorkbook("47026.xlsm");
        Sheet sheet = source.getSheetAt(0);
        Row row = sheet.getRow(0);
        Cell cell = row.getCell(0);
        cell.setCellFormula(null);
        cell.setCellValue("456");
    }
}