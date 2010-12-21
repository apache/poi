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
package org.apache.poi.hdgf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.poi.util.LZWDecompresser;

/**
 * A decoder for the crazy LZW implementation used
 *  in Visio.
 * According to VSDump, "it's a slightly perverted version of LZW
 *  compression, with inverted meaning of flag byte and 0xFEE as an
 *  'initial shift'". It uses 12 bit codes
 * (http://www.gnome.ru/projects/vsdump_en.html)
 *
 * Two good resources on LZW are:
 *  http://en.wikipedia.org/wiki/LZW
 *  http://marknelson.us/1989/10/01/lzw-data-compression/
 */
public class HDGFLZW extends LZWDecompresser {
   public HDGFLZW() {
      // We're the wrong way round!
      super(false);
   }

   /**
    * Compress the given input stream, returning the array of bytes
    *  of the compressed input
    */
   public byte[] compress(InputStream src) throws IOException {
      ByteArrayOutputStream res = new ByteArrayOutputStream();
      compress(src,res);
      return res.toByteArray();
   }

   /**
    * We have a slight shift by 18 bytes
    */
   @Override
   protected int adjustDictionaryOffset(int pntr) {
      if(pntr > 4078) {
         pntr = pntr - 4078;
      } else {
         pntr = pntr + 18;
      }
      return pntr;
   }

   /**
    * We want an empty dictionary, so do nothing
    */
   @Override
   protected void populateDictionary(byte[] dict) {
   }

   /**
    * Performs the Visio compatible streaming LZW compression.
    */
   public void compress(InputStream src, OutputStream res) throws IOException {
      HDGFLZWCompressor c = new HDGFLZWCompressor();
      c.compress(src, res);
   }
}
