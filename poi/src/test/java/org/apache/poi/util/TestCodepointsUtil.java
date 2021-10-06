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

package org.apache.poi.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Unit test for CodepointsUtil
 */
class TestCodepointsUtil {

    @Test
    void testIterator() {
        final String unicodeSurrogates = "\uD835\uDF4A\uD835\uDF4B\uD835\uDF4C\uD835\uDF4D\uD835\uDF4E"
                + "abcdef123456";
        Iterator<String> sci = CodepointsUtil.iteratorFor(unicodeSurrogates);
        List<String> codePoints = new ArrayList<>();
        CodepointsUtil.iteratorFor(unicodeSurrogates).forEachRemaining(codePoints::add);
        assertEquals(17, codePoints.size());
        for(String point : codePoints){
            assertTrue(point.length() >=1 && point.length() <= 2, "codepoint " + point + "is wrong size");
        }
    }

}

