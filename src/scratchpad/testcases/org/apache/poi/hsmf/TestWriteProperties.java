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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import junit.framework.TestCase;

import org.apache.poi.hsmf.datatypes.MAPIProperty;
import org.apache.poi.hsmf.datatypes.MessagePropertiesChunk;
import org.apache.poi.hsmf.datatypes.NameIdChunks;
import org.apache.poi.hsmf.datatypes.PropertiesChunk;
import org.apache.poi.hsmf.datatypes.PropertyValue;
import org.apache.poi.hsmf.datatypes.Types;
import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.CodePageUtil;

/**
 * Tests to verify writing properties basically works.
 */
public final class TestWriteProperties extends TestCase {

   /**
    * No initialization required.
    * @throws Exception
    */
   public TestWriteProperties() throws IOException {
   }

   /**
    * Test writing properties in ASCII based MSG.
    */
   public void testWritePropertiesASCII() throws Exception {
       // Create file system and write properties
       POIFSFileSystem fs = new POIFSFileSystem();
       MessagePropertiesChunk topLevelChunk = new MessagePropertiesChunk(null, false);

       // Create nameid entries.
       DirectoryEntry nameid = fs.getRoot().createDirectory(NameIdChunks.NAME);
       // GUID stream
       nameid.createDocument(PropertiesChunk.DEFAULT_NAME_PREFIX + "00020102", new ByteArrayInputStream(new byte[0]));
       // Entry stream
       nameid.createDocument(PropertiesChunk.DEFAULT_NAME_PREFIX + "00030102", new ByteArrayInputStream(new byte[0]));
       // String stream
       nameid.createDocument(PropertiesChunk.DEFAULT_NAME_PREFIX + "00040102", new ByteArrayInputStream(new byte[0]));
       // Base properties.

       // Attachment/Recipient counter.
       topLevelChunk.setAttachmentCount(0);
       topLevelChunk.setRecipientCount(0);
       topLevelChunk.setNextAttachmentId(0);
       topLevelChunk.setNextRecipientId(0);
       
       // ASCII string format, HTML enabled.
       byte[] storeSupportMaskData = new byte[4];
       PropertyValue.LongPropertyValue storeSupportPropertyValue = new PropertyValue.LongPropertyValue(MAPIProperty.STORE_SUPPORT_MASK,
               MessagePropertiesChunk.PROPERTIES_FLAG_READABLE | MessagePropertiesChunk.PROPERTIES_FLAG_WRITEABLE,
               storeSupportMaskData);
       storeSupportPropertyValue.setValue(0x00030000);
       topLevelChunk.setProperty(storeSupportPropertyValue);
       
       // Simple properties. Strings as ASCII 7 bit.
       topLevelChunk.setProperty(new PropertyValue(MAPIProperty.MESSAGE_CLASS, MessagePropertiesChunk.PROPERTIES_FLAG_READABLE | MessagePropertiesChunk.PROPERTIES_FLAG_WRITEABLE, "IPM.Note".getBytes("CP1252"))); //outlook message
       topLevelChunk.setProperty(new PropertyValue(MAPIProperty.HASATTACH, MessagePropertiesChunk.PROPERTIES_FLAG_READABLE | MessagePropertiesChunk.PROPERTIES_FLAG_WRITEABLE, new byte[]{1}));
       topLevelChunk.setProperty(new PropertyValue(MAPIProperty.MESSAGE_FLAGS,  MessagePropertiesChunk.PROPERTIES_FLAG_READABLE | MessagePropertiesChunk.PROPERTIES_FLAG_WRITEABLE, ByteBuffer.allocate(4).putInt(8).array()));
       topLevelChunk.setProperty(new PropertyValue(MAPIProperty.SUBJECT, MessagePropertiesChunk.PROPERTIES_FLAG_READABLE | MessagePropertiesChunk.PROPERTIES_FLAG_WRITEABLE, "Subject öäü Subject".getBytes("CP1252")));
       topLevelChunk.setProperty(new PropertyValue(MAPIProperty.BODY_HTML, MessagePropertiesChunk.PROPERTIES_FLAG_READABLE | MessagePropertiesChunk.PROPERTIES_FLAG_WRITEABLE, "<!DOCTYPE html><html><meta charset=\\\"utf-8\\\"><body>HTML öäü</body></html>".getBytes("CP1252"), Types.BINARY));
       
       // ASCII codepage (general).
       byte[] codepageData = new byte[4];
       PropertyValue.LongPropertyValue codepagePropertyValue = new PropertyValue.LongPropertyValue(MAPIProperty.MESSAGE_CODEPAGE,
               MessagePropertiesChunk.PROPERTIES_FLAG_READABLE | MessagePropertiesChunk.PROPERTIES_FLAG_WRITEABLE,
               codepageData);
       codepagePropertyValue.setValue(CodePageUtil.CP_WINDOWS_1252);
       topLevelChunk.setProperty(codepagePropertyValue);
       
       // ASCII codepage (body).
       byte[] cpidData = new byte[4];
       PropertyValue.LongPropertyValue cpidValue = new PropertyValue.LongPropertyValue(MAPIProperty.INTERNET_CPID,
               MessagePropertiesChunk.PROPERTIES_FLAG_READABLE | MessagePropertiesChunk.PROPERTIES_FLAG_WRITEABLE,
               cpidData);
       cpidValue.setValue(CodePageUtil.CP_WINDOWS_1252);
       topLevelChunk.setProperty(cpidValue);

       // Write.
       topLevelChunk.writeProperties(fs.getRoot());
       ByteArrayOutputStream binary = new ByteArrayOutputStream();
       fs.writeFilesystem(binary);
       fs.close();
       
       // Re-parse generated MSG
       try (MAPIMessage reparsed = new MAPIMessage(new ByteArrayInputStream(binary.toByteArray()))) {
           reparsed.guess7BitEncoding();
           assertEquals("Subject öäü Subject", reparsed.getSubject());
           assertEquals(Types.ASCII_STRING, reparsed.getMainChunks().getSubjectChunk().getType());
           assertEquals("<!DOCTYPE html><html><meta charset=\\\"utf-8\\\"><body>HTML öäü</body></html>", reparsed.getHtmlBody());
           assertEquals(Types.BINARY, reparsed.getMainChunks().getAll().get(MAPIProperty.BODY_HTML).get(0).getType());
       }
   }

