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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.util.Iterator;

import org.apache.poi.POIDataSamples;
import org.apache.poi.poifs.common.POIFSConstants;
import org.apache.poi.util.IOUtils;
import org.junit.Test;

/**
 * Tests for the Mini Store in the NIO POIFS
 */
@SuppressWarnings("CatchMayIgnoreException")
public final class TestPOIFSMiniStore {
   private static final POIDataSamples _inst = POIDataSamples.getPOIFSInstance();
   
   /**
    * Check that for a given mini block, we can correctly figure
    *  out what the next one is
    */
   @Test
   public void testNextBlock() throws Exception {
      // It's the same on 512 byte and 4096 byte block files!
      POIFSFileSystem fsA = new POIFSFileSystem(_inst.getFile("BlockSize512.zvi"));
      POIFSFileSystem fsB = new POIFSFileSystem(_inst.openResourceAsStream("BlockSize512.zvi"));
      POIFSFileSystem fsC = new POIFSFileSystem(_inst.getFile("BlockSize4096.zvi"));
      POIFSFileSystem fsD = new POIFSFileSystem(_inst.openResourceAsStream("BlockSize4096.zvi"));
      for(POIFSFileSystem fs : new POIFSFileSystem[] {fsA,fsB,fsC,fsD}) {
         POIFSMiniStore ministore = fs.getMiniStore();
         
         // 0 -> 51 is one stream
         for(int i=0; i<50; i++) {
            assertEquals(i+1, ministore.getNextBlock(i));
         }
         assertEquals(POIFSConstants.END_OF_CHAIN, ministore.getNextBlock(50));
         
         // 51 -> 103 is the next
         for(int i=51; i<103; i++) {
            assertEquals(i+1, ministore.getNextBlock(i));
         }
         assertEquals(POIFSConstants.END_OF_CHAIN, ministore.getNextBlock(103));
         
         // Then there are 3 one block ones
         assertEquals(POIFSConstants.END_OF_CHAIN, ministore.getNextBlock(104));
         assertEquals(POIFSConstants.END_OF_CHAIN, ministore.getNextBlock(105));
         assertEquals(POIFSConstants.END_OF_CHAIN, ministore.getNextBlock(106));
         
         // 107 -> 154 is the next
         for(int i=107; i<154; i++) {
            assertEquals(i+1, ministore.getNextBlock(i));
         }
         assertEquals(POIFSConstants.END_OF_CHAIN, ministore.getNextBlock(154));

         // 155 -> 160 is the next
         for(int i=155; i<160; i++) {
            assertEquals(i+1, ministore.getNextBlock(i));
         }
         assertEquals(POIFSConstants.END_OF_CHAIN, ministore.getNextBlock(160));
         
         // 161 -> 166 is the next
         for(int i=161; i<166; i++) {
            assertEquals(i+1, ministore.getNextBlock(i));
         }
         assertEquals(POIFSConstants.END_OF_CHAIN, ministore.getNextBlock(166));
         
         // 167 -> 172 is the next
         for(int i=167; i<172; i++) {
            assertEquals(i+1, ministore.getNextBlock(i));
         }
         assertEquals(POIFSConstants.END_OF_CHAIN, ministore.getNextBlock(172));
         
         // Now some short ones
         assertEquals(174                        , ministore.getNextBlock(173));
         assertEquals(POIFSConstants.END_OF_CHAIN, ministore.getNextBlock(174));
         
         assertEquals(POIFSConstants.END_OF_CHAIN, ministore.getNextBlock(175));
         
         assertEquals(177                        , ministore.getNextBlock(176));
         assertEquals(POIFSConstants.END_OF_CHAIN, ministore.getNextBlock(177));
         
         assertEquals(179                        , ministore.getNextBlock(178));
         assertEquals(180                        , ministore.getNextBlock(179));
         assertEquals(POIFSConstants.END_OF_CHAIN, ministore.getNextBlock(180));
         
         // 181 onwards is free
         for(int i=181; i<fs.getBigBlockSizeDetails().getBATEntriesPerBlock(); i++) {
            assertEquals(POIFSConstants.UNUSED_BLOCK, ministore.getNextBlock(i));
         }
      }
      fsD.close();
      fsC.close();
      fsB.close();
      fsA.close();
   }

