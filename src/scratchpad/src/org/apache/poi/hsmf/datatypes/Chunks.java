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

package org.apache.poi.hsmf.datatypes;


/**
 * Collection of convenence chunks for standard parts of the MSG file.
 *
 * @author Travis Ferguson
 */
public final class Chunks {
	/* String parts of Outlook Messages that are currently known */

	/** Type of message that the MSG represents (ie. IPM.Note) */
	public StringChunk messageClass;
	/** BODY Chunk, for plain/text messages */
	public StringChunk textBodyChunk;
	/** Subject link chunk, in plain/text */
	public StringChunk subjectChunk;
	/** Value that is in the TO field (not actually the addresses as they are stored in recip directory nodes */
	public StringChunk displayToChunk;
	/** Value that is in the FROM field */
	public StringChunk displayFromChunk;
	/** value that shows in the CC field */
	public StringChunk displayCCChunk;
	/** Value that shows in the BCC field */
	public StringChunk displayBCCChunk;
	/** Sort of like the subject line, but without the RE: and FWD: parts. */
	public StringChunk conversationTopic;
	/** Type of server that the message originated from (SMTP, etc). */
	public StringChunk sentByServerType;

	private Chunks(boolean newStringType) {
		messageClass = new StringChunk(0x001A, newStringType);
		textBodyChunk = new StringChunk(0x1000, newStringType);
		subjectChunk = new StringChunk(0x0037, newStringType);
		displayToChunk = new StringChunk(0x0E04, newStringType);
		displayFromChunk = new StringChunk(0x0C1A, newStringType);
		displayCCChunk = new StringChunk(0x0E03, newStringType);
		displayBCCChunk = new StringChunk(0x0E02, newStringType);
		conversationTopic = new StringChunk(0x0070, newStringType);
		sentByServerType = new StringChunk(0x0075, newStringType);
	}

	public static Chunks getInstance(boolean newStringType) {
		return new Chunks(newStringType);
	}
}
