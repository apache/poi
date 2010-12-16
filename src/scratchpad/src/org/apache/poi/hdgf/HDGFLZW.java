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
public class HDGFLZW {

   /**
    * Given an integer, turn it into a java byte, handling
    *  the wrapping.
    * This is a convenience method
    */
   public static byte fromInt(int b) {
      if(b < 128) return (byte)b;
      return (byte)(b - 256);
   }
   /**
    * Given a java byte, turn it into an integer between 0
    *  and 255 (i.e. handle the unwrapping).
    * This is a convenience method
    */
   public static int fromByte(byte b) {
      if(b >= 0) {
         return b;
      }
      return b + 256;
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
    * Decompresses the given input stream, returning the array of bytes
    *  of the decompressed input.
    */
   public byte[] decode(InputStream src) throws IOException {
      ByteArrayOutputStream res = new ByteArrayOutputStream();
      decode(src,res);
      return res.toByteArray();
   }
   
   /**
    * Perform a streaming decompression of the input.
    * Works by:
    * 1) Reading a flag byte, the 8 bits of which tell you if the
    *     following 8 codes are compressed our un-compressed
    * 2) Consider the 8 bits in turn
    * 3) If the bit is set, the next code is un-compressed, so
    *     add it to the dictionary and output it
    * 4) If the bit isn't set, then read in the length and start
    *     position in the dictionary, and output the bytes there
    * 5) Loop until we've done all 8 bits, then read in the next
    *     flag byte
    */
   public void decode(InputStream src, OutputStream res) throws IOException {
      // We use 12 bit codes:
      // * 0-255 are real bytes
      // * 256-4095 are the substring codes
      // Java handily initialises our buffer / dictionary
      //  to all zeros
      byte[] buffer = new byte[4096];

      // How far through the output we've got
      // (This is normally used &4095, so it nicely wraps)
      int pos = 0;
      // The flag byte is treated as its 8 individual
      //  bits, which tell us if the following 8 codes
      //  are compressed or un-compressed
      int flag;
      // The mask, between 1 and 255, which is used when
      //  processing each bit of the flag byte in turn
      int mask;

      // These are bytes as looked up in the dictionary
      // It needs to be signed, as it'll get passed on to
      //  the output stream
      byte[] dataB = new byte[19];
      // This is an unsigned byte read from the stream
      // It needs to be unsigned, so that bit stuff works
      int dataI;
      // The compressed code sequence is held over 2 bytes
      int dataIPt1, dataIPt2;
      // How long a code sequence is, and where in the
      //  dictionary to start at
      int len, pntr;

      while( (flag = src.read()) != -1 ) {
         // Compare each bit in our flag byte in turn:
         for(mask = 1; mask < 256 ; mask <<= 1) {
            // Is this a new code (un-compressed), or
            //  the use of existing codes (compressed)?
            if( (flag & mask) > 0 ) {
               // Retrieve the un-compressed code
               if( (dataI = src.read()) != -1) {
                  // Save the byte into the dictionary
                  buffer[(pos&4095)] = fromInt(dataI);
                  pos++;
                  // And output the byte
                  res.write( new byte[] {fromInt(dataI)} );
               }
            } else {
               // We have a compressed sequence
               // Grab the next 16 bits of data
               dataIPt1 = src.read();
               dataIPt2 = src.read();
               if(dataIPt1 == -1 || dataIPt2 == -1) break;

               // Build up how long the code sequence is, and
               //  what position of the code to start at
               // (The position is the first 12 bits, the
               //  length is the last 4 bits)
               len = (dataIPt2 & 15) + 3;
               pntr = (dataIPt2 & 240)*16 + dataIPt1;

               // If the pointer happens to be passed the end
               //  of our buffer, then wrap around
               if(pntr > 4078) {
                  pntr = pntr - 4078;
               } else {
                  pntr = pntr + 18;
               }

               // Loop over the codes, outputting what they correspond to
               for(int i=0; i<len; i++) {
                  dataB[i] = buffer[(pntr + i) & 4095];
                  buffer[ (pos + i) & 4095 ] = dataB[i];
               }
               res.write(dataB, 0, len);

               // Record how far along the stream we have moved
               pos = pos + len;
            }
         }
      }
   }

   /**
    * Performs the Visio compatible streaming LZW compression.
    */
   public void compress(InputStream src, OutputStream res) throws IOException {
      HDGFLZWCompressor c = new HDGFLZWCompressor();
      c.compress(src, res);
   }
}
