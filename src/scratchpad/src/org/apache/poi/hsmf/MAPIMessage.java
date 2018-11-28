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
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.POIReadOnlyDocument;
import org.apache.poi.hmef.attribute.MAPIRtfAttribute;
import org.apache.poi.hsmf.datatypes.AttachmentChunks;
import org.apache.poi.hsmf.datatypes.AttachmentChunks.AttachmentChunksSorter;
import org.apache.poi.hsmf.datatypes.ByteChunk;
import org.apache.poi.hsmf.datatypes.Chunk;
import org.apache.poi.hsmf.datatypes.ChunkGroup;
import org.apache.poi.hsmf.datatypes.Chunks;
import org.apache.poi.hsmf.datatypes.MAPIProperty;
import org.apache.poi.hsmf.datatypes.NameIdChunks;
import org.apache.poi.hsmf.datatypes.PropertyValue;
import org.apache.poi.hsmf.datatypes.PropertyValue.LongPropertyValue;
import org.apache.poi.hsmf.datatypes.PropertyValue.TimePropertyValue;
import org.apache.poi.hsmf.datatypes.RecipientChunks;
import org.apache.poi.hsmf.datatypes.RecipientChunks.RecipientChunksSorter;
import org.apache.poi.hsmf.datatypes.StringChunk;
import org.apache.poi.hsmf.datatypes.Types;
import org.apache.poi.hsmf.exceptions.ChunkNotFoundException;
import org.apache.poi.hsmf.parsers.POIFSChunkParser;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.CodePageUtil;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

/**
 * Reads an Outlook MSG File in and provides hooks into its data structure.
 * 
 * If you want to develop with HSMF, you might find it worth getting
 *  some of the Microsoft public documentation, such as:
 *  
 * [MS-OXCMSG]: Message and Attachment Object Protocol Specification
 */
public class MAPIMessage extends POIReadOnlyDocument {

   /**
    * A MAPI file can be an email (NOTE) or a number of other types
    */
   public enum MESSAGE_CLASS {
      APPOINTMENT,
      CONTACT,
      NOTE,
      POST,
      STICKY_NOTE,
      TASK,
      UNKNOWN,
      UNSPECIFIED
   }

   /** For logging problems we spot with the file */
   private POILogger logger = POILogFactory.getLogger(MAPIMessage.class);
   
   private Chunks mainChunks;
   private NameIdChunks nameIdChunks;
   private RecipientChunks[] recipientChunks;
   private AttachmentChunks[] attachmentChunks;

   private boolean returnNullOnMissingChunk;

   /**
    * Constructor for creating new files.
    */
   public MAPIMessage() {
      // TODO - make writing possible
      super(new POIFSFileSystem());
   }


   /**
    * Constructor for reading MSG Files from the file system.
    * 
    * @param filename Name of the file to read
    * @exception IOException on errors reading, or invalid data
    */
   public MAPIMessage(String filename) throws IOException {
      this(new File(filename));
   }
   /**
    * Constructor for reading MSG Files from the file system.
    * 
    * @param file The file to read from
    * @exception IOException on errors reading, or invalid data
    */
   public MAPIMessage(File file) throws IOException {
      this(new POIFSFileSystem(file));
   }

