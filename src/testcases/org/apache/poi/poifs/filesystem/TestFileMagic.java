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

import org.apache.commons.codec.Charsets;
import org.apache.poi.POIDataSamples;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.*;

public class TestFileMagic {
    @Test
    public void testFileMagic() {
        assertEquals(FileMagic.XML, FileMagic.valueOf("XML"));
        assertEquals(FileMagic.XML, FileMagic.valueOf("<?xml".getBytes(Charsets.UTF_8)));

        assertEquals(FileMagic.HTML, FileMagic.valueOf("HTML"));
        assertEquals(FileMagic.HTML, FileMagic.valueOf("<!DOCTYP".getBytes(Charsets.UTF_8)));
        assertEquals(FileMagic.HTML, FileMagic.valueOf("<!DOCTYPE".getBytes(Charsets.UTF_8)));
        assertEquals(FileMagic.HTML, FileMagic.valueOf("<html".getBytes(Charsets.UTF_8)));
        assertEquals(FileMagic.HTML, FileMagic.valueOf("\n\r<html".getBytes(Charsets.UTF_8)));
        assertEquals(FileMagic.HTML, FileMagic.valueOf("\n<html".getBytes(Charsets.UTF_8)));
        assertEquals(FileMagic.HTML, FileMagic.valueOf("\r\n<html".getBytes(Charsets.UTF_8)));
        assertEquals(FileMagic.HTML, FileMagic.valueOf("\r<html".getBytes(Charsets.UTF_8)));

        try {
            FileMagic.valueOf("some string");
            fail("Should catch exception here");
        } catch (IllegalArgumentException e) {
            // expected here
        }
    }

    @Test
    public void testFileMagicFile() throws IOException {
        assertEquals(FileMagic.OLE2, FileMagic.valueOf(POIDataSamples.getSpreadSheetInstance().getFile("SampleSS.xls")));
        assertEquals(FileMagic.OOXML, FileMagic.valueOf(POIDataSamples.getSpreadSheetInstance().getFile("SampleSS.xlsx")));
    }

    @Test
    public void testFileMagicStream() throws IOException {
        try (InputStream stream = new BufferedInputStream(new FileInputStream(POIDataSamples.getSpreadSheetInstance().getFile("SampleSS.xls")))) {
            assertEquals(FileMagic.OLE2, FileMagic.valueOf(stream));
        }
        try (InputStream stream = new BufferedInputStream(new FileInputStream(POIDataSamples.getSpreadSheetInstance().getFile("SampleSS.xlsx")))) {
            assertEquals(FileMagic.OOXML, FileMagic.valueOf(stream));
        }
    }

    @Test
    public void testPrepare() throws IOException {
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
}
