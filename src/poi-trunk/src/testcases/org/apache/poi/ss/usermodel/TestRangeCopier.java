/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.ss.usermodel;

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;
import org.apache.poi.ss.ITestDataProvider;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;

@Ignore
public abstract class TestRangeCopier {
    protected Sheet sheet1;
    protected Sheet sheet2;
    protected Workbook workbook;
    protected RangeCopier rangeCopier; 
    protected RangeCopier transSheetRangeCopier; 
    protected ITestDataProvider testDataProvider;

    protected void initSheets() {
        sheet1 = workbook.getSheet("sheet1");
        sheet2 = workbook.getSheet("sheet2");
    }
    
    @Test
    public void copySheetRangeWithoutFormulas() {
        CellRangeAddress rangeToCopy = CellRangeAddress.valueOf("B1:C2");   //2x2
        CellRangeAddress destRange = CellRangeAddress.valueOf("C2:D3");     //2x2
        rangeCopier.copyRange(rangeToCopy, destRange);
        assertEquals("1.1", sheet1.getRow(2).getCell(2).toString());
        assertEquals("2.1", sheet1.getRow(2).getCell(3).toString());
    }

    @Test
    public void tileTheRangeAway() {
        CellRangeAddress tileRange = CellRangeAddress.valueOf("C4:D5");
        CellRangeAddress destRange = CellRangeAddress.valueOf("F4:K5"); 
        rangeCopier.copyRange(tileRange, destRange);
        assertEquals("1.3", getCellContent(sheet1, "H4"));  
        assertEquals("1.3", getCellContent(sheet1, "J4"));  
        assertEquals("$C1+G$2", getCellContent(sheet1, "G5"));  
        assertEquals("SUM(G3:I3)", getCellContent(sheet1, "H5"));   
        assertEquals("$C1+I$2", getCellContent(sheet1, "I5"));  
        assertEquals("", getCellContent(sheet1, "L5"));  //out of borders
        assertEquals("", getCellContent(sheet1, "G7")); //out of borders
    }
    
    @Test
    public void tileTheRangeOver() {
        CellRangeAddress tileRange = CellRangeAddress.valueOf("C4:D5");
        CellRangeAddress destRange = CellRangeAddress.valueOf("A4:C5"); 
        rangeCopier.copyRange(tileRange, destRange);
        assertEquals("1.3", getCellContent(sheet1, "A4"));
        assertEquals("$C1+B$2", getCellContent(sheet1, "B5"));
        assertEquals("SUM(B3:D3)", getCellContent(sheet1, "C5"));
    }

    @Test
    public void copyRangeToOtherSheet() {
        Sheet destSheet = sheet2;
        CellRangeAddress tileRange = CellRangeAddress.valueOf("C4:D5"); // on sheet1
        CellRangeAddress destRange = CellRangeAddress.valueOf("F4:J6"); // on sheet2 
        transSheetRangeCopier.copyRange(tileRange, destRange);
        assertEquals("1.3", getCellContent(destSheet, "H4"));
        assertEquals("1.3", getCellContent(destSheet, "J4"));
        assertEquals("$C1+G$2", getCellContent(destSheet, "G5"));
        assertEquals("SUM(G3:I3)", getCellContent(destSheet, "H5"));
        assertEquals("$C1+I$2", getCellContent(destSheet, "I5"));
    }
    
    protected static String getCellContent(Sheet sheet, String coordinates) {
        try {
            CellReference p = new CellReference(coordinates);
            return sheet.getRow(p.getRow()).getCell(p.getCol()).toString();
        }
        catch (NullPointerException e) { // row or cell does not exist
            return "";
        }
    }
}
