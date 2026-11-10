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

import static org.apache.poi.hssf.record.TestcaseRecordInputStream.confirmRecordEncoding;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.poi.util.HexRead;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link FileSharingRecord}
 */
final class TestFileSharingRecord {

    /** user name stored as 8-bit characters (fHighByte = 0): "acns" */
    private static final byte[] DATA_COMPRESSED = HexRead.readFromString(
        "00 00 6F CC 04 00 00 61 63 6E 73");

    /** user name stored as UTF-16LE (fHighByte = 1): Korean "홍길동" */
    private static final byte[] DATA_UNICODE = HexRead.readFromString(
        "00 00 6F CC 03 00 01 4D D6 38 AE D9 B3");

    /** zero length user name, no fHighByte */
    private static final byte[] DATA_EMPTY = HexRead.readFromString(
        "01 00 6F CC 00 00");

    @Test
    void compressedUsername() {
        RecordInputStream in = TestcaseRecordInputStream.create(FileSharingRecord.sid, DATA_COMPRESSED);
        FileSharingRecord rec = new FileSharingRecord(in);
        assertEquals(0, in.remaining());
        assertEquals("acns", rec.getUsername());
        confirmRecordEncoding(FileSharingRecord.sid, DATA_COMPRESSED, rec.serialize());
    }

    @Test
    void unicodeUsername() {
        RecordInputStream in = TestcaseRecordInputStream.create(FileSharingRecord.sid, DATA_UNICODE);
        FileSharingRecord rec = new FileSharingRecord(in);
        assertEquals(0, in.remaining());
        assertEquals("홍길동", rec.getUsername());
        confirmRecordEncoding(FileSharingRecord.sid, DATA_UNICODE, rec.serialize());
    }

    @Test
    void emptyUsername() {
        RecordInputStream in = TestcaseRecordInputStream.create(FileSharingRecord.sid, DATA_EMPTY);
        FileSharingRecord rec = new FileSharingRecord(in);
        assertEquals(0, in.remaining());
        assertEquals(1, rec.getReadOnly());
        assertEquals("", rec.getUsername());
        confirmRecordEncoding(FileSharingRecord.sid, DATA_EMPTY, rec.serialize());
    }

    @Test
    void latin1UsernameStaysCompressed() {
        FileSharingRecord rec = new FileSharingRecord();
        rec.setUsername("José");
        confirmRecordEncoding(FileSharingRecord.sid,
            HexRead.readFromString("00 00 00 00 04 00 00 4A 6F 73 E9"), rec.serialize());
    }
}
