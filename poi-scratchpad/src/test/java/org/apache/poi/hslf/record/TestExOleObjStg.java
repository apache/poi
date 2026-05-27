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

package org.apache.poi.hslf.record;


import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;

import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.RecordFormatException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

/**
 * Tests that {@link ExOleObjStg} works properly
 */
@Isolated   // this test changes global static MAX_RECORD_LENGTH
public final class TestExOleObjStg {

    // From a real file (embedded SWF control)
    // <ExOleObjStg info="16" type="4113" size="347" offset="4322" header="10 00 11 10 5B 01 00 00 ">....
    private static byte[] data;

    private int savedMaxRecordLength;

    @BeforeEach
    void saveMaxRecordLength() {
        savedMaxRecordLength = ExOleObjStg.getMaxRecordLength();
    }

    @AfterEach
    void restoreMaxRecordLength() {
        ExOleObjStg.setMaxRecordLength(savedMaxRecordLength);
    }

    @BeforeAll
    public static void init() throws IOException {
        data = org.apache.poi.poifs.storage.RawDataUtil.decompress(
        "H4sIAAAAAAAAAAFjAZz+EAAREFsBAAAADgAAeJy7cF7wwcKNUg8Z0IAdAzPDv/+cDGxIYoxAzATjCDAwsEDF/"+
        "v3//x8kxAzE/0fBkAJBDPlAWMKgwODKkAekixgq0ZMCXiDGwAqPc1BayLtdcyl33XnBaTtcXINDUNUGvEgvK2"+
        "Y4ycgOZDswQsScgbaD7E0Fk8Uk2Q0CQgxMjMj+IVafAiPJVuEE5NhPTUCJ/aC8+xdLuvgHxaNgeAN65v8JZ1k"+
        "bQfmflWE0/1MTUGI/TB+sHBjN9yMLwNp0oLYbqD03GvcjC6SHpoUKABtkbzghmAPawudgkAGSaQw5DGUMBUAy"+
        "EVgvpAJrBz1gGV0OFCdOBR+QDGfIBJbtKcByvhzI4wbj//85GLiALA+gXDpDBlgtqKfhwxACl4e4AuYaYHeDI"+
        "RioEmSKI9C2HLhZyKqQ9fBilHXcwN4KN1wdM1Q1iJcINZGDgQfsJxC/GOib4Q8AvWU91AJ49g1jAQAA"
        );
    }

    @Test
    void testRead() throws Exception {
        ExOleObjStg record = new ExOleObjStg(data, 0, data.length);
        assertEquals(RecordTypes.ExOleObjStg.typeID, record.getRecordType());

        int len = record.getDataLength();
        byte[] oledata = IOUtils.toByteArray(record.getData());
        assertEquals(len, oledata.length);

        try (POIFSFileSystem fs = new POIFSFileSystem(record.getData())) {
            DocumentEntry doc = (DocumentEntry) fs.getRoot().getEntryCaseInsensitive("Contents");
            assertNotNull(doc);
        }
    }

    @Test
    void testWrite() throws Exception {
        ExOleObjStg record = new ExOleObjStg(data, 0, data.length);
        UnsynchronizedByteArrayOutputStream baos = UnsynchronizedByteArrayOutputStream.builder().get();
        record.writeOut(baos);
        byte[] b = baos.toByteArray();

        assertArrayEquals(data, b);
    }

    @Test
    void testNewRecord() throws Exception {
        ExOleObjStg src = new ExOleObjStg(data, 0, data.length);
        byte[] oledata = IOUtils.toByteArray(src.getData());

        ExOleObjStg tgt = new ExOleObjStg();
        tgt.setData(oledata);


        assertEquals(src.getDataLength(), tgt.getDataLength());

        UnsynchronizedByteArrayOutputStream out = UnsynchronizedByteArrayOutputStream.builder().get();
        tgt.writeOut(out);
        byte[] b = out.toByteArray();

        assertEquals(data.length, b.length);
        assertArrayEquals(data, b);
    }

    @Test
    void testGetDataRejectsOversizedClaimedSize() {
        // Build a minimal compressed ExOleObjStg record whose claimed
        // uncompressed size (first 4 bytes of _data) exceeds MAX_RECORD_LENGTH.
        // header: 8 bytes, with bytes [0..1] != 0 to make isCompressed() return true
        byte[] header = new byte[8];
        LittleEndian.putShort(header, 0, (short) 0x10); // non-zero -> compressed

        // _data: first 4 bytes = claimed size (200_000_001 > default 100_000_000),
        // followed by a trivial DEFLATE stream (1 empty stored block)
        byte[] deflateEmpty = { 0x78, (byte) 0x9C, 0x03, 0x00, 0x00, 0x00, 0x00, 0x01 };
        byte[] recordData = new byte[4 + deflateEmpty.length];
        LittleEndian.putInt(recordData, 0, 200_000_001);
        System.arraycopy(deflateEmpty, 0, recordData, 4, deflateEmpty.length);

        byte[] rawRecord = new byte[8 + recordData.length];
        System.arraycopy(header, 0, rawRecord, 0, 8);
        System.arraycopy(recordData, 0, rawRecord, 8, recordData.length);

        ExOleObjStg record = new ExOleObjStg(rawRecord, 0, rawRecord.length);
        assertThrows(RecordFormatException.class, record::getData,
                "getData() must throw RecordFormatException when claimed size exceeds MAX_RECORD_LENGTH");
    }

    @Test
    void testGetDataRejectsNegativeClaimedSize() {
        // A claimed size stored as a negative integer (sign bit set) must be rejected.
        byte[] header = new byte[8];
        LittleEndian.putShort(header, 0, (short) 0x10);

        byte[] recordData = new byte[8];
        LittleEndian.putInt(recordData, 0, -1); // negative size

        byte[] rawRecord = new byte[8 + recordData.length];
        System.arraycopy(header, 0, rawRecord, 0, 8);
        System.arraycopy(recordData, 0, rawRecord, 8, recordData.length);

        ExOleObjStg record = new ExOleObjStg(rawRecord, 0, rawRecord.length);
        assertThrows(RecordFormatException.class, record::getData,
                "getData() must throw RecordFormatException when claimed size is negative");
    }

    @Test
    void testGetDataWithinLimit() throws Exception {
        // The existing real-file data has a small decompressed size; this must work normally.
        ExOleObjStg record = new ExOleObjStg(data, 0, data.length);
        assertNotNull(record.getData(),
                "getData() must succeed when the claimed size is within MAX_RECORD_LENGTH");
    }

    @Test
    void testGetDataRejectsWhenLimitLowered() throws Exception {
        // Lower MAX_RECORD_LENGTH below the record's actual decompressed size,
        // then verify that getData() throws.
        ExOleObjStg record = new ExOleObjStg(data, 0, data.length);
        int actualSize = record.getDataLength(); // e.g. ~23_000 bytes for the test record
        ExOleObjStg.setMaxRecordLength(actualSize - 1);
        assertThrows(RecordFormatException.class, record::getData,
                "getData() must throw when the claimed size exceeds the lowered MAX_RECORD_LENGTH");
    }
}
