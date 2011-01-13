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
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LZWDecompresser;
import org.apache.poi.util.LittleEndian;


/**
 * Within a {@link HMEFMessage}, the content is often
 *  stored in as RTF, but LZW compressed. This class
 *  handles decompressing it for you.
 *  
 * Note - this doesn't quite decompress the data correctly,
 *  more work and unit testing is required...
 */
public final class CompressedRTF extends LZWDecompresser {
   public static final byte[] COMPRESSED_SIGNATURE =
      new byte[] { (byte)'L', (byte)'Z', (byte)'F', (byte)'u' };
   public static final byte[] UNCOMPRESSED_SIGNATURE =
      new byte[] { (byte)'M', (byte)'E', (byte)'L', (byte)'A' };
   public static final int COMPRESSED_SIGNATURE_INT =
      LittleEndian.getInt(COMPRESSED_SIGNATURE);
   public static final int UNCOMPRESSED_SIGNATURE_INT =
      LittleEndian.getInt(UNCOMPRESSED_SIGNATURE);
   
   // The 4096 byte LZW dictionary is pre-loaded with some common
   //  RTF fragments. These come from RTFLIB32.LIB, which ships
   //  with older versions of Visual Studio or the EDK
   public static final String LZW_RTF_PRELOAD = 
      "{\\rtf1\\ansi\\mac\\deff0\\deftab720{\\fonttbl;}{\\f0\\fnil \\froman \\fswiss " +
      "\\fmodern \\fscript \\fdecor MS Sans SerifSymbolArialTimes New RomanCourier" +
      "{\\colortbl\\red0\\green0\\blue0\n\r\\par \\pard\\plain\\f0\\fs20\\b\\i\\u\\tab\\tx";
   
   public CompressedRTF() {
      super(true);
   }

   public void decompress(InputStream src, OutputStream res) throws IOException {
      // Validate the header on the front of the RTF
      int compressedSize = LittleEndian.readInt(src);
      int uncompressedSize = LittleEndian.readInt(src);
      int compressionType = LittleEndian.readInt(src);
      int dataCRC = LittleEndian.readInt(src);
      
      // TODO - Handle CRC checking on the output side
      
      // Do we need to do anything?
      if(compressionType == UNCOMPRESSED_SIGNATURE_INT) {
         // Nope, nothing fancy to do
         IOUtils.copy(src, res);
      } else if(compressionType == COMPRESSED_SIGNATURE_INT) {
         // We need to decompress it below
      } else {
         throw new IllegalArgumentException("Invalid compression signature " + compressionType);
      }

      // Have it processed
      super.decompress(src, res);
   }

   @Override
   protected int adjustDictionaryOffset(int offset) {
      // TODO Do we need to change anything?
      return 0;
   }

   @Override
   protected void populateDictionary(byte[] dict) {
      try {
         byte[] preload = LZW_RTF_PRELOAD.getBytes("US-ASCII");
         System.arraycopy(preload, 0, dict, 0, preload.length);
      } catch(UnsupportedEncodingException e) {
         throw new RuntimeException("Your JVM is broken as it doesn't support US ASCII");
      }
   }
}
