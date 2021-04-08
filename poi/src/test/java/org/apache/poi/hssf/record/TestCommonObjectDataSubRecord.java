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

import org.junit.jupiter.api.Test;

/**
 * Tests the serialization and deserialization of the {@link CommonObjectDataSubRecord}
 * class works correctly.  Test data taken directly from a real
 * Excel file.
 */
final class TestCommonObjectDataSubRecord {
	byte[] data = new byte[] {
		(byte)0x12,(byte)0x00,(byte)0x01,(byte)0x00,
		(byte)0x01,(byte)0x00,(byte)0x11,(byte)0x60,
		(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
		(byte)0x00,(byte)0x0D,(byte)0x26,(byte)0x01,
		(byte)0x00,(byte)0x00,
	};

	@Test
	void testLoad() {
		CommonObjectDataSubRecord record = new CommonObjectDataSubRecord(TestcaseRecordInputStream.createLittleEndian(data), data.length);

		assertEquals( CommonObjectDataSubRecord.OBJECT_TYPE_LIST_BOX, record.getObjectType());
		assertEquals((short) 1, record.getObjectId());
		assertEquals((short) 1, record.getOption());
        assertTrue(record.isLocked());
        assertFalse(record.isPrintable());
        assertFalse(record.isAutofill());
        assertFalse(record.isAutoline());
		assertEquals(24593, record.getReserved1());
		assertEquals(218103808, record.getReserved2());
		assertEquals(294, record.getReserved3());
		assertEquals(18, record.getDataSize());
	}

	@SuppressWarnings("squid:S2699")
	@Test
	void testStore() {
		CommonObjectDataSubRecord record = new CommonObjectDataSubRecord();

		record.setObjectType(CommonObjectDataSubRecord.OBJECT_TYPE_LIST_BOX);
		record.setObjectId( 1);
		record.setOption((short) 1);
		record.setLocked(true);
		record.setPrintable(false);
		record.setAutofill(false);
		record.setAutoline(false);
		record.setReserved1(24593);
		record.setReserved2(218103808);
		record.setReserved3(294);

		byte[] recordBytes = record.serialize();
		confirmRecordEncoding(CommonObjectDataSubRecord.sid, data, recordBytes);
	}
}
