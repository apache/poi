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

package org.apache.poi.hmef.dev;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.poi.hmef.HMEFMessage;
import org.apache.poi.hmef.attribute.MAPIAttribute;
import org.apache.poi.hmef.attribute.TNEFAttribute;
import org.apache.poi.hmef.attribute.TNEFDateAttribute;
import org.apache.poi.hmef.attribute.TNEFProperty;
import org.apache.poi.hmef.attribute.TNEFStringAttribute;
import org.apache.poi.util.HexDump;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndian;

/**
 * Developer focused raw dumper
 */
public final class HMEFDumper {

   //arbitrarily selected; may need to increase
   private static final int DEFAULT_MAX_RECORD_LENGTH = 1_000_000;
   private static int MAX_RECORD_LENGTH = DEFAULT_MAX_RECORD_LENGTH;

   /**
    * @param length the max record length allowed for HMEFDumper
    */
   public static void setMaxRecordLength(int length) {
      MAX_RECORD_LENGTH = length;
   }

   /**
    * @return the max record length allowed for HMEFDumper
    */
   public static int getMaxRecordLength() {
      return MAX_RECORD_LENGTH;
   }

   public static void main(String[] args) throws Exception {
      if(args.length < 1) {
         throw new IllegalArgumentException("Filename must be given");
      }

      boolean truncatePropData = true;
      for (String arg : args) {
         if (arg.equalsIgnoreCase("--full")) {
            truncatePropData = false;
            continue;
         }

          try (InputStream stream = new FileInputStream(arg)) {
              HMEFDumper dumper = new HMEFDumper(stream);
              dumper.setTruncatePropertyData(truncatePropData);
              dumper.dump();
          }
      }
   }

   private InputStream inp;
   private boolean truncatePropertyData;

   public HMEFDumper(InputStream inp) throws IOException {
      this.inp = inp;

      // Check the signature matches
      int sig = LittleEndian.readInt(inp);
      if(sig != HMEFMessage.HEADER_SIGNATURE) {
         throw new IllegalArgumentException(
               "TNEF signature not detected in file, " +
               "expected " + HMEFMessage.HEADER_SIGNATURE +
               " but got " + sig
         );
      }

      // Skip over the File ID
      LittleEndian.readUShort(inp);
   }

   public void setTruncatePropertyData(boolean truncate) {
      truncatePropertyData = truncate;
   }

   private void dump() throws IOException {
      int level;
      int attachments = 0;

      while(true) {
         // Fetch the level
         level = inp.read();
         if(level == TNEFProperty.LEVEL_END_OF_FILE) {
            break;
         }

         // Build the attribute
         TNEFAttribute attr = TNEFAttribute.create(inp);

         // Is it a new attachment?
         if(level == TNEFProperty.LEVEL_ATTACHMENT &&
               attr.getProperty() == TNEFProperty.ID_ATTACHRENDERDATA) {
            attachments++;
            System.out.println();
            System.out.println("Attachment # " + attachments);
            System.out.println();
         }

         // Print the attribute into
         System.out.println(
               "Level " + level + " : Type " + attr.getType() +
               " : ID " + attr.getProperty()
         );

         // Print the contents
         String indent = "  ";

         if(attr instanceof TNEFStringAttribute) {
            System.out.println(indent + indent + indent + ((TNEFStringAttribute)attr).getString());
         }
         if(attr instanceof TNEFDateAttribute) {
            System.out.println(indent + indent + indent + ((TNEFDateAttribute)attr).getDate());
         }

         System.out.println(indent + "Data of length " + attr.getData().length);
         if(attr.getData().length > 0) {
            int len = attr.getData().length;
            if(truncatePropertyData) {
               len = Math.min( attr.getData().length, 48 );
            }

            int loops = len/16;
            if(loops == 0) loops = 1;

            for(int i=0; i<loops; i++) {
               int thisLen = 16;
               int offset = i*16;
               if(i == loops-1) {
                  thisLen = len - offset;
               }

               byte[] data = IOUtils.safelyClone(attr.getData(), offset, thisLen, MAX_RECORD_LENGTH);

               System.out.print(
                     indent + HexDump.dump(data, 0, 0)
               );
            }
         }
         System.out.println();

         if(attr.getProperty() == TNEFProperty.ID_MAPIPROPERTIES ||
               attr.getProperty() == TNEFProperty.ID_ATTACHMENT) {
            List<MAPIAttribute> attrs = MAPIAttribute.create(attr);
            for(MAPIAttribute ma : attrs) {
               System.out.println(indent + indent + ma);
            }
            System.out.println();
         }
      }
   }
}
