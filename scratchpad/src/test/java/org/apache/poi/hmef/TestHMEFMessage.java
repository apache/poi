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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hmef.attribute.MAPIAttribute;
import org.apache.poi.hmef.attribute.MAPIRtfAttribute;
import org.apache.poi.hmef.attribute.MAPIStringAttribute;
import org.apache.poi.hmef.attribute.TNEFAttribute;
import org.apache.poi.hmef.attribute.TNEFProperty;
import org.apache.poi.hsmf.datatypes.MAPIProperty;
import org.apache.poi.hsmf.datatypes.Types;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndian;
import org.junit.jupiter.api.Test;

public final class TestHMEFMessage {
    private static final POIDataSamples _samples = POIDataSamples.getHMEFInstance();

    @Test
    void testCounts() throws Exception {
        HMEFMessage msg = openSample("quick-winmail.dat");

        // Should have 4 attributes on the message
        assertEquals(4, msg.getMessageAttributes().size());

        // And should have 54 MAPI attributes on it
        assertEquals(54, msg.getMessageMAPIAttributes().size());


        // Should have 5 attachments
        assertEquals(5, msg.getAttachments().size());

        // Each attachment should have 6 normal attributes, and
        //  20 or so MAPI ones
        for (Attachment attach : msg.getAttachments()) {
            int attrCount = attach.getAttributes().size();
            int mapiAttrCount = attach.getMAPIAttributes().size();

            assertEquals(6, attrCount);
            assertTrue(mapiAttrCount >= 20, "Should be 20-25 mapi attributes, found " + mapiAttrCount);
            assertTrue(mapiAttrCount <= 25, "Should be 20-25 mapi attributes, found " + mapiAttrCount);
        }
    }

    @Test
    void testBasicMessageAttributes() throws Exception {
        HMEFMessage msg = openSample("quick-winmail.dat");

        // Should have version, codepage, class and MAPI
        assertEquals(4, msg.getMessageAttributes().size());
        assertNotNull(msg.getMessageAttribute(TNEFProperty.ID_TNEFVERSION));
        assertNotNull(msg.getMessageAttribute(TNEFProperty.ID_OEMCODEPAGE));
        assertNotNull(msg.getMessageAttribute(TNEFProperty.ID_MESSAGECLASS));
        assertNotNull(msg.getMessageAttribute(TNEFProperty.ID_MAPIPROPERTIES));

        // Check the order
        assertEquals(TNEFProperty.ID_TNEFVERSION, msg.getMessageAttributes().get(0).getProperty());
        assertEquals(TNEFProperty.ID_OEMCODEPAGE, msg.getMessageAttributes().get(1).getProperty());
        assertEquals(TNEFProperty.ID_MESSAGECLASS, msg.getMessageAttributes().get(2).getProperty());
        assertEquals(TNEFProperty.ID_MAPIPROPERTIES, msg.getMessageAttributes().get(3).getProperty());

        // Check some that aren't there
        assertNull(msg.getMessageAttribute(TNEFProperty.ID_AIDOWNER));
        assertNull(msg.getMessageAttribute(TNEFProperty.ID_ATTACHDATA));

        // Now check the details of one or two
        TNEFAttribute version = msg.getMessageAttribute(TNEFProperty.ID_TNEFVERSION);
        assertNotNull(version);
        assertEquals(0x010000, LittleEndian.getInt(version.getData()));

        TNEFAttribute msgCls = msg.getMessageAttribute(TNEFProperty.ID_MESSAGECLASS);
        assertNotNull(msgCls);
        assertEquals("IPM.Microsoft Mail.Note\0", new String(msgCls.getData(), StandardCharsets.US_ASCII));
    }

    @Test
    void testBasicMessageMAPIAttributes() throws Exception {
        HMEFMessage msg = openSample("quick-winmail.dat");

        assertEquals("This is a test message", msg.getSubject());
        assertEquals("{\\rtf1", msg.getBody().substring(0, 6));
    }

