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
		int endOfDataPos = offset + header.getLength() + header.getSizeInBytes();
		
		// Check we have enough data, and tweak the header size
		//  as required
		if(endOfDataPos > data.length) {
			System.err.println("Header called for " + header.getLength() +" bytes, but that would take us passed the end of the data!");
			
			endOfDataPos = data.length;
			header.length = data.length - offset - header.getSizeInBytes();
			
			if(header.hasTrailer()) {
				header.length -= 8;
				endOfDataPos  -= 8;
			}
			if(header.hasSeparator()) {
				header.length -= 4;
				endOfDataPos  -= 4;
			}
		}
		

		// Create the trailer and separator, if required
		ChunkTrailer trailer = null;
		ChunkSeparator separator = null;
		if(header.hasTrailer()) {
			if(endOfDataPos <= data.length-8) {
				trailer = new ChunkTrailer(
					data, endOfDataPos);
				endOfDataPos += 8;
			} else {
				System.err.println("Header claims a length to " + endOfDataPos + " there's then no space for the trailer in the data (" + data.length + ")");
			}
		}
		if(header.hasSeparator()) {
			if(endOfDataPos <= data.length-4) {
				separator = new ChunkSeparator(
						data, endOfDataPos);
			} else {
				System.err.println("Header claims a length to " + endOfDataPos + " there's then no space for the separator in the data (" + data.length + ")");
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
