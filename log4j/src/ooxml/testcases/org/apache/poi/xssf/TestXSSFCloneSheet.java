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

package org.apache.poi.xssf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import org.apache.poi.hssf.HSSFITestDataProvider;
import org.apache.poi.ss.usermodel.BaseTestCloneSheet;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class TestXSSFCloneSheet  extends BaseTestCloneSheet {
    public TestXSSFCloneSheet() {
        super(HSSFITestDataProvider.instance);
    }

    private static final String OTHER_SHEET_NAME = "Another";
    private static final String VALID_SHEET_NAME = "Sheet01";
    private XSSFWorkbook wb;

    @BeforeEach
    void setUp() {
        wb = new XSSFWorkbook();
        wb.createSheet(VALID_SHEET_NAME);
    }

    @Test
    void testCloneSheetIntStringValidName() {
        XSSFSheet cloned = wb.cloneSheet(0, OTHER_SHEET_NAME);
        assertEquals(OTHER_SHEET_NAME, cloned.getSheetName());
        assertEquals(2, wb.getNumberOfSheets());
    }

    @Test
    void testCloneSheetIntStringInvalidName() {
        assertThrows(IllegalArgumentException.class, () -> wb.cloneSheet(0, VALID_SHEET_NAME));
        assertEquals(1, wb.getNumberOfSheets());
    }

    @Test
    void test60512() throws IOException {
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("60512.xlsm");

        assertEquals(1, wb.getNumberOfSheets());
        Sheet sheet = wb.cloneSheet(0);
        assertNotNull(sheet);
        assertEquals(2, wb.getNumberOfSheets());


        Workbook wbBack = XSSFTestDataSamples.writeOutAndReadBack(wb);
        assertNotNull(wbBack);
        wbBack.close();

        wb.close();
    }

    @Test
    void test61605() throws IOException {
        try (Workbook template_wb = XSSFTestDataSamples.openSampleWorkbook("61605.xlsx")) {
            Sheet template_sh = template_wb.getSheetAt(0);
            assertNotNull(template_sh);
            Sheet source_sh = template_wb.cloneSheet(0);
            assertNotNull(source_sh);
        }
    }
}
