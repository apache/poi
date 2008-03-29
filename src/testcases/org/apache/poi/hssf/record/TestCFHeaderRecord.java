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

package org.apache.poi.hssf.record;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.poi.hssf.record.cf.CellRange;

/**
 * Tests the serialization and deserialization of the TestCFHeaderRecord
 * class works correctly.  
 *
 * @author Dmitriy Kumshayev 
 */
public class TestCFHeaderRecord
        extends TestCase
{

    public TestCFHeaderRecord(String name)
    {
        super(name);
    }

    public void testCreateCFHeaderRecord () 
    {
        CFHeaderRecord record = new CFHeaderRecord();
        List ranges = new ArrayList();
        ranges.add(new CellRange(0,-1,(short)5,(short)5));
        ranges.add(new CellRange(0,-1,(short)6,(short)6));
        ranges.add(new CellRange(0,1,(short)0,(short)1));
        ranges.add(new CellRange(0,1,(short)2,(short)3));
        ranges.add(new CellRange(2,3,(short)0,(short)1));
        ranges.add(new CellRange(2,3,(short)2,(short)3));
        record.setCellRanges(ranges);
        ranges = record.getCellRanges();
        assertEquals(6,ranges.size());
        CellRange enclosingCellRange = record.getEnclosingCellRange();
        assertEquals(0, enclosingCellRange.getFirstRow());
        assertEquals(-1, enclosingCellRange.getLastRow());
        assertEquals(0, enclosingCellRange.getFirstColumn());
        assertEquals(6, enclosingCellRange.getLastColumn());
        record.setNeedRecalculation(true);
        assertTrue(record.getNeedRecalculation());
        record.setNeedRecalculation(false);
        assertFalse(record.getNeedRecalculation());
    }
    
    public void testSerialization() {
    	byte[] recordData = new byte[]
    	{
       		(byte)0x03, (byte)0x00,
       		(byte)0x01,	(byte)0x00,
       		
       		(byte)0x00,	(byte)0x00,
       		(byte)0x03,	(byte)0x00,
       		(byte)0x00,	(byte)0x00,
       		(byte)0x03,	(byte)0x00,
       		
       		(byte)0x04,	(byte)0x00,
       		
       		(byte)0x00,	(byte)0x00,
       		(byte)0x01,	(byte)0x00,
       		(byte)0x00,	(byte)0x00,
       		(byte)0x01,	(byte)0x00,
       		
       		(byte)0x00,	(byte)0x00,
       		(byte)0x01,	(byte)0x00,
       		(byte)0x02,	(byte)0x00,
       		(byte)0x03,	(byte)0x00,
       		
       		(byte)0x02,	(byte)0x00,
       		(byte)0x03,	(byte)0x00,
       		(byte)0x00,	(byte)0x00,
       		(byte)0x01,	(byte)0x00,
       		
       		(byte)0x02,	(byte)0x00,
       		(byte)0x03,	(byte)0x00,
       		(byte)0x02,	(byte)0x00,
       		(byte)0x03,	(byte)0x00,
    	};
    	
    	CFHeaderRecord record = new CFHeaderRecord(new TestcaseRecordInputStream(CFHeaderRecord.sid, (short)recordData.length, recordData));
		
    	assertEquals("#CFRULES", 3, record.getNumberOfConditionalFormats());
    	assertTrue(record.getNeedRecalculation());
        CellRange enclosingCellRange = record.getEnclosingCellRange();
        assertEquals(0, enclosingCellRange.getFirstRow());
        assertEquals(3, enclosingCellRange.getLastRow());
        assertEquals(0, enclosingCellRange.getFirstColumn());
        assertEquals(3, enclosingCellRange.getLastColumn());
        List ranges = record.getCellRanges();
        assertEquals(0, ((CellRange)ranges.get(0)).getFirstRow());
        assertEquals(1, ((CellRange)ranges.get(0)).getLastRow());
        assertEquals(0, ((CellRange)ranges.get(0)).getFirstColumn());
        assertEquals(1, ((CellRange)ranges.get(0)).getLastColumn());
        assertEquals(0, ((CellRange)ranges.get(1)).getFirstRow());
        assertEquals(1, ((CellRange)ranges.get(1)).getLastRow());
        assertEquals(2, ((CellRange)ranges.get(1)).getFirstColumn());
        assertEquals(3, ((CellRange)ranges.get(1)).getLastColumn());
        assertEquals(2, ((CellRange)ranges.get(2)).getFirstRow());
        assertEquals(3, ((CellRange)ranges.get(2)).getLastRow());
        assertEquals(0, ((CellRange)ranges.get(2)).getFirstColumn());
        assertEquals(1, ((CellRange)ranges.get(2)).getLastColumn());
        assertEquals(2, ((CellRange)ranges.get(3)).getFirstRow());
        assertEquals(3, ((CellRange)ranges.get(3)).getLastRow());
        assertEquals(2, ((CellRange)ranges.get(3)).getFirstColumn());
        assertEquals(3, ((CellRange)ranges.get(3)).getLastColumn());
        assertEquals(recordData.length+4, record.getRecordSize());
    	
		byte[] output = record.serialize();
		
		assertEquals("Output size", recordData.length+4, output.length); //includes sid+recordlength
		
		for (int i = 0; i < recordData.length;i++) 
		{
			assertEquals("CFHeaderRecord doesn't match", recordData[i], output[i+4]);
		}
    }
    
    
    public static void main(String[] ignored_args)
	{
		System.out.println("Testing org.apache.poi.hssf.record.CFHeaderRecord");
		junit.textui.TestRunner.run(TestCFHeaderRecord.class);
	}
    
}
