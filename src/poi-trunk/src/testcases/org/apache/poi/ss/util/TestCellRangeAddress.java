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

package org.apache.poi.ss.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.poi.hssf.record.TestcaseRecordInputStream;
import org.apache.poi.util.LittleEndianOutputStream;
import org.junit.Test;

public final class TestCellRangeAddress {
    static final byte[] data = new byte[] {
            0x02, 0x00, 0x04, 0x00,
            0x00, 0x00, 0x03, 0x00,
    };

    @Test
    public void testLoad() {
        CellRangeAddress ref = new CellRangeAddress(
                TestcaseRecordInputStream.create(0x000, data));
        assertEquals(2, ref.getFirstRow());
        assertEquals(4, ref.getLastRow());
        assertEquals(0, ref.getFirstColumn());
        assertEquals(3, ref.getLastColumn());

        assertEquals(8, CellRangeAddress.ENCODED_SIZE);
    }

    @Test
    public void testLoadInvalid() {
        try {
            new CellRangeAddress(
                TestcaseRecordInputStream.create(0x000, new byte[] { (byte)0x02 }));
            fail();
        } catch (RuntimeException e) {
            assertTrue("Had: " + e, e.getMessage().contains("Ran out of data"));
        }
    }

    @Test
    public void testStore() throws IOException {
        CellRangeAddress ref = new CellRangeAddress(0, 0, 0, 0);

        byte[] recordBytes;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (LittleEndianOutputStream out = new LittleEndianOutputStream(baos)) {
            // With nothing set
            ref.serialize(out);
            recordBytes = baos.toByteArray();
            assertEquals(recordBytes.length, data.length);
            for (int i = 0; i < data.length; i++) {
                assertEquals("At offset " + i, 0, recordBytes[i]);
            }

            // Now set the flags
            ref.setFirstRow((short) 2);
            ref.setLastRow((short) 4);
            ref.setFirstColumn((short) 0);
            ref.setLastColumn((short) 3);

            // Re-test
            baos.reset();
            ref.serialize(out);
            recordBytes = baos.toByteArray();

            assertEquals(recordBytes.length, data.length);
            for (int i = 0; i < data.length; i++) {
                assertEquals("At offset " + i, data[i], recordBytes[i]);
            }
        }
    }
    
    @Test
    public void testCreateIllegal() throws IOException {
        // for some combinations we expected exceptions
        try {
            new CellRangeAddress(1, 0, 0, 0);
            fail("Expect to catch an exception");
        } catch (IllegalArgumentException e) {
            // expected here
        }
        try {
            new CellRangeAddress(0, 0, 1, 0);
            fail("Expect to catch an exception");
        } catch (IllegalArgumentException e) {
            // expected here
        }
    }

    @Test
    public void testCopy() throws IOException {
        CellRangeAddress ref = new CellRangeAddress(1, 2, 3, 4);
        CellRangeAddress copy = ref.copy();
        assertEquals(ref.toString(), copy.toString());
    }

    @Test
    public void testGetEncodedSize() throws IOException {
        assertEquals(2*CellRangeAddress.ENCODED_SIZE, CellRangeAddress.getEncodedSize(2));
    }

