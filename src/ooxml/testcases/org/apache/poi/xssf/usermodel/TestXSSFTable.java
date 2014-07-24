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
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.util.TempFile;
import org.junit.Test;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTable;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableColumn;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public final class TestXSSFTable {

    public TestXSSFTable() {
    }

    @Test
    public void bug56274() throws IOException {
        // read sample file
        XSSFWorkbook inputWorkbook = XSSFTestDataSamples.openSampleWorkbook("56274.xlsx");

        // read the original sheet header order
        XSSFRow row = inputWorkbook.getSheetAt(0).getRow(0);
        List<String> headers = new ArrayList<String>();
        for (Cell cell : row) {
            headers.add(cell.getStringCellValue());
        }

        // save the worksheet as-is using SXSSF
        File outputFile = TempFile.createTempFile("poi-56274", ".xlsx");
        SXSSFWorkbook outputWorkbook = new org.apache.poi.xssf.streaming.SXSSFWorkbook(inputWorkbook);
        outputWorkbook.write(new FileOutputStream(outputFile));

        // re-read the saved file and make sure headers in the xml are in the original order
        inputWorkbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook(new FileInputStream(outputFile));
        CTTable ctTable = inputWorkbook.getSheetAt(0).getTables().get(0).getCTTable();
        List<CTTableColumn> ctTableColumnList = ctTable.getTableColumns().getTableColumnList();

        assertEquals("number of headers in xml table should match number of header cells in worksheet",
                headers.size(), ctTableColumnList.size());
        for (int i = 0; i < headers.size(); i++) {
            assertEquals("header name in xml table should match number of header cells in worksheet",
                    headers.get(i), ctTableColumnList.get(i).getName());
        }
        assertTrue(outputFile.delete());
    }

}