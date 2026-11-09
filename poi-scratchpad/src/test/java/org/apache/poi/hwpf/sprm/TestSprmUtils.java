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

package org.apache.poi.hwpf.sprm;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class TestSprmUtils {

    @Test
    void addVariableLengthSprm() {
        List<byte[]> list = new ArrayList<>();
        // type 6 (0xD202) has a variable length parameter
        assertEquals(5, SprmUtils.addSprm((short) 0xD202, 0, new byte[]{1, 2}, list));
        assertArrayEquals(new byte[]{0x02, (byte) 0xD2, 2, 1, 2}, list.get(0));
    }

    @Test
    void variableLengthSprmNeedsParameter() {
        List<byte[]> list = new ArrayList<>();
        assertThrows(IllegalArgumentException.class, () -> SprmUtils.addSprm((short) 0xD202, 0, null, list));
        assertEquals(0, list.size());
    }
}
