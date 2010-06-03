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

package org.apache.poi.hsmf.extractor;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import junit.framework.TestCase;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hsmf.MAPIMessage;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * Tests to verify that the text extractor works
 */
public final class TestOutlookTextExtractor extends TestCase {
   private POIDataSamples samples;

	public TestOutlookTextExtractor() throws IOException {
        samples = POIDataSamples.getHSMFInstance();
	}
	
	private void assertContains(String haystack, String needle) {
	   if(haystack.indexOf(needle) > -1) {
	      return;
	   }
	   fail("'" + needle + "' wasn't found in '" + haystack + "'");
	}
	
   public void testQuick() throws Exception {
      POIFSFileSystem simple = new POIFSFileSystem(
            new FileInputStream(samples.getFile("quick.msg"))
      );
      MAPIMessage msg = new MAPIMessage(simple);
      
      OutlookTextExtactor ext = new OutlookTextExtactor(msg);
      String text = ext.getText();
      
      assertContains(text, "From: Kevin Roast\n");
      assertContains(text, "To: Kevin Roast <kevin.roast@alfresco.org>\n");
      assertEquals(-1, text.indexOf("CC:"));
      assertEquals(-1, text.indexOf("BCC:"));
      assertEquals(-1, text.indexOf("Attachment:"));
      assertContains(text, "Subject: Test the content transformer\n");
      Calendar cal = new GregorianCalendar(2007, 5, 14, 9, 42, 55);
      SimpleDateFormat f = new SimpleDateFormat("E, d MMM yyyy HH:mm:ss");
      String dateText = f.format(cal.getTime());
      assertContains(text, "Date: " + dateText + "\n");
      assertContains(text, "The quick brown fox jumps over the lazy dog");
   }
   
   public void testSimple() throws Exception {
      MAPIMessage msg = new MAPIMessage(new POIFSFileSystem(
            new FileInputStream(samples.getFile("simple_test_msg.msg"))
      ));
      
      OutlookTextExtactor ext = new OutlookTextExtactor(msg);
      String text = ext.getText();
      
      assertContains(text, "From: Travis Ferguson\n");
      assertContains(text, "To: travis@overwrittenstack.com\n");
      assertEquals(-1, text.indexOf("CC:"));
      assertEquals(-1, text.indexOf("BCC:"));
      assertContains(text, "Subject: test message\n");
      assertContains(text, "Date: Fri, 6 Jul 2007 01:27:17 -0400\n");
      assertContains(text, "This is a test message.");
   }

   public void testConstructors() throws Exception {
      String inp = (new OutlookTextExtactor(new FileInputStream(
            samples.getFile("simple_test_msg.msg")
      )).getText());
      String poifs = (new OutlookTextExtactor(new POIFSFileSystem(new FileInputStream(
            samples.getFile("simple_test_msg.msg")
      ))).getText());
      String mapi = (new OutlookTextExtactor(new MAPIMessage(new FileInputStream(
            samples.getFile("simple_test_msg.msg")
      ))).getText());
      
      assertEquals(inp, poifs);
      assertEquals(inp, mapi);
   }
   
