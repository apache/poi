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

package org.apache.poi.hsmf.parsers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Calendar;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hsmf.MAPIMessage;
import org.apache.poi.hsmf.datatypes.AttachmentChunks;
import org.apache.poi.hsmf.datatypes.ChunkGroup;
import org.apache.poi.hsmf.datatypes.Chunks;
import org.apache.poi.hsmf.datatypes.MAPIProperty;
import org.apache.poi.hsmf.datatypes.NameIdChunks;
import org.apache.poi.hsmf.datatypes.RecipientChunks;
import org.apache.poi.hsmf.datatypes.RecipientChunks.RecipientChunksSorter;
import org.apache.poi.hsmf.datatypes.StringChunk;
import org.apache.poi.hsmf.datatypes.Types;
import org.apache.poi.hsmf.exceptions.ChunkNotFoundException;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.LocaleUtil;
import org.junit.jupiter.api.Test;

/**
 * Tests to verify that the chunk parser works properly
 */
public final class TestPOIFSChunkParser {

    private final POIDataSamples samples = POIDataSamples.getHSMFInstance();

    @Test
    void testFindsCore() throws IOException, ChunkNotFoundException {
        try (POIFSFileSystem simple = new POIFSFileSystem(samples.getFile("quick.msg"), true)) {

      // Check a few core things are present
      simple.getRoot().getEntryCaseInsensitive(
            (new StringChunk(MAPIProperty.SUBJECT.id, Types.ASCII_STRING)).getEntryName()
      );
      simple.getRoot().getEntryCaseInsensitive(
            (new StringChunk(MAPIProperty.SENDER_NAME.id, Types.ASCII_STRING)).getEntryName()
      );

            // Now load the file
            try (MAPIMessage msg = new MAPIMessage(simple)) {
                assertEquals("Kevin Roast", msg.getDisplayTo());
                assertEquals("Kevin Roast", msg.getDisplayFrom());
                assertEquals("Test the content transformer", msg.getSubject());

                // Check date too
                Calendar calExp = LocaleUtil.getLocaleCalendar(2007, 5, 14, 9, 42, 55);
                Calendar calAct = msg.getMessageDate();
                assertEquals(calExp, calAct);
            }
        }
    }

    @Test
    void testFindsRecips() throws IOException, ChunkNotFoundException {
        try (POIFSFileSystem simple = new POIFSFileSystem(samples.getFile("quick.msg"), true)) {

      simple.getRoot().getEntryCaseInsensitive("__recip_version1.0_#00000000");

            ChunkGroup[] groups = POIFSChunkParser.parse(simple.getRoot());
            assertEquals(3, groups.length);
            assertTrue(groups[0] instanceof Chunks);
            assertTrue(groups[1] instanceof RecipientChunks);
            assertTrue(groups[2] instanceof NameIdChunks);

            RecipientChunks recips = (RecipientChunks) groups[1];
            assertEquals("kevin.roast@alfresco.org", recips.getRecipientSMTPChunk().getValue());
            assertEquals("/O=HOSTEDSERVICE2/OU=FIRST ADMINISTRATIVE GROUP/CN=RECIPIENTS/CN=Kevin.roast@ben",
                    recips.getRecipientEmailChunk().getValue());

            String search = new String(recips.getRecipientSearchChunk().getValue(), StandardCharsets.US_ASCII);
            assertEquals("CN=KEVIN.ROAST@BEN\0", search.substring(search.length() - 19));

            // Now via MAPIMessage
            try (MAPIMessage msg = new MAPIMessage(simple)) {
                assertNotNull(msg.getRecipientDetailsChunks());
                assertEquals(1, msg.getRecipientDetailsChunks().length);

                assertEquals("kevin.roast@alfresco.org", msg.getRecipientDetailsChunks()[0].getRecipientSMTPChunk().getValue());
                assertEquals("kevin.roast@alfresco.org", msg.getRecipientDetailsChunks()[0].getRecipientEmailAddress());
                assertEquals("Kevin Roast", msg.getRecipientDetailsChunks()[0].getRecipientName());
                assertEquals("kevin.roast@alfresco.org", msg.getRecipientEmailAddress());

                // Try both SMTP and EX files for recipient
                assertEquals("EX", msg.getRecipientDetailsChunks()[0].getDeliveryTypeChunk().getValue());
                assertEquals("kevin.roast@alfresco.org", msg.getRecipientDetailsChunks()[0].getRecipientSMTPChunk().getValue());
                assertEquals("/O=HOSTEDSERVICE2/OU=FIRST ADMINISTRATIVE GROUP/CN=RECIPIENTS/CN=Kevin.roast@ben",
                        msg.getRecipientDetailsChunks()[0].getRecipientEmailChunk().getValue());
            }
        }

            // Now look at another message
        try (POIFSFileSystem simple = new POIFSFileSystem(samples.getFile("simple_test_msg.msg"), true)) {
            try (MAPIMessage msg = new MAPIMessage(simple)) {
                assertNotNull(msg.getRecipientDetailsChunks());
                assertEquals(1, msg.getRecipientDetailsChunks().length);

                assertEquals("SMTP", msg.getRecipientDetailsChunks()[0].getDeliveryTypeChunk().getValue());
                assertNull(msg.getRecipientDetailsChunks()[0].getRecipientSMTPChunk());
                assertNull(msg.getRecipientDetailsChunks()[0].getRecipientNameChunk());
                assertEquals("travis@overwrittenstack.com",
                        msg.getRecipientDetailsChunks()[0].getRecipientEmailChunk().getValue());
                assertEquals("travis@overwrittenstack.com", msg.getRecipientEmailAddress());
            }
        }
    }

