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
 * Collection of convenience chunks for the
 *  Recip(ient) part of an outlook file
 */
public final class RecipientChunks implements ChunkGroup {
   public static final String PREFIX = "__recip_version1.0_#";
   
   public static final int RECIPIENT_NAME   = 0x3001;
   public static final int DELIVERY_TYPE    = 0x3002;
   public static final int RECIPIENT_SEARCH = 0x300B;
   public static final int RECIPIENT_EMAIL  = 0x39FE;
   
   /** TODO */
   public ByteChunk recipientSearchChunk;
   /**
    * The "name", which could be their name if an
    *  internal person, or their email address
    *  if an external person
    */
   public StringChunk recipientNameChunk;
   /** 
    * The email address of the recipient, but
    *  isn't always present...
    */
   public StringChunk recipientEmailChunk;
   /**
    * Normally EX or SMTP. Will generally affect
    *  where the email address ends up.
    */
   public StringChunk deliveryTypeChunk;
   
   
   /**
    * Tries to find their email address, in
    *  whichever chunk holds it given the
    *  delivery type.
    */
   public String getRecipientEmailAddress() {
      if(recipientEmailChunk != null) {
         return recipientEmailChunk.getValue();
      }
      // Probably in the name field
      if(recipientNameChunk != null) {
         String name = recipientNameChunk.getValue();
         if(name.indexOf('@') > -1) {
            // Strip leading and trailing quotes if needed
            if(name.startsWith("'") && name.endsWith("'")) {
               return name.substring(1, name.length()-1);
            }
            return name;
         }
      }
      // Check the search chunk
      if(recipientSearchChunk != null) {
         String search = recipientSearchChunk.getAs7bitString();
         if(search.indexOf("SMTP:") != -1) {
            return search.substring(search.indexOf("SMTP:") + 5);
         }
      }
      // Can't find it
      return null;
   }
   
   /** Holds all the chunks that were found. */
   private List<Chunk> allChunks = new ArrayList<Chunk>();

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
      case RECIPIENT_SEARCH:
         // TODO - parse
         recipientSearchChunk = (ByteChunk)chunk;
         break;
      case RECIPIENT_NAME:
         recipientNameChunk = (StringChunk)chunk;
         break;
      case RECIPIENT_EMAIL:
         recipientEmailChunk = (StringChunk)chunk;
         break;
      case DELIVERY_TYPE:
         deliveryTypeChunk = (StringChunk)chunk;
         break;
      }

      // And add to the main list
      allChunks.add(chunk);
   }
}
