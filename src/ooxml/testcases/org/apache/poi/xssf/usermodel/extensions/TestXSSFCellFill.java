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

package org.apache.poi.xssf.usermodel.extensions;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTColor;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFill;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTPatternFill;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STPatternType;


public class TestXSSFCellFill {

    @Test
    public void testGetFillBackgroundColor() {
        CTFill ctFill = CTFill.Factory.newInstance();
        XSSFCellFill cellFill = new XSSFCellFill(ctFill, null);
        CTPatternFill ctPatternFill = ctFill.addNewPatternFill();
        CTColor bgColor = ctPatternFill.addNewBgColor();
        assertNotNull(cellFill.getFillBackgroundColor());
        bgColor.setIndexed(2);
        assertEquals(2, cellFill.getFillBackgroundColor().getIndexed());
    }

    @Test
    public void testGetFillForegroundColor() {
        CTFill ctFill = CTFill.Factory.newInstance();
        XSSFCellFill cellFill = new XSSFCellFill(ctFill, null);
        CTPatternFill ctPatternFill = ctFill.addNewPatternFill();
        CTColor fgColor = ctPatternFill.addNewFgColor();
        assertNotNull(cellFill.getFillForegroundColor());
        fgColor.setIndexed(8);
        assertEquals(8, cellFill.getFillForegroundColor().getIndexed());
    }

    @Test
    public void testGetSetPatternType() {
        CTFill ctFill = CTFill.Factory.newInstance();
        XSSFCellFill cellFill = new XSSFCellFill(ctFill, null);
        CTPatternFill ctPatternFill = ctFill.addNewPatternFill();
        ctPatternFill.setPatternType(STPatternType.SOLID);
        assertEquals(FillPatternType.SOLID_FOREGROUND.ordinal(), cellFill.getPatternType().intValue()-1);
    }

    @Test
    public void testGetNotModifies() {
        CTFill ctFill = CTFill.Factory.newInstance();
        XSSFCellFill cellFill = new XSSFCellFill(ctFill, null);
        CTPatternFill ctPatternFill = ctFill.addNewPatternFill();
        ctPatternFill.setPatternType(STPatternType.DARK_DOWN);
        assertEquals(8, cellFill.getPatternType().intValue());
    }

    @Test
    public void testColorFromTheme() throws IOException {
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("styles.xlsx");
        XSSFCell cellWithThemeColor = wb.getSheetAt(0).getRow(10).getCell(0);
        //color RGB will be extracted from theme
        XSSFColor foregroundColor = cellWithThemeColor.getCellStyle().getFillForegroundXSSFColor();
        byte[] rgb = foregroundColor.getRGB();
        byte[] rgbWithTint = foregroundColor.getRGBWithTint();
        // Dk2
        assertEquals(rgb[0],31);
        assertEquals(rgb[1],73);
        assertEquals(rgb[2],125);
        // Dk2, lighter 40% (tint is about 0.39998)
        // 31 * (1.0 - 0.39998) + (255 - 255 * (1.0 - 0.39998)) = 120.59552 => 120 (byte)
        // 73 * (1.0 - 0.39998) + (255 - 255 * (1.0 - 0.39998)) = 145.79636 => -111 (byte)
        // 125 * (1.0 - 0.39998) + (255 - 255 * (1.0 - 0.39998)) = 176.99740 => -80 (byte)
        assertEquals(rgbWithTint[0],120);
        assertEquals(rgbWithTint[1],-111);
        assertEquals(rgbWithTint[2],-80);
        wb.close();
    }
    
    @Test
    public void testFillWithoutColors() {
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("FillWithoutColor.xlsx");
        XSSFCell cellWithFill = wb.getSheetAt(0).getRow(5).getCell(1);
        XSSFCellStyle style = cellWithFill.getCellStyle();
        assertNotNull(style);
        assertNull("had an empty background color", style.getFillBackgroundColorColor());
        assertNull("had an empty background color", style.getFillBackgroundXSSFColor());
    }
}
