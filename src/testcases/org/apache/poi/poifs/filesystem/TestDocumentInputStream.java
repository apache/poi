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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.apache.poi.POIDataSamples;
import org.apache.poi.util.SuppressForbidden;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Class to test DocumentInputStream functionality
 */
final class TestDocumentInputStream {
    private DocumentNode _workbook_n;
    private byte[] _workbook_data;
    private static final int _workbook_size = 5000;

    // non-even division of _workbook_size, also non-even division of
    // any block size
    private static final int _buffer_size = 6;

    @BeforeEach
    void setUp() throws Exception {
        int blocks = (_workbook_size + 511) / 512;

        _workbook_data = new byte[512 * blocks];
        Arrays.fill(_workbook_data, (byte) -1);
        for (int j = 0; j < _workbook_size; j++) {
            _workbook_data[j] = (byte) (j * j);
        }

        // Now create the POIFS Version
        byte[] _workbook_data_only = Arrays.copyOf(_workbook_data, _workbook_size);

        POIFSFileSystem poifs = new POIFSFileSystem();
        // Make it easy when debugging to see what isn't the doc
        byte[] minus1 = new byte[512];
        Arrays.fill(minus1, (byte) -1);
        poifs.getBlockAt(-1).put(minus1);
        poifs.getBlockAt(0).put(minus1);
        poifs.getBlockAt(1).put(minus1);

        // Create the POIFS document
        _workbook_n = (DocumentNode) poifs.createDocument(
                new ByteArrayInputStream(_workbook_data_only),
                "Workbook"
        );
    }

    /**
     * test constructor
     */
    @Test
    void testConstructor() throws IOException {
        try (DocumentInputStream nstream = new DocumentInputStream(_workbook_n)) {
            assertEquals(_workbook_size, _workbook_n.getSize());
            assertEquals(_workbook_size, available(nstream));
        }
    }

    /**
     * test available() behavior
     */
    @Test
    void testAvailable() throws IOException {
        DocumentInputStream nstream = new DocumentInputStream(_workbook_n);
        assertEquals(_workbook_size, available(nstream));
        nstream.close();

        assertThrows(IllegalStateException.class, () -> available(nstream));
    }

