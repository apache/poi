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

package org.apache.poi.hslf.blip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.poi.hslf.record.RecordAtom;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.RecordFormatException;
import org.junit.jupiter.api.Test;

/**
 * Tests that the raw-data size calculations in the HSLF blip classes use
 * {@code long} arithmetic so that pathological (attacker-supplied) values
 * cannot cause integer overflow before the array is allocated.
 */
public class TestBlipAllocation {

    // -----------------------------------------------------------------------
    // Bitmap.calcRawDataSize
    // -----------------------------------------------------------------------

    @Test
    void testBitmapCalcRawDataSizeNormal() {
        // checksumLen=16, uidCount=1, dataLen=100  =>  16*1 + 1 + 100 = 117
        assertEquals(117L, Bitmap.calcRawDataSize(1, 16, 100));

        // checksumLen=16, uidCount=2, dataLen=100  =>  16*2 + 1 + 100 = 133
        assertEquals(133L, Bitmap.calcRawDataSize(2, 16, 100));
    }

    @Test
    void testBitmapCalcRawDataSizeNoOverflow() {
        // Values that would silently overflow with pure int arithmetic:
        //   16 * 1 + 1 + Integer.MAX_VALUE  =  16 + 1 + 2_147_483_647
        //   = 2_147_483_664  which wraps to a negative int but is fine as long
        long expected = 16L + 1L + Integer.MAX_VALUE;
        assertEquals(expected, Bitmap.calcRawDataSize(1, 16, Integer.MAX_VALUE));
    }

    @Test
    void testBitmapSafelyAllocateRejectsOversizedBuffer() {
        // A size larger than Integer.MAX_VALUE must be rejected by safelyAllocate
        long oversized = (long) Integer.MAX_VALUE + 1L;
        assertThrows(RecordFormatException.class,
                () -> IOUtils.safelyAllocate(oversized, RecordAtom.getMaxRecordLength()));
    }

    // -----------------------------------------------------------------------
    // Metafile.calcRawDataSize  (shared by EMF, WMF, PICT)
    // -----------------------------------------------------------------------

    @Test
    void testMetafileCalcRawDataSizeNormal() {
        // checksumLen=16, uidCount=1, headerSize=34, compressedLen=200
        //   16*1 + 34 + 200 = 250
        assertEquals(250L, Metafile.calcRawDataSize(1, 16, 34, 200));

        // checksumLen=16, uidCount=2, headerSize=34, compressedLen=200
        //   16*2 + 34 + 200 = 266
        assertEquals(266L, Metafile.calcRawDataSize(2, 16, 34, 200));
    }

    @Test
    void testMetafileCalcRawDataSizeNoOverflow() {
        // checksumLen=16, uidCount=2, headerSize=34, compressedLen=Integer.MAX_VALUE-50
        // Pure-int arithmetic: 32 + 34 + (Integer.MAX_VALUE - 50) overflows to a
        // negative number. Long arithmetic must yield the correct positive result.
        int compressedLen = Integer.MAX_VALUE - 50;
        long expected = (long) 16 * 2 + 34L + compressedLen;
        assertEquals(expected, Metafile.calcRawDataSize(2, 16, 34, compressedLen));
    }

    @Test
    void testMetafileSafelyAllocateRejectsOversizedBuffer() {
        long oversized = (long) Integer.MAX_VALUE + 5L;
        assertThrows(RecordFormatException.class,
                () -> IOUtils.safelyAllocate(oversized, RecordAtom.getMaxRecordLength()));
    }
}
