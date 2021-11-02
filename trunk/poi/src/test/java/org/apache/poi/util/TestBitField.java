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

package org.apache.poi.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Class to test BitField functionality
 */
final class TestBitField {
    private static final BitField bf_multi  = BitFieldFactory.getInstance(0x3F80);
    private static final BitField bf_single = BitFieldFactory.getInstance(0x4000);

    @Test
    void testGetValue() {
        assertEquals(127, bf_multi.getValue(-1));
        assertEquals(0, bf_multi.getValue(0));
        assertEquals(1, bf_single.getValue(-1));
        assertEquals(0, bf_single.getValue(0));
    }

    @Test
    void testGetShortValue() {
        assertEquals(( short ) 127, bf_multi.getShortValue(( short ) -1));
        assertEquals(( short ) 0, bf_multi.getShortValue(( short ) 0));
        assertEquals(( short ) 1, bf_single.getShortValue(( short ) -1));
        assertEquals(( short ) 0, bf_single.getShortValue(( short ) 0));
    }

    @Test
    void testGetRawValue() {
        assertEquals(0x3F80, bf_multi.getRawValue(-1));
        assertEquals(0, bf_multi.getRawValue(0));
        assertEquals(0x4000, bf_single.getRawValue(-1));
        assertEquals(0, bf_single.getRawValue(0));
    }

    @Test
    void testGetShortRawValue() {
        assertEquals(( short ) 0x3F80, bf_multi.getShortRawValue(( short ) -1));
        assertEquals(( short ) 0, bf_multi.getShortRawValue(( short ) 0));
        assertEquals(( short ) 0x4000, bf_single.getShortRawValue(( short ) -1));
        assertEquals(( short ) 0, bf_single.getShortRawValue(( short ) 0));
    }

    @Test
    void testIsSet() {
        assertFalse(bf_multi.isSet(0));
        for (int j = 0x80; j <= 0x3F80; j += 0x80)
        {
            assertTrue(bf_multi.isSet(j));
        }
        assertFalse(bf_single.isSet(0));
        assertTrue(bf_single.isSet(0x4000));
    }

    @Test
    void testIsAllSet() {
        for (int j = 0; j < 0x3F80; j += 0x80)
        {
            assertFalse(bf_multi.isAllSet(j));
        }
        assertTrue(bf_multi.isAllSet(0x3F80));
        assertFalse(bf_single.isAllSet(0));
        assertTrue(bf_single.isAllSet(0x4000));
    }

    @Test
    void testSetValue() {
        for (int j = 0; j < 128; j++)
        {
            assertEquals(j, bf_multi.getValue(bf_multi.setValue(0, j)));
            assertEquals(j << 7, bf_multi.setValue(0, j));
        }

        // verify that excess bits are stripped off
        assertEquals(0, bf_multi.setValue(0x3f80, 128));
        for (int j = 0; j < 2; j++)
        {
            assertEquals(j, bf_single.getValue(bf_single.setValue(0, j)));
            assertEquals(j << 14, bf_single.setValue(0, j));
        }

        // verify that excess bits are stripped off
        assertEquals(0, bf_single.setValue(0x4000, 2));
    }

    @Test
    void testSetShortValue() {
        for (int j = 0; j < 128; j++)
        {
            assertEquals(( short ) j, bf_multi.getShortValue(bf_multi.setShortValue(( short ) 0, ( short ) j)));
            assertEquals(( short ) (j << 7), bf_multi.setShortValue(( short ) 0, ( short ) j));
        }

        // verify that excess bits are stripped off
        assertEquals(( short ) 0, bf_multi.setShortValue(( short ) 0x3f80, ( short ) 128));
        for (int j = 0; j < 2; j++) {
            assertEquals(( short ) j, bf_single.getShortValue(bf_single.setShortValue(( short ) 0, ( short ) j)));
            assertEquals(( short ) (j << 14), bf_single.setShortValue(( short ) 0, ( short ) j));
        }

        // verify that excess bits are stripped off
        assertEquals(( short ) 0, bf_single.setShortValue(( short ) 0x4000, ( short ) 2));
    }

