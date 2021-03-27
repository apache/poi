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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.apache.poi.hdgf.chunks.ChunkFactory.CommandDefinition;
import org.apache.poi.poifs.storage.RawDataUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public final class TestChunks {
	private static byte[] data_a, data_b;

	@BeforeAll
	public static void setup() throws IOException {
		data_a = RawDataUtil.decompress(
			"H4sIAAAAAAAAAHNjYGD4DwRMQNqFAQygFAMTWAIbYIBqQqZRARMSOwNKMwOxChAzMoRIACkeNC3MUAwDjEjGTEISb" +
			"wGLh3pCeCfsoYwD9vbojP1QqQ/2cAYLplNBIACV+8EeuzKE2/4DXaoAZm6HOhUE/CFOU1BwgCnEw+DgcIQxHXGrYv" +
			"zMD6JOMCACwwNiC9SNF+zxMFC988GeEepUdrg/+MHMVKgnQFiGAR5F6KEFU4IMmpHYXBCXsUIdCQUApUvwomMCAAA=");
		data_b = RawDataUtil.decompress(
			"H4sIAAAAAAAAAHNjYGD4DwTMQNqFAQygFAMTWAIbYIBqQqZRATMSOwNuHgODAhAzMoRIACkONC1MUAwDjFB6EpJYC" +
			"1hNqD2Ep+gAZajBGAfsYYz9nhDGB3s4A9OVYBCAysWpDu4uYFixKICZJ5Cc6YHitAv2eBioFn2wZwQZwsjIwA63gR" +
			"/MTIUaD8IyDPCAY0F3EJIrYKAZic0FcRkrkPKDC55kQIR2G9iAAJAZNlDMii8EaAoA66WHVpECAAA=");
	}

    @Test
	void testChunkHeaderA() {
		ChunkHeader h = ChunkHeader.createChunkHeader(11, data_a, 0);

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

    @Test
    void testChunkHeaderB() {
		ChunkHeader h = ChunkHeader.createChunkHeader(11, data_b, 0);

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

    @Test
    void testOneChunk() throws Exception {
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
		assertEquals(2, chunk.getCommandDefinitions().length);
		assertEquals(0, chunk.getCommands().length);

		assertEquals(10, chunk.getCommandDefinitions()[0].getType());
		assertEquals(0, chunk.getCommandDefinitions()[0].getOffset());
		assertEquals("PageSheet", chunk.getCommandDefinitions()[0].getName());

		assertEquals(18, chunk.getCommandDefinitions()[1].getType());
		assertEquals(0, chunk.getCommandDefinitions()[1].getOffset());
		assertEquals("0", chunk.getCommandDefinitions()[1].getName());
	}

    @Test
    void testAnotherChunk() throws Exception {
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
		final CommandDefinition[] cdef = chunk.getCommandDefinitions();
		assertEquals(2, cdef.length);
		assertEquals(0, chunk.getCommands().length);

		assertEquals(10, cdef[0].getType());
		assertEquals(0, cdef[0].getOffset());
		assertEquals("PropList", cdef[0].getName());

		assertEquals(18, cdef[1].getType());
		assertEquals(0, cdef[1].getOffset());
		assertEquals("0", cdef[1].getName());
	}

    @Test
    void testManyChunks() throws Exception {
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
	}
}
