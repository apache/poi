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
   /** BODY Html Chunk, for html messages */
   public StringChunk htmlBodyChunkString;
   public ByteChunk htmlBodyChunkBinary;
   /** BODY Rtf Chunk, for Rtf (Rich) messages */
   public ByteChunk rtfBodyChunk;
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
      if(chunk.getChunkId() == MAPIProperty.MESSAGE_CLASS.id) {
         messageClass = (StringChunk)chunk;
      }
      else if(chunk.getChunkId() == MAPIProperty.INTERNET_MESSAGE_ID.id) {
         messageId = (StringChunk)chunk;
      }
      else if(chunk.getChunkId() == MAPIProperty.MESSAGE_SUBMISSION_ID.id) {
         // TODO - parse
         submissionChunk = (MessageSubmissionChunk)chunk;
      }
      else if(chunk.getChunkId() == MAPIProperty.RECEIVED_BY_ADDRTYPE.id) {
         sentByServerType = (StringChunk)chunk;
      }
      else if(chunk.getChunkId() == MAPIProperty.TRANSPORT_MESSAGE_HEADERS.id) {
         messageHeaders = (StringChunk)chunk;
      }
      
      else if(chunk.getChunkId() == MAPIProperty.CONVERSATION_TOPIC.id) {
         conversationTopic = (StringChunk)chunk;
      }
      else if(chunk.getChunkId() == MAPIProperty.SUBJECT.id) {
         subjectChunk = (StringChunk)chunk;
      }
      else if(chunk.getChunkId() == MAPIProperty.ORIGINAL_SUBJECT.id) {
         // TODO
      }
      
      else if(chunk.getChunkId() == MAPIProperty.DISPLAY_TO.id) {
         displayToChunk = (StringChunk)chunk;
      }
      else if(chunk.getChunkId() == MAPIProperty.DISPLAY_CC.id) {
         displayCCChunk = (StringChunk)chunk;
      }
      else if(chunk.getChunkId() == MAPIProperty.DISPLAY_BCC.id) {
         displayBCCChunk = (StringChunk)chunk;
      }
      
      else if(chunk.getChunkId() == MAPIProperty.SENDER_EMAIL_ADDRESS.id) {
         emailFromChunk = (StringChunk)chunk;
      }
      else if(chunk.getChunkId() == MAPIProperty.SENDER_NAME.id) {
         displayFromChunk = (StringChunk)chunk;
      }
      else if(chunk.getChunkId() == MAPIProperty.BODY.id) {
         textBodyChunk = (StringChunk)chunk;
      }
      else if(chunk.getChunkId() == MAPIProperty.BODY_HTML.id) {
         if(chunk instanceof StringChunk) {
            htmlBodyChunkString = (StringChunk)chunk;
         }
         if(chunk instanceof ByteChunk) {
            htmlBodyChunkBinary = (ByteChunk)chunk;
         }
      }
      else if(chunk.getChunkId() == MAPIProperty.RTF_COMPRESSED.id) {
         rtfBodyChunk = (ByteChunk)chunk;
      }
      
      // And add to the main list
      allChunks.add(chunk);
   }
}
