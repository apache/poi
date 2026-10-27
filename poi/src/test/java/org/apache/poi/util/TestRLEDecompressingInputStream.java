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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
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
    void noCompressionExample() throws IOException {
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
    void normalCompressionExample() throws IOException {
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
    void maximumCompressionExample() throws IOException {
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

    /**
     * skip must stop at the requested count when it spans several chunks
     */
    @Test
    void skipAcrossChunks() throws IOException {
        // two uncompressed chunks of 4096 bytes each
        final int chunkSize = 4096;
        byte[] data = new byte[1 + 2 * (2 + chunkSize)];
        data[0] = 0x01;
        int pos = 1;
        int value = 0;
        for (int chunk = 0; chunk < 2; chunk++) {
            // chunk header: raw chunk (bit 15 unset), signature 0x3000, size 4096 - 1
            data[pos++] = (byte) 0xFF;
            data[pos++] = (byte) 0x3F;
            for (int i = 0; i < chunkSize; i++) {
                data[pos++] = (byte) (value++ % 251);
            }
        }

        try (InputStream stream = new RLEDecompressingInputStream(new ByteArrayInputStream(data))) {
            assertEquals(10, stream.skip(10));
            assertEquals(5000, stream.skip(5000));
            assertEquals(5010 % 251, stream.read());
            assertEquals(2 * chunkSize - 5011, stream.skip(10000));
            assertEquals(0, stream.skip(1));
            assertEquals(-1, stream.read());
        }
    }

    /**
     * a copy token as the first item of a chunk has nothing to copy from
     */
    @Test
    void copyTokenBeforeChunkStart() {
        // compressed chunk of 3 bytes: flag byte with item 0 set, copy token 0x0000 (offset 1, length 3)
        final byte[] compressed = {
            0x01, 0x02, (byte)0xB0, 0x01, 0x00, 0x00
        };
        assertThrows(IllegalStateException.class, () -> RLEDecompressingInputStream.decompress(compressed));
    }

    /**
     * a copy token may not expand the chunk past 4096 bytes
     */
    @Test
    void copyTokenPastChunkEnd() {
        // literal 'A', then copy token 0x0FFF: offset 1, length 4098
        final byte[] compressed = {
            0x01, 0x03, (byte)0xB0, 0x02, 0x41, (byte)0xFF, 0x0F
        };
        assertThrows(IllegalStateException.class, () -> RLEDecompressingInputStream.decompress(compressed));
    }

    /**
     * a chunk of exactly 4096 bytes is fine, one more literal is not
     */
    @Test
    void literalPastChunkEnd() throws IOException {
        // literal 'A', then copy token 0x0FFC: offset 1, length 4095 -> 4096 bytes
        final byte[] full = {
            0x01, 0x03, (byte)0xB0, 0x02, 0x41, (byte)0xFC, 0x0F
        };
        final byte[] expected = new byte[4096];
        Arrays.fill(expected, (byte) 0x41);
        assertArrayEquals(expected, RLEDecompressingInputStream.decompress(full));

        // the same chunk followed by the literal 'B'
        final byte[] overflow = {
            0x01, 0x04, (byte)0xB0, 0x02, 0x41, (byte)0xFC, 0x0F, 0x42
        };
        assertThrows(IllegalStateException.class, () -> RLEDecompressingInputStream.decompress(overflow));
    }

    /**
     * a chunk without any token must not repeat a byte of the previous chunk,
     * and reading must carry on with the chunk after it
     */
    @Test
    void emptyChunkDoesNotRepeatBytes() throws IOException {
        // "abc", then a compressed chunk consisting of a flag byte only, then "d",
        // then a chunk header without flag byte
        final byte[] compressed = {
            0x01, 0x03, (byte)0xB0, 0x00, 0x61, 0x62, 0x63, 0x00, (byte)0xB0, 0x00,
            0x01, (byte)0xB0, 0x00, 0x64, 0x05, (byte)0xB0
        };
        checkRLEDecompression("abcd", compressed);

        try (InputStream stream = new RLEDecompressingInputStream(new ByteArrayInputStream(compressed))) {
            assertEquals('a', stream.read());
            assertEquals('b', stream.read());
            assertEquals('c', stream.read());
            assertEquals('d', stream.read());
            assertEquals(-1, stream.read());
            assertEquals(-1, stream.read());
        }
    }

    /**
     * a chunk that fails to decode must not leave bytes of the previous chunk to be read
     */
    @Test
    @SuppressForbidden("checks available() reports nothing after a failed chunk")
    void failedChunkLeavesNoStaleBytes() throws IOException {
        // "abcd", then a chunk with the literal 'x' and a copy token reaching 2 bytes back
        final byte[] compressed = {
            0x01, 0x04, (byte)0xB0, 0x00, 0x61, 0x62, 0x63, 0x64,
            0x03, (byte)0xB0, 0x02, 0x78, 0x00, 0x10
        };
        try (InputStream stream = new RLEDecompressingInputStream(new ByteArrayInputStream(compressed))) {
            final byte[] b = new byte[4];
            assertEquals(4, stream.read(b));
            assertArrayEquals("abcd".getBytes(StringUtil.UTF8), b);
            assertThrows(IllegalStateException.class, stream::read);
            assertEquals(-1, stream.read());
            assertEquals(-1, stream.read(b));
            assertEquals(0, stream.skip(1));
            assertEquals(0, stream.available());
        }
    }

    /**
     * input that ends inside a literal or a copy token keeps the bytes decompressed before it,
     * as input that ends at a flag byte does
     */
    @Test
    void truncatedChunkKeepsDecompressedBytes() throws IOException {
        // a flag byte for 8 literals, but the input ends after "ab"
        final byte[] midLiteral = {
            0x01, 0x05, (byte)0xB0, 0x00, 0x61, 0x62
        };
        checkRLEDecompression("ab", midLiteral);

        // "ab", then a flag byte for a copy token, but only one byte of it follows
        final byte[] midCopyToken = {
            0x01, 0x05, (byte)0xB0, 0x04, 0x61, 0x62, 0x00
        };
        checkRLEDecompression("ab", midCopyToken);
    }

    /**
     * an uncompressed chunk always holds 4096 bytes
     */
    @Test
    void shortUncompressedChunk() {
        // raw chunk (bit 15 unset) with size field 0x002: 3 bytes
        final byte[] compressed = {
            0x01, 0x02, 0x30, 0x61, 0x62, 0x63
        };
        assertThrows(IllegalStateException.class, () -> RLEDecompressingInputStream.decompress(compressed));
    }

    private static void checkRLEDecompression(String expected, byte[] runLengthEncodedData) throws IOException {
        InputStream compressedStream = new ByteArrayInputStream(runLengthEncodedData);
        UnsynchronizedByteArrayOutputStream out = UnsynchronizedByteArrayOutputStream.builder().get();
        try (InputStream stream = new RLEDecompressingInputStream(compressedStream)) {
            IOUtils.copy(stream, out);
        }
        String expanded = out.toString(StringUtil.UTF8);
        assertEquals(expected, expanded);
    }
}