    @Test
    public void testFormatAsString() throws IOException {
        CellRangeAddress ref = new CellRangeAddress(1, 2, 3, 4);
        
        assertEquals("D2:E3", ref.formatAsString());
        assertEquals("D2:E3", CellRangeAddress.valueOf(ref.formatAsString()).formatAsString());

        assertEquals("sheet1!$D$2:$E$3", ref.formatAsString("sheet1", true));
        assertEquals("sheet1!$D$2:$E$3", CellRangeAddress.valueOf(ref.formatAsString()).formatAsString("sheet1", true));
        assertEquals("sheet1!$D$2:$E$3", CellRangeAddress.valueOf(ref.formatAsString("sheet1", true)).formatAsString("sheet1", true));

        assertEquals("sheet1!D2:E3", ref.formatAsString("sheet1", false));
        assertEquals("sheet1!D2:E3", CellRangeAddress.valueOf(ref.formatAsString()).formatAsString("sheet1", false));
        assertEquals("sheet1!D2:E3", CellRangeAddress.valueOf(ref.formatAsString("sheet1", false)).formatAsString("sheet1", false));

        assertEquals("D2:E3", ref.formatAsString(null, false));
        assertEquals("D2:E3", CellRangeAddress.valueOf(ref.formatAsString()).formatAsString(null, false));
        assertEquals("D2:E3", CellRangeAddress.valueOf(ref.formatAsString(null, false)).formatAsString(null, false));
        
        assertEquals("$D$2:$E$3", ref.formatAsString(null, true));
        assertEquals("$D$2:$E$3", CellRangeAddress.valueOf(ref.formatAsString()).formatAsString(null, true));
        assertEquals("$D$2:$E$3", CellRangeAddress.valueOf(ref.formatAsString(null, true)).formatAsString(null, true));
        
        ref = new CellRangeAddress(-1, -1, 3, 4);
        assertEquals("D:E", ref.formatAsString());
        assertEquals("sheet1!$D:$E", CellRangeAddress.valueOf(ref.formatAsString()).formatAsString("sheet1", true));
        assertEquals("sheet1!$D:$E", CellRangeAddress.valueOf(ref.formatAsString("sheet1", true)).formatAsString("sheet1", true));
        assertEquals("$D:$E", CellRangeAddress.valueOf(ref.formatAsString()).formatAsString(null, true));
        assertEquals("$D:$E", CellRangeAddress.valueOf(ref.formatAsString(null, true)).formatAsString(null, true));
        assertEquals("sheet1!D:E", CellRangeAddress.valueOf(ref.formatAsString()).formatAsString("sheet1", false));
        assertEquals("sheet1!D:E", CellRangeAddress.valueOf(ref.formatAsString("sheet1", false)).formatAsString("sheet1", false));

        ref = new CellRangeAddress(1, 2, -1, -1);
        assertEquals("2:3", ref.formatAsString());
        assertEquals("sheet1!$2:$3", CellRangeAddress.valueOf(ref.formatAsString()).formatAsString("sheet1", true));
        assertEquals("sheet1!$2:$3", CellRangeAddress.valueOf(ref.formatAsString("sheet1", true)).formatAsString("sheet1", true));
        assertEquals("$2:$3", CellRangeAddress.valueOf(ref.formatAsString()).formatAsString(null, true));
        assertEquals("$2:$3", CellRangeAddress.valueOf(ref.formatAsString(null, true)).formatAsString(null, true));
        assertEquals("sheet1!2:3", CellRangeAddress.valueOf(ref.formatAsString()).formatAsString("sheet1", false));
        assertEquals("sheet1!2:3", CellRangeAddress.valueOf(ref.formatAsString("sheet1", false)).formatAsString("sheet1", false));

        ref = new CellRangeAddress(1, 1, 2, 2);
        assertEquals("C2", ref.formatAsString());
        assertEquals("sheet1!$C$2", CellRangeAddress.valueOf(ref.formatAsString()).formatAsString("sheet1", true));
        assertEquals("sheet1!$C$2", CellRangeAddress.valueOf(ref.formatAsString("sheet1", true)).formatAsString("sheet1", true));
        assertEquals("$C$2", CellRangeAddress.valueOf(ref.formatAsString()).formatAsString(null, true));
        assertEquals("$C$2", CellRangeAddress.valueOf(ref.formatAsString(null, true)).formatAsString(null, true));
        assertEquals("sheet1!C2", CellRangeAddress.valueOf(ref.formatAsString()).formatAsString("sheet1", false));
        assertEquals("sheet1!C2", CellRangeAddress.valueOf(ref.formatAsString("sheet1", false)).formatAsString("sheet1", false));

        // is this a valid address?
        ref = new CellRangeAddress(-1, -1, -1, -1);
        assertEquals(":", ref.formatAsString());
    }
    
    @Test
    public void testEquals() {
        final CellRangeAddress ref1 = new CellRangeAddress(1, 2, 3, 4);
        final CellRangeAddress ref2 = new CellRangeAddress(1, 2, 3, 4);
        assertEquals(ref1, ref2);
        
        // Invert first/last row, but refer to same area
        ref2.setFirstRow(2);
        ref2.setLastRow(1);
        assertEquals(ref1, ref2);
        
        // Invert first/last column, but refer to same area
        ref2.setFirstColumn(4);
        ref2.setLastColumn(3);
        assertEquals(ref1, ref2);
        
        // Refer to a different area
        assertNotEquals(ref1, new CellRangeAddress(3, 4, 1, 2));
    }
    
