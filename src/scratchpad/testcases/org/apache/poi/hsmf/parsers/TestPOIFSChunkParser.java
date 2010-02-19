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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;

import org.apache.poi.hsmf.MAPIMessage;
import org.apache.poi.hsmf.datatypes.AttachmentChunks;
import org.apache.poi.hsmf.datatypes.ChunkGroup;
import org.apache.poi.hsmf.datatypes.Chunks;
import org.apache.poi.hsmf.datatypes.NameIdChunks;
import org.apache.poi.hsmf.datatypes.RecipientChunks;
import org.apache.poi.hsmf.datatypes.RecipientChunks.RecipientChunksSorter;
import org.apache.poi.hsmf.datatypes.StringChunk;
import org.apache.poi.hsmf.datatypes.Types;
import org.apache.poi.hsmf.exceptions.ChunkNotFoundException;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.POIDataSamples;

import junit.framework.TestCase;

/**
 * Tests to verify that the chunk parser works properly
 */
public final class TestPOIFSChunkParser extends TestCase {
   private POIDataSamples samples;

	public TestPOIFSChunkParser() throws IOException {
        samples = POIDataSamples.getHSMFInstance();
	}
	
   public void testFindsCore() throws IOException {
      POIFSFileSystem simple = new POIFSFileSystem(
            new FileInputStream(samples.getFile("quick.msg"))
      );
      
      // Check a few core things are present
      simple.getRoot().getEntry(
            (new StringChunk(Chunks.SUBJECT, Types.ASCII_STRING)).getEntryName()
      );
      simple.getRoot().getEntry(
            (new StringChunk(Chunks.DISPLAY_FROM, Types.ASCII_STRING)).getEntryName()
      );
      
      // Now load the file
      MAPIMessage msg = new MAPIMessage(simple);
      try {
         assertEquals("Kevin Roast", msg.getDisplayTo());
         assertEquals("Kevin Roast", msg.getDisplayFrom());
         assertEquals("Test the content transformer", msg.getSubject());
      } catch(ChunkNotFoundException e) {
         fail();
      }
      
      // Check date too
      try {
         SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
         
         Calendar c = msg.getMessageDate();
         assertEquals( "2007-06-14 09:42:55", f.format(c.getTime()) );
      } catch(ChunkNotFoundException e) {
         fail();
      }
   }
   
   public void testFindsRecips() throws IOException, ChunkNotFoundException {
      POIFSFileSystem simple = new POIFSFileSystem(
            new FileInputStream(samples.getFile("quick.msg"))
      );
      
      simple.getRoot().getEntry("__recip_version1.0_#00000000");

      ChunkGroup[] groups = POIFSChunkParser.parse(simple.getRoot());
      assertEquals(3, groups.length);
      assertTrue(groups[0] instanceof Chunks);
      assertTrue(groups[1] instanceof RecipientChunks);
      assertTrue(groups[2] instanceof NameIdChunks);
      
      RecipientChunks recips = (RecipientChunks)groups[1];
      assertEquals("kevin.roast@alfresco.org", recips.recipientSMTPChunk.getValue());
      assertEquals("/O=HOSTEDSERVICE2/OU=FIRST ADMINISTRATIVE GROUP/CN=RECIPIENTS/CN=Kevin.roast@ben", 
            recips.recipientEmailChunk.getValue());
      
      String search = new String(recips.recipientSearchChunk.getValue(), "ASCII");
      assertEquals("CN=KEVIN.ROAST@BEN\0", search.substring(search.length()-19));
      
      // Now via MAPIMessage
      MAPIMessage msg = new MAPIMessage(simple);
      assertNotNull(msg.getRecipientDetailsChunks());
      assertEquals(1, msg.getRecipientDetailsChunks().length);
      
      assertEquals("kevin.roast@alfresco.org", msg.getRecipientDetailsChunks()[0].recipientSMTPChunk.getValue());
      assertEquals("kevin.roast@alfresco.org", msg.getRecipientDetailsChunks()[0].getRecipientEmailAddress());
      assertEquals("Kevin Roast", msg.getRecipientDetailsChunks()[0].getRecipientName());
      assertEquals("kevin.roast@alfresco.org", msg.getRecipientEmailAddress());
      
      
      // Try both SMTP and EX files for recipient
      assertEquals("EX", msg.getRecipientDetailsChunks()[0].deliveryTypeChunk.getValue());
      assertEquals("kevin.roast@alfresco.org", msg.getRecipientDetailsChunks()[0].recipientSMTPChunk.getValue());
      assertEquals("/O=HOSTEDSERVICE2/OU=FIRST ADMINISTRATIVE GROUP/CN=RECIPIENTS/CN=Kevin.roast@ben", 
            msg.getRecipientDetailsChunks()[0].recipientEmailChunk.getValue());
      
      // Now look at another message
      msg = new MAPIMessage(new POIFSFileSystem(
            new FileInputStream(samples.getFile("simple_test_msg.msg"))
      ));
      assertNotNull(msg.getRecipientDetailsChunks());
      assertEquals(1, msg.getRecipientDetailsChunks().length);
      
      assertEquals("SMTP", msg.getRecipientDetailsChunks()[0].deliveryTypeChunk.getValue());
      assertEquals(null, msg.getRecipientDetailsChunks()[0].recipientSMTPChunk);
      assertEquals(null, msg.getRecipientDetailsChunks()[0].recipientNameChunk);
      assertEquals("travis@overwrittenstack.com", msg.getRecipientDetailsChunks()[0].recipientEmailChunk.getValue());
      assertEquals("travis@overwrittenstack.com", msg.getRecipientEmailAddress());
   }
   
