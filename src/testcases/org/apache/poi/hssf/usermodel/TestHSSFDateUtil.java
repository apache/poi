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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.TimeZone;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.model.InternalWorkbook;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.util.LocaleUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Class TestHSSFDateUtil
 */
class TestHSSFDateUtil {

    static TimeZone userTimeZone;

    @BeforeAll
    public static void setCEST() {
        userTimeZone = LocaleUtil.getUserTimeZone();
        LocaleUtil.setUserTimeZone(TimeZone.getTimeZone("CEST"));
    }

    @AfterAll
    public static void resetTimeZone() {
        LocaleUtil.setUserTimeZone(userTimeZone);
    }

    /**
     * Test that against a real, test file, we still do everything
     *  correctly
     */
    @Test
    void onARealFile() throws IOException {

        HSSFWorkbook workbook = HSSFTestDataSamples.openSampleWorkbook("DateFormats.xls");
        HSSFSheet sheet       = workbook.getSheetAt(0);
        InternalWorkbook wb           = workbook.getWorkbook();
        assertNotNull(wb);

        HSSFRow  row;
        HSSFCell cell;
        HSSFCellStyle style;

        double aug_10_2007 = 39304.0;

        // Should have dates in 2nd column
        // All of them are the 10th of August
        // 2 US dates, 3 UK dates
        row  = sheet.getRow(0);
        cell = row.getCell(1);
        style = cell.getCellStyle();
        assertEquals(aug_10_2007, cell.getNumericCellValue(), 0.0001);
        assertEquals("d-mmm-yy", style.getDataFormatString());
        assertTrue(DateUtil.isInternalDateFormat(style.getDataFormat()));
        assertTrue(DateUtil.isADateFormat(style.getDataFormat(), style.getDataFormatString()));
        assertTrue(DateUtil.isCellDateFormatted(cell));

        row  = sheet.getRow(1);
        cell = row.getCell(1);
        style = cell.getCellStyle();
        assertEquals(aug_10_2007, cell.getNumericCellValue(), 0.0001);
        assertFalse(DateUtil.isInternalDateFormat(cell.getCellStyle().getDataFormat()));
        assertTrue(DateUtil.isADateFormat(style.getDataFormat(), style.getDataFormatString()));
        assertTrue(DateUtil.isCellDateFormatted(cell));

        row  = sheet.getRow(2);
        cell = row.getCell(1);
        style = cell.getCellStyle();
        assertEquals(aug_10_2007, cell.getNumericCellValue(), 0.0001);
        assertTrue(DateUtil.isInternalDateFormat(cell.getCellStyle().getDataFormat()));
        assertTrue(DateUtil.isADateFormat(style.getDataFormat(), style.getDataFormatString()));
        assertTrue(DateUtil.isCellDateFormatted(cell));

        row  = sheet.getRow(3);
        cell = row.getCell(1);
        style = cell.getCellStyle();
        assertEquals(aug_10_2007, cell.getNumericCellValue(), 0.0001);
        assertFalse(DateUtil.isInternalDateFormat(cell.getCellStyle().getDataFormat()));
        assertTrue(DateUtil.isADateFormat(style.getDataFormat(), style.getDataFormatString()));
        assertTrue(DateUtil.isCellDateFormatted(cell));

        row  = sheet.getRow(4);
        cell = row.getCell(1);
        style = cell.getCellStyle();
        assertEquals(aug_10_2007, cell.getNumericCellValue(), 0.0001);
        assertFalse(DateUtil.isInternalDateFormat(cell.getCellStyle().getDataFormat()));
        assertTrue(DateUtil.isADateFormat(style.getDataFormat(), style.getDataFormatString()));
        assertTrue(DateUtil.isCellDateFormatted(cell));

        workbook.close();
    }
}
