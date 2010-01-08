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
import org.apache.poi.hsmf.datatypes.AttachmentChunks;
import org.apache.poi.hsmf.exceptions.ChunkNotFoundException;

/**
 * Tests to verify that we can read attachments from msg file
 * 
 * @author Nicolas Bureau
 */
public class TestFileWithAttachmentsRead extends TestCase {
   private MAPIMessage mapiMessage;

   /**
    * Initialize this test, load up the attachment_test_msg.msg mapi message.
    * 
    * @throws Exception
    */
   public TestFileWithAttachmentsRead() throws IOException {
      POIDataSamples samples = POIDataSamples.getHSMFInstance();
      this.mapiMessage = new MAPIMessage(samples.openResourceAsStream("attachment_test_msg.msg"));
   }

   /**
    * Test to see if we can retrieve attachments.
    * 
    * @throws ChunkNotFoundException
    * 
    */
   public void testRetrieveAttachments() {
      AttachmentChunks[] attachments = mapiMessage.getAttachmentFiles();
      int obtained = attachments.length;
      int expected = 2;

      TestCase.assertEquals(obtained, expected);
   }

   /**
    * Test to see if attachments are not empty.
    */
   public void testReadAttachments() throws IOException {
      AttachmentChunks[] attachments = mapiMessage.getAttachmentFiles();

      // Basic checks
      for (AttachmentChunks attachment : attachments) {
         assertTrue(attachment.attachFileName.getValue().length() > 0);
         assertTrue(attachment.attachLongFileName.getValue().length() > 0);
         assertTrue(attachment.attachExtension.getValue().length() > 0);
         if(attachment.attachMimeTag != null) {
            assertTrue(attachment.attachMimeTag.getValue().length() > 0);
         }
      }

      AttachmentChunks attachment;

      // Now check in detail
      attachment = mapiMessage.getAttachmentFiles()[0];
      assertEquals("TEST-U~1.DOC", attachment.attachFileName.toString());
      assertEquals("test-unicode.doc", attachment.attachLongFileName.toString());
      assertEquals(".doc", attachment.attachExtension.getValue());
      assertEquals(null, attachment.attachMimeTag);
      assertEquals(24064, attachment.attachData.getValue().length);

      attachment = mapiMessage.getAttachmentFiles()[1];
      assertEquals("pj1.txt", attachment.attachFileName.toString());
      assertEquals("pj1.txt", attachment.attachLongFileName.toString());
      assertEquals(".txt", attachment.attachExtension.getValue());
      assertEquals(null, attachment.attachMimeTag);
      assertEquals(89, attachment.attachData.getValue().length);
   }
}
