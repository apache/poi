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

package org.apache.poi.hsmf.model;

import org.apache.poi.hsmf.datatypes.Chunk;
import org.apache.poi.hsmf.datatypes.Chunks;
import org.apache.poi.hsmf.datatypes.StringChunk;

import junit.framework.TestCase;

/**
 * Verifies that the Chunks class is actually setup properly and hasn't been changed in ways
 * that will break the library.
 *
 * @author Travis Ferguson
 *
 */
public final class TestChunkData extends TestCase {
	private Chunks chunks = Chunks.getInstance(false);

	public void testChunkCreate() {
		StringChunk chunk = new StringChunk(0x0200, false);
		TestCase.assertEquals("__substg1.0_0200001E", chunk.getEntryName());

		/* test the lower and upper limits of the chunk ids */
		chunk = new StringChunk(0x0000, false);
		TestCase.assertEquals("__substg1.0_0000001E", chunk.getEntryName());

		chunk = new StringChunk(0xFFFF, false);
		TestCase.assertEquals("__substg1.0_FFFF001E", chunk.getEntryName());

		chunk = new StringChunk(0xFFFF, true);
		TestCase.assertEquals("__substg1.0_FFFF001F", chunk.getEntryName());
	}

	public void testTextBodyChunk() {
		StringChunk chunk = new StringChunk(0x1000, false);
		TestCase.assertEquals(chunk.getEntryName(), chunks.textBodyChunk.getEntryName());
	}

	public void testDisplayToChunk() {
		StringChunk chunk = new StringChunk(0x0E04, false);
		TestCase.assertEquals(chunk.getEntryName(), chunks.displayToChunk.getEntryName());
	}


	public void testDisplayCCChunk() {
		StringChunk chunk = new StringChunk(0x0E03, false);
		TestCase.assertEquals(chunk.getEntryName(), chunks.displayCCChunk.getEntryName());
	}

	public void testDisplayBCCChunk() {
		StringChunk chunk = new StringChunk(0x0E02, false);
		TestCase.assertEquals(chunk.getEntryName(), chunks.displayBCCChunk.getEntryName());
	}

	public void testSubjectChunk() {
		Chunk chunk = new StringChunk(0x0037, false);
		TestCase.assertEquals(chunk.getEntryName(), chunks.subjectChunk.getEntryName());
	}

}
