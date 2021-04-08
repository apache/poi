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

package org.apache.poi.hssf.record;


import static org.apache.poi.hssf.record.TestcaseRecordInputStream.confirmRecordEncoding;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.poi.util.HexRead;
import org.junit.jupiter.api.Test;

/**
 * Tests the serialization and deserialization of the {@link FontRecord}
 * class works correctly.  Test data taken directly from a real Excel file.
 */
final class TestFontRecord {

    private static final int SID = 0x31;
    private static final byte[] data = {
            0xC8-256, 0,       // font height = xc8
            0, 0,             // attrs = 0
            0xFF-256, 0x7F,     // colour palette = x7fff
            0x90-256, 0x01,     // bold weight = x190
            0, 0,  // supersubscript
            0, 0,  // underline, family
            0, 0,  // charset, padding
            5, 0,  // name length, unicode flag
            0x41, 0x72, 0x69, 0x61, 0x6C, // Arial, as unicode

    };

    @Test
    void testLoad() {

        FontRecord record = new FontRecord(TestcaseRecordInputStream.create(0x31, data));
        assertEquals(0xc8, record.getFontHeight());
        assertEquals(0x00, record.getAttributes());
        assertFalse(record.isItalic());
        assertFalse(record.isStruckout());
        assertFalse(record.isMacoutlined());
        assertFalse(record.isMacshadowed());
        assertEquals(0x7fff, record.getColorPaletteIndex());
        assertEquals(0x190, record.getBoldWeight());
        assertEquals(0x00, record.getSuperSubScript());
        assertEquals(0x00, record.getUnderline());
        assertEquals(0x00, record.getFamily());
        assertEquals(0x00, record.getCharset());
        assertEquals("Arial", record.getFontName());

        assertEquals(21 + 4, record.getRecordSize());
    }

    @SuppressWarnings("squid:S2699")
    @Test
    void testStore() {
//      .fontheight      = c8
//      .attributes      = 0
//           .italic     = false
//           .strikout   = false
//           .macoutlined= false
//           .macshadowed= false
//      .colorpalette    = 7fff
//      .boldweight      = 190
//      .supersubscript  = 0
//      .underline       = 0
//      .family          = 0
//      .charset         = 0
//      .namelength      = 5
//      .fontname        = Arial

        FontRecord record = new FontRecord();
        record.setFontHeight((short)0xc8);
        record.setAttributes((short)0);
        record.setColorPaletteIndex((short)0x7fff);
        record.setBoldWeight((short)0x190);
        record.setSuperSubScript((short)0);
        record.setUnderline((byte)0);
        record.setFamily((byte)0);
        record.setCharset((byte)0);
        record.setFontName("Arial");

        byte [] recordBytes = record.serialize();
        confirmRecordEncoding(0x31, data, recordBytes);
    }

    @Test
    void testCloneOnto() {
        FontRecord base = new FontRecord(TestcaseRecordInputStream.create(0x31, data));

        FontRecord other = new FontRecord();
        other.cloneStyleFrom(base);

        byte [] recordBytes = other.serialize();
        assertEquals(recordBytes.length - 4, data.length);
        for (int i = 0; i < data.length; i++)
            assertEquals(data[i], recordBytes[i+4], "At offset " + i);
    }

    @Test
    void testSameProperties() {
        FontRecord f1 = new FontRecord(TestcaseRecordInputStream.create(0x31, data));
        FontRecord f2 = new FontRecord(TestcaseRecordInputStream.create(0x31, data));

        assertTrue(f1.sameProperties(f2));

        f2.setFontName("Arial2");
        assertFalse(f1.sameProperties(f2));
        f2.setFontName("Arial");
        assertTrue(f1.sameProperties(f2));

        f2.setFontHeight((short)11);
        assertFalse(f1.sameProperties(f2));
        f2.setFontHeight((short)0xc8);
        assertTrue(f1.sameProperties(f2));
    }

    /**
     * Bugzilla 47250 suggests that the unicode options byte should be present even when the name
     * length is zero.  The OOO documentation seems to agree with this and POI had no test data
     * samples to say otherwise.
     */
    @Test
    void testEmptyName_bug47250() {
        byte[] emptyNameData = HexRead.readFromString(
                "C8 00 00 00 FF 7F 90 01 00 00 00 00 00 00 "
                + "00" // zero length
                + "00" // unicode options byte
                );

        RecordInputStream in = TestcaseRecordInputStream.create(SID, emptyNameData);
        FontRecord fr = new FontRecord(in);
        assertNotEquals(1, in.available());
        assertEquals(0, in.available());

        assertEquals(0, fr.getFontName().length());
        byte[] recordBytes = fr.serialize();
        confirmRecordEncoding(SID, emptyNameData, recordBytes);
    }
}
