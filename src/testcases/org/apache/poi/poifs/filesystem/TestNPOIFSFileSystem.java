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

import junit.framework.TestCase;

import org.apache.poi.POIDataSamples;
import org.apache.poi.poifs.common.POIFSConstants;

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
         // Check the FAT was properly processed
         // TODO
         
         // Check the properties
         // TODO
      }
      
      // Now with a simple 4096 block file
      fsA = new NPOIFSFileSystem(_inst.getFile("BlockSize4096.zvi"));
      fsB = new NPOIFSFileSystem(_inst.openResourceAsStream("BlockSize4096.zvi"));
      for(NPOIFSFileSystem fs : new NPOIFSFileSystem[] {fsA,fsB}) {
         // Check the FAT was properly processed
         // TODO
         
         // Check the properties
         // TODO
      }
   }
   
   /**
    * Check that for a given block, we can correctly figure
    *  out what the next one is
    */
   public void testNextBlock() throws Exception {
      NPOIFSFileSystem fs = new NPOIFSFileSystem(_inst.getFile("BlockSize512.zvi"));
      
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

   /**
    * Check we get the right data back for each block
    */
   public void testGetBlock() throws Exception {
      NPOIFSFileSystem fs = new NPOIFSFileSystem(_inst.getFile("BlockSize512.zvi"));
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
}