   public void testFindsMultipleRecipients() throws IOException, ChunkNotFoundException {
      POIFSFileSystem multiple = new POIFSFileSystem(
            new FileInputStream(samples.getFile("example_received_unicode.msg"))
      );
      
      multiple.getRoot().getEntry("__recip_version1.0_#00000000");
      multiple.getRoot().getEntry("__recip_version1.0_#00000001");
      multiple.getRoot().getEntry("__recip_version1.0_#00000002");
      multiple.getRoot().getEntry("__recip_version1.0_#00000003");
      multiple.getRoot().getEntry("__recip_version1.0_#00000004");
      multiple.getRoot().getEntry("__recip_version1.0_#00000005");
      
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
            (RecipientChunks)groups[1],
            (RecipientChunks)groups[2],
            (RecipientChunks)groups[3],
            (RecipientChunks)groups[4],
            (RecipientChunks)groups[6],
            (RecipientChunks)groups[7],
      };
      assertEquals(6, chunks.length);
      assertEquals(0, chunks[0].recipientNumber);
      assertEquals(2, chunks[1].recipientNumber);
      assertEquals(4, chunks[2].recipientNumber);
      assertEquals(5, chunks[3].recipientNumber);
      assertEquals(3, chunks[4].recipientNumber);
      assertEquals(1, chunks[5].recipientNumber);
      
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
      MAPIMessage msg = new MAPIMessage(multiple);
      assertEquals(6, msg.getRecipientEmailAddressList().length);
      assertEquals(6, msg.getRecipientNamesList().length);
      
      assertEquals("'Ashutosh Dandavate'", msg.getRecipientNamesList()[0]);
      assertEquals("'Paul Holmes-Higgin'", msg.getRecipientNamesList()[1]);
      assertEquals("'Mike Farman'",        msg.getRecipientNamesList()[2]);
      assertEquals("nickb@alfresco.com",   msg.getRecipientNamesList()[3]);
      assertEquals("nick.burch@alfresco.com", msg.getRecipientNamesList()[4]);
      assertEquals("'Roy Wetherall'",      msg.getRecipientNamesList()[5]);
      
