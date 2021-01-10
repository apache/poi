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

package org.apache.poi.hssf.record;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.InputStream;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.usermodel.DVConstraint;
import org.apache.poi.hssf.usermodel.HSSFDataValidation;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.junit.jupiter.api.Test;

/**
 * Verify that presence of PLV record doesn't break data
 * validation, bug #53972:
 * https://issues.apache.org/bugzilla/show_bug.cgi?id=53972
 */

final class TestPLVRecord {
    private final static String DV_DEFINITION = "$A$1:$A$5";
    private final static String XLS_FILENAME = "53972.xls";
    private final static String SHEET_NAME = "S2";

    @Test
    void testPLVRecord() throws Exception {
        try (InputStream is = HSSFTestDataSamples.openSampleFileStream(XLS_FILENAME);
             HSSFWorkbook workbook = new HSSFWorkbook(is)) {

            CellRangeAddressList cellRange = new CellRangeAddressList(0, 0, 1, 1);
            DataValidationConstraint constraint = DVConstraint.createFormulaListConstraint(DV_DEFINITION);
            HSSFDataValidation dataValidation = new HSSFDataValidation(cellRange, constraint);

            // This used to throw an IllegalStateException before
            // Identified bug 53972, PLV record breaks addDataValidation()
            HSSFSheet sheet = workbook.getSheet(SHEET_NAME);
            assertDoesNotThrow(() -> sheet.addValidationData(dataValidation));
        }
    }
}
