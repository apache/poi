
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


package org.apache.poi.poifs.nio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import org.apache.poi.POIDataSamples;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.TempFile;
import org.junit.jupiter.api.Test;

/**
 * Tests for the datasource implementations
 */
class TestDataSource {
    private static final POIDataSamples data = POIDataSamples.getPOIFSInstance();

    @Test
    void testFile() throws Exception {
        File f = data.getFile("Notes.ole2");

        try (FileBackedDataSource ds = new FileBackedDataSource(f)) {
            checkDataSource(ds, false);
        }

        // try a second time
        try (FileBackedDataSource ds = new FileBackedDataSource(f)) {
            checkDataSource(ds, false);
        }
    }

    @Test
    void testFileWritable() throws Exception {
        File temp = TempFile.createTempFile("TestDataSource", ".test");
        try {
            writeDataToFile(temp);

            try (FileBackedDataSource ds = new FileBackedDataSource(temp, false))  {
                checkDataSource(ds, true);
            }

            // try a second time
            try (FileBackedDataSource ds = new FileBackedDataSource(temp, false)) {
                checkDataSource(ds, true);
            }

            writeDataToFile(temp);
        } finally {
            assertTrue(temp.exists());
            assertTrue(temp.delete(), "Could not delete file " + temp);
        }
    }


    @Test
    void testRewritableFile() throws Exception {
        File temp = TempFile.createTempFile("TestDataSource", ".test");
        try {
            writeDataToFile(temp);

            ;
            try (FileBackedDataSource ds = new FileBackedDataSource(temp, true)) {
                ByteBuffer buf = ds.read(0, 10);
                assertNotNull(buf);
                buf = ds.read(8, 0x400);
                assertNotNull(buf);
            }

            // try a second time
            ;
            try (FileBackedDataSource ds = new FileBackedDataSource(temp, true)) {
                ByteBuffer buf = ds.read(0, 10);
                assertNotNull(buf);
                buf = ds.read(8, 0x400);
                assertNotNull(buf);
            }

            writeDataToFile(temp);
        } finally {
            assertTrue(temp.exists());
            assertTrue(temp.delete());
        }
    }

    private void writeDataToFile(File temp) throws IOException {
        try (OutputStream str = new FileOutputStream(temp)) {
            try (InputStream in = data.openResourceAsStream("Notes.ole2")) {
                IOUtils.copy(in, str);
            }
        }
    }

    private void checkDataSource(FileBackedDataSource ds, boolean writeable) throws IOException {
        assertEquals(writeable, ds.isWriteable());
        assertNotNull(ds.getChannel());

        // rewriting changes the size
        if (writeable) {
            assertTrue(ds.size() == 8192 || ds.size() == 8198, "Had: " + ds.size());
        } else {
            assertEquals(8192, ds.size());
        }

        // Start of file
        ByteBuffer bs;
        bs = ds.read(4, 0);
        assertEquals(4, bs.capacity());
        assertEquals(0, bs.position());
        assertEquals(0xd0 - 256, bs.get(0));
        assertEquals(0xcf - 256, bs.get(1));
        assertEquals(0x11, bs.get(2));
        assertEquals(0xe0 - 256, bs.get(3));
        assertEquals(0xd0 - 256, bs.get());
        assertEquals(0xcf - 256, bs.get());
        assertEquals(0x11, bs.get());
        assertEquals(0xe0 - 256, bs.get());

        // Mid way through
        bs = ds.read(8, 0x400);
        assertEquals(8, bs.capacity());
        assertEquals(0, bs.position());
        assertEquals((byte) 'R', bs.get(0));
        assertEquals(0, bs.get(1));
        assertEquals((byte) 'o', bs.get(2));
        assertEquals(0, bs.get(3));
        assertEquals((byte) 'o', bs.get(4));
        assertEquals(0, bs.get(5));
        assertEquals((byte) 't', bs.get(6));
        assertEquals(0, bs.get(7));

        // Can go to the end, but not past it
        bs = ds.read(8, 8190);
        // TODO How best to warn of a short read?
        assertEquals(0, bs.position());

        // Can't go off the end
        assertThrows(IndexOutOfBoundsException.class, () -> ds.read(4, ds.size()),
            "Shouldn't be able to read off the end of the file");
    }

    @Test
    void testByteArray() {
        byte[] data = new byte[256];
        byte b;
        for (int i = 0; i < data.length; i++) {
            b = (byte) i;
            data[i] = b;
        }

        ByteArrayBackedDataSource ds = new ByteArrayBackedDataSource(data);

        // Start
        ByteBuffer bs;
        bs = ds.read(4, 0);
        assertEquals(0, bs.position());
        assertEquals(0x00, bs.get());
        assertEquals(0x01, bs.get());
        assertEquals(0x02, bs.get());
        assertEquals(0x03, bs.get());

        // Middle
        bs = ds.read(4, 100);
        assertEquals(100, bs.position());
        assertEquals(100, bs.get());
        assertEquals(101, bs.get());
        assertEquals(102, bs.get());
        assertEquals(103, bs.get());

        // End
        bs = ds.read(4, 252);
        assertEquals(-4, bs.get());
        assertEquals(-3, bs.get());
        assertEquals(-2, bs.get());
        assertEquals(-1, bs.get());

        // Off the end
        bs = ds.read(4, 254);
        assertEquals(-2, bs.get());
        assertEquals(-1, bs.get());
        assertThrows(BufferUnderflowException.class, bs::get, "Shouldn't be able to read off the end");

        // Past the end
        assertThrows(IndexOutOfBoundsException.class, () -> ds.read(4, 256), "Shouldn't be able to read off the end");


        // Overwrite
        bs = ByteBuffer.allocate(4);
        bs.put(0, (byte) -55);
        bs.put(1, (byte) -54);
        bs.put(2, (byte) -53);
        bs.put(3, (byte) -52);

        assertEquals(256, ds.size());
        ds.write(bs, 40);
        assertEquals(256, ds.size());
        bs = ds.read(4, 40);

        assertEquals(-55, bs.get());
        assertEquals(-54, bs.get());
        assertEquals(-53, bs.get());
        assertEquals(-52, bs.get());

        // Append
        bs = ByteBuffer.allocate(4);
        bs.put(0, (byte) -55);
        bs.put(1, (byte) -54);
        bs.put(2, (byte) -53);
        bs.put(3, (byte) -52);

        assertEquals(256, ds.size());
        ds.write(bs, 256);
        assertEquals(260, ds.size());

        bs = ds.read(4, 256);
        assertEquals(256, bs.position());
        assertEquals(-55, bs.get());
        assertEquals(-54, bs.get());
        assertEquals(-53, bs.get());
        assertEquals(-52, bs.get());
    }
}
