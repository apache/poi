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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.poi.hsmf.datatypes.Chunk;
import org.apache.poi.hsmf.datatypes.Chunks;
import org.apache.poi.hsmf.datatypes.StringChunk;
import org.apache.poi.hsmf.exceptions.ChunkNotFoundException;
import org.apache.poi.hsmf.parsers.POIFSChunkParser;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * Reads an Outlook MSG File in and provides hooks into its data structure.
 *
 * @author Travis Ferguson
 */
public class MAPIMessage {
	private POIFSChunkParser chunkParser;
	private POIFSFileSystem fs;
	private Chunks chunks;

	/**
	 * Constructor for creating new files.
	 *
	 */
	public MAPIMessage() {
		//TODO make writing possible
	}


	/**
	 * Constructor for reading MSG Files from the file system.
	 * @param filename
	 * @throws IOException
	 */
	public MAPIMessage(String filename) throws IOException {
		this(new FileInputStream(new File(filename)));
	}

	/**
	 * Constructor for reading MSG Files from an input stream.
	 * @param in
	 * @throws IOException
	 */
	public MAPIMessage(InputStream in) throws IOException {
		this.fs = new POIFSFileSystem(in);
		chunkParser = new POIFSChunkParser(this.fs);

		// Figure out the right string type, based on
		//  the chunks present
		chunks = chunkParser.identifyChunks();
	}


	/**
	 * Gets a string value based on the passed chunk.
	 * @param chunk
	 * @throws ChunkNotFoundException
	 */
	public String getStringFromChunk(StringChunk chunk) throws ChunkNotFoundException {
		Chunk out = this.chunkParser.getDocumentNode(chunk);
		StringChunk strchunk = (StringChunk)out;
		return strchunk.toString();
	}


	/**
	 * Gets the plain text body of this Outlook Message
	 * @return The string representation of the 'text' version of the body, if available.
	 * @throws IOException
	 * @throws ChunkNotFoundException
	 */
	public String getTextBody() throws ChunkNotFoundException {
		return getStringFromChunk(chunks.textBodyChunk);
	}

	/**
	 * Gets the subject line of the Outlook Message
	 * @throws ChunkNotFoundException
	 */
	public String getSubject() throws ChunkNotFoundException {
		return getStringFromChunk(chunks.subjectChunk);
	}


	/**
	 * Gets the display value of the "TO" line of the outlook message
	 * This is not the actual list of addresses/values that will be sent to if you click Reply in the email.
	 * @throws ChunkNotFoundException
	 */
	public String getDisplayTo() throws ChunkNotFoundException {
		return getStringFromChunk(chunks.displayToChunk);
	}

	/**
	 * Gets the display value of the "FROM" line of the outlook message
	 * This is not the actual address that was sent from but the formated display of the user name.
	 * @throws ChunkNotFoundException
	 */
	public String getDisplayFrom() throws ChunkNotFoundException {
		return getStringFromChunk(chunks.displayFromChunk);
	}

	/**
	 * Gets the display value of the "TO" line of the outlook message
	 * This is not the actual list of addresses/values that will be sent to if you click Reply in the email.
	 * @throws ChunkNotFoundException
	 */
	public String getDisplayCC() throws ChunkNotFoundException {
		return getStringFromChunk(chunks.displayCCChunk);
	}

	/**
	 * Gets the display value of the "TO" line of the outlook message
	 * This is not the actual list of addresses/values that will be sent to if you click Reply in the email.
	 * @throws ChunkNotFoundException
	 */
	public String getDisplayBCC() throws ChunkNotFoundException {
		return getStringFromChunk(chunks.displayBCCChunk);
	}


	/**
	 * Gets the conversation topic of the parsed Outlook Message.
	 * This is the part of the subject line that is after the RE: and FWD:
	 * @throws ChunkNotFoundException
	 */
	public String getConversationTopic() throws ChunkNotFoundException {
		return getStringFromChunk(chunks.conversationTopic);
	}

	/**
	 * Gets the message class of the parsed Outlook Message.
	 * (Yes, you can use this to determine if a message is a calendar item, note, or actual outlook Message)
	 * For emails the class will be IPM.Note
	 *
	 * @throws ChunkNotFoundException
	 */
	public String getMessageClass() throws ChunkNotFoundException {
		return getStringFromChunk(chunks.messageClass);
	}

	/**
	 * Gets the message attachments.
	 *
	 * @return a map containing attachment name (String) and data (ByteArrayInputStream)
	 */
	public Map getAttachmentFiles() {
		return this.chunkParser.getAttachmentList();
	}
}