   /**
    * Check we get the right data back for each block
    */
   @Test
   public void testGetBlock() throws Exception {
      // It's the same on 512 byte and 4096 byte block files!
      POIFSFileSystem fsA = new POIFSFileSystem(_inst.getFile("BlockSize512.zvi"));
      POIFSFileSystem fsB = new POIFSFileSystem(_inst.openResourceAsStream("BlockSize512.zvi"));
      POIFSFileSystem fsC = new POIFSFileSystem(_inst.getFile("BlockSize4096.zvi"));
      POIFSFileSystem fsD = new POIFSFileSystem(_inst.openResourceAsStream("BlockSize4096.zvi"));
      for(POIFSFileSystem fs : new POIFSFileSystem[] {fsA,fsB,fsC,fsD}) {
         // Mini stream should be at big block zero
         assertEquals(0, fs._get_property_table().getRoot().getStartBlock());
         
         // Grab the ministore
         POIFSMiniStore ministore = fs.getMiniStore();
         ByteBuffer b;
         
         // Runs from the start of the data section in 64 byte chungs
         b = ministore.getBlockAt(0);
         assertEquals((byte)0x9e, b.get());
         assertEquals((byte)0x75, b.get());
         assertEquals((byte)0x97, b.get());
         assertEquals((byte)0xf6, b.get());
         assertEquals((byte)0xff, b.get());
         assertEquals((byte)0x21, b.get());
         assertEquals((byte)0xd2, b.get());
         assertEquals((byte)0x11, b.get());
         
         // And the next block
         b = ministore.getBlockAt(1);
         assertEquals((byte)0x00, b.get());
         assertEquals((byte)0x00, b.get());
         assertEquals((byte)0x03, b.get());
         assertEquals((byte)0x00, b.get());
         assertEquals((byte)0x12, b.get());
         assertEquals((byte)0x02, b.get());
         assertEquals((byte)0x00, b.get());
         assertEquals((byte)0x00, b.get());
         
         // Check the last data block
         b = ministore.getBlockAt(180);
         assertEquals((byte)0x30, b.get());
         assertEquals((byte)0x00, b.get());
         assertEquals((byte)0x00, b.get());
         assertEquals((byte)0x00, b.get());
         assertEquals((byte)0x00, b.get());
         assertEquals((byte)0x00, b.get());
         assertEquals((byte)0x00, b.get());
         assertEquals((byte)0x80, b.get());
         
         // And the rest until the end of the big block is zeros
         for(int i=181; i<184; i++) {
            b = ministore.getBlockAt(i);
            assertEquals((byte)0, b.get());
            assertEquals((byte)0, b.get());
            assertEquals((byte)0, b.get());
            assertEquals((byte)0, b.get());
            assertEquals((byte)0, b.get());
            assertEquals((byte)0, b.get());
            assertEquals((byte)0, b.get());
            assertEquals((byte)0, b.get());
         }
      }
      fsD.close();
      fsC.close();
      fsB.close();
      fsA.close();
   }
   
   /**
    * Ask for free blocks where there are some already
    *  to be had from the SFAT
    */
   @Test
   public void testGetFreeBlockWithSpare() throws Exception {
      POIFSFileSystem fs = new POIFSFileSystem(_inst.getFile("BlockSize512.zvi"));
      POIFSMiniStore ministore = fs.getMiniStore();
      
      // Our 2nd SBAT block has spares
      assertFalse(ministore.getBATBlockAndIndex(0).getBlock().hasFreeSectors());
      assertTrue(ministore.getBATBlockAndIndex(128).getBlock().hasFreeSectors());
      
      // First free one at 181
      assertEquals(POIFSConstants.UNUSED_BLOCK, ministore.getNextBlock(181));
      assertEquals(POIFSConstants.UNUSED_BLOCK, ministore.getNextBlock(182));
      assertEquals(POIFSConstants.UNUSED_BLOCK, ministore.getNextBlock(183));
      assertEquals(POIFSConstants.UNUSED_BLOCK, ministore.getNextBlock(184));
      
      // Ask, will get 181
      assertEquals(181, ministore.getFreeBlock());
      
      // Ask again, will still get 181 as not written to
      assertEquals(181, ministore.getFreeBlock());
      
      // Allocate it, then ask again
      ministore.setNextBlock(181, POIFSConstants.END_OF_CHAIN);
      assertEquals(182, ministore.getFreeBlock());
      
      fs.close();
   }

