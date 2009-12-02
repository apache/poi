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

import java.util.Arrays;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.poi.util.HexRead;

/**
 * Tests for {@link ColumnInfoRecord}
 *
 * @author Josh Micich
 */
public final class TestColumnInfoRecord extends TestCase {

	public void testBasic() {
		byte[] data = HexRead.readFromString("7D 00 0C 00 14 00 9B 00 C7 19 0F 00 01 13 00 00");

		RecordInputStream in = TestcaseRecordInputStream.create(data);
		ColumnInfoRecord cir = new ColumnInfoRecord(in);
		assertEquals(0, in.remaining());

		assertEquals(20, cir.getFirstColumn());
		assertEquals(155, cir.getLastColumn());
		assertEquals(6599, cir.getColumnWidth());
		assertEquals(15, cir.getXFIndex());
		assertEquals(true, cir.getHidden());
		assertEquals(3, cir.getOutlineLevel());
		assertEquals(true, cir.getCollapsed());
		assertTrue(Arrays.equals(data, cir.serialize()));
	}

	/**
	 * Some applications skip the last reserved field when writing {@link ColumnInfoRecord}s
	 * The attached file was apparently created by "SoftArtisans OfficeWriter for Excel".
	 * Excel reads that file OK and assumes zero for the value of the reserved field.
	 */
	public void testZeroResevedBytes_bug48332() {
		// Taken from bugzilla attachment 24661 (offset 0x1E73)
		byte[] inpData = HexRead.readFromString("7D 00 0A 00 00 00 00 00 D5 19 0F 00 02 00");
		byte[] outData = HexRead.readFromString("7D 00 0C 00 00 00 00 00 D5 19 0F 00 02 00 00 00");

		RecordInputStream in = TestcaseRecordInputStream.create(inpData);
		ColumnInfoRecord cir;
		try {
			cir = new ColumnInfoRecord(in);
		} catch (RuntimeException e) {
			if (e.getMessage().equals("Unusual record size remaining=(0)")) {
				throw new AssertionFailedError("Identified bug 48332");
			}
			throw e;
		}
		assertEquals(0, in.remaining());
		assertTrue(Arrays.equals(outData, cir.serialize()));
	}

	/**
	 * Some sample files have just one reserved byte (field 6):
	 * OddStyleRecord.xls, NoGutsRecords.xls, WORKBOOK_in_capitals.xls
	 * but this seems to cause no problem to Excel
	 */
	public void testOneReservedByte() {
		byte[] inpData = HexRead.readFromString("7D 00 0B 00 00 00 00 00 24 02 0F 00 00 00 01");
		byte[] outData = HexRead.readFromString("7D 00 0C 00 00 00 00 00 24 02 0F 00 00 00 01 00");
		RecordInputStream in = TestcaseRecordInputStream.create(inpData);
		ColumnInfoRecord cir = new ColumnInfoRecord(in);
		assertEquals(0, in.remaining());
		assertTrue(Arrays.equals(outData, cir.serialize()));
	}
}