   /**
    * Test writing properties in unicode based MSG.
    */
   public void testWritePropertiesUnicode() throws Exception {
       // Create file system and write properties
       POIFSFileSystem fs = new POIFSFileSystem();
       MessagePropertiesChunk topLevelChunk = new MessagePropertiesChunk(null, false);

       // Create nameid entries.
       DirectoryEntry nameid = fs.getRoot().createDirectory(NameIdChunks.NAME);
       // GUID stream
       nameid.createDocument(PropertiesChunk.DEFAULT_NAME_PREFIX + "00020102", new ByteArrayInputStream(new byte[0]));
       // Entry stream
       nameid.createDocument(PropertiesChunk.DEFAULT_NAME_PREFIX + "00030102", new ByteArrayInputStream(new byte[0]));
       // String stream
       nameid.createDocument(PropertiesChunk.DEFAULT_NAME_PREFIX + "00040102", new ByteArrayInputStream(new byte[0]));
       // Base properties.

       // Attachment/Recipient counter.
       topLevelChunk.setAttachmentCount(0);
       topLevelChunk.setRecipientCount(0);
       topLevelChunk.setNextAttachmentId(0);
       topLevelChunk.setNextRecipientId(0);
       
       // Unicode string format, HTML enabled.
       byte[] storeSupportMaskData = new byte[4];
       PropertyValue.LongPropertyValue storeSupportPropertyValue = new PropertyValue.LongPropertyValue(MAPIProperty.STORE_SUPPORT_MASK,
               MessagePropertiesChunk.PROPERTIES_FLAG_READABLE | MessagePropertiesChunk.PROPERTIES_FLAG_WRITEABLE,
               storeSupportMaskData);
       storeSupportPropertyValue.setValue(0x00050000);
       topLevelChunk.setProperty(storeSupportPropertyValue);
       
       // Simple properties. Strings as unicode.
       topLevelChunk.setProperty(new PropertyValue(MAPIProperty.MESSAGE_CLASS, MessagePropertiesChunk.PROPERTIES_FLAG_READABLE | MessagePropertiesChunk.PROPERTIES_FLAG_WRITEABLE, "IPM.Note".getBytes("UTF-16LE"), Types.UNICODE_STRING)); //outlook message
       topLevelChunk.setProperty(new PropertyValue(MAPIProperty.HASATTACH, MessagePropertiesChunk.PROPERTIES_FLAG_READABLE | MessagePropertiesChunk.PROPERTIES_FLAG_WRITEABLE, new byte[]{1}));
       topLevelChunk.setProperty(new PropertyValue(MAPIProperty.MESSAGE_FLAGS,  MessagePropertiesChunk.PROPERTIES_FLAG_READABLE | MessagePropertiesChunk.PROPERTIES_FLAG_WRITEABLE, ByteBuffer.allocate(4).putInt(8).array()));
       topLevelChunk.setProperty(new PropertyValue(MAPIProperty.SUBJECT, MessagePropertiesChunk.PROPERTIES_FLAG_READABLE | MessagePropertiesChunk.PROPERTIES_FLAG_WRITEABLE, "Subject öäü Subject".getBytes("UTF-16LE"), Types.UNICODE_STRING));
       topLevelChunk.setProperty(new PropertyValue(MAPIProperty.BODY_HTML, MessagePropertiesChunk.PROPERTIES_FLAG_READABLE | MessagePropertiesChunk.PROPERTIES_FLAG_WRITEABLE, "<!DOCTYPE html><html><meta charset=\\\"windows-1252\\\"><body>HTML öäü</body></html>".getBytes("CP1252"), Types.BINARY));
       
       // ASCII codepage (body).
       byte[] cpidData = new byte[4];
       PropertyValue.LongPropertyValue cpidValue = new PropertyValue.LongPropertyValue(MAPIProperty.INTERNET_CPID,
               MessagePropertiesChunk.PROPERTIES_FLAG_READABLE | MessagePropertiesChunk.PROPERTIES_FLAG_WRITEABLE,
               cpidData);
       cpidValue.setValue(CodePageUtil.CP_WINDOWS_1252);
       topLevelChunk.setProperty(cpidValue);

       // Write.
       topLevelChunk.writeProperties(fs.getRoot());
       ByteArrayOutputStream binary = new ByteArrayOutputStream();
       fs.writeFilesystem(binary);
       fs.close();
       
       // Re-parse generated MSG
       try (MAPIMessage reparsed = new MAPIMessage(new ByteArrayInputStream(binary.toByteArray()))) {
           assertEquals("Subject öäü Subject", reparsed.getSubject());
           assertEquals(Types.UNICODE_STRING, reparsed.getMainChunks().getSubjectChunk().getType());
           assertEquals("<!DOCTYPE html><html><meta charset=\\\"windows-1252\\\"><body>HTML öäü</body></html>", reparsed.getHtmlBody());
           assertEquals(Types.BINARY, reparsed.getMainChunks().getAll().get(MAPIProperty.BODY_HTML).get(0).getType());
       }
   }
}
