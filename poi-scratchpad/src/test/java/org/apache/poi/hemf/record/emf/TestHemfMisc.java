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

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;

import org.apache.poi.hemf.record.emf.HemfMisc.EmfExtCreatePen;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianInputStream;
import org.apache.poi.util.RecordFormatException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TestHemfMisc {

    /**
     * EMR_EXTCREATEPEN reads a 32-bit NumStyleEntries count straight from the EMF
     * stream and allocates the dash-pattern {@code float[]} before any entry is
     * read. A crafted count (oversized or negative) must be rejected by the
     * standard allocation check instead of triggering an OutOfMemoryError /
     * NegativeArraySizeException.
     */
    @ParameterizedTest
    @ValueSource(ints = { Integer.MAX_VALUE, 0xFFFFFFFF })
    void extCreatePenRejectsInvalidStyleEntryCount(int numStyleEntries) throws Exception {
        // ExtLogPen fixed part up to and including NumStyleEntries (11 * 4 bytes).
        // The allocation check fires immediately after NumStyleEntries is read,
        // before any DIB or dash data, so this fixed prefix is all that's needed.
        byte[] data = new byte[44];
        // penIndex(0), offBmi(4), cbBmi(8), offBits(12), cbBits(16) -> 0
        // PenStyle(20): low 3 bits = PS_USERSTYLE (0x7) so the parser's
        // USERSTYLE-dash assertion is satisfied and execution reaches the guard
        LittleEndian.putInt(data, 20, 0x00000007);
        // width(24), brushStyle(28), colorRef(32), hatchStyle(36) -> 0
        LittleEndian.putInt(data, 40, numStyleEntries);

        EmfExtCreatePen pen = new EmfExtCreatePen();
        try (LittleEndianInputStream leis = new LittleEndianInputStream(new ByteArrayInputStream(data))) {
            assertThrows(RecordFormatException.class,
                    () -> pen.init(leis, data.length, HemfRecordType.extCreatePen.id));
        }
    }
}