   /**
    * Ask for free blocks where no free ones exist, and so the
    *  stream needs to be extended and another SBAT added
    */
   @Test
   public void testGetFreeBlockWithNoneSpare() throws Exception {
      POIFSFileSystem fs = new POIFSFileSystem(_inst.openResourceAsStream("BlockSize512.zvi"));
      POIFSMiniStore ministore = fs.getMiniStore();
      
      // We've spare ones from 181 to 255
      for(int i=181; i<256; i++) {
         assertEquals(POIFSConstants.UNUSED_BLOCK, ministore.getNextBlock(i));
      }
      
      // Check our SBAT free stuff is correct
      assertFalse(ministore.getBATBlockAndIndex(0).getBlock().hasFreeSectors());
      assertTrue(ministore.getBATBlockAndIndex(128).getBlock().hasFreeSectors());
      
      // Allocate all the spare ones
      for(int i=181; i<256; i++) {
         ministore.setNextBlock(i, POIFSConstants.END_OF_CHAIN);
      }
      
      // SBAT are now full, but there's only the two
      assertFalse(ministore.getBATBlockAndIndex(0).getBlock().hasFreeSectors());
      assertFalse(ministore.getBATBlockAndIndex(128).getBlock().hasFreeSectors());
      try {
         assertFalse(ministore.getBATBlockAndIndex(256).getBlock().hasFreeSectors());
         fail("Should only be two SBATs");
      } catch(IndexOutOfBoundsException e) {}
      
      // Now ask for a free one, will need to extend the SBAT chain
      assertEquals(256, ministore.getFreeBlock());

      assertFalse(ministore.getBATBlockAndIndex(0).getBlock().hasFreeSectors());
      assertFalse(ministore.getBATBlockAndIndex(128).getBlock().hasFreeSectors());
      assertTrue(ministore.getBATBlockAndIndex(256).getBlock().hasFreeSectors());
      assertEquals(POIFSConstants.END_OF_CHAIN, ministore.getNextBlock(254)); // 2nd SBAT 
      assertEquals(POIFSConstants.END_OF_CHAIN, ministore.getNextBlock(255)); // 2nd SBAT
      assertEquals(POIFSConstants.UNUSED_BLOCK, ministore.getNextBlock(256)); // 3rd SBAT
      assertEquals(POIFSConstants.UNUSED_BLOCK, ministore.getNextBlock(257)); // 3rd SBAT
      
      fs.close();
   }
   
