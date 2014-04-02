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

package org.apache.poi.hdgf.chunks;

import junit.framework.TestCase;

public final class TestChunks extends TestCase {
public static final byte[] data_a = new byte[] { 70, 0, 0, 0,
	-1, -1, -1, -1, 2, 0, 0, 0, 68, 0, 0, 0, 0, 0, 0, 68, 0, 0, 0, 0, 0,
	0, 0, 2, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
	-1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 0, 0, 0, -1, -1, -1, -1, 0, 0, 0,
	0, -1, -1, -1, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
	2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 104, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0,
	0, 36, 0, 0, 0, 1, 0, 84, 24, 0, 0, 0, 12, 0, 0, 0, 0, 0, 0, 0, 0, 0,
	0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0,
	0, 0, 2, 0, 0, 0, 0, 0, 0, 0, -110, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
	-124, 0, 0, 0, 2, 0, 85, 73, 0, 0, 0, 0, 0, 0, -56, 63, 73, 0, 0, 0,
	0, 0, 0, -64, 63, 63, 0, 0, 0, 0, 0, 0, -64, 63, 63, 0, 0, 0, 0, 0, 0,
	-64, -65, 73, 0, 0, 0, 0, 0, 0, -16, 63, 73, 0, 0, 0, 0, 0, 0, -16, 63,
	4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 80,
	0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -16, 63, 0, 0, 0, 0, 0, 0, 0,
	0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
	1, -1, 3, 0, 0, 32, 0, 0, 0, 0, 0, -73, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0,
	79, 0, 0, 0, 2, 0, 85, 32, 32, 64, 0, 0, 0, 0, 0, 0, 0, 0, 64, 0, 0, 0,
	0, 0, 0, 0, 0, 64, 0, 0, 0, 0, 0, 0, 0, 0, 64, 0, 0, 0, 0, 0, 0, 0, 0,
	8, 8, 65, 0, 0, 0, 0, 0, 0, 0, 0, 65, 0, 0, 0, 0, 0, 0, 0, 0, 64, 0, 0,
	0, 0, 0, 0, 0, 0, 64, 0, 0, 0, 0, 0, 0, 0, 0, 1, -13, 15, 0, 0, 0, 0,
	-56, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 72, 0, 0, 0, 2, 0, 85, 63, 0, 0,
	0, 0, 0, 0, -48, 63, 63, 0, 0, 0, 0, 0, 0, -48, 63, 63, 0, 0, 0, 0, 0,
	0, -48, 63, 63, 0, 0, 0, 0, 0, 0, -48, 63, 0, 0, 0, 0, 0, 0, -16, 63,
	0, 0, 0, 0, 0, 0, -16, 63, 1, 0, 1, 0, 0, 0, 0, 0, 7, 0, 0, 0, 0, 0, 0,
	0, 1, -1, 15, 7, 0, 0, 0, 0, 101, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 28,
	0, 0, 0, 1, 0, 84, 24, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
	0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
	-125, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 10, 0, 0, 0, 2, 0, 85, 5, 0, 0,
	0, 72, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
};
public static final byte[] data_b = new byte[] { 70, 0, 0, 0,
	-1, -1, -1, -1, 3, 0, 0, 0, 68, 0, 0, 0, 0, 0, 0, 68, 0, 0, 0, 0, 0,
	0, 0, 2, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
	-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 	0, 0, 0, -1, -1, -1, -1,
	0, 0, 0, 0, -1, -1, -1, -1, 0, 0, 0, 0, 0, 0, 	0, 0, 0, 0, 0, 0, 0, 0,
	0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 104, 0, 0, 0, 0, 0, 0,
	0, 2, 0, 0, 0, 32, 0, 0, 0, 1, 0, 84, 24, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0,
	0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
	0, 1, 0, 0, 0, 0, 0, 0, 0, -110, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -124,
	0, 0, 0, 2, 0, 85, 63, 0, 0, 0, 0, 0, 0, 33, 64, 63, 0, 0, 0, 0, 0, 0,
	38, 64, 63, 0, 0, 0, 0, 0, 0, -64, 63, 63, 0, 0, 0, 0, 0, 0, -64, -65,
	73, 0, 0, 0, 0, 0, 0, -16, 63, 73, 0, 0, 0, 0, 0, 0, -16, 63, 0, 0, 0,
	0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 80, 0, 0, 0,
	0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -16, 63, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
	0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, -1, 3,
	0, 4, 32, 0, 0, 0, 0, 0, -56, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 72, 0, 0,
	0, 2, 0, 85, 63, 0, 0, 0, 0, 0, 0, -48, 63, 63, 0, 0, 0, 0, 0, 0, -48,
	63, 63, 0, 0, 0, 0, 0, 0, -48, 63, 63, 0, 0, 0, 0, 0, 0, -48, 63, 0, 0,
	0, 0, 0, 0, -16, 63, 0, 0, 0, 0, 0, 0, -16, 63, 1, 0, 1, 0, 0, 1, 1, 0,
	7, 0, 0, 0, 0, 0, 0, 0, 1, -1, 15, 7, 0, 0, 0, 0, 101, 0, 0, 0, 1, 0, 0,
	0, 1, 0, 0, 0, 28, 0, 0, 0, 1, 0, 84, 24, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0,
	0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
	0, 0, 0, 0, 0, -125, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 10, 0, 0, 0, 2, 0,
	85, 5, 0, 0, 0, 78, 0, 0, 0, 0, 0, 0, 0, 0, 0, -55, 0, 0, 0, 2, 0, 0, 0,
	0, 0, 0, 0, -122, 0, 0, 0, 1, 0, 80, 1, 0, 0, 0, 60, 0, 0, 0, 60, 0, 0,
	0, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
	0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
	0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
	0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
	0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
	0, 0, 0
};

