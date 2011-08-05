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

package org.apache.poi.xssf.usermodel.streaming;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.SXSSFITestDataProvider;

/**
 *
 */
public class TestSXSSFCell extends BaseTestCell {

    public TestSXSSFCell() {
        super(SXSSFITestDataProvider.instance);
    }

    /**
     * this test involves evaluation of formulas which isn't supported for SXSSF
     */
    @Override
    public void testConvertStringFormulaCell() {
        try {
            super.testConvertStringFormulaCell();
            fail("expected exception");
        } catch (IllegalArgumentException e){
            assertEquals(
                    "Unexpected type of cell: class org.apache.poi.xssf.streaming.SXSSFCell. " +
                    "Only XSSFCells can be evaluated.", e.getMessage());
        }
    }

    /**
     * this test involves evaluation of formulas which isn't supported for SXSSF
     */
    @Override
    public void testSetTypeStringOnFormulaCell() {
        try {
            super.testSetTypeStringOnFormulaCell();
            fail("expected exception");
        } catch (IllegalArgumentException e){
            assertEquals(
                    "Unexpected type of cell: class org.apache.poi.xssf.streaming.SXSSFCell. " +
                    "Only XSSFCells can be evaluated.", e.getMessage());
        }
    }

    public void testXmlEncoding(){
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet sh = wb.createSheet();
        Row row = sh.createRow(0);
        Cell cell = row.createCell(0);
        String sval = "<>\t\r\n\u00a0 &\"POI\'\u2122";
        cell.setCellValue(sval);

        wb = _testDataProvider.writeOutAndReadBack(wb);

        assertEquals(sval, wb.getSheetAt(0).getRow(0).getCell(0).getStringCellValue());

    }
}
