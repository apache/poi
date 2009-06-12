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

package org.apache.poi.poifs.storage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Random;

import junit.framework.TestCase;

import org.apache.poi.util.DummyPOILogger;

/**
 * Class to test RawDataBlock functionality
 *
 * @author Marc Johnson
 */
public final class TestRawDataBlock extends TestCase {
	static {
		// We always want to use our own
		//  logger
		System.setProperty(
				"org.apache.poi.util.POILogger",
				"org.apache.poi.util.DummyPOILogger"
		);
	}

	/**
	 * Test creating a normal RawDataBlock
	 */
	public void testNormalConstructor() throws IOException {
		byte[] data = new byte[ 512 ];

		for (int j = 0; j < 512; j++)
		{
			data[ j ] = ( byte ) j;
		}
		RawDataBlock block = new RawDataBlock(new ByteArrayInputStream(data));

		assertTrue("Should not be at EOF", !block.eof());
		byte[] out_data = block.getData();

		assertEquals("Should be same length", data.length, out_data.length);
		for (int j = 0; j < 512; j++)
		{
			assertEquals("Should be same value at offset " + j, data[ j ],
						 out_data[ j ]);
		}
	}

	/**
	 * Test creating an empty RawDataBlock
	 */
	public void testEmptyConstructor() throws IOException {
		byte[]	   data  = new byte[ 0 ];
		RawDataBlock block = new RawDataBlock(new ByteArrayInputStream(data));

		assertTrue("Should be at EOF", block.eof());
		try
		{
			block.getData();
		}
		catch (IOException ignored)
		{

			// as expected
		}
	}

	/**
	 * Test creating a short RawDataBlock
	 * Will trigger a warning, but no longer an IOException,
	 *  as people seem to have "valid" truncated files
	 */
	public void testShortConstructor() throws Exception {
		// Get the logger to be used
		DummyPOILogger logger = new DummyPOILogger();
		Field fld = RawDataBlock.class.getDeclaredField("log");
		fld.setAccessible(true);
		fld.set(null, logger);
		assertEquals(0, logger.logged.size());

		// Test for various data sizes
		for (int k = 1; k <= 512; k++)
		{
			byte[] data = new byte[ k ];

			for (int j = 0; j < k; j++)
			{
				data[ j ] = ( byte ) j;
			}
			RawDataBlock block = null;

			logger.reset();
			assertEquals(0, logger.logged.size());

			// Have it created
			block = new RawDataBlock(new ByteArrayInputStream(data));
			assertNotNull(block);

			// Check for the warning is there for <512
			if(k < 512) {
				assertEquals(
						"Warning on " + k + " byte short block",
						1, logger.logged.size()
				);

				// Build the expected warning message, and check
				String bts = k + " byte";
				if(k > 1) {
					bts += "s";
				}

				assertEquals(
						"7 - Unable to read entire block; "+bts+" read before EOF; expected 512 bytes. Your document was either written by software that ignores the spec, or has been truncated!",
						(String)(logger.logged.get(0))
				);
			} else {
				assertEquals(0, logger.logged.size());
			}
		}
	}

	/**
	 * Tests that when using a slow input stream, which
	 *  won't return a full block at a time, we don't
	 *  incorrectly think that there's not enough data
	 */
	public void testSlowInputStream() throws Exception {
		// Get the logger to be used
		DummyPOILogger logger = new DummyPOILogger();
		Field fld = RawDataBlock.class.getDeclaredField("log");
		fld.setAccessible(true);
		fld.set(null, logger);
		assertEquals(0, logger.logged.size());

		// Test for various ok data sizes
		for (int k = 1; k < 512; k++) {
			byte[] data = new byte[ 512 ];
			for (int j = 0; j < data.length; j++) {
				data[j] = (byte) j;
			}

			// Shouldn't complain, as there is enough data,
			//  even if it dribbles through
			RawDataBlock block =
				new RawDataBlock(new SlowInputStream(data, k));
			assertFalse(block.eof());
		}

		// But if there wasn't enough data available, will
		//  complain
		for (int k = 1; k < 512; k++) {
			byte[] data = new byte[ 511 ];
			for (int j = 0; j < data.length; j++) {
				data[j] = (byte) j;
			}

			logger.reset();
			assertEquals(0, logger.logged.size());

			// Should complain, as there isn't enough data
			RawDataBlock block =
				new RawDataBlock(new SlowInputStream(data, k));
			assertNotNull(block);
			assertEquals(
					"Warning on " + k + " byte short block",
					1, logger.logged.size()
			);
		}
	}

	/**
	 * An input stream which will return a maximum of
	 *  a given number of bytes to read, and often claims
	 *  not to have any data
	 */
	public static class SlowInputStream extends InputStream {
		private Random rnd = new Random();
		private byte[] data;
		private int chunkSize;
		private int pos = 0;

		public SlowInputStream(byte[] data, int chunkSize) {
			this.chunkSize = chunkSize;
			this.data = data;
		}

		/**
		 * 75% of the time, claim there's no data available
		 */
		private boolean claimNoData() {
			if(rnd.nextFloat() < 0.25f) {
				return false;
			}
			return true;
		}

		public int read() {
			if(pos >= data.length) {
				return -1;
			}
			int ret = data[pos];
			pos++;

			if(ret < 0) ret += 256;
			return ret;
		}

		/**
		 * Reads the requested number of bytes, or the chunk
		 *  size, whichever is lower.
		 * Quite often will simply claim to have no data
		 */
		public int read(byte[] b, int off, int len) {
			// Keep the length within the chunk size
			if(len > chunkSize) {
				len = chunkSize;
			}
			// Don't read off the end of the data
			if(pos + len > data.length) {
				len = data.length - pos;

				// Spot when we're out of data
				if(len == 0) {
					return -1;
				}
			}

			// 75% of the time, claim there's no data
			if(claimNoData()) {
				return 0;
			}

			// Copy, and return what we read
			System.arraycopy(data, pos, b, off, len);
			pos += len;
			return len;
		}

		public int read(byte[] b) {
			return read(b, 0, b.length);
		}
	}
}
