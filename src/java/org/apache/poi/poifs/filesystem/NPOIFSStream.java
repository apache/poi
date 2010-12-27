
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


package org.apache.poi.poifs.filesystem;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;

import org.apache.poi.poifs.common.POIFSConstants;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem.ChainLoopDetector;
import org.apache.poi.poifs.property.Property;
import org.apache.poi.poifs.storage.HeaderBlock;

/**
 * This handles reading and writing a stream within a
 *  {@link NPOIFSFileSystem}. It can supply an iterator
 *  to read blocks, and way to write out to existing and
 *  new blocks.
 * Most users will want a higher level version of this, 
 *  which deals with properties to track which stream
 *  this is.
 * This only works on big block streams, it doesn't
 *  handle small block ones.
 * This uses the new NIO code
 * 
 * TODO Implement a streaming write method
 */

public class NPOIFSStream implements Iterable<ByteBuffer>
{
	private NPOIFSFileSystem filesystem;
	private int startBlock;
	
	/**
	 * Constructor for an existing stream. It's up to you
	 *  to know how to get the start block (eg from a 
	 *  {@link HeaderBlock} or a {@link Property}) 
	 */
	public NPOIFSStream(NPOIFSFileSystem filesystem, int startBlock) {
	   this.filesystem = filesystem;
	   this.startBlock = startBlock;
	}
	
	/**
	 * Constructor for a new stream. A start block won't
	 *  be allocated until you begin writing to it.
	 */
	public NPOIFSStream(NPOIFSFileSystem filesystem) {
	   this.filesystem = filesystem;
	   this.startBlock = POIFSConstants.END_OF_CHAIN;
	}
	
	/**
	 * What block does this stream start at?
	 * Will be {@link POIFSConstants#END_OF_CHAIN} for a
	 *  new stream that hasn't been written to yet.
	 */
	public int getStartBlock() {
	   return startBlock;
	}

	/**
	 * Returns an iterator that'll supply one {@link ByteBuffer}
	 *  per block in the stream.
	 */
   public Iterator<ByteBuffer> iterator() {
      return getBlockIterator();
   }
	
   public Iterator<ByteBuffer> getBlockIterator() {
      if(startBlock == POIFSConstants.END_OF_CHAIN) {
         throw new IllegalStateException(
               "Can't read from a new stream before it has been written to"
         );
      }
      return new StreamBlockByteBufferIterator(startBlock);
   }
   
   /**
    * Updates the contents of the stream to the new
    *  set of bytes.
    * Note - if this is property based, you'll still
    *  need to update the size in the property yourself
    */
   public void updateContents(byte[] contents) throws IOException {
      // How many blocks are we going to need?
      int blocks = (int)Math.ceil(contents.length / filesystem.getBigBlockSize());
      
      // Make sure we don't encounter a loop whilst overwriting
      //  the existing blocks
      ChainLoopDetector loopDetector = filesystem.new ChainLoopDetector();
      
      // Start writing
      int prevBlock = POIFSConstants.END_OF_CHAIN;
      int nextBlock = startBlock;
      for(int i=0; i<blocks; i++) {
         int thisBlock = nextBlock;
         loopDetector.claim(thisBlock);
         
         // Allocate a block if needed, otherwise figure
         //  out what the next block will be
         if(thisBlock == POIFSConstants.END_OF_CHAIN) {
            thisBlock = filesystem.getFreeBlock();
            nextBlock = POIFSConstants.END_OF_CHAIN;
            
            // Mark the previous block as carrying on
            if(prevBlock != POIFSConstants.END_OF_CHAIN) {
               filesystem.setNextBlock(prevBlock, thisBlock);
            }
         } else {
            nextBlock = filesystem.getNextBlock(thisBlock);
         }
         
         // Write it
         ByteBuffer buffer = filesystem.getBlockAt(thisBlock);
         buffer.put(contents, i*filesystem.getBigBlockSize(), filesystem.getBigBlockSize());
         
         // Update pointers
         prevBlock = thisBlock;
      }
      
      // If we're overwriting, free any remaining blocks
      while(nextBlock != POIFSConstants.END_OF_CHAIN) {
         int thisBlock = nextBlock;
         loopDetector.claim(thisBlock);
         nextBlock = filesystem.getNextBlock(thisBlock);
         filesystem.setNextBlock(thisBlock, POIFSConstants.UNUSED_BLOCK);
      }
      
      // Mark the end of the stream
      filesystem.setNextBlock(nextBlock, POIFSConstants.END_OF_CHAIN);
   }
   
   // TODO Streaming write support too
   
   /**
    * Class that handles a streaming read of one stream
    */
   protected class StreamBlockByteBufferIterator implements Iterator<ByteBuffer> {
      private ChainLoopDetector loopDetector;
      private int nextBlock;
      
      protected StreamBlockByteBufferIterator(int firstBlock) {
         this.nextBlock = firstBlock;
         try {
            this.loopDetector = filesystem.new ChainLoopDetector();
         } catch(IOException e) {
            throw new RuntimeException(e);
         }
      }

      public boolean hasNext() {
         if(nextBlock == POIFSConstants.END_OF_CHAIN) {
            return false;
         }
         return true;
      }

      public ByteBuffer next() {
         if(nextBlock == POIFSConstants.END_OF_CHAIN) {
            throw new IndexOutOfBoundsException("Can't read past the end of the stream");
         }
         
         try {
            loopDetector.claim(nextBlock);
            ByteBuffer data = filesystem.getBlockAt(nextBlock);
            nextBlock = filesystem.getNextBlock(nextBlock);
            return data;
         } catch(IOException e) {
            throw new RuntimeException(e);
         }
      }

      public void remove() {
         throw new UnsupportedOperationException();
      }
   }
}

