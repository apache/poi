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

package org.apache.poi.xssf.binary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.poi.util.LittleEndian;
import org.junit.jupiter.api.Test;

class TestXSSFBUtils {

    /**
     * A char count whose doubled byte length exceeds Integer.MAX_VALUE used to
     * be narrowed via {@code 2*(int)numChars}, wrapping to a negative value that
     * slipped past the {@code offset+numBytes > data.length} check and surfaced
     * as a bare StringIndexOutOfBoundsException. The bounds check must reject it
     * with an XSSFBParseException instead.
     */
    @Test
    void rejectsOversizedWideString() {
        byte[] data = new byte[100];
        LittleEndian.putUInt(data, 0, 0x40000000L); // ~1G chars -> 2G bytes

        assertThrows(XSSFBParseException.class,
                () -> XSSFBUtils.readXLWideString(data, 0, new StringBuilder()));
        assertThrows(XSSFBParseException.class,
                () -> XSSFBUtils.readXLNullableWideString(data, 0, new StringBuilder()));
    }

    /**
     * A char count above 0x7FFFFFFF used to overflow to a small positive
     * numBytes, silently reading the wrong length and desyncing the record
     * stream. It must be rejected by the same bounds check.
     */
    @Test
    void rejectsHighBitWideString() {
        byte[] data = new byte[100];
        LittleEndian.putUInt(data, 0, 0x80000001L);

        assertThrows(XSSFBParseException.class,
                () -> XSSFBUtils.readXLWideString(data, 0, new StringBuilder()));
    }

    @Test
    void readsValidWideString() throws Exception {
        byte[] str = "POI".getBytes(java.nio.charset.StandardCharsets.UTF_16LE);
        byte[] data = new byte[4 + str.length];
        LittleEndian.putUInt(data, 0, 3L);
        System.arraycopy(str, 0, data, 4, str.length);

        StringBuilder sb = new StringBuilder();
        int read = XSSFBUtils.readXLWideString(data, 0, sb);
        assertEquals("POI", sb.toString());
        assertEquals(data.length, read);
    }
}