    /**
     * test mark/reset/markSupported.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    void testMarkFunctions() throws IOException {
        byte[] buffer = new byte[_workbook_size / 5];
        byte[] small_buffer = new byte[212];

        DocumentInputStream stream = new DocumentInputStream(_workbook_n);
        // Read a fifth of it, and check all's correct
        stream.read(buffer);
        for (int j = 0; j < buffer.length; j++) {
            assertEquals(_workbook_data[j], buffer[j], "checking byte " + j);
        }
        assertEquals(_workbook_size - buffer.length, available(stream));

        // Reset, and check the available goes back to being the
        //  whole of the stream
        stream.reset();
        assertEquals(_workbook_size, available(stream));


        // Read part of a block
        stream.read(small_buffer);
        for (int j = 0; j < small_buffer.length; j++) {
            assertEquals(_workbook_data[j], small_buffer[j], "checking byte " + j);
        }
        assertEquals(_workbook_size - small_buffer.length, available(stream));
        stream.mark(0);

        // Read the next part
        stream.read(small_buffer);
        for (int j = 0; j < small_buffer.length; j++) {
            assertEquals(_workbook_data[j + small_buffer.length], small_buffer[j], "checking byte " + j);
        }
        assertEquals(_workbook_size - 2 * small_buffer.length, available(stream));

        // Reset, check it goes back to where it was
        stream.reset();
        assertEquals(_workbook_size - small_buffer.length, available(stream));

        // Read
        stream.read(small_buffer);
        for (int j = 0; j < small_buffer.length; j++) {
            assertEquals(_workbook_data[j + small_buffer.length], small_buffer[j], "checking byte " + j);
        }
        assertEquals(_workbook_size - 2 * small_buffer.length, available(stream));


        // Now read at various points
        Arrays.fill(small_buffer, (byte) 0);
        stream.read(small_buffer, 6, 8);
        stream.read(small_buffer, 100, 10);
        stream.read(small_buffer, 150, 12);
        int pos = small_buffer.length * 2;
        for (int j = 0; j < small_buffer.length; j++) {
            byte exp = 0;
            if (j >= 6 && j < 6 + 8) {
                exp = _workbook_data[pos];
                pos++;
            }
            if (j >= 100 && j < 100 + 10) {
                exp = _workbook_data[pos];
                pos++;
            }
            if (j >= 150 && j < 150 + 12) {
                exp = _workbook_data[pos];
                pos++;
            }

            assertEquals(exp, small_buffer[j], "checking byte " + j);
        }

        // Now repeat it with spanning multiple blocks
        stream = new DocumentInputStream(_workbook_n);
        // Read several blocks work
        buffer = new byte[_workbook_size / 5];
        stream.read(buffer);
        for (int j = 0; j < buffer.length; j++) {
            assertEquals(_workbook_data[j], buffer[j], "checking byte " + j);
        }
        assertEquals(_workbook_size - buffer.length, available(stream));

        // Read all of it again, check it began at the start again
        stream.reset();
        assertEquals(_workbook_size, available(stream));

        stream.read(buffer);
        for (int j = 0; j < buffer.length; j++) {
            assertEquals(_workbook_data[j], buffer[j], "checking byte " + j);
        }

        // Mark our position, and read another whole buffer
        stream.mark(12);
        stream.read(buffer);
        assertEquals(_workbook_size - (2 * buffer.length), available(stream));
        for (int j = buffer.length; j < (2 * buffer.length); j++) {
            assertEquals(_workbook_data[j], buffer[j - buffer.length], "checking byte " + j);
        }

        // Reset, should go back to only one buffer full read
        stream.reset();
        assertEquals(_workbook_size - buffer.length, available(stream));

        // Read the buffer again
        stream.read(buffer);
        assertEquals(_workbook_size - (2 * buffer.length), available(stream));
        for (int j = buffer.length; j < (2 * buffer.length); j++) {
            assertEquals(_workbook_data[j], buffer[j - buffer.length], "checking byte " + j);
        }
        assertTrue(stream.markSupported());
    }

    /**
     * test simple read method
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    void testReadSingleByte() throws IOException {
        DocumentInputStream stream = new DocumentInputStream(_workbook_n);
        int remaining = _workbook_size;

        // Try and read each byte in turn
        for (int j = 0; j < _workbook_size; j++) {
            int b = stream.read();
            assertTrue(b >= 0, "checking sign of " + j);
            assertEquals(_workbook_data[j], (byte) b, "validating byte " + j);
            remaining--;
            assertEquals(remaining, available(stream), "checking remaining after reading byte " + j);
        }

        // Ensure we fell off the end
        assertEquals(-1, stream.read());

        // Check that after close we can no longer read
        stream.close();
        assertThrows(IOException.class, stream::read);
    }

    /**
     * Test buffered read
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    void testBufferRead() throws IOException {
        DocumentInputStream stream = new DocumentInputStream(_workbook_n);
        // Need to give a byte array to read
        assertThrows(NullPointerException.class, () -> stream.read(null));

        // test reading zero length buffer
        assertEquals(0, stream.read(new byte[0]));
        assertEquals(_workbook_size, available(stream));
        byte[] buffer = new byte[_buffer_size];
        int offset = 0;

        while (available(stream) >= buffer.length) {
            assertEquals(_buffer_size, stream.read(buffer));
            for (byte element : buffer) {
                assertEquals(_workbook_data[offset], element, "in main loop, byte " + offset);
                offset++;
            }
            assertEquals(_workbook_size - offset, available(stream), "offset " + offset);
        }
        assertEquals(_workbook_size % _buffer_size, available(stream));
        Arrays.fill(buffer, (byte) 0);
        int count = stream.read(buffer);

        assertEquals(_workbook_size % _buffer_size, count);
        for (int j = 0; j < count; j++) {
            assertEquals(_workbook_data[offset], buffer[j], "past main loop, byte " + offset);
            offset++;
        }
        assertEquals(_workbook_size, offset);
        for (int j = count; j < buffer.length; j++) {
            assertEquals(0, buffer[j], "checking remainder, byte " + j);
        }
        assertEquals(-1, stream.read(buffer));
        stream.close();

        assertThrows(IOException.class, () -> stream.read(buffer));
    }

    /**
     * Test complex buffered read
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    void testComplexBufferRead() throws IOException {
        DocumentInputStream stream = new DocumentInputStream(_workbook_n);
        assertThrows(IllegalArgumentException.class, () -> stream.read(null, 0, 1));

        // test illegal offsets and lengths
        assertThrows(IndexOutOfBoundsException.class, () -> stream.read(new byte[5], -4, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> stream.read(new byte[5], 0, -4));
        assertThrows(IndexOutOfBoundsException.class, () -> stream.read(new byte[5], 0, 6));

        // test reading zero
        assertEquals(0, stream.read(new byte[5], 0, 0));
        assertEquals(_workbook_size, available(stream));
        byte[] buffer = new byte[_workbook_size];
        int offset = 0;

        while (available(stream) >= _buffer_size) {
            Arrays.fill(buffer, (byte) 0);
            assertEquals(_buffer_size, stream.read(buffer, offset, _buffer_size));
            for (int j = 0; j < offset; j++) {
                assertEquals(0, buffer[j], "checking byte " + j);
            }
            for (int j = offset; j < (offset + _buffer_size); j++) {
                assertEquals(_workbook_data[j], buffer[j], "checking byte " + j);
            }
            for (int j = offset + _buffer_size; j < buffer.length; j++) {
                assertEquals(0, buffer[j], "checking byte " + j);
            }
            offset += _buffer_size;
            assertEquals(_workbook_size - offset, available(stream), "offset " + offset);
        }
        assertEquals(_workbook_size % _buffer_size, available(stream));
        Arrays.fill(buffer, (byte) 0);
        int count = stream.read(buffer, offset, _workbook_size % _buffer_size);

        assertEquals(_workbook_size % _buffer_size, count);
        for (int j = 0; j < offset; j++) {
            assertEquals(0, buffer[j], "checking byte " + j);
        }
        for (int j = offset; j < buffer.length; j++) {
            assertEquals(_workbook_data[j], buffer[j], "checking byte " + j);
        }
        assertEquals(_workbook_size, offset + count);
        for (int j = count; j < offset; j++) {
            assertEquals(0, buffer[j], "byte " + j);
        }

        assertEquals(-1, stream.read(buffer, 0, 1));
        stream.close();

        assertThrows(IOException.class, () -> stream.read(buffer, 0, 1));
    }

    /**
     * Tests that we can skip within the stream
     */
    @Test
    void testSkip() throws IOException {
        DocumentInputStream stream = new DocumentInputStream(_workbook_n);
        assertEquals(_workbook_size, available(stream));
        int count = available(stream);

        while (available(stream) >= _buffer_size) {
            assertEquals(_buffer_size, stream.skip(_buffer_size));
            count -= _buffer_size;
            assertEquals(count, available(stream));
        }
        assertEquals(_workbook_size % _buffer_size,
                stream.skip(_buffer_size));
        assertEquals(0, available(stream));
        stream.reset();
        assertEquals(_workbook_size, available(stream));
        assertEquals(_workbook_size, stream.skip(_workbook_size * 2));
        assertEquals(0, available(stream));
        stream.reset();
        assertEquals(_workbook_size, available(stream));
        assertEquals(_workbook_size,
                stream.skip(2 + (long) Integer.MAX_VALUE));
        assertEquals(0, available(stream));
    }

