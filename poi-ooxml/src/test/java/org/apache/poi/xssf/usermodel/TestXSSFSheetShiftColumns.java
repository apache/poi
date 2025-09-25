/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */
package org.apache.poi.xssf.usermodel;

import java.io.IOException;

import org.apache.poi.ss.usermodel.BaseTestSheetShiftColumns;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.XSSFITestDataProvider;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestXSSFSheetShiftColumns extends BaseTestSheetShiftColumns {
    public TestXSSFSheetShiftColumns() {
        super(); 
        workbook = new XSSFWorkbook();
        _testDataProvider = XSSFITestDataProvider.instance; 
    }

    protected Workbook openWorkbook(String spreadsheetFileName) throws IOException {
        return XSSFTestDataSamples.openSampleWorkbook(spreadsheetFileName);
    }

    protected Workbook getReadBackWorkbook(Workbook wb) {
        return XSSFTestDataSamples.writeOutAndReadBack(wb);
    }

    @Test
    public void testBug69154() throws Exception {
        // this does not appear to work for HSSF but let's get it working for XSSF anyway
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Sheet sheet = wb.createSheet();
            for (int i = 0; i < 4; i++) {
                Row row = sheet.createRow(i);
                for (int j = 0; j < 6; j++) {
                    String value = new CellAddress(i, j).formatAsString();
                    row.createCell(j).setCellValue(value);
                }
            }
            final int firstRow = 1; // worked with 0, but failed with 1!
            final int secondRow = firstRow + 1;
            sheet.addMergedRegion(new CellRangeAddress(firstRow, secondRow, 0, 0));
            sheet.addMergedRegion(new CellRangeAddress(firstRow, firstRow, 1, 2));
            sheet.addMergedRegion(new CellRangeAddress(firstRow, secondRow, 3, 3));
            assertEquals(3, sheet.getNumMergedRegions());
            sheet.shiftColumns(2, 5, -1);
            assertEquals(2, sheet.getNumMergedRegions());
        }
    }
}