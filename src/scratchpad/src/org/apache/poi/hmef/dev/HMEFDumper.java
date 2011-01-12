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

import org.apache.poi.hmef.Attribute;
import org.apache.poi.hmef.HMEFMessage;
import org.apache.poi.hmef.MAPIAttribute;
import org.apache.poi.util.HexDump;
import org.apache.poi.util.LittleEndian;

/**
 * Developer focused raw dumper
 */
public final class HMEFDumper {
   public static void main(String[] args) throws Exception {
      if(args.length < 1) {
         throw new IllegalArgumentException("Filename must be given");
      }
      
      boolean truncatePropData = true;
      for(int i=0; i<args.length; i++) {
         if(args[i].equalsIgnoreCase("--full")) {
            truncatePropData = false;
            continue;
         }
         
         HMEFDumper dumper = new HMEFDumper(
               new FileInputStream(args[i])
         );
         dumper.setTruncatePropertyData(truncatePropData);
         dumper.dump();
      }
   }
   
   private InputStream inp;
   private boolean truncatePropertyData;
   
   public HMEFDumper(InputStream inp) throws IOException {
      this.inp = inp;
      
      // Check the signature matches
      long sig = LittleEndian.readInt(inp);
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
      
      while(true) {
         // Fetch the level
         level = inp.read();
         if(level == Attribute.LEVEL_END_OF_FILE) {
            break;
         }
       
         // Build the attribute
         Attribute attr = new Attribute(inp);
         
         // Print the attribute into
         System.out.println(
               "Level " + level + " : Type " + attr.getType() +
               " : ID " + attr.getId().toString()
         );
         
         // Print the contents
         String indent = "  ";
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

               byte data[] = new byte[thisLen];
               System.arraycopy(attr.getData(), offset, data, 0, thisLen);
               
               System.out.print(
                     indent + HexDump.dump(data, 0, 0)
               );
            }
         }
         System.out.println();
         
         if(attr.getId() == Attribute.ID_MAPIPROPERTIES) {
            List<MAPIAttribute> attrs = MAPIAttribute.create(attr);
            for(MAPIAttribute ma : attrs) {
               System.out.println(indent + indent + ma);
            }
            System.out.println();
         }
      }
   }
}