    @Test
    public void testGetMinMaxRow() {
        final CellRangeAddress ref = new CellRangeAddress(1, 2, 3, 4);
        assertEquals(1, ref.getMinRow());
        assertEquals(2, ref.getMaxRow());
        
        ref.setFirstRow(10);
        //now ref is CellRangeAddress(10, 2, 3, 4)
        assertEquals(2, ref.getMinRow());
        assertEquals(10, ref.getMaxRow());
    }
    
    @Test
    public void testGetMinMaxColumn() {
        final CellRangeAddress ref = new CellRangeAddress(1, 2, 3, 4);
        assertEquals(3, ref.getMinColumn());
        assertEquals(4, ref.getMaxColumn());
        
        ref.setFirstColumn(10);
        //now ref is CellRangeAddress(1, 2, 10, 4)
        assertEquals(4, ref.getMinColumn());
        assertEquals(10, ref.getMaxColumn());
    }
    
    @Test
    public void testIntersects() {
        final CellRangeAddress baseRegion = new CellRangeAddress(0, 1, 0, 1);
        
        final CellRangeAddress duplicateRegion = new CellRangeAddress(0, 1, 0, 1);
        assertIntersects(baseRegion, duplicateRegion);
        
        final CellRangeAddress partiallyOverlappingRegion = new CellRangeAddress(1, 2, 1, 2);
        assertIntersects(baseRegion, partiallyOverlappingRegion);
        
        final CellRangeAddress subsetRegion = new CellRangeAddress(0, 1, 0, 0);
        assertIntersects(baseRegion, subsetRegion);
    
        final CellRangeAddress supersetRegion = new CellRangeAddress(0, 2, 0, 2);
        assertIntersects(baseRegion, supersetRegion);
        
        final CellRangeAddress disjointRegion = new CellRangeAddress(10, 11, 10, 11);
        assertNotIntersects(baseRegion, disjointRegion);
    }
    
    @Test
    public void containsRow() {
        final CellRangeAddress region = new CellRangeAddress(10, 12, 3, 5);
        
        assertFalse(region.containsRow(9));
        assertTrue(region.containsRow(10));
        assertTrue(region.containsRow(11));
        assertTrue(region.containsRow(12));
        assertFalse(region.containsRow(13));
    }
    
    @Test
    public void containsColumn() {
        final CellRangeAddress region = new CellRangeAddress(10, 12, 3, 5);
        
        assertFalse(region.containsColumn(2));
        assertTrue(region.containsColumn(3));
        assertTrue(region.containsColumn(4));
        assertTrue(region.containsColumn(5));
        assertFalse(region.containsColumn(6));
    }
    
    @Test
    public void iterator() {
        final CellRangeAddress A1_B2 = new CellRangeAddress(0, 1, 0, 1);
        
        // the cell address iterator iterates in row major order
        final Iterator<CellAddress> iter = A1_B2.iterator();
        assertEquals("A1", new CellAddress(0, 0), iter.next());
        assertEquals("B1", new CellAddress(0, 1), iter.next());
        assertEquals("A2", new CellAddress(1, 0), iter.next());
        assertEquals("B2", new CellAddress(1, 1), iter.next());
        assertFalse(iter.hasNext());
        try {
            iter.next();
            fail("Expected NoSuchElementException");
        } catch (final NoSuchElementException e) {
            //expected
        }
        try {
            iter.remove();
            fail("Expected UnsupportedOperationException");
        } catch (final UnsupportedOperationException e) {
            //expected
        }
        
        // for each interface
        int count = 0;
        for (final CellAddress addr : A1_B2) {
            assertNotNull(addr);
            count++;
        }
        assertEquals(4, count);
    }
    
    private static void assertIntersects(CellRangeAddress regionA, CellRangeAddress regionB) {
        if (!(regionA.intersects(regionB) && regionB.intersects(regionA))) {
            final String A = regionA.formatAsString();
            final String B = regionB.formatAsString();
            fail("expected: regions "+A+" and "+B+" intersect");
        }
    }
    private static void assertNotIntersects(CellRangeAddress regionA, CellRangeAddress regionB) {
        if ((regionA.intersects(regionB) || regionB.intersects(regionA))) {
            final String A = regionA.formatAsString();
            final String B = regionB.formatAsString();
            fail("expected: regions "+A+" and "+B+" do not intersect");
        }
    }
}
