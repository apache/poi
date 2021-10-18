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

import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.junit.jupiter.api.Test;

/**
 * Tests that {@link ExOleObjAtom} works properly
 */
public final class TestExOleObjAtom {
    // From a real file (embedded SWF control)
    private final byte[] data = {
            0x01, 0x00, (byte)0xC3, 0x0F, 0x18, 0x00, 0x00, 0x00,
            0x01, 0x00, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00, 0x00, (byte)0x96, 0x13, 0x00  };

    @Test
    void testRead() {
        ExOleObjAtom record = new ExOleObjAtom(data, 0, data.length);
        assertEquals(RecordTypes.ExOleObjAtom.typeID, record.getRecordType());

        assertEquals(ExOleObjAtom.DRAW_ASPECT_VISIBLE, record.getDrawAspect());
        assertEquals(ExOleObjAtom.TYPE_CONTROL, record.getType());
        assertEquals(1, record.getObjID());
        assertEquals(ExOleObjAtom.SUBTYPE_DEFAULT, record.getSubType());
        assertEquals(2, record.getObjStgDataRef());
        // the meaning is unknown
        assertEquals(1283584, record.getOptions());
    }

    @Test
    void testWrite() throws Exception {
        ExOleObjAtom record = new ExOleObjAtom(data, 0, data.length);
        UnsynchronizedByteArrayOutputStream baos = new UnsynchronizedByteArrayOutputStream();
        record.writeOut(baos);
        assertArrayEquals(data, baos.toByteArray());
    }

    @Test
    void testNewRecord() throws Exception {
        ExOleObjAtom record = new ExOleObjAtom();
        record.setDrawAspect(ExOleObjAtom.DRAW_ASPECT_VISIBLE);
        record.setType(ExOleObjAtom.TYPE_CONTROL);
        record.setObjID(1);
        record.setSubType(ExOleObjAtom.SUBTYPE_DEFAULT);
        record.setObjStgDataRef(2);
        record.setOptions(1283584);

        UnsynchronizedByteArrayOutputStream baos = new UnsynchronizedByteArrayOutputStream();
        record.writeOut(baos);
        assertArrayEquals(data, baos.toByteArray());
    }
}
