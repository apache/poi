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

package org.apache.poi.poifs.nio;

import org.apache.poi.util.IOUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * A POIFS {@link DataSource} backed by a byte array.
 */
public class ByteArrayBackedDataSource extends DataSource {
   //Can we make this shorter?
   private static final int MAX_RECORD_LENGTH = Integer.MAX_VALUE;

   private byte[] buffer;
   private long size;

   public ByteArrayBackedDataSource(byte[] data, int size) { // NOSONAR
      this.buffer = data;
      this.size = size;
   }
   
   public ByteArrayBackedDataSource(byte[] data) {
      this(data, data.length);
   }

   @Override
   public ByteBuffer read(int length, long position) {
      // Handle non-standard files that have references to blocks beyond EOF
      if(position >= size) {
         // Check system property dynamically to allow runtime configuration
         boolean allowCorruptBlocks = Boolean.getBoolean("org.apache.poi.poifs.allowCorruptBlocks");
         if (!allowCorruptBlocks) {
            throw new IndexOutOfBoundsException(
               "Position " + position + " is beyond EOF (" + size + "). " +
               "Set system property 'org.apache.poi.poifs.allowCorruptBlocks' to true " +
               "to allow reading corrupt files with missing blocks.");
         }
         // Return a zero-filled buffer in tolerant mode
         // This allows processing of documents with corrupted block chains (e.g., some WPS files)
         return ByteBuffer.allocate(length);
      }

      int toRead = (int)Math.min(length, size - position);
      
      return ByteBuffer.wrap(buffer, (int)position, toRead);
   }

   @Override
   public void write(ByteBuffer src, long position) {
      // Extend if needed
      long endPosition = position + src.capacity();
      if(endPosition > buffer.length) {
         extend(endPosition);
      }

      // Now copy
      src.get(buffer, (int)position, src.capacity());

      // Update size if needed
      if(endPosition > size) {
         size = endPosition;
      }
   }

   private void extend(long length) {
      // Consider extending by a bit more than requested
      long difference = length - buffer.length;
      if(difference < buffer.length*0.25) {
         difference = (long)(buffer.length*0.25);
      }
      if(difference < 4096) {
         difference = 4096;
      }

      long totalLen = difference+buffer.length;
      byte[] nb = IOUtils.safelyAllocate(totalLen, MAX_RECORD_LENGTH);
      System.arraycopy(buffer, 0, nb, 0, (int)size);
      buffer = nb;
   }

   @Override
   public void copyTo(OutputStream stream) throws IOException {
      stream.write(buffer, 0, (int)size);
   }

   @Override
   public long size() {
      return size;
   }

   @Override
   public void close() {
      buffer = null;
      size = -1;
   }
}
