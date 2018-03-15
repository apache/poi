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
import org.apache.poi.util.HexRead;
import org.apache.poi.util.HexDump;
import java.util.List;
import java.io.ByteArrayInputStream;

/**
 * Tests the serialization and deserialization of the EndSubRecord
 * class works correctly.  Test data taken directly from a real
 * Excel file.
 *
 * @author Yegor Kozlov
 */
public final class TestInterfaceEndRecord extends TestCase {

    public void testCreate() {
        InterfaceEndRecord record = InterfaceEndRecord.instance;
        assertEquals(0, record.getDataSize());
    }

    /**
     * Silently swallow unexpected contents in InterfaceEndRecord.
     * Although it violates the spec, Excel silently converts this
     * data to an {@link InterfaceHdrRecord}.
     */
    public void testUnexpectedBytes_bug47251(){
        String hex = "" +
                "09 08 10 00 00 06 05 00 EC 15 CD 07 C1 C0 00 00 06 03 00 00 " +   //BOF
                "E2 00 02 00 B0 04 " + //INTERFACEEND with extra two bytes
                "0A 00 00 00";    // EOF
        byte[] data = HexRead.readFromString(hex);
        List<Record> records = RecordFactory.createRecords(new ByteArrayInputStream(data));
        assertEquals(3, records.size());
        Record rec1 = records.get(1);
        assertEquals(InterfaceHdrRecord.class, rec1.getClass());
        InterfaceHdrRecord r = (InterfaceHdrRecord)rec1;
        assertEquals("[E1, 00, 02, 00, B0, 04]", HexDump.toHex(r.serialize()));
    }
}
