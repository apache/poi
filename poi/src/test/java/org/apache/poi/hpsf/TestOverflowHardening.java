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
package org.apache.poi.hpsf;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.poi.hpsf.wellknown.PropertyIDMap;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianByteArrayInputStream;
import org.apache.poi.util.RecordFormatException;
import org.junit.jupiter.api.Test;

/**
 * Verifies that HPSF readers reject crafted lengths whose subsequent
 * allocation-size arithmetic would silently overflow signed integer math.
 */
class TestOverflowHardening {

    /**
     * Reproduces the int*2 multiplication overflow in {@link UnicodeString#read}:
     * a length of {@code 0x40000001} produces {@code length*2 == 0x80000002},
     * which wraps to a negative int. {@link Math#multiplyExact} now surfaces
     * the overflow as an {@link ArithmeticException} at the parser, where the
     * malformed field actually lives.
     */
    @Test
    void unicodeStringLengthMultiplicationOverflowRejected() {
        byte[] data = new byte[4];
        LittleEndian.putInt(data, 0, 0x40000001);
        LittleEndianByteArrayInputStream lei = new LittleEndianByteArrayInputStream(data, 0);

        UnicodeString us = new UnicodeString();
        assertThrows(RecordFormatException.class, () -> us.read(lei));
    }

    /**
     * Reproduces the long*long multiplication overflow in
     * {@link Array.ArrayHeader#getNumberOfScalarValues}: with three dimensions
     * of size {@code 0x80000000} the unchecked product
     * {@code 2^31 * 2^31 * 2^31 = 2^93} wraps inside a 64-bit long and the
     * subsequent {@code > Integer.MAX_VALUE} guard at {@code Array.read} is
     * silently bypassed. {@link Math#multiplyExact} now rejects the crafted
     * array header with an {@link ArithmeticException}.
     */
    @Test
    void arrayDimensionMultiplicationOverflowRejected() {
        // ArrayHeader layout: type (4) + numDimensions (4) + numDimensions * (size:4 + indexOffset:4)
        // 3 dimensions of size 0x80000000 each -> product overflows 2^63 (long max)
        byte[] data = new byte[4 + 4 + 3 * (4 + 4)];
        int off = 0;
        LittleEndian.putInt(data, off, Variant.VT_I4);
        off += 4;
        LittleEndian.putInt(data, off, 3);
        off += 4;
        for (int i = 0; i < 3; i++) {
            LittleEndian.putUInt(data, off, 0x80000000L);
            off += 4;
            LittleEndian.putInt(data, off, 0);
            off += 4;
        }
        LittleEndianByteArrayInputStream lei = new LittleEndianByteArrayInputStream(data, 0);

        Array a = new Array();
        assertThrows(RecordFormatException.class, () -> a.read(lei));
    }

    /**
     * A dictionary entry's length is an unsigned 32-bit field, so the byte count derived from it
     * has to be range checked while it is still a long. Narrowing first made the conversion throw
     * {@link ArithmeticException} out of the {@code Section} constructor, bypassing the
     * corrupted-dictionary path that the oversize guard exists to take.
     */
    @Test
    void dictionaryEntryLengthAboveIntMaxIsTreatedAsCorrupt() {
        // ClassID (16) + section offset (4), then the section itself: size, property count,
        // one property entry pointing at the dictionary, and a dictionary holding a single
        // entry whose length is too large to narrow to an int
        byte[] data = new byte[48];
        LittleEndian.putUInt(data, 16, 20);                             // offset of the section
        LittleEndian.putUInt(data, 20, 28);                             // section size
        LittleEndian.putUInt(data, 24, 1);                              // property count
        LittleEndian.putUInt(data, 28, PropertyIDMap.PID_DICTIONARY);   // property id
        LittleEndian.putUInt(data, 32, 16);                             // property offset
        LittleEndian.putUInt(data, 36, 1);                              // dictionary entry count
        LittleEndian.putUInt(data, 40, 1);                              // entry key
        LittleEndian.putUInt(data, 44, 0xFFFFFFFFL);                    // entry length

        Section s = assertDoesNotThrow(() -> new Section(data, 0));
        assertTrue(s.getDictionary() == null || s.getDictionary().isEmpty(),
            "a dictionary entry with a bogus length should be discarded, not returned");
    }
}
