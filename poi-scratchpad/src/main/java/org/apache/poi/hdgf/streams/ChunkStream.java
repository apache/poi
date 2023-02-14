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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hdgf.chunks.Chunk;
import org.apache.poi.hdgf.chunks.ChunkFactory;
import org.apache.poi.hdgf.chunks.ChunkHeader;
import org.apache.poi.hdgf.pointers.Pointer;

import static org.apache.logging.log4j.util.Unbox.box;

public final class ChunkStream extends Stream {
    private static final Logger LOG = LogManager.getLogger(ChunkStream.class);

    private final ChunkFactory chunkFactory;
    /** All the Chunks we contain */
    private Chunk[] chunks;

    ChunkStream(Pointer pointer, StreamStore store, ChunkFactory chunkFactory) {
        super(pointer, store);
        this.chunkFactory = chunkFactory;

        // For compressed stores, we require all of the data
        store.copyBlockHeaderToContents();
    }

    public Chunk[] getChunks() { return chunks; }

    /**
     * Process the contents of the stream out into chunks
     */
    public void findChunks() {
        ArrayList<Chunk> chunksA = new ArrayList<>();

        if(getPointer().getOffset() == 0x64b3) {
            int i = 0;
            i++;
        }

        int pos = 0;
        byte[] contents = getStore().getContents();
        try {
            while(pos < contents.length) {
                // Ensure we have enough data to create a chunk from
                int headerSize = ChunkHeader.getHeaderSize(chunkFactory.getVersion());
                if(pos+headerSize <= contents.length) {
                    Chunk chunk = chunkFactory.createChunk(contents, pos);
                    chunksA.add(chunk);

                    pos += chunk.getOnDiskSize();
                } else {
                    LOG.atWarn().log("Needed {} bytes to create the next chunk header, but only found {} bytes, ignoring rest of data", box(headerSize),box(contents.length - pos));
                    pos = contents.length;
                }
            }
        }
        catch (Exception e)
        {
            LOG.atError().withThrowable(e).log("Failed to create chunk at {}, ignoring rest of data.", box(pos));
        }

        chunks = chunksA.toArray(new Chunk[0]);
    }
}
