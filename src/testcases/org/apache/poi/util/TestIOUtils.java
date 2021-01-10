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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import org.apache.poi.EmptyFileException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Class to test IOUtils
 */
final class TestIOUtils {

    private static File TMP;
    private static final long LENGTH = 300+new Random().nextInt(9000);

    @BeforeAll
    public static void setUp() throws IOException {
        TMP = File.createTempFile("poi-ioutils-", "");
        OutputStream os = new FileOutputStream(TMP);
        for (int i = 0; i < LENGTH; i++) {
            os.write(0x01);
        }
        os.flush();
        os.close();

    }

    @AfterAll
    public static void tearDown() {
        if (TMP != null) assertTrue(TMP.delete());
    }

    private static InputStream data123() {
        return new ByteArrayInputStream(new byte[]{1,2,3});
    }

    @Test
    void testPeekFirst8Bytes() throws Exception {
        assertArrayEquals("01234567".getBytes(StandardCharsets.UTF_8),
                IOUtils.peekFirst8Bytes(new ByteArrayInputStream("0123456789".getBytes(StandardCharsets.UTF_8))));
    }

    @Test
    void testPeekFirst8BytesWithPushbackInputStream() throws Exception {
        assertArrayEquals("01234567".getBytes(StandardCharsets.UTF_8),
                IOUtils.peekFirst8Bytes(new PushbackInputStream(new ByteArrayInputStream("0123456789".getBytes(StandardCharsets.UTF_8)), 8)));
    }

    @Test
    void testPeekFirst8BytesTooLessAvailable() throws Exception {
        assertArrayEquals(new byte[] { 1, 2, 3, 0, 0, 0, 0, 0}, IOUtils.peekFirst8Bytes(data123()));
    }

    @Test
    void testPeekFirst8BytesEmpty() {
        assertThrows(EmptyFileException.class, () ->
            IOUtils.peekFirst8Bytes(new ByteArrayInputStream(new byte[0])));
    }

    @Test
    void testToByteArray() throws Exception {
        assertArrayEquals(new byte[] { 1, 2, 3}, IOUtils.toByteArray(data123()));
    }

    @Test
    void testToByteArrayToSmall() {
        assertThrows(IOException.class, () -> IOUtils.toByteArray(data123(), 10));
    }

    @Test
    void testToByteArrayMaxLengthToSmall() {
        assertThrows(IOException.class, () -> IOUtils.toByteArray(data123(), 10, 10));
    }

    @Test
    void testToByteArrayNegativeLength() {
        assertThrows(RecordFormatException.class, () -> IOUtils.toByteArray(data123(), -1));
    }

    @Test
    void testToByteArrayNegativeMaxLength() {
        assertThrows(RecordFormatException.class,  () -> IOUtils.toByteArray(data123(), 10, -1));
    }

    @Test
    void testToByteArrayByteBuffer() {
        assertArrayEquals(new byte[] { 1, 2, 3},
                IOUtils.toByteArray(ByteBuffer.wrap(new byte[]{1, 2, 3}), 10));
    }

    @Test
    void testToByteArrayByteBufferNonArray() {
        ByteBuffer buffer = ByteBuffer.allocate(3);
        buffer.put(new byte[] { 1, 2, 3});
        buffer.position(0);
        assertFalse(buffer.asReadOnlyBuffer().hasArray());
        assertEquals(3, buffer.asReadOnlyBuffer().remaining());

        assertArrayEquals(new byte[] { 1, 2, 3},
                IOUtils.toByteArray(buffer.asReadOnlyBuffer(), 3));
    }

