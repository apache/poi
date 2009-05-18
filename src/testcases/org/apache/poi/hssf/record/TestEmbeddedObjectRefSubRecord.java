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

import java.util.Arrays;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.poi.util.HexRead;

/**
 * Tests the serialization and deserialization of the TestEmbeddedObjectRefSubRecord
 * class works correctly.  Test data taken directly from a real
 * Excel file.
 *
 * @author Yegor Kozlov
 */
public final class TestEmbeddedObjectRefSubRecord extends TestCase {

	private static final short EORSR_SID = EmbeddedObjectRefSubRecord.sid;

	public void testStore() {
		String data1
				= "20 00 05 00 FC 10 76 01 02 24 14 DF 00 03 10 00 "
				+ "00 46 6F 72 6D 73 2E 43 68 65 63 6B 42 6F 78 2E "
				+ "31 00 00 00 00 00 70 00 00 00 00 00 00 00 00 00 "
				+ "00 00";

		byte[] src = hr(data1);

		RecordInputStream in = TestcaseRecordInputStream.create(EORSR_SID, src);

		EmbeddedObjectRefSubRecord record1 = new EmbeddedObjectRefSubRecord(in, src.length);

		byte[] ser = record1.serialize();

		RecordInputStream in2 = TestcaseRecordInputStream.create(ser);
		EmbeddedObjectRefSubRecord record2 = new EmbeddedObjectRefSubRecord(in2, ser.length-4);

		confirmData(src, ser);
		assertEquals(record1.getOLEClassName(), record2.getOLEClassName());

		byte[] ser2 = record1.serialize();
		assertTrue(Arrays.equals(ser, ser2));
	}

	/**
	 * @param expectedData does not include sid & size
	 * @param actualFullRecordData includes sid & size
	 */
	private static void confirmData(byte[] expectedData, byte[] actualFullRecordData) {
		assertEquals(expectedData.length, actualFullRecordData.length-4);
		for (int i = 0; i < expectedData.length; i++) {
			if(expectedData[i] != actualFullRecordData[i+4]) {
				throw new AssertionFailedError("Difference at offset (" + i + ")");
			}
		}
	}

	public void testCreate() {

		EmbeddedObjectRefSubRecord record1 = new EmbeddedObjectRefSubRecord();

		byte[] ser = record1.serialize();
		RecordInputStream in2 = TestcaseRecordInputStream.create(ser);
		EmbeddedObjectRefSubRecord record2 = new EmbeddedObjectRefSubRecord(in2, ser.length-4);

		assertEquals(record1.getOLEClassName(), record2.getOLEClassName());
		assertEquals(record1.getStreamId(), record2.getStreamId());

		byte[] ser2 = record1.serialize();
		assertTrue(Arrays.equals(ser, ser2));
	}

	public void testCameraTool_bug45912() {
		/**
		 * taken from ftPictFmla sub-record in attachment 22645 (offset 0x40AB).
		 */
		byte[] data45912 = hr(
				"12 00 0B 00 F8 02 88 04 3B 00 " +
				"00 00 00 01 00 00 00 01 " +
				"00 00");
		RecordInputStream in = TestcaseRecordInputStream.create(EORSR_SID, data45912);

		EmbeddedObjectRefSubRecord rec = new EmbeddedObjectRefSubRecord(in, data45912.length);
		byte[] ser2 = rec.serialize();
		confirmData(data45912, ser2);
	}

	private static byte[] hr(String string) {
		return HexRead.readFromString(string);
	}