      assertEquals("ashutosh.dandavate@alfresco.com", msg.getRecipientEmailAddressList()[0]);
      assertEquals("paul.hh@alfresco.com",            msg.getRecipientEmailAddressList()[1]);
      assertEquals("mikef@alfresco.com",              msg.getRecipientEmailAddressList()[2]);
      assertEquals("nickb@alfresco.com",              msg.getRecipientEmailAddressList()[3]);
      assertEquals("nick.burch@alfresco.com",         msg.getRecipientEmailAddressList()[4]);
      assertEquals("roy.wetherall@alfresco.com",      msg.getRecipientEmailAddressList()[5]);
   }
   
   public void testFindsNameId() throws IOException {
      POIFSFileSystem simple = new POIFSFileSystem(
            new FileInputStream(samples.getFile("quick.msg"))
      );
      
      simple.getRoot().getEntry("__nameid_version1.0");

      ChunkGroup[] groups = POIFSChunkParser.parse(simple.getRoot());
      assertEquals(3, groups.length);
      assertTrue(groups[0] instanceof Chunks);
      assertTrue(groups[1] instanceof RecipientChunks);
      assertTrue(groups[2] instanceof NameIdChunks);
      
      NameIdChunks nameId = (NameIdChunks)groups[2];
      assertEquals(10, nameId.getAll().length);
      
      // Now via MAPIMessage
      MAPIMessage msg = new MAPIMessage(simple);
      assertNotNull(msg.getNameIdChunks());
      assertEquals(10, msg.getNameIdChunks().getAll().length);
   }
   
	public void testFindsAttachments() throws IOException {
	   POIFSFileSystem with = new POIFSFileSystem(
	         new FileInputStream(samples.getFile("attachment_test_msg.msg"))
	   );
      POIFSFileSystem without = new POIFSFileSystem(
            new FileInputStream(samples.getFile("quick.msg"))
      );
      AttachmentChunks attachment;
      
      
      // Check raw details on the one with
      with.getRoot().getEntry("__attach_version1.0_#00000000");
      with.getRoot().getEntry("__attach_version1.0_#00000001");
      POIFSChunkParser.parse(with.getRoot());
      
      ChunkGroup[] groups = POIFSChunkParser.parse(with.getRoot());
      assertEquals(5, groups.length);
      assertTrue(groups[0] instanceof Chunks);
      assertTrue(groups[1] instanceof RecipientChunks);
      assertTrue(groups[2] instanceof AttachmentChunks);
      assertTrue(groups[3] instanceof AttachmentChunks);
      assertTrue(groups[4] instanceof NameIdChunks);
      
      attachment = (AttachmentChunks)groups[2];
      assertEquals("TEST-U~1.DOC", attachment.attachFileName.toString());
      assertEquals("test-unicode.doc", attachment.attachLongFileName.toString());
      assertEquals(24064, attachment.attachData.getValue().length);
      
      attachment = (AttachmentChunks)groups[3];
      assertEquals("pj1.txt", attachment.attachFileName.toString());
      assertEquals("pj1.txt", attachment.attachLongFileName.toString());
      assertEquals(89, attachment.attachData.getValue().length);
	   
      
      // Check raw details on one without
      try {
         without.getRoot().getEntry("__attach_version1.0_#00000000");
         fail();
      } catch(FileNotFoundException e) {}
      try {
         without.getRoot().getEntry("__attach_version1.0_#00000001");
         fail();
      } catch(FileNotFoundException e) {}
      
      
	   // One with, from the top
      MAPIMessage msgWith = new MAPIMessage(with);
      assertEquals(2, msgWith.getAttachmentFiles().length);

      attachment = msgWith.getAttachmentFiles()[0];
      assertEquals("TEST-U~1.DOC", attachment.attachFileName.toString());
      assertEquals("test-unicode.doc", attachment.attachLongFileName.toString());
      assertEquals(24064, attachment.attachData.getValue().length);
      
      attachment = msgWith.getAttachmentFiles()[1];
      assertEquals("pj1.txt", attachment.attachFileName.toString());
      assertEquals("pj1.txt", attachment.attachLongFileName.toString());
      assertEquals(89, attachment.attachData.getValue().length);
      
      // Plus check core details are there
      try {
         assertEquals("'nicolas1.23456@free.fr'", msgWith.getDisplayTo());
         assertEquals("Nicolas1 23456", msgWith.getDisplayFrom());
         assertEquals("test pi\u00e8ce jointe 1", msgWith.getSubject());
      } catch(ChunkNotFoundException e) {
         fail();
      }
	   
      
	   // One without, from the top
      MAPIMessage msgWithout = new MAPIMessage(without);
      
      // No attachments
      assertEquals(0, msgWithout.getAttachmentFiles().length);
      
      // But has core details
      try {
         assertEquals("Kevin Roast", msgWithout.getDisplayTo());
         assertEquals("Kevin Roast", msgWithout.getDisplayFrom());
         assertEquals("Test the content transformer", msgWithout.getSubject());
      } catch(ChunkNotFoundException e) {
         fail();
      }
	}
}
