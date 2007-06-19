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

/**
 * Factor class to create the appropriate chunks, which
 *  needs the version of the file to process the chunk header
 *  and trailer areas.
 * Makes use of chunks_parse_cmds.tbl from vsdump to be able
 *  to c
 */
public class ChunkFactory {
	private int version;
	public ChunkFactory(int version) {
		this.version = version;
	}
	public int getVersion() { return version; }

	/**
	 * Creates the appropriate chunk at the given location.
	 * @param data
	 * @param offset
	 * @return
	 */
	public Chunk createChunk(byte[] data, int offset) {
		// Create the header
		ChunkHeader header = 
			ChunkHeader.createChunkHeader(version, data, offset);
		
		// Create the trailer and separator, if required
		ChunkTrailer trailer = null;
		ChunkSeparator separator = null;
		if(header.hasTrailer()) {
			trailer = new ChunkTrailer(
					data, header.getLength() + header.getSizeInBytes());
			if(header.hasSeparator()) {
				separator = new ChunkSeparator(
					data, header.getLength() + header.getSizeInBytes() + 8);
			}
		}
		
		// Now, create the chunk
		byte[] contents = new byte[header.getLength()];
		System.arraycopy(data, offset+header.getSizeInBytes(), contents, 0, contents.length);
		Chunk chunk = new Chunk(header, trailer, separator, contents);
		
		// Feed in the stuff from  chunks_parse_cmds.tbl
		// TODO
		
		// All done
		return chunk;
	}
}
