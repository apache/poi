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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.HashMap;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hpsf.NoPropertySetStreamException;
import org.apache.poi.hpsf.Property;
import org.apache.poi.hpsf.PropertySet;
import org.apache.poi.hpsf.Section;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.poifs.common.POIFSBigBlockSize;
import org.apache.poi.poifs.common.POIFSConstants;
import org.apache.poi.poifs.storage.BATBlock;
import org.apache.poi.poifs.storage.HeaderBlock;
import org.apache.poi.util.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests for the older OPOIFS-based POIFSFileSystem
 */
final class TestPOIFSFileSystem {
   private final POIDataSamples _samples = POIDataSamples.getPOIFSInstance();

	/**
	 * Mock exception used to ensure correct error handling
	 */
	private static final class MyEx extends RuntimeException {
		MyEx() {
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

		TestIS(InputStream is, int failIndex) {
			_is = is;
			_failIndex = failIndex;
			_currentIx = 0;
			_isClosed = false;
		}

		@Override
		public int read() throws IOException {
			int result = _is.read();
			if(result >=0) {
				checkRead(1);
			}
			return result;
		}
		@Override
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
		@Override
        public void close() throws IOException {
			_isClosed = true;
			_is.close();
		}
		boolean isClosed() {
			return _isClosed;
		}
	}

