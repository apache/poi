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

import java.nio.ByteBuffer;
import java.util.Iterator;

import junit.framework.TestCase;

import org.apache.poi.POIDataSamples;

/**
 * Tests {@link NPOIFSStream}
 * 
 * TODO Write unit tests
 */
public final class TestNPOIFSStream extends TestCase {
   private static final POIDataSamples _inst = POIDataSamples.getPOIFSInstance();

   /**
    * Read a single block stream
    */
   public void testReadTinyStream() throws Exception {
      NPOIFSFileSystem fs = new NPOIFSFileSystem(_inst.getFile("BlockSize512.zvi"));

      // 98 is actually the last block in a two block stream...
      NPOIFSStream stream = new NPOIFSStream(fs, 98);
      Iterator<ByteBuffer> i = stream.getBlockIterator();
      assertEquals(true, i.hasNext());
      assertEquals(true, i.hasNext());
      assertEquals(true, i.hasNext());
      ByteBuffer b = i.next();
      assertEquals(false, i.hasNext());
      assertEquals(false, i.hasNext());
      assertEquals(false, i.hasNext());
      
      // Check the contents
      assertEquals((byte)0x81, b.get());
      assertEquals((byte)0x00, b.get());
      assertEquals((byte)0x00, b.get());
      assertEquals((byte)0x00, b.get());
      assertEquals((byte)0x82, b.get());
      assertEquals((byte)0x00, b.get());
      assertEquals((byte)0x00, b.get());
      assertEquals((byte)0x00, b.get());
   }

   /**
    * Read a stream with only two blocks in it 
    */
   public void testReadShortStream() throws Exception {
      NPOIFSFileSystem fs = new NPOIFSFileSystem(_inst.getFile("BlockSize512.zvi"));
      
      // 97 -> 98 -> end
      NPOIFSStream stream = new NPOIFSStream(fs, 97);
      Iterator<ByteBuffer> i = stream.getBlockIterator();
      assertEquals(true, i.hasNext());
      assertEquals(true, i.hasNext());
      assertEquals(true, i.hasNext());
      ByteBuffer b97 = i.next();
      assertEquals(true, i.hasNext());
      assertEquals(true, i.hasNext());
      ByteBuffer b98 = i.next();
      assertEquals(false, i.hasNext());
      assertEquals(false, i.hasNext());
      assertEquals(false, i.hasNext());
      
      // Check the contents of the 1st block
      assertEquals((byte)0x01, b97.get());
      assertEquals((byte)0x00, b97.get());
      assertEquals((byte)0x00, b97.get());
      assertEquals((byte)0x00, b97.get());
      assertEquals((byte)0x02, b97.get());
      assertEquals((byte)0x00, b97.get());
      assertEquals((byte)0x00, b97.get());
      assertEquals((byte)0x00, b97.get());
      
      // Check the contents of the 2nd block
      assertEquals((byte)0x81, b98.get());
      assertEquals((byte)0x00, b98.get());
      assertEquals((byte)0x00, b98.get());
      assertEquals((byte)0x00, b98.get());
      assertEquals((byte)0x82, b98.get());
      assertEquals((byte)0x00, b98.get());
      assertEquals((byte)0x00, b98.get());
      assertEquals((byte)0x00, b98.get());
   }
   
   /**
    * Read a stream with many blocks 
    */
   public void testReadLongerStream() throws Exception {
      NPOIFSFileSystem fs = new NPOIFSFileSystem(_inst.getFile("BlockSize512.zvi"));
      
      ByteBuffer b0 = null;
      ByteBuffer b1 = null;
      ByteBuffer b22 = null;
      
      // The stream at 0 has 23 blocks in it
      NPOIFSStream stream = new NPOIFSStream(fs, 0);
      Iterator<ByteBuffer> i = stream.getBlockIterator();
      int count = 0;
      while(i.hasNext()) {
         ByteBuffer b = i.next();
         if(count == 0) {
            b0 = b;
         }
         if(count == 1) {
            b1 = b;
         }
         if(count == 22) {
            b22 = b;
         }
         
         count++;
      }
      assertEquals(23, count);
      
      // Check the contents
      //  1st block is at 0
      assertEquals((byte)0x9e, b0.get());
      assertEquals((byte)0x75, b0.get());
      assertEquals((byte)0x97, b0.get());
      assertEquals((byte)0xf6, b0.get());
            
      //  2nd block is at 1
      assertEquals((byte)0x86, b1.get());
      assertEquals((byte)0x09, b1.get());
      assertEquals((byte)0x22, b1.get());
      assertEquals((byte)0xfb, b1.get());
      
      //  last block is at 89
      assertEquals((byte)0xfe, b22.get());
      assertEquals((byte)0xff, b22.get());
      assertEquals((byte)0x00, b22.get());
      assertEquals((byte)0x00, b22.get());
      assertEquals((byte)0x05, b22.get());
      assertEquals((byte)0x01, b22.get());
      assertEquals((byte)0x02, b22.get());
      assertEquals((byte)0x00, b22.get());
   }

