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

import org.apache.poi.hssf.record.RecordInputStream.LeftoverDataException;
import org.apache.poi.util.HexRead;
/**
 * Tests for {@link BoolErrRecord}
 */
public final class TestBoolErrRecord extends TestCase {

	public void testError() {
		byte[] data = HexRead.readFromString(
				"00 00 00 00 0F 00 " + // row, col, xfIndex
				"07 01 " // #DIV/0!, isError
				);

		RecordInputStream in = TestcaseRecordInputStream.create(BoolErrRecord.sid, data);
		BoolErrRecord ber = new BoolErrRecord(in);
		assertTrue(ber.isError());
		assertEquals(7, ber.getErrorValue());
		
		TestcaseRecordInputStream.confirmRecordEncoding(BoolErrRecord.sid, data, ber.serialize());
	}

	/**
	 * Bugzilla 47479 was due to an apparent error in OOO which (as of version 3.0.1) 
	 * writes the <i>value</i> field of BOOLERR records as 2 bytes instead of 1.<br/>
	 * Coincidentally, the extra byte written is zero, which is exactly the value 
	 * required by the <i>isError</i> field.  This probably why Excel seems to have
	 * no problem.  OOO does not have the same bug for error values (which wouldn't
	 * work by the same coincidence). 
	 */
	public void testOooBadFormat_bug47479() {
		byte[] data = HexRead.readFromString(
				"05 02 09 00 " + // sid, size
				"00 00 00 00 0F 00 " + // row, col, xfIndex
				"01 00 00 " // extra 00 byte here
				);

		RecordInputStream in = TestcaseRecordInputStream.create(data);
		BoolErrRecord ber = new BoolErrRecord(in);
		boolean hasMore;
		try {
			hasMore = in.hasNextRecord();
		} catch (LeftoverDataException e) {
			if ("Initialisation of record 0x205 left 1 bytes remaining still to be read.".equals(e.getMessage())) {
				throw new AssertionFailedError("Identified bug 47479");
			}
			throw e;
		}
		assertFalse(hasMore);
		assertTrue(ber.isBoolean());
		assertEquals(true, ber.getBooleanValue());
		
		// Check that the record re-serializes correctly
		byte[] outData = ber.serialize();
		byte[] expData = HexRead.readFromString(
				"05 02 08 00 " +
				"00 00 00 00 0F 00 " +
				"01 00 " // normal number of data bytes
				);
		assertTrue(Arrays.equals(expData, outData));
	}
}
