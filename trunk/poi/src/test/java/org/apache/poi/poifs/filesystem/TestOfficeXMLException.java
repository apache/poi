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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.junit.jupiter.api.Test;

/**
 * Class to test that POIFS complains when given an Office 2003 XML
 * of Office Open XML (OOXML, 2007+) document
 */
class TestOfficeXMLException {

    private static InputStream openSampleStream(String sampleFileName) {
        return HSSFTestDataSamples.openSampleFileStream(sampleFileName);
    }

    @Test
    void testOOXMLException() throws IOException {
        try (InputStream in = openSampleStream("sample.xlsx")) {
            OfficeXmlFileException ex = assertThrows(OfficeXmlFileException.class, () -> new POIFSFileSystem(in));
            assertTrue(ex.getMessage().contains("You are calling the part of POI that deals with OLE2 Office Documents"));
        }
    }

    @Test
    void test2003XMLException() throws IOException {
        try (InputStream in = openSampleStream("SampleSS.xml")) {
            NotOLE2FileException ex = assertThrows(NotOLE2FileException.class, () -> new POIFSFileSystem(in));
            assertTrue(ex.getMessage().contains("The supplied data appears to be a raw XML file"));
        }
    }

    @Test
    void testDetectAsPOIFS() throws IOException {
        // ooxml file isn't
        confirmIsPOIFS("SampleSS.xlsx", FileMagic.OOXML);

        // 2003 xml file isn't
        confirmIsPOIFS("SampleSS.xml", FileMagic.XML);

        // xls file is
        confirmIsPOIFS("SampleSS.xls", FileMagic.OLE2);

        // older biff formats aren't
        confirmIsPOIFS("testEXCEL_3.xls", FileMagic.BIFF3);
        confirmIsPOIFS("testEXCEL_4.xls", FileMagic.BIFF4);

        // newer excel formats are
        confirmIsPOIFS("testEXCEL_5.xls", FileMagic.OLE2);
        confirmIsPOIFS("testEXCEL_95.xls", FileMagic.OLE2);

        // text file isn't
        confirmIsPOIFS("SampleSS.txt", FileMagic.UNKNOWN);
    }

    private void confirmIsPOIFS(String sampleFileName, FileMagic expected) throws IOException {
        final File file = HSSFTestDataSamples.getSampleFile(sampleFileName);
        assertEquals(expected, FileMagic.valueOf(file));
    }

    @Test
    void testFileCorruption() throws Exception {

        // create test InputStream
        byte[] testData = {1, 2, 3};
        InputStream testInput = new ByteArrayInputStream(testData);

        // detect header
        InputStream in = FileMagic.prepareToCheckMagic(testInput);

        assertNotEquals(FileMagic.OLE2, FileMagic.valueOf(in));

        // check if InputStream is still intact
        byte[] test = new byte[3];
        assertEquals(3, in.read(test));
        assertArrayEquals(testData, test);
        assertEquals(-1, in.read());
    }


    @Test
    void testFileCorruptionOPOIFS() throws Exception {

        // create test InputStream
        byte[] testData = {(byte) 1, (byte) 2, (byte) 3};
        InputStream testInput = new ByteArrayInputStream(testData);

        // detect header
        InputStream in = FileMagic.prepareToCheckMagic(testInput);
        assertNotEquals(FileMagic.OLE2, FileMagic.valueOf(in));
        assertEquals(FileMagic.UNKNOWN, FileMagic.valueOf(in));

        // check if InputStream is still intact
        byte[] test = new byte[3];
        assertEquals(3, in.read(test));
        assertArrayEquals(testData, test);
        assertEquals(-1, in.read());
    }
}
