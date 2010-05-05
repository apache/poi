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
      if(spare != null) {
         // This is risky, but spare is normally only a byte or two...
         System.arraycopy(spare, 0, b, off, spare.length);
         int ret = spare.length;
         spare = null;
         return ret;
      }
      
      int read = source.read(b, off, len);
      read = fixUp(b, off, read);
      return read;
   }

   @Override
   public int read(byte[] b) throws IOException {
      return this.read(b, 0, b.length);
   }

   private int fixUp(byte[] b, int offset, int read) {
      // Find places to fix
      ArrayList<Integer> fixAt = new ArrayList<Integer>();
      for(int i=offset; i<offset+read-4; i++) {
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
      
      // Save a bit, if needed to fit
      int overshoot = offset+read+fixAt.size() - b.length;  
      if(overshoot > 0) {
         spare = new byte[overshoot];
         System.arraycopy(b, b.length-overshoot, spare, 0, overshoot);
         read -= overshoot;
      }
      
      // Fix them, in reverse order so the
      //  positions are valid
      for(int j=fixAt.size()-1; j>=0; j--) {
         int i = fixAt.get(j); 

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
