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

import org.apache.poi.hssf.record.common.UnicodeString;
import org.apache.poi.hssf.record.cont.ContinuableRecordOutput;
import org.apache.poi.util.IntMapper;

/**
 * Tests that records size calculates correctly.
 * 
 * @author Glen Stampoultzis (glens at apache.org)
 */
public final class TestSSTRecordSizeCalculator extends TestCase {
	private static final String SMALL_STRING = "Small string";
	private static final int COMPRESSED_PLAIN_STRING_OVERHEAD = 3;
	private static final int OPTION_FIELD_SIZE = 1;
	
	private final IntMapper<UnicodeString> strings = new IntMapper<UnicodeString>();

	private void confirmSize(int expectedSize) {
		ContinuableRecordOutput cro = ContinuableRecordOutput.createForCountingOnly();
		SSTSerializer ss = new SSTSerializer(strings, 0, 0);
		ss.serialize(cro);
		assertEquals(expectedSize, cro.getTotalSize());
	}

	public void testBasic() {
		strings.add(makeUnicodeString(SMALL_STRING));
		confirmSize(SSTRecord.SST_RECORD_OVERHEAD
				+ COMPRESSED_PLAIN_STRING_OVERHEAD
				+ SMALL_STRING.length());
	}

	public void testBigStringAcrossUnicode() {
		int bigString = SSTRecord.MAX_DATA_SPACE + 100;
		strings.add(makeUnicodeString(bigString));
		confirmSize(SSTRecord.SST_RECORD_OVERHEAD
				+ COMPRESSED_PLAIN_STRING_OVERHEAD
				+ SSTRecord.MAX_DATA_SPACE
				+ SSTRecord.STD_RECORD_OVERHEAD
				+ OPTION_FIELD_SIZE
				+ 100);
	}

	public void testPerfectFit() {
		int perfectFit = SSTRecord.MAX_DATA_SPACE - COMPRESSED_PLAIN_STRING_OVERHEAD;
		strings.add(makeUnicodeString(perfectFit));
		confirmSize(SSTRecord.SST_RECORD_OVERHEAD
				+ COMPRESSED_PLAIN_STRING_OVERHEAD
				+ perfectFit);
	}

	public void testJustOversized() {
		int tooBig = SSTRecord.MAX_DATA_SPACE - COMPRESSED_PLAIN_STRING_OVERHEAD + 1;
		strings.add(makeUnicodeString(tooBig));
		confirmSize(SSTRecord.SST_RECORD_OVERHEAD
				+ COMPRESSED_PLAIN_STRING_OVERHEAD
				+ tooBig - 1
				// continue record
				+ SSTRecord.STD_RECORD_OVERHEAD
				+ OPTION_FIELD_SIZE + 1);

	}

	public void testSecondStringStartsOnNewContinuation() {
		int perfectFit = SSTRecord.MAX_DATA_SPACE - COMPRESSED_PLAIN_STRING_OVERHEAD;
		strings.add(makeUnicodeString(perfectFit));
		strings.add(makeUnicodeString(SMALL_STRING));
		confirmSize(SSTRecord.SST_RECORD_OVERHEAD
				+ SSTRecord.MAX_DATA_SPACE
				// second string
				+ SSTRecord.STD_RECORD_OVERHEAD
				+ COMPRESSED_PLAIN_STRING_OVERHEAD
				+ SMALL_STRING.length());
	}

	public void testHeaderCrossesNormalContinuePoint() {
		int almostPerfectFit = SSTRecord.MAX_DATA_SPACE - COMPRESSED_PLAIN_STRING_OVERHEAD - 2;
		strings.add(makeUnicodeString(almostPerfectFit));
		String oneCharString = new String(new char[1]);
		strings.add(makeUnicodeString(oneCharString));
		confirmSize(SSTRecord.SST_RECORD_OVERHEAD
				+ COMPRESSED_PLAIN_STRING_OVERHEAD
				+ almostPerfectFit
				// second string
				+ SSTRecord.STD_RECORD_OVERHEAD
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
