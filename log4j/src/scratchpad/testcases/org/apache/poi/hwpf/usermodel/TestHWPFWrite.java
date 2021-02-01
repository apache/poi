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

package org.apache.poi.hwpf.usermodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.HWPFTestCase;
import org.apache.poi.hwpf.HWPFTestDataSamples;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.TempFile;
import org.junit.jupiter.api.Test;

/**
 * Test various write situations
 */
public final class TestHWPFWrite extends HWPFTestCase {
    private static final POIDataSamples SAMPLES = POIDataSamples.getDocumentInstance();

    /**
     * Write to a stream
     */
    @Test
    void testWriteStream() throws IOException {
        HWPFDocument doc = HWPFTestDataSamples.openSampleFile("SampleDoc.doc");

        Range r = doc.getRange();
        assertEquals("I am a test document\r", r.getParagraph(0).text());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        doc.write(baos);
        doc.close();
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

        doc = new HWPFDocument(bais);
        r = doc.getRange();
        assertEquals("I am a test document\r", r.getParagraph(0).text());
        doc.close();
    }

    /**
     * Write to a new file
     */
    @Test
    void testWriteNewFile() throws IOException {
        HWPFDocument doc = HWPFTestDataSamples.openSampleFile("SampleDoc.doc");

        Range r = doc.getRange();
        assertEquals("I am a test document\r", r.getParagraph(0).text());

        File file = TempFile.createTempFile("TestDocument", ".doc");
        doc.write(file);
        doc.close();

        // Check reading from File and Stream
        doc = new HWPFDocument(new FileInputStream(file));
        r = doc.getRange();
        assertEquals("I am a test document\r", r.getParagraph(0).text());
        doc.close();

        doc = new HWPFDocument(new POIFSFileSystem(file));
        r = doc.getRange();
        assertEquals("I am a test document\r", r.getParagraph(0).text());
        doc.close();
    }

    /**
     * Writing to the file we opened from - note, uses a temp file to avoid
     * changing our test files!
     */
    @Test
    void testInPlaceWrite() throws Exception {
        // Setup as a copy of a known-good file
        final File file = TempFile.createTempFile("TestDocument", ".doc");
        try (InputStream inputStream = SAMPLES.openResourceAsStream("SampleDoc.doc");
             FileOutputStream outputStream = new FileOutputStream(file)) {
            IOUtils.copy(inputStream, outputStream);
        }

        // Open from the temp file in read-write mode
        POIFSFileSystem poifs = new POIFSFileSystem(file, false);
        HWPFDocument doc = new HWPFDocument(poifs.getRoot());
        Range r = doc.getRange();
        assertEquals("I am a test document\r", r.getParagraph(0).text());

        // Change
        r.replaceText("X XX a test document\r", false);

        // Save in-place, close, re-open and check
        doc.write();
        doc.close();
        poifs.close();

        poifs = new POIFSFileSystem(file);
        doc = new HWPFDocument(poifs.getRoot());
        r = doc.getRange();
        assertEquals("X XX a test document\r", r.getParagraph(0).text());
        doc.close();
        poifs.close();
    }

    @Test
    void testInvalidInPlaceWriteInputStream() throws IOException {
        // Can't work for InputStream opened files

        try (InputStream is = SAMPLES.openResourceAsStream("SampleDoc.doc");
             HWPFDocument doc = new HWPFDocument(is)) {
            assertThrows(IllegalStateException.class, doc::write);
        }
    }

    @Test
    void testInvalidInPlaceWritePOIFS() throws Exception {
        // Can't work for Read-Only files
        try (POIFSFileSystem fs = new POIFSFileSystem(SAMPLES.getFile("SampleDoc.doc"), true);
            HWPFDocument doc = new HWPFDocument(fs.getRoot())) {
            assertThrows(IllegalStateException.class, doc::write);
        }
    }
}
