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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hsmf.datatypes.AttachmentChunks;
import org.apache.poi.hsmf.exceptions.ChunkNotFoundException;

/**
 * Tests to verify that we can read attachments from msg file
 */
public class TestFileWithAttachmentsRead {
    private static MAPIMessage twoSimpleAttachments;
    private static MAPIMessage pdfMsgAttachments;
    private static MAPIMessage inlineImgMsgAttachments;

    /**
     * Initialize this test, load up the attachment_test_msg.msg mapi message.
     * 
     * @throws Exception
     */
    @BeforeClass
    public static void setUp() throws IOException {
        POIDataSamples samples = POIDataSamples.getHSMFInstance();
        twoSimpleAttachments = new MAPIMessage(samples.openResourceAsStream("attachment_test_msg.msg"));
        pdfMsgAttachments = new MAPIMessage(samples.openResourceAsStream("attachment_msg_pdf.msg"));
        inlineImgMsgAttachments = new MAPIMessage(samples.openResourceAsStream("attachment_msg_inlineImg.msg"));
    }

    @AfterClass
    public static void tearDown() throws IOException {
        twoSimpleAttachments.close();
        pdfMsgAttachments.close();
        inlineImgMsgAttachments.close();
    }

    /**
     * Test to see if we can retrieve attachments.
     * 
     * @throws ChunkNotFoundException
     * 
     */
    @Test
    public void testRetrieveAttachments() {
        // Simple file
        AttachmentChunks[] attachments = twoSimpleAttachments.getAttachmentFiles();
        assertEquals(2, attachments.length);
        
        // Other file
        attachments = pdfMsgAttachments.getAttachmentFiles();
        assertEquals(2, attachments.length);
    }

    /**
     * Bug 60550: Test to see if we get the correct Content-IDs of inline images`.
     */
    @Test
    public void testReadContentIDField() throws IOException {
        AttachmentChunks[] attachments = inlineImgMsgAttachments.getAttachmentFiles();

        AttachmentChunks attachment;

        // Check in Content-ID field
        attachment = inlineImgMsgAttachments.getAttachmentFiles()[0];
        assertEquals("image001.png", attachment.getAttachFileName().getValue());
        assertEquals(".png", attachment.getAttachExtension().getValue());
        assertEquals("image001.png@01D0A524.96D40F30", attachment.getAttachContentId().getValue());

        attachment = inlineImgMsgAttachments.getAttachmentFiles()[1];
        assertEquals("image002.png", attachment.getAttachFileName().getValue());
        assertEquals(".png", attachment.getAttachExtension().getValue());
        assertEquals("image002.png@01D0A524.96D40F30", attachment.getAttachContentId().getValue());

        attachment = inlineImgMsgAttachments.getAttachmentFiles()[2];
        assertEquals("image003.png", attachment.getAttachFileName().getValue());
        assertEquals(".png", attachment.getAttachExtension().getValue());
        assertEquals("image003.png@01D0A526.B4C739C0", attachment.getAttachContentId().getValue());

        attachment = inlineImgMsgAttachments.getAttachmentFiles()[3];
        assertEquals("image006.jpg", attachment.getAttachFileName().getValue());
        assertEquals(".jpg", attachment.getAttachExtension().getValue());
        assertEquals("image006.jpg@01D0A526.B649E220", attachment.getAttachContentId().getValue());
    }


    /**
     * Test to see if attachments are not empty.
     */
    @Test
    public void testReadAttachments() throws IOException {
        AttachmentChunks[] attachments = twoSimpleAttachments.getAttachmentFiles();

        // Basic checks
        for (AttachmentChunks attachment : attachments) {
            assertTrue(attachment.getAttachFileName().getValue().length() > 0);
            assertTrue(attachment.getAttachLongFileName().getValue().length() > 0);
            assertTrue(attachment.getAttachExtension().getValue().length() > 0);
            if (attachment.getAttachMimeTag() != null) {
                assertTrue(attachment.getAttachMimeTag().getValue().length() > 0);
            }
        }

        AttachmentChunks attachment;

        // Now check in detail
        attachment = twoSimpleAttachments.getAttachmentFiles()[0];
        assertEquals("TEST-U~1.DOC", attachment.getAttachFileName().getValue());
        assertEquals("test-unicode.doc", attachment.getAttachLongFileName().getValue());
        assertEquals(".doc", attachment.getAttachExtension().getValue());
        assertNull(attachment.getAttachMimeTag());
        assertEquals(24064, attachment.getAttachData().getValue().length); // or compare the hashes of the attachment data

        attachment = twoSimpleAttachments.getAttachmentFiles()[1];
        assertEquals("pj1.txt", attachment.getAttachFileName().getValue());
        assertEquals("pj1.txt", attachment.getAttachLongFileName().getValue());
        assertEquals(".txt", attachment.getAttachExtension().getValue());
        assertNull(attachment.getAttachMimeTag());
        assertEquals(89, attachment.getAttachData().getValue().length); // or compare the hashes of the attachment data
    }
    
    /**
     * Test that we can handle both PDF and MSG attachments
     */
    @Test
    public void testReadMsgAttachments() throws Exception {
        AttachmentChunks[] attachments = pdfMsgAttachments.getAttachmentFiles();
        assertEquals(2, attachments.length);
         
        AttachmentChunks attachment;

        // Second is a PDF
        attachment = pdfMsgAttachments.getAttachmentFiles()[1];
        assertEquals("smbprn~1.pdf", attachment.getAttachFileName().getValue());
        assertEquals("smbprn.00009008.KdcPjl.pdf", attachment.getAttachLongFileName().getValue());
        assertEquals(".pdf", attachment.getAttachExtension().getValue());
        assertNull(attachment.getAttachMimeTag());
        assertNull(attachment.getAttachmentDirectory());
        assertEquals(13539, attachment.getAttachData().getValue().length); //or compare the hashes of the attachment data
        
        // First in a nested message
        attachment = pdfMsgAttachments.getAttachmentFiles()[0];
        assertEquals("Test Attachment", attachment.getAttachFileName().getValue());
        assertNull(attachment.getAttachLongFileName());
        assertNull(attachment.getAttachExtension());
        assertNull(attachment.getAttachMimeTag());
        assertNull(attachment.getAttachData());
        assertNotNull(attachment.getAttachmentDirectory());
        
        // Check we can see some bits of it
        MAPIMessage nested = attachment.getAttachmentDirectory().getAsEmbededMessage();
        assertEquals(1, nested.getRecipientNamesList().length);
        assertEquals("Nick Booth", nested.getRecipientNames());
        assertEquals("Test Attachment", nested.getConversationTopic());
    }
}
