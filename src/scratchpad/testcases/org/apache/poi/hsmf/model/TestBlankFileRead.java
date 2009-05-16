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

import java.io.IOException;

import org.apache.poi.hsmf.MAPIMessage;
import org.apache.poi.hsmf.exceptions.ChunkNotFoundException;

import junit.framework.TestCase;


/**
 * Tests to verify that the library can read blank msg files.
 * 
 * @author Travis Ferguson
 *
 */
public class TestBlankFileRead extends TestCase {
	private MAPIMessage mapiMessage;
	
	/**
	 * Initialize this test, load up the blank.msg mapi message.
	 * @throws IOException 
	 */
	public TestBlankFileRead() throws IOException {
		String dirname = System.getProperty("HSMF.testdata.path");
		this.mapiMessage = new MAPIMessage(dirname + "/blank.msg");
	}
	
	/** 
	 * Check if we can read the body of the blank message, we expect "".
	 * 
	 * @throws Exception
	 */
	public void testReadBody() throws Exception {
		try {
			mapiMessage.getTextBody();		
		} catch(ChunkNotFoundException exp) {
			return;
		}
		
		TestCase.fail("Should have thrown a ChunkNotFoundException but didn't");
	}
	

	/**
	 * Test to see if we can read the CC Chunk.
	 * @throws ChunkNotFoundException 
	 * 
	 */
	public void testReadDisplayCC() throws ChunkNotFoundException {
		String obtained = mapiMessage.getDisplayCC();
		String expected = "";
		
		TestCase.assertEquals(expected, obtained);
	}
	
	/**
	 * Test to see if we can read the CC Chunk.
	 * @throws ChunkNotFoundException 
	 * 
	 */
	public void testReadDisplayTo() throws ChunkNotFoundException {
		String obtained = mapiMessage.getDisplayTo();
		String expected = "";
		
		TestCase.assertEquals(expected, obtained);
	}
	
	/**
	 * Test to see if we can read the FROM Chunk.
	 * @throws ChunkNotFoundException 
	 * 
	 */
	public void testReadDisplayFrom() throws ChunkNotFoundException {
		try {
			mapiMessage.getDisplayFrom();		
		} catch(ChunkNotFoundException exp) {
			return;
		}
		
		TestCase.fail("Should have thrown a ChunkNotFoundException but didn't");
	}
	
	/**
	 * Test to see if we can read the CC Chunk.
	 * @throws ChunkNotFoundException 
	 * 
	 */
	public void testReadDisplayBCC() throws ChunkNotFoundException {
		String obtained = mapiMessage.getDisplayBCC();
		String expected = "";
		
		TestCase.assertEquals(expected, obtained);
	}
	
	
	/**
	 * Check if we can read the subject line of the blank message, we expect ""
	 * 
	 * @throws Exception
	 */
	public void testReadSubject() throws Exception {
		String obtained = mapiMessage.getSubject();
		TestCase.assertEquals("", obtained);
	}
	
	
	/**
	 * Check if we can read the subject line of the blank message, we expect ""
	 * 
	 * @throws Exception
	 */
	public void testReadConversationTopic() throws Exception {
		try {
			mapiMessage.getConversationTopic();
		} catch(ChunkNotFoundException exp) {
			return;
		}
		TestCase.fail("We shouldn't have a ConversationTopic node on the blank.msg file.");
	}
	
	
}
