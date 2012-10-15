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

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import junit.framework.TestCase;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hsmf.dev.HSMFDump;
import org.apache.poi.hsmf.extractor.OutlookTextExtactor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * Tests that we can read fixed sized properties, as well as variable
 *  ones, for example Submission Dates
 */
public final class TestFixedSizedProperties extends TestCase {
	protected static final String messageSucceeds = "53784_succeeds.msg";
	protected static final String messageFails = "53784_fails.msg";
	private MAPIMessage mapiMessageSucceeds;
	private MAPIMessage mapiMessageFails;
	private POIFSFileSystem fsMessageSucceeds;
	private POIFSFileSystem fsMessageFails;	

	/**
	 * Initialize this test, load up the messages.
	 * 
	 * @throws Exception
	 */
	public TestFixedSizedProperties() throws Exception {
		POIDataSamples samples = POIDataSamples.getHSMFInstance();
		this.mapiMessageSucceeds = new MAPIMessage(
				samples.openResourceAsStream(messageSucceeds));
		this.mapiMessageFails = new MAPIMessage(
				samples.openResourceAsStream(messageFails));		
		this.fsMessageSucceeds = new POIFSFileSystem(new FileInputStream(samples.getFile(messageSucceeds)));
		this.fsMessageFails = new POIFSFileSystem(new FileInputStream(samples.getFile(messageFails)));
	}

	/**
	 * Test to see if we can read the Date Chunk with OutlookTextExtractor.
	 * TODO Work out why the Fri 22nd vs Monday 25th problem is occurring and fix
	 */
	public void DISABLEDtestReadMessageDateSucceedsWithOutlookTextExtractor() {
		OutlookTextExtactor ext = new OutlookTextExtactor(mapiMessageSucceeds);
		String text = ext.getText();

		assertContains(text, "Date: Fri, 22 Jun 2012 21:32:54\n");
	}

	/**
	 * Test to see if we can read the Date Chunk with OutlookTextExtractor.
    * TODO Work out why the Thu 21st vs Monday 25th problem is occurring and fix
	 */
	public void DISABLEDtestReadMessageDateFailsWithOutlookTextExtractor() {
		OutlookTextExtactor ext = new OutlookTextExtactor(mapiMessageFails);
		String text = ext.getText();

		assertContains(text, "Date: Thu, 21 Jun 2012 17:14:04\n");
	}
	
   /**
    * Test to see if we can read the Date Chunk with HSMFDump.
    * @throws IOException 
    */
   public void testReadMessageDateSucceedsWithHSMFDump() throws IOException {
      PrintStream stream = new PrintStream(new ByteArrayOutputStream());
      HSMFDump dump = new HSMFDump(fsMessageSucceeds);
      dump.dump(stream);
   }	
	
   /**
    * Test to see if we can read the Date Chunk with HSMFDump.
    * @throws Exception 
    */
   public void testReadMessageDateFailsWithHSMFDump() throws Exception {
      PrintStream stream = new PrintStream(new ByteArrayOutputStream());
      HSMFDump dump = new HSMFDump(fsMessageFails);
      dump.dump(stream);
   }
	
   /**
    * TODO Work out why the Fri 22nd vs Monday 25th problem is occurring and fix
    */
	public void DISABLEDtestClientSubmitTime() throws Exception {
	   SimpleDateFormat f = new SimpleDateFormat("E, d MMM yyyy HH:mm:ss");
	   f.setTimeZone(TimeZone.getTimeZone("GMT"));

	   Calendar clientSubmitTime = mapiMessageSucceeds.getMessageDate();
	   assertEquals("Fri, 22 Jun 2012 18:32:54", f.format(clientSubmitTime.getTime()));
	}

	private static void assertContains(String haystack, String needle) {
      if (haystack.indexOf(needle) > -1) {
         return;
      }
      fail("'" + needle + "' wasn't found in '" + haystack + "'");
   }
}
