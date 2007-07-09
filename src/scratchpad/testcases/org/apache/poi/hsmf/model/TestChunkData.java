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
public class TestChunkData extends TestCase {
	public void testChunkCreate() {
		StringChunk chunk = new StringChunk(0x0200);
		TestCase.assertEquals("__substg1.0_0200001E", chunk.getEntryName());
		
		/* test the lower and upper limits of the chunk ids */
		chunk = new StringChunk(0x0000);
		TestCase.assertEquals("__substg1.0_0000001E", chunk.getEntryName());
		
		chunk = new StringChunk(0xFFFF);
		TestCase.assertEquals("__substg1.0_FFFF001E", chunk.getEntryName());
	}
	
	public void testTextBodyChunk() {
		StringChunk chunk = new StringChunk(0x1000);
		TestCase.assertEquals(chunk.getEntryName(), Chunks.getInstance().textBodyChunk.getEntryName());
	}

	public void testDisplayToChunk() {
		StringChunk chunk = new StringChunk(0x0E04);
		TestCase.assertEquals(chunk.getEntryName(), Chunks.getInstance().displayToChunk.getEntryName());
	}
	

	public void testDisplayCCChunk() {
		StringChunk chunk = new StringChunk(0x0E03);
		TestCase.assertEquals(chunk.getEntryName(), Chunks.getInstance().displayCCChunk.getEntryName());
	}

	public void testDisplayBCCChunk() {
		StringChunk chunk = new StringChunk(0x0E02);
		TestCase.assertEquals(chunk.getEntryName(), Chunks.getInstance().displayBCCChunk.getEntryName());
	}
	
	public void testSubjectChunk() {
		Chunk chunk = new StringChunk(0x0037);
		TestCase.assertEquals(chunk.getEntryName(), Chunks.getInstance().subjectChunk.getEntryName());
	}
	
}