   /**
    * Constructor for reading MSG Files from an input stream.
    * 
    * <p>Note - this will buffer the whole message into memory
    *  in order to process. For lower memory use, use {@link #MAPIMessage(File)}
    *  
    * @param in The InputStream to buffer then read from
    * @exception IOException on errors reading, or invalid data
    */
   public MAPIMessage(InputStream in) throws IOException {
      this(new POIFSFileSystem(in));
   }
   /**
    * Constructor for reading MSG Files from a POIFS filesystem
    * 
    * @param fs Open POIFS FileSystem containing the message
    * @exception IOException on errors reading, or invalid data
    */
   public MAPIMessage(POIFSFileSystem fs) throws IOException {
      this(fs.getRoot());
   }
   /**
    * Constructor for reading MSG Files from a certain
    *  point within a POIFS filesystem
    * @param poifsDir Directory containing the message
    * @exception IOException on errors reading, or invalid data
    */
   public MAPIMessage(DirectoryNode poifsDir) throws IOException {
      super(poifsDir);

      // Grab all the chunks
      ChunkGroup[] chunkGroups = POIFSChunkParser.parse(poifsDir);

      // Grab interesting bits
      ArrayList<AttachmentChunks> attachments = new ArrayList<>();
      ArrayList<RecipientChunks>  recipients  = new ArrayList<>();
      for(ChunkGroup group : chunkGroups) {
         // Should only ever be one of each of these
         if(group instanceof Chunks) {
            mainChunks = (Chunks)group;
         } else if(group instanceof NameIdChunks) {
            nameIdChunks = (NameIdChunks)group;
         } else if(group instanceof RecipientChunks) {
            recipients.add( (RecipientChunks)group );
         }

         // Can be multiple of these - add to list(s)
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
    * @throws ChunkNotFoundException If the text-body chunk does not exist and
    *       returnNullOnMissingChunk is set
    */
   public String getTextBody() throws ChunkNotFoundException {
      return getStringFromChunk(mainChunks.getTextBodyChunk());
   }

   /**
    * Gets the html body of this Outlook Message, if this email
    *  contains a html version.
    * @return The string representation of the 'html' version of the body, if available.
    * @throws ChunkNotFoundException If the html-body chunk does not exist and
    *       returnNullOnMissingChunk is set
    */
   public String getHtmlBody() throws ChunkNotFoundException {
      if(mainChunks.getHtmlBodyChunkBinary() != null) {
         return mainChunks.getHtmlBodyChunkBinary().getAs7bitString();
      }
      return getStringFromChunk(mainChunks.getHtmlBodyChunkString());
   }

   /**
    * Gets the RTF Rich Message body of this Outlook Message, if this email
    *  contains a RTF (rich) version.
    * @return The string representation of the 'RTF' version of the body, if available.
    * @throws ChunkNotFoundException If the rtf-body chunk does not exist and
    *       returnNullOnMissingChunk is set
    */
   public String getRtfBody() throws ChunkNotFoundException {
      ByteChunk chunk = mainChunks.getRtfBodyChunk();
      if(chunk == null) {
         if(returnNullOnMissingChunk) {
            return null;
         } else {
            throw new ChunkNotFoundException();
         }
      }
      
      try {
         MAPIRtfAttribute rtf = new MAPIRtfAttribute(
               MAPIProperty.RTF_COMPRESSED, Types.BINARY.getId(), chunk.getValue()
         );
         return rtf.getDataString();
      } catch(IOException e) {
         throw new RuntimeException("Shouldn't happen", e);
      }
   }

   /**
    * Gets the subject line of the Outlook Message
    * @throws ChunkNotFoundException If the subject-chunk does not exist and
    *       returnNullOnMissingChunk is set
    */
   public String getSubject() throws ChunkNotFoundException {
      return getStringFromChunk(mainChunks.getSubjectChunk());
   }

   /**
    * Gets the display value of the "FROM" line of the outlook message
    * This is not the actual address that was sent from but the formated display of the user name.
    * @throws ChunkNotFoundException If the from-chunk does not exist and
    *       returnNullOnMissingChunk is set
    */
   public String getDisplayFrom() throws ChunkNotFoundException {
      return getStringFromChunk(mainChunks.getDisplayFromChunk());
   }

   /**
    * Gets the display value of the "TO" line of the outlook message.
    * If there are multiple recipients, they will be separated
    *  by semicolons. 
    * This is not the actual list of addresses/values that will be 
    *  sent to if you click Reply in the email - those are stored
    *  in {@link RecipientChunks}.
    * @throws ChunkNotFoundException If the to-chunk does not exist and
    *       returnNullOnMissingChunk is set
    */
   public String getDisplayTo() throws ChunkNotFoundException {
      return getStringFromChunk(mainChunks.getDisplayToChunk());
   }

   /**
    * Gets the display value of the "CC" line of the outlook message.
    * If there are multiple recipients, they will be separated
    *  by semicolons. 
    * This is not the actual list of addresses/values that will be 
    *  sent to if you click Reply in the email - those are stored
    *  in {@link RecipientChunks}.
    * @throws ChunkNotFoundException If the cc-chunk does not exist and
    *       returnNullOnMissingChunk is set
    */
   public String getDisplayCC() throws ChunkNotFoundException {
      return getStringFromChunk(mainChunks.getDisplayCCChunk());
   }

   /**
    * Gets the display value of the "BCC" line of the outlook message.
    * If there are multiple recipients, they will be separated
    *  by semicolons. 
    * This is not the actual list of addresses/values that will be 
    *  sent to if you click Reply in the email - those are stored
    *  in {@link RecipientChunks}.
    * This will only be present in sent emails, not received ones!
    * @throws ChunkNotFoundException If the bcc-chunk does not exist and
    *       returnNullOnMissingChunk is set
    */
   public String getDisplayBCC() throws ChunkNotFoundException {
      return getStringFromChunk(mainChunks.getDisplayBCCChunk());
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
    * Tries to identify the correct encoding for 7-bit (non-unicode)
    *  strings in the file.
    * <p>Many messages store their strings as unicode, which is
    *  nice and easy. Some use one-byte encodings for their
    *  strings, but don't always store the encoding anywhere
    *  helpful in the file.</p>
    * <p>This method checks for codepage properties, and failing that
    *  looks at the headers for the message, and uses these to 
    *  guess the correct encoding for your file.</p>
    * <p>Bug #49441 has more on why this is needed</p>
    */
   public void guess7BitEncoding() {
      // First choice is a codepage property
      for (MAPIProperty prop : new MAPIProperty[] {
               MAPIProperty.MESSAGE_CODEPAGE,
               MAPIProperty.INTERNET_CPID
      }) {
        List<PropertyValue> val = mainChunks.getProperties().get(prop);
        if (val != null && val.size() > 0) {
           int codepage = ((LongPropertyValue)val.get(0)).getValue();
           try {
               String encoding = CodePageUtil.codepageToEncoding(codepage, true);
               set7BitEncoding(encoding);
               return;
            } catch(UnsupportedEncodingException e) {
               logger.log(POILogger.WARN, "Invalid codepage ID ", codepage, 
                          " set for the message via ", prop, ", ignoring");
            }
         }
      }
     
       
      // Second choice is a charset on a content type header
      try {
         String[] headers = getHeaders();
         if(headers != null && headers.length > 0) {
            // Look for a content type with a charset
            Pattern p = Pattern.compile("Content-Type:.*?charset=[\"']?([^;'\"]+)[\"']?", Pattern.CASE_INSENSITIVE);

            for(String header : headers) {
               if(header.startsWith("Content-Type")) {
                  Matcher m = p.matcher(header);
                  if(m.matches()) {
                     // Found it! Tell all the string chunks
                     String charset = m.group(1);

                     if (!charset.equalsIgnoreCase("utf-8")) { 
                        set7BitEncoding(charset);
                     }
                     return;
                  }
               }
            }
         }
      } catch(ChunkNotFoundException e) {}
      
      // Nothing suitable in the headers, try HTML
      try {
         String html = getHtmlBody();
         if(html != null && html.length() > 0) {
            // Look for a content type in the meta headers
            Pattern p = Pattern.compile(
                  "<META\\s+HTTP-EQUIV=\"Content-Type\"\\s+CONTENT=\"text/html;\\s+charset=(.*?)\""
            );
            Matcher m = p.matcher(html);
            if(m.find()) {
               // Found it! Tell all the string chunks
               String charset = m.group(1);
               set7BitEncoding(charset);
            }
         }
      } catch(ChunkNotFoundException e) {}
   }

   /**
    * Many messages store their strings as unicode, which is
    *  nice and easy. Some use one-byte encodings for their
    *  strings, but don't easily store the encoding anywhere
    *  in the file!
    * If you know what the encoding is of your file, you can
    *  use this method to set the 7 bit encoding for all
    *  the non unicode strings in the file.
    * @see #guess7BitEncoding()
    */
   public void set7BitEncoding(String charset) {
      for(Chunk c : mainChunks.getChunks()) {
         if(c instanceof StringChunk) {
            ((StringChunk)c).set7BitEncoding(charset);
         }
      }

      if (nameIdChunks!=null) {
         for(Chunk c : nameIdChunks.getChunks()) {
            if(c instanceof StringChunk) {
                ((StringChunk)c).set7BitEncoding(charset);
            }
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
   
   /**
    * Does this file contain any strings that
    *  are stored as 7 bit rather than unicode?
    */
   public boolean has7BitEncodingStrings() {
      for(Chunk c : mainChunks.getChunks()) {
         if(c instanceof StringChunk) {
            if( c.getType() == Types.ASCII_STRING ) {
               return true;
            }
         }
      }
      
      if (nameIdChunks!=null) {
         for(Chunk c : nameIdChunks.getChunks()) {
            if(c instanceof StringChunk) {
               if( c.getType() == Types.ASCII_STRING ) {
                  return true;
               }
            }
         }
      }
      
      for(RecipientChunks rc : recipientChunks) {
         for(Chunk c : rc.getAll()) {
            if(c instanceof StringChunk) {
               if( c.getType() == Types.ASCII_STRING ) {
                  return true;
               }
            }
         }
      }
      return false;
   }
   
   /**
    * Returns all the headers, one entry per line
    */
   public String[] getHeaders() throws ChunkNotFoundException {
      String headers = getStringFromChunk(mainChunks.getMessageHeaders());
      if(headers == null) {
         return null;
      }
      return headers.split("\\r?\\n");
   }

   /**
    * Gets the conversation topic of the parsed Outlook Message.
    * This is the part of the subject line that is after the RE: and FWD:
    * @throws ChunkNotFoundException If the conversation-topic chunk does not exist and
    *       returnNullOnMissingChunk is set
    */
   public String getConversationTopic() throws ChunkNotFoundException {
      return getStringFromChunk(mainChunks.getConversationTopic());
   }

   /**
    * Gets the message class of the parsed Outlook Message.
    * (Yes, you can use this to determine if a message is a calendar
    *  item, note, or actual outlook Message)
    * For emails the class will be IPM.Note
    *
    * @throws ChunkNotFoundException If the message-class chunk does not exist and
    *       returnNullOnMissingChunk is set
    */
   public MESSAGE_CLASS getMessageClassEnum() throws ChunkNotFoundException {
      String mc = getStringFromChunk(mainChunks.getMessageClass());
      if (mc == null || mc.trim().length() == 0) {
         return MESSAGE_CLASS.UNSPECIFIED;
      } else if (mc.equalsIgnoreCase("IPM.Note")) {
         return MESSAGE_CLASS.NOTE;
      } else if (mc.equalsIgnoreCase("IPM.Contact")) {
         return MESSAGE_CLASS.CONTACT;
      } else if (mc.equalsIgnoreCase("IPM.Appointment")) {
         return MESSAGE_CLASS.APPOINTMENT;
      } else if (mc.equalsIgnoreCase("IPM.StickyNote")) {
         return MESSAGE_CLASS.STICKY_NOTE;
      } else if (mc.equalsIgnoreCase("IPM.Task")) {
         return MESSAGE_CLASS.TASK;
      } else if (mc.equalsIgnoreCase("IPM.Post")) {
         return MESSAGE_CLASS.POST;
      } else {
         logger.log(POILogger.WARN, "I don't recognize message class '"+mc+"'. " +
                 "Please open an issue on POI's bugzilla");
         return MESSAGE_CLASS.UNKNOWN;
      }
   }
   /**
    * Gets the date that the message was accepted by the
    *  server on.
    */
   public Calendar getMessageDate() throws ChunkNotFoundException {
      if (mainChunks.getSubmissionChunk() != null) {
         return mainChunks.getSubmissionChunk().getAcceptedAtTime();
      }
      else {
         // Try a few likely suspects...
         for (MAPIProperty prop : new MAPIProperty[] {
               MAPIProperty.CLIENT_SUBMIT_TIME, MAPIProperty.LAST_MODIFICATION_TIME,
               MAPIProperty.CREATION_TIME
         }) {
            List<PropertyValue> val = mainChunks.getProperties().get(prop);
            if (val != null && val.size() > 0) {
               return ((TimePropertyValue)val.get(0)).getValue();
            }
         }
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
      StringBuilder list = new StringBuilder();
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
