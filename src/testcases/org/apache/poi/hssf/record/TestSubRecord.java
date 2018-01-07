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


import static org.junit.Assert.assertArrayEquals;

import org.apache.poi.util.HexRead;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.RecordFormatException;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

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
		assertArrayEquals(dataAutoFilter, data2);
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
			throw new AssertionFailedError("Identified bug 45778");
		}
		assertEquals("Encoded length", data.length, data2.length);
		for (int i = 0; i < data.length; i++) {
			if (data[i] != data2[i]) {
				throw new AssertionFailedError("Encoded data differs at index " + i);
			}
		}
		assertArrayEquals(data, data2);
	}

	/**
	 * Some versions of POI (e.g. 3.1 - prior to svn r707450 / bug 45778) interpreted the ftLbs 
	 * subrecord second short (0x1FEE) as a length, and hence read lots of extra padding.  This 
	 * buffer-overrun in {@link RecordInputStream} happened silently due to problems later fixed
	 * in svn 707778. When the ObjRecord is written, the extra padding is written too, making the 
	 * record 8224 bytes long instead of 70.  
	 * (An aside: It seems more than a coincidence that this problem creates a record of exactly
	 * {@link RecordInputStream#MAX_RECORD_DATA_SIZE} but not enough is understood about 
	 * subrecords to explain this.)<br>
	 * 
	 * Excel reads files with this excessive padding OK.  It also truncates the over-sized
	 * ObjRecord back to the proper size.  POI should do the same.
	 */
	public void testWayTooMuchPadding_bug46545() {
		byte[] data = HexRead.readFromString(""
			+ "15 00 12 00 14 00 13 00 01 21 00 00 00"
			+ "00 98 0B 5B 09 00 00 00 00 0C 00 14 00 00 00 00 00 00 00 00"
			+ "00 00 00 01 00 01 00 00 00 10 00 00 00 "
			// ftLbs
			+ "13 00 EE 1F 00 00 "
			+ "01 00 00 00 01 06 00 00 02 00 08 00 75 00 "
			// ftEnd
			+ "00 00 00 00"
		);
		final int LBS_START_POS = 0x002E;
		final int WRONG_LBS_SIZE = 0x1FEE;
		assertEquals(0x0013, LittleEndian.getShort(data, LBS_START_POS+0));
		assertEquals(WRONG_LBS_SIZE, LittleEndian.getShort(data, LBS_START_POS+2));
		int wrongTotalSize = LBS_START_POS + 4 + WRONG_LBS_SIZE;
		byte[] wrongData = new byte[wrongTotalSize];
		System.arraycopy(data, 0, wrongData, 0, data.length);
		// wrongData has the ObjRecord data as would have been written by v3.1 
		
		RecordInputStream in = TestcaseRecordInputStream.create(ObjRecord.sid, wrongData);
		ObjRecord or;
		try {
			or = new ObjRecord(in);
		} catch (RecordFormatException e) {
			if (e.getMessage().startsWith("Leftover 8154 bytes in subrecord data")) {
				throw new AssertionFailedError("Identified bug 46545");
			}
			throw e;
		}
		// make sure POI properly truncates the ObjRecord data
		byte[] data2 = or.serialize();
		TestcaseRecordInputStream.confirmRecordEncoding(ObjRecord.sid, data, data2);
	}
}
