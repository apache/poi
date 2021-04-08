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

package org.apache.poi.hmef;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hmef.attribute.MAPIAttribute;
import org.apache.poi.hmef.attribute.MAPIRtfAttribute;
import org.apache.poi.hsmf.datatypes.MAPIProperty;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.StringUtil;
import org.junit.jupiter.api.Test;

public final class TestCompressedRTF {
    private static final POIDataSamples _samples = POIDataSamples.getHMEFInstance();

    private static final String block1 = "{\\rtf1\\adeflang102";
    private static final String block2 = block1 + "5\\ansi\\ansicpg1252";

    /**
     * Check that things are as we expected. If this fails,
     * then decoding has no hope...
     */
    @Test
    void testQuickBasics() throws Exception {
        HMEFMessage msg;
        try (InputStream is = _samples.openResourceAsStream("quick-winmail.dat")) {
            msg = new HMEFMessage(is);
        }

        MAPIAttribute rtfAttr = msg.getMessageMAPIAttribute(MAPIProperty.RTF_COMPRESSED);
        assertNotNull(rtfAttr);
        assertTrue(rtfAttr instanceof MAPIRtfAttribute);

        // Check the start of the compressed version
        byte[] data = ((MAPIRtfAttribute) rtfAttr).getRawData();
        assertEquals(5907, data.length);

        // First 16 bytes is header stuff
        // Check it has the length + compressed marker
        assertEquals(5907 - 4, LittleEndian.getShort(data));
        assertEquals(
                "LZFu",
                StringUtil.getFromCompressedUnicode(data, 8, 4)
        );


        // Now Look at the code
        byte[] exp = {
            (byte) 0x07, // Flag: cccUUUUU
            (byte) 0x00, //  c1a: offset 0 / 0x000
            (byte) 0x06, //  c1b: length 6+2  -> {\rtf1\a
            (byte) 0x01, //  c2a: offset 16 / 0x010
            (byte) 0x01, //  c2b: length 1+2  ->  def
            (byte) 0x0b, //  c3a: offset 182 / 0xb6
            (byte) 0x60, //  c3b: length 0+2  -> la
            (byte) 0x6e, // n
            (byte) 0x67, // g
            (byte) 0x31, // 1
            (byte) 0x30, // 0
            (byte) 0x32, // 2

            (byte) 0x66, // Flag:  UccUUccU
            (byte) 0x35, // 5
            (byte) 0x00, //  c2a: offset 6 / 0x006
            (byte) 0x64, //  c2b: length 4+2  -> \ansi\a
            (byte) 0x00, //  c3a: offset 7 / 0x007
            (byte) 0x72, //  c3b: length 2+2  -> nsi
            (byte) 0x63, // c
            (byte) 0x70, // p
            (byte) 0x0d, //  c6a: offset 221 / 0x0dd
            (byte) 0xd0, //  c6b: length 0+2  -> g1
            (byte) 0x0e, //  c7a: offset 224 / 0x0e0
            (byte) 0x00, //  c7b: length 0+2  -> 25
            (byte) 0x32, // 2
        };

        assertArrayEquals(exp, Arrays.copyOfRange(data, 16, 16+25));
    }

    /**
     * Check that we can decode the first 8 codes
     * (1 flag byte + 8 codes)
     */
    @Test
    void testFirstBlock() throws Exception {
        HMEFMessage msg;
        try (InputStream is = _samples.openResourceAsStream("quick-winmail.dat")) {
            msg = new HMEFMessage(is);
        }

        MAPIAttribute attr = msg.getMessageMAPIAttribute(MAPIProperty.RTF_COMPRESSED);
        assertNotNull(attr);
        MAPIRtfAttribute rtfAttr = (MAPIRtfAttribute) attr;

        // Truncate to header + flag + data for flag
        byte[] data = Arrays.copyOf(rtfAttr.getRawData(), 16 + 12);

        // Decompress it
        CompressedRTF comp = new CompressedRTF();
        byte[] decomp = comp.decompress(new ByteArrayInputStream(data));
        String decompStr = new String(decomp, StandardCharsets.US_ASCII);

        // Test
        assertEquals(block1.length(), decomp.length);
        assertEquals(block1, decompStr);
    }

    /**
     * Check that we can decode the first 16 codes
     * (flag + 8 codes, flag + 8 codes)
     */
    @Test
    void testFirstTwoBlocks() throws Exception {
        HMEFMessage msg;
        try (InputStream is = _samples.openResourceAsStream("quick-winmail.dat")) {
            msg = new HMEFMessage(is);
        }

        MAPIAttribute attr = msg.getMessageMAPIAttribute(MAPIProperty.RTF_COMPRESSED);
        assertNotNull(attr);
        MAPIRtfAttribute rtfAttr = (MAPIRtfAttribute) attr;

        // Truncate to header + flag + data for flag + flag + data
        byte[] data = Arrays.copyOf(rtfAttr.getRawData(), 16 + 12 + 13);

        // Decompress it
        CompressedRTF comp = new CompressedRTF();
        byte[] decomp = comp.decompress(new ByteArrayInputStream(data));
        String decompStr = new String(decomp, StandardCharsets.US_ASCII);

        // Test
        assertEquals(block2, decompStr);
    }

    /**
     * Check that we can correctly decode the whole file
     * TODO Fix what looks like a padding issue
     */
    @Test
    void testFull() throws Exception {
        final HMEFMessage msg;
        try (InputStream is = _samples.openResourceAsStream("quick-winmail.dat")) {
            msg = new HMEFMessage(is);
        }

       MAPIAttribute attr = msg.getMessageMAPIAttribute(MAPIProperty.RTF_COMPRESSED);
       assertNotNull(attr);
       MAPIRtfAttribute rtfAttr = (MAPIRtfAttribute)attr;

        final byte[] expected;
        try (InputStream stream = _samples.openResourceAsStream("quick-contents/message.rtf")) {
            expected = IOUtils.toByteArray(stream);
        }

        CompressedRTF comp = new CompressedRTF();
        byte[] data = rtfAttr.getRawData();
        byte[] decomp = comp.decompress(new ByteArrayInputStream(data));

        // Check the length was as expected
        assertEquals(data.length, comp.getCompressedSize() + 16);
        assertEquals(expected.length, comp.getDeCompressedSize());

        // Will have been padded though
        assertEquals(expected.length + 2, decomp.length);

        // By byte
        assertArrayEquals(expected, Arrays.copyOf(decomp, expected.length));

        // By String
        String expString = new String(expected, StandardCharsets.US_ASCII);
        String decompStr = rtfAttr.getDataString();
        assertEquals(expString, decompStr);
    }
}
