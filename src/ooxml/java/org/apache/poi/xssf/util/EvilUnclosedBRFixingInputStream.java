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
package org.apache.poi.xssf.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * This is a seriously sick fix for the fact that some .xlsx
 *  files contain raw bits of HTML, without being escaped
 *  or properly turned into XML.
 * The result is that they contain things like &gt;br&lt;,
 *  which breaks the XML parsing.
 * This very sick InputStream wrapper attempts to spot
 *  these go past, and fix them.
 * Only works for UTF-8 and US-ASCII based streams!
 * It should only be used where experience shows the problem
 *  can occur...
 */
public class EvilUnclosedBRFixingInputStream extends InputStream {
   private InputStream source;
   private byte[] spare;
   
   private static byte[] detect = new byte[] {
      (byte)'<', (byte)'b', (byte)'r', (byte)'>'
   };
   
   public EvilUnclosedBRFixingInputStream(InputStream source) {
      this.source = source;
   }

   /**
    * Warning - doesn't fix!
    */
   @Override
   public int read() throws IOException {
      return source.read();
   }

   @Override
   public int read(byte[] b, int off, int len) throws IOException {
      // Grab any data left from last time
      int readA = readFromSpare(b, off, len);

      // Now read from the stream 
      int readB = source.read(b, off+readA, len-readA);
      
      // Figure out how much we've done
      int read;
      if(readB == -1 || readB == 0) {
         read = readA; 
      } else {
         read = readA + readB;
      }
      
      // Fix up our data
      if(read > 0) {
         read = fixUp(b, off, read);
      }
      
      // All done
      return read;
   }

   @Override
   public int read(byte[] b) throws IOException {
      return this.read(b, 0, b.length);
   }
   
   /**
    * Reads into the buffer from the spare bytes
    */
   private int readFromSpare(byte[] b, int offset, int len) {
      if(spare == null) return 0;
      if(len == 0) throw new IllegalArgumentException("Asked to read 0 bytes");
      
      if(spare.length <= len) {
         // All fits, good
         System.arraycopy(spare, 0, b, offset, spare.length);
         int read = spare.length;
         spare = null;
         return read;
      } else {
         // We have more spare than they can copy with...
         byte[] newspare = new byte[spare.length-len];
         System.arraycopy(spare, 0, b, offset, len);
         System.arraycopy(spare, len, newspare, 0, newspare.length);
         spare = newspare;
         return len;
      }
   }
   private void addToSpare(byte[] b, int offset, int len, boolean atTheEnd) {
      if(spare == null) {
         spare = new byte[len];
         System.arraycopy(b, offset, spare, 0, len);
      } else {
         byte[] newspare = new byte[spare.length+len];
         if(atTheEnd) {
            System.arraycopy(spare, 0, newspare, 0, spare.length);
            System.arraycopy(b, offset, newspare, spare.length, len);
         } else {
            System.arraycopy(b, offset, newspare, 0, len);
            System.arraycopy(spare, 0, newspare, len, spare.length);
         }
         spare = newspare;
      }
   }

   private int fixUp(byte[] b, int offset, int read) {
      // Do we have any potential overhanging ones?
      for(int i=0; i<detect.length-1; i++) {
         int base = offset+read-1-i;
         if(base < 0) continue;
            
         boolean going = true;
         for(int j=0; j<=i && going; j++) {
            if(b[base+j] == detect[j]) {
               // Matches
            } else {
               going = false;
            }
         }
         if(going) {
            // There could be a <br> handing over the end, eg <br|
            addToSpare(b, base, i+1, true);
            read -= 1;
            read -= i;
            break;
         }
      }
      
      // Find places to fix
      ArrayList<Integer> fixAt = new ArrayList<Integer>();
      for(int i=offset; i<=offset+read-detect.length; i++) {
         boolean going = true;
         for(int j=0; j<detect.length && going; j++) {
            if(b[i+j] != detect[j]) {
               going = false;
            }
         }
         if(going) {
            fixAt.add(i);
         }
      }
      
      if(fixAt.size()==0) {
         return read;
      }
      
      // If there isn't space in the buffer to contain
      //  all the fixes, then save the overshoot for next time
      int needed = offset+read+fixAt.size();
      int overshoot = needed - b.length;  
      if(overshoot > 0) {
         // Make sure we don't loose part of a <br>!
         int fixes = 0;
         for(int at : fixAt) {
            if(at > offset+read-detect.length-overshoot-fixes) {
               overshoot = needed - at - 1 - fixes;
               break;
            }
            fixes++;
         }

         addToSpare(b, offset+read-overshoot, overshoot, false);
         read -= overshoot;
      }
      
      // Fix them, in reverse order so the
      //  positions are valid
      for(int j=fixAt.size()-1; j>=0; j--) {
         int i = fixAt.get(j);
         if(i >= read+offset) {
            // This one has moved into the overshoot
            continue;
         }
         if(i > read-3) {
            // This one has moved into the overshoot
            continue;
         }

         byte[] tmp = new byte[read-i-3];
         System.arraycopy(b, i+3, tmp, 0, tmp.length);
         b[i+3] = (byte)'/';
         System.arraycopy(tmp, 0, b, i+4, tmp.length);
         // It got one longer
         read++;
      }
      return read;
   }
}
