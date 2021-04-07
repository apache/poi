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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hsmf.exceptions.ChunkNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


/**
 * Tests to verify that the library can read blank msg files.
 */
public final class TestBlankFileRead {
    private MAPIMessage mapiMessage;

    /**
     * Initialize this test, load up the blank.msg mapi message.
     */
    @BeforeEach
    void setup() throws IOException {
        POIDataSamples samples = POIDataSamples.getHSMFInstance();
        mapiMessage = new MAPIMessage(samples.openResourceAsStream("blank.msg"));
    }

    /**
     * Check if we can read the body of the blank message, we expect "".
     */
    @Test
    void testReadBody() {
        assertThrows(ChunkNotFoundException.class, mapiMessage::getTextBody);
    }


    /**
     * Test to see if we can read the CC Chunk.
     */
    @Test
    void testReadDisplayCC() throws ChunkNotFoundException {
        String obtained = mapiMessage.getDisplayCC();
        String expected = "";

        assertEquals(expected, obtained);
    }

    /**
     * Test to see if we can read the CC Chunk.
     */
    @Test
    void testReadDisplayTo() throws ChunkNotFoundException {
        String obtained = mapiMessage.getDisplayTo();
        String expected = "";

        assertEquals(expected, obtained);
    }

    /**
     * Test to see if we can read the FROM Chunk.
     */
    @Test
    void testReadDisplayFrom() {
        assertThrows(ChunkNotFoundException.class, mapiMessage::getDisplayFrom);
    }

    /**
     * Test to see if we can read the CC Chunk.
     */
    @Test
    void testReadDisplayBCC() throws ChunkNotFoundException {
        String obtained = mapiMessage.getDisplayBCC();
        String expected = "";

        assertEquals(expected, obtained);
    }


    /**
     * Check if we can read the subject line of the blank message, we expect ""
     */
    @Test
    void testReadSubject() throws Exception {
        String obtained = mapiMessage.getSubject();
        assertEquals("", obtained);
    }


    /**
     * Check if we can read the subject line of the blank message, we expect ""
     */
    @Test
    void testReadConversationTopic() {
        assertThrows(ChunkNotFoundException.class, mapiMessage::getConversationTopic);
    }
}
