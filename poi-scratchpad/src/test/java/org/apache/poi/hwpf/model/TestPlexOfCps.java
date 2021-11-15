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

package org.apache.poi.hwpf.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.poi.hwpf.HWPFDocFixture;
import org.apache.poi.util.LittleEndian;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public final class TestPlexOfCps {
    private PlexOfCps _plexOfCps;
    private HWPFDocFixture _hWPFDocFixture;

    @Test
    void testWriteRead() {
        _plexOfCps = new PlexOfCps(4);

        int last = 0;
        for (int x = 0; x < 110; x++) {
            byte[] intHolder = new byte[4];
            int span = (int) (110.0f * Math.random());
            LittleEndian.putInt(intHolder, 0, span);
            _plexOfCps.addProperty(new GenericPropertyNode(last, last + span, intHolder));
            last += span;
        }

        byte[] output = _plexOfCps.toByteArray();
        _plexOfCps = new PlexOfCps(output, 0, output.length, 4);
        int len = _plexOfCps.length();
        assertEquals(110, len);

        last = 0;
        for (int x = 0; x < len; x++) {
            GenericPropertyNode node = _plexOfCps.getProperty(x);
            assertEquals(node.getStart(), last);
            last = node.getEnd();
            int span = LittleEndian.getInt(node.getBytes());
            assertEquals(node.getEnd() - node.getStart(), span);
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        /* @todo verify the constructors*/
        _hWPFDocFixture = new HWPFDocFixture(this, HWPFDocFixture.DEFAULT_TEST_FILE);

        _hWPFDocFixture.setUp();
    }

    @AfterEach
    void tearDown() {
        _plexOfCps = null;
        _hWPFDocFixture.tearDown();

        _hWPFDocFixture = null;
    }

}
