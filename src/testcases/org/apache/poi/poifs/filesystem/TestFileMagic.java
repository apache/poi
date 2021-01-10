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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Random;

import org.apache.poi.POIDataSamples;
import org.apache.poi.util.TempFile;
import org.junit.jupiter.api.Test;

class TestFileMagic {
    @Test
    void testFileMagic() {
        assertEquals(FileMagic.XML, FileMagic.valueOf("XML"));
        assertEquals(FileMagic.XML, FileMagic.valueOf("<?xml".getBytes(StandardCharsets.UTF_8)));

        assertEquals(FileMagic.HTML, FileMagic.valueOf("HTML"));
        assertEquals(FileMagic.HTML, FileMagic.valueOf("<!DOCTYP".getBytes(StandardCharsets.UTF_8)));
        assertEquals(FileMagic.HTML, FileMagic.valueOf("<!DOCTYPE".getBytes(StandardCharsets.UTF_8)));
        assertEquals(FileMagic.HTML, FileMagic.valueOf("<html".getBytes(StandardCharsets.UTF_8)));
        assertEquals(FileMagic.HTML, FileMagic.valueOf("\n\r<html".getBytes(StandardCharsets.UTF_8)));
        assertEquals(FileMagic.HTML, FileMagic.valueOf("\n<html".getBytes(StandardCharsets.UTF_8)));
        assertEquals(FileMagic.HTML, FileMagic.valueOf("\r\n<html".getBytes(StandardCharsets.UTF_8)));
        assertEquals(FileMagic.HTML, FileMagic.valueOf("\r<html".getBytes(StandardCharsets.UTF_8)));

        assertEquals(FileMagic.JPEG, FileMagic.valueOf(new byte[]{ (byte)0xFF, (byte)0xD8, (byte)0xFF, (byte)0xDB }));
        assertEquals(FileMagic.JPEG, FileMagic.valueOf(new byte[]{ (byte)0xFF, (byte)0xD8, (byte)0xFF, (byte)0xE0, 'a', 'b', 'J', 'F', 'I', 'F', 0x00, 0x01 }));
        assertEquals(FileMagic.JPEG, FileMagic.valueOf(new byte[]{ (byte)0xFF, (byte)0xD8, (byte)0xFF, (byte)0xEE }));
        assertEquals(FileMagic.JPEG, FileMagic.valueOf(new byte[]{ (byte)0xFF, (byte)0xD8, (byte)0xFF, (byte)0xE1, 'd', 'c', 'E', 'x', 'i', 'f', 0x00, 0x00 }));

        assertEquals(FileMagic.UNKNOWN, FileMagic.valueOf("something".getBytes(StandardCharsets.UTF_8)));
        assertEquals(FileMagic.UNKNOWN, FileMagic.valueOf(new byte[0]));

        assertThrows(IllegalArgumentException.class, () -> FileMagic.valueOf("some string"));
    }

    @Test
    void testFileMagicFile() throws IOException {
        assertEquals(FileMagic.OLE2, FileMagic.valueOf(POIDataSamples.getSpreadSheetInstance().getFile("SampleSS.xls")));
        assertEquals(FileMagic.OOXML, FileMagic.valueOf(POIDataSamples.getSpreadSheetInstance().getFile("SampleSS.xlsx")));
    }

    @Test
    void testFileMagicStream() throws IOException {
        try (InputStream stream = new BufferedInputStream(new FileInputStream(POIDataSamples.getSpreadSheetInstance().getFile("SampleSS.xls")))) {
            assertEquals(FileMagic.OLE2, FileMagic.valueOf(stream));
        }
        try (InputStream stream = new BufferedInputStream(new FileInputStream(POIDataSamples.getSpreadSheetInstance().getFile("SampleSS.xlsx")))) {
            assertEquals(FileMagic.OOXML, FileMagic.valueOf(stream));
        }
    }

    @Test
    void testPrepare() throws IOException {
        try (InputStream stream = new BufferedInputStream(new FileInputStream(POIDataSamples.getSpreadSheetInstance().getFile("SampleSS.xlsx")))) {
            assertSame(stream, FileMagic.prepareToCheckMagic(stream));
        }

        try (InputStream stream = new InputStream() {
            @Override
            public int read() {
                return 0;
            }
        }) {
            assertNotSame(stream, FileMagic.prepareToCheckMagic(stream));
        }
    }

    @Test
    void testMatchingButTooLessData() {
        // this matches JPG, but is not long enough, previously this caused an Exception
        byte[] data = new byte[] { -1, -40, -1, -32, 0, 16, 74, 70 };

        assertEquals(FileMagic.UNKNOWN, FileMagic.valueOf(data));
    }

    @Test
    void testShortFile() throws IOException {
        // having a file shorter than 8 bytes previously caused an exception
        fetchMagicFromData(new byte[] { -1, -40, -1, -32, 0 });
        fetchMagicFromData(new byte[] { -1, -40, -1, -32 });
        fetchMagicFromData(new byte[] { -1, -40, -1 });
        fetchMagicFromData(new byte[] { -1, -40 });
        fetchMagicFromData(new byte[] { -1 });
        fetchMagicFromData(new byte[0]);
    }

    private void fetchMagicFromData(byte[] data) throws IOException {
        File file = TempFile.createTempFile("TestFileMagic", ".bin");
        try {
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(data);
            }

            assertEquals(FileMagic.UNKNOWN, FileMagic.valueOf(file));
        } finally {
            assertTrue(file.delete());
        }
    }

    @Test
    void testMarkRequired() throws IOException {
        byte[] data = new byte[] { -1, -40, -1, -32, 0 };

        File file = TempFile.createTempFile("TestFileMagic", ".bin");
        try {
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(data);
            }

            // a FileInputStream does not support "marking"
            try (FileInputStream str = new FileInputStream(file)) {
                assertFalse(str.markSupported());

                assertThrows(IOException.class, () -> FileMagic.valueOf(str));
            }
        } finally {
            assertTrue(file.delete());
        }
    }

    @Test
    void testPatterns() {
        // just try to trash the functionality with some byte-patterns
        for(int i = 0; i < 256;i++) {
            final byte[] data = new byte[12];
            for(int j = 0;j < 12; j++) {
                data[j] = (byte)i;

                assertEquals(FileMagic.UNKNOWN, FileMagic.valueOf(data));
            }
        }
    }

    @Test
    void testRandomPatterns() {
        Random random = new Random();

        // just try to trash the functionality with some byte-patterns
        for(int i = 0; i < 1000;i++) {
            final byte[] data = new byte[12];
            random.nextBytes(data);

            // we cannot check for UNKNOWN as we might hit valid byte-patterns here as well
            try {
                assertNotNull(FileMagic.valueOf(data));
            } catch (Exception e) {
                throw new IllegalStateException("Failed with pattern " + Arrays.toString(data), e);
            }
        }
    }
}
