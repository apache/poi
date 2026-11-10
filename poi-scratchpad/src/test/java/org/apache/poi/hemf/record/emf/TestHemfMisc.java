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

package org.apache.poi.hemf.record.emf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.poi.hemf.record.emf.HemfMisc.EmfExtCreatePen;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianInputStream;
import org.apache.poi.util.RecordFormatException;
import org.junit.jupiter.api.Test;

class TestHemfMisc {

    /**
     * EMR_EXTCREATEPEN reads the unsigned NumStyleEntries field straight from the
     * record and uses it as the length of the dash-pattern array. A crafted count
     * of 0x40000000 makes that allocation enormous, and a high-bit value turns
     * negative when narrowed to int, so the count has to be capped before the
     * array is created.
     */
    @Test
    void numStyleEntriesIsBounded() throws Exception {
        byte[] data = new byte[44];
        for (int i = 0; i < 10; i++) {
            LittleEndian.putInt(data, i * 4, 0);    // penIndex .. hatchStyle
        }
        LittleEndian.putInt(data, 20, 0x7);         // penStyle with PS_USERSTYLE line dash
        LittleEndian.putInt(data, 40, 0x40000000);  // numStyleEntries

        EmfExtCreatePen penRecord = new EmfExtCreatePen();
        try (LittleEndianInputStream leis = new LittleEndianInputStream(new ByteArrayInputStream(data))) {
            assertThrows(RecordFormatException.class,
                    () -> penRecord.init(leis, (long) data.length, (long) 0x5F));
        }
    }

    @Test
    void testInit() throws IOException {
        HemfMisc.EmfEof misc = new HemfMisc.EmfEof();
        assertEquals(12, misc.init(new LittleEndianInputStream(new ByteArrayInputStream(new byte[] {
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
        })), 12, 0));
    }

    @Test
    void testInitWithPal() throws IOException {
        HemfMisc.EmfEof misc = new HemfMisc.EmfEof();
        assertEquals(16, misc.init(new LittleEndianInputStream(new ByteArrayInputStream(new byte[] {
                1, 0, 0, 0, 16, 0, 0, 0, 99, 98, 97, 96, 0, 0, 0, 0
        })), 16, 0));
    }

    @Test
    void testInitWithPalInvalid1() {
        HemfMisc.EmfEof misc = new HemfMisc.EmfEof();
        assertThrows(IllegalArgumentException.class,
                () -> misc.init(new LittleEndianInputStream(new ByteArrayInputStream(new byte[] {
                1, 0, 0, 0, 1, 0, 0, 0, 99, 98, 0, 0, 0, 0
        })), 12, 0));
    }

    @Test
    void testInitWithPalInvalid2() {
        HemfMisc.EmfEof misc = new HemfMisc.EmfEof();
        assertThrows(IllegalArgumentException.class,
                () -> misc.init(new LittleEndianInputStream(new ByteArrayInputStream(new byte[] {
                1, 0, 0, 0, 16, 0, 0, 0, 99, 98, 97, 96, 0, 0, 0, 0
        })), 12, 0));
    }

    @Test
    void testInitInvalidSize() {
        HemfMisc.EmfEof misc = new HemfMisc.EmfEof();
        assertThrows(IllegalArgumentException.class,
                () -> misc.init(new LittleEndianInputStream(new ByteArrayInputStream(new byte[] {
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
        })), 11, 0));
    }
}
