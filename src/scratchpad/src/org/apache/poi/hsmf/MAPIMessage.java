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
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.POIDocument;
import org.apache.poi.hsmf.datatypes.AttachmentChunks;
import org.apache.poi.hsmf.datatypes.AttachmentChunks.AttachmentChunksSorter;
import org.apache.poi.hsmf.datatypes.Chunk;
import org.apache.poi.hsmf.datatypes.ChunkGroup;
import org.apache.poi.hsmf.datatypes.Chunks;
import org.apache.poi.hsmf.datatypes.NameIdChunks;
import org.apache.poi.hsmf.datatypes.RecipientChunks;
import org.apache.poi.hsmf.datatypes.RecipientChunks.RecipientChunksSorter;
import org.apache.poi.hsmf.datatypes.StringChunk;
import org.apache.poi.hsmf.exceptions.ChunkNotFoundException;
import org.apache.poi.hsmf.parsers.POIFSChunkParser;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * Reads an Outlook MSG File in and provides hooks into its data structure.
 * 
 * If you want to develop with HSMF, you might find it worth getting
 *  some of the microsoft public documentation, such as:
 *  
 * [MS-OXCMSG]: Message and Attachment Object Protocol Specification
 */
public class MAPIMessage extends POIDocument {
   private Chunks mainChunks;
   private NameIdChunks nameIdChunks;
   private RecipientChunks[] recipientChunks;
   private AttachmentChunks[] attachmentChunks;

   private boolean returnNullOnMissingChunk = false;

   /**
    * Constructor for creating new files.
    *
    */
   public MAPIMessage() {
      // TODO - make writing possible
      super(new POIFSFileSystem());
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
      this(new POIFSFileSystem(in));
   }
   /**
    * Constructor for reading MSG Files from a POIFS filesystem
    * @param fs
    * @throws IOException
    */
   public MAPIMessage(POIFSFileSystem fs) throws IOException {
      this(fs.getRoot(), fs);
   }
   /**
    * Constructor for reading MSG Files from a certain
    *  point within a POIFS filesystem
    * @param poifsDir
    * @param fs
    * @throws IOException
    */
   public MAPIMessage(DirectoryNode poifsDir, POIFSFileSystem fs) throws IOException {
      super(poifsDir, fs);

      // Grab all the chunks
      ChunkGroup[] chunkGroups = POIFSChunkParser.parse(poifsDir);

      // Grab interesting bits
      ArrayList<AttachmentChunks> attachments = new ArrayList<AttachmentChunks>();
      ArrayList<RecipientChunks>  recipients  = new ArrayList<RecipientChunks>();
      for(ChunkGroup group : chunkGroups) {
         // Should only ever be one of these
         if(group instanceof Chunks) {
            mainChunks = (Chunks)group;
         } else if(group instanceof NameIdChunks) {
            nameIdChunks = (NameIdChunks)group;
         } else if(group instanceof RecipientChunks) {
            recipients.add( (RecipientChunks)group );
         }

         // Add to list(s)
         if(group instanceof AttachmentChunks) {
            attachments.add( (AttachmentChunks)group );
         }
      }
      attachmentChunks = attachments.toArray(new AttachmentChunks[attachments.size()]);
      recipientChunks  = recipients.toArray(new RecipientChunks[recipients.size()]);

      // Now sort these chunks lists so they're in ascending order,
      //  rather than in random filesystem order
      Arrays.sort(attachmentChunks, new AttachmentChunksSorter());
      Arrays.sort(recipientChunks, new RecipientChunksSorter());
   }


   /**
    * Gets a string value based on the passed chunk.
    * @throws ChunkNotFoundException if the chunk isn't there
    */
   public String getStringFromChunk(StringChunk chunk) throws ChunkNotFoundException {
      if(chunk == null) {
         if(returnNullOnMissingChunk) {
            return null;
         } else {
            throw new ChunkNotFoundException();
         }
      }
      return chunk.getValue();
   }


   /**
    * Gets the plain text body of this Outlook Message
    * @return The string representation of the 'text' version of the body, if available.
    * @throws ChunkNotFoundException
    */
   public String getTextBody() throws ChunkNotFoundException {
      return getStringFromChunk(mainChunks.textBodyChunk);
   }

   /**
    * Gets the subject line of the Outlook Message
    * @throws ChunkNotFoundException
    */
   public String getSubject() throws ChunkNotFoundException {
      return getStringFromChunk(mainChunks.subjectChunk);
   }

