/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */
package org.apache.poi.hwpf.model;

import org.junit.Test;

import static org.junit.Assert.*;

public class PlfLfoTest {
    @Test
    public void testAdd() {
        PlfLfo p = new PlfLfo(new byte[] {0, 0, 0, 0}, 0, 0);
        assertEquals(0, p.getLfoMac());
        p.add(new LFO(new byte[] {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}, 0), new LFOData());
        assertEquals(1, p.getLfoMac());
        assertNotNull(p.getLfo(1));
        assertNotNull(p.getLfoData(1));
    }

    @Test
    public void testEquals() {
        PlfLfo p = new PlfLfo(new byte[] {0, 0, 0, 0}, 0, 0);
        PlfLfo p2 = new PlfLfo(new byte[] {0, 0, 0, 0}, 0, 0);
        assertEquals(0, p.getLfoMac());
        assertEquals(0, p2.getLfoMac());

        assertTrue(p.equals(p2));
        //noinspection ObjectEqualsNull
        assertFalse(p.equals(null));

        p.add(new LFO(new byte[] {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}, 0), new LFOData());
        assertEquals(1, p.getLfoMac());

        assertFalse(p.equals(p2));

        p2.add(new LFO(new byte[] {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}, 0), new LFOData());
        assertEquals(1, p2.getLfoMac());
        assertTrue(p.equals(p2));
    }
}