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

package org.apache.poi.xssf.usermodel.helpers;

import junit.framework.TestCase;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCol;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCols;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorksheet;


public class TestColumnHelper extends TestCase {
    
    public void testCleanColumns() {
        CTWorksheet worksheet = CTWorksheet.Factory.newInstance();
        
        CTCols cols1 = worksheet.addNewCols();
        CTCol col1 = cols1.addNewCol();
        col1.setMin(1);
        col1.setMax(1);
        col1.setWidth(88);
        col1.setHidden(true);
        CTCol col2 = cols1.addNewCol();
        col2.setMin(2);
        col2.setMax(3);
        CTCols cols2 = worksheet.addNewCols();
        CTCol col4 = cols2.addNewCol();
        col4.setMin(3);
        col4.setMax(6);
        
        // Test cleaning cols
        assertEquals(2, worksheet.sizeOfColsArray());
        int count = countColumns(worksheet);
        assertEquals(7, count);
        // Clean columns and test a clean worksheet
        ColumnHelper helper = new ColumnHelper(worksheet);
        assertEquals(1, worksheet.sizeOfColsArray());
        count = countColumns(worksheet);
        assertEquals(6, count);
        assertEquals((double) 88, helper.getColumn(1).getWidth());
        assertTrue(helper.getColumn(1).getHidden());
        
    }
    
    public void testGetColumn() {
        CTWorksheet worksheet = CTWorksheet.Factory.newInstance();
        
        CTCols cols1 = worksheet.addNewCols();
        CTCol col1 = cols1.addNewCol();
        col1.setMin(1);
        col1.setMax(1);
        col1.setWidth(88);
        col1.setHidden(true);
        CTCol col2 = cols1.addNewCol();
        col2.setMin(2);
        col2.setMax(3);
        CTCols cols2 = worksheet.addNewCols();
        CTCol col4 = cols2.addNewCol();
        col4.setMin(3);
        col4.setMax(6);
        
        ColumnHelper helper = new ColumnHelper(worksheet);
        assertNotNull(helper.getColumn(1));
        assertEquals((double) 88, helper.getColumn(1).getWidth());
        assertTrue(helper.getColumn(1).getHidden());
        assertFalse(helper.getColumn(2).getHidden());
        assertNull(helper.getColumn(99));
    }
    
    public void testSetColumnAttributes() {
        CTCol col = CTCol.Factory.newInstance();
        col.setWidth(12);
        col.setHidden(true);
        CTCol newCol = CTCol.Factory.newInstance();
        assertEquals((double) 0, newCol.getWidth());
        assertFalse(newCol.getHidden());
        ColumnHelper helper = new ColumnHelper(CTWorksheet.Factory.newInstance());
        helper.setColumnAttributes(col, newCol);
        assertEquals((double) 12, newCol.getWidth());
        assertTrue(newCol.getHidden());
    }
    
    public void testGetOrCreateColumn() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = (XSSFSheet) workbook.createSheet("Sheet 1");
        ColumnHelper columnHelper = sheet.getColumnHelper();
        CTCol col = columnHelper.getOrCreateColumn(3);
        assertNotNull(col);
        assertNotNull(columnHelper.getColumn(3));

        CTCol col2 = columnHelper.getOrCreateColumn(30);
        assertNotNull(col2);
        assertNotNull(columnHelper.getColumn(30));
    }

    private int countColumns(CTWorksheet worksheet) {
        int count;
        count = 0;
        for (int i = 0 ; i < worksheet.sizeOfColsArray() ; i++) {
            for (int y = 0 ; y < worksheet.getColsArray(i).sizeOfColArray() ; y++) {
                for (long k = worksheet.getColsArray(i).getColArray(y).getMin() ; k <= worksheet.getColsArray(i).getColArray(y).getMax() ; k++) {
                    count++;
                }
            }
        }
        return count;
    }
    
}
