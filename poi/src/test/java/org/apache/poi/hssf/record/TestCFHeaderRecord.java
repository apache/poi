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

import static org.apache.poi.hssf.record.TestcaseRecordInputStream.confirmRecordEncoding;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.poi.ss.util.CellRangeAddress;
import org.junit.jupiter.api.Test;

/**
 * Tests the serialization and deserialization of the {@link CFHeaderRecord}
 *  and {@link CFHeader12Record} classes works correctly.
 */
final class TestCFHeaderRecord {
	@Test
	void testCreateCFHeaderRecord () {
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

        assertFalse(record.getNeedRecalculation());
		assertEquals(0, record.getID());

		record.setNeedRecalculation(true);
        assertTrue(record.getNeedRecalculation());
        assertEquals(0, record.getID());

        record.setID(7);
		record.setNeedRecalculation(false);
        assertFalse(record.getNeedRecalculation());
        assertEquals(7, record.getID());
	}

	@Test
    void testCreateCFHeader12Record () {
        CFHeader12Record record = new CFHeader12Record();
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

        assertFalse(record.getNeedRecalculation());
        assertEquals(0, record.getID());

        record.setNeedRecalculation(true);
        assertTrue(record.getNeedRecalculation());
        assertEquals(0, record.getID());

        record.setID(7);
        record.setNeedRecalculation(false);
        assertFalse(record.getNeedRecalculation());
        assertEquals(7, record.getID());
    }

	@Test
	void testSerialization() {
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

		assertEquals(3, record.getNumberOfConditionalFormats(), "#CFRULES");
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
		confirmRecordEncoding(CFHeaderRecord.sid, recordData, output);
	}

	@Test
	void testExtremeRows() {
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

		// bug 44739b - invalid cell range (-25536, 2, -15536, 2)
		CFHeaderRecord record = new CFHeaderRecord(TestcaseRecordInputStream.create(CFHeaderRecord.sid, recordData));

		assertEquals(19, record.getNumberOfConditionalFormats(), "#CFRULES");
		assertFalse(record.getNeedRecalculation());
		confirm(record.getEnclosingCellRange(), 0, 65535, 0, 255);
		CellRangeAddress[] ranges = record.getCellRanges();
		assertEquals(3, ranges.length);
		confirm(ranges[0], 40000, 50000, 2, 2);
		confirm(ranges[1], 0, 65535, 5, 5);
		confirm(ranges[2], 7, 7, 0, 255);

		byte[] output = record.serialize();
		confirmRecordEncoding(CFHeaderRecord.sid, recordData, output);
	}

	private static void confirm(CellRangeAddress cr, int expFirstRow, int expLastRow, int expFirstCol, int expLastColumn) {
		assertEquals(expFirstRow, cr.getFirstRow(), "first row");
		assertEquals(expLastRow, cr.getLastRow(), "last row");
		assertEquals(expFirstCol, cr.getFirstColumn(), "first column");
		assertEquals(expLastColumn, cr.getLastColumn(), "last column");
	}
}
