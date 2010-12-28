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
import org.apache.poi.poifs.common.POIFSConstants;

/**
 * Tests {@link NPOIFSStream}
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
    * Tests that we can load some streams that are
    *  stored in the mini stream.
    */
   public void testReadMiniStreams() throws Exception {
      NPOIFSFileSystem fs = new NPOIFSFileSystem(_inst.openResourceAsStream("BlockSize512.zvi"));
      NPOIFSMiniStore ministore = fs.getMiniStore();
      
      // 178 -> 179 -> 180 -> end
      NPOIFSStream stream = new NPOIFSStream(ministore, 178);
      Iterator<ByteBuffer> i = stream.getBlockIterator();
      assertEquals(true, i.hasNext());
      assertEquals(true, i.hasNext());
      assertEquals(true, i.hasNext());
      ByteBuffer b178 = i.next();
      assertEquals(true, i.hasNext());
      assertEquals(true, i.hasNext());
      ByteBuffer b179 = i.next();
      assertEquals(true, i.hasNext());
      ByteBuffer b180 = i.next();
      assertEquals(false, i.hasNext());
      assertEquals(false, i.hasNext());
      assertEquals(false, i.hasNext());
      
      // Check the contents of the 1st block
      assertEquals((byte)0xfe, b178.get());
      assertEquals((byte)0xff, b178.get());
      assertEquals((byte)0x00, b178.get());
      assertEquals((byte)0x00, b178.get());
      assertEquals((byte)0x05, b178.get());
      assertEquals((byte)0x01, b178.get());
      assertEquals((byte)0x02, b178.get());
      assertEquals((byte)0x00, b178.get());
      
      // And the 2nd
      assertEquals((byte)0x6c, b179.get());
      assertEquals((byte)0x00, b179.get());
      assertEquals((byte)0x00, b179.get());
      assertEquals((byte)0x00, b179.get());
      assertEquals((byte)0x28, b179.get());
      assertEquals((byte)0x00, b179.get());
      assertEquals((byte)0x00, b179.get());
      assertEquals((byte)0x00, b179.get());
      
      // And the 3rd
      assertEquals((byte)0x30, b180.get());
      assertEquals((byte)0x00, b180.get());
      assertEquals((byte)0x00, b180.get());
      assertEquals((byte)0x00, b180.get());
      assertEquals((byte)0x00, b180.get());
      assertEquals((byte)0x00, b180.get());
      assertEquals((byte)0x00, b180.get());
      assertEquals((byte)0x80, b180.get());
   }

   /**
    * Writing the same amount of data as before
    */
   public void testReplaceStream() throws Exception {
      NPOIFSFileSystem fs = new NPOIFSFileSystem(_inst.openResourceAsStream("BlockSize512.zvi"));
      
      byte[] data = new byte[512];
      for(int i=0; i<data.length; i++) {
         data[i] = (byte)(i%256);
      }
      
      // 98 is actually the last block in a two block stream...
      NPOIFSStream stream = new NPOIFSStream(fs, 98);
      stream.updateContents(data);
      
      // Check the reading of blocks
      Iterator<ByteBuffer> it = stream.getBlockIterator();
      assertEquals(true, it.hasNext());
      ByteBuffer b = it.next();
      assertEquals(false, it.hasNext());
      
      // Now check the contents
      data = new byte[512];
      b.get(data);
      for(int i=0; i<data.length; i++) {
         byte exp = (byte)(i%256);
         assertEquals(exp, data[i]);
      }
   }
   
   /**
    * Writes less data than before, some blocks will need
    *  to be freed
    */
   public void testReplaceStreamWithLess() throws Exception {
      NPOIFSFileSystem fs = new NPOIFSFileSystem(_inst.openResourceAsStream("BlockSize512.zvi"));
      
      byte[] data = new byte[512];
      for(int i=0; i<data.length; i++) {
         data[i] = (byte)(i%256);
      }
      
      // 97 -> 98 -> end
      assertEquals(98, fs.getNextBlock(97));
      assertEquals(POIFSConstants.END_OF_CHAIN, fs.getNextBlock(98));
      
      // Create a 2 block stream, will become a 1 block one
      NPOIFSStream stream = new NPOIFSStream(fs, 97);
      stream.updateContents(data);
      
      // 97 should now be the end, and 98 free
      assertEquals(POIFSConstants.END_OF_CHAIN, fs.getNextBlock(97));
      assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(98));
      
      // Check the reading of blocks
      Iterator<ByteBuffer> it = stream.getBlockIterator();
      assertEquals(true, it.hasNext());
      ByteBuffer b = it.next();
      assertEquals(false, it.hasNext());
      
      // Now check the contents
      data = new byte[512];
      b.get(data);
      for(int i=0; i<data.length; i++) {
         byte exp = (byte)(i%256);
         assertEquals(exp, data[i]);
      }
   }
   
   /**
    * Writes more data than before, new blocks will be needed
    */
   public void testReplaceStreamWithMore() throws Exception {
      NPOIFSFileSystem fs = new NPOIFSFileSystem(_inst.openResourceAsStream("BlockSize512.zvi"));
      
      byte[] data = new byte[512*3];
      for(int i=0; i<data.length; i++) {
         data[i] = (byte)(i%256);
      }
      
      // 97 -> 98 -> end
      assertEquals(98, fs.getNextBlock(97));
      assertEquals(POIFSConstants.END_OF_CHAIN, fs.getNextBlock(98));
      
      // 100 is our first free one
      assertEquals(POIFSConstants.FAT_SECTOR_BLOCK, fs.getNextBlock(99));
      assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(100));
      
      // Create a 2 block stream, will become a 3 block one
      NPOIFSStream stream = new NPOIFSStream(fs, 97);
      stream.updateContents(data);
      
      // 97 -> 98 -> 100 -> end
      assertEquals(98, fs.getNextBlock(97));
      assertEquals(100, fs.getNextBlock(98));
      assertEquals(POIFSConstants.END_OF_CHAIN, fs.getNextBlock(100));
      
      // Check the reading of blocks
      Iterator<ByteBuffer> it = stream.getBlockIterator();
      int count = 0;
      while(it.hasNext()) {
         ByteBuffer b = it.next();
         data = new byte[512];
         b.get(data);
         for(int i=0; i<data.length; i++) {
            byte exp = (byte)(i%256);
            assertEquals(exp, data[i]);
         }
         count++;
      }
      assertEquals(3, count);
   }
   
   /**
    * Writes to a new stream in the file
    */
   public void testWriteNewStream() throws Exception {
      NPOIFSFileSystem fs = new NPOIFSFileSystem(_inst.openResourceAsStream("BlockSize512.zvi"));
      
      // 100 is our first free one
      assertEquals(POIFSConstants.FAT_SECTOR_BLOCK, fs.getNextBlock(99));
      assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(100));
      assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(101));
      assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(102));
      assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(103));
      assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(104));
      
      
      // Add a single block one
      byte[] data = new byte[512];
      for(int i=0; i<data.length; i++) {
         data[i] = (byte)(i%256);
      }
      
      NPOIFSStream stream = new NPOIFSStream(fs);
      stream.updateContents(data);
      
      // Check it was allocated properly
      assertEquals(POIFSConstants.FAT_SECTOR_BLOCK, fs.getNextBlock(99));
      assertEquals(POIFSConstants.END_OF_CHAIN, fs.getNextBlock(100));
      assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(101));
      assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(102));
      assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(103));
      assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(104));
      
      // And check the contents
      Iterator<ByteBuffer> it = stream.getBlockIterator();
      int count = 0;
      while(it.hasNext()) {
         ByteBuffer b = it.next();
         data = new byte[512];
         b.get(data);
         for(int i=0; i<data.length; i++) {
            byte exp = (byte)(i%256);
            assertEquals(exp, data[i]);
         }
         count++;
      }
      assertEquals(1, count);
      
      
      // And a multi block one
      data = new byte[512*3];
      for(int i=0; i<data.length; i++) {
         data[i] = (byte)(i%256);
      }
      
      stream = new NPOIFSStream(fs);
      stream.updateContents(data);
      
      // Check it was allocated properly
      assertEquals(POIFSConstants.FAT_SECTOR_BLOCK, fs.getNextBlock(99));
      assertEquals(POIFSConstants.END_OF_CHAIN, fs.getNextBlock(100));
      assertEquals(102,                         fs.getNextBlock(101));
      assertEquals(103,                         fs.getNextBlock(102));
      assertEquals(POIFSConstants.END_OF_CHAIN, fs.getNextBlock(103));
      assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(104));
      
      // And check the contents
      it = stream.getBlockIterator();
      count = 0;
      while(it.hasNext()) {
         ByteBuffer b = it.next();
         data = new byte[512];
         b.get(data);
         for(int i=0; i<data.length; i++) {
            byte exp = (byte)(i%256);
            assertEquals(exp, data[i]);
         }
         count++;
      }
      assertEquals(3, count);
      
      // Free it
      stream.free();
      assertEquals(POIFSConstants.FAT_SECTOR_BLOCK, fs.getNextBlock(99));
      assertEquals(POIFSConstants.END_OF_CHAIN, fs.getNextBlock(100));
      assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(101));
      assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(102));
      assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(103));
      assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(104));
   }
   
   /**
    * Writes to a new stream in the file, where we've not enough
    *  free blocks so new FAT segments will need to be allocated
    *  to support this
    */
   public void testWriteNewStreamExtraFATs() throws Exception {
      NPOIFSFileSystem fs = new NPOIFSFileSystem(_inst.openResourceAsStream("BlockSize512.zvi"));
      
      // Allocate almost all the blocks
      assertEquals(POIFSConstants.FAT_SECTOR_BLOCK, fs.getNextBlock(99));
      assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(100));
      assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(127));
      for(int i=100; i<127; i++) {
         fs.setNextBlock(i, POIFSConstants.END_OF_CHAIN);
      }
      assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(127));
      assertEquals(true, fs.getBATBlockAndIndex(0).getBlock().hasFreeSectors());

      
      // Write a 3 block stream
      byte[] data = new byte[512*3];
      for(int i=0; i<data.length; i++) {
         data[i] = (byte)(i%256);
      }
      NPOIFSStream stream = new NPOIFSStream(fs);
      stream.updateContents(data);
      
      // Check we got another BAT
      assertEquals(false, fs.getBATBlockAndIndex(0).getBlock().hasFreeSectors());
      assertEquals(true,  fs.getBATBlockAndIndex(128).getBlock().hasFreeSectors());
      
      // the BAT will be in the first spot of the new block
      assertEquals(POIFSConstants.END_OF_CHAIN, fs.getNextBlock(126));
      assertEquals(129,                         fs.getNextBlock(127));
      assertEquals(POIFSConstants.FAT_SECTOR_BLOCK, fs.getNextBlock(128));
      assertEquals(130,                         fs.getNextBlock(129));
      assertEquals(POIFSConstants.END_OF_CHAIN, fs.getNextBlock(130));
      assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(131));
   }
   
   /**
    * Replaces data in an existing stream, with a bit
    *  more data than before, in a 4096 byte block file
    */
   public void testWriteStream4096() throws Exception {
      NPOIFSFileSystem fs = new NPOIFSFileSystem(_inst.openResourceAsStream("BlockSize4096.zvi"));
      
      // 0 -> 1 -> 2 -> end
      assertEquals(1, fs.getNextBlock(0));
      assertEquals(2, fs.getNextBlock(1));
      assertEquals(POIFSConstants.END_OF_CHAIN, fs.getNextBlock(2));
      assertEquals(4, fs.getNextBlock(3));
      
      // First free one is at 15
      assertEquals(POIFSConstants.FAT_SECTOR_BLOCK, fs.getNextBlock(14));
      assertEquals(POIFSConstants.UNUSED_BLOCK,     fs.getNextBlock(15));
      
      
      // Write a 5 block file 
      byte[] data = new byte[4096*5];
      for(int i=0; i<data.length; i++) {
         data[i] = (byte)(i%256);
      }
      NPOIFSStream stream = new NPOIFSStream(fs, 0);
      stream.updateContents(data);
      
      
      // Check it
      assertEquals(1, fs.getNextBlock(0));
      assertEquals(2, fs.getNextBlock(1));
      assertEquals(15, fs.getNextBlock(2)); // Jumps
      assertEquals(4, fs.getNextBlock(3));  // Next stream
      assertEquals(POIFSConstants.FAT_SECTOR_BLOCK, fs.getNextBlock(14));
      assertEquals(16,                              fs.getNextBlock(15)); // Continues
      assertEquals(POIFSConstants.END_OF_CHAIN,     fs.getNextBlock(16)); // Ends
      assertEquals(POIFSConstants.UNUSED_BLOCK,     fs.getNextBlock(17)); // Free

      // Check the contents too
      Iterator<ByteBuffer> it = stream.getBlockIterator();
      int count = 0;
      while(it.hasNext()) {
         ByteBuffer b = it.next();
         data = new byte[512];
         b.get(data);
         for(int i=0; i<data.length; i++) {
            byte exp = (byte)(i%256);
            assertEquals(exp, data[i]);
         }
         count++;
      }
      assertEquals(5, count);
   }
   
   /**
    * Tests that we can write into the mini stream
    */
   public void testWriteMiniStreams() throws Exception {
      NPOIFSFileSystem fs = new NPOIFSFileSystem(_inst.openResourceAsStream("BlockSize512.zvi"));
      
      // TODO
   }

   /**
    * Craft a nasty file with a loop, and ensure we don't get stuck
    */
   public void testWriteFailsOnLoop() throws Exception {
      NPOIFSFileSystem fs = new NPOIFSFileSystem(_inst.getFile("BlockSize512.zvi"));
      
      // Hack the FAT so that it goes 0->1->2->0
      fs.setNextBlock(0, 1);
      fs.setNextBlock(1, 2);
      fs.setNextBlock(2, 0);
      
      // Try to write a large amount, should fail on the write
      byte[] data = new byte[512*4];
      NPOIFSStream stream = new NPOIFSStream(fs, 0);
      try {
         stream.updateContents(data);
         fail("Loop should have been detected but wasn't!");
      } catch(IllegalStateException e) {}
      
      // Now reset, and try on a small bit
      // Should fail during the freeing set
      fs.setNextBlock(0, 1);
      fs.setNextBlock(1, 2);
      fs.setNextBlock(2, 0);
      
      data = new byte[512];
      stream = new NPOIFSStream(fs, 0);
      try {
         stream.updateContents(data);
         fail("Loop should have been detected but wasn't!");
      } catch(IllegalStateException e) {}
   }
}
