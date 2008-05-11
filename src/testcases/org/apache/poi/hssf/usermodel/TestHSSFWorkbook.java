/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.apache.poi.hssf.usermodel;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.record.NameRecord;
/**
 * 
 */
public final class TestHSSFWorkbook extends TestCase {
    private static HSSFWorkbook openSample(String sampleFileName) {
        return HSSFTestDataSamples.openSampleWorkbook(sampleFileName);
    }

    public void testSetRepeatingRowsAndColumns() {
        // Test bug 29747
        HSSFWorkbook b = new HSSFWorkbook( );
        b.createSheet();
        b.createSheet();
        b.createSheet();
        b.setRepeatingRowsAndColumns( 2, 0,1,-1,-1 );
        NameRecord nameRecord = b.getWorkbook().getNameRecord( 0 );
        assertEquals( 3, nameRecord.getIndexToSheet() );
    }

    public void testCaseInsensitiveNames() {
        HSSFWorkbook b = new HSSFWorkbook( );
        HSSFSheet originalSheet = b.createSheet("Sheet1");
        HSSFSheet fetchedSheet = b.getSheet("sheet1");
        if(fetchedSheet == null) {
            throw new AssertionFailedError("Identified bug 44892");
        }
        assertEquals(originalSheet, fetchedSheet);
        try {
            b.createSheet("sHeeT1");
            fail("should have thrown exceptiuon due to duplicate sheet name");
        } catch (IllegalArgumentException e) {
            // expected during successful test
            assertEquals("The workbook already contains a sheet of this name", e.getMessage());
        }
    }

    public void testDuplicateNames() {
        HSSFWorkbook b = new HSSFWorkbook( );
        b.createSheet("Sheet1");
        b.createSheet();
        b.createSheet("name1");
        try
        {
            b.createSheet("name1");
            fail();
        }
        catch ( IllegalArgumentException pass )
        {
        }
        b.createSheet();
        try
        {
            b.setSheetName( 3,  "name1" );
            fail();
        }
        catch ( IllegalArgumentException pass )
        {
        }

        try
        {
            b.setSheetName( 3,  "name1" );
            fail();
        }
        catch ( IllegalArgumentException pass )
        {
        }

        b.setSheetName( 3,  "name2" );
        b.setSheetName( 3,  "name2" );
        b.setSheetName( 3,  "name2" );
        
        HSSFWorkbook c = new HSSFWorkbook( );
        c.createSheet("Sheet1");
        c.createSheet("Sheet2");
        c.createSheet("Sheet3");
        c.createSheet("Sheet4");

    }
    
    public void testWindowOneDefaults() {
        HSSFWorkbook b = new HSSFWorkbook( );
        try {
            assertEquals(b.getSelectedTab(), 0);
            assertEquals(b.getDisplayedTab(), 0);
        } catch (NullPointerException npe) {
            fail("WindowOneRecord in Workbook is probably not initialized");
        }
    }
    
    public void testSheetSelection() {
        HSSFWorkbook b = new HSSFWorkbook();
        b.createSheet("Sheet One");
        b.createSheet("Sheet Two");
        b.setSelectedTab((short) 1);
        b.setDisplayedTab((short) 1);
        assertEquals(b.getSelectedTab(), 1);
        assertEquals(b.getDisplayedTab(), 1);
    }
    
    public void testSheetClone() {
        // First up, try a simple file
        HSSFWorkbook b = new HSSFWorkbook();
        assertEquals(0, b.getNumberOfSheets());
        b.createSheet("Sheet One");
        b.createSheet("Sheet Two");
        
        assertEquals(2, b.getNumberOfSheets());
        b.cloneSheet(0);
        assertEquals(3, b.getNumberOfSheets());
        
        // Now try a problem one with drawing records in it
        b = openSample("SheetWithDrawing.xls");
        assertEquals(1, b.getNumberOfSheets());
        b.cloneSheet(0);
        assertEquals(2, b.getNumberOfSheets());
    }
    
    public void testReadWriteWithCharts() {
        HSSFWorkbook b;
        HSSFSheet s;
        
        // Single chart, two sheets
        b = openSample("44010-SingleChart.xls");
        assertEquals(2, b.getNumberOfSheets());
        assertEquals("Graph2", b.getSheetName(1));
        s = b.getSheetAt(1);
        assertEquals(0, s.getFirstRowNum());
        assertEquals(8, s.getLastRowNum());
        
        // Has chart on 1st sheet??
        // FIXME
        assertNotNull(b.getSheetAt(0).getDrawingPatriarch());
        assertNull(b.getSheetAt(1).getDrawingPatriarch());
        assertFalse(b.getSheetAt(0).getDrawingPatriarch().containsChart());
        
        // We've now called getDrawingPatriarch() so 
        //  everything will be all screwy
        // So, start again
        b = openSample("44010-SingleChart.xls");
        
        b = writeRead(b);
        assertEquals(2, b.getNumberOfSheets());
        s = b.getSheetAt(1);
        assertEquals(0, s.getFirstRowNum());
        assertEquals(8, s.getLastRowNum());

        
        // Two charts, three sheets
        b = openSample("44010-TwoCharts.xls");
        assertEquals(3, b.getNumberOfSheets());
        
        s = b.getSheetAt(1);
        assertEquals(0, s.getFirstRowNum());
        assertEquals(8, s.getLastRowNum());
        s = b.getSheetAt(2);
        assertEquals(0, s.getFirstRowNum());
        assertEquals(8, s.getLastRowNum());
        
        // Has chart on 1st sheet??
        // FIXME
        assertNotNull(b.getSheetAt(0).getDrawingPatriarch());
        assertNull(b.getSheetAt(1).getDrawingPatriarch());
        assertNull(b.getSheetAt(2).getDrawingPatriarch());
        assertFalse(b.getSheetAt(0).getDrawingPatriarch().containsChart());
        
        // We've now called getDrawingPatriarch() so 
        //  everything will be all screwy
        // So, start again
        b = openSample("44010-TwoCharts.xls");
        
        b = writeRead(b);
        assertEquals(3, b.getNumberOfSheets());
        
        s = b.getSheetAt(1);
        assertEquals(0, s.getFirstRowNum());
        assertEquals(8, s.getLastRowNum());
        s = b.getSheetAt(2);
        assertEquals(0, s.getFirstRowNum());
        assertEquals(8, s.getLastRowNum());
    }
    
    private static HSSFWorkbook writeRead(HSSFWorkbook b) {
        return HSSFTestDataSamples.writeOutAndReadBack(b);
    }
}