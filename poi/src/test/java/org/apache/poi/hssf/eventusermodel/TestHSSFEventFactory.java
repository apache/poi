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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.record.BoundSheetRecord;
import org.apache.poi.hssf.record.ContinueRecord;
import org.apache.poi.hssf.record.DVALRecord;
import org.apache.poi.hssf.record.DVRecord;
import org.apache.poi.hssf.record.EOFRecord;
import org.apache.poi.hssf.record.FeatHdrRecord;
import org.apache.poi.hssf.record.NumberRecord;
import org.apache.poi.hssf.record.SelectionRecord;
import org.apache.poi.hssf.record.WindowTwoRecord;
import org.apache.poi.hssf.record.crypto.Biff8EncryptionKey;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.junit.jupiter.api.Test;

/**
 * Testing for {@link HSSFEventFactory}
 */
final class TestHSSFEventFactory {
    private final List<org.apache.poi.hssf.record.Record> records = new ArrayList<>();

    private void openSample(String sampleFileName) throws IOException {
        records.clear();
        HSSFRequest req = new HSSFRequest();
        req.addListenerForAllRecords(records::add);
        try (InputStream is = HSSFTestDataSamples.openSampleFileStream(sampleFileName);
             POIFSFileSystem fs = new POIFSFileSystem(is)) {
            HSSFEventFactory factory = new HSSFEventFactory();
            factory.processWorkbookEvents(req, fs);
        }
    }

    @Test
    void testWithMissingRecords() throws Exception {

        openSample("SimpleWithSkip.xls");

        int numRec = records.size();

        // Check we got the records
        assertTrue( numRec > 100 );

        // Check that the last few records are as we expect
        // (Makes sure we don't accidently skip the end ones)
        Class<?>[] exp = { WindowTwoRecord.class, SelectionRecord.class, EOFRecord.class };
        Class<?>[] act = records.subList(numRec - 3, numRec).stream().map(Object::getClass).toArray(Class[]::new);
        assertArrayEquals(exp, act);
    }

    @Test
    void testWithCrazyContinueRecords() throws Exception {
        // Some files have crazy ordering of their continue records
        // Check that we don't break on them (bug #42844)

        openSample("ContinueRecordProblem.xls");

        int numRec = records.size();
        // Check we got the records
        assertTrue( numRec > 100 );

        // And none of them are continue ones
        assertFalse(records.stream().anyMatch(r -> r instanceof ContinueRecord));

        // Check that the last few records are as we expect
        // (Makes sure we don't accidentally skip the end ones)
        Class<?>[] exp = { DVALRecord.class, DVRecord.class, FeatHdrRecord.class, EOFRecord.class };
        Class<?>[] act = records.subList(numRec-4, numRec).stream().map(Object::getClass).toArray(Class[]::new);
        assertArrayEquals(exp, act);
    }

    /**
     * Unknown records can be continued.
     * Check that HSSFEventFactory doesn't break on them.
     * (the test file was provided in a reopen of bug #42844)
     */
    @Test
    @SuppressWarnings("java:S2699")
    void testUnknownContinueRecords() throws Exception {
        openSample("42844.xls");
    }

    @Test
    @SuppressWarnings("java:S2699")
    void testWithDifferentWorkbookName() throws Exception {
        openSample("BOOK_in_capitals.xls");
        openSample("WORKBOOK_in_capitals.xls");
    }

    @Test
    @SuppressWarnings("java:S2699")
    void testWithPasswordProtectedWorkbooksNoPass() {
        // Without a password, can't be read
        assertThrows(EncryptedDocumentException.class, () -> openSample("xor-encryption-abc.xls"));
    }

    @Test
    void testWithPasswordProtectedWorkbooks() throws Exception {
        // With the password, is properly processed
        Biff8EncryptionKey.setCurrentUserPassword("abc");
        try {
            openSample("xor-encryption-abc.xls");

            // Check we got the sheet and the contents
            assertTrue(records.size() > 50);

            // Has one sheet, with values 1,2,3 in column A rows 1-3
            boolean hasSheet = false, hasA1 = false, hasA2 = false, hasA3 = false;
            for (org.apache.poi.hssf.record.Record r : records) {
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

            assertTrue(hasSheet, "Sheet record not found");
            assertTrue(hasA1, "Numeric record for A1 not found");
            assertTrue(hasA2, "Numeric record for A2 not found");
            assertTrue(hasA3, "Numeric record for A3 not found");
        } finally {
            Biff8EncryptionKey.setCurrentUserPassword(null);
        }
    }
}
