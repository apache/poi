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
package org.apache.poi.hsmf.datatypes;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.poi.hsmf.MAPIMessage;
import org.apache.poi.poifs.filesystem.DirectoryNode;

/**
 * A Chunk that is just a placeholder in the
 *  MAPIMessage directory structure, which
 *  contains children.
 * This is most commonly used with nested
 *  MAPIMessages
 */
public class DirectoryChunk extends Chunk {
    private DirectoryNode dir;
    
    public DirectoryChunk(DirectoryNode dir, String namePrefix, int chunkId, int type) {
        super(namePrefix, chunkId, type);
        this.dir = dir;
    }
    
    /**
     * Returns the directory entry for this chunk.
     * You can then use standard POIFS methods to
     *  enumerate the entries in it.
     */
    public DirectoryNode getDirectory() {
        return dir;
    }
    
    /**
     * Treats the directory as an embeded MAPIMessage
     *  (it normally is one), and returns a MAPIMessage
     *  object to process it with.
     */
    public MAPIMessage getAsEmbededMessage() throws IOException {
        return new MAPIMessage(dir, dir.getFileSystem());
    }

    @Override
    public void readValue(InputStream value) {
        // DirectoryChunks have 0 byte contents
    }

    @Override
    public void writeValue(OutputStream out) {
        // DirectoryChunks have 0 byte contents
    }
}
