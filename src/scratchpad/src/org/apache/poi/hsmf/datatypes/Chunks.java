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

import java.util.ArrayList;
import java.util.List;


/**
 * Collection of convenience chunks for standard parts of the MSG file.
 * 
 * Not all of these will be present in any given file.
 * 
 * A partial list is available at:
 *  http://msdn.microsoft.com/en-us/library/ms526356%28v=exchg.10%29.aspx
 */
public final class Chunks implements ChunkGroup {
   /** Holds all the chunks that were found. */
   private List<Chunk> allChunks = new ArrayList<Chunk>();
   
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
   /** The email headers */
   public StringChunk messageHeaders;
   /** TODO */
   public MessageSubmissionChunk submissionChunk; 
   /** TODO */
   public StringChunk emailFromChunk; 
   /** The message ID */
   public StringChunk messageId;

   public Chunk[] getAll() {
      return allChunks.toArray(new Chunk[allChunks.size()]);
   }
   public Chunk[] getChunks() {
      return getAll();
   }
	
   /**
    * Called by the parser whenever a chunk is found.
    */
   public void record(Chunk chunk) {
      if(chunk.getChunkId() == MAPIAttribute.MESSAGE_CLASS.id) {
         messageClass = (StringChunk)chunk;
      }
      else if(chunk.getChunkId() == MAPIAttribute.INTERNET_MESSAGE_ID.id) {
         messageId = (StringChunk)chunk;
      }
      else if(chunk.getChunkId() == MAPIAttribute.MESSAGE_SUBMISSION_ID.id) {
         // TODO - parse
         submissionChunk = (MessageSubmissionChunk)chunk;
      }
      else if(chunk.getChunkId() == MAPIAttribute.RECEIVED_BY_ADDRTYPE.id) {
         sentByServerType = (StringChunk)chunk;
      }
      else if(chunk.getChunkId() == MAPIAttribute.TRANSPORT_MESSAGE_HEADERS.id) {
         messageHeaders = (StringChunk)chunk;
      }
      
      else if(chunk.getChunkId() == MAPIAttribute.CONVERSATION_TOPIC.id) {
         conversationTopic = (StringChunk)chunk;
      }
      else if(chunk.getChunkId() == MAPIAttribute.SUBJECT.id) {
         subjectChunk = (StringChunk)chunk;
      }
      else if(chunk.getChunkId() == MAPIAttribute.ORIGINAL_SUBJECT.id) {
         // TODO
      }
      
      else if(chunk.getChunkId() == MAPIAttribute.DISPLAY_TO.id) {
         displayToChunk = (StringChunk)chunk;
      }
      else if(chunk.getChunkId() == MAPIAttribute.DISPLAY_CC.id) {
         displayCCChunk = (StringChunk)chunk;
      }
      else if(chunk.getChunkId() == MAPIAttribute.DISPLAY_BCC.id) {
         displayBCCChunk = (StringChunk)chunk;
      }
      
      else if(chunk.getChunkId() == MAPIAttribute.SENDER_EMAIL_ADDRESS.id) {
         emailFromChunk = (StringChunk)chunk;
      }
      else if(chunk.getChunkId() == MAPIAttribute.SENDER_NAME.id) {
         displayFromChunk = (StringChunk)chunk;
      }
      else if(chunk.getChunkId() == MAPIAttribute.BODY.id) {
         textBodyChunk = (StringChunk)chunk;
      }
      
      // And add to the main list
      allChunks.add(chunk);
   }
}