	/**
	 * Test for undesired behaviour observable as of svn revision 618865 (5-Feb-2008).
	 * POIFSFileSystem was not closing the input stream.
	 */
	@Test
	void testAlwaysClose() throws IOException {
		// Normal case - read until EOF and close
		try (TestIS testIS = new TestIS(openSampleStream("13224.xls"), -1);
			POIFSFileSystem ignored = new POIFSFileSystem(testIS)){
			assertTrue(testIS.isClosed(), "input stream was not closed");
		}

		// intended to crash after reading 10000 bytes
		try (TestIS testIS = new TestIS(openSampleStream("13224.xls"), 10000)){
			assertThrows(MyEx.class, () -> new POIFSFileSystem(testIS));
			// but still should close
			assertTrue(testIS.isClosed(), "input stream was not closed");
		}
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
	@ParameterizedTest
	@CsvSource({ "ShortLastBlock.qwp, 1303681", "ShortLastBlock.wps, 140787" })
	void testShortLastBlock(String file, int size) throws Exception {
		// Open the file up
		try (POIFSFileSystem fs = new POIFSFileSystem(_samples.openResourceAsStream(file))) {

			// Write it into a temp output array
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			fs.writeFilesystem(baos);

			// Check sizes
			assertEquals(size, baos.size());
		}
	}

	/**
	 * Check that we do the right thing when the list of which
	 *  sectors are BAT blocks points off the list of
	 *  sectors that exist in the file.
	 */
	@Test
	void testFATandDIFATsectors() throws Exception {
		try (InputStream stream = _samples.openResourceAsStream("ReferencesInvalidSectors.mpp")) {
			IndexOutOfBoundsException ex = assertThrows(
				IndexOutOfBoundsException.class,
				() -> new POIFSFileSystem(stream),
				"File is corrupt and shouldn't have been opened"
			);
			assertTrue(ex.getMessage().contains("Block 1148 not found"));
		}
	}

	/**
	 * Tests that we can write and read a file that contains XBATs
	 *  as well as regular BATs.
	 * However, because a file needs to be at least 6.875mb big
	 *  to have an XBAT in it, we don't have a test one. So, generate it.
	 */
	@Test
	void testBATandXBAT() throws Exception {
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


	   // Now load it and check
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
	@Test
	void test4KBlocks() throws Exception {
        POIDataSamples _samples = POIDataSamples.getPOIFSInstance();
		try (InputStream inp = _samples.openResourceAsStream("BlockSize4096.zvi")) {
			// First up, check that we can process the header properly
			HeaderBlock header_block = new HeaderBlock(inp);
			POIFSBigBlockSize bigBlockSize = header_block.getBigBlockSize();
			assertEquals(4096, bigBlockSize.getBigBlockSize());

			// Check the fat info looks sane
			assertEquals(1, header_block.getBATArray().length);
			assertEquals(1, header_block.getBATCount());
			assertEquals(0, header_block.getXBATCount());

			// Now try and open properly
			POIFSFileSystem fs = new POIFSFileSystem(
					_samples.openResourceAsStream("BlockSize4096.zvi"));
			assertTrue(fs.getRoot().getEntryCount() > 3);

			// Check we can get at all the contents
			checkAllDirectoryContents(fs.getRoot());

			// Finally, check we can do a similar 512byte one too
			fs = new POIFSFileSystem(
					_samples.openResourceAsStream("BlockSize512.zvi"));
			assertTrue(fs.getRoot().getEntryCount() > 3);
			checkAllDirectoryContents(fs.getRoot());
		}
	}
	private void checkAllDirectoryContents(DirectoryEntry dir) throws IOException {
	   for(Entry entry : dir) {
	      if(entry instanceof DirectoryEntry) {
	         checkAllDirectoryContents((DirectoryEntry)entry);
	      } else {
	         DocumentNode doc = (DocumentNode) entry;
	         try (DocumentInputStream dis = new DocumentInputStream(doc)) {
    	         IOUtils.toByteArray(dis);
	         }
	      }
	   }
	}

	@SuppressWarnings("SameParameterValue")
	private static InputStream openSampleStream(String sampleFileName) {
		return HSSFTestDataSamples.openSampleFileStream(sampleFileName);
	}

	@Test
	public void fileMagics() {
		for (FileMagic fm : FileMagic.values()) {
			if (fm == FileMagic.UNKNOWN) {
				continue;
			}
			for (byte[] b : fm.magic) {
				assertEquals(fm, FileMagic.valueOf(b));
			}
		}

		assertEquals(FileMagic.UNKNOWN, FileMagic.valueOf("foobaa".getBytes(UTF_8)));
	}

	@Test
	void test64322() throws NoPropertySetStreamException, IOException {
		try (POIFSFileSystem poiFS = new POIFSFileSystem(_samples.getFile("64322.ole2"))) {
			int count = recurseDir(poiFS.getRoot());

			assertEquals(1285, count, "Expecting a fixed number of entries being found in the test-document");
		}
	}

	@Test
	void test64322a() throws NoPropertySetStreamException, IOException {
		try (POIFSFileSystem poiFS = new POIFSFileSystem(_samples.openResourceAsStream("64322.ole2"))) {
			int count = recurseDir(poiFS.getRoot());

			assertEquals(1285, count, "Expecting a fixed number of entries being found in the test-document");
		}
	}

	private static int recurseDir(DirectoryEntry dir) throws IOException, NoPropertySetStreamException {
		int count = 0;
		for (Entry entry : dir) {
			count++;
			if (entry instanceof DirectoryEntry) {
				count += recurseDir((DirectoryEntry) entry);
			}
			if (entry instanceof DocumentEntry) {
				DocumentEntry de = (DocumentEntry) entry;
				HashMap<String, String> props = new HashMap<>();
				try (DocumentInputStream dis = new DocumentInputStream(de)) {
					props.put("name", de.getName());

					if (PropertySet.isPropertySetStream(dis)) {
						dis.mark(10000000);
						PropertySet ps = null;
						try {
							ps = new PropertySet(dis);

						} catch (UnsupportedEncodingException e) {
							// ignore
						}
						if (ps != null) {
							for (Section section : ps.getSections()) {
								for (Property p : section.getProperties()) {
									String prop = section.getDictionary() != null
											? section.getDictionary().get(p.getID())
											: String.valueOf(p.getID());
									if (p.getValue() != null)
										props.put("property_" + prop, p.getValue().toString());
								}
							}
						}
						dis.reset();
					}
				}
				assertTrue(props.size() > 0);
			}
		}
		return count;
	}
}
