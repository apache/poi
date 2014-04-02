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
 *
 * @author Josh Micich
 */
public final class TestRecalcIdRecord extends TestCase {

	private static RecalcIdRecord create(byte[] data) {
		RecordInputStream in = TestcaseRecordInputStream.create(RecalcIdRecord.sid, data);
		RecalcIdRecord result = new RecalcIdRecord(in);
		assertEquals(0, in.remaining());
		return result;
	}
	public void testBasicDeserializeReserialize() {
		
		byte[] data = HexRead.readFromString(
				"C1 01" +  // rt
				"00 00" +  // reserved
				"1D EB 01 00"); // engine id

		RecalcIdRecord r = create(data);
		TestcaseRecordInputStream.confirmRecordEncoding(RecalcIdRecord.sid, data, r.serialize());
	}

	public void testBadFirstField_bug48096() {
		/**
		 * Data taken from the sample file referenced in Bugzilla 48096, file offset 0x0D45.
		 * The apparent problem is that the first data short field has been written with the
		 * wrong <i>endianness</n>.  Excel seems to ignore whatever value is present in this
		 * field, and always rewrites it as (C1, 01). POI now does the same.
		 */
		byte[] badData  = HexRead.readFromString("C1 01 08 00 01 C1 00 00 00 01 69 61");
		byte[] goodData = HexRead.readFromString("C1 01 08 00 C1 01 00 00 00 01 69 61");

		RecordInputStream in = TestcaseRecordInputStream.create(badData);
		RecalcIdRecord r;
		try {
			r = new RecalcIdRecord(in);
		} catch (RecordFormatException e) {
			if (e.getMessage().equals("expected 449 but got 49409")) {
				throw new AssertionFailedError("Identified bug 48096");
			}
			throw e;
		}
		assertEquals(0, in.remaining());
		assertTrue(Arrays.equals(r.serialize(), goodData));
	}
}
