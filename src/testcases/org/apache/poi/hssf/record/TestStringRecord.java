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


import junit.framework.TestCase;

/**
 * Tests the serialization and deserialization of the StringRecord
 * class works correctly.  Test data taken directly from a real
 * Excel file.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public class TestStringRecord extends TestCase {
    byte[] data = new byte[] {
        (byte)0x0B,(byte)0x00,   // length
        (byte)0x00,              // option
        // string
        (byte)0x46,(byte)0x61,(byte)0x68,(byte)0x72,(byte)0x7A,(byte)0x65,(byte)0x75,(byte)0x67,(byte)0x74,(byte)0x79,(byte)0x70
    };

    public void testLoad() {

        StringRecord record = new StringRecord(new TestcaseRecordInputStream((short)0x207, (short)data.length, data));
        assertEquals( "Fahrzeugtyp", record.getString());

        assertEquals( 18, record.getRecordSize() );
    }

    public void testStore()
    {
        StringRecord record = new StringRecord();
        record.setString("Fahrzeugtyp");

        byte [] recordBytes = record.serialize();
        assertEquals(recordBytes.length - 4, data.length);
        for (int i = 0; i < data.length; i++)
            assertEquals("At offset " + i, data[i], recordBytes[i+4]);
    }
}
