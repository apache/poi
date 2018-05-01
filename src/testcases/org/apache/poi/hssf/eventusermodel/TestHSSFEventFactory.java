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

package org.apache.poi.hssf.eventusermodel;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.record.BoundSheetRecord;
import org.apache.poi.hssf.record.ContinueRecord;
import org.apache.poi.hssf.record.DVALRecord;
import org.apache.poi.hssf.record.DVRecord;
import org.apache.poi.hssf.record.EOFRecord;
import org.apache.poi.hssf.record.FeatHdrRecord;
import org.apache.poi.hssf.record.NumberRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.SelectionRecord;
import org.apache.poi.hssf.record.WindowTwoRecord;
import org.apache.poi.hssf.record.crypto.Biff8EncryptionKey;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.junit.After;

/**
 * Testing for {@link HSSFEventFactory}
 */
public final class TestHSSFEventFactory extends TestCase {
    private static InputStream openSample(String sampleFileName) {
        return HSSFTestDataSamples.openSampleFileStream(sampleFileName);
    }

    public void testWithMissingRecords() throws Exception {

        HSSFRequest req = new HSSFRequest();
        MockHSSFListener mockListen = new MockHSSFListener();
        req.addListenerForAllRecords(mockListen);

        POIFSFileSystem fs = new POIFSFileSystem(openSample("SimpleWithSkip.xls"));
        HSSFEventFactory factory = new HSSFEventFactory();
        factory.processWorkbookEvents(req, fs);

        Record[] recs = mockListen.getRecords();
        // Check we got the records
        assertTrue( recs.length > 100 );

        // Check that the last few records are as we expect
        // (Makes sure we don't accidently skip the end ones)
        int numRec = recs.length;
        assertEquals(WindowTwoRecord.class, recs[numRec-3].getClass());
        assertEquals(SelectionRecord.class, recs[numRec-2].getClass());
        assertEquals(EOFRecord.class,	   recs[numRec-1].getClass());
    }

    public void testWithCrazyContinueRecords() throws Exception {
        // Some files have crazy ordering of their continue records
        // Check that we don't break on them (bug #42844)

        HSSFRequest req = new HSSFRequest();
        MockHSSFListener mockListen = new MockHSSFListener();
        req.addListenerForAllRecords(mockListen);

        POIFSFileSystem fs = new POIFSFileSystem(openSample("ContinueRecordProblem.xls"));
        HSSFEventFactory factory = new HSSFEventFactory();
        factory.processWorkbookEvents(req, fs);

        Record[] recs = mockListen.getRecords();
        // Check we got the records
        assertTrue( recs.length > 100 );

        // And none of them are continue ones
        for (Record rec : recs) {
            assertFalse( rec instanceof ContinueRecord );
        }

        // Check that the last few records are as we expect
        // (Makes sure we don't accidently skip the end ones)
        int numRec = recs.length;
        assertEquals(DVALRecord.class,    recs[numRec-4].getClass());
        assertEquals(DVRecord.class,      recs[numRec-3].getClass());
        assertEquals(FeatHdrRecord.class, recs[numRec-2].getClass());
        assertEquals(EOFRecord.class,     recs[numRec-1].getClass());
    }

    /**
     * Unknown records can be continued.
     * Check that HSSFEventFactory doesn't break on them.
     * (the test file was provided in a reopen of bug #42844)
     */
    public void testUnknownContinueRecords() throws Exception {

        HSSFRequest req = new HSSFRequest();
        MockHSSFListener mockListen = new MockHSSFListener();
        req.addListenerForAllRecords(mockListen);

        POIFSFileSystem fs = new POIFSFileSystem(openSample("42844.xls"));
        HSSFEventFactory factory = new HSSFEventFactory();
        factory.processWorkbookEvents(req, fs);
    }

    private static class MockHSSFListener implements HSSFListener {
        private final List<Record> records = new ArrayList<>();

        public MockHSSFListener() {}
        public Record[] getRecords() {
            Record[] result = new Record[records.size()];
            records.toArray(result);
            return result;
        }

        @Override
        public void processRecord(Record record) {
            records.add(record);
        }
    }

    public void testWithDifferentWorkbookName() throws Exception {
        HSSFRequest req = new HSSFRequest();
        MockHSSFListener mockListen = new MockHSSFListener();
        req.addListenerForAllRecords(mockListen);

        POIFSFileSystem fs = new POIFSFileSystem(openSample("BOOK_in_capitals.xls"));
        HSSFEventFactory factory = new HSSFEventFactory();
        factory.processWorkbookEvents(req, fs);

        fs = new POIFSFileSystem(openSample("WORKBOOK_in_capitals.xls"));
        factory = new HSSFEventFactory();
        factory.processWorkbookEvents(req, fs);
    }

    public void testWithPasswordProtectedWorkbooks() throws Exception {
        HSSFRequest req = new HSSFRequest();
        MockHSSFListener mockListen = new MockHSSFListener();
        req.addListenerForAllRecords(mockListen);

        // Without a password, can't be read
        POIFSFileSystem fs = new POIFSFileSystem(openSample("xor-encryption-abc.xls"));

        HSSFEventFactory factory = new HSSFEventFactory();
        try {
            factory.processWorkbookEvents(req, fs);
            fail("Shouldn't be able to process protected workbook without the password");
        } catch (EncryptedDocumentException e) {}


        // With the password, is properly processed
        Biff8EncryptionKey.setCurrentUserPassword("abc");
        try {
            req = new HSSFRequest();
            mockListen = new MockHSSFListener();
            req.addListenerForAllRecords(mockListen);
            factory.processWorkbookEvents(req, fs);

            // Check we got the sheet and the contents
            Record[] recs = mockListen.getRecords();
            assertTrue(recs.length > 50);

            // Has one sheet, with values 1,2,3 in column A rows 1-3
            boolean hasSheet = false, hasA1 = false, hasA2 = false, hasA3 = false;
            for (Record r : recs) {
                if (r instanceof BoundSheetRecord) {
                    BoundSheetRecord bsr = (BoundSheetRecord) r;
                    assertEquals("Sheet1", bsr.getSheetname());
                    hasSheet = true;
                }
                if (r instanceof NumberRecord) {
                    NumberRecord nr = (NumberRecord) r;
                    if (nr.getColumn() == 0 && nr.getRow() == 0) {
                        assertEquals(1, (int) nr.getValue());
                        hasA1 = true;
                    }
                    if (nr.getColumn() == 0 && nr.getRow() == 1) {
                        assertEquals(2, (int) nr.getValue());
                        hasA2 = true;
                    }
                    if (nr.getColumn() == 0 && nr.getRow() == 2) {
                        assertEquals(3, (int) nr.getValue());
                        hasA3 = true;
                    }
                }
            }

            assertTrue("Sheet record not found", hasSheet);
            assertTrue("Numeric record for A1 not found", hasA1);
            assertTrue("Numeric record for A2 not found", hasA2);
            assertTrue("Numeric record for A3 not found", hasA3);
        } finally {
            Biff8EncryptionKey.setCurrentUserPassword(null);
        }
    }
}	
