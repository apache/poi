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

/**
 * Tests to verify that we can perform basic opperations on 
 *  a range of files
 */
public final class TestBasics extends TestCase {
   private MAPIMessage simple;
   private MAPIMessage quick;
   private MAPIMessage outlook30;
   private MAPIMessage attachments;
   private MAPIMessage noRecipientAddress;
   private MAPIMessage cyrillic;

	/**
	 * Initialize this test, load up the blank.msg mapi message.
	 * @throws Exception
	 */
	public TestBasics() throws IOException {
        POIDataSamples samples = POIDataSamples.getHSMFInstance();
		simple = new MAPIMessage(samples.openResourceAsStream("simple_test_msg.msg"));
      quick  = new MAPIMessage(samples.openResourceAsStream("quick.msg"));
      outlook30  = new MAPIMessage(samples.openResourceAsStream("outlook_30_msg.msg"));
      attachments = new MAPIMessage(samples.openResourceAsStream("attachment_test_msg.msg"));
      noRecipientAddress = new MAPIMessage(samples.openResourceAsStream("no_recipient_address.msg"));
      cyrillic = new MAPIMessage(samples.openResourceAsStream("cyrillic_message.msg"));
	}
	
	/**
	 * Can we always get the recipient's email?
	 */
	public void testRecipientEmail() throws Exception {
      assertEquals("travis@overwrittenstack.com", simple.getRecipientEmailAddress());
      assertEquals("kevin.roast@alfresco.org", quick.getRecipientEmailAddress());
      assertEquals("nicolas1.23456@free.fr", attachments.getRecipientEmailAddress());
      
      // This one has lots...
      assertEquals(18, outlook30.getRecipientEmailAddressList().length);
      assertEquals("shawn.bohn@pnl.gov; gus.calapristi@pnl.gov; Richard.Carter@pnl.gov; " +
      		"barb.cheney@pnl.gov; nick.cramer@pnl.gov; vern.crow@pnl.gov; Laura.Curtis@pnl.gov; " +
      		"julie.dunkle@pnl.gov; david.gillen@pnl.gov; michelle@pnl.gov; Jereme.Haack@pnl.gov; " +
      		"Michelle.Hart@pnl.gov; ranata.johnson@pnl.gov; grant.nakamura@pnl.gov; " +
      		"debbie.payne@pnl.gov; stuart.rose@pnl.gov; randall.scarberry@pnl.gov; Leigh.Williams@pnl.gov", 
            outlook30.getRecipientEmailAddress()
      );
	}
	
	/**
	 * Test subject
	 */
	public void testSubject() throws Exception {
      assertEquals("test message", simple.getSubject());
      assertEquals("Test the content transformer", quick.getSubject());
      assertEquals("IN-SPIRE servers going down for a bit, back up around 8am", outlook30.getSubject());
      assertEquals("test pi\u00e8ce jointe 1", attachments.getSubject());
	}
	
	/**
	 * Test message headers
	 */
	public void testHeaders() throws Exception {
	   // Simple email first
	   assertEquals(26, simple.getHeaders().length);
	   assertTrue(simple.getHeaders()[0].startsWith("Return-path:"));
      assertTrue(simple.getHeaders()[1].equals("Envelope-to: travis@overwrittenstack.com"));
      assertTrue(simple.getHeaders()[25].startsWith("X-Antivirus-Scanner: Clean"));
      
      // Quick doesn't have them
      try {
         quick.getHeaders();
         fail();
      } catch(ChunkNotFoundException e) {}
      
      // Attachments doesn't have them
      try {
         attachments.getHeaders();
         fail();
      } catch(ChunkNotFoundException e) {}
      
      // Outlook30 has some
      assertEquals(33, outlook30.getHeaders().length);
      assertTrue(outlook30.getHeaders()[0].startsWith("Microsoft Mail Internet Headers"));
      assertTrue(outlook30.getHeaders()[1].startsWith("x-mimeole:"));
      assertTrue(outlook30.getHeaders()[32].startsWith("\t\"Williams")); // May need better parsing in future
	}
	
	/**
	 * Test attachments
	 */
	public void testAttachments() throws Exception {
      assertEquals(0, simple.getAttachmentFiles().length);
      assertEquals(0, quick.getAttachmentFiles().length);
      assertEquals(0, outlook30.getAttachmentFiles().length);
      assertEquals(2, attachments.getAttachmentFiles().length);
	}
	
	/**
	 * Test missing chunks
	 */
	public void testMissingChunks() throws Exception {
	   assertEquals(false, attachments.isReturnNullOnMissingChunk());

	   try {
	      attachments.getMessageDate();
	      fail();
	   } catch(ChunkNotFoundException e) {
	      // Good
	   }
	   
	   attachments.setReturnNullOnMissingChunk(true);
	   
	   assertEquals(null, attachments.getMessageDate());
	   
      attachments.setReturnNullOnMissingChunk(false);
      
      try {
         attachments.getMessageDate();
         fail();
      } catch(ChunkNotFoundException e) {
         // Good
      }
	}
	
	/**
	 * More missing chunk testing, this time for
	 *  missing recipient email address
	 */
	public void testMissingAddressChunk() throws Exception {
      assertEquals(false, noRecipientAddress.isReturnNullOnMissingChunk());

      try {
         noRecipientAddress.getRecipientEmailAddress();
         fail();
      } catch(ChunkNotFoundException e) {
         // Good
      }
      try {
         noRecipientAddress.getRecipientEmailAddressList();
         fail();
      } catch(ChunkNotFoundException e) {
         // Good
      }
      
      noRecipientAddress.setReturnNullOnMissingChunk(true);
      
      noRecipientAddress.getRecipientEmailAddress();
      noRecipientAddress.getRecipientEmailAddressList();
      assertEquals("", noRecipientAddress.getRecipientEmailAddress());
      assertEquals(1, noRecipientAddress.getRecipientEmailAddressList().length);
      assertEquals(null, noRecipientAddress.getRecipientEmailAddressList()[0]);
      
      // Check a few other bits too
      assertEquals("Microsoft Outlook 2003 Team", noRecipientAddress.getDisplayFrom());
      assertEquals("New Outlook User", noRecipientAddress.getDisplayTo());
      
      noRecipientAddress.setReturnNullOnMissingChunk(false);
	}
	
   /**
    * We default to CP1252, but can sometimes do better
    *  if needed.
    * This file is really CP1251, according to the person
    *  who submitted it in bug #49441
    */
   public void testEncoding() throws Exception {
      assertEquals(2, cyrillic.getRecipientDetailsChunks().length);
      assertEquals("CP1252", cyrillic.getRecipientDetailsChunks()[0].recipientDisplayNameChunk.get7BitEncoding());
      assertEquals("CP1252", cyrillic.getRecipientDetailsChunks()[1].recipientDisplayNameChunk.get7BitEncoding());
      
      cyrillic.guess7BitEncoding();
      
      assertEquals("Cp1251", cyrillic.getRecipientDetailsChunks()[0].recipientDisplayNameChunk.get7BitEncoding());
      assertEquals("Cp1251", cyrillic.getRecipientDetailsChunks()[1].recipientDisplayNameChunk.get7BitEncoding());
   }
}
