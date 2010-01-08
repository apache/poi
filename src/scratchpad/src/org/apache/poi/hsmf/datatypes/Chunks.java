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
 * Not all of these will be present in any given file
 */
public final class Chunks implements ChunkGroup {
   /* String parts of Outlook Messages that are currently known */
   public static final int MESSAGE_CLASS       = 0x001A;
   public static final int SUBJECT             = 0x0037;
   public static final int DATE                = 0x0047;
   public static final int CONVERSATION_TOPIC  = 0x0070;
   public static final int SENT_BY_SERVER_TYPE = 0x0075;
   // RECEIVEDEMAIL = 76
   public static final int DISPLAY_TO          = 0x0E04;
   public static final int DISPLAY_FROM        = 0x0C1A;
   public static final int EMAIL_FROM          = 0x0C1F;
   public static final int DISPLAY_CC          = 0x0E03;
   public static final int DISPLAY_BCC         = 0x0E02;
   public static final int TEXT_BODY           = 0x1000;
   
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
   /** TODO */
   public ByteChunk dateChunk; 
   /** TODO */
   public StringChunk emailFromChunk; 

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
      switch(chunk.getChunkId()) {
      case MESSAGE_CLASS:
         messageClass = (StringChunk)chunk;
         break;
      case SUBJECT:
         subjectChunk = (StringChunk)chunk;
         break;
      case DATE:
         dateChunk = (ByteChunk)chunk;
         break;
      case CONVERSATION_TOPIC:
         conversationTopic = (StringChunk)chunk;
         break;
      case SENT_BY_SERVER_TYPE:
         sentByServerType = (StringChunk)chunk;
         break;
      case DISPLAY_TO:
         displayToChunk = (StringChunk)chunk;
         break;
      case DISPLAY_FROM:
         displayFromChunk = (StringChunk)chunk;
         break;
      case EMAIL_FROM:
         emailFromChunk = (StringChunk)chunk;
         break;
      case DISPLAY_CC:
         displayCCChunk = (StringChunk)chunk;
         break;
      case DISPLAY_BCC:
         displayBCCChunk = (StringChunk)chunk;
         break;
      case TEXT_BODY:
         textBodyChunk = (StringChunk)chunk;
         break;
      }

      // And add to the main list
      allChunks.add(chunk);
   }
}