    @Test
    void testToByteArrayByteBufferToSmall() {
        assertArrayEquals(new byte[] { 1, 2, 3, 4, 5, 6, 7},
                IOUtils.toByteArray(ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 5, 6, 7}), 3));
    }

    @Test
    void testSkipFully() throws IOException {
        try (InputStream is =  new FileInputStream(TMP)) {
            long skipped = IOUtils.skipFully(is, 20000L);
            assertEquals(LENGTH, skipped);
        }
    }

    @Test
    void testSkipFullyGtIntMax() throws IOException {
        try (InputStream is =  new FileInputStream(TMP)) {
            long skipped = IOUtils.skipFully(is, Integer.MAX_VALUE + 20000L);
            assertEquals(LENGTH, skipped);
        }
    }

    @Test
    void testSkipFullyByteArray() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (InputStream is = new FileInputStream(TMP)) {
            IOUtils.copy(is, bos);
            long skipped = IOUtils.skipFully(new ByteArrayInputStream(bos.toByteArray()), 20000L);
            assertEquals(LENGTH, skipped);
        }
    }

    @Test
    void testSkipFullyByteArrayGtIntMax() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (InputStream is = new FileInputStream(TMP)) {
            IOUtils.copy(is, bos);
            long skipped = IOUtils.skipFully(new ByteArrayInputStream(bos.toByteArray()), Integer.MAX_VALUE + 20000L);
            assertEquals(LENGTH, skipped);
        }
    }

    @Test
    void testSkipFullyBug61294() throws IOException {
        IOUtils.skipFully(new ByteArrayInputStream(new byte[0]), 1);
    }

    @Test
    void testZeroByte() throws IOException {
        long skipped = IOUtils.skipFully((new ByteArrayInputStream(new byte[0])), 100);
        assertEquals(-1L, skipped);
    }

    @Test
    void testSkipZero() throws IOException {
        try (InputStream is =  new FileInputStream(TMP)) {
            long skipped = IOUtils.skipFully(is, 0);
            assertEquals(0, skipped);
        }
    }

    @Test
    void testSkipNegative() throws IOException {
        try (InputStream is =  new FileInputStream(TMP)) {
            assertThrows(IllegalArgumentException.class, () -> IOUtils.skipFully(is, -1));
        }
    }

    @Test
    void testMaxLengthTooLong() throws IOException {
        try (InputStream is = new FileInputStream(TMP)) {
            assertThrows(RecordFormatException.class, () -> IOUtils.toByteArray(is, Integer.MAX_VALUE, 100));
        }
    }

    @Test
    void testMaxLengthIgnored() throws IOException {
        try (InputStream is = new FileInputStream(TMP)) {
            IOUtils.toByteArray(is, 90, Integer.MAX_VALUE);
            IOUtils.toByteArray(is, 90, 100);
            IOUtils.toByteArray(is, Integer.MAX_VALUE, Integer.MAX_VALUE);
        }
    }

    @Test
    void testMaxLengthInvalid() throws IOException {
        try (InputStream is = new FileInputStream(TMP)) {
            assertThrows(RecordFormatException.class, () -> IOUtils.toByteArray(is, 90, 80));
        }
    }

    @Test
    void testWonkyInputStream() throws IOException {
        long skipped = IOUtils.skipFully(new WonkyInputStream(), 10000);
        assertEquals(10000, skipped);
    }

    @Test
    void testSetMaxOverride() throws IOException {
        ByteArrayInputStream stream = new ByteArrayInputStream("abc".getBytes(StandardCharsets.UTF_8));
        byte[] bytes = IOUtils.toByteArray(stream);
        assertNotNull(bytes);
        assertEquals("abc", new String(bytes, StandardCharsets.UTF_8));
    }

    @Test
    void testSetMaxOverrideLimit() throws IOException {
        IOUtils.setByteArrayMaxOverride(30 * 1024 * 1024);
        try {
            ByteArrayInputStream stream = new ByteArrayInputStream("abc".getBytes(StandardCharsets.UTF_8));
            byte[] bytes = IOUtils.toByteArray(stream);
            assertNotNull(bytes);
            assertEquals("abc", new String(bytes, StandardCharsets.UTF_8));
        } finally {
            IOUtils.setByteArrayMaxOverride(-1);
        }
    }

    @Test
    void testSetMaxOverrideOverLimit() {
        IOUtils.setByteArrayMaxOverride(2);
        try {
            ByteArrayInputStream stream = new ByteArrayInputStream("abc".getBytes(StandardCharsets.UTF_8));
            assertThrows(RecordFormatException.class, () -> IOUtils.toByteArray(stream));
        } finally {
            IOUtils.setByteArrayMaxOverride(-1);
        }
    }

    @Test
    void testSetMaxOverrideWithLength() throws IOException {
        ByteArrayInputStream stream = new ByteArrayInputStream("abc".getBytes(StandardCharsets.UTF_8));
        byte[] bytes = IOUtils.toByteArray(stream, 3, 100);
        assertNotNull(bytes);
        assertEquals("abc", new String(bytes, StandardCharsets.UTF_8));
    }

    @Test
    void testSetMaxOverrideLimitWithLength() throws IOException {
        IOUtils.setByteArrayMaxOverride(30 * 1024 * 1024);
        try {
            ByteArrayInputStream stream = new ByteArrayInputStream("abc".getBytes(StandardCharsets.UTF_8));
            byte[] bytes = IOUtils.toByteArray(stream, 3, 100);
            assertNotNull(bytes);
            assertEquals("abc", new String(bytes, StandardCharsets.UTF_8));
        } finally {
            IOUtils.setByteArrayMaxOverride(-1);
        }
    }

    @Test
    void testSetMaxOverrideOverLimitWithLength() throws IOException {
        IOUtils.setByteArrayMaxOverride(2);
        try {
            ByteArrayInputStream stream = new ByteArrayInputStream("abc".getBytes(StandardCharsets.UTF_8));
            assertThrows(RecordFormatException.class, () -> IOUtils.toByteArray(stream, 3, 100));
        } finally {
            IOUtils.setByteArrayMaxOverride(-1);
        }
    }

    @Test
    void testSafelyAllocate() {
        byte[] bytes = IOUtils.safelyAllocate(30, 200);
        assertNotNull(bytes);
        assertEquals(30, bytes.length);
    }

    @Test
    void testSafelyAllocateLimit() {
        IOUtils.setByteArrayMaxOverride(40);
        try {
            byte[] bytes = IOUtils.safelyAllocate(30, 200);
            assertNotNull(bytes);
            assertEquals(30, bytes.length);
        } finally {
            IOUtils.setByteArrayMaxOverride(-1);
        }
    }

    @Test
    void testReadFully() throws IOException {
        byte[] bytes = new byte[2];
        IOUtils.readFully(new ByteArrayInputStream(new byte[] {1, 2, 3}), bytes, 0, 2);
        assertArrayEquals(new byte[] {1,2}, bytes);
    }

    @Test
    void testReadFullySimple() throws IOException {
        byte[] bytes = new byte[2];
        IOUtils.readFully(new ByteArrayInputStream(new byte[] {1, 2, 3}), bytes);
        assertArrayEquals(new byte[] {1,2}, bytes);
    }

    @Test
    void testReadFullyOffset() throws IOException {
        byte[] bytes = new byte[3];
        IOUtils.readFully(new ByteArrayInputStream(new byte[] {1, 2, 3}), bytes, 1, 2);
        assertArrayEquals(new byte[] {0, 1,2}, bytes);
    }

    @Test
    void testReadFullyAtLength() throws IOException {
        byte[] bytes = new byte[3];
        IOUtils.readFully(new ByteArrayInputStream(new byte[] {1, 2, 3}), bytes, 0, 3);
        assertArrayEquals(new byte[] {1,2, 3}, bytes);
    }

    /**
     * This returns 0 for the first call to skip and then reads
     * as requested.  This tests that the fallback to read() works.
     */
    private static class WonkyInputStream extends InputStream {
        int skipCalled;
        int readCalled;

        @Override
        public int read() {
            readCalled++;
            return 0;
        }

        @Override
        public int read(byte[] arr, int offset, int len) {
            readCalled++;
            return len;
        }

        @Override
        public long skip(long len) {
            skipCalled++;
            if (skipCalled == 1) {
                return 0;
            } else if (skipCalled > 100) {
                return len;
            } else {
                return 100;
            }
        }

        @Override
        public int available() {
            return 100000;
        }
    }
}
