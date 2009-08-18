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

package org.apache.poi.hslf.record;


import junit.framework.TestCase;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.Arrays;

import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.poifs.filesystem.DocumentEntry;

/**
 * Tests that {@link ExOleObjStg} works properly
 *
 * @author Yegor Kozlov
 */
public final class TestExOleObjStg extends TestCase {

    // From a real file (embedded SWF control)
    /*
    <ExOleObjStg info="16" type="4113" size="347" offset="4322" header="10 00 11 10 5B 01 00 00 ">
      00 0E 00 00 78 9C BB 70 5E F0 C1 C2 8D 52 0F 19 D0 80 1D 03 33 C3 BF FF 9C
      0C 6C 48 62 8C 40 CC 04 E3 08 30 30 B0 40 C5 FE FD FF FF 1F 24 C4 0C C4 FF
      47 C1 90 02 41 0C F9 40 58 C2 A0 C0 E0 CA 90 07 A4 8B 18 2A D1 93 02 5E 20
      C6 C0 0A 8F 73 50 5A C8 BB 5D 73 29 77 DD 79 C1 69 3B 5C 5C 83 43 50 D5 06
      BC 48 2F 2B 66 38 C9 C8 0E 64 3B 30 42 C4 9C 81 B6 83 EC 4D 05 93 C5 24 D9
      0D 02 42 0C 4C 8C C8 FE 21 56 9F 02 23 C9 56 E1 04 E4 D8 4F 4D 40 89 FD A0
      BC FB 17 4B BA F8 07 C5 A3 60 78 03 7A E6 FF 09 67 59 1B 41 F9 9F 95 61 34
      FF 53 13 50 62 3F 4C 1F AC 1C 18 CD F7 23 0B C0 DA 74 A0 B6 1B A8 3D 37 1A
      F7 23 0B A4 87 A6 85 0A 00 1B 64 6F 38 21 98 03 DA C2 E7 60 90 01 92 69 0C
      39 0C 65 0C 05 40 32 11 58 2F A4 02 6B 07 3D 60 19 5D 0E 14 27 4E 05 1F 90
      0C 67 C8 04 96 ED 29 C0 72 BE 1C C8 E3 06 E3 FF FF 39 18 B8 80 2C 0F A0 5C
      3A 43 06 58 2D A8 A7 E1 C3 10 02 97 87 B8 02 E6 1A 60 77 83 21 18 A8 12 64
      8A 23 D0 B6 1C B8 59 C8 AA 90 F5 F0 62 94 75 DC C0 DE 0A 37 5C 1D 33 54 35
      88 97 08 35 91 83 81 07 EC 27 10 BF 18 E8 9B E1 0F 00 BD 65 3D D4
    </ExOleObjStg>
     */
    private byte[] data = new byte[] {
            0x10, 0x00, 0x11, 0x10, 0x5B, 0x01, 0x00, 0x00, 0x00, 0x0E, 0x00, 0x00, 0x78, (byte)0x9C, (byte)0xBB, 0x70,
            0x5E, (byte)0xF0, (byte)0xC1, (byte)0xC2, (byte)0x8D, 0x52, 0x0F, 0x19, (byte)0xD0, (byte)0x80, 0x1D, 0x03,
            0x33, (byte)0xC3, (byte)0xBF, (byte)0xFF, (byte)0x9C, 0x0C, 0x6C, 0x48, 0x62, (byte)0x8C, 0x40, (byte)0xCC,
            0x04, (byte)0xE3, 0x08, 0x30, 0x30, (byte)0xB0, 0x40, (byte)0xC5, (byte)0xFE, (byte)0xFD, (byte)0xFF, (byte)0xFF,
            0x1F, 0x24, (byte)0xC4, (byte)0x0C, (byte)0xC4, (byte)0xFF, 0x47, (byte)0xC1, (byte)0x90, 0x02, 0x41, 0x0C,
            (byte)0xF9, 0x40, 0x58, (byte)0xC2, (byte)0xA0, (byte)0xC0, (byte)0xE0, (byte)0xCA, (byte)0x90, 0x07, (byte)0xA4,
            (byte)0x8B, 0x18, 0x2A, (byte)0xD1, (byte)0x93, 0x02, 0x5E, 0x20, (byte)0xC6, (byte)0xC0, 0x0A, (byte)0x8F,
            0x73, 0x50, 0x5A, (byte)0xC8, (byte)0xBB, 0x5D, 0x73, 0x29, 0x77, (byte)0xDD, 0x79, (byte)0xC1, 0x69, 0x3B,
            0x5C, 0x5C, (byte)0x83, 0x43, 0x50, (byte)0xD5, 0x06, (byte)0xBC, 0x48, 0x2F, 0x2B, 0x66, 0x38, (byte)0xC9,
            (byte)0xC8, 0x0E, 0x64, 0x3B, 0x30, 0x42, (byte)0xC4, (byte)0x9C, (byte)0x81, (byte)0xB6, (byte)0x83, (byte)0xEC,
            0x4D, 0x05, (byte)0x93, (byte)0xC5, 0x24, (byte)0xD9, 0x0D, 0x02, 0x42, 0x0C, 0x4C, (byte)0x8C, (byte)0xC8,
            (byte)0xFE, 0x21, 0x56, (byte)0x9F, 0x02, 0x23, (byte)0xC9, 0x56, (byte)0xE1, 0x04, (byte)0xE4, (byte)0xD8,
            0x4F, 0x4D, 0x40, (byte)0x89, (byte)0xFD, (byte)0xA0, (byte)0xBC, (byte)0xFB, 0x17, 0x4B, (byte)0xBA, (byte)0xF8,
            0x07, (byte)0xC5, (byte)0xA3, 0x60, 0x78, 0x03, 0x7A, (byte)0xE6, (byte)0xFF, 0x09, 0x67, 0x59, 0x1B, 0x41,
            (byte)0xF9, (byte)0x9F, (byte)0x95, 0x61, 0x34, (byte)0xFF, 0x53, 0x13, 0x50, 0x62, 0x3F, 0x4C, 0x1F, (byte)0xAC,
            0x1C, 0x18, (byte)0xCD, (byte)0xF7, 0x23, 0x0B, (byte)0xC0, (byte)0xDA, 0x74, (byte)0xA0, (byte)0xB6, 0x1B,
            (byte)0xA8, 0x3D, 0x37, 0x1A, (byte)0xF7, 0x23, 0x0B, (byte)0xA4, (byte)0x87, (byte)0xA6, (byte)0x85, 0x0A,
            0x00, 0x1B, 0x64, 0x6F, 0x38, 0x21, (byte)0x98, 0x03, (byte)0xDA, (byte)0xC2, (byte)0xE7, 0x60, (byte)0x90,
            0x01, (byte)0x92, 0x69, 0x0C, 0x39, 0x0C, 0x65, 0x0C, 0x05, 0x40, 0x32, 0x11, 0x58, 0x2F, (byte)0xA4, 0x02,
            0x6B, 0x07, 0x3D, 0x60, 0x19, 0x5D, 0x0E, 0x14, 0x27, 0x4E, 0x05, 0x1F, (byte)0x90, 0x0C, 0x67, (byte)0xC8,
            0x04, (byte)0x96, (byte)0xED, 0x29, (byte)0xC0, 0x72, (byte)0xBE, 0x1C, (byte)0xC8, (byte)0xE3, 0x06, (byte)0xE3,
            (byte)0xFF, (byte)0xFF, 0x39, 0x18, (byte)0xB8, (byte)0x80, 0x2C, 0x0F, (byte)0xA0, 0x5C, 0x3A, 0x43, 0x06, 0x58,
            0x2D, (byte)0xA8, (byte)0xA7, (byte)0xE1, (byte)0xC3, 0x10, 0x02, (byte)0x97, (byte)0x87, (byte)0xB8, 0x02,
            (byte)0xE6, 0x1A, 0x60, 0x77, (byte)0x83, 0x21, 0x18, (byte)0xA8, 0x12, 0x64, (byte)0x8A, 0x23, (byte)0xD0,
            (byte)0xB6, 0x1C, (byte)0xB8, 0x59, (byte)0xC8, (byte)0xAA, (byte)0x90, (byte)0xF5, (byte)0xF0, 0x62, (byte)0x94,
            0x75, (byte)0xDC, (byte)0xC0, (byte)0xDE, 0x0A, 0x37, 0x5C, 0x1D, 0x33, 0x54, 0x35, (byte)0x88, (byte)0x97, 0x08,
            0x35, (byte)0x91, (byte)0x83, (byte)0x81, 0x07, (byte)0xEC, 0x27, 0x10, (byte)0xBF, 0x18, (byte)0xE8, (byte)0x9B,
            (byte)0xE1, 0x0F, 0x00, (byte)0xBD, 0x65, 0x3D, (byte)0xD4
                };

