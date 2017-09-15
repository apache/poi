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

import java.io.IOException;

import junit.framework.TestCase;
import org.apache.poi.POIDataSamples;
import org.apache.poi.hsmf.exceptions.ChunkNotFoundException;
import org.junit.Test;

/**
 * Tests to verify that we can read a simple msg file, that is in plain/text
 *  format with no attachments or extra recipents.
 */
public final class TestSimpleFileRead extends TestCase {
    private final MAPIMessage mapiMessage;

    /**
     * Initialize this test, load up the blank.msg mapi message.
     * @throws Exception
     */
    public TestSimpleFileRead() throws IOException {
        POIDataSamples samples = POIDataSamples.getHSMFInstance();
        this.mapiMessage = new MAPIMessage(samples.openResourceAsStream("simple_test_msg.msg"));
    }

    /**
     * Test to see if we can read the CC Chunk.
     * @throws ChunkNotFoundException
     *
     */
    public void testReadDisplayCC() throws ChunkNotFoundException {
        String obtained = mapiMessage.getDisplayCC();
        String expected = "";

        TestCase.assertEquals(obtained, expected);
    }

    /**
     * Test to see if we can read the CC Chunk.
     * @throws ChunkNotFoundException
     *
     */
    public void testReadDisplayTo() throws ChunkNotFoundException {
        String obtained = mapiMessage.getDisplayTo();
        String expected = "travis@overwrittenstack.com";

        TestCase.assertEquals(obtained, expected);
    }

    /**
     * Test to see if we can read the From Chunk.
     * @throws ChunkNotFoundException
     *
     */
    public void testReadDisplayFrom() throws ChunkNotFoundException {
        String obtained = mapiMessage.getDisplayFrom();
        String expected = "Travis Ferguson";

        TestCase.assertEquals(obtained, expected);
    }

    /**
     * Test to see if we can read the CC Chunk.
     * @throws ChunkNotFoundException
     *
     */
    public void testReadDisplayBCC() throws ChunkNotFoundException {
        String obtained = mapiMessage.getDisplayBCC();
        String expected = "";

        TestCase.assertEquals(obtained, expected);
    }


    /**
     * Check if we can read the body of the blank message, we expect "".
     *
     * @throws Exception
     */
    public void testReadBody() throws Exception {
        String obtained = mapiMessage.getTextBody();
        String expected = "This is a test message.";

        TestCase.assertEquals(obtained, expected);
    }

    /**
     * Check if we can read the subject line of the blank message, we expect ""
     *
     * @throws Exception
     */
    public void testReadSubject() throws Exception {
        String obtained = mapiMessage.getSubject();
        String expected = "test message";

        TestCase.assertEquals(expected, obtained);
    }

    /**
     * Check if we can read the subject line of the blank message, we expect ""
     *
     * @throws Exception
     */
    public void testReadConversationTopic() throws Exception {
        String obtained = mapiMessage.getConversationTopic();
        TestCase.assertEquals("test message", obtained);
    }


    /**
     * Check if we can read the subject line of the blank message, we expect ""
     *
     * @throws Exception
     */
    public void testReadMessageClass() throws Exception {
        MAPIMessage.MESSAGE_CLASS obtained = mapiMessage.getMessageClassEnum();
        TestCase.assertEquals(MAPIMessage.MESSAGE_CLASS.NOTE, obtained);
    }

    /**
     * Check the various message classes
     *
     * @throws Exception
     */
    @Test
    public void testReadMessageClass2() throws Exception {
        TestCase.assertEquals(
                MAPIMessage.MESSAGE_CLASS.NOTE, mapiMessage.getMessageClassEnum());

        for (String messageClass : new String[]{
                "Appointment", "Contact", "Post", "StickyNote", "Task"
        }) {
            MAPIMessage.MESSAGE_CLASS mc = new MAPIMessage(
                    POIDataSamples.getHSMFInstance().getFile("msgClass"+messageClass+".msg")
            ).getMessageClassEnum();
            assertTrue( mc + " but expected " + messageClass,
                    messageClass.equalsIgnoreCase(mc.toString().replaceAll("_", "")));
        }
    }
}
