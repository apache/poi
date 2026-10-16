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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;

import org.apache.poi.hwmf.record.HwmfBitmapDib;
import org.apache.poi.util.LittleEndianInputStream;
import org.junit.jupiter.api.Test;

class TestHemfFill {

    /**
     * When the bitmap header and bits are not contiguous, readBitmap copies them
     * into an intermediate buffer. cbBmi and cbBits come straight from the record,
     * so they must not be used to presize that buffer beyond the record limit:
     * here they add up to almost 2 GB while the record only holds a few bytes.
     */
    @Test
    void oversizedBitmapSizesDoNotPresizeBuffer() throws Exception {
        byte[] data = new byte[16];
        try (LittleEndianInputStream leis = new LittleEndianInputStream(new ByteArrayInputStream(data))) {
            // the record is truncated, so parsing still fails, but not by allocating ~2 GB first
            Throwable t = assertThrows(Throwable.class, () -> HemfFill.readBitmap(
                    leis, new HwmfBitmapDib(), HEADER_SIZE,
                    1, 0x40000000, 0x7FFFFFF0, 0x3FFFFF00));
            assertFalse(t instanceof OutOfMemoryError, "buffer was presized from cbBmi + cbBits");
        }
    }
}
