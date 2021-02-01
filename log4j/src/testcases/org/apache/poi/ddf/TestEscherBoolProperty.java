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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

final class TestEscherBoolProperty {
    @Test
    void testToString() {
        EscherBoolProperty p = new EscherBoolProperty(EscherPropertyTypes.GEOMETRY__FILLOK, 1);
        String expected =
            "{   /* GEOMETRY__FILLOK */\n" +
            "\t  \"id\": 383 /* 0x017f */\n" +
            "\t, \"name\": \"geometry.fillok\"\n" +
            "\t, \"propertyNumber\": 383 /* 0x017f */\n" +
            "\t, \"propertySize\": 6\n" +
            "\t, \"flags\": 383 /*  */ \n" +
            "\t, \"value\": 1\n" +
            "}";
        assertEquals(expected, p.toString().replace("\r", ""));
    }
}