	public void testChunkHeaderA() throws Exception {
		ChunkFactory cf = new ChunkFactory(11);
		ChunkHeader h =
			ChunkHeader.createChunkHeader(11, data_a, 0);

		assertTrue(h instanceof ChunkHeaderV11);
		ChunkHeaderV11 header = (ChunkHeaderV11)h;

		assertEquals(70, header.getType());
		assertEquals(-1, header.getId());
		assertEquals(2, header.getUnknown1());
		assertEquals(68, header.getLength());
		assertEquals(0, header.getUnknown2());
		assertEquals(0, header.getUnknown3());

		assertTrue(header.hasTrailer());
		assertTrue(header.hasSeparator());
	}
	public void testChunkHeaderB() throws Exception {
		ChunkFactory cf = new ChunkFactory(11);
		ChunkHeader h =
			ChunkHeader.createChunkHeader(11, data_b, 0);

		assertTrue(h instanceof ChunkHeaderV11);
		ChunkHeaderV11 header = (ChunkHeaderV11)h;

		assertEquals(70, header.getType());
		assertEquals(-1, header.getId());
		assertEquals(3, header.getUnknown1());
		assertEquals(68, header.getLength());
		assertEquals(0, header.getUnknown2());
		assertEquals(0, header.getUnknown3());

		assertTrue(header.hasTrailer());
		assertTrue(header.hasSeparator());
	}

	public void testOneChunk() throws Exception {
		ChunkFactory cf = new ChunkFactory(11);
		cf.createChunk(data_a, 0);
		cf.createChunk(data_b, 0);

		Chunk chunk = cf.createChunk(data_a, 0);
		assertNotNull(chunk.getHeader());
		assertNotNull(chunk.getTrailer());
		assertNotNull(chunk.getSeparator());

		// Should be 19 + length + 8 + 4 big
		assertEquals(68, chunk.getHeader().getLength());
		assertEquals(68+19+8+4, chunk.getOnDiskSize());

		// Type is 70, or 0x46
		assertEquals(70, chunk.getHeader().getType());
		assertEquals(0x46, chunk.getHeader().getType());

		// Should have two virtual chunk commands, a
		//  10 (page sheet) and an 18
		assertEquals(2, chunk.commandDefinitions.length);
		assertEquals(0, chunk.getCommands().length);

		assertEquals(10, chunk.commandDefinitions[0].getType());
		assertEquals(0, chunk.commandDefinitions[0].getOffset());
		assertEquals("PageSheet", chunk.commandDefinitions[0].getName());

		assertEquals(18, chunk.commandDefinitions[1].getType());
		assertEquals(0, chunk.commandDefinitions[1].getOffset());
		assertEquals("0", chunk.commandDefinitions[1].getName());
	}

	public void testAnotherChunk() throws Exception {
		ChunkFactory cf = new ChunkFactory(11);

		// Go for the 2nd chunk in the stream
		int offset = 0;
		Chunk chunk = cf.createChunk(data_b, offset);
		offset += chunk.getOnDiskSize();
		chunk = cf.createChunk(data_b, offset);

		assertNotNull(chunk.getHeader());
		assertNotNull(chunk.getTrailer());
		assertNotNull(chunk.getSeparator());

		// Should be 19 + length + 8 + 4 big
		assertEquals(32, chunk.getHeader().getLength());
		assertEquals(32+19+8+4, chunk.getOnDiskSize());

		// Type is 104, or 0x68
		assertEquals(104, chunk.getHeader().getType());
		assertEquals(0x68, chunk.getHeader().getType());

		// Should have two virtual chunk commands, a
		//  10 (Unknown) and an 18
		assertEquals(2, chunk.commandDefinitions.length);
		assertEquals(0, chunk.getCommands().length);

		assertEquals(10, chunk.commandDefinitions[0].getType());
		assertEquals(0, chunk.commandDefinitions[0].getOffset());
		assertEquals("PropList", chunk.commandDefinitions[0].getName());

		assertEquals(18, chunk.commandDefinitions[1].getType());
		assertEquals(0, chunk.commandDefinitions[1].getOffset());
		assertEquals("0", chunk.commandDefinitions[1].getName());
	}

	public void testManyChunks() throws Exception {
		ChunkFactory cf = new ChunkFactory(11);
		Chunk chunk;
		int offset = 0;

		chunk = cf.createChunk(data_a, offset);
		assertNotNull(chunk.getHeader());
		assertNotNull(chunk.getTrailer());
		assertNotNull(chunk.getSeparator());
		offset += chunk.getOnDiskSize();

		chunk = cf.createChunk(data_a, offset);
		assertNotNull(chunk.getHeader());
		assertNotNull(chunk.getTrailer());
		assertNotNull(chunk.getSeparator());
		offset += chunk.getOnDiskSize();

		// Has a separator but no trailer
		chunk = cf.createChunk(data_a, offset);
		assertNotNull(chunk.getHeader());
		assertNull(chunk.getTrailer());
		assertNotNull(chunk.getSeparator());
		offset += chunk.getOnDiskSize();

		chunk = cf.createChunk(data_a, offset);
		assertNotNull(chunk.getHeader());
		assertNull(chunk.getTrailer());
		assertNotNull(chunk.getSeparator());
		offset += chunk.getOnDiskSize();

		chunk = cf.createChunk(data_a, offset);
		assertNotNull(chunk.getHeader());
		assertNull(chunk.getTrailer());
		assertNotNull(chunk.getSeparator());
		offset += chunk.getOnDiskSize();
	}
}
