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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Map;
import java.util.TimeZone;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hsmf.datatypes.AttachmentChunks;
import org.apache.poi.hsmf.datatypes.Chunk;
import org.apache.poi.hsmf.datatypes.ChunkBasedPropertyValue;
import org.apache.poi.hsmf.datatypes.MAPIProperty;
import org.apache.poi.hsmf.datatypes.MessagePropertiesChunk;
import org.apache.poi.hsmf.datatypes.NameIdChunks;
import org.apache.poi.hsmf.datatypes.PropertiesChunk;
import org.apache.poi.hsmf.datatypes.PropertyValue;
import org.apache.poi.hsmf.datatypes.RecipientChunks;
import org.apache.poi.hsmf.datatypes.Types;
import org.apache.poi.hsmf.datatypes.Types.MAPIType;
import org.apache.poi.hsmf.exceptions.ChunkNotFoundException;
import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.Entry;
import org.apache.poi.poifs.filesystem.EntryUtils;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.LocaleUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TestExtractEmbeddedMSG {
    private static MAPIMessage pdfMsgAttachments;

    /**
     * Initialize this test, load up the attachment_msg_pdf.msg mapi message.
     *
     * @throws Exception
     */
    @BeforeAll
    public static void setUp() throws IOException {
        POIDataSamples samples = POIDataSamples.getHSMFInstance();
        pdfMsgAttachments = new MAPIMessage(samples.openResourceAsStream("attachment_msg_pdf.msg"));
    }

    @AfterAll
    public static void tearDown() throws IOException {
        pdfMsgAttachments.close();
    }

    /**
     * Test to see if embedded message properties can be read, extracted, and
     * re-parsed
     *
     * @throws ChunkNotFoundException
     *
     */
    @Test
    void testEmbeddedMSGProperties() throws IOException, ChunkNotFoundException {
        AttachmentChunks[] attachments = pdfMsgAttachments.getAttachmentFiles();
        assertEquals(2, attachments.length);
        if (attachments.length == 2) {
            MAPIMessage attachedMsg = attachments[0].getEmbeddedMessage();
            assertNotNull(attachedMsg);
            // test properties of embedded message
            testFixedAndVariableLengthPropertiesOfAttachedMSG(attachedMsg);
            // rebuild top level message from embedded message
            try (POIFSFileSystem extractedAttachedMsg = rebuildFromAttached(attachedMsg)) {
                try (ByteArrayOutputStream extractedAttachedMsgOut = new ByteArrayOutputStream()) {
                    extractedAttachedMsg.writeFilesystem(extractedAttachedMsgOut);
                    byte[] extratedAttachedMsgRaw = extractedAttachedMsgOut.toByteArray();
                    MAPIMessage extractedMsgTopLevel = new MAPIMessage(
                            new ByteArrayInputStream(extratedAttachedMsgRaw));
                    // test properties of rebuilt embedded message
                    testFixedAndVariableLengthPropertiesOfAttachedMSG(extractedMsgTopLevel);
                }
            }
        }
    }

    private void testFixedAndVariableLengthPropertiesOfAttachedMSG(MAPIMessage msg) throws ChunkNotFoundException {
        // test fixed length property
        msg.setReturnNullOnMissingChunk(true);
        Calendar messageDate = msg.getMessageDate();
        assertNotNull(messageDate);
        Calendar expectedMessageDate = LocaleUtil.getLocaleCalendar();
        expectedMessageDate.set(2010, 05, 17, 23, 52, 19); // 2010/06/17 23:52:19 GMT
        expectedMessageDate.setTimeZone(TimeZone.getTimeZone("GMT"));
        expectedMessageDate.set(Calendar.MILLISECOND, 0);
        assertEquals(expectedMessageDate.getTimeInMillis(), messageDate.getTimeInMillis());
        // test variable length property
        assertEquals(msg.getSubject(), "Test Attachment");
    }

    private POIFSFileSystem rebuildFromAttached(MAPIMessage attachedMsg) throws IOException {
        // Create new MSG and copy properties.
        POIFSFileSystem newDoc = new POIFSFileSystem();
        MessagePropertiesChunk topLevelChunk = new MessagePropertiesChunk(null);
        // Copy attachments and recipients.
        int recipientscount = 0;
        int attachmentscount = 0;
        for (Entry entry : attachedMsg.getDirectory()) {
            if (entry.getName().startsWith(RecipientChunks.PREFIX)) {
                recipientscount++;
                DirectoryEntry newDir = newDoc.createDirectory(entry.getName());
                for (Entry e : ((DirectoryEntry) entry)) {
                    EntryUtils.copyNodeRecursively(e, newDir);
                }
            } else if (entry.getName().startsWith(AttachmentChunks.PREFIX)) {
                attachmentscount++;
                DirectoryEntry newDir = newDoc.createDirectory(entry.getName());
                for (Entry e : ((DirectoryEntry) entry)) {
                    EntryUtils.copyNodeRecursively(e, newDir);
                }
            }
        }
        // Copy properties from properties stream.
        MessagePropertiesChunk mpc = attachedMsg.getMainChunks().getMessageProperties();
        for (Map.Entry<MAPIProperty, PropertyValue> p : mpc.getRawProperties().entrySet()) {
            PropertyValue val = p.getValue();
            if (!(val instanceof ChunkBasedPropertyValue)) {
                MAPIType type = val.getActualType();
                if (type != null && type != Types.UNKNOWN) {
                    topLevelChunk.setProperty(val);
                }
            }
        }
        // Create nameid entries.
        DirectoryEntry nameid = newDoc.getRoot().createDirectory(NameIdChunks.NAME);
        // GUID stream
        nameid.createDocument(PropertiesChunk.DEFAULT_NAME_PREFIX + "00020102", new ByteArrayInputStream(new byte[0]));
        // Entry stream
        nameid.createDocument(PropertiesChunk.DEFAULT_NAME_PREFIX + "00030102", new ByteArrayInputStream(new byte[0]));
        // String stream
        nameid.createDocument(PropertiesChunk.DEFAULT_NAME_PREFIX + "00040102", new ByteArrayInputStream(new byte[0]));
        // Base properties.
        // Attachment/Recipient counter.
        topLevelChunk.setAttachmentCount(attachmentscount);
        topLevelChunk.setRecipientCount(recipientscount);
        topLevelChunk.setNextAttachmentId(attachmentscount);
        topLevelChunk.setNextRecipientId(recipientscount);
        // Unicode string format.
        byte[] storeSupportMaskData = new byte[4];
        PropertyValue.LongPropertyValue storeSupportPropertyValue = new PropertyValue.LongPropertyValue(MAPIProperty.STORE_SUPPORT_MASK,
                MessagePropertiesChunk.PROPERTIES_FLAG_READABLE | MessagePropertiesChunk.PROPERTIES_FLAG_WRITEABLE,
                storeSupportMaskData);
        storeSupportPropertyValue.setValue(0x00040000);
        topLevelChunk.setProperty(storeSupportPropertyValue);
        topLevelChunk.setProperty(new PropertyValue(MAPIProperty.HASATTACH,
                MessagePropertiesChunk.PROPERTIES_FLAG_READABLE | MessagePropertiesChunk.PROPERTIES_FLAG_WRITEABLE,
                attachmentscount == 0 ? new byte[] { 0 } : new byte[] { 1 }));
        // Copy properties from MSG file system.
        for (Chunk chunk : attachedMsg.getMainChunks().getChunks()) {
            if (!(chunk instanceof MessagePropertiesChunk)) {
                String entryName = chunk.getEntryName();
                String entryType = entryName.substring(entryName.length() - 4);
                int iType = Integer.parseInt(entryType, 16);
                MAPIType type = Types.getById(iType);
                if (type != null && type != Types.UNKNOWN) {
                    MAPIProperty mprop = MAPIProperty.createCustom(chunk.getChunkId(), type, chunk.getEntryName());
                    ByteArrayOutputStream data = new ByteArrayOutputStream();
                    chunk.writeValue(data);
                    PropertyValue pval = new PropertyValue(mprop, MessagePropertiesChunk.PROPERTIES_FLAG_READABLE
                            | MessagePropertiesChunk.PROPERTIES_FLAG_WRITEABLE, data.toByteArray(), type);
                    topLevelChunk.setProperty(pval);
                }
            }
        }
        topLevelChunk.writeProperties(newDoc.getRoot());
        return newDoc;
    }
}
