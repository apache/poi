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

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.poi.hssf.record.RecordInputStream;
import org.apache.poi.hssf.record.TestcaseRecordInputStream;
import org.apache.poi.hssf.record.pivottable.ViewFieldsRecord;
import org.apache.poi.util.HexRead;

/**
 * Tests for {@link ViewFieldsRecord}
 * 
 * @author Josh Micich
 */
public final class TestViewFieldsRecord extends TestCase {
	
	public void testUnicodeFlag_bug46693() {
		byte[] data = HexRead.readFromString("01 00 01 00 01 00 04 00 05 00 00 6D 61 72 63 6F");
		RecordInputStream in = TestcaseRecordInputStream.create(ViewFieldsRecord.sid, data);
		ViewFieldsRecord rec = new ViewFieldsRecord(in);
		if (in.remaining() == 1) {
			throw new AssertionFailedError("Identified bug 46693b");
		}
		assertEquals(0, in.remaining());
		assertEquals(4+data.length, rec.getRecordSize());
	}
	
	public void testSerialize() {
		// This hex data was produced by changing the 'Custom Name' property, 
		// available under 'Field Settings' from the 'PivotTable Field List' (Excel 2007)
		confirmSerialize("00 00 01 00 01 00 00 00 FF FF");
		confirmSerialize("01 00 01 00 01 00 04 00 05 00 00 6D 61 72 63 6F");
		confirmSerialize("01 00 01 00 01 00 04 00 0A 00 01 48 00 69 00 73 00 74 00 6F 00 72 00 79 00 2D 00 82 69 81 89");
	}

	private static ViewFieldsRecord confirmSerialize(String hexDump) {
		byte[] data = HexRead.readFromString(hexDump);
		RecordInputStream in = TestcaseRecordInputStream.create(ViewFieldsRecord.sid, data);
		ViewFieldsRecord rec = new ViewFieldsRecord(in);
		assertEquals(0, in.remaining());
		assertEquals(4+data.length, rec.getRecordSize());
		byte[] data2 = rec.serialize();
		TestcaseRecordInputStream.confirmRecordEncoding(ViewFieldsRecord.sid, data, data2);
		return rec;
	}
}
