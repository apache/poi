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

import org.apache.poi.hssf.HSSFITestDataProvider;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.ss.usermodel.BaseTestSheetShiftRows;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

final class TestHSSFSheetShiftRows extends BaseTestSheetShiftRows {

    public TestHSSFSheetShiftRows() {
        super(HSSFITestDataProvider.instance);
    }

    @Test
    public void testBug69021() throws IOException {
        try (HSSFWorkbook workbook = HSSFTestDataSamples.openSampleWorkbook("bug69021.xls")) {
            Sheet sheet = workbook.getSheetAt(0);
            int rowIndex = 2;
            sheet.shiftRows(rowIndex, sheet.getLastRowNum(), 1);
            Row row = sheet.createRow(rowIndex);
            row.createCell(0).setCellValue("switch");
            HSSFWorkbook wbBack = HSSFTestDataSamples.writeOutAndReadBack(workbook);
            assertNotNull(wbBack);
        }
    }
}
