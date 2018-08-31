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

import static org.apache.poi.POITestCase.assertContains;
import static org.apache.poi.POITestCase.assertNotContained;
import static org.junit.Assert.assertEquals;

import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hsmf.MAPIMessage;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.LocaleUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests to verify that the text extractor works
 */
public final class TestOutlookTextExtractor {
   private final POIDataSamples samples = POIDataSamples.getHSMFInstance();
	
   private static TimeZone userTZ;
   
   @BeforeClass
   public static void initTimeZone() {
       userTZ = LocaleUtil.getUserTimeZone();
       LocaleUtil.setUserTimeZone(LocaleUtil.TIMEZONE_UTC);
   }
   
   @AfterClass
   public static void resetTimeZone() {
       LocaleUtil.setUserTimeZone(userTZ);
   }
   
   @Test
   public void testQuick() throws Exception {
      POIFSFileSystem poifs = new POIFSFileSystem(samples.getFile("quick.msg"), true);
      MAPIMessage msg = new MAPIMessage(poifs);
      
      OutlookTextExtactor ext = new OutlookTextExtactor(msg);
      String text = ext.getText();
      
      assertContains(text, "From: Kevin Roast\n");
      assertContains(text, "To: Kevin Roast <kevin.roast@alfresco.org>\n");
      assertNotContained(text, "CC:");
      assertNotContained(text, "BCC:");
      assertNotContained(text, "Attachment:");
      assertContains(text, "Subject: Test the content transformer\n");
      Calendar cal = LocaleUtil.getLocaleCalendar(2007, 5, 14, 9, 42, 55);
      SimpleDateFormat f = new SimpleDateFormat("E, d MMM yyyy HH:mm:ss Z", Locale.ROOT);
      f.setTimeZone(LocaleUtil.getUserTimeZone());
      String dateText = f.format(cal.getTime());
      assertContains(text, "Date: " + dateText + "\n");
      assertContains(text, "The quick brown fox jumps over the lazy dog");

      ext.close();
      poifs.close();
   }
   
   @Test
   public void testSimple() throws Exception {
      POIFSFileSystem poifs = new POIFSFileSystem(samples.getFile("simple_test_msg.msg"), true);
      MAPIMessage msg = new MAPIMessage(poifs);
      
      OutlookTextExtactor ext = new OutlookTextExtactor(msg);
      String text = ext.getText();
      
      assertContains(text, "From: Travis Ferguson\n");
      assertContains(text, "To: travis@overwrittenstack.com\n");
      assertNotContained(text, "CC:");
      assertNotContained(text, "BCC:");
      assertContains(text, "Subject: test message\n");
      assertContains(text, "Date: Fri, 6 Jul 2007 05:27:17 +0000\n");
      assertContains(text, "This is a test message.");

      ext.close();
      poifs.close();
   }

   @Test
   public void testConstructors() throws Exception {
        FileInputStream fis = new FileInputStream(samples.getFile("simple_test_msg.msg"));
        OutlookTextExtactor ext = new OutlookTextExtactor(fis);
        String inp = ext.getText();
        ext.close();
        fis.close();

        POIFSFileSystem poifs = new POIFSFileSystem(samples.getFile("simple_test_msg.msg"), true);
        ext = new OutlookTextExtactor(poifs);
        String poifsTxt = ext.getText();
        ext.close();
        poifs.close();

        fis = new FileInputStream(samples.getFile("simple_test_msg.msg"));
        ext = new OutlookTextExtactor(new MAPIMessage(fis));
        String mapi = ext.getText();
        ext.close();
        fis.close();

        assertEquals(inp, poifsTxt);
        assertEquals(inp, mapi);
   }
   
   /**
    * Test that we correctly handle multiple To+CC+BCC
    *  recipients in an email we sent.
    */
   @Test
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
         POIFSFileSystem poifs = new POIFSFileSystem(samples.getFile(file), true);
         MAPIMessage msg = new MAPIMessage(poifs);
         
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
         assertContains(text, "Date:");
         assertContains(text, "The quick brown fox jumps over the lazy dog");

