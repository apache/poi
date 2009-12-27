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

package org.apache.poi.ss.util;

import java.io.ByteArrayOutputStream;

import org.apache.poi.hssf.record.TestcaseRecordInputStream;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.LittleEndianOutputStream;

import junit.framework.TestCase;

public final class TestCellRangeAddress extends TestCase {
 byte[] data = new byte[] {
     (byte)0x02,(byte)0x00, 
     (byte)0x04,(byte)0x00, 
     (byte)0x00,(byte)0x00, 
     (byte)0x03,(byte)0x00, 
 };

 public void testLoad() {
	 CellRangeAddress ref = new CellRangeAddress(
          TestcaseRecordInputStream.create(0x000, data)
    );
    assertEquals(2, ref.getFirstRow());
    assertEquals(4, ref.getLastRow());
    assertEquals(0, ref.getFirstColumn());
    assertEquals(3, ref.getLastColumn());
  
    assertEquals( 8, CellRangeAddress.ENCODED_SIZE );
 }

 public void testStore()
 {
	 CellRangeAddress ref = new CellRangeAddress(0,0,0,0);
	
	byte[] recordBytes;
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	LittleEndianOutputStream out = new LittleEndianOutputStream(baos);
	
	// With nothing set
	ref.serialize(out);
	recordBytes = baos.toByteArray();
    assertEquals(recordBytes.length, data.length);
    for (int i = 0; i < data.length; i++) {
         assertEquals("At offset " + i, 0, recordBytes[i]);
    }
	
	// Now set the flags
    ref.setFirstRow((short)2);
    ref.setLastRow((short)4);
    ref.setFirstColumn((short)0);
    ref.setLastColumn((short)3);
	
	// Re-test
    baos.reset();
	ref.serialize(out);
	recordBytes = baos.toByteArray();
	 
    assertEquals(recordBytes.length, data.length);
    for (int i = 0; i < data.length; i++) {
         assertEquals("At offset " + i, data[i], recordBytes[i]);
    }
 }
}
