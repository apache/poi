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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.apache.poi.EmptyFileException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

@Isolated   // this test changes global static BYTE_ARRAY_MAX_OVERRIDE
final class TestIOUtils {
    private static File TMP;
    private static final long LENGTH = 300 + RandomSingleton.getInstance().nextInt(9000);

    @BeforeAll
    public static void setUp() throws IOException {
        TMP = File.createTempFile("poi-ioutils-", "");
        try (OutputStream os = new FileOutputStream(TMP)) {
            for (int i = 0; i < LENGTH; i++) {
                os.write(0x01);
            }
        }
    }

    @AfterAll
    public static void tearDown() {
        if (TMP != null) {
            assertTrue(TMP.delete());
        }
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
    void testToByteArrayTooSmall() {
        assertThrows(IOException.class, () -> IOUtils.toByteArray(data123(), 10));
    }

    @Test
    void testToByteArrayMaxLengthTooSmall() {
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
    void testToByteArrayMaxLength() throws IOException {
        final byte[] array = new byte[]{1, 2, 3, 4, 5, 6, 7};
        try (ByteArrayInputStream is = new ByteArrayInputStream(array)) {
            assertArrayEquals(array, IOUtils.toByteArrayWithMaxLength(is, 7));
        }
    }

    @Test
    void testToByteArrayMaxLengthWithByteArrayInitLenShort() throws IOException {
        final byte[] array = new byte[]{1, 2, 3, 4, 5, 6, 7};
        IOUtils.setMaxByteArrayInitSize(2);
        try (ByteArrayInputStream is = new ByteArrayInputStream(array)) {
            assertEquals(2, IOUtils.getMaxByteArrayInitSize());
            assertArrayEquals(array, IOUtils.toByteArrayWithMaxLength(is, 7));
        } finally {
            IOUtils.setMaxByteArrayInitSize(-1);
        }
    }

    @Test
    void testToByteArrayMaxLengthWithByteArrayInitLenLong() throws IOException {
        final byte[] array = new byte[]{1, 2, 3, 4, 5, 6, 7};
        IOUtils.setMaxByteArrayInitSize(8192);
        try (ByteArrayInputStream is = new ByteArrayInputStream(array)) {
            assertEquals(8192, IOUtils.getMaxByteArrayInitSize());
            assertArrayEquals(array, IOUtils.toByteArrayWithMaxLength(is, 7));
        } finally {
            IOUtils.setMaxByteArrayInitSize(-1);
        }
    }

    @Test
    void testToByteArrayMaxLengthLongerThanArray() throws IOException {
        final byte[] array = new byte[]{1, 2, 3, 4, 5, 6, 7};
        try (ByteArrayInputStream is = new ByteArrayInputStream(array)) {
            assertArrayEquals(array, IOUtils.toByteArrayWithMaxLength(is, 8));
        }
    }

    @Test
    void testToByteArrayMaxLengthShorterThanArray() throws IOException {
        final byte[] array = new byte[]{1, 2, 3, 4, 5, 6, 7};
        try (ByteArrayInputStream is = new ByteArrayInputStream(array)) {
            assertThrows(RecordFormatException.class, () -> IOUtils.toByteArrayWithMaxLength(is, 3));
        }
    }

    @Test
    void testToByteArrayMaxLengthShorterThanArrayWithByteArrayOverride() throws IOException {
        final byte[] array = new byte[]{1, 2, 3, 4, 5, 6, 7};
        IOUtils.setByteArrayMaxOverride(30 * 1024 * 1024);
        try (ByteArrayInputStream is = new ByteArrayInputStream(array)) {
            assertArrayEquals(array, IOUtils.toByteArrayWithMaxLength(is, 3));
        } finally {
            IOUtils.setByteArrayMaxOverride(-1);
        }
    }

    @Test
    void testCalculateByteArrayInitLength() throws IOException {
        assertEquals(4096, IOUtils.calculateByteArrayInitLength(false, 6000, 10000));
        assertEquals(3000, IOUtils.calculateByteArrayInitLength(false, 3000, 10000));
        assertEquals(3000, IOUtils.calculateByteArrayInitLength(false, 10000, 3000));
        assertEquals(10000, IOUtils.calculateByteArrayInitLength(true, 10000, 12000));
        assertEquals(10000, IOUtils.calculateByteArrayInitLength(true, 12000, 10000));
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
        UnsynchronizedByteArrayOutputStream bos = UnsynchronizedByteArrayOutputStream.builder().get();
        try (InputStream is = new FileInputStream(TMP)) {
            assertEquals(LENGTH, IOUtils.copy(is, bos));
            long skipped = IOUtils.skipFully(bos.toInputStream(), 20000L);
            assertEquals(LENGTH, skipped);
        }
    }

    @Test
    void testSkipFullyByteArrayGtIntMax() throws IOException {
        UnsynchronizedByteArrayOutputStream bos = UnsynchronizedByteArrayOutputStream.builder().get();
        try (InputStream is = new FileInputStream(TMP)) {
            assertEquals(LENGTH, IOUtils.copy(is, bos));
            long skipped = IOUtils.skipFully(bos.toInputStream(), Integer.MAX_VALUE + 20000L);
            assertEquals(LENGTH, skipped);
        }
    }

    @Test
    void testCopyToFile() throws IOException {
        File dest = TempFile.createTempFile("poi-ioutils-", "");
        try {
            try (InputStream is = new FileInputStream(TMP)) {
                assertEquals(LENGTH, IOUtils.copy(is, dest));
            }

            try (FileInputStream strOrig = new FileInputStream(TMP);
                FileInputStream strDest = new FileInputStream(dest)) {
                byte[] bytesOrig = new byte[(int)LENGTH];
                byte[] bytesDest = new byte[(int)LENGTH];
                IOUtils.readFully(strOrig, bytesOrig);
                IOUtils.readFully(strDest, bytesDest);
                assertArrayEquals(bytesOrig, bytesDest);
            }
        } finally {
            assertTrue(dest.delete());
        }
    }

    @Test
    void testCopyToInvalidFile() throws IOException {
        try (InputStream is = new FileInputStream(TMP)) {
            assertThrows(RuntimeException.class,
                    () -> {
                        // try with two different paths so we fail on both Unix and Windows
                        IOUtils.copy(is, new File("/notexisting/directory/structure"));
                        IOUtils.copy(is, new File("c:\\note&/()\"§=§%&!§$81§0_:;,.-'#*+~`?ß´ß0´ß9243xisting\\directory\\structure"));
                    });
        }
    }

    @Test
    void testSkipFullyBug61294() throws IOException {
        long skipped = IOUtils.skipFully(new ByteArrayInputStream(new byte[0]), 1);
        assertEquals(-1L, skipped);
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
            int len = IOUtils.toByteArray(is, 90, Integer.MAX_VALUE).length;
            assertEquals(90, len);
            len = IOUtils.toByteArray(is, 90, 100).length;
            assertEquals(90, len);
            len = IOUtils.toByteArray(is, Integer.MAX_VALUE, Integer.MAX_VALUE).length;
            assertTrue(len >= 300-2*90,
                    "Had: " + len + " when reading file " + TMP + " with size " + TMP.length());
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
    void testSetMaxOverrideOverLimitWithLength() {
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
        assertEquals(2, IOUtils.readFully(new ByteArrayInputStream(new byte[] {1, 2, 3}), bytes, 0, 2));
        assertArrayEquals(new byte[] {1,2}, bytes);
    }

    @Test
    void testReadFullyEOF() throws IOException {
        byte[] bytes = new byte[2];
        assertEquals(2, IOUtils.readFully(new NullInputStream(2), bytes, 0, 4));
        assertArrayEquals(new byte[] {0,0}, bytes);
    }

    @Test
    void testReadFullyEOFZero() throws IOException {
        byte[] bytes = new byte[2];
        assertEquals(-1, IOUtils.readFully(new NullInputStream(0), bytes, 0, 4));
        assertArrayEquals(new byte[] {0,0}, bytes);
    }

    @Test
    void testReadFullySimple() throws IOException {
        byte[] bytes = new byte[2];
        assertEquals(2, IOUtils.readFully(new ByteArrayInputStream(new byte[] {1, 2, 3}), bytes));
        assertArrayEquals(new byte[] {1,2}, bytes);
    }

    @Test
    void testReadFullyOffset() throws IOException {
        byte[] bytes = new byte[3];
        assertEquals(2, IOUtils.readFully(new ByteArrayInputStream(new byte[] {1, 2, 3}), bytes, 1, 2));
        assertArrayEquals(new byte[] {0, 1,2}, bytes);
    }

    @Test
    void testReadFullyAtLength() throws IOException {
        byte[] bytes = new byte[3];
        assertEquals(3, IOUtils.readFully(new ByteArrayInputStream(new byte[] {1, 2, 3}), bytes, 0, 3));
        assertArrayEquals(new byte[] {1,2, 3}, bytes);
    }


    @Test
    void testReadFullyChannel() throws IOException {
        ByteBuffer bytes = ByteBuffer.allocate(2);
        assertEquals(2, IOUtils.readFully(new SimpleByteChannel(new byte[]{1, 2, 3}), bytes));
        assertArrayEquals(new byte[] {1,2}, bytes.array());
        assertEquals(2, bytes.position());
    }

    @Test
    void testReadFullyChannelEOF() throws IOException {
        ByteBuffer bytes = ByteBuffer.allocate(2);
        assertEquals(-1, IOUtils.readFully(new EOFByteChannel(false), bytes));
        assertArrayEquals(new byte[] {0,0}, bytes.array());
        assertEquals(0, bytes.position());
    }

    @Test
    void testReadFullyChannelEOFException() {
        ByteBuffer bytes = ByteBuffer.allocate(2);
        assertThrows(IOException.class,
                () -> IOUtils.readFully(new EOFByteChannel(true), bytes));
    }

    @Test
    void testReadFullyChannelSimple() throws IOException {
        ByteBuffer bytes = ByteBuffer.allocate(2);
        assertEquals(2, IOUtils.readFully(new SimpleByteChannel(new byte[] {1, 2, 3}), bytes));
        assertArrayEquals(new byte[] {1,2}, bytes.array());
        assertEquals(2, bytes.position());
    }

    @Test
    public void testChecksum() {
        assertEquals(0L, IOUtils.calculateChecksum(new byte[0]));
        assertEquals(3057449933L, IOUtils.calculateChecksum(new byte[] { 1, 2, 3, 4}));
    }

    @Test
    public void testChecksumStream() throws IOException {
        assertEquals(0L, IOUtils.calculateChecksum(new NullInputStream(0)));
        assertEquals(0L, IOUtils.calculateChecksum(new NullInputStream(1)));
        assertEquals(3057449933L, IOUtils.calculateChecksum(new ByteArrayInputStream(new byte[] { 1, 2, 3, 4})));
        assertThrows(EOFException.class,
                () -> IOUtils.calculateChecksum(new NullInputStream(1, true)));
    }

    @Test
    void testSafelyCloneNull() {
        assertNull(IOUtils.safelyClone(null, 0, 0, 0));
    }

    @Test
    void testSafelyCloneInvalid() {
        assertThrows( RecordFormatException.class,
                () -> IOUtils.safelyClone(new byte[0], -1, 0, 0));
        assertThrows( RecordFormatException.class,
                () -> IOUtils.safelyClone(new byte[0], 0, -2, 0));
        assertThrows( RecordFormatException.class,
                () -> IOUtils.safelyClone(new byte[0], 0, 0, -3));
    }

    @Test
    void testSafelyCloneEmpty() {
        byte[] bytes = new byte[0];
        byte[] ret = IOUtils.safelyClone(bytes, 0, 0, 0);
        assertNotNull(ret);
        assertEquals(0, ret.length);
    }

    @Test
    void testSafelyCloneDataButLengthLimit() {
        byte[] bytes = new byte[] { 1, 2, 3, 4 };
        assertThrows( RecordFormatException.class,
                () -> IOUtils.safelyClone(bytes, 0, bytes.length, 0));
    }

    @Test
    void testSafelyCloneData() {
        byte[] bytes = new byte[] { 1, 2, 3, 4 };
        byte[] ret = IOUtils.safelyClone(bytes, 0, bytes.length, 100);
        assertNotNull(ret);
        assertEquals(4, ret.length);
    }

    @Test
    void testSafelyCloneDataHugeLength() {
        byte[] bytes = new byte[] { 1, 2, 3, 4 };
        byte[] ret = IOUtils.safelyClone(bytes, 0, Integer.MAX_VALUE, 100);
        assertNotNull(ret);
        assertEquals(4, ret.length);
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

    private static class EOFByteChannel implements ReadableByteChannel {
        private final boolean throwException;

        public EOFByteChannel(boolean throwException) {
            this.throwException = throwException;
        }

        @Override
        public int read(ByteBuffer dst) throws IOException {
            if (throwException) {
                throw new IOException("EOF");
            }

            return -1;
        }

        @Override
        public boolean isOpen() {
            return false;
        }

        @Override
        public void close() throws IOException {

        }
    }

    private static class SimpleByteChannel extends InputStream implements ReadableByteChannel {
        private final byte[] bytes;

        public SimpleByteChannel(byte[] bytes) {
            this.bytes = bytes;
        }

        @Override
        public int read() throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public int read(ByteBuffer dst) throws IOException {
            int toRead = Math.min(bytes.length, dst.capacity());
            dst.put(bytes, 0, toRead);
            return toRead;
        }

        @Override
        public boolean isOpen() {
            return false;
        }
    }

    public static class NullInputStream extends InputStream {
        private final int bytes;
        private final boolean exception;

        private int position;

        public NullInputStream(int bytes) {
            this(bytes, false);
        }

        public NullInputStream(int bytes, boolean exception) {
            this.bytes = bytes;
            this.exception = exception;
        }

        @Override
        public int read() throws IOException {
            if (position >= bytes) {
                return handleReturn();
            }

            position++;
            return 0;
        }

        private int handleReturn() throws EOFException {
            if (exception) {
                throw new EOFException();
            } else {
                return -1;
            }
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int toRead = Math.min(b.length, len);
            if (toRead > (bytes - position)) {
                return handleReturn();
            }
            toRead = Math.min(toRead, (bytes - position));

            position += toRead;
            return toRead;
        }
    }
}
