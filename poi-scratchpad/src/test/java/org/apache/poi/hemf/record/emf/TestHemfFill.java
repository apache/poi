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

import static org.apache.poi.hemf.record.emf.HemfRecordIterator.HEADER_SIZE;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;

import org.apache.poi.hwmf.record.HwmfBitmapDib;
import org.apache.poi.util.LittleEndianInputStream;
import org.apache.poi.util.RecordFormatException;
import org.junit.jupiter.api.Test;

class TestHemfFill {

    /**
     * cbBmi and cbBits come straight from the record, so a bitmap that claims to
     * extend past the end of the record must be rejected before anything is
     * buffered: here they add up to almost 2 GB while the record only holds 8 bytes.
     */
    @Test
    void bitmapExceedingRecordIsRejected() throws Exception {
        byte[] data = new byte[16];
        try (LittleEndianInputStream leis = new LittleEndianInputStream(new ByteArrayInputStream(data))) {
            assertThrows(RecordFormatException.class, () -> HemfFill.readBitmap(
                    leis, new HwmfBitmapDib(), HEADER_SIZE, 8,
                    1, 0x40000000, 0x7FFFFFF0, 0x3FFFFF00));
        }
    }

    /**
     * cbBmi + cbBits must not overflow into a negative bitmap size, even when
     * the record claims to be big enough to hold them.
     */
    @Test
    void bitmapSizeOverflowIsRejected() throws Exception {
        byte[] data = new byte[16];
        try (LittleEndianInputStream leis = new LittleEndianInputStream(new ByteArrayInputStream(data))) {
            assertThrows(RecordFormatException.class, () -> HemfFill.readBitmap(
                    leis, new HwmfBitmapDib(), HEADER_SIZE, 0xFFFFFFFFL,
                    1, 0x40000000, 0x40000001, 0x40000000));
        }
    }
}
