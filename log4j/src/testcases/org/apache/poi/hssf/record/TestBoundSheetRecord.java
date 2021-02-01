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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.util.HexRead;
import org.junit.jupiter.api.Test;

/**
 * Tests BoundSheetRecord.
 */
final class TestBoundSheetRecord {

	@Test
	void testRecordLength() {
		BoundSheetRecord record = new BoundSheetRecord("Sheet1");
		assertEquals(18, record.getRecordSize());
	}

	@Test
	void testWideRecordLength() {
		BoundSheetRecord record = new BoundSheetRecord("Sheet\u20ac");
		assertEquals(24, record.getRecordSize());
	}

	@Test
	void testName() {
		BoundSheetRecord record = new BoundSheetRecord("1234567890223456789032345678904");
		assertThrows(IllegalArgumentException.class, () -> record.setSheetname("s//*s"));
	}

	@Test
	void testDeserializeUnicode() {

		byte[] data = HexRead.readFromString(""
			+ "85 00 1A 00" // sid, length
			+ "3C 09 00 00" // bof
			+ "00 00"// flags
			+ "09 01" // str-len. unicode flag
			// string data
			+ "21 04 42 04 40 04"
			+ "30 04 3D 04 38 04"
			+ "47 04 3A 04 30 04"
		);

		RecordInputStream in = TestcaseRecordInputStream.create(data);
		BoundSheetRecord bsr = new BoundSheetRecord(in);
		// sheet name is unicode Russian for 'minor page'
		assertEquals("\u0421\u0442\u0440\u0430\u043D\u0438\u0447\u043A\u0430", bsr.getSheetname());

		byte[] data2 = bsr.serialize();
		assertArrayEquals(data, data2);
	}

	@Test
	void testOrdering() {
		BoundSheetRecord bs1 = new BoundSheetRecord("SheetB");
		BoundSheetRecord bs2 = new BoundSheetRecord("SheetC");
		BoundSheetRecord bs3 = new BoundSheetRecord("SheetA");
		bs1.setPositionOfBof(11);
		bs2.setPositionOfBof(33);
		bs3.setPositionOfBof(22);

		List<BoundSheetRecord> l = new ArrayList<>();
		l.add(bs1);
		l.add(bs2);
		l.add(bs3);

		BoundSheetRecord[] r = BoundSheetRecord.orderByBofPosition(l);
		assertEquals(3, r.length);
		assertEquals(bs1, r[0]);
		assertEquals(bs3, r[1]);
		assertEquals(bs2, r[2]);
	}

	@Test
	void testValidNames() {
		assertTrue(isValid("Sheet1"));
		assertTrue(isValid("O'Brien's sales"));
		assertTrue(isValid(" data # "));
		assertTrue(isValid("data $1.00"));

		assertFalse(isValid("data?"));
		assertFalse(isValid("abc/def"));
		assertFalse(isValid("data[0]"));
		assertFalse(isValid("data*"));
		assertFalse(isValid("abc\\def"));
		assertFalse(isValid("'data"));
		assertFalse(isValid("data'"));
	}

	private static boolean isValid(String sheetName) {
		try {
			new BoundSheetRecord(sheetName);
			return true;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}
}
