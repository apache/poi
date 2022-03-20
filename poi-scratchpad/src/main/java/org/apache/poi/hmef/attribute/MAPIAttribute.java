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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.input.UnsynchronizedByteArrayInputStream;
import org.apache.poi.hmef.Attachment;
import org.apache.poi.hmef.HMEFMessage;
import org.apache.poi.hsmf.datatypes.MAPIProperty;
import org.apache.poi.hsmf.datatypes.Types;
import org.apache.poi.hsmf.datatypes.Types.MAPIType;
import org.apache.poi.util.HexDump;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.StringUtil;

/**
 * A pure-MAPI attribute which applies to a {@link HMEFMessage}
 *  or one of its {@link Attachment}s.
 */
public class MAPIAttribute {

   //arbitrarily selected; may need to increase
   private static final int DEFAULT_MAX_RECORD_LENGTH = 1_000_000;
   private static int MAX_RECORD_LENGTH = 1_000_000;
   private static int MAX_RECORD_COUNT = 10_000;

   private final MAPIProperty property;
   private final int type;
   private final byte[] data;

   /**
    * @param length the max record length allowed for MAPIAttribute
    */
   public static void setMaxRecordLength(int length) {
      MAX_RECORD_LENGTH = length;
   }

   /**
    * @return the max record length allowed for MAPIAttribute
    */
   public static int getMaxRecordLength() {
      return MAX_RECORD_LENGTH;
   }

   /**
    * Constructs a single new attribute from
    *  the contents of the stream
    */
   public MAPIAttribute(MAPIProperty property, int type, byte[] data) {
      this.property = property;
      this.type = type;
      this.data = data.clone();
   }

   public MAPIProperty getProperty() {
      return property;
   }

   public int getType() {
      return type;
   }

   public byte[] getData() {
      return data;
   }

   public String toString() {
      String hex;
      if(data.length <= 16) {
         hex = HexDump.toHex(data);
      } else {
         byte[] d = Arrays.copyOf(data, 16);
         hex = HexDump.toHex(d);
         hex = hex.substring(0, hex.length()-1) + ", ....]";
      }

      return property + " " + hex;
   }

