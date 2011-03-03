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

package org.apache.poi.hmef;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hmef.attribute.MAPIAttribute;
import org.apache.poi.hmef.attribute.MAPIStringAttribute;
import org.apache.poi.hmef.attribute.TNEFAttribute;
import org.apache.poi.hmef.attribute.TNEFMAPIAttribute;
import org.apache.poi.hmef.attribute.TNEFProperty;
import org.apache.poi.hsmf.datatypes.MAPIProperty;
import org.apache.poi.util.LittleEndian;

/**
 * HMEF - Implementation of the Microsoft TNEF message
 *  encoding format (aka winmail.dat)
 * See:
 *   http://support.microsoft.com/kb/241538
 *   http://en.wikipedia.org/wiki/Transport_Neutral_Encapsulation_Format
 *   http://search.cpan.org/dist/Convert-TNEF/
 */
public final class HMEFMessage {
   public static final long HEADER_SIGNATURE = 0x223e9f78;
   
   private int fileId; 
   private List<TNEFAttribute> messageAttributes = new ArrayList<TNEFAttribute>();
   private List<MAPIAttribute> mapiAttributes = new ArrayList<MAPIAttribute>();
   private List<Attachment> attachments = new ArrayList<Attachment>();
   
   public HMEFMessage(InputStream inp) throws IOException {
      // Check the signature matches
      long sig = LittleEndian.readInt(inp);
      if(sig != HEADER_SIGNATURE) {
         throw new IllegalArgumentException(
               "TNEF signature not detected in file, " +
               "expected " + HEADER_SIGNATURE + " but got " + sig
         );
      }
      
      // Read the File ID
      fileId = LittleEndian.readUShort(inp);
      
      // Now begin processing the contents
      process(inp, 0);
   }
   
   private void process(InputStream inp, int lastLevel) throws IOException {
      // Fetch the level
      int level = inp.read();
      if(level == TNEFProperty.LEVEL_END_OF_FILE) {
         return;
      }
    
      // Build the attribute
      TNEFAttribute attr = TNEFAttribute.create(inp);
      
      // Decide what to attach it to, based on the levels and IDs
      if(level == TNEFProperty.LEVEL_MESSAGE) {
         messageAttributes.add(attr);
         
         if(attr instanceof TNEFMAPIAttribute) {
            TNEFMAPIAttribute tnefMAPI = (TNEFMAPIAttribute)attr;
            mapiAttributes.addAll( tnefMAPI.getMAPIAttributes() );
         }
      } else if(level == TNEFProperty.LEVEL_ATTACHMENT) {
         // Previous attachment or a new one?
         if(attachments.size() == 0 || attr.getProperty() == TNEFProperty.ID_ATTACHRENDERDATA) {
            attachments.add(new Attachment());
         }
         
         // Save the attribute for it
         Attachment attach = attachments.get(attachments.size()-1);
         attach.addAttribute(attr);
      } else {
         throw new IllegalStateException("Unhandled level " + level);
      }
      
      // Handle the next one down
      process(inp, level);
   }
   
   /**
    * Returns all HMEF/TNEF attributes of the message. 
    * Note - In a typical message, most of the interesting properties
    *  are stored as {@link MAPIAttribute}s - see {@link #getMessageMAPIAttributes()} 
    */
   public List<TNEFAttribute> getMessageAttributes() {
      return messageAttributes;
   }
   
   /**
    * Returns all MAPI attributes of the message.
    * Note - A small number of HMEF/TNEF specific attributes normally
    *  apply to most messages, see {@link #getMessageAttributes()}
    */
   public List<MAPIAttribute> getMessageMAPIAttributes() {
      return mapiAttributes;
   }
   
   /**
    * Returns all the Attachments of the message.
    */
   public List<Attachment> getAttachments() {
      return attachments;
   }
   
   /**
    * Return the message attribute with the given ID,
    *  or null if there isn't one. 
    */
   public TNEFAttribute getMessageAttribute(TNEFProperty id) {
      for(TNEFAttribute attr : messageAttributes) {
         if(attr.getProperty() == id) {
            return attr;
         }
      }
      return null;
   }
   
   /**
    * Return the message MAPI Attribute with the given ID,
    *  or null if there isn't one. 
    */
   public MAPIAttribute getMessageMAPIAttribute(MAPIProperty id) {
      for(MAPIAttribute attr : mapiAttributes) {
         if(attr.getProperty() == id) {
            return attr;
         }
      }
      return null;
   }
   
   /**
    * Return the string value of the mapi property, or null
    *  if it isn't set
    */
   private String getString(MAPIProperty id) {
      return MAPIStringAttribute.getAsString( getMessageMAPIAttribute(id) );
   }
   
   /**
    * Returns the Message Subject, or null if the mapi property
    *  for this isn't set
    */
   public String getSubject() {
      return getString(MAPIProperty.CONVERSATION_TOPIC);
   }
   
   /**
    * Returns the Message Body, as RTF, or null if the mapi property
    *  for this isn't set
    */
   public String getBody() {
      return getString(MAPIProperty.RTF_COMPRESSED);
   }
}
