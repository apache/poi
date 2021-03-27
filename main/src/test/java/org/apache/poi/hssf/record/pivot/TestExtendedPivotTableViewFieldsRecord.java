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

package org.apache.poi.hssf.record.pivot;

import static org.apache.poi.hssf.record.TestcaseRecordInputStream.confirmRecordEncoding;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.poi.hssf.record.RecordInputStream;
import org.apache.poi.hssf.record.TestcaseRecordInputStream;
import org.apache.poi.hssf.record.pivottable.ExtendedPivotTableViewFieldsRecord;
import org.apache.poi.util.HexRead;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ExtendedPivotTableViewFieldsRecord}
 */
final class TestExtendedPivotTableViewFieldsRecord {

	@Test
	void testSubNameNotPresent_bug46693() {
		// This data came from attachment 23347 of bug 46693 at offset 0xAA43
		byte[] data = HexRead.readFromString(
				"00 01 14 00" + // BIFF header
				"1E 14 00 0A FF FF FF FF 00 00 FF FF 00 00 00 00 00 00 00 00");
		RecordInputStream in = TestcaseRecordInputStream.create(data);
		// bug 46693a - Expected to find a ContinueRecord in order to read remaining 65535 of 65535 chars
		ExtendedPivotTableViewFieldsRecord rec = new ExtendedPivotTableViewFieldsRecord(in);

		assertEquals(data.length, rec.getRecordSize());
	}

	@SuppressWarnings("squid:S2699")
	@Test
	void testOlderFormat_bug46918() {
		// There are 10 SXVDEX records in the file (not uploaded) that originated bugzilla 46918
		// They all had the following hex encoding:
        byte[] data = HexRead.readFromString("00 01 0A 00 1E 14 00 0A FF FF FF FF 00 00");

		RecordInputStream in = TestcaseRecordInputStream.create(data);
		// bug 46918 - Not enough data (0) to read requested (2) bytes
		ExtendedPivotTableViewFieldsRecord rec = new ExtendedPivotTableViewFieldsRecord(in);

        byte[] expReserData = HexRead.readFromString("1E 14 00 0A FF FF FF FF 00 00" +
                "FF FF 00 00 00 00 00 00 00 00");

		confirmRecordEncoding(ExtendedPivotTableViewFieldsRecord.sid, expReserData, rec.serialize());
	}
}
