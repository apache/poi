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

package org.apache.poi.xdgf.usermodel.section;

import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

import org.junit.Assert;
import org.junit.Test;

public class CombinedIteratorTest {

    void testIteration(CombinedIterable<String> iterable, String... expected) {

        Iterator<String> iter = iterable.iterator();

        for (String element : expected) {
            Assert.assertEquals(true, iter.hasNext());
            Assert.assertEquals(element, iter.next());
        }

        Assert.assertEquals(false, iter.hasNext());
    }

    @Test
    public void testNullMaster() {

        SortedMap<Long, String> base = new TreeMap<>();
        base.put(1L, "B1");
        base.put(2L, "B2");
        base.put(3L, "B3");

        testIteration(new CombinedIterable<>(base, null), "B1", "B2",
                "B3");
    }

    @Test
    public void testNoMatchesBaseFirst() {

        SortedMap<Long, String> base = new TreeMap<>();
        base.put(1L, "B1");
        base.put(2L, "B2");
        base.put(3L, "B3");

        SortedMap<Long, String> master = new TreeMap<>();
        master.put(4L, "M4");
        master.put(5L, "M5");
        master.put(6L, "M6");

        testIteration(new CombinedIterable<>(base, master), "B1", "B2",
                "B3", "M4", "M5", "M6");
    }

    @Test
    public void testNoMatchesMasterFirst() {

        SortedMap<Long, String> base = new TreeMap<>();
        base.put(4L, "B4");
        base.put(5L, "B5");
        base.put(6L, "B6");

        SortedMap<Long, String> master = new TreeMap<>();
        master.put(1L, "M1");
        master.put(2L, "M2");
        master.put(3L, "M3");

        testIteration(new CombinedIterable<>(base, master), "M1", "M2",
                "M3", "B4", "B5", "B6");
    }

    @Test
    public void testInterleaved1() {

        SortedMap<Long, String> base = new TreeMap<>();
        base.put(1L, "B1");
        base.put(3L, "B3");
        base.put(5L, "B5");

        SortedMap<Long, String> master = new TreeMap<>();
        master.put(2L, "M2");
        master.put(4L, "M4");
        master.put(6L, "M6");

        testIteration(new CombinedIterable<>(base, master), "B1", "M2",
                "B3", "M4", "B5", "M6");
    }

    @Test
    public void testInterleaved2() {

        SortedMap<Long, String> base = new TreeMap<>();
        base.put(1L, "B1");
        base.put(2L, "B2");
        base.put(5L, "B5");
        base.put(6L, "B6");

        SortedMap<Long, String> master = new TreeMap<>();
        master.put(3L, "M3");
        master.put(4L, "M4");
        master.put(7L, "M7");
        master.put(8L, "M8");

        testIteration(new CombinedIterable<>(base, master), "B1", "B2",
                "M3", "M4", "B5", "B6", "M7", "M8");
    }

    @Test
    public void testAllMatching() {

        SortedMap<Long, String> base = new TreeMap<>();
        base.put(1L, "B1");
        base.put(2L, "B2");
        base.put(3L, "B3");

        SortedMap<Long, String> master = new TreeMap<>();
        master.put(1L, "M1");
        master.put(2L, "M2");
        master.put(3L, "M3");

        testIteration(new CombinedIterable<>(base, master), "B1", "B2",
                "B3");
    }

    @Test
    public void testAllMatching2() {

        SortedMap<Long, String> base = new TreeMap<>();
        base.put(1L, "B1");
        base.put(2L, "B2");
        base.put(3L, "B3");

        SortedMap<Long, String> master = new TreeMap<>();
        master.put(1L, "M1");
        master.put(2L, "M2");
        master.put(3L, "M3");
        master.put(4L, "M4");

        testIteration(new CombinedIterable<>(base, master), "B1", "B2",
                "B3", "M4");
    }
}
