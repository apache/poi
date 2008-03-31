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

import junit.framework.TestCase;

import org.apache.poi.hssf.record.cf.CellRange;

/**
 * Tests the serialization and deserialization of the TestCFHeaderRecord
 * class works correctly.  
 *
 * @author Dmitriy Kumshayev 
 */
public final class TestCFHeaderRecord extends TestCase
{

	public void testCreateCFHeaderRecord () 
	{
		CFHeaderRecord record = new CFHeaderRecord();
		CellRange[] ranges = {
			new CellRange(0,-1,5,5),
			new CellRange(0,-1,6,6),
			new CellRange(0,1,0,1),
			new CellRange(0,1,2,3),
			new CellRange(2,3,0,1),
			new CellRange(2,3,2,3),
		};
		record.setCellRanges(ranges);
		ranges = record.getCellRanges();
		assertEquals(6,ranges.length);
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
		CellRange[] ranges = record.getCellRanges();
		CellRange range0 = ranges[0];
		assertEquals(0, range0.getFirstRow());
		assertEquals(1, range0.getLastRow());
		assertEquals(0, range0.getFirstColumn());
		assertEquals(1, range0.getLastColumn());
		CellRange range1 = ranges[1];
		assertEquals(0, range1.getFirstRow());
		assertEquals(1, range1.getLastRow());
		assertEquals(2, range1.getFirstColumn());
		assertEquals(3, range1.getLastColumn());
		CellRange range2 = ranges[2];
		assertEquals(2, range2.getFirstRow());
		assertEquals(3, range2.getLastRow());
		assertEquals(0, range2.getFirstColumn());
		assertEquals(1, range2.getLastColumn());
		CellRange range3 = ranges[3];
		assertEquals(2, range3.getFirstRow());
		assertEquals(3, range3.getLastRow());
		assertEquals(2, range3.getFirstColumn());
		assertEquals(3, range3.getLastColumn());
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
