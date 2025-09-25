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
package org.apache.poi.util.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.poi.ooxml.util.IdentifierManager;
import org.junit.jupiter.api.Test;

class TestIdentifierManager {
    @Test
    void testBasic() {
        IdentifierManager manager = new IdentifierManager(0L,100L);
        assertEquals(101L,manager.getRemainingIdentifiers());
        assertEquals(0L,manager.reserveNew());
        assertEquals(100L,manager.getRemainingIdentifiers());
        assertEquals(1L,manager.reserve(0L));
        assertEquals(99L,manager.getRemainingIdentifiers());
    }

    @Test
    void testLongLimits() {
        long min = IdentifierManager.MIN_ID;
        long max = IdentifierManager.MAX_ID;
        IdentifierManager manager = new IdentifierManager(min,max);
        //noinspection ConstantValue
        assertTrue(max - min + 1 > 0, "Limits lead to a long variable overflow");
        assertTrue(manager.getRemainingIdentifiers() > 0, "Limits lead to a long variable overflow");
        assertEquals(min,manager.reserveNew());
        assertEquals(max,manager.reserve(max));
        assertEquals(max - min -1, manager.getRemainingIdentifiers());
        manager.release(max);
        manager.release(min);
    }

    @Test
    void testReserve() {
        IdentifierManager manager1 = new IdentifierManager(10L, 30L);
        assertEquals(12L, manager1.reserve(12L));
        long reserve = manager1.reserve(12L);
        assertNotEquals(12L, reserve, "Same id must be reserved twice!");
        assertTrue(manager1.release(12L));
        assertTrue(manager1.release(reserve));
        assertFalse(manager1.release(12L));
        assertFalse(manager1.release(reserve));

        IdentifierManager manager2 = new IdentifierManager(0L, 2L);
        assertEquals(0L, manager2.reserve(0L));
        assertEquals(1L, manager2.reserve(1L));
        assertEquals(2L, manager2.reserve(2L));
        assertThrows(IllegalStateException.class, () -> manager2.reserve(0L));
        assertThrows(IllegalStateException.class, () -> manager2.reserve(1L));
        assertThrows(IllegalStateException.class, () -> manager2.reserve(2L));
    }

    @Test
    void testReserveNew() {
        IdentifierManager manager = new IdentifierManager(10L,12L);
        assertEquals(10L,manager.reserveNew());
        assertEquals(11L,manager.reserveNew());
        assertEquals(12L,manager.reserveNew());
        assertThrows(IllegalStateException.class, manager::reserveNew);
    }

    @Test
    void testRelease() {
        IdentifierManager manager = new IdentifierManager(10L,20L);
        assertEquals(10L,manager.reserve(10L));
        assertEquals(11L,manager.reserve(11L));
        assertEquals(12L,manager.reserve(12L));
        assertEquals(13L,manager.reserve(13L));
        assertEquals(14L,manager.reserve(14L));

        assertTrue(manager.release(10L));
        assertEquals(10L,manager.reserve(10L));
        assertTrue(manager.release(10L));

        assertTrue(manager.release(11L));
        assertEquals(11L,manager.reserve(11L));
        assertTrue(manager.release(11L));
        assertFalse(manager.release(11L));
        assertFalse(manager.release(10L));

        assertEquals(10L,manager.reserve(10L));
        assertEquals(11L,manager.reserve(11L));
        assertTrue(manager.release(12L));
    }

    @Test
    void testBounds() {
        assertThrows(IllegalArgumentException.class,
                () -> new IdentifierManager(1, 0),
                "Lower bound higher than upper");

        assertThrows(IllegalArgumentException.class,
                () -> new IdentifierManager(-1, 0),
                "Out of lower bound");

        assertThrows(IllegalArgumentException.class,
                () -> new IdentifierManager(0, Long.MAX_VALUE),
                "Out of upper bound");

        IdentifierManager manager = new IdentifierManager(10, 100);
        assertThrows(IllegalArgumentException.class,
                () -> manager.reserve(9),
                "Reserve out of lower bound");
        assertThrows(IllegalArgumentException.class,
                () -> manager.reserve(101),
                "Reserve out of upper bound");

        // normal reserving works
        assertEquals(10, manager.reserve(10));
        assertEquals(100, manager.reserve(100));

        assertEquals(11, manager.reserve(10));
        assertEquals(12, manager.reserve(100));

        for (int i = 13; i < 100; i++) {
            assertEquals(i, manager.reserve(10));
            assertEquals(100 - i - 1,
                    manager.getRemainingIdentifiers());
        }

        // now the manager is exhausted
        assertThrows(IllegalStateException.class,
                () -> manager.reserve(10),
                "No more ids left any more");

        assertThrows(IllegalArgumentException.class,
                () -> manager.release(9),
                "Release out of lower bound");
        assertThrows(IllegalArgumentException.class,
                () -> manager.release(101),
                "Release out of upper bound");

        for (int i = 10; i < 100; i++) {
            assertTrue(manager.release(i));
            assertEquals(i - 10 + 1,
                    manager.getRemainingIdentifiers());
        }

        assertEquals(100 - 10, manager.getRemainingIdentifiers());
    }
}
