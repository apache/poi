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

package org.apache.poi.ss.formula;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.emptyArray;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link FormulaCellCacheEntrySet}, the open-addressing identity set of the formula cells
 * that consume a cached cell.
 */
class TestFormulaCellCacheEntrySet {

    @Test
    void addRemoveToArray() {
        FormulaCellCacheEntrySet set = new FormulaCellCacheEntrySet();
        assertThat(set.toArray(), emptyArray());
        assertNull(set.peekAny());

        FormulaCellCacheEntry a = new FormulaCellCacheEntry();
        FormulaCellCacheEntry b = new FormulaCellCacheEntry();
        assertTrue(set.add(a));
        assertFalse(set.add(a), "adding again");
        assertTrue(set.add(b));
        assertThat(set.toArray(), arrayContainingInAnyOrder(a, b));

        assertTrue(set.remove(a));
        assertFalse(set.remove(a), "removing again");
        assertThat(set.toArray(), arrayContainingInAnyOrder(b));
        assertTrue(set.remove(b));
        assertThat(set.toArray(), emptyArray());
        assertNull(set.peekAny());
    }

    @Test
    void drainingLargeSet() {
        // enough entries to go through several grow re-hashes on the way in and several shrink
        // re-hashes on the way out
        int n = 1000;
        FormulaCellCacheEntrySet set = new FormulaCellCacheEntrySet();
        Set<FormulaCellCacheEntry> expected = Collections.newSetFromMap(new IdentityHashMap<>());
        for (int i = 0; i < n; i++) {
            FormulaCellCacheEntry e = new FormulaCellCacheEntry();
            assertTrue(set.add(e));
            expected.add(e);
        }
        assertThat(set.toArray(), arrayWithSize(n));

        int drained = 0;
        FormulaCellCacheEntry e;
        while ((e = set.peekAny()) != null) {
            assertTrue(set.containsPeeked(e));
            assertTrue(expected.remove(e), "peekAny returned an entry that was removed already");
            assertTrue(set.remove(e));
            assertFalse(set.containsPeeked(e));
            drained++;
        }
        assertEquals(n, drained);
        assertTrue(expected.isEmpty());
        assertThat(set.toArray(), emptyArray());
    }

    @Test
    void peekAnyDoesNotRemove() {
        FormulaCellCacheEntrySet set = new FormulaCellCacheEntrySet();
        FormulaCellCacheEntry a = new FormulaCellCacheEntry();
        set.add(a);
        assertNotNull(set.peekAny());
        assertEquals(a, set.peekAny());
        assertTrue(set.containsPeeked(a));
        assertThat(set.toArray(), arrayContainingInAnyOrder(a));
    }
}
