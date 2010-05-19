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


import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTColor;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFill;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTPatternFill;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STPatternType;

import junit.framework.TestCase;


public class TestXSSFCellFill extends TestCase {
	
	public void testGetFillBackgroundColor() {
		CTFill ctFill = CTFill.Factory.newInstance();
		XSSFCellFill cellFill = new XSSFCellFill(ctFill);
		CTPatternFill ctPatternFill = ctFill.addNewPatternFill();
		CTColor bgColor = ctPatternFill.addNewBgColor();
		assertNotNull(cellFill.getFillBackgroundColor());
		bgColor.setIndexed(2);
		assertEquals(2, cellFill.getFillBackgroundColor().getIndexed());
	}
	
	public void testGetFillForegroundColor() {
		CTFill ctFill = CTFill.Factory.newInstance();
		XSSFCellFill cellFill = new XSSFCellFill(ctFill);
		CTPatternFill ctPatternFill = ctFill.addNewPatternFill();
		CTColor fgColor = ctPatternFill.addNewFgColor();
		assertNotNull(cellFill.getFillForegroundColor());
		fgColor.setIndexed(8);
		assertEquals(8, cellFill.getFillForegroundColor().getIndexed());
	}
	
	public void testGetSetPatternType() {
		CTFill ctFill = CTFill.Factory.newInstance();
		XSSFCellFill cellFill = new XSSFCellFill(ctFill);
		CTPatternFill ctPatternFill = ctFill.addNewPatternFill();
		ctPatternFill.setPatternType(STPatternType.SOLID);
		//assertEquals(FillPatternType.SOLID_FOREGROUND.ordinal(), cellFill.getPatternType().ordinal());
	}

    public void testGetNotModifies() {
        CTFill ctFill = CTFill.Factory.newInstance();
        XSSFCellFill cellFill = new XSSFCellFill(ctFill);
        CTPatternFill ctPatternFill = ctFill.addNewPatternFill();
        ctPatternFill.setPatternType(STPatternType.DARK_DOWN);
        assertEquals(8, cellFill.getPatternType().intValue());
    }

    public void testColorFromTheme() {
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("styles.xlsx");
        XSSFCell cellWithThemeColor = wb.getSheetAt(0).getRow(10).getCell(0);
        //color RGB will be extracted from theme
        XSSFColor foregroundColor = cellWithThemeColor.getCellStyle().getFillForegroundXSSFColor();
        byte[] rgb = foregroundColor.getRgb();
        byte[] rgbWithTint = foregroundColor.getRgbWithTint();
        assertEquals(rgb[0],-18);
        assertEquals(rgb[1],-20);
        assertEquals(rgb[2],-31);
        assertEquals(rgbWithTint[0],-12);
        assertEquals(rgbWithTint[1],-13);
        assertEquals(rgbWithTint[2],-20);
    }
}