    @Test
    void testFindsMultipleRecipients() throws IOException, ChunkNotFoundException {
        try (POIFSFileSystem multiple = new POIFSFileSystem(samples.getFile("example_received_unicode.msg"), true)) {

      multiple.getRoot().getEntryCaseInsensitive("__recip_version1.0_#00000000");
      multiple.getRoot().getEntryCaseInsensitive("__recip_version1.0_#00000001");
      multiple.getRoot().getEntryCaseInsensitive("__recip_version1.0_#00000002");
      multiple.getRoot().getEntryCaseInsensitive("__recip_version1.0_#00000003");
      multiple.getRoot().getEntryCaseInsensitive("__recip_version1.0_#00000004");
      multiple.getRoot().getEntryCaseInsensitive("__recip_version1.0_#00000005");

            ChunkGroup[] groups = POIFSChunkParser.parse(multiple.getRoot());
            assertEquals(9, groups.length);
            assertTrue(groups[0] instanceof Chunks);
            assertTrue(groups[1] instanceof RecipientChunks);
            assertTrue(groups[2] instanceof RecipientChunks);
            assertTrue(groups[3] instanceof RecipientChunks);
            assertTrue(groups[4] instanceof RecipientChunks);
            assertTrue(groups[5] instanceof AttachmentChunks);
            assertTrue(groups[6] instanceof RecipientChunks);
            assertTrue(groups[7] instanceof RecipientChunks);
            assertTrue(groups[8] instanceof NameIdChunks);

            // In FS order initially
            RecipientChunks[] chunks = new RecipientChunks[] {
                    (RecipientChunks) groups[1],
                    (RecipientChunks) groups[2],
                    (RecipientChunks) groups[3],
                    (RecipientChunks) groups[4],
                    (RecipientChunks) groups[6],
                    (RecipientChunks) groups[7],
            };
            assertEquals(6, chunks.length);
            assertEquals(0, chunks[0].getRecipientNumber());
            assertEquals(2, chunks[1].getRecipientNumber());
            assertEquals(4, chunks[2].getRecipientNumber());
            assertEquals(5, chunks[3].getRecipientNumber());
            assertEquals(3, chunks[4].getRecipientNumber());
            assertEquals(1, chunks[5].getRecipientNumber());

            // Check
            assertEquals("'Ashutosh Dandavate'", chunks[0].getRecipientName());
            assertEquals("ashutosh.dandavate@alfresco.com", chunks[0].getRecipientEmailAddress());
            assertEquals("'Mike Farman'", chunks[1].getRecipientName());
            assertEquals("mikef@alfresco.com", chunks[1].getRecipientEmailAddress());
            assertEquals("nick.burch@alfresco.com", chunks[2].getRecipientName());
            assertEquals("nick.burch@alfresco.com", chunks[2].getRecipientEmailAddress());
            assertEquals("'Roy Wetherall'", chunks[3].getRecipientName());
            assertEquals("roy.wetherall@alfresco.com", chunks[3].getRecipientEmailAddress());
            assertEquals("nickb@alfresco.com", chunks[4].getRecipientName());
            assertEquals("nickb@alfresco.com", chunks[4].getRecipientEmailAddress());
            assertEquals("'Paul Holmes-Higgin'", chunks[5].getRecipientName());
            assertEquals("paul.hh@alfresco.com", chunks[5].getRecipientEmailAddress());

            // Now sort, and re-check
            Arrays.sort(chunks, new RecipientChunksSorter());

            assertEquals("'Ashutosh Dandavate'", chunks[0].getRecipientName());
            assertEquals("ashutosh.dandavate@alfresco.com", chunks[0].getRecipientEmailAddress());
            assertEquals("'Paul Holmes-Higgin'", chunks[1].getRecipientName());
            assertEquals("paul.hh@alfresco.com", chunks[1].getRecipientEmailAddress());
            assertEquals("'Mike Farman'", chunks[2].getRecipientName());
            assertEquals("mikef@alfresco.com", chunks[2].getRecipientEmailAddress());
            assertEquals("nickb@alfresco.com", chunks[3].getRecipientName());
            assertEquals("nickb@alfresco.com", chunks[3].getRecipientEmailAddress());
            assertEquals("nick.burch@alfresco.com", chunks[4].getRecipientName());
            assertEquals("nick.burch@alfresco.com", chunks[4].getRecipientEmailAddress());
            assertEquals("'Roy Wetherall'", chunks[5].getRecipientName());
            assertEquals("roy.wetherall@alfresco.com", chunks[5].getRecipientEmailAddress());

            // Finally check on message
            try (MAPIMessage msg = new MAPIMessage(multiple)) {
                assertEquals(6, msg.getRecipientEmailAddressList().length);
                assertEquals(6, msg.getRecipientNamesList().length);

                assertEquals("'Ashutosh Dandavate'", msg.getRecipientNamesList()[0]);
                assertEquals("'Paul Holmes-Higgin'", msg.getRecipientNamesList()[1]);
                assertEquals("'Mike Farman'", msg.getRecipientNamesList()[2]);
                assertEquals("nickb@alfresco.com", msg.getRecipientNamesList()[3]);
                assertEquals("nick.burch@alfresco.com", msg.getRecipientNamesList()[4]);
                assertEquals("'Roy Wetherall'", msg.getRecipientNamesList()[5]);

                assertEquals("ashutosh.dandavate@alfresco.com", msg.getRecipientEmailAddressList()[0]);
                assertEquals("paul.hh@alfresco.com", msg.getRecipientEmailAddressList()[1]);
                assertEquals("mikef@alfresco.com", msg.getRecipientEmailAddressList()[2]);
                assertEquals("nickb@alfresco.com", msg.getRecipientEmailAddressList()[3]);
                assertEquals("nick.burch@alfresco.com", msg.getRecipientEmailAddressList()[4]);
                assertEquals("roy.wetherall@alfresco.com", msg.getRecipientEmailAddressList()[5]);
            }
        }
    }

