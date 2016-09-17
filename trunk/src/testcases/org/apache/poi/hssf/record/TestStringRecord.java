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


import org.apache.poi.util.HexRead;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianByteArrayInputStream;
import org.apache.poi.util.LittleEndianInput;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

/**
 * Tests the serialization and deserialization of the StringRecord
 * class works correctly.  Test data taken directly from a real
 * Excel file.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public final class TestStringRecord extends TestCase {
	private static final byte[] data = HexRead.readFromString(
			"0B 00 " + // length
			"00 " +    // option
			// string
			"46 61 68 72 7A 65 75 67 74 79 70"
	);

	public void testLoad() {

		StringRecord record = new StringRecord(TestcaseRecordInputStream.create(0x207, data));
		assertEquals( "Fahrzeugtyp", record.getString());

		assertEquals( 18, record.getRecordSize() );
	}

	public void testStore() {
		StringRecord record = new StringRecord();
		record.setString("Fahrzeugtyp");

		byte [] recordBytes = record.serialize();
		assertEquals(recordBytes.length - 4, data.length);
		for (int i = 0; i < data.length; i++)
			assertEquals("At offset " + i, data[i], recordBytes[i+4]);
	}
    
	public void testContinue() {
		int MAX_BIFF_DATA = RecordInputStream.MAX_RECORD_DATA_SIZE;
		int TEXT_LEN = MAX_BIFF_DATA + 1000; // deliberately over-size
		String textChunk = "ABCDEGGHIJKLMNOP"; // 16 chars
		StringBuffer sb = new StringBuffer(16384);
		while (sb.length() < TEXT_LEN) {
			sb.append(textChunk);
		}
		sb.setLength(TEXT_LEN);

		StringRecord sr = new StringRecord();
		sr.setString(sb.toString());
		byte[] ser = sr.serialize();
		assertEquals(StringRecord.sid, LittleEndian.getUShort(ser, 0));
		if (LittleEndian.getUShort(ser, 2) > MAX_BIFF_DATA) {
			throw new AssertionFailedError(
					"StringRecord should have been split with a continue record");
		}
		// Confirm expected size of first record, and ushort strLen.
		assertEquals(MAX_BIFF_DATA, LittleEndian.getUShort(ser, 2));
		assertEquals(TEXT_LEN, LittleEndian.getUShort(ser, 4));

		// Confirm first few bytes of ContinueRecord
		LittleEndianInput crIn = new LittleEndianByteArrayInputStream(ser, (MAX_BIFF_DATA + 4));
		int nCharsInFirstRec = MAX_BIFF_DATA - (2 + 1); // strLen, optionFlags
		int nCharsInSecondRec = TEXT_LEN - nCharsInFirstRec;
		assertEquals(ContinueRecord.sid, crIn.readUShort());
		assertEquals(1 + nCharsInSecondRec, crIn.readUShort());
		assertEquals(0, crIn.readUByte());
		assertEquals('N', crIn.readUByte());
		assertEquals('O', crIn.readUByte());

		// re-read and make sure string value is the same
		RecordInputStream in = TestcaseRecordInputStream.create(ser);
		StringRecord sr2 = new StringRecord(in);
		assertEquals(sb.toString(), sr2.getString());
	}
}
