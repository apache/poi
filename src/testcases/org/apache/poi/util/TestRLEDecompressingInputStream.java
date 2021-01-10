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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.junit.jupiter.api.Test;

class TestRLEDecompressingInputStream {

    /**
     * Section 3.2.1 No Compression Example
     *
     * The following string illustrates an ASCII text string with a set of characters that cannot be compressed
     * by the compression algorithm specified in section 2.4.1.
     *
     * abcdefghijklmnopqrstuv.
     *
     * This example is provided to demonstrate the results of compressing and decompressing the string
     * using an interoperable implementation of the algorithm specified in section 2.4.1.
     *
     * The following hex array represents the compressed byte array of the example string as compressed by
     * the compression algorithm.
     *
     * 01 19 B0 00 61 62 63 64 65 66 67 68 00 69 6A 6B 6C
     * 6D 6E 6F 70 00 71 72 73 74 75 76 2E
     *
     * The following hex array represents the decompressed byte array of the example string as
     * decompressed by the decompression algorithm.
     *
     * 61 62 63 64 65 66 67 68 69 6A 6B 6C 6D 6E 6F 70 71
     * 72 73 74 75 76 2E
     *
     */
    @Test
    void noCompressionExample() {
        final byte[] compressed = {
            0x01, 0x19, (byte)0xB0, 0x00, 0x61, 0x62, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x00, 0x69, 0x6A, 0x6B, 0x6C,
            0x6D, 0x6E, 0x6F, 0x70, 0x00, 0x71, 0x72, 0x73, 0x74, 0x75, 0x76, 0x2E
        };
        final String expected = "abcdefghijklmnopqrstuv.";
        checkRLEDecompression(expected, compressed);
    }

    /**
     * Section 3.2.2 Normal Compression Example
     *
     * The following string illustrates an ASCII text string with a typical set of characters that can be
     * compressed by the compression algorithm.
     *
     *     #aaabcdefaaaaghijaaaaaklaaamnopqaaaaaaaaaaaarstuvwxyzaaa
     *
     * This example is provided to demonstrate the results of compressing and decompressing the example
     * string using an interoperable implementation of the algorithm specified in section 2.4.1.
     *
     * The following hex array represents the compressed byte array of the example string as compressed by
     * the compression algorithm:
     *
     *     01 2F B0 00 23 61 61 61 62 63 64 65 82 66 00 70
     *     61 67 68 69 6A 01 38 08 61 6B 6C 00 30 6D 6E 6F
     *     70 06 71 02 70 04 10 72 73 74 75 76 10 77 78 79
     *     7A 00 3C
     *
     * The following hex array represents the decompressed byte array of the example string as
     * decompressed by the decompression algorithm:
     *
     *     23 61 61 61 62 63 64 65  66 61 61 61 61 67 68 69
     *     6a 61 61 61 61 61 6B 6C  61 61 61 6D 6E 6F 70 71
     *     61 61 61 61 61 61 61 61  61 61 61 61 72 73 74 75
     *     76 77 78 79 7A 61 61 61
     */
    @Test
    void normalCompressionExample() {
        final byte[] compressed = {
            0x01, 0x2F, (byte)0xB0, 0x00, 0x23, 0x61, 0x61, 0x61, 0x62, 0x63, 0x64, 0x65, (byte)0x82, 0x66, 0x00, 0x70,
            0x61, 0x67, 0x68, 0x69, 0x6A, 0x01, 0x38, 0x08, 0x61, 0x6B, 0x6C, 0x00, 0x30, 0x6D, 0x6E, 0x6F,
            0x70, 0x06, 0x71, 0x02, 0x70, 0x04, 0x10, 0x72, 0x73, 0x74, 0x75, 0x76, 0x10, 0x77, 0x78, 0x79,
            0x7A, 0x00, 0x3C
        };
        final String expected = "#aaabcdefaaaaghijaaaaaklaaamnopqaaaaaaaaaaaarstuvwxyzaaa";
        checkRLEDecompression(expected, compressed);
    }

    /**
     * Section 3.2.3 Maximum Compression Example
     *
     * The following string illustrates an ASCII text string with a typical set of characters that can be
     * compressed by the compression algorithm.
     *
     *     aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
     *
     * This example is provided to demonstrate the results of compressing and decompressing the example
     * string using an interoperable implementation of the algorithm specified in section 2.4.1.
     *
     * The following hex array represents the compressed byte array of the example string as compressed by
     * the compression algorithm:
     *
     *     01 03 B0 02 61 45 00
     *
     * The following hex array represents the decompressed byte array of the example string as
     * decompressed by the decompression algorithm:
     *
     *     61 61 61 61 61 61 61 61  61 61 61 61 61 61 61 61
     *     61 61 61 61 61 61 61 61  61 61 61 61 61 61 61 61
     *     61 61 61 61 61 61 61 61  61 61 61 61 61 61 61 61
     *     61 61 61 61 61 61 61 61  61 61 61 61 61 61 61 61
     *     61 61 61 61 61 61 61 61  61
     */
    @Test
    void maximumCompressionExample() {
        final byte[] compressed = {
            0x01, 0x03, (byte)0xB0, 0x02, 0x61, 0x45, 0x00
        };
        final String expected = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
        checkRLEDecompression(expected, compressed);
    }

    @Test
    void decompress() throws IOException {
        final byte[] compressed = {
            0x01, 0x03, (byte)0xB0, 0x02, 0x61, 0x45, 0x00
        };
        final byte[] expanded = RLEDecompressingInputStream.decompress(compressed);
        final byte[] expected = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa".getBytes(StringUtil.UTF8);
        assertArrayEquals(expected, expanded);
    }

    private static void checkRLEDecompression(String expected, byte[] runLengthEncodedData) {
        InputStream compressedStream = new ByteArrayInputStream(runLengthEncodedData);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            InputStream stream = new RLEDecompressingInputStream(compressedStream);
            try {
                IOUtils.copy(stream, out);
            } finally {
                out.close();
                stream.close();
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        String expanded;
        try {
            expanded = out.toString(StringUtil.UTF8.name());
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        assertEquals(expected, expanded);
    }
}