    public void testRead() throws Exception {
        ExOleObjStg record = new ExOleObjStg(data, 0, data.length);
        assertEquals(RecordTypes.ExOleObjStg.typeID, record.getRecordType());

        int len = record.getDataLength();
        byte[] oledata = readAll(record.getData());
        assertEquals(len, oledata.length);

        POIFSFileSystem fs = new POIFSFileSystem(record.getData());
        assertTrue("Constructed POIFS from ExOleObjStg data", true);
        DocumentEntry doc = (DocumentEntry)fs.getRoot().getEntry("Contents");
        assertNotNull(doc);
        assertTrue("Fetched the Contents stream containing OLE properties", true);
    }

    public void testWrite() throws Exception {
        ExOleObjStg record = new ExOleObjStg(data, 0, data.length);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        record.writeOut(baos);
        byte[] b = baos.toByteArray();

        assertTrue(Arrays.equals(data, b));
    }

    public void testNewRecord() throws Exception {
        ExOleObjStg src = new ExOleObjStg(data, 0, data.length);
        byte[] oledata = readAll(src.getData());

        ExOleObjStg tgt = new ExOleObjStg();
        tgt.setData(oledata);


        assertEquals(src.getDataLength(), tgt.getDataLength());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        tgt.writeOut(out);
        byte[] b = out.toByteArray();

        assertEquals(data.length, b.length);
        assertTrue(Arrays.equals(data, b));
    }

    private byte[] readAll(InputStream is) throws IOException {
        int pos;
        byte[] chunk = new byte[1024];
        ByteArrayOutputStream out = new  ByteArrayOutputStream();
        while((pos = is.read(chunk)) > 0){
            out.write(chunk, 0, pos);
        }
        return out.toByteArray();

    }
}
