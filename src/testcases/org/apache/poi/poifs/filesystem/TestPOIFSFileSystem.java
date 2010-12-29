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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Iterator;

import junit.framework.TestCase;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.poifs.common.POIFSBigBlockSize;
import org.apache.poi.poifs.common.POIFSConstants;
import org.apache.poi.poifs.storage.BATBlock;
import org.apache.poi.poifs.storage.BlockAllocationTableReader;
import org.apache.poi.poifs.storage.HeaderBlock;
import org.apache.poi.poifs.storage.RawDataBlockList;

/**
 * Tests for POIFSFileSystem
 *
 * @author Josh Micich
 */
public final class TestPOIFSFileSystem extends TestCase {
   private POIDataSamples _samples = POIDataSamples.getPOIFSInstance();
   

	/**
	 * Mock exception used to ensure correct error handling
	 */
	private static final class MyEx extends RuntimeException {
		public MyEx() {
			// no fields to initialise
		}
	}
	/**
	 * Helps facilitate testing. Keeps track of whether close() was called.
	 * Also can throw an exception at a specific point in the stream.
	 */
	private static final class TestIS extends InputStream {

		private final InputStream _is;
		private final int _failIndex;
		private int _currentIx;
		private boolean _isClosed;

		public TestIS(InputStream is, int failIndex) {
			_is = is;
			_failIndex = failIndex;
			_currentIx = 0;
			_isClosed = false;
		}

		public int read() throws IOException {
			int result = _is.read();
			if(result >=0) {
				checkRead(1);
			}
			return result;
		}
		public int read(byte[] b, int off, int len) throws IOException {
			int result = _is.read(b, off, len);
			checkRead(result);
			return result;
		}

		private void checkRead(int nBytes) {
			_currentIx += nBytes;
			if(_failIndex > 0 && _currentIx > _failIndex) {
				throw new MyEx();
			}
		}
		public void close() throws IOException {
			_isClosed = true;
			_is.close();
		}
		public boolean isClosed() {
			return _isClosed;
		}
	}

