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

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.LocaleUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

import java.io.IOException;
import java.util.TimeZone;

import static org.apache.poi.xssf.XSSFTestDataSamples.openSampleWorkbook;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Isolated // modifies the default locale and we don't want to affect other tests running in parallel
public class TestXSSFWithLocale {
    /**
     * ISO-8601 style cell formats with a T in them, eg
     * cell format of "yyyy-MM-ddTHH:mm:ss"
     */
    @Test
    void bug54034() throws IOException {
        TimeZone tz = LocaleUtil.getUserTimeZone();
        LocaleUtil.setUserTimeZone(TimeZone.getTimeZone("CET"));
        try (Workbook wb = openSampleWorkbook("54034.xlsx")) {
            Sheet sheet = wb.getSheet("Sheet1");
            Row row = sheet.getRow(1);
            Cell cell = row.getCell(2);
            assertTrue(DateUtil.isCellDateFormatted(cell));

            DataFormatter fmt = new DataFormatter();
            assertEquals("yyyy\\-mm\\-dd\\Thh:mm", cell.getCellStyle().getDataFormatString());
            assertEquals("2012-08-08T22:59", fmt.formatCellValue(cell));
        } finally {
            LocaleUtil.setUserTimeZone(tz);
        }
    }
}