   /**
    * Test that we correctly handle multiple To+CC+BCC
    *  recipients in an email we sent.
    */
   public void testSentWithMulipleRecipients() throws Exception {
      // To: 'Ashutosh Dandavate' <ashutosh.dandavate@alfresco.com>,
      //   'Paul Holmes-Higgin' <paul.hh@alfresco.com>,
      //   'Mike Farman' <mikef@alfresco.com>
      // Cc: nickb@alfresco.com, nick.burch@alfresco.com,
      //   'Roy Wetherall' <roy.wetherall@alfresco.com>
      // Bcc: 'David Caruana' <dave.caruana@alfresco.com>,
      //   'Vonka Jan' <roy.wetherall@alfresco.com>
      
      String[] files = new String[] {
            "example_sent_regular.msg", "example_sent_unicode.msg"
      };
      for(String file : files) {
         MAPIMessage msg = new MAPIMessage(new POIFSFileSystem(
               new FileInputStream(samples.getFile(file))
         ));
         
         OutlookTextExtactor ext = new OutlookTextExtactor(msg);
         String text = ext.getText();
         
         assertContains(text, "From: Mike Farman\n");
         assertContains(text, "To: 'Ashutosh Dandavate' <ashutosh.dandavate@alfresco.com>; " +
         		"'Paul Holmes-Higgin' <paul.hh@alfresco.com>; 'Mike Farman' <mikef@alfresco.com>\n");
         assertContains(text, "CC: 'nickb@alfresco.com' <nickb@alfresco.com>; " +
         		"'nick.burch@alfresco.com' <nick.burch@alfresco.com>; 'Roy Wetherall' <roy.wetherall@alfresco.com>\n");
         assertContains(text, "BCC: 'David Caruana' <dave.caruana@alfresco.com>; " +
         		"'Vonka Jan' <jan.vonka@alfresco.com>\n");
         assertContains(text, "Subject: This is a test message please ignore\n");
         assertEquals(-1, text.indexOf("Date:"));
         assertContains(text, "The quick brown fox jumps over the lazy dog");
      }
   }
   
   /**
    * Test that we correctly handle multiple To+CC
    *  recipients in an email we received.
    */
   public void testReceivedWithMultipleRecipients() throws Exception {
      // To: 'Ashutosh Dandavate' <ashutosh.dandavate@alfresco.com>,
      //   'Paul Holmes-Higgin' <paul.hh@alfresco.com>,
      //   'Mike Farman' <mikef@alfresco.com>
      // Cc: nickb@alfresco.com, nick.burch@alfresco.com,
      //   'Roy Wetherall' <roy.wetherall@alfresco.com>
      // (No BCC shown) 
      
      
      String[] files = new String[] {
            "example_received_regular.msg", "example_received_unicode.msg"
      };
      for(String file : files) {
         MAPIMessage msg = new MAPIMessage(new POIFSFileSystem(
               new FileInputStream(samples.getFile(file))
         ));
         
         OutlookTextExtactor ext = new OutlookTextExtactor(msg);
         String text = ext.getText();
         
         assertContains(text, "From: Mike Farman\n");
         assertContains(text, "To: 'Ashutosh Dandavate' <ashutosh.dandavate@alfresco.com>; " +
               "'Paul Holmes-Higgin' <paul.hh@alfresco.com>; 'Mike Farman' <mikef@alfresco.com>\n");
         assertContains(text, "CC: nickb@alfresco.com; " +
               "nick.burch@alfresco.com; 'Roy Wetherall' <roy.wetherall@alfresco.com>\n");
         assertEquals(-1, text.indexOf("BCC:"));
         assertContains(text, "Subject: This is a test message please ignore\n");
         assertContains(text, "Date: Mon, 11 Jan 2010 16:25:07 +0000 (GMT)\n");
         assertContains(text, "The quick brown fox jumps over the lazy dog");
      }
   }
   
   /**
    * See also {@link org.apache.poi.extractor.TestExtractorFactory#testEmbeded()}
    */
   public void testWithAttachments() throws Exception {
      POIFSFileSystem simple = new POIFSFileSystem(
            new FileInputStream(samples.getFile("attachment_test_msg.msg"))
      );
      MAPIMessage msg = new MAPIMessage(simple);
      OutlookTextExtactor ext = new OutlookTextExtactor(msg);
      
      // Check the normal bits
      String text = ext.getText();
      
      assertContains(text, "From: Nicolas1");
      assertContains(text, "To: 'nicolas1.23456@free.fr'");
      assertEquals(-1, text.indexOf("CC:"));
      assertEquals(-1, text.indexOf("BCC:"));
      assertContains(text, "Subject: test");
      assertEquals(-1, text.indexOf("Date:"));
      assertContains(text, "Attachment: test-unicode.doc\n");
      assertContains(text, "Attachment: pj1.txt\n");
      assertContains(text, "contenu");
      
      // Embeded bits are checked in
      //  TestExtractorFactory
   }
}
