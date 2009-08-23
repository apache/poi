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

package org.apache.poi.xssf.io;

import java.io.File;
import java.util.List;

import junit.framework.TestCase;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFPictureData;
import org.apache.poi.xssf.XSSFITestDataProvider;
import org.apache.poi.POIDataSamples;


public class TestLoadSaveXSSF extends TestCase {
    private static final POIDataSamples _ssSampels = POIDataSamples.getSpreadSheetInstance();

    public void testLoadSample() throws Exception {
        XSSFWorkbook workbook = new XSSFWorkbook(_ssSampels.openResourceAsStream("sample.xlsx"));
        assertEquals(3, workbook.getNumberOfSheets());
        assertEquals("Sheet1", workbook.getSheetName(0));
        Sheet sheet = workbook.getSheetAt(0);
        Row row = sheet.getRow(0);
        Cell cell = row.getCell((short) 1);
        assertNotNull(cell);
        assertEquals(111.0, cell.getNumericCellValue(), 0.0);
        cell = row.getCell((short) 0);
        assertEquals("Lorem", cell.getRichStringCellValue().getString());
    }

    // TODO filename string hard coded in XSSFWorkbook constructor in order to make ant test-ooxml target be successful.
    public void testLoadStyles() throws Exception {
        XSSFWorkbook workbook = new XSSFWorkbook(_ssSampels.openResourceAsStream("styles.xlsx"));
        Sheet sheet = workbook.getSheetAt(0);
        Row row = sheet.getRow(0);
        Cell cell = row.getCell((short) 0);
        CellStyle style = cell.getCellStyle();
        // assertNotNull(style);
    }

    // TODO filename string hard coded in XSSFWorkbook constructor in order to make ant test-ooxml target be successful.
    public void testLoadPictures() throws Exception {
        XSSFWorkbook workbook = new XSSFWorkbook(_ssSampels.openResourceAsStream("picture.xlsx"));
        List<XSSFPictureData> pictures = workbook.getAllPictures();
        assertEquals(1, pictures.size());
    }
}
