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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
import java.util.Random;

import org.apache.poi.EmptyFileException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Class to test IOUtils
 */
public final class TestIOUtils {

    static File TMP;
    static final long LENGTH = 300+new Random().nextInt(9000);

    @BeforeClass
    public static void setUp() throws IOException {
        TMP = File.createTempFile("poi-ioutils-", "");
        OutputStream os = new FileOutputStream(TMP);
        for (int i = 0; i < LENGTH; i++) {
            os.write(0x01);
        }
        os.flush();
        os.close();

    }

    @AfterClass
    public static void tearDown() {
        assertTrue(TMP.delete());
    }

    @Test
    public void testPeekFirst8Bytes() throws Exception {
        assertArrayEquals("01234567".getBytes("UTF-8"),
                IOUtils.peekFirst8Bytes(new ByteArrayInputStream("0123456789".getBytes("UTF-8"))));
    }

    @Test
    public void testPeekFirst8BytesWithPushbackInputStream() throws Exception {
        assertArrayEquals("01234567".getBytes("UTF-8"),
                IOUtils.peekFirst8Bytes(new PushbackInputStream(new ByteArrayInputStream("0123456789".getBytes("UTF-8")), 8)));
    }

    @Test
    public void testPeekFirst8BytesTooLessAvailable() throws Exception {
        assertArrayEquals(new byte[] { 1, 2, 3, 0, 0, 0, 0, 0},
                IOUtils.peekFirst8Bytes(new ByteArrayInputStream(new byte[] { 1, 2, 3})));
    }

    @Test(expected = EmptyFileException.class)
    public void testPeekFirst8BytesEmpty() throws Exception {
        IOUtils.peekFirst8Bytes(new ByteArrayInputStream(new byte[] {}));
    }

    @Test
    public void testToByteArray() throws Exception {
        assertArrayEquals(new byte[] { 1, 2, 3},
                IOUtils.toByteArray(new ByteArrayInputStream(new byte[] { 1, 2, 3})));
    }

    @Test(expected = IOException.class)
    public void testToByteArrayToSmall() throws Exception {
        assertArrayEquals(new byte[] { 1, 2, 3},
                IOUtils.toByteArray(new ByteArrayInputStream(new byte[] { 1, 2, 3}), 10));
    }

    @Test
    public void testToByteArrayByteBuffer() {
        assertArrayEquals(new byte[] { 1, 2, 3},
                IOUtils.toByteArray(ByteBuffer.wrap(new byte[]{1, 2, 3}), 10));
    }

    @Test
    public void testToByteArrayByteBufferToSmall() {
        assertArrayEquals(new byte[] { 1, 2, 3, 4, 5, 6, 7},
                IOUtils.toByteArray(ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 5, 6, 7}), 3));
    }

    @Test
    public void testSkipFully() throws IOException {
        try (InputStream is =  new FileInputStream(TMP)) {
            long skipped = IOUtils.skipFully(is, 20000L);
            assertEquals("length: " + LENGTH, LENGTH, skipped);
        }
    }

    @Test
    public void testSkipFullyGtIntMax() throws IOException {
        try (InputStream is =  new FileInputStream(TMP)) {
            long skipped = IOUtils.skipFully(is, Integer.MAX_VALUE + 20000L);
            assertEquals("length: " + LENGTH, LENGTH, skipped);
        }
    }

    @Test
    public void testSkipFullyByteArray() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (InputStream is = new FileInputStream(TMP)) {
            IOUtils.copy(is, bos);
            long skipped = IOUtils.skipFully(new ByteArrayInputStream(bos.toByteArray()), 20000L);
            assertEquals("length: " + LENGTH, LENGTH, skipped);
        }
    }

    @Test
    public void testSkipFullyByteArrayGtIntMax() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (InputStream is = new FileInputStream(TMP)) {
            IOUtils.copy(is, bos);
            long skipped = IOUtils.skipFully(new ByteArrayInputStream(bos.toByteArray()), Integer.MAX_VALUE + 20000L);
            assertEquals("length: " + LENGTH, LENGTH, skipped);
        }
    }

    @Test
    public void testSkipFullyBug61294() throws IOException {
        IOUtils.skipFully(new ByteArrayInputStream(new byte[0]), 1);
    }

    @Test
    public void testZeroByte() throws IOException {
        long skipped = IOUtils.skipFully((new ByteArrayInputStream(new byte[0])), 100);
        assertEquals("zero byte", -1L, skipped);
    }

    @Test
    public void testSkipZero() throws IOException {
        try (InputStream is =  new FileInputStream(TMP)) {
            long skipped = IOUtils.skipFully(is, 0);
            assertEquals("zero length", 0, skipped);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSkipNegative() throws IOException {
        try (InputStream is =  new FileInputStream(TMP)) {
            IOUtils.skipFully(is, -1);
        }
    }

    @Test(expected = RecordFormatException.class)
    public void testMaxLengthTooLong() throws IOException {
        try (InputStream is = new FileInputStream(TMP)) {
            IOUtils.toByteArray(is, Integer.MAX_VALUE, 100);
        }
    }

    @Test
    public void testMaxLengthIgnored() throws IOException {
        try (InputStream is = new FileInputStream(TMP)) {
            IOUtils.toByteArray(is, 90, Integer.MAX_VALUE);
            IOUtils.toByteArray(is, 90, 100);
            IOUtils.toByteArray(is, Integer.MAX_VALUE, Integer.MAX_VALUE);
        }
    }

    @Test(expected = RecordFormatException.class)
    public void testMaxLengthInvalid() throws IOException {
        try (InputStream is = new FileInputStream(TMP)) {
            IOUtils.toByteArray(is, 90, 80);
        }
    }

    @Test
    public void testWonkyInputStream() throws IOException {
        long skipped = IOUtils.skipFully(new WonkyInputStream(), 10000);
        assertEquals("length: "+LENGTH, 10000, skipped);
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
