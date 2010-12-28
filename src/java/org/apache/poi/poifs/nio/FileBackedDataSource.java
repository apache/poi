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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;

import org.apache.poi.util.IOUtils;

/**
 * A POIFS {@link DataSource} backed by a File
 */
public class FileBackedDataSource extends DataSource {
   private FileChannel channel;
   
   public FileBackedDataSource(File file) throws FileNotFoundException {
      if(!file.exists()) {
         throw new FileNotFoundException(file.toString());
      }
      this.channel = (new RandomAccessFile(file, "r")).getChannel();
   }
   public FileBackedDataSource(FileChannel channel) {
      this.channel = channel;
   }
   
   public ByteBuffer read(int length, long position) throws IOException {
      if(position >= size()) {
         throw new IllegalArgumentException("Position " + position + " past the end of the file");
      }

      // Read
      channel.position(position);
      ByteBuffer dst = ByteBuffer.allocate(length);
      int worked = IOUtils.readFully(channel, dst);
      
      // Check
      if(worked == -1) {
         throw new IllegalArgumentException("Position " + position + " past the end of the file");
      }
      
      // Ready it for reading
      dst.position(0);
      
      // All done
      return dst;
   }
   
   public void write(ByteBuffer src, long position) throws IOException {
      channel.write(src, position);
   }
   
   public void copyTo(OutputStream stream) throws IOException {
      // Wrap the OutputSteam as a channel
      WritableByteChannel out = Channels.newChannel(stream);
      // Now do the transfer
      channel.transferTo(0, channel.size(), out);
   }
   
   public long size() throws IOException {
      return channel.size();
   }
   
   public void close() throws IOException {
      channel.close();
   }
}