         ext.close();
         poifs.close();
      }
   }
   
   /**
    * Test that we correctly handle multiple To+CC
    *  recipients in an email we received.
    */
   @Test
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
          POIFSFileSystem poifs = new POIFSFileSystem(samples.getFile(file), true);
          MAPIMessage msg = new MAPIMessage(poifs);

         
         OutlookTextExtactor ext = new OutlookTextExtactor(msg);
         String text = ext.getText();
         
         assertContains(text, "From: Mike Farman\n");
         assertContains(text, "To: 'Ashutosh Dandavate' <ashutosh.dandavate@alfresco.com>; " +
               "'Paul Holmes-Higgin' <paul.hh@alfresco.com>; 'Mike Farman' <mikef@alfresco.com>\n");
         assertContains(text, "CC: nickb@alfresco.com; " +
               "nick.burch@alfresco.com; 'Roy Wetherall' <roy.wetherall@alfresco.com>\n");
         assertNotContained(text, "BCC:");
         assertContains(text, "Subject: This is a test message please ignore\n");
         assertContains(text, "Date: Mon, 11 Jan 2010 16:2"); // Exact times differ slightly
         assertContains(text, "The quick brown fox jumps over the lazy dog");

         ext.close();
         poifs.close();
      }
   }
   
   /**
    * See also {@link org.apache.poi.extractor.ooxml.TestExtractorFactory#testEmbeded()}
    */
   @SuppressWarnings("JavadocReference")
   @Test
   public void testWithAttachments() throws Exception {
      POIFSFileSystem poifs = new POIFSFileSystem(samples.getFile("attachment_test_msg.msg"), true);
      MAPIMessage msg = new MAPIMessage(poifs);
      OutlookTextExtactor ext = new OutlookTextExtactor(msg);
      
      // Check the normal bits
      String text = ext.getText();
      
      assertContains(text, "From: Nicolas1");
      assertContains(text, "To: 'nicolas1.23456@free.fr'");
      assertNotContained(text, "CC:");
      assertNotContained(text, "BCC:");
      assertContains(text, "Subject: test");
      assertContains(text, "Date: Wed, 22 Apr");
      assertContains(text, "Attachment: test-unicode.doc\n");
      assertContains(text, "Attachment: pj1.txt\n");
      assertContains(text, "contenu");
      
      // Embeded bits are checked in
      //  TestExtractorFactory

      ext.close();
      poifs.close();
   }

    @Test
   public void testWithAttachedMessage() throws Exception {
       POIFSFileSystem poifs = new POIFSFileSystem(samples.getFile("58214_with_attachment.msg"), true);
         MAPIMessage msg = new MAPIMessage(poifs);
         OutlookTextExtactor ext = new OutlookTextExtactor(msg);
         String text = ext.getText();
         
         // Check we got bits from the main message
         assertContains(text, "Master mail");
         assertContains(text, "ante in lacinia euismod");
         
         // But not the attached message
         assertNotContained(text, "Test mail attachment");
         assertNotContained(text, "Lorem ipsum dolor sit");
         
         ext.close();
         poifs.close();
   }

    @Test
   public void testEncodings() throws Exception {
      POIFSFileSystem poifs = new POIFSFileSystem(samples.getFile("chinese-traditional.msg"), true);
      MAPIMessage msg = new MAPIMessage(poifs);
      OutlookTextExtactor ext = new OutlookTextExtactor(msg);
      String text = ext.getText();
      
      // Check the english bits
      assertContains(text, "From: Tests Chang@FT");
      assertContains(text, "tests.chang@fengttt.com");
      
      // And check some chinese bits
      assertContains(text, "(\u5f35\u6bd3\u502b)");
      assertContains(text, "( MSG \u683c\u5f0f\u6e2c\u8a66 )");

      ext.close();
      poifs.close();
   }
}
