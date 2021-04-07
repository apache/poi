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

package org.apache.poi.ss.usermodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * @author Yegor Kozlov
 */
final class TestIndexedColors {

    @Test
    void fromInt() {
        int[] illegalIndices = { -1, 65 };
        for (int index : illegalIndices) {
            assertThrows(IllegalArgumentException.class, () -> IndexedColors.fromInt(index));
        }
        assertEquals(IndexedColors.BLACK, IndexedColors.fromInt(8));
        assertEquals(IndexedColors.GOLD, IndexedColors.fromInt(51));
        assertEquals(IndexedColors.AUTOMATIC, IndexedColors.fromInt(64));
    }

    @Test
    void getIndex() {
        assertEquals(51, IndexedColors.GOLD.getIndex());
    }

    @Test
    void index() {
        assertEquals(51, IndexedColors.GOLD.index);
    }
}