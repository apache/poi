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

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.poi.ss.util.CellRangeAddress;

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
		CellRangeAddress[] ranges = {
			new CellRangeAddress(0,0xFFFF,5,5),
			new CellRangeAddress(0,0xFFFF,6,6),
			new CellRangeAddress(0,1,0,1),
			new CellRangeAddress(0,1,2,3),
			new CellRangeAddress(2,3,0,1),
			new CellRangeAddress(2,3,2,3),
		};
		record.setCellRanges(ranges);
		ranges = record.getCellRanges();
		assertEquals(6,ranges.length);
		CellRangeAddress enclosingCellRange = record.getEnclosingCellRange();
		assertEquals(0, enclosingCellRange.getFirstRow());
		assertEquals(65535, enclosingCellRange.getLastRow());
		assertEquals(0, enclosingCellRange.getFirstColumn());
		assertEquals(6, enclosingCellRange.getLastColumn());
		record.setNeedRecalculation(true);
		assertTrue(record.getNeedRecalculation());
		record.setNeedRecalculation(false);
		assertFalse(record.getNeedRecalculation());
	}
	
	public void testSerialization() {
		byte[] recordData = 
		{
			(byte)0x03, (byte)0x00,
			(byte)0x01,	(byte)0x00,
			
			(byte)0x00,	(byte)0x00,
			(byte)0x03,	(byte)0x00,
			(byte)0x00,	(byte)0x00,
			(byte)0x03,	(byte)0x00,
			
			(byte)0x04,	(byte)0x00, // nRegions
			
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

		CFHeaderRecord record = new CFHeaderRecord(TestcaseRecordInputStream.create(CFHeaderRecord.sid, recordData));

		assertEquals("#CFRULES", 3, record.getNumberOfConditionalFormats());
		assertTrue(record.getNeedRecalculation());
		confirm(record.getEnclosingCellRange(), 0, 3, 0, 3);
		CellRangeAddress[] ranges = record.getCellRanges();
		assertEquals(4, ranges.length);
		confirm(ranges[0], 0, 1, 0, 1);
		confirm(ranges[1], 0, 1, 2, 3);
		confirm(ranges[2], 2, 3, 0, 1);
		confirm(ranges[3], 2, 3, 2, 3);
		assertEquals(recordData.length+4, record.getRecordSize());

		byte[] output = record.serialize();

		assertEquals("Output size", recordData.length+4, output.length); //includes sid+recordlength

		for (int i = 0; i < recordData.length; i++) 
		{
			assertEquals("CFHeaderRecord doesn't match", recordData[i], output[i+4]);
		}
	}
	
	public void testExtremeRows() {
		byte[] recordData = {
			(byte)0x13, (byte)0x00, // nFormats
			(byte)0x00,	(byte)0x00,
			
			(byte)0x00,	(byte)0x00,
			(byte)0xFF,	(byte)0xFF,
			(byte)0x00,	(byte)0x00,
			(byte)0xFF,	(byte)0x00,
			
			(byte)0x03,	(byte)0x00, // nRegions
			
			(byte)0x40,	(byte)0x9C,
			(byte)0x50,	(byte)0xC3,
			(byte)0x02,	(byte)0x00,
			(byte)0x02,	(byte)0x00,
			
			(byte)0x00,	(byte)0x00,
			(byte)0xFF,	(byte)0xFF,
			(byte)0x05,	(byte)0x00,
			(byte)0x05,	(byte)0x00,
			
			(byte)0x07,	(byte)0x00,
			(byte)0x07,	(byte)0x00,
			(byte)0x00,	(byte)0x00,
			(byte)0xFF,	(byte)0x00,
		};

		CFHeaderRecord record;
		try {
			record = new CFHeaderRecord(TestcaseRecordInputStream.create(CFHeaderRecord.sid, recordData));
		} catch (IllegalArgumentException e) {
			if(e.getMessage().equals("invalid cell range (-25536, 2, -15536, 2)")) {
				throw new AssertionFailedError("Identified bug 44739b");
			}
			throw e;
		}

		assertEquals("#CFRULES", 19, record.getNumberOfConditionalFormats());
		assertFalse(record.getNeedRecalculation());
		confirm(record.getEnclosingCellRange(), 0, 65535, 0, 255);
		CellRangeAddress[] ranges = record.getCellRanges();
		assertEquals(3, ranges.length);
		confirm(ranges[0], 40000, 50000, 2, 2);
		confirm(ranges[1], 0, 65535, 5, 5);
		confirm(ranges[2], 7, 7, 0, 255);

		byte[] output = record.serialize();

		assertEquals("Output size", recordData.length+4, output.length); //includes sid+recordlength

		for (int i = 0; i < recordData.length;i++) {
			assertEquals("CFHeaderRecord doesn't match", recordData[i], output[i+4]);
		}
	}

	private static void confirm(CellRangeAddress cr, int expFirstRow, int expLastRow, int expFirstCol, int expLastColumn) {
		assertEquals("first row", expFirstRow, cr.getFirstRow());
		assertEquals("last row", expLastRow, cr.getLastRow());
		assertEquals("first column", expFirstCol, cr.getFirstColumn());
		assertEquals("last column", expLastColumn, cr.getLastColumn());
	}
}
