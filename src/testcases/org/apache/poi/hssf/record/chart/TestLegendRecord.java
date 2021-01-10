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

package org.apache.poi.hssf.record.chart;

import static org.apache.poi.hssf.record.TestcaseRecordInputStream.confirmRecordEncoding;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.poi.hssf.record.TestcaseRecordInputStream;
import org.junit.jupiter.api.Test;

/**
 * Tests the serialization and deserialization of the LegendRecord class works
 * correctly. Test data taken directly from a real Excel file.
 */
final class TestLegendRecord {
	byte[] data = new byte[] { (byte) 0x76, (byte) 0x0E, (byte) 0x00, (byte) 0x00, (byte) 0x86,
			(byte) 0x07, (byte) 0x00, (byte) 0x00, (byte) 0x19, (byte) 0x01, (byte) 0x00,
			(byte) 0x00, (byte) 0x8B, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x03,
			(byte) 0x01, (byte) 0x1F, (byte) 0x00 };

	@Test
	void testLoad() {
		LegendRecord record = new LegendRecord(TestcaseRecordInputStream.create(0x1015, data));

		assertEquals(0xe76, record.getXAxisUpperLeft());

		assertEquals(0x786, record.getYAxisUpperLeft());

		assertEquals(0x119, record.getXSize());

		assertEquals(0x8b, record.getYSize());

		assertEquals((byte) 0x3, record.getType());

		assertEquals((byte) 0x1, record.getSpacing());

		assertEquals((short) 0x1f, record.getOptions());
        assertTrue(record.isAutoPosition());
        assertTrue(record.isAutoSeries());
        assertTrue(record.isAutoXPositioning());
        assertTrue(record.isAutoYPositioning());
        assertTrue(record.isVertical());
        assertFalse(record.isDataTable());

		assertEquals(24, record.getRecordSize());
	}

	@SuppressWarnings("squid:S2699")
	@Test
	void testStore() {
		LegendRecord record = new LegendRecord();

		record.setXAxisUpperLeft(0xe76);
		record.setYAxisUpperLeft(0x786);
		record.setXSize(0x119);
		record.setYSize(0x8b);
		record.setType((byte) 0x3);
		record.setSpacing((byte) 0x1);
		record.setOptions((short) 0x1f);
		record.setAutoPosition(true);
		record.setAutoSeries(true);
		record.setAutoXPositioning(true);
		record.setAutoYPositioning(true);
		record.setVertical(true);
		record.setDataTable(false);

		byte[] recordBytes = record.serialize();
		confirmRecordEncoding(LegendRecord.sid, data, recordBytes);
	}
}
