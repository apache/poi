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
   private List<Attribute> messageAttributes = new ArrayList<Attribute>();
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
      
      // Finally expand out the MAPI Attributes
      for(Attribute attr : messageAttributes) {
         if(attr.getId() == Attribute.ID_MAPIPROPERTIES) {
            mapiAttributes.addAll( 
                  MAPIAttribute.create(attr) 
            );
         }
      }
      for(Attachment attachment : attachments) {
         for(Attribute attr : attachment.getAttributes()) {
            if(attr.getId() == Attribute.ID_MAPIPROPERTIES) {
               attachment.getMAPIAttributes().addAll(
                     MAPIAttribute.create(attr) 
               );
            }
         }
      }
   }
   
   private void process(InputStream inp, int lastLevel) throws IOException {
      // Fetch the level
      int level = inp.read();
      if(level == Attribute.LEVEL_END_OF_FILE) {
         return;
      }
    
      // Build the attribute
      Attribute attr = new Attribute(inp);
      
      // Decide what to attach it to, based on the levels and IDs
      if(level == Attribute.LEVEL_MESSAGE) {
         messageAttributes.add(attr);
      } else if(level == Attribute.LEVEL_ATTACHMENT) {
         // Previous attachment or a new one?
         if(attachments.size() == 0 || attr.getId() == Attribute.ID_ATTACHRENDERDATA) {
            attachments.add(new Attachment());
         }
         
         // Save the attribute for it
         attachments.get(attachments.size()-1).addAttribute(attr);
      } else {
         throw new IllegalStateException("Unhandled level " + level);
      }
      
      // Handle the next one down
      process(inp, level);
   }
}
