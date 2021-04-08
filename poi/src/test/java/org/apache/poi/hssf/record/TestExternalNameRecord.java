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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.poi.util.HexDump;
import org.apache.poi.util.HexRead;
import org.junit.jupiter.api.Test;

final class TestExternalNameRecord {

	private static final byte[] dataFDS = {
		0, 0, 0, 0, 0, 0, 3, 0, 70, 68, 83, 0, 0,
	};

	// data taken from bugzilla 44774 att 21790
	private static final byte[] dataAutoDocName = {
		-22, 127, 0, 0, 0, 0, 29, 0, 39, 49, 57, 49, 50, 49, 57, 65, 87, 52, 32, 67, 111, 114,
		112, 44, 91, 87, 79, 82, 75, 79, 85, 84, 95, 80, 88, 93, 39,
	};

	// data taken from bugzilla 44774 att 21790
	private static final byte[] dataPlainName = {
		0, 0, 0, 0, 0, 0, 9, 0, 82, 97, 116, 101, 95, 68, 97, 116, 101, 9, 0, 58, 0, 0, 0, 0, 4, 0, 8, 0
		// TODO - the last 2 bytes of formula data (8,0) seem weird.  They encode to ConcatPtg, UnknownPtg
		// UnknownPtg is otherwise not created by any other test cases
	};

	private static ExternalNameRecord createSimpleENR(byte[] data) {
		return new ExternalNameRecord(TestcaseRecordInputStream.create(0x0023, data));
	}

	@Test
	void testBasicDeserializeReserialize() {

		ExternalNameRecord enr = createSimpleENR(dataFDS);
		assertEquals("FDS", enr.getText());

		// bug 44695
		confirmRecordEncoding(0x0023, dataFDS, enr.serialize());
	}

	@Test
	void testBasicSize() {
		ExternalNameRecord enr = createSimpleENR(dataFDS);
		assertNotEquals(13, enr.getRecordSize(), "Identified bug 44695");
		assertEquals(17, enr.getRecordSize());

		assertNotNull(enr.serialize());
	}

	@Test
	void testAutoStdDocName() {

		ExternalNameRecord enr = createSimpleENR(dataAutoDocName);
		assertEquals("'191219AW4 Corp,[WORKOUT_PX]'", enr.getText());
		assertTrue(enr.isAutomaticLink());
		assertFalse(enr.isBuiltInName());
		assertFalse(enr.isIconifiedPictureLink());
		assertFalse(enr.isOLELink());
		assertFalse(enr.isPicureLink());
		assertTrue(enr.isStdDocumentNameIdentifier());

		confirmRecordEncoding(0x0023, dataAutoDocName, enr.serialize());
	}

	@Test
	void testPlainName() {

		ExternalNameRecord enr = createSimpleENR(dataPlainName);
		assertEquals("Rate_Date", enr.getText());
		assertFalse(enr.isAutomaticLink());
		assertFalse(enr.isBuiltInName());
		assertFalse(enr.isIconifiedPictureLink());
		assertFalse(enr.isOLELink());
		assertFalse(enr.isPicureLink());
		assertFalse(enr.isStdDocumentNameIdentifier());

		confirmRecordEncoding(0x0023, dataPlainName, enr.serialize());
	}

	@Test
	void testDDELink_bug47229() {
		/*
		 * Hex dump read directly from text of bugzilla 47229
		 */
		final byte[] dataDDE = HexRead.readFromString(
				"E2 7F 00 00 00 00 " +
				"37 00 " + // text len
				// 010672AT0 MUNI,[RTG_MOODY_UNDERLYING,RTG_SP_UNDERLYING]
				"30 31 30 36 37 32 41 54 30 20 4D 55 4E 49 2C " +
				"5B 52 54 47 5F 4D 4F 4F 44 59 5F 55 4E 44 45 52 4C 59 49 4E 47 2C " +
				"52 54 47 5F 53 50 5F 55 4E 44 45 52 4C 59 49 4E 47 5D " +
				// constant array { { "#N/A N.A.", "#N/A N.A.", }, }
				" 01 00 00 " +
				"02 09 00 00 23 4E 2F 41 20 4E 2E 41 2E " +
				"02 09 00 00 23 4E 2F 41 20 4E 2E 41 2E");

		// actual msg reported in bugzilla 47229 is different
		// because that seems to be using a version from before svn r646666
		ExternalNameRecord enr = createSimpleENR(dataDDE);
		assertEquals("010672AT0 MUNI,[RTG_MOODY_UNDERLYING,RTG_SP_UNDERLYING]", enr.getText());

		confirmRecordEncoding(0x0023, dataDDE, enr.serialize());
	}

	@Test
	void testUnicodeName_bug47384() {
		// data taken from bugzilla 47384 att 23830 at offset 0x13A0
		byte[] dataUN = HexRead.readFromString(
				"23 00 22 00" +
				"00 00 00 00 00 00 " +
				"0C 01 " +
				"59 01 61 00 7A 00 65 00 6E 00 ED 00 5F 00 42 00 69 00 6C 00 6C 00 61 00" +
				"00 00");

		RecordInputStream in = TestcaseRecordInputStream.create(dataUN);
		ExternalNameRecord enr = new ExternalNameRecord(in);
		assertEquals("\u0159azen\u00ED_Billa", enr.getText());
        byte[] ser = enr.serialize();
        assertEquals(HexDump.toHex(dataUN), HexDump.toHex(ser));
	}

	@Test
    void test48339() {
        // data taken from bugzilla 48339
        byte[] data = HexRead.readFromString(
                "23 00 09 00" +
                "F4, FF, 14, 2D, 61, 01, 01, 00, 27");

        RecordInputStream in = TestcaseRecordInputStream.create(data);
        ExternalNameRecord enr = new ExternalNameRecord(in);
        byte[] ser = enr.serialize();
        assertEquals(HexDump.toHex(data), HexDump.toHex(ser));
    }

	@Test
    void testNPEWithFileFrom49219() {
        // the file at test-data/spreadsheet/49219.xls has ExternalNameRecords without actual data,
    	// we did handle this during reading, but failed during serializing this out, ensure it works now
        byte[] data = new byte[] {
        		2, 127, 0, 0, 0, 0,
        		9, 0, 82, 97, 116, 101, 95, 68, 97, 116, 101};

		ExternalNameRecord enr = createSimpleENR(data);

        byte[] ser = enr.serialize();
        assertEquals("[23, 00, 11, 00, 02, 7F, 00, 00, 00, 00, 09, 00, 52, 61, 74, 65, 5F, 44, 61, 74, 65]",
        		HexDump.toHex(ser));
    }
}
