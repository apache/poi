
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
        

package org.apache.poi.hssf.usermodel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import junit.framework.TestCase;

import org.apache.poi.hssf.model.Sheet;

/**
 * Tests HSSFWorkbook method setSheetOrder()
 *
 *
 * @author Ruel Loehr (loehr1 at us.ibm.com)
 */

public class TestHSSFSheetOrder
        extends TestCase
{
    public TestHSSFSheetOrder(String s)
    {
        super(s);
    }

    /**
     * Test the sheet set order method
     */

    public void testBackupRecord()
            throws Exception
    {
        HSSFWorkbook wb = new HSSFWorkbook();
        
        for (int i=0; i < 10; i++)
        {
			HSSFSheet s = wb.createSheet("Sheet " + i);
			Sheet sheet = s.getSheet();
        }
        
        // Check the initial order
        assertEquals(0, wb.getSheetIndex("Sheet 0"));
        assertEquals(1, wb.getSheetIndex("Sheet 1"));
        assertEquals(2, wb.getSheetIndex("Sheet 2"));
        assertEquals(3, wb.getSheetIndex("Sheet 3"));
        assertEquals(4, wb.getSheetIndex("Sheet 4"));
        assertEquals(5, wb.getSheetIndex("Sheet 5"));
        assertEquals(6, wb.getSheetIndex("Sheet 6"));
        assertEquals(7, wb.getSheetIndex("Sheet 7"));
        assertEquals(8, wb.getSheetIndex("Sheet 8"));
        assertEquals(9, wb.getSheetIndex("Sheet 9"));

        // Change
        wb.getWorkbook().setSheetOrder("Sheet 6", 0);
        wb.getWorkbook().setSheetOrder("Sheet 3", 7);
        wb.getWorkbook().setSheetOrder("Sheet 1", 9);
        
        // Check they're currently right
        assertEquals(0, wb.getSheetIndex("Sheet 6"));
        assertEquals(1, wb.getSheetIndex("Sheet 0"));
        assertEquals(2, wb.getSheetIndex("Sheet 2"));
        assertEquals(3, wb.getSheetIndex("Sheet 4"));
        assertEquals(4, wb.getSheetIndex("Sheet 5"));
        assertEquals(5, wb.getSheetIndex("Sheet 7"));
        assertEquals(6, wb.getSheetIndex("Sheet 3"));
        assertEquals(7, wb.getSheetIndex("Sheet 8"));
        assertEquals(8, wb.getSheetIndex("Sheet 9"));
        assertEquals(9, wb.getSheetIndex("Sheet 1"));
          
        // Read it in and see if it is correct.
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        wb.write(baos);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        HSSFWorkbook wbr = new HSSFWorkbook(bais);
        
        assertEquals(0, wbr.getSheetIndex("Sheet 6"));
        assertEquals(1, wbr.getSheetIndex("Sheet 0"));
        assertEquals(2, wbr.getSheetIndex("Sheet 2"));
        assertEquals(3, wbr.getSheetIndex("Sheet 4"));
        assertEquals(4, wbr.getSheetIndex("Sheet 5"));
        assertEquals(5, wbr.getSheetIndex("Sheet 7"));
        assertEquals(6, wbr.getSheetIndex("Sheet 3"));
        assertEquals(7, wbr.getSheetIndex("Sheet 8"));
        assertEquals(8, wbr.getSheetIndex("Sheet 9"));
        assertEquals(9, wbr.getSheetIndex("Sheet 1"));
        
        // Now get the index by the sheet, not the name
        for(int i=0; i<10; i++) {
        	HSSFSheet s = wbr.getSheetAt(i);
        	assertEquals(i, wbr.getSheetIndex(s));
        }
    }
}


