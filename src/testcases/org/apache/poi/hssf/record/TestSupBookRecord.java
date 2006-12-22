
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
 * Tests the serialization and deserialization of the SupBook record
 * class works correctly.  
 *
 * @author Andrew C. Oliver (acoliver at apache dot org)
 */
public class TestSupBookRecord
        extends TestCase
{
    /**
     * This contains a fake data section of a SubBookRecord
     */
    byte[] data = new byte[] {
        (byte)0x04,(byte)0x00,(byte)0x01,(byte)0x04
    };

    public TestSupBookRecord(String name)
    {
        super(name);
    }

    /**
     * tests that we can load the record
     */
    public void testLoad()
            throws Exception
    {

        SupBookRecord record = new SupBookRecord(new TestcaseRecordInputStream((short)0x01AE, (short)data.length, data));      
        assertEquals( 0x401, record.getFlag());             //expected flag
        assertEquals( 0x4, record.getNumberOfSheets() );    //expected # of sheets

        assertEquals( 8, record.getRecordSize() );  //sid+size+data

        record.validateSid((short)0x01AE);
    }
    
    
    /**
     * Tests that we can store the record
     *
     */
    public void testStore()
    {
        SupBookRecord record = new SupBookRecord();
        record.setFlag( (short) 0x401 );
        record.setNumberOfSheets( (short)0x4 );
        


        byte [] recordBytes = record.serialize();
        assertEquals(recordBytes.length - 4, data.length);
        for (int i = 0; i < data.length; i++)
            assertEquals("At offset " + i, data[i], recordBytes[i+4]);
    }    
    
    public static void main(String [] args) {
        System.out
        .println("Testing org.apache.poi.hssf.record.SupBookRecord");
        junit.textui.TestRunner.run(TestSupBookRecord.class);
    }
    

}