    /**
     * Test that we can read files at multiple levels down the tree
     */
    @Test
    void testReadMultipleTreeLevels() throws Exception {
        final POIDataSamples _samples = POIDataSamples.getPublisherInstance();
        File sample = _samples.getFile("Sample.pub");

        DocumentInputStream stream;

        try (POIFSFileSystem poifs = new POIFSFileSystem(sample)) {
            // Ensure we have what we expect on the root
            assertEquals(poifs, poifs.getRoot().getFileSystem());

            // Check inside
            DirectoryNode root = poifs.getRoot();
            // Top Level
            Entry top = root.getEntry("Contents");
            assertTrue(top.isDocumentEntry());
            stream = root.createDocumentInputStream(top);
            assertNotEquals(-1, stream.read());

            // One Level Down
            DirectoryNode escher = (DirectoryNode) root.getEntry("Escher");
            Entry one = escher.getEntry("EscherStm");
            assertTrue(one.isDocumentEntry());
            stream = escher.createDocumentInputStream(one);
            assertNotEquals(-1, stream.read());

            // Two Levels Down
            DirectoryNode quill = (DirectoryNode) root.getEntry("Quill");
            DirectoryNode quillSub = (DirectoryNode) quill.getEntry("QuillSub");
            Entry two = quillSub.getEntry("CONTENTS");
            assertTrue(two.isDocumentEntry());
            stream = quillSub.createDocumentInputStream(two);
            assertNotEquals(-1, stream.read());
        }
    }

    @SuppressForbidden("just for testing")
    private static int available(InputStream is) throws IOException {
        return is.available();
    }
}
