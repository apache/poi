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

package org.apache.poi.hsmf.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.poi.hsmf.MAPIMessage;
import org.apache.poi.hsmf.datatypes.AttachmentChunks;
import org.apache.poi.hsmf.exceptions.ChunkNotFoundException;
import org.apache.poi.POIDataSamples;

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
	// public void testReadDisplayCC() throws ChunkNotFoundException {
	public void testRetrieveAttachments() {
		AttachmentChunks[] attachments = mapiMessage.getAttachmentFiles();
		int obtained = attachments.length;
		int expected = 2;

		TestCase.assertEquals(obtained, expected);
	}

	/**
	 * Test to see if attachments are not empty.
	 * 
	 * @throws ChunkNotFoundException
	 * 
	 */
	public void testReadAttachments() throws IOException {
      AttachmentChunks[] attachments = mapiMessage.getAttachmentFiles();

		for (AttachmentChunks attachment : attachments) {
		   assertTrue(attachment.attachFileName.getValue().length() > 0);
         assertTrue(attachment.attachLongFileName.getValue().length() > 0);
         assertTrue(attachment.attachExtension.getValue().length() > 0);
         assertTrue(attachment.attachMimeTag.getValue().length() > 0);
		}
		
		// TODO better checking
	}

}
