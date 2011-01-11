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
import java.util.Comparator;
import java.util.List;


/**
 * Collection of convenience chunks for the
 *  Recip(ient) part of an outlook file.
 * 
 * If a message has multiple recipients, there will be
 *  several of these.
 */
public final class RecipientChunks implements ChunkGroup {
   public static final String PREFIX = "__recip_version1.0_#";
   
   public static final MAPIAttribute RECIPIENT_NAME   = MAPIAttribute.DISPLAY_NAME;
   public static final MAPIAttribute DELIVERY_TYPE    = MAPIAttribute.ADDRTYPE;
   public static final MAPIAttribute RECIPIENT_EMAIL_ADDRESS = MAPIAttribute.EMAIL_ADDRESS;
   public static final MAPIAttribute RECIPIENT_SEARCH        = MAPIAttribute.SEARCH_KEY;
   public static final MAPIAttribute RECIPIENT_SMTP_ADDRESS  = MAPIAttribute.SMTP_ADDRESS;
   public static final MAPIAttribute RECIPIENT_DISPLAY_NAME  = MAPIAttribute.RECIPIENT_DISPLAY_NAME;
   
   /** Our 0 based position in the list of recipients */
   public int recipientNumber;
   
   /** TODO */
   public ByteChunk recipientSearchChunk;
   /**
    * The "name", which could be their name if an
    *  internal person, or their email address
    *  if an external person
    */
   public StringChunk recipientNameChunk;
   /** 
    * The email address of the recipient, which
    *  could be in SMTP or SEARCH format, but
    *  isn't always present...
    */
   public StringChunk recipientEmailChunk;
   /** 
    * The smtp destination email address of
    *  the recipient, but isn't always present...
    */
   public StringChunk recipientSMTPChunk;
   /**
    * Normally EX or SMTP. Will generally affect
    *  where the email address ends up.
    */
   public StringChunk deliveryTypeChunk;
   /**
    * The display name of the recipient.
    * Normally seems to hold the same value
    *  as in recipientNameChunk
    */
   public StringChunk recipientDisplayNameChunk;
   
   
   public RecipientChunks(String name) {
      recipientNumber = -1;
      int splitAt = name.lastIndexOf('#');
      if(splitAt > -1) {
         String number = name.substring(splitAt+1);
         try {
            recipientNumber = Integer.parseInt(number, 16);
         } catch(NumberFormatException e) {
            System.err.println("Invalid recipient number in name " + name);
         }
      }
   }
   
   /**
    * Tries to find their name,
    *  in whichever chunk holds it.
    */
   public String getRecipientName() {
      if(recipientNameChunk != null) {
         return recipientNameChunk.getValue();
      }
      if(recipientDisplayNameChunk != null) {
         return recipientDisplayNameChunk.getValue();
      }
      
      // Can't find it
      return null;
   }
   
   /**
    * Tries to find their email address, in
    *  whichever chunk holds it given the
    *  delivery type.
    */
   public String getRecipientEmailAddress() {
      // If we have this, it really has the email 
      if(recipientSMTPChunk != null) {
         return recipientSMTPChunk.getValue();
      }
      
      // This might be a real email, or might be
      //  in CN=... format
      if(recipientEmailChunk != null) {
         String email = recipientEmailChunk.getValue();
         int cne = email.indexOf("/CN="); 
         if(cne == -1) {
            // Normal smtp address
            return email;
         } else {
            // /O=..../CN=em@ail
            return email.substring(cne+4);
         }
      }
      
      // Might be in the name field, check there
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
      
      // Check the search chunk, see if it's 
      //  encoded as a SMTP destination in there.
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
      if(chunk.getChunkId() == RECIPIENT_SEARCH.id) {
         // TODO - parse
         recipientSearchChunk = (ByteChunk)chunk;
      }
      else if(chunk.getChunkId() == RECIPIENT_NAME.id) {
         recipientDisplayNameChunk = (StringChunk)chunk;
      }
      else if(chunk.getChunkId() == RECIPIENT_DISPLAY_NAME.id) {
         recipientNameChunk = (StringChunk)chunk;
      }
      else if(chunk.getChunkId() == RECIPIENT_EMAIL_ADDRESS.id) {
         recipientEmailChunk = (StringChunk)chunk;
      }
      else if(chunk.getChunkId() == RECIPIENT_SMTP_ADDRESS.id) {
         recipientSMTPChunk = (StringChunk)chunk;
      }
      else if(chunk.getChunkId() == DELIVERY_TYPE.id) {
         deliveryTypeChunk = (StringChunk)chunk;
      }

      // And add to the main list
      allChunks.add(chunk);
   }
   
   /**
    * Orders by the recipient number.
    */
   public static class RecipientChunksSorter implements Comparator<RecipientChunks> {
      public int compare(RecipientChunks a, RecipientChunks b) {
         if(a.recipientNumber < b.recipientNumber)
            return -1;
         if(a.recipientNumber > b.recipientNumber)
            return +1;
         return 0;
      }
   }
}
