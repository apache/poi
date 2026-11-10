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

package org.apache.poi.hemf.record.emfplus;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;

import org.apache.poi.hemf.record.emfplus.HemfPlusDraw.EmfPlusDrawDriverString;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianInputStream;
import org.apache.poi.util.RecordFormatException;
import org.junit.jupiter.api.Test;

class TestHemfPlusDraw {

    /**
     * EmfPlusDrawDriverString reads a 32-bit glyph count and allocates the
     * glyph buffer as glyphCount*2 bytes. A crafted count of 0x40000000 makes
     * that product wrap to a negative int, which slips past the MAX_OBJECT_SIZE
     * cap inside IOUtils.toByteArray. The size cap must reject it instead.
     */
    @Test
    void glyphCountByteLengthDoesNotOverflow() throws Exception {
        byte[] data = new byte[32];
        int pos = 0;
        LittleEndian.putInt(data, pos, 0); pos += 4;            // brushId
        LittleEndian.putInt(data, pos, 0); pos += 4;            // optionsFlags
        LittleEndian.putInt(data, pos, 0); pos += 4;            // matrixPresent
        LittleEndian.putInt(data, pos, 0x40000000); pos += 4;   // glyphCount -> *2 wraps negative

        EmfPlusDrawDriverString record = new EmfPlusDrawDriverString();
        try (LittleEndianInputStream leis = new LittleEndianInputStream(new ByteArrayInputStream(data))) {
            assertThrows(RecordFormatException.class,
                    () -> record.init(leis, data.length, 0x4036, 0));
        }
    }
}
