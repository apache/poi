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

import org.apache.poi.hsmf.datatypes.Types.MAPIType;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.DocumentNode;
import org.apache.poi.util.IOUtils;

/**
 * A Chunk that either acts as {@link ByteChunk} (if not initialized with a node) or
 * lazy loads its binary data from the document (if linked with a node via {@link #readValue(DocumentNode)}).
 */
public class ByteChunkDeferred extends ByteChunk {

    private DocumentNode node;

    /**
     * Creates a Byte Stream Chunk, with the specified type.
     */
    public ByteChunkDeferred(String namePrefix, int chunkId, MAPIType type) {
        super(namePrefix, chunkId, type);
    }

    /**
     * Links the chunk to a document
     * @param node the document node
     */
    public void readValue(DocumentNode node) {
        this.node = node;
    }

    public void readValue(InputStream value) throws IOException {
        if (node == null) {
            super.readValue(value);
        }
    }

    @Override
    public void writeValue(OutputStream out) throws IOException {
        if (node == null) {
            super.writeValue(out);
            return;
        }

        try (DocumentInputStream dis = createDocumentInputStream()) {
            IOUtils.copy(dis, out);
        }
    }

    /**
     * Get bytes directly.
     */
    public byte[] getValue() {
        if (node == null) {
            return super.getValue();
        }

        try (DocumentInputStream dis = createDocumentInputStream()) {
            return IOUtils.toByteArray(dis, node.getSize());
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Set bytes directly.
     * <p>
     * updating the linked document node/msg file directly would be unexpected,
     * so we remove the link and act as a ByteChunk from then
     */
    public void setValue(byte[] value) {
        node = null;
        super.setValue(value);
    }

    private DocumentInputStream createDocumentInputStream() throws IOException {
        return ((DirectoryNode) node.getParent()).createDocumentInputStream(node);
    }
}
