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

import junit.framework.*;

/**
 * Class to test BitField functionality
 *
 * @author Marc Johnson
 * @author Glen Stampoultzis (gstamp@iprimus.com.au)
 */
public final class TestBitField extends TestCase {
    private static BitField bf_multi  = BitFieldFactory.getInstance(0x3F80);
    private static BitField bf_single = BitFieldFactory.getInstance(0x4000);

    public void testGetValue() {
        assertEquals(bf_multi.getValue(-1), 127);
        assertEquals(bf_multi.getValue(0), 0);
        assertEquals(bf_single.getValue(-1), 1);
        assertEquals(bf_single.getValue(0), 0);
    }

    public void testGetShortValue() {
        assertEquals(bf_multi.getShortValue(( short ) -1), ( short ) 127);
        assertEquals(bf_multi.getShortValue(( short ) 0), ( short ) 0);
        assertEquals(bf_single.getShortValue(( short ) -1), ( short ) 1);
        assertEquals(bf_single.getShortValue(( short ) 0), ( short ) 0);
    }

    public void testGetRawValue() {
        assertEquals(bf_multi.getRawValue(-1), 0x3F80);
        assertEquals(bf_multi.getRawValue(0), 0);
        assertEquals(bf_single.getRawValue(-1), 0x4000);
        assertEquals(bf_single.getRawValue(0), 0);
    }

    public void testGetShortRawValue() {
        assertEquals(bf_multi.getShortRawValue(( short ) -1),
                     ( short ) 0x3F80);
        assertEquals(bf_multi.getShortRawValue(( short ) 0), ( short ) 0);
        assertEquals(bf_single.getShortRawValue(( short ) -1),
                     ( short ) 0x4000);
        assertEquals(bf_single.getShortRawValue(( short ) 0), ( short ) 0);
    }

    public void testIsSet() {
        assertTrue(!bf_multi.isSet(0));
        for (int j = 0x80; j <= 0x3F80; j += 0x80)
        {
            assertTrue(bf_multi.isSet(j));
        }
        assertTrue(!bf_single.isSet(0));
        assertTrue(bf_single.isSet(0x4000));
    }

    public void testIsAllSet() {
        for (int j = 0; j < 0x3F80; j += 0x80)
        {
            assertTrue(!bf_multi.isAllSet(j));
        }
        assertTrue(bf_multi.isAllSet(0x3F80));
        assertTrue(!bf_single.isAllSet(0));
        assertTrue(bf_single.isAllSet(0x4000));
    }

    public void testSetValue() {
        for (int j = 0; j < 128; j++)
        {
            assertEquals(bf_multi.getValue(bf_multi.setValue(0, j)), j);
            assertEquals(bf_multi.setValue(0, j), j << 7);
        }

        // verify that excess bits are stripped off
        assertEquals(bf_multi.setValue(0x3f80, 128), 0);
        for (int j = 0; j < 2; j++)
        {
            assertEquals(bf_single.getValue(bf_single.setValue(0, j)), j);
            assertEquals(bf_single.setValue(0, j), j << 14);
        }

        // verify that excess bits are stripped off
        assertEquals(bf_single.setValue(0x4000, 2), 0);
    }

    public void testSetShortValue() {
        for (int j = 0; j < 128; j++)
        {
            assertEquals(bf_multi
                .getShortValue(bf_multi
                    .setShortValue(( short ) 0, ( short ) j)), ( short ) j);
            assertEquals(bf_multi.setShortValue(( short ) 0, ( short ) j),
                         ( short ) (j << 7));
        }

        // verify that excess bits are stripped off
        assertEquals(bf_multi.setShortValue(( short ) 0x3f80, ( short ) 128),
                     ( short ) 0);
        for (int j = 0; j < 2; j++)
        {
            assertEquals(bf_single
                .getShortValue(bf_single
                    .setShortValue(( short ) 0, ( short ) j)), ( short ) j);
            assertEquals(bf_single.setShortValue(( short ) 0, ( short ) j),
                         ( short ) (j << 14));
        }

        // verify that excess bits are stripped off
        assertEquals(bf_single.setShortValue(( short ) 0x4000, ( short ) 2),
                     ( short ) 0);
    }

    public void testByte() {
        assertEquals(1, BitFieldFactory.getInstance(1).setByteBoolean(( byte ) 0, true));
        assertEquals(2, BitFieldFactory.getInstance(2).setByteBoolean(( byte ) 0, true));
        assertEquals(4, BitFieldFactory.getInstance(4).setByteBoolean(( byte ) 0, true));
        assertEquals(8, BitFieldFactory.getInstance(8).setByteBoolean(( byte ) 0, true));
        assertEquals(16, BitFieldFactory.getInstance(16).setByteBoolean(( byte ) 0, true));
        assertEquals(32, BitFieldFactory.getInstance(32).setByteBoolean(( byte ) 0, true));
        assertEquals(64, BitFieldFactory.getInstance(64).setByteBoolean(( byte ) 0, true));
        assertEquals(-128,
                     BitFieldFactory.getInstance(128).setByteBoolean(( byte ) 0, true));
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

        assertEquals(false, BitFieldFactory.getInstance(0x40).isSet(clearedBit));
    }

    public void testClear() {
        assertEquals(bf_multi.clear(-1), 0xFFFFC07F);
        assertEquals(bf_single.clear(-1), 0xFFFFBFFF);
    }

    public void testClearShort() {
        assertEquals(bf_multi.clearShort(( short ) -1), ( short ) 0xC07F);
        assertEquals(bf_single.clearShort(( short ) -1), ( short ) 0xBFFF);
    }

    public void testSet() {
        assertEquals(bf_multi.set(0), 0x3F80);
        assertEquals(bf_single.set(0), 0x4000);
    }

    public void testSetShort() {
        assertEquals(bf_multi.setShort(( short ) 0), ( short ) 0x3F80);
        assertEquals(bf_single.setShort(( short ) 0), ( short ) 0x4000);
    }

    public void testSetBoolean() {
        assertEquals(bf_multi.set(0), bf_multi.setBoolean(0, true));
        assertEquals(bf_single.set(0), bf_single.setBoolean(0, true));
        assertEquals(bf_multi.clear(-1), bf_multi.setBoolean(-1, false));
        assertEquals(bf_single.clear(-1), bf_single.setBoolean(-1, false));
    }

    public void testSetShortBoolean() {
        assertEquals(bf_multi.setShort(( short ) 0),
                     bf_multi.setShortBoolean(( short ) 0, true));
        assertEquals(bf_single.setShort(( short ) 0),
                     bf_single.setShortBoolean(( short ) 0, true));
        assertEquals(bf_multi.clearShort(( short ) -1),
                     bf_multi.setShortBoolean(( short ) -1, false));
        assertEquals(bf_single.clearShort(( short ) -1),
                     bf_single.setShortBoolean(( short ) -1, false));
    }
}
