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

package org.apache.poi.hmef.extractor;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.apache.poi.POIDataSamples;
import org.apache.poi.hmef.HMEFMessage;
import org.apache.poi.hmef.attribute.TNEFProperty;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.TempFile;
import org.apache.poi.util.StringUtil;
import org.junit.jupiter.api.Test;

public class TestHMEFContentsExtractor {
    @Test
    void testMain() throws IOException {
        POIDataSamples samples = POIDataSamples.getHMEFInstance();
        File message = samples.getFile("quick-winmail.dat");
        File outputDirectory = TempFile.createTempDirectory("quick-winmail-main");
        String[] args = new String[] { message.getAbsolutePath(), outputDirectory.getAbsolutePath() };
        HMEFContentsExtractor.main(args);

        String[] contents = new String[] {
                "message.rtf", // from extractMessageBody
                "quick.txt", "quick.pdf", "quick.xml", "quick.doc", "quick.html" // from extractAttachments
        };

        for (String filename : contents) {
            File f = new File(outputDirectory, filename);
            assertTrue(f.exists(), f + " does not exist");
            assertTrue(f.delete());
        }

        String[] list = outputDirectory.list();
        assertNotNull(list);
        assertEquals( 0, list.length, "Had: " + Arrays.toString(list));
        assertTrue(outputDirectory.delete());
    }

    @Test
    void testExtractMessageBody_OutputStream() throws IOException {
        POIDataSamples samples = POIDataSamples.getHMEFInstance();
        File winmailTNEFFile = samples.getFile("quick-winmail.dat");
        HMEFContentsExtractor extractor = new HMEFContentsExtractor(winmailTNEFFile);
        try (UnsynchronizedByteArrayOutputStream out = UnsynchronizedByteArrayOutputStream.builder().get()) {
            extractor.extractMessageBody(out);
            assertTrue(out.size() > 0);
            byte[] expectedMagic = new byte[]{'{', '\\', 'r', 't', 'f'};
            byte[] magic = Arrays.copyOf(out.toByteArray(), 5);
            assertArrayEquals(expectedMagic, magic, "RTF magic number");
        }
    }

    @Test
    void testExtractMessageBody_File() throws IOException {
        POIDataSamples samples = POIDataSamples.getHMEFInstance();
        File winmailTNEFFile = samples.getFile("quick-winmail.dat");
        HMEFContentsExtractor extractor = new HMEFContentsExtractor(winmailTNEFFile);
        File rtf = TempFile.createTempFile("quick-winmail-message-body", ".rtf");
        assertTrue(rtf.delete());
        extractor.extractMessageBody(rtf);
        assertTrue(rtf.length() > 0, "RTF message body is empty");
    }

    @Test
    void testExtractAttachmentsRejectsPathTraversal() throws IOException {
        File outputDirectory = TempFile.createTempDirectory("hmef-attachments");
        File escapedFile = new File(
                outputDirectory.getParentFile(), outputDirectory.getName() + "-escaped.txt");
        if (escapedFile.exists()) {
            assertTrue(escapedFile.delete());
        }
        assertFalse(escapedFile.exists());

        HMEFContentsExtractor extractor = new HMEFContentsExtractor(
                new HMEFMessage(new ByteArrayInputStream(createTnefWithAttachment(
                        ".." + File.separator + escapedFile.getName(), "contents"))));

        assertThrows(IOException.class, () -> extractor.extractAttachments(outputDirectory));
        assertFalse(escapedFile.exists());
        assertTrue(outputDirectory.delete());
    }

    private static byte[] createTnefWithAttachment(String filename, String contents) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        LittleEndian.putInt(HMEFMessage.HEADER_SIGNATURE, out);
        LittleEndian.putUShort(0, out);
        writeAttribute(out, TNEFProperty.ID_ATTACHRENDERDATA.id, TNEFProperty.TYPE_BYTE, new byte[0]);
        writeAttribute(out, TNEFProperty.ID_ATTACHTITLE.id, TNEFProperty.TYPE_STRING,
                (filename + "\0").getBytes(StringUtil.UTF8));
        writeAttribute(out, TNEFProperty.ID_ATTACHDATA.id, TNEFProperty.TYPE_BYTE, contents.getBytes(StringUtil.UTF8));
        return out.toByteArray();
    }

    private static void writeAttribute(ByteArrayOutputStream out, int id, int type, byte[] data) throws IOException {
        out.write(TNEFProperty.LEVEL_ATTACHMENT);
        LittleEndian.putUShort(id, out);
        LittleEndian.putUShort(type, out);
        LittleEndian.putInt(data.length, out);
        out.write(data, 0, data.length);
        LittleEndian.putUShort(0, out);
    }
}
