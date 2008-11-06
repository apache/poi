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
public final class TestSupBookRecord extends TestCase {
    /**
     * This contains a fake data section of a SubBookRecord
     */
    byte[] dataIR = new byte[] {
        (byte)0x04,(byte)0x00,(byte)0x01,(byte)0x04,
    };
    byte[] dataAIF = new byte[] {
        (byte)0x01,(byte)0x00,(byte)0x01,(byte)0x3A,
    };
    byte[] dataER = new byte[] {
        (byte)0x02,(byte)0x00,
        (byte)0x07,(byte)0x00,   (byte)0x00,   
                (byte)'t', (byte)'e', (byte)'s', (byte)'t', (byte)'U', (byte)'R', (byte)'L',  
        (byte)0x06,(byte)0x00,   (byte)0x00,   
                (byte)'S', (byte)'h', (byte)'e', (byte)'e', (byte)'t', (byte)'1', 
        (byte)0x06,(byte)0x00,   (byte)0x00,   
                (byte)'S', (byte)'h', (byte)'e', (byte)'e', (byte)'t', (byte)'2', 
   };

    /**
     * tests that we can load the record
     */
    public void testLoadIR() {

        SupBookRecord record = new SupBookRecord(TestcaseRecordInputStream.create(0x01AE, dataIR));      
        assertTrue( record.isInternalReferences() );             //expected flag
        assertEquals( 0x4, record.getNumberOfSheets() );    //expected # of sheets

        assertEquals( 8, record.getRecordSize() );  //sid+size+data
    }
    /**
     * tests that we can load the record
     */
    public void testLoadER() {

        SupBookRecord record = new SupBookRecord(TestcaseRecordInputStream.create(0x01AE, dataER));      
        assertTrue( record.isExternalReferences() );             //expected flag
        assertEquals( 0x2, record.getNumberOfSheets() );    //expected # of sheets

        assertEquals( 34, record.getRecordSize() );  //sid+size+data
        
        assertEquals("testURL", record.getURL());
        String[] sheetNames = record.getSheetNames();
        assertEquals(2, sheetNames.length);
        assertEquals("Sheet1", sheetNames[0]);
        assertEquals("Sheet2", sheetNames[1]);
    }
    
    /**
     * tests that we can load the record
     */
    public void testLoadAIF() {

        SupBookRecord record = new SupBookRecord(TestcaseRecordInputStream.create(0x01AE, dataAIF));      
        assertTrue( record.isAddInFunctions() );             //expected flag
        assertEquals( 0x1, record.getNumberOfSheets() );    //expected # of sheets
        assertEquals( 8, record.getRecordSize() );  //sid+size+data
    }
   
    /**
     * Tests that we can store the record
     *
     */
    public void testStoreIR() {
        SupBookRecord record = SupBookRecord.createInternalReferences((short)4);

        TestcaseRecordInputStream.confirmRecordEncoding(0x01AE, dataIR, record.serialize());
    }   
    
    public void testStoreER() {
        String url = "testURL";
        String[] sheetNames = { "Sheet1", "Sheet2", };
        SupBookRecord record = SupBookRecord.createExternalReferences(url, sheetNames);

        TestcaseRecordInputStream.confirmRecordEncoding(0x01AE, dataER, record.serialize());
    }    
}
