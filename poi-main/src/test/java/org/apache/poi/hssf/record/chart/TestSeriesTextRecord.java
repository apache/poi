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

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.poi.hssf.record.RecordFormatException;
import org.apache.poi.hssf.record.RecordInputStream;
import org.apache.poi.hssf.record.TestcaseRecordInputStream;
import org.apache.poi.util.HexRead;

/**
 * Tests the serialization and deserialization of the SeriesTextRecord class
 * works correctly. Test data taken directly from a real Excel file.
 * 
 * 
 * @author Andrew C. Oliver (acoliver at apache.org)
 */
public final class TestSeriesTextRecord extends TestCase {
	private static final byte[] SIMPLE_DATA = HexRead
			.readFromString("00 00 0C 00 56 61 6C 75 65 20 4E 75 6D 62 65 72");

	public void testLoad() {
		SeriesTextRecord record = new SeriesTextRecord(TestcaseRecordInputStream.create(0x100d, SIMPLE_DATA));

		assertEquals((short) 0, record.getId());
		assertEquals("Value Number", record.getText());

		assertEquals(SIMPLE_DATA.length + 4, record.getRecordSize());
	}

	public void testStore() {
		SeriesTextRecord record = new SeriesTextRecord();

		record.setId(0);
		record.setText("Value Number");

		byte[] recordBytes = record.serialize();
		TestcaseRecordInputStream.confirmRecordEncoding(SeriesTextRecord.sid, SIMPLE_DATA,
				recordBytes);
	}

	public void testReserializeLongTitle() {
		// Hex dump from bug 45784 attachment 22560 streamOffset=0x0CD1
		byte[] data = HexRead.readFromString(
				"00 00, "
				+ "82 "
				+ "01 "
				+ "50 00 6C 00 61 00 73 00 6D 00 61 00 20 00 4C 00 "
				+ "65 00 76 00 65 00 6C 00 73 00 20 00 6F 00 66 00 "
				+ "20 00 4C 00 2D 00 30 00 30 00 30 00 31 00 31 00 "
				+ "31 00 32 00 32 00 32 00 2D 00 33 00 33 00 33 00 "
				+ "58 00 34 00 34 00 34 00 20 00 69 00 6E 00 20 00 "
				+ "53 00 44 00 20 00 72 00 61 00 74 00 0A 00 50 00 "
				+ "4F 00 20 00 33 00 2E 00 30 00 20 00 6D 00 67 00 "
				+ "2F 00 6B 00 67 00 20 00 28 00 35 00 2E 00 30 00 "
				+ "20 00 6D 00 4C 00 2F 00 6B 00 67 00 29 00 20 00 "
				+ "69 00 6E 00 20 00 4D 00 65 00 74 00 68 00 6F 00 "
				+ "63 00 65 00 6C 00 0A 00 49 00 56 00 20 00 30 00 "
				+ "2E 00 35 00 20 00 6D 00 67 00 2F 00 6B 00 67 00 "
				+ "20 00 28 00 31 00 2E 00 30 00 20 00 6D 00 4C 00 "
				+ "2F 00 6B 00 67 00 29 00 20 00 69 00 6E 00 20 00 "
				+ "36 00 30 00 25 00 20 00 50 00 45 00 47 00 20 00 "
				+ "32 00 30 00 30 00 0A 00 46 00 20 00 3D 00 61 00 "
				+ "62 00 63 00");

		RecordInputStream in = TestcaseRecordInputStream.create(SeriesTextRecord.sid, data);
		SeriesTextRecord str;
		try {
			str = new SeriesTextRecord(in);
		} catch (RecordFormatException e) {
			if (e.getCause() instanceof IllegalArgumentException) {
				// 'would be' error msg changed at svn r703620
				// "Illegal length - asked for -126 but only 130 left!"
				// "Bad requested string length (-126)"
				throw new AssertionFailedError("Identified bug 45784a");
			}
			throw e;
		}

		if (str.getRecordSize() < 0) {
			throw new AssertionFailedError("Identified bug 45784b");
		}
		byte[] ser;
		try {
			ser = str.serialize();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		TestcaseRecordInputStream.confirmRecordEncoding(SeriesTextRecord.sid, data, ser);
	}
}
