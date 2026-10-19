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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Test;

final class TestReplacingInputStream {

    @Test
    void replace() throws IOException {
        assertEquals("a<br/>b<br/>", replace("a<br>b<br>", "<br>", "<br/>"));
        assertEquals("<b<b", replace("<b<b", "<br>", "<br/>"));
        assertEquals("ab", replace("a xmlns=\"x\"b", " xmlns=\"x\"", null));
    }

    /**
     * Bytes of 0x80 and above in the pattern and the replacement
     */
    @Test
    void replaceHighBytes() throws IOException {
        byte[] input = {0x41, (byte) 0xC3, (byte) 0xA4, 0x42, (byte) 0xC3};
        byte[] pattern = {(byte) 0xC3, (byte) 0xA4};
        byte[] replacement = {(byte) 0xFF, 0x00, (byte) 0x80};
        byte[] expected = {0x41, (byte) 0xFF, 0x00, (byte) 0x80, 0x42, (byte) 0xC3};

        try (InputStream is = new ReplacingInputStream(new ByteArrayInputStream(input), pattern, replacement)) {
            assertArrayEquals(expected, IOUtils.toByteArray(is));
        }

        // every value returned by read() must be in the range 0..255 or -1 at the end
        try (InputStream is = new ReplacingInputStream(new ByteArrayInputStream(input), pattern, replacement)) {
            for (byte b : expected) {
                assertEquals(b & 0xFF, is.read());
            }
            assertEquals(-1, is.read());
        }
    }

    @Test
    void skipGoesThroughReplacement() throws IOException {
        try (InputStream is = new ReplacingInputStream(new ByteArrayInputStream("a<br>bc".getBytes(UTF_8)), "<br>", "<br/>")) {
            assertEquals(6, is.skip(6));
            assertEquals('b', is.read());
            assertEquals(1, is.skip(10));
            assertEquals(0, is.skip(1));
            assertEquals(-1, is.read());
        }
    }

    @Test
    void markNotSupported() throws IOException {
        try (InputStream is = new ReplacingInputStream(new ByteArrayInputStream(new byte[1]), "a", "b")) {
            assertFalse(is.markSupported());
            assertThrows(IOException.class, is::reset);
        }
    }

    private static String replace(String input, String pattern, String replacement) throws IOException {
        try (InputStream is = new ReplacingInputStream(new ByteArrayInputStream(input.getBytes(UTF_8)), pattern, replacement)) {
            return new String(IOUtils.toByteArray(is), UTF_8);
        }
    }
}
