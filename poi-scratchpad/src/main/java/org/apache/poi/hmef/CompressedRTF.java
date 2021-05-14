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
import java.nio.charset.StandardCharsets;

import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LZWDecompresser;
import org.apache.poi.util.LittleEndian;


/**
 * Within a {@link HMEFMessage}, the content is often
 *  stored in as RTF, but LZW compressed. This class
 *  handles decompressing it for you.
 */
public final class CompressedRTF extends LZWDecompresser {
   // github-77: mutable static fields could be changed by malicious code or by accident.
   // These byte arrays should be package protected.
   /*package*/ static final byte[] COMPRESSED_SIGNATURE =
      new byte[] { (byte)'L', (byte)'Z', (byte)'F', (byte)'u' };
   /*package*/ static final byte[] UNCOMPRESSED_SIGNATURE =
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

   private int compressedSize;
   private int decompressedSize;

   public CompressedRTF() {
      // Out flag has the normal meaning
      // Length wise, we're 2 longer than we say, so the max len is 18
      // Endian wise, we're big endian, so 0x1234 is pos 0x123
      super(true, 2, true);
   }

   /**
    * Decompresses the whole of the compressed RTF
    *  stream, outputting the resulting RTF bytes.
    * Note - will decompress any padding at the end of
    *  the input, if present, use {@link #getDeCompressedSize()}
    *  if you need to know how much of the result is
    *  real. (Padding may be up to 7 bytes).
    */
   @Override
   public void decompress(InputStream src, OutputStream res) throws IOException {
      // Validate the header on the front of the RTF
      compressedSize = LittleEndian.readInt(src);
      decompressedSize = LittleEndian.readInt(src);
      int compressionType = LittleEndian.readInt(src);
      /* int dataCRC = */ LittleEndian.readInt(src);

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

   /**
    * Returns how big the compressed version was.
    */
   public int getCompressedSize() {
      // Return the size less the header
      return compressedSize - 12;
   }

   /**
    * Returns how big the decompressed version was.
    */
   public int getDeCompressedSize() {
      return decompressedSize;
   }

   /**
    * We use regular dictionary offsets, so no
    *  need to change anything
    */
   @Override
   protected int adjustDictionaryOffset(int offset) {
      return offset;
   }

   @Override
   protected int populateDictionary(byte[] dict) {
     // Copy in the RTF constants
     byte[] preload = LZW_RTF_PRELOAD.getBytes(StandardCharsets.US_ASCII);
     System.arraycopy(preload, 0, dict, 0, preload.length);

     // Start adding new codes after the constants
     return preload.length;
   }
}