   /**
    * Read a stream with several blocks in a 4096 byte block file 
    */
   public void testReadStream4096() throws Exception {
      NPOIFSFileSystem fs = new NPOIFSFileSystem(_inst.getFile("BlockSize4096.zvi"));
      
      // 0 -> 1 -> 2 -> end
      NPOIFSStream stream = new NPOIFSStream(fs, 0);
      Iterator<ByteBuffer> i = stream.getBlockIterator();
      assertEquals(true, i.hasNext());
      assertEquals(true, i.hasNext());
      assertEquals(true, i.hasNext());
      ByteBuffer b0 = i.next();
      assertEquals(true, i.hasNext());
      assertEquals(true, i.hasNext());
      ByteBuffer b1 = i.next();
      assertEquals(true, i.hasNext());
      assertEquals(true, i.hasNext());
      ByteBuffer b2 = i.next();
      assertEquals(false, i.hasNext());
      assertEquals(false, i.hasNext());
      assertEquals(false, i.hasNext());
      
      // Check the contents of the 1st block
      assertEquals((byte)0x9E, b0.get());
      assertEquals((byte)0x75, b0.get());
      assertEquals((byte)0x97, b0.get());
      assertEquals((byte)0xF6, b0.get());
      assertEquals((byte)0xFF, b0.get());
      assertEquals((byte)0x21, b0.get());
      assertEquals((byte)0xD2, b0.get());
      assertEquals((byte)0x11, b0.get());
      
      // Check the contents of the 2nd block
      assertEquals((byte)0x00, b1.get());
      assertEquals((byte)0x00, b1.get());
      assertEquals((byte)0x03, b1.get());
      assertEquals((byte)0x00, b1.get());
      assertEquals((byte)0x00, b1.get());
      assertEquals((byte)0x00, b1.get());
      assertEquals((byte)0x00, b1.get());
      assertEquals((byte)0x00, b1.get());
      
      // Check the contents of the 3rd block
      assertEquals((byte)0x6D, b2.get());
      assertEquals((byte)0x00, b2.get());
      assertEquals((byte)0x00, b2.get());
      assertEquals((byte)0x00, b2.get());
      assertEquals((byte)0x03, b2.get());
      assertEquals((byte)0x00, b2.get());
      assertEquals((byte)0x46, b2.get());
      assertEquals((byte)0x00, b2.get());
   }
   
   /**
    * Craft a nasty file with a loop, and ensure we don't get stuck
    */
   public void testReadFailsOnLoop() throws Exception {
      NPOIFSFileSystem fs = new NPOIFSFileSystem(_inst.getFile("BlockSize512.zvi"));
      
      // Hack the FAT so that it goes 0->1->2->0
      fs.setNextBlock(0, 1);
      fs.setNextBlock(1, 2);
      fs.setNextBlock(2, 0);
      
      // Now try to read
      NPOIFSStream stream = new NPOIFSStream(fs, 0);
      Iterator<ByteBuffer> i = stream.getBlockIterator();
      assertEquals(true, i.hasNext());
      
      // 1st read works
      i.next();
      assertEquals(true, i.hasNext());
      
      // 2nd read works
      i.next();
      assertEquals(true, i.hasNext());
      
      // 3rd read works
      i.next();
      assertEquals(true, i.hasNext());
      
      // 4th read blows up as it loops back to 0
      try {
         i.next();
         fail("Loop should have been detected but wasn't!");
      } catch(RuntimeException e) {
         // Good, it was detected
      }
      assertEquals(true, i.hasNext());
   }

   /**
    * Writing the same amount of data as before
    */
   public void testReplaceStream() throws Exception {
      NPOIFSFileSystem fs = new NPOIFSFileSystem(_inst.getFile("BlockSize512.zvi"));
      
      // TODO
   }
   
   /**
    * Writes less data than before, some blocks will need
    *  to be freed
    */
   public void testReplaceStreamWithLess() throws Exception {
      NPOIFSFileSystem fs = new NPOIFSFileSystem(_inst.getFile("BlockSize512.zvi"));
      
      // TODO
   }
   
   /**
    * Writes more data than before, new blocks will be needed
    */
   public void testReplaceStreamWithMore() throws Exception {
      NPOIFSFileSystem fs = new NPOIFSFileSystem(_inst.getFile("BlockSize512.zvi"));
      
      // TODO
   }
   
   /**
    * Writes to a new stream in the file
    */
   public void testWriteNewStream() throws Exception {
      NPOIFSFileSystem fs = new NPOIFSFileSystem(_inst.getFile("BlockSize512.zvi"));
      
      // TODO
   }
   
   /**
    * Writes to a new stream in the file, where we've not enough
    *  free blocks so new FAT segments will need to be allocated
    *  to support this
    */
   public void testWriteNewStreamExtraFATs() throws Exception {
      NPOIFSFileSystem fs = new NPOIFSFileSystem(_inst.getFile("BlockSize512.zvi"));
      
      // TODO
   }
   
   /**
    * Replaces data in an existing stream, with a bit
    *  more data than before, in a 4096 byte block file
    */
   public void testWriteStream4096() throws Exception {
      NPOIFSFileSystem fs = new NPOIFSFileSystem(_inst.getFile("BlockSize4096.zvi"));
      
      // TODO
   }
   
   /**
    * Craft a nasty file with a loop, and ensure we don't get stuck
    */
   public void testWriteFailsOnLoop() throws Exception {
      // TODO
   }
}
