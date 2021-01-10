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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.poi.util.HexRead;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Tests for {@link RecordInputStream}
 */
final class TestRecordInputStream {

	/**
	 * Data inspired by attachment 22626 of bug 45866<br>
	 * A unicode string of 18 chars, with a continue record where the compression flag changes
	 */
	private static final String HED_DUMP1 = ""
			+ "1A 59 00 8A 9E 8A " // 3 uncompressed unicode chars
			+ "3C 00 " // Continue sid
			+ "10 00 " // rec size 16 (1+15)
			+ "00"	// next chunk is compressed
			+ "20 2D 20 4D 75 6C 74 69 6C 69 6E 67 75 61 6C " // 15 chars
	;
	/**
	 * same string re-arranged
	 */
	private static final String HED_DUMP2 = ""
			// 15 chars at end of current record
			+ "4D 75 6C 74 69 6C 69 6E 67 75 61 6C 20 2D 20"
			+ "3C 00 " // Continue sid
			+ "07 00 " // rec size 7 (1+6)
			+ "01"	// this bit uncompressed
			+ "1A 59 00 8A 9E 8A " // 3 uncompressed unicode chars
	;


	@Test
	void testChangeOfCompressionFlag_bug25866() {
		byte[] changingFlagSimpleData = HexRead.readFromString(""
				+ "AA AA "  // fake SID
				+ "06 00 "  // first rec len 6
				+ HED_DUMP1
				);
		RecordInputStream in = TestcaseRecordInputStream.create(changingFlagSimpleData);

		// bug 45866 - compressByte in continue records must be 1 while reading unicode LE string
		String actual  = in.readUnicodeLEString(18);
		assertEquals("\u591A\u8A00\u8A9E - Multilingual", actual);
	}

	@Test
	void testChangeFromUnCompressedToCompressed() {
		byte[] changingFlagSimpleData = HexRead.readFromString(""
				+ "AA AA "  // fake SID
				+ "0F 00 "  // first rec len 15
				+ HED_DUMP2
				);
		RecordInputStream in = TestcaseRecordInputStream.create(changingFlagSimpleData);
		String actual = in.readCompressedUnicode(18);
		assertEquals("Multilingual - \u591A\u8A00\u8A9E", actual);
	}

	@Test
	void testReadString() {
		byte[] changingFlagFullData = HexRead.readFromString(""
				+ "AA AA "  // fake SID
				+ "12 00 "  // first rec len 18 (15 + next 3 bytes)
				+ "12 00 "  // total chars 18
				+ "00 "	 // this bit compressed
				+ HED_DUMP2
				);
		RecordInputStream in = TestcaseRecordInputStream.create(changingFlagFullData);
		String actual = in.readString();
		assertEquals("Multilingual - \u591A\u8A00\u8A9E", actual);
	}

	@ParameterizedTest
	@CsvSource({"1, 200", "0, 200", "999999999, 200", HeaderRecord.sid+", 200"})
	void testLeftoverDataException(int sid, int remainingByteCount) {
	    // just ensure that the exception is created correctly, even with unknown sids
		assertDoesNotThrow(() -> new RecordInputStream.LeftoverDataException(sid, remainingByteCount));
	}
}
