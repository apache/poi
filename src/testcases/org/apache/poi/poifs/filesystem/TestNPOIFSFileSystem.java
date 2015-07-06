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

import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hpsf.PropertySet;
import org.apache.poi.hpsf.PropertySetFactory;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.poifs.common.POIFSConstants;
import org.apache.poi.poifs.property.NPropertyTable;
import org.apache.poi.poifs.property.Property;
import org.apache.poi.poifs.property.RootProperty;
import org.apache.poi.poifs.storage.HeaderBlock;
import org.apache.poi.util.IOUtils;
import org.junit.Test;

/**
 * Tests for the new NIO POIFSFileSystem implementation
 */
public final class TestNPOIFSFileSystem {
   private static final POIDataSamples _inst = POIDataSamples.getPOIFSInstance();
   
   /**
    * Returns test files with 512 byte and 4k block sizes, loaded
    *  both from InputStreams and Files
    */
   protected NPOIFSFileSystem[] get512and4kFileAndInput() throws Exception {
       NPOIFSFileSystem fsA = new NPOIFSFileSystem(_inst.getFile("BlockSize512.zvi"));
       NPOIFSFileSystem fsB = new NPOIFSFileSystem(_inst.openResourceAsStream("BlockSize512.zvi"));
       NPOIFSFileSystem fsC = new NPOIFSFileSystem(_inst.getFile("BlockSize4096.zvi"));
       NPOIFSFileSystem fsD = new NPOIFSFileSystem(_inst.openResourceAsStream("BlockSize4096.zvi"));
       return new NPOIFSFileSystem[] {fsA,fsB,fsC,fsD};
   }

   protected static void assertBATCount(NPOIFSFileSystem fs, int expectedBAT, int expectedXBAT) throws IOException {
       int foundBAT = 0;
       int foundXBAT = 0;
       int sz = (int)(fs.size() / fs.getBigBlockSize());
       for (int i=0; i<sz; i++) {
           if(fs.getNextBlock(i) == POIFSConstants.FAT_SECTOR_BLOCK) {
               foundBAT++;
           }
           if(fs.getNextBlock(i) == POIFSConstants.DIFAT_SECTOR_BLOCK) {
               foundXBAT++;
           }
       }
       assertEquals("Wrong number of BATs", expectedBAT, foundBAT);
       assertEquals("Wrong number of XBATs with " + expectedBAT + " BATs", expectedXBAT, foundXBAT);
   }
   protected void assertContentsMatches(byte[] expected, DocumentEntry doc) throws IOException {
       NDocumentInputStream inp = new NDocumentInputStream(doc);
       byte[] contents = new byte[doc.getSize()];
       assertEquals(doc.getSize(), inp.read(contents));
       inp.close();
       
       if (expected != null)
           assertThat(expected, equalTo(contents));
   }
   
   protected static HeaderBlock writeOutAndReadHeader(NPOIFSFileSystem fs) throws IOException {
       ByteArrayOutputStream baos = new ByteArrayOutputStream();
       fs.writeFilesystem(baos);
       
       HeaderBlock header = new HeaderBlock(new ByteArrayInputStream(baos.toByteArray()));
       return header;
   }
   
   protected static NPOIFSFileSystem writeOutAndReadBack(NPOIFSFileSystem original) throws IOException {
       ByteArrayOutputStream baos = new ByteArrayOutputStream();
       original.writeFilesystem(baos);
       original.close();
       return new NPOIFSFileSystem(new ByteArrayInputStream(baos.toByteArray()));
   }
   
   @Test
   public void basicOpen() throws Exception {
      NPOIFSFileSystem fsA, fsB;
      
      // With a simple 512 block file
      fsA = new NPOIFSFileSystem(_inst.getFile("BlockSize512.zvi"));
      fsB = new NPOIFSFileSystem(_inst.openResourceAsStream("BlockSize512.zvi"));
      for(NPOIFSFileSystem fs : new NPOIFSFileSystem[] {fsA,fsB}) {
         assertEquals(512, fs.getBigBlockSize());
      }
      fsA.close();
      fsB.close();
      
      // Now with a simple 4096 block file
      fsA = new NPOIFSFileSystem(_inst.getFile("BlockSize4096.zvi"));
      fsB = new NPOIFSFileSystem(_inst.openResourceAsStream("BlockSize4096.zvi"));
      for(NPOIFSFileSystem fs : new NPOIFSFileSystem[] {fsA,fsB}) {
         assertEquals(4096, fs.getBigBlockSize());
      }
      fsA.close();
      fsB.close();
   }

