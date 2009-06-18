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

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
/**
 *
 * @author Josh Micich
 */
public final class TestExternalNameRecord extends TestCase {

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
	public void testBasicDeserializeReserialize() {

		ExternalNameRecord enr = createSimpleENR(dataFDS);
		assertEquals("FDS", enr.getText());

		try {
			TestcaseRecordInputStream.confirmRecordEncoding(0x0023, dataFDS, enr.serialize());
		} catch (ArrayIndexOutOfBoundsException e) {
			if(e.getMessage().equals("15")) {
				throw new AssertionFailedError("Identified bug 44695");
			}
		}
	}

	public void testBasicSize() {
		ExternalNameRecord enr = createSimpleENR(dataFDS);
		if(enr.getRecordSize() == 13) {
			throw new AssertionFailedError("Identified bug 44695");
		}
		assertEquals(17, enr.getRecordSize());
	}

	public void testAutoStdDocName() {

		ExternalNameRecord enr;
		try {
			enr = createSimpleENR(dataAutoDocName);
		} catch (ArrayIndexOutOfBoundsException e) {
			if(e.getMessage() == null) {
				throw new AssertionFailedError("Identified bug XXXX");
			}
			throw e;
		}
		assertEquals("'191219AW4 Corp,[WORKOUT_PX]'", enr.getText());
		assertTrue(enr.isAutomaticLink());
		assertFalse(enr.isBuiltInName());
		assertFalse(enr.isIconifiedPictureLink());
		assertFalse(enr.isOLELink());
		assertFalse(enr.isPicureLink());
		assertTrue(enr.isStdDocumentNameIdentifier());

		TestcaseRecordInputStream.confirmRecordEncoding(0x0023, dataAutoDocName, enr.serialize());
	}

	public void testPlainName() {

		ExternalNameRecord enr = createSimpleENR(dataPlainName);
		assertEquals("Rate_Date", enr.getText());
		assertFalse(enr.isAutomaticLink());
		assertFalse(enr.isBuiltInName());
		assertFalse(enr.isIconifiedPictureLink());
		assertFalse(enr.isOLELink());
		assertFalse(enr.isPicureLink());
		assertFalse(enr.isStdDocumentNameIdentifier());

		TestcaseRecordInputStream.confirmRecordEncoding(0x0023, dataPlainName, enr.serialize());
	}

	public void testDDELink_bug47229() {
		/**
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
		ExternalNameRecord enr;
		try {
			enr = createSimpleENR(dataDDE);
		} catch (RecordFormatException e) {
			// actual msg reported in bugzilla 47229 is different
			// because that seems to be using a version from before svn r646666
			if (e.getMessage().startsWith("Some unread data (is formula present?)")) {
				throw new AssertionFailedError("Identified bug 47229 - failed to read ENR with OLE/DDE result data");
			}
			throw e;
		}
		assertEquals("010672AT0 MUNI,[RTG_MOODY_UNDERLYING,RTG_SP_UNDERLYING]", enr.getText());

		TestcaseRecordInputStream.confirmRecordEncoding(0x0023, dataDDE, enr.serialize());
	}

	public void testUnicodeName_bug47384() {
		// data taken from bugzilla 47384 att 23830 at offset 0x13A0
		byte[] dataUN = HexRead.readFromString(
				"23 00 22 00" +
				"00 00 00 00 00 00 " +
				"0C 01 " +
				"59 01 61 00 7A 00 65 00 6E 00 ED 00 5F 00 42 00 69 00 6C 00 6C 00 61 00" +
				"00 00");

		RecordInputStream in = TestcaseRecordInputStream.create(dataUN);
		ExternalNameRecord enr;
		try {
			enr = new ExternalNameRecord(in);
		} catch (RecordFormatException e) {
			if (e.getMessage().startsWith("Expected to find a ContinueRecord in order to read remaining 242 of 268 chars")) {
				throw new AssertionFailedError("Identified bug 47384 - failed to read ENR with unicode name");
			}
			throw e;
		}
		assertEquals("\u0159azen\u00ED_Billa", enr.getText());
	}
}