   /**
    * Parses a MAPI Properties TNEF Attribute, and returns
    *  the list of MAPI Attributes contained within it
    */
   public static List<MAPIAttribute> create(TNEFAttribute parent) throws IOException {
      if(parent.getProperty() == TNEFProperty.ID_MAPIPROPERTIES) {
         // Regular MAPI Properties, normally on the message
      }
      else if(parent.getProperty() == TNEFProperty.ID_ATTACHMENT) {
         // MAPI Properties for an attachment
      }
      else {
         // Something else, oh dear...
         throw new IllegalArgumentException(
               "Can only create from a MAPIProperty attribute, " +
               "instead received a " + parent.getProperty() + " one"
         );
      }
      try(UnsynchronizedByteArrayInputStream inp = new UnsynchronizedByteArrayInputStream(parent.getData())) {
         // First up, get the number of attributes
         int count = LittleEndian.readInt(inp);
         List<MAPIAttribute> attrs = new ArrayList<>();

         // Now, read each one in in turn
         for(int i=0; i<count; i++) {
            int typeAndMV = LittleEndian.readUShort(inp);
            int id = LittleEndian.readUShort(inp);

            // Is it either Multi-Valued or Variable-Length?
            boolean isMV = false;
            boolean isVL = false;
            int typeId = typeAndMV;
            if( (typeAndMV & Types.MULTIVALUED_FLAG) != 0 ) {
               isMV = true;
               typeId -= Types.MULTIVALUED_FLAG;
            }
            if(typeId == Types.ASCII_STRING.getId() || typeId == Types.UNICODE_STRING.getId() ||
                    typeId == Types.BINARY.getId() || typeId == Types.DIRECTORY.getId()) {
               isVL = true;
            }

            // Turn the type ID into a strongly typed thing
            MAPIType type = Types.getById(typeId);
            if (type == null) {
               type = Types.createCustom(typeId);
            }

            // If it's a named property, rather than a standard
            //  MAPI property, grab the details of it
            MAPIProperty prop = MAPIProperty.get(id);
            if(id >= 0x8000 && id <= 0xFFFF) {
               byte[] guid = new byte[16];
               if (IOUtils.readFully(inp, guid) < 0) {
                  throw new IOException("Not enough data to read guid");
               }
               int mptype = LittleEndian.readInt(inp);

               // Get the name of it
               String name;
               if(mptype == 0) {
                  // It's based on a normal one
                  int mpid = LittleEndian.readInt(inp);
                  MAPIProperty base = MAPIProperty.get(mpid);
                  name = base.name;
               } else {
                  // Custom name was stored
                  int mplen = LittleEndian.readInt(inp);
                  byte[] mpdata = IOUtils.safelyAllocate(mplen, MAX_RECORD_LENGTH);
                  if (IOUtils.readFully(inp, mpdata) < 0) {
                     throw new IOException("Not enough data to read " + mplen + " bytes for attribute name");
                  }
                  name = StringUtil.getFromUnicodeLE(mpdata, 0, (mplen/2)-1);
                  skipToBoundary(mplen, inp);
               }

               // Now create
               prop = MAPIProperty.createCustom(id, type, name);
            }
            if(prop == MAPIProperty.UNKNOWN) {
               prop = MAPIProperty.createCustom(id, type, "(unknown " + Integer.toHexString(id) + ")");
            }

            // Now read in the value(s)
            int values = 1;
            if(isMV || isVL) {
               values = LittleEndian.readInt(inp);
               IOUtils.safelyAllocateCheck(values, MAX_RECORD_COUNT);
            }

            if (type == Types.NULL && values > 1) {
               throw new IOException("Placeholder/NULL arrays aren't supported.");
            }

            for(int j=0; j<values; j++) {
               int len = getLength(type, inp);
               byte[] data = IOUtils.safelyAllocate(len, MAX_RECORD_LENGTH);
               if (IOUtils.readFully(inp, data) < 0) {
                  throw new IOException("Not enough data to read " + len + " bytes of attribute value");
               }
               skipToBoundary(len, inp);

               // Create
               MAPIAttribute attr;
               if(type == Types.UNICODE_STRING || type == Types.ASCII_STRING) {
                  attr = new MAPIStringAttribute(prop, typeId, data);
               } else if(type == Types.APP_TIME || type == Types.TIME) {
                  attr = new MAPIDateAttribute(prop, typeId, data);
               } else if(id == MAPIProperty.RTF_COMPRESSED.id) {
                  attr = new MAPIRtfAttribute(prop, typeId, data);
               } else {
                  attr = new MAPIAttribute(prop, typeId, data);
               }
               attrs.add(attr);
            }
         }

         // All done
         return attrs;
      }
   }

   private static int getLength(MAPIType type, InputStream inp) throws IOException {
      if (type.isFixedLength()) {
         return type.getLength();
      }
      if (type == Types.ASCII_STRING ||
          type == Types.UNICODE_STRING ||
          type == Types.DIRECTORY ||
          type == Types.BINARY) {
            // Need to read the length, as it varies
            return LittleEndian.readInt(inp);
      } else {
            throw new IllegalArgumentException("Unknown type " + type);
      }
   }
   private static void skipToBoundary(int length, InputStream inp) throws IOException {
      // Data is always padded out to a 4 byte boundary
      if(length % 4 != 0) {
         int toSkip = 4 - (length % 4);
         long skipped = IOUtils.skipFully(inp, toSkip);
         if (skipped != toSkip) {
            throw new IOException("tried to skip "+toSkip +" but only skipped:"+skipped);
         }
      }
   }
}
