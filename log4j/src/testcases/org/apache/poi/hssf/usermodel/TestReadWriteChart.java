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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.model.InternalSheet;
import org.apache.poi.hssf.record.BOFRecord;
import org.apache.poi.hssf.record.EOFRecord;
import org.apache.poi.hssf.record.RecordBase;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.util.LocaleUtil;
import org.junit.jupiter.api.Test;

/**
 * @author Glen Stampoultzis (glens at apache.org)
 */
final class TestReadWriteChart {

    /**
     * In the presence of a chart we need to make sure BOF/EOF records still exist.
     */
    @Test
    void testBOFandEOFRecords() throws Exception {
        HSSFWorkbook workbook  = HSSFTestDataSamples.openSampleWorkbook("SimpleChart.xls");
        HSSFSheet       sheet     = workbook.getSheetAt(0);
        HSSFRow         firstRow  = sheet.getRow(0);
        HSSFCell        firstCell = firstRow.getCell(0);

        //System.out.println("first assertion for date");
        Calendar calExp = LocaleUtil.getLocaleCalendar(2000, 0, 1, 10, 51, 2);
        Date dateAct = DateUtil.getJavaDate(firstCell.getNumericCellValue(), false);
        assertEquals(calExp.getTime(), dateAct);
        HSSFRow  row  = sheet.createRow(15);
        HSSFCell cell = row.createCell(1);

        cell.setCellValue(22);
        InternalSheet newSheet = workbook.getSheetAt(0).getSheet();
        List<RecordBase> records  = newSheet.getRecords();

        assertTrue(records.get(0) instanceof BOFRecord);
        assertTrue(records.get(records.size() - 1) instanceof EOFRecord);

        workbook.close();
    }
}
