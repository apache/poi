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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class TestPlfLfo {
    @Test
    void testAdd() {
        PlfLfo p = new PlfLfo(new byte[] {0, 0, 0, 0}, 0, 0);
        assertEquals(0, p.getLfoMac());
        p.add(new LFO(new byte[] {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}, 0), new LFOData());
        assertEquals(1, p.getLfoMac());
        assertNotNull(p.getLfo(1));
        assertNotNull(p.getLfoData(1));
    }

    @Test
    void testEquals() {
        PlfLfo p = new PlfLfo(new byte[] {0, 0, 0, 0}, 0, 0);
        PlfLfo p2 = new PlfLfo(new byte[] {0, 0, 0, 0}, 0, 0);
        assertEquals(0, p.getLfoMac());
        assertEquals(0, p2.getLfoMac());

        assertEquals(p, p2);
        //noinspection ObjectEqualsNull
        assertNotEquals(null, p);

        p.add(new LFO(new byte[] {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}, 0), new LFOData());
        assertEquals(1, p.getLfoMac());

        assertNotEquals(p, p2);

        p2.add(new LFO(new byte[] {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}, 0), new LFOData());
        assertEquals(1, p2.getLfoMac());
        assertEquals(p, p2);
    }
}