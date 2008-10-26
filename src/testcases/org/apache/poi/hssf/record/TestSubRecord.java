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
 * Tests Subrecord components of an OBJ record.  Test data taken directly
 * from a real Excel file.
 *
 * @author Michael Zalewski (zalewski@optonline.net)
 */
public final class TestSubRecord extends TestCase {
	/*
	   The following is a dump of the OBJ record corresponding to an auto-filter
	   drop-down list. The 3rd subrecord beginning at offset 0x002e (type=0x0013)
	   does not conform to the documentation, because the length field is 0x1fee,
	   which is longer than the entire OBJ record.

	   00000000 15 00 12 00 14 00 01 00 01 21 00 00 00 00 3C 13 .........!....<.  Type=0x15 Len=0x0012 ftCmo
	   00000010 F4 03 00 00 00 00
	                              0C 00 14 00 00 00 00 00 00 00 ................  Type=0x0c Len=0x0014 ftSbs
	   00000020 00 00 00 00 01 00 08 00 00 00 10 00 00 00
	                                                      13 00 ................  Type=0x13 Len=0x1FEE ftLbsData
	   00000030 EE 1F 00 00 08 00 08 00 01 03 00 00 0A 00 14 00 ................
	   00000040 6C 00
	                  00 00 00 00                               l.....            Type=0x00 Len=0x0000 ftEnd
	*/

	private static final byte[] dataAutoFilter 
		= HexRead.readFromString(""
			+ "5D 00 46 00 " // ObjRecord.sid, size=70
			// ftCmo
			+ "15 00 12 00 "
			+ "14 00 01 00 01 00 01 21 00 00 3C 13 F4 03 00 00 00 00 "
			// ftSbs (currently UnknownSubrecord)
			+ "0C 00 14 00 "
			+ "00 00 00 00 00 00 00 00 00 00 01 00 08 00 00 00 10 00 00 00 "
			// ftLbsData (currently UnknownSubrecord)
			+ "13 00 EE 1F 00 00 "
			+ "08 00 08 00 01 03 00 00 0A 00 14 00 6C 00 "
			// ftEnd
			+ "00 00 00 00"
		);


	/**
	 * Make sure that ftLbsData (which has abnormal size info) is parsed correctly.
	 * If the size field is interpreted incorrectly, the resulting ObjRecord becomes way too big.
	 * At the time of fixing (Oct-2008 svn r707447) {@link RecordInputStream} allowed  buffer 
	 * read overruns, so the bug was mostly silent.
	 */
	public void testReadAll_bug45778() {
		RecordInputStream in = TestcaseRecordInputStream.create(dataAutoFilter);
		ObjRecord or = new ObjRecord(in);
		byte[] data2 = or.serialize();
		if (data2.length == 8228) {
			throw new AssertionFailedError("Identified bug 45778");
		}
		assertEquals(74, data2.length);
		assertTrue(Arrays.equals(dataAutoFilter, data2));
	}
	
	public void testReadManualComboWithFormula() {
		byte[] data = HexRead.readFromString(""
   			+ "5D 00 66 00 "
   			+ "15 00 12 00 14 00 02 00 11 20 00 00 00 00 "
   			+ "20 44 C6 04 00 00 00 00 0C 00 14 00 04 F0 C6 04 "
   			+ "00 00 00 00 00 00 01 00 06 00 00 00 10 00 00 00 "
   			+ "0E 00 0C 00 05 00 80 44 C6 04 24 09 00 02 00 02 "
   			+ "13 00 DE 1F 10 00 09 00 80 44 C6 04 25 0A 00 0F "
   			+ "00 02 00 02 00 02 06 00 03 00 08 00 00 00 00 00 "
   			+ "08 00 00 00 00 00 00 00 " // TODO sometimes last byte is non-zero
   		);
		
		RecordInputStream in = TestcaseRecordInputStream.create(data);
		ObjRecord or = new ObjRecord(in);
		byte[] data2 = or.serialize();
		if (data2.length == 8228) {
			throw new AssertionFailedError("Identified bug XXXXX");
		}
		assertEquals("Encoded length", data.length, data2.length);
		for (int i = 0; i < data.length; i++) {
			if (data[i] != data2[i]) {
				throw new AssertionFailedError("Encoded data differs at index " + i);
			}
		}
		assertTrue(Arrays.equals(data, data2));
	}
}
