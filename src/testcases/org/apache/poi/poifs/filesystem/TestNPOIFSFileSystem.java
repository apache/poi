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
import org.apache.poi.poifs.property.NPropertyTable;
import org.apache.poi.poifs.property.Property;
import org.apache.poi.poifs.property.RootProperty;

/**
 * Tests for the new NIO POIFSFileSystem implementation
 */
public final class TestNPOIFSFileSystem extends TestCase {
   private static final POIDataSamples _inst = POIDataSamples.getPOIFSInstance();

   public void testBasicOpen() throws Exception {
      NPOIFSFileSystem fsA, fsB;
      
      // With a simple 512 block file
      fsA = new NPOIFSFileSystem(_inst.getFile("BlockSize512.zvi"));
      fsB = new NPOIFSFileSystem(_inst.openResourceAsStream("BlockSize512.zvi"));
      for(NPOIFSFileSystem fs : new NPOIFSFileSystem[] {fsA,fsB}) {
         assertEquals(512, fs.getBigBlockSize());
      }
      
      // Now with a simple 4096 block file
      fsA = new NPOIFSFileSystem(_inst.getFile("BlockSize4096.zvi"));
      fsB = new NPOIFSFileSystem(_inst.openResourceAsStream("BlockSize4096.zvi"));
      for(NPOIFSFileSystem fs : new NPOIFSFileSystem[] {fsA,fsB}) {
         assertEquals(4096, fs.getBigBlockSize());
      }
   }

   public void testPropertiesAndFatOnRead() throws Exception {
      NPOIFSFileSystem fsA, fsB;
      
      // With a simple 512 block file
      fsA = new NPOIFSFileSystem(_inst.getFile("BlockSize512.zvi"));
      fsB = new NPOIFSFileSystem(_inst.openResourceAsStream("BlockSize512.zvi"));
      for(NPOIFSFileSystem fs : new NPOIFSFileSystem[] {fsA,fsB}) {
         // Check the FAT was properly processed:
         // Verify we only got one block
         fs.getBATBlockAndIndex(0);
         fs.getBATBlockAndIndex(1);
         try {
            fs.getBATBlockAndIndex(140);
            fail("Should only be one BAT, but a 2nd was found");
         } catch(IndexOutOfBoundsException e) {}
         
         // Verify a few next offsets
         // 97 -> 98 -> END
         assertEquals(98, fs.getNextBlock(97));
         assertEquals(POIFSConstants.END_OF_CHAIN, fs.getNextBlock(98));
         
         
         // Check the properties
         NPropertyTable props = fs._get_property_table();
         assertEquals(90, props.getStartBlock());
         assertEquals(7, props.countBlocks());
         
         // Root property tells us about the Mini Stream
         RootProperty root = props.getRoot();
         assertEquals("Root Entry", root.getName());
         assertEquals(11564, root.getSize());
         assertEquals(0, root.getStartBlock());
         
         // Check its children too
         Property prop;
         Iterator<Property> pi = root.getChildren();
         prop = pi.next();
         assertEquals("Thumbnail", prop.getName());
         prop = pi.next();
         assertEquals("\u0005DocumentSummaryInformation", prop.getName());
         prop = pi.next();
         assertEquals("\u0005SummaryInformation", prop.getName());
         prop = pi.next();
         assertEquals("Image", prop.getName());
         prop = pi.next();
         assertEquals("Tags", prop.getName());
         assertEquals(false, pi.hasNext());
         
         
         // Check the SBAT (Small Blocks FAT) was properly processed
         // TODO
      }
      
      // Now with a simple 4096 block file
      fsA = new NPOIFSFileSystem(_inst.getFile("BlockSize4096.zvi"));
      fsB = new NPOIFSFileSystem(_inst.openResourceAsStream("BlockSize4096.zvi"));
      for(NPOIFSFileSystem fs : new NPOIFSFileSystem[] {fsA,fsB}) {
         // Check the FAT was properly processed
         // Verify we only got one block
         fs.getBATBlockAndIndex(0);
         fs.getBATBlockAndIndex(1);
         try {
            fs.getBATBlockAndIndex(1040);
            fail("Should only be one BAT, but a 2nd was found");
         } catch(IndexOutOfBoundsException e) {}
         
         // Verify a few next offsets
         // 0 -> 1 -> 2 -> END
         assertEquals(1, fs.getNextBlock(0));
         assertEquals(2, fs.getNextBlock(1));
         assertEquals(POIFSConstants.END_OF_CHAIN, fs.getNextBlock(2));

         
         // Check the properties
         NPropertyTable props = fs._get_property_table();
         assertEquals(12, props.getStartBlock());
         assertEquals(1, props.countBlocks());
         
         // Root property tells us about the Mini Stream
         RootProperty root = props.getRoot();
         assertEquals("Root Entry", root.getName());
         assertEquals(11564, root.getSize());
         assertEquals(0, root.getStartBlock());
         
         // Check its children too
         Property prop;
         Iterator<Property> pi = root.getChildren();
         prop = pi.next();
         assertEquals("Thumbnail", prop.getName());
         prop = pi.next();
         assertEquals("\u0005DocumentSummaryInformation", prop.getName());
         prop = pi.next();
         assertEquals("\u0005SummaryInformation", prop.getName());
         prop = pi.next();
         assertEquals("Image", prop.getName());
         prop = pi.next();
         assertEquals("Tags", prop.getName());
         assertEquals(false, pi.hasNext());
         
         
         // Check the SBAT (Small Blocks FAT) was properly processed
         // TODO
      }
   }
   
