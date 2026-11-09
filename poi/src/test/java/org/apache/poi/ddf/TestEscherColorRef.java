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

package org.apache.poi.ddf;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class TestEscherColorRef {

    @Test
    void readColor() {
        byte[] data = {0x10, 0x20, 0x30, 0x00};
        EscherColorRef ref = new EscherColorRef(data, 0, 4);
        assertArrayEquals(new int[]{0x10, 0x20, 0x30}, ref.getRGB());
    }

    @Test
    void rejectsOtherLengths() {
        byte[] data = new byte[8];
        assertThrows(IllegalArgumentException.class, () -> new EscherColorRef(data, 0, 5));
        assertThrows(IllegalArgumentException.class, () -> new EscherColorRef(data, 0, 8));
    }
}