    /**
     * Checks that the compressed RTF message contents
     * can be correctly extracted
     */
    @Test
    void testMessageContents() throws Exception {
        HMEFMessage msg = openSample("quick-winmail.dat");

        // Firstly by byte
        MAPIRtfAttribute rtf = (MAPIRtfAttribute)
                msg.getMessageMAPIAttribute(MAPIProperty.RTF_COMPRESSED);
        assertNotNull(rtf);
        assertContents("message.rtf", rtf.getData());

        // Then by String
        String contents = msg.getBody();
        // It's all low bytes
        byte[] contentsBytes = contents.getBytes(StandardCharsets.US_ASCII);
        assertContents("message.rtf", contentsBytes);

        // try to get a message id that does not exist
        assertNull(msg.getMessageMAPIAttribute(MAPIProperty.AB_DEFAULT_DIR));
    }

    @Test
    void testMessageSample1() throws Exception {
        HMEFMessage msg = openSample("winmail-sample1.dat");

        // Firstly by byte
        MAPIRtfAttribute rtf = (MAPIRtfAttribute) msg
                .getMessageMAPIAttribute(MAPIProperty.RTF_COMPRESSED);
        // assertContents("message.rtf", rtf.getData());
        assertNotNull(rtf);

        // Then by String
        String contents = msg.getBody();
        //System.out.println(contents);
        // It's all low bytes
        byte[] contentsBytes = contents.getBytes(StandardCharsets.US_ASCII);
        // assertContents("message.rtf", contentsBytes);
        assertNotNull(contentsBytes);

        assertNotNull(msg.getSubject());
        assertNotNull(msg.getBody());
    }

    @Test
    void testInvalidMessage() {
        InputStream str = new ByteArrayInputStream(new byte[4]);
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> new HMEFMessage(str)
        );
        assertEquals("TNEF signature not detected in file, expected 574529400 but got 0", ex.getMessage());
    }

    @Test
    void testNoData() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // Header
        LittleEndian.putInt(HMEFMessage.HEADER_SIGNATURE, out);

        // field
        LittleEndian.putUShort(0, out);

        byte[] bytes = out.toByteArray();
        InputStream str = new ByteArrayInputStream(bytes);
        HMEFMessage msg = new HMEFMessage(str);
        assertNull(msg.getSubject());
        assertNull(msg.getBody());
    }

    @Test
    void testInvalidLevel() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // Header
        LittleEndian.putInt(HMEFMessage.HEADER_SIGNATURE, out);

        // field
        LittleEndian.putUShort(0, out);

        // invalid level
        LittleEndian.putUShort(90, out);

        InputStream str = new ByteArrayInputStream(out.toByteArray());
        IllegalStateException ex = assertThrows(
            IllegalStateException.class,
            () -> new HMEFMessage(str)
        );
        assertEquals("Unhandled level 90", ex.getMessage());
    }

    @Test
    void testCustomProperty() throws IOException {
        HMEFMessage msg = openSample("quick-winmail.dat");

        // Should have non-standard properties with IDs 0xE28 and 0xE29
        boolean hasE28 = false;
        boolean hasE29 = false;
        for (MAPIAttribute attr : msg.getMessageMAPIAttributes()) {
            if (attr.getProperty().id == 0xe28) hasE28 = true;
            if (attr.getProperty().id == 0xe29) hasE29 = true;
        }
        assertTrue(hasE28);
        assertTrue(hasE29);

        // Ensure we can fetch those as custom ones
        MAPIProperty propE28 = MAPIProperty.createCustom(0xe28, Types.ASCII_STRING, "Custom E28");
        MAPIProperty propE29 = MAPIProperty.createCustom(0xe29, Types.ASCII_STRING, "Custom E29");
        assertNotNull(msg.getMessageMAPIAttribute(propE28));
        assertNotNull(msg.getMessageMAPIAttribute(propE29));

        MAPIStringAttribute propE28b = (MAPIStringAttribute)msg.getMessageMAPIAttribute(propE28);
        assertNotNull(propE28b);
        assertEquals(MAPIStringAttribute.class, propE28b.getClass());
        assertEquals("Zimbra - Mark Rogers", propE28b.getDataString().substring(10));
    }

    static HMEFMessage openSample(String filename) throws IOException {
        try (InputStream is = _samples.openResourceAsStream(filename)) {
            return new HMEFMessage(is);
        }
    }

    static void assertContents(String filename, byte[] actual) throws IOException {
        try (InputStream stream = _samples.openResourceAsStream("quick-contents/" + filename)) {
            byte[] expected = IOUtils.toByteArray(stream);
            assertArrayEquals(expected, actual);
        }
    }

}