   /**
    * Check that for a given block, we can correctly figure
    *  out what the next one is
    */
   public void testNextBlock() throws Exception {
      NPOIFSFileSystem fsA = new NPOIFSFileSystem(_inst.getFile("BlockSize512.zvi"));
      NPOIFSFileSystem fsB = new NPOIFSFileSystem(_inst.openResourceAsStream("BlockSize512.zvi"));
      for(NPOIFSFileSystem fs : new NPOIFSFileSystem[] {fsA,fsB}) {
         // 0 -> 21 are simple
         for(int i=0; i<21; i++) {
            assertEquals(i+1, fs.getNextBlock(i));
         }
         // 21 jumps to 89, then ends
         assertEquals(89, fs.getNextBlock(21));
         assertEquals(POIFSConstants.END_OF_CHAIN, fs.getNextBlock(89));
         
         // 22 -> 88 simple sequential stream
         for(int i=22; i<88; i++) {
            assertEquals(i+1, fs.getNextBlock(i));
         }
         assertEquals(POIFSConstants.END_OF_CHAIN, fs.getNextBlock(88));
         
         // 90 -> 96 is another stream
         for(int i=90; i<96; i++) {
            assertEquals(i+1, fs.getNextBlock(i));
         }
         assertEquals(POIFSConstants.END_OF_CHAIN, fs.getNextBlock(96));
         
         // 97+98 is another
         assertEquals(98, fs.getNextBlock(97));
         assertEquals(POIFSConstants.END_OF_CHAIN, fs.getNextBlock(98));
         
         // 99 is our FAT block
         assertEquals(POIFSConstants.FAT_SECTOR_BLOCK, fs.getNextBlock(99));
         
         // 100 onwards is free
         for(int i=100; i<fs.getBigBlockSizeDetails().getBATEntriesPerBlock(); i++) {
            assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(i));
         }
      }
      
