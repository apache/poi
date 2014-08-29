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

package org.apache.poi.xssf.model;

import static org.junit.Assert.assertEquals;

import java.io.FileOutputStream;

import org.apache.commons.codec.binary.Hex;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;

public class TestThemesTable {
    private String testFile = "Themes.xlsx";

    @Test
    public void testThemesTableColors() throws Exception {
        XSSFWorkbook workbook = XSSFTestDataSamples.openSampleWorkbook(testFile);
        String rgbExpected[] = {
            "ffffff", // Lt1
            "000000", // Dk1
            "eeece1", // Lt2
            "1f497d", // DK2
            "4f81bd", // Accent1
            "c0504d", // Accent2
            "9bbb59", // Accent3
            "8064a2", // Accent4
            "4bacc6", // Accent5
            "f79646", // Accent6
            "0000ff", // Hlink
            "800080"  // FolHlink
        };
        boolean createFile = false;
        int i=0;
        for (Row row : workbook.getSheetAt(0)) {
            XSSFFont font = ((XSSFRow)row).getCell(0).getCellStyle().getFont();
            XSSFColor color = font.getXSSFColor();
            assertEquals("Failed color theme "+i, rgbExpected[i], Hex.encodeHexString(color.getRgb()));
            long themeIdx = font.getCTFont().getColorArray(0).getTheme();
            assertEquals("Failed color theme "+i, i, themeIdx);
            if (createFile) {
                XSSFCellStyle cs = (XSSFCellStyle)row.getSheet().getWorkbook().createCellStyle();
                cs.setFillForegroundColor(color);
                cs.setFillPattern(CellStyle.SOLID_FOREGROUND);
                row.createCell(1).setCellStyle(cs);
            }
            i++;
        }
        
        if (createFile) {
            FileOutputStream fos = new FileOutputStream("foobaa.xlsx");
            workbook.write(fos);
            fos.close();
        }
    }
}