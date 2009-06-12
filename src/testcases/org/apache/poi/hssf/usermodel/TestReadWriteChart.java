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

import java.util.GregorianCalendar;
import java.util.List;

import junit.framework.TestCase;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.model.Sheet;
import org.apache.poi.hssf.record.BOFRecord;
import org.apache.poi.hssf.record.EOFRecord;

/**
 * @author Glen Stampoultzis (glens at apache.org)
 */
public final class TestReadWriteChart extends TestCase {

    /**
     * In the presence of a chart we need to make sure BOF/EOF records still exist.
     */
    public void testBOFandEOFRecords() {
        HSSFWorkbook workbook  = HSSFTestDataSamples.openSampleWorkbook("SimpleChart.xls");
        HSSFSheet       sheet     = workbook.getSheetAt(0);
        HSSFRow         firstRow  = sheet.getRow(0);
        HSSFCell        firstCell = firstRow.getCell(0);

        //System.out.println("first assertion for date");
        assertEquals(new GregorianCalendar(2000, 0, 1, 10, 51, 2).getTime(),
                     HSSFDateUtil
                         .getJavaDate(firstCell.getNumericCellValue(), false));
        HSSFRow  row  = sheet.createRow(15);
        HSSFCell cell = row.createCell(1);

        cell.setCellValue(22);
        Sheet newSheet = workbook.getSheetAt(0).getSheet();
        List  records  = newSheet.getRecords();

        assertTrue(records.get(0) instanceof BOFRecord);
        assertTrue(records.get(records.size() - 1) instanceof EOFRecord);
    }
}
