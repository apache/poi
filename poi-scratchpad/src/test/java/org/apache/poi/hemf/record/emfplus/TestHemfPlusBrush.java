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

import org.apache.poi.hemf.record.emfplus.HemfPlusBrush.EmfPlusPathGradientBrushData;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianInputStream;
import org.apache.poi.util.RecordFormatException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TestHemfPlusBrush {

    /**
     * EmfPlusPathGradientBrushData reads a 32-bit surrounding-color count straight
     * from the EMF+ stream and allocates the color array before any color data is
     * read. A crafted count (oversized or negative) must be rejected by the
     * standard allocation check instead of triggering an OutOfMemoryError /
     * NegativeArraySizeException.
     */
    @ParameterizedTest
    @ValueSource(ints = { Integer.MAX_VALUE, 0xFFFFFFFF })
    void rejectsInvalidSurroundingColorCount(int colorCount) throws Exception {
        byte[] data = new byte[24];
        LittleEndian.putInt(data, 0, 0);             // dataFlags
        LittleEndian.putInt(data, 4, 0);             // wrapMode -> WRAP_MODE_TILE
        LittleEndian.putInt(data, 8, 0);             // centerColor
        // bytes 12..19: centerPoint (two floats)
        LittleEndian.putInt(data, 20, colorCount);   // surrounding color count

        EmfPlusPathGradientBrushData brush = new EmfPlusPathGradientBrushData();
        try (LittleEndianInputStream leis = new LittleEndianInputStream(new ByteArrayInputStream(data))) {
            assertThrows(RecordFormatException.class, () -> brush.init(leis, data.length));
        }
    }
}
