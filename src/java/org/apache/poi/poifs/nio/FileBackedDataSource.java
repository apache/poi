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
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * A POIFS {@link DataSource} backed by a File
 */
public class FileBackedDataSource extends DataSource {
   private final static POILogger logger = POILogFactory.getLogger( FileBackedDataSource.class );
   
   private FileChannel channel;
   private boolean writable;
   // remember file base, which needs to be closed too
   private RandomAccessFile srcFile;
   
   // Buffers which map to a file-portion are not closed automatically when the Channel is closed
   // therefore we need to keep the list of mapped buffers and do some ugly reflection to try to 
   // clean the buffer during close().
   // See https://bz.apache.org/bugzilla/show_bug.cgi?id=58480, 
   // http://stackoverflow.com/questions/3602783/file-access-synchronized-on-java-object and
   // http://bugs.java.com/view_bug.do?bug_id=4724038 for related discussions
   private List<ByteBuffer> buffersToClean = new ArrayList<>();

   public FileBackedDataSource(File file) throws FileNotFoundException {
       this(newSrcFile(file, "r"), true);
   }

   public FileBackedDataSource(File file, boolean readOnly) throws FileNotFoundException {
       this(newSrcFile(file, readOnly ? "r" : "rw"), readOnly);
   }

   public FileBackedDataSource(RandomAccessFile srcFile, boolean readOnly) {
       this(srcFile.getChannel(), readOnly);
       this.srcFile = srcFile;
   }   
   
   public FileBackedDataSource(FileChannel channel, boolean readOnly) {
      this.channel = channel;
      this.writable = !readOnly;
   }
   
   public boolean isWriteable() {
       return this.writable;
   }
   
   public FileChannel getChannel() {
       return this.channel;
   }

   @Override
   public ByteBuffer read(int length, long position) throws IOException {
      if(position >= size()) {
         throw new IndexOutOfBoundsException("Position " + position + " past the end of the file");
      }
      
      // TODO Could we do the read-only case with MapMode.PRIVATE instead?
      // See https://docs.oracle.com/javase/7/docs/api/java/nio/channels/FileChannel.MapMode.html#PRIVATE
      // Or should we have 3 modes instead of the current boolean - 
      //  read-write, read-only, read-to-write-elsewhere? 
      
      // Do we read or map (for read/write)?
      ByteBuffer dst;
      if (writable) {
          dst = channel.map(FileChannel.MapMode.READ_WRITE, position, length);

          // remember this buffer for cleanup
          buffersToClean.add(dst);
      } else {
          // allocate the buffer on the heap if we cannot map the data in directly
          channel.position(position);
          dst = ByteBuffer.allocate(length);

          // Read the contents and check that we could read some data
          int worked = IOUtils.readFully(channel, dst);
          if(worked == -1) {
              throw new IndexOutOfBoundsException("Position " + position + " past the end of the file");
          }
      }

      // make it ready for reading
      dst.position(0);

      // All done
      return dst;
   }

   @Override
   public void write(ByteBuffer src, long position) throws IOException {
      channel.write(src, position);
   }

   @Override
   public void copyTo(OutputStream stream) throws IOException {
      // Wrap the OutputSteam as a channel
      try (WritableByteChannel out = Channels.newChannel(stream)) {
          // Now do the transfer
          channel.transferTo(0, channel.size(), out);
      }
   }

   @Override
   public long size() throws IOException {
      return channel.size();
   }

   @Override
   public void close() throws IOException {
	   // also ensure that all buffers are unmapped so we do not keep files locked on Windows
	   // We consider it a bug if a Buffer is still in use now! 
       for(ByteBuffer buffer : buffersToClean) {
           unmap(buffer);
       }
       buffersToClean.clear();

       if (srcFile != null) {
          // see http://bugs.java.com/bugdatabase/view_bug.do?bug_id=4796385
          srcFile.close();
      } else {
          channel.close();
      }
   }

   private static RandomAccessFile newSrcFile(File file, String mode) throws FileNotFoundException {
       if(!file.exists()) {
           throw new FileNotFoundException(file.toString());
        }
        return new RandomAccessFile(file, mode);
   }

   // need to use reflection to avoid depending on the sun.nio internal API
   // unfortunately this might break silently with newer/other Java implementations, 
   // but we at least have unit-tests which will indicate this when run on Windows
   private static void unmap(final ByteBuffer buffer) {
       // not necessary for HeapByteBuffer, avoid lots of log-output on this class
       if(buffer.getClass().getName().endsWith("HeapByteBuffer")) {
           return;
       }

       if (CleanerUtil.UNMAP_SUPPORTED) {
           try {
               CleanerUtil.getCleaner().freeBuffer(buffer);
           } catch (IOException e) {
               logger.log(POILogger.WARN, "Failed to unmap the buffer", e);
           }
       } else {
           logger.log(POILogger.DEBUG, CleanerUtil.UNMAP_NOT_SUPPORTED_REASON);
       }
   }
}
