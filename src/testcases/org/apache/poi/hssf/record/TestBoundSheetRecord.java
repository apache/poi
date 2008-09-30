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

import java.util.ArrayList;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

/**
 * Tests BoundSheetRecord.
 *
 * @see BoundSheetRecord
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public final class TestBoundSheetRecord extends TestCase {


    public void testRecordLength() {
        BoundSheetRecord record = new BoundSheetRecord("Sheet1");
        assertEquals(18, record.getRecordSize());
    }

    public void testWideRecordLength() {
        BoundSheetRecord record = new BoundSheetRecord("Sheet\u20ac");
        assertEquals(24, record.getRecordSize());
    }

    public void testName() {
        BoundSheetRecord record = new BoundSheetRecord("1234567890223456789032345678904");

        try {
            record.setSheetname("s//*s");
            throw new AssertionFailedError("Should have thrown IllegalArgumentException, but didnt");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }
    
    public void testDeserializeUnicode() {
       
		byte[] data = {
//			(byte)0x85, 0x00,       // sid
//			0x1a, 0x00, 			// length
			0x3C, 0x09, 0x00, 0x00, // bof
			0x00, 0x00, 			// flags
			0x09, 					// len( str )
			0x01, 					// unicode
			// <str>
			0x21, 0x04, 0x42, 0x04, 0x40, 0x04, 
			0x30, 0x04, 0x3D, 0x04, 0x38, 0x04,
			0x47, 0x04, 0x3A, 0x04, 0x30, 0x04
			// </str>
		};
	
		RecordInputStream in = new TestcaseRecordInputStream(BoundSheetRecord.sid, data);
		BoundSheetRecord bsr = new BoundSheetRecord(in);
		// sheet name is unicode Russian for 'minor page'
		assertEquals("\u0421\u0442\u0440\u0430\u043D\u0438\u0447\u043A\u0430", bsr.getSheetname());
		
	}

    public void testOrdering() {
    	BoundSheetRecord bs1 = new BoundSheetRecord("SheetB");
    	BoundSheetRecord bs2 = new BoundSheetRecord("SheetC");
    	BoundSheetRecord bs3 = new BoundSheetRecord("SheetA");
    	bs1.setPositionOfBof(11);
    	bs2.setPositionOfBof(33);
    	bs3.setPositionOfBof(22);

    	ArrayList l = new ArrayList();
    	l.add(bs1);
    	l.add(bs2);
    	l.add(bs3);

    	BoundSheetRecord[] r = BoundSheetRecord.orderByBofPosition(l);
    	assertEquals(3, r.length);
    	assertEquals(bs1, r[0]);
    	assertEquals(bs3, r[1]);
    	assertEquals(bs2, r[2]);
    }
}
