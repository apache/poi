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

import org.apache.poi.hssf.util.CellRangeAddress8Bit;

import junit.framework.TestCase;

/**
 * Tests the serialization and deserialization of the TableRecord
 * class works correctly.  Test data taken directly from a real
 * Excel file.
 */
public final class TestTableRecord extends TestCase {
	byte[] header = new byte[] {
			0x36, 02, 0x10, 00, // sid=x236, 16 bytes long
	};
	byte[] data = new byte[] {
			03, 00,  // from row 3 
			8, 00,   // to row 8
			04,      // from col 4
			06,      // to col 6
			00, 00,  // no flags set
			04, 00,  // row inp row 4
			01, 00,  // col inp row 1
			0x76, 0x40, // row inp col 0x4076 (!)
			00, 00   // col inp col 0
	};

	public void testLoad() {

		TableRecord record = new TableRecord(TestcaseRecordInputStream.create(0x236, data));

		CellRangeAddress8Bit range = record.getRange();
		assertEquals(3, range.getFirstRow());
		assertEquals(8, range.getLastRow());
		assertEquals(4, range.getFirstColumn());
		assertEquals(6, range.getLastColumn());
		assertEquals(0, record.getFlags());
		assertEquals(4, record.getRowInputRow());
		assertEquals(1, record.getColInputRow());
		assertEquals(0x4076, record.getRowInputCol());
		assertEquals(0, record.getColInputCol());

		assertEquals( 16 + 4, record.getRecordSize() );
	}

    public void testStore()
    {
//    	Offset 0x3bd9 (15321)
//    	recordid = 0x236, size = 16
//    	[TABLE]
//    	    .row from      = 3
//    	    .row to        = 8
//    	    .column from   = 4
//    	    .column to     = 6
//    	    .flags         = 0
//    	        .always calc     =false
//    	    .reserved      = 0
//    	    .row input row = 4
//    	    .col input row = 1
//    	    .row input col = 4076
//    	    .col input col = 0
//    	[/TABLE]

		CellRangeAddress8Bit crab = new CellRangeAddress8Bit(3, 8, 4, 6);
		TableRecord record = new TableRecord(crab);
		record.setFlags((byte)0);
		record.setRowInputRow(4);
		record.setColInputRow(1);
		record.setRowInputCol(0x4076);
		record.setColInputCol(0);

		byte [] recordBytes = record.serialize();
		assertEquals(recordBytes.length - 4, data.length);
		for (int i = 0; i < data.length; i++)
			assertEquals("At offset " + i, data[i], recordBytes[i+4]);
	}
}