   /**
    * Test that we will extend the underlying chain of 
    *  big blocks that make up the ministream as needed
    */
   @Test
   public void testCreateBlockIfNeeded() throws Exception {
      POIFSFileSystem fs = new POIFSFileSystem(_inst.openResourceAsStream("BlockSize512.zvi"));
      POIFSMiniStore ministore = fs.getMiniStore();
      
      // 178 -> 179 -> 180, 181+ is free
      assertEquals(179                        , ministore.getNextBlock(178));
      assertEquals(180                        , ministore.getNextBlock(179));
      assertEquals(POIFSConstants.END_OF_CHAIN, ministore.getNextBlock(180));
      for(int i=181; i<256; i++) {
         assertEquals(POIFSConstants.UNUSED_BLOCK, ministore.getNextBlock(i));
      }
      
      // However, the ministore data only covers blocks to 183
      for(int i=0; i<=183; i++) {
         ministore.getBlockAt(i);
      }
      try {
         ministore.getBlockAt(184);
         fail("No block at 184");
      } catch(IndexOutOfBoundsException e) {}
      
      // The ministore itself is made up of 23 big blocks
      Iterator<ByteBuffer> it = new POIFSStream(fs, fs.getRoot().getProperty().getStartBlock()).getBlockIterator();
      int count = 0;
      while(it.hasNext()) {
         count++;
         it.next();
      }
      assertEquals(23, count);
      
      // Ask it to get block 184 with creating, it will do
      ministore.createBlockIfNeeded(184);
      
      // The ministore should be one big block bigger now
      it = new POIFSStream(fs, fs.getRoot().getProperty().getStartBlock()).getBlockIterator();
      count = 0;
      while(it.hasNext()) {
         count++;
         it.next();
      }
      assertEquals(24, count);
      
      // The mini block block counts now run to 191
      for(int i=0; i<=191; i++) {
         ministore.getBlockAt(i);
      }
      try {
         ministore.getBlockAt(192);
         fail("No block at 192");
      } catch(IndexOutOfBoundsException e) {}
      
      
      // Now try writing through to 192, check that the SBAT and blocks are there
      byte[] data = new byte[15*64];
      POIFSStream stream = new POIFSStream(ministore, 178);
      stream.updateContents(data);
      
      // Check now
      assertEquals(179                        , ministore.getNextBlock(178));
      assertEquals(180                        , ministore.getNextBlock(179));
      assertEquals(181                        , ministore.getNextBlock(180));
      assertEquals(182                        , ministore.getNextBlock(181));
      assertEquals(183                        , ministore.getNextBlock(182));
      assertEquals(184                        , ministore.getNextBlock(183));
      assertEquals(185                        , ministore.getNextBlock(184));
      assertEquals(186                        , ministore.getNextBlock(185));
      assertEquals(187                        , ministore.getNextBlock(186));
      assertEquals(188                        , ministore.getNextBlock(187));
      assertEquals(189                        , ministore.getNextBlock(188));
      assertEquals(190                        , ministore.getNextBlock(189));
      assertEquals(191                        , ministore.getNextBlock(190));
      assertEquals(192                        , ministore.getNextBlock(191));
      assertEquals(POIFSConstants.END_OF_CHAIN, ministore.getNextBlock(192));
      for(int i=193; i<256; i++) {
         assertEquals(POIFSConstants.UNUSED_BLOCK, ministore.getNextBlock(i));
      }
      
      fs.close();
   }
   
   @Test
   public void testCreateMiniStoreFirst() throws Exception {
       POIFSFileSystem fs = new POIFSFileSystem();
       POIFSMiniStore ministore = fs.getMiniStore();
       DocumentInputStream dis;
       DocumentEntry entry;

       // Initially has Properties + BAT but nothing else
       assertEquals(POIFSConstants.END_OF_CHAIN,     fs.getNextBlock(0));
       assertEquals(POIFSConstants.FAT_SECTOR_BLOCK, fs.getNextBlock(1));
       assertEquals(POIFSConstants.UNUSED_BLOCK,     fs.getNextBlock(2));
       // Ministore has no blocks, so can't iterate until used
       try {
           ministore.getNextBlock(0);
       } catch (IndexOutOfBoundsException e) {}
       
       // Write a very small new document, will populate the ministore for us
       byte[] data = new byte[8];
       for (int i=0; i<data.length; i++) {
           data[i] = (byte)(i+42);
       }
       fs.getRoot().createDocument("mini", new ByteArrayInputStream(data));
       
       // Should now have a mini-fat and a mini-stream
       assertEquals(POIFSConstants.END_OF_CHAIN,    fs.getNextBlock(0));
       assertEquals(POIFSConstants.FAT_SECTOR_BLOCK,fs.getNextBlock(1));
       assertEquals(POIFSConstants.END_OF_CHAIN,    fs.getNextBlock(2));
       assertEquals(POIFSConstants.END_OF_CHAIN,    fs.getNextBlock(3));
       assertEquals(POIFSConstants.UNUSED_BLOCK,    fs.getNextBlock(4));
       assertEquals(POIFSConstants.END_OF_CHAIN,    ministore.getNextBlock(0));
       assertEquals(POIFSConstants.UNUSED_BLOCK,    ministore.getNextBlock(1));
       
       // Re-fetch the mini store, and add it a second time
       ministore = fs.getMiniStore();
       fs.getRoot().createDocument("mini2", new ByteArrayInputStream(data));
       
       // Main unchanged, ministore has a second
       assertEquals(POIFSConstants.END_OF_CHAIN,    fs.getNextBlock(0));
       assertEquals(POIFSConstants.FAT_SECTOR_BLOCK,fs.getNextBlock(1));
       assertEquals(POIFSConstants.END_OF_CHAIN,    fs.getNextBlock(2));
       assertEquals(POIFSConstants.END_OF_CHAIN,    fs.getNextBlock(3));
       assertEquals(POIFSConstants.UNUSED_BLOCK,    fs.getNextBlock(4));
       assertEquals(POIFSConstants.END_OF_CHAIN,    ministore.getNextBlock(0));
       assertEquals(POIFSConstants.END_OF_CHAIN,    ministore.getNextBlock(1));
       assertEquals(POIFSConstants.UNUSED_BLOCK,    ministore.getNextBlock(2));
       
       // Check the data is unchanged and the right length
       entry = (DocumentEntry)fs.getRoot().getEntry("mini");
       assertEquals(data.length, entry.getSize());
       byte[] rdata = new byte[data.length];
       dis = new DocumentInputStream(entry);
       IOUtils.readFully(dis, rdata);
       assertArrayEquals(data, rdata);
       dis.close();
       
       entry = (DocumentEntry)fs.getRoot().getEntry("mini2");
       assertEquals(data.length, entry.getSize());
       rdata = new byte[data.length];
       dis = new DocumentInputStream(entry);
       IOUtils.readFully(dis, rdata);
       assertArrayEquals(data, rdata);
       dis.close();

       // Done
       fs.close();
   }
   
