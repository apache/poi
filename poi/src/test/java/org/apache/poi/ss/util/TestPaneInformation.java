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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

final class TestPaneInformation {
    @Test
    void testEquals() {
        PaneInformation pi1 = new PaneInformation((short) 1, (short) 2, (short) 3, (short) 4, (byte) 5, true);
        PaneInformation p12 = new PaneInformation((short) 1, (short) 2, (short) 3, (short) 4, (byte) 5, true);
        assertEquals(pi1, p12);
        assertEquals(pi1.hashCode(), p12.hashCode());
    }

    @Test
    void testNotEquals() {
        PaneInformation pi1 = new PaneInformation((short) 1, (short) 1, (short) 1, (short) 1, (byte) 1, true);
        PaneInformation pi2 = new PaneInformation((short) 1, (short) 1, (short) 1, (short) 1, (byte) 1, false);
        PaneInformation pi3 = new PaneInformation((short) 1, (short) 1, (short) 1, (short) 1, (byte) 2, true);
        PaneInformation pi4 = new PaneInformation((short) 1, (short) 1, (short) 1, (short) 2, (byte) 1, true);
        PaneInformation pi5 = new PaneInformation((short) 1, (short) 1, (short) 1, (short) 2, (byte) 1, false);
        PaneInformation pi6 = new PaneInformation((short) 1, (short) 1, (short) 2, (short) 1, (byte) 1, true);
        PaneInformation pi7 = new PaneInformation((short) 1, (short) 1, (short) 2, (short) 1, (byte) 1, false);
        PaneInformation pi8 = new PaneInformation((short) 1, (short) 2, (short) 1, (short) 1, (byte) 1, true);
        PaneInformation pi9 = new PaneInformation((short) 1, (short) 2, (short) 1, (short) 1, (byte) 1, false);
        PaneInformation pi10 = new PaneInformation((short) 2, (short) 1, (short) 1, (short) 1, (byte) 1, true);
        PaneInformation pi11 = new PaneInformation((short) 2, (short) 1, (short) 1, (short) 1, (byte) 1, false);
        assertNotEquals(pi1, pi2);
        assertNotEquals(pi1, pi3);
        assertNotEquals(pi1, pi4);
        assertNotEquals(pi1, pi5);
        assertNotEquals(pi1, pi6);
        assertNotEquals(pi1, pi7);
        assertNotEquals(pi1, pi8);
        assertNotEquals(pi1, pi9);
        assertNotEquals(pi1, pi10);
        assertNotEquals(pi1, pi11);
        assertNotEquals(pi1.hashCode(), pi2.hashCode());
        assertNotEquals(pi1.hashCode(), pi3.hashCode());
        assertNotEquals(pi1.hashCode(), pi4.hashCode());
        assertNotEquals(pi1.hashCode(), pi5.hashCode());
        assertNotEquals(pi1.hashCode(), pi6.hashCode());
        assertNotEquals(pi1.hashCode(), pi7.hashCode());
        assertNotEquals(pi1.hashCode(), pi8.hashCode());
        assertNotEquals(pi1.hashCode(), pi9.hashCode());
        assertNotEquals(pi1.hashCode(), pi10.hashCode());
        assertNotEquals(pi1.hashCode(), pi11.hashCode());
    }
}