    @Test
    void testByte() {
        assertEquals(1, BitFieldFactory.getInstance(1).setByteBoolean(( byte ) 0, true));
        assertEquals(2, BitFieldFactory.getInstance(2).setByteBoolean(( byte ) 0, true));
        assertEquals(4, BitFieldFactory.getInstance(4).setByteBoolean(( byte ) 0, true));
        assertEquals(8, BitFieldFactory.getInstance(8).setByteBoolean(( byte ) 0, true));
        assertEquals(16, BitFieldFactory.getInstance(16).setByteBoolean(( byte ) 0, true));
        assertEquals(32, BitFieldFactory.getInstance(32).setByteBoolean(( byte ) 0, true));
        assertEquals(64, BitFieldFactory.getInstance(64).setByteBoolean(( byte ) 0, true));
        assertEquals(-128, BitFieldFactory.getInstance(128).setByteBoolean(( byte ) 0, true));
        assertEquals(0, BitFieldFactory.getInstance(1).setByteBoolean(( byte ) 1, false));
        assertEquals(0, BitFieldFactory.getInstance(2).setByteBoolean(( byte ) 2, false));
        assertEquals(0, BitFieldFactory.getInstance(4).setByteBoolean(( byte ) 4, false));
        assertEquals(0, BitFieldFactory.getInstance(8).setByteBoolean(( byte ) 8, false));
        assertEquals(0, BitFieldFactory.getInstance(16).setByteBoolean(( byte ) 16, false));
        assertEquals(0, BitFieldFactory.getInstance(32).setByteBoolean(( byte ) 32, false));
        assertEquals(0, BitFieldFactory.getInstance(64).setByteBoolean(( byte ) 64, false));
        assertEquals(0, BitFieldFactory.getInstance(128).setByteBoolean(( byte ) 128,
                                     false));
        assertEquals(-2, BitFieldFactory.getInstance(1).setByteBoolean(( byte ) 255, false));
        byte clearedBit = BitFieldFactory.getInstance(0x40).setByteBoolean(( byte ) -63,
                                       false);

        assertFalse(BitFieldFactory.getInstance(0x40).isSet(clearedBit));
    }

    @Test
    void testClear() {
        assertEquals(0xFFFFC07F, bf_multi.clear(-1));
        assertEquals(0xFFFFBFFF, bf_single.clear(-1));
    }

    @Test
    void testClearShort() {
        assertEquals(( short ) 0xC07F, bf_multi.clearShort(( short ) -1));
        assertEquals(( short ) 0xBFFF, bf_single.clearShort(( short ) -1));
    }

    @Test
    void testSet() {
        assertEquals(0x3F80, bf_multi.set(0));
        assertEquals(0x4000, bf_single.set(0));
    }

    @Test
    void testSetShort() {
        assertEquals(( short ) 0x3F80, bf_multi.setShort(( short ) 0));
        assertEquals(( short ) 0x4000, bf_single.setShort(( short ) 0));
    }

    @Test
    void testSetBoolean() {
        assertEquals(bf_multi.set(0), bf_multi.setBoolean(0, true));
        assertEquals(bf_single.set(0), bf_single.setBoolean(0, true));
        assertEquals(bf_multi.clear(-1), bf_multi.setBoolean(-1, false));
        assertEquals(bf_single.clear(-1), bf_single.setBoolean(-1, false));
    }

    @Test
    void testSetShortBoolean() {
        assertEquals(bf_multi.setShort(( short ) 0), bf_multi.setShortBoolean(( short ) 0, true));
        assertEquals(bf_single.setShort(( short ) 0), bf_single.setShortBoolean(( short ) 0, true));
        assertEquals(bf_multi.clearShort(( short ) -1), bf_multi.setShortBoolean(( short ) -1, false));
        assertEquals(bf_single.clearShort(( short ) -1), bf_single.setShortBoolean(( short ) -1, false));
    }

    @Test
    void testSetLargeValues() {
       final BitField bf1 = new BitField(0xF), bf2 = new BitField(0xF0000000);
       int a = 0;
       a = bf1.setValue(a, 9);
       a = bf2.setValue(a, 9);
       assertEquals(9, bf1.getValue(a));
       assertEquals(9, bf2.getValue(a));
    }
}
