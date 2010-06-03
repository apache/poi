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
package org.apache.poi.hsmf.extractor;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;

import org.apache.poi.POIOLE2TextExtractor;
import org.apache.poi.hsmf.MAPIMessage;
import org.apache.poi.hsmf.datatypes.AttachmentChunks;
import org.apache.poi.hsmf.exceptions.ChunkNotFoundException;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.StringUtil.StringsIterator;

/**
 * A text extractor for HSMF (Outlook) .msg files.
 * Outputs in a format somewhat like a plain text email.
 */
public class OutlookTextExtactor extends POIOLE2TextExtractor {
   public OutlookTextExtactor(MAPIMessage msg) {
      super(msg);
   }
   public OutlookTextExtactor(DirectoryNode poifsDir, POIFSFileSystem fs) throws IOException {
      this(new MAPIMessage(poifsDir, fs));
   }
   public OutlookTextExtactor(POIFSFileSystem fs) throws IOException {
      this(new MAPIMessage(fs));
   }
   public OutlookTextExtactor(InputStream inp) throws IOException {
      this(new MAPIMessage(inp));
   }

   /**
    * Returns the underlying MAPI message
    */
   public MAPIMessage getMAPIMessage() {
      return (MAPIMessage)document;
   }
      
   /**
    * Outputs something a little like a RFC822 email
    */
   public String getText() {
      MAPIMessage msg = (MAPIMessage)document;
      StringBuffer s = new StringBuffer();
      
      StringsIterator emails;
      try {
         emails = new StringsIterator(
               msg.getRecipientEmailAddressList()
         );
      } catch(ChunkNotFoundException e) {
         emails = new StringsIterator(new String[0]);
      }
      
      try {
         s.append("From: " + msg.getDisplayFrom() + "\n");
      } catch(ChunkNotFoundException e) {}
      
      // For To, CC and BCC, try to match the names
      //  up with their email addresses. Relies on the
      //  Recipient Chunks being in the same order as
      //  people in To + CC + BCC.
      try {
         handleEmails(s, "To", msg.getDisplayTo(), emails);
      } catch(ChunkNotFoundException e) {}
      try {
         handleEmails(s, "CC", msg.getDisplayCC(), emails);
      } catch(ChunkNotFoundException e) {}
      try {
         handleEmails(s, "BCC", msg.getDisplayBCC(), emails);
      } catch(ChunkNotFoundException e) {}
      
      // Date - try two ways to find it
      try {
         // First try via the proper chunk
         SimpleDateFormat f = new SimpleDateFormat("E, d MMM yyyy HH:mm:ss");
         s.append("Date: " + f.format(msg.getMessageDate().getTime()) + "\n");
      } catch(ChunkNotFoundException e) {
         try {
            // Failing that try via the raw headers 
            String[] headers = msg.getHeaders();
            for(String header: headers) {
               if(header.toLowerCase().startsWith("date:")) {
                  s.append(
                        "Date:" + 
                        header.substring(header.indexOf(':')+1) +
                        "\n"
                  );
                  break;
               }
            }
         } catch(ChunkNotFoundException he) {
            // We can't find the date, sorry...
         }
      }
      
      try {
         s.append("Subject: " + msg.getSubject() + "\n");
      } catch(ChunkNotFoundException e) {}
      
      // Display attachment names
      // To get the attachments, use ExtractorFactory
      for(AttachmentChunks att : msg.getAttachmentFiles()) {
         String ats = att.attachLongFileName.getValue();
         if(att.attachMimeTag != null && 
               att.attachMimeTag.getValue() != null) {
            ats = att.attachMimeTag.getValue() + " = " + ats; 
         }
         s.append("Attachment: " + ats + "\n");
      }
      
      try {
         s.append("\n" + msg.getTextBody() + "\n");
      } catch(ChunkNotFoundException e) {}
      
      return s.toString();
   }
   
   /**
    * Takes a Display focused string, eg "Nick; Jim" and an iterator
    *  of emails, and does its best to return something like
    *  "Nick <nick@example.com>; Jim <jim@example.com>"
    */
   protected void handleEmails(StringBuffer s, String type, String displayText, StringsIterator emails) {
      if(displayText == null || displayText.length() == 0) {
         return;
      }
      
      String[] names = displayText.split(";\\s*");
      boolean first = true;
      
      s.append(type + ": ");
      for(String name : names) {
         if(first) {
            first = false;
         } else {
            s.append("; ");
         }
         
         s.append(name);
         if(emails.hasNext()) {
            String email = emails.next();
            // Append the email address in <>, assuming
            //  the name wasn't already the email address
            if(! email.equals(name)) {
               s.append( " <" + email + ">");
            }
         }
      }
      s.append("\n");
   }
}
