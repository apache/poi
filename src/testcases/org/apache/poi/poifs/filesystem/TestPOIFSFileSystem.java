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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.poifs.common.POIFSBigBlockSize;
import org.apache.poi.poifs.storage.HeaderBlockReader;
import org.apache.poi.poifs.storage.RawDataBlockList;

/**
 * Tests for POIFSFileSystem
 *
 * @author Josh Micich
 */
public final class TestPOIFSFileSystem extends TestCase {

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

		POIDataSamples _samples = POIDataSamples.getPOIFSInstance();
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
      POIDataSamples _samples = POIDataSamples.getPOIFSInstance();
      
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
	 * Most OLE2 files use 512byte blocks. However, a small number
	 *  use 4k blocks. Check that we can open these.
	 */
	public void test4KBlocks() throws Exception {
      POIDataSamples _samples = POIDataSamples.getPOIFSInstance();
	   InputStream inp = _samples.openResourceAsStream("BlockSize4096.zvi");
	   
	   // First up, check that we can process the header properly
      HeaderBlockReader header_block_reader = new HeaderBlockReader(inp);
      POIFSBigBlockSize bigBlockSize = header_block_reader.getBigBlockSize();
      assertEquals(4096, bigBlockSize.getBigBlockSize());
      
      // Check the fat info looks sane
      assertEquals(109, header_block_reader.getBATArray().length);
      assertTrue(header_block_reader.getBATCount() > 0);
      assertEquals(0, header_block_reader.getXBATCount());
      
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

	private static InputStream openSampleStream(String sampleFileName) {
		return HSSFTestDataSamples.openSampleFileStream(sampleFileName);
	}
}
