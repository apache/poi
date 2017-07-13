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

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Class to test IOUtils
 */
public final class TestIOUtils {
    static File TMP = null;
    static long SEED = new Random().nextLong();
    static Random RANDOM = new Random(SEED);

    @BeforeClass
    public static void setUp() throws IOException {
        TMP = File.createTempFile("poi-ioutils-", "");
        OutputStream os = new FileOutputStream(TMP);
        for (int i = 0; i < RANDOM.nextInt(10000); i++) {
            os.write(RANDOM.nextInt((byte)127));
        }
        os.flush();
        os.close();

    }

    @AfterClass
    public static void tearDown() throws IOException {
        TMP.delete();
    }

    @Test
    public void testSkipFully() throws IOException {
        InputStream is =  new FileInputStream(TMP);
        long skipped = IOUtils.skipFully(is, 20000L);
        assertEquals("seed: "+SEED, -1L, skipped);
    }

    @Test
    public void testSkipFullyGtIntMax() throws IOException {
        InputStream is =  new FileInputStream(TMP);
        long skipped = IOUtils.skipFully(is, Integer.MAX_VALUE + 20000L);
        assertEquals("seed: "+SEED, -1L, skipped);
    }

    @Test
    public void testSkipFullyByteArray() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        InputStream is = new FileInputStream(TMP);
        IOUtils.copy(is, bos);
        long skipped = IOUtils.skipFully(new ByteArrayInputStream(bos.toByteArray()), 20000L);
        assertEquals("seed: "+SEED, -1L, skipped);
    }

    @Test
    public void testSkipFullyByteArrayGtIntMax() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        InputStream is = new FileInputStream(TMP);
        IOUtils.copy(is, bos);
        long skipped = IOUtils.skipFully(new ByteArrayInputStream(bos.toByteArray()), Integer.MAX_VALUE+ 20000L);
        assertEquals("seed: "+SEED, -1L, skipped);
    }

    @Test
    public void testWonkyInputStream() throws IOException {
        long skipped = IOUtils.skipFully(new WonkyInputStream(), 10000);
        assertEquals("seed: "+SEED, 10000, skipped);
    }

    /**
     * This returns 0 for the first call to skip and then reads
     * as requested.  This tests that the fallback to read() works.
     */
    private static class WonkyInputStream extends InputStream {
        int skipCalled = 0;
        int readCalled = 0;

        @Override
        public int read() throws IOException {
            readCalled++;
            return 0;
        }

        @Override
        public int read(byte[] arr, int offset, int len) throws IOException {
            readCalled++;
            return len;
        }

        @Override
        public long skip(long len) throws IOException {
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
