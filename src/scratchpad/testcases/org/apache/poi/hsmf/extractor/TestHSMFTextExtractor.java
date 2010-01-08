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

import junit.framework.TestCase;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hsmf.MAPIMessage;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * Tests to verify that the text extractor works
 */
public final class TestHSMFTextExtractor extends TestCase {
   private POIDataSamples samples;

	public TestHSMFTextExtractor() throws IOException {
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
      
      HSMFTextExtactor ext = new HSMFTextExtactor(msg);
      String text = ext.getText();
      
      assertContains(text, "From: Kevin Roast\n");
      assertContains(text, "To: Kevin Roast\n");
      assertEquals(-1, text.indexOf("CC:"));
      assertEquals(-1, text.indexOf("BCC:"));
      assertContains(text, "Subject: Test the content transformer\n");
      assertContains(text, "Date: Thu, 14 Jun 2007 09:42:55\n");
      assertContains(text, "The quick brown fox jumps over the lazy dog");
   }
   
   public void testSimple() throws Exception {
      MAPIMessage msg = new MAPIMessage(new POIFSFileSystem(
            new FileInputStream(samples.getFile("simple_test_msg.msg"))
      ));
      
      HSMFTextExtactor ext = new HSMFTextExtactor(msg);
      String text = ext.getText();
      
      assertContains(text, "From: Travis Ferguson\n");
      assertContains(text, "To: travis@overwrittenstack.com\n");
      assertEquals(-1, text.indexOf("CC:"));
      assertEquals(-1, text.indexOf("BCC:"));
      assertContains(text, "Subject: test message\n");
      assertEquals(-1, text.indexOf("Date:"));
      assertContains(text, "This is a test message.");
   }

   public void testConstructors() throws Exception {
      String inp = (new HSMFTextExtactor(new FileInputStream(
            samples.getFile("simple_test_msg.msg")
      )).getText());
      String poifs = (new HSMFTextExtactor(new POIFSFileSystem(new FileInputStream(
            samples.getFile("simple_test_msg.msg")
      ))).getText());
      String mapi = (new HSMFTextExtactor(new MAPIMessage(new FileInputStream(
            samples.getFile("simple_test_msg.msg")
      ))).getText());
      
      assertEquals(inp, poifs);
      assertEquals(inp, mapi);
   }
}
