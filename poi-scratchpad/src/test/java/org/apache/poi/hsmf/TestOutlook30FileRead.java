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

package org.apache.poi.hsmf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hsmf.exceptions.ChunkNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests to verify that we can still work on the newer Outlook 3.0 files.
 */
public final class TestOutlook30FileRead {
    private MAPIMessage mapiMessage;

    /**
     * Initialize this test, load up the blank.msg mapi message.
     */
    @BeforeEach
    void setup() throws IOException {
        POIDataSamples samples = POIDataSamples.getHSMFInstance();
        this.mapiMessage = new MAPIMessage(samples.openResourceAsStream("outlook_30_msg.msg"));
    }

    /**
     * Test to see if we can read the CC Chunk.
     */
    @Test
    void testReadDisplayCC() throws ChunkNotFoundException {
        String obtained = mapiMessage.getDisplayCC();
        String expected = "";

        assertEquals(obtained, expected);
    }

    /**
     * Test to see if we can read the CC Chunk.
     */
    @Test
    void testReadDisplayTo() throws ChunkNotFoundException {
        String obtained = mapiMessage.getDisplayTo();

        assertTrue(obtained.startsWith("Bohn, Shawn"));
    }

    /**
     * Test to see if we can read the From Chunk.
     */
    @Test
    void testReadDisplayFrom() throws ChunkNotFoundException {
        String obtained = mapiMessage.getDisplayFrom();
        String expected = "Cramer, Nick";

        assertEquals(obtained, expected);
    }

    /**
     * Test to see if we can read the CC Chunk.
     */
    @Test
    void testReadDisplayBCC() throws ChunkNotFoundException {
        String obtained = mapiMessage.getDisplayBCC();
        String expected = "";

        assertEquals(obtained, expected);
    }


    /**
     * Check if we can read the body of the blank message, we expect "".
     */
    @Test
    void testReadBody() throws Exception {
        String obtained = mapiMessage.getTextBody();
        assertTrue(obtained.startsWith("I am shutting down"));
    }

    /**
     * Check if we can read the subject line of the blank message, we expect ""
     */
    @Test
    void testReadSubject() throws Exception {
        String obtained = mapiMessage.getSubject();
        String expected = "IN-SPIRE servers going down for a bit, back up around 8am";

        assertEquals(expected, obtained);
    }

    /**
     * Check if we can read the subject line of the blank message, we expect ""
     */
    @Test
    void testReadConversationTopic() throws Exception {
        String obtained = mapiMessage.getConversationTopic();
        assertEquals("IN-SPIRE servers going down for a bit, back up around 8am", obtained);
    }

    /**
     * Check if we can read the subject line of the blank message, we expect ""
     */
    @Test
    void testReadMessageClass() throws Exception {
        MAPIMessage.MESSAGE_CLASS obtained = mapiMessage.getMessageClassEnum();
        assertEquals(MAPIMessage.MESSAGE_CLASS.NOTE, obtained);
    }

    /**
     * Ensure we can get the HTML and RTF versions
     */
    @Test
    void testReadBodyContents() throws Exception {
        String html = mapiMessage.getHtmlBody();
        String rtf = mapiMessage.getRtfBody();
        assertNotNull(html);
        assertNotNull(rtf);

        assertTrue( html.startsWith("<!DOCTYPE"), "Wrong text:\n" + html );
        assertTrue( rtf.startsWith("{\\rtf1"), "Wrong text:\n" + rtf );
    }
}
