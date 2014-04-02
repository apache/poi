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

package org.apache.poi.hdgf.streams;

import org.apache.poi.hdgf.pointers.Pointer;

public final class TestStreamBasics extends StreamTest {
	/** The header from when compressedStream is decompressed */
	public static final byte[] compressedStreamDCHeader = new byte[] {
		-60, 2, 0, 0
	};
	public static final byte[] compressedStream = new byte[] {
		123, -60, 2, -21, -16, 1, 0, 0, -72, -13, -16, 78, -32, -5, 1,
		0, 3, -21, -16, 10, 5, 4, -21, -16, 21, 9, -21, -16, 103, -21,
		-16, 34, -36, -1, 52, 15, 70, 15, 120, 88, 15, -7, -2, -28, -9,
		-123, 21, 0, 44, -122, 1, -4, 104, 15, -24, -13, 40, -98, 32,
		78, 102, -67, -1, -2, -30, 64, 40, -67, -113, -73, 116, -98,
		-85, 2, 66, 123, 9, 109, -85, 2, -89, 14, -56, -69, -83, -79,
		-34, -3, 120, 110, 75, -9, -10, 20, -6, -25, -12, 22, -21, -16,
		-12, -81, 67, 1, -128, -70, -21, -16, 84, -21, -16, 70, 0, 23,
		-21, -16, 76, 47, -40, 79, 1, -44, -21, -16, 32, 3, 18, 12, 17,
		-43, -68, 17, 16, -8, 21, 22, -1, -21, -16, -84, -1, -35, 79,
		-9, -10, 96, 0, 46, -21, -16, 44, -39, -41, 79, 1, 119, -13,
		-16, -106, -13, -16, 84, 0, 125, 26, -21, -16, 68, -38, 79, 1,
		17, 10, 0, -97, 50, 10, 0, 0, -42, -108, 15, 118, 31, 0, -3, 29,
		-21, -16, -100, -25, 79, 1, -18, 97, -36, 76, 16, -21, -16, 86,
		0, 36, -5, 1, -5, 79, 63, 1, -124, 98, 0, 0, 28, 3, 20, -34, -3,
		125, 33, -21, -16, 100, -4, 79, 1, -92, -91, 16, -22, 24, 19, 41,
		-21, -16, -44, -59, 16, 108, 100, 0, -21, 0, 71, -105, 18, 39, 85,
		17, -3, 79, 1, 95, -108, 113, 0, 0, 104, 3, 18, 49, 49, 17, -1, 64,
		85, 1, 0, 114, 0, 0, -93, -36, -21, -16, 100, 31, 0, 0, -40, -21,
		-16, -92, 66, 127, 85, 1, 98, 119, 0, 0, -48, 79, 18, -3, 50, -17,
		1, 67, 85, 1, 81, -127, 0, -41, 0, 14, 6, 4, 17, 63, -63, 17, 68,
		85, -65, 1, 30, -120, 0, 0, 42, 79, 18, 68, 126, -21, -16, -76, 69,
		85, 1, 102, -119, 72, 37, 0, 97, 33 };
	public static final byte[] uncompressedStream = new byte[] {
		0, 1, 0, 0, -72, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 1, 0, 3, 0, 0, 0, 3, 0, 0, 0, 3, 0, 0, 0, 4, 0, 0,
		0, 9, 0, 0, 0, 103, 0, 0, 0, 34, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		-123, 21, 0, 44, -123, 21, 0, 44, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 40, -98, 32, 78, 102, -67,
		-2, -30, 64, 40, -67, -113, -73, 116, -67, -2, -30, 64, 40, 66,
		123, 9, 109, -67, -2, -30, 64, 40, -98, 32, 78, 102, -67, -2, -30,
		64, 40, -67, -113, -73, 116, -67, -2, -30, 64, -56, -83, -79, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 120, 110, 75, 1, 0, 0, 0,
		0, 0, 0, 0, 0, 20, 0, 0, 0, 0, 0, 0, 0, 22, 0, 0, 0, -12, -81, 67,
		1, -128, 0, 0, 0, 84, 0, 0, 0, 70, 0, 23, 0, 0, 0, 76, -40, 79, 1,
		-44, 0, 0, 0, 32, 0, 0, 0, 84, 0, 23, 0, 0, 0, -68, -40, 79, 1, -8,
		0, 0, 0, 32, 0, 0, 0, 84, 0, -1, 0, 0, 0, -84, -1, 79, 1, 0, 0, 0,
		0, 0, 0, 0, 0, 96, 0, 46, 0, 0, 0, 44, -39, 79, 1, 119, 1, 0, 0,
		-106, 1, 0, 0, 84, 0, 26, 0, 0, 0, 68, -38, 79, 1, 17, 3, 0, 0,
		50, 10, 0, 0, -42, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		29, 0, 0, 0, -100, -25, 79, 1, -18, 97, 0, 0, -106, 0, 0, 0, 86, 0,
		36, 0, 0, 0, -12, -5, 79, 1, -124, 98, 0, 0, 28, 0, 0, 0, 84, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 33, 0, 0, 0, 100,
		-4, 79, 1, -92, 98, 0, 0, 32, 0, 0, 0, 84, 0, 41, 0, 0, 0, -44, -4,
		79, 1, 108, 100, 0, 0, 71, 0, 0, 0, 86, 0, 39, 0, 0, 0, 68, -3, 79,
		1, -108, 113, 0, 0, 104, 0, 0, 0, 84, 0, 49, 0, 0, 0, -84, 64, 85,
		1, 0, 114, 0, 0, -93, 0, 0, 0, -42, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, -40, 0, 0, 0, -92, 66, 85, 1, 98, 119,
		0, 0, -48, 1, 0, 0, 84, 0, 50, 0, 0, 0, 20, 67, 85, 1, 81, -127,
		0, 0, 14, 6, 0, 0, 84, 0, 63, 0, 0, 0, 100, 68, 85, 1, 30, -120,
		0, 0, 42, 1, 0, 0, 84, 0, 68, 0, 0, 0, -76, 69, 85, 1, 102, -119,
		0, 0, 42, 1, 0, 0, 84, 0, 0, 0, 0, 0
	};

