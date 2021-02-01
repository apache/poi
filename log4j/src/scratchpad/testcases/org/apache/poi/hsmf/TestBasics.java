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

import static org.apache.poi.POITestCase.assertContains;
import static org.apache.poi.POITestCase.assertStartsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hsmf.exceptions.ChunkNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests to verify that we can perform basic opperations on
 *  a range of files
 */
public final class TestBasics {
   private MAPIMessage simple;
   private MAPIMessage quick;
   private MAPIMessage outlook30;
   private MAPIMessage attachments;
   private MAPIMessage noRecipientAddress;
   private MAPIMessage unicode;
   private MAPIMessage cyrillic;
   private MAPIMessage chinese;

   /**
    * Initialize this test, load up the blank.msg mapi message.
    */
   @BeforeEach
   void setup() throws IOException {
       POIDataSamples samples = POIDataSamples.getHSMFInstance();
       simple = new MAPIMessage(samples.openResourceAsStream("simple_test_msg.msg"));
       quick  = new MAPIMessage(samples.openResourceAsStream("quick.msg"));
       outlook30  = new MAPIMessage(samples.openResourceAsStream("outlook_30_msg.msg"));
       attachments = new MAPIMessage(samples.openResourceAsStream("attachment_test_msg.msg"));
       noRecipientAddress = new MAPIMessage(samples.openResourceAsStream("no_recipient_address.msg"));
       unicode = new MAPIMessage(samples.openResourceAsStream("example_received_unicode.msg"));
       cyrillic = new MAPIMessage(samples.openResourceAsStream("cyrillic_message.msg"));
       chinese = new MAPIMessage(samples.openResourceAsStream("chinese-traditional.msg"));
   }

