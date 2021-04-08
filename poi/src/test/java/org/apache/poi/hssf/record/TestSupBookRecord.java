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


import static org.apache.poi.hssf.record.SupBookRecord.CH_ALT_STARTUP_DIR;
import static org.apache.poi.hssf.record.SupBookRecord.CH_DOWN_DIR;
import static org.apache.poi.hssf.record.SupBookRecord.CH_LIB_DIR;
import static org.apache.poi.hssf.record.SupBookRecord.CH_SAME_VOLUME;
import static org.apache.poi.hssf.record.SupBookRecord.CH_STARTUP_DIR;
import static org.apache.poi.hssf.record.SupBookRecord.CH_UP_DIR;
import static org.apache.poi.hssf.record.SupBookRecord.CH_VOLUME;
import static org.apache.poi.hssf.record.SupBookRecord.PATH_SEPERATOR;
import static org.apache.poi.hssf.record.TestcaseRecordInputStream.confirmRecordEncoding;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Tests the serialization and deserialization of the SupBook record
 * class works correctly.
 */
final class TestSupBookRecord {
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
    @Test
    void testLoadIR() {
        SupBookRecord record = new SupBookRecord(TestcaseRecordInputStream.create(0x01AE, dataIR));
        assertTrue( record.isInternalReferences() );             //expected flag
        assertEquals( 0x4, record.getNumberOfSheets() );    //expected # of sheets

        assertEquals( 8, record.getRecordSize() );  //sid+size+data
    }

    /**
     * tests that we can load the record
     */
    @Test
    void testLoadER() {
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
    @Test
    void testLoadAIF() {
        SupBookRecord record = new SupBookRecord(TestcaseRecordInputStream.create(0x01AE, dataAIF));
        assertTrue( record.isAddInFunctions() );             //expected flag
        assertEquals( 0x1, record.getNumberOfSheets() );    //expected # of sheets
        assertEquals( 8, record.getRecordSize() );  //sid+size+data
    }

    /**
     * Tests that we can store the record
     */
    @SuppressWarnings("squid:S2699")
    @Test
    void testStoreIR() {
        SupBookRecord record = SupBookRecord.createInternalReferences((short)4);

        confirmRecordEncoding(0x01AE, dataIR, record.serialize());
    }

    @SuppressWarnings("squid:S2699")
    @Test
    void testStoreER() {
        String url = "testURL";
        String[] sheetNames = { "Sheet1", "Sheet2", };
        SupBookRecord record = SupBookRecord.createExternalReferences(url, sheetNames);

        confirmRecordEncoding(0x01AE, dataER, record.serialize());
    }

    @Test
    void testStoreAIF() {
        SupBookRecord record = SupBookRecord.createAddInFunctions();
        assertEquals(1, record.getNumberOfSheets());
        assertTrue(record.isAddInFunctions());
        confirmRecordEncoding(0x01AE, dataAIF, record.serialize());
    }

    @Test
    void testExternalReferenceUrl() {
    	String[] sheetNames = new String[]{"SampleSheet"};
    	final char startMarker = (char)1;

		SupBookRecord record;

		record = new SupBookRecord(startMarker + "test.xls", sheetNames);
    	assertEquals("test.xls", record.getURL());

    	//UNC path notation
    	record = new SupBookRecord(startMarker + "" + CH_VOLUME + "@servername" + CH_DOWN_DIR + "test.xls", sheetNames);
    	assertEquals("\\\\servername" + PATH_SEPERATOR + "test.xls", record.getURL());

    	//Absolute path notation - different device
    	record = new SupBookRecord(startMarker + "" + CH_VOLUME + "D" + CH_DOWN_DIR + "test.xls", sheetNames);
    	assertEquals("D:" + PATH_SEPERATOR + "test.xls", record.getURL());

    	//Absolute path notation - same device
    	record = new SupBookRecord(startMarker + "" + CH_SAME_VOLUME + "folder" + CH_DOWN_DIR + "test.xls", sheetNames);
    	assertEquals(PATH_SEPERATOR + "folder" + PATH_SEPERATOR + "test.xls", record.getURL());

    	//Relative path notation - down
    	record = new SupBookRecord(startMarker + "folder" + CH_DOWN_DIR + "test.xls", sheetNames);
    	assertEquals("folder" + PATH_SEPERATOR + "test.xls", record.getURL());

    	//Relative path notation - up
    	record = new SupBookRecord(startMarker +""+ CH_UP_DIR + "test.xls", sheetNames);
    	assertEquals(".." + PATH_SEPERATOR + "test.xls", record.getURL());

    	//Relative path notation - for EXCEL.exe - fallback
    	record = new SupBookRecord(startMarker +""+ CH_STARTUP_DIR + "test.xls", sheetNames);
    	assertEquals("." + PATH_SEPERATOR + "test.xls", record.getURL());

    	//Relative path notation - for EXCEL lib folder - fallback
    	record = new SupBookRecord(startMarker +""+ CH_LIB_DIR + "test.xls", sheetNames);
    	assertEquals("." + PATH_SEPERATOR + "test.xls", record.getURL());

    	//Relative path notation - for alternative EXCEL.exe - fallback
    	record = new SupBookRecord(startMarker +""+ CH_ALT_STARTUP_DIR+ "test.xls", sheetNames);
    	assertEquals("." + PATH_SEPERATOR + "test.xls", record.getURL());
    }
}