	/**
	 * Test for undesired behaviour observable as of svn revision 618865 (5-Feb-2008).
	 * POIFSFileSystem was not closing the input stream.
	 */
	public void testAlwaysClose() {
		TestIS testIS;

		// Normal case - read until EOF and close
		testIS = new TestIS(openSampleStream("13224.xls"), -1);
		try {
			new POIFSFileSystem(testIS);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		assertTrue("input stream was not closed", testIS.isClosed());

		// intended to crash after reading 10000 bytes
		testIS = new TestIS(openSampleStream("13224.xls"), 10000);
		try {
			new POIFSFileSystem(testIS);
			fail("ex should have been thrown");
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (MyEx e) {
			// expected
		}
		assertTrue("input stream was not closed", testIS.isClosed()); // but still should close
	}

	/**
	 * Test for bug # 48898 - problem opening an OLE2
	 *  file where the last block is short (i.e. not a full
	 *  multiple of 512 bytes)
	 *
	 * As yet, this problem remains. One school of thought is
	 *  not not issue an EOF when we discover the last block
	 *  is short, but this seems a bit wrong.
	 * The other is to fix the handling of the last block in
	 *  POIFS, since it seems to be slight wrong
	 */
	public void testShortLastBlock() throws Exception {
		String[] files = new String[] {
			"ShortLastBlock.qwp", "ShortLastBlock.wps"
		};

		for(int i=0; i<files.length; i++) {
			// Open the file up
			POIFSFileSystem fs = new POIFSFileSystem(
			    _samples.openResourceAsStream(files[i])
			);

			// Write it into a temp output array
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			fs.writeFilesystem(baos);

			// Check sizes
		}
	}
	
	/**
	 * Check that we do the right thing when the list of which
	 *  sectors are BAT blocks points off the list of
	 *  sectors that exist in the file.
	 */
	public void testFATandDIFATsectors() throws Exception {
      // Open the file up
      try {
         POIFSFileSystem fs = new POIFSFileSystem(
             _samples.openResourceAsStream("ReferencesInvalidSectors.mpp")
         );
         fail("File is corrupt and shouldn't have been opened");
      } catch(IOException e) {
         String msg = e.getMessage();
         assertTrue(msg.startsWith("Your file contains 695 sectors"));
      }
	}
	
	/**
	 * Tests that we can write and read a file that contains XBATs
	 *  as well as regular BATs.
	 * However, because a file needs to be at least 6.875mb big
	 *  to have an XBAT in it, we don't have a test one. So, generate it.
	 */
	public void testBATandXBAT() throws Exception {
	   byte[] hugeStream = new byte[8*1024*1024];
	   POIFSFileSystem fs = new POIFSFileSystem();
	   fs.getRoot().createDocument(
	         "BIG", new ByteArrayInputStream(hugeStream)
	   );
	   
	   ByteArrayOutputStream baos = new ByteArrayOutputStream();
	   fs.writeFilesystem(baos);
	   byte[] fsData = baos.toByteArray();
	   
	   
	   // Check the header was written properly
	   InputStream inp = new ByteArrayInputStream(fsData); 
	   HeaderBlock header = new HeaderBlock(inp);
	   assertEquals(109+21, header.getBATCount());
	   assertEquals(1, header.getXBATCount());
	   
	   
	   // We should have 21 BATs in the XBAT
	   ByteBuffer xbatData = ByteBuffer.allocate(512);
	   xbatData.put(fsData, (1+header.getXBATIndex())*512, 512);
	   xbatData.position(0);
	   BATBlock xbat = BATBlock.createBATBlock(POIFSConstants.SMALLER_BIG_BLOCK_SIZE_DETAILS, xbatData);
	   for(int i=0; i<21; i++) {
	      assertTrue(xbat.getValueAt(i) != POIFSConstants.UNUSED_BLOCK);
	   }
	   for(int i=21; i<127; i++) {
	      assertEquals(POIFSConstants.UNUSED_BLOCK, xbat.getValueAt(i));
	   }
	   assertEquals(POIFSConstants.END_OF_CHAIN, xbat.getValueAt(127));
	   
	   
	   // Load the blocks and check with that
	   RawDataBlockList blockList = new RawDataBlockList(inp, POIFSConstants.SMALLER_BIG_BLOCK_SIZE_DETAILS);
	   assertEquals(fsData.length / 512, blockList.blockCount() + 1); // Header not counted
	   new BlockAllocationTableReader(header.getBigBlockSize(),
            header.getBATCount(),
            header.getBATArray(),
            header.getXBATCount(),
            header.getXBATIndex(),
            blockList);
      assertEquals(fsData.length / 512, blockList.blockCount() + 1); // Header not counted
      
	   // Now load it and check
	   fs = null;
	   fs = new POIFSFileSystem(
	         new ByteArrayInputStream(fsData)
	   );
	   
	   DirectoryNode root = fs.getRoot();
	   assertEquals(1, root.getEntryCount());
	   DocumentNode big = (DocumentNode)root.getEntry("BIG");
	   assertEquals(hugeStream.length, big.getSize());
	}
	
	/**
	 * Most OLE2 files use 512byte blocks. However, a small number
	 *  use 4k blocks. Check that we can open these.
	 */
	public void test4KBlocks() throws Exception {
      POIDataSamples _samples = POIDataSamples.getPOIFSInstance();
	   InputStream inp = _samples.openResourceAsStream("BlockSize4096.zvi");
	   
	   // First up, check that we can process the header properly
      HeaderBlock header_block = new HeaderBlock(inp);
      POIFSBigBlockSize bigBlockSize = header_block.getBigBlockSize();
      assertEquals(4096, bigBlockSize.getBigBlockSize());
      
      // Check the fat info looks sane
      assertEquals(1, header_block.getBATArray().length);
      assertEquals(1, header_block.getBATCount());
      assertEquals(0, header_block.getXBATCount());
      
      // Now check we can get the basic fat
      RawDataBlockList data_blocks = new RawDataBlockList(inp, bigBlockSize);

	   
	   // Now try and open properly
	   POIFSFileSystem fs = new POIFSFileSystem(
	         _samples.openResourceAsStream("BlockSize4096.zvi")
	   );
	   assertTrue(fs.getRoot().getEntryCount() > 3);
	   
	   // Check we can get at all the contents
	   checkAllDirectoryContents(fs.getRoot());
	   
	   
	   // Finally, check we can do a similar 512byte one too
	   fs = new POIFSFileSystem(
            _samples.openResourceAsStream("BlockSize512.zvi")
      );
      assertTrue(fs.getRoot().getEntryCount() > 3);
      checkAllDirectoryContents(fs.getRoot());
	}
	private void checkAllDirectoryContents(DirectoryEntry dir) throws IOException {
	   for(Entry entry : dir) {
	      if(entry instanceof DirectoryEntry) {
	         checkAllDirectoryContents((DirectoryEntry)entry);
	      } else {
	         DocumentNode doc = (DocumentNode) entry;
	         DocumentInputStream dis = new DocumentInputStream(doc);
	         int numBytes = dis.available();
	         byte[] data = new byte [numBytes];
            dis.read(data);
	      }
	   }
	}
	
	/**
	 * Test that we can open files that come via Lotus notes.
	 * These have a top level directory without a name....
	 */
	public void testNotesOLE2Files() throws Exception {
      POIDataSamples _samples = POIDataSamples.getPOIFSInstance();
      
      // Open the file up
      POIFSFileSystem fs = new POIFSFileSystem(
          _samples.openResourceAsStream("Notes.ole2")
      );
      
      // Check the contents
      assertEquals(1, fs.getRoot().getEntryCount());
      
      Entry entry = fs.getRoot().getEntries().next();
      assertTrue(entry.isDirectoryEntry());
      assertTrue(entry instanceof DirectoryEntry);
      
      // The directory lacks a name!
      DirectoryEntry dir = (DirectoryEntry)entry;
      assertEquals("", dir.getName());
      
      // Has two children
      assertEquals(2, dir.getEntryCount());
      
      // Check them
      Iterator<Entry> it = dir.getEntries();
      entry = it.next();
      assertEquals(true, entry.isDocumentEntry());
      assertEquals("\u0001Ole10Native", entry.getName());
      
      entry = it.next();
      assertEquals(true, entry.isDocumentEntry());
      assertEquals("\u0001CompObj", entry.getName());
	}

	private static InputStream openSampleStream(String sampleFileName) {
		return HSSFTestDataSamples.openSampleFileStream(sampleFileName);
	}
}
