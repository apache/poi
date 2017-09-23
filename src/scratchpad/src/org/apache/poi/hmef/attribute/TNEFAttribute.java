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

package org.apache.poi.hmef.attribute;

import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.hmef.Attachment;
import org.apache.poi.hmef.HMEFMessage;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndian;


/**
 * An attribute which applies to a {@link HMEFMessage}
 *  or one of its {@link Attachment}s.
 * Note - the types and IDs differ from standard Outlook/MAPI
 *  ones, so we can't just re-use the HSMF ones.
 */
public class TNEFAttribute {

   //arbitrarily selected; may need to increase
   private static final int MAX_RECORD_LENGTH = 1_000_000;

   private final TNEFProperty property;
   private final int type;
   private final byte[] data;
   private final int checksum;
   
   /**
    * Constructs a single new attribute from the id, type,
    *  and the contents of the stream
    */
   protected TNEFAttribute(int id, int type, InputStream inp) throws IOException {
      this.type = type;
      int length = LittleEndian.readInt(inp);
      
      property = TNEFProperty.getBest(id, type);
      data = IOUtils.safelyAllocate(length, MAX_RECORD_LENGTH);
      IOUtils.readFully(inp, data);
      
      checksum = LittleEndian.readUShort(inp);
   }
   
   /**
    * Creates a new TNEF Attribute by reading data from
    *  the stream within a {@link HMEFMessage}
    */
   public static TNEFAttribute create(InputStream inp) throws IOException {
      int id   = LittleEndian.readUShort(inp);
      int type = LittleEndian.readUShort(inp);
      
      // Create as appropriate
      if(id == TNEFProperty.ID_MAPIPROPERTIES.id ||
            id == TNEFProperty.ID_ATTACHMENT.id) {
         return new TNEFMAPIAttribute(id, type, inp);
      }
      if(type == TNEFProperty.TYPE_STRING ||
           type == TNEFProperty.TYPE_TEXT) {
         return new TNEFStringAttribute(id, type, inp);
      }
      if(type == TNEFProperty.TYPE_DATE) {
         return new TNEFDateAttribute(id, type, inp);
      }
      return new TNEFAttribute(id, type, inp); 
   }

   public TNEFProperty getProperty() {
      return property;
   }

   public int getType() {
      return type;
   }

   public byte[] getData() {
      return data;
   }
   
   public String toString() {
      return "Attribute " + property + ", type=" + type +
             ", data length=" + data.length; 
   }
}
