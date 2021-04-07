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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.poi.hssf.record.common.UnicodeString;
import org.apache.poi.hssf.record.cont.ContinuableRecordOutput;
import org.apache.poi.util.IntMapper;
import org.apache.poi.util.LittleEndianConsts;
import org.junit.jupiter.api.Test;

/**
 * Tests that records size calculates correctly.
 */
final class TestSSTRecordSizeCalculator {
	private static final String SMALL_STRING = "Small string";
	private static final int COMPRESSED_PLAIN_STRING_OVERHEAD = 3;
	private static final int OPTION_FIELD_SIZE = 1;

	private final IntMapper<UnicodeString> strings = new IntMapper<>();


	/** standard record overhead: two shorts (record id plus data space size)*/
	private static final int STD_RECORD_OVERHEAD = 2 * LittleEndianConsts.SHORT_SIZE;

	/** SST overhead: the standard record overhead, plus the number of strings and the number of unique strings -- two ints */
	private static final int SST_RECORD_OVERHEAD = STD_RECORD_OVERHEAD + 2 * LittleEndianConsts.INT_SIZE;

	/** how much data can we stuff into an SST record? That would be _max minus the standard SST record overhead */
	private static final int MAX_DATA_SPACE = RecordInputStream.MAX_RECORD_DATA_SIZE - 8;


	private void confirmSize(int expectedSize) {
		ContinuableRecordOutput cro = ContinuableRecordOutput.createForCountingOnly();
		SSTSerializer ss = new SSTSerializer(strings, 0, 0);
		ss.serialize(cro);
		assertEquals(expectedSize, cro.getTotalSize());
	}

	@Test
	void testBasic() {
		strings.add(makeUnicodeString(SMALL_STRING));
		confirmSize(SST_RECORD_OVERHEAD
				+ COMPRESSED_PLAIN_STRING_OVERHEAD
				+ SMALL_STRING.length());
	}

	@Test
	void testBigStringAcrossUnicode() {
		int bigString = MAX_DATA_SPACE + 100;
		strings.add(makeUnicodeString(bigString));
		confirmSize(SST_RECORD_OVERHEAD
				+ COMPRESSED_PLAIN_STRING_OVERHEAD
				+ MAX_DATA_SPACE
				+ STD_RECORD_OVERHEAD
				+ OPTION_FIELD_SIZE
				+ 100);
	}

	@Test
	void testPerfectFit() {
		int perfectFit = MAX_DATA_SPACE - COMPRESSED_PLAIN_STRING_OVERHEAD;
		strings.add(makeUnicodeString(perfectFit));
		confirmSize(SST_RECORD_OVERHEAD
				+ COMPRESSED_PLAIN_STRING_OVERHEAD
				+ perfectFit);
	}

	@Test
	void testJustOversized() {
		int tooBig = MAX_DATA_SPACE - COMPRESSED_PLAIN_STRING_OVERHEAD + 1;
		strings.add(makeUnicodeString(tooBig));
		confirmSize(SST_RECORD_OVERHEAD
				+ COMPRESSED_PLAIN_STRING_OVERHEAD
				+ tooBig - 1
				// continue record
				+ STD_RECORD_OVERHEAD
				+ OPTION_FIELD_SIZE + 1);

	}

	@Test
	void testSecondStringStartsOnNewContinuation() {
		int perfectFit = MAX_DATA_SPACE - COMPRESSED_PLAIN_STRING_OVERHEAD;
		strings.add(makeUnicodeString(perfectFit));
		strings.add(makeUnicodeString(SMALL_STRING));
		confirmSize(SST_RECORD_OVERHEAD
				+ MAX_DATA_SPACE
				// second string
				+ STD_RECORD_OVERHEAD
				+ COMPRESSED_PLAIN_STRING_OVERHEAD
				+ SMALL_STRING.length());
	}

	@Test
	void testHeaderCrossesNormalContinuePoint() {
		int almostPerfectFit = MAX_DATA_SPACE - COMPRESSED_PLAIN_STRING_OVERHEAD - 2;
		strings.add(makeUnicodeString(almostPerfectFit));
		String oneCharString = new String(new char[1]);
		strings.add(makeUnicodeString(oneCharString));
		confirmSize(SST_RECORD_OVERHEAD
				+ COMPRESSED_PLAIN_STRING_OVERHEAD
				+ almostPerfectFit
				// second string
				+ STD_RECORD_OVERHEAD
				+ COMPRESSED_PLAIN_STRING_OVERHEAD
				+ oneCharString.length());

	}

	private static UnicodeString makeUnicodeString(int size) {
		String s = new String(new char[size]);
		return makeUnicodeString(s);
	}

	private static UnicodeString makeUnicodeString(String s) {
		UnicodeString st = new UnicodeString(s);
		st.setOptionFlags((byte) 0);
		return st;
	}
}