    @Test
    void testFindsNameId() throws IOException {
        try (POIFSFileSystem simple = new POIFSFileSystem(samples.getFile("quick.msg"), true)) {

      simple.getRoot().getEntryCaseInsensitive("__nameid_version1.0");

            ChunkGroup[] groups = POIFSChunkParser.parse(simple.getRoot());
            assertEquals(3, groups.length);
            assertTrue(groups[0] instanceof Chunks);
            assertTrue(groups[1] instanceof RecipientChunks);
            assertTrue(groups[2] instanceof NameIdChunks);

            NameIdChunks nameId = (NameIdChunks) groups[2];
            assertEquals(10, nameId.getAll().length);

            // Now via MAPIMessage
            try (MAPIMessage msg = new MAPIMessage(simple)) {
                assertNotNull(msg.getNameIdChunks());
                assertEquals(10, msg.getNameIdChunks().getAll().length);
            }
        }
    }

    @Test
    void testFindsAttachments() throws IOException, ChunkNotFoundException {
        try (POIFSFileSystem with = new POIFSFileSystem(samples.getFile("attachment_test_msg.msg"), true);
                POIFSFileSystem without = new POIFSFileSystem(samples.getFile("quick.msg"), true)) {
            AttachmentChunks attachment;


      // Check raw details on the one with
      with.getRoot().getEntryCaseInsensitive("__attach_version1.0_#00000000");
      with.getRoot().getEntryCaseInsensitive("__attach_version1.0_#00000001");
      POIFSChunkParser.parse(with.getRoot());

            ChunkGroup[] groups = POIFSChunkParser.parse(with.getRoot());
            assertEquals(5, groups.length);
            assertTrue(groups[0] instanceof Chunks);
            assertTrue(groups[1] instanceof RecipientChunks);
            assertTrue(groups[2] instanceof AttachmentChunks);
            assertTrue(groups[3] instanceof AttachmentChunks);
            assertTrue(groups[4] instanceof NameIdChunks);

            attachment = (AttachmentChunks) groups[2];
            assertEquals("TEST-U~1.DOC", attachment.getAttachFileName().toString());
            assertEquals("test-unicode.doc", attachment.getAttachLongFileName().toString());
            assertEquals(24064, attachment.getAttachData().getValue().length);

            attachment = (AttachmentChunks) groups[3];
            assertEquals("pj1.txt", attachment.getAttachFileName().toString());
            assertEquals("pj1.txt", attachment.getAttachLongFileName().toString());
            assertEquals(89, attachment.getAttachData().getValue().length);


      // Check raw details on one without
      assertFalse(without.getRoot().hasEntryCaseInsensitive("__attach_version1.0_#00000000"));
      assertFalse(without.getRoot().hasEntryCaseInsensitive("__attach_version1.0_#00000001"));

            // One with, from the top
            try (MAPIMessage msgWith = new MAPIMessage(with)) {
                assertEquals(2, msgWith.getAttachmentFiles().length);

                attachment = msgWith.getAttachmentFiles()[0];
                assertEquals("TEST-U~1.DOC", attachment.getAttachFileName().toString());
                assertEquals("test-unicode.doc", attachment.getAttachLongFileName().toString());
                assertEquals(24064, attachment.getAttachData().getValue().length);

                attachment = msgWith.getAttachmentFiles()[1];
                assertEquals("pj1.txt", attachment.getAttachFileName().toString());
                assertEquals("pj1.txt", attachment.getAttachLongFileName().toString());
                assertEquals(89, attachment.getAttachData().getValue().length);

                // Plus check core details are there
                assertEquals("'nicolas1.23456@free.fr'", msgWith.getDisplayTo());
                assertEquals("Nicolas1 23456", msgWith.getDisplayFrom());
                assertEquals("test pi\u00e8ce jointe 1", msgWith.getSubject());

                // One without, from the top
                try (MAPIMessage msgWithout = new MAPIMessage(without)) {

                    // No attachments
                    assertEquals(0, msgWithout.getAttachmentFiles().length);

                    // But has core details
                    assertEquals("Kevin Roast", msgWithout.getDisplayTo());
                    assertEquals("Kevin Roast", msgWithout.getDisplayFrom());
                    assertEquals("Test the content transformer", msgWithout.getSubject());
                }
            }

        }
    }

    /**
     * Bugzilla #51873 - Outlook 2002 files created with dragging and
     * dropping files to the disk include a non-standard named streams
     * such as "Olk10SideProps_0001"
     */
    @Test
    void testOlk10SideProps() throws IOException, ChunkNotFoundException {
        try (POIFSFileSystem poifs = new POIFSFileSystem(samples.getFile("51873.msg"), true)) {
            try (MAPIMessage msg = new MAPIMessage(poifs)) {
                // Check core details came through
                assertEquals("bubba@bubbasmith.com", msg.getDisplayTo());
                assertEquals("Test with Olk10SideProps_ Chunk", msg.getSubject());
            }
        }
    }

    @Test
    void testInvalidChunk() throws IOException {
        try (POIFSFileSystem poifs =
                new POIFSFileSystem(samples.getFile("clusterfuzz-testcase-minimized-POIHSMFFuzzer-4848576776503296.msg"), true)) {
            assertThrows(IllegalArgumentException.class,
                    () -> new MAPIMessage(poifs));
        }
    }
}
