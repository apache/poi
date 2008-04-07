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

import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

import org.apache.poi.hssf.HSSFTestDataSamples;

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

	private static InputStream openSampleStream(String sampleFileName) {
		return HSSFTestDataSamples.openSampleFileStream(sampleFileName);
	}
}