   @Test
   public void testMultiBlockStream() throws Exception {
       byte[] data1B = new byte[63];
       byte[] data2B = new byte[64+14];
       for (int i=0; i<data1B.length; i++) {
           data1B[i] = (byte)(i+2);
       }
       for (int i=0; i<data2B.length; i++) {
           data2B[i] = (byte)(i+4);
       }
       
       // New filesystem and store to use
       POIFSFileSystem fs = new POIFSFileSystem();

       // Initially has Properties + BAT but nothing else
       assertEquals(POIFSConstants.END_OF_CHAIN,     fs.getNextBlock(0));
       assertEquals(POIFSConstants.FAT_SECTOR_BLOCK, fs.getNextBlock(1));
       assertEquals(POIFSConstants.UNUSED_BLOCK,     fs.getNextBlock(2));
       
       // Store the 2 block one, should use 2 mini blocks, and request
       // the use of 2 big blocks
       POIFSMiniStore ministore = fs.getMiniStore();
       fs.getRoot().createDocument("mini2", new ByteArrayInputStream(data2B));
       
       // Check
       assertEquals(POIFSConstants.END_OF_CHAIN,     fs.getNextBlock(0));
       assertEquals(POIFSConstants.FAT_SECTOR_BLOCK, fs.getNextBlock(1));
       assertEquals(POIFSConstants.END_OF_CHAIN,     fs.getNextBlock(2)); // SBAT
       assertEquals(POIFSConstants.END_OF_CHAIN,     fs.getNextBlock(3)); // Mini
       assertEquals(POIFSConstants.UNUSED_BLOCK,     fs.getNextBlock(4));
       
       // First 2 Mini blocks will be used
       assertEquals(2, ministore.getFreeBlock());
       
       // Add one more mini-stream, and check
       fs.getRoot().createDocument("mini1", new ByteArrayInputStream(data1B));
       
       assertEquals(POIFSConstants.END_OF_CHAIN,     fs.getNextBlock(0));
       assertEquals(POIFSConstants.FAT_SECTOR_BLOCK, fs.getNextBlock(1));
       assertEquals(POIFSConstants.END_OF_CHAIN,     fs.getNextBlock(2)); // SBAT
       assertEquals(POIFSConstants.END_OF_CHAIN,     fs.getNextBlock(3)); // Mini
       assertEquals(POIFSConstants.UNUSED_BLOCK,     fs.getNextBlock(4));
       
       // One more mini-block will be used
       assertEquals(3, ministore.getFreeBlock());
       
       // Check the contents too
       byte[] r1 = new byte[data1B.length];
       DocumentInputStream dis = fs.createDocumentInputStream("mini1");
       IOUtils.readFully(dis, r1);
       dis.close();
       assertArrayEquals(data1B, r1);
       
       byte[] r2 = new byte[data2B.length];
       dis = fs.createDocumentInputStream("mini2");
       IOUtils.readFully(dis, r2);
       dis.close();
       assertArrayEquals(data2B, r2);
       fs.close();
   }
}