   @Test
   public void propertiesAndFatOnRead() throws Exception {
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
         NPOIFSMiniStore ministore = fs.getMiniStore();
         
         // Verify we only got two SBAT blocks
         ministore.getBATBlockAndIndex(0);
         ministore.getBATBlockAndIndex(128);
         try {
            ministore.getBATBlockAndIndex(256);
            fail("Should only be two SBATs, but a 3rd was found");
         } catch(IndexOutOfBoundsException e) {}
         
         // Verify a few offsets: 0->50 is a stream
         for(int i=0; i<50; i++) {
            assertEquals(i+1, ministore.getNextBlock(i));
         }
         assertEquals(POIFSConstants.END_OF_CHAIN, ministore.getNextBlock(50));
         
         fs.close();
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
         NPOIFSMiniStore ministore = fs.getMiniStore();
         
         // Verify we only got one SBAT block
         ministore.getBATBlockAndIndex(0);
         ministore.getBATBlockAndIndex(128);
         ministore.getBATBlockAndIndex(1023);
         try {
            ministore.getBATBlockAndIndex(1024);
            fail("Should only be one SBAT, but a 2nd was found");
         } catch(IndexOutOfBoundsException e) {}
         
         // Verify a few offsets: 0->50 is a stream
         for(int i=0; i<50; i++) {
            assertEquals(i+1, ministore.getNextBlock(i));
         }
         assertEquals(POIFSConstants.END_OF_CHAIN, ministore.getNextBlock(50));
         
         fs.close();
      }
   }
   
   /**
    * Check that for a given block, we can correctly figure
    *  out what the next one is
    */
   @Test
   public void nextBlock() throws Exception {
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
         
         fs.close();
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
         
         fs.close();
      }
   }

   /**
    * Check we get the right data back for each block
    */
   @Test
   public void getBlock() throws Exception {
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
         
         fs.close();
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
         
         fs.close();
      }
   }
   
   /**
    * Ask for free blocks where there are some already
    *  to be had from the FAT
    */
   @Test
   public void getFreeBlockWithSpare() throws Exception {
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
      
      // All done
      fs.close();
   }

   /**
    * Ask for free blocks where no free ones exist, and so the
    *  file needs to be extended and another BAT/XBAT added
    */
   @Test
   public void getFreeBlockWithNoneSpare() throws Exception {
      NPOIFSFileSystem fs = new NPOIFSFileSystem(_inst.openResourceAsStream("BlockSize512.zvi"));
      int free;

      // We have one BAT at block 99
      assertEquals(POIFSConstants.FAT_SECTOR_BLOCK, fs.getNextBlock(99));
      assertBATCount(fs, 1, 0);
      
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
      assertBATCount(fs, 1, 0);

      
      // Now ask for a free one, will need to extend the file
      assertEquals(129, fs.getFreeBlock());
      
      assertEquals(false, fs.getBATBlockAndIndex(0).getBlock().hasFreeSectors());
      assertEquals(true, fs.getBATBlockAndIndex(128).getBlock().hasFreeSectors());
      assertEquals(POIFSConstants.FAT_SECTOR_BLOCK, fs.getNextBlock(128));
      assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(129));
      
      // We now have 2 BATs, but no XBATs
      assertBATCount(fs, 2, 0);
      
      
      // Fill up to hold 109 BAT blocks
      for(int i=0; i<109; i++) {
         fs.getFreeBlock();
         int startOffset = i*128;
         while( fs.getBATBlockAndIndex(startOffset).getBlock().hasFreeSectors() ) {
            free = fs.getFreeBlock();
            fs.setNextBlock(free, POIFSConstants.END_OF_CHAIN);
         }
      }
      
      assertEquals(false, fs.getBATBlockAndIndex(109*128-1).getBlock().hasFreeSectors());
      try {
         assertEquals(false, fs.getBATBlockAndIndex(109*128).getBlock().hasFreeSectors());
         fail("Should only be 109 BATs");
      } catch(IndexOutOfBoundsException e) {}
      
      // We now have 109 BATs, but no XBATs
      assertBATCount(fs, 109, 0);
      
      
      // Ask for it to be written out, and check the header
      HeaderBlock header = writeOutAndReadHeader(fs);
      assertEquals(109, header.getBATCount());
      assertEquals(0, header.getXBATCount());
      
      
      // Ask for another, will get our first XBAT
      free = fs.getFreeBlock();
      assertEquals(false, fs.getBATBlockAndIndex(109*128-1).getBlock().hasFreeSectors());
      assertEquals(true, fs.getBATBlockAndIndex(110*128-1).getBlock().hasFreeSectors());
      try {
         assertEquals(false, fs.getBATBlockAndIndex(110*128).getBlock().hasFreeSectors());
         fail("Should only be 110 BATs");
      } catch(IndexOutOfBoundsException e) {}
      assertBATCount(fs, 110, 1);
      
      header = writeOutAndReadHeader(fs);
      assertEquals(110, header.getBATCount());
      assertEquals(1, header.getXBATCount());

      
      // Fill the XBAT, which means filling 127 BATs
      for(int i=109; i<109+127; i++) {
         fs.getFreeBlock();
         int startOffset = i*128;
         while( fs.getBATBlockAndIndex(startOffset).getBlock().hasFreeSectors() ) {
            free = fs.getFreeBlock();
            fs.setNextBlock(free, POIFSConstants.END_OF_CHAIN);
         }
         assertBATCount(fs, i+1, 1);
      }
      
      // Should now have 109+127 = 236 BATs
      assertEquals(false, fs.getBATBlockAndIndex(236*128-1).getBlock().hasFreeSectors());
      try {
         assertEquals(false, fs.getBATBlockAndIndex(236*128).getBlock().hasFreeSectors());
         fail("Should only be 236 BATs");
      } catch(IndexOutOfBoundsException e) {}
      assertBATCount(fs, 236, 1);

      
      // Ask for another, will get our 2nd XBAT
      free = fs.getFreeBlock();
      assertEquals(false, fs.getBATBlockAndIndex(236*128-1).getBlock().hasFreeSectors());
      assertEquals(true, fs.getBATBlockAndIndex(237*128-1).getBlock().hasFreeSectors());
      try {
         assertEquals(false, fs.getBATBlockAndIndex(237*128).getBlock().hasFreeSectors());
         fail("Should only be 237 BATs");
      } catch(IndexOutOfBoundsException e) {}
      
      
      // Check the counts now
      assertBATCount(fs, 237, 2);

      // Check the header
      header = writeOutAndReadHeader(fs);
      
      
      // Now, write it out, and read it back in again fully
      fs = writeOutAndReadBack(fs);

      // Check that it is seen correctly
      assertBATCount(fs, 237, 2);

      assertEquals(false, fs.getBATBlockAndIndex(236*128-1).getBlock().hasFreeSectors());
      assertEquals(true, fs.getBATBlockAndIndex(237*128-1).getBlock().hasFreeSectors());
      try {
         assertEquals(false, fs.getBATBlockAndIndex(237*128).getBlock().hasFreeSectors());
         fail("Should only be 237 BATs");
      } catch(IndexOutOfBoundsException e) {}

      
      // All done
      fs.close();
   }
   
   /**
    * Test that we can correctly get the list of directory
    *  entries, and the details on the files in them
    */
   @Test
   public void listEntries() throws Exception {
      for(NPOIFSFileSystem fs : get512and4kFileAndInput()) {
         DirectoryEntry root = fs.getRoot();
         assertEquals(5, root.getEntryCount());
         
         // Check by the names
         Entry thumbnail = root.getEntry("Thumbnail");
         Entry dsi = root.getEntry("\u0005DocumentSummaryInformation");
         Entry si = root.getEntry("\u0005SummaryInformation");
         Entry image = root.getEntry("Image");
         Entry tags = root.getEntry("Tags");
         
         assertEquals(false, thumbnail.isDirectoryEntry());
         assertEquals(false, dsi.isDirectoryEntry());
         assertEquals(false, si.isDirectoryEntry());
         assertEquals(true, image.isDirectoryEntry());
         assertEquals(false, tags.isDirectoryEntry());
         
         // Check via the iterator
         Iterator<Entry> it = root.getEntries();
         assertEquals(thumbnail.getName(), it.next().getName());
         assertEquals(dsi.getName(), it.next().getName());
         assertEquals(si.getName(), it.next().getName());
         assertEquals(image.getName(), it.next().getName());
         assertEquals(tags.getName(), it.next().getName());
         
         // Look inside another
         DirectoryEntry imageD = (DirectoryEntry)image;
         assertEquals(7, imageD.getEntryCount());
         
         fs.close();
      }
   }
   
   /**
    * Tests that we can get the correct contents for
    *  a document in the filesystem 
    */
   @Test
   public void getDocumentEntry() throws Exception {
      for(NPOIFSFileSystem fs : get512and4kFileAndInput()) {
         DirectoryEntry root = fs.getRoot();
         Entry si = root.getEntry("\u0005SummaryInformation");
         
         assertEquals(true, si.isDocumentEntry());
         DocumentNode doc = (DocumentNode)si;
         
         // Check we can read it
         assertContentsMatches(null, doc);
         
         // Now try to build the property set
         DocumentInputStream inp = new NDocumentInputStream(doc);
         PropertySet ps = PropertySetFactory.create(inp);
         SummaryInformation inf = (SummaryInformation)ps;
         
         // Check some bits in it
         assertEquals(null, inf.getApplicationName());
         assertEquals(null, inf.getAuthor());
         assertEquals(null, inf.getSubject());
         assertEquals(131333, inf.getOSVersion());
         
         // Finish with this one
         inp.close();
         
         
         // Try the other summary information
         si = root.getEntry("\u0005DocumentSummaryInformation");
         assertEquals(true, si.isDocumentEntry());
         doc = (DocumentNode)si;
         assertContentsMatches(null, doc);
         
         inp = new NDocumentInputStream(doc);
         ps = PropertySetFactory.create(inp);
         DocumentSummaryInformation dinf = (DocumentSummaryInformation)ps;
         assertEquals(131333, dinf.getOSVersion());
         
         fs.close();
      }
   }
   
   /**
    * Read a file, write it and read it again.
    * Then, alter+add some streams, write and read
    */
   @Test
   public void readWriteRead() throws Exception {
       SummaryInformation sinf = null;
       DocumentSummaryInformation dinf = null;
       DirectoryEntry root = null, testDir = null;
       
       for(NPOIFSFileSystem fs : get512and4kFileAndInput()) {
           // Check we can find the entries we expect
           root = fs.getRoot();
           assertEquals(5, root.getEntryCount());
           assertThat(root.getEntryNames(), hasItem("Thumbnail"));
           assertThat(root.getEntryNames(), hasItem("Image"));
           assertThat(root.getEntryNames(), hasItem("Tags"));
           assertThat(root.getEntryNames(), hasItem("\u0005DocumentSummaryInformation"));
           assertThat(root.getEntryNames(), hasItem("\u0005SummaryInformation"));

           
           // Write out, re-load
           fs = writeOutAndReadBack(fs);
           
           // Check they're still there
           root = fs.getRoot();
           assertEquals(5, root.getEntryCount());
           assertThat(root.getEntryNames(), hasItem("Thumbnail"));
           assertThat(root.getEntryNames(), hasItem("Image"));
           assertThat(root.getEntryNames(), hasItem("Tags"));
           assertThat(root.getEntryNames(), hasItem("\u0005DocumentSummaryInformation"));
           assertThat(root.getEntryNames(), hasItem("\u0005SummaryInformation"));
           
           
           // Check the contents of them - parse the summary block and check
           sinf = (SummaryInformation)PropertySetFactory.create(new NDocumentInputStream(
                   (DocumentEntry)root.getEntry(SummaryInformation.DEFAULT_STREAM_NAME)));
           assertEquals(131333, sinf.getOSVersion());
           
           dinf = (DocumentSummaryInformation)PropertySetFactory.create(new NDocumentInputStream(
                   (DocumentEntry)root.getEntry(DocumentSummaryInformation.DEFAULT_STREAM_NAME)));
           assertEquals(131333, dinf.getOSVersion());
           
           
           // Add a test mini stream
           testDir = root.createDirectory("Testing 123");
           testDir.createDirectory("Testing 456");
           testDir.createDirectory("Testing 789");
           byte[] mini = new byte[] { 42, 0, 1, 2, 3, 4, 42 };
           testDir.createDocument("Mini", new ByteArrayInputStream(mini));
           
           
           // Write out, re-load
           fs = writeOutAndReadBack(fs);
           
           root = fs.getRoot();
           testDir = (DirectoryEntry)root.getEntry("Testing 123");
           assertEquals(6, root.getEntryCount());
           assertThat(root.getEntryNames(), hasItem("Thumbnail"));
           assertThat(root.getEntryNames(), hasItem("Image"));
           assertThat(root.getEntryNames(), hasItem("Tags"));
           assertThat(root.getEntryNames(), hasItem("Testing 123"));
           assertThat(root.getEntryNames(), hasItem("\u0005DocumentSummaryInformation"));
           assertThat(root.getEntryNames(), hasItem("\u0005SummaryInformation"));

           
           // Check old and new are there
           sinf = (SummaryInformation)PropertySetFactory.create(new NDocumentInputStream(
                   (DocumentEntry)root.getEntry(SummaryInformation.DEFAULT_STREAM_NAME)));
           assertEquals(131333, sinf.getOSVersion());
           
           dinf = (DocumentSummaryInformation)PropertySetFactory.create(new NDocumentInputStream(
                   (DocumentEntry)root.getEntry(DocumentSummaryInformation.DEFAULT_STREAM_NAME)));
           assertEquals(131333, dinf.getOSVersion());

           assertContentsMatches(mini, (DocumentEntry)testDir.getEntry("Mini"));
           
           
           // Write out and read once more, just to be sure
           fs = writeOutAndReadBack(fs);
           
           root = fs.getRoot();
           testDir = (DirectoryEntry)root.getEntry("Testing 123");
           assertEquals(6, root.getEntryCount());
           assertThat(root.getEntryNames(), hasItem("Thumbnail"));
           assertThat(root.getEntryNames(), hasItem("Image"));
           assertThat(root.getEntryNames(), hasItem("Tags"));
           assertThat(root.getEntryNames(), hasItem("Testing 123"));
           assertThat(root.getEntryNames(), hasItem("\u0005DocumentSummaryInformation"));
           assertThat(root.getEntryNames(), hasItem("\u0005SummaryInformation"));

           sinf = (SummaryInformation)PropertySetFactory.create(new NDocumentInputStream(
                   (DocumentEntry)root.getEntry(SummaryInformation.DEFAULT_STREAM_NAME)));
           assertEquals(131333, sinf.getOSVersion());
           
           dinf = (DocumentSummaryInformation)PropertySetFactory.create(new NDocumentInputStream(
                   (DocumentEntry)root.getEntry(DocumentSummaryInformation.DEFAULT_STREAM_NAME)));
           assertEquals(131333, dinf.getOSVersion());

           assertContentsMatches(mini, (DocumentEntry)testDir.getEntry("Mini"));
           
           
           // Add a full stream, delete a full stream
           byte[] main4096 = new byte[4096];
           main4096[0] = -10;
           main4096[4095] = -11;
           testDir.createDocument("Normal4096", new ByteArrayInputStream(main4096));
           
           root.getEntry("Tags").delete();
           
           
           // Write out, re-load
           fs = writeOutAndReadBack(fs);

           // Check it's all there
           root = fs.getRoot();
           testDir = (DirectoryEntry)root.getEntry("Testing 123");
           assertEquals(5, root.getEntryCount());
           assertThat(root.getEntryNames(), hasItem("Thumbnail"));
           assertThat(root.getEntryNames(), hasItem("Image"));
           assertThat(root.getEntryNames(), hasItem("Testing 123"));
           assertThat(root.getEntryNames(), hasItem("\u0005DocumentSummaryInformation"));
           assertThat(root.getEntryNames(), hasItem("\u0005SummaryInformation"));

           
           // Check old and new are there
           sinf = (SummaryInformation)PropertySetFactory.create(new NDocumentInputStream(
                   (DocumentEntry)root.getEntry(SummaryInformation.DEFAULT_STREAM_NAME)));
           assertEquals(131333, sinf.getOSVersion());
           
           dinf = (DocumentSummaryInformation)PropertySetFactory.create(new NDocumentInputStream(
                   (DocumentEntry)root.getEntry(DocumentSummaryInformation.DEFAULT_STREAM_NAME)));
           assertEquals(131333, dinf.getOSVersion());

           assertContentsMatches(mini, (DocumentEntry)testDir.getEntry("Mini"));
           assertContentsMatches(main4096, (DocumentEntry)testDir.getEntry("Normal4096"));

           
           // Delete a directory, and add one more
           testDir.getEntry("Testing 456").delete();
           testDir.createDirectory("Testing ABC");
           
           
           // Save
           fs = writeOutAndReadBack(fs);

           // Check
           root = fs.getRoot();
           testDir = (DirectoryEntry)root.getEntry("Testing 123");
           
           assertEquals(5, root.getEntryCount());
           assertThat(root.getEntryNames(), hasItem("Thumbnail"));
           assertThat(root.getEntryNames(), hasItem("Image"));
           assertThat(root.getEntryNames(), hasItem("Testing 123"));
           assertThat(root.getEntryNames(), hasItem("\u0005DocumentSummaryInformation"));
           assertThat(root.getEntryNames(), hasItem("\u0005SummaryInformation"));
           
           assertEquals(4, testDir.getEntryCount());
           assertThat(testDir.getEntryNames(), hasItem("Mini"));
           assertThat(testDir.getEntryNames(), hasItem("Normal4096"));
           assertThat(testDir.getEntryNames(), hasItem("Testing 789"));
           assertThat(testDir.getEntryNames(), hasItem("Testing ABC"));
           
           
           // Add another mini stream
           byte[] mini2 = new byte[] { -42, 0, -1, -2, -3, -4, -42 };
           testDir.createDocument("Mini2", new ByteArrayInputStream(mini2));
           
           // Save, load, check
           fs = writeOutAndReadBack(fs);
           
           root = fs.getRoot();
           testDir = (DirectoryEntry)root.getEntry("Testing 123");
           
           assertEquals(5, root.getEntryCount());
           assertThat(root.getEntryNames(), hasItem("Thumbnail"));
           assertThat(root.getEntryNames(), hasItem("Image"));
           assertThat(root.getEntryNames(), hasItem("Testing 123"));
           assertThat(root.getEntryNames(), hasItem("\u0005DocumentSummaryInformation"));
           assertThat(root.getEntryNames(), hasItem("\u0005SummaryInformation"));
           
           assertEquals(5, testDir.getEntryCount());
           assertThat(testDir.getEntryNames(), hasItem("Mini"));
           assertThat(testDir.getEntryNames(), hasItem("Mini2"));
           assertThat(testDir.getEntryNames(), hasItem("Normal4096"));
           assertThat(testDir.getEntryNames(), hasItem("Testing 789"));
           assertThat(testDir.getEntryNames(), hasItem("Testing ABC"));

           assertContentsMatches(mini, (DocumentEntry)testDir.getEntry("Mini"));
           assertContentsMatches(mini2, (DocumentEntry)testDir.getEntry("Mini2"));
           assertContentsMatches(main4096, (DocumentEntry)testDir.getEntry("Normal4096"));

           
           // Delete a mini stream, add one more
           testDir.getEntry("Mini").delete();
           
           byte[] mini3 = new byte[] { 42, 0, 42, 0, 42, 0, 42 };
           testDir.createDocument("Mini3", new ByteArrayInputStream(mini3));

           
           // Save, load, check
           fs = writeOutAndReadBack(fs);
           
           root = fs.getRoot();
           testDir = (DirectoryEntry)root.getEntry("Testing 123");
           
           assertEquals(5, root.getEntryCount());
           assertThat(root.getEntryNames(), hasItem("Thumbnail"));
           assertThat(root.getEntryNames(), hasItem("Image"));
           assertThat(root.getEntryNames(), hasItem("Testing 123"));
           assertThat(root.getEntryNames(), hasItem("\u0005DocumentSummaryInformation"));
           assertThat(root.getEntryNames(), hasItem("\u0005SummaryInformation"));
           
           assertEquals(5, testDir.getEntryCount());
           assertThat(testDir.getEntryNames(), hasItem("Mini2"));
           assertThat(testDir.getEntryNames(), hasItem("Mini3"));
           assertThat(testDir.getEntryNames(), hasItem("Normal4096"));
           assertThat(testDir.getEntryNames(), hasItem("Testing 789"));
           assertThat(testDir.getEntryNames(), hasItem("Testing ABC"));

           assertContentsMatches(mini2, (DocumentEntry)testDir.getEntry("Mini2"));
           assertContentsMatches(mini3, (DocumentEntry)testDir.getEntry("Mini3"));
           assertContentsMatches(main4096, (DocumentEntry)testDir.getEntry("Normal4096"));
           
           
           // Change some existing streams
           NPOIFSDocument mini2Doc = new NPOIFSDocument((DocumentNode)testDir.getEntry("Mini2"));
           mini2Doc.replaceContents(new ByteArrayInputStream(mini));
           
           byte[] main4106 = new byte[4106];
           main4106[0] = 41;
           main4106[4105] = 42;
           NPOIFSDocument mainDoc = new NPOIFSDocument((DocumentNode)testDir.getEntry("Normal4096"));
           mainDoc.replaceContents(new ByteArrayInputStream(main4106));
           
           
           // Re-check
           fs = writeOutAndReadBack(fs);
           
           root = fs.getRoot();
           testDir = (DirectoryEntry)root.getEntry("Testing 123");
           
           assertEquals(5, root.getEntryCount());
           assertThat(root.getEntryNames(), hasItem("Thumbnail"));
           assertThat(root.getEntryNames(), hasItem("Image"));
           assertThat(root.getEntryNames(), hasItem("Testing 123"));
           assertThat(root.getEntryNames(), hasItem("\u0005DocumentSummaryInformation"));
           assertThat(root.getEntryNames(), hasItem("\u0005SummaryInformation"));
           
           assertEquals(5, testDir.getEntryCount());
           assertThat(testDir.getEntryNames(), hasItem("Mini2"));
           assertThat(testDir.getEntryNames(), hasItem("Mini3"));
           assertThat(testDir.getEntryNames(), hasItem("Normal4096"));
           assertThat(testDir.getEntryNames(), hasItem("Testing 789"));
           assertThat(testDir.getEntryNames(), hasItem("Testing ABC"));

           assertContentsMatches(mini, (DocumentEntry)testDir.getEntry("Mini2"));
           assertContentsMatches(mini3, (DocumentEntry)testDir.getEntry("Mini3"));
           assertContentsMatches(main4106, (DocumentEntry)testDir.getEntry("Normal4096"));
           
           
           // All done
           fs.close();
       }
   }
   
   /**
    * Create a new file, write it and read it again
    * Then, add some streams, write and read
    */
   @Test
   @SuppressWarnings("resource")
   public void createWriteRead() throws Exception {
      NPOIFSFileSystem fs = new NPOIFSFileSystem();
      DocumentEntry miniDoc;
      DocumentEntry normDoc;
      
      // Initially has Properties + BAT but not SBAT
      assertEquals(POIFSConstants.END_OF_CHAIN, fs.getNextBlock(0));
      assertEquals(POIFSConstants.FAT_SECTOR_BLOCK, fs.getNextBlock(1));
      assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(2));
      
      // Check that the SBAT is empty
      assertEquals(POIFSConstants.END_OF_CHAIN, fs.getRoot().getProperty().getStartBlock());
      
      // Check that properties table was given block 0
      assertEquals(0, fs._get_property_table().getStartBlock());

      // Write and read it
      fs = writeOutAndReadBack(fs);
      
      // No change, SBAT remains empty 
      assertEquals(POIFSConstants.END_OF_CHAIN,     fs.getNextBlock(0));
      assertEquals(POIFSConstants.FAT_SECTOR_BLOCK, fs.getNextBlock(1));
      assertEquals(POIFSConstants.UNUSED_BLOCK,     fs.getNextBlock(2));
      assertEquals(POIFSConstants.UNUSED_BLOCK,     fs.getNextBlock(3));
      assertEquals(POIFSConstants.END_OF_CHAIN,     fs.getRoot().getProperty().getStartBlock());
      assertEquals(0, fs._get_property_table().getStartBlock());

      
      // Put everything within a new directory
      DirectoryEntry testDir = fs.createDirectory("Test Directory");
      
      // Add a new Normal Stream (Normal Streams minimum 4096 bytes)
      byte[] main4096 = new byte[4096];
      main4096[0] = -10;
      main4096[4095] = -11;
      testDir.createDocument("Normal4096", new ByteArrayInputStream(main4096));

      assertEquals(POIFSConstants.END_OF_CHAIN,     fs.getNextBlock(0));
      assertEquals(POIFSConstants.FAT_SECTOR_BLOCK, fs.getNextBlock(1));
      assertEquals(3, fs.getNextBlock(2));
      assertEquals(4, fs.getNextBlock(3));
      assertEquals(5, fs.getNextBlock(4));
      assertEquals(6, fs.getNextBlock(5));
      assertEquals(7, fs.getNextBlock(6));
      assertEquals(8, fs.getNextBlock(7));
      assertEquals(9, fs.getNextBlock(8));
      assertEquals(POIFSConstants.END_OF_CHAIN, fs.getNextBlock(9));
      assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(10));
      assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(11));
      // SBAT still unused
      assertEquals(POIFSConstants.END_OF_CHAIN, fs.getRoot().getProperty().getStartBlock());

      
      // Add a bigger Normal Stream
      byte[] main5124 = new byte[5124];
      main5124[0] = -22;
      main5124[5123] = -33;
      testDir.createDocument("Normal5124", new ByteArrayInputStream(main5124));

      assertEquals(POIFSConstants.END_OF_CHAIN,     fs.getNextBlock(0));
      assertEquals(POIFSConstants.FAT_SECTOR_BLOCK, fs.getNextBlock(1));
      assertEquals(3, fs.getNextBlock(2));
      assertEquals(4, fs.getNextBlock(3));
      assertEquals(5, fs.getNextBlock(4));
      assertEquals(6, fs.getNextBlock(5));
      assertEquals(7, fs.getNextBlock(6));
      assertEquals(8, fs.getNextBlock(7));
      assertEquals(9, fs.getNextBlock(8));
      assertEquals(POIFSConstants.END_OF_CHAIN, fs.getNextBlock(9));

      assertEquals(11, fs.getNextBlock(10));
      assertEquals(12, fs.getNextBlock(11));
      assertEquals(13, fs.getNextBlock(12));
      assertEquals(14, fs.getNextBlock(13));
      assertEquals(15, fs.getNextBlock(14));
      assertEquals(16, fs.getNextBlock(15));
      assertEquals(17, fs.getNextBlock(16));
      assertEquals(18, fs.getNextBlock(17));
      assertEquals(19, fs.getNextBlock(18));
      assertEquals(20, fs.getNextBlock(19));
      assertEquals(POIFSConstants.END_OF_CHAIN, fs.getNextBlock(20));
      assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(21));
      assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(22));

      assertEquals(POIFSConstants.END_OF_CHAIN, fs.getRoot().getProperty().getStartBlock());
      
      
      // Now Add a mini stream
      byte[] mini = new byte[] { 42, 0, 1, 2, 3, 4, 42 };
      testDir.createDocument("Mini", new ByteArrayInputStream(mini));
      
      // Mini stream will get one block for fat + one block for data
      assertEquals(POIFSConstants.END_OF_CHAIN,     fs.getNextBlock(0));
      assertEquals(POIFSConstants.FAT_SECTOR_BLOCK, fs.getNextBlock(1));
      assertEquals(3, fs.getNextBlock(2));
      assertEquals(4, fs.getNextBlock(3));
      assertEquals(5, fs.getNextBlock(4));
      assertEquals(6, fs.getNextBlock(5));
      assertEquals(7, fs.getNextBlock(6));
      assertEquals(8, fs.getNextBlock(7));
      assertEquals(9, fs.getNextBlock(8));
      assertEquals(POIFSConstants.END_OF_CHAIN, fs.getNextBlock(9));

      assertEquals(11, fs.getNextBlock(10));
      assertEquals(12, fs.getNextBlock(11));
      assertEquals(13, fs.getNextBlock(12));
      assertEquals(14, fs.getNextBlock(13));
      assertEquals(15, fs.getNextBlock(14));
      assertEquals(16, fs.getNextBlock(15));
      assertEquals(17, fs.getNextBlock(16));
      assertEquals(18, fs.getNextBlock(17));
      assertEquals(19, fs.getNextBlock(18));
      assertEquals(20, fs.getNextBlock(19));
      assertEquals(POIFSConstants.END_OF_CHAIN, fs.getNextBlock(20));
      assertEquals(POIFSConstants.END_OF_CHAIN, fs.getNextBlock(21));
      assertEquals(POIFSConstants.END_OF_CHAIN, fs.getNextBlock(22));
      assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(23));

      // Check the mini stream location was set
      // (21 is mini fat, 22 is first mini stream block)
      assertEquals(22, fs.getRoot().getProperty().getStartBlock());
      
      
      // Write and read back
      fs = writeOutAndReadBack(fs);
      HeaderBlock header = writeOutAndReadHeader(fs);
      
      // Check the header has the right points in it
      assertEquals(1, header.getBATCount());
      assertEquals(1, header.getBATArray()[0]);
      assertEquals(0, header.getPropertyStart());
      assertEquals(1, header.getSBATCount());
      assertEquals(21, header.getSBATStart());
      assertEquals(22, fs._get_property_table().getRoot().getStartBlock());
      
      // Block use should be almost the same, except the properties
      //  stream will have grown out to cover 2 blocks
      // Check the block use is all unchanged
      assertEquals(23, fs.getNextBlock(0)); // Properties now extends over 2 blocks
      assertEquals(POIFSConstants.FAT_SECTOR_BLOCK, fs.getNextBlock(1));
      
      assertEquals(3, fs.getNextBlock(2));
      assertEquals(4, fs.getNextBlock(3));
      assertEquals(5, fs.getNextBlock(4));
      assertEquals(6, fs.getNextBlock(5));
      assertEquals(7, fs.getNextBlock(6));
      assertEquals(8, fs.getNextBlock(7));
      assertEquals(9, fs.getNextBlock(8));
      assertEquals(POIFSConstants.END_OF_CHAIN, fs.getNextBlock(9)); // End of normal4096

      assertEquals(11, fs.getNextBlock(10));
      assertEquals(12, fs.getNextBlock(11));
      assertEquals(13, fs.getNextBlock(12));
      assertEquals(14, fs.getNextBlock(13));
      assertEquals(15, fs.getNextBlock(14));
      assertEquals(16, fs.getNextBlock(15));
      assertEquals(17, fs.getNextBlock(16));
      assertEquals(18, fs.getNextBlock(17));
      assertEquals(19, fs.getNextBlock(18));
      assertEquals(20, fs.getNextBlock(19));
      assertEquals(POIFSConstants.END_OF_CHAIN, fs.getNextBlock(20)); // End of normal5124 
      
      assertEquals(POIFSConstants.END_OF_CHAIN, fs.getNextBlock(21)); // Mini Stream FAT
      assertEquals(POIFSConstants.END_OF_CHAIN, fs.getNextBlock(22)); // Mini Stream data
      assertEquals(POIFSConstants.END_OF_CHAIN, fs.getNextBlock(23)); // Properties #2
      assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(24));

      
      // Check some data
      assertEquals(1, fs.getRoot().getEntryCount());
      testDir = (DirectoryEntry)fs.getRoot().getEntry("Test Directory");
      assertEquals(3, testDir.getEntryCount());

      miniDoc = (DocumentEntry)testDir.getEntry("Mini");
      assertContentsMatches(mini, miniDoc);
      
      normDoc = (DocumentEntry)testDir.getEntry("Normal4096");
      assertContentsMatches(main4096, normDoc);

      normDoc = (DocumentEntry)testDir.getEntry("Normal5124");
      assertContentsMatches(main5124, normDoc);
      
      
      // Delete a couple of streams
      miniDoc.delete();
      normDoc.delete();

      
      // Check - will have un-used sectors now
      fs = writeOutAndReadBack(fs);
      
      assertEquals(POIFSConstants.END_OF_CHAIN,     fs.getNextBlock(0)); // Props back in 1 block
      assertEquals(POIFSConstants.FAT_SECTOR_BLOCK, fs.getNextBlock(1));
      
      assertEquals(3, fs.getNextBlock(2));
      assertEquals(4, fs.getNextBlock(3));
      assertEquals(5, fs.getNextBlock(4));
      assertEquals(6, fs.getNextBlock(5));
      assertEquals(7, fs.getNextBlock(6));
      assertEquals(8, fs.getNextBlock(7));
      assertEquals(9, fs.getNextBlock(8));
      assertEquals(POIFSConstants.END_OF_CHAIN, fs.getNextBlock(9)); // End of normal4096

      assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(10));
      assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(11));
      assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(12));
      assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(13));
      assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(14));
      assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(15));
      assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(16));
      assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(17));
      assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(18));
      assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(19));
      assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(20));
      
      assertEquals(POIFSConstants.END_OF_CHAIN, fs.getNextBlock(21)); // Mini Stream FAT
      assertEquals(POIFSConstants.END_OF_CHAIN, fs.getNextBlock(22)); // Mini Stream data
      assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(23)); // Properties gone
      assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(24));
      assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(25));
      
      // All done
      fs.close();
   }
   
   @Test
   public void addBeforeWrite() throws Exception {
       NPOIFSFileSystem fs = new NPOIFSFileSystem();
       DocumentEntry miniDoc;
       DocumentEntry normDoc;
       HeaderBlock hdr;
       
       // Initially has Properties + BAT but nothing else
       assertEquals(POIFSConstants.END_OF_CHAIN, fs.getNextBlock(0));
       assertEquals(POIFSConstants.FAT_SECTOR_BLOCK, fs.getNextBlock(1));
       assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(2));
       
       hdr = writeOutAndReadHeader(fs);
       // No mini stream, and no xbats
       // Will have fat then properties stream
       assertEquals(1, hdr.getBATCount());
       assertEquals(1, hdr.getBATArray()[0]);
       assertEquals(0, hdr.getPropertyStart());
       assertEquals(POIFSConstants.END_OF_CHAIN, hdr.getSBATStart());
       assertEquals(POIFSConstants.END_OF_CHAIN, hdr.getXBATIndex());
       assertEquals(POIFSConstants.SMALLER_BIG_BLOCK_SIZE*3, fs.size());
       
       
       // Get a clean filesystem to start with
       fs = new NPOIFSFileSystem();
       
       // Put our test files in a non-standard place
       DirectoryEntry parentDir = fs.createDirectory("Parent Directory");
       DirectoryEntry testDir = parentDir.createDirectory("Test Directory");
       
       
       // Add to the mini stream
       byte[] mini = new byte[] { 42, 0, 1, 2, 3, 4, 42 };
       testDir.createDocument("Mini", new ByteArrayInputStream(mini));
       
       // Add to the main stream
       byte[] main4096 = new byte[4096];
       main4096[0] = -10;
       main4096[4095] = -11;
       testDir.createDocument("Normal4096", new ByteArrayInputStream(main4096));

       
       // Check the mini stream was added, then the main stream
       assertEquals(POIFSConstants.END_OF_CHAIN, fs.getNextBlock(0));
       assertEquals(POIFSConstants.FAT_SECTOR_BLOCK, fs.getNextBlock(1)); 
       assertEquals(POIFSConstants.END_OF_CHAIN, fs.getNextBlock(2)); // Mini Fat
       assertEquals(POIFSConstants.END_OF_CHAIN, fs.getNextBlock(3)); // Mini Stream
       assertEquals(5,                           fs.getNextBlock(4)); // Main Stream
       assertEquals(6,                           fs.getNextBlock(5));
       assertEquals(7,                           fs.getNextBlock(6));
       assertEquals(8,                           fs.getNextBlock(7));
       assertEquals(9,                           fs.getNextBlock(8));
       assertEquals(10,                          fs.getNextBlock(9));
       assertEquals(11,                          fs.getNextBlock(10));
       assertEquals(POIFSConstants.END_OF_CHAIN, fs.getNextBlock(11));
       assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(12));
       assertEquals(POIFSConstants.SMALLER_BIG_BLOCK_SIZE*13, fs.size());
       
       
       // Check that we can read the right data pre-write
       miniDoc = (DocumentEntry)testDir.getEntry("Mini");
       assertContentsMatches(mini, miniDoc);

       normDoc = (DocumentEntry)testDir.getEntry("Normal4096");
       assertContentsMatches(main4096, normDoc);
       
       
       // Write, read, check
       hdr = writeOutAndReadHeader(fs);
       fs = writeOutAndReadBack(fs);
       
       // Check the header details - will have the sbat near the start,
       //  then the properties at the end
       assertEquals(1, hdr.getBATCount());
       assertEquals(1, hdr.getBATArray()[0]);
       assertEquals(2, hdr.getSBATStart());
       assertEquals(0, hdr.getPropertyStart());
       assertEquals(POIFSConstants.END_OF_CHAIN, hdr.getXBATIndex());
       
       // Check the block allocation is unchanged, other than
       //  the properties stream going in at the end
       assertEquals(12,                          fs.getNextBlock(0)); // Properties
       assertEquals(POIFSConstants.FAT_SECTOR_BLOCK, fs.getNextBlock(1));
       assertEquals(POIFSConstants.END_OF_CHAIN, fs.getNextBlock(2));
       assertEquals(POIFSConstants.END_OF_CHAIN, fs.getNextBlock(3));
       assertEquals(5,                           fs.getNextBlock(4));
       assertEquals(6,                           fs.getNextBlock(5));
       assertEquals(7,                           fs.getNextBlock(6));
       assertEquals(8,                           fs.getNextBlock(7));
       assertEquals(9,                           fs.getNextBlock(8));
       assertEquals(10,                          fs.getNextBlock(9));
       assertEquals(11,                          fs.getNextBlock(10));
       assertEquals(POIFSConstants.END_OF_CHAIN, fs.getNextBlock(11));
       assertEquals(POIFSConstants.END_OF_CHAIN, fs.getNextBlock(12));
       assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(13));
       assertEquals(POIFSConstants.SMALLER_BIG_BLOCK_SIZE*14, fs.size());
       
       
       // Check the data
       DirectoryEntry fsRoot = fs.getRoot();
       assertEquals(1, fsRoot.getEntryCount());
       
       parentDir = (DirectoryEntry)fsRoot.getEntry("Parent Directory");
       assertEquals(1, parentDir.getEntryCount());
       
       testDir = (DirectoryEntry)parentDir.getEntry("Test Directory");
       assertEquals(2, testDir.getEntryCount());

       miniDoc = (DocumentEntry)testDir.getEntry("Mini");
       assertContentsMatches(mini, miniDoc);

       normDoc = (DocumentEntry)testDir.getEntry("Normal4096");
       assertContentsMatches(main4096, normDoc);
       
       
       // Add one more stream to each, then save and re-load
       byte[] mini2 = new byte[] { -42, 0, -1, -2, -3, -4, -42 };
       testDir.createDocument("Mini2", new ByteArrayInputStream(mini2));
       
       // Add to the main stream
       byte[] main4106 = new byte[4106];
       main4106[0] = 41;
       main4106[4105] = 42;
       testDir.createDocument("Normal4106", new ByteArrayInputStream(main4106));
       
       
       // Recheck the data in all 4 streams
       fs = writeOutAndReadBack(fs);
       
       fsRoot = fs.getRoot();
       assertEquals(1, fsRoot.getEntryCount());
       
       parentDir = (DirectoryEntry)fsRoot.getEntry("Parent Directory");
       assertEquals(1, parentDir.getEntryCount());
       
       testDir = (DirectoryEntry)parentDir.getEntry("Test Directory");
       assertEquals(4, testDir.getEntryCount());

       miniDoc = (DocumentEntry)testDir.getEntry("Mini");
       assertContentsMatches(mini, miniDoc);

       miniDoc = (DocumentEntry)testDir.getEntry("Mini2");
       assertContentsMatches(mini2, miniDoc);

       normDoc = (DocumentEntry)testDir.getEntry("Normal4106");
       assertContentsMatches(main4106, normDoc);
       
       // All done
       fs.close();
   }
   
   @Test
   public void readZeroLengthEntries() throws Exception {
       NPOIFSFileSystem fs = new NPOIFSFileSystem(_inst.getFile("only-zero-byte-streams.ole2"));
       DirectoryNode testDir = fs.getRoot();
       assertEquals(3, testDir.getEntryCount());
       DocumentEntry entry;
       
       entry = (DocumentEntry)testDir.getEntry("test-zero-1");
       assertNotNull(entry);
       assertEquals(0, entry.getSize());
       
       entry = (DocumentEntry)testDir.getEntry("test-zero-2");
       assertNotNull(entry);
       assertEquals(0, entry.getSize());
       
       entry = (DocumentEntry)testDir.getEntry("test-zero-3");
       assertNotNull(entry);
       assertEquals(0, entry.getSize());
       
       // Check properties, all have zero length, no blocks
       NPropertyTable props = fs._get_property_table();
       assertEquals(POIFSConstants.END_OF_CHAIN, props.getRoot().getStartBlock());
       for (Property prop : props.getRoot()) {
           assertEquals("test-zero-", prop.getName().substring(0, 10));
           assertEquals(POIFSConstants.END_OF_CHAIN, prop.getStartBlock());
       }
       
       // All done
       fs.close();
   }
   
   @Test
   public void writeZeroLengthEntries() throws Exception {
       NPOIFSFileSystem fs = new NPOIFSFileSystem();
       DirectoryNode testDir = fs.getRoot();
       DocumentEntry miniDoc;
       DocumentEntry normDoc;
       DocumentEntry emptyDoc;
       
       // Add mini and normal sized entries to start
       byte[] mini2 = new byte[] { -42, 0, -1, -2, -3, -4, -42 };
       testDir.createDocument("Mini2", new ByteArrayInputStream(mini2));
       
       // Add to the main stream
       byte[] main4106 = new byte[4106];
       main4106[0] = 41;
       main4106[4105] = 42;
       testDir.createDocument("Normal4106", new ByteArrayInputStream(main4106));
       
       // Now add some empty ones
       byte[] empty = new byte[0];
       testDir.createDocument("empty-1", new ByteArrayInputStream(empty));
       testDir.createDocument("empty-2", new ByteArrayInputStream(empty));
       testDir.createDocument("empty-3", new ByteArrayInputStream(empty));
       
       // Check
       miniDoc = (DocumentEntry)testDir.getEntry("Mini2");
       assertContentsMatches(mini2, miniDoc);

       normDoc = (DocumentEntry)testDir.getEntry("Normal4106");
       assertContentsMatches(main4106, normDoc);
       
       emptyDoc = (DocumentEntry)testDir.getEntry("empty-1");
       assertContentsMatches(empty, emptyDoc);
       
       emptyDoc = (DocumentEntry)testDir.getEntry("empty-2");
       assertContentsMatches(empty, emptyDoc);
       
       emptyDoc = (DocumentEntry)testDir.getEntry("empty-3");
       assertContentsMatches(empty, emptyDoc);
       
       // Look at the properties entry, and check the empty ones
       //  have zero size and no start block
       NPropertyTable props = fs._get_property_table();
       Iterator<Property> propsIt = props.getRoot().getChildren();
       
       Property prop = propsIt.next();
       assertEquals("Mini2", prop.getName());
       assertEquals(0, prop.getStartBlock());
       assertEquals(7, prop.getSize());
       
       prop = propsIt.next();
       assertEquals("Normal4106", prop.getName());
       assertEquals(4, prop.getStartBlock()); // BAT, Props, SBAT, MIni
       assertEquals(4106, prop.getSize());
       
       prop = propsIt.next();
       assertEquals("empty-1", prop.getName());
       assertEquals(POIFSConstants.END_OF_CHAIN, prop.getStartBlock());
       assertEquals(0, prop.getSize());
       
       prop = propsIt.next();
       assertEquals("empty-2", prop.getName());
       assertEquals(POIFSConstants.END_OF_CHAIN, prop.getStartBlock());
       assertEquals(0, prop.getSize());
       
       prop = propsIt.next();
       assertEquals("empty-3", prop.getName());
       assertEquals(POIFSConstants.END_OF_CHAIN, prop.getStartBlock());
       assertEquals(0, prop.getSize());
       
       
       // Save and re-check
       fs = writeOutAndReadBack(fs);
       testDir = fs.getRoot();
       
       miniDoc = (DocumentEntry)testDir.getEntry("Mini2");
       assertContentsMatches(mini2, miniDoc);

       normDoc = (DocumentEntry)testDir.getEntry("Normal4106");
       assertContentsMatches(main4106, normDoc);
       
       emptyDoc = (DocumentEntry)testDir.getEntry("empty-1");
       assertContentsMatches(empty, emptyDoc);
       
       emptyDoc = (DocumentEntry)testDir.getEntry("empty-2");
       assertContentsMatches(empty, emptyDoc);
       
       emptyDoc = (DocumentEntry)testDir.getEntry("empty-3");
       assertContentsMatches(empty, emptyDoc);
       
       // Check that a mini-stream was assigned, with one block used
       assertEquals(3, testDir.getProperty().getStartBlock()); 
       assertEquals(64, testDir.getProperty().getSize()); 
       
       // All done
       fs.close();
   }

   /**
    * Test that we can read a file with NPOIFS, create a new NPOIFS instance,
    *  write it out, read it with POIFS, and see the original data
    */
   @Test
   public void NPOIFSReadCopyWritePOIFSRead() throws Exception {
       File testFile = POIDataSamples.getSpreadSheetInstance().getFile("Simple.xls");
       NPOIFSFileSystem src = new NPOIFSFileSystem(testFile);
       byte wbDataExp[] = IOUtils.toByteArray(src.createDocumentInputStream("Workbook"));
       
       NPOIFSFileSystem nfs = new NPOIFSFileSystem();
       EntryUtils.copyNodes(src.getRoot(), nfs.getRoot());
       src.close();

       ByteArrayOutputStream bos = new ByteArrayOutputStream();
       nfs.writeFilesystem(bos);
       nfs.close();
       
       POIFSFileSystem pfs = new POIFSFileSystem(new ByteArrayInputStream(bos.toByteArray()));
       byte wbDataAct[] = IOUtils.toByteArray(pfs.createDocumentInputStream("Workbook"));
       
       assertThat(wbDataExp, equalTo(wbDataAct));
   }
}
