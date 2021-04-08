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

import org.apache.poi.util.HexRead;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link StyleRecord}
 */
final class TestStyleRecord {
	@Test
	void testUnicodeReadName() {
		byte[] data = HexRead.readFromString(
				"11 00 09 00 01 38 5E C4 89 5F 00 53 00 68 00 65 00 65 00 74 00 31 00");
		RecordInputStream in = TestcaseRecordInputStream.create(StyleRecord.sid, data);
		StyleRecord sr = new StyleRecord(in);
		assertEquals("\u5E38\u89C4_Sheet1", sr.getName()); // "<Conventional>_Sheet1"
		// bug 46385 - Incorrect number of bytes written - expected 27 but got 18
		byte[] ser = sr.serialize();
		confirmRecordEncoding(StyleRecord.sid, data, ser);
	}
}