	public void testCompressedStream() {
		// Create a fake pointer
		Pointer ptr = new TestPointer(true, 0, compressedStream.length, -1, (short)-1);
		// Now the stream
		Stream stream = Stream.createStream(ptr, compressedStream, null, null);

		// Check
		assertNotNull(stream.getPointer());
		assertNotNull(stream.getStore());
		assertTrue(stream.getStore() instanceof StreamStore);
		assertTrue(stream.getStore() instanceof CompressedStreamStore);
		assertTrue(stream instanceof UnknownStream);

		// Check the stream store
		CompressedStreamStore ss = (CompressedStreamStore)stream.getStore();
		assertEquals(4, ss._getBlockHeader().length);
		assertEquals(compressedStream.length, ss._getCompressedContents().length);
		assertEquals(uncompressedStream.length, ss.getContents().length);

		for(int i=0; i<uncompressedStream.length; i++) {
			assertEquals(uncompressedStream[i], ss.getContents()[i]);
		}
	}

	public void testUncompressedStream() {
		// Create a fake pointer
		Pointer ptr = new TestPointer(false, 0, uncompressedStream.length, -1, (short)-1);
		// Now the stream
		Stream stream = Stream.createStream(ptr, uncompressedStream, null, null);

		// Check
		assertNotNull(stream.getPointer());
		assertNotNull(stream.getStore());
		assertTrue(stream.getStore() instanceof StreamStore);
		assertFalse(stream.getStore() instanceof CompressedStreamStore);
		assertTrue(stream instanceof UnknownStream);
	}
}