   /**
    * Can we always get the recipient's email?
    */
   @Test
   void testRecipientEmail() throws Exception {
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
   @Test
   void testSubject() throws Exception {
      assertEquals("test message", simple.getSubject());
      assertEquals("Test the content transformer", quick.getSubject());
      assertEquals("IN-SPIRE servers going down for a bit, back up around 8am", outlook30.getSubject());
      assertEquals("test pi\u00e8ce jointe 1", attachments.getSubject());
   }

   /**
    * Test message headers
    */
   @Test
   void testHeaders() throws Exception {
      // Simple email first
      assertEquals(26, simple.getHeaders().length);
      assertStartsWith(simple.getHeaders()[0], "Return-path:");
      assertEquals("Envelope-to: travis@overwrittenstack.com", simple.getHeaders()[1]);
      assertStartsWith(simple.getHeaders()[25], "X-Antivirus-Scanner: Clean");

      // Quick doesn't have them
      assertThrows(ChunkNotFoundException.class, quick::getHeaders);

      // Attachments doesn't have them
      assertThrows(ChunkNotFoundException.class, attachments::getHeaders);

      // Outlook30 has some
      assertEquals(33, outlook30.getHeaders().length);
      assertStartsWith(outlook30.getHeaders()[0], "Microsoft Mail Internet Headers");
      assertStartsWith(outlook30.getHeaders()[1], "x-mimeole:");
      assertStartsWith(outlook30.getHeaders()[32], "\t\"Williams"); // May need better parsing in future
   }

   @Test
   void testBody() throws Exception {
      // Messages may have their bodies saved as plain text, html, and/or rtf.
      assertEquals("This is a test message.", simple.getTextBody());
      assertEquals("The quick brown fox jumps over the lazy dog\r\n", quick.getTextBody());
      assertStartsWith(outlook30.getTextBody(), "I am shutting down the IN-SPIRE servers now for 30ish minutes.\r\n\r\n");
      assertStartsWith(attachments.getTextBody(), "contenu\r\n\r\n");
      assertStartsWith(unicode.getTextBody(), "..less you are Nick.....");

      // outlook30 has chunks for all 3 body formats
      // Examine one of the paragraphs is present in all 3 formats, surrounded by markup tags
      String text = "I am shutting down the IN-SPIRE servers now for 30ish minutes.";
      assertStartsWith(outlook30.getTextBody(), text + "\r\n\r\n");
      assertEquals(850494485, outlook30.getTextBody().hashCode());

      assertStartsWith(outlook30.getHtmlBody(), "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 3.2//EN\">\r\n<HTML>\r\n<HEAD>");
      assertContains(outlook30.getHtmlBody(), "<P DIR=LTR><SPAN LANG=\"en-us\"><FONT FACE=\"Calibri\">" + text + "</FONT></SPAN></P>");
      assertEquals(-654938715, outlook30.getHtmlBody().hashCode());

      assertStartsWith(outlook30.getRtfBody(), "{\\rtf1\\adeflang1025\\ansi\\ansicpg1252\\uc1\\adeff3150");
      assertContains(outlook30.getRtfBody(), "{\\rtlch\\fcs1 \\af31507 \\ltrch\\fcs0 \\cf0\\insrsid5003910 " + text + "\r\n\\par \r\n\\par");
      assertEquals(891652290, outlook30.getRtfBody().hashCode());
   }

   /**
    * Test attachments
    */
   @Test
   void testAttachments() {
      assertEquals(0, simple.getAttachmentFiles().length);
      assertEquals(0, quick.getAttachmentFiles().length);
      assertEquals(0, outlook30.getAttachmentFiles().length);
      assertEquals(2, attachments.getAttachmentFiles().length);
   }

   /**
    * Test missing chunks.
    * Use a file with no HTML body
    */
   @Test
   void testMissingChunks() throws Exception {
      assertFalse(attachments.isReturnNullOnMissingChunk());
      assertThrows(ChunkNotFoundException.class, attachments::getHtmlBody);

      attachments.setReturnNullOnMissingChunk(true);

      assertNull(attachments.getHtmlBody());

      attachments.setReturnNullOnMissingChunk(false);
      assertThrows(ChunkNotFoundException.class, attachments::getHtmlBody);
   }

   /**
    * More missing chunk testing, this time for
    *  missing recipient email address
    */
   @Test
   void testMissingAddressChunk() throws Exception {
      assertFalse(noRecipientAddress.isReturnNullOnMissingChunk());
      assertThrows(ChunkNotFoundException.class, noRecipientAddress::getRecipientEmailAddress);
      assertThrows(ChunkNotFoundException.class, noRecipientAddress::getRecipientEmailAddressList);

      noRecipientAddress.setReturnNullOnMissingChunk(true);

      noRecipientAddress.getRecipientEmailAddress();
      noRecipientAddress.getRecipientEmailAddressList();
      assertEquals("", noRecipientAddress.getRecipientEmailAddress());
      assertEquals(1, noRecipientAddress.getRecipientEmailAddressList().length);
      assertNull(noRecipientAddress.getRecipientEmailAddressList()[0]);

      // Check a few other bits too
      assertEquals("Microsoft Outlook 2003 Team", noRecipientAddress.getDisplayFrom());
      assertEquals("New Outlook User", noRecipientAddress.getDisplayTo());

      noRecipientAddress.setReturnNullOnMissingChunk(false);
   }

   /**
    * Test the 7 bit detection
    */
   @Test
   void test7BitDetection() {
      assertFalse(unicode.has7BitEncodingStrings());
      assertTrue(simple.has7BitEncodingStrings());
      assertTrue(chinese.has7BitEncodingStrings());
      assertTrue(cyrillic.has7BitEncodingStrings());
   }

   /**
    * We default to CP1252, but can sometimes do better
    *  if needed.
    * This file is really CP1251, according to the person
    *  who submitted it in bug #49441
    */
   @Test
   void testEncoding() throws Exception {
      assertEquals(2, cyrillic.getRecipientDetailsChunks().length);
      assertEquals("CP1252", cyrillic.getRecipientDetailsChunks()[0].getRecipientDisplayNameChunk().get7BitEncoding());
      assertEquals("CP1252", cyrillic.getRecipientDetailsChunks()[1].getRecipientDisplayNameChunk().get7BitEncoding());

      cyrillic.guess7BitEncoding();

      assertEquals("Cp1251", cyrillic.getRecipientDetailsChunks()[0].getRecipientDisplayNameChunk().get7BitEncoding());
      assertEquals("Cp1251", cyrillic.getRecipientDetailsChunks()[1].getRecipientDisplayNameChunk().get7BitEncoding());

      // Override it, check it's taken
      cyrillic.set7BitEncoding("UTF-8");
      assertEquals("UTF-8", cyrillic.getRecipientDetailsChunks()[0].getRecipientDisplayNameChunk().get7BitEncoding());
      assertEquals("UTF-8", cyrillic.getRecipientDetailsChunks()[1].getRecipientDisplayNameChunk().get7BitEncoding());


      // Check with a file that has no headers
      assertThrows(ChunkNotFoundException.class, chinese::getHeaders, "File doesn't have headers!");

      String html = chinese.getHtmlBody();
      assertTrue(html.contains("text/html; charset=big5"), "Charset not found:\n" + html);

      // Defaults to CP1251
      assertEquals("CP1252", chinese.getRecipientDetailsChunks()[0].getRecipientDisplayNameChunk().get7BitEncoding());

      // But after guessing goes to the correct one, cp950 (Windows Traditional Chinese)
      chinese.guess7BitEncoding();
      assertEquals("cp950", chinese.getRecipientDetailsChunks()[0].getRecipientDisplayNameChunk().get7BitEncoding());
   }
}
