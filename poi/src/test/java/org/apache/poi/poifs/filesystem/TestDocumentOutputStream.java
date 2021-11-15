
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

package org.apache.poi.poifs.filesystem;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.apache.poi.util.IOUtils;
import org.junit.jupiter.api.Test;

/**
 * Class to test DocumentOutputStream functionality
 */
final class TestDocumentOutputStream {

    /**
     * test write(int) behavior
     */
    @Test
    void testWrite1() throws IOException {
        final byte[] expected = data(25);

        POIFSWriterListener l = (event) -> {
            DocumentOutputStream dstream = event.getStream();
            assertDoesNotThrow(() -> { for (byte b : expected) dstream.write(b); }, "stream exhausted too early");
            assertThrows(IOException.class, () -> dstream.write(0));
        };

        compare(l, expected);
    }

    /**
     * test write(byte[]) behavior
     */
    @Test
    void testWrite2() throws IOException {
        final byte[] expected = data(24);

        POIFSWriterListener l = (event) -> {
            DocumentOutputStream dstream = event.getStream();
            assertDoesNotThrow(() -> dstream.write(expected), "stream exhausted too early");
            assertThrows(IOException.class, () -> dstream.write(new byte[]{'7','7','7','7'}));
        };

        compare(l, expected);
    }

    /**
     * test write(byte[], int, int) behavior
     */
    @Test
    void testWrite3() throws IOException {
        byte[] input = data(50);
        byte[] expected = Arrays.copyOfRange(input, 1, 1+25);

        POIFSWriterListener l = (event) -> {
            DocumentOutputStream dstream = event.getStream();
            assertDoesNotThrow(() -> dstream.write(input, 1, 25), "stream exhausted too early");
            assertThrows(IOException.class, () -> dstream.write(input, 0, 1));
        };

        compare(l, expected);
    }

    private static byte[] data(int len) {
        byte[] input = new byte[len];
        for (int i = 0; i < len; i++) {
            input[i] = (byte)('0' + (i%10));
        }
        return input;
    }

    private void compare(POIFSWriterListener l, byte[] expected) throws IOException {
        try (POIFSFileSystem poifs = new POIFSFileSystem()) {
            DirectoryNode root = poifs.getRoot();
            root.createDocument("foo", expected.length, l);

            try (DocumentInputStream is = root.createDocumentInputStream("foo")) {
                final UnsynchronizedByteArrayOutputStream bos = new UnsynchronizedByteArrayOutputStream(expected.length);
                IOUtils.copy(is, bos);
                assertArrayEquals(expected, bos.toByteArray());
            }
        }
    }
}
