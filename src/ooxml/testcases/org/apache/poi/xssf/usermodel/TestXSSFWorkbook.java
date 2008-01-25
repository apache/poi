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

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

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
    
    public void testSetSheetName() throws Exception {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet1 = workbook.createSheet("sheet1");
        assertEquals("sheet1", workbook.getSheetName(0));
        workbook.setSheetName(0, "sheet2");
        assertEquals("sheet2", workbook.getSheetName(0));
    }
    
    public void testCloneSheet() throws Exception {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet1 = workbook.createSheet("sheet");
        Sheet sheet2 = workbook.cloneSheet(0);
        assertEquals(2, workbook.getNumberOfSheets());
        assertEquals("sheet(1)", workbook.getSheetName(1));
        workbook.setSheetName(1, "clonedsheet");
        Sheet sheet3 = workbook.cloneSheet(1);
        assertEquals(3, workbook.getNumberOfSheets());
        assertEquals("clonedsheet(1)", workbook.getSheetName(2));
    }
    
    public void testGetSheetByName() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet1 = workbook.createSheet("sheet1");
        Sheet sheet2 = workbook.createSheet("sheet2");
        assertSame(sheet1, workbook.getSheet("sheet1"));
        assertSame(sheet2, workbook.getSheet("sheet2"));
        assertNull(workbook.getSheet("nosheet"));
    }
    
    public void testRemoveSheetAt() throws Exception {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet1 = workbook.createSheet("sheet1");
        Sheet sheet2 = workbook.createSheet("sheet2");
        Sheet sheet3 = workbook.createSheet("sheet3");
        workbook.removeSheetAt(1);
        assertEquals(2, workbook.getNumberOfSheets());
        assertEquals("sheet3", workbook.getSheetName(1));
        workbook.removeSheetAt(0);
        assertEquals(1, workbook.getNumberOfSheets());
        assertEquals("sheet3", workbook.getSheetName(0));
        workbook.removeSheetAt(0);
        assertEquals(0, workbook.getNumberOfSheets());
    }
    
    public void testSave() throws Exception {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet1 = workbook.createSheet("sheet1");
        Sheet sheet2 = workbook.createSheet("sheet2");
        Sheet sheet3 = workbook.createSheet("sheet3");
        File file = File.createTempFile("poi-", ".xlsx");
        System.out.println("Saving to " + file.getAbsolutePath());
        OutputStream out = new FileOutputStream(file);
        workbook.write(out);
        out.close();
    }
}