   /**
    * Gets the display value of the "FROM" line of the outlook message
    * This is not the actual address that was sent from but the formated display of the user name.
    * @throws ChunkNotFoundException
    */
   public String getDisplayFrom() throws ChunkNotFoundException {
      return getStringFromChunk(mainChunks.displayFromChunk);
   }

   /**
    * Gets the display value of the "TO" line of the outlook message.
    * If there are multiple recipients, they will be separated
    *  by semicolons. 
    * This is not the actual list of addresses/values that will be 
    *  sent to if you click Reply in the email - those are stored
    *  in {@link RecipientChunks}.
    * @throws ChunkNotFoundException
    */
   public String getDisplayTo() throws ChunkNotFoundException {
      return getStringFromChunk(mainChunks.displayToChunk);
   }

   /**
    * Gets the display value of the "CC" line of the outlook message.
    * If there are multiple recipients, they will be separated
    *  by semicolons. 
    * This is not the actual list of addresses/values that will be 
    *  sent to if you click Reply in the email - those are stored
    *  in {@link RecipientChunks}.
    * @throws ChunkNotFoundException
    */
   public String getDisplayCC() throws ChunkNotFoundException {
      return getStringFromChunk(mainChunks.displayCCChunk);
   }

   /**
    * Gets the display value of the "BCC" line of the outlook message.
    * If there are multiple recipients, they will be separated
    *  by semicolons. 
    * This is not the actual list of addresses/values that will be 
    *  sent to if you click Reply in the email - those are stored
    *  in {@link RecipientChunks}.
    * This will only be present in sent emails, not received ones!
    * @throws ChunkNotFoundException
    */
   public String getDisplayBCC() throws ChunkNotFoundException {
      return getStringFromChunk(mainChunks.displayBCCChunk);
   }

   /**
    * Returns all the recipients' email address, separated by
    *  semicolons. Checks all the likely chunks in search of 
    *  the addresses.
    */
   public String getRecipientEmailAddress() throws ChunkNotFoundException {
      return toSemicolonList(getRecipientEmailAddressList());
   }
   /**
    * Returns an array of all the recipient's email address, normally
    *  in TO then CC then BCC order.
    * Checks all the likely chunks in search of the addresses. 
    */
   public String[] getRecipientEmailAddressList() throws ChunkNotFoundException {
      if(recipientChunks == null || recipientChunks.length == 0) {
         throw new ChunkNotFoundException("No recipients section present");
      }

      String[] emails = new String[recipientChunks.length];
      for(int i=0; i<emails.length; i++) {
         RecipientChunks rc = recipientChunks[i];
         String email = rc.getRecipientEmailAddress();
         if(email != null) {
            emails[i] = email;
         } else {
            if(returnNullOnMissingChunk) {
               emails[i] = null;
            } else {
               throw new ChunkNotFoundException("No email address holding chunks found for the " + (i+1) + "th recipient");
            }
         }
      }

      return emails;
   }


   /**
    * Returns all the recipients' names, separated by
    *  semicolons. Checks all the likely chunks in search of 
    *  the names.
    * See also {@link #getDisplayTo()}, {@link #getDisplayCC()}
    *  and {@link #getDisplayBCC()}.
    */
   public String getRecipientNames() throws ChunkNotFoundException {
      return toSemicolonList(getRecipientNamesList());
   }
   /**
    * Returns an array of all the recipient's names, normally
    *  in TO then CC then BCC order.
    * Checks all the likely chunks in search of the names. 
    * See also {@link #getDisplayTo()}, {@link #getDisplayCC()}
    *  and {@link #getDisplayBCC()}.
    */
   public String[] getRecipientNamesList() throws ChunkNotFoundException {
      if(recipientChunks == null || recipientChunks.length == 0) {
         throw new ChunkNotFoundException("No recipients section present");
      }

      String[] names = new String[recipientChunks.length];
      for(int i=0; i<names.length; i++) {
         RecipientChunks rc = recipientChunks[i];
         String name = rc.getRecipientName();
         if(name != null) {
            names[i] = name;
         } else {
            throw new ChunkNotFoundException("No display name holding chunks found for the " + (i+1) + "th recipient");
         }
      }

      return names;
   }
   