      // Quick check on 4096 byte blocks too
      fsA = new NPOIFSFileSystem(_inst.getFile("BlockSize4096.zvi"));
      fsB = new NPOIFSFileSystem(_inst.openResourceAsStream("BlockSize4096.zvi"));
      for(NPOIFSFileSystem fs : new NPOIFSFileSystem[] {fsA,fsB}) {
         // 0 -> 1 -> 2 -> end
         assertEquals(1, fs.getNextBlock(0));
         assertEquals(2, fs.getNextBlock(1));
         assertEquals(POIFSConstants.END_OF_CHAIN, fs.getNextBlock(2));
         
         // 4 -> 11 then end
         for(int i=4; i<11; i++) {
            assertEquals(i+1, fs.getNextBlock(i));
         }
         assertEquals(POIFSConstants.END_OF_CHAIN, fs.getNextBlock(11));
      }
   }

   /**
    * Check we get the right data back for each block
    */
   public void testGetBlock() throws Exception {
      NPOIFSFileSystem fsA = new NPOIFSFileSystem(_inst.getFile("BlockSize512.zvi"));
      NPOIFSFileSystem fsB = new NPOIFSFileSystem(_inst.openResourceAsStream("BlockSize512.zvi"));
      for(NPOIFSFileSystem fs : new NPOIFSFileSystem[] {fsA,fsB}) {
         ByteBuffer b;
         
         // The 0th block is the first data block
         b = fs.getBlockAt(0);
         assertEquals((byte)0x9e, b.get());
         assertEquals((byte)0x75, b.get());
         assertEquals((byte)0x97, b.get());
         assertEquals((byte)0xf6, b.get());
         
         // And the next block
         b = fs.getBlockAt(1);
         assertEquals((byte)0x86, b.get());
         assertEquals((byte)0x09, b.get());
         assertEquals((byte)0x22, b.get());
         assertEquals((byte)0xfb, b.get());
         
         // Check the final block too
         b = fs.getBlockAt(99);
         assertEquals((byte)0x01, b.get());
         assertEquals((byte)0x00, b.get());
         assertEquals((byte)0x00, b.get());
         assertEquals((byte)0x00, b.get());
         assertEquals((byte)0x02, b.get());
         assertEquals((byte)0x00, b.get());
         assertEquals((byte)0x00, b.get());
         assertEquals((byte)0x00, b.get());
      }
      
      // Quick check on 4096 byte blocks too
      fsA = new NPOIFSFileSystem(_inst.getFile("BlockSize4096.zvi"));
      fsB = new NPOIFSFileSystem(_inst.openResourceAsStream("BlockSize4096.zvi"));
      for(NPOIFSFileSystem fs : new NPOIFSFileSystem[] {fsA,fsB}) {
         ByteBuffer b;
         
         // The 0th block is the first data block
         b = fs.getBlockAt(0);
         assertEquals((byte)0x9e, b.get());
         assertEquals((byte)0x75, b.get());
         assertEquals((byte)0x97, b.get());
         assertEquals((byte)0xf6, b.get());
         
         // And the next block
         b = fs.getBlockAt(1);
         assertEquals((byte)0x00, b.get());
         assertEquals((byte)0x00, b.get());
         assertEquals((byte)0x03, b.get());
         assertEquals((byte)0x00, b.get());

         // The 14th block is the FAT
         b = fs.getBlockAt(14);
         assertEquals((byte)0x01, b.get());
         assertEquals((byte)0x00, b.get());
         assertEquals((byte)0x00, b.get());
         assertEquals((byte)0x00, b.get());
         assertEquals((byte)0x02, b.get());
         assertEquals((byte)0x00, b.get());
         assertEquals((byte)0x00, b.get());
         assertEquals((byte)0x00, b.get());
      }
   }
   
   /**
    * Ask for free blocks where there are some already
    *  to be had from the FAT
    */
   public void testGetFreeBlockWithSpare() throws Exception {
      NPOIFSFileSystem fs = new NPOIFSFileSystem(_inst.getFile("BlockSize512.zvi"));
      
      // Our first BAT block has spares
      assertEquals(true, fs.getBATBlockAndIndex(0).getBlock().hasFreeSectors());
      
      // First free one is 100
      assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(100));
      assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(101));
      assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(102));
      assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(103));
      
      // Ask, will get 100
      assertEquals(100, fs.getFreeBlock());
      
      // Ask again, will still get 100 as not written to
      assertEquals(100, fs.getFreeBlock());
      
      // Allocate it, then ask again
      fs.setNextBlock(100, POIFSConstants.END_OF_CHAIN);
      assertEquals(101, fs.getFreeBlock());
   }

   /**
    * Ask for free blocks where no free ones exist, and so the
    *  file needs to be extended and another BAT/XBAT added
    */
   public void testGetFreeBlockWithNoneSpare() throws Exception {
      NPOIFSFileSystem fs = new NPOIFSFileSystem(_inst.openResourceAsStream("BlockSize512.zvi"));
      
      // We've spare ones from 100 to 128
      for(int i=100; i<128; i++) {
         assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(i));
      }
      
      // Check our BAT knows it's free
      assertEquals(true, fs.getBATBlockAndIndex(0).getBlock().hasFreeSectors());
      
      // Allocate all the spare ones
      for(int i=100; i<128; i++) {
         fs.setNextBlock(i, POIFSConstants.END_OF_CHAIN);
      }
      
      // BAT is now full, but there's only the one
      assertEquals(false, fs.getBATBlockAndIndex(0).getBlock().hasFreeSectors());
      try {
         assertEquals(false, fs.getBATBlockAndIndex(128).getBlock().hasFreeSectors());
         fail("Should only be one BAT");
      } catch(IndexOutOfBoundsException e) {}
      
      // Now ask for a free one, will need to extend the file
      assertEquals(129, fs.getFreeBlock());
      
      assertEquals(false, fs.getBATBlockAndIndex(0).getBlock().hasFreeSectors());
      assertEquals(true, fs.getBATBlockAndIndex(128).getBlock().hasFreeSectors());
      assertEquals(POIFSConstants.FAT_SECTOR_BLOCK, fs.getNextBlock(128));
      assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(129));
      
      
      // Fill up to hold 109 BAT blocks
      // TODO
      
      // Ask for another, will get our first XBAT
      // TODO
      
      // Fill the XBAT
      // TODO
      
      // Ask for another, will get our 2nd XBAT
      // TODO
      
      // Write it out and read it back in again
      // Ensure it's correct
      // TODO
   }
}
