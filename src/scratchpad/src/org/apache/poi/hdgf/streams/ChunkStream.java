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

import java.util.ArrayList;

import org.apache.poi.hdgf.chunks.Chunk;
import org.apache.poi.hdgf.chunks.ChunkFactory;
import org.apache.poi.hdgf.pointers.Pointer;

public class ChunkStream extends Stream {
	private ChunkFactory chunkFactory;
	/** All the Chunks we contain */
	private Chunk[] chunks;
	
	protected ChunkStream(Pointer pointer, StreamStore store, ChunkFactory chunkFactory) {
		super(pointer, store);
		this.chunkFactory = chunkFactory;
	}
	
	public Chunk[] getChunks() { return chunks; }
	
	/**
	 * Process the contents of the stream out into chunks
	 */
	public void findChunks() {
		ArrayList chunksA = new ArrayList();
		
		int pos = 0;
		byte[] contents = getStore().getContents();
		while(pos < contents.length) {
			Chunk chunk = chunkFactory.createChunk(contents, pos);
			chunksA.add(chunk);
			
			pos += chunk.getOnDiskSize();
		}
		
		chunks = (Chunk[])chunksA.toArray(new Chunk[chunksA.size()]);
	}
}