   /**
    * Many messages store their strings as unicode, which is
    *  nice and easy. Some use one-byte encodings for their
    *  strings, but don't easily store the encoding anywhere
    *  in the file!
    * This method looks at the headers for the message, and
    *  tries to use these to guess the correct encoding for
    *  your file.
    * Bug #49441 has more on why this is needed
    */
   public void guess7BitEncoding() {
      try {
         String[] headers = getHeaders();
         if(headers == null || headers.length == 0) {
            return;
         }

         // Look for a content type with a charset
         Pattern p = Pattern.compile("Content-Type:.*?charset=[\"']?(.*?)[\"']?");
         for(String header : headers) {
            if(header.startsWith("Content-Type")) {
               Matcher m = p.matcher(header);
               if(m.matches()) {
                  // Found it! Tell all the string chunks
                  String charset = m.group(1);
                  
                  for(Chunk c : mainChunks.getAll()) {
                     if(c instanceof StringChunk) {
                        ((StringChunk)c).set7BitEncoding(charset);
                     }
                  }
                  for(Chunk c : nameIdChunks.getAll()) {
                     if(c instanceof StringChunk) {
                        ((StringChunk)c).set7BitEncoding(charset);
                     }
                  }
                  for(RecipientChunks rc : recipientChunks) {
                     for(Chunk c : rc.getAll()) {
                        if(c instanceof StringChunk) {
                           ((StringChunk)c).set7BitEncoding(charset);
                        }
                     }
                  }
               }
            }
         }
      } catch(ChunkNotFoundException e) {}
   }
   
   /**
    * Returns all the headers, one entry per line
    */
   public String[] getHeaders() throws ChunkNotFoundException {
      String headers = getStringFromChunk(mainChunks.messageHeaders);
      if(headers == null) {
         return null;
      }
      return headers.split("\\r?\\n");
   }

   /**
    * Gets the conversation topic of the parsed Outlook Message.
    * This is the part of the subject line that is after the RE: and FWD:
    * @throws ChunkNotFoundException
    */
   public String getConversationTopic() throws ChunkNotFoundException {
      return getStringFromChunk(mainChunks.conversationTopic);
   }

   /**
    * Gets the message class of the parsed Outlook Message.
    * (Yes, you can use this to determine if a message is a calendar 
    *  item, note, or actual outlook Message)
    * For emails the class will be IPM.Note
    *
    * @throws ChunkNotFoundException
    */
   public String getMessageClass() throws ChunkNotFoundException {
      return getStringFromChunk(mainChunks.messageClass);
   }

   /**
    * Gets the date that the message was accepted by the
    *  server on.
    */
   public Calendar getMessageDate() throws ChunkNotFoundException {
      if(mainChunks.submissionChunk != null) {
         return mainChunks.submissionChunk.getAcceptedAtTime();
      }
      if(returnNullOnMissingChunk)
         return null;
      throw new ChunkNotFoundException();
   }


   /**
    * Gets the main, core details chunks
    */
   public Chunks getMainChunks() {
      return mainChunks;
   }
   /**
    * Gets all the recipient details chunks.
    * These will normally be in the order of:
    *  * TO recipients, in the order returned by {@link #getDisplayTo()}
    *  * CC recipients, in the order returned by {@link #getDisplayCC()}
    *  * BCC recipients, in the order returned by {@link #getDisplayBCC()}
    */
   public RecipientChunks[] getRecipientDetailsChunks() {
      return recipientChunks;
   }
   /**
    * Gets the Name ID chunks, or
    *  null if there aren't any
    */
   public NameIdChunks getNameIdChunks() {
      return nameIdChunks;
   }
   /**
    * Gets the message attachments.
    */
   public AttachmentChunks[] getAttachmentFiles() {
      return attachmentChunks;
   }


   /**
    * Note - not yet supported, sorry.
    */
   public void write(OutputStream out) throws IOException {
      throw new UnsupportedOperationException("Writing isn't yet supported for HSMF, sorry");
   }


   /**
    * Will you get a null on a missing chunk, or a 
    *  {@link ChunkNotFoundException} (default is the
    *  exception).
    */
   public boolean isReturnNullOnMissingChunk() {
      return returnNullOnMissingChunk;
   }

   /**
    * Sets whether on asking for a missing chunk,
    *  you get back null or a {@link ChunkNotFoundException}
    *  (default is the exception). 
    */
   public void setReturnNullOnMissingChunk(boolean returnNullOnMissingChunk) {
      this.returnNullOnMissingChunk = returnNullOnMissingChunk;
   }


   private String toSemicolonList(String[] l) {
      StringBuffer list = new StringBuffer();
      boolean first = true;

      for(String s : l) {
         if(s == null) continue;
         if(first) {
            first = false;
         } else {
            list.append("; ");
         }
         list.append(s);
      }

      return list.toString();
   }
}
