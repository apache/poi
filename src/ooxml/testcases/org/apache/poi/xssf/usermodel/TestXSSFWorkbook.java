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

import junit.framework.TestCase;

import org.apache.poi.ss.usermodel.Sheet;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSheet;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorkbook;


public class TestXSSFWorkbook extends TestCase {
    
    public void testGetSheetIndex() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet1 = workbook.createSheet("sheet1");
        Sheet sheet2 = workbook.createSheet("sheet2");
        assertEquals(0, workbook.getSheetIndex(sheet1));
        assertEquals(0, workbook.getSheetIndex("sheet1"));
        assertEquals(1, workbook.getSheetIndex(sheet2));
        assertEquals(1, workbook.getSheetIndex("sheet2"));
        assertEquals(-1, workbook.getSheetIndex("noSheet"));
    }
    
    public void testSetSheetOrder() throws Exception {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet1 = workbook.createSheet("sheet1");
        Sheet sheet2 = workbook.createSheet("sheet2");
        assertSame(sheet1, workbook.getSheetAt(0));
        assertSame(sheet2, workbook.getSheetAt(1));
        workbook.setSheetOrder("sheet2", 0);
        assertSame(sheet2, workbook.getSheetAt(0));
        assertSame(sheet1, workbook.getSheetAt(1));
        // Test reordering of CTSheets
        CTWorkbook ctwb = workbook.getWorkbook();
        CTSheet[] ctsheets = ctwb.getSheets().getSheetArray();
        assertEquals("sheet2", ctsheets[0].getName());
        assertEquals("sheet1", ctsheets[1].getName());
        
        // Borderline case: only one sheet
        workbook = new XSSFWorkbook();
        sheet1 = workbook.createSheet("sheet1");
        assertSame(sheet1, workbook.getSheetAt(0));
        workbook.setSheetOrder("sheet1", 0);
        assertSame(sheet1, workbook.getSheetAt(0));
    }
    
    public void testSetSelectedTab() throws Exception {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet1 = workbook.createSheet("sheet1");
        Sheet sheet2 = workbook.createSheet("sheet2");
        assertEquals(-1, workbook.getSelectedTab());
        workbook.setSelectedTab((short) 0);
        assertEquals(0, workbook.getSelectedTab());
        workbook.setSelectedTab((short) 1);
        assertEquals(1, workbook.getSelectedTab());
    }
}
