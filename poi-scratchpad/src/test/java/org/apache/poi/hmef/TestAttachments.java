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

import static org.apache.poi.hmef.TestHMEFMessage.openSample;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import org.apache.poi.util.LocaleUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public final class TestAttachments {
    private static HMEFMessage quick;

    @BeforeAll
    public static void setUp() throws IOException {
        quick = openSample("quick-winmail.dat");
    }

    /**
     * Check the file is as we expect
     */
    @Test
    void testCounts() {
        // Should have 5 attachments
        assertEquals(5, quick.getAttachments().size());
    }

    /**
     * Check some basic bits about the attachments
     */
    @Test
    void testBasicAttachments() {
        List<Attachment> attachments = quick.getAttachments();

        // Word first
        assertEquals("quick.doc", attachments.get(0).getFilename());
        assertEquals("quick.doc", attachments.get(0).getLongFilename());
        assertEquals(".doc", attachments.get(0).getExtension());

        // Then HTML
        assertEquals("QUICK~1.HTM", attachments.get(1).getFilename());
        assertEquals("quick.html", attachments.get(1).getLongFilename());
        assertEquals(".html", attachments.get(1).getExtension());

        // Then PDF
        assertEquals("quick.pdf", attachments.get(2).getFilename());
        assertEquals("quick.pdf", attachments.get(2).getLongFilename());
        assertEquals(".pdf", attachments.get(2).getExtension());

        // Then Text
        assertEquals("quick.txt", attachments.get(3).getFilename());
        assertEquals("quick.txt", attachments.get(3).getLongFilename());
        assertEquals(".txt", attachments.get(3).getExtension());

        // And finally XML
        assertEquals("quick.xml", attachments.get(4).getFilename());
        assertEquals("quick.xml", attachments.get(4).getLongFilename());
        assertEquals(".xml", attachments.get(4).getExtension());
    }

    /**
     * Query the attachments in detail, and check we see
     * the right values for key things
     */
    @Test
    void testAttachmentDetails() {
        List<Attachment> attachments = quick.getAttachments();
        assertEquals(5, attachments.size());

        // Pick a predictable date format + timezone
        DateFormat fmt = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss", Locale.UK);
        fmt.setTimeZone(LocaleUtil.TIMEZONE_UTC);

        for (Attachment attachment : attachments) {
            // They should all have the same date on them
            assertEquals("28-Apr-2010 12:40:56", fmt.format(attachment.getModifiedDate()));
            // They should all have a 3512 byte metafile rendered version
            byte[] meta = attachment.getRenderedMetaFile();
            assertNotNull(meta);
            assertEquals(3512, meta.length);
        }
    }

    /**
     * Ensure the attachment contents come back as they should do
     */
    @Test
    void testAttachmentContents() throws Exception {
        List<Attachment> attachments = quick.getAttachments();

        assertContents("quick.doc", attachments.get(0));
        assertContents("quick.html", attachments.get(1));
        assertContents("quick.pdf", attachments.get(2));
        assertContents("quick.txt", attachments.get(3));
        assertContents("quick.xml", attachments.get(4));
    }

    static void assertContents(String filename, Attachment attachment) throws IOException {
        assertEquals(filename, attachment.getLongFilename());
        TestHMEFMessage.assertContents(filename, attachment.getContents());
    }
}
