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

package org.apache.poi.hwmf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.poi.hwmf.record.HwmfBitmapDib;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianInputStream;
import org.junit.jupiter.api.Test;

class TestHwmfBitmapDib {

    @Test
    void readsCoreHeader() throws IOException {
        byte[] data = new byte[16];
        // BitmapCoreHeader: size, 1x1 pixel, 1 plane, 24 bits per pixel
        LittleEndian.putInt(data, 0, 0x0C);
        LittleEndian.putUShort(data, 4, 1);
        LittleEndian.putUShort(data, 6, 1);
        LittleEndian.putUShort(data, 8, 1);
        LittleEndian.putUShort(data, 10, 24);
        // one pixel, padded to 4 bytes
        data[12] = (byte) 0xFF;

        HwmfBitmapDib dib = new HwmfBitmapDib();
        try (LittleEndianInputStream leis = new LittleEndianInputStream(new ByteArrayInputStream(data))) {
            assertEquals(data.length, dib.init(leis, data.length));
        }
    }

    @Test
    void rejectsUnknownCompression() {
        byte[] data = new byte[44];
        // BitmapInfoHeader: size, 1x1 pixel, 1 plane, 24 bits per pixel, compression 0x7F isn't defined
        LittleEndian.putInt(data, 0, 40);
        LittleEndian.putInt(data, 4, 1);
        LittleEndian.putInt(data, 8, 1);
        LittleEndian.putUShort(data, 12, 1);
        LittleEndian.putUShort(data, 14, 24);
        LittleEndian.putInt(data, 16, 0x7F);

        HwmfBitmapDib dib = new HwmfBitmapDib();
        LittleEndianInputStream leis = new LittleEndianInputStream(new ByteArrayInputStream(data));
        IOException ex = assertThrows(IOException.class, () -> dib.init(leis, data.length));
        assertEquals("Invalid compression option (unknown) for bitcount (BI_BITCOUNT_5).", ex.getMessage());
    }
}
