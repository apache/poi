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

import junit.framework.*;
import org.apache.poi.hssf.record.NameRecord;

public class TestHSSFWorkbook extends TestCase
{
    HSSFWorkbook hssfWorkbook;

    public void testSetRepeatingRowsAndColumns() throws Exception
    {
        // Test bug 29747
        HSSFWorkbook b = new HSSFWorkbook( );
        b.createSheet();
        b.createSheet();
        b.createSheet();
        b.setRepeatingRowsAndColumns( 2, 0,1,-1,-1 );
        NameRecord nameRecord = b.getWorkbook().getNameRecord( 0 );
        assertEquals( 3, nameRecord.getIndexToSheet() );
    }

    public void testDuplicateNames()
            throws Exception
    {
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
        HSSFSheet s = b.createSheet("Sheet Two");
        b.setSelectedTab((short) 1);
        b.setDisplayedTab((short) 1);
        assertEquals(b.getSelectedTab(), 1);
        assertEquals(b.getDisplayedTab(), 1);
    }
}