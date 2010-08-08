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

import java.util.Arrays;

/**
 * Tests the serialization and deserialization of the FtCblsSubRecord
 * class works correctly.
 *
 * @author Yegor Kozlov
 */
public final class TestFtCblsSubRecord extends TestCase {
    private byte[] data = new byte[] {
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x64, 0x00,
        0x01, 0x00, 0x0A, 0x00, 0x00, 0x00, 0x10, 0x00, 0x01, 0x00
    };

    public void testRead() {

        FtCblsSubRecord record = new FtCblsSubRecord(TestcaseRecordInputStream.create(FtCblsSubRecord.sid, data), data.length);

        assertEquals(FtCblsSubRecord.sid, record.getSid());
        assertEquals(data.length, record.getDataSize());
    }

    public void testWrite() {
        FtCblsSubRecord record = new FtCblsSubRecord();
        assertEquals(FtCblsSubRecord.sid, record.getSid());
        assertEquals(data.length, record.getDataSize());

        byte [] ser = record.serialize();
        assertEquals(ser.length - 4, data.length);

    }

    public void testClone()
    {
        FtCblsSubRecord record = new FtCblsSubRecord();
        byte[] src = record.serialize();

        FtCblsSubRecord cloned = (FtCblsSubRecord)record.clone();
        byte[] cln = cloned.serialize();

        assertEquals(record.getDataSize(), cloned.getDataSize());
        assertTrue(Arrays.equals(src, cln));
    }
}