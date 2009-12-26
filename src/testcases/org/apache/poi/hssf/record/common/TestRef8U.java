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

package org.apache.poi.hssf.record.common;

import java.io.ByteArrayOutputStream;

import org.apache.poi.hssf.record.TestcaseRecordInputStream;
import org.apache.poi.util.LittleEndianOutputStream;

import junit.framework.TestCase;

public final class TestRef8U extends TestCase {
 byte[] data = new byte[] {
     (byte)0x02,(byte)0x00, 
     (byte)0x04,(byte)0x00, 
     (byte)0x00,(byte)0x00, 
     (byte)0x03,(byte)0x00, 
 };

 public void testLoad() {
    Ref8U ref = new Ref8U(
          TestcaseRecordInputStream.create(0x000, data)
    );
    assertEquals(2, ref.getFirstRow());
    assertEquals(4, ref.getLastRow());
    assertEquals(0, ref.getFirstCol());
    assertEquals(3, ref.getLastCol());
  
    assertEquals( 8, Ref8U.getDataSize() );
 }

 public void testStore()
 {
	Ref8U ref = new Ref8U();
	
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
    ref.setFirstCol((short)0);
    ref.setLastCol((short)3);
	
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
