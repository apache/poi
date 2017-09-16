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

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assume.assumeTrue;

import java.util.Arrays;

/**
 * Tests that the common CellAddress works as we need it to.
 */
public final class TestCellAddress {
    @Test
    public void testConstructors() {
        CellAddress cellAddress;
        final CellReference cellRef = new CellReference("Sheet1", 0, 0, true, true);
        final String address = "A1";
        final int row = 0;
        final int col = 0;

        cellAddress = new CellAddress(row, col);
        assertEquals(CellAddress.A1, cellAddress);

        cellAddress = new CellAddress(address);
        assertEquals(CellAddress.A1, cellAddress);

        cellAddress = new CellAddress(cellRef);
        assertEquals(CellAddress.A1, cellAddress);
    }

    @Test
    public void testFormatAsString() {
        assertEquals("A1", CellAddress.A1.formatAsString());
    }

    @Test
    public void testEquals() {
        assertEquals(new CellReference(6, 4), new CellReference(6, 4));
        assertNotEquals(new CellReference(4, 6), new CellReference(6, 4));
    }
    
    @SuppressWarnings("EqualsWithItself")
    @Test
    public void testCompareTo() {
        final CellAddress A1 = new CellAddress(0, 0);
        final CellAddress A2 = new CellAddress(1, 0);
        final CellAddress B1 = new CellAddress(0, 1);
        final CellAddress B2 = new CellAddress(1, 1);
        
        assertEquals(0,  A1.compareTo(A1));
        assertEquals(-1, A1.compareTo(B1));
        assertEquals(-1, A1.compareTo(A2));
        assertEquals(-1, A1.compareTo(B2));
        
        assertEquals(1,  B1.compareTo(A1));
        assertEquals(0,  B1.compareTo(B1));
        assertEquals(-1, B1.compareTo(A2));
        assertEquals(-1, B1.compareTo(B2));
        
        assertEquals(1,  A2.compareTo(A1));
        assertEquals(1,  A2.compareTo(B1));
        assertEquals(0,  A2.compareTo(A2));
        assertEquals(-1, A2.compareTo(B2));
        
        assertEquals(1,  B2.compareTo(A1));
        assertEquals(1,  B2.compareTo(B1));
        assertEquals(1,  B2.compareTo(A2));
        assertEquals(0,  B2.compareTo(B2));
        
        CellAddress[] sorted = {A1, B1, A2, B2};
        CellAddress[] unsorted = {B1, B2, A1, A2};
        assumeTrue(!Arrays.equals(sorted, unsorted));
        Arrays.sort(unsorted);
        assertArrayEquals(sorted, unsorted);
    }

    @Test
    public void testGetRow() {
        final CellAddress addr = new CellAddress(6, 4);
        assertEquals(6, addr.getRow());
    }
    
    @Test
    public void testGetColumn() {
        final CellAddress addr = new CellAddress(6, 4);
        assertEquals(4, addr.getColumn());
    }
}
