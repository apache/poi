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

package org.apache.poi.ss.tests.usermodel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.apache.poi.ss.usermodel.BaseTestRangeCopier;
import org.apache.poi.ss.usermodel.CellCopyPolicy;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.XSSFITestDataProvider;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xssf.usermodel.XSSFRangeCopier;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestXSSFRangeCopier extends BaseTestRangeCopier {
    public TestXSSFRangeCopier() {
        super();
        workbook = new XSSFWorkbook();
        testDataProvider = XSSFITestDataProvider.instance;
    }

    @BeforeEach
    void init() {
        workbook = XSSFTestDataSamples.openSampleWorkbook("tile-range-test.xlsx");
        initSheets();
        rangeCopier = new XSSFRangeCopier(sheet1, sheet1);
        transSheetRangeCopier = new XSSFRangeCopier(sheet1, sheet2);
    }

    @AfterEach
    void shutdown() throws IOException {
        workbook.close();
    }

    @Test // XSSF only. HSSF version wouldn't be so simple. And also this test is contained in following, more complex tests, so it's not really important.
    void copyRow() {
        Row existingRow = sheet1.getRow(4);
        XSSFRow newRow = (XSSFRow)sheet1.getRow(5);
        CellCopyPolicy policy = new CellCopyPolicy();
        newRow.copyRowFrom(existingRow, policy);
        assertEquals("$C2+B$2", newRow.getCell(1).getCellFormula());
    }

}