	/**
	 * tests various examples of OLE controls
	 */
	public void testVarious() {
		String[] rawData = {
			"12 00 0B 00 70 95 0B 05 3B 01 00 36 00 40 00 18 00 19 00 18",
			"12 00 0B 00 B0 4D 3E 03 3B 00 00 00 00 01 00 00 80 01 C0 00",
			"0C 00 05 00 60 AF 3B 03 24 FD FF FE C0 FE",
			"24 00 05 00 40 42 3E 03 02 80 CD B4 04 03 15 00 00 46 6F 72 6D 73 2E 43 6F 6D 6D 61 6E 64 42 75 74 74 6F 6E 2E 31 00 00 00 00 54 00 00 00 00 00 00 00 00 00 00 00",
			"22 00 05 00 10 4E 3E 03 02 00 4C CC 04 03 12 00 00 46 6F 72 6D 73 2E 53 70 69 6E 42 75 74 74 6F 6E 2E 31 00 54 00 00 00 20 00 00 00 00 00 00 00 00 00 00 00",
			"20 00 05 00 E0 41 3E 03 02 00 FC 0B 05 03 10 00 00 46 6F 72 6D 73 2E 43 6F 6D 62 6F 42 6F 78 2E 31 00 74 00 00 00 4C 00 00 00 00 00 00 00 00 00 00 00",
			"24 00 05 00 00 4C AF 03 02 80 E1 93 05 03 14 00 00 46 6F 72 6D 73 2E 4F 70 74 69 6F 6E 42 75 74 74 6F 6E 2E 31 00 C0 00 00 00 70 00 00 00 00 00 00 00 00 00 00 00",
			"20 00 05 00 E0 A4 28 04 02 80 EA 93 05 03 10 00 00 46 6F 72 6D 73 2E 43 68 65 63 6B 42 6F 78 2E 31 00 30 01 00 00 6C 00 00 00 00 00 00 00 00 00 00 00",
			"1C 00 05 00 30 40 3E 03 02 00 CC B4 04 03 0D 00 00 46 6F 72 6D 73 2E 4C 61 62 65 6C 2E 31 9C 01 00 00 54 00 00 00 00 00 00 00 00 00 00 00",
			"1E 00 05 00 B0 A4 28 04 02 00 D0 0A 05 03 0F 00 00 46 6F 72 6D 73 2E 4C 69 73 74 42 6F 78 2E 31 F0 01 00 00 48 00 00 00 00 00 00 00 00 00 00 00",
			"24 00 05 00 C0 AF 3B 03 02 80 D1 0A 05 03 14 00 00 46 6F 72 6D 73 2E 54 6F 67 67 6C 65 42 75 74 74 6F 6E 2E 31 00 38 02 00 00 6C 00 00 00 00 00 00 00 00 00 00 00",
			"1E 00 05 00 90 AF 3B 03 02 80 D4 0A 05 03 0F 00 00 46 6F 72 6D 73 2E 54 65 78 74 42 6F 78 2E 31 A4 02 00 00 48 00 00 00 00 00 00 00 00 00 00 00",
			"24 00 05 00 60 40 3E 03 02 00 D6 0A 05 03 14 00 00 46 6F 72 6D 73 2E 54 6F 67 67 6C 65 42 75 74 74 6F 6E 2E 31 00 EC 02 00 00 6C 00 00 00 00 00 00 00 00 00 00 00",
			"20 00 05 00 20 4D 3E 03 02 00 D9 0A 05 03 11 00 00 46 6F 72 6D 73 2E 53 63 72 6F 6C 6C 42 61 72 2E 31 58 03 00 00 20 00 00 00 00 00 00 00 00 00 00 00",
			"20 00 05 00 00 AF 28 04 02 80 31 AC 04 03 10 00 00 53 68 65 6C 6C 2E 45 78 70 6C 6F 72 65 72 2E 32 00 78 03 00 00 AC 00 00 00 00 00 00 00 00 00 00 00",
		};

		for (int i = 0; i < rawData.length; i++) {
			confirmRead(hr(rawData[i]), i);
		}
	}

	private static void confirmRead(byte[] data, int i) {
		RecordInputStream in = TestcaseRecordInputStream.create(EORSR_SID, data);

		EmbeddedObjectRefSubRecord rec = new EmbeddedObjectRefSubRecord(in, data.length);
		byte[] ser2 = rec.serialize();
		TestcaseRecordInputStream.confirmRecordEncoding("Test record " + i, EORSR_SID, data, ser2);
	}

	public void testVisioDrawing_bug46199() {
		/**
		 * taken from ftPictFmla sub-record in attachment 22860 (stream offset 0x768F).<br/>
		 * Note that the since the string length is zero, there is no unicode flag byte
		 */
		byte[] data46199 = hr(
				  "0E 00 "
				+ "05 00 "
				+ "28 25 A3 01 "
				+ "02 6C D1 34 02 "
				+ "03 00 00 "
				+ "0F CB E8 00");
		RecordInputStream in = TestcaseRecordInputStream.create(EORSR_SID, data46199);

		EmbeddedObjectRefSubRecord rec;
		try {
			rec = new EmbeddedObjectRefSubRecord(in, data46199.length);
		} catch (RecordFormatException e) {
			if (e.getMessage().equals("Not enough data (3) to read requested (4) bytes")) {
				throw new AssertionFailedError("Identified bug 22860");
			}
			throw e;
		}
		byte[] ser2 = rec.serialize();
		TestcaseRecordInputStream.confirmRecordEncoding(EORSR_SID, data46199, ser2);
	}
}
