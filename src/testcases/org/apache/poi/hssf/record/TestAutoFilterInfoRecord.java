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
import org.apache.poi.ddf.EscherClientDataRecord;
import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.ddf.EscherDggRecord;
import org.apache.poi.ddf.EscherSpRecord;
import org.apache.poi.hssf.model.DrawingManager2;
import org.apache.poi.util.HexDump;
import org.apache.poi.util.HexRead;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

/**
 * Tests the AutoFilterInfoRecord class.
 *
 * @author Yegor Kozlov
 */
public final class TestAutoFilterInfoRecord extends TestCase {
    private byte[] data = new byte[] {
        0x05, 0x00
    };

    public void testRead() {

        AutoFilterInfoRecord record = new AutoFilterInfoRecord(TestcaseRecordInputStream.create(AutoFilterInfoRecord.sid, data));

        assertEquals(AutoFilterInfoRecord.sid, record.getSid());
        assertEquals(data.length, record.getDataSize());
        assertEquals(5, record.getNumEntries());
        record.setNumEntries((short)3);
        assertEquals(3, record.getNumEntries());
    }

    public void testWrite() {
        AutoFilterInfoRecord record = new AutoFilterInfoRecord();
        record.setNumEntries((short)3);

        byte [] ser = record.serialize();
        assertEquals(ser.length - 4, data.length);
        record = new AutoFilterInfoRecord(TestcaseRecordInputStream.create(ser));
        assertEquals(3, record.getNumEntries());
    }

    public void testClone()
    {
        AutoFilterInfoRecord record = new AutoFilterInfoRecord();
        record.setNumEntries((short)3);
        byte[] src = record.serialize();

        AutoFilterInfoRecord cloned = (AutoFilterInfoRecord)record.clone();
        assertEquals(3, record.getNumEntries());
        byte[] cln = cloned.serialize();

        assertEquals(record.getDataSize(), cloned.getDataSize());
        assertTrue(Arrays.equals(src, cln));
    }
}